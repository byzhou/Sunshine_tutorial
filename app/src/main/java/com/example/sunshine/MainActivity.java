package com.example.sunshine;

import android.support.v4.app.Fragment;
import android.support.v4.view.LayoutInflaterCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (savedInstanceState == null){
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.activity_main, new PlaceholderFragment())
                    .commit();
        }
    }

    public static class PlaceholderFragment extends Fragment {

        public PlaceholderFragment(){

        }
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
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
            return rootView;
        }
    }
}
