package com.litbig.setting.wifi;

import android.content.Context;
import android.net.NetworkInfo;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;

public class ListViewAdapter extends BaseAdapter {
    private ArrayList<WifiListItem> wifiList;
    private Context mContext;

    public ListViewAdapter(ArrayList<WifiListItem> list, Context context) {
        wifiList = list;
        mContext = context;
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
        ImageView iconConnect = convertView.findViewById(R.id.icon_connect_imageview);
        iconConnect.setLayoutParams(new LinearLayout.LayoutParams((int)(42 * WifiSetting.getXYDensity(mContext).x), ViewGroup.LayoutParams.MATCH_PARENT));
        TextView ssid = convertView.findViewById(R.id.ssid);
        ssid.setTextSize(TypedValue.COMPLEX_UNIT_PX, 26 * WifiSetting.getDisplayRate(mContext));
        TextView state = convertView.findViewById(R.id.state);

        convertView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, (int)(88 * WifiSetting.getXYDensity(mContext).y)));
        WifiListItem result = wifiList.get(position);

        int signal = result.getLevel();

        if(result.getSecurity() == WifiListItem.SECURITY_WEP || result.getSecurity() == WifiListItem.SECURITY_PSK || result.getSecurity() == WifiListItem.SECURITY_EAP){
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

        if(result.getState() == NetworkInfo.DetailedState.CONNECTED) {
            ssid.setSelected(true);
            iconConnect.setVisibility(View.VISIBLE);
        } else {
            ssid.setSelected(false);
            iconConnect.setVisibility(View.GONE);
        }

        ssid.setText(result.getSsid()+"  ");
        state.setText(result.getSummary());

        return convertView;
    }
}
