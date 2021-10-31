package com.mbrow233.familymap;

import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceManager;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.joanzapata.iconify.IconDrawable;
import com.joanzapata.iconify.fonts.FontAwesomeIcons;
import com.mbrow233.familymap.data.DataCache;
import com.mbrow233.familymap.data.DataGenerator;
import com.mbrow233.familymap.data.PersonItem;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import Model.Event;
import Model.Person;

public class PersonActivity extends AppCompatActivity {

    private TextView firstName;
    private TextView lastName;
    private TextView gender;

    int[] colors = {R.color.hue_azure, R.color.hue_blue,
            R.color.hue_cyan, R.color.hue_green, R.color.hue_magenta,
            R.color.hue_orange, R.color.hue_red, R.color.hue_rose,
            R.color.hue_violet, R.color.hue_yellow};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_person);

        ExpandableListView expandableListView = findViewById(R.id.expandableListView);

        String curPersonID = getIntent().getStringExtra("PERSON");
        Person curPerson = DataCache.getInstance().getPeople().get(curPersonID);

        getSupportActionBar().setTitle(R.string.person_activity_titlebar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        firstName = findViewById(R.id.first_name);
        lastName = findViewById(R.id.last_name);
        gender = findViewById(R.id.gender);

        firstName.setText(curPerson.getFirstName());
        lastName.setText(curPerson.getLastName());
        if (curPerson.getGender().equals("f")) {
            gender.setText(R.string.female_title);
        }
        else {
            gender.setText(R.string.male_title);
        }

        DataGenerator dataGenerator = new DataGenerator();

        List<Event> events = dataGenerator.getEvents(curPersonID);

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        boolean fatherSide = sharedPreferences.getBoolean("fathersside", false);
        boolean motherSide = sharedPreferences.getBoolean("mothersside", false);
        boolean male = sharedPreferences.getBoolean("maleevents", false);
        boolean female = sharedPreferences.getBoolean("femaleevents", false);

        Map<String, Event> fatherSideEvents = DataCache.getInstance().getFatherSideEvents();
        Map <String, Event> motherSideEvents = DataCache.getInstance().getMotherSideEvents();
        Map <String, Event> maleEvents = DataCache.getInstance().getMaleEvents();
        Map <String, Event> femaleEvents = DataCache.getInstance().getFemaleEvents();

        if (!fatherSide && (fatherSideEvents != null)) {
            for (Map.Entry<String, Event> entry : fatherSideEvents.entrySet()) {
                events.remove(entry.getValue());
            }
        }
        if (!motherSide && (motherSideEvents!=null)) {
            for (Map.Entry<String, Event> entry : motherSideEvents.entrySet()) {
                events.remove(entry.getValue());
            }
        }
        if (!male && (maleEvents != null)) {
            for (Map.Entry<String, Event> entry : maleEvents.entrySet()) {
                events.remove(entry.getValue());
            }
        }
        if (!female && (femaleEvents != null)) {
            for (Map.Entry<String, Event> entry : femaleEvents.entrySet()) {
                events.remove(entry.getValue());
            }
        }



        List<PersonItem> persons = dataGenerator.getPersons(curPersonID);

        expandableListView.setAdapter(new ExpandableListAdapter(events, persons));
    }

    //todo:change this out with a recyclerview?
    private class ExpandableListAdapter extends BaseExpandableListAdapter {

        private static final int EVENT_GROUP_POSITION = 0;
        private static final int PERSON_GROUP_POSITION = 1;

        private final List<Event> events;
        private final List<PersonItem> persons;

        ExpandableListAdapter(List<Event> events, List<PersonItem> persons) {
            this.events = events;
            this.persons = persons;
        }

        @Override
        public int getGroupCount() {
            return 2;
        }

        @Override
        public int getChildrenCount(int groupPosition) {
            switch (groupPosition) {
                case EVENT_GROUP_POSITION:
                    return events.size();
                case PERSON_GROUP_POSITION:
                    return persons.size();
                default:
                    throw new IllegalArgumentException("Unrecognized group position: " + groupPosition);
            }
        }

        @Override
        public Object getGroup(int groupPosition) {
            switch (groupPosition) {
                case EVENT_GROUP_POSITION:
                    return getString(R.string.event_group_title);
                case PERSON_GROUP_POSITION:
                    return getString(R.string.person_group_title);
                default:
                    throw new IllegalArgumentException("Unrecognized group position: " + groupPosition);
            }
        }

        @Override
        public Object getChild(int groupPosition, int childPosition) {
            switch (groupPosition) {
                case EVENT_GROUP_POSITION:
                    return events.get(childPosition);
                case PERSON_GROUP_POSITION:
                    return persons.get(childPosition);
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
            if(convertView == null) {
                convertView = getLayoutInflater().inflate(R.layout.list_item_group, parent, false);
            }

            TextView titleView = convertView.findViewById(R.id.listTitle);

            switch (groupPosition) {
                case EVENT_GROUP_POSITION:
                    titleView.setText(R.string.event_group_title);
                    break;
                case PERSON_GROUP_POSITION:
                    titleView.setText(R.string.person_group_title);
                    break;
                default:
                    throw new IllegalArgumentException("Unrecognized group position: " + groupPosition);
            }

            return convertView;
        }

        @Override
        public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
            View itemView;

            switch(groupPosition) {
                case EVENT_GROUP_POSITION:
                    itemView = getLayoutInflater().inflate(R.layout.event_item, parent, false);
                    initializeEventView(itemView, childPosition);
                    break;
                case PERSON_GROUP_POSITION:
                    itemView = getLayoutInflater().inflate(R.layout.person_item, parent, false);
                    initializePersonView(itemView, childPosition);
                    break;
                default:
                    throw new IllegalArgumentException("Unrecognized group position: " + groupPosition);
            }

            return itemView;
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
            Drawable icon = new IconDrawable(getApplicationContext(), FontAwesomeIcons.fa_map_marker).
                        colorRes(hue).sizeDp(40);
            eventImageView.setImageDrawable(icon);

            eventItemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(getApplicationContext(), EventActivity.class);
                    String curEvent = events.get(childPosition).getEventID();
                    intent.putExtra("EVENT", curEvent);
                    DataCache.getInstance().setCurEvent(curEvent);
                    startActivity(intent);
                }
            });
        }

        private void initializePersonView(View personItemView, final int childPosition) {
            Person curPerson = persons.get(childPosition).getPerson();

            TextView personNameView = personItemView.findViewById(R.id.person_name);
            String toSet = curPerson.getFirstName() + " " + curPerson.getLastName();
            personNameView.setText(toSet);

            TextView personRelationView = personItemView.findViewById(R.id.person_relation);
            personRelationView.setText(persons.get(childPosition).getRelation());

            ImageView personIconView = personItemView.findViewById(R.id.person_icon);
            Drawable genderIcon;
            if (curPerson.getGender().equalsIgnoreCase("m")) {
                genderIcon = new IconDrawable(getApplicationContext(), FontAwesomeIcons.fa_male).
                        colorRes(R.color.male_icon).sizeDp(40);
            }
            else {
                genderIcon = new IconDrawable(getApplicationContext(), FontAwesomeIcons.fa_female).
                        colorRes(R.color.female_icon).sizeDp(40);
            }
            personIconView.setImageDrawable(genderIcon);

            personItemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(getApplicationContext(), PersonActivity.class);
                    String curPerson = persons.get(childPosition).getPerson().getPersonID();
                    intent.putExtra("PERSON", curPerson);
                    startActivity(intent);
                }
            });
        }

        @Override
        public boolean isChildSelectable(int groupPosition, int childPosition) {
            return true;
        }
    }
}