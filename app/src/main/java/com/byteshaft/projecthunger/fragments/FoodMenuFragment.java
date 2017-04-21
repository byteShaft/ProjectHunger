package com.byteshaft.projecthunger.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;

import com.byteshaft.projecthunger.R;
import com.byteshaft.projecthunger.utils.Helpers;

/**
 * Created by fi8er1 on 02/11/2016.
 */

public class FoodMenuFragment extends Fragment {

    View baseViewFoodMenuFragment;
    ProgressBar pbFoodMenuFragment;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        baseViewFoodMenuFragment = inflater.inflate(R.layout.fragment_food_menu, container, false);
        String urlReadyForGoogleDocs = "https://docs.google.com/gview?embedded=true&url=http://139.59.187.73" + Helpers.urlFoodMenu;
        pbFoodMenuFragment = (ProgressBar) baseViewFoodMenuFragment.findViewById(R.id.pb_food_menu_fragment);

        final WebView wvFoodMenuFragment = (WebView) baseViewFoodMenuFragment.findViewById(R.id.wv_food_menu_fragment);
        WebSettings webSettings = wvFoodMenuFragment.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setBuiltInZoomControls(true);
        webSettings.setUseWideViewPort(true);
        webSettings.setLoadWithOverviewMode(true);
        wvFoodMenuFragment.loadUrl(urlReadyForGoogleDocs);
        wvFoodMenuFragment.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                pbFoodMenuFragment.setVisibility(View.GONE);
                wvFoodMenuFragment.setVisibility(View.VISIBLE);
            }
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                view.loadUrl(url);
                return true;
            }
        });
        return baseViewFoodMenuFragment;
    }
}
