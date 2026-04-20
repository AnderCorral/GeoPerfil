package com.ander.geoperfil;

import android.content.Context;
import android.net.Uri;
import android.util.Base64;

import androidx.annotation.NonNull;
import androidx.work.Data;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;

public class FotoWorker extends Worker {

    public FotoWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        String accion = getInputData().getString("accion");

        if ("subir".equals(accion)) {
            return subirFoto();
        } else if ("descargar".equals(accion)) {
            return descargarFoto();
        }

        return Result.success(new Data.Builder()
                .putString("resultado", "error")
                .putString("mensaje", "Acción no válida")
                .build());
    }

    private Result subirFoto() {
        HttpURLConnection urlConnection = null;

        try {
            String idUsuario = getInputData().getString("idUsuario");
            String rutaLocal = getInputData().getString("rutaLocal");

            File fichero = new File(rutaLocal);
            InputStream is = new java.io.FileInputStream(fichero);
            ByteArrayOutputStream stream = new ByteArrayOutputStream();

            byte[] buffer = new byte[4096];
            int leidos;

            while ((leidos = is.read(buffer)) != -1) {
                stream.write(buffer, 0, leidos);
            }

            is.close();

            byte[] fototransformada = stream.toByteArray();
            String fotoen64 = Base64.encodeToString(fototransformada, Base64.DEFAULT);

            URL destino = new URL(Config.URL_IMAGENES);
            urlConnection = (HttpURLConnection) destino.openConnection();
            urlConnection.setConnectTimeout(5000);
            urlConnection.setReadTimeout(5000);
            urlConnection.setRequestMethod("POST");
            urlConnection.setDoOutput(true);
            urlConnection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

            Uri.Builder builder = new Uri.Builder()
                    .appendQueryParameter("accion", "subir")
                    .appendQueryParameter("idUsuario", idUsuario)
                    .appendQueryParameter("imagen", fotoen64);

            String parametros = builder.build().getEncodedQuery();

            PrintWriter out = new PrintWriter(urlConnection.getOutputStream());
            out.print(parametros);
            out.close();

            int statusCode = urlConnection.getResponseCode();

            if (statusCode == 200) {
                BufferedInputStream inputStream = new BufferedInputStream(urlConnection.getInputStream());
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));

                String line;
                String result = "";

                while ((line = bufferedReader.readLine()) != null) {
                    result += line;
                }

                inputStream.close();

                JSONParser parser = new JSONParser();
                JSONObject json = (JSONObject) parser.parse(result);

                String resultado = (String) json.get("resultado");
                String mensaje = (String) json.get("mensaje");
                String foto = json.get("foto") == null ? "" : String.valueOf(json.get("foto"));

                return Result.success(new Data.Builder()
                        .putString("resultado", resultado)
                        .putString("mensaje", mensaje)
                        .putString("foto", foto)
                        .build());
            }

            return Result.success(new Data.Builder()
                    .putString("resultado", "error")
                    .putString("mensaje", "Código HTTP: " + statusCode)
                    .build());

        } catch (Exception e) {
            return Result.success(new Data.Builder()
                    .putString("resultado", "error")
                    .putString("mensaje", e.getMessage())
                    .build());
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
        }
    }

    private Result descargarFoto() {
        HttpURLConnection conn = null;

        try {
            String urlFoto = getInputData().getString("urlFoto");
            String idUsuario = getInputData().getString("idUsuario");

            URL destino = new URL(urlFoto);
            conn = (HttpURLConnection) destino.openConnection();

            int responseCode = conn.getResponseCode();

            if (responseCode == HttpURLConnection.HTTP_OK) {
                InputStream is = conn.getInputStream();

                File ficheroLocal = new File(getApplicationContext().getFilesDir(),
                        "perfil_" + idUsuario + ".jpg");

                FileOutputStream fos = new FileOutputStream(ficheroLocal);

                byte[] buffer = new byte[4096];
                int leidos;

                while ((leidos = is.read(buffer)) != -1) {
                    fos.write(buffer, 0, leidos);
                }

                fos.flush();
                fos.close();
                is.close();

                return Result.success(new Data.Builder()
                        .putString("resultado", "ok")
                        .putString("rutaLocal", ficheroLocal.getAbsolutePath())
                        .build());
            }

            return Result.success(new Data.Builder()
                    .putString("resultado", "error")
                    .putString("mensaje", "Código HTTP: " + responseCode)
                    .build());

        } catch (Exception e) {
            return Result.success(new Data.Builder()
                    .putString("resultado", "error")
                    .putString("mensaje", e.getMessage())
                    .build());
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
    }
}