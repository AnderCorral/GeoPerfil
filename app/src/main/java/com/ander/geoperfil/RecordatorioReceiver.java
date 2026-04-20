package com.ander.geoperfil;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;

public class RecordatorioReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (!"com.ander.geoperfil.RECORDATORIO_DIARIO".equals(intent.getAction())) {
            return;
        }

        NotificationManager elManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel elCanal = new NotificationChannel(
                    "IdCanalRecordatorio",
                    "Recordatorios GeoPerfil",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            elCanal.setDescription("Canal para los recordatorios diarios de GeoPerfil");
            elCanal.enableVibration(true);
            elCanal.setVibrationPattern(new long[]{0, 1000, 500, 1000});

            if (elManager != null) {
                elManager.createNotificationChannel(elCanal);
            }
        }

        Intent i = new Intent(context, MapaActivity.class);
        PendingIntent intentEnNot;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            intentEnNot = PendingIntent.getActivity(
                    context,
                    0,
                    i,
                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
            );
        } else {
            intentEnNot = PendingIntent.getActivity(
                    context,
                    0,
                    i,
                    PendingIntent.FLAG_UPDATE_CURRENT
            );
        }

        NotificationCompat.Builder elBuilder =
                new NotificationCompat.Builder(context, "IdCanalRecordatorio")
                        .setSmallIcon(android.R.drawable.stat_sys_warning)
                        .setContentTitle("GeoPerfil")
                        .setContentText("Recuerda actualizar tu ubicación de hoy.")
                        .setSubText("Pulsa para abrir el mapa")
                        .setVibrate(new long[]{0, 1000, 500, 1000})
                        .setAutoCancel(true)
                        .setContentIntent(intentEnNot);

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
                ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS)
                        == PackageManager.PERMISSION_GRANTED) {

            if (elManager != null) {
                elManager.notify(100, elBuilder.build());
            }
        }
    }
}