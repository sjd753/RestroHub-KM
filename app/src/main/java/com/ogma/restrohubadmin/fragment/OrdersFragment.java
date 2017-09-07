package com.ogma.restrohubadmin.fragment;


import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.ogma.restrohubadmin.R;
import com.ogma.restrohubadmin.application.App;
import com.ogma.restrohubadmin.application.AppSettings;
import com.ogma.restrohubadmin.enums.URL;
import com.ogma.restrohubadmin.network.HttpClient;
import com.ogma.restrohubadmin.network.NetworkConnection;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * A simple {@link Fragment} subclass.
 */
public class OrdersFragment extends Fragment {


    private App app;
    private CoordinatorLayout coordinatorLayout;
    private TabLayout tabLayout;
    private ViewPagerAdapter pagerAdapter;
    private ViewPager pager;
    private JSONArray jArr_new = new JSONArray();
    private JSONArray jArr_prep = new JSONArray();
    private JSONArray jArr_comp = new JSONArray();

    public OrdersFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        app = (App) getActivity().getApplication();
        app.setAppSettings(new AppSettings(getActivity()));
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_orders, container, false);

        FloatingActionButton fab = (FloatingActionButton) view.findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (prepareExecuteAsync())
                    new FetchOrdersTask().execute();
            }
        });

        coordinatorLayout = (CoordinatorLayout) view.findViewById(R.id.coordinator_layout);
        tabLayout = (TabLayout) view.findViewById(R.id.tab_layout);
        pagerAdapter = new ViewPagerAdapter(getActivity().getSupportFragmentManager());
        pager = (ViewPager) view.findViewById(R.id.view_pager);
        pager.setAdapter(pagerAdapter);
        pager.setOffscreenPageLimit(2);
        //Notice how The Tab Layout adn View Pager object are linked
        tabLayout.setupWithViewPager(pager);
//        pager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));

        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        //Must be called after pager adapter is set since pager fragment is notified onPostExecute
        if (prepareExecuteAsync())
            new FetchOrdersTask().execute();
    }

    private boolean prepareExecuteAsync() {
        NetworkConnection connection = new NetworkConnection(getActivity());
        if (connection.isNetworkConnected()) {
            return true;
        } else if (connection.isNetworkConnectingOrConnected()) {
            Snackbar.make(coordinatorLayout, "Connection temporarily unavailable", Snackbar.LENGTH_SHORT).show();
        } else {
            Snackbar.make(coordinatorLayout, "You're offline", Snackbar.LENGTH_SHORT).show();
        }
        return false;
    }

    private class ViewPagerAdapter extends FragmentStatePagerAdapter {

        private final String titles[] = getResources().getStringArray(R.array.order_tabs);

        public ViewPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0:
                    return new OrdersTabFragment();
                case 1:
                    return new OrdersTabFragment();
                case 2:
                    return new OrdersTabFragment();
                default:
                    return new Fragment();
            }
        }

        @Override
        public int getCount() {
            return titles.length;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return titles[position];
        }
    }

    public class FetchOrdersTask extends AsyncTask<String, Void, Boolean> {
        private String error_msg = "Server error!";
        private JSONObject response;
        private Snackbar snackbar;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            snackbar = Snackbar.make(coordinatorLayout, "Updating orders...", Snackbar.LENGTH_INDEFINITE);
            snackbar.show();
        }

        @Override
        protected Boolean doInBackground(String... params) {
            try {
                JSONObject mJsonObject = new JSONObject();
                mJsonObject.put("user_id", app.getAppSettings().__uId);
                mJsonObject.put("restaurant_id", app.getAppSettings().__uRestaurantId);

                Log.e("Send Obj:", mJsonObject.toString());

                response = HttpClient.SendHttpPost(URL.ORDER_LIST.getURL() + "/" + System.currentTimeMillis(), mJsonObject);

                boolean status = response != null && response.getInt("is_error") == 0;
                if (status) {
                    jArr_new = response.getJSONArray("new_orders");
                    jArr_prep = response.getJSONArray("processing");
                    jArr_comp = response.getJSONArray("serving");
                } else {
                    jArr_new = new JSONArray();
                    jArr_prep = new JSONArray();
                    jArr_comp = new JSONArray();
                }
                return status;
            } catch (JSONException | NullPointerException e) {
                e.printStackTrace();
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean status) {
            snackbar.dismiss();
            notifyPagerFragments();
            if (status) {
                Snackbar.make(coordinatorLayout, "Updated successfully", Snackbar.LENGTH_LONG).show();
            } else {
                try {
                    Snackbar.make(coordinatorLayout, response.getString("err_msg"), Snackbar.LENGTH_LONG).show();
                } catch (JSONException | NullPointerException e) {
                    e.printStackTrace();
                    Snackbar.make(coordinatorLayout, error_msg, Snackbar.LENGTH_SHORT).show();
                }
            }
        }

        private void notifyPagerFragments() {
            for (int i = 0; i < pagerAdapter.getCount(); i++) {
                Fragment fragment = (Fragment) pagerAdapter.instantiateItem(pager, i);
                if (fragment instanceof OrdersTabFragment) {
                    if (pagerAdapter.getPageTitle(i).toString().equals(getString(R.string.tab_pending)))
                        ((OrdersTabFragment) fragment).notifyDataSetChanged(jArr_new, true);
                    else if (pagerAdapter.getPageTitle(i).toString().equals(getString(R.string.tab_processing)))
                        ((OrdersTabFragment) fragment).notifyDataSetChanged(jArr_prep, false);
                    else if (pagerAdapter.getPageTitle(i).toString().equals(getString(R.string.tab_serving)))
                        ((OrdersTabFragment) fragment).notifyDataSetChanged(jArr_comp, false);
                    else {
                        throw new IllegalArgumentException("Given viewpager title " + pagerAdapter.getPageTitle(i) + " does not match any of the predefined titles in the strings.xml file.");
                    }
                }
            }
        }
    }

}
