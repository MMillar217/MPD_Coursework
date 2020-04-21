package com.marcmillar.S1828600.mpd.cw;
//Marc Millar - S1828600
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.animation.ObjectAnimator;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.database.Cursor;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.maps.android.clustering.ClusterItem;
import com.google.maps.android.clustering.ClusterManager;

import java.util.ArrayList;
import java.util.List;

import static com.marcmillar.S1828600.mpd.cw.Constants.ERROR_DIALOG_REQUEST;
import static com.marcmillar.S1828600.mpd.cw.Constants.MAPVIEW_BUNDLE_KEY;
import static com.marcmillar.S1828600.mpd.cw.Constants.PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION;
import static com.marcmillar.S1828600.mpd.cw.Constants.PERMISSIONS_REQUEST_ENABLE_GPS;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, OnMapReadyCallback, XmlParser.IXmlParserFinishedListener {

	private static MainActivity _instance = null;

	// Xml elements
	private View mainView;
	private TextView lblTitle;
	private MapView mMapView;
	private RecyclerView mWorksItemList;
	private RelativeLayout mMapContainer;
	private GoogleMap mGoogleMap;

	private LatLngBounds mMapBoundary;

	// Declare a variable for the cluster manager.
	private ClusterManager clusterManager;
	private MyClusterManagerRenderer myClusterManagerRenderer;
	private ArrayList<ClusterPoint> mClusterPoints = new ArrayList<>();
	private ArrayList<String> pointsList = new ArrayList<>();
	private ArrayList<String> titlesList = new ArrayList<>();
	private ArrayList<String> snippetsList = new ArrayList<>();

	private boolean mLocationPermissionGranted = false;

	//mapexpansion onclick
	private static final int MAP_LAYOUT_STATE_CONTRACTED = 0;
	private static final int MAP_LAYOUT_STATE_EXPANDED = 1;
	private int mMapLayoutState = 0;
	public ImageButton expandMap;

	private static final String TAG = "MainActivity";



	public MainActivity() {
		this._instance = this;
	}

	public static MainActivity getInstance() {
		return _instance;
	}


	//called when the main activity loads
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		//test recycler
		Log.d(TAG, "onCreate: started");
		initItems();

		//assigning variables from layout
		mainView = (View) findViewById(R.id.mainView);
		mainView.setBackgroundColor(getResources().getColor(R.color.white, null));
		mWorksItemList = (RecyclerView) findViewById(R.id.recycler_view);
		mMapView = (MapView) findViewById(R.id.points_map);
		expandMap = (ImageButton) findViewById(R.id.btn_full_screen_map);
		expandMap.setOnClickListener(this);
		mMapContainer = (RelativeLayout) findViewById(R.id.map_container);
		lblTitle = (TextView) findViewById(R.id.lblTitle);

		//if the phone is landscape - split recyclerview into 2 columns
		int orientation = this.getResources().getConfiguration().orientation;
		if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
			GridLayoutManager gridLayoutManager = new GridLayoutManager(this, 2);
			mWorksItemList.setLayoutManager(gridLayoutManager);

		}


		//bottom navigation bar - picks up on which feed the user wants to see
		BottomNavigationView bottomNavigationView = (BottomNavigationView) findViewById(R.id.bottomNav);
		//possibly need helper

		bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
			@Override
			public boolean onNavigationItemSelected(@NonNull MenuItem item) {
				switch (item.getItemId()) {
					case R.id.plannedRW:
						lblTitle.setText("Planned Roadworks");
						BottomNavigationView bottomNavigationView1 = (BottomNavigationView) findViewById(R.id.bottomNav);
						rssRvAdapter.setRssItems(new ArrayList<GeoRSSItem>());
						String urlSource = "https://trafficscotland.org/rss/feeds/plannedroadworks.aspx";
						startProgress(urlSource);
						break;
					case R.id.roadworks:
						lblTitle.setText("Roadworks");
						rssRvAdapter.setRssItems(new ArrayList<GeoRSSItem>());
						String urlSourceRW = "https://trafficscotland.org/rss/feeds/roadworks.aspx";
						startProgress(urlSourceRW);
						break;

					case R.id.currentInc:
						lblTitle.setText("Current Incidents");
						rssRvAdapter.setRssItems(new ArrayList<GeoRSSItem>());
						String urlSourceCur = "https://trafficscotland.org/rss/feeds/currentincidents.aspx";
						startProgress(urlSourceCur);
						break;
				}
				return false;
			}
		});

		//default - load the planned feed
		String urlSource = "https://trafficscotland.org/rss/feeds/plannedroadworks.aspx";
		lblTitle.setText("");
		startProgress(urlSource);

		initGoogleMap(savedInstanceState);

	}

	//saves the markers the user wants when the orientation changes
	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);

		Bundle mapViewBundle = outState.getBundle(MAPVIEW_BUNDLE_KEY);
		if (mapViewBundle == null) {
			mapViewBundle = new Bundle();
			outState.putBundle(MAPVIEW_BUNDLE_KEY, mapViewBundle);
		}

		mMapView.onSaveInstanceState(mapViewBundle);

		outState.putStringArrayList("points", pointsList);
		outState.putStringArrayList("titles", titlesList);
		outState.putStringArrayList("snippets", snippetsList);
	}


	@Override
	public void onClick(View v) {

		switch (v.getId()){
			//expand map to full screen
			case R.id.btn_full_screen_map:{
				if(mMapLayoutState == MAP_LAYOUT_STATE_CONTRACTED){
					mMapLayoutState = MAP_LAYOUT_STATE_EXPANDED;
					expandMapAnimation();
				}
				else if(mMapLayoutState == MAP_LAYOUT_STATE_EXPANDED){
					mMapLayoutState = MAP_LAYOUT_STATE_CONTRACTED;
					contractMapAnimation();
				}
			}
			break;
		}

	}

	//map services
	private boolean checkMapServices() {
		if (isServicesOK()) {
			if (isMapsEnabled()) {
				return true;
			}
		}
		return false;
	}

	//getting location permissions
	private void buildAlertMessageNoGps() {
		final AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage("This application requires GPS to work properly, do you want to enable it?")
				.setCancelable(false)
				.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
					public void onClick(@SuppressWarnings("unused") final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
						Intent enableGpsIntent = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
						startActivityForResult(enableGpsIntent, PERMISSIONS_REQUEST_ENABLE_GPS);
					}
				});
		final AlertDialog alert = builder.create();
		alert.show();
	}

	//check if maps are enabled
	public boolean isMapsEnabled() {
		final LocationManager manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

		if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
			buildAlertMessageNoGps();
			return false;
		}
		return true;
	}

	//check location permissions
	private void getLocationPermission() {
		/*
		 * Request location permission, so that we can get the location of the
		 * device. The result of the permission request is handled by a callback,
		 * onRequestPermissionsResult.
		 */
		if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
				android.Manifest.permission.ACCESS_FINE_LOCATION)
				== PackageManager.PERMISSION_GRANTED) {
			mLocationPermissionGranted = true;

			lblTitle.setText("Planned Roadworks");
			rssRvAdapter.setRssItems(new ArrayList<GeoRSSItem>());
			String urlSource = "https://trafficscotland.org/rss/feeds/plannedroadworks.aspx";
			startProgress(urlSource);
		} else {
			ActivityCompat.requestPermissions(this,
					new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
					PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
		}
	}

	//checking that the google services are available
	public boolean isServicesOK() {
		Log.d(TAG, "isServicesOK: checking google services version");

		int available = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(MainActivity.this);

		if (available == ConnectionResult.SUCCESS) {
			//everything is fine and the user can make map requests
			Log.d(TAG, "isServicesOK: Google Play Services is working");
			return true;
		} else if (GoogleApiAvailability.getInstance().isUserResolvableError(available)) {
			//an error occured but we can resolve it
			Log.d(TAG, "isServicesOK: an error occured but we can fix it");
			Dialog dialog = GoogleApiAvailability.getInstance().getErrorDialog(MainActivity.this, available, ERROR_DIALOG_REQUEST);
			dialog.show();
		} else {
			Toast.makeText(this, "You can't make map requests", Toast.LENGTH_SHORT).show();
		}
		return false;
	}

	//request to use user's location
	@Override
	public void onRequestPermissionsResult(int requestCode,
	                                       @NonNull String permissions[],
	                                       @NonNull int[] grantResults) {
		mLocationPermissionGranted = false;
		switch (requestCode) {
			case PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION: {
				// If request is cancelled, the result arrays are empty.
				if (grantResults.length > 0
						&& grantResults[0] == PackageManager.PERMISSION_GRANTED) {
					mLocationPermissionGranted = true;
				}
			}
		}
	}

	//if the location permission is granted, load the data - if not, ask the user to enable location services
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		Log.d(TAG, "onActivityResult: called.");
		switch (requestCode) {
			case PERMISSIONS_REQUEST_ENABLE_GPS: {
				if (mLocationPermissionGranted) {
					lblTitle.setText("Planned Roadworks");
					rssRvAdapter.setRssItems(new ArrayList<GeoRSSItem>());
					String urlSource = "https://trafficscotland.org/rss/feeds/plannedroadworks.aspx";
					startProgress(urlSource);
				} else {
					getLocationPermission();
				}
			}
		}

	}

	//runs the parser for the relevant/selected feed
	public void startProgress(String urlSource) {
		// Run network access on a separate thread;
		Thread t = new Thread(new XmlParser(this, urlSource));
		t.start();
	}

	//test recyclerview - fake values
	private void initItems() {
		Log.d(TAG, "initItems: preparing items");
		initRecyclerView();
	}

	private RssItemRecyclerViewAdapter rssRvAdapter;

	private void initRecyclerView() {
		Log.d(TAG, "initRecyclerView: init recyclerview");
		RecyclerView recyclerView = findViewById(R.id.recycler_view);
		rssRvAdapter = new RssItemRecyclerViewAdapter(new ArrayList<GeoRSSItem>(), this);
		recyclerView.setAdapter(rssRvAdapter);
		recyclerView.setLayoutManager(new LinearLayoutManager(this));
	}

	//when parsing is complete
	@Override
	public void XmlParserFinished(final XmlParser parser) {
		class RssToUiThreadTask implements Runnable {
			MainActivity m;
			List<GeoRSSItem> rssItems;

			//pass items to ui
			RssToUiThreadTask(MainActivity m, List<GeoRSSItem> rssItems) {
				this.m = m;
				this.rssItems = rssItems;

				Log.d(TAG, "RssToUiThreadTask: items " + this.rssItems);
			}

			public void run() {
				m.rssRvAdapter.setRssItems(rssItems);
			}
		}

		MainActivity.this.runOnUiThread(new RssToUiThreadTask(this, parser.RssItems));
	}

	//initialises the map
	private void initGoogleMap(Bundle savedInstanceState) {
		Bundle mapViewBundle = null;
		if (savedInstanceState != null) {
			mapViewBundle = savedInstanceState.getBundle(MAPVIEW_BUNDLE_KEY);
		}
		mMapView = (MapView) findViewById(R.id.points_map);
		mMapView.onCreate(mapViewBundle);
		mMapView.getMapAsync(this);
	}

	//runs after the location services have been run
	@Override
	public void onResume() {
		super.onResume();
		mMapView.onResume();
		if (checkMapServices()) {
			if (mLocationPermissionGranted) {
				lblTitle.setText("Planned Roadworks");
				rssRvAdapter.setRssItems(new ArrayList<GeoRSSItem>());
				String urlSource = "https://trafficscotland.org/rss/feeds/plannedroadworks.aspx";
				startProgress(urlSource);
			} else {
				getLocationPermission();
			}
		}
	}

	//
	@Override
	public void onStart() {
		super.onStart();
		mMapView.onStart();
	}

	@Override
	public void onStop() {
		super.onStop();
		mMapView.onStop();
	}

	//when the map is ready
	@Override
	public void onMapReady(GoogleMap map) {
		map.addMarker(new MarkerOptions().position(new LatLng(57.3068191633232, -2.04433330924239)).title("A90 Foveran").snippet("Mon, 11 May 2020 00:00:00 GMT"));
		map.setMyLocationEnabled(true);
		this.mGoogleMap = map;

		clusterManager = new ClusterManager<ClusterPoint>(this, mGoogleMap);
		myClusterManagerRenderer = new MyClusterManagerRenderer(this, mGoogleMap, clusterManager);

		//moves camera view to Scotland
		double lat2 = 55.857693;
		double lng2 = -4.234898;
		float zoom = 6;
		LatLng latLng = new LatLng(lat2, lng2);
		map.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoom));

	}

	//on-click: when a user clicks an item, this method adds that item to the map
	public void addMarker(LatLng latLng, String title, String snippet) {
		if (this.mGoogleMap == null)
			return;

		ClusterPoint newClusterPoint = new ClusterPoint(latLng, title, snippet, 0);
		clusterManager.addItem(newClusterPoint);
		mClusterPoints.add(newClusterPoint);
		String strLatLng = latLng.toString();
		Log.d(TAG, "addMarker: cluster points: " + mClusterPoints);
		clusterManager.cluster();

		double lat2 = 55.857693;
		double lng2 = -4.234898;
		LatLng latLng2 = new LatLng(lat2, lng2);
		float zoom = 6;
		mGoogleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoom));
	}

	@Override
	public void onPause() {
		mMapView.onPause();
		super.onPause();
	}

	@Override
	public void onDestroy() {
		mMapView.onDestroy();
		super.onDestroy();
	}

	@Override
	public void onLowMemory() {
		super.onLowMemory();
		mMapView.onLowMemory();
	}

	//when the user expands the map to full screen
	private void expandMapAnimation(){
		ViewWeightAnimationWrapper mapAnimationWrapper = new ViewWeightAnimationWrapper(mMapContainer);
		ObjectAnimator mapAnimation = ObjectAnimator.ofFloat(mapAnimationWrapper,
				"weight",
				0,
				100);
		mapAnimation.setDuration(1500);

		ViewWeightAnimationWrapper recyclerAnimationWrapper = new ViewWeightAnimationWrapper(mWorksItemList);
		ObjectAnimator recyclerAnimation = ObjectAnimator.ofFloat(recyclerAnimationWrapper,
				"weight",
				100,
				0);
		recyclerAnimation.setDuration(1500);

		recyclerAnimation.start();
		mapAnimation.start();
	}

	//hides map
	private void contractMapAnimation(){
		ViewWeightAnimationWrapper mapAnimationWrapper = new ViewWeightAnimationWrapper(mMapContainer);
		ObjectAnimator mapAnimation = ObjectAnimator.ofFloat(mapAnimationWrapper,
				"weight",
				100,
				0);
		mapAnimation.setDuration(1500);

		ViewWeightAnimationWrapper recyclerAnimationWrapper = new ViewWeightAnimationWrapper(mWorksItemList);
		ObjectAnimator recyclerAnimation = ObjectAnimator.ofFloat(recyclerAnimationWrapper,
				"weight",
				0,
				100);
		recyclerAnimation.setDuration(1500);

		recyclerAnimation.start();
		mapAnimation.start();
	}
}
