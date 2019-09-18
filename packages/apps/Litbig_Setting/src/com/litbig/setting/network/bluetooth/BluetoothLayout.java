package com.litbig.setting.network.bluetooth;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Set;

import com.litbig.app.setting.R;
import com.litbig.setting.network.NetworkViewInterface;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

public class BluetoothLayout extends RelativeLayout
		implements NetworkViewInterface, BluetoothListViewAdapter.ListBtnClickListener {
	private final String TAG = "BluetoothConnectLayout";

    // Key names received from the BluetoothChatService Handler
    public static final String DEVICE_NAME = "device_name";
    public static final String TOAST = "toast";
	
	private LinearLayout mLlBtListArea;
	private TextView mTvBtSwitch;
	private ImageButton mBtnRefresh;
	private ScrollView mScrollView;
	private TextView mTvPairedDeviceTitle;
	private TextView mTvNewDeviceTitle;
	
	private Context mContext;

	private int mState = BluetoothAdapter.ERROR;
	
	private boolean mBtState = false;
	
    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothListViewAdapter mPairedDevicesArrayAdapter;
    private BluetoothListViewAdapter mNewDevicesArrayAdapter;
    private static ArrayList<BluetoothListItem> mPairedDevicesArrayList = new ArrayList<>();
    private static ArrayList<BluetoothListItem> mNewDevicesArrayList = new ArrayList<>();
    private ListView mPairedDevicesListView;
    private ListView mNewDevicesListView;
    private BluetoothListItem mSelectItem;
    private HashMap<String, String> mChangeNameMap = new HashMap<>();
    
    private PairedSettingDialog mPairedDialog;
    
    private BluetoothDeviceConnect mConnectService = null;
    
	private InputMethodManager mInputMethodManager;
	
    private boolean mIsParing = false;
    
	public BluetoothLayout(Context context, AttributeSet attrs) { 
		super(context, attrs);
		// TODO Auto-generated constructor stub
		mContext = context;
		
		// Bluetooth On/Off broadcasts
		IntentFilter filter = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
		
		// Discovery broadcasts
		filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
		filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
		filter.addAction(BluetoothDevice.ACTION_FOUND);
//		filter.addAction(BluetoothDevice.ACTION_NAME_CHANGED);
		
		// Pairing/connecting broadcasts
		filter.addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
		filter.addAction(BluetoothDevice.ACTION_ACL_CONNECTED);
		filter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED);
		filter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECT_REQUESTED);
		
		// Fine-grained state broadcasts
