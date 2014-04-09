package ge.pol.icmia.vano.carfinder.app;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

public class MapsActivity extends ActionBarActivity implements LocationListener{

	private GoogleMap mMap; // Might be null if Google Play services APK is not available.
	private LocationManager locationManager = null;
	private String provider = null;
	private static Toast toast = null;
	//private Marker currentMarker = null;
	private Marker carMarker = null;
	private SharedPreferences sharedPref = null;
	private SharedPreferences.Editor editor = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		toast = Toast.makeText(getApplicationContext(), "", Toast.LENGTH_LONG);
		sharedPref = getPreferences(Context.MODE_PRIVATE);
		editor = sharedPref.edit();

		setContentView(R.layout.activity_maps);
		setUpMapIfNeeded();

		//getActionBar().setDisplayHomeAsUpEnabled(true);
	}

	@Override
	protected void onResume() {
		super.onResume();
		setUpMapIfNeeded();
		locationManager.requestLocationUpdates(provider, 400, 1, this);
	}

	@Override
	protected void onPause(){
		super.onPause();
		locationManager.removeUpdates(this);
	}

	private void setUpMapIfNeeded() {
		// Do a null check to confirm that we have not already instantiated the map.
		if (mMap == null) {
			// Try to obtain the map from the SupportMapFragment.
			mMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map))
					.getMap();
			// Check if we were successful in obtaining the map.
			if (mMap != null) {
				setUpMap();
			}
		}
	}

	private void setUpMap() {

		checkGPSService();

		boolean isSet = sharedPref.getBoolean(getString(R.string.isSet), false);

		if(isSet) initCarLocation();

		Criteria criteria = new Criteria();
		provider = locationManager.getBestProvider(criteria, false);

		toast.setText("provider: " + provider);
		toast.show();
		mMap.setMyLocationEnabled(true);
//		MarkerOptions markerOptions = new MarkerOptions();
		Location location = locationManager.getLastKnownLocation(provider);
		LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
		mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 18.0f));

//		markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.current));
//		markerOptions.position(new LatLng(location.getLatitude(), location.getLongitude()));
//		markerOptions.title("You");
//		markerOptions.snippet("your current position");
//		currentMarker = mMap.addMarker(markerOptions);
//
//		if(location != null){
//			onLocationChanged(location);
//		}
		saveCurrentPosition();
	}

	private void checkGPSService(){
		locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
		if(!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)){
			Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
			startActivity(intent);
		}
	}

	public void saveCurrentPosition(){
		Location location = locationManager.getLastKnownLocation(provider);

		editor.putFloat(getString(R.string.carLat), (float) location.getLatitude());
		editor.putFloat(getString(R.string.carLng), (float) location.getLongitude());
		editor.putBoolean(getString(R.string.isSet), true);
		editor.commit();

		initCarLocation();
		toast.setText("saved");
		toast.show();
	}

	private void clearSavedPosition(){
		editor.putBoolean(getString(R.string.isSet), false);
		editor.commit();

		carMarker.remove();
		toast.setText("cleared");
		toast.show();
	}

	private void initCarLocation(){

		double carLat = sharedPref.getFloat(getString(R.string.carLat), 0);
		double carLng = sharedPref.getFloat(getString(R.string.carLng), 0);

		MarkerOptions markerOptions = new MarkerOptions();
		markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.parking));
		markerOptions.position(new LatLng(carLat, carLng));
		markerOptions.title("CAR");
		//markerOptions.icon(new BitmapDescrip);
		markerOptions.snippet("location of your car");
		if(carMarker != null) carMarker.remove();
		carMarker = mMap.addMarker(markerOptions);

	}

//	@Override
//	public void onLocationChanged(Location location){
//		double lat = location.getLatitude();
//		double lng = location.getLongitude();
//		String msg = "lat: " + lat + ", lng: " + lng;
//		System.out.println(msg);
//		LatLng latLng = new LatLng(lat, lng);
//		currentMarker.setPosition(latLng);
//		//markerOptions.position(new LatLng(lat, lng));
//		mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 18.0f));
//
//	//	toast.setText(msg);
//	//	toast.show();
//
//	}

	@Override
	public void onStatusChanged(String s, int i, Bundle bundle) {

	}

	@Override
	public void onProviderEnabled(String s) {
		toast.setText("Enabled new provider " + s);
		toast.show();
	}

	@Override
	public void onProviderDisabled(String s) {
		toast.setText("Disabled provider " + s);
		toast.show();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu){
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.main_activity_actions, menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item){
		switch (item.getItemId()){
			case R.id.save_btn:
				saveCurrentPosition();
				return true;
			case R.id.clear_btn:
				clearSavedPosition();
				return true;
			default:
				return super.onOptionsItemSelected(item);
		}
	}

	@Override
	public void onLocationChanged(Location location) {

	}

}