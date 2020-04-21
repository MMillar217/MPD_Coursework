package com.marcmillar.S1828600.mpd.cw;
//Marc Millar - S1828600
import java.util.ArrayList;

public class GeoRSSItem {
	//fields
	private String title;
	private String description;
	private String link;
	private String georsPoint;
	private String pubDate;

	//constructors
	public GeoRSSItem() {
		title = "";
		description = "";
		link = "";
		georsPoint = "";
		pubDate = "";
	}

	public GeoRSSItem(String aTitle, String aDescription, String aLink,
	                  String aGeorsPoint, String aPubDate) {
		title = aTitle;
		description = aDescription;
		link = aLink;
		georsPoint = aGeorsPoint;
		pubDate = aPubDate;
	}

	//getters
	public String getTitle() {
		return title;
	}
	public String getDescription() {
		return description;
	}
	public String getLink() {
		return link;
	}
	public String getGeorsPoint() { return georsPoint;	}
	public String getPubDate() {
		return pubDate;
	}

	//setters
	public void setTitle(String aTitle) {
		title = aTitle;
	}
	public void setDescription(String aDescription) {
		description = aDescription;
	}
	public void setLink(String aLink) {
		link = aLink;
	}
	public void setGeorsPoint(String aGeorsPoint) {
		georsPoint = aGeorsPoint;
	}
	public void setPubDate(String aPubDate) {
		pubDate = aPubDate;
	}



	// Methods
	public String toString() {
		String temp;

//		temp = "Title: " + title + System.getProperty("line.separator") +
//				"Description: " + description + System.getProperty("line.separator") +
//				"Link: " + link + System.getProperty("line.separator") +
//				"GeoRSS:point: " + georsPoint + System.getProperty("line.separator") +
//				"Pub Date: " + pubDate;

		temp = georsPoint + " | " + pubDate;

		return temp;
	}

}
