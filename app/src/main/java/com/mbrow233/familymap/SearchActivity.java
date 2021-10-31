package com.mbrow233.familymap;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.SearchManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

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

public class SearchActivity extends AppCompatActivity {

    private static final int EVENT_ITEM_VIEW_TYPE = 0;
    private static final int PERSON_ITEM_VIEW_TYPE = 1;

    private List<Event> eventsToSearch;
    private List<Person> personsToSearch;

    int[] colors = {R.color.hue_azure, R.color.hue_blue,
            R.color.hue_cyan, R.color.hue_green, R.color.hue_magenta,
            R.color.hue_orange, R.color.hue_red, R.color.hue_rose,
            R.color.hue_violet, R.color.hue_yellow};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        RecyclerView recyclerView = findViewById(R.id.RecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(SearchActivity.this));

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        final List<Event>[] events = new List[]{new ArrayList<>()};
        eventsToSearch = new ArrayList<>();
        final List<Person>[] persons = new List[]{new ArrayList<>()};
        personsToSearch = new ArrayList<>();

        Map<String, Event> eventMap = DataCache.getInstance().getEvents();
        for (Map.Entry<String, Event> entry : eventMap.entrySet()) {
            Event toAdd = entry.getValue();
            eventsToSearch.add(toAdd);
        }

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
                eventsToSearch.remove(entry.getValue());
            }
        }
        if (!motherSide && (motherSideEvents!=null)) {
            for (Map.Entry<String, Event> entry : motherSideEvents.entrySet()) {
                eventsToSearch.remove(entry.getValue());
            }
        }
        if (!male && (maleEvents != null)) {
            for (Map.Entry<String, Event> entry : maleEvents.entrySet()) {
                eventsToSearch.remove(entry.getValue());
            }
        }
        if (!female && (femaleEvents != null)) {
            for (Map.Entry<String, Event> entry : femaleEvents.entrySet()) {
                eventsToSearch.remove(entry.getValue());
            }
        }

        Map<String, Person> personMap = DataCache.getInstance().getPeople();
        for (Map.Entry<String, Person> entry : personMap.entrySet()) {
            Person toAdd = entry.getValue();
            personsToSearch.add(toAdd);
        }

        SearchView searchView = findViewById(R.id.search_box);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                events[0] = searchEvents(query);
                persons[0] = searchPersons(query);
                SearchAdapter adapter = new SearchAdapter(persons[0], events[0]);
                recyclerView.setAdapter(adapter);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                events[0] = searchEvents(newText);
                persons[0] = searchPersons(newText);
                SearchAdapter adapter = new SearchAdapter(persons[0], events[0]);
                recyclerView.setAdapter(adapter);
                return false;
            }
        });
    }

    private List<Event> searchEvents (String query) {
        List<Event> testEvent = new ArrayList<>();
        for (Event event : eventsToSearch) {
            Person associatedPerson = DataCache.getInstance().getPeople().get(event.getPersonID());
            String searchString = " " + event.getEventType() + " " + Integer.toString(event.getYear()) + " "
                    + associatedPerson.getFirstName() + " " + associatedPerson.getLastName() + " "
                    + event.getCity() + " " + event.getCountry()+ " ";
            if (searchString.toLowerCase().contains(query.toLowerCase())) {
                testEvent.add(event);
            }
        }
        return testEvent;
    }

    private List<Person> searchPersons (String query) {
        List<Person> testPerson = new ArrayList<>();
        for (Person person : personsToSearch) {
            String searchString = " " + person.getFirstName() + " " + person.getLastName() + " ";
            if (person.getGender().equalsIgnoreCase("m")) {
                searchString += "male" + " ";
            }
            else {
                searchString += "female" + " ";
            }
            if (searchString.toLowerCase().contains(query.toLowerCase())) {
                testPerson.add(person);
            }
        }
        return testPerson;
    }


    private class SearchAdapter extends RecyclerView.Adapter<SearchViewHolder> {
        private final List<Person> persons;
        private final List<Event> events;


        SearchAdapter(List<Person> persons, List<Event> events) {
            this.persons = persons;
            this.events = events;

        }

        @Override
        public int getItemViewType(int position) {
            return position < persons.size() ? PERSON_ITEM_VIEW_TYPE : EVENT_ITEM_VIEW_TYPE;
        }

        @NonNull
        @Override
        public SearchViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view;

            if(viewType == PERSON_ITEM_VIEW_TYPE) {
                view = getLayoutInflater().inflate(R.layout.person_item, parent, false);
            } else {
                view = getLayoutInflater().inflate(R.layout.event_item, parent, false);
            }

            return new SearchViewHolder(view, viewType);
        }

        @Override
        public void onBindViewHolder(@NonNull SearchViewHolder holder, int position) {
            if(position < persons.size()) {
                holder.bind(persons.get(position));
            } else {
                holder.bind(events.get(position - persons.size()));
            }
        }

        @Override
        public int getItemCount() {
            return persons.size() + events.size();
        }
    }

    private class SearchViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private final TextView eventType;
        private final TextView location;
        private final TextView eventYear;
        private final TextView name;
        private final ImageView icon;
        private final TextView relationship;

        private final int viewType;
        private Event event;
        private Person person;

        SearchViewHolder(View view, int viewType) {
            super(view);
            this.viewType = viewType;

            itemView.setOnClickListener(this);

            if(viewType == EVENT_ITEM_VIEW_TYPE) {
                name = itemView.findViewById(R.id.event_person);
                location = itemView.findViewById(R.id.event_location);
                eventType = itemView.findViewById(R.id.event_type);
                eventYear = itemView.findViewById(R.id.event_year);
                icon = itemView.findViewById(R.id.event_icon);
                relationship = null;

            } else {
                name = itemView.findViewById(R.id.person_name);
                icon = itemView.findViewById(R.id.person_icon);
                eventType = null;
                location = null;
                eventYear = null;
                relationship = itemView.findViewById(R.id.person_relation);
            }
        }

        private void bind(Event event) {
            this.event = event;
            String toSet = DataCache.getInstance().getPeople().get(event.getPersonID()).getFirstName() +
                    " " + DataCache.getInstance().getPeople().get(event.getPersonID()).getLastName();
            name.setText(toSet);

            toSet = event.getCity() + ", " + event.getCountry();
            location.setText(toSet);

            eventYear.setText(Integer.toString(event.getYear()));
            eventType.setText(event.getEventType());

            List<String> stringsList = new ArrayList<>(DataCache.getInstance().getEventTypes());
            int colorIndex = stringsList.indexOf(event.getEventType().toUpperCase()) % 10;
            int hue = colors[colorIndex];
            Drawable iconSet = new IconDrawable(getApplicationContext(), FontAwesomeIcons.fa_map_marker).
                    colorRes(hue).sizeDp(40);
            icon.setImageDrawable(iconSet);
        }

        private void bind(Person person) {
            this.person = person;
            String toSet = person.getFirstName() + " " + person.getLastName();
            name.setText(toSet);
            relationship.setText("");

            Drawable genderIcon;
            if (person.getGender().equalsIgnoreCase("m")) {
                genderIcon = new IconDrawable(getApplicationContext(), FontAwesomeIcons.fa_male).
                        colorRes(R.color.male_icon).sizeDp(40);
            }
            else {
                genderIcon = new IconDrawable(getApplicationContext(), FontAwesomeIcons.fa_female).
                        colorRes(R.color.female_icon).sizeDp(40);
            }
            icon.setImageDrawable(genderIcon);
        }

        @Override
        public void onClick(View view) {
            if(viewType == EVENT_ITEM_VIEW_TYPE) {
                Intent intent = new Intent(getApplicationContext(), EventActivity.class);
                String curEvent = event.getEventID();
                intent.putExtra("EVENT", curEvent);
                DataCache.getInstance().setCurEvent(curEvent);
                startActivity(intent);
            } else {
                Intent intent = new Intent(getApplicationContext(), PersonActivity.class);
                String curPerson = person.getPersonID();
                intent.putExtra("PERSON", curPerson);
                startActivity(intent);
            }
        }
    }
}