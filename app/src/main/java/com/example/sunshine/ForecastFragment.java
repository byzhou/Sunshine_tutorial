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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

import android.text.format.Time;

import static com.example.sunshine.R.styleable.MenuItem;

/**
 * Created by bobzhou on 11/1/16.
 */


public class ForecastFragment extends android.support.v4.app.Fragment {

    private ArrayAdapter<String> mForecastAdapter;

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
            try {
                String[] returnWeatherStringArray = myFetchWeather.get();

                mForecastAdapter = new ArrayAdapter<>(
                        getActivity(),
                        R.layout.list_item_forecast,
                        R.id.listview_forecast,
                        returnWeatherStringArray
                );

            } catch (Exception e) {

            }
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

        mForecastAdapter = new ArrayAdapter<>(
                getActivity(),
                R.layout.list_item_forecast,
                R.id.listview_forecast,
                weatherItems
        );

        ListView listView = (ListView) rootView.findViewById(R.id.list_view_forecast);
        listView.setAdapter(mForecastAdapter);
        // These two need to be declared outside the try/catch
        // so that they can be closed in the finally block.
        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;

        // Will contain the raw JSON response as a string.
        String forecastJsonStr = null;

        Log.d("start network", "This is even before try");


        return rootView;
    }

    public class fetchWeather extends AsyncTask<String, Void, String[]> {
        HttpURLConnection urlConnection;
        BufferedReader reader;
        String forecastJsonStr;

        private final String LOG_TAG = fetchWeather.class.getSimpleName();

        @Override
        protected String[] doInBackground(String... urls) {

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

            String[] weatherDataString = null;
            try {
                weatherDataString = getWeatherDataFromJson(forecastJsonStr, 7);
                Log.v("returnString", weatherDataString[1]);
            } catch (JSONException e) {
                Log.e("json_parser", "json object cannot be created!");
            }
            return weatherDataString;
        }

        // onPostExecute displays the results of the AsyncTask.
        @Override
        protected void onPostExecute(String[] result) {


        }


    }


    /* The date/time conversion code is going to be moved outside the asynctask later,
     * so for convenience we're breaking it out into its own method now.
     */
    private String getReadableDateString(long time) {
        // Because the API returns a unix timestamp (measured in seconds),
        // it must be converted to milliseconds in order to be converted to valid date.
        SimpleDateFormat shortenedDateFormat = new SimpleDateFormat("EEE MMM dd");
        return shortenedDateFormat.format(time);
    }

    /**
     * Prepare the weather high/lows for presentation.
     */
    private String formatHighLows(double high, double low) {
        // For presentation, assume the user doesn't care about tenths of a degree.
        long roundedHigh = Math.round(high);
        long roundedLow = Math.round(low);

        String highLowStr = roundedHigh + "/" + roundedLow;
        return highLowStr;
    }

    /**
     * Take the String representing the complete forecast in JSON Format and
     * pull out the data we need to construct the Strings needed for the wireframes.
     * <p>
     * Fortunately parsing is easy:  constructor takes the JSON string and converts it
     * into an Object hierarchy for us.
     */
    private String[] getWeatherDataFromJson(String forecastJsonStr, int numDays)
            throws JSONException {

        // These are the names of the JSON objects that need to be extracted.
        final String OWM_LIST = "list";
        final String OWM_WEATHER = "weather";
        final String OWM_TEMPERATURE = "temp";
        final String OWM_MAX = "max";
        final String OWM_MIN = "min";
        final String OWM_DESCRIPTION = "main";
        String[] resultStrs = new String[numDays];

        JSONObject forecastJson = new JSONObject(forecastJsonStr);
        JSONArray weatherArray = forecastJson.getJSONArray(OWM_LIST);


        // OWM returns daily forecasts based upon the local time of the city that is being
        // asked for, which means that we need to know the GMT offset to translate this data
        // properly.

        // Since this data is also sent in-order and the first day is always the
        // current day, we're going to take advantage of that to get a nice
        // normalized UTC date for all of our weather.
        Log.v("json", "json file has been successfully created");
        Time dayTime = new Time();
        dayTime.setToNow();

        // we start at the day returned by local time. Otherwise this is a mess.
        int julianStartDay = Time.getJulianDay(System.currentTimeMillis(), dayTime.gmtoff);
        Log.v("julianStartDay", "julianStratDay" + String.valueOf(julianStartDay));
        // now we work exclusively in UTC
        dayTime = new Time();

        for (int i = 0; i < weatherArray.length(); i++) {
            // For now, using the format "Day, description, hi/low"
            String day;
            String description;
            String highAndLow;

            // Get the JSON object representing the day
            JSONObject dayForecast = weatherArray.getJSONObject(i);
            Log.v("jsonInLoop", "create a json file in a loop");

            // The date/time is returned as a long.  We need to convert that
            // into something human-readable, since most people won't read "1400356800" as
            // "this saturday".
            long dateTime;
            // Cheating to convert this to UTC time, which is what we want anyhow
            dateTime = dayTime.setJulianDay(julianStartDay + i);
            day = getReadableDateString(dateTime);

            // description is in a child array called "weather", which is 1 element long.
            JSONObject weatherObject = dayForecast.getJSONArray(OWM_WEATHER).getJSONObject(0);
            description = weatherObject.getString(OWM_DESCRIPTION);
            Log.v("jsonInLoop", "create a json file in a loop2");
            Log.d("description", description);

            // Temperatures are in a child object called "temp".  Try not to name variables
            // "temp" when working with temperature.  It confuses everybody.
            //JSONObject temperatureObject = dayForecast.getJSONObject(1);
            Log.v("jsonArray", "create a json array in a loop3");
            double high = dayForecast.getJSONObject("main").getDouble("temp_max");
            double low = dayForecast.getJSONObject("main").getDouble("temp_min");

            Log.v("jsonInLoop", "create a json file in a loop4");

            highAndLow = formatHighLows(high, low);
            resultStrs[i] = day + " - " + description + " - " + highAndLow;
            Log.v("resultStrings", resultStrs[i]);
        }

        for (String s : resultStrs) {
            Log.v("forecast", "Forecast entry: " + s);
        }

        return resultStrs;

    }

}
