package com.example.weathermap;

public class PlaceInfo {
    private int gridX, gridY;

    public PlaceInfo(int x, int y) {
        gridX = x;
        gridY = y;
    }

    public int getGridX() {
        return gridX;
    }

    public int getGridY() {
        return gridY;
    }
}