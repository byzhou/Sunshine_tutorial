package com.example.sunshine;

import android.app.Fragment;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

import static com.example.sunshine.R.styleable.MenuItem;

/**
 * Created by bobzhou on 11/1/16.
 */


public class ForecastFragment extends android.support.v4.app.Fragment {

    public ForecastFragment() {

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.forecastfragment, menu);
    }

    public fetchWeather myFetchWeather;

    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_refresh) {
            myFetchWeather = new fetchWeather();
            myFetchWeather.execute("94043,us");
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);
        ArrayList<String> weatherItems = new ArrayList<>(5);
        weatherItems.add("Today - Sunny - 88/63");
        weatherItems.add("Tomorrow - Foggy -89/23");
        weatherItems.add("Wednesday - Rainy");
        weatherItems.add("Thursday - Rainy");
        weatherItems.add("Friday - Rainy");

        ArrayAdapter<String> weatherAdapter = new ArrayAdapter<>(
                getActivity(),
                R.layout.list_item_forecast,
                R.id.listview_forecast,
                weatherItems
        );

        ListView listView = (ListView) rootView.findViewById(R.id.list_view_forecast);
        listView.setAdapter(weatherAdapter);
        // These two need to be declared outside the try/catch
        // so that they can be closed in the finally block.
        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;

        // Will contain the raw JSON response as a string.
        String forecastJsonStr = null;

        Log.d("start network", "This is even before try");


        return rootView;
    }

    public class fetchWeather extends AsyncTask<String, Void, String> {
        HttpURLConnection urlConnection;
        BufferedReader reader;
        String forecastJsonStr;

        private final String LOG_TAG = fetchWeather.class.getSimpleName();

        @Override
        protected String doInBackground(String... urls) {

            // params comes from the execute() call: params[0] is the url.
            try {
                // Construct the URL for the OpenWeatherMap query
                // Possible parameters are avaiable at OWM's forecast API page, at
                // http://openweathermap.org/API#forecast
                Log.d("start network", "This is in the try");

                Uri.Builder builder = new Uri.Builder();
                builder.scheme("http")
                        .authority("api.openweathermap.org")
                        .appendPath("data")
                        .appendPath("2.5")
                        .appendPath("forecast")
                        .appendPath("weather")
                        .appendQueryParameter("zip", urls[0])
                        .appendQueryParameter("units", "metric")
                        .appendQueryParameter("cnt", "7")
                        .appendQueryParameter("mode", "json")
                        .appendQueryParameter("APPID", "3164401a3f292605d908b8371fb5105c");
                String url_string = builder.build().toString();
                URL url = new URL(url_string);
                //URL url = new URL("http://api.openweathermap.org/data/2.5/forecast/weather?zip=94040,us&units=metric&cnt=7&mode=json&APPID=3164401a3f292605d908b8371fb5105c");

                // Create the request to OpenWeatherMap, and open the connection
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                Log.d("Connection", "url connected");

                // Read the input stream into a String
                InputStream inputStream = urlConnection.getInputStream();
                StringBuffer buffer = new StringBuffer();
                if (inputStream == null) {
                    // Nothing to do.
                    Log.d("input stream", "No input stream");
                    return null;
                }
                reader = new BufferedReader(new InputStreamReader(inputStream));

                String line;
                while ((line = reader.readLine()) != null) {
                    // Since it's JSON, adding a newline isn't necessary (it won't affect parsing)
                    // But it does make debugging a *lot* easier if you print out the completed
                    // buffer for debugging.
                    buffer.append(line + "\n");
                }

                Log.d("reading json", "all the data has been read");

                if (buffer.length() == 0) {
                    // Stream was empty.  No point in parsing.
                    Log.d("buffer size", "buffer size is zero");
                    return null;
                }
                forecastJsonStr = buffer.toString();

                Log.v("returnString", forecastJsonStr);
            } catch (IOException e) {
                Log.e("PlaceholderFragment", "Error ", e);
                // If the code didn't successfully get the weather data, there's no point in attemping
                // to parse it.
                return null;
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (final IOException e) {
                        Log.e("PlaceholderFragment", "Error closing stream", e);
                    }
                }
            }
            return forecastJsonStr;
        }

        // onPostExecute displays the results of the AsyncTask.
        @Override
        protected void onPostExecute(String result) {


        }
    }

}

