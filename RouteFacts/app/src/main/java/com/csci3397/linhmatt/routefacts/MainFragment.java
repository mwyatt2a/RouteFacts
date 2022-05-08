package com.csci3397.linhmatt.routefacts;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkCapabilities;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import android.os.Handler;
import android.os.Looper;
import android.speech.tts.TextToSpeech;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;

import org.json.*;
import org.w3c.dom.Text;

import java.util.Calendar;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link MainFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class MainFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    TextToSpeech textToSpeech;

    public MainFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment MainFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static MainFragment newInstance(String param1, String param2) {

        MainFragment fragment = new MainFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
        textToSpeech = null;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_main, container, false);

        if (getArguments() != null) {
            String city = getArguments().getString("city", "~");
            String state = getArguments().getString("state", "~");
            generate(view, true, city, state);
        }

        View.OnClickListener gen = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TextView placeView = view.findViewById(R.id.txtMainPlace);
                placeView.setText("Loading...");
                generate(view, false, "", "");
            }
        };

        Button mainGen = view.findViewById(R.id.btnMainGenerate);
        mainGen.setOnClickListener(gen);

        return view;
    }

    public void getPlaces(String city, View view, boolean tts) {
        TextView fact = view.findViewById(R.id.txtMainFact);
        fact.setText("Loading...");
        if (tts) {
            textToSpeech = new TextToSpeech(getActivity(), new TextToSpeech.OnInitListener() {
                @Override
                public void onInit(int status) {
                    if (status == TextToSpeech.SUCCESS) {
                        textToSpeech.speak("In San Antonio, there is a university called Trinity University. In that university there is a student name Matthew Wyatt.", TextToSpeech.QUEUE_FLUSH, null, "id");
                    }
                }
            });
        }
    }

    public void generate(View view, boolean loadLoc, String City, String State) {

        if (loadLoc) {
            Database db = new Database(getActivity());
            TextView placeView = view.findViewById(R.id.txtMainPlace);
            placeView.setText(City + ", " + State);
            Cursor cursor = db.getSettings();
            cursor.moveToFirst();
            boolean exists = false;
            for (int i = 0; i < cursor.getCount(); i++) {
                if (cursor.getString(0).equals("voice")) {
                    exists = true;
                }
                else {
                    cursor.moveToNext();
                }
            }
            getPlaces(City, view, exists);
        }
        else {
            LocationManager locationManager = (LocationManager) getActivity().getSystemService(getActivity().LOCATION_SERVICE);
            boolean isenabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);

            if (isenabled) {
                LocationRequest locationRequest = LocationRequest.create();
                locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
                locationRequest.setInterval(5000);
                locationRequest.setFastestInterval(2000);

                if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
                    requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
                }
                LocationServices.getFusedLocationProviderClient(getActivity())
                        .requestLocationUpdates(locationRequest, new LocationCallback() {
                            @Override
                            public void onLocationResult(@NonNull LocationResult locationResult) {
                                super.onLocationResult(locationResult);
                                LocationServices.getFusedLocationProviderClient(getActivity())
                                        .removeLocationUpdates(this);
                                if (locationResult != null) {
                                    double latitude = locationResult.getLastLocation().getLatitude();
                                    double longitude = locationResult.getLastLocation().getLongitude();
                                    getLocation(latitude, longitude, view);
                                } else {
                                    Toast.makeText(getActivity(), "Something Went Wrong With GPS", Toast.LENGTH_SHORT).show();
                                }
                            }
                        }, Looper.getMainLooper());
            } else {
                Toast.makeText(getActivity(), "GPS Is Disabled", Toast.LENGTH_LONG).show();
            }
        }
    }


    void getLocation(double lat, double lon, View view) {
        ConnectivityManager connectivityManager = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkCapabilities networkCapabilities = connectivityManager.getNetworkCapabilities(connectivityManager.getActiveNetwork());
        Database db = new Database(getActivity());
        boolean isAvailable = false;
        if (networkCapabilities != null) {
            if (networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)) {
                isAvailable = true;
            } else if (networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
                isAvailable = true;
            }
        }

        if (isAvailable) {
            OkHttpClient client = new OkHttpClient();
            String url = "https://api.geoapify.com/v1/geocode/reverse?lat=" + lat + "&lon=" + lon + "&apiKey=66653654ed06480ca68fbee93e6388e6";
            Request request = new Request.Builder().url(url).build();
            Call call = client.newCall(request);
            call.enqueue(new Callback() {
                @Override
                public void onFailure(@NotNull Call call, @NotNull IOException e) {
                    Toast.makeText(getContext(), "Error Getting Data", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                    try (ResponseBody responseBody = response.body()) {
                        if (!response.isSuccessful()) {
                            TextView placeView = view.findViewById(R.id.txtMainPlace);
                            placeView.setText("Error");
                            throw new IOException();
                        } else {
                            try {
                                JSONObject obj = new JSONObject(responseBody.string()).getJSONArray("features").getJSONObject(0).getJSONObject("properties");
                                String city = obj.getString("city");
                                String state = obj.getString("state");
                                Date calender = Calendar.getInstance(TimeZone.getTimeZone("EST")).getTime();
                                Integer date = calender.getDay() + calender.getMonth()*31 + (calender.getYear()- 100)*372;
                                db.updateHistory(city, state, date);
                                TextView placeView = view.findViewById(R.id.txtMainPlace);
                                placeView.setText(city + ", " + state);
                                Cursor cursor = db.getSettings();
                                cursor.moveToFirst();
                                boolean exists = false;
                                for (int i = 0; i < cursor.getCount(); i++) {
                                    if (cursor.getString(0).equals("voice")) {
                                        exists = true;
                                    }
                                    else {
                                        cursor.moveToNext();
                                    }
                                }
                                getPlaces(city, view, exists);
                            } catch (JSONException e) {
                                TextView placeView = view.findViewById(R.id.txtMainPlace);
                                placeView.setText("Error2");
                            }
                        }
                    }
                }
            });
        } else {
            Toast.makeText(getActivity(), "Not Connected To The Internet", Toast.LENGTH_LONG).show();
        }
    }
}