package com.example.weathermap;

public class WeatherInfo {
    private String hour, day, temperature, state;

    public WeatherInfo(String hour, String day, String temperature, String state) {
        this.hour = hour;
        this.day = day;
        this.temperature = temperature;
        this.state = state;
    }

    public void setHour(String hour) {
        this.hour = hour;
    }
    public void setDay(String day) {
        this.day = day;
    }
    public void setTemperature(String temperature) {
        this.temperature = temperature;
    }
    public void setState(String state) {
        this.state = state;
    }

    public String getHour() {
        return hour;
    }
    public String getDay() {
        return day;
    }
    public String getTemperature() {
        return temperature;
    }
    public String getState() {
        return state;
    }
}
