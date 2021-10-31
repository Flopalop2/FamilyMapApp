package com.mbrow233.familymap.data;

import Model.Person;

public class PersonItem {
    private Person person;
    private String relation;

    public PersonItem(Person person, String relation) {
        this.person = person;
        this.relation = relation;
    }

    public Person getPerson() {
        return person;
    }

    public void setPerson(Person person) {
        this.person = person;
    }

    public String getRelation() {
        return relation;
    }

    public void setRelation(String relation) {
        this.relation = relation;
    }
}
