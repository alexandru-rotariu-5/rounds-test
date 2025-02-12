package ai.rounds.speedmeter.services;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.service.notification.StatusBarNotification;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import ai.rounds.speedmeter.R;
import ai.rounds.speedmeter.utils.PermissionHelper;
import ai.rounds.speedmeter.repo.TrackerRepo;
import ai.rounds.speedmeter.ui.speed.SpeedViewActivity;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * Service dedicated to navigation related data retrieving and calculation
 */
public class SpeedTrackingService extends Service implements com.google.android.gms.location.LocationListener,
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private static final String CHANNEL_ID = "SPEEDMETER_CHANNEL_ID";
    private static final String CHANNEL_NAME = "SPEEDMETER";
    private static final int NOTIFICATION_ID = 1337;

    private NotificationManager notificationManager;

    public static final String EXTRA_PROVIDER = "ai.rounds.speedmeter.SpeedTrackingService.extra_provider";
    public static final String EXTRA_SESSION_ID = "ai.rounds.speedmeter.SpeedTrackingService.extra_session_id";

    public static final String EXTRA_SPEED = "ai.rounds.speedmeter.SpeedTrackingService.extra_speed";

    public static final String INTENT_ACTION_SPEED_UPDATE = "ai.rounds.speedmeter.SpeedTrackingService.speed_update";

    public static final String INTENT_ACTION_STOP_MOVING = "ai.rounds.speedmeter.SpeedTrackingService.stop_moving";

    private static final int ACCURACY_DELTA_MAX = 200;

    private static final long TIME_INTERVAL_FASTEST = TimeUnit.SECONDS.toMillis(1);// 1 second

    private static final long TIME_INTERVAL_BASE = TimeUnit.SECONDS.toMillis(3);// 3 seconds

    private static final long TIME_DELTA_MAX = TimeUnit.MINUTES.toMillis(2);// 2 minutes

    private static final int TIME_DELTA_MIN = 600;// 600 millisecondes

    private GoogleApiClient googleApiClient;

    private Location lastLocation;

    public SpeedTrackingService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.i("dev", "SpeedTrackingService started.");

        if (PermissionHelper.hasLocationPermission(this)) {
            notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            buildGoogleApiClient();
            googleApiClient.connect();
        }
    }

    @Override
    public void onDestroy() {
        Log.i("dev", "SpeedTrackingService stopped.");

        if (googleApiClient.isConnected()) {
            googleApiClient.disconnect();
        }

        String sessionId = TrackerRepo.getCurrentSessionId();

        if (TrackerRepo.isInitialized()) {
            TrackerRepo.finalizeSession();
        }

        Intent stopIntent = new Intent(INTENT_ACTION_STOP_MOVING);
        stopIntent.putExtra(EXTRA_SESSION_ID, sessionId);
        sendBroadcast(stopIntent);

        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Notification trackingLocation = createNotification();
        startForeground(NOTIFICATION_ID, trackingLocation);
        startLocationUpdates();
    }

    @Override
    public void onConnectionSuspended(int i) {
        stopLocationUpdates();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Notification notif = new NotificationCompat.Builder(getBaseContext(), CHANNEL_ID)
                .setContentTitle(getString(R.string.error))
                .setContentText(getString(R.string.connection_error_message))
                .build();

        ((NotificationManager) getSystemService(NOTIFICATION_SERVICE)).notify(1, notif);
        stopSelf();
    }

    @Override
    public void onLocationChanged(@NonNull Location location) {
        if (isBetterLocation(location, lastLocation)) {
            if (lastLocation != null) {
                float speed = location.getSpeed();

                TrackerRepo.addDistance(location.distanceTo(lastLocation));
                TrackerRepo.addSpeed(speed);

                Intent speedUpdateIntent = new Intent(INTENT_ACTION_SPEED_UPDATE);
                speedUpdateIntent.putExtra(EXTRA_SPEED, speed);
                speedUpdateIntent.putExtra(EXTRA_PROVIDER, location.getProvider() + " " + Math.round(location.getSpeed()));
                sendBroadcast(speedUpdateIntent);
            }

            lastLocation = location;
        }
    }

    private synchronized void buildGoogleApiClient() {
        googleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
    }

    private Notification createNotification() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_LOW);
            notificationManager.createNotificationChannel(channel);
        }

        Intent notificationIntent = new Intent(this, SpeedViewActivity.class);
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_IMMUTABLE);

        return new NotificationCompat.Builder(getBaseContext(), CHANNEL_ID)
                .setContentIntent(contentIntent)
                .setSmallIcon(R.drawable.ic_satellite)
                .setContentTitle(getString(R.string.channel_description))
                .setContentText(getString(R.string.tracking_location_notification))
                .build();
    }

    @SuppressLint("MissingPermission")
    private void startLocationUpdates() {
        // Building the location request
        LocationRequest locationRequest = new LocationRequest();
        locationRequest.setInterval(TIME_INTERVAL_BASE);
        locationRequest.setFastestInterval(TIME_INTERVAL_FASTEST);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        // Requesting location updates
        LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, locationRequest, this);

        // Starting a new tracking session
        TrackerRepo.initializeSession(this);
    }

    private void stopLocationUpdates() {
        // Stopping location updates
        LocationServices.FusedLocationApi.removeLocationUpdates(googleApiClient, this);
    }

    /**
     * Determines whether a location is better than an older one or not by comparing:
     *  - time delta of (new) location vs (old) currentBestLocation
     *  - accuracy delta of (new) location vs (old) currentBestLocation
     *  - provider of (new) location vs (old) currentBestLocation
     *
     * @param location            the newer location
     * @param currentBestLocation the older location
     * @return True if the newer location is the better one.
     */
    private boolean isBetterLocation(Location location, Location currentBestLocation) {
        if (currentBestLocation == null) return true;

        long timeDelta = location.getTime() - currentBestLocation.getTime();

        // Determining the time delta based conditions
        boolean isTooOldOrTooRecent = timeDelta < -TIME_DELTA_MAX || timeDelta < TIME_DELTA_MIN;
        boolean isNewerEnough = timeDelta > TIME_DELTA_MAX;
        boolean isNewer = timeDelta > 0;

        if (isNewerEnough) {
            return true;
        } else if (isTooOldOrTooRecent) {
            return false;
        }

        int accuracyDelta = (int) (location.getAccuracy() - currentBestLocation.getAccuracy());

        // Determining the accuracy delta based conditions
        boolean isLessAccurate = accuracyDelta > 0;
        boolean isMoreAccurate = accuracyDelta < 0;
        boolean isSignificantlyLessAccurate = accuracyDelta > ACCURACY_DELTA_MAX;

        boolean sameProvider = areFromSameProvider(location.getProvider(), currentBestLocation.getProvider());

        if (isMoreAccurate || (isNewer && !isLessAccurate)) {
            return true;
        } else if (isNewer && !isSignificantlyLessAccurate && sameProvider) {
            return true;
        }

        return false;
    }

    private boolean areFromSameProvider(String provider1, String provider2) {
        return Objects.equals(provider1, provider2);
    }

    public static boolean isRunning(Context context) {
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(NOTIFICATION_SERVICE);
        StatusBarNotification[] notifications = notificationManager.getActiveNotifications();
        for (StatusBarNotification notification : notifications) {
            if (notification.getId() == NOTIFICATION_ID) {
                return true;
            }
        }
        return false;
    }
}
