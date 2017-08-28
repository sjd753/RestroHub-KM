package com.ogma.restrohubadmin.network;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

public class NetworkConnection {

	private Context mContext;
	private ConnectivityManager cm;

	public NetworkConnection(Context context) {
		// TODO Auto-generated constructor stub
		mContext = context;
		cm = (ConnectivityManager) mContext
				.getSystemService(Context.CONNECTIVITY_SERVICE);
	}

	public boolean isNetworkConnected() {

		NetworkInfo activeNetworkInfo = cm.getActiveNetworkInfo();
		return activeNetworkInfo != null && activeNetworkInfo.isConnected();
	}

	public boolean isNetworkConnectingOrConnected() {

		NetworkInfo activeNetworkInfo = cm.getActiveNetworkInfo();
		return activeNetworkInfo != null
				&& activeNetworkInfo.isConnectedOrConnecting();
	}
}
