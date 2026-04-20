package com.ander.geoperfil;

import android.content.Context;
import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.work.Data;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;

public class UsuarioWorker extends Worker {

    public UsuarioWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        HttpURLConnection urlConnection = null;

        try {
            String accion = getInputData().getString("accion");
            String nombre = getInputData().getString("nombre");
            String email = getInputData().getString("email");
            String password = getInputData().getString("password");
            String idUsuario = getInputData().getString("idUsuario");

            URL destino = new URL(Config.URL_USUARIOS);
            urlConnection = (HttpURLConnection) destino.openConnection();
            urlConnection.setConnectTimeout(5000);
            urlConnection.setReadTimeout(5000);
            urlConnection.setRequestMethod("POST");
            urlConnection.setDoOutput(true);
            urlConnection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

            Uri.Builder builder = new Uri.Builder()
                    .appendQueryParameter("accion", accion == null ? "" : accion)
                    .appendQueryParameter("nombre", nombre == null ? "" : nombre)
                    .appendQueryParameter("email", email == null ? "" : email)
                    .appendQueryParameter("password", password == null ? "" : password)
                    .appendQueryParameter("idUsuario", idUsuario == null ? "" : idUsuario);

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

                String resultado = json.get("resultado") == null ? "" : String.valueOf(json.get("resultado"));
                String mensaje = json.get("mensaje") == null ? "" : String.valueOf(json.get("mensaje"));
                String id = json.get("id") == null ? "" : String.valueOf(json.get("id"));
                String nombreRespuesta = json.get("nombre") == null ? "" : String.valueOf(json.get("nombre"));
                String emailRespuesta = json.get("email") == null ? "" : String.valueOf(json.get("email"));
                String foto = json.get("foto") == null ? "" : String.valueOf(json.get("foto"));

                Data outputData = new Data.Builder()
                        .putString("resultado", resultado)
                        .putString("mensaje", mensaje)
                        .putString("id", id)
                        .putString("nombre", nombreRespuesta)
                        .putString("email", emailRespuesta)
                        .putString("foto", foto)
                        .build();

                return Result.success(outputData);
            }

            Data outputData = new Data.Builder()
                    .putString("resultado", "error")
                    .putString("mensaje", "Código HTTP: " + statusCode)
                    .build();

            return Result.success(outputData);

        } catch (Exception e) {
            Data outputData = new Data.Builder()
                    .putString("resultado", "error")
                    .putString("mensaje", e.getMessage())
                    .build();

            return Result.success(outputData);
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
        }
    }
}