package com.example.onlineneighborhood;

import android.content.Context;
import android.os.Bundle;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.view.MenuItem;
import android.widget.TextView;


public class BottomNavigationActivity extends AppCompatActivity implements BottomNavigationView.OnNavigationItemSelectedListener {


    public static Context contextOfApplication;
    public static Context getContextOfApplication()
    {
        return contextOfApplication;
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bottom_navigation);
        BottomNavigationView navView = findViewById(R.id.nav_view);
        navView.setOnNavigationItemSelectedListener(this);
        contextOfApplication = getApplicationContext();

    }


    private boolean loadFragment(Fragment fragment){

       if(fragment != null){

           getSupportFragmentManager()
                   .beginTransaction()
                   .replace(R.id.fragment_container,fragment).commit();
       }
       return false;
    }


    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {

        Fragment fragment = null;
        switch (menuItem.getItemId()){

            case R.id.navigation_profile:
                fragment= new ProfileFragment();
                break;


            case R.id.navigation_map:
                fragment = new MapsFragment();
                break;

            case R.id.navigation_home:
                fragment= new HomeFragment();
                break;

        }
        return loadFragment(fragment);
    }
}
