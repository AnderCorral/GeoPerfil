package com.ander.geoperfil;

import android.Manifest;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.Looper;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class MapaActivity extends FragmentActivity implements OnMapReadyCallback {

    private static final int CODIGO_PERMISO_LOCALIZACION = 100;

    private GoogleMap elmapa;
    private TextView etiqLatitud;
    private TextView etiqLongitud;
    private Button botonActualizarUbicacion;

    private FusedLocationProviderClient proveedordelocalizacion;
    private LocationRequest peticion;
    private LocationCallback actualizador;

    private double ultimaLatitud = 0.0;
    private double ultimaLongitud = 0.0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mapa);

        etiqLatitud = findViewById(R.id.etiquetaLatitud);
        etiqLongitud = findViewById(R.id.etiquetaLongitud);
        botonActualizarUbicacion = findViewById(R.id.botonActualizarUbicacion);

        SupportMapFragment elfragmento =
                (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.fragmentoMapa);
        if (elfragmento != null) {
            elfragmento.getMapAsync(this);
        }

        proveedordelocalizacion = LocationServices.getFusedLocationProviderClient(this);

        peticion = new LocationRequest.Builder(10000)
                .setMinUpdateIntervalMillis(5000)
                .setPriority(Priority.PRIORITY_HIGH_ACCURACY)
                .build();

        actualizador = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                super.onLocationResult(locationResult);

                if (locationResult != null && locationResult.getLastLocation() != null) {
                    Location location = locationResult.getLastLocation();
                    actualizarUbicacionEnPantalla(location);
                } else {
                    etiqLatitud.setText("Latitud: (desconocida)");
                    etiqLongitud.setText("Longitud: (desconocida)");
                }
            }
        };

        botonActualizarUbicacion.setOnClickListener(v -> obtenerUltimaPosicion());
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        elmapa = googleMap;
        elmapa.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        comprobarPermisosYLocalizar();
    }

    private void comprobarPermisosYLocalizar() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            obtenerUltimaPosicion();
        } else {
            ActivityCompat.requestPermissions(
                    this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    CODIGO_PERMISO_LOCALIZACION
            );
        }
    }

    private void obtenerUltimaPosicion() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        proveedordelocalizacion.getLastLocation()
                .addOnSuccessListener(this, location -> {
                    if (location != null) {
                        actualizarUbicacionEnPantalla(location);
                    } else {
                        etiqLatitud.setText("Latitud: (desconocida)");
                        etiqLongitud.setText("Longitud: (desconocida)");
                        Toast.makeText(this, "Ubicación no disponible todavía", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(this, e ->
                        Toast.makeText(this, "Error obteniendo ubicación", Toast.LENGTH_SHORT).show()
                );
    }

    private void actualizarUbicacionEnPantalla(Location location) {
        ultimaLatitud = location.getLatitude();
        ultimaLongitud = location.getLongitude();

        etiqLatitud.setText("Latitud: " + ultimaLatitud);
        etiqLongitud.setText("Longitud: " + ultimaLongitud);

        guardarDatosWidget();

        if (elmapa != null) {
            LatLng nuevascoordenadas = new LatLng(ultimaLatitud, ultimaLongitud);

            elmapa.clear();
            elmapa.addMarker(new MarkerOptions()
                    .position(nuevascoordenadas)
                    .title("Mi ubicación"));

            CameraUpdate actualizar = CameraUpdateFactory.newLatLngZoom(nuevascoordenadas, 16);
            elmapa.moveCamera(actualizar);
        }
    }

    private void guardarDatosWidget() {
        SharedPreferences prefs = getSharedPreferences(GeoPerfilWidget.PREFS_WIDGET, MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();

        editor.putString("latitud", String.valueOf(ultimaLatitud));
        editor.putString("longitud", String.valueOf(ultimaLongitud));

        SimpleDateFormat formato = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());
        String hora = formato.format(new Date());
        editor.putString("hora", hora);

        editor.apply();

        GeoPerfilWidget.actualizarTodosLosWidgets(this);
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            proveedordelocalizacion.requestLocationUpdates(
                    peticion,
                    actualizador,
                    Looper.getMainLooper()
            );
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        proveedordelocalizacion.removeLocationUpdates(actualizador);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == CODIGO_PERMISO_LOCALIZACION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                obtenerUltimaPosicion();
            } else {
                Toast.makeText(this, "Permiso de localización denegado", Toast.LENGTH_SHORT).show();
            }
        }
    }
}