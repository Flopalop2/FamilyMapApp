package com.mbrow233.familymap.data;

import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.clustering.*;

import Model.Event;

public class MyClusterItem implements com.google.maps.android.clustering.ClusterItem {
    private final LatLng position;
    private final String title;
    private final String snippet;

    private final String eventID;

    public MyClusterItem(double lat, double lng, String title, String snippet, String eventID) {
        position = new LatLng(lat, lng);
        this.title = title;
        this.snippet = snippet;
        this.eventID = eventID;
    }

    @Override
    public LatLng getPosition() {
        return position;
    }

    @Override
    public String getTitle() {
        return title;
    }

    @Override
    public String getSnippet() {
        return snippet;
    }

    public String getEventID() {
        return eventID;
    }
}