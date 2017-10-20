package com.mindblots.calculator;

import java.util.ArrayList;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class ProvinceAdapter extends BaseAdapter {
	private ArrayList<TaxRate> listProvinces;
	private Activity activity;

	public ProvinceAdapter(Activity activity, ArrayList<TaxRate> listProvinces) {
		super();
		this.listProvinces = listProvinces;
		this.activity = activity;
	}

	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return listProvinces.size();
	}

	@Override
	public TaxRate getItem(int position) {
		// TODO Auto-generated method stub
		return listProvinces.get(position);
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return 0;
	}

	public int getItemPosition(String item) {
		int position = 0;
		for (TaxRate r : listProvinces) {
			if (r.Province.equalsIgnoreCase(item)) {
				return position;
			} else {
				++position;
			}

		}
		return 0;
	}

	public static class ViewHolder {
		public ImageView imgViewFlag;
		public TextView txtViewTitle;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {

		ViewHolder view;
		LayoutInflater inflator = activity.getLayoutInflater();

		if (convertView == null) {
			view = new ViewHolder();
			convertView = inflator.inflate(R.layout.province_row, null);

			view.txtViewTitle = (TextView) convertView
					.findViewById(R.id.provinceTxt);

			convertView.setTag(view);
		} else {
			view = (ViewHolder) convertView.getTag();
		}

		view.txtViewTitle.setText(listProvinces.get(position).Province);

		return convertView;
	}
}
