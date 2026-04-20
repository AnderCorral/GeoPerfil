package com.ander.geoperfil;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.work.Data;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkInfo;
import androidx.work.WorkManager;

public class RegistroActivity extends AppCompatActivity {

    private EditText cajaNombre;
    private EditText cajaEmail;
    private EditText cajaPassword;
    private Button botonRegistrar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registro);

        cajaNombre = findViewById(R.id.cajaNombreRegistro);
        cajaEmail = findViewById(R.id.cajaEmailRegistro);
        cajaPassword = findViewById(R.id.cajaPasswordRegistro);
        botonRegistrar = findViewById(R.id.botonRegistrar);

        botonRegistrar.setOnClickListener(v -> registrarUsuario());
    }

    private void registrarUsuario() {
        String nombre = cajaNombre.getText().toString().trim();
        String email = cajaEmail.getText().toString().trim();
        String password = cajaPassword.getText().toString().trim();

        if (nombre.isEmpty() || email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Debes rellenar todos los campos", Toast.LENGTH_SHORT).show();
            return;
        }

        Data datos = new Data.Builder()
                .putString("accion", "register")
                .putString("nombre", nombre)
                .putString("email", email)
                .putString("password", password)
                .build();

        OneTimeWorkRequest otwr =
                new OneTimeWorkRequest.Builder(UsuarioWorker.class)
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
                                Toast.makeText(RegistroActivity.this, mensaje, Toast.LENGTH_SHORT).show();

                                Intent i = new Intent(RegistroActivity.this, MenuActivity.class);
                                i.putExtra("idUsuario", workInfo.getOutputData().getString("id"));
                                i.putExtra("nombre", workInfo.getOutputData().getString("nombre"));
                                i.putExtra("email", workInfo.getOutputData().getString("email"));
                                i.putExtra("foto", workInfo.getOutputData().getString("foto"));
                                startActivity(i);
                                finish();
                            } else {
                                Toast.makeText(RegistroActivity.this, mensaje, Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                });

        WorkManager.getInstance(this).enqueue(otwr);
    }
}