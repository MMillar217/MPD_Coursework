package com.marcmillar.S1828600.mpd.cw;
//Marc Millar - S1828600
import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.clustering.ClusterItem;

public class ClusterPoint implements ClusterItem {
	private LatLng position;
	private String title;
	private String snippet;
	private int worksIcon;

	//constructor
	public ClusterPoint(LatLng position, String title, String snippet, int worksIcon) {
		this.position = position;
		this.title = title;
		this.snippet = snippet;
		this.worksIcon = worksIcon;
	}

	//getters and setters
	public LatLng getPosition() {
		return position;
	}

	public void setPosition(LatLng position) {
		this.position = position;
	}


	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}


	public String getSnippet() {
		return snippet;
	}

	public void setSnippet(String snippet) {
		this.snippet = snippet;
	}

	public int getWorksIcon() {
		return worksIcon;
	}

	public void setWorksIcon(int worksIcon) {
		this.worksIcon = worksIcon;
	}
}
