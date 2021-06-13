package com.example.weathermap;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
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
import java.util.ArrayList;
import java.util.Calendar;

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

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback, GoogleMap.OnMarkerClickListener {
    private static final LatLng[] COORDINATE = new LatLng[City.values().length];
    private static final Marker[] MARKER = new Marker[City.values().length];
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1001;

    private GoogleMap mMap;
    private Document doc = null;

    private final ArrayList<WeatherInfo>[] weathers = new ArrayList[City.values().length];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        for (int i=0; i<City.values().length; i++) {
            weathers[i] = new ArrayList<WeatherInfo>();
        }
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

            new GetXMLTask(this, city).execute("https://www.kma.go.kr/wid/queryDFS.jsp?");
        }

        LatLng center = new LatLng(36, 127.8);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(center, 7));   // 중심으로 이동
        mMap.setOnMarkerClickListener(this);

        enableMyLocation();
    }

    @Override
    public boolean onMarkerClick(@NonNull Marker marker) {
        WeatherDetailsDialogFragment weatherDetailsDialogFragment =
                new WeatherDetailsDialogFragment(weathers[City.valueOf(marker.getTitle()).ordinal()]);
        weatherDetailsDialogFragment.show(getSupportFragmentManager(), "Weather Details");

        return true;
    }

    private Bitmap createBitmapFromView(View view) {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        view.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        view.measure(displayMetrics.widthPixels, displayMetrics.heightPixels);
        view.layout(0, 0, displayMetrics.widthPixels, displayMetrics.heightPixels);
        view.buildDrawingCache();

        Bitmap bitmap = Bitmap.createBitmap(view.getMeasuredWidth(), view.getMeasuredHeight(), Bitmap.Config.ARGB_8888);

        Canvas canvas = new Canvas(bitmap);
        view.draw(canvas);

        return bitmap;
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
        private Context context;
        private City city;

        public GetXMLTask(Context context, City city) {
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
            NodeList nodeList = doc.getElementsByTagName("data");
            String hour, day, temp, weather;

            for (int i = 0; i < nodeList.getLength(); i++) {
                Node node = nodeList.item(i);
                Element fstElmnt = (Element) node;

                NodeList hourList = fstElmnt.getElementsByTagName("hour");
                Element hourElement = (Element) hourList.item(0);
                hourList = hourElement.getChildNodes();
                hour = hourList.item(0).getNodeValue();
                hour = String.valueOf(Integer.parseInt(hour) - 3);

                NodeList dayList = fstElmnt.getElementsByTagName("day");
                Element dayElement = (Element) dayList.item(0);
                dayList = dayElement.getChildNodes();
                day = dayList.item(0).getNodeValue();
                day = String.valueOf(Calendar.getInstance().get(Calendar.DAY_OF_MONTH) +
                        Integer.parseInt(day));

                NodeList tempList = fstElmnt.getElementsByTagName("temp");
                Element tempElement = (Element) tempList.item(0);
                tempList = tempElement.getChildNodes();
                temp = tempList.item(0).getNodeValue();

                NodeList weatherStateList = fstElmnt.getElementsByTagName("wfKor");
                Element weatherStateElement = (Element) weatherStateList.item(0);
                weatherStateList = weatherStateElement.getChildNodes();
                weather = weatherStateList.item(0).getNodeValue();

                weathers[city.ordinal()].add(new WeatherInfo(hour, day, temp, weather));
            }

            View view = ((LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE))
                    .inflate(R.layout.marker, null);
            TextView cityName = view.findViewById(R.id.textview_city_name);
            TextView temperature = view.findViewById(R.id.textview_temperature);
            ImageView weatherImage = view.findViewById(R.id.imageview_weather);

            cityName.setText(city.name());
            temperature.setText(weathers[city.ordinal()].get(0).getTemperature());

            switch (weathers[city.ordinal()].get(0).getState()) {
                case "맑음":
                    weatherImage.setImageResource(R.drawable.ic_wi_day_sunny);
                    break;
                case "구름 조금":
                    weatherImage.setImageResource(R.drawable.ic_wi_cloud);
                    break;
                case "구름 많음":
                case "흐림":
                    weatherImage.setImageResource(R.drawable.ic_wi_cloudy);
                    break;
                case "비":
                    weatherImage.setImageResource(R.drawable.ic_wi_rain);
                    break;
                case "눈/비":
                    weatherImage.setImageResource(R.drawable.ic_wi_rain_mix);
                    break;
                case "눈":
                    weatherImage.setImageResource(R.drawable.ic_wi_snow);
                    break;
                default:
                    break;
            }

            MARKER[city.ordinal()] = mMap.addMarker(new MarkerOptions()
                    .position(city.getCoordinate())
                    .title(city.name())
                    .icon(BitmapDescriptorFactory.fromBitmap(createBitmapFromView(view))));
        }
    }
}