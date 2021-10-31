package com.mbrow233.familymap;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.preference.Preference;
import androidx.preference.PreferenceManager;

import android.os.Handler;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.health.SystemHealthManager;
import android.provider.ContactsContract;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.Button;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMapOptions;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.internal.IGoogleMapDelegate;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.maps.android.clustering.Cluster;
import com.google.maps.android.clustering.ClusterManager;
import com.mbrow233.familymap.data.DataCache;
import com.mbrow233.familymap.data.DataGenerator;
import com.mbrow233.familymap.data.MyClusterItem;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import Model.Event;
import Model.Person;

import com.joanzapata.iconify.IconDrawable;
import com.joanzapata.iconify.fonts.FontAwesomeIcons;
import com.mbrow233.familymap.data.PersonItem;


public class MapFragment extends Fragment implements OnMapReadyCallback, GoogleMap.OnMapLoadedCallback {
    private GoogleMap map;

    private TextView eventInfo;
    private TableRow eventBox;
    private String curEventID;
    private ImageView eventIcon;
    private boolean setBottom;
    ExpandableListView expandableListView;
    private Button button;
    private ConstraintLayout diagBox;

    int[] colors = {R.color.hue_azure, R.color.hue_blue,
            R.color.hue_cyan, R.color.hue_green, R.color.hue_magenta,
            R.color.hue_orange, R.color.hue_red, R.color.hue_rose,
            R.color.hue_violet, R.color.hue_yellow};

    List<Polyline> polylines = new ArrayList<Polyline>();


    private ClusterManager<MyClusterItem> clusterManager;

