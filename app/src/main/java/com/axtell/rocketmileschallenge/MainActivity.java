package com.axtell.rocketmileschallenge;

import android.content.Context;
import android.content.pm.PackageManager;
import android.location.LocationListener;
import android.location.LocationManager;
import android.support.v4.app.FragmentManager;
import android.app.FragmentTransaction;
import android.location.Location;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;

public class MainActivity extends AppCompatActivity implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    private GoogleApiClient mGoogleApiClient;
    private ViewPager mViewPager;
    private PagerAdapter mPagerAdapter;
    private LocationManager mLocationManager;

    private static final double CHICAGO_LAT_DEGREES = 41.8369;
    private static final double CHICAGO_LONG_DEGREES = -87.6847;
    private static final int NUM_OF_DAYS = 5;
    private static final long LOCATION_REFRESH_TIME = 30000; // in milliseconds, ~ 30 seconds
    private static final float LOCATION_REFRESH_DISTANCE = 1E4f; // 10000 in meters

    private double mCurLat = CHICAGO_LAT_DEGREES;
    private double mCurLong = CHICAGO_LONG_DEGREES;

    private String mWeatherData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mViewPager = (ViewPager) findViewById(R.id.pager);
        mPagerAdapter = new SwipePagerAdapter(getSupportFragmentManager());
        mViewPager.setAdapter(mPagerAdapter);

        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }
    }

    private void updatePagerAdapter() {
        mPagerAdapter = new SwipePagerAdapter(getSupportFragmentManager());
        mViewPager.setAdapter(mPagerAdapter);
    }

    private class SwipePagerAdapter extends FragmentStatePagerAdapter {
        public SwipePagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            Bundle bundle = new Bundle();
            bundle.putInt("position", position);
            if (mWeatherData != null) {
                bundle.putString("weatherData", mWeatherData);
            }

            DayFragment newDayFragment = new DayFragment();
            newDayFragment.setArguments(bundle);

            return newDayFragment;
        }

        @Override
        public int getCount() {
            return NUM_OF_DAYS;
        }
    }

    @Override
    protected void onStart() {
        mGoogleApiClient.connect();
        super.onStart();
    }

    @Override
    protected void onStop() {
        mGoogleApiClient.disconnect();
        super.onStop();
    }

    private void getUserLocation(Location location) {

        if (location == null) {
            String permission = "android.permission.ACCESS_COARSE_LOCATION";
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_DENIED) {
                mLocationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

                LocationListener locationListener = new LocationListener() {
                    public void onLocationChanged(Location location) {
                        getUserLocation(location);
                    }

                    public void onStatusChanged(String provider, int status, Bundle extras) {}

                    public void onProviderEnabled(String provider) {}

                    public void onProviderDisabled(String provider) {}
                };

                mLocationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,
                        LOCATION_REFRESH_TIME, LOCATION_REFRESH_DISTANCE, locationListener);

                Location curLocation = mLocationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                if (curLocation != null) {
                    mCurLat = curLocation.getLatitude();
                    mCurLong = curLocation.getLongitude();
                }
            }
        } else {
            mCurLat = location.getLatitude();
            mCurLong = location.getLongitude();
        }

        getWeatherData(mCurLat, mCurLong);
    }

    private void getWeatherData(double latitude, double longitude) {
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        String weatherUrl = "http://forecast.weather.gov/MapClick.php?lat=" + latitude + "&lon=" + longitude + "&FcstType=json";
        StringRequest stringRequest = new StringRequest(Request.Method.GET, weatherUrl,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        mWeatherData = response;
                        updatePagerAdapter();
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast msg = Toast.makeText(getApplicationContext(), "Error retrieving data", Toast.LENGTH_SHORT);
                msg.show();
            }
        });
        requestQueue.add(stringRequest);
    }

    @Override
    public void onConnected(Bundle bundle) {
        getUserLocation(null);
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Toast msg = Toast.makeText(this, "Connection failed", Toast.LENGTH_SHORT);
        msg.show();
    }



}
