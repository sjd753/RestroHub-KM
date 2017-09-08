package com.ogma.restrohubadmin.fragment;


import android.database.DataSetObserver;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.Button;
import android.widget.ExpandableListView;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
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
public class OrdersTabFragment extends Fragment {

    private App app;
    private NetworkConnection connection;
    private ImageLoader imageLoader;
    private CoordinatorLayout coordinatorLayout;
    private SwipeRefreshLayout swipeRefreshLayout;
    private JSONArray jArr = new JSONArray();
    private ExpandableListView expandableListView;
    private ExpandableListAdapter expandableListAdapter;
//    private boolean isPending;


    public OrdersTabFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        app = (App) getActivity().getApplication();
        app.setAppSettings(new AppSettings(getActivity()));
        connection = new NetworkConnection(getActivity());
        imageLoader = ImageLoader.getInstance();
        if (!imageLoader.isInited())
            imageLoader.init(ImageLoaderConfiguration.createDefault(getActivity()));
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_orders_tab, container, false);

        coordinatorLayout = (CoordinatorLayout) view.findViewById(R.id.coordinator_layout);
        swipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.swipe_refresh_layout);
        swipeRefreshLayout.setColorSchemeResources(R.color.colorAccent, R.color.colorPrimary, android.R.color.black);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                Fragment fragment = getActivity().getSupportFragmentManager().findFragmentById(R.id.fragment_container);
                if (fragment != null && fragment instanceof OrdersFragment && prepareExecuteAsync()) {
                    ((OrdersFragment) fragment).new FetchOrdersTask().execute();
                    swipeRefreshLayout.setRefreshing(true);
                }
            }
        });

        expandableListAdapter = new ExpandableListAdapter();
        expandableListView = (ExpandableListView) view.findViewById(R.id.elv_orders);
        expandableListView.setAdapter(expandableListAdapter);

        return view;
    }

    private boolean prepareExecuteAsync() {
        if (connection.isNetworkConnected()) {
            return true;
        } else if (connection.isNetworkConnectingOrConnected()) {
            Snackbar.make(coordinatorLayout, "Connection temporarily unavailable", Snackbar.LENGTH_SHORT).show();
        } else {
            Snackbar.make(coordinatorLayout, "You're offline", Snackbar.LENGTH_SHORT).show();
        }
        return false;
    }

    public void notifyDataSetChanged(JSONArray jArr) {
        this.jArr = jArr;

        swipeRefreshLayout.setRefreshing(false);
        expandableListAdapter.notifyDataSetChanged();
        if (expandableListAdapter.getGroupCount() > 0) {
            expandAll();
            expandableListView.setBackgroundColor(Color.WHITE);
        } else {
            expandableListView.setBackgroundColor(Color.TRANSPARENT);
        }
    }


    private void expandAll() {
        for (int i = 0; i < expandableListAdapter.getGroupCount(); i++) {
            expandableListView.expandGroup(i);
        }
    }

    private class ExpandableListAdapter extends BaseExpandableListAdapter {
        private GroupViewHolder groupViewHolder;
        private ChildViewHolder childViewHolder;

        @Override
        public void registerDataSetObserver(DataSetObserver dataSetObserver) {
            super.registerDataSetObserver(dataSetObserver);
        }

        @Override
        public void unregisterDataSetObserver(DataSetObserver dataSetObserver) {
            super.unregisterDataSetObserver(dataSetObserver);
        }

        @Override
        public int getGroupCount() {
            return jArr.length();
        }

        @Override
        public int getChildrenCount(int groupPosition) {
            return jArr.optJSONObject(groupPosition).optJSONArray("order_list").length();
        }

        @Override
        public Object getGroup(int groupPosition) {
            return jArr.optJSONObject(groupPosition);
        }

        @Override
        public Object getChild(int groupPosition, int childPosition) {
            return jArr.optJSONObject(groupPosition).optJSONArray("order_list").optJSONObject(childPosition);
        }

        @Override
        public long getGroupId(int groupPosition) {
            return jArr.optJSONObject(groupPosition).optInt("order_id");
        }

        @Override
        public long getChildId(int groupPosition, int childPosition) {
            return jArr.optJSONObject(groupPosition).optJSONArray("order_list").optJSONObject(childPosition).optInt("id");
        }

        @Override
        public boolean hasStableIds() {
            return false;
        }

        @Override
        public View getGroupView(final int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
            View itemView = convertView;
            if (itemView == null) {
                itemView = getActivity().getLayoutInflater().inflate(R.layout.order_group_item, parent, false);
                groupViewHolder = new GroupViewHolder();
                groupViewHolder.tvTitle = (TextView) itemView.findViewById(R.id.tv_title);
                groupViewHolder.btnAction = (Button) itemView.findViewById(R.id.btn_group_action);
                itemView.setTag(groupViewHolder);
            } else {
                groupViewHolder = (GroupViewHolder) itemView.getTag();
            }

            groupViewHolder.tvTitle.setText(jArr.optJSONObject(groupPosition).optString("table_name"));

            Log.e("orderstatus ", jArr.optJSONObject(groupPosition).optString("order_status"));
            if (jArr.optJSONObject(groupPosition).optString("order_status").equals("pending")) {
                groupViewHolder.btnAction.setVisibility(View.VISIBLE);
                groupViewHolder.btnAction.setText("PROCESS");
                groupViewHolder.btnAction.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        String orderId = String.valueOf(getGroupId(groupPosition));
                        String orderStatus = jArr.optJSONObject(groupPosition).optString("order_status");
                        Log.e("onClick: ", orderId);
                        if (prepareExecuteAsync())
                            new ChangeOrderItemStatusTask().execute(orderId, orderStatus);
                    }
                });
            } else if (jArr.optJSONObject(groupPosition).optString("order_status").equals("processing")) {
                groupViewHolder.btnAction.setVisibility(View.VISIBLE);
                groupViewHolder.btnAction.setText("SERVE");
                groupViewHolder.btnAction.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        String orderId = String.valueOf(getGroupId(groupPosition));
                        String orderStatus = jArr.optJSONObject(groupPosition).optString("order_status");
                        Log.e("onClick: ", orderId);
                        if (prepareExecuteAsync())
                            new ChangeOrderItemStatusTask().execute(orderId, orderStatus);
                    }
                });
            } else {
                groupViewHolder.btnAction.setVisibility(View.GONE);
            }

//                try {
//                    groupViewHolder.tvTitle.setText(jsonArray.getJSONObject(groupPosition).getString("category_name"));
//                } catch (JSONException e) {
//                    e.printStackTrace();
//                }

//                ((ExpandableListView) parent).expandGroup(groupPosition);

            return itemView;
        }

        @Override
        public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
            View itemView = convertView;
            if (itemView == null) {
                itemView = getActivity().getLayoutInflater().inflate(R.layout.order_child_item, parent, false);
                childViewHolder = new ChildViewHolder();
                childViewHolder.tvTitle = (TextView) itemView.findViewById(R.id.tv_child_item_name);
                childViewHolder.tvQuantity = (TextView) itemView.findViewById(R.id.tv_child_item_quantity);
                childViewHolder.tvOrderStatus = (TextView) itemView.findViewById(R.id.tv_child_item_order_status);
                childViewHolder.tvTotalPrice = (TextView) itemView.findViewById(R.id.tv_child_item_total_price);
                childViewHolder.tvPrice = (TextView) itemView.findViewById(R.id.tv_child_item_price);
                itemView.setTag(childViewHolder);
            } else {
                childViewHolder = (ChildViewHolder) itemView.getTag();
            }

            childViewHolder.tvTitle.setText(jArr.optJSONObject(groupPosition)
                    .optJSONArray("order_list").optJSONObject(childPosition).optString("menu_name"));
            int quantity = Integer.parseInt(jArr.optJSONObject(groupPosition)
                    .optJSONArray("order_list").optJSONObject(childPosition).optString("quantity"));
            childViewHolder.tvQuantity.setText("Quantity: " + quantity);
//            if (list.get(groupPosition).getMenuItems().get(childPosition).getStatus().equals(DatabaseHandler.Tables.OrderDetails.OrderStatus.PENDING.getStatus())) {
//                childViewHolder.tvOrderStatus.setVisibility(View.INVISIBLE);
//            } else {
//                childViewHolder.tvOrderStatus.setVisibility(View.VISIBLE);
//            }
            float offerPrice = Float.parseFloat(jArr.optJSONObject(groupPosition)
                    .optJSONArray("order_list").optJSONObject(childPosition).optString("offer_price"));
            childViewHolder.tvPrice.setText("$" + offerPrice);
            childViewHolder.tvTotalPrice.setText("$" + offerPrice * quantity);

//                try {
//                    childViewHolder.tvTitle.setText(jsonArray.getJSONObject(groupPosition).getJSONArray("menu")
//                            .getJSONObject(childPosition).getString("name"));
//                    int quantity = Integer.parseInt(jsonArray.getJSONObject(groupPosition)
//                            .getJSONArray("menu").getJSONObject(childPosition)
//                            .getString("quantity"));
//                    childViewHolder.tvQuantity.setText("Quantity: " + quantity);
//                    int offerPrice = Integer.parseInt(jsonArray.getJSONObject(groupPosition)
//                            .getJSONArray("menu").getJSONObject(childPosition)
//                            .getString("offer_price"));
//                    childViewHolder.tvPrice.setText("$" + offerPrice);
//                    childViewHolder.tvTotalPrice.setText("$" + offerPrice * quantity);
//
//                } catch (JSONException e) {
//                    e.printStackTrace();
//                }

            return itemView;
        }

        @Override
        public boolean isChildSelectable(int groupPosition, int childPosition) {
            return false;
        }

        @Override
        public boolean areAllItemsEnabled() {
            return true;
        }

        @Override
        public boolean isEmpty() {
            return false;
        }

        @Override
        public void onGroupExpanded(int groupPosition) {

        }

        @Override
        public void onGroupCollapsed(int groupPosition) {

        }

        @Override
        public long getCombinedChildId(long groupId, long childId) {
            return 0;
        }

        @Override
        public long getCombinedGroupId(long groupId) {
            return 0;
        }

        class GroupViewHolder {
            TextView tvTitle;
            Button btnAction;
        }

        class ChildViewHolder {
            TextView tvTitle, tvQuantity, tvOrderStatus, tvTotalPrice, tvPrice;
        }
    }

    public class ChangeOrderItemStatusTask extends AsyncTask<String, Void, Boolean> {
        private String error_msg = "Server error!";
        private JSONObject response;

        @Override
        protected Boolean doInBackground(String... params) {
            try {
                JSONObject mJsonObject = new JSONObject();
                mJsonObject.put("user_id", app.getAppSettings().__uId);
                mJsonObject.put("restaurant_id", app.getAppSettings().__uRestaurantId);
                mJsonObject.put("order_id", params[0]);
                mJsonObject.put("order_status", params[1]);


                Log.e("Send Obj:", mJsonObject.toString());

                response = HttpClient.SendHttpPost(URL.CHANGE_ORDER_ITEM_STATUS.getURL() + "/" + System.currentTimeMillis(), mJsonObject);

                return response != null && response.getInt("is_error") == 0;
            } catch (JSONException | NullPointerException e) {
                e.printStackTrace();
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean status) {
            if (status) {
                Fragment fragment = getActivity().getSupportFragmentManager().findFragmentById(R.id.fragment_container);
                if (fragment != null && fragment instanceof OrdersFragment && prepareExecuteAsync()) {
                    ((OrdersFragment) fragment).new FetchOrdersTask().execute();
                    swipeRefreshLayout.setRefreshing(true);
                }
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
