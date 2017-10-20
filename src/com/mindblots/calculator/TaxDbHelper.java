package com.mindblots.calculator;

import java.util.ArrayList;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class TaxDbHelper extends SQLiteOpenHelper {
	private static final String DATABASE_NAME = "canadiantaxrates_data.db";

	final String TAG = "TAXDBHELPER === === ====";

	public TaxDbHelper(Context context, int databaseVersion) {
		super(context, DATABASE_NAME, null, databaseVersion);
	}

	public void InsertDefaults() {
		Insert("ON", "Ontario", "HST=13%", "amount* 13/100", "amount*13/113");
		Insert("QC", "Quebec", "GST+QST=14.5%",
				"((((amount*0.05)+amount)*9.5)/100)+(amount*0.05)",
				"((amount/1.14975)*0.05)+((amount/1.14975)*0.09975)");
		Insert("NS", "Nova Scotia", "HST=15%", "amount*15/100", "amount*15/115");
		Insert("NB", "New Brunswick", "HST=15%", "amount* 15/100",
				"amount*15/115");
		Insert("MB", "Manitoba", "GST+PST=13%", "amount* 13/100",
				"amount*13/113");
		Insert("BC", "British Columbia", "HST=12%", "amount*12/100",
				"amount*12/112");
		Insert("PE", "Prince Edward Island", "HST=14%", "amount*14/100",
				"amount*14/114");
		Insert("SK", "Saskatchewan", "GST+PST=10%", "amount/10", "amount/11");
		Insert("AB", "Alberta", "GST=5%", "(amount* 5)/100", "amount*5/105");
		Insert("NL", "Newfoundland and Labrador", "GST=15%",
				"(amount* 15)/100", " amount*15/115");
		Insert("NT", "Northwest Territories", "GST=5%", "(amount* 5)/100",
				"amount*5/105");
		Insert("YT", "Yukon", "GST=5%", "(amount* 5)/100", "amount*5/105");
		Insert("NU", "Nunavut", "GST=5%", "(amount* 5)/100", "amount*5/105");
		// Log.i(TAG, "ALL PROVINCES INSERTED");
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		// CREATE BABY NAMES TABLE
		db.execSQL("CREATE TABLE " + TaxRate.TABLE_NAME + " (" + TaxRate.ID
				+ " varchar(2) PRIMARY KEY, " + TaxRate.PROVINCE + " TEXT,"
				+ TaxRate.TAXRATES + " TEXT," + TaxRate.TAXPLUS + " TEXT,"
				+ TaxRate.TAXMINUS + " TEXT," + TaxRate.ISDEFAULT
				+ " INTEGER);");

	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		db.execSQL("DROP TABLE IF EXISTS " + TaxRate.TABLE_NAME);

	}

	public void Insert(String provinceCode, String province, String taxRates,
			String taxPlus, String taxMinus) {
		ContentValues cv = new ContentValues();
		cv.put(TaxRate.ID, provinceCode);
		cv.put(TaxRate.PROVINCE, province);
		cv.put(TaxRate.TAXRATES, taxRates);
		cv.put(TaxRate.TAXPLUS, taxPlus);
		cv.put(TaxRate.TAXMINUS, taxMinus);
		cv.put(TaxRate.ISDEFAULT, 0);
		SQLiteDatabase sd = getWritableDatabase();
		sd.insert(TaxRate.TABLE_NAME, TaxRate.PROVINCE, cv);
		sd.close();
	}

	public TaxRate getTaxRate(String pro) {
		SQLiteDatabase sd = getWritableDatabase();
		Cursor c = sd.rawQuery("SELECT * FROM taxrates WHERE  province LIKE '"
				+ pro + "%'", null);
		if (c != null)
			c.moveToFirst();
		TaxRate tr = new TaxRate();
		tr.id = c.getString(0);
		tr.Province = c.getString(1);
		tr.TaxRates = c.getString(2);
		tr.TaxPlus = c.getString(3);
		tr.TaxMinus = c.getString(4);
		return tr;
	}

	public ArrayList<TaxRate> getAllProvinces() {
		ArrayList<TaxRate> rates = new ArrayList<TaxRate>();

		// 1. build the query
		String query = "SELECT  * FROM " + TaxRate.TABLE_NAME;

		// 2. get reference to writable DB
		SQLiteDatabase db = this.getWritableDatabase();
		Cursor cursor = db.rawQuery(query, null);

		TaxRate r = null;
		if (cursor.moveToFirst()) {
			do {
				r = new TaxRate();
				r.id = cursor.getString(0);
				r.Province = cursor.getString(1);
				r.TaxRates = cursor.getString(2);
				r.TaxPlus = cursor.getString(3);
				r.TaxMinus = cursor.getString(4);

				rates.add(r);
			} while (cursor.moveToNext());
		}

		// Log.i(TAG, rates.size() + "");

		return rates;
	}

}
