package com.marcmillar.S1828600.mpd.cw;
//Marc Millar - S1828600
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;


import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

//RecyclerView adapter - handles the items after they have been parsed
public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.ViewHolder> {
	private static final String TAG = "RecyclerViewAdapter";

	private ArrayList<String> mItemTitles = new ArrayList<>();
	private ArrayList<String> mItemDescs = new ArrayList<>();
	private ArrayList<String> mItemPubDates = new ArrayList<>();
	private ArrayList<String> mItemPoints = new ArrayList<>();
	private Context mContext;

	//constructor
	public RecyclerViewAdapter(ArrayList<String> itemTitles, ArrayList<String> itemDescs,
	                           ArrayList<String> itemPubDates, ArrayList<String> itemPoints,
	                           Context context) {
		mItemTitles = itemTitles;
		mItemDescs = itemDescs;
		mItemPubDates = itemPubDates;
		mItemPoints = itemPoints;
		mContext = context;
	}

	//when view holder is created
	@NonNull
	@Override
	public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
		View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_listitem, parent, false);
		ViewHolder holder = new ViewHolder(view);
		return holder;
	}

	//when the view holder is binded to the app
	@Override
	public void onBindViewHolder(@NonNull ViewHolder holder, final int position) {
		Log.d(TAG, "onBindViewHolder: called.");
		holder.itemTitle.setText(mItemTitles.get(position));
		holder.itemDesc.setText(mItemDescs.get(position));
		holder.itemPubDate.setText(mItemPubDates.get(position));
		holder.itemPoint.setText(mItemPoints.get(position));

		holder.parentLayout.setOnClickListener(new View.OnClickListener(){
			@Override
			public void onClick(View view){
				Log.d(TAG, "onClick: clicked on " + mItemTitles.get(position));
				Toast.makeText(mContext, mItemTitles.get(position), Toast.LENGTH_SHORT).show();
			}
		});
	}

	@Override
	public int getItemCount() {
		return mItemTitles.size();
	}

	//send text to recyclerview item
	public class ViewHolder extends RecyclerView.ViewHolder{

		TextView itemTitle;
		TextView itemDesc;
		TextView itemPubDate;
		TextView itemPoint;
		RelativeLayout parentLayout;
		public ViewHolder(@NonNull View itemView) {
			super(itemView);
			itemTitle = itemView.findViewById(R.id.itemTitle);
			itemDesc = itemView.findViewById(R.id.itemDesc);
			itemPubDate = itemView.findViewById(R.id.itemPubDate);
			itemPoint = itemView.findViewById(R.id.itemPoint);
			parentLayout = itemView.findViewById(R.id.parentLayout);
		}
	}
}
