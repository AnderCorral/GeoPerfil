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

public class MainActivity extends AppCompatActivity {

    private EditText cajaEmail;
    private EditText cajaPassword;
    private Button botonLogin;
    private Button botonRegistro;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        cajaEmail = findViewById(R.id.cajaEmailLogin);
        cajaPassword = findViewById(R.id.cajaPasswordLogin);
        botonLogin = findViewById(R.id.botonLogin);
        botonRegistro = findViewById(R.id.botonIrRegistro);

        botonLogin.setOnClickListener(v -> hacerLogin());

        botonRegistro.setOnClickListener(v -> {
            Intent i = new Intent(this, RegistroActivity.class);
            startActivity(i);
        });
    }

    private void hacerLogin() {
        String email = cajaEmail.getText().toString().trim();
        String password = cajaPassword.getText().toString().trim();

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Debes rellenar email y contraseña", Toast.LENGTH_SHORT).show();
            return;
        }

        Data datos = new Data.Builder()
                .putString("accion", "login")
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
                                Intent i = new Intent(MainActivity.this, MenuActivity.class);
                                i.putExtra("idUsuario", workInfo.getOutputData().getString("id"));
                                i.putExtra("nombre", workInfo.getOutputData().getString("nombre"));
                                i.putExtra("email", workInfo.getOutputData().getString("email"));
                                i.putExtra("foto", workInfo.getOutputData().getString("foto"));
                                startActivity(i);
                                finish();
                            } else {
                                Toast.makeText(MainActivity.this, mensaje, Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                });

        WorkManager.getInstance(this).enqueue(otwr);
    }
}