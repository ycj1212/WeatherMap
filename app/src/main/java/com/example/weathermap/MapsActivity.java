package com.example.weathermap;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import java.net.URL;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

enum City {
    SEOUL(new LatLng(37.540705, 126.956764)),
    INCHEON(new LatLng(37.469221, 126.573234)),
    GWANGJU(new LatLng(35.126033, 126.831302)),
    DAEGU(new LatLng(35.798838, 128.583052)),
    ULSAN(new LatLng(35.519301, 129.239078)),
    DAEJEON(new LatLng(36.321655, 127.378953)),
    BUSAN(new LatLng(35.198362, 129.053922)),
    GYEONGGI(new LatLng(37.567167, 127.190292)),
    GANGWON(new LatLng(37.555837, 128.209315)),
    CHUNGNAM(new LatLng(36.557229, 126.779757)),
    CHUNGBUK(new LatLng(36.628503, 127.929344)),
    GYEONGBUK(new LatLng(36.248647, 128.664734)),
    GYEONGNAM(new LatLng(35.259787, 128.664734)),
    JEONBUK(new LatLng(35.716705, 127.144185)),
    JEONNAM(new LatLng(34.819400, 126.893113)),
    JEJU(new LatLng(33.364805, 126.542671));

    private final LatLng coordinate;
    City(LatLng coordinate) {
        this.coordinate = coordinate;
    }
    public LatLng getCoordinate() {
        return coordinate;
    }
}

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, GoogleMap.OnCameraMoveListener {
    private static final LatLng[] COORDINATE = new LatLng[City.values().length];
    private static final Marker[] MARKER = new Marker[City.values().length];
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1001;

    private GoogleMap mMap;
    private Document doc = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        UiSettings mapUiSetting = mMap.getUiSettings();
        mapUiSetting.setZoomControlsEnabled(true);
        mapUiSetting.setCompassEnabled(true);

        for (City city : City.values()) {
            int i = city.ordinal();
            LatLng coordinate = city.getCoordinate();

            COORDINATE[i] = coordinate;
            MARKER[i] = mMap.addMarker(new MarkerOptions().position(coordinate).title(city.name()));

            new GetXMLTask(this, city).execute("https://www.kma.go.kr/wid/queryDFS.jsp?");
        }

        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(36, 127.8), 7));   // 중심으로 이동
        mMap.setOnCameraMoveListener(this); // test

        enableMyLocation();
    }

    @Override
    public void onCameraMove() {
        Toast.makeText(this, "줌: "+mMap.getCameraPosition().zoom, Toast.LENGTH_SHORT).show();
    }

    // request location permission
    public void enableMyLocation() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if ((ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED)
                    && mMap != null) {
                mMap.setMyLocationEnabled(true);
            } else {
                requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        LOCATION_PERMISSION_REQUEST_CODE);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case LOCATION_PERMISSION_REQUEST_CODE:
                if (grantResults.length > 0 &&
                        grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    enableMyLocation();
                } else {
                    moveTaskToBack(true);
                    finish();
                    android.os.Process.killProcess(android.os.Process.myPid());
                }
                return;
        }
    }

    @SuppressLint("NewApi")
    private class GetXMLTask extends AsyncTask<String, Void, Document> {
        private Activity context;
        private City city;

        public GetXMLTask(Activity context, City city) {
            this.context = context;
            this.city = city;
        }

        @Override
        protected Document doInBackground(String... urls) {
            URL url;

            try {
                LatLng coordinate = city.getCoordinate();
                url = new URL(urls[0]+"gridx="+coordinate.latitude+"&gridy="+coordinate.longitude);
                DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
                DocumentBuilder db = dbf.newDocumentBuilder();

                doc = db.parse(new InputSource(url.openStream()));
                doc.getDocumentElement().normalize();
            } catch (Exception e) {
                Toast.makeText(getBaseContext(), "Parsing Error", Toast.LENGTH_SHORT).show();
            }

            return doc;
        }

        @Override
        protected void onPostExecute(Document doc) {
            String s = "";
            NodeList nodeList = doc.getElementsByTagName("data");

            for (int i = 0; i < nodeList.getLength(); i++) {
                s += "" + i + ": 날씨 정보: ";
                Node node = nodeList.item(i);
                Element fstElmnt = (Element) node;

                NodeList nameList = fstElmnt.getElementsByTagName("temp");
                Element nameElement = (Element) nameList.item(0);
                nameList = nameElement.getChildNodes();
                s += "온도 = " + ((Node) nameList.item(0)).getNodeValue() + " ,";

                NodeList websiteList = fstElmnt.getElementsByTagName("wfKor");
                Element websiteElement = (Element) websiteList.item(0);
                websiteList = websiteElement.getChildNodes();
                s += "날씨 = " + ((Node) websiteList.item(0)).getNodeValue() + "\n";
            }

            Toast.makeText(context, s, Toast.LENGTH_LONG).show();
        }
    }
}