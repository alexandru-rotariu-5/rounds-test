package ai.rounds.speedmeter.ui.speed;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import ai.rounds.speedmeter.R;
import ai.rounds.speedmeter.utils.Formatter;
import ai.rounds.speedmeter.utils.PermissionHelper;
import ai.rounds.speedmeter.services.SpeedTrackingService;
import ai.rounds.speedmeter.ui.stats.TripStatsActivity;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class SpeedViewFragment extends Fragment {

    public static final int LOCATION_PERMISSION_REQUEST_ID = 0x56;
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
            getActivity().registerReceiver(speedTrackingReceiver, new IntentFilter(SpeedTrackingService.INTENT_ACTION_SPEED_UPDATE));
            getActivity().registerReceiver(speedTrackingReceiver, new IntentFilter(SpeedTrackingService.INTENT_ACTION_STOP_MOVING));
        }
        if (SpeedTrackingService.isRunning(getActivity())) {
            toggleTrackingBtn.setImageResource(R.drawable.icon_stop);
        } else {
            toggleTrackingBtn.setImageResource(R.drawable.icon_play);
            speedTextView.setText(R.string.stop);
        }
    }

    @Override
    public void onDestroy() {
        if (speedTrackingReceiver != null) {
            getActivity().unregisterReceiver(speedTrackingReceiver);
        }

        super.onDestroy();
    }

    private void init() {

        locationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);

        toggleTrackingBtn = rootView.findViewById(R.id.fba_toggle_tracking);

        toggleTrackingBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (PermissionHelper.hasLocationPermission(getActivity())) {
                    if (isGPSEnabled()) {
                        if (!SpeedTrackingService.isRunning(getActivity())) {
                            startTracking();
                        } else {
                            stopTracking();
                        }
                    } else {
                        showGPSMandatoryAlert();
                    }
                } else {
                    showLocationPermissionExplanation();
                }
            }
        });

        speedTextView = rootView.findViewById(R.id.txtv_speed);
        speedMeterView = rootView.findViewById(R.id.smv_speedmeterview);
        speedTrackingReceiver = new SpeedTrackingBroadcastReceiver();
    }

    private void startTracking() {
        Intent serviceIntent = new Intent(getActivity().getApplicationContext(), SpeedTrackingService.class);
        ContextCompat.startForegroundService(getActivity().getApplicationContext(), serviceIntent);
        speedTextView.setText(R.string.enabling);
        toggleTrackingBtn.setImageResource(R.drawable.icon_stop);
    }

    private void stopTracking() {
        Intent serviceIntent = new Intent(getActivity().getApplicationContext(), SpeedTrackingService.class);
        getActivity().getApplicationContext().stopService(serviceIntent);
        speedTextView.setText(R.string.stop);
        toggleTrackingBtn.setImageResource(R.drawable.icon_play);
    }

    private void showLocationPermissionExplanation() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        builder.setTitle(R.string.location_explanation_title)
                .setMessage(R.string.location_explanation_msg)
                .setNeutralButton(R.string.understood, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        PermissionHelper.requestLocationPermission(getActivity(), LOCATION_PERMISSION_REQUEST_ID);
                    }
                })
                .show();
    }

    private void showGPSMandatoryAlert() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        builder.setTitle(R.string.gps_mandatory_title)
                .setMessage(R.string.gps_mandatory_msg)
                .setNeutralButton(R.string.understood, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
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

            if (isGPSEnabled()) {
                startTracking();
            } else {
                showGPSMandatoryAlert();
            }
        }
    }

    private class SpeedTrackingBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();

            switch (action) {
                case SpeedTrackingService.INTENT_ACTION_SPEED_UPDATE:
                    float speed = intent.getFloatExtra(SpeedTrackingService.EXTRA_SPEED, 0f);
                    speedTextView.setText(Formatter.getKilometersPerHour(speed));
                    speedMeterView.updateSpeed(speed * 3600 / 1000);
                    break;

                case SpeedTrackingService.INTENT_ACTION_STOP_MOVING:
                    speedTextView.setText(R.string.stop);
                    speedMeterView.updateSpeed(0f);

                    toggleTrackingBtn.setImageResource(R.drawable.icon_play);

                    Intent summmaryIntent = new Intent(getActivity(), TripStatsActivity.class);
                    summmaryIntent.putExtra(TripStatsActivity.EXTRA_SESSION_ID, intent.getStringExtra(SpeedTrackingService.EXTRA_SESSION_ID));
                    startActivity(summmaryIntent);
                    break;
            }
        }
    }
}
