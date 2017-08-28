package com.ogma.restrohubadmin.activity;

import android.app.ProgressDialog;
import android.content.Intent;
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

public class AddUser extends AppCompatActivity {

    private App app;
    private CoordinatorLayout coordinatorLayout;

    private TextInputLayout tilFirstName;
    private EditText etFirstName;
    private TextInputLayout tilLastName;
    private EditText etLastName;
    private TextInputLayout tilEmail;
    private EditText etEmail;
    private TextInputLayout tilUsername;
    private EditText etUsername;
    private TextInputLayout tilPassword;
    private EditText etPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_user);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        assert getSupportActionBar() != null;
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        app = (App) getApplication();
        app.setAppSettings(new AppSettings(this));

        coordinatorLayout = (CoordinatorLayout) findViewById(R.id.coordinator_layout);

        tilFirstName = (TextInputLayout) findViewById(R.id.til_first_name);
        etFirstName = (EditText) findViewById(R.id.et_first_name);

        tilLastName = (TextInputLayout) findViewById(R.id.til_last_name);
        etLastName = (EditText) findViewById(R.id.et_last_name);

        tilEmail = (TextInputLayout) findViewById(R.id.til_email);
        etEmail = (EditText) findViewById(R.id.et_email);

        tilUsername = (TextInputLayout) findViewById(R.id.til_username);
        etUsername = (EditText) findViewById(R.id.et_username);

        tilPassword = (TextInputLayout) findViewById(R.id.til_password);
        etPassword = (EditText) findViewById(R.id.et_password);
    }

    public void onClick(View view) {
        if (view.getId() == R.id.btn_save) {
            if (validate() && prepareExecuteAsync()) {
                new AddUserTask().execute(
                        etFirstName.getText().toString().trim(),
                        etLastName.getText().toString().trim().trim(),
                        etEmail.getText().toString().trim(),
                        etUsername.getText().toString().trim(),
                        etPassword.getText().toString());
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
        if (TextUtils.isEmpty(etFirstName.getText().toString().trim())) {
            tilFirstName.setErrorEnabled(true);
            tilFirstName.setError("Please enter first name");
            return false;
        } else {
            tilFirstName.setErrorEnabled(false);
        }

        if (TextUtils.isEmpty(etLastName.getText().toString().trim())) {
            tilLastName.setErrorEnabled(true);
            tilLastName.setError("Please enter last name");
            return false;
        } else {
            tilLastName.setErrorEnabled(false);
        }

//        if (TextUtils.isEmpty(etEmail.getText().toString().trim())) {
//            tilEmail.setErrorEnabled(true);
//            tilEmail.setError("Please enter email");
//            return false;
//        } else {
//            tilEmail.setErrorEnabled(false);
//        }

        if (TextUtils.isEmpty(etUsername.getText().toString().trim())) {
            tilUsername.setErrorEnabled(true);
            tilUsername.setError("Please enter username");
            return false;
        } else {
            tilUsername.setErrorEnabled(false);
        }

        if (TextUtils.isEmpty(etPassword.getText().toString().trim())) {
            tilPassword.setErrorEnabled(true);
            tilPassword.setError("Please enter password");
            return false;
        } else {
            tilPassword.setErrorEnabled(false);
        }

        return true;
    }


    private class AddUserTask extends AsyncTask<String, Void, Boolean> {
        private String error_msg = "Server error!";
        private Snackbar snackbar;
        private JSONObject response;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            snackbar = Snackbar.make(coordinatorLayout, "Creating user", Snackbar.LENGTH_INDEFINITE);
            snackbar.show();
        }

        @Override
        protected Boolean doInBackground(String... params) {
            try {
                JSONObject mJsonObject = new JSONObject();
                mJsonObject.put("restaurant_id", app.getAppSettings().__uRestaurantId);
                mJsonObject.put("firstname", params[0]);
                mJsonObject.put("lastname", params[1]);
                mJsonObject.put("email", params[2]);
                mJsonObject.put("username", params[3]);
                mJsonObject.put("password", params[4]);
                mJsonObject.put("user_type", "RC");
                mJsonObject.put("device_token", "");
                mJsonObject.put("device_type", "android");

                Log.e("Send Obj:", mJsonObject.toString());

                response = HttpClient.SendHttpPost(URL.CREATE_RESTAURANT_CUSTOMER.getURL() + "/" + System.currentTimeMillis(), mJsonObject);

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
                Snackbar snackbar = Snackbar.make(coordinatorLayout, "User added successfully", Snackbar.LENGTH_LONG);
                snackbar.setCallback(new Snackbar.Callback() {
                    @Override
                    public void onDismissed(Snackbar snackbar, int event) {
                        super.onDismissed(snackbar, event);
                        AddUser.this.finish();
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
