package com.mbrow233.familymap.data;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import Model.Event;
import Model.Person;

public class DataGenerator {
    public List<Event> getEvents(String curPersonID) {
        List<Event> events = DataCache.getInstance().getPersonEvents().get(curPersonID);
        List<Event> sorted = new ArrayList<>();

        Event birth = null;
        Event death = null;

        for (Event curEvent : events) {
            if(sorted.isEmpty()) {
                sorted.add(curEvent);
            }
            else {
                Iterator<Event> iterator = sorted.iterator();
                while(iterator.hasNext()) {
                    Event sortedEvent = (Event) iterator.next();

                    if (curEvent.getEventType().equalsIgnoreCase("Birth")) {
                        birth = curEvent;
                        break;
                    }
                    if (curEvent.getEventType().equalsIgnoreCase("Death")) {
                        death = curEvent;
                        break;
                    }
                    if (sortedEvent.getYear() == curEvent.getYear()) {
                        if (sortedEvent.getEventType().compareToIgnoreCase(curEvent.getEventType()) > 0) {
                            sorted.add(sorted.indexOf(sortedEvent), curEvent);
                            break;
                        }
                        else{
                            sorted.add(sorted.indexOf(sortedEvent) + 1, curEvent);
                            break;
                        }
                    }
                    else if (sortedEvent.getYear() > curEvent.getYear()) {
                        sorted.add(sorted.indexOf(sortedEvent), curEvent);
                        break;
                    }
                    if (!iterator.hasNext()) {
                        sorted.add(curEvent);
                        break;
                    }
                }
            }
        }

        if (birth != null) {
            sorted.add(0, birth);
        }
        if (death != null) {
            sorted.add(death);
        }

        return sorted;
    }

    public List<PersonItem> getPersons(String curPersonID) {
        List<PersonItem> personItems = new ArrayList<>();
        DataCache dataCache = DataCache.getInstance();

        Person curPerson = dataCache.getPeople().get(curPersonID);

        if(curPerson.getFatherID() != null) {
            personItems.add(new PersonItem(dataCache.getPeople().get(curPerson.getFatherID()), "Father"));
        }

        if(curPerson.getMotherID() != null) {
            personItems.add(new PersonItem(dataCache.getPeople().get(curPerson.getMotherID()), "Mother"));
        }

        if(curPerson.getSpouseID() != null) {
            personItems.add(new PersonItem(dataCache.getPeople().get(curPerson.getSpouseID()), "Spouse"));
        }

        Map<String, Person> people = dataCache.getPeople();
        for (Map.Entry<String, Person> entry : people.entrySet()) {
            Person tempPerson = entry.getValue();
            if (tempPerson.getFatherID() != null) {
                if (tempPerson.getFatherID().equals(curPersonID)) {
                    personItems.add(new PersonItem(tempPerson, "Child"));
                }
            }
            else if (tempPerson.getMotherID() != null) {
                if (tempPerson.getMotherID().equals(curPersonID)) {
                    personItems.add(new PersonItem(tempPerson, "Child"));
                }
            }
        }

        return personItems;
    }
}