    @Override
    public View onCreateView(@NonNull LayoutInflater layoutInflater, ViewGroup container, Bundle savedInstanceState) {
        Bundle arguments = getArguments();
        curEventID = null;
        super.onCreateView(layoutInflater, container, savedInstanceState);
        View view = layoutInflater.inflate(R.layout.fragment_map, container, false);
        if (savedInstanceState != null){
            curEventID = savedInstanceState.getString("CUR_EVENT");
            setBottom = true;
        }
        else {
            if (arguments != null) {
                curEventID = getArguments().getString("CUR_EVENT");
                setBottom = true;
            }
        }

        expandableListView = (ExpandableListView) view.findViewById(R.id.expandableListView2);

        eventInfo = (TextView) view.findViewById(R.id.event_info);
        eventBox = (TableRow) view.findViewById(R.id.event_info_box);
        eventIcon = (ImageView) view.findViewById(R.id.info_logo);
        diagBox = (ConstraintLayout) view.findViewById(R.id.diag_box);
        button = (Button) view.findViewById(R.id.diag_button);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                diagBox.setVisibility(View.GONE);
                setBottomField(map, false);
                DataCache.getInstance().setCurCluster(null);
            }
        });

        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        return view;
    }



    @Override
    public void onMapReady(GoogleMap googleMap) {
        map = googleMap;
        DataCache.getInstance().setMap(map);
        map.setOnMapLoadedCallback(this);


        clusterManager = new ClusterManager<MyClusterItem>(getActivity().getApplicationContext(), map);
        MarkerClusterRenderer<MyClusterItem> clusterRenderer = new MarkerClusterRenderer<MyClusterItem>(getActivity().getApplicationContext(), map, clusterManager);
        clusterManager.setRenderer(clusterRenderer);

        clusterManager.clearItems();
        clusterManager.cluster();
        addItems();
        clusterManager.cluster();

        if(setBottom) {
            setBottomField(map, true);
        }
        else if (DataCache.getInstance().getCurCluster() != null) {
            openClusterBox(map);
        }


        //todo: remove this
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity().getApplicationContext());
        SharedPreferences.OnSharedPreferenceChangeListener listener = new SharedPreferences.OnSharedPreferenceChangeListener() {
            @Override
            public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
                System.out.println("Settings Changed");
                clusterManager.clearItems();
                clusterManager.cluster();
                addItems();
                clusterManager.cluster();
            }
        };
        sharedPreferences.registerOnSharedPreferenceChangeListener(listener);

        eventInfo.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                //oh no this is not the best way to implement this...
                for(Polyline line : polylines) {
                    line.remove();
                }

                polylines.clear();

                clusterManager.clearItems();
                clusterManager.cluster();
                addItems();
                clusterManager.cluster();
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        clusterManager.setOnClusterItemClickListener(new ClusterManager.OnClusterItemClickListener<MyClusterItem>() {
            @Override
            public boolean onClusterItemClick(MyClusterItem item) {
                curEventID = item.getEventID();

                setBottomField(map, true);

                return false;
            }
        });

        clusterManager.setOnClusterClickListener(new ClusterManager.OnClusterClickListener<MyClusterItem>() {
            @Override
            public boolean onClusterClick(Cluster<MyClusterItem> cluster) {

                DataCache.getInstance().setCurCluster(cluster);
                openClusterBox(map);

                return false;
            }
        });

        // Point the map's listeners at the listeners implemented by the cluster
        // manager.
        map.setOnCameraIdleListener(clusterManager);
        map.setOnMarkerClickListener(clusterManager);
        map.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                eventInfo.setText(R.string.default_event_detail);
                curEventID = null;
                eventIcon.setImageResource(android.R.drawable.ic_menu_info_details);
                clusterManager.clearItems();
                clusterManager.cluster();
                addItems();
                clusterManager.cluster();
            }
        });

        eventBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(curEventID != null) {
                    Intent intent = new Intent(getActivity().getApplicationContext(), PersonActivity.class);
                    String curPerson = DataCache.getInstance().getEventPersons().get(curEventID);
                    intent.putExtra("PERSON", curPerson);
                    startActivity(intent);
                }
            }
        });

        clusterManager.clearItems();
        clusterManager.cluster();
        addItems();
        clusterManager.cluster();

    }

    private void openClusterBox(GoogleMap map){
        map.animateCamera(CameraUpdateFactory.newLatLng(DataCache.getInstance().getCurCluster().getPosition()));

        List<Event> events = new ArrayList<>();
        Map<String, Event> eventMap = DataCache.getInstance().getEvents();
        for (MyClusterItem item : DataCache.getInstance().getCurCluster().getItems()) {
            events.add(eventMap.get(item.getEventID()));
        }

        diagBox.setVisibility(View.VISIBLE);

        expandableListView.setAdapter(new MapFragment.ExpandableListAdapter(events));
    }

    private void setBottomField(GoogleMap map, Boolean dontAnimate) {
        if (curEventID != null) {

            Event curEvent = DataCache.getInstance().getEvents().get(curEventID);

            LatLng latLng = new LatLng(curEvent.getLatitude(), curEvent.getLongitude());

            //if (dontAnimate) {
                map.animateCamera(CameraUpdateFactory.newLatLng(latLng));
                map.moveCamera(CameraUpdateFactory.zoomTo(4.0f)); //cant make them both animate.........gah
            //}
            //else {
                map.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 4.0f));
            //}

            Person curPerson = DataCache.getInstance().getPeople().get(curEvent.getPersonID());

            Drawable genderIcon;
            if (curPerson.getGender().equalsIgnoreCase("m")) {
                genderIcon = new IconDrawable(getActivity().getApplicationContext(), FontAwesomeIcons.fa_male).
                        colorRes(R.color.male_icon).sizeDp(40);
            } else {
                genderIcon = new IconDrawable(getActivity().getApplicationContext(), FontAwesomeIcons.fa_female).
                        colorRes(R.color.female_icon).sizeDp(40);
            }
            eventIcon.setImageDrawable(genderIcon);


            String toSet = curPerson.getFirstName() + " " + curPerson.getLastName() + "\n" +
                    curEvent.getEventType().toUpperCase() + ": " + curEvent.getCity() + ", "
                    + curEvent.getCountry() + " " + curEvent.getYear();
            eventInfo.setText(toSet);

            drawLines(map);
        }
    }

    private void drawLines(GoogleMap map) {
        for(Polyline line : polylines) {
            line.remove();
        }
        polylines.clear();

        Event curEvent = DataCache.getInstance().getEvents().get(curEventID);
        Person curPerson = null;
        if (curEvent != null) {
            curPerson = DataCache.getInstance().getPeople().get(curEvent.getPersonID());
        }

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity().getApplicationContext());
        boolean lifeStoryLines = sharedPreferences.getBoolean("storylines", false);
        boolean familyTreeLines = sharedPreferences.getBoolean("treelines", false);
        boolean spouseLines = sharedPreferences.getBoolean("spouselines", false);


        if (curEvent != null && curPerson != null) {
            DataGenerator dataGenerator = new DataGenerator();
            List<Event> personEvents = dataGenerator.getEvents(curPerson.getPersonID());
            if (lifeStoryLines) {
                PolylineOptions options = new PolylineOptions();
                options.color(R.color.teal_200);

                for (Event tempEvent : personEvents) {
                    LatLng toAdd = new LatLng(tempEvent.getLatitude(), tempEvent.getLongitude());
                    options.add(toAdd);
                }

                polylines.add(map.addPolyline(options));
            }
            if (familyTreeLines) {
                getFamilyLines(curPerson, 0);
            }
            if (spouseLines) {
                //todo: okay lines update expect this one, I'll take it
                PolylineOptions options = new PolylineOptions();
                LatLng latLng = new LatLng(curEvent.getLatitude(), curEvent.getLongitude());
                options.add(latLng);
                String spouseID = DataCache.getInstance().getPeople().get(curEvent.getPersonID()).getSpouseID();
                Event birth = new Event();
                if (spouseID != null) {
                    List<Event> spouseEvents = DataCache.getInstance().getPersonEvents().get(spouseID);
                    for (Event event : spouseEvents) {
                        if (event.getEventType().equalsIgnoreCase("birth")) {
                            birth = event;
                            break;
                        }
                    }
                    if (birth.getEventType() == null) {
                        for (Event event : spouseEvents) {
                            if (birth == null) {
                                birth = event;
                            }
                            if (event.getYear() < birth.getYear()) {
                                birth = event;
                            }
                        }
                    }
                    LatLng spouseLatLng = new LatLng(birth.getLatitude(), birth.getLongitude());
                    options.add(spouseLatLng);
                    options.color(R.color.hue_red);
                    polylines.add(map.addPolyline(options));
                }
            }
        }

    }

    private void getFamilyLines(Person curPerson, int level) {
        level++;
        DataGenerator dataGenerator = new DataGenerator();
        List<Event> personEvents = dataGenerator.getEvents(curPerson.getPersonID());
        PolylineOptions options = new PolylineOptions();
        Event birth = new Event();
        for (Event event : personEvents) {
            if (event.getEventType().equalsIgnoreCase("birth")) {
                birth = event;
                break;
            }
        }
        if(birth.getEventType() == null) {
            for (Event event : personEvents) {
                if (birth == null) {
                    birth = event;
                }
                if (event.getYear() < birth.getYear()) {
                    birth = event;
                }
            }
        }
        if (level == 1) {
            birth = DataCache.getInstance().getEvents().get(curEventID);
        }
        LatLng birthLatLng = new LatLng(birth.getLatitude(), birth.getLongitude());
        options.add(birthLatLng);

        if (curPerson.getFatherID() != null) {
            String fatherID = curPerson.getFatherID();
            personEvents = dataGenerator.getEvents(fatherID);
            Event fatherBirth = new Event();
            for (Event event : personEvents) {
                if (event.getEventType().equalsIgnoreCase("birth")) {
                    fatherBirth = event;
                    break;
                }
            }
            if(fatherBirth.getEventType() == null) {
                for (Event event : personEvents) {
                    if (fatherBirth == null) {
                        fatherBirth = event;
                    }
                    if (event.getYear() < fatherBirth.getYear()) {
                        fatherBirth = event;
                    }
                }
            }
            LatLng fatherBirthLatLng = new LatLng(fatherBirth.getLatitude(), fatherBirth.getLongitude());
            options.add(fatherBirthLatLng);
            options.width(20.0f / level);
            polylines.add(map.addPolyline(options));
            Person father = DataCache.getInstance().getPeople().get(fatherID);
            getFamilyLines(father, level);
        }
        options = new PolylineOptions();

        if (curPerson.getMotherID() != null) {
            options.add(birthLatLng);
            String motherID = curPerson.getMotherID();
            personEvents = dataGenerator.getEvents(motherID);
            Event motherBirth = new Event();
            for (Event event : personEvents) {
                if (event.getEventType().equalsIgnoreCase("birth")) {
                    motherBirth = event;
                    break;
                }
            }
            if(motherBirth.getEventType() == null) {
                for (Event event : personEvents) {
                    if (motherBirth == null) {
                        motherBirth = event;
                    }
                    if (event.getYear() < motherBirth.getYear()) {
                        motherBirth = event;
                    }
                }
            }
            LatLng motherBirthLatLng = new LatLng(motherBirth.getLatitude(), motherBirth.getLongitude());
            options.add(motherBirthLatLng);
            options.width(20.0f / level);
            polylines.add(map.addPolyline(options));
            Person mother = DataCache.getInstance().getPeople().get(motherID);
            getFamilyLines(mother, level);
        }

        level--;
        return;


    }


    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("CUR_EVENT", curEventID);
        DataCache.getInstance().setMap(map);
        DataCache.getInstance().setCurEvent(curEventID);

    }

    @Override
    public void onResume() {
        super.onResume();
        //map = DataCache.getInstance().getMap();
        curEventID = DataCache.getInstance().getCurEvent();
        if (map != null) {
            drawLines(map);
            clusterManager.clearItems();
            clusterManager.cluster();
            addItems();
            clusterManager.cluster();
        }

    }

    private void addItems() {
        clusterManager.clearItems();
        clusterManager.cluster();

        Map<String, Event> eventList = DataCache.getInstance().getEvents();
        System.out.println(eventList.toString());
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity().getApplicationContext());
        boolean fatherSide = sharedPreferences.getBoolean("fathersside", false);
        boolean motherSide = sharedPreferences.getBoolean("mothersside", false);
        boolean male = sharedPreferences.getBoolean("maleevents", false);
        boolean female = sharedPreferences.getBoolean("femaleevents", false);

        Map <String, Event> fatherSideEvents = DataCache.getInstance().getFatherSideEvents();
        System.out.println(fatherSideEvents);
        System.out.println(fatherSide);
        Map <String, Event> motherSideEvents = DataCache.getInstance().getMotherSideEvents();
        Map <String, Event> maleEvents = DataCache.getInstance().getMaleEvents();
        Map <String, Event> femaleEvents = DataCache.getInstance().getFemaleEvents();

        if (!fatherSide && (fatherSideEvents != null)) {
            for (Map.Entry<String, Event> entry : fatherSideEvents.entrySet()) {
                eventList.remove(entry.getKey());
                System.out.println("working");
            }
        }
        if (!motherSide && (motherSideEvents!=null)) {
            for (Map.Entry<String, Event> entry : motherSideEvents.entrySet()) {
                eventList.remove(entry.getKey());
            }
        }
        if (!male && (maleEvents != null)) {
            for (Map.Entry<String, Event> entry : maleEvents.entrySet()) {
                eventList.remove(entry.getKey());
            }
        }
        if (!female && (femaleEvents != null)) {
            for (Map.Entry<String, Event> entry : femaleEvents.entrySet()) {
                eventList.remove(entry.getKey());
            }
        }

        System.out.println(eventList.toString());

        Iterator iterator = eventList.entrySet().iterator();
        while(iterator.hasNext()) {
            Map.Entry mapEntry = (Map.Entry) iterator.next();
            Event curEvent = (Event) mapEntry.getValue();

            LatLng location = new LatLng(curEvent.getLatitude(), curEvent.getLongitude());
            String title = curEvent.getCity() + ", " + curEvent.getCountry();
            MyClusterItem item = new MyClusterItem(curEvent.getLatitude(), curEvent.getLongitude(), curEvent.getCity() + ", " + curEvent.getCountry(), curEvent.getEventType(), curEvent.getEventID());
            clusterManager.addItem(item);
        }

    }

    @Override
    public void onMapLoaded() {
        // You probably don't need this callback. It occurs after onMapReady and I have seen
        // cases where you get an error when adding markers or otherwise interacting with the map in
        // onMapReady(...) because the map isn't really all the way ready. If you see that, just
        // move all code where you interact with the map (everything after
        // map.setOnMapLoadedCallback(...) above) to here.

    }



    private class ExpandableListAdapter extends BaseExpandableListAdapter {

        private static final int EVENT_GROUP_POSITION = 0;

        private final List<Event> events;

        ExpandableListAdapter(List<Event> events) {
            this.events = events;
        }

        @Override
        public int getGroupCount() {
            return 1;
        }

        @Override
        public int getChildrenCount(int groupPosition) {
            switch (groupPosition) {
                case EVENT_GROUP_POSITION:
                    return events.size();
                default:
                    throw new IllegalArgumentException("Unrecognized group position: " + groupPosition);
            }
        }

        @Override
        public Object getGroup(int groupPosition) {
            switch (groupPosition) {
                case EVENT_GROUP_POSITION:
                    return getString(R.string.event_group_title);
                default:
                    throw new IllegalArgumentException("Unrecognized group position: " + groupPosition);
            }
        }

        @Override
        public Object getChild(int groupPosition, int childPosition) {
            switch (groupPosition) {
                case EVENT_GROUP_POSITION:
                    return events.get(childPosition);
                default:
                    throw new IllegalArgumentException("Unrecognized group position: " + groupPosition);
            }
        }

        @Override
        public long getGroupId(int groupPosition) {
            return groupPosition;
        }

        @Override
        public long getChildId(int groupPosition, int childPosition) {
            return childPosition;
        }

        @Override
        public boolean hasStableIds() {
            return false;
        }

        @Override
        public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = getLayoutInflater().inflate(R.layout.list_item_group, parent, false);
            }

            TextView titleView = convertView.findViewById(R.id.listTitle);

            switch (groupPosition) {
                case EVENT_GROUP_POSITION:
                    titleView.setText(R.string.event_group_title);
                    break;
                default:
                    throw new IllegalArgumentException("Unrecognized group position: " + groupPosition);
            }

            ExpandableListView eLV = (ExpandableListView) parent;
            eLV.expandGroup(groupPosition);

            return convertView;
        }

        @Override
        public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
            View itemView;

            switch (groupPosition) {
                case EVENT_GROUP_POSITION:
                    itemView = getLayoutInflater().inflate(R.layout.event_item, parent, false);
                    initializeEventView(itemView, childPosition);
                    break;
                default:
                    throw new IllegalArgumentException("Unrecognized group position: " + groupPosition);
            }
            return itemView;
        }

        @Override
        public boolean isChildSelectable(int groupPosition, int childPosition) {
            return false;
        }

        private void initializeEventView(View eventItemView, final int childPosition) {
            TextView eventTypeView = eventItemView.findViewById(R.id.event_type);
            String toSet = events.get(childPosition).getEventType() + ":";
            eventTypeView.setText(toSet);

            TextView eventLocationView = eventItemView.findViewById(R.id.event_location);
            toSet = events.get(childPosition).getCity() + ", " + events.get(childPosition).getCountry();
            eventLocationView.setText(toSet);

            TextView eventYearView = eventItemView.findViewById(R.id.event_year);
            toSet = "(" + events.get(childPosition).getYear() + ")";
            eventYearView.setText(toSet);

            TextView eventPersonView = eventItemView.findViewById(R.id.event_person);
            Person tempPerson = DataCache.getInstance().getPeople().get(events.get(childPosition).getPersonID());
            toSet = tempPerson.getFirstName() + " " + tempPerson.getLastName();
            eventPersonView.setText(toSet);

            ImageView eventImageView = eventItemView.findViewById(R.id.event_icon);
            List<String> stringsList = new ArrayList<>(DataCache.getInstance().getEventTypes());
            int colorIndex = stringsList.indexOf(events.get(childPosition).getEventType().toUpperCase()) % 10;
            int hue = colors[colorIndex];
            Drawable icon = new IconDrawable(getActivity().getApplicationContext(), FontAwesomeIcons.fa_map_marker).
                    colorRes(hue).sizeDp(40);
            eventImageView.setImageDrawable(icon);

            eventItemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    curEventID = events.get(childPosition).getEventID();
                }
            });
        }

    }
}