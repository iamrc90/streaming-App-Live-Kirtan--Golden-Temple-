package com.example.kirtan;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class CustomArrayAdaptor extends ArrayAdapter<Station>{
	private Activity activity;
	private List<Station> stations;
	private Station objBean;
	private int row;

	public CustomArrayAdaptor(Activity act, int resource, List<Station> arrayList) {
		super(act, resource, arrayList);
		this.activity = act;
		this.row = resource;
		this.stations = arrayList;
	}

	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		View view = convertView;
		ViewHolder holder;
		
			LayoutInflater inflater = (LayoutInflater) activity
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			view = inflater.inflate(row, null);

			holder = new ViewHolder();
			view.setTag(holder);
		if ((stations == null) || ((position + 1) > stations.size()))
			return view;

		objBean = stations.get(position);

		holder.stationName = (TextView) view.findViewById(R.id.stationName);

		if (holder.stationName != null && null != objBean.getStationName()
				&& objBean.getStationName().trim().length() > 0) {
			holder.stationName.setText(Html.fromHtml(objBean.getStationName()));
		}
		return view;
	}

	public class ViewHolder {
		public TextView stationName;
	}
}
