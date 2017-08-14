package com.byteshaft.projecthunger.fragments;

import android.graphics.Color;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.Interpolator;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.byteshaft.projecthunger.MainActivity;
import com.byteshaft.projecthunger.R;
import com.byteshaft.projecthunger.WelcomeActivity;
import com.byteshaft.projecthunger.utils.AppGlobals;
import com.byteshaft.projecthunger.utils.DatabaseHelpers;
import com.byteshaft.projecthunger.utils.Helpers;
import com.byteshaft.projecthunger.utils.WebServiceHelpers;
import com.byteshaft.requests.HttpRequest;
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
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.EOFException;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;

import static com.byteshaft.projecthunger.MainActivity.fragmentManager;
import static com.byteshaft.projecthunger.MainActivity.selectedProjectType;
import static com.byteshaft.projecthunger.R.id.rl_map_info_window;

/**
 * Created by fi8er1 on 04/02/2017.
 */

public class MapsFragment extends Fragment implements View.OnClickListener {

    public static ArrayList<String> placesIDsList;
    public static HashMap<String, ArrayList<String>> hashMapPlacesData;
    public static int responseCode;
    public static boolean isMapFragmentRunning;
    public static boolean addToFavoritesPending;
    static ImageButton ibMapMarkerCustomInfoFavorite;
    private static GoogleMap mMap = null;
    View baseViewMapFragment;
    ImageButton ibMapHelp;
    boolean bounce;
    Animation animMapInfoWindowIn;
    Animation animMapInfoWindowOut;
    Animation animMapInfoLogoCompleteFading;
    ImageView ivMapFragmentInfoLogo;
    RelativeLayout rlMapFragmentInfoWindow;
    String searchRadius = "50000";
    String searchPlacesApiKey = "AIzaSyDevj8C58BdLPwmEdIqCgxZZZbuQEV69GY";
    String searchLocation;
    String markerInFocusID = "";
    String formattedAddressForDatabase = null;
    DatabaseHelpers mDatabaseHelpers;
    ImageButton ibMapMarkerCustomInfoCall;
    ImageButton ibMapMarkerCustomInfoMenu;
    ImageButton ibMapMarkerCustomInfoRoute;
    RatingBar rbMapMarkerCustomInfoWindowRatingBar;
    RatingBar rbMapMarkerCustomInfoWindowRatingBarIndicator;
    TextView tvMapMarkerCustomInfoWindowPlaceName;
    TextView tvMapMarkerCustomInfoWindowOpenedClosed;
    TextView tvMapMarkerCustomInfoWindowNumberOfRatings;
    TextView tvMapMarkerCustomInfoWindowRateIt;
    LinearLayout llMapMarkerInfoWindow;
    LinearLayout llMapMarkerInfoWindowRating;
    HttpURLConnection connection;
    boolean sameMarkerClickedAgain;
    boolean isGetPizzaPlacesTaskRunning;
    boolean isGetFormattedAddressTaskRunning;
    GetPizzaPlaces taskGetPizzaPlaces;
    GetFormattedAddress taskGetFormattedAddress;
    Runnable initiateCall = new Runnable() {
        public void run() {
            Helpers.initiateCallIntent(MainActivity.getInstance(), hashMapPlacesData.get(markerInFocusID).get(6));
        }
    };
    Runnable showMenu = new Runnable() {
        public void run() {
            Helpers.loadFragment(fragmentManager, new FoodMenuFragment(), "FoodMenuFragment");
        }
    };
    private boolean isThisPizzeriaAddedToFavorites;
    Runnable addToFavorites = new Runnable() {
        public void run() {
            if (isGetFormattedAddressTaskRunning) {
                Helpers.showProgressDialog(MainActivity.getInstance(), "Retrieving address");
                addToFavoritesPending = true;
            } else {
                addToFavoritesStuff();
            }
        }
    };
    Runnable removeFromFavorites = new Runnable() {
        public void run() {
            mDatabaseHelpers.deleteEntry(markerInFocusID);
            isThisPizzeriaAddedToFavorites = false;
            ibMapMarkerCustomInfoFavorite.setBackgroundResource(R.mipmap.ic_map_marker_custom_window_favorite_add);
            Toast.makeText(MainActivity.getInstance(), "Removed from favorites", Toast.LENGTH_SHORT).show();
        }
    };
    private boolean isRatingChangedByUser;
    Runnable ratePizzeria = new Runnable() {
        public void run() {
            sendRatingRequest();
        }
    };
    private RoutingListener mRoutingListener;
    private boolean routeBuildExecuted;
    private Menu actionsMenu;
    private boolean isAutoCameraPanOnMarkerClick;
    private boolean isMapMarkerInfoWindowShown;
    private boolean cameraAnimatedToCurrentLocation;
    private LatLng currentLatLngAuto;
    Runnable navigate = new Runnable() {
        public void run() {
            Helpers.showProgressDialog(MainActivity.getInstance(), "Establishing route");
            mMap.clear();
            mMap.addMarker(new MarkerOptions().position(Helpers.latLngForNavigation)
                    .icon(BitmapDescriptorFactory.fromResource(getAppropriateMapMarkerIconImageID(MainActivity.selectedProjectType, 1))));
            Routing routing = new Routing.Builder()
                    .travelMode(Routing.TravelMode.DRIVING)
                    .withListener(mRoutingListener)
                    .waypoints(currentLatLngAuto, Helpers.latLngForNavigation)
                    .build();
            routing.execute();
            routeBuildExecuted = true;
        }
    };
    private Animation animLayoutInfoWindowBottomUp;
    private Animation animLayoutInfoWindowBottomDown;
    private boolean simpleMapView = true;
    private GoogleMap.OnMyLocationChangeListener myLocationChangeListener = new GoogleMap.OnMyLocationChangeListener() {
        @Override
        public void onMyLocationChange(Location location) {
            Helpers.dismissProgressDialog();
            currentLatLngAuto = new LatLng(location.getLatitude(), location.getLongitude());
            if (!cameraAnimatedToCurrentLocation) {
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLatLngAuto, 15.0f));
                cameraAnimatedToCurrentLocation = true;
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (isMapFragmentRunning) {
                            taskGetPizzaPlaces = (GetPizzaPlaces) new GetPizzaPlaces().execute();
                        }
                    }
                }, 2500);
            }
        }
    };

    public static String getStringForReview(
            String name, String location, String review_stars, String reviewer_id) {

        JSONObject json = new JSONObject();
        try {
            json.put("name", name);
            json.put("location", location);
            json.put("review_stars", review_stars);
            json.put("reviewer_id", reviewer_id);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return json.toString();
    }

    private String returnProperSearchTerm() {
        String searchTerm = null;
        if (Helpers.getAppropriateProjectName(selectedProjectType).equals("Pepperoni")) {
            searchTerm = "pizza";
        } else if (Helpers.getAppropriateProjectName(selectedProjectType).equals("Taco")) {
            searchTerm = "mexican food";
        } else if (Helpers.getAppropriateProjectName(selectedProjectType).equals("Buffalo")) {
            searchTerm = "wings";
        } else if (Helpers.getAppropriateProjectName(selectedProjectType).equals("Grill")) {
            searchTerm = "burger";
        }
        return searchTerm;
    }

    private void addToFavoritesStuff() {
        String rating;
        String timings;
        String formattedAddress;

        String businessTypeForAddingFavorites;

        if (hashMapPlacesData.get(markerInFocusID).get(8) != null) {
            businessTypeForAddingFavorites = hashMapPlacesData.get(markerInFocusID).get(8);
        } else {
//            String[] dummyStringArrayToSetBusinessType = new String[]{"false", "false", "false", "false"};
//            dummyStringArrayToSetBusinessType[selectedProjectType] = "true";
//            businessTypeForAddingFavorites = Arrays.toString(dummyStringArrayToSetBusinessType);
            businessTypeForAddingFavorites = "null";
        }


        if (hashMapPlacesData.get(markerInFocusID).get(2) != null) {
            if (!hashMapPlacesData.get(markerInFocusID).get(2).equalsIgnoreCase("0")) {
                rating = hashMapPlacesData.get(markerInFocusID).get(2);
            } else {
                rating = null;
            }
        } else {
            rating = null;
        }

        if (hashMapPlacesData.get(markerInFocusID).get(3) != null) {
            if (hashMapPlacesData.get(markerInFocusID).get(3).equalsIgnoreCase("true") ||
                    hashMapPlacesData.get(markerInFocusID).get(3).equalsIgnoreCase("false")) {
                timings = null;
            } else {
                timings = hashMapPlacesData.get(markerInFocusID).get(3);
            }
        } else {
            timings = null;
        }

        if (hashMapPlacesData.get(markerInFocusID).get(4) != null) {
            formattedAddress = hashMapPlacesData.get(markerInFocusID).get(4);
        } else {
            formattedAddress = formattedAddressForDatabase;
        }

        try {
            formattedAddress = formattedAddress.replace(", United States", "");
        } catch (Exception ignored) {
        }

        mDatabaseHelpers.createNewEntry(markerInFocusID, businessTypeForAddingFavorites, hashMapPlacesData.get(markerInFocusID).get(0),
                hashMapPlacesData.get(markerInFocusID).get(1), formattedAddress, timings,
                rating, hashMapPlacesData.get(markerInFocusID).get(5),
                hashMapPlacesData.get(markerInFocusID).get(6), hashMapPlacesData.get(markerInFocusID).get(7));

        ibMapMarkerCustomInfoFavorite.setBackgroundResource(R.mipmap.ic_map_marker_custom_window_favorite_remove);
        isThisPizzeriaAddedToFavorites = true;

        DatabaseHelpers mDatabaseHelpers = new DatabaseHelpers(getActivity());
        Log.i("completeDatabase", "" + mDatabaseHelpers.getAllRecords());
        Toast.makeText(MainActivity.getInstance(), "Added to favorites", Toast.LENGTH_SHORT).show();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        baseViewMapFragment = inflater.inflate(R.layout.fragment_map, container, false);

        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager()
                .findFragmentById(R.id.map_main);
        mapFragment.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap googleMap) {
                mMap = googleMap;
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(36.778259, -119.417931), 4.0f));
                Helpers.showProgressDialog(MainActivity.getInstance(), "Acquiring current location");
                mMap.setMyLocationEnabled(true);
                mMap.getUiSettings().setMyLocationButtonEnabled(false);
                mMap.getUiSettings().setCompassEnabled(true);
                mMap.setOnMyLocationChangeListener(myLocationChangeListener);
                mMap.getUiSettings().setMapToolbarEnabled(false);

                mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
                    @Override
                    public boolean onMarkerClick(final Marker marker) {
                        isAutoCameraPanOnMarkerClick = true;
                        bounce = false;
                        sameMarkerClickedAgain = markerInFocusID.equalsIgnoreCase(marker.getSnippet());
                        markerInFocusID = marker.getSnippet();
                        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(marker.getPosition(), 16.0f), new GoogleMap.CancelableCallback() {
                            @Override
                            public void onFinish() {
                                isAutoCameraPanOnMarkerClick = false;
                                bounce = true;
                                setMarkerBounce(marker);
                            }

                            @Override
                            public void onCancel() {
                                isAutoCameraPanOnMarkerClick = false;
                            }
                        });
                        cameraAnimatedToCurrentLocation = true;
                        if (!isMapMarkerInfoWindowShown) {
                            populateInfoWindowItems(markerInFocusID);
                            llMapMarkerInfoWindow.setVisibility(View.VISIBLE);
                            llMapMarkerInfoWindow.startAnimation(animLayoutInfoWindowBottomUp);
                            isMapMarkerInfoWindowShown = true;
                        } else if (!sameMarkerClickedAgain) {
                            llMapMarkerInfoWindow.setVisibility(View.GONE);
                            rbMapMarkerCustomInfoWindowRatingBar.setVisibility(View.GONE);
                            isRatingChangedByUser = false;
                            new Handler().postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    isRatingChangedByUser = false;
                                    rbMapMarkerCustomInfoWindowRatingBar.setRating(0);
                                }
                            }, 250);
                            llMapMarkerInfoWindow.startAnimation(animLayoutInfoWindowBottomDown);
                            isMapMarkerInfoWindowShown = false;
                            new Handler().postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    populateInfoWindowItems(markerInFocusID);
                                    llMapMarkerInfoWindow.setVisibility(View.VISIBLE);
                                    llMapMarkerInfoWindow.startAnimation(animLayoutInfoWindowBottomUp);
                                    isMapMarkerInfoWindowShown = true;
                                }
                            }, 500);
                        }
                        return true;
                    }
                });

                mMap.setOnCameraMoveListener(new GoogleMap.OnCameraMoveListener() {
                    @Override
                    public void onCameraMove() {
                        if (isMapMarkerInfoWindowShown && !isAutoCameraPanOnMarkerClick) {
                            bounce = false;
                            llMapMarkerInfoWindow.setVisibility(View.GONE);
                            rbMapMarkerCustomInfoWindowRatingBar.setVisibility(View.GONE);
                            isRatingChangedByUser = false;
                            new Handler().postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    rbMapMarkerCustomInfoWindowRatingBar.setRating(0);
                                }
                            }, 250);
                            llMapMarkerInfoWindow.startAnimation(animLayoutInfoWindowBottomDown);
                            isMapMarkerInfoWindowShown = false;
                        }
                    }
                });
            }
        });
        mDatabaseHelpers = new DatabaseHelpers(MainActivity.getInstance());

        rlMapFragmentInfoWindow = (RelativeLayout) baseViewMapFragment.findViewById(rl_map_info_window);
        llMapMarkerInfoWindow = (LinearLayout) baseViewMapFragment.findViewById(R.id.ll_map_marker_info_window);
        llMapMarkerInfoWindowRating = (LinearLayout) baseViewMapFragment.findViewById(R.id.ll_map_marker_info_window_rating);
        llMapMarkerInfoWindowRating.setOnClickListener(this);
        animLayoutInfoWindowBottomUp = AnimationUtils.loadAnimation(MainActivity.getInstance(), R.anim.anim_bottom_up);
        animLayoutInfoWindowBottomUp.setFillAfter(true);
        animLayoutInfoWindowBottomDown = AnimationUtils.loadAnimation(MainActivity.getInstance(), R.anim.anim_bottom_down);
        animLayoutInfoWindowBottomDown.setFillAfter(true);
        animMapInfoWindowIn = AnimationUtils.loadAnimation(MainActivity.getInstance(), R.anim.anim_transition_fragment_slide_left_enter);
        animMapInfoWindowIn.setFillAfter(true);
        animMapInfoWindowOut = AnimationUtils.loadAnimation(MainActivity.getInstance(), R.anim.anim_transition_fragment_slide_right_exit);
        animMapInfoWindowOut.setFillAfter(true);
        animMapInfoLogoCompleteFading = AnimationUtils.loadAnimation(MainActivity.getInstance(), R.anim.anim_text_complete_fading);
        animMapInfoLogoCompleteFading.setFillAfter(true);
        ivMapFragmentInfoLogo = (ImageView) baseViewMapFragment.findViewById(R.id.iv_map_fragment_info_logo);
        ibMapMarkerCustomInfoFavorite = (ImageButton) baseViewMapFragment.findViewById(R.id.ib_map_marker_custom_info_window_favorite);
        ibMapMarkerCustomInfoCall = (ImageButton) baseViewMapFragment.findViewById(R.id.ib_map_marker_custom_info_window_call);
        ibMapMarkerCustomInfoMenu = (ImageButton) baseViewMapFragment.findViewById(R.id.ib_map_marker_custom_info_window_menu);
        ibMapMarkerCustomInfoRoute = (ImageButton) baseViewMapFragment.findViewById(R.id.ib_map_marker_custom_info_window_route);
        rbMapMarkerCustomInfoWindowRatingBar = (RatingBar) baseViewMapFragment.findViewById(R.id.rb_map_marker_info_window_rating_bar);
        rbMapMarkerCustomInfoWindowRatingBarIndicator = (RatingBar) baseViewMapFragment.findViewById(R.id.rb_map_marker_info_window_rating_bar_indicator);
        tvMapMarkerCustomInfoWindowPlaceName = (TextView) baseViewMapFragment.findViewById(R.id.tv_map_marker_custom_info_window_title);
        tvMapMarkerCustomInfoWindowOpenedClosed = (TextView) baseViewMapFragment.findViewById(R.id.tv_map_marker_custom_info_window_opening_closing_time);
        tvMapMarkerCustomInfoWindowNumberOfRatings = (TextView) baseViewMapFragment.findViewById(R.id.tv_map_marker_custom_info_window_number_of_ratings);
        tvMapMarkerCustomInfoWindowRateIt = (TextView) baseViewMapFragment.findViewById(R.id.tv_map_marker_custom_info_window_rate_it);
        ibMapHelp = (ImageButton) baseViewMapFragment.findViewById(R.id.ib_map_help);


        animLayoutInfoWindowBottomUp.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                ibMapMarkerCustomInfoFavorite.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }
        });

        animLayoutInfoWindowBottomDown.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                ibMapMarkerCustomInfoFavorite.setVisibility(View.GONE);
                rbMapMarkerCustomInfoWindowRatingBar.setVisibility(View.GONE);
            }

            @Override
            public void onAnimationEnd(Animation animation) {}

            @Override
            public void onAnimationRepeat(Animation animation) {}
        });

        animMapInfoWindowIn.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                ibMapHelp.setVisibility(View.GONE);
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                ivMapFragmentInfoLogo.startAnimation(animMapInfoLogoCompleteFading);
                rlMapFragmentInfoWindow.setClickable(true);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {}
        });

        animMapInfoWindowOut.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                ivMapFragmentInfoLogo.clearAnimation();
                rlMapFragmentInfoWindow.setClickable(false);
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                AppGlobals.setMapFirstRun(false);
                ibMapHelp.setVisibility(View.VISIBLE);
                rlMapFragmentInfoWindow.setVisibility(View.GONE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {}
        });

        ibMapMarkerCustomInfoFavorite.setOnClickListener(this);
        llMapMarkerInfoWindowRating.setOnClickListener(this);
        ibMapMarkerCustomInfoCall.setOnClickListener(this);
        ibMapMarkerCustomInfoMenu.setOnClickListener(this);
        ibMapMarkerCustomInfoRoute.setOnClickListener(this);
        rlMapFragmentInfoWindow.setOnClickListener(this);
        ibMapHelp.setOnClickListener(this);

        rbMapMarkerCustomInfoWindowRatingBar.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {
            Timer ratingChangedTimer = new Timer();

            @Override
            public void onRatingChanged(final RatingBar ratingBar, float v, boolean b) {
                if (v >= 1) {
                    ratingChangedTimer.cancel();
                    ratingChangedTimer = new Timer();
                    ratingChangedTimer.schedule(new TimerTask() {
                        @Override
                        public void run() {
                            if (isMapFragmentRunning) {
                                MainActivity.getInstance().runOnUiThread(new Runnable() {
                                    public void run() {
                                        Helpers.AlertDialogWithPositiveFunctionNegativeButton(MainActivity.getInstance(), hashMapPlacesData.get(markerInFocusID).get(0),
                                                "Want to rate " + rbMapMarkerCustomInfoWindowRatingBar.getRating() + "/5.0", "Yes", "No", ratePizzeria);
                                    }
                                });
                            }
                        }
                    }, 250);
                } else if (v < 1) {
                    if (isRatingChangedByUser) {
                        rbMapMarkerCustomInfoWindowRatingBar.setRating(1);
                    }
                }
            }
        });

        mRoutingListener = new RoutingListener() {
            @Override
            public void onRoutingFailure() {
            }

            @Override
            public void onRoutingStart() {
            }

            @Override
            public void onRoutingSuccess(PolylineOptions polylineOptions, Route route) {
                actionsMenu.findItem(R.id.action_navigation_cancel).setVisible(true);
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
                Helpers.dismissProgressDialog();
                mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
            }

            @Override
            public void onRoutingCancelled() {
            }
        };
        MainActivity.getInstance().setTitle(Helpers.getAppropriateProjectName(MainActivity.selectedProjectType));
        setHasOptionsMenu(true);

        if (AppGlobals.isMapFirstRun()) {
            ibMapHelp.setVisibility(View.GONE);
        }

        return baseViewMapFragment;
    }


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_map, menu);
        actionsMenu = menu;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_current_location:

                /* Action button to move the map to user's current location. */

                if (Helpers.isAnyLocationServiceAvailable()) {
                    if (mMap != null) {
                        if (currentLatLngAuto != null) {
                            CameraPosition cameraPosition2 =
                                    new CameraPosition.Builder()
                                            .target(currentLatLngAuto)
                                            .zoom(16.0f)
                                            .build();
                            mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition2));
                        } else {
                            Toast.makeText(MainActivity.getInstance(), "Error: Location not available",
                                    Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(MainActivity.getInstance(), "Error: Map not ready",
                                Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(MainActivity.getInstance(), "Error: Location Service disabled",
                            Toast.LENGTH_SHORT).show();
                }
                return true;
            case R.id.action_change_map:

                /* Action button to change the google map view from Normal to Hybrid and vice versa. */

                if (simpleMapView) {
                    mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
                    setActionIcon(false);
                    simpleMapView = false;
                } else {
                    mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                    setActionIcon(true);
                    simpleMapView = true;
                }
                return true;
            case R.id.action_navigation_cancel:

                /* Action button to cancel navigation */

                actionsMenu.findItem(R.id.action_navigation_cancel).setVisible(false);

                mMap.clear();
                drawNearbyPlacesOnMapWithMarkers();
                return true;
            default:
        }
        return super.onOptionsItemSelected(item);
    }

    private void setActionIcon(boolean simpleMap) {

        /* Here the mapView change action button will be replaced with the respective icon. */

        MenuItem item = actionsMenu.findItem(R.id.action_change_map);
        if (actionsMenu != null) {
            if (simpleMap) {
                item.setIcon(R.mipmap.ic_action_map_satellite);
            } else {
                item.setIcon(R.mipmap.ic_action_map_simple);
            }
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        isMapFragmentRunning = false;
        if (isGetPizzaPlacesTaskRunning) {
            taskGetPizzaPlaces.cancel(true);
            Helpers.dismissProgressDialog();
        } else if (isGetFormattedAddressTaskRunning) {
            taskGetFormattedAddress.cancel(true);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        isMapFragmentRunning = true;
        if (!Helpers.isDeviceReadyForLocationAcquisition(getActivity())) {
            if (selectedProjectType != 4) {
                MainActivity.getInstance().onBackPressed();
            } else {
                Helpers.loadActivity(getActivity(), WelcomeActivity.class);
            }
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.ib_map_marker_custom_info_window_favorite:
                if (!isThisPizzeriaAddedToFavorites) {
                    addToFavoritesPending = false;
                    if (hashMapPlacesData.get(markerInFocusID).get(4) == null) {
                        taskGetFormattedAddress = (GetFormattedAddress) new GetFormattedAddress().execute();
                    }
                    Helpers.AlertDialogWithPositiveFunctionNegativeButton(MainActivity.getInstance(),
                            hashMapPlacesData.get(markerInFocusID).get(0), "Add to favorites?", "Yes", "No", addToFavorites);
                } else {
                    Helpers.AlertDialogWithPositiveFunctionNegativeButton(MainActivity.getInstance(),
                            hashMapPlacesData.get(markerInFocusID).get(0), "Remove from favorites?", "Yes", "No", removeFromFavorites);
                }
                break;
            case R.id.ib_map_marker_custom_info_window_call:
                Helpers.AlertDialogWithPositiveFunctionNegativeButton(MainActivity.getInstance(),
                        hashMapPlacesData.get(markerInFocusID).get(0), "Want to initiate a call?", "Yes", "No", initiateCall);
                break;
            case R.id.ib_map_marker_custom_info_window_menu:
                Helpers.urlFoodMenu = hashMapPlacesData.get(markerInFocusID).get(7);
                Helpers.AlertDialogWithPositiveFunctionNegativeButton(MainActivity.getInstance(),
                        hashMapPlacesData.get(markerInFocusID).get(0), "Want to see food menu?", "Yes", "No", showMenu);
                break;
            case R.id.ib_map_marker_custom_info_window_route:
                String[] stringToLatLng = hashMapPlacesData.get(markerInFocusID).get(1).split(",");
                double latitude = Double.parseDouble(stringToLatLng[0]);
                double longitude = Double.parseDouble(stringToLatLng[1]);
                Helpers.latLngForNavigation = new LatLng(latitude, longitude);
                Helpers.AlertDialogWithPositiveFunctionNegativeButton(MainActivity.getInstance(),
                        hashMapPlacesData.get(markerInFocusID).get(0), "Want to navigate?", "Yes", "No", navigate);
                break;
            case R.id.rl_map_info_window:
                rlMapFragmentInfoWindow.startAnimation(animMapInfoWindowOut);
                break;
            case R.id.ll_map_marker_info_window_rating:
                if (rbMapMarkerCustomInfoWindowRatingBar.getVisibility() == View.GONE) {
                    isRatingChangedByUser = true;
                    rbMapMarkerCustomInfoWindowRatingBar.setVisibility(View.VISIBLE);
                } else {
                    rbMapMarkerCustomInfoWindowRatingBar.setVisibility(View.GONE);
                    isRatingChangedByUser = false;
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            rbMapMarkerCustomInfoWindowRatingBar.setRating(0);
                        }
                    }, 250);
                }
                break;
            case R.id.ib_map_help:
                rlMapFragmentInfoWindow.startAnimation(animMapInfoWindowIn);
                rlMapFragmentInfoWindow.setVisibility(View.VISIBLE);
                break;
        }
    }

    private void drawNearbyPlacesOnMapWithMarkers() {
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLatLngAuto, 11.0f));
        for (int i = 0; i < placesIDsList.size(); i++) {
            try {
                String[] latLngString = hashMapPlacesData.get(placesIDsList.get(i)).get(1).split(",");
                double latitude = Double.parseDouble(latLngString[0]);
                double longitude = Double.parseDouble(latLngString[1]);
                LatLng latLngPlacePosition = new LatLng(latitude, longitude);
                if (hashMapPlacesData.get(placesIDsList.get(i)).get(2) != null &&
                        Float.parseFloat(hashMapPlacesData.get(placesIDsList.get(i)).get(2)) > 4.0) {
                    mMap.addMarker(new MarkerOptions().position(latLngPlacePosition)
                            .icon(BitmapDescriptorFactory.fromResource(getAppropriateMapMarkerIconImageID(MainActivity.selectedProjectType, 2)))
                            .snippet("" + placesIDsList.get(i)));
                } else if (hashMapPlacesData.get(placesIDsList.get(i)).get(2) != null &&
                        Float.parseFloat(hashMapPlacesData.get(placesIDsList.get(i)).get(2)) < 2.0 &&
                        Float.parseFloat(hashMapPlacesData.get(placesIDsList.get(i)).get(2)) != 0.0) {
                    mMap.addMarker(new MarkerOptions().position(latLngPlacePosition)
                            .icon(BitmapDescriptorFactory.fromResource(getAppropriateMapMarkerIconImageID(MainActivity.selectedProjectType, 0)))
                            .snippet("" + placesIDsList.get(i)));
                } else {
                    mMap.addMarker(new MarkerOptions().position(latLngPlacePosition)
                            .icon(BitmapDescriptorFactory.fromResource(getAppropriateMapMarkerIconImageID(MainActivity.selectedProjectType, 1)))
                            .snippet("" + placesIDsList.get(i)));
                }
            } catch (NullPointerException e) {
                e.printStackTrace();
            }
        }
    }

    private void populateInfoWindowItems(String placeID) {
        tvMapMarkerCustomInfoWindowPlaceName.setText(hashMapPlacesData.get(placeID).get(0));

        if (hashMapPlacesData.get(placeID).get(2) != null) {
            rbMapMarkerCustomInfoWindowRatingBarIndicator.setStepSize(0.1f);
            rbMapMarkerCustomInfoWindowRatingBarIndicator.setRating(Float.parseFloat(hashMapPlacesData.get(placeID).get(2)));
        } else {
            rbMapMarkerCustomInfoWindowRatingBarIndicator.setRating(0);
        }

        if (hashMapPlacesData.get(placeID).get(3) != null) {
            tvMapMarkerCustomInfoWindowOpenedClosed.setVisibility(View.VISIBLE);
            if (hashMapPlacesData.get(placeID).get(3).equalsIgnoreCase("true")) {
                tvMapMarkerCustomInfoWindowOpenedClosed.setText("Open Now");
                tvMapMarkerCustomInfoWindowOpenedClosed.setTextColor(Color.parseColor("#4CAF50"));
            } else if (hashMapPlacesData.get(placeID).get(3).equalsIgnoreCase("false")) {
                tvMapMarkerCustomInfoWindowOpenedClosed.setText("Closed Now");
                tvMapMarkerCustomInfoWindowOpenedClosed.setTextColor(Color.parseColor("#F44336"));
            } else {
                Log.i("total", " " + hashMapPlacesData.get(placeID).get(3));
                tvMapMarkerCustomInfoWindowOpenedClosed.setText(Helpers.getBusinessTimingsForCurrentDay(hashMapPlacesData.get(placeID).get(3)));
                tvMapMarkerCustomInfoWindowOpenedClosed.setTextColor(Color.parseColor("#000000"));
            }
        } else {
            tvMapMarkerCustomInfoWindowOpenedClosed.setVisibility(View.GONE);
        }

        if (hashMapPlacesData.get(placeID).get(5) != null) {
            tvMapMarkerCustomInfoWindowNumberOfRatings.setVisibility(View.VISIBLE);
            tvMapMarkerCustomInfoWindowNumberOfRatings.setText("(" + hashMapPlacesData.get(placeID).get(5) + ")");
        } else {
            tvMapMarkerCustomInfoWindowNumberOfRatings.setVisibility(View.GONE);
        }

        if (mDatabaseHelpers.entryExists(placeID)) {
            ibMapMarkerCustomInfoFavorite.setBackgroundResource(R.mipmap.ic_map_marker_custom_window_favorite_remove);
            isThisPizzeriaAddedToFavorites = true;
        } else {
            ibMapMarkerCustomInfoFavorite.setBackgroundResource(R.mipmap.ic_map_marker_custom_window_favorite_add);
            isThisPizzeriaAddedToFavorites = false;
        }

        if (hashMapPlacesData.get(placeID).get(6) != null) {
            ibMapMarkerCustomInfoCall.setVisibility(View.VISIBLE);
        } else {
            ibMapMarkerCustomInfoCall.setVisibility(View.GONE);
        }

        if (hashMapPlacesData.get(placeID).get(7) != null) {
            ibMapMarkerCustomInfoMenu.setVisibility(View.VISIBLE);
        } else {
            ibMapMarkerCustomInfoMenu.setVisibility(View.GONE);
        }

        if (hashMapPlacesData.get(markerInFocusID).get(8) != null) {
            tvMapMarkerCustomInfoWindowRateIt.setVisibility(View.VISIBLE);
            llMapMarkerInfoWindowRating.setOnClickListener(this);
        } else {
            tvMapMarkerCustomInfoWindowRateIt.setVisibility(View.GONE);
            llMapMarkerInfoWindowRating.setOnClickListener(null);
        }
    }

    private void sendRatingRequest() {
        HttpRequest request = new HttpRequest(MainActivity.getInstance());
        Helpers.showProgressDialog(MainActivity.getInstance(), "Sending rating request");
        request.setOnReadyStateChangeListener(new HttpRequest.OnReadyStateChangeListener() {
            @Override
            public void onReadyStateChange(HttpRequest request, int readyState) {
                switch (readyState) {
                    case HttpRequest.STATE_DONE:
                        switch (request.getStatus()) {
                            case HttpURLConnection.HTTP_CREATED:
                                Toast.makeText(MainActivity.getInstance(), "Successfully rated", Toast.LENGTH_SHORT).show();
                                rbMapMarkerCustomInfoWindowRatingBar.setVisibility(View.GONE);
                                isRatingChangedByUser = false;
                                rbMapMarkerCustomInfoWindowRatingBar.setRating(0);
                                break;
                            case HttpURLConnection.HTTP_BAD_REQUEST:
                                // Something was wrong
                                Helpers.AlertDialogMessage(MainActivity.getInstance(), "Error", request.getStatusText(), "Ok");
                                rbMapMarkerCustomInfoWindowRatingBar.setVisibility(View.GONE);
                                isRatingChangedByUser = false;
                                rbMapMarkerCustomInfoWindowRatingBar.setRating(0);
                                break;
                            case HttpURLConnection.HTTP_CONFLICT:
                                Helpers.AlertDialogMessage(MainActivity.getInstance(), "Rating Conflict", "This business is already rated by you.", "Ok");
                                rbMapMarkerCustomInfoWindowRatingBar.setVisibility(View.GONE);
                                isRatingChangedByUser = false;
                                rbMapMarkerCustomInfoWindowRatingBar.setRating(0);
                                break;
                        }
                }
                Helpers.dismissProgressDialog();
            }
        });

        request.setOnErrorListener(new HttpRequest.OnErrorListener() {
            @Override
            public void onError(HttpRequest request, short error, Exception exception) {
                if (exception instanceof EOFException) {
                    sendRatingRequest();
                }
            }
        });
        request.open("POST", "http://139.59.187.73/api/restaurants/review");
        request.send(getStringForReview(hashMapPlacesData.get(markerInFocusID).get(0), hashMapPlacesData.get(markerInFocusID).get(1),
                Float.toString(rbMapMarkerCustomInfoWindowRatingBar.getRating()), AppGlobals.getUniqueDeviceId()));
    }

    private int getAppropriateMapMarkerIconImageID(int projectType, int rating) {
        int id = -1;
        if (projectType == 0) {
            if (rating == 0) {
                id = R.mipmap.ic_marker_map_pizza_low_rating;
            } else if (rating == 1) {
                id = R.mipmap.ic_marker_map_pizza_normal_rating;
            } else if (rating == 2) {
                id = R.mipmap.ic_marker_map_pizza_high_rating;
            }
        } else if (projectType == 1) {
            if (rating == 0) {
                id = R.mipmap.ic_marker_map_wings_low_rating;
            } else if (rating == 1) {
                id = R.mipmap.ic_marker_map_wings_normal_rating;
            } else if (rating == 2) {
                id = R.mipmap.ic_marker_map_wings_high_rating;
            }
        } else if (projectType == 2) {
            if (rating == 0) {
                id = R.mipmap.ic_marker_map_burger_low_rating;
            } else if (rating == 1) {
                id = R.mipmap.ic_marker_map_burger_normal_rating;
            } else if (rating == 2) {
                id = R.mipmap.ic_marker_map_burger_high_rating;
            }
        } else if (projectType == 3) {
            if (rating == 0) {
                id = R.mipmap.ic_marker_map_taco_low_rating;
            } else if (rating == 1) {
                id = R.mipmap.ic_marker_map_taco_normal_rating;
            } else if (rating == 2) {
                id = R.mipmap.ic_marker_map_taco_high_rating;
            }
        } else if (projectType == 4) {
            if (rating == 0) {
                id = R.mipmap.ic_marker_map_late_low_rating;
            } else if (rating == 1) {
                id = R.mipmap.ic_marker_map_late_normal_rating;
            } else if (rating == 2) {
                id = R.mipmap.ic_marker_map_late_high_rating;
            }
        }
        return id;
    }

    private void setMarkerBounce(final Marker marker) {
        final Handler handler = new Handler();
        final long startTime = SystemClock.uptimeMillis();
        final long duration = 650;
        final Interpolator interpolator = new AccelerateDecelerateInterpolator();
        handler.post(new Runnable() {
            @Override
            public void run() {
                if (bounce) {
                    long elapsed = SystemClock.uptimeMillis() - startTime;
                    float t = Math.max(interpolator.getInterpolation((float) elapsed / duration), 0);
                    marker.setAnchor(0.5f, 1.0f + t);
                    handler.postDelayed(this, 12);
                } else {
                    handler.removeCallbacks(this);
                    marker.setAnchor(0.5f, 1.0f);
                }
            }
        });
    }

    private class GetPizzaPlaces extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            isGetPizzaPlacesTaskRunning = true;
            Helpers.showProgressDialog(MainActivity.getInstance(), "Retrieving nearby " + Helpers.getAppropriateProjectName(selectedProjectType));
        }

        @Override
        protected Void doInBackground(Void... params) {
            try {
                searchLocation = currentLatLngAuto.latitude + "," + currentLatLngAuto.longitude;
                ArrayList<String> latLngArray = new ArrayList<>();
                hashMapPlacesData = new HashMap<>();
                placesIDsList = new ArrayList<>();

                connection = WebServiceHelpers.openConnectionForUrl("http://139.59.187.73/api/restaurants/filter?radius=50&base_location=" + searchLocation, "GET");
                JSONArray jsonArrayDigitalOceans = new JSONArray(WebServiceHelpers.readResponse(connection));

                Log.e("selected", "" + selectedProjectType);
                Log.e("array", "" + jsonArrayDigitalOceans);

                if (selectedProjectType != 4) {
                    connection = WebServiceHelpers.openConnectionForUrl("https://maps.googleapis.com/maps/api/place/textsearch/" +
                            "json?query=" + returnProperSearchTerm() + "&location=" + searchLocation +
                            "&radius=" + searchRadius + "&key=" + searchPlacesApiKey, "GET");
                    responseCode = connection.getResponseCode();

                    JSONObject jsonObjectGooglePlacesApiMain = new JSONObject(WebServiceHelpers.readResponse(connection));
                    JSONArray jsonArrayMain = jsonObjectGooglePlacesApiMain.getJSONArray("results");

                    for (int i = 0; i < jsonArrayDigitalOceans.length(); i++) {
                        JSONObject jsonObjectDigitalOceans = jsonArrayDigitalOceans.getJSONObject(i);
                        String id = jsonObjectDigitalOceans.getString("id");
                        String[] businessType = jsonObjectDigitalOceans.getString("business_type").split(",");
                        if (!placesIDsList.contains(id) && Boolean.parseBoolean(businessType[selectedProjectType])) {
                            boolean matchFound = false;
                            int loopCount = 0;
                            placesIDsList.add(id);
                            ArrayList<String> arrayListDataStringDigitalOceans = new ArrayList<>();
                            arrayListDataStringDigitalOceans.add(jsonObjectDigitalOceans.getString("name"));
                            arrayListDataStringDigitalOceans.add(jsonObjectDigitalOceans.getString("location"));
                            latLngArray.add(jsonObjectDigitalOceans.getString("location"));
                            while (!matchFound && loopCount < jsonArrayMain.length()) {
                                JSONObject jsonObject = jsonArrayMain.getJSONObject(loopCount);
                                JSONObject jsonObjectGeometry = jsonObject.getJSONObject("geometry");
                                JSONObject jsonObjectLocation = jsonObjectGeometry.getJSONObject("location");
                                if (jsonObjectDigitalOceans.getString("location").equalsIgnoreCase(Helpers.formatLatLngToLimitCharacterLengthAndReturnInSingleString(
                                        jsonObjectLocation.getString("lat"), jsonObjectLocation.getString("lng")))) {
                                    matchFound = true;
                                    if (!jsonObjectDigitalOceans.getString("rating").equalsIgnoreCase("")) {
                                        float ratingDigitalOceans = Float.parseFloat(jsonObjectDigitalOceans.getString("rating"));
                                        if (jsonObject.has("rating")) {
                                            float ratingGooglePlacesAPI = Float.parseFloat(jsonObject.getString("rating"));
                                            arrayListDataStringDigitalOceans.add(Float.toString((ratingDigitalOceans + ratingGooglePlacesAPI) / 2));
                                        } else {
                                            arrayListDataStringDigitalOceans.add(Float.toString(ratingDigitalOceans));
                                        }
                                    } else {
                                        if (jsonObject.has("rating")) {
                                            arrayListDataStringDigitalOceans.add(jsonObject.getString("rating"));
                                        } else {
                                            arrayListDataStringDigitalOceans.add(null);
                                        }
                                    }

                                    if (jsonObjectDigitalOceans.getString("timings").equalsIgnoreCase("")) {
                                        JSONObject jsonObjectOpeningHours;
                                        if (jsonObject.has("opening_hours")) {
                                            jsonObjectOpeningHours = jsonObject.getJSONObject("opening_hours");
                                        } else {
                                            jsonObjectOpeningHours = null;
                                        }

                                        if (jsonObjectOpeningHours != null && jsonObjectOpeningHours.has("open_now")) {
                                            arrayListDataStringDigitalOceans.add(jsonObjectOpeningHours.getString("open_now"));
                                        } else {
                                            arrayListDataStringDigitalOceans.add(null);
                                        }

                                    } else {
                                        if (!jsonObjectDigitalOceans.getString("timings").equalsIgnoreCase("")) {
                                            arrayListDataStringDigitalOceans.add(jsonObjectDigitalOceans.getString("timings"));
                                        } else {
                                            arrayListDataStringDigitalOceans.add(null);
                                        }
                                    }

                                    if (jsonObject.has("formatted_address")) {
                                        arrayListDataStringDigitalOceans.add(jsonObject.getString("formatted_address"));
                                    } else {
                                        arrayListDataStringDigitalOceans.add(null);
                                    }
                                }
                                loopCount++;
                            }

                            if (!matchFound) {
                                if (!jsonObjectDigitalOceans.getString("rating").equalsIgnoreCase("")) {
                                    arrayListDataStringDigitalOceans.add(jsonObjectDigitalOceans.getString("rating"));
                                } else {
                                    arrayListDataStringDigitalOceans.add(null);
                                }

                                if (!jsonObjectDigitalOceans.getString("timings").equalsIgnoreCase("")) {
                                    arrayListDataStringDigitalOceans.add(jsonObjectDigitalOceans.getString("timings"));
                                } else {
                                    arrayListDataStringDigitalOceans.add(null);
                                }
                                arrayListDataStringDigitalOceans.add(null);
                            }

                            arrayListDataStringDigitalOceans.add(jsonObjectDigitalOceans.getString("review_count"));

                            if (!jsonObjectDigitalOceans.getString("contact").equalsIgnoreCase("null") &&
                                    !jsonObjectDigitalOceans.getString("contact").equalsIgnoreCase("")) {
                                arrayListDataStringDigitalOceans.add(jsonObjectDigitalOceans.getString("contact"));
                            } else {
                                arrayListDataStringDigitalOceans.add(null);
                            }

                            if (!jsonObjectDigitalOceans.getString("menu").equalsIgnoreCase("null")) {
                                arrayListDataStringDigitalOceans.add(jsonObjectDigitalOceans.getString("menu"));
                            } else {
                                arrayListDataStringDigitalOceans.add(null);
                            }

                            arrayListDataStringDigitalOceans.add(jsonObjectDigitalOceans.getString("business_type"));
                            hashMapPlacesData.put(id, arrayListDataStringDigitalOceans);
                        }
                    }

                    for (int i = 0; i < jsonArrayMain.length(); i++) {
                        JSONObject jsonObject = jsonArrayMain.getJSONObject(i);

                        JSONObject jsonObjectGeometry = jsonObject.getJSONObject("geometry");
                        JSONObject jsonObjectLocation = jsonObjectGeometry.getJSONObject("location");
                        JSONObject jsonObjectOpeningHours;
                        if (jsonObject.has("opening_hours")) {
                            jsonObjectOpeningHours = jsonObject.getJSONObject("opening_hours");
                        } else {
                            jsonObjectOpeningHours = null;
                        }
                        String id = jsonObject.getString("id");

                        if (!placesIDsList.contains(id) && !latLngArray.contains(Helpers.formatLatLngToLimitCharacterLengthAndReturnInSingleString(
                                jsonObjectLocation.getString("lat"), jsonObjectLocation.getString("lng")))) {
                            placesIDsList.add(id);
                            ArrayList<String> arrayListString = new ArrayList<>();
                            arrayListString.add(jsonObject.getString("name"));
                            arrayListString.add(Helpers.formatLatLngToLimitCharacterLengthAndReturnInSingleString(
                                    jsonObjectLocation.getString("lat"), jsonObjectLocation.getString("lng")));
                            if (jsonObject.has("rating")) {
                                arrayListString.add(jsonObject.getString("rating"));
                            } else {
                                arrayListString.add(null);
                            }

                            if (jsonObjectOpeningHours != null && jsonObjectOpeningHours.has("open_now")) {
                                arrayListString.add(jsonObjectOpeningHours.getString("open_now"));
                            } else {
                                arrayListString.add(null);
                            }

                            if (jsonObject.has("formatted_address")) {
                                arrayListString.add(jsonObject.getString("formatted_address"));
                            } else {
                                arrayListString.add(null);
                            }

                            arrayListString.add(null);
                            arrayListString.add(null);
                            arrayListString.add(null);
                            arrayListString.add(null);
                            hashMapPlacesData.put(id, arrayListString);
                        }
                    }
                } else {
                    for (int i = 0; i < jsonArrayDigitalOceans.length(); i++) {
                        JSONObject jsonObjectDigitalOceans = jsonArrayDigitalOceans.getJSONObject(i);
                        String id = jsonObjectDigitalOceans.getString("id");
                        String[] arrayOpeningAndClosingTimeForCurrentDay = Helpers.getBusinessTimingsForCurrentDay(jsonObjectDigitalOceans.getString("timings")).split("-");
                        if (!placesIDsList.contains(id) && Helpers.isCurrentTimeBetween(
                                Helpers.convertAmPmTo24HoursTime(arrayOpeningAndClosingTimeForCurrentDay[0]),
                                Helpers.convertAmPmTo24HoursTime(arrayOpeningAndClosingTimeForCurrentDay[1]))) {
                            placesIDsList.add(id);
                            ArrayList<String> arrayListDataStringDigitalOceans = new ArrayList<>();
                            arrayListDataStringDigitalOceans.add(jsonObjectDigitalOceans.getString("name"));
                            arrayListDataStringDigitalOceans.add(jsonObjectDigitalOceans.getString("location"));

                            if (!jsonObjectDigitalOceans.getString("rating").equalsIgnoreCase("")) {
                                arrayListDataStringDigitalOceans.add(jsonObjectDigitalOceans.getString("rating"));
                            } else {
                                arrayListDataStringDigitalOceans.add(null);
                            }

                            if (!jsonObjectDigitalOceans.getString("timings").equalsIgnoreCase("")) {
                                arrayListDataStringDigitalOceans.add(jsonObjectDigitalOceans.getString("timings"));
                            } else {
                                arrayListDataStringDigitalOceans.add(null);
                            }

                            arrayListDataStringDigitalOceans.add(null);

                            arrayListDataStringDigitalOceans.add(jsonObjectDigitalOceans.getString("review_count"));

                            if (!jsonObjectDigitalOceans.getString("contact").equalsIgnoreCase("null") &&
                                    !jsonObjectDigitalOceans.getString("contact").equalsIgnoreCase("")) {
                                arrayListDataStringDigitalOceans.add(jsonObjectDigitalOceans.getString("contact"));
                            } else {
                                arrayListDataStringDigitalOceans.add(null);
                            }

                            if (!jsonObjectDigitalOceans.getString("menu").equalsIgnoreCase("null")) {
                                arrayListDataStringDigitalOceans.add(jsonObjectDigitalOceans.getString("menu"));
                            } else {
                                arrayListDataStringDigitalOceans.add(null);
                            }

                            arrayListDataStringDigitalOceans.add(jsonObjectDigitalOceans.getString("business_type"));
                            hashMapPlacesData.put(id, arrayListDataStringDigitalOceans);
                        }
                    }
                }

                responseCode = connection.getResponseCode();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            isGetPizzaPlacesTaskRunning = false;
            if (responseCode == 200 && placesIDsList.size() > 0) {
                drawNearbyPlacesOnMapWithMarkers();
                if (AppGlobals.isMapFirstRun()) {
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            rlMapFragmentInfoWindow.startAnimation(animMapInfoWindowIn);
                            rlMapFragmentInfoWindow.setVisibility(View.VISIBLE);
                        }
                    }, 2000);
                }
            } else {
                Toast.makeText(MainActivity.getInstance(), "Unable to find " + Helpers.getAppropriateProjectName(selectedProjectType) + " nearby", Toast.LENGTH_LONG).show();
                MainActivity.getInstance().onBackPressed();
            }
            Helpers.dismissProgressDialog();
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
            isGetPizzaPlacesTaskRunning = false;
            Helpers.dismissProgressDialog();
        }
    }

    private class GetFormattedAddress extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            isGetFormattedAddressTaskRunning = true;
        }

        @Override
        protected Void doInBackground(Void... params) {
            String[] latLngString = hashMapPlacesData.get(markerInFocusID).get(1).split(",");
            double latitude = Double.parseDouble(latLngString[0]);
            double longitude = Double.parseDouble(latLngString[1]);
            LatLng latLngForAddress = new LatLng(latitude, longitude);
            formattedAddressForDatabase = Helpers.getAddress(MainActivity.getInstance(), latLngForAddress);
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            isGetFormattedAddressTaskRunning = false;
            if (addToFavoritesPending && isMapFragmentRunning) {
                Helpers.dismissProgressDialog();
                addToFavoritesStuff();
            }
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
            isGetFormattedAddressTaskRunning = false;
        }
    }

}