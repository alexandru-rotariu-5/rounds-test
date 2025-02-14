package ai.rounds.speedmeter.ui.speed;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.Objects;

import ai.rounds.speedmeter.R;
import ai.rounds.speedmeter.services.SpeedTrackingService;
import ai.rounds.speedmeter.ui.stats.TripStatsActivity;
import ai.rounds.speedmeter.utils.Formatter;
import ai.rounds.speedmeter.utils.PermissionHelper;

public class SpeedViewFragment extends Fragment {

    public static final int LOCATION_PERMISSION_REQUEST_ID = 0x56;
    public static final int NOTIFICATION_PERMISSION_REQUEST_ID = 0x57;
    private FloatingActionButton toggleTrackingBtn;
    private LocationManager locationManager;
    private SpeedMeterView speedMeterView;
    private SpeedTrackingBroadcastReceiver speedTrackingReceiver;

    private TextView speedTextView;

    private View rootView;

    public SpeedViewFragment() {
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_monitoring, container, false);
        init();
        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (speedTrackingReceiver != null) {
            ContextCompat.registerReceiver(requireActivity(), speedTrackingReceiver, new IntentFilter(SpeedTrackingService.INTENT_ACTION_SPEED_UPDATE), ContextCompat.RECEIVER_NOT_EXPORTED);
            ContextCompat.registerReceiver(requireActivity(), speedTrackingReceiver, new IntentFilter(SpeedTrackingService.INTENT_ACTION_STOP_MOVING), ContextCompat.RECEIVER_NOT_EXPORTED);
            Log.d("=====", "BroadcastReceiver registered");
        }
        if (SpeedTrackingService.isRunning(requireActivity())) {
            toggleTrackingBtn.setImageResource(R.drawable.icon_stop);
        } else {
            toggleTrackingBtn.setImageResource(R.drawable.icon_play);
            speedTextView.setText(R.string.stop);
        }
    }

    @Override
    public void onDestroy() {
        if (speedTrackingReceiver != null) {
            requireActivity().unregisterReceiver(speedTrackingReceiver);
        }

        super.onDestroy();
    }

    private void init() {

        locationManager = (LocationManager) requireActivity().getSystemService(Context.LOCATION_SERVICE);

        speedTextView = rootView.findViewById(R.id.txtv_speed);
        speedMeterView = rootView.findViewById(R.id.smv_speedmeterview);
        speedTrackingReceiver = new SpeedTrackingBroadcastReceiver();

        toggleTrackingBtn = rootView.findViewById(R.id.fba_toggle_tracking);

        toggleTrackingBtn.setOnClickListener(v -> {
            tryToggleTracking();
        });
    }

    private void tryToggleTracking() {
        // Check for location permission
        if (!PermissionHelper.hasLocationPermission(requireActivity())) {
            Log.d("=====", "No location permission. Showing explanation.");
            showLocationPermissionExplanation();
            return;
        }

        Log.d("=====", "Location permission granted.");

        // If Android version is >= TIRAMISU, check for notification permission
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (!PermissionHelper.hasNotificationPermission(requireActivity())) {
                Log.d("=====", "No notification permission. Showing explanation.");
                showNotificationPermissionExplanation();
                return;
            }
            Log.d("=====", "Has notification permission");
        } else {
            // No need to check for notification permission on versions below TIRAMISU
            Log.d("=====", "Notification permission check skipped (below TIRAMISU)");
        }

        // Check if GPS is enabled
        if (!isGPSEnabled()) {
            Log.d("=====", "GPS not enabled. Showing alert.");
            showGPSMandatoryAlert();
            return;
        }

        Log.d("=====", "GPS enabled.");

        // Toggle speed tracking service based on its current state
        if (!SpeedTrackingService.isRunning(requireActivity())) {
            Log.d("=====", "Speed tracking is not running");
            startTracking();
        } else {
            Log.d("=====", "Speed tracking is running");
            stopTracking();
        }
    }


    private void startTracking() {
        Log.d("=====", "Starting tracking");
        Intent serviceIntent = new Intent(requireActivity().getApplicationContext(), SpeedTrackingService.class);
        ContextCompat.startForegroundService(requireActivity().getApplicationContext(), serviceIntent);
        speedTextView.setText(R.string.enabling);
        toggleTrackingBtn.setImageResource(R.drawable.icon_stop);

        Log.d("=====", "SpeedTrackingService started");
    }

    private void stopTracking() {
        Log.d("=====", "Stopping tracking");
        Intent serviceIntent = new Intent(requireActivity().getApplicationContext(), SpeedTrackingService.class);
        requireActivity().getApplicationContext().stopService(serviceIntent);
        speedTextView.setText(R.string.stop);
        toggleTrackingBtn.setImageResource(R.drawable.icon_play);
    }

    private void showLocationPermissionExplanation() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());

        builder.setTitle(R.string.location_explanation_title)
                .setMessage(R.string.location_explanation_msg)
                .setNeutralButton(R.string.understood, (dialog, which) -> PermissionHelper.requestLocationPermission(requireActivity(), LOCATION_PERMISSION_REQUEST_ID))
                .show();
    }

    private void showNotificationPermissionExplanation() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            builder.setTitle(R.string.notification_explanation_title)
                    .setMessage(R.string.notification_explanation_msg)
                    .setNeutralButton(R.string.understood, (dialog, which) -> PermissionHelper.requestNotificationPermission(requireActivity(), NOTIFICATION_PERMISSION_REQUEST_ID))
                    .show();
        }
    }

    private void showGPSMandatoryAlert() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());

        builder.setTitle(R.string.gps_mandatory_title)
                .setMessage(R.string.gps_mandatory_msg)
                .setNeutralButton(R.string.understood, (dialog, which) -> dialog.dismiss())
                .show();
    }

    private boolean isGPSEnabled() {
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == LOCATION_PERMISSION_REQUEST_ID
                && grantResults.length > 0
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

            tryToggleTracking();
        }
        if (requestCode == NOTIFICATION_PERMISSION_REQUEST_ID
                && grantResults.length > 0
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            tryToggleTracking();
        }
    }

    private class SpeedTrackingBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            Log.d("=====", "Received action: " + action);

            switch (Objects.requireNonNull(action)) {
                case SpeedTrackingService.INTENT_ACTION_SPEED_UPDATE:
                    float speed = intent.getFloatExtra(SpeedTrackingService.EXTRA_SPEED, 0f);
                    speedTextView.setText(Formatter.getKilometersPerHour(speed));
                    speedMeterView.updateSpeed(speed * 3600 / 1000);
                    Log.d("=====", "SPEED UPDATE: " + speed);
                    break;

                case SpeedTrackingService.INTENT_ACTION_STOP_MOVING:
                    Log.d("=====", "STOP MOVING RECEIVED");
                    speedTextView.setText(R.string.stop);
                    speedMeterView.updateSpeed(0f);

                    toggleTrackingBtn.setImageResource(R.drawable.icon_play);

                    Intent summmaryIntent = new Intent(requireActivity(), TripStatsActivity.class);
                    summmaryIntent.putExtra(TripStatsActivity.EXTRA_SESSION_ID, intent.getStringExtra(SpeedTrackingService.EXTRA_SESSION_ID));
                    startActivity(summmaryIntent);
                    break;
            }
        }
    }
}
