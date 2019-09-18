package com.litbig.setting.network.wifi;

import java.util.ArrayList;

import com.litbig.app.setting.R;

import android.content.Context;
import android.net.NetworkInfo.DetailedState;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class ListViewAdapter extends BaseAdapter{

	private ArrayList<WifiListItem> wifiList;
	
	public ListViewAdapter(ArrayList<WifiListItem> list) {
		wifiList = list;
	}
	
	public void clear() {
		if(wifiList != null)
			wifiList.clear();
	}
	
	@Override
	public int getCount() {
		if(wifiList != null)
			return wifiList.size();
		else
			return 0;
	}

	@Override
	public Object getItem(int position) {
		return wifiList.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		final Context context = parent.getContext();
		
		if(convertView == null) {
			LayoutInflater inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			convertView = inflater.inflate(R.layout.wifi_list_item, parent, false);
		}
		
		ImageView iconImage = convertView.findViewById(R.id.icon);
		TextView ssid = convertView.findViewById(R.id.ssid);
		TextView state = convertView.findViewById(R.id.state);
		
		WifiListItem result = wifiList.get(position);

		int signal = result.getLevel();
		
		if(result.security == WifiListItem.SECURITY_WEP || result.security == WifiListItem.SECURITY_PSK || result.security == WifiListItem.SECURITY_EAP){
			switch(signal){
			case 4:
				iconImage.setImageResource(R.drawable.wifi_close_04);
				break;
			case 3:
				iconImage.setImageResource(R.drawable.wifi_close_03);
				break;
			case 2:
				iconImage.setImageResource(R.drawable.wifi_close_02);
				break;
			case 1:
				iconImage.setImageResource(R.drawable.wifi_close_01);
				break;
			case 0:
				iconImage.setImageResource(R.drawable.wifi_close_00);
				break;
			}
		}
		else {
			switch(signal){
			case 4:
				iconImage.setImageResource(R.drawable.wifi_open_04);
				break;
			case 3:
				iconImage.setImageResource(R.drawable.wifi_open_03);
				break;
			case 2:
				iconImage.setImageResource(R.drawable.wifi_open_02);
				break;
			case 1:
				iconImage.setImageResource(R.drawable.wifi_open_01);
				break;
			case 0:
				iconImage.setImageResource(R.drawable.wifi_open_00);
				break;
			}
		}
		
		if(result.getState() == DetailedState.CONNECTED)
			ssid.setSelected(true);
		else
			ssid.setSelected(false);
		
		ssid.setText(result.ssid);
		state.setText(result.getSummary());
		
		return convertView;
	}
}

