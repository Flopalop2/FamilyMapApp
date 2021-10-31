package com.mbrow233.familymap;

import android.content.Context;

import androidx.annotation.NonNull;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.maps.android.clustering.Cluster;
import com.google.maps.android.clustering.ClusterItem;
import com.google.maps.android.clustering.ClusterManager;
import com.google.maps.android.clustering.view.DefaultClusterRenderer;
import com.mbrow233.familymap.data.DataCache;

import java.util.ArrayList;
import java.util.List;

public class MarkerClusterRenderer<T extends ClusterItem> extends DefaultClusterRenderer<T> {
    float[] colors = {BitmapDescriptorFactory.HUE_AZURE, BitmapDescriptorFactory.HUE_BLUE,
            BitmapDescriptorFactory.HUE_CYAN, BitmapDescriptorFactory.HUE_GREEN, BitmapDescriptorFactory.HUE_MAGENTA,
            BitmapDescriptorFactory.HUE_ORANGE, BitmapDescriptorFactory.HUE_RED, BitmapDescriptorFactory.HUE_ROSE,
            BitmapDescriptorFactory.HUE_VIOLET, BitmapDescriptorFactory.HUE_YELLOW};


    public MarkerClusterRenderer(Context context, GoogleMap googleMap, ClusterManager<T> clusterManager){
        super(context, googleMap, clusterManager);
    }

    @Override
    protected boolean shouldRenderAsCluster(Cluster cluster) {
        return cluster.getSize() >= 2;
    }

    @Override
    protected void onBeforeClusterItemRendered(@NonNull T item, @NonNull MarkerOptions markerOptions) {
        super.onBeforeClusterItemRendered(item, markerOptions);

        List<String> stringsList = new ArrayList<>(DataCache.getInstance().getEventTypes());
        int colorIndex = stringsList.indexOf(item.getSnippet().toUpperCase()) % 10;
        float hue = colors[colorIndex];
        markerOptions.icon(BitmapDescriptorFactory.defaultMarker(hue));
    }
}