package com.ogma.restrohubadmin.activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.ogma.restrohubadmin.R;
import com.ogma.restrohubadmin.application.App;
import com.ogma.restrohubadmin.application.AppSettings;
import com.ogma.restrohubadmin.enums.URL;
import com.ogma.restrohubadmin.network.HttpClient;
import com.ogma.restrohubadmin.network.NetworkConnection;
import com.ogma.restrohubadmin.utility.UniversalImageLoaderFactory;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class Menus extends AppCompatActivity {

    public static final String EXTRA_ID = "category_id";
    public static final String EXTRA_NAME = "category_name";
    private static final String TAG = Menus.class.getName();

    private App app;
    private RecyclerView recyclerView;
    private RecyclerAdapter recyclerAdapter;
    private CoordinatorLayout coordinatorLayout;
    private JSONArray jArr = new JSONArray();
    private ImageLoader imageLoader;
    private String categoryId = "", categoryName = "";
    private int itemOfferPrice = 0;
    private FloatingActionButton fab;
    private JSONObject jSelectedItem;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menus);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        assert getSupportActionBar() != null;
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        app = (App) getApplication();
        app.setAppSettings(new AppSettings(this));

        imageLoader = new UniversalImageLoaderFactory.Builder(this).getInstance().initForAdapter().build();

        coordinatorLayout = (CoordinatorLayout) findViewById(R.id.coordinator_layout);

        recyclerView = (RecyclerView) findViewById(R.id.rv_menus);

        recyclerView.setHasFixedSize(true);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(linearLayoutManager);

        recyclerAdapter = new RecyclerAdapter();
        recyclerView.setAdapter(recyclerAdapter);

        fab = (FloatingActionButton) findViewById(R.id.fab);

        if (getIntent().getStringExtra(EXTRA_ID) != null && getIntent().getStringExtra(EXTRA_NAME) != null) {
            categoryId = getIntent().getStringExtra(EXTRA_ID);
            categoryName = getIntent().getStringExtra(EXTRA_NAME);
            if (prepareExecuteAsync())
                new FetchMenuTask().execute();
        } else {
            Snackbar snackbar = Snackbar.make(coordinatorLayout, "An internal error occurred", Snackbar.LENGTH_SHORT);
            snackbar.setCallback(new Snackbar.Callback() {
                @Override
                public void onDismissed(Snackbar snackbar, int event) {
                    super.onDismissed(snackbar, event);
                    finish();
                }

                @Override
                public void onShown(Snackbar snackbar) {
                    super.onShown(snackbar);
                }
            });
            snackbar.show();
        }
    }

    private boolean prepareExecuteAsync() {
        NetworkConnection connection = new NetworkConnection(this);
        if (connection.isNetworkConnected()) {
            return true;
        } else if (connection.isNetworkConnectingOrConnected()) {
            Snackbar.make(coordinatorLayout, "Connection temporarily unavailable", Snackbar.LENGTH_SHORT).show();
        } else {
            Snackbar.make(coordinatorLayout, "You're offline", Snackbar.LENGTH_SHORT).show();
        }
        return false;
    }

    @Override
    public boolean onCreateOptionsMenu(android.view.Menu menu) {
        getMenuInflater().inflate(R.menu.orders, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }

//        if (item.getItemId() == R.id.menu_action_cart) {
//            startActivity(new Intent(this, Cart.class));
////            Snackbar.make(coordinatorLayout, "Empty cart", Snackbar.LENGTH_SHORT).show();
//            return true;
//        }

        return super.onOptionsItemSelected(item);
    }

    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.fab:
                startActivity(new Intent(this, AddMenu.class).putExtra(AddMenu.EXTRA_CATEGORY_ID, categoryId));
                break;
            default:
                break;
        }
    }

    private class RecyclerAdapter extends RecyclerView.Adapter<RecyclerAdapter.ViewHolder> {

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.menu_item, parent, false);
            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    int position = recyclerView.getChildAdapterPosition(view);
                    Log.e(TAG, "onClick at position: " + position);
                    try {
                        startActivity(new Intent(Menus.this, AddMenu.class)
                                .putExtra(AddMenu.EXTRA_EDIT_DATA, jArr.getJSONObject(position).toString())
                                .putExtra(AddMenu.EXTRA_CATEGORY_ID, categoryId));
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
                holder.tvName.setText(jArr.getJSONObject(position).getString("name"));
                holder.tvDetail.setText(jArr.getJSONObject(position).getString("description"));
                holder.tvPrice.setText("$" + jArr.getJSONObject(position).getString("price"));
                holder.tvOfferPrice.setText("$" + jArr.getJSONObject(position).getString("offer_price"));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }


        @Override
        public int getItemCount() {
            return jArr.length();
        }

        public class ViewHolder extends RecyclerView.ViewHolder {

            public TextView tvName, tvDetail, tvPrice, tvOfferPrice;

            public ViewHolder(View itemView) {
                super(itemView);
                tvName = (TextView) itemView.findViewById(R.id.tv_menu_item_name);
                tvDetail = (TextView) itemView.findViewById(R.id.tv_menu_item_detail);
                tvPrice = (TextView) itemView.findViewById(R.id.tv_menu_item_price);
                tvOfferPrice = (TextView) itemView.findViewById(R.id.tv_menu_item_offer_price);
            }
        }
    }

    private class FetchMenuTask extends AsyncTask<String, Void, Boolean> {
        private String error_msg = "Server error!";
        private Snackbar snackbar;
        private JSONObject response;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            snackbar = Snackbar.make(coordinatorLayout, "Fetching menus...", Snackbar.LENGTH_INDEFINITE);
            snackbar.show();
        }

        @Override
        protected Boolean doInBackground(String... params) {
            try {
                JSONObject mJsonObject = new JSONObject();
                mJsonObject.put("category_id", categoryId);

                Log.e("Send Obj:", mJsonObject.toString());

                response = HttpClient.SendHttpPost(URL.MENU_LIST.getURL(), mJsonObject);
                boolean status = response != null && response.getInt("is_error") == 0;
                if (status) {
                    jArr = response.getJSONArray("menu");
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