//		filter.addAction(BluetoothDevice.ACTION_CLASS_CHANGED);
		
		// Dock event broadcasts
		filter.addAction(Intent.ACTION_DOCK_EVENT);
		
		mContext.registerReceiver(mReceiver, filter);
        if (mConnectService == null)
        	mConnectService = new BluetoothDeviceConnect(mContext, mHandler);

		mInputMethodManager = (InputMethodManager)mContext.getSystemService(Context.INPUT_METHOD_SERVICE);
        
	}
    
	@Override
	protected void onAttachedToWindow() {
		super.onAttachedToWindow();
		
		mTvBtSwitch = findViewById(R.id.btn_tb_bluetooth_switch);
		mBtnRefresh 	= findViewById(R.id.btn_bluetooth_refresh);
		mTvPairedDeviceTitle 	= findViewById(R.id.title_paired_devices);
		mTvNewDeviceTitle 	= findViewById(R.id.title_new_devices);
		
		mLlBtListArea 	= findViewById(R.id.bluetooth_list_area);
		
		mTvBtSwitch.setOnClickListener(mClickListener);
		mBtnRefresh.setOnClickListener(mClickListener);
		
		mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
		
		handleBtStateChanged(mBluetoothAdapter.getState());
		setBtListVisibility(mBtState); 
		
        mPairedDevicesArrayAdapter = new BluetoothListViewAdapter(mPairedDevicesArrayList,this);
        mPairedDevicesListView = findViewById(R.id.paired_devices);
        mPairedDevicesListView.setAdapter(mPairedDevicesArrayAdapter);
        mPairedDevicesListView.setOnItemClickListener(mDeviceClickListener);
        mPairedDevicesListView.setOnTouchListener(mListTouchListener);
        mPairedDevicesListView.setVerticalScrollBarEnabled(false);
        mPairedDevicesListView.setHorizontalScrollBarEnabled(false);
        mPairedDevicesArrayAdapter.notifyDataSetChanged();

        if(mNewDevicesArrayAdapter == null)
        	mNewDevicesArrayAdapter = new BluetoothListViewAdapter(mNewDevicesArrayList,this);
        mNewDevicesListView = findViewById(R.id.new_devices);
        mNewDevicesListView.setAdapter(mNewDevicesArrayAdapter);
        mNewDevicesListView.setOnItemClickListener(mDeviceClickListener);
        mNewDevicesListView.setOnTouchListener(mListTouchListener);
        mNewDevicesListView.setVerticalScrollBarEnabled(false);
        mNewDevicesListView.setHorizontalScrollBarEnabled(false);
    	mNewDevicesArrayAdapter.notifyDataSetChanged();

		setPairedDeviceState();
		
        // Register for broadcasts when a device is discovered
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        mContext.registerReceiver(mReceiver, filter);

        // Register for broadcasts when discovery has finished
        filter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        mContext.registerReceiver(mReceiver, filter);

        // Get the local Bluetooth adapter
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if(mPairedDevicesArrayList.size() == 0) doPairedDeviceRefresh();
        else mTvPairedDeviceTitle.setVisibility(View.VISIBLE);
	}

    private OnItemClickListener mDeviceClickListener = new OnItemClickListener() {
        public void onItemClick(AdapterView<?> av, View v, int arg2, long arg3) {
            mBluetoothAdapter.cancelDiscovery();
			if (av == mPairedDevicesListView) {
				mSelectItem = mPairedDevicesArrayList.get(arg2);
			}else if(av == mNewDevicesListView) {
				if(mIsParing) return;
				mSelectItem = mNewDevicesArrayList.get(arg2);
				mIsParing = true;
			}
			BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(mSelectItem.getAddress());
			if(mSelectItem.getState() == BluetoothDeviceConnect.STATE_CONNECTED) mConnectService.disconnect(device); 
			else mConnectService.connect(device);
        }
    };
    
    private View.OnTouchListener mListTouchListener = new View.OnTouchListener() {
		@Override
		public boolean onTouch(View v, MotionEvent event) {
			if(mScrollView != null) mScrollView.requestDisallowInterceptTouchEvent(true);
			return false;
		}
	};
	
	private View.OnClickListener mClickListener = new View.OnClickListener() {
		@Override
		public void onClick(View view) {
			if (view == mTvBtSwitch) {
				setBluetoothEnabled(!mBtState);
			} else if (view == mBtnRefresh) {
				doDiscovery();
			} 
		}
	};

    private synchronized void doDiscovery() {
	        mBluetoothAdapter.cancelDiscovery();
//	        if (mNewDevicesArrayAdapter != null && mNewDevicesArrayList != null) {
//	        	mNewDevicesArrayAdapter.clear();
//	        	mNewDevicesArrayList.clear();
//	        }
	        mBluetoothAdapter.startDiscovery();
    }
    
	@Override
	protected void onDetachedFromWindow() {

        if (mBluetoothAdapter != null) {
            mBluetoothAdapter.cancelDiscovery();
        }
		if (mContext != null) {
			mContext.unregisterReceiver(mReceiver);
		}
		
		super.onDetachedFromWindow();
	}
	
	private void setBtListVisibility(boolean value) {
		if(value) {
			mLlBtListArea.setVisibility(View.VISIBLE);
		} else {
			mLlBtListArea.setVisibility(View.GONE);
		}
	}
	
	synchronized void setBluetoothStateInt(int state) {
		mState = state;
		
		if (state == BluetoothAdapter.STATE_ON) {
		}
	}
	
	public boolean syncBluetoothState() {
		int currentState = mBluetoothAdapter.getState();
		if (currentState != mState) {
			setBluetoothStateInt(mBluetoothAdapter.getState());
			return true;
		}
		
		return false;
	}
	
	public void setBluetoothEnabled(boolean enabled) {
		boolean success = enabled ? mBluetoothAdapter.enable() : mBluetoothAdapter.disable();
		
		if(success) {
			setBluetoothStateInt(enabled
					? BluetoothAdapter.STATE_TURNING_ON
					: BluetoothAdapter.STATE_TURNING_OFF);
		} else {
			
			syncBluetoothState();
		}
	}
	
	private void setSwitchBtButton(boolean value) {
		
		if(mTvBtSwitch == null)
			 return ;
		
		if(value) {
			mTvBtSwitch.setText(mContext.getString(R.string.on));
		} else {
			mTvBtSwitch.setText(mContext.getString(R.string.off));
		}
		
		mTvBtSwitch.setSelected(value);
		mTvBtSwitch.setClickable(true);

		setBtListVisibility(value);
	}

	private void handleBtStateChanged(int state) {
		switch(state) {
		case BluetoothAdapter.STATE_ON:
			setSwitchBtButton(true);
			
			mBtState = true;
			break;
		case BluetoothAdapter.STATE_TURNING_ON:
		case BluetoothAdapter.STATE_TURNING_OFF:
			mTvBtSwitch.setClickable(false);
			break;
		case BluetoothAdapter.STATE_OFF:
			setSwitchBtButton(false);

			mBtState = false;
			break;
			default:
			setSwitchBtButton(false);
			
			break;
		}
	}
	
	private BroadcastReceiver mReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			final String action = intent.getAction();
			BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
			Log.d(TAG, "BroadcastReceiver action : " + action);
			if(device != null) Log.d(TAG, "device : " + device.getName());
			if (action.equals(BluetoothAdapter.ACTION_STATE_CHANGED)) {
				int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR);
				handleBtStateChanged(state);
			} else if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                if (device.getBondState() != BluetoothDevice.BOND_BONDED) {
                	BluetoothListItem holder = new BluetoothListItem();
                	holder.setAddress(device.getAddress());
                	holder.setName(device.getName());
                	holder.setPaired(false);
                	holder.setPosition(mNewDevicesArrayList.size());

					if (mNewDevicesArrayAdapter != null && device.getName() != null) {
						if (containItemOnList(mNewDevicesArrayList, device.getAddress()) != -1) {
							int size = mNewDevicesArrayList.size();
							int rePosition = 0;
							int cnt = 0;
							BluetoothListItem reItem = null;
							for (BluetoothListItem item : mNewDevicesArrayList) {
								if (item.getAddress().equals(device.getAddress())) {
									rePosition = item.getPosition();
									item.setPosition(size);
									reItem = item;
									break;
								}
								++cnt;
							}

							for (int i = size-1; i >= 0; i--) {
			                	BluetoothListItem item = mNewDevicesArrayList.get(i);
								int cntPosition = item.getPosition();
								if (cntPosition > rePosition) {
									item.setPosition((cntPosition - 1));
									mNewDevicesArrayList.set(i, item);
								}
							}
							mNewDevicesArrayList.set(cnt, reItem);
						} else {
							mNewDevicesArrayList.add(holder);
						}
					}
					Collections.sort(mNewDevicesArrayList, mComparator);
					mNewDevicesArrayAdapter.notifyDataSetChanged();

					setPairedDeviceState();
                }
			} else if (BluetoothDevice.ACTION_ACL_DISCONNECT_REQUESTED.equals(action)) {
			} else if (BluetoothDevice.ACTION_ACL_DISCONNECTED.equals(action)) {
                setPairedDeviceState(device, BluetoothDeviceConnect.STATE_NONE);
			} else if (BluetoothDevice.ACTION_ACL_CONNECTED.equals(action)) {
                setPairedDeviceState(device, BluetoothDeviceConnect.STATE_CONNECTED);
            } else if (BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(action)) {
				mBtnRefresh.setAlpha(0.5f);
				mBtnRefresh.setClickable(false);
            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
				mBtnRefresh.setAlpha(1f);
				mBtnRefresh.setClickable(true);
			} else if (BluetoothDevice.ACTION_BOND_STATE_CHANGED.equals(action)) {
                int state = intent.getIntExtra(BluetoothDevice.EXTRA_BOND_STATE,-1);
                Log.d(TAG,"bond stat : " + state);
                switch (state) {
				case BluetoothDevice.BOND_BONDED:
					mConnectService.connectDone(true);
					doPairedDeviceRefresh();
	                /*if(device.getAclState())setPairedDeviceState(device, BluetoothDeviceConnect.STATE_CONNECTED);
	                else*/ setPairedDeviceState(device, BluetoothDeviceConnect.STATE_NONE);
					mIsParing = false;
					break;
				case BluetoothDevice.BOND_BONDING:
	                setPairedDeviceState(device, BluetoothDeviceConnect.STATE_PAIRING);
					break;
				case BluetoothDevice.BOND_NONE:
					mConnectService.connectDone(false);
					doPairedDeviceRefresh();
					mNewDevicesArrayAdapter.stateClear();
					mNewDevicesArrayAdapter.notifyDataSetChanged();
					mIsParing = false;
					break;
				default:
					break;
				}
			}
		}
	};	
	
	public final Comparator mComparator = new Comparator<BluetoothListItem>() {
		private final Collator collator = Collator.getInstance();
		@Override public int compare(BluetoothListItem obj1, BluetoothListItem obj2) {
			if (obj1.getPosition() > obj2.getPosition()) return -1;
			else return 0;
		}
	};
	
	private void setPairedDeviceState(BluetoothDevice device, int state) {
		int position = 0;
		for (BluetoothListItem bluetoothListItem : mPairedDevicesArrayList) {
			if(bluetoothListItem.getAddress().equals(device.getAddress())) {
				bluetoothListItem.setState(state);
				mPairedDevicesArrayList.set(position, bluetoothListItem);
                mPairedDevicesArrayAdapter.notifyDataSetChanged();
				return;
			}
			else position++;
		}
	}

	private void setPairedDeviceState() {
		if (mNewDevicesArrayList != null) {
			mTvNewDeviceTitle.setText(mContext.getString(R.string.bluetooth_title_new_device) + " [ "
					+ mNewDevicesArrayList.size() + " ]");
		} else {
			mTvNewDeviceTitle.setText(mContext.getString(R.string.bluetooth_title_new_device) + "[ 0 ]");
		}
		if (mPairedDevicesArrayList != null) {
			mTvPairedDeviceTitle.setText(mContext.getString(R.string.bluetooth_title_paired_device) + " [ "
					+ mPairedDevicesArrayList.size() + " ]");
		} else {
			mTvPairedDeviceTitle.setText(mContext.getString(R.string.bluetooth_title_new_device) + "[ 0 ]");
		}
	}
	
    private synchronized boolean doPairedDeviceRefresh() {
        Log.d(TAG,"doPairedDeviceRefresh");
		if (mPairedDevicesArrayAdapter != null){
			Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
			if (pairedDevices.size() > 0) {
				for (BluetoothDevice device : pairedDevices) {
	            	if(mSelectItem !=null && mSelectItem.getAddress().equals(device.getAddress())) {
	            		if(mSelectItem.getState() == BluetoothDeviceConnect.STATE_A2DP_CONNECT_FAIL) {
	            			device.removeBond();
	    		            int position = containItemOnList(mPairedDevicesArrayList, mSelectItem.getAddress());
		            		if(position != -1) {
		            			mPairedDevicesArrayList.remove(position);
	            				mPairedDevicesArrayAdapter.notifyDataSetChanged();
		            		}
	            			continue;
	            		}
	            		if(containItemOnList(mNewDevicesArrayList, mSelectItem)) {
	            			mNewDevicesArrayList.remove(mSelectItem);
	            			mNewDevicesArrayAdapter.notifyDataSetChanged();
	            		}
	            	}
					if(containItemOnList(mPairedDevicesArrayList, device.getAddress()) != -1){

						continue;
					}
 	            	BluetoothListItem holder = new BluetoothListItem();
	            	holder.setAddress(device.getAddress());
	            	String changeName = mChangeNameMap.get(device.getAddress()); 
	            	if(changeName != null) holder.setName(changeName);
	            	else holder.setName(device.getName());
	            	holder.setPaired(true);

	            	mPairedDevicesArrayList.add(holder);
	            	mPairedDevicesArrayAdapter.notifyDataSetChanged();
				}
				
		    	BluetoothListViewAdapter listAdapter = (BluetoothListViewAdapter) mPairedDevicesListView.getAdapter();
		        if (listAdapter == null) return false;
		        int totalHeight = 0, count = mPairedDevicesArrayList.size();
		        int desiredWidth = MeasureSpec.makeMeasureSpec(mPairedDevicesListView.getWidth(), MeasureSpec.AT_MOST);
		        if(count < 1) return false;
		        if(count > 4) count = 4;
		        for (int i = 0; i < count; i++) {
		            View listItem = listAdapter.getView(i, null, mPairedDevicesListView);
		            listItem.measure(desiredWidth, MeasureSpec.UNSPECIFIED);
		            totalHeight += listItem.getMeasuredHeight()+6;            
		        }
		        ViewGroup.LayoutParams params = mPairedDevicesListView.getLayoutParams();
		        params.height = totalHeight;
		        mPairedDevicesListView.setLayoutParams(params);
		        mPairedDevicesListView.requestLayout();
		        
				mTvPairedDeviceTitle.setVisibility(View.VISIBLE);
				mPairedDevicesListView.setVisibility(View.VISIBLE);
				setPairedDeviceState();
				return true;
			} else {
				mTvPairedDeviceTitle.setVisibility(View.GONE);
				mPairedDevicesListView.setVisibility(View.GONE);
			}
		} else {
			mTvPairedDeviceTitle.setVisibility(View.GONE);
		}
		return false;
    }

	private boolean containItemOnList(ArrayList<BluetoothListItem> list, BluetoothListItem item) {
		return list.indexOf(item) > -1;
	}
	
	private int containItemOnList(ArrayList<BluetoothListItem> list, String address) {
		int i = 0;
		for(BluetoothListItem item : list) {
			if(item.getAddress().equals(address)) {
				return i;
			}
			++i;
		}
		return -1;
	}
	
	@Override
	public void setScrollView(ScrollView v) {
		mScrollView = v;
	}
	
	@Override
	public void onResume() {
		mChangeNameMap = DeviceNameSharePreference.getChangeDevName(mContext);
	}
	
	@Override
	public void onPause() {
		DeviceNameSharePreference.commintChangeDevName(mContext, mChangeNameMap);
		if(mInputMethodManager != null && mPairedDialog != null) {
			mInputMethodManager.hideSoftInputFromWindow(mPairedDialog.getCurrentFocus().getWindowToken(), 0);
		}
	}
	
	private BluetoothListItem mPairedChangeItem;
	@Override
	public void onListBtnClick(int position) {
		mPairedChangeItem = mPairedDevicesArrayList.get(position);
		mPairedDialog = new PairedSettingDialog(mContext, mPairedChangeItem, mHandler, mInputMethodManager);
		mPairedDialog.show();
	}

	
    public static final int MESSAGE_STATE_CHANGE = 10;
    public static final int MESSAGE_DEVICE_NAME_CHANGE = 11;
    public static final int MESSAGE_PAIRING_DISCONNECT = 12;
    private final Handler mHandler = new Handler() {
        @Override public void handleMessage(Message msg) {
            switch (msg.what) {
            case MESSAGE_STATE_CHANGE:
            	int state = msg.arg1;
                Log.i(TAG, "MESSAGE_STATE_CHANGE: " + msg.arg1);
                mSelectItem.setState(state);
                switch (state) {
				case BluetoothDeviceConnect.STATE_A2DP_CONNECT_FAIL:
					doPairedDeviceRefresh();
					Toast.makeText(mContext, mContext.getString(R.string.bluetooth_connect_fail), Toast.LENGTH_LONG).show();
					break;
				case BluetoothDeviceConnect.STATE_CONNECT_FAIL:
					Toast.makeText(mContext, mContext.getString(R.string.bluetooth_connect_fail), Toast.LENGTH_LONG).show();
					break;
				default:
					if(!mSelectItem.isPaired()) {
						if(containItemOnList(mNewDevicesArrayList, mSelectItem)) {
							if(state == BluetoothDeviceConnect.STATE_PAIRED) {
								mNewDevicesArrayList.remove(mSelectItem);
							}else {
								mNewDevicesArrayList.set(mNewDevicesArrayList.indexOf(mSelectItem), mSelectItem);
							}
						}
					}else {
						if(containItemOnList(mPairedDevicesArrayList, mSelectItem)) {
							mPairedDevicesArrayList.set(mPairedDevicesArrayList.indexOf(mSelectItem), mSelectItem);
						}
					}
					break;
				}
                mNewDevicesArrayAdapter.notifyDataSetChanged();
                mPairedDevicesArrayAdapter.notifyDataSetChanged();
                break;
            case MESSAGE_DEVICE_NAME_CHANGE:
            	int position = mPairedDevicesArrayList.indexOf(mPairedChangeItem);
            	mChangeNameMap.put(mPairedChangeItem.getAddress(), msg.obj.toString());
            	mPairedChangeItem.setName(msg.obj.toString());
            	mPairedDevicesArrayList.set(position, mPairedChangeItem);
				mPairedDevicesArrayAdapter.notifyDataSetChanged();
            	break;
            case MESSAGE_PAIRING_DISCONNECT:
            	BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(mPairedChangeItem.getAddress());
            	if(containItemOnList(mPairedDevicesArrayList, mPairedChangeItem));
    				mPairedDevicesArrayList.remove(mPairedChangeItem);
            	mChangeNameMap.remove(device.getAddress());
            	device.removeBond();
            	break;
            }
        }
    };
}
