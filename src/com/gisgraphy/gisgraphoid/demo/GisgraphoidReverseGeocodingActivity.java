package com.gisgraphy.gisgraphoid.demo;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.location.Address;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;
import android.widget.ToggleButton;
import android.widget.TwoLineListItem;

import com.gisgraphy.domain.valueobject.CountriesStaticData;
import com.gisgraphy.gisgraphoid.GisgraphyGeocoder;
import com.gisgraphy.gisgraphoid.GisgraphyGeocoderMock;
import com.gisgraphy.gisgraphoid.JTSHelper;
import com.gisgraphy.gisgraphoid.example.R;

/**
 * Sample code to use Gisgraphoid Geocoder
 * 
 * @see {@link GisgraphyGeocoder}
 * 
 * @author <a href="mailto:david.masclet@gisgraphy.com">David Masclet</a>
 * 
 */
public class GisgraphoidReverseGeocodingActivity extends Activity {
	public static final int MAX_RESULTS = 10;

	private static final String LOG_TAG = "gisgraphoid-demo";

	protected static List<String> SORTED_COUNTRY_LIST = CountriesStaticData.sortedCountriesName;
	protected static String[] SORTED_COUNTRY_ARRAY = CountriesStaticData.sortedCountriesName.toArray(new String[CountriesStaticData.sortedCountriesName.size()]);

	protected static final int DATA_CHANGED = 0;
	protected static final int NO_INPUT = 1;
	protected static final int NO_RESULT = 2;
	protected static final int REVERSE_GEOCODING_IN_PROGRESS = 3;
	protected static final int REVERSE_GEOCODING_DONE = 4;
	protected static final int WRONG_LAT = 5;
	protected static final int WRONG_LONG = 6;

	// private MapView googleMap;
	protected Button btnSearch;
	protected EditText latitude;
	protected EditText longitude;
	protected ToggleButton useMyPosition;
	protected ListView mList;
	protected ProgressDialog progressDialog;

	protected Dialog wrongLatDialog;
	protected Dialog noInputDialog;
	protected Dialog wrongLongDialog;
	protected Dialog noResultDialog;

	protected GisgraphyGeocoder gisgraphyGeocoder;

	protected AddressResultAdapter addressAdapter;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.gisgraphoidreversegeocoding);

		// error dialogs
		noInputDialog = new AlertDialog.Builder(GisgraphoidReverseGeocodingActivity.this).setIcon(0).setTitle(getResources().getString(R.string.dialog_default_title)).setPositiveButton(R.string.ok, null).setMessage(getResources().getString(R.string.lat_long_mandatory)).create();
		wrongLatDialog = new AlertDialog.Builder(GisgraphoidReverseGeocodingActivity.this).setIcon(0).setTitle(getResources().getString(R.string.dialog_default_title)).setPositiveButton(R.string.ok, null).setMessage(getResources().getString(R.string.wrong_lat)).create();
		wrongLongDialog = new AlertDialog.Builder(GisgraphoidReverseGeocodingActivity.this).setIcon(0).setTitle(getResources().getString(R.string.dialog_default_title)).setPositiveButton(R.string.ok, null).setMessage(getResources().getString(R.string.wrong_long)).create();
		noResultDialog = new AlertDialog.Builder(GisgraphoidReverseGeocodingActivity.this).setIcon(0).setTitle(getResources().getString(R.string.dialog_default_title)).setPositiveButton(R.string.ok, null).setMessage(getResources().getString(R.string.no_result)).create();

		// input
		latitude = (EditText) findViewById(R.id.reverse_latitude);
		latitude.setMaxLines(1);
		latitude.setEllipsize(TextUtils.TruncateAt.END);
		latitude.requestFocus();

		longitude = (EditText) findViewById(R.id.reverse_longitude);
		longitude.setEllipsize(TextUtils.TruncateAt.END);
		longitude.setMaxLines(1);

		useMyPosition = (ToggleButton) findViewById(R.id.reverse_geocoding_use_my_position);

		LocationManager mlocManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		LocationListener mlocListener = new GisgraphoidLocationListener();
		mlocManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, mlocListener);

		// results display
		mList = (ListView) findViewById(R.id.reverse_results);
		addressAdapter = new AddressResultAdapter();
		mList.setAdapter(addressAdapter);
		mList.setOnItemClickListener(addressAdapter);

		// progress dialog
		progressDialog = new ProgressDialog(this);

		// button
		btnSearch = (Button) findViewById(R.id.reverse_btn);
		btnSearch.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				doReverseGeocoding();
			}
		});
	}

	protected void doReverseGeocoding() {
		progressDialog.setMessage(getResources().getString(R.string.reverse_geocoding_in_progress));

		new Thread(new Runnable() {
			public void run() {
				String latitude_input = latitude.getText().toString();
				String longitude_input = longitude.getText().toString();
				try {
					double longitudeAsDouble;
					double latitudeAsDouble;
					// check lat/long are not null
					if (latitude_input == null || latitude_input.trim().equals("") || longitude_input == null || longitude_input.trim().equals("")) {
						handler.sendEmptyMessage(NO_INPUT);
						return;
					}
					// check that latitude is a number and in the correct range
					try {
						latitudeAsDouble = new Double(latitude_input);
						JTSHelper.checkLatitude(latitudeAsDouble);
					} catch (Exception e) {
						handler.sendEmptyMessage(WRONG_LAT);
						return;
					}
					// check that longitude is a number and in the correct range
					try {
						longitudeAsDouble = new Double(longitude_input);
						JTSHelper.checkLongitude(longitudeAsDouble);
					} catch (Exception e) {
						handler.sendEmptyMessage(WRONG_LONG);
						return;
					}

					Locale locale = Locale.getDefault();
					gisgraphyGeocoder = createGeocoder(locale);
					// gisgraphyGeocoder.setBaseUrl("http://192.168.0.12:8080/geocoding/geocode");

					// Reverse Geocode !
					handler.sendEmptyMessage(REVERSE_GEOCODING_IN_PROGRESS);
					List<Address> foundAdresses = gisgraphyGeocoder.getFromLocation(latitudeAsDouble, longitudeAsDouble, MAX_RESULTS); // Search
					handler.sendEmptyMessage(REVERSE_GEOCODING_DONE);
					Log.i(LOG_TAG, foundAdresses.size() + " result found for lat=" + latitude_input + " and long=" + longitude_input);

					if (foundAdresses.size() == 0) {
						// if no address found, display a dialog box
						Log.i(LOG_TAG, "no result found for lat=" + latitude_input + " and long=" + longitude_input);
						handler.sendEmptyMessage(NO_RESULT);

					} else {
						// else display results
						Log.i(LOG_TAG, foundAdresses.size() + " result found for lat=" + latitude_input + " and long=" + longitude_input);
						addressAdapter.setaddress(foundAdresses);
						handler.sendEmptyMessage(DATA_CHANGED);
					}

				} catch (Exception e) {
					Log.e(LOG_TAG, "Error during geocoding of  lat=" + latitude_input + " and long=" + longitude_input + " : " + e.getMessage(), e);
					Dialog locationError = new AlertDialog.Builder(GisgraphoidReverseGeocodingActivity.this).setIcon(0).setTitle(getResources().getString(R.string.dialog_default_title)).setPositiveButton(R.string.ok, null).setMessage(getResources().getString(R.string.no_result)).create();
					locationError.show();
				}
			}
		}).start();
	}

	/**
	 * Handler for geocoding events
	 */
	private final Handler handler = new Handler() {
		public void handleMessage(Message message) {

			switch (message.what) {
			case DATA_CHANGED:
				addressAdapter.notifyDataSetChanged();
				break;
			case NO_INPUT:
				noInputDialog.show();
				break;
			case NO_RESULT:
				noResultDialog.show();
				break;
			case REVERSE_GEOCODING_IN_PROGRESS:
				progressDialog.show();
				break;
			case REVERSE_GEOCODING_DONE:
				if (progressDialog.isShowing()) {
					progressDialog.dismiss();
					break;
				}
			case WRONG_LAT:
				wrongLatDialog.show();
				break;
			case WRONG_LONG:
				wrongLongDialog.show();
				break;
			default:
				break;
			}
		}
	};

	/**
	 * @param locale
	 *            the locale for the geocoder
	 * @return a new geocoder instance
	 */
	protected GisgraphyGeocoder createGeocoder(Locale locale) {
		// return new GisgraphyGeocoderMock(this,locale);
		return new GisgraphyGeocoder(this, locale);
	}

	/**
	 * start a new Activity to display an address on a map, actually implements Openstreetmap with osmdroid
	 * 
	 * @param address
	 *            the address to display on a map
	 */
	protected void viewOnMap(Address address) {
		Intent next = new Intent();
		next.setClass(this, GisgraphoidMapActivity.class);
		next.putExtra(ExtraInfos.FEATURE_NAME, address.getFeatureName());
		next.putExtra(ExtraInfos.LATITUDE, address.getLatitude());
		next.putExtra(ExtraInfos.LONGITUDE, address.getLongitude());
		next.putExtra(ExtraInfos.ADDRESS,address);
		startActivity(next);

	}

	/**
	 * Adapter to display Address results
	 * 
	 * @author <a href="mailto:david.masclet@gisgraphy.com">David Masclet</a>
	 * 
	 */
	class AddressResultAdapter extends BaseAdapter implements AdapterView.OnItemClickListener {

		private List<Address> addresses = new ArrayList<Address>();;
		private LayoutInflater mInflater;

		public AddressResultAdapter(List<Address> addresses) {
			super();
			setaddress(addresses);
			mInflater = (LayoutInflater) GisgraphoidReverseGeocodingActivity.this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		}

		public AddressResultAdapter() {
			super();
			mInflater = (LayoutInflater) GisgraphoidReverseGeocodingActivity.this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		}

		public void setaddress(List<Address> addresses) {
			if (addresses != null) {
				this.addresses = addresses;
			}
		}

		public int getCount() {
			return addresses.size();
		}

		public Object getItem(int position) {
			return position;
		}

		public long getItemId(int position) {
			return position;
		}

		public View getView(int position, View convertView, ViewGroup parent) {
			TwoLineListItem view = (convertView != null) ? (TwoLineListItem) convertView : createView(parent);
			bindView(view, addresses.get(position));
			return view;
		}

		private TwoLineListItem createView(ViewGroup parent) {
			TwoLineListItem item = (TwoLineListItem) mInflater.inflate(android.R.layout.simple_list_item_2, parent, false);
			item.getText2().setSingleLine();
			item.getText2().setEllipsize(TextUtils.TruncateAt.END);
			item.getText1().setSingleLine();
			item.getText1().setEllipsize(TextUtils.TruncateAt.END);
			return item;
		}

		private void bindView(TwoLineListItem view, Address address) {
			view.getText1().setText(address.getFeatureName());
			view.getText2().setText(getResources().getString(R.string.latitude) + "=" + address.getLatitude() + ";" + getResources().getString(R.string.longitude) + "=" + address.getLongitude());
		}

		public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
			viewOnMap(addresses.get(position));
			finish();
		}

	}

	public class GisgraphoidLocationListener implements LocationListener {
		private static final int MAX_COORDINATE_SIZE = 9;

		public void onLocationChanged(Location loc) {
			if (useMyPosition.isChecked()) {
				String currentLatitude = loc.getLatitude()+"";
				int maxCurrentLatSize=currentLatitude.length()>MAX_COORDINATE_SIZE?MAX_COORDINATE_SIZE:currentLatitude.length();
				latitude.setText((currentLatitude + "").substring(0,maxCurrentLatSize));
				String currentLongitude = loc.getLongitude()+"";
				int maxCurrentLongSize=currentLongitude.length()>MAX_COORDINATE_SIZE?MAX_COORDINATE_SIZE:currentLatitude.length();
				longitude.setText(currentLongitude.substring(0,maxCurrentLongSize));
			}

		}

		public void onProviderDisabled(String provider) {
			if (useMyPosition.isChecked()) {
				Toast.makeText(getApplicationContext(), "Gps is Disabled", Toast.LENGTH_SHORT).show();
			}
		}

		public void onProviderEnabled(String provider) {
			if (useMyPosition.isChecked()) {
				Toast.makeText(getApplicationContext(), "Gps is Enabled", Toast.LENGTH_SHORT).show();
			}
		}

		public void onStatusChanged(String provider, int status, Bundle extras) {
		}
	}

}