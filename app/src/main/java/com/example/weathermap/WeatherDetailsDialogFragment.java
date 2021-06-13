package com.example.weathermap;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.util.ArrayList;

public class WeatherDetailsDialogFragment extends BottomSheetDialogFragment {
    private TextView cityName;
    private RecyclerView recyclerView;
    private WeatherDetailsAdapter adapter;
    private ArrayList<WeatherInfo> items;
    private String name;

    public WeatherDetailsDialogFragment(ArrayList<WeatherInfo> items, String cityName) {
        this.items = items;
        name = cityName;
    }

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState
    ) {
        View view = inflater.inflate(R.layout.fragment_bottom_sheet, container, false);

        cityName = view.findViewById(R.id.textview_city_name);
        cityName.setText(name);

        recyclerView = view.findViewById(R.id.recyclerview);
        adapter = new WeatherDetailsAdapter(items);
        recyclerView.setAdapter(adapter);

        return view;
    }
}
