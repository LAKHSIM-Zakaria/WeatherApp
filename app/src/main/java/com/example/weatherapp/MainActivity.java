package com.example.weatherapp;

import androidx.appcompat.app.AppCompatActivity;
import android.net.Uri;
import android.os.Bundle;
import android.os.*;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import org.json.JSONArray;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.ExecutionException;

public class MainActivity extends AppCompatActivity {
    TextView cityName;
    Button search;
    TextView show;
    VideoView backgroundVideo;
    String url;

    class getWeather extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... urls) {
            StringBuilder result = new StringBuilder();
            try {
                URL url = new URL(urls[0]);
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.connect();

                InputStream inputStream = urlConnection.getInputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));

                String line = "";
                while ((line = reader.readLine()) != null) {
                    result.append(line).append("\n");
                }
                return result.toString();
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            try {
                JSONObject jsonObject = new JSONObject(result);
                JSONObject main = jsonObject.getJSONObject("main");
                JSONArray weatherArray = jsonObject.getJSONArray("weather");
                JSONObject weather = weatherArray.getJSONObject(0);
                String mainWeather = weather.getString("main");
                String weatherDescription = weather.getString("description");
                String icon = weather.getString("icon");

                double tempKelvin = main.getDouble("temp");
                double feelsLikeKelvin = main.getDouble("feels_like");
                double tempMinKelvin = main.getDouble("temp_min");
                double tempMaxKelvin = main.getDouble("temp_max");
                int pressure = main.getInt("pressure");
                int humidity = main.getInt("humidity");

                JSONObject wind = jsonObject.getJSONObject("wind");
                double windSpeed = wind.getDouble("speed");
                int windDeg = wind.getInt("deg");

                String weatherInfo = "Temperature: " + kelvinToCelsius(tempKelvin) + "°C\n" +
                        "Feels Like: " + kelvinToCelsius(feelsLikeKelvin) + "°C\n" +
                        "Temperature Min: " + kelvinToCelsius(tempMinKelvin) + "°C\n" +
                        "Temperature Max: " + kelvinToCelsius(tempMaxKelvin) + "°C\n" +
                        "Pressure: " + pressure + " hPa\n" +
                        "Humidity: " + humidity + "%\n" +
                        "Wind Speed: " + windSpeed + " m/s\n" +
                        "Wind Direction: " + windDeg + "°\n" +
                        "Weather: " + mainWeather + " (" + weatherDescription + ")";

                show.setText(weatherInfo);
                setBackgroundVideo(mainWeather);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }


        private String kelvinToCelsius(double kelvin) {
            return String.format("%.2f", kelvin - 273.15);
        }

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        cityName = findViewById(R.id.cityName);
        search = findViewById(R.id.search);
        show = findViewById(R.id.weather);
        backgroundVideo = findViewById(R.id.backgroundVideo);

        setBackgroundVideo("Clear");

        search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                show.setVisibility(View.VISIBLE);
                String city = cityName.getText().toString();
                try {
                    if (city != null) {
                        url = "https://api.openweathermap.org/data/2.5/weather?q=" + city + "&appid={API_KEY}";
                    } else {
                        Toast.makeText(MainActivity.this, "Enter City", Toast.LENGTH_SHORT).show();
                    }
                    getWeather task = new getWeather();
                    String response = task.execute(url).get();
                    if (response == null) {
                        show.setText("Cannot find Weather");
                    }
                } catch (ExecutionException | InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void setBackgroundVideo(String weatherCondition) {
        int videoResId = R.raw.sunny_video; // Default video
        switch (weatherCondition) {
            case "Clear":
                videoResId = R.raw.sunny_video; // Replace with your sunny video
                break;
            case "Clouds":
                videoResId = R.raw.cloudy_video; // Replace with your cloudy video
                break;
            case "Rain":
                videoResId = R.raw.rain_video; // Replace with your rain video
                break;
            case "Snow":
                videoResId = R.raw.snow_video; // Replace with your snow video
                break;
            case "Wind":
                videoResId = R.raw.wind_video; // Replace with your windy video
                break;
        }

        backgroundVideo.setVideoURI(Uri.parse("android.resource://" + getPackageName() + "/" + videoResId));
        backgroundVideo.start();

        backgroundVideo.setOnCompletionListener(mp -> {
            mp.seekTo(0);
            mp.start();
        });
    }

}
