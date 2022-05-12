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
import android.util.Log;
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

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Random;
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
    List<String> storedfacts = new ArrayList<>();
    List<String> storedwikiid = new ArrayList<>();
    List<Integer> storedidx = new ArrayList<>();
    int idx = 0;
    String key = "";

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
            Double lat = getArguments().getDouble("lat", 0);
            Double lon = getArguments().getDouble("lon", 0);
            generate(view, true, city, state, lat, lon);
        }

        View.OnClickListener gen = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TextView placeView = view.findViewById(R.id.txtMainPlace);
                placeView.setText("Loading...");
                generate(view, false, "", "", 0.0, 0.0);
            }
        };

        Button mainGen = view.findViewById(R.id.btnMainGenerate);
        mainGen.setOnClickListener(gen);

        Button prevFact = view.findViewById(R.id.btnMainPrevious);
        Button nextFact = view.findViewById(R.id.btnMainNext);
        TextView mainF = view.findViewById(R.id.txtMainFact);
        prevFact.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int len = storedfacts.size();
                if (len > 0) {
                    if (idx==0){ //display firstfact
                        mainF.setText(storedfacts.get(idx));
                    } else {
                        if(idx<len && idx>0){ //if idx is less than length, ok to subtract
                            idx-=1; //
                            mainF.setText(storedfacts.get(idx));
                        }
                    }
                } else {
                    //do nothing
                }
            }
        });

        nextFact.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int len = storedfacts.size();
                if (len>0) {
                    //do nothing
                    if (idx==(len-1)) { //if idx is the max length-1, display last fact
                        mainF.setText(storedfacts.get((len-1)));
                    } else {
                        if(idx<len && idx>0){ //if idx is less than length, ok to add
                            idx+=1; //
                            mainF.setText(storedfacts.get(idx));
                        }
                    }
                } else {
                    //do nothing
                }
            }
        });

        return view;
    }

    public void generate(View view, boolean loadLoc, String City, String State, Double Lat, Double Lon) {

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
            getPlaces(City, view, exists, Lat, Lon, storedwikiid);
            //getPlaces("San Antonio", view, exists, Lat, Lon);
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
                                db.updateHistory(city, state, date, lat, lon);
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
                                getPlaces(city, view, exists, lat, lon, storedwikiid);
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


    void getPlaces(String city, View view, boolean tts, double lat, double lon, List<String> widList) {
        TextView fact = view.findViewById(R.id.txtMainFact);
        fact.setText("Loading...");

        ConnectivityManager connectivityManager = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkCapabilities networkCapabilities = connectivityManager.getNetworkCapabilities(connectivityManager.getActiveNetwork());
        boolean isAvailable = false;
        if (networkCapabilities != null) {
            if (networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)) {
                isAvailable = true;
            } else if (networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
                isAvailable = true;
            }
        }


        if (isAvailable) {
            //if stored wiki id list is not empty, do not bother trying to get list again
            if (widList.size() > 0) {
                int numplaces = widList.size();
                Log.e("TAG", "widList size for getplaces is: " + numplaces);
                int randidx = getRandIndex(numplaces);
                String wikiid = widList.get(randidx);
                getValidWikiId(city,view,tts,lat,lon,widList,wikiid, randidx);
            } else {
                //first get list of places within 1000m of lat and lon
                OkHttpClient client = new OkHttpClient();
                String url = "https://api.opentripmap.com/0.1/en/places/radius?apikey=5ae2e3f221c38a28845f05b6612704727243708fa850b3b1f3203aa8&radius=1000&lon=" + lon + "&lat=" + lat;
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
                                fact.setText("Error");
                                throw new IOException();
                            } else {
                                try {
                                    //store all the places within the nearest 1000 m
                                    JSONArray allplaces = new JSONObject(responseBody.string()).getJSONArray("features");

                                    //filter all blank names, take only those with wikipages
                                    //make list of valid wiki ids
                                    List<String> filteredplaces = new ArrayList<String>();
                                    for (int i = 0; i < allplaces.length(); i++) {
                                        JSONObject checkproperties = allplaces.getJSONObject(i).getJSONObject("properties");
                                        Iterator<String> propertieskeys = checkproperties.keys();
                                        boolean checkwid = false;
                                        while(propertieskeys.hasNext()) {
                                            String keyforproperties = propertieskeys.next();
                                            if (keyforproperties.equals("wikidata")) {
                                                checkwid = true;
                                            }
                                        }
                                        if (checkwid) {
                                            String wid = checkproperties.getString("wikidata");
                                            filteredplaces.add(wid);
                                        }
                                    }
                                    storedwikiid = filteredplaces;

                                    //get random object (place)'s wikiid from list of stored wikiid
                                    int numplaces = filteredplaces.size();
                                    int randidx = getRandIndex(numplaces);
                                    String wikiid = filteredplaces.get(randidx);
                                    Log.e("TAG", "initial widList size for getplaces is: " + numplaces);
                                    Log.e("TAG", "widList id for getvalidwikiid else is: " + wikiid);
                                    getValidWikiId(city,view,tts,lat,lon,filteredplaces,wikiid, randidx);
                                } catch (JSONException e) {
                                    fact.setText("Test cats Error2");
                                }
                            }
                        }
                    }
                });
            }
        } else {
            Toast.makeText(getActivity(), "Not Connected To The Internet", Toast.LENGTH_LONG).show();
        }
    }

    void getValidWikiId(String city, View view, boolean tts, double lat, double lon, List<String> widList, String wid, Integer widx) {
        TextView fact = view.findViewById(R.id.txtMainFact);
        fact.setText("Loading...");

        ConnectivityManager connectivityManager = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkCapabilities networkCapabilities = connectivityManager.getNetworkCapabilities(connectivityManager.getActiveNetwork());
        boolean isAvailable = false;
        if (networkCapabilities != null) {
            if (networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)) {
                isAvailable = true;
            } else if (networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
                isAvailable = true;
            }
        }

        //check network available, we search for valid wikiarticle titles using arraylist of wikiids
        if (isAvailable) {
            OkHttpClient client = new OkHttpClient();
            //go find the wiki article title to pass in for last api that extracts text
            //https://www.wikidata.org/w/api.php?action=wbgetentities&format=json&props=sitelinks&ids=Q4347159&sitefilter=enwiki
            String nurl = "https://www.wikidata.org/w/api.php?action=wbgetentities&format=json&props=sitelinks&sitefilter=enwiki&ids=" + wid;
            Request wikidataRequest = new Request.Builder().url(nurl).build();
            Call call2 = client.newCall(wikidataRequest);
            call2.enqueue(new Callback() {
                @Override
                public void onFailure(@NotNull Call call, @NotNull IOException e) {
                    Toast.makeText(getContext(), "Error Getting Data", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                    try (ResponseBody responseBody2 = response.body()) {
                        if (!response.isSuccessful()) {
                            fact.setText("Error");
                            throw new IOException();
                        } else {
                            try {
                                //get all entities from wikiid
                                JSONObject wikigrab = new JSONObject(responseBody2.string()).getJSONObject("entities");
                                //titlekey is wikiid
                                Iterator<String> keyfortitle = wikigrab.keys();
                                String titlekey = keyfortitle.next();

                                //check if enwiki has an article, commonswiki is no good and empty is no good
                                JSONObject wikigrab2 = wikigrab.getJSONObject(titlekey).getJSONObject("sitelinks");
                                Iterator<String> keyforsitelinks = wikigrab2.keys();
                                boolean checkwikitype = false;
                                if (keyforsitelinks.hasNext()) {
                                    while (keyforsitelinks.hasNext()) {
                                        String wikitype = keyforsitelinks.next();
                                        if (wikitype.equals("enwiki")) {
                                            checkwikitype = true;
                                        }
                                    }
                                    //if enwiki has article, call getfact to use text extract api
                                    if (checkwikitype) {
                                        JSONObject wikigrab3 = wikigrab.getJSONObject(titlekey).getJSONObject("sitelinks").getJSONObject("enwiki");
                                        String wikititle = wikigrab3.getString("title");
                                        getFact(wikititle, view, tts, lat, lon, city, widList, wid, widx);
                                    }
                                } else {
                                    //what happens when there is no enwiki? you have to try to get a new place, so call function again
                                    //fact.setText("wait");

                                    //remove nonvalid element from widList
                                    widList.remove(widx);
                                    storedwikiid = widList;
                                    int numplaces = widList.size();
                                    int randidx = getRandIndex(numplaces);
                                    String nwid = widList.get(randidx);
                                    Log.e("TAG", "widList size for getvalidwikiid is: " + numplaces);
                                    Log.e("TAG", "widList id for getvalidwikiid else is: " + nwid);
                                    //call function again on different wikiid
                                    getValidWikiId(city,view,tts,lat,lon,widList,nwid, randidx);
                                    //getPlaces(city, view, tts, lat, lon);
                                }
                            } catch (JSONException e2) {

                            }

                        }
                    }
                }
            });
        } else {
            Toast.makeText(getActivity(), "Not Connected To The Internet", Toast.LENGTH_LONG).show();
        }
    }

    //getFact -- use wiki textextract api to get pure text from article
    void getFact(String placename, View view, boolean tts, double lat, double lon, String city,List<String> widList, String wid, Integer widx) {
        ConnectivityManager connectivityManager = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkCapabilities networkCapabilities = connectivityManager.getNetworkCapabilities(connectivityManager.getActiveNetwork());
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
            //add this to url --> &exsentences=3 to limit to 3 sentences

            String url = "https://en.wikipedia.org/w/api.php?action=query&prop=extracts&exlimit=1&explaintext=1&exsectionformat=plain&exsentences=3&format=json&titles=" + placename;
            Request request = new Request.Builder().url(url).build();
            Call call = client.newCall(request);
            call.enqueue(new Callback() {
                @Override
                public void onFailure(@NotNull Call call, @NotNull IOException e) {
                    Toast.makeText(getContext(), "Error Getting FactInfo for place", Toast.LENGTH_SHORT).show();
                }
                @Override
                public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                    try (ResponseBody responseBody = response.body()) {
                        if (!response.isSuccessful()) {
                            throw new IOException();
                        } else {
                            //placeinfo[0] = "checking try catch";
                            TextView viewFact = view.findViewById(R.id.txtMainFact);
                            try {

                                JSONObject obj = new JSONObject(responseBody.string()).getJSONObject("query").getJSONObject("pages");
                                Iterator<String> keyarr = obj.keys(); //get page keys
                                key = keyarr.next();
                                //if pageid is -1, there is no description to use, else there is description for fact
                                if(key.equals("-1")) {
                                    //call getplaces again if there is no page to be found to get a new title
                                    viewFact.setText("wait restart");

                                    //remove nonvalid element from widList
                                    widList.remove(widx);
                                    storedwikiid = widList;

                                    int numplaces = widList.size();
                                    int randidx = getRandIndex(numplaces);
                                    String nwid = widList.get(randidx);
                                    Log.e("TAG", "widList size for getFact restart is: " + numplaces);
                                    Log.e("TAG", "widList id for getFact restart is: " + nwid);
                                    //call function again on different wikiid
                                    getValidWikiId(city,view,tts,lat,lon,widList,nwid,randidx);
                                } else {
                                    //if key is not -1, there is an article, so put description up
                                    int numplaces = widList.size();
                                    Log.e("TAG", "widList size for getFact else is: " + numplaces);
                                    Log.e("TAG", "widList id for getFact else is: " + widx);
                                    JSONObject page = obj.getJSONObject(key);
                                    String factdescript = page.getString("extract");
                                    storedfacts.add(factdescript);
                                    viewFact.setText(factdescript);
                                    if (tts) {
                                        textToSpeech = new TextToSpeech(getActivity(), new TextToSpeech.OnInitListener() {
                                            @Override
                                            public void onInit(int status) {
                                                if (status == TextToSpeech.SUCCESS) {
                                                    textToSpeech.speak(factdescript, TextToSpeech.QUEUE_FLUSH, null, "id");
                                                }
                                            }
                                        });
                                    }
                                }
                            } catch (JSONException e) {

                            }
                        }
                    }
                }
            });
        } else {
            Toast.makeText(getActivity(), "Not Connected To The Internet", Toast.LENGTH_LONG).show();
        }
    }

    int getRandIndex(Integer max) {
        // creating an object of Random class
        Random random = new Random();
        // Generates random integers 0 to max-1
        int idx = random.nextInt(max);
        
        //we're making sure to go through all wiki id objects and cycle through
        //clear list of ints if match size of wikiid
        if(storedidx.size() == max) {
            storedidx.clear();
            Log.e("TAG", "clear idx list");
            return idx;
        } else if (storedidx.size() == 0) {
            //make list of ints if empty list
            for(int i=0; i<max; i++) {
                storedidx.add(i);
                Log.e("TAG", "initializing idx list for getRand is: " + idx);
            }
            storedidx.remove(idx);
            return idx;
        } else {
            if(storedidx.contains(idx)) {
                int findidx = storedidx.indexOf(idx);
                storedidx.remove(findidx); //remove where idx inside is found
                Log.e("TAG", "using this idx: " + idx);
                return idx;
            } else {
                while(storedidx.contains(idx) == false) {
                    int sizeofstore = storedidx.size();
                    int nidx = random.nextInt(sizeofstore); //pick random new idx from storage list
                    idx = storedidx.get(nidx);
                }
                Log.e("TAG", "new idx: " + idx);
                int findidx = storedidx.indexOf(idx);
                storedidx.remove(findidx);
                return idx;
            }
        }
    }
}
