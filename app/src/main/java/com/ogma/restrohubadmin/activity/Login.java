package com.ogma.restrohubadmin.activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;

import com.google.firebase.iid.FirebaseInstanceId;
import com.ogma.restrohubadmin.R;
import com.ogma.restrohubadmin.application.App;
import com.ogma.restrohubadmin.application.AppSettings;
import com.ogma.restrohubadmin.enums.URL;
import com.ogma.restrohubadmin.network.HttpClient;
import com.ogma.restrohubadmin.network.NetworkConnection;

import org.json.JSONException;
import org.json.JSONObject;

public class Login extends AppCompatActivity {

    private App app;
    private CoordinatorLayout coordinatorLayout;
    private TextInputLayout tilEmail;
    private EditText etEmail;
    private TextInputLayout tilPassword;
    private EditText etPassword;
    private CheckBox cbRememberMe;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        app = (App) getApplication();
        app.setAppSettings(new AppSettings(this));

        coordinatorLayout = (CoordinatorLayout) findViewById(R.id.coordinator_layout);

        tilEmail = (TextInputLayout) findViewById(R.id.til_email);
        etEmail = (EditText) findViewById(R.id.et_email);

        tilPassword = (TextInputLayout) findViewById(R.id.til_password);
        etPassword = (EditText) findViewById(R.id.et_password);

        cbRememberMe = (CheckBox) findViewById(R.id.cb_remember_me);

        //set the saved email and password
        etEmail.setText(app.getAppSettings().__uUsername);
        etPassword.setText(app.getAppSettings().__uPassword);
    }

    public void onClick(View view) {
        if (view.getId() == R.id.btn_login) {
            if (validate() && prepareExecuteAsync()) {
                new LoginTask().execute(etEmail.getText().toString().trim(),
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
        if (TextUtils.isEmpty(etEmail.getText().toString().trim())) {
            tilEmail.setErrorEnabled(true);
            tilEmail.setError("Please enter username");
            return false;
        } else {
            tilEmail.setErrorEnabled(false);
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


    private class LoginTask extends AsyncTask<String, Void, Boolean> {
        private String error_msg = "Server error!";
        private ProgressDialog mDialog = new ProgressDialog(Login.this);
        private JSONObject response;
        private String __uRestaurantId = "", __uId = "", __uUsername = "", __uPassword = "";

        @Override
        protected Boolean doInBackground(String... params) {
            try {
                JSONObject mJsonObject = new JSONObject();
                mJsonObject.put("username", params[0]);
                mJsonObject.put("password", params[1]);
                mJsonObject.put("user_type", "KM");
                mJsonObject.put("device_id", FirebaseInstanceId.getInstance().getToken() != null ? FirebaseInstanceId.getInstance().getToken() : "");

                __uPassword = params[1];

                Log.e("Send Obj:", mJsonObject.toString());

                response = HttpClient.SendHttpPost(URL.LOGIN.getURL() + "/" + System.currentTimeMillis(), mJsonObject);
                boolean status = response != null && response.getInt("is_error") == 0;
                if (status) {
                    __uRestaurantId = response.getString("restaurant_id");
                    __uId = response.getString("id");
                    __uUsername = response.getString("username");

                }
                return status;
            } catch (JSONException | NullPointerException e) {
                e.printStackTrace();
                mDialog.dismiss();
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean status) {
            mDialog.dismiss();
            if (status) {
                if (cbRememberMe.isChecked()) {
                    app.getAppSettings().setSession(__uRestaurantId, __uId, __uUsername, __uPassword, true);
                } else {
                    app.getAppSettings().setSession(__uRestaurantId, __uId, __uUsername, "", true);
                }
                startActivity(new Intent(Login.this, Home.class)
                        .setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK));
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
