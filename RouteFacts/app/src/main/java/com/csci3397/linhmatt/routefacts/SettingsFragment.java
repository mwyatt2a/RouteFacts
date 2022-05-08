package com.csci3397.linhmatt.routefacts;

import android.database.Cursor;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.Toast;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link SettingsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class SettingsFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public SettingsFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment SettingsFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static SettingsFragment newInstance(String param1, String param2) {
        SettingsFragment fragment = new SettingsFragment();
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
        View view = inflater.inflate(R.layout.fragment_settings, container, false);

        Database db = new Database(getActivity());
        Cursor cursor = db.getSettings();
        cursor.moveToFirst();
        boolean voice = false;
        boolean auto = false;
        boolean background = false;

        for (int i = 0; i < cursor.getCount(); i++) {
            if (cursor.getString(0).equals("voice")) voice = true;
            if (cursor.getString(0).equals("auto")) auto = true;
            if (cursor.getString(0).equals("background")) background = true;
            cursor.moveToNext();
        }

        if (voice) {
            CheckBox box = view.findViewById(R.id.chkbxVoice);
            box.setChecked(true);
        }
        if (auto) {
            CheckBox box = view.findViewById(R.id.chkbxLocation);
            box.setChecked(true);
        }
        if (background) {
            CheckBox box = view.findViewById(R.id.chkbxBackground);
            box.setChecked(true);
        }

        CompoundButton.OnCheckedChangeListener voiceListener = new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton cb, boolean b) {
                db.updateSettings("voice");
            }
        };

        CompoundButton.OnCheckedChangeListener autoListener = new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton cb, boolean b) {
                db.updateSettings("auto");
                if (b) {
                    CheckBox backgroundBox = view.findViewById(R.id.chkbxBackground);
                    backgroundBox.setVisibility(View.VISIBLE);
                }
                else {
                    CheckBox backgroundBox = view.findViewById(R.id.chkbxBackground);
                    backgroundBox.setVisibility(View.INVISIBLE);
                }
            }
        };

        CompoundButton.OnCheckedChangeListener backgroundListener = new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton cb, boolean b) {
                db.updateSettings("background");
            }
        };

        CheckBox voiceBox = view.findViewById(R.id.chkbxVoice);
        voiceBox.setOnCheckedChangeListener(voiceListener);
        CheckBox autoBox = view.findViewById(R.id.chkbxLocation);
        autoBox.setOnCheckedChangeListener(autoListener);
        CheckBox backgroundBox = view.findViewById(R.id.chkbxBackground);
        backgroundBox.setOnCheckedChangeListener(backgroundListener);
        if (auto) {
            backgroundBox.setVisibility(View.VISIBLE);
        }
        else {
            backgroundBox.setVisibility(View.INVISIBLE);
        }

        return view;
    }
}