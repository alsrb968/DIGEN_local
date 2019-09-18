/*
 * Copyright (C) 2009 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.litbig.setting.network.bluetooth;

import java.util.List;

import android.bluetooth.BluetoothA2dp;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.os.Handler;
import android.util.Log;

/**
 * This class does all the work for setting up and managing Bluetooth
 * connections with other devices. It has a thread that listens for
 * incoming connections, a thread for connecting with a device, and a
 * thread for performing data transmissions when connected.
 */
public class BluetoothDeviceConnect {
    // Debugging
    private static final String TAG = "BluetoothConnectThread";
    private static final boolean D = false;

    // Member fields
    private final BluetoothAdapter mAdapter;
    private final Handler mHandler;
    private ConnectThread mConnectThread;
    private int mState;
    private BluetoothA2dp mBluetoothA2dp;

    // Constants that indicate the current connection state
    public static final int STATE_NONE = 0;
    public static final int STATE_LISTEN = 1;
    public static final int STATE_PAIRING = 2;
    public static final int STATE_PAIRED = 4;
    public static final int STATE_CONNECTING = 5;
    public static final int STATE_CONNECTED = 6;
    public static final int STATE_A2DP_CONNECT_FAIL = 7;
    public static final int STATE_CONNECT_FAIL = 8;
    public static final int STATE_DISCONNECTING = 9;
    private Context mContext;
    /**
     * Constructor. Prepares a new BluetoothChat session.
     * @param context  The UI Activity Context
     * @param handler  A Handler to send messages back to the UI Activity
     */
    public BluetoothDeviceConnect(Context context, Handler handler) {
        // Establish connection to the proxy.
        mAdapter = BluetoothAdapter.getDefaultAdapter();
        mContext = context;
        mState = STATE_NONE;
        mHandler = handler;
        mAdapter.getProfileProxy(mContext, mProfileListener, BluetoothProfile.A2DP);
    }

    /**
     * Set the current state of the chat connection
     * @param state  An integer defining the current connection state
     */
    private synchronized void setState(int state) {
        if (D) Log.d(TAG, "setState() " + mState + " -> " + state);
        mState = state;

        // Give the new state to the Handler so the UI Activity can update
        mHandler.obtainMessage(BluetoothLayout.MESSAGE_STATE_CHANGE, state, -1).sendToTarget();
    }

    /**
     * Return the current connection state. */
    public synchronized int getState() {
        return mState;
    }

    public synchronized List<BluetoothDevice> onProfileProxy() {
		Log.w(TAG, "onProfileProxy");
    	mSearchFinish = false;
        mAdapter.getProfileProxy(mContext, mProfileListener, BluetoothProfile.A2DP);
        while(!mSearchFinish) {
        	try {
				Thread.sleep(1);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
        }
        return mDeviceList;
    }

    /**
     * Start the ConnectThread to initiate a connection to a remote device.
     * @param device  The BluetoothDevice to connect
     * @param secure Socket Security type - Secure (true) , Insecure (false)
     * @return
     */
    public synchronized void connect(BluetoothDevice device) {
        if (D) Log.d(TAG, "connect to: " + device);

        if (mState == STATE_CONNECTING) {
            if (mConnectThread != null) {mConnectThread.connectDone(false); mConnectThread = null;}
        }

        mConnectThread = new ConnectThread(device);
        mConnectThread.start();
		setState(STATE_CONNECTING);
	}
    
    public synchronized void disconnect(BluetoothDevice device) {
		setState(STATE_DISCONNECTING);
    	mBluetoothA2dp.disconnect(device);
    }

	private List<BluetoothDevice> mDeviceList = null;
	private boolean mSearchFinish = false;
	private static final int[] states = { BluetoothProfile.STATE_DISCONNECTING, BluetoothProfile.STATE_DISCONNECTED,
			BluetoothProfile.STATE_CONNECTED, BluetoothProfile.STATE_CONNECTING };
	private BluetoothProfile.ServiceListener mProfileListener = new BluetoothProfile.ServiceListener() {
		public void onServiceConnected(int profile, BluetoothProfile proxy) {
			if (profile == BluetoothProfile.A2DP) {
				mBluetoothA2dp = (BluetoothA2dp) proxy;
				mDeviceList = mBluetoothA2dp.getDevicesMatchingConnectionStates(states);
			}

			if (mDeviceList != null && mDeviceList.size() > 0) {
				for (BluetoothDevice device : mDeviceList) {
					Log.w(TAG, "mBluetooth found new device: " + device.getName());
				}
			} else {
				Log.w(TAG, "mBluetooth A2dp size 0");
			}
			mSearchFinish = true;
		}

		public void onServiceDisconnected(int profile) {
			Log.d(TAG, "disconnect, mProfileListener");
			if (profile == BluetoothProfile.A2DP) {
				mBluetoothA2dp = null;

			}
		}
	};

	/**
	 * Stop all threads
	 */
    public synchronized void stop() {
        if (D) Log.d(TAG, "stop");

        if (mConnectThread != null) {
            mConnectThread.connectDone(false);
            mConnectThread = null;
        }

        setState(STATE_NONE); 
        mAdapter.closeProfileProxy(BluetoothProfile.A2DP, mBluetoothA2dp);
    }
    public void connectDone(boolean success) {
    	if(mConnectThread != null) {
   			mConnectThread.connectDone(success);
    	}
    }
    /**
     * This thread runs while attempting to make an outgoing connection
     * with a device. It runs straight through; the connection either
     * succeeds or fails.
     */
    private class ConnectThread extends Thread {
        private final BluetoothDevice mmDevice;
        private boolean mmSuccess  = false;
        
        public ConnectThread(BluetoothDevice device) {
            mmDevice = device;
        }

    	public void connectDone(boolean success){
    		mmSuccess = success;
    		awakeThread();
    	}

    	public void awakeThread(){
    		synchronized (this) {
    			notifyAll();
    			this.interrupt();
    		}
    	}
        public void run() {
            if(mAdapter.isDiscovering()) mAdapter.cancelDiscovery();
            if(mmDevice.getBondState() !=BluetoothDevice.BOND_BONDED) {
                setState(STATE_PAIRING); 
                mmDevice.createBond();
            }else {
            	if(mBluetoothA2dp.getConnectionState(mmDevice) == BluetoothA2dp.STATE_CONNECTED) {
            		if(!mBluetoothA2dp.disconnect(mmDevice))setState(STATE_NONE);
            	}else {
            		if(!mBluetoothA2dp.connect(mmDevice))setState(STATE_NONE);
            		
            	}
            	return ;
            }
            if(mBluetoothA2dp == null) {
            	setState(STATE_NONE);
            	return;
            }
            try {
				while (true) {
					synchronized (this) {
						wait(1000);
					}
				}
            } catch (InterruptedException e) {
            	e.printStackTrace();
            }finally {
            	if(mmSuccess) {
            		if(!(onProfileProxy().indexOf(mmDevice) > -1)) {
            			setState(STATE_A2DP_CONNECT_FAIL);
            		}else {
            			if(!mBluetoothA2dp.connect(mmDevice))setState(STATE_NONE);
            		}
            	}else {
            		setState(STATE_CONNECT_FAIL);
            	}
			}
            Log.w(TAG, "ConnectThread end");
        }
    }
}
