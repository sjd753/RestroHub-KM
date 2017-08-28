package com.ogma.restrohubadmin.activity;

import android.os.AsyncTask;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import com.ogma.restrohubadmin.R;
import com.ogma.restrohubadmin.application.App;
import com.ogma.restrohubadmin.application.AppSettings;
import com.ogma.restrohubadmin.enums.URL;
import com.ogma.restrohubadmin.network.HttpClient;
import com.ogma.restrohubadmin.network.NetworkConnection;

import org.json.JSONException;
import org.json.JSONObject;

public class AddTable extends AppCompatActivity {

    private App app;
    private CoordinatorLayout coordinatorLayout;

    private TextInputLayout tilTableName;
    private EditText etTableName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_table);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        assert getSupportActionBar() != null;
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        app = (App) getApplication();
        app.setAppSettings(new AppSettings(this));

        coordinatorLayout = (CoordinatorLayout) findViewById(R.id.coordinator_layout);

        tilTableName = (TextInputLayout) findViewById(R.id.til_table_name);
        etTableName = (EditText) findViewById(R.id.et_table_name);

    }

    public void onClick(View view) {
        if (view.getId() == R.id.btn_save) {
            if (validate() && prepareExecuteAsync()) {
                new AddTableTask().execute(etTableName.getText().toString().trim());
            }
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

    private boolean validate() {
        if (TextUtils.isEmpty(etTableName.getText().toString().trim())) {
            tilTableName.setErrorEnabled(true);
            tilTableName.setError("Please enter table name");
            return false;
        } else {
            tilTableName.setErrorEnabled(false);
        }

        return true;
    }


    private class AddTableTask extends AsyncTask<String, Void, Boolean> {
        private String error_msg = "Server error!";
        private Snackbar snackbar;
        private JSONObject response;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            snackbar = Snackbar.make(coordinatorLayout, "Creating table", Snackbar.LENGTH_INDEFINITE);
            snackbar.show();
        }

        @Override
        protected Boolean doInBackground(String... params) {
            try {
                JSONObject mJsonObject = new JSONObject();
                mJsonObject.put("user_id", app.getAppSettings().__uId);
                mJsonObject.put("restaurant_id", app.getAppSettings().__uRestaurantId);
                mJsonObject.put("name", params[0]);
                mJsonObject.put("device_token", "");
                mJsonObject.put("device_type", "android");

                Log.e("Send Obj:", mJsonObject.toString());

                response = HttpClient.SendHttpPost(URL.CREATE_RESTAURANT_TABLE.getURL() + "/" + System.currentTimeMillis(), mJsonObject);

                return response != null && response.getInt("is_error") == 0;
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
                Snackbar snackbar = Snackbar.make(coordinatorLayout, "Table added successfully", Snackbar.LENGTH_LONG);
                snackbar.setCallback(new Snackbar.Callback() {
                    @Override
                    public void onDismissed(Snackbar snackbar, int event) {
                        super.onDismissed(snackbar, event);
                        AddTable.this.finish();
                    }
                });
                snackbar.show();
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
