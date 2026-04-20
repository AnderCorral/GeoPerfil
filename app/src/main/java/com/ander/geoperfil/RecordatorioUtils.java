package com.ander.geoperfil;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;

import java.util.Calendar;

public class RecordatorioUtils {

    public static final String PREFS_RECORDATORIO = "recordatorio_geoperfil";

    public static PendingIntent crearPendingIntentRecordatorio(Context context) {
        Intent intentBC = new Intent(context, RecordatorioReceiver.class);
        intentBC.setAction("com.ander.geoperfil.RECORDATORIO_DIARIO");

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return PendingIntent.getBroadcast(
                    context,
                    1,
                    intentBC,
                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
            );
        } else {
            return PendingIntent.getBroadcast(
                    context,
                    1,
                    intentBC,
                    PendingIntent.FLAG_UPDATE_CURRENT
            );
        }
    }

    public static void activarRecordatorioDiario(Context context) {
        Calendar calendario = Calendar.getInstance();
        calendario.set(Calendar.HOUR_OF_DAY, 20);
        calendario.set(Calendar.MINUTE, 0);
        calendario.set(Calendar.SECOND, 0);
        calendario.set(Calendar.MILLISECOND, 0);

        if (calendario.getTimeInMillis() <= System.currentTimeMillis()) {
            calendario.add(Calendar.DAY_OF_MONTH, 1);
        }

        PendingIntent ibc = crearPendingIntentRecordatorio(context);

        AlarmManager gestor = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (gestor != null) {
            gestor.setInexactRepeating(
                    AlarmManager.RTC_WAKEUP,
                    calendario.getTimeInMillis(),
                    AlarmManager.INTERVAL_DAY,
                    ibc
            );
        }

        SharedPreferences prefs = context.getSharedPreferences(PREFS_RECORDATORIO, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean("activo", true);
        editor.apply();
    }

    public static void desactivarRecordatorioDiario(Context context) {
        PendingIntent ibc = crearPendingIntentRecordatorio(context);

        AlarmManager gestor = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (gestor != null) {
            gestor.cancel(ibc);
        }

        SharedPreferences prefs = context.getSharedPreferences(PREFS_RECORDATORIO, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean("activo", false);
        editor.apply();
    }

    public static boolean estaActivo(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_RECORDATORIO, Context.MODE_PRIVATE);
        return prefs.getBoolean("activo", false);
    }
}