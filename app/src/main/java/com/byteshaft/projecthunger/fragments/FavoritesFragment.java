package com.byteshaft.projecthunger.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RatingBar;
import android.widget.TextView;

import com.byteshaft.projecthunger.MainActivity;
import com.byteshaft.projecthunger.R;
import com.byteshaft.projecthunger.utils.DatabaseHelpers;
import com.byteshaft.projecthunger.utils.Helpers;
import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.HashMap;

import static com.byteshaft.projecthunger.utils.Helpers.loadFragment;
import static com.byteshaft.projecthunger.utils.Helpers.onRecheckLocationAvailableTaskType;


/**
 * Created by fi8er1 on 17/11/2016.
 */

public class FavoritesFragment extends android.support.v4.app.Fragment {

    View baseViewFavoritesFragment;
    DatabaseHelpers mDatabaseHelpers;
    ListView lvFavoritesFragment;
    FavoritesListAdapter favoritesListAdapter;
    String contactNumberToInitiateCall;
    String idToDeleteEntry;

    Runnable initiateCall = new Runnable() {
        public void run() {
            Helpers.initiateCallIntent(getActivity(), contactNumberToInitiateCall);
        }
    };

    Runnable showMenu = new Runnable() {
        public void run() {
            loadFragment(MainActivity.fragmentManager, new FoodMenuFragment(), "FoodMenuFragment");
        }
    };

    Runnable navigate = new Runnable() {
        public void run() {
            onRecheckLocationAvailableTaskType = 0;
            if (Helpers.isDeviceReadyForLocationAcquisition(getActivity())) {
                loadFragment(MainActivity.fragmentManager, new NavigateFragment(), "NavigateFragment");
            }
        }
    };

    Runnable deleteEntry = new Runnable() {
        public void run() {
            mDatabaseHelpers.deleteEntry(idToDeleteEntry);
            favoritesListAdapter = new FavoritesListAdapter(getActivity(), R.layout.favorites_list_row,
                    mDatabaseHelpers.getAllRecords());
            lvFavoritesFragment.setAdapter(favoritesListAdapter);
            if (mDatabaseHelpers.isEmpty()) {
                getActivity().onBackPressed();
            }
        }
    };

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        baseViewFavoritesFragment = inflater.inflate(R.layout.fragment_favorites, container, false);
        mDatabaseHelpers = new DatabaseHelpers(getActivity());
        lvFavoritesFragment = (ListView) baseViewFavoritesFragment.findViewById(R.id.lv_favorites_fragment);
        favoritesListAdapter = new FavoritesListAdapter(getActivity(), R.layout.favorites_list_row,
                mDatabaseHelpers.getAllRecords());
        lvFavoritesFragment.setAdapter(favoritesListAdapter);

        MainActivity.getInstance().setTitle("Favorites");
        return baseViewFavoritesFragment;
    }

    class FavoritesListAdapter extends ArrayAdapter<String> {
        
        ArrayList<HashMap> arrayListTest;

        public FavoritesListAdapter(Context context, int resource, ArrayList<HashMap> arrayList) {
            super(context, resource);
            arrayListTest = arrayList;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            ViewHolder viewHolder;
            if (convertView == null) {
                viewHolder = new ViewHolder();
                LayoutInflater layoutInflater = getActivity().getLayoutInflater();
                convertView = layoutInflater.inflate(R.layout.favorites_list_row, parent, false);
                viewHolder.rbFavorites = (RatingBar) convertView.findViewById(R.id.rBar_favorites_list);
                viewHolder.tvFavoritesBusinessName = (TextView) convertView.findViewById(R.id.tv_favorites_list_business_name);
                viewHolder.tvFavoritesNumberOfRatings = (TextView) convertView.findViewById(R.id.tv_favorites_list_number_of_ratings);
                viewHolder.tvFavoritesTimings = (TextView) convertView.findViewById(R.id.tv_favorites_list_business_timings);
                viewHolder.tvFavoritesFormattedAddress = (TextView) convertView.findViewById(R.id.tv_favorites_list_business_formatted_address);
                viewHolder.ibFavoritesCall = (ImageButton) convertView.findViewById(R.id.ib_favorites_list_call);
                viewHolder.ibFavoritesMenu = (ImageButton) convertView.findViewById(R.id.ib_favorites_list_menu);
                viewHolder.ibFavoritesNavigate = (ImageButton) convertView.findViewById(R.id.ib_favorites_list_route);
                viewHolder.ibFavoritesDelete = (ImageButton) convertView.findViewById(R.id.ib_favorites_list_delete);
                viewHolder.ivFavoritesPizza = (ImageView) convertView.findViewById(R.id.iv_favorites_list_business_type_pizza);
                viewHolder.ivFavoritesWings = (ImageView) convertView.findViewById(R.id.iv_favorites_list_business_type_wings);
                viewHolder.ivFavoritesBurger = (ImageView) convertView.findViewById(R.id.iv_favorites_list_business_type_burger);
                viewHolder.ivFavoritesTaco = (ImageView) convertView.findViewById(R.id.iv_favorites_list_business_type_taco);
                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }

            if (arrayListTest.get(position).get("rating") != null) {
                viewHolder.rbFavorites.setStepSize(0.1f);
                viewHolder.rbFavorites.setRating(Float.parseFloat(arrayListTest.get(position).get("rating").toString()));
            } else {
                viewHolder.rbFavorites.setRating(0);
            }

            if (arrayListTest.get(position).get("number_of_ratings") != null) {
                if (arrayListTest.get(position).get("rating") != null) {
                    if (viewHolder.rbFavorites.getRating() == 0.0) {
                        viewHolder.tvFavoritesNumberOfRatings.setVisibility(View.VISIBLE);
                        viewHolder.tvFavoritesNumberOfRatings.setText("(0)");
                    } else {
                        viewHolder.tvFavoritesNumberOfRatings.setText("(" + arrayListTest.get(position).get("number_of_ratings") + ")");
                        viewHolder.tvFavoritesNumberOfRatings.setVisibility(View.VISIBLE);
                    }
                }
            } else {
                if (arrayListTest.get(position).get("rating") == null) {
                    viewHolder.tvFavoritesNumberOfRatings.setVisibility(View.VISIBLE);
                    viewHolder.tvFavoritesNumberOfRatings.setText("(0)");
                }
            }

            final String name = arrayListTest.get(position).get("name").toString();

            viewHolder.tvFavoritesBusinessName.setText(name);

            if (arrayListTest.get(position).get("timings") != null) {
                viewHolder.tvFavoritesTimings.setText(Helpers.getBusinessTimingsForCurrentDay(arrayListTest.get(position).get("timings").toString()));
            }

            if (arrayListTest.get(position).get("contact") != null) {
                viewHolder.ibFavoritesCall.setVisibility(View.VISIBLE);
                viewHolder.ibFavoritesCall.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        contactNumberToInitiateCall = arrayListTest.get(position).get("contact").toString();
                        Helpers.AlertDialogWithPositiveFunctionNegativeButton(getActivity(),
                                name, "Want to initiate a call?", "Yes", "No", initiateCall);
                    }
                });
            } else {
                viewHolder.ibFavoritesCall.setVisibility(View.GONE);
            }

            if (arrayListTest.get(position).get("food_menu") != null) {
                viewHolder.ibFavoritesMenu.setVisibility(View.VISIBLE);
                viewHolder.ibFavoritesMenu.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Helpers.urlFoodMenu = arrayListTest.get(position).get("food_menu").toString();
                        Helpers.AlertDialogWithPositiveFunctionNegativeButton(getActivity(),
                                name, "Want to see food menu?", "Yes", "No", showMenu);
                    }
                });
            } else {
                viewHolder.ibFavoritesMenu.setVisibility(View.GONE);
            }

            if (arrayListTest.get(position).get("location") != null) {
                viewHolder.ibFavoritesNavigate.setVisibility(View.VISIBLE);
                viewHolder.ibFavoritesNavigate.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        String[] stringToLatLng = arrayListTest.get(position).get("location").toString().split(",");
                        double latitude = Double.parseDouble(stringToLatLng[0]);
                        double longitude = Double.parseDouble(stringToLatLng[1]);
                        Helpers.latLngForNavigation = new LatLng(latitude, longitude);
                        Helpers.AlertDialogWithPositiveFunctionNegativeButton(getActivity(),
                                name, "Want to navigate?", "Yes", "No", navigate);
                    }
                });

            } else {
                viewHolder.ibFavoritesNavigate.setVisibility(View.GONE);
            }

            if (arrayListTest.get(position).get("formatted_address") != null) {
                viewHolder.tvFavoritesFormattedAddress.setVisibility(View.VISIBLE);
                viewHolder.tvFavoritesFormattedAddress.setText(arrayListTest.get(position).get("formatted_address").toString());
            } else {
                viewHolder.tvFavoritesFormattedAddress.setVisibility(View.GONE);
            }

            viewHolder.ibFavoritesDelete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    idToDeleteEntry = arrayListTest.get(position).get("id").toString();
                    Helpers.AlertDialogWithPositiveFunctionNegativeButton(getActivity(), name, "Are you sure you want to delete this entry?",
                            "Yes", "Cancel", deleteEntry);
                }
            });

            String businessType = arrayListTest.get(position).get("type").toString();
            Log.i("favoritesTesT", "" + businessType);

            if (!businessType.equals("null")) {
                String[] businessTypeArray = businessType.split(",");
                if (Boolean.parseBoolean(businessTypeArray[0])) {
                    viewHolder.ivFavoritesPizza.setVisibility(View.VISIBLE);
                }
                if (Boolean.parseBoolean(businessTypeArray[1])) {
                    viewHolder.ivFavoritesWings.setVisibility(View.VISIBLE);
                }
                if (Boolean.parseBoolean(businessTypeArray[2])) {
                    viewHolder.ivFavoritesBurger.setVisibility(View.VISIBLE);
                }
                if (Boolean.parseBoolean(businessTypeArray[3])) {
                    viewHolder.ivFavoritesTaco.setVisibility(View.VISIBLE);
                }
            }

            return convertView;
        }

        @Override
        public int getCount() {
            return arrayListTest.size();
        }
    }

    static class ViewHolder {
        RatingBar rbFavorites;
        TextView tvFavoritesBusinessName;
        TextView tvFavoritesNumberOfRatings;
        TextView tvFavoritesTimings;
        TextView tvFavoritesFormattedAddress;
        ImageButton ibFavoritesCall;
        ImageButton ibFavoritesMenu;
        ImageButton ibFavoritesNavigate;
        ImageButton ibFavoritesDelete;
        ImageView ivFavoritesPizza;
        ImageView ivFavoritesWings;
        ImageView ivFavoritesBurger;
        ImageView ivFavoritesTaco;
    }



}
