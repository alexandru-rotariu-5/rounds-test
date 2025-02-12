package ai.rounds.speedmeter.ui.stats;

import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;

import ai.rounds.speedmeter.R;

/**
 * Tracking session summary activity
 */
public class TripStatsActivity extends AppCompatActivity {

    /**
     * Key to get the session id from extras
     */
    public static final String EXTRA_SESSION_ID = "ai.rounds.speedmeter.TripStatsActivity.extra_session_id";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_simple_fragment);
        Toolbar toolbar = findViewById(R.id.tb_main_toolbar);

        toolbar.setVisibility(View.VISIBLE);
        toolbar.setTitle(R.string.summary_title);
        toolbar.setNavigationIcon(ContextCompat.getDrawable(this, R.drawable.ic_clear));
        toolbar.setNavigationOnClickListener(view -> onBackPressed());

        getSupportFragmentManager()
                .beginTransaction()
                .add(R.id.fragment_container, new TripStatsFragment())
                .commitAllowingStateLoss();
    }
}
