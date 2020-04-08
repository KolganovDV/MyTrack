package com.example.mytrack;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private static final int REQUEST_CODE_PERMISSION_ACCESS_FINE_LOCATION = 1;
    private GoogleMap mMap;
    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference myRef = database.getReference("pos");
    Location gps;
    String s;
    //Линия которую будем рисовать
    PolylineOptions line = new PolylineOptions();

    Location locPos;

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //ОСТАНАВЛИВАЕМ СЛУЖБУ ЖПС
        /*stopService(
                new Intent(MapsActivity.this, MyServiceGPS.class));*/
    }
    LocationManager manager;
    LocationListener listener;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        int permissionStatus = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION);
        if (permissionStatus != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.ACCESS_FINE_LOCATION},
                    REQUEST_CODE_PERMISSION_ACCESS_FINE_LOCATION);
        }
        //ЗАПУСКАЕМ СЛУЖБУ ЖПС
        //startService(
        /*if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(
                    new Intent(MapsActivity.this, MyServiceGPS.class));
        }*/
        if(savedInstanceState != null){
            line.addAll((List)savedInstanceState.getParcelableArrayList("key"));
        }
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        Thread myThread = new Thread( // создаём новый поток
                new Runnable() { // описываем объект Runnable в конструкторе
                    public void run() {
                       // play(); // вызываем метод воспроизведения
                        while (true);
                    }
                }
        );
        manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        listener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {

                if (location!=null) {
                    gps  = location;
                    //myRef.setValue(gps);
                    /*String.format("%1$.4f %2$.4f %3$tF %3$tT",
                            location.getLatitude(), location.getLongitude(), new Date(location.getTime()));
                    //tvGPS.setText(gps);
                    // tvGPS.setText(String.valueOf(location.getLongitude()));*/
                }
                else{
                    // tvGPS.setText("Sorry, location");
                    //tvGPS.setText("unavailable");
                }

            }
            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {
            }
            @Override
            public void onProviderEnabled(String provider) {
            }
            @Override
            public void onProviderDisabled(String provider) {
            }
        };

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // Проверка наличия разрешений
            // Если нет разрешения на использование соответсвующих разркешений выполняем какие-то действия
            return;
        }
        manager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0,listener);

        //myThread.start();

        DatabaseReference myRef2 = database.getReference("pos");
        myRef2.addValueEventListener(new ValueEventListener() {
            // Область показа маркеров
            LatLngBounds.Builder builder = new LatLngBounds.Builder();
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.
                //String value = dataSnapshot.getValue(String.class);
                Map<String, Object> td = (HashMap<String, Object>) dataSnapshot.getValue();
                //locPos = new Location(dataSnapshot.getValue(Location.class));
                if (td==null)return;
                double lat = Double.parseDouble(td.get("latitude").toString());
                double lag = Double.parseDouble(td.get("longitude").toString());


                //locPos.setLatitude(lat);
                //locPos.setLongitude(lag);
                //readDB("pos");
                // Add a marker in Sydney and move the camera
                LatLng sydney = new LatLng(lat, lag);
                mMap.clear();
                mMap.addMarker(new MarkerOptions().position(sydney).title("ЭТО Я"));
                line.add(new LatLng(sydney.latitude, sydney.longitude));


                builder.include(sydney);
                //builder.include(secondMarker.getPosition());
                LatLngBounds bounds = builder.build();

                LatLng center = bounds.getCenter();
                builder.include(new LatLng(center.latitude-0.001f,center.longitude-0.001f));
                builder.include(new LatLng(center.latitude+0.001f,center.longitude+0.001f));
                bounds = builder.build();
                //CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, markerPadding);

                mMap.setLatLngBoundsForCameraTarget(bounds);
               mMap.addPolyline(line);
                mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, 15));
                //mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));
                /*CameraPosition cameraPosition = new CameraPosition.Builder()
                        .target(new LatLng(sydney.latitude, sydney.longitude))
                        .zoom(15)
                        .build();
                mMap.moveCamera( CameraUpdateFactory.newCameraPosition(cameraPosition));*/

                /*if (locPos != null) {
                    LatLng sydney = new LatLng(locPos.getLatitude(), locPos.getLongitude());
                    mMap.addMarker(new MarkerOptions().position(sydney).title("ЭТО Я"));
                    mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));
                }
                else {
                    LatLng sydney = new LatLng(56.8587, 53.3017);
                    mMap.addMarker(new MarkerOptions().position(sydney).title("ЭТО Я"));
                    // mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));

                    //устанавливаем позицию и масштаб отображения карты
                    CameraPosition cameraPosition = new CameraPosition.Builder()
                            .target(new LatLng(56.8587, 53.3017))
                            .zoom(15)
                            .build();
                    mMap.moveCamera( CameraUpdateFactory.newCameraPosition(cameraPosition));
                }*/

                //Log.d(TAG, "Value is: " + value);
                //s = value;
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                //Log.w(TAG, "Failed to read value.", error.toException());
                s= "ERROR";
            }
        });
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        ArrayList<LatLng> listLine = (ArrayList)line.getPoints();
        outState.putParcelableArrayList("key", listLine);
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        //Делаем линию более менее симпатичное
        line.width(8f).color(R.color.colorPrimary);

        //Добавляем линию на карту
        googleMap.addPolyline(line);

        //readDB("pos");
        // Add a marker in Sydney and move the camera
       if (locPos != null) {
            LatLng sydney = new LatLng(locPos.getLatitude(), locPos.getLongitude());
            mMap.addMarker(new MarkerOptions().position(sydney).title("ЭТО Я"));
           // mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));
           //устанавливаем позицию и масштаб отображения карты
           CameraPosition cameraPosition = new CameraPosition.Builder()
                   .target(new LatLng(sydney.latitude, sydney.longitude))
                   .zoom(15)
                   .build();
           mMap.moveCamera( CameraUpdateFactory.newCameraPosition(cameraPosition));
        }
       /*else {
           LatLng sydney = new LatLng(56.8587, 53.3017);
           mMap.addMarker(new MarkerOptions().position(sydney).title("ЭТО Я"));
          // mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));

           //устанавливаем позицию и масштаб отображения карты
           CameraPosition cameraPosition = new CameraPosition.Builder()
                   .target(new LatLng(56.8587, 53.3017))
                   .zoom(15)
                   .build();
           mMap.moveCamera( CameraUpdateFactory.newCameraPosition(cameraPosition));
       }*/
    }

    private void readDB(String name){
        DatabaseReference myRef2 = database.getReference(name);
        myRef2.addValueEventListener(new ValueEventListener() {

            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.
                String value = dataSnapshot.getValue(String.class);
                locPos = dataSnapshot.getValue(Location.class);
                //Log.d(TAG, "Value is: " + value);
                s = value;
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                //Log.w(TAG, "Failed to read value.", error.toException());
                s= "ERROR";
            }
        });
        //return locPos;
    }
    public void clickBtnSetting(View v){
        Intent intent = new Intent(this, SettingsActivity.class);
        startActivity(intent);
    }
    public void clickBtnClear(View v){
        line.getPoints().clear();
        mMap.clear();
    }
}