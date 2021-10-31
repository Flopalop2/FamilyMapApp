package com.mbrow233.familymap;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.joanzapata.iconify.IconDrawable;
import com.joanzapata.iconify.Iconify;
import com.joanzapata.iconify.fonts.FontAwesomeIcons;
import com.joanzapata.iconify.fonts.FontAwesomeModule;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;


public class MainActivity extends AppCompatActivity {

    LoginFragment loginFragment;
    FragmentManager fm;
    public static boolean loggedIn;

    //todo: do a search for string literals
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        invalidateOptionsMenu();

        setContentView(R.layout.activity_main);
        getSupportActionBar().setTitle(R.string.welcome);

        Iconify.with(new FontAwesomeModule());



        if (savedInstanceState == null) {
            fm = this.getSupportFragmentManager();
            loginFragment = (LoginFragment) fm.findFragmentById(R.id.welcome_screen);
            if (loginFragment == null) {
                loginFragment = new LoginFragment();
                fm.beginTransaction()
                        .add(R.id.fragment_frame, loginFragment)
                        .commit();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (loggedIn) {
            super.onCreateOptionsMenu(menu);

            new MenuInflater(this).inflate(R.menu.menu, menu);

            menu.findItem(R.id.action_settings).setIcon(
                    new IconDrawable(getApplicationContext(), FontAwesomeIcons.fa_gear)
                            .colorRes(R.color.white)
                            .actionBarSize());

            menu.findItem(R.id.action_search).setIcon(
                    new IconDrawable(getApplicationContext(), FontAwesomeIcons.fa_search)
                            .colorRes(R.color.white)
                            .actionBarSize());

            return true;
        }
        else {
            return false;
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.action_search:
                Intent intent = new Intent(getApplicationContext(), SearchActivity.class);
                startActivity(intent);
                return true;
            case R.id.action_settings:
                Intent intent2 = new Intent(getApplicationContext(), SettingsActivity.class);
                startActivity(intent2);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();
        if (!loggedIn) {
            Fragment myFragment = (Fragment) getSupportFragmentManager().findFragmentByTag("MAP_FRAG");
            if (myFragment != null) {
                LoginFragment fragment = (LoginFragment) fm.findFragmentById(R.id.map_frag_holder);
                if (fragment == null) {
                    fragment = new LoginFragment();
                    fm.beginTransaction()
                            .replace(R.id.fragment_frame, fragment, "LOGIN_FRAG")
                            //.addToBackStack("MAP_FRAG")
                            .commit();
                }
            }
            invalidateOptionsMenu();
        }
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    public void changeToMapFragment() {
        loggedIn = true;
        fm = this.getSupportFragmentManager();

        invalidateOptionsMenu();
        getSupportActionBar().setTitle(R.string.app_name);

        MapFragment fragment = (MapFragment) fm.findFragmentById(R.id.map_frag_holder);
        if (fragment == null) {
            fragment = new MapFragment();
            fm.beginTransaction()
                    .replace(R.id.fragment_frame, fragment, "MAP_FRAG")
                    //.addToBackStack("MAP_FRAG")
                    .commit();
        }
    }
}