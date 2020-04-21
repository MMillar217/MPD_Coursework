package com.marcmillar.S1828600.mpd.cw;
//Marc Millar - S1828600
import android.content.Context;
import android.graphics.Bitmap;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.maps.android.clustering.Cluster;
import com.google.maps.android.clustering.ClusterManager;
import com.google.maps.android.clustering.view.DefaultClusterRenderer;
import com.google.maps.android.ui.IconGenerator;

public class MyClusterManagerRenderer extends DefaultClusterRenderer<ClusterPoint> {

	private final IconGenerator iconGenerator;
	private final ImageView imageView;
	private final int markerWidth;
	private final int markerHeight;

	//constructor to handle cluster items
	public MyClusterManagerRenderer(Context context, GoogleMap map, ClusterManager<ClusterPoint> clusterManager) {
		super(context, map, clusterManager);
		clusterManager.setRenderer(this);

		iconGenerator = new IconGenerator(context.getApplicationContext());
		imageView = new ImageView(context.getApplicationContext());
		markerWidth = (int) context.getResources().getDimension(R.dimen.custom_marker_image);
		markerHeight = (int) context.getResources().getDimension(R.dimen.custom_marker_image);
		imageView.setLayoutParams(new ViewGroup.LayoutParams(markerWidth, markerHeight));
		int padding = (int) context.getResources().getDimension(R.dimen.custom_marker_padding);
		imageView.setPadding(padding, padding, padding, padding);
		iconGenerator.setContentView(imageView);
	}

	//makes icons appear on the map instead of default markers
	protected void onBeforeClusterItemRendered(ClusterPoint item, MarkerOptions markerOptions, String itemTitle) {
		Bitmap icon = iconGenerator.makeIcon();
		markerOptions.icon(BitmapDescriptorFactory.fromBitmap(icon)).title(itemTitle);

	}
}
