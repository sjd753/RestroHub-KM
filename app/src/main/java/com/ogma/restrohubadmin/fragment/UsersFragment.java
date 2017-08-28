package com.ogma.restrohubadmin.fragment;


import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import com.ogma.restrohubadmin.R;
import com.ogma.restrohubadmin.activity.AddTable;
import com.ogma.restrohubadmin.activity.AddUser;
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
public class UsersFragment extends Fragment implements View.OnClickListener {

    private static final String TAG = TablesFragment.class.getName();

    private App app;
    private RecyclerView recyclerView;
    private RecyclerAdapter recyclerAdapter;
    private CoordinatorLayout coordinatorLayout;
    private FloatingActionButton fab;
    private JSONArray jArr = new JSONArray();

    public UsersFragment() {
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
        View view = inflater.inflate(R.layout.fragment_users, container, false);

        coordinatorLayout = (CoordinatorLayout) view.findViewById(R.id.coordinator_layout);
        fab = (FloatingActionButton) view.findViewById(R.id.fab);
        fab.setOnClickListener(this);

        recyclerView = (RecyclerView) view.findViewById(R.id.rv_users);

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
            new FetchUsersTask().execute();
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

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.fab:
                startActivity(new Intent(getActivity(), AddUser.class));
                break;
            default:
                break;
        }
    }

    private class RecyclerAdapter extends RecyclerView.Adapter<RecyclerAdapter.ViewHolder> {

        @Override
        public RecyclerAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.user_item, parent, false);
            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    int position = recyclerView.getChildAdapterPosition(view);
                    Log.e(TAG, "onClick at position: " + position);

                }
            });
            return new RecyclerAdapter.ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(RecyclerAdapter.ViewHolder holder, int position) {
            try {
                String name = jArr.getJSONObject(position).getString("first_name") + " " +
                        jArr.getJSONObject(position).getString("last_name");
                holder.tvName.setText(name);
                String username = "Username: " + jArr.getJSONObject(position).getString("username");
                holder.tvUsername.setText(username);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }


        @Override
        public int getItemCount() {
            return jArr.length();
        }

        public class ViewHolder extends RecyclerView.ViewHolder {

            public TextView tvName, tvUsername;
            public ImageButton ivBtnEdit, ivBtnDelete;

            public ViewHolder(View itemView) {
                super(itemView);
                tvName = (TextView) itemView.findViewById(R.id.tv_name);
                tvUsername = (TextView) itemView.findViewById(R.id.tv_username);
                ivBtnEdit = (ImageButton) itemView.findViewById(R.id.iv_btn_edit);
                ivBtnDelete = (ImageButton) itemView.findViewById(R.id.iv_btn_delete);

                ivBtnEdit.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Log.e(TAG, "onClick: edit at position " + getAdapterPosition());
                    }
                });

                ivBtnDelete.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Log.e(TAG, "onClick: delete at position " + getAdapterPosition());
                    }
                });
            }
        }
    }

    private class FetchUsersTask extends AsyncTask<String, Void, Boolean> {
        private String error_msg = "Server error!";
        private Snackbar snackbar;
        private JSONObject response;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            snackbar = Snackbar.make(coordinatorLayout, "Fetching users...", Snackbar.LENGTH_INDEFINITE);
            snackbar.show();
        }

        @Override
        protected Boolean doInBackground(String... params) {
            try {
                JSONObject mJsonObject = new JSONObject();
                mJsonObject.put("restaurant_id", app.getAppSettings().__uRestaurantId);

                Log.e("Send Obj:", mJsonObject.toString());

                response = HttpClient.SendHttpPost(URL.CUSTOMER_LIST.getURL(), mJsonObject);
                boolean status = response != null && response.getInt("is_error") == 0;
                if (status) {
                    jArr = response.getJSONArray("customer_list");
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
