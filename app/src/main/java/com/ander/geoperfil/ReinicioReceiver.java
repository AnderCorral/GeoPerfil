package com.ander.geoperfil;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class ReinicioReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
            if (RecordatorioUtils.estaActivo(context)) {
                RecordatorioUtils.activarRecordatorioDiario(context);
            }
        }
    }
}