package com.litbig.setting.wifi;

import android.content.Context;
import android.net.NetworkInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;

public class WifiListItem{
    private final String TAG = "WifiListItem";

    public static final int SECURITY_NONE	= 0;
    public static final int SECURITY_WEP	= 1;
    public static final int SECURITY_PSK	= 2;
    public static final int SECURITY_EAP	= 3;

    public enum PskType {
        UNKNOWN,
        WPA,
        WPA2,
        WPA_WPA2
    }

    private String ssid;
    private String bssid;
    private int security;
    private int networkId;
    private boolean wpsAvailable = false;
    private PskType pskType = PskType.UNKNOWN;

    WifiListItem (Context context, WifiConfiguration config){
        loadConfig(config);
        mContext = context;
        refresh();
    }

    WifiListItem (Context context, ScanResult result) {
        loadResult(result);
        mContext = context;
        refresh();
    }

    public String getSsid() {
        return ssid;
    }

    public void setSsid(String ssid) {
        this.ssid = ssid;
    }

    public String getBssid() {
        return bssid;
    }

    public void setBssid(String bssid) {
        this.bssid = bssid;
    }

    public int getSecurity() {
        return security;
    }

    public void setSecurity(int security) {
        this.security = security;
    }

    public int getNetworkId() {
        return networkId;
    }

    public void setNetworkId(int networkId) {
        this.networkId = networkId;
    }

    public boolean isWpsAvailable() {
        return wpsAvailable;
    }

    public void setWpsAvailable(boolean wpsAvailable) {
        this.wpsAvailable = wpsAvailable;
    }

    public PskType getPskType() {
        return pskType;
    }

    public void setPskType(PskType pskType) {
        this.pskType = pskType;
    }

    private WifiConfiguration mConfig;
    ScanResult mScanResult;

    private String mSummary;
    private int mRssi;
    private WifiInfo mInfo;
    private NetworkInfo.DetailedState mState;
    private Context mContext;

    private void loadResult(ScanResult result) {
        ssid = result.SSID;
        bssid = result.BSSID;
        security = getSecurity(result);
        wpsAvailable = security != SECURITY_EAP && result.capabilities.contains("WPS");

        if(security == SECURITY_PSK)
            pskType = getPskType(result);

        networkId = -1;
        mRssi = result.level;
        mScanResult = result;
    }

    private void loadConfig(WifiConfiguration config) {
        ssid = (config.SSID == null  ? "" : removeDoubleQuotes(config.SSID));
        bssid = config.BSSID;
        security = getSecurity(config);
        networkId = config.networkId;
        mRssi = Integer.MAX_VALUE;
        mConfig = config;
    }

    public boolean update(ScanResult result) {
        if(ssid.equals(result.SSID) && security == getSecurity(result)) {
            if(WifiManager.compareSignalLevel(result.level, mRssi) > 0) {
                int oldLevel = getLevel();
                mRssi = result.level;
                if(getLevel() != oldLevel) {
                    // noti changed
                }
            }
            if(security == SECURITY_PSK)
                pskType = getPskType(result);

            refresh();

            return true;
        }
        return false;
    }

    public void update(WifiInfo info, NetworkInfo.DetailedState state) {
        if(info != null && networkId != WifiConfiguration.INVALID_NETWORK_ID && networkId == info.getNetworkId()) {
            mRssi = info.getRssi();
            mInfo = info;
            mState = state;
            refresh();
        } else if (mInfo != null) {
            mInfo = null;
            mState = null;
            refresh();
        }
    }

    public int getSecurity(ScanResult result) {
        if(result.capabilities.contains("WEP"))
            return SECURITY_WEP;
        else if(result.capabilities.contains("PSK"))
            return SECURITY_PSK;
        else if(result.capabilities.contains("EAP"))
            return SECURITY_EAP;

        return SECURITY_NONE;
    }

    public static int getSecurity(WifiConfiguration config) {
        if(config.allowedKeyManagement.get(WifiConfiguration.KeyMgmt.WPA_PSK))
            return SECURITY_PSK;

        if(config.allowedKeyManagement.get(WifiConfiguration.KeyMgmt.WPA_EAP))
            return SECURITY_EAP;

        return (config.wepKeys[0] != null) ? SECURITY_EAP : SECURITY_NONE;//TODO: @deprecated
    }

    public PskType getPskType(ScanResult result) {
        boolean wpa = result.capabilities.contains("WPA-PSK");
        boolean wpa2 = result.capabilities.contains("WPA2-PSK");

        if(wpa && wpa2)
            return PskType.WPA_WPA2;
        else if(wpa2)
            return PskType.WPA2;
        else if(wpa)
            return PskType.WPA;
        else
            return PskType.UNKNOWN;
    }

    public String getSecurityString(boolean concise) {
        switch(security) {
            case SECURITY_EAP:
                return concise ? mContext.getString(R.string.wifi_security_short_eap)
                        : mContext.getString(R.string.wifi_security_eap);
            case SECURITY_PSK:
                switch(pskType) {
                    case WPA:
                        return concise ? mContext.getString(R.string.wifi_security_short_wpa)
                                : mContext.getString(R.string.wifi_security_wpa);
                    case WPA2:
                        return concise ? mContext.getString(R.string.wifi_security_short_wpa2)
                                : mContext.getString(R.string.wifi_security_wpa2);
                    case WPA_WPA2:
                        return concise ? mContext.getString(R.string.wifi_security_short_wpa_wpa2)
                                : mContext.getString(R.string.wifi_security_wpa_wpa2);
                    case UNKNOWN:
                    default:
                        return concise ? mContext.getString(R.string.wifi_security_short_psk_generic)
                                : mContext.getString(R.string.wifi_security_psk_generic);
                }
            case SECURITY_WEP:
                return concise ? mContext.getString(R.string.wifi_security_short_wep)
                        : mContext.getString(R.string.wifi_security_wep);
            case SECURITY_NONE:
            default:
                return concise ? "" : mContext.getString(R.string.wifi_security_open);
        }
    }

    public int getLevel() {
        if(mRssi == Integer.MAX_VALUE)
            return -1;

        return WifiManager.calculateSignalLevel(mRssi, 5);
    }

    public WifiConfiguration getConfig() {
        return mConfig;
    }

    public WifiInfo getInfo() {
        return mInfo;
    }

    public NetworkInfo.DetailedState getState() {
        return mState;
    }

    public String getSummary() {
        return mSummary;
    }

    public static String removeDoubleQuotes(String string) {
        int length = string.length();
        if((length > 1) && (string.charAt(0) == '"') && (string.charAt(length - 1) == '"'))
            return string.substring(1, length - 1);

        return string;
    }

    public static String convertToQuotedString(String string) {
        return "\"" + string + "\"";
    }

    private void refresh() {
        if(mConfig != null && mConfig.status == WifiConfiguration.Status.DISABLED) {
            WifiConfiguration.NetworkSelectionStatus networkStatus =
                    mConfig.getNetworkSelectionStatus();
            switch(networkStatus.getNetworkSelectionDisableReason()/*mConfig.disableReason*/) {
                case WifiConfiguration.NetworkSelectionStatus.DISABLED_AUTHENTICATION_FAILURE/*DISABLED_AUTH_FAILURE*/:
                    mSummary = mContext.getString(R.string.wifi_disabled_password_failure);

//				DeviceToast toast = new DeviceToast(mContext);
//				toast.showToast(mContext.getString(R.string.wifi_toast_msg_password_incorrect), Toast.LENGTH_SHORT);

                    WifiManager manager = (WifiManager)mContext.getSystemService(Context.WIFI_SERVICE);
                    manager.forget(mConfig.networkId, null);
                    break;
                case WifiConfiguration.NetworkSelectionStatus.DISABLED_DHCP_FAILURE:
                case WifiConfiguration.NetworkSelectionStatus.DISABLED_DNS_FAILURE:
                    mSummary = mContext.getString(R.string.wifi_disabled_network_failure);
                    break;
                case WifiConfiguration.NetworkSelectionStatus.DISABLED_ASSOCIATION_REJECTION/*DISABLED_UNKNOWN_REASON*/:
                    mSummary = mContext.getString(R.string.wifi_disabled_generic);
                    break;
            }
        }

        else if(mRssi == Integer.MAX_VALUE) {
            mSummary = mContext.getString(R.string.wifi_not_in_range);
        }

        else if(mState != null) { // active connection
            int index = mState.ordinal();

            String[] formats = mContext.getResources().getStringArray(R.array.wifi_status);

            if (index >= formats.length || formats[index].length() == 0)
                mSummary = "";
            else
                mSummary = String.format(formats[index], ssid);
        }

        else {
            StringBuilder summary = new StringBuilder();
            if(mConfig != null) {
                summary.append(mContext.getString(R.string.wifi_remembered));
            }

            if(security != SECURITY_NONE) {
                String securityStrFormat;
                if(summary.length() == 0)
                    securityStrFormat = mContext.getString(R.string.wifi_secured_first_item);
                else
                    securityStrFormat = mContext.getString(R.string.wifi_secured_second_item);

                summary.append(String.format(securityStrFormat, getSecurityString(true)));
            }

            if(mConfig == null && wpsAvailable) {
                if(summary.length() == 0)
                    summary.append(mContext.getString(R.string.wifi_wps_available_first_item));
                else
                    summary.append(mContext.getString(R.string.wifi_wps_available_second_item));
            }

            mSummary = summary.toString();
        }
    }

    public void generateOpenNetworkConfig() {
        if (security != SECURITY_NONE)
            throw new IllegalStateException();
        if (mConfig != null)
            return;
        mConfig = new WifiConfiguration();
        mConfig.SSID = convertToQuotedString(ssid);
        mConfig.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
    }
}
