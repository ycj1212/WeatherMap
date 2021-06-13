package com.example.weathermap;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class WeatherDetailsAdapter extends RecyclerView.Adapter<WeatherDetailsAdapter.ViewHolder> {
    private ArrayList<WeatherInfo> items;

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView tv_day, tv_hour, tv_state, tv_temperature;
        private final ImageView weatherImage;

        public ViewHolder(View view) {
            super(view);

            tv_day = view.findViewById(R.id.textview_day);
            tv_hour = view.findViewById(R.id.textview_hour);
            tv_state = view.findViewById(R.id.textview_weather_state);
            tv_temperature = view.findViewById(R.id.textview_temperature);
            weatherImage = view.findViewById(R.id.imageview_weather);
        }

        public TextView getDay() {
            return tv_day;
        }

        public TextView getHour() {
            return tv_hour;
        }

        public TextView getState() {
            return tv_state;
        }

        public TextView getTemperature() {
            return tv_temperature;
        }

        public ImageView getWeatherImage() {
            return weatherImage;
        }
    }

    public WeatherDetailsAdapter(ArrayList<WeatherInfo> items) {
        this.items = items;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_weather_details, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        WeatherInfo item = items.get(position);
        holder.getDay().setText(item.getDay()+"일");
        holder.getHour().setText(item.getHour()+"시");
        holder.getTemperature().setText(item.getTemperature()+"도");
        holder.getState().setText(item.getState());
        switch (item.getState()) {
            case "맑음":
                holder.getWeatherImage().setImageResource(R.drawable.ic_wi_day_sunny);
                break;
            case "구름 조금":
                holder.getWeatherImage().setImageResource(R.drawable.ic_wi_cloud);
                break;
            case "구름 많음":
            case "흐림":
                holder.getWeatherImage().setImageResource(R.drawable.ic_wi_cloudy);
                break;
            case "비":
                holder.getWeatherImage().setImageResource(R.drawable.ic_wi_rain);
                break;
            case "눈/비":
                holder.getWeatherImage().setImageResource(R.drawable.ic_wi_rain_mix);
                break;
            case "눈":
                holder.getWeatherImage().setImageResource(R.drawable.ic_wi_snow);
                break;
            default:
                break;
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }
}
