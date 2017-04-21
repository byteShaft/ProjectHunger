package com.byteshaft.projecthunger.fragments;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.byteshaft.projecthunger.MainActivity;
import com.byteshaft.projecthunger.R;
import com.byteshaft.projecthunger.utils.Helpers;
import com.directions.route.Route;
import com.directions.route.Routing;
import com.directions.route.RoutingListener;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

/**
 * Created by fi8er1 on 27/11/2016.
 */

public class NavigateFragment extends Fragment {

    private static GoogleMap mMap = null;
    View baseViewNavigateFragment;
    private FragmentManager fm;
    private SupportMapFragment mapFragment;
    private RoutingListener mRoutingListener;
    private LatLng currentLatLngAuto = null;
    private boolean routeBuildExecuted;

    private GoogleMap.OnMyLocationChangeListener myLocationChangeListener = new GoogleMap.OnMyLocationChangeListener() {
        @Override
        public void onMyLocationChange(Location location) {
            Helpers.dismissProgressDialog();
            currentLatLngAuto = new LatLng(location.getLatitude(), location.getLongitude());
            if (!routeBuildExecuted) {
                Routing routing = new Routing.Builder()
                        .travelMode(Routing.TravelMode.DRIVING)
                        .withListener(mRoutingListener)
                        .waypoints(currentLatLngAuto, Helpers.latLngForNavigation)
                        .build();
                routing.execute();
                routeBuildExecuted = true;
                Helpers.showProgressDialog(getActivity(), "Establishing route");
            }
        }
    };

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        baseViewNavigateFragment = inflater.inflate(R.layout.fragment_navigate, container, false);
        fm = getChildFragmentManager();
        mapFragment = (SupportMapFragment) fm.findFragmentById(R.id.map_navigate);
        mapFragment.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap googleMap) {
                mMap = googleMap;
                if (ActivityCompat.checkSelfPermission(MainActivity.getInstance(),
                        Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                        ActivityCompat.checkSelfPermission(MainActivity.getInstance(),
                                Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
                    return;
                }
                mMap.setMyLocationEnabled(true);
                mMap.getUiSettings().setMyLocationButtonEnabled(false);
                mMap.getUiSettings().setCompassEnabled(true);
                mMap.setOnMyLocationChangeListener(myLocationChangeListener);
                mMap.getUiSettings().setMapToolbarEnabled(false);
                mMap.addMarker(new MarkerOptions().position(Helpers.latLngForNavigation)
                        .icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_marker_map_pizza_normal_rating)));
                CameraPosition cameraPosition =
                        new CameraPosition.Builder()
                                .target(Helpers.latLngForNavigation)
                                .zoom(14.0f)
                                .build();
                mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
            }
        });

        mRoutingListener = new RoutingListener() {
            @Override
            public void onRoutingFailure() {}

            @Override
            public void onRoutingStart() {}

            @Override
            public void onRoutingSuccess(PolylineOptions polylineOptions, Route route) {
                Helpers.dismissProgressDialog();
                mMap.addPolyline(new PolylineOptions()
                        .addAll(polylineOptions.getPoints())
                        .width(12)
                        .geodesic(true)
                        .color(Color.parseColor("#80000000")));

                mMap.addPolyline(new PolylineOptions()
                        .addAll(polylineOptions.getPoints())
                        .width(6)
                        .geodesic(true)
                        .color(Color.RED));

                CameraPosition cameraPosition = new CameraPosition.Builder()
                        .target(currentLatLngAuto)
                        .zoom(15.0f)
                        .build();
                mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
            }

            @Override
            public void onRoutingCancelled() {}
        };

        MainActivity.getInstance().setTitle("Navigate");
        return baseViewNavigateFragment;
    }

}
