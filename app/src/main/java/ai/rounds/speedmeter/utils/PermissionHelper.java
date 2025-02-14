package ai.rounds.speedmeter.utils;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.util.Log;

import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

/**
 * Clas helper simplifying permission checking and requesting
 */
public class PermissionHelper {

    public static void requestLocationPermission(Activity activity, int requestID) {
        Log.d("=====", "Requesting location permission");
        ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, requestID);
    }

    public static boolean hasLocationPermission(Context context) {
        return ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED;
    }

    /**
     * Request Notification Permission (requires Android 13 or above)
     */
    @RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
    public static void requestNotificationPermission(Activity activity, int requestID) {
        ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.POST_NOTIFICATIONS}, requestID);
    }

    /**
     * Check if Notification Permission is granted (requires Android 13 or above)
     */
    @RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
    public static boolean hasNotificationPermission(Context context) {
        return ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS)
                == PackageManager.PERMISSION_GRANTED;
    }
}
