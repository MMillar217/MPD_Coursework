package com.marcmillar.S1828600.mpd.cw;
//Marc Millar - S1828600

import android.content.Context;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;


import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.maps.model.LatLng;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class RssItemRecyclerViewAdapter extends RecyclerView.Adapter<RssItemRecyclerViewAdapter.RssViewHolder> {
	private static final String TAG = "RecyclerViewAdapter";

	private List<GeoRSSItem> mRssItems;
	private Context mContext;

	//constructor
	public RssItemRecyclerViewAdapter(List<GeoRSSItem> rssItems, Context context) {
		mRssItems = rssItems;
		mContext = context;
	}

	//sets the items
	public void setRssItems(List<GeoRSSItem> items) {
		this.mRssItems = items;
		notifyDataSetChanged();
	}

	//when the view holder is created
	@NonNull
	@Override
	public RssViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
		View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_listitem, parent, false);
		return new RssViewHolder(view);
	}

	//when the view holder is binded to the app
	@Override
	public void onBindViewHolder(@NonNull RssViewHolder holder, final int position) {
		Log.d(TAG, "onBindViewHolder: called.");
		holder.itemTitle.setText(mRssItems.get(position).getTitle());
		holder.itemDesc.setText(mRssItems.get(position).getDescription());
		holder.itemPubDate.setText("Published: " + mRssItems.get(position).getPubDate());

		//replace any occurances of "<br />" with a line separator
		String str = holder.itemDesc.getText().toString();
		if (holder.itemDesc.getText().toString().matches((".*[<br />].*"))) {

			String repStr = str.replace("<br />", System.getProperty("line.separator"));
			holder.itemDesc.setText(repStr);
		}
		try {
			//planned roadworks and roadworks dates
			String parts[] = str.split(" ");
			String startDate = parts[3] + " " + parts[4] + " " + parts[5];
			String endDate = parts[11] + " " + parts[12] + " " + parts[13];
			String sMonth = parts[4];
			String eMonth = parts[12];

			SimpleDateFormat formatDesc = new SimpleDateFormat("EEEE, dd MMMM yyyy - HH:mm");

			String startDateFromDesc = parts[2] + " " + parts[3] + " " + parts[4] + " " + parts[5] + " - " + parts[7].replace("<br", "");
			String endDateFromDesc = parts[10] + " " + parts[11] + " " + parts[12] + " " + parts[13] + " - " + parts[15].replace("<br", "");

			Date dateStarts = formatDesc.parse(startDateFromDesc);
			Date dateEnds = formatDesc.parse(endDateFromDesc);


			//in milliseconds
			long diff = dateEnds.getTime() - dateStarts.getTime();
			long diffDays = diff / (24 * 60 * 60 * 1000);
			//determining the colour of the text based on the amount of time a piece of roadworks will take
			//green
			if (diffDays <= 3) {
				//DrawableCompat.setTint(wrappedDrawable, (Color.parseColor("#00c717")));
				holder.itemTitle.setBackgroundColor(Color.parseColor("#00c717"));
			}
			//black
			if (diffDays >= 4 && diffDays <= 6) {
				//DrawableCompat.setTint(wrappedDrawable, Color.BLACK);
				holder.itemTitle.setBackgroundColor(Color.parseColor("#ffffff"));
			}
			//yellow
			if (diffDays >= 7 && diffDays <= 14) {
				//DrawableCompat.setTint(wrappedDrawable, (Color.parseColor("#ffff00")));
				holder.itemTitle.setBackgroundColor(Color.parseColor("#ffff00"));
			}
			//orange
			if (diffDays >= 14 && diffDays < 31) {
				//DrawableCompat.setTint(wrappedDrawable, Color.parseColor("#cf8804"));
				holder.itemTitle.setBackgroundColor(Color.parseColor("#cf8804"));

			}
			//red
			if (diffDays >= 31) {
				//DrawableCompat.setTint(wrappedDrawable, Color.RED);
				holder.itemTitle.setBackgroundColor(Color.parseColor("#f0000c"));
			}

			//find month number for each date
			int startMonthNum = findMonthNumber(sMonth);
			int endMonthNum = findMonthNumber(eMonth);

			//displays start and end dates
			String starting = parts[3] + "/" + startMonthNum + "/" + parts[5];
			String ending = parts[11] + "/" + endMonthNum + "/" + parts[13];
			holder.itemPoint.setText("Start Date: " + starting + " | End Date: " + ending);

			//get descriptions
			String description = "";
			for (int i = 16; i < parts.length; i++) {
				description = description + parts[i] + " ";
			}
			description = description.replace("/>", "");
			holder.itemDesc.setText(description.trim());
			holder.itemPubDate.setText("");

		} catch (Exception ex) {
			//current incidents dates
			holder.itemTitle.setBackgroundColor(Color.parseColor("#ffffff"));
			holder.itemPoint.setText("");
		}

		//add item to map
		holder.parentLayout.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				Log.d(TAG, "onClick: clicked on " + mRssItems.get(position).getTitle());
				Toast.makeText(mContext, mRssItems.get(position).getTitle() + " added to map", Toast.LENGTH_SHORT).show();

				String str = mRssItems.get(position).getGeorsPoint();
				String[] parts = str.split(" ");
				String strLat = parts[0];
				String strLng = parts[1];
				double lat = Double.parseDouble(strLat);
				double lng = Double.parseDouble(strLng);

				LatLng latLng = new LatLng(lat, lng);
				MainActivity main = MainActivity.getInstance();
				main.addMarker(latLng,
						mRssItems.get(position).getTitle(),
						mRssItems.get(position).getPubDate());

			}
		});
	}

	//finding month as a number for displaying start and end dates
	public int findMonthNumber(String month) {
		int monthNum;
		switch (month) {
			case "January":
				monthNum = 1;
				break;
			case "February":
				monthNum = 2;
				break;
			case "March":
				monthNum = 3;

				break;
			case "April":
				monthNum = 4;

				break;
			case "May":
				monthNum = 5;

				break;
			case "June":
				monthNum = 6;

				break;
			case "July":
				monthNum = 7;

				break;
			case "August":
				monthNum = 8;

				break;
			case "September":
				monthNum = 9;

				break;
			case "October":
				monthNum = 10;

				break;
			case "November":
				monthNum = 11;

				break;
			case "December":
				monthNum = 12;

				break;
			default:
				monthNum = 0;
		}
		return monthNum;
	}

	@Override
	public int getItemCount() {
		return mRssItems.size();
	}


	public class RssViewHolder extends RecyclerView.ViewHolder {
		TextView itemTitle;
		TextView itemDesc;
		TextView itemPubDate;
		TextView itemPoint;
		RelativeLayout parentLayout;

		public RssViewHolder(@NonNull View itemView) {
			super(itemView);

			itemTitle = itemView.findViewById(R.id.itemTitle);
			itemDesc = itemView.findViewById(R.id.itemDesc);
			itemPubDate = itemView.findViewById(R.id.itemPubDate);
			itemPoint = itemView.findViewById(R.id.itemPoint);
			parentLayout = itemView.findViewById(R.id.parentLayout);
		}
	}
}
