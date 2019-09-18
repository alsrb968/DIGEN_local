package com.litbig.setting.system;

import java.io.File;
import java.io.IOException;

import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiManager;
import android.os.Handler;
//import android.os.IQBService;
import android.os.Message;
import android.os.PowerManager;
import android.os.RecoverySystem;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemProperties;
import android.util.AttributeSet;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.litbig.app.setting.R;
import com.litbig.setting.FileUtil;
import com.litbig.setting.MsgDialog;
//import com.litbig.setting.eng.AliveCheckSwitchView;
//import com.litbig.setting.eng.EngActivity;

public class SystemLayout extends RelativeLayout{

	private final String FILE_PATH_SERIAL_NUMBER = "/security/.ivi_sn";
	
	private Context mContext;
	
	private TextView mTvModelName, mTvFwVersion, mTvSerialNumber, mTvMcuVersion, mTvCasId;
	private TextView mTvUpdate, mTvReboot, mTvReset;
	
	private boolean mSDUpdate = false;
	private MsgDialog mMsgDialog;
	
	private boolean mEnableEngMode;
	private int mEnterEngStep;
	
	private int mDlgMode = -1;
	
	public static final int DLG_MODE_UPDATE = 0;
	public static final int DLG_MODE_RESET 	= 1;
	public static final int DLG_MODE_REBOOT	= 2;
	
	public SystemLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
	
		mContext = context;
		
		mEnterEngStep = 5;
		mEnableEngMode = false;
	}
	
	@Override
	protected void onAttachedToWindow() {
		super.onAttachedToWindow();
		
		mTvModelName 	= findViewById(R.id.tv_model_name);
		mTvFwVersion	= findViewById(R.id.tv_fw_version);
		mTvSerialNumber = findViewById(R.id.tv_serial_number);
		mTvMcuVersion	= findViewById(R.id.tv_mcu_version);
		mTvCasId		= findViewById(R.id.tv_cas_id);
		
		mTvFwVersion.setText("v " + SystemProperties.get("persist.sys.fw.version", "N/A"));
		mTvMcuVersion.setText("v " + SystemProperties.get("persist.sys.mcu.version", "N/A"));
		mTvCasId.setText(SystemProperties.get("persist.sys.cas.id", "N/A"));
		
		String serialNumber = FileUtil.readFile(new File(FILE_PATH_SERIAL_NUMBER));
		
		if (serialNumber == null)
			serialNumber = "N/A";
		
		mTvSerialNumber.setText(serialNumber);
		
		mTvUpdate	= findViewById(R.id.btn_tb_update);
		mTvReboot	= findViewById(R.id.btn_tb_reboot);
		mTvReset	= findViewById(R.id.btn_tb_reset);
		
		BtnClickListener btnClickListener = new BtnClickListener();
		mTvUpdate.setOnClickListener(btnClickListener);
		mTvReboot.setOnClickListener(btnClickListener);
		mTvReset.setOnClickListener(btnClickListener);
		
		ViewLongClickListener viewLongClickListener = new ViewLongClickListener();
		mTvModelName.setOnLongClickListener(viewLongClickListener);
		mTvModelName.setOnClickListener(btnClickListener);
	}
	
	public class ViewLongClickListener implements View.OnLongClickListener {

		@Override
		public boolean onLongClick(View view) {
			// TODO Auto-generated method stub
			if (view == mTvModelName) {
				if (!mEnableEngMode) {
					mEnableEngMode = true;

					Toast.makeText(mContext, R.string.toast_enabled_eng_mode, Toast.LENGTH_SHORT).show();
				}	
			}
			
			return false;
		}
		
	}
	
	public class BtnClickListener implements View.OnClickListener {

		@Override
		public void onClick(View view) {
			
			if (view == mTvUpdate) {	
				if (checkUpdateFile()) {
					showUpdateDialog();
				} else {

					Toast.makeText(mContext, R.string.toast_no_update_file, Toast.LENGTH_SHORT).show();
				}
			}
			
			else if (view == mTvReboot) {
				showRebootDialog();
			}
			
			else if (view == mTvReset) {
				showResetDialog();
			}
			
			else if (view == mTvModelName) {
				if (mEnableEngMode) {
					if (mEnterEngStep > 0) {						
						mEnterEngStep--;
					} else {
						startEngMode();
					}
				}
			}
		}
	}
	
	private boolean checkUpdateFile() {		
		File file = new File("/storage/sdcard1/update.zip");

		mSDUpdate = file.exists();
		
		// check update file in usb device
		file = new File("storage/usb0/update.zip");
		if (file.exists()) {
		}
		
		return mSDUpdate;
	}
	
	private void installUpdateFile(String filePath) {
//
//		File file = new File(filePath);
//
//		final IQBService qb = IQBService.Stub.asInterface(ServiceManager.checkService("quickboot"));
//
//		try {
//			qb.doHibernateImageErase();
//		} catch (RemoteException e) {
//			e.printStackTrace();
//		}
//
//		try {
//			RecoverySystem.installPackage(mContext, file);
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
	}
	
	private void rebootWipeUserData() {
		mContext.sendBroadcast(new Intent("android.intent.action.MASTER_CLEAR"));
	}
	
	private void rebootSystem() {
		PowerManager pm = (PowerManager)mContext.getSystemService(Context.POWER_SERVICE);
		pm.reboot(null);
	}
	
	private void showUpdateDialog() {
		if (mSDUpdate) {
			mDlgMode = DLG_MODE_UPDATE;
			mMsgDialog = new MsgDialog(mContext, mContext.getString(R.string.dlg_update_title), mContext.getString(R.string.dlg_update_msg), mHandler);
			mMsgDialog.show();
		} else {
			Toast.makeText(mContext, R.string.toast_no_update_file, Toast.LENGTH_SHORT).show();
		}
	}
	
	private void showResetDialog() {
		mDlgMode = DLG_MODE_RESET;
		mMsgDialog = new MsgDialog(mContext, mContext.getString(R.string.dlg_reset_title), mContext.getString(R.string.dlg_reset_msg), mHandler);
		mMsgDialog.show();
	}
	
	private void showRebootDialog() {
		mDlgMode = DLG_MODE_REBOOT;
		mMsgDialog = new MsgDialog(mContext, mContext.getString(R.string.dlg_reboot_title), mContext.getString(R.string.dlg_reboot_msg), mHandler);
		mMsgDialog.show();
	}
	
	private void startEngMode() {
//		Intent intent = new Intent(mContext, EngActivity.class);
//		mContext.startActivity(intent);
	}
	
	private Handler mHandler = new Handler () {
		@Override
		public void handleMessage(Message msg) {
			switch(msg.what) {
			case MsgDialog.MSG_EVENT_OK:
				
				if (mDlgMode == DLG_MODE_UPDATE) {
					installUpdateFile("/sdcard/update.zip");
				}
				
				else if (mDlgMode == DLG_MODE_RESET) {
//					AliveCheckSwitchView.changeAliveCheckFlag(mContext, "mcu", false);
					rebootWipeUserData();
				}
					
				else if (mDlgMode == DLG_MODE_REBOOT) {
					rebootSystem();
				}
				break;
			case MsgDialog.MSG_EVENT_CANCEL:
				break;
			}
			mDlgMode = -1;
		}
	};
}
