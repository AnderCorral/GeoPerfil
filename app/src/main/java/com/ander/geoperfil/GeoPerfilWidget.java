package com.ander.geoperfil;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.widget.RemoteViews;

public class GeoPerfilWidget extends AppWidgetProvider {

    public static final String PREFS_WIDGET = "widget_geoperfil";

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        for (int appWidgetId : appWidgetIds) {
            actualizarWidget(context, appWidgetManager, appWidgetId);
        }
    }

    static void actualizarWidget(Context context, AppWidgetManager appWidgetManager, int appWidgetId) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_WIDGET, Context.MODE_PRIVATE);

        String nombre = prefs.getString("nombre", "(sin usuario)");
        String latitud = prefs.getString("latitud", "(sin datos)");
        String longitud = prefs.getString("longitud", "(sin datos)");
        String hora = prefs.getString("hora", "(sin actualizar)");

        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_geoperfil);

        views.setTextViewText(R.id.etiquetaNombreWidget, "Usuario: " + nombre);
        views.setTextViewText(R.id.etiquetaCoordenadasWidget, "Lat: " + latitud + " | Lon: " + longitud);
        views.setTextViewText(R.id.etiquetaHoraWidget, "Actualizado: " + hora);

        Intent intentAbrirMapa = new Intent(context, MapaActivity.class);
        PendingIntent pendingIntentAbrirMapa = PendingIntent.getActivity(
                context,
                appWidgetId,
                intentAbrirMapa,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        views.setOnClickPendingIntent(R.id.botonAbrirMapaWidget, pendingIntentAbrirMapa);

        appWidgetManager.updateAppWidget(appWidgetId, views);
    }

    public static void actualizarTodosLosWidgets(Context context) {
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
        ComponentName componente = new ComponentName(context, GeoPerfilWidget.class);
        int[] ids = appWidgetManager.getAppWidgetIds(componente);

        for (int appWidgetId : ids) {
            actualizarWidget(context, appWidgetManager, appWidgetId);
        }
    }
}