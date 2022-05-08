package com.csci3397.linhmatt.routefacts;

import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import android.os.Bundle;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {
    BottomNavigationView bottomNavigationView;
    NavController navController;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        bottomNavigationView = findViewById(R.id.navbar);

        navController = Navigation.findNavController(this, R.id.fragment);

        bottomNavigationView.setSelectedItemId(R.id.M);

        bottomNavigationView.setOnItemSelectedListener(item -> {
            if (item.getItemId() == R.id.S) {
                navController.navigate(R.id.settingsFragment);
            }
            else if (item.getItemId() == R.id.M) {
                navController.navigate(R.id.mainFragment);
            }
            else if (item.getItemId() == R.id.R) {
                navController.navigate(R.id.routesFragment);
            }
            else if (item.getItemId() == R.id.H) {
                navController.navigate(R.id.historyFragment);
            }

            return true;
        });
    }
}