package com.gisgraphy.gisgraphoid.demo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.location.Address;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TwoLineListItem;

import com.gisgraphy.domain.valueobject.CountriesStaticData;
import com.gisgraphy.gisgraphoid.GisgraphyGeocoder;
import com.gisgraphy.gisgraphoid.example.R;

/**
 * Sample code to use Gisgraphoid Geocoder
 * @see {@link GisgraphyGeocoder}
 * 
 * @author <a href="mailto:david.masclet@gisgraphy.com">David Masclet</a>
 * 
 */
public class GisgraphoidGeocodingActivity extends Activity {
	public static final int MAX_RESULTS = 10;

	private static final String LOG_TAG = "gisgraphoid-demo";

	protected static List<String> SORTED_COUNTRY_LIST = CountriesStaticData.sortedCountriesName;
	protected static String[] SORTED_COUNTRY_ARRAY = CountriesStaticData.sortedCountriesName.toArray(new String[CountriesStaticData.sortedCountriesName.size()]);

	protected static final int DATA_CHANGED = 0;
	protected static final int NO_INPUT = 1;
	protected static final int NO_RESULT = 2;
	protected static final int GEOCODING_IN_PROGRESS = 3;
	protected static final int GEOCODING_DONE = 4;

	// private MapView googleMap;
	protected Button btnSearch;
	protected EditText addressInput;
	protected ListView mList;
	protected Spinner spinner;
	protected ProgressDialog progressDialog;

	protected Dialog emptyAddressInputDialog;
	protected Dialog noResultDialog;

	protected GisgraphyGeocoder gisgraphyGeocoder;

	protected AddressResultAdapter addressAdapter;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.gisgraphoidgeocoding);

		// error dialogs
		emptyAddressInputDialog = new AlertDialog.Builder(GisgraphoidGeocodingActivity.this).setIcon(0).setTitle(getResources().getString(R.string.dialog_default_title)).setPositiveButton(R.string.ok, null).setMessage(getResources().getString(R.string.empty_input)).create();
		noResultDialog = new AlertDialog.Builder(GisgraphoidGeocodingActivity.this).setIcon(0).setTitle(getResources().getString(R.string.dialog_default_title)).setPositiveButton(R.string.ok, null).setMessage(getResources().getString(R.string.no_result)).create();

		// input
		addressInput = (EditText) findViewById(R.id.gisgraphoid_geocoding_address_input);
		addressInput.setMaxLines(1);
		addressInput.setEllipsize(TextUtils.TruncateAt.END);
		addressInput.requestFocus();

		addressInput.setOnKeyListener(new OnKeyListener() {
			public boolean onKey(View v, int keyCode, KeyEvent event) {
				if ((event.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
					doGeocoding();
					return true;
				}
				return false;

			}
		});

		// results display
		mList = (ListView) findViewById(R.id.gisgraphoid_geocoding_results);
		addressAdapter = new AddressResultAdapter();
		mList.setAdapter(addressAdapter);
		mList.setOnItemClickListener(addressAdapter);

		// progress dialog
		progressDialog = new ProgressDialog(this);

		// choose locale
		spinner = (Spinner) findViewById(R.id.gisgraphoid_spinnerlocale);
		Collections.sort(SORTED_COUNTRY_LIST);
		ArrayAdapter<String> countryCodeAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, SORTED_COUNTRY_ARRAY);
		// countryCodeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spinner.setAdapter(countryCodeAdapter);
		String defaultcountryCode = Locale.getDefault().getCountry();
		Log.i(LOG_TAG, "default country code=" + defaultcountryCode);
		int position = CountriesStaticData.getPositionFromCountryCode(defaultcountryCode);
		Log.i(LOG_TAG, "set spinner position to " + position + ". Country=" + CountriesStaticData.sortedCountriesName.get(position));
		spinner.setSelection(position);

		// button
		btnSearch = (Button) findViewById(R.id.simpleGM_btn_search);
		btnSearch.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				doGeocoding();
			}
		});
	}

	protected void doGeocoding() {
		progressDialog.setMessage(getResources().getString(R.string.geocoding_in_progress));

		new Thread(new Runnable() {
			public void run() {
				try {
					String addressToGeocode = addressInput.getText().toString();
					// check empty address
					if (addressToGeocode == null || addressToGeocode.trim().equals("")) {
						handler.sendEmptyMessage(NO_INPUT);
						return;
					}
					// get Locale and country
					int spinnerItemPosition = spinner.getSelectedItemPosition();
					Log.i(LOG_TAG, "spinner item position = " + spinnerItemPosition);
					String countryCode = CountriesStaticData.getCountryCodeFromPosition(spinnerItemPosition);
					Log.i(LOG_TAG, "countrycode selected=" + countryCode);
					Locale locale = new Locale("EN", countryCode);

					gisgraphyGeocoder = createGeocoder(locale);
					// gisgraphyGeocoder.setBaseUrl("http://192.168.0.12:8080/geocoding/geocode");

					// Geocode !
					handler.sendEmptyMessage(GEOCODING_IN_PROGRESS);
					List<Address> foundAdresses = gisgraphyGeocoder.getFromLocationName(addressToGeocode, MAX_RESULTS); // Search
					handler.sendEmptyMessage(GEOCODING_DONE);

					if (foundAdresses.size() == 0) {
						// if no address found, display a dialog box
						Log.i(LOG_TAG, "no result found for " + addressInput);
						handler.sendEmptyMessage(NO_RESULT);

					} else {
						// else display results
						Log.i(LOG_TAG, foundAdresses.size() + " result(s) found for " + addressInput);
						addressAdapter.setaddress(foundAdresses);
						handler.sendEmptyMessage(DATA_CHANGED);
					}

				} catch (Exception e) {
					Log.e(LOG_TAG, "Error during geocoding of " + addressInput + " : " + e.getMessage(), e);
					Dialog locationError = new AlertDialog.Builder(GisgraphoidGeocodingActivity.this).setIcon(0).setTitle(getResources().getString(R.string.dialog_default_title)).setPositiveButton(R.string.ok, null).setMessage(getResources().getString(R.string.no_result)).create();
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
				emptyAddressInputDialog.show();
				break;
			case NO_RESULT:
				noResultDialog.show();
				break;
			case GEOCODING_IN_PROGRESS:
				progressDialog.show();
				break;
			case GEOCODING_DONE:
				if (progressDialog.isShowing()) {
					progressDialog.dismiss();
					break;
				}
			default:
				break;
			}
		}
	};

	/**
	 * @param locale the locale for the geocoder
	 * @return a new geocoder instance
	 */
	protected GisgraphyGeocoder createGeocoder(Locale locale) {
		// return new GisgraphyGeocoderMock(this,locale);
		return new GisgraphyGeocoder(this, locale);
	}

	/**
	 * start a new Activity to display an address on a map
	 * @param address the address to display on a map
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
	 * 	Adapter to display Address results
	 *  @author <a href="mailto:david.masclet@gisgraphy.com">David Masclet</a>
	 *
	 */
	class AddressResultAdapter extends BaseAdapter implements AdapterView.OnItemClickListener {

		private List<Address> addresses = new ArrayList<Address>();;
		private LayoutInflater mInflater;

		public AddressResultAdapter(List<Address> addresses) {
			super();
			setaddress(addresses);
			mInflater = (LayoutInflater) GisgraphoidGeocodingActivity.this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		}

		public AddressResultAdapter() {
			super();
			mInflater = (LayoutInflater) GisgraphoidGeocodingActivity.this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
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

}