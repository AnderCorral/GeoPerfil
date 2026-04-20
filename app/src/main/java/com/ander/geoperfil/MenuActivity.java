package com.ander.geoperfil;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class MenuActivity extends AppCompatActivity {

    private static final int CODIGO_PERFIL = 300;
    private static final int CODIGO_PERMISO_NOTIFICACIONES = 400;

    private String idUsuario;
    private String nombre;
    private String email;
    private String foto;

    private TextView etiquetaBienvenida;
    private Button botonMapa;
    private Button botonPerfil;
    private Button botonSalir;
    private Button botonActivarRecordatorio;
    private Button botonDesactivarRecordatorio;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);

        etiquetaBienvenida = findViewById(R.id.etiquetaBienvenida);
        botonMapa = findViewById(R.id.botonMapa);
        botonPerfil = findViewById(R.id.botonPerfil);
        botonSalir = findViewById(R.id.botonSalir);
        botonActivarRecordatorio = findViewById(R.id.botonActivarRecordatorio);
        botonDesactivarRecordatorio = findViewById(R.id.botonDesactivarRecordatorio);

        idUsuario = getIntent().getStringExtra("idUsuario");
        nombre = getIntent().getStringExtra("nombre");
        email = getIntent().getStringExtra("email");
        foto = getIntent().getStringExtra("foto");

        etiquetaBienvenida.setText("Bienvenido, " + nombre);

        guardarNombreEnPreferencias();
        GeoPerfilWidget.actualizarTodosLosWidgets(this);

        pedirPermisoNotificacionesSiHaceFalta();

        botonMapa.setOnClickListener(v -> {
            Intent i = new Intent(this, MapaActivity.class);
            i.putExtra("idUsuario", idUsuario);
            i.putExtra("nombre", nombre);
            i.putExtra("email", email);
            i.putExtra("foto", foto);
            startActivity(i);
        });

        botonPerfil.setOnClickListener(v -> {
            Intent i = new Intent(this, PerfilActivity.class);
            i.putExtra("idUsuario", idUsuario);
            i.putExtra("nombre", nombre);
            i.putExtra("email", email);
            i.putExtra("foto", foto);
            startActivityForResult(i, CODIGO_PERFIL);
        });

        botonActivarRecordatorio.setOnClickListener(v -> {
            RecordatorioUtils.activarRecordatorioDiario(this);
            Toast.makeText(this, "Recordatorio diario activado para las 20:00", Toast.LENGTH_SHORT).show();
        });

        botonDesactivarRecordatorio.setOnClickListener(v -> {
            RecordatorioUtils.desactivarRecordatorioDiario(this);
            Toast.makeText(this, "Recordatorio diario desactivado", Toast.LENGTH_SHORT).show();
        });

        botonSalir.setOnClickListener(v -> {
            Intent i = new Intent(this, MainActivity.class);
            startActivity(i);
            finish();
        });
    }

    private void pedirPermisoNotificacionesSiHaceFalta() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(
                        this,
                        new String[]{Manifest.permission.POST_NOTIFICATIONS},
                        CODIGO_PERMISO_NOTIFICACIONES
                );
            }
        }
    }

    private void guardarNombreEnPreferencias() {
        SharedPreferences prefs = getSharedPreferences(GeoPerfilWidget.PREFS_WIDGET, MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("nombre", nombre);
        editor.apply();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CODIGO_PERFIL && resultCode == RESULT_OK && data != null) {
            String nuevaFoto = data.getStringExtra("foto");
            String nuevoNombre = data.getStringExtra("nombre");
            String nuevoEmail = data.getStringExtra("email");

            if (nuevaFoto != null) {
                foto = nuevaFoto;
            }

            if (nuevoNombre != null) {
                nombre = nuevoNombre;
                etiquetaBienvenida.setText("Bienvenido, " + nombre);
            }

            if (nuevoEmail != null) {
                email = nuevoEmail;
            }

            guardarNombreEnPreferencias();
            GeoPerfilWidget.actualizarTodosLosWidgets(this);
        }
    }
}