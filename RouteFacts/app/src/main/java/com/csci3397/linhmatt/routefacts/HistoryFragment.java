package com.csci3397.linhmatt.routefacts;

import android.database.Cursor;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link HistoryFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class HistoryFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public HistoryFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment HistoryFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static HistoryFragment newInstance(String param1, String param2) {
        HistoryFragment fragment = new HistoryFragment();
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
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_history, container, false);

        Database db = new Database(getActivity());
        Cursor cursor = db.getDB();
        cursor.moveToFirst();

        LinearLayout layout = view.findViewById(R.id.layHistoryLinear);
        for (int i = 0; i < cursor.getCount(); i++) {
            TextView row = new TextView(getActivity());
            String city = cursor.getString(0);
            String state = cursor.getString(1);
            Integer dateNum = cursor.getInt(2);
            Integer year = dateNum/372;
            Integer month = (dateNum - year*372)/31 + 1;
            Integer day = (dateNum - year*372 - (month-1)*31) + 1;
            row.setText(city + ", " + state + ": " + month + "/" + day + "/" + year);
            row.setTextSize(20);
            row.setPaddingRelative(0,25,0,25);
            View.OnClickListener listener = new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    NavController navController = Navigation.findNavController(getActivity(), R.id.fragment);
                    Bundle bundle = new Bundle();
                    bundle.putString("city", city);
                    bundle.putString("state", state);
                    navController.navigate(R.id.mainFragment, bundle);
                }
            };
            row.setOnClickListener(listener);
            layout.addView(row);
            cursor.moveToNext();
        }

        return view;
    }
}