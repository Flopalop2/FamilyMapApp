package com.mbrow233.familymap.data;


import com.google.android.gms.maps.GoogleMap;
import com.google.maps.android.clustering.Cluster;

import Model.Person;
import Model.Event;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class DataCache {
    private static DataCache instance = new DataCache();

    public static DataCache getInstance(){
        return instance;
    }

    public Map<String, Person> getPeople() {
        return people;
    }

    public void setPeople(Map<String, Person> people) {
        this.people = people;
    }

    public Map<String, Event> getEvents() {
        return events;
    }

    public void setEvents(Map<String, Event> events) {
        this.events = events;
    }

    private Map<String, Person> people;
    private Map<String, Event> events;
    private Map<String, List<Event>> personEvents;
    private Map<String, String> eventPersons;
    private Set<String> eventTypes;

    public String getBasePerson() {
        return basePerson;
    }

    public void setBasePerson(String basePerson) {
        this.basePerson = basePerson;
    }

    private String basePerson;

    private Map<String, Event> maleEvents;
    private Map<String, Event> femaleEvents;
    private Map<String, Person> fatherSidePersons;
    private Map<String, Person> motherSidePersons;
    private Map<String, Event> fatherSideEvents;
    private Map<String, Event> motherSideEvents;

    public Map<String, Event> getMaleEvents() {
        return maleEvents;
    }

    public void setMaleEvents(Map<String, Event> maleEvents) {
        this.maleEvents = maleEvents;
    }

    public Map<String, Event> getFemaleEvents() {
        return femaleEvents;
    }

    public void setFemaleEvents(Map<String, Event> femaleEvents) {
        this.femaleEvents = femaleEvents;
    }

    public Map<String, Person> getFatherSidePersons() {
        return fatherSidePersons;
    }

    public void setFatherSidePersons(Map<String, Person> fatherSidePersons) {
        this.fatherSidePersons = fatherSidePersons;
    }

    public Map<String, Person> getMotherSidePersons() {
        return motherSidePersons;
    }

    public void setMotherSidePersons(Map<String, Person> motherSidePersons) {
        this.motherSidePersons = motherSidePersons;
    }

    public Map<String, Event> getFatherSideEvents() {
        return fatherSideEvents;
    }

    public void setFatherSideEvents(Map<String, Event> fatherSideEvents) {
        this.fatherSideEvents = fatherSideEvents;
    }

    public Map<String, Event> getMotherSideEvents() {
        return motherSideEvents;
    }

    public void setMotherSideEvents(Map<String, Event> motherSideEvents) {
        this.motherSideEvents = motherSideEvents;
    }

    public GoogleMap getMap() {
        return map;
    }

    public void setMap(GoogleMap map) {
        this.map = map;
    }

    private GoogleMap map;

    public String getCurEvent() {
        return curEvent;
    }

    public void setCurEvent(String curEvent) {
        this.curEvent = curEvent;
    }

    private String curEvent;

    public Cluster<MyClusterItem> getCurCluster() {
        return curCluster;
    }

    public void setCurCluster(Cluster<MyClusterItem> curCluster) {
        this.curCluster = curCluster;
    }

    private Cluster<MyClusterItem> curCluster;
    private String authToken;

    public Set<String> getEventTypes() {
        return eventTypes;
    }

    public void setEventTypes(Set<String> eventTypes) {
        this.eventTypes = eventTypes;
    }

    private String userID;

    public Map<String, List<Event>> getPersonEvents() {
        return personEvents;
    }

    public void setPersonEvents(Map<String, List<Event>> personEvents) {
        this.personEvents = personEvents;
    }

    public Map<String, String> getEventPersons() {
        return eventPersons;
    }

    public void setEventPersons(Map<String, String> eventPersons) {
        this.eventPersons = eventPersons;
    }

    private DataCache() {
        people = new HashMap<>();
        events = new HashMap<>();
        personEvents = new HashMap<>();
        eventTypes = new HashSet<>();
        eventPersons = new HashMap<>();
    }

}
