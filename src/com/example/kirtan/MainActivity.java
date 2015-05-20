package com.example.kirtan;

import java.util.ArrayList;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends ListActivity {
	private static final String stationsFeed = "http://bookshub.netau.net/stations.php";
	private static final String ARRAY_NAME = "stations";
	private static final String STATION_NAME = "stationName";
	private static final String STATION_URL = "stationURL";
	private ArrayList<Station> stationsList;
	private CustomArrayAdaptor objAdapter;
	private Intent serviceIntent;
	private boolean doubleBackToExitPressedOnce;
	private Handler mHandler = new Handler();
	private int currentStationPlaying = -1;
	private TextView currentRow;
	private boolean isPlaying = false;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// Download Json Data
		if (MyUtil.isNetworkAvailable(MainActivity.this)) {
			new DownloadData().execute(stationsFeed);
		} else {
			MyUtil.makeAlertDialog(this, "Error!!!!",
					"You are not connected to internet.", "Ok",
					new DialogInterface.OnClickListener() {

						@Override
						public void onClick(DialogInterface arg0, int arg1) {
							MainActivity.this.finish();
						}
					});
		}
		setContentView(R.layout.activity_main);
		serviceIntent = new Intent(this, MyPlayService.class);
		stationsList = new ArrayList<Station>();
	}

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);
	
		if(isPlaying){
			stopMyPlayService();
			if(currentStationPlaying == position){
				return;
			}
		}
		serviceIntent.putExtra("songToPlay", stationsList.get(position)
				.getStationURL());
		try {
			startService(serviceIntent);
			currentRow = (TextView)v.findViewById(R.id.subtitle);
			currentStationPlaying = position;
			currentRow.setText("Touch here to stop...");
			this.isPlaying = true;
		} catch (Exception e) {
			e.printStackTrace();
			MyUtil.makeToast(MainActivity.this,
					"Can't be played due to some internal error.");
		}
	}

	private void stopMyPlayService() {
		try {
			stopService(serviceIntent);
			currentRow.setText("");
			this.isPlaying = false;
			Log.d("test1", "service stopped");
		} catch (Exception e) {
			e.printStackTrace();
			MyUtil.makeToast(MainActivity.this, "Some internal error.");
		}
	}

	private class DownloadData extends AsyncTask<String, Void, String> {
		ProgressDialog pDialog;

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			pDialog = new ProgressDialog(MainActivity.this);
			pDialog.setMessage("Loading...");
			pDialog.setCancelable(false);
			pDialog.show();
		}

		@Override
		protected String doInBackground(String... params) {
			return MyUtil.getJSONString(params[0]);
		}

		@Override
		protected void onPostExecute(String result) {
			super.onPostExecute(result);
			Log.d("test",result);

			if (null != pDialog && pDialog.isShowing()) {
				pDialog.dismiss();
			}

			if (null == result || result.length() == 0) {
				MyUtil.makeToast(MainActivity.this,
						"No data Found on web!");
				MainActivity.this.finish();
			} else {

				try {
					JSONObject mainJson = new JSONObject(result);
					JSONArray jsonArray = mainJson.getJSONArray(ARRAY_NAME);
					for (int i = 0; i < jsonArray.length(); i++) {
						JSONObject objJson = jsonArray.getJSONObject(i);
						Station objItem = new Station();
						objItem.setStationName(objJson.getString(STATION_NAME));
						objItem.setStationURL(objJson.getString(STATION_URL));
						stationsList.add(objItem);
					}
				} catch (JSONException e) {
					e.printStackTrace();
				}
				setAdapterToListview();
			}
		}

		private void setAdapterToListview() {
			objAdapter = new CustomArrayAdaptor(MainActivity.this,
					R.layout.custom_cell_view, stationsList);
			setListAdapter(objAdapter);
			setVisible(true);
		}
	}

	private final Runnable mRunnable = new Runnable() {
		@Override
		public void run() {
			doubleBackToExitPressedOnce = false;
		}
	};

	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (mHandler != null) {
			mHandler.removeCallbacks(mRunnable);
		}
		if(isPlaying)
		stopMyPlayService();
		finish();
	}

	@Override
	public void onBackPressed() {
		if (doubleBackToExitPressedOnce) {
			super.onBackPressed();
			return;
		}
		this.doubleBackToExitPressedOnce = true;
		Toast.makeText(this, "Please click BACK again to exit",
				Toast.LENGTH_SHORT).show();

		mHandler.postDelayed(mRunnable, 2000);
	}
}