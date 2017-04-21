package com.byteshaft.projecthunger.fragments;

import android.Manifest;
import android.app.TimePickerDialog;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.telephony.PhoneNumberUtils;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.byteshaft.projecthunger.MainActivity;
import com.byteshaft.projecthunger.R;
import com.byteshaft.projecthunger.utils.Helpers;
import com.byteshaft.requests.FormData;
import com.byteshaft.requests.HttpRequest;
import com.github.angads25.filepicker.controller.DialogSelectionListener;
import com.github.angads25.filepicker.model.DialogConfigs;
import com.github.angads25.filepicker.model.DialogProperties;
import com.github.angads25.filepicker.view.FilePickerDialog;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import static com.byteshaft.projecthunger.MainActivity.getInstance;
import static com.byteshaft.projecthunger.MainActivity.selectedProjectType;

/**
 * Created by fi8er1 on 17/11/2016.
 */

public class RegisterFragment extends android.support.v4.app.Fragment implements View.OnClickListener, HttpRequest.OnFileUploadProgressListener {
    public static String mapLocationPoint;
    public static LinearLayout llRegisterFragmentLocation;
    public static TextView tvRegisterFragmentBusinessLocation;
    private static GoogleMap mMap = null;
    private static LatLng currentLatLngAuto;
    LinearLayout llRegisterFragmentMain;
    View baseViewRegisterFragment;
    EditText etRegisterFragmentBusinessName;
    EditText etRegisterFragmentBusinessNumber;
    EditText etRegisterFragmentOwnersName;
    EditText etRegisterFragmentOwnersEmail;
    TextView tvRegisterFragmentRegisterLocationTitle;
    TextView tvRegisterFragmentMenuPDF;
    EditText etMapSearch;
    Marker mapLocationPointMarker;
    CheckBox cbRegisterFragmentBusinessTypePizza;
    CheckBox cbRegisterFragmentBusinessTypeWings;
    CheckBox cbRegisterFragmentBusinessTypeBurger;
    CheckBox cbRegisterFragmentBusinessTypeTaco;
    String businessName;
    String businessNumber;
    String businessTimings;
    String businessLocation;
    String businessOwnersName;
    String businessOwnersEmail;
    String businessMenuPDF;
    ScrollView svRegisterFragment;
    LinearLayout llRegisterFragmentLocationInfo;
    LinearLayout llRegisterFragmentMenuPDF;
    LinearLayout llRegisterFragmentBusinessTimingsOpeningTime;
    LinearLayout llRegisterFragmentBusinessTimingsClosingTime;
    RelativeLayout rlRegisterFragmentLocation;
    RelativeLayout rlRegisterFragmentTimings;
    CheckBox cbRegisterFragmentMenuPDF;
    ImageButton btnRegisterFragmentLocationCancel;
    ImageButton btnRegisterFragmentLocationDone;
    ImageButton btnRegisterFragmentLocationCurrent;
    ImageButton btnRegisterFragmentLocationSearch;
    RadioGroup rgRegisterFragmentBusinessTimingsInputType;
    RadioButton rbRegisterFragmentBusinessTimingsInputTypeSameForAllWeekDays;
    RadioButton rbRegisterFragmentBusinessTimingsInputTypeSetIndividually;
    ImageView ivRegisterFragmentMapTransparentOverlay;
    Button btnRegisterFragmentBusinessTimingsNavigationBack;
    Button btnRegisterFragmentBusinessTimingsNavigationDone;
    int intRegisterFragmentBusinessTimingsSelectedDayCount = 0;
    String[] sArrayRegisterFragmentBusinessTimingsWeekDays = new String[]{"Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday"};
    ArrayList<String> sArrayListRegisterFragmentBusinessTimingsWeekDays = new ArrayList<>(7);
    TextView tvRegisterFragmentBusinessTimings;
    TextView tvRegisterFragmentBusinessTimingsOpeningTime;
    TextView tvRegisterFragmentBusinessTimingsClosingTime;
    TextView tvRegisterFragmentBusinessTimingsHeader;
    TextView tvRegisterFragmentBusinessTimingsFooter;
    CheckBox cbRegisterFragmentBusinessTimingsHoliday;
    Button btnRegisterFragmentSubmitRequest;
    TextView tvRegisterFragmentBusinessTypeTitle;
    TextView tvRegisterFragmentLocationInfo;
    boolean isSearchEditTextVisible;
    boolean isTimingsSetByUser;
    int timePickerHour;
    int timePickerMinutes;
    Runnable retryRegistration = new Runnable() {
        public void run() {
            sendRegistrationRequest();
        }
    };
    private Animation animLayoutBottomUp;
    private String inputMapSearch;
    private boolean mapRegisterLocationAdded;
    private boolean isRegisterFragmentOpen;
    private boolean cameraAnimatedToCurrentLocation;
    private Animation animLayoutMapSearchBarFadeOut;
    private Animation animLayoutMapSearchBarFadeIn;
    private Animation animTextFading;
    private GoogleMap.OnMyLocationChangeListener myLocationChangeListener = new GoogleMap.OnMyLocationChangeListener() {
        @Override
        public void onMyLocationChange(Location location) {
            currentLatLngAuto = new LatLng(location.getLatitude(), location.getLongitude());
            if (!cameraAnimatedToCurrentLocation) {
                tvRegisterFragmentLocationInfo.clearAnimation();
                cameraAnimatedToCurrentLocation = true;
                if (!mapRegisterLocationAdded) {
                    tvRegisterFragmentLocationInfo.setText("Current Location Acquired");
                    tvRegisterFragmentLocationInfo.setTextColor(Color.parseColor("#A4C639"));
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLatLngAuto, 15.0f));
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            tvRegisterFragmentLocationInfo.setText("Tap and hold to set a Business Point");
                            tvRegisterFragmentLocationInfo.setTextColor(Color.parseColor("#ffffff"));
                            tvRegisterFragmentLocationInfo.clearAnimation();
                        }
                    }, 2000);
                }
            }
        }
    };

    public static String getStringForRegistrationWithoutFile(
            String name, String location, String contact,
            String businessType, String businessTimings, String ownerName, String ownerEmail) {
        JSONObject json = new JSONObject();
        try {
            json.put("name", name);
            json.put("contact", contact);
            json.put("timings", businessTimings);
            json.put("business_type", businessType);
            json.put("location", location);
            json.put("owner_name", ownerName);
            json.put("owner_email", ownerEmail);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return json.toString();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        baseViewRegisterFragment = inflater.inflate(R.layout.fragment_register, container, false);

        cbRegisterFragmentBusinessTypePizza = (CheckBox) baseViewRegisterFragment.findViewById(R.id.cb_fragment_register_business_type_pizza);
        cbRegisterFragmentBusinessTypeWings = (CheckBox) baseViewRegisterFragment.findViewById(R.id.cb_fragment_register_business_type_wings);
        cbRegisterFragmentBusinessTypeBurger = (CheckBox) baseViewRegisterFragment.findViewById(R.id.cb_fragment_register_business_type_burger);
        cbRegisterFragmentBusinessTypeTaco = (CheckBox) baseViewRegisterFragment.findViewById(R.id.cb_fragment_register_business_type_taco);

        checkSelectedProjectCheckBox(selectedProjectType);


        etRegisterFragmentBusinessName = (EditText) baseViewRegisterFragment.findViewById(R.id.et_fragment_register_business_name);
        etRegisterFragmentBusinessNumber = (EditText) baseViewRegisterFragment.findViewById(R.id.et_fragment_register_business_number);
        tvRegisterFragmentBusinessTypeTitle = (TextView) baseViewRegisterFragment.findViewById(R.id.tv_fragment_register_business_type_title);
        tvRegisterFragmentRegisterLocationTitle = (TextView) baseViewRegisterFragment.findViewById(R.id.tv_fragment_register_business_location_title);
        tvRegisterFragmentBusinessTimings = (TextView) baseViewRegisterFragment.findViewById(R.id.tv_fragment_register_business_timings);
        tvRegisterFragmentBusinessTimingsOpeningTime = (TextView) baseViewRegisterFragment.findViewById(R.id.tv_fragment_register_business_timings_opening_time);
        tvRegisterFragmentBusinessTimingsClosingTime = (TextView) baseViewRegisterFragment.findViewById(R.id.tv_fragment_register_business_timings_closing_time);
        tvRegisterFragmentBusinessTimingsHeader = (TextView) baseViewRegisterFragment.findViewById(R.id.tv_fragment_register_business_timings_header);
        tvRegisterFragmentBusinessTimingsFooter = (TextView) baseViewRegisterFragment.findViewById(R.id.tv_fragment_register_business_timings_footer);
        cbRegisterFragmentBusinessTimingsHoliday = (CheckBox) baseViewRegisterFragment.findViewById(R.id.cb_fragment_register_business_timings_holiday);
        rgRegisterFragmentBusinessTimingsInputType = (RadioGroup) baseViewRegisterFragment.findViewById(R.id.rg_fragment_register_business_timings_input_type);
        rbRegisterFragmentBusinessTimingsInputTypeSameForAllWeekDays = (RadioButton) baseViewRegisterFragment.findViewById(R.id.rb_fragment_register_business_timings_same_for_all_week_days);
        rbRegisterFragmentBusinessTimingsInputTypeSetIndividually = (RadioButton) baseViewRegisterFragment.findViewById(R.id.rb_fragment_register_business_timings_set_individually);
        tvRegisterFragmentBusinessLocation = (TextView) baseViewRegisterFragment.findViewById(R.id.tv_register_fragment_business_location);
        etRegisterFragmentOwnersName = (EditText) baseViewRegisterFragment.findViewById(R.id.et_fragment_register_owner_name);
        etRegisterFragmentOwnersEmail = (EditText) baseViewRegisterFragment.findViewById(R.id.et_fragment_register_owner_email);
        etMapSearch = (EditText) baseViewRegisterFragment.findViewById(R.id.et_map_search);
        tvRegisterFragmentMenuPDF = (TextView) baseViewRegisterFragment.findViewById(R.id.tv_fragment_register_pdf_menu);
        cbRegisterFragmentMenuPDF = (CheckBox) baseViewRegisterFragment.findViewById(R.id.cb_fragment_register_pdf);
        svRegisterFragment = (ScrollView) baseViewRegisterFragment.findViewById(R.id.sv_register_fragment);
        ivRegisterFragmentMapTransparentOverlay = (ImageView) baseViewRegisterFragment.findViewById(R.id.iv_register_fragment_map_transparent_overlay);
        llRegisterFragmentMain = (LinearLayout) baseViewRegisterFragment.findViewById(R.id.ll_fragment_register_main);
        llRegisterFragmentMenuPDF = (LinearLayout) baseViewRegisterFragment.findViewById(R.id.ll_fragment_register_pdf_menu);
        llRegisterFragmentLocationInfo = (LinearLayout) baseViewRegisterFragment.findViewById(R.id.ll_register_fragment_location_info);
        llRegisterFragmentBusinessTimingsOpeningTime = (LinearLayout) baseViewRegisterFragment.findViewById(R.id.ll_fragment_register_business_timings_opening_time);
        llRegisterFragmentBusinessTimingsClosingTime = (LinearLayout) baseViewRegisterFragment.findViewById(R.id.ll_fragment_register_business_timings_closing_time);
        rlRegisterFragmentLocation = (RelativeLayout) baseViewRegisterFragment.findViewById(R.id.rl_fragment_register_business_location);
        rlRegisterFragmentTimings = (RelativeLayout) baseViewRegisterFragment.findViewById(R.id.rl_fragment_register_business_timings);
        btnRegisterFragmentLocationCancel = (ImageButton) baseViewRegisterFragment.findViewById(R.id.btn_register_fragment_map_location_cancel);
        btnRegisterFragmentLocationDone = (ImageButton) baseViewRegisterFragment.findViewById(R.id.btn_fragment_register_map_done);
        btnRegisterFragmentLocationCurrent = (ImageButton) baseViewRegisterFragment.findViewById(R.id.btn_fragment_register_map_current_location);
        btnRegisterFragmentLocationSearch = (ImageButton) baseViewRegisterFragment.findViewById(R.id.btn_fragment_register_map_search);
        btnRegisterFragmentSubmitRequest = (Button) baseViewRegisterFragment.findViewById(R.id.btn_fragment_register_submit);
        btnRegisterFragmentBusinessTimingsNavigationBack = (Button) baseViewRegisterFragment.findViewById(R.id.btn_fragment_register_business_timings_back);
        btnRegisterFragmentBusinessTimingsNavigationDone = (Button) baseViewRegisterFragment.findViewById(R.id.btn_fragment_register_business_timings_done);
        tvRegisterFragmentLocationInfo = (TextView) baseViewRegisterFragment.findViewById(R.id.tv_register_fragment_location_info);
        animLayoutMapSearchBarFadeOut = AnimationUtils.loadAnimation(getActivity(), android.R.anim.fade_out);
        animLayoutMapSearchBarFadeIn = AnimationUtils.loadAnimation(getActivity(), android.R.anim.fade_in);
        animLayoutBottomUp = AnimationUtils.loadAnimation(getActivity(), R.anim.anim_bottom_up);
        animTextFading = AnimationUtils.loadAnimation(getActivity(), R.anim.anim_text_complete_fading);

        tvRegisterFragmentBusinessTimings.setOnClickListener(this);
        tvRegisterFragmentBusinessTimingsOpeningTime.setOnClickListener(this);
        tvRegisterFragmentBusinessTimingsClosingTime.setOnClickListener(this);
        btnRegisterFragmentLocationCancel.setOnClickListener(this);
        tvRegisterFragmentBusinessLocation.setOnClickListener(this);
        btnRegisterFragmentLocationDone.setOnClickListener(this);
        btnRegisterFragmentLocationCurrent.setOnClickListener(this);
        btnRegisterFragmentLocationSearch.setOnClickListener(this);
        btnRegisterFragmentSubmitRequest.setOnClickListener(this);
        tvRegisterFragmentMenuPDF.setOnClickListener(this);
        btnRegisterFragmentBusinessTimingsNavigationBack.setOnClickListener(this);
        btnRegisterFragmentBusinessTimingsNavigationDone.setOnClickListener(this);

        rgRegisterFragmentBusinessTimingsInputType.check(R.id.rb_fragment_register_business_timings_same_for_all_week_days);

        rgRegisterFragmentBusinessTimingsInputType.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                if (i == R.id.rb_fragment_register_business_timings_same_for_all_week_days) {
                    cbRegisterFragmentBusinessTimingsHoliday.setVisibility(View.GONE);
                    tvRegisterFragmentBusinessTimingsHeader.setVisibility(View.GONE);
                    tvRegisterFragmentBusinessTimingsFooter.setVisibility(View.GONE);
                    btnRegisterFragmentBusinessTimingsNavigationBack.setVisibility(View.GONE);
                    btnRegisterFragmentBusinessTimingsNavigationDone.setText("Done");
                    sArrayListRegisterFragmentBusinessTimingsWeekDays = new ArrayList<>();
                    intRegisterFragmentBusinessTimingsSelectedDayCount = 0;
                    cbRegisterFragmentBusinessTimingsHoliday.setChecked(false);
                    disableTimingsFields(false);
                } else if (i == R.id.rb_fragment_register_business_timings_set_individually) {
                    cbRegisterFragmentBusinessTimingsHoliday.setVisibility(View.VISIBLE);
                    btnRegisterFragmentBusinessTimingsNavigationBack.setVisibility(View.VISIBLE);
                    sArrayListRegisterFragmentBusinessTimingsWeekDays = new ArrayList<>();
                    cbRegisterFragmentBusinessTimingsHoliday.setText("Set " + sArrayRegisterFragmentBusinessTimingsWeekDays[intRegisterFragmentBusinessTimingsSelectedDayCount] +
                            " as a holiday");
                    tvRegisterFragmentBusinessTimingsHeader.setVisibility(View.VISIBLE);
                    tvRegisterFragmentBusinessTimingsHeader.setText(sArrayRegisterFragmentBusinessTimingsWeekDays[intRegisterFragmentBusinessTimingsSelectedDayCount]);
                    btnRegisterFragmentBusinessTimingsNavigationDone.setText("Next");
                    if (intRegisterFragmentBusinessTimingsSelectedDayCount == 0) {
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                btnRegisterFragmentBusinessTimingsNavigationBack.setEnabled(false);
                                btnRegisterFragmentBusinessTimingsNavigationBack.setAlpha(.2f);
                            }
                        }, 650);
                    } else {
                        btnRegisterFragmentBusinessTimingsNavigationBack.setEnabled(true);
                        btnRegisterFragmentBusinessTimingsNavigationBack.setAlpha(1);
                    }
                }
            }
        });

        cbRegisterFragmentBusinessTimingsHoliday.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                disableTimingsFields(b);
            }
        });

        ivRegisterFragmentMapTransparentOverlay.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                int action = event.getAction();
                switch (action) {
                    case MotionEvent.ACTION_DOWN:
                        svRegisterFragment.requestDisallowInterceptTouchEvent(true);
                        return false;
                    case MotionEvent.ACTION_UP:
                        svRegisterFragment.requestDisallowInterceptTouchEvent(false);
                        return true;
                    case MotionEvent.ACTION_MOVE:
                        svRegisterFragment.requestDisallowInterceptTouchEvent(true);
                        return false;
                    default:
                        return true;
                }
            }
        });

        cbRegisterFragmentMenuPDF.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b) {
                    tvRegisterFragmentMenuPDF.setText("");
                    llRegisterFragmentMenuPDF.setVisibility(View.VISIBLE);
                    tvRegisterFragmentMenuPDF.setError(null);
                } else {
                    llRegisterFragmentMenuPDF.setVisibility(View.GONE);
                }
            }
        });

        etMapSearch.addTextChangedListener(new TextWatcher() {
            Timer textChangeTimer = new Timer();

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                textChangeTimer.cancel();
                inputMapSearch = etMapSearch.getText().toString();
                if (inputMapSearch.length() > 2) {
                    textChangeTimer = new Timer();
                    textChangeTimer.schedule(new TimerTask() {
                        @Override
                        public void run() {
                            if (isRegisterFragmentOpen) {
                                Geocoder geocoder = new Geocoder(getActivity());
                                List<Address> addresses = null;
                                try {
                                    addresses = geocoder.getFromLocationName(inputMapSearch, 3);
                                    if (addresses != null && !addresses.equals("")) {
                                        searchAnimateCamera(addresses);
                                    }
                                } catch (Exception ignored) {
                                }
                            }
                        }
                    }, 1500);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        animLayoutMapSearchBarFadeOut.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                etMapSearch.setText("");
                isSearchEditTextVisible = false;
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }
        });

        animLayoutMapSearchBarFadeIn.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                svRegisterFragment.smoothScrollTo(0, tvRegisterFragmentRegisterLocationTitle.getBottom());
                isSearchEditTextVisible = true;
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }
        });

        MainActivity.getInstance().setTitle("Register");
        return baseViewRegisterFragment;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case 1: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    llRegisterFragmentLocation.callOnClick();
                } else {
                    Toast.makeText(getActivity(), "Location access permission denied", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_fragment_register_submit:
                businessName = etRegisterFragmentBusinessName.getText().toString();
                businessNumber = etRegisterFragmentBusinessNumber.getText().toString();
                businessTimings = "";
                int loopCount = 0;
                for (int i = 0; i < sArrayListRegisterFragmentBusinessTimingsWeekDays.size(); i++) {
                    if (loopCount > 0) {
                        businessTimings += ",";
                    }
                    businessTimings += sArrayListRegisterFragmentBusinessTimingsWeekDays.get(i);
                    loopCount++;
                }
                businessOwnersName = etRegisterFragmentOwnersName.getText().toString();
                businessOwnersEmail = etRegisterFragmentOwnersEmail.getText().toString();
                businessLocation = tvRegisterFragmentBusinessLocation.getText().toString();
                businessMenuPDF = tvRegisterFragmentMenuPDF.getText().toString();
                if (validateRegisterInfo()) {
                    sendRegistrationRequest();
                }
                break;
            case R.id.btn_register_fragment_map_location_cancel:
                if (mapRegisterLocationAdded) {
                    btnRegisterFragmentLocationCancel.setVisibility(View.GONE);
                    tvRegisterFragmentLocationInfo.setText("");
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            mapLocationPointMarker.remove();
                            mapRegisterLocationAdded = false;
                            tvRegisterFragmentLocationInfo.setText("Tap and hold to set a Business Point");
                        }
                    }, 300);
                }
                break;
            case R.id.tv_register_fragment_business_location:
                Helpers.onRecheckLocationAvailableTaskType = 1;
                if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) !=
                        PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getActivity(),
                        Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
                } else if (!Helpers.checkPlayServicesAvailability(getActivity())) {
                    Helpers.AlertDialogWithPositiveFunctionNegativeButton(getActivity(), "Location components missing",
                            "You need to install GooglePlayServices to continue", "Install",
                            "Dismiss", Helpers.openPlayServicesInstallation);
                } else if (!Helpers.isAnyLocationServiceAvailable()) {
                    Helpers.AlertDialogWithPositiveNegativeNeutralFunctions(getActivity(), "Location Service disabled",
                            "Enable device GPS to continue", "Settings", "ReCheck", "Dismiss",
                            Helpers.openLocationServiceSettings, Helpers.recheckLocationServiceStatus);
                } else {
                    if (rlRegisterFragmentLocation.getVisibility() == View.GONE && tvRegisterFragmentBusinessLocation.getText().toString().isEmpty()) {
                        rlRegisterFragmentLocation.setVisibility(View.VISIBLE);
                        tvRegisterFragmentBusinessLocation.setVisibility(View.GONE);
                        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager()
                                .findFragmentById(R.id.map_register_fragment);
                        mapFragment.getMapAsync(new OnMapReadyCallback() {
                            @Override
                            public void onMapReady(GoogleMap googleMap) {
                                mMap = googleMap;
                                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(22.685677, 79.408410), 4.0f));
                                mMap.setMyLocationEnabled(true);
                                mMap.getUiSettings().setMyLocationButtonEnabled(false);
                                mMap.getUiSettings().setCompassEnabled(true);
                                mMap.setOnMyLocationChangeListener(myLocationChangeListener);

                                new Handler().postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        llRegisterFragmentLocationInfo.setVisibility(View.VISIBLE);
                                        llRegisterFragmentLocationInfo.startAnimation(animLayoutBottomUp);
                                        tvRegisterFragmentLocationInfo.setText("Acquiring Current Location");
                                        tvRegisterFragmentLocationInfo.setAnimation(animTextFading);
                                    }
                                }, 500);

                                mMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
                                    @Override
                                    public void onMapLongClick(final LatLng latLng) {
                                        if (isSearchEditTextVisible) {
                                            setSearchBarVisibility(false);
                                        }
                                        if (!mapRegisterLocationAdded) {
                                            double latitude = latLng.latitude;
                                            double longitude = latLng.longitude;
                                            mapLocationPoint = Helpers.formatLatLngToLimitCharacterLengthAndReturnInSingleString(String.valueOf(latitude), String.valueOf(longitude));
                                            mapLocationPointMarker = mMap.addMarker(new MarkerOptions().position(latLng)
                                                    .icon(BitmapDescriptorFactory.fromResource(getAppropriateRegisterMapMarkerIconImageID(MainActivity.selectedProjectType))).snippet("-1"));
                                            mapRegisterLocationAdded = true;
                                            btnRegisterFragmentLocationCancel.setVisibility(View.VISIBLE);
                                            AsyncTask.execute(new Runnable() {
                                                @Override
                                                public void run() {
                                                    final String address = Helpers.getAddress(getActivity(), latLng);
                                                    getActivity().runOnUiThread(new Runnable() {
                                                        @Override
                                                        public void run() {
                                                            if (address != null && !address.equalsIgnoreCase("address not found")) {
                                                                tvRegisterFragmentLocationInfo.setText("Point Set - " + address);
                                                            } else {
                                                                tvRegisterFragmentLocationInfo.setText("Point Set");
                                                            }
                                                        }
                                                    });
                                                }
                                            });
                                        }
                                    }
                                });


                            }
                        });
                    }
                }
                break;
            case R.id.btn_fragment_register_map_done:
                if (mapRegisterLocationAdded) {
                    Helpers.hideSoftKeyboard(getActivity());
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            tvRegisterFragmentBusinessLocation.setText(mapLocationPoint);
                            tvRegisterFragmentBusinessLocation.setVisibility(View.VISIBLE);
                            rlRegisterFragmentLocation.setVisibility(View.GONE);
                        }
                    }, 250);
                } else {
                    Toast.makeText(getActivity(), "Business point not set", Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.btn_fragment_register_map_current_location:
                if (Helpers.isAnyLocationServiceAvailable()) {
                    if (mMap != null) {
                        if (currentLatLngAuto != null) {
                            CameraPosition cameraPosition =
                                    new CameraPosition.Builder()
                                            .target(currentLatLngAuto)
                                            .zoom(16.0f)
                                            .build();
                            mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
                        } else {
                            Toast.makeText(getActivity(), "Error: Location not available", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(getActivity(), "Error: Map not ready", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(getActivity(), "Error: Location Service disabled", Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.btn_fragment_register_map_search:
                setSearchBarVisibility(!isSearchEditTextVisible);
                break;
            case R.id.tv_fragment_register_business_timings:
                rlRegisterFragmentTimings.setVisibility(View.VISIBLE);
                tvRegisterFragmentBusinessTimings.setVisibility(View.GONE);
                isTimingsSetByUser = false;
                tvRegisterFragmentBusinessTimings.setError(null);
                break;
            case R.id.tv_fragment_register_business_timings_opening_time:
                showTimePickerDialog(0);
                break;
            case R.id.tv_fragment_register_business_timings_closing_time:
                showTimePickerDialog(1);
                break;
            case R.id.tv_fragment_register_pdf_menu:
                pdfPicker();
                break;
            case R.id.btn_fragment_register_business_timings_back:
                intRegisterFragmentBusinessTimingsSelectedDayCount--;
                btnRegisterFragmentBusinessTimingsNavigationDone.setText("Next");
                cbRegisterFragmentBusinessTimingsHoliday.setText("Set " + sArrayRegisterFragmentBusinessTimingsWeekDays[intRegisterFragmentBusinessTimingsSelectedDayCount] +
                        " as a holiday");
                if (sArrayListRegisterFragmentBusinessTimingsWeekDays.get(intRegisterFragmentBusinessTimingsSelectedDayCount).equalsIgnoreCase("Holiday")) {
                    disableTimingsFields(true);
                    cbRegisterFragmentBusinessTimingsHoliday.setChecked(true);
                } else {
                    cbRegisterFragmentBusinessTimingsHoliday.setChecked(false);
                    if (sArrayListRegisterFragmentBusinessTimingsWeekDays.get(intRegisterFragmentBusinessTimingsSelectedDayCount).contains("-")) {
                        String[] splitTimings = sArrayListRegisterFragmentBusinessTimingsWeekDays.get(intRegisterFragmentBusinessTimingsSelectedDayCount).split("-");
                        tvRegisterFragmentBusinessTimingsOpeningTime.setText(splitTimings[0]);
                        tvRegisterFragmentBusinessTimingsClosingTime.setText(splitTimings[1]);
                    }
                }
                if (intRegisterFragmentBusinessTimingsSelectedDayCount == 0) {
                    btnRegisterFragmentBusinessTimingsNavigationBack.setEnabled(false);
                    btnRegisterFragmentBusinessTimingsNavigationBack.setAlpha(.2f);
                }
                tvRegisterFragmentBusinessTimingsHeader.setText(sArrayRegisterFragmentBusinessTimingsWeekDays[intRegisterFragmentBusinessTimingsSelectedDayCount]);
                break;
            case R.id.btn_fragment_register_business_timings_done:
                if (rgRegisterFragmentBusinessTimingsInputType.getCheckedRadioButtonId() == R.id.rb_fragment_register_business_timings_same_for_all_week_days &&
                        tvRegisterFragmentBusinessTimingsOpeningTime.getText().length() > 1 && tvRegisterFragmentBusinessTimingsClosingTime.getText().length() > 1) {
                    rlRegisterFragmentTimings.setVisibility(View.GONE);
                    tvRegisterFragmentBusinessTimings.setVisibility(View.VISIBLE);
                    tvRegisterFragmentBusinessTimings.setText("Schedule Set");
                    tvRegisterFragmentBusinessTimings.setTextColor(Color.GREEN);
                    sArrayListRegisterFragmentBusinessTimingsWeekDays = new ArrayList<>();
                    for (int i = 0; i < sArrayRegisterFragmentBusinessTimingsWeekDays.length; i++) {
                        sArrayListRegisterFragmentBusinessTimingsWeekDays.add(i, tvRegisterFragmentBusinessTimingsOpeningTime.getText() + "-" +
                                tvRegisterFragmentBusinessTimingsClosingTime.getText());
                    }
                    isTimingsSetByUser = true;
                    Log.i("array", "" + sArrayListRegisterFragmentBusinessTimingsWeekDays);
                } else if (rgRegisterFragmentBusinessTimingsInputType.getCheckedRadioButtonId() == R.id.rb_fragment_register_business_timings_set_individually &&
                        (tvRegisterFragmentBusinessTimingsOpeningTime.getText().length() > 1 && tvRegisterFragmentBusinessTimingsClosingTime.getText().length() > 1 ||
                                cbRegisterFragmentBusinessTimingsHoliday.isChecked()) && intRegisterFragmentBusinessTimingsSelectedDayCount > 5) {
                    rlRegisterFragmentTimings.setVisibility(View.GONE);
                    tvRegisterFragmentBusinessTimings.setVisibility(View.VISIBLE);
                    tvRegisterFragmentBusinessTimings.setText("Schedule Set");
                    tvRegisterFragmentBusinessTimings.setTextColor(Color.GREEN);
                    if (cbRegisterFragmentBusinessTimingsHoliday.isChecked()) {
                        if (sArrayListRegisterFragmentBusinessTimingsWeekDays.size() > intRegisterFragmentBusinessTimingsSelectedDayCount) {
                            sArrayListRegisterFragmentBusinessTimingsWeekDays.remove(intRegisterFragmentBusinessTimingsSelectedDayCount);
                            sArrayListRegisterFragmentBusinessTimingsWeekDays.add(intRegisterFragmentBusinessTimingsSelectedDayCount, "Holiday");
                        } else {
                            sArrayListRegisterFragmentBusinessTimingsWeekDays.add(intRegisterFragmentBusinessTimingsSelectedDayCount, "Holiday");
                        }
                    } else {
                        if (sArrayListRegisterFragmentBusinessTimingsWeekDays.size() > intRegisterFragmentBusinessTimingsSelectedDayCount) {
                            sArrayListRegisterFragmentBusinessTimingsWeekDays.remove(intRegisterFragmentBusinessTimingsSelectedDayCount);
                            sArrayListRegisterFragmentBusinessTimingsWeekDays.add(intRegisterFragmentBusinessTimingsSelectedDayCount, tvRegisterFragmentBusinessTimingsOpeningTime.getText() + "-" +
                                    tvRegisterFragmentBusinessTimingsClosingTime.getText());
                        } else {
                            sArrayListRegisterFragmentBusinessTimingsWeekDays.add(intRegisterFragmentBusinessTimingsSelectedDayCount, tvRegisterFragmentBusinessTimingsOpeningTime.getText() + "-" +
                                    tvRegisterFragmentBusinessTimingsClosingTime.getText());
                        }
                    }
                    isTimingsSetByUser = true;
                    Log.i("array", "" + sArrayListRegisterFragmentBusinessTimingsWeekDays);
                } else if (rgRegisterFragmentBusinessTimingsInputType.getCheckedRadioButtonId() == R.id.rb_fragment_register_business_timings_set_individually &&
                        tvRegisterFragmentBusinessTimingsOpeningTime.getText().length() > 1 && tvRegisterFragmentBusinessTimingsClosingTime.getText().length() > 1 ||
                        cbRegisterFragmentBusinessTimingsHoliday.isChecked()) {
                    if (cbRegisterFragmentBusinessTimingsHoliday.isChecked()) {
                        if (sArrayListRegisterFragmentBusinessTimingsWeekDays.size() > intRegisterFragmentBusinessTimingsSelectedDayCount) {
                            sArrayListRegisterFragmentBusinessTimingsWeekDays.remove(intRegisterFragmentBusinessTimingsSelectedDayCount);
                            sArrayListRegisterFragmentBusinessTimingsWeekDays.add(intRegisterFragmentBusinessTimingsSelectedDayCount, "Holiday");
                        } else {
                            sArrayListRegisterFragmentBusinessTimingsWeekDays.add(intRegisterFragmentBusinessTimingsSelectedDayCount, "Holiday");
                        }
                    } else {
                        if (sArrayListRegisterFragmentBusinessTimingsWeekDays.size() > intRegisterFragmentBusinessTimingsSelectedDayCount) {
                            sArrayListRegisterFragmentBusinessTimingsWeekDays.remove(intRegisterFragmentBusinessTimingsSelectedDayCount);
                            sArrayListRegisterFragmentBusinessTimingsWeekDays.add(intRegisterFragmentBusinessTimingsSelectedDayCount, tvRegisterFragmentBusinessTimingsOpeningTime.getText() + "-" +
                                    tvRegisterFragmentBusinessTimingsClosingTime.getText());
                        } else {
                            sArrayListRegisterFragmentBusinessTimingsWeekDays.add(intRegisterFragmentBusinessTimingsSelectedDayCount, tvRegisterFragmentBusinessTimingsOpeningTime.getText() + "-" +
                                    tvRegisterFragmentBusinessTimingsClosingTime.getText());
                        }
                    }
                    intRegisterFragmentBusinessTimingsSelectedDayCount++;
                    if (intRegisterFragmentBusinessTimingsSelectedDayCount > 5) {
                        btnRegisterFragmentBusinessTimingsNavigationDone.setText("Done");
                    }
                    tvRegisterFragmentBusinessTimingsHeader.setText(sArrayRegisterFragmentBusinessTimingsWeekDays[intRegisterFragmentBusinessTimingsSelectedDayCount]);
                    if (sArrayListRegisterFragmentBusinessTimingsWeekDays.size() > intRegisterFragmentBusinessTimingsSelectedDayCount &&
                            sArrayListRegisterFragmentBusinessTimingsWeekDays.get(intRegisterFragmentBusinessTimingsSelectedDayCount).equalsIgnoreCase("Holiday")) {
                        disableTimingsFields(true);
                        cbRegisterFragmentBusinessTimingsHoliday.setChecked(true);
                    } else {
                        disableTimingsFields(false);
                        cbRegisterFragmentBusinessTimingsHoliday.setChecked(false);
                        if (sArrayListRegisterFragmentBusinessTimingsWeekDays.size() > intRegisterFragmentBusinessTimingsSelectedDayCount &&
                                sArrayListRegisterFragmentBusinessTimingsWeekDays.get(intRegisterFragmentBusinessTimingsSelectedDayCount).contains("-")) {
                            String[] splitTimings = sArrayListRegisterFragmentBusinessTimingsWeekDays.get(intRegisterFragmentBusinessTimingsSelectedDayCount).split("-");
                            tvRegisterFragmentBusinessTimingsOpeningTime.setText(splitTimings[0]);
                            tvRegisterFragmentBusinessTimingsClosingTime.setText(splitTimings[1]);
                        }
                    }

                    btnRegisterFragmentBusinessTimingsNavigationBack.setEnabled(true);
                    btnRegisterFragmentBusinessTimingsNavigationBack.setAlpha(1);

                    cbRegisterFragmentBusinessTimingsHoliday.setText("Set " + sArrayRegisterFragmentBusinessTimingsWeekDays[intRegisterFragmentBusinessTimingsSelectedDayCount] +
                            " as a holiday");
                    Log.i("intCount", "" + intRegisterFragmentBusinessTimingsSelectedDayCount);
                    Log.i("array", "" + sArrayListRegisterFragmentBusinessTimingsWeekDays);
                } else {
                    Toast.makeText(getInstance(), "Timing not set", Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }

    private void setSearchBarVisibility(boolean visibility) {
        if (!visibility) {
            Helpers.hideSoftKeyboard(getActivity());
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    etMapSearch.startAnimation(animLayoutMapSearchBarFadeOut);
                    etMapSearch.setVisibility(View.INVISIBLE);
                }
            }, 250);
        } else {
            etMapSearch.setVisibility(View.VISIBLE);
            etMapSearch.startAnimation(animLayoutMapSearchBarFadeIn);
        }
    }

    protected void searchAnimateCamera(List<Address> addresses) {
        final Address addressForSearch = addresses.get(0);
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                LatLng latLngSearch = new LatLng(addressForSearch.getLatitude(), addressForSearch.getLongitude());
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLngSearch, 15.0f));
                mMap.clear();
                btnRegisterFragmentLocationCancel.callOnClick();
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        isRegisterFragmentOpen = true;
    }

    @Override
    public void onPause() {
        super.onPause();
        isRegisterFragmentOpen = false;
    }

    public boolean validateRegisterInfo() {
        boolean valid = true;

        if (!cbRegisterFragmentBusinessTypePizza.isChecked() && !cbRegisterFragmentBusinessTypeWings.isChecked() &&
                !cbRegisterFragmentBusinessTypeBurger.isChecked() && !cbRegisterFragmentBusinessTypeTaco.isChecked()) {
            tvRegisterFragmentBusinessTypeTitle.setError("check at least one");
            valid = false;
        } else {
            tvRegisterFragmentBusinessTypeTitle.setError(null);
        }

        if (businessName.trim().length() < 3) {
            etRegisterFragmentBusinessName.setError("At least 3 characters");
            valid = false;
        } else {
            etRegisterFragmentBusinessName.setError(null);
        }

        if (businessNumber.trim().isEmpty()) {
            etRegisterFragmentBusinessNumber.setError("Empty");
            valid = false;
        } else if (!businessNumber.isEmpty() && !PhoneNumberUtils.isGlobalPhoneNumber(businessNumber)) {
            etRegisterFragmentBusinessNumber.setError("Number is invalid");
            valid = false;
        } else {
            etRegisterFragmentBusinessNumber.setError(null);
        }

        if (!isTimingsSetByUser) {
            tvRegisterFragmentBusinessTimings.setError("Not set");
            valid = false;
        } else {
            tvRegisterFragmentBusinessTimings.setError(null);
        }

        if (businessOwnersName.trim().length() < 3) {
            etRegisterFragmentOwnersName.setError("At least 3 characters");
            valid = false;
        } else {
            etRegisterFragmentOwnersName.setError(null);
        }

        if (businessOwnersEmail.trim().isEmpty()) {
            etRegisterFragmentOwnersEmail.setError("Empty");
            valid = false;
        } else if (!businessOwnersEmail.trim().isEmpty() && !android.util.Patterns.EMAIL_ADDRESS.matcher(businessOwnersEmail).matches()) {
            etRegisterFragmentOwnersEmail.setError("Invalid E-Mail");
            valid = false;
        } else {
            etRegisterFragmentOwnersEmail.setError(null);
        }

        if (cbRegisterFragmentMenuPDF.isChecked() && businessMenuPDF.trim().isEmpty()) {
            tvRegisterFragmentMenuPDF.setError("PDF not attached");
            valid = false;
        } else {
            tvRegisterFragmentMenuPDF.setError(null);
        }

        if (businessLocation.trim().isEmpty()) {
            tvRegisterFragmentBusinessLocation.setError("Location not set");
            valid = false;
        } else {
            tvRegisterFragmentBusinessLocation.setError(null);
        }

        return valid;
    }

    private void showTimePickerDialog(final int etNumbering) {
        Calendar currentTimeForOpening = Calendar.getInstance();
        timePickerHour = currentTimeForOpening.get(Calendar.HOUR_OF_DAY);
        timePickerMinutes = currentTimeForOpening.get(Calendar.MINUTE);
        TimePickerDialog mTimePicker;
        mTimePicker = new TimePickerDialog(getActivity(), new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker timePicker, int i, int i1) {
                if (etNumbering == 0) {
                    tvRegisterFragmentBusinessTimingsOpeningTime.setText(Helpers.format24HoursToAmPm(i + ":" + i1));
                } else {
                    tvRegisterFragmentBusinessTimingsClosingTime.setText(Helpers.format24HoursToAmPm(i + ":" + i1));
                }
            }
        }, timePickerHour, timePickerMinutes, false);
        if (etNumbering == 0) {
            mTimePicker.setTitle("Opening Time");
        } else {
            mTimePicker.setTitle("Closing Time");
        }
        mTimePicker.show();
    }

    private void pdfPicker() {
        String[] extensions = {".pdf", "txt"};

        DialogProperties properties = new DialogProperties();
        properties.selection_mode = DialogConfigs.SINGLE_MODE;
        properties.selection_type = DialogConfigs.FILE_SELECT;
        properties.root = new File(DialogConfigs.DEFAULT_DIR);
        properties.error_dir = new File(DialogConfigs.DEFAULT_DIR);
        properties.extensions = extensions;

        FilePickerDialog dialog = new FilePickerDialog(getActivity(), properties);
        dialog.setTitle("Select a File");

        dialog.setDialogSelectionListener(new DialogSelectionListener() {
            @Override
            public void onSelectedFilePaths(String[] files) {
                //files is the array of the paths of files selected by the Application User.
                tvRegisterFragmentMenuPDF.setText(files[0]);
                tvRegisterFragmentMenuPDF.setError(null);
            }
        });

        dialog.show();
    }

    private void sendRegistrationRequest() {
        HttpRequest request = new HttpRequest(getActivity());
        if (cbRegisterFragmentMenuPDF.isChecked()) {
            Helpers.showProgressDialogWithProgress(getActivity(), "Sending registration request");
        } else {
            Helpers.showProgressDialog(getActivity(), "Sending registration request");
        }
        request.setOnReadyStateChangeListener(new HttpRequest.OnReadyStateChangeListener() {
            @Override
            public void onReadyStateChange(HttpRequest request, int readyState) {
                switch (readyState) {
                    case HttpRequest.STATE_DONE:
                        Helpers.dismissProgressDialog();
                        switch (request.getStatus()) {
                            case HttpURLConnection.HTTP_CREATED:
                                registrationRequestSuccess();
                                break;
                            case HttpURLConnection.HTTP_BAD_REQUEST:
                                registrationRequestFailed(request.getStatusText(), true);
                                break;
                            case 413:
                                registrationRequestFailed("Menu file too large.", false);
                                break;
                        }
                }
            }
        });
        request.setOnErrorListener(new HttpRequest.OnErrorListener() {
            @Override
            public void onError(HttpRequest request, short error, Exception exception) {
            }
        });
        request.open("POST", "http://139.59.187.73/api/restaurants/register");
        if (cbRegisterFragmentMenuPDF.isChecked()) {
            FormData data = new FormData();
            data.append(FormData.TYPE_CONTENT_TEXT, "name", businessName);
            data.append(FormData.TYPE_CONTENT_TEXT, "contact", businessNumber);
            data.append(FormData.TYPE_CONTENT_TEXT, "timings", businessTimings);
            data.append(FormData.TYPE_CONTENT_TEXT, "location", businessLocation);
            data.append(FormData.TYPE_CONTENT_TEXT, "owner_name", businessOwnersName);
            data.append(FormData.TYPE_CONTENT_TEXT, "owner_email", businessOwnersEmail);
            data.append(FormData.TYPE_CONTENT_TEXT, "business_type", getBusinessTypeStringForRegistration());
            data.append(FormData.TYPE_CONTENT_FILE, "menu", businessMenuPDF);
            request.setOnFileUploadProgressListener(this);
            request.send(data);
        } else {
            request.send(getStringForRegistrationWithoutFile(businessName, businessLocation, businessNumber, getBusinessTypeStringForRegistration(),
                    businessTimings, businessOwnersName, businessOwnersEmail));
        }
    }

    @Override
    public void onFileUploadProgress(HttpRequest request, File file, long loaded, long total) {
        if (cbRegisterFragmentMenuPDF.isChecked()) {
            Helpers.progressDialog.setMax((int) total);
            Helpers.progressDialog.setProgress((int) loaded);
            Helpers.progressDialog.setMessage("Uploading menu file");
        }
    }

    private void registrationRequestFailed(String message, boolean messageWithFunction) {
        if (messageWithFunction) {
            Helpers.AlertDialogWithPositiveFunctionNegativeButton(getActivity(), "Request Failed", message, "Retry", "Dismiss", retryRegistration);
        } else {
            Helpers.AlertDialogMessage(getActivity(), "Request Failed", message, "Dismiss");
        }
    }

    private void registrationRequestSuccess() {
        Helpers.AlertDialogMessage(getActivity(), "Request Sent", "Registration request successfully sent. Pending admin approval.", "Ok");
        getActivity().onBackPressed();
    }

    private int getAppropriateRegisterMapMarkerIconImageID(int authenticationType) {
        int id = -1;
        if (authenticationType == 0) {
            id = R.mipmap.ic_register_fragment_map_pizza_business_point_marker;
        } else if (authenticationType == 1) {
            id = R.mipmap.ic_register_fragment_map_wings_business_point_marker;
        } else if (authenticationType == 2) {
            id = R.mipmap.ic_register_fragment_map_burger_business_point_marker;
        } else if (authenticationType == 3) {
            id = R.mipmap.ic_register_fragment_map_taco_business_point_marker;
        }
        return id;
    }

    private void disableTimingsFields(boolean disable) {
        if (disable) {
            tvRegisterFragmentBusinessTimingsOpeningTime.setOnClickListener(null);
            llRegisterFragmentBusinessTimingsOpeningTime.setAlpha(.2f);
            tvRegisterFragmentBusinessTimingsClosingTime.setOnClickListener(null);
            llRegisterFragmentBusinessTimingsClosingTime.setAlpha(.2f);
        } else {
            tvRegisterFragmentBusinessTimingsOpeningTime.setOnClickListener(this);
            llRegisterFragmentBusinessTimingsOpeningTime.setAlpha(1);
            tvRegisterFragmentBusinessTimingsClosingTime.setOnClickListener(this);
            llRegisterFragmentBusinessTimingsClosingTime.setAlpha(1);
        }
    }

    private void checkSelectedProjectCheckBox(int projectType) {
        if (projectType == 0) {
            cbRegisterFragmentBusinessTypePizza.setChecked(true);
        } else if (projectType == 1) {
            cbRegisterFragmentBusinessTypeWings.setChecked(true);
        } else if (projectType == 2) {
            cbRegisterFragmentBusinessTypeBurger.setChecked(true);
        } else if (projectType == 3) {
            cbRegisterFragmentBusinessTypeTaco.setChecked(true);
        }
    }

    private String getBusinessTypeStringForRegistration() {
        String businessType = "";
        if (cbRegisterFragmentBusinessTypePizza.isChecked()) {
            businessType += "true,";
        } else {
            businessType += "false,";
        }

        if (cbRegisterFragmentBusinessTypeWings.isChecked()) {
            businessType += "true,";
        } else {
            businessType += "false,";
        }

        if (cbRegisterFragmentBusinessTypeBurger.isChecked()) {
            businessType += "true,";
        } else {
            businessType += "false,";
        }

        if (cbRegisterFragmentBusinessTypeTaco.isChecked()) {
            businessType += "true";
        } else {
            businessType += "false";
        }
        return businessType;
    }
}
