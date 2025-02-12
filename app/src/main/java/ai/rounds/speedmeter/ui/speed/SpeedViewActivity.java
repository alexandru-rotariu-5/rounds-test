package ai.rounds.speedmeter.ui.speed;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import ai.rounds.speedmeter.R;

public class SpeedViewActivity extends AppCompatActivity {

    private SpeedViewFragment speedViewFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_simple_fragment);
        speedViewFragment = new SpeedViewFragment();

        getSupportFragmentManager()
                .beginTransaction()
                .add(R.id.fragment_container, speedViewFragment)
                .commitAllowingStateLoss();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        speedViewFragment.onRequestPermissionsResult(requestCode, permissions, grantResults);
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }
}
