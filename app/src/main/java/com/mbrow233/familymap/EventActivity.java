package com.mbrow233.familymap;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;

import android.os.Bundle;

import com.joanzapata.iconify.Iconify;
import com.joanzapata.iconify.fonts.FontAwesomeModule;
import com.mbrow233.familymap.data.DataCache;

public class EventActivity extends AppCompatActivity {
    MapFragment mapFragment;
    FragmentManager fm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event);

        String curEventID = getIntent().getStringExtra("EVENT");

        if (savedInstanceState == null) {
            fm = this.getSupportFragmentManager();
            mapFragment = (MapFragment) fm.findFragmentById(R.id.welcome_screen);
            if (mapFragment == null) {
                mapFragment = new MapFragment();
                Bundle args = new Bundle();
                args.putString("CUR_EVENT", curEventID);
                DataCache.getInstance().setCurEvent(curEventID);
                mapFragment.setArguments(args);
                fm.beginTransaction()
                        .add(R.id.event_map_fragment, mapFragment)
                        .commit();
            }
        }
    }
}