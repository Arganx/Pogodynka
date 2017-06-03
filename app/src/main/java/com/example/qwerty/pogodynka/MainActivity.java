package com.example.qwerty.pogodynka;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import static android.Manifest.permission.ACCESS_COARSE_LOCATION;
import static android.Manifest.permission.ACCESS_FINE_LOCATION;

public class MainActivity extends AppCompatActivity {


    private TextView loc;
    private LocationManager locationManager;
    private Button start;
    private LocationListener locationListener;
    private String server_url;
    private Double longtitude;
    private Double latitude;
    private TextView temp;
    private TextView speed;
    private TextView pressure;
    private Button stop;
    private boolean onoff;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        onoff=false;

        loc = (TextView) findViewById(R.id.Lokacja);
        start = (Button) findViewById(R.id.btn);
        temp = (TextView) findViewById(R.id.temp);
        speed = (TextView) findViewById(R.id.speed);
        pressure = (TextView) findViewById(R.id.press);
        stop = (Button) findViewById(R.id.stop);

        stop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(onoff==true)
                {
                    locationManager.removeUpdates(locationListener);
                    onoff=false;
                }
            }
        });


        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

        locationListener = new LocationListener() {
            public void onLocationChanged(Location location) {
                loc.setText("Longtitude: " + location.getLongitude() + "\n Latitude:" + location.getLatitude());
            }

            public void onStatusChanged(String provider, int status, Bundle extras) {
            }

            public void onProviderEnabled(String provider) {
            }

            public void onProviderDisabled(String provider) {
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivity(intent);
            }
        };

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{
                    Manifest.permission.ACCESS_COARSE_LOCATION,Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.INTERNET},10);
            return;
        }
        else
        {
            configureButton();
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode)
        {
            case 10:
                if(grantResults.length>0&&grantResults[0]== PackageManager.PERMISSION_GRANTED)
                {
                    configureButton();
                }

        }
    }


    private void configureButton() {
        start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                locationManager.requestLocationUpdates("gps", 0, 0, locationListener);
                Location location= locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                loc.setText("Longtitude: " + location.getLongitude() + "\n Latitude:" + location.getLatitude());
                longtitude=location.getLongitude();
                latitude=location.getLatitude();
                server_url="http://api.openweathermap.org/data/2.5/weather?lat=" + latitude+"&lon=" + longtitude+"&appid=a79a2ed02ae5c67d759c4a0ce1a9360e&units=metric";

                JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, server_url,(String) null, new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            temp.setText("Temperatura: " + response.getJSONObject("main").get("temp").toString()+"°C");
                            pressure.setText("Cisnienie: " + response.getJSONObject("main").get("pressure").toString()+"hPa");
                            speed.setText("Predkosc wiatru: " + response.getJSONObject("wind").get("speed").toString()+"km/h");
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(MainActivity.this,"Cos nie wyszlo :(",Toast.LENGTH_SHORT);
                        error.printStackTrace();
                    }
                });

                MySingleton.getmInstance(MainActivity.this).addToRequestQue(jsonObjectRequest);
                onoff=true;
            }
        });
    }
}