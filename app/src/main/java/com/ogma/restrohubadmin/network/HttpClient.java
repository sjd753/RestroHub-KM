
package com.ogma.restrohubadmin.network;

import android.util.Log;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.zip.GZIPInputStream;

public class HttpClient {
	private static final String TAG = "HttpClient";

	public static JSONObject SendHttpPost(String URL, JSONObject jsonObjSend) {

		try {
			DefaultHttpClient httpclient = new DefaultHttpClient();
			HttpPost httpPostRequest = new HttpPost(URL);

			StringEntity se;
			se = new StringEntity(jsonObjSend.toString());

			httpPostRequest.setEntity(se);
			httpPostRequest.setHeader("Accept", "application/json");
			httpPostRequest.setHeader("Content-type", "application/json");
			httpPostRequest.setHeader("Accept-Encoding", "gzip"); 

			long t = System.currentTimeMillis();
			HttpResponse response = (HttpResponse) httpclient.execute(httpPostRequest);
			Log.i(TAG, "HTTPResponse received in [" + (System.currentTimeMillis() - t) + "ms]");

			HttpEntity entity = response.getEntity();

			if (entity != null) {
				InputStream instream = entity.getContent();
				Header contentEncoding = response.getFirstHeader("Content-Encoding");
				if (contentEncoding != null && contentEncoding.getValue().equalsIgnoreCase("gzip")) {
					instream = new GZIPInputStream(instream);
				}

				String resultString= convertStreamToString(instream);
				instream.close();
				resultString = resultString.substring(0,resultString.length()-1); 

				JSONObject jsonObjRecv = new JSONObject(resultString);
				Log.i(TAG, "<JSONObject>\n" + jsonObjRecv.toString() + "\n</JSONObject>");

				return jsonObjRecv;
			} 

		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return null;
	}


	private static String convertStreamToString(InputStream is) {
		BufferedReader reader = new BufferedReader(new InputStreamReader(is));
		StringBuilder sb = new StringBuilder();

		String line = null;
		try {
			while ((line = reader.readLine()) != null) {
				sb.append(line + "\n");
				Log.e("JSON", "" + line);
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				is.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return sb.toString();
	}

}
