package com.ogma.restrohubadmin.fragment;


import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.ogma.restrohubadmin.R;
import com.ogma.restrohubadmin.activity.AddCategory;
import com.ogma.restrohubadmin.activity.Menus;
import com.ogma.restrohubadmin.application.App;
import com.ogma.restrohubadmin.application.AppSettings;
import com.ogma.restrohubadmin.enums.URL;
import com.ogma.restrohubadmin.network.HttpClient;
import com.ogma.restrohubadmin.network.NetworkConnection;
import com.ogma.restrohubadmin.utility.UniversalImageLoaderFactory;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * A simple {@link Fragment} subclass.
 */
public class CategoriesFragment extends Fragment {

    private static final String TAG = CategoriesFragment.class.getName();

    private App app;
    private RecyclerView recyclerView;
    private RecyclerAdapter recyclerAdapter;
    private CoordinatorLayout coordinatorLayout;
    private JSONArray jArr = new JSONArray();
    private ImageLoader imageLoader;

    public CategoriesFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        app = (App) getActivity().getApplication();
        app.setAppSettings(new AppSettings(getActivity()));
        imageLoader = new UniversalImageLoaderFactory.Builder(getActivity()).getInstance().initForAdapter().build();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_categories, container, false);

        coordinatorLayout = (CoordinatorLayout) view.findViewById(R.id.coordinatorLayout);

        recyclerView = (RecyclerView) view.findViewById(R.id.rv_categories);

        recyclerView.setHasFixedSize(true);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(linearLayoutManager);

        recyclerAdapter = new RecyclerAdapter();
        recyclerView.setAdapter(recyclerAdapter);
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (prepareExecuteAsync())
            new FetchCategoryTask().execute();
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

    private class RecyclerAdapter extends RecyclerView.Adapter<RecyclerAdapter.ViewHolder> {

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.category_item, parent, false);
            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    int position = recyclerView.getChildAdapterPosition(view);
                    Log.e(TAG, "onClick at position: " + position);
                    try {
                        startActivity(new Intent(getActivity(), Menus.class)
                                .putExtra(Menus.EXTRA_ID, jArr.getJSONObject(position).getString("id"))
                                .putExtra(Menus.EXTRA_NAME, jArr.getJSONObject(position).getString("name")));
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            try {
                imageLoader.displayImage(jArr.getJSONObject(position).getString("image"),
                        holder.ivCategory,
                        UniversalImageLoaderFactory.getDefaultOptions(R.drawable.loader));
                holder.tvCategory.setText(jArr.getJSONObject(position).getString("name"));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }


        @Override
        public int getItemCount() {
            return jArr.length();
        }

        public class ViewHolder extends RecyclerView.ViewHolder {

            public ImageView ivCategory;
            public TextView tvCategory;
            public Button btnEdit;

            public ViewHolder(View itemView) {
                super(itemView);
                ivCategory = (ImageView) itemView.findViewById(R.id.iv_item_category);
                tvCategory = (TextView) itemView.findViewById(R.id.tv_item_category);
                btnEdit = (Button) itemView.findViewById(R.id.btn_edit_category);
                btnEdit.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
//                        int position = recyclerView.getChildAdapterPosition(view);
                        int position = getAdapterPosition();
                        Log.e(TAG, "onClick on edit at position: " + position);
                        try {
                            startActivity(new Intent(getActivity(), AddCategory.class)
                                    .putExtra(AddCategory.EXTRA_EDIT_DATA, jArr.getJSONObject(position).toString()));
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
        }
    }

    private class FetchCategoryTask extends AsyncTask<String, Void, Boolean> {
        private String error_msg = "Server error!";
        private Snackbar snackbar;
        private JSONObject response;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            snackbar = Snackbar.make(coordinatorLayout, "Fetching categories...", Snackbar.LENGTH_INDEFINITE);
            snackbar.show();
        }

        @Override
        protected Boolean doInBackground(String... params) {
            try {
                JSONObject mJsonObject = new JSONObject();
                mJsonObject.put("restaurant_id", app.getAppSettings().__uRestaurantId);

                Log.e("Send Obj:", mJsonObject.toString());

                response = HttpClient.SendHttpPost(URL.CATEGORY_LIST.getURL(), mJsonObject);
                boolean status = response != null && response.getInt("is_error") == 0;
                if (status) {
                    jArr = response.getJSONArray("category");
                }
                return status;
            } catch (JSONException | NullPointerException e) {
                e.printStackTrace();
                snackbar.dismiss();
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean status) {
            snackbar.dismiss();
            if (status) {
                recyclerAdapter.notifyDataSetChanged();
            } else {
                try {
                    Snackbar.make(coordinatorLayout, response.getString("err_msg"), Snackbar.LENGTH_LONG).show();
                } catch (JSONException | NullPointerException e) {
                    e.printStackTrace();
                    Snackbar.make(coordinatorLayout, error_msg, Snackbar.LENGTH_SHORT).show();
                }
            }
        }
    }
}
