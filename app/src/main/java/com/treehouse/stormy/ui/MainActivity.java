package com.treehouse.stormy.ui;

import android.content.Context;
import android.databinding.DataBindingUtil;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.treehouse.stormy.model.CurrentWeather;
import com.treehouse.stormy.model.LocationLabel;
import com.treehouse.stormy.R;
import com.treehouse.stormy.databinding.ActivityMainBinding;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {

    public static final String TAG = MainActivity.class.getSimpleName();
    private CurrentWeather currentWeather;
    private LocationLabel locationLabel;
    private ImageView iconImageView;

    private double latitude = 42.7654;
    private double longitude = 71.4676;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getForecast(latitude, longitude);
        Log.d(TAG, "Main UI code is running, hooray!");
    }

    private void getForecast(double latitude, double longitude) {

        iconImageView = findViewById(R.id.iconImageView);

        // Setup Dark Sky Link
        TextView darkSky = findViewById(R.id.darkSkyAttribution);
        darkSky.setMovementMethod(LinkMovementMethod.getInstance());

        String apiKey = "57eaf3aa961968bf65b0619680588073";


        String forecastURL = "https://api.darksky.net/forecast/" + apiKey +
                "/" + latitude + "," + longitude;

        String locApiURL = "https://nominatim.openstreetmap.org/reverse?format=json&lat=" + latitude
                + "&lon=-" + longitude + "&zoom=18&addressdetails=1";


        if (isNetworkAvailable()) {

            //getLocation(locApiURL);
            getForcastApi(forecastURL);


        }
        else {
            Toast.makeText(this, R.string.network_unavailable_message,
                    Toast.LENGTH_LONG).show();
        }
    }

    private void getLocation (String locApiURL) {
        OkHttpClient client1 = new OkHttpClient();

        Request request1 = new Request.Builder()
                .url(locApiURL)
                .build();

        Call call1 = client1.newCall(request1);
        call1.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {

            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {

                try {
                    String jsonData1 = response.body().string();
                    Log.v(TAG, jsonData1);
                    if (response.isSuccessful()) {
                        locationLabel = getLocationDetails(jsonData1);

                        currentWeather.setLocationLabel(locationLabel.getFormattedLocation());

                    } else {
                        alertUserAboutError();
                    }
                } catch (IOException e) {
                    Log.e(TAG, "Call 1 IO Exception caught: ", e);
                } catch (JSONException e) {
                    Log.e(TAG, "Call 1 JSON Exception caught: ", e);
                }
            }
        });
    }

    private void getForcastApi(String forecastURL) {

        final ActivityMainBinding binder = DataBindingUtil
                .setContentView(MainActivity.this, R.layout.activity_main);

        OkHttpClient client2 = new OkHttpClient();


        Request request2 = new Request.Builder()
                .url(forecastURL)
                .build();

        Call call2 = client2.newCall(request2);
        call2.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {

            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                try {
                    String jsonData = response.body().string();
                    Log.v(TAG, jsonData);
                    if (response.isSuccessful()) {
                        currentWeather = getCurrentDetails(jsonData);

                        final CurrentWeather displayWeather = new CurrentWeather(
                                currentWeather.getLocationLabel(),
                                currentWeather.getSummary(),
                                currentWeather.getIcon(),
                                currentWeather.getTime(),
                                currentWeather.getTemperature(),
                                currentWeather.getHumidity(),
                                currentWeather.getPrecipChance(),
                                currentWeather.getTimezone()
                        );

                        binder.setWeather(displayWeather);

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Drawable drawable = getResources().getDrawable(displayWeather.getIconId());
                                iconImageView.setImageDrawable(drawable);
                            }
                        });

                    } else {
                        alertUserAboutError();
                    }
                } catch (IOException e) {
                    Log.e(TAG, "Call 2 IO Exception caught: ", e);
                } catch (JSONException e) {
                    Log.e(TAG, "Call 2 JSON Exception caught: ", e);
                }

            }
        });
    }

    private CurrentWeather getCurrentDetails(String jsonData) throws JSONException {
        JSONObject forecast = new JSONObject(jsonData);

        String timezone = forecast.getString("timezone");
        Log.i(TAG, "From JSON: " + timezone);

        JSONObject currently = forecast.getJSONObject("currently");
        JSONObject timeZone = forecast.getJSONObject("timezone");

        CurrentWeather currentWeather = new CurrentWeather();

        // Parse weather data from currently object
        currentWeather.setHumidity(currently.getDouble("humidity"));
        currentWeather.setTime(currently.getLong("time"));
        currentWeather.setIcon(currently.getString("icon"));
        currentWeather.setLocationLabel(timeZone.getString("timezone"));
        currentWeather.setPrecipChance(currently.getDouble("precipProbability"));
        currentWeather.setSummary(currently.getString("summary"));
        currentWeather.setTemperature(currently.getDouble("temperature"));
        currentWeather.setTimezone(timezone);

        Log.d(TAG, currentWeather.getFormattedTime());

        return currentWeather;
    }

    private LocationLabel getLocationDetails(String jsonData) throws JSONException {
        JSONObject location = new JSONObject(jsonData);

        LocationLabel locationLabel = new LocationLabel();

        locationLabel.setCity(location.getString("city"));
        locationLabel.setState(location.getString("state"));

        return locationLabel;
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager manager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = manager.getActiveNetworkInfo();

        boolean isAvailable = false;

        if (networkInfo != null && networkInfo.isConnected()) {
            isAvailable = true;
        }

        return isAvailable;
    }

    private void alertUserAboutError() {
        AlertDialogFragment dialog = new AlertDialogFragment();
        dialog.show(getFragmentManager(), "error_dialog");
    }

    public void refreshOnClick(View view) {
        getForecast(latitude, longitude);
        Toast.makeText(this, "Refreshing...", Toast.LENGTH_SHORT).show();
    }

    public void refreshOnClick(View view, double longitude, double latitude) {
        getForecast(latitude, longitude);
        Toast.makeText(this, "Refreshing...", Toast.LENGTH_SHORT).show();
    }

}