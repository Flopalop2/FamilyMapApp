package com.mbrow233.familymap;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.ContactsContract;

import com.mbrow233.familymap.data.DataCache;
import com.mbrow233.familymap.net.ServerProxy;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import Model.Event;
import Model.Person;
import Request.LoginRequest;
import Result.AllPersonResults;
import Result.EventsResult;
import Result.LoginResult;

class DataTask implements Runnable {
    private final Handler messageHandler;
    private String SUCCEED_KEY = "succeed_key";
    //private String authToken; //I was going to pass in authToken here but instead I just save it in serverproxy
    private String TAG = "DataTask";

    public DataTask(Handler messageHandler) {
        this.messageHandler = messageHandler;
    }

    @Override
    public void run() {
        AllPersonResults allPersonResults = ServerProxy.getAllPeople();
        EventsResult eventsResult = ServerProxy.getAllEvents();
        if (allPersonResults.isSuccess() && eventsResult.isSuccess()) {
            Map<String, Event> events = new HashMap<>();
            Map<String, List<Event>> personEvents = new HashMap<>();
            Map<String, Person> persons = new HashMap<>();
            Map<String, String> eventPersons = new HashMap<>();
            Set<String> eventTypes = new HashSet<>();


            for (int i = 0; i < eventsResult.getData().length; ++i) {
                Event curEvent = eventsResult.getData()[i];

                events.put(curEvent.getEventID(), curEvent);

                eventPersons.put(curEvent.getEventID(), curEvent.getPersonID());

                List<Event> eventList = personEvents.get(curEvent.getPersonID());
                if(eventList == null) {
                    eventList = new ArrayList<Event>();
                    eventList.add(curEvent);
                    personEvents.put(curEvent.getPersonID(), eventList);
                } else {
                    if(!eventList.contains(curEvent)) eventList.add(curEvent);
                }

                String eventType = curEvent.getEventType();
                eventTypes.add(eventType.toUpperCase());
            }
            for (int i = 0; i < allPersonResults.getData().length; ++i) {
                persons.put(allPersonResults.getData()[i].getPersonID(), allPersonResults.getData()[i]);
            }
            DataCache.getInstance().setEvents(events);
            DataCache.getInstance().setPeople(persons);
            DataCache.getInstance().setEventTypes(eventTypes);
            DataCache.getInstance().setPersonEvents(personEvents);
            DataCache.getInstance().setEventPersons(eventPersons);
            setUpFilters();
            sendMessage(true);
        }
        else {
            sendMessage(false);
        }


    }

    private Map<String, Event> maleEvents = new HashMap<>();;
    private Map<String, Event> femaleEvents = new HashMap<>();;
    private Map<String, Person> fatherSidePersons = new HashMap<>();;
    private Map<String, Person> motherSidePersons = new HashMap<>();;
    private Map<String, Event> fatherSideEvents = new HashMap<>();;
    private Map<String, Event> motherSideEvents = new HashMap<>();;

    public void setUpFilters() {
        Map<String, Event> tempEvents = DataCache.getInstance().getEvents();
        for (Map.Entry<String, Event> entry: tempEvents.entrySet()) {
            Event tempEvent = entry.getValue();
            String personID = DataCache.getInstance().getPeople().get(tempEvent.getPersonID()).getPersonID();
            String gender = DataCache.getInstance().getPeople().get(personID).getGender();
            if (gender.equalsIgnoreCase("m")) {
                maleEvents.put(tempEvent.getEventID(), tempEvent);
            }
            else {
                femaleEvents.put(tempEvent.getEventID(), tempEvent);
            }
        }
        Person root = DataCache.getInstance().getPeople().get(DataCache.getInstance().getPeople().get(DataCache.getInstance().getBasePerson()).getFatherID());
        getSideOfFamily(root, fatherSidePersons);

        root = DataCache.getInstance().getPeople().get(DataCache.getInstance().getPeople().get(DataCache.getInstance().getBasePerson()).getMotherID());
        getSideOfFamily(root, motherSidePersons);


        for (Map.Entry<String, Person> entry: fatherSidePersons.entrySet()) {
            Person tempPerson = entry.getValue();
            List<Event> tempEvents2 = DataCache.getInstance().getPersonEvents().get(tempPerson.getPersonID());
            for (Event event : tempEvents2) {
                fatherSideEvents.put(event.getEventID(), event);
            }
        }

        for (Map.Entry<String, Person> entry: motherSidePersons.entrySet()) {
            Person tempPerson = entry.getValue();
            List<Event> tempEvents2 = DataCache.getInstance().getPersonEvents().get(tempPerson.getPersonID());
            for (Event event : tempEvents2) {
                motherSideEvents.put(event.getEventID(), event);
            }
        }

        DataCache.getInstance().setMaleEvents(maleEvents);
        System.out.println(maleEvents.toString());
        DataCache.getInstance().setFemaleEvents(femaleEvents);
        DataCache.getInstance().setFatherSidePersons(fatherSidePersons);
        DataCache.getInstance().setMotherSidePersons(motherSidePersons);
        DataCache.getInstance().setFatherSideEvents(fatherSideEvents);
        DataCache.getInstance().setMotherSideEvents(motherSideEvents);
    }

    private void getSideOfFamily (Person root, Map<String, Person> tempMap) {
        if (root.getFatherID()!= null) {
            Person father = DataCache.getInstance().getPeople().get(root.getFatherID());
            tempMap.put(root.getFatherID(), father);
            getSideOfFamily(father, tempMap);
        }
        if (root.getMotherID()!=null) {
            Person mother = DataCache.getInstance().getPeople().get(root.getMotherID());
            tempMap.put(root.getMotherID(), mother);
            getSideOfFamily(mother, tempMap);
        }
        return;
    }

    private void sendMessage(boolean succeeded) {
        Message message = Message.obtain();

        Bundle messageBundle = new Bundle();
        messageBundle.putBoolean(SUCCEED_KEY, succeeded);
        message.setData(messageBundle);

        messageHandler.sendMessage(message);
    }
}