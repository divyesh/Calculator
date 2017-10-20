package com.mindblots.calculator;

import java.io.IOException;
import java.io.InputStream;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Locale;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.ActivityInfo;
import android.content.res.AssetManager;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.inmobi.androidsdk.IMAdInterstitial;
import com.inmobi.androidsdk.IMAdRequest;
import com.inmobi.androidsdk.IMAdView;

import de.congrace.exp4j.Calculable;
import de.congrace.exp4j.ExpressionBuilder;

public class CalculatorActivity extends Activity implements
		OnItemSelectedListener, OnClickListener, OnLongClickListener {
	private static final String TAG = "Calculator";
	private TextView display;
	private TextView taxTextView;
	private TextView taxDisplay;
	private TextView totalDisplay;
	private TextView totalTextView;
	private TextView taxInfo;
	private String operator;
	private ArrayList<Button> buttons;
	private RelativeLayout flagImageView;
	private String currentDisplay;
	private double previousValue;
	private double currentValue;
	private boolean isOperatorPressed;
	private boolean isdigitPressed;
	public static final String STORED_PROVINCE = "storedProvinces";
	private String currentProvince;
	private DecimalFormat decimalFormat;
	private SharedPreferences settings;
	private IMAdView mIMAdView;
	private IMAdInterstitial mIMAdInterstitial;
	private IMAdRequest mAdRequest;
	private TaxDbHelper dbHelper;
	private ArrayList<TaxRate> rates = null;
	private ProvinceAdapter proAdapter = null;
	private TaxRate defaultTaxRate = null;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

		AddToDb();

		initiControls();
		// initAds();
	}

	private void AddToDb() {
		SharedPreferences sp = getSharedPreferences(STORED_PROVINCE,
				Context.MODE_PRIVATE);
		boolean hasVisited = sp.getBoolean("hasVisited", false);
		int databaseversion = sp.getInt("databaseversion", 1);
		// Check database version from webservice, if different then update
		// database
		dbHelper = new TaxDbHelper(getBaseContext(), databaseversion);
		if (!hasVisited) {
			dbHelper.InsertDefaults();
			Editor e = sp.edit();
			e.putBoolean("hasVisited", true);
			e.commit();
		}
		rates = dbHelper.getAllProvinces();
	}

	private void initAds() {
		mIMAdView = (IMAdView) findViewById(R.id.imAdview);
		DisplayMetrics displaymetrics = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
		final float scale = displaymetrics.density;
		int height = (int) (50 * scale + 0.5f);
		int wwidth = (int) (320 * scale + 0.5f);
		mIMAdView
				.setLayoutParams(new LinearLayout.LayoutParams(wwidth, height));
		mAdRequest = new IMAdRequest();
		mAdRequest.setTestMode(false);
		mIMAdView.setIMAdRequest(mAdRequest);

		mIMAdView.setRefreshInterval(IMAdView.REFRESH_INTERVAL_DEFAULT);
		// TEST AD ID 4028cba631d63df10131e1d3818b00cc
		mIMAdInterstitial = new IMAdInterstitial(CalculatorActivity.this,
				"4028cbff38989ad10138c6abb8720817");

		// ORIGINAL AD ID 4028cbff38989ad10138c6abb8720817
		// mIMAdInterstitial = new IMAdInterstitial(CalculatorActivity.this,
		// "4028cbff38989ad10138c6abb8720817");
	}

	public void onRefreshAd() {

		mIMAdView.loadNewAd();
	}

	private static final int[] BUTTON_IDS = { R.id.zero, R.id.one, R.id.two,
			R.id.three, R.id.four, R.id.five, R.id.six, R.id.seven, R.id.eight,
			R.id.nine, R.id.point, R.id.equalsto, R.id.plus, R.id.minus,
			R.id.multiply, R.id.divide, R.id.taxPlus, R.id.taxMinus, };

	// private static final int[] BUTTON_IDS = { R.id.btn0, R.id.btn1,
	// R.id.btn2,
	// R.id.btn3, R.id.btn4, R.id.btn5, R.id.btn6, R.id.btn7, R.id.btn8,
	// R.id.btn9, R.id.btnPoint, R.id.btnEqualsTo, R.id.btnPlus,
	// R.id.btnMinus, R.id.btnMultiply, R.id.btnDivide, R.id.btnTaxPlus,
	// R.id.btnTaxMinus, };

	private void initiControls() {
		previousValue = 0;
		currentValue = 0;
		currentDisplay = "";
		operator = "";
		isOperatorPressed = false;
		isdigitPressed = false;
		decimalFormat = new DecimalFormat("#.##");
		display = (TextView) findViewById(R.id.display);
		display.setOnClickListener(this);
		display.setOnLongClickListener(this);
		taxDisplay = (TextView) findViewById(R.id.taxDisplay);
		taxTextView = (TextView) findViewById(R.id.taxTextView);
		totalTextView = (TextView) findViewById(R.id.totalTextView);
		totalDisplay = (TextView) findViewById(R.id.totalDisplay);
		taxInfo = (TextView) findViewById(R.id.taxInfo);
		taxInfo.setText("This is new Tax Info");
		buttons = new ArrayList<Button>();
		for (int id : BUTTON_IDS) {
			Button btn = (Button) (Button) findViewById(id);
			btn.setOnClickListener(this);
			buttons.add(btn);
		}

		flagImageView = (RelativeLayout) findViewById(R.id.linearLayoutParent);
		//
		// // Create an ArrayAdapter using the string array and a default
		// spinner
		// // layout
		settings = getSharedPreferences(STORED_PROVINCE, 0);
		currentProvince = settings.getString(STORED_PROVINCE, "Ontario").trim();

		proAdapter = new ProvinceAdapter(this, rates);
		Spinner spinner = (Spinner) findViewById(R.id.provinces_spinner);

		spinner.setAdapter(proAdapter);

		spinner.setOnItemSelectedListener(this);
		spinner.setSelection(proAdapter.getItemPosition(currentProvince));
	}

	@Override
	public void onItemSelected(AdapterView<?> parent, View view, int pos,
			long id) {
		resetCurrentDisplay();
		defaultTaxRate = rates.get(pos);

		SharedPreferences.Editor editor = settings.edit();
		editor.putString(STORED_PROVINCE, defaultTaxRate.Province);
		editor.commit();

		taxTextView.setText("Tax :");
		taxInfo.setText(defaultTaxRate.TaxRates);
		// use AssetManager to load next image from assets folder
		AssetManager assets = getAssets();

		InputStream stream; // used to read in flag images

		try {
			// get an InputStream to the asset representing the next flag
			String locateFlag = defaultTaxRate.Province.toLowerCase(Locale.US)
					.replace(" ", "");

			stream = assets.open("provinces/" + locateFlag + ".png");
			Drawable flag = Drawable.createFromStream(stream, locateFlag);
			flagImageView.setBackgroundDrawable(flag);

		} catch (IOException e) {
			Toast.makeText(CalculatorActivity.this, "" + pos,
					Toast.LENGTH_SHORT).show();
		}

	}

	@Override
	public void onNothingSelected(AdapterView<?> arg0) {
		// TODO Auto-generated method stub

	}

	private void addToDisplay(String text) {
		int len = currentDisplay.length();

		// check if operator is presses before the number is presses
		if (isOperatorPressed) {
			resetCurrentDisplay();
		}
		if (len < 10) {
			if (!text.equals(".")) {
				if (currentDisplay.equals("0"))
					currentDisplay = text;
				else
					currentDisplay = currentDisplay + text;
				isdigitPressed = true;
			} else {
				if (!(currentDisplay.indexOf(".") > 0)) {
					currentDisplay = currentDisplay + text;
				}

			}
		}
		display.setText(currentDisplay);
	}

	private void removeDisplayText() {
		int len = currentDisplay.length();
		if (len > 0)
			currentDisplay = currentDisplay.substring(0, len - 1);
		len = currentDisplay.length();
		if (len <= 0) {
			currentDisplay = "0";
			previousValue = 0;
		}
		display.setText(currentDisplay);
	}

	private void resetCurrentDisplay() {
		currentDisplay = "0";
		isOperatorPressed = false;
		taxDisplay.setText("0.0");
		taxDisplay.setTextSize(22.0f);
		totalDisplay.setText("0.0");
		totalDisplay.setTextSize(22.0f);
		display.setText(currentDisplay);
	}

	private String RemoveDecimal0(double val) {
		String v = String.valueOf(Double.valueOf(decimalFormat.format(val)));
		return Integer.valueOf(v.substring(v.indexOf('.') + 1)) > 0 ? v : v
				.substring(0, v.indexOf('.'));
	}

	private void Calculate(String newoperator) {
		currentValue = currentDisplay.isEmpty() ? previousValue : Double
				.parseDouble(currentDisplay);

		if (isdigitPressed) {
			try {
				currentValue = new ExpressionBuilder(previousValue + operator
						+ currentValue).build().calculate();
				// Log.i(TAG, currentValue + "");
				currentDisplay = RemoveDecimal0(currentValue);
			} catch (Exception ex) {

			}
		}
		previousValue = currentValue;

		operator = newoperator;
		isOperatorPressed = true;
		isdigitPressed = false;
		display.setText(currentDisplay);
	}

	@Override
	public void onClick(View v) {

		switch (v.getId()) {
		case R.id.zero:
			addToDisplay("0");
			break;
		case R.id.one:
			addToDisplay("1");
			break;
		case R.id.two:
			addToDisplay("2");
			break;
		case R.id.three:
			addToDisplay("3");
			break;
		case R.id.four:
			addToDisplay("4");
			break;
		case R.id.five:
			addToDisplay("5");
			break;
		case R.id.six:
			addToDisplay("6");
			break;
		case R.id.seven:
			addToDisplay("7");
			break;
		case R.id.eight:
			addToDisplay("8");
			break;
		case R.id.nine:
			addToDisplay("9");
			break;
		case R.id.point:
			addToDisplay(".");
			break;
		case R.id.display:
			removeDisplayText();
			break;
		case R.id.equalsto: {
			Calculate("=");
		}
			break;
		case R.id.plus: {
			Calculate("+");
		}
			break;
		case R.id.minus: {
			Calculate("-");
		}
			break;
		case R.id.multiply: {
			Calculate("*");
		}
			break;
		case R.id.divide: {
			Calculate("/");
		}
			break;

		case R.id.taxPlus:
			double tax = 0;
			double totTax = 0;
			try {

				totalTextView.setText("Total : ");
				double curVal = Double.parseDouble(currentDisplay);
				Calculable calc;

				calc = new ExpressionBuilder(defaultTaxRate.TaxPlus)
						.withVariable("amount", curVal).build();

				tax = calc.calculate();

				totTax = Double.parseDouble(currentDisplay) + tax;
				taxDisplay.setText(String.valueOf(Double.valueOf(decimalFormat
						.format(tax))));
				totalDisplay.setText(String.valueOf(Double
						.valueOf(decimalFormat.format(totTax))));

				display.setText(currentDisplay);
				operator = "";
				isOperatorPressed = true;
				isdigitPressed = false;
			} catch (NumberFormatException ex) {
				throw new NumberFormatException("Display:" + currentDisplay
						+ " tax:" + tax + " total:" + totTax);
			} catch (Exception e) {

			}
			break;

		case R.id.taxMinus:
			tax = 0;
			totTax = 0;
			try {
				totalTextView.setText("Sub Total : ");
				double curVal = Double.parseDouble(currentDisplay);

				Calculable calc = new ExpressionBuilder(defaultTaxRate.TaxMinus)
						.withVariable("amount", curVal).build();
				tax = calc.calculate();

				taxDisplay.setText(String.valueOf(Double.valueOf(decimalFormat
						.format(tax))));

				totTax = Double.parseDouble(currentDisplay) - tax;

				totalDisplay.setText(String.valueOf(Double
						.valueOf(decimalFormat.format(totTax))));

				display.setText(currentDisplay);
				operator = "";
				isOperatorPressed = true;
				isdigitPressed = false;

			} catch (NumberFormatException ex) {
				throw new NumberFormatException("Display:" + currentDisplay
						+ " tax:" + tax + " total:" + totTax);

			} catch (Exception e) {

			}
			break;
		}
	}

	@Override
	public boolean onLongClick(View v) {
		resetCurrentDisplay();
		return false;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.activity_main, menu);
		return true;
	}
}
