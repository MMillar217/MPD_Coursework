package com.marcmillar.S1828600.mpd.cw;
//Marc Millar - S1828600
import android.util.Log;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.URL;
import java.net.URLConnection;

import java.util.ArrayList;
import java.util.Stack;
import java.util.List;

public class XmlParser implements Runnable {
	public interface IXmlParserFinishedListener {
		void XmlParserFinished(XmlParser parser);
	}

	private String url;
	private Stack<String> xmlElement;

	private IXmlParserFinishedListener finishedListener;

	public List<GeoRSSItem> RssItems;



	public XmlParser(IXmlParserFinishedListener finishedListener, String url) {
		this.finishedListener = finishedListener;
		this.url = url;
		this.xmlElement = new Stack<>();
		this.RssItems = new ArrayList<>();
	}

	private String getStackName() {
		if (xmlElement.empty())
			return "{EMPTY STACK}";
		return xmlElement.peek();
	}

	//parses the data
	@Override
	public void run() {
		// Get url contents
		String xml = getUrlContents(this.url);

		XmlPullParserFactory factory = null;
		XmlPullParser xpp = null;
		GeoRSSItem rssItem = null;

		try {
			Log.e("XmlRoadWorksParser", "Starting to parse content");

			factory = XmlPullParserFactory.newInstance();
			factory.setNamespaceAware(true);
			xpp = factory.newPullParser();

			assert xml != null;
			try {
				xpp.setInput(new StringReader(xml));
			} catch (Exception ex) {
				ex.printStackTrace();
			}
			int eventType = xpp.getEventType();
			//while the document still has more tags to look through
			while (eventType != XmlPullParser.END_DOCUMENT) {
				String xmlTagName = xpp.getName();
				if (xmlTagName == null) xmlTagName = "";
				else xmlTagName = xmlTagName.trim();

				System.out.println("XML Processing tag: " + xmlTagName
						+ ", Stack = " + getStackName()
						+ ", Event Type = " + eventType);

				switch (eventType) {
					//gets the tag's name - if it's 'item', create a new rssItem
					case XmlPullParser.START_TAG:
						System.out.println("XML START_TAG: " + xmlTagName + "");
						xmlElement.push(xpp.getName());

						// If we are starting a RSS item
						if (xpp.getName().trim().equals("item")) {
							rssItem = new GeoRSSItem();
						}

						break;

					//add item
					case XmlPullParser.END_TAG:
						System.out.println("XML END_TAG: " + xmlTagName + "");
						if (!xmlElement.empty())
							xmlElement.pop();

						// Save our XML item
						if (xmlTagName.equals("item")) {
							RssItems.add(rssItem);
							rssItem = null;
						}

						break;
					//if it's text, find the text and then get the text
					case XmlPullParser.TEXT:
						String xmlText = xpp.getText();
						if (xmlText == null) xmlText = "";
						else xmlText = xmlText.trim();

						if (rssItem == null) {
							// Handle this error
							try { eventType = xpp.next(); }
							catch (IOException | XmlPullParserException e) {
								e.printStackTrace();
							}

							continue;
						}

						//<title>A823/M90 J2 southbound onslip</title>
						//<description></description>
						//<link>http://tscot.org/04pFB2020771</link>
						//<georss:point>56.0452494271758 -3.40671493524297</georss:point>
						//<pubDate>Fri, 03 Apr 2020 00:00:00 GMT</pubDate>

						//assign values to each item attribute
						switch (xmlElement.peek()) {
							case "title":
								rssItem.setTitle(xmlText);
								break;

							case "description":
								rssItem.setDescription(xmlText);
								break;

							case "link":

								break;

							// georss:point, is displayed as point because
							// georss is a xml namespace (xmlns) not apart of the element name.
							case "point":
								rssItem.setGeorsPoint(xmlText);
								break;

							case "pubDate":
								rssItem.setPubDate(xmlText);
								break;
						}

						System.out.println("XML TEXT: " + xmlTagName + ", TEXT: " + xpp.getText());
						break;
				}

				try {
					eventType = xpp.next();
				} catch (IOException | XmlPullParserException e) {
					e.printStackTrace();
				}
			}

			System.out.println("End document");

			if (finishedListener != null)
				finishedListener.XmlParserFinished(this);
		} catch (XmlPullParserException e) {
			e.printStackTrace();
		}
	}

	//gets the contents from the given link
	private String getUrlContents(String resourceUrl) {
		// Setup and initialize variables
		URL url;
		URLConnection urlConnection;
		BufferedReader br = null;
		String inputLine = "";
		String content = "";
		StringBuilder sbWebContent = new StringBuilder();

		try {
			Log.e("Web", "Getting string from url: " + resourceUrl);

			// Connect to the url and open a reader
			url = new URL(resourceUrl);
			urlConnection = url.openConnection();
			br = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));

			// Throw away the first 2 header lines before parsing
			while ((inputLine = br.readLine()) != null)
				sbWebContent.append(inputLine);

			// Close the reader
			br.close();

			// Return the web content
			content = sbWebContent.toString();
			return content;
		} catch (IOException ex) {
			// Failed to parse the content or another error, return null and print error.
			Log.e("Web", "IOException while parsing content from "
					+ resourceUrl + ": " + ex.getMessage());
			return null;
		}
	}
}
