package com.litbig.app.launcher;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.support.v4.content.AsyncTaskLoader;

import android.os.SystemProperties;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Comparator;

/**
 * @credit http://developer.android.com/reference/android/content/AsyncTaskLoader.html
 */
public class AppsLoader extends AsyncTaskLoader<ArrayList<AppModel>> {
    private ArrayList<AppModel> mInstalledApps;

    private final PackageManager mPm;
    private PackageIntentReceiver mPackageObserver;

    AppsLoader(Context context) {
        super(context);

        mPm = context.getPackageManager();
    }

    @Override
    public ArrayList<AppModel> loadInBackground() {
        // retrieve the list of installed applications
        List<ApplicationInfo> apps = mPm.getInstalledApplications(0);

        if (apps == null) {
            apps = new ArrayList<>();
        }

        final Context context = getContext();

        // create corresponding apps and load their labels
        ArrayList<AppModel> items = new ArrayList<>(apps.size());
        for (int i = 0; i < apps.size(); i++) {
//        	if (isSystemApp(apps.get(i)) == false) {
//        		String pkg = apps.get(i).packageName;
//
//        		// only apps which are launchable
//        		if (context.getPackageManager().getLaunchIntentForPackage(pkg) != null && !pkg.contains("litbig") && !pkg.contains("wfd")) {
//        			AppModel app = new AppModel(context, apps.get(i));
//        			app.loadLabel(context);
//        			items.add(app);
//        		}
//        	}
        	//+++++[jacob]+++++
        	String pkg = apps.get(i).packageName;
        	if (context.getPackageManager().getLaunchIntentForPackage(pkg) != null && pkg.contains("litbig") && !pkg.contains("launcher")) {
    			AppModel app = new AppModel(context, apps.get(i));
    			app.loadLabel(context);
    			items.add(app);
    		}
        	//+++++[jacob]+++++
        }

        // sort the list
        Collections.sort(items, ALPHA_COMPARATOR);

        return items;
    }
    
    public boolean isSystemApp(ApplicationInfo info) {
    	if (info.packageName.equals("com.android.browser")||info.packageName.equals("com.android.youtube"))
    		return false;
    	
    	if (SystemProperties.get("persist.sys.mode", "").equals("eng"))
    		return false;
    	
    	if ((info.flags & ApplicationInfo.FLAG_SYSTEM) == 0) {
    		if (info.installLocation == PackageInfo.INSTALL_LOCATION_PREFER_EXTERNAL ||
    				info.installLocation == PackageInfo.INSTALL_LOCATION_UNSPECIFIED) {
    			return false;
    		}
    	}
    	
    	return true;
    }

    @Override
    public void deliverResult(ArrayList<AppModel> apps) {
        if (isReset()) {
            // An async query came in while the loader is stopped.  We
            // don't need the result.
            if (apps != null) {
                onReleaseResources(apps);
            }
        }

        mInstalledApps = apps;

        if (isStarted()) {
            // If the Loader is currently started, we can immediately
            // deliver its results.
            super.deliverResult(apps);
        }

        // At this point we can release the resources associated with
        // 'oldApps' if needed; now that the new result is delivered we
        // know that it is no longer in use.
        if (apps != null) {
            onReleaseResources(apps);
        }
    }

    @Override
    protected void onStartLoading() {
        if (mInstalledApps != null) {
            // If we currently have a result available, deliver it
            // immediately.
            deliverResult(mInstalledApps);
        }

        // watch for changes in app install and uninstall operation
        if (mPackageObserver == null) {
            mPackageObserver = new PackageIntentReceiver(this);
        }

        if (takeContentChanged() || mInstalledApps == null ) {
            // If the data has changed since the last time it was loaded
            // or is not currently available, start a load.
            forceLoad();
        }
    }

    @Override
    protected void onStopLoading() {
        // Attempt to cancel the current load task if possible.
        cancelLoad();
    }

    @Override
    public void onCanceled(ArrayList<AppModel> apps) {
        super.onCanceled(apps);

        // At this point we can release the resources associated with 'apps'
        // if needed.
        onReleaseResources(apps);
    }

    @Override
    protected void onReset() {
        // Ensure the loader is stopped
        onStopLoading();

        // At this point we can release the resources associated with 'apps'
        // if needed.
        if (mInstalledApps != null) {
            onReleaseResources(mInstalledApps);
            mInstalledApps = null;
        }

        // Stop monitoring for changes.
        if (mPackageObserver != null) {
            getContext().unregisterReceiver(mPackageObserver);
            mPackageObserver = null;
        }
    }

    private void onReleaseResources(ArrayList<AppModel> apps) {
        // do nothing
    }


    /**
     * Perform alphabetical comparison of application entry objects.
     */
    private static final Comparator<AppModel> ALPHA_COMPARATOR = new Comparator<AppModel>() {
        private final Collator sCollator = Collator.getInstance();
        @Override
        public int compare(AppModel object1, AppModel object2) {
            return sCollator.compare(object2.getLabel(), object1.getLabel());
        }
    };
}
