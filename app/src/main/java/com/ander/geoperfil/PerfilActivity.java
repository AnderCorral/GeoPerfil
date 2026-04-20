package com.ander.geoperfil;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.lifecycle.Observer;
import androidx.work.Data;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkInfo;
import androidx.work.WorkManager;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class PerfilActivity extends AppCompatActivity {

    private static final int CODIGO_FOTO_ARCHIVO = 200;
    private static final int CODIGO_PERMISO_CAMARA = 201;

    private TextView etiquetaNombre;
    private TextView etiquetaEmail;
    private ImageView imageViewPerfil;
    private Button botonHacerFoto;
    private Button botonSubirFoto;

    private String idUsuario;
    private String nombre;
    private String email;
    private String fotoRemota;

    private File fichImg;
    private Uri uriimagen;
    private String rutaFotoLocal = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_perfil);

        etiquetaNombre = findViewById(R.id.etiquetaNombrePerfil);
        etiquetaEmail = findViewById(R.id.etiquetaEmailPerfil);
        imageViewPerfil = findViewById(R.id.imageViewPerfil);
        botonHacerFoto = findViewById(R.id.botonHacerFoto);
        botonSubirFoto = findViewById(R.id.botonSubirFoto);

        idUsuario = getIntent().getStringExtra("idUsuario");
        nombre = getIntent().getStringExtra("nombre");
        email = getIntent().getStringExtra("email");
        fotoRemota = getIntent().getStringExtra("foto");

        etiquetaNombre.setText("Nombre: " + nombre);
        etiquetaEmail.setText("Email: " + email);

        botonHacerFoto.setOnClickListener(v -> comprobarPermisoCamara());
        botonSubirFoto.setOnClickListener(v -> subirFotoServidor());

        if (fotoRemota != null && !fotoRemota.isEmpty()) {
            descargarFotoRemota();
        }
    }

    private void comprobarPermisoCamara() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED) {
            sacarFoto();
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CAMERA},
                    CODIGO_PERMISO_CAMARA);
        }
    }

    private void sacarFoto() {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String nombrefich = "IMG_" + timeStamp + "_";

        File directorio = this.getFilesDir();

        try {
            fichImg = File.createTempFile(nombrefich, ".jpg", directorio);
            rutaFotoLocal = fichImg.getAbsolutePath();

            uriimagen = FileProvider.getUriForFile(
                    this,
                    "com.ander.geoperfil.provider",
                    fichImg
            );

            Intent elIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            elIntent.putExtra(MediaStore.EXTRA_OUTPUT, uriimagen);
            startActivityForResult(elIntent, CODIGO_FOTO_ARCHIVO);

        } catch (IOException e) {
            Toast.makeText(this, "Error preparando la foto", Toast.LENGTH_SHORT).show();
        }
    }

    private void subirFotoServidor() {
        if (rutaFotoLocal.isEmpty()) {
            Toast.makeText(this, "Primero debes hacer una foto", Toast.LENGTH_SHORT).show();
            return;
        }

        Data datos = new Data.Builder()
                .putString("accion", "subir")
                .putString("idUsuario", idUsuario)
                .putString("rutaLocal", rutaFotoLocal)
                .build();

        OneTimeWorkRequest otwr =
                new OneTimeWorkRequest.Builder(FotoWorker.class)
                        .setInputData(datos)
                        .build();

        WorkManager.getInstance(this).getWorkInfoByIdLiveData(otwr.getId())
                .observe(this, new Observer<WorkInfo>() {
                    @Override
                    public void onChanged(WorkInfo workInfo) {
                        if (workInfo != null && workInfo.getState().isFinished()) {
                            String resultado = workInfo.getOutputData().getString("resultado");
                            String mensaje = workInfo.getOutputData().getString("mensaje");

                            if ("ok".equals(resultado)) {
                                fotoRemota = workInfo.getOutputData().getString("foto");

                                Intent intentResultado = new Intent();
                                intentResultado.putExtra("foto", fotoRemota);
                                intentResultado.putExtra("nombre", nombre);
                                intentResultado.putExtra("email", email);
                                setResult(RESULT_OK, intentResultado);

                                Toast.makeText(PerfilActivity.this, "Foto subida correctamente", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(PerfilActivity.this, mensaje, Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                });

        WorkManager.getInstance(this).enqueue(otwr);
    }

    private void descargarFotoRemota() {
        Data datos = new Data.Builder()
                .putString("accion", "descargar")
                .putString("idUsuario", idUsuario)
                .putString("urlFoto", fotoRemota)
                .build();

        OneTimeWorkRequest otwr =
                new OneTimeWorkRequest.Builder(FotoWorker.class)
                        .setInputData(datos)
                        .build();

        WorkManager.getInstance(this).getWorkInfoByIdLiveData(otwr.getId())
                .observe(this, new Observer<WorkInfo>() {
                    @Override
                    public void onChanged(WorkInfo workInfo) {
                        if (workInfo != null && workInfo.getState().isFinished()) {
                            String resultado = workInfo.getOutputData().getString("resultado");

                            if ("ok".equals(resultado)) {
                                String rutaLocal = workInfo.getOutputData().getString("rutaLocal");
                                imageViewPerfil.setImageURI(Uri.fromFile(new File(rutaLocal)));
                            }
                        }
                    }
                });

        WorkManager.getInstance(this).enqueue(otwr);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CODIGO_FOTO_ARCHIVO && resultCode == RESULT_OK) {
            imageViewPerfil.setImageURI(uriimagen);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == CODIGO_PERMISO_CAMARA) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                sacarFoto();
            } else {
                Toast.makeText(this, "Permiso de cámara denegado", Toast.LENGTH_SHORT).show();
            }
        }
    }
}