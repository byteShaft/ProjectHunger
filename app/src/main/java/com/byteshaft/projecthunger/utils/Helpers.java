package com.byteshaft.projecthunger.utils;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import com.byteshaft.projecthunger.R;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.maps.model.LatLng;

import java.io.IOException;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class Helpers {

    public static LatLng latLngForNavigation;
    public static String urlFoodMenu;

    public static int onRecheckLocationAvailableTaskType;
    public static ProgressDialog progressDialog;

    public static void hideSoftKeyboard(Activity activity) {
        View view = activity.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    public static void initiateCallIntent(Activity activity, String number) {
        Intent intent = new Intent(Intent.ACTION_DIAL);
        intent.setData(Uri.parse("tel:" + number));
        activity.startActivity(intent);
    }

    public static void showProgressDialogWithProgress(Context context, String message) {
        progressDialog = new ProgressDialog(context);
        progressDialog.setMessage(message);
        progressDialog.setCancelable(false);
        progressDialog.setIndeterminate(false);
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        progressDialog.show();
    }

    public static void showProgressDialog(Context context, String message) {
        progressDialog = new ProgressDialog(context);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setMessage(message);
        progressDialog.setCancelable(false);
        progressDialog.setIndeterminate(true);
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.show();
    }

    public static void dismissProgressDialog() {
        if (progressDialog != null) {
            progressDialog.dismiss();
        }
    }

    private static final Runnable openLocationServiceSettings = new Runnable() {
        public void run() {
            Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            AppGlobals.getRunningActivityInstance().startActivity(intent);
        }
    };

//    private static final Runnable recheckLocationServiceStatus = new Runnable() {
//        public void run() {
//            if (!Helpers.isAnyLocationServiceAvailable()) {
//                Helpers.AlertDialogWithPositiveNegativeFunctions(MainActivity.getInstance(),
//                        "Location Service disabled", "Enable device GPS to continue", "Settings", "ReCheck",
//                        openLocationServiceSettings, recheckLocationServiceStatus);
//            } else {
//                if (onRecheckLocationAvailableTaskType == 0) {
//                    loadFragment(MainActivity.fragmentManager, new MapsFragment(), "MapsFragment");
//                } else if (onRecheckLocationAvailableTaskType == 1) {
//                    RegisterFragment.tvRegisterFragmentBusinessLocation.callOnClick();
//                }
//            }
//        }
//    };

    public static void loadFragment(FragmentManager fragmentManager, android.support.v4.app.Fragment fragment, String fragmentName) {
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.setCustomAnimations(R.anim.anim_transition_fragment_slide_right_enter, R.anim.anim_transition_fragment_slide_left_exit,
                R.anim.anim_transition_fragment_slide_left_enter, R.anim.anim_transition_fragment_slide_right_exit);
        if (fragmentName != null) {
            if (fragmentName.equals("FoodMenuFragment")) {
                transaction.add(R.id.container_main, fragment).addToBackStack(fragmentName);
            } else {
                transaction.replace(R.id.container_main, fragment).addToBackStack(fragmentName);
            }
        } else {
            transaction.replace(R.id.container_main, fragment);
        }
        transaction.commit();
    }

    public static void loadActivity(Context context, Class activity) {
        Intent myIntent = new Intent(context, activity);
        context.startActivity(myIntent);
    }

    public static boolean isAnyLocationServiceAvailable() {
        LocationManager locationManager = getLocationManager();
        return isGpsEnabled(locationManager) || isNetworkBasedGpsEnabled(locationManager);
    }

    private static LocationManager getLocationManager() {
        return (LocationManager) AppGlobals.getContext().getSystemService(Context.LOCATION_SERVICE);
    }

    private static boolean isGpsEnabled(LocationManager locationManager) {
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
    }

    private static boolean isNetworkBasedGpsEnabled(LocationManager locationManager) {
        return locationManager.isProviderEnabled((LocationManager.NETWORK_PROVIDER));
    }

    public static void AlertDialogMessage(Context context, String title, String message, String neutralButtonText) {
        new AlertDialog.Builder(context)
                .setTitle(title)
                .setMessage(message)
                .setCancelable(false)
                .setPositiveButton(neutralButtonText, null)
                .show();
    }

    private static void AlertDialogWithPositiveNegativeFunctions(
            Context context, String title, String message, String positiveButtonText,
            String negativeButtonText, final Runnable listenerYes, final Runnable listenerNo) {
        new AlertDialog.Builder(context)
                .setTitle(title)
                .setMessage(message)
                .setCancelable(false)
                .setPositiveButton(positiveButtonText, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        listenerYes.run();
                    }
                })
                .setNegativeButton(negativeButtonText, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        listenerNo.run();
                    }
                })
                .show();
    }

    public static void AlertDialogWithPositiveNegativeNeutralFunctions(
            Context context, String title, String message, String positiveButtonText,
            String negativeButtonText, String neutralButtonText, final Runnable listenerYes,
            final Runnable listenerNo) {
        new AlertDialog.Builder(context)
                .setTitle(title)
                .setMessage(message)
                .setCancelable(false)
                .setPositiveButton(positiveButtonText, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        listenerYes.run();
                    }
                })
                .setNegativeButton(negativeButtonText, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (listenerNo != null) {
                            listenerNo.run();
                        }
                    }
                })
                .setNeutralButton(neutralButtonText, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .show();
    }

    public static void AlertDialogWithPositiveFunctionNegativeButton(
            Context context, String title, String message, String positiveButtonText,
            String negativeButtonText, final Runnable listenerYes) {
        new AlertDialog.Builder(context)
                .setTitle(title)
                .setMessage(message)
                .setCancelable(false)
                .setPositiveButton(positiveButtonText, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        listenerYes.run();
                    }
                })
                .setNegativeButton(negativeButtonText, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .show();
    }

    public static String getAddress(Context context, LatLng latLng) {
        Geocoder geocoder;
        List<Address> addresses;
        String address;
        geocoder = new Geocoder(context, Locale.getDefault());
        try {
            addresses = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1);
            address = addresses.get(0).getAddressLine(0);
        } catch (IOException e) {
            address = null;
            e.printStackTrace();
        }
        return address;
    }

    public static String format24HoursToAmPm(String time) {
        DateFormat f1 = new SimpleDateFormat("HH:mm"); //HH for hour of the day (0 - 23)
        Date d = null;
        try {
            d = f1.parse(time);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        DateFormat f2 = new SimpleDateFormat("h:mma");
        return  f2.format(d).toLowerCase();
    }

    public static String formatLatLngToLimitCharacterLengthAndReturnInSingleString(String latitude, String longitude) {
        return new DecimalFormat(".######").format(Double.parseDouble(latitude)) + "," + new DecimalFormat(".######").format(Double.parseDouble(longitude));
    }

    public static String getAppropriateProjectName(int projectType) {
        String name = null;
        if (projectType == 0) {
            name = "Pepperoni";
        } else if (projectType == 1) {
            name = "Buffalo";
        } else if (projectType == 2) {
            name = "Grill";
        } else if (projectType == 3) {
            name = "Taco";
        } else if (projectType == 4) {
            name = "Late-Night";
        }
        return name;
    }

    public static String getBusinessTimingsForCurrentDay(String timings) {
        String[] timingsArray = timings.split(",");
        Calendar calendar = Calendar.getInstance(Locale.US);
        int numberOfDayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
        return timingsArray[numberOfDayOfWeek - 1];
    }

    private static boolean hasPermissionsForDevicesAboveMarshmallowIfNotRequestPermissions(Activity activity) {
        int PERMISSION_ALL = 1;
        String[] PERMISSIONS = {Manifest.permission.ACCESS_FINE_LOCATION};
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && activity != null && PERMISSIONS != null) {
            for (String permission : PERMISSIONS) {
                if (ActivityCompat.checkSelfPermission(activity, permission) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(activity, PERMISSIONS, PERMISSION_ALL);
                    return false;
                }
            }
        }
        return true;
    }

    private static boolean areGooglePlayServicesAvailable(Activity activity) {
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        int resultCode = apiAvailability.isGooglePlayServicesAvailable(activity);
        return resultCode == ConnectionResult.SUCCESS;
    }

    private static final Runnable openInstallationActivityForPlayServices = new Runnable() {
        public void run() {
            Intent i = new Intent();
            i.setAction(Intent.ACTION_VIEW);
            i.addCategory(Intent.CATEGORY_BROWSABLE);
            i.setData(Uri.parse("https://play.google.com/store/apps/details?id=com.google.android.gms&hl=en"));
            AppGlobals.getContext().startActivity(i);
        }
    };

    public static boolean isDeviceReadyForLocationAcquisition(Activity activity) {
        boolean ready = true;
        if (hasPermissionsForDevicesAboveMarshmallowIfNotRequestPermissions(activity)) {
            if (!isAnyLocationServiceAvailable()) {
                AlertDialogWithPositiveNegativeFunctions(activity,
                        "Location Service disabled", "Enable location services to continue", "Settings", "ReCheck",
                        openLocationServiceSettings, recheckLocationServiceStatus);
                ready = false;
            } else if (!areGooglePlayServicesAvailable(activity)) {
                Helpers.AlertDialogWithPositiveFunctionNegativeButton(activity, "Location components missing",
                        "You need to install GooglePlayServices to continue", "Install",
                        "Dismiss", Helpers.openInstallationActivityForPlayServices);
                ready = false;
            }
        }
        return ready;
    }

    private static final Runnable recheckLocationServiceStatus = new Runnable() {
        public void run() {
            isDeviceReadyForLocationAcquisition(AppGlobals.getRunningActivityInstance());
        }
    };

    public static boolean isCurrentTimeBetween(String initialTime, String finalTime) throws ParseException {

        Calendar calender = Calendar.getInstance();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH:mm:ss");
        String currentTime = simpleDateFormat.format(calender.getTime());

        String reg = "^([0-1][0-9]|2[0-3]):([0-5][0-9]):([0-5][0-9])$";
        if (initialTime.matches(reg) && finalTime.matches(reg) && currentTime.matches(reg)) {
            boolean valid = false;
            //Start Time
            java.util.Date inTime = new SimpleDateFormat("HH:mm:ss").parse(initialTime);
            Calendar calendar1 = Calendar.getInstance();
            calendar1.setTime(inTime);

            //Current Time
            java.util.Date checkTime = new SimpleDateFormat("HH:mm:ss").parse(currentTime);
            Calendar calendar3 = Calendar.getInstance();
            calendar3.setTime(checkTime);

            //End Time
            java.util.Date finTime = new SimpleDateFormat("HH:mm:ss").parse(finalTime);
            Calendar calendar2 = Calendar.getInstance();
            calendar2.setTime(finTime);

            if (finalTime.compareTo(initialTime) < 0) {
                calendar2.add(Calendar.DATE, 1);
                calendar3.add(Calendar.DATE, 1);
            }

            java.util.Date actualTime = calendar3.getTime();
            if ((actualTime.after(calendar1.getTime()) || actualTime.compareTo(calendar1.getTime()) == 0)
                    && actualTime.before(calendar2.getTime())) {
                valid = true;
            }
            return valid;
        } else {
            throw new IllegalArgumentException("Not a valid time, expecting HH:MM:SS format");
        }

    }

    public static String convertAmPmTo24HoursTime(String time) throws Exception {
        SimpleDateFormat displayFormat = new SimpleDateFormat("HH:mm");
        SimpleDateFormat parseFormat = new SimpleDateFormat("hh:mma");
        Date date = parseFormat.parse(time);
        return displayFormat.format(date) + ":00";
    }
}