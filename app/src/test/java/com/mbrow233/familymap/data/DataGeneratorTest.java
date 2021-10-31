package com.mbrow233.familymap.data;

import junit.framework.TestCase;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import Model.Event;
import Model.Person;

import static org.junit.Assert.assertThrows;

public class DataGeneratorTest extends TestCase {

    DataGenerator dataGenerator;
    public void setUp() throws Exception {
        super.setUp();
        dataGenerator = new DataGenerator();
    }

    public void tearDown() throws Exception {
    }

    public void testGetEvents() {
        List<Event> testList = new ArrayList<>();
        testList.add(new Event("1234", "asdf", "o0s0v3t5f4x5y8h7", 10, 10, "country", "city", "Type", 1999));
        testList.add(new Event("1235", "asdf", "o0s0v3t5f4x5y8h7", 10, 10, "country", "city", "Type", 1999));
        Map<String, List<Event>> list = new HashMap<>();
        list.put("o0s0v3t5f4x5y8h7", testList);
        DataCache.getInstance().setPersonEvents(list);
        List<Event> eventList = dataGenerator.getEvents("o0s0v3t5f4x5y8h7");

        assertThrows(NullPointerException.class , ()->dataGenerator.getEvents("111"));
        assertEquals(2, eventList.size());

    }

    public void testGetPersons() {
        Person personItem = new Person("o0s0v3t5f4x5y8h7", "o0s0v3t5f4x5y8h7", "7", "10", "country", "city", "Type", "o0s0v3t5f4x5y8h7");
        Person personItem1 = new Person("o0s0v3t5f4x5y8h7", "o0s0v3t5f4x5y8h7", "10", "10", "country", "city", "Type", "o0s0v3t5f4x5y8h7");
        Map<String , Person> list = new HashMap<>();
        list.put("o0s0v3t5f4x5y8h7", personItem);
        list.put("1111", personItem1);
        DataCache.getInstance().setPeople(list);
        List<PersonItem> eventList = dataGenerator.getPersons("o0s0v3t5f4x5y8h7");

        assertThrows(NullPointerException.class , ()->dataGenerator.getPersons("111"));
        assertEquals(3, eventList.size());

    }
}