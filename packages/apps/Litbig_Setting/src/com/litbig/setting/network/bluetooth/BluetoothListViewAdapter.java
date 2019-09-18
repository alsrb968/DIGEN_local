package com.litbig.setting.network.bluetooth;

import java.util.ArrayList;

import com.litbig.app.setting.R;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

// ----------
// ListAdapter
class BluetoothListViewAdapter extends BaseAdapter implements View.OnClickListener{
	private ArrayList<BluetoothListItem> mBluetoothList;
	private ListBtnClickListener mListBtnClickListener;
	public interface ListBtnClickListener {
		void onListBtnClick(int position) ;
	}
	
	public BluetoothListViewAdapter(ArrayList<BluetoothListItem> list, ListBtnClickListener clickListener) {
        mListBtnClickListener = clickListener ;
		mBluetoothList = list;
	}
    
	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		final Context context = parent.getContext();
		if(convertView == null) {
			LayoutInflater inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			convertView = inflater.inflate(R.layout.device_list_item, parent, false);
		}
		
		ImageView iconImage = convertView.findViewById(R.id.device_connect_stat_image);
		iconImage.setImageDrawable(context.getResources().getDrawable(R.drawable.bt_dis, null));
		TextView name = convertView.findViewById(R.id.device_name_text);
		TextView state = convertView.findViewById(R.id.device_stat_text);
		ImageButton iconImageButton = convertView.findViewById(R.id.device_setting_button);
		iconImageButton.setTag(position);
		iconImageButton.setOnClickListener(this);
		
		BluetoothListItem item;
		if(mBluetoothList.size()>position)item = mBluetoothList.get(position);
		else return convertView;
		
		name.setText(item.getName());
		if(item.isPaired()) {
			iconImageButton.setVisibility(View.VISIBLE);
			iconImageButton.setFocusable(false);
			iconImage.setFocusableInTouchMode(false);
		}else {
			iconImageButton.setVisibility(View.GONE);
		}
		if(item.getState() == BluetoothDeviceConnect.STATE_NONE) {
			state.setVisibility(View.GONE);
		}else {
			state.setVisibility(View.VISIBLE);
			switch (item.getState()) {
			case BluetoothDeviceConnect.STATE_PAIRING:
				state.setText(context.getText(R.string.bluetooth_state_pairing));
				break;
			case BluetoothDeviceConnect.STATE_CONNECTING:
				state.setText(context.getText(R.string.bluetooth_state_connecting));
				break;
			case BluetoothDeviceConnect.STATE_CONNECTED:
				state.setText(context.getText(R.string.bluetooth_state_connected));
				iconImage.setImageDrawable(context.getResources().getDrawable(R.drawable.bt_con, null));
				break;
			case BluetoothDeviceConnect.STATE_DISCONNECTING:
				state.setText(context.getText(R.string.bluetooth_state_disconnecting));
			default:
				state.setVisibility(View.GONE);
				break;
			}
		}
		return convertView;
	}
	@Override
	public int getCount() {
		if(mBluetoothList != null)
			return mBluetoothList.size();
		else
			return 0;
	}

	@Override
	public Object getItem(int position) {
		return mBluetoothList.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}
	
	public void clear() {
		if(mBluetoothList != null)
			mBluetoothList.clear();
	}
	public void stateClear() {
		if(mBluetoothList != null) {
			for (int i = 0; i < mBluetoothList.size(); i++) {
				BluetoothListItem item = mBluetoothList.get(i);
				item.setState(BluetoothDeviceConnect.STATE_NONE);
				mBluetoothList.set(i, item);
			}
		}
	}

	@Override
	public void onClick(View v) {
		if (this.mListBtnClickListener != null) {
			this.mListBtnClickListener.onListBtnClick((Integer)v.getTag());
		}
	}
}
