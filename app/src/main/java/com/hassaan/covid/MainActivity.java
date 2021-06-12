package com.hassaan.covid;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.provider.Settings;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    Double longitude, latitude;
    FusedLocationProviderClient mFusedLocationClient;
    TextView topText, locationText, casesText, searchCountryText;
    Button searchBtn, fightBtn, recoverBtn;
    BarChart casesBarChart;
    int PERMISSION_ID = 44;

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        casesBarChart = findViewById(R.id.casesBarChart);
        locationText = findViewById(R.id.locationText);
        casesText = findViewById(R.id.casesText);
        topText = findViewById(R.id.topText);
        searchCountryText = findViewById(R.id.searchCountryText);
        searchBtn = findViewById(R.id.searchBtn);
        fightBtn = findViewById(R.id.fightBtn);
        recoverBtn = findViewById(R.id.recoverBtn);
        SpannableStringBuilder spannable = new SpannableStringBuilder("Welcome to, COViD");
        spannable.setSpan(
                new ForegroundColorSpan(Color.parseColor("#FF82D3")),
                15,
                16,
                Spannable.SPAN_EXCLUSIVE_INCLUSIVE
        );
        topText.setText(spannable);
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        getLastLocation();

        searchBtn.setOnClickListener(v -> {
            if (!searchCountryText.getText().equals("")) {
                try {
                    fetchCases(searchCountryText.getText().toString());
                } catch (UnsupportedEncodingException e) {
                    System.err.println("Unable to encode country");
                }
            }
        });

        fightBtn.setOnClickListener(v -> {
            Intent intent = new Intent(getApplicationContext(), FightCovidActivity.class);
            startActivity(intent);
        });

        recoverBtn.setOnClickListener(v -> {
            Intent intent = new Intent(getApplicationContext(), RecoverCovidActivity.class);
            startActivity(intent);
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private void fetchCases(String country) throws UnsupportedEncodingException {
        RequestQueue queue = Volley.newRequestQueue(this);
        String url = "https://disease.sh/v3/covid-19/countries/" + URLEncoder.encode(country, String.valueOf(StandardCharsets.UTF_8)) + "?strict=true";
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                response -> {
                    try {
                        JSONObject responseJSON = new JSONObject(response);
                        casesText.setText("Covid Cases in " + responseJSON.get("country") + ":");
                        ArrayList<BarEntry> entries = new ArrayList<>();
                        entries.add(new BarEntry(Float.parseFloat(String.valueOf(responseJSON.get("cases"))), 0));
                        entries.add(new BarEntry(Float.parseFloat(String.valueOf(responseJSON.get("active"))), 1));
                        entries.add(new BarEntry(Float.parseFloat(String.valueOf(responseJSON.get("recovered"))), 2));
                        entries.add(new BarEntry(Float.parseFloat(String.valueOf(responseJSON.get("deaths"))), 3));
                        BarDataSet bardataset = new BarDataSet(entries, "Covid Cases");
                        ArrayList<String> labels = new ArrayList<>();
                        labels.add("Total");
                        labels.add("Active");
                        labels.add("Recovered");
                        labels.add("Deaths");
                        BarData data = new BarData(labels, bardataset);
                        bardataset.setColors(new int[] {
                                Color.rgb(255, 237, 77),
                                Color.rgb(77, 95, 255),
                                Color.rgb(77, 255, 124),
                                Color.rgb(255, 77, 77)
                        });
                        casesBarChart.setData(data);
                        casesBarChart.setDescription("");
                        casesBarChart.animateY(2000);
                        casesBarChart.getAxisLeft().setEnabled(false);
                        casesBarChart.getAxisRight().setEnabled(false);
                        casesBarChart.getXAxis().setEnabled(false);
                        casesBarChart.setTouchEnabled(false);
                    } catch (JSONException e) {
                        casesText.setText("Could not fetch cases.");
                    }
                }, error -> casesText.setText("Could not fetch cases.")
        );
        queue.add(stringRequest);
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @SuppressLint("SetTextI18n")
    private void setLocationText() {
        try {
            Geocoder gcd = new Geocoder(this, Locale.getDefault());
            List<Address> addresses = gcd.getFromLocation(latitude, longitude, 1);
            if (addresses.size() > 0) {
                locationText.setText(addresses.get(0).getLocality() + ", " + addresses.get(0).getCountryName());
                try {
                    fetchCases(addresses.get(0).getCountryName());
                } catch (UnsupportedEncodingException e) {
                    System.err.println("Unable to encode country");
                }
            } else {
                locationText.setText("Could not get location.");
            }
        } catch (IOException e) {
            System.err.println("Geocoder Exception.");
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @SuppressLint("MissingPermission")
    private void getLastLocation() {
        if (checkPermissions()) {
            if (isLocationEnabled()) {
                mFusedLocationClient.getLastLocation().addOnCompleteListener(task -> {
                    Location location = task.getResult();
                    if (location == null) {
                        requestNewLocationData();
                    } else {
                        longitude = location.getLongitude();
                        latitude = location.getLatitude();
                        setLocationText();
                    }
                });
            } else {
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivity(intent);
            }
        } else {
            requestPermissions();
        }
    }

    @SuppressLint("MissingPermission")
    private void requestNewLocationData() {
        LocationRequest mLocationRequest = new LocationRequest();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setInterval(5);
        mLocationRequest.setFastestInterval(0);
        mLocationRequest.setNumUpdates(1);
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        mFusedLocationClient.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper());
    }

    private LocationCallback mLocationCallback = new LocationCallback() {

        @RequiresApi(api = Build.VERSION_CODES.KITKAT)
        @Override
        public void onLocationResult(LocationResult locationResult) {
            Location mLastLocation = locationResult.getLastLocation();
            latitude = mLastLocation.getLatitude();
            longitude = mLastLocation.getLongitude();
            setLocationText();
        }
    };

    private boolean checkPermissions() {
        return ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    private void requestPermissions() {
        ActivityCompat.requestPermissions(this, new String[]{
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSION_ID);
    }

    private boolean isLocationEnabled() {
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    public void
    onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_ID) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getLastLocation();
            }
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    public void onResume() {
        super.onResume();
        if (checkPermissions()) {
            getLastLocation();
        }
    }
}