package com.axtell.rocketmileschallenge;

import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONObject;


public class DayFragment extends Fragment {

    int mPosition;
    String mWeatherData;

    public DayFragment() {
        // I would like to pass in data here, and keep the data in the main activity, right?
        // And every time the activity loads, re-get the data, and reload the fragments
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        Bundle args = getArguments();
        mPosition = getArguments().getInt("position");
        mWeatherData = getArguments().getString("weatherData");

        return inflater.inflate(R.layout.fragment_day, container, false);

    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        TextView locationDescription = (TextView) view.findViewById(R.id.location_description);
        TextView timeDescription = (TextView) view.findViewById(R.id.time_description);
        TextView temperatureDescription = (TextView) view.findViewById(R.id.temperature_description);
        TextView descriptionDescription = (TextView) view.findViewById(R.id.description_description);

        // parse the weather data
        try {
            int index = mPosition * 2;
            JSONObject obj = new JSONObject(mWeatherData);

            JSONObject locationObject = obj.getJSONObject("location");
            locationDescription.setText(locationObject.getString("areaDescription"));

            JSONObject timeObject = obj.getJSONObject("time");
            JSONArray timeArray = timeObject.getJSONArray("startPeriodName");
            timeDescription.setText(timeArray.get(index).toString());

            JSONObject weatherObject = obj.getJSONObject("data");
            JSONArray temperatureArray = weatherObject.getJSONArray("temperature");
            temperatureDescription.setText(temperatureArray.get(index).toString() + (char) 0x00B0 + "F");

            JSONObject descriptionObject = obj.getJSONObject("data");
            JSONArray descriptionArray = descriptionObject.getJSONArray("weather");
            descriptionDescription.setText(descriptionArray.get(index).toString());


        } catch (Throwable t) {
            Log.e("Test", "Unable to parse the JSON response");

        }
    }
}
