package ai.rounds.speedmeter.ui.stats;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import ai.rounds.speedmeter.R;
import ai.rounds.speedmeter.db.access.SessionAccess;
import ai.rounds.speedmeter.utils.Formatter;
import ai.rounds.speedmeter.models.Session;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Tracking session summary fragment
 */
public class TripStatsFragment extends Fragment {

    private View mRootView;

    public TripStatsFragment() {
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mRootView = inflater.inflate(R.layout.fragment_summary, container, false);
        init();
        return mRootView;
    }

    private void init() {

        Session session;
        SessionAccess access = new SessionAccess(getActivity());
        access.openToRead();

        if (getActivity().getIntent().hasExtra(TripStatsActivity.EXTRA_SESSION_ID)) {
            session = access.getTrackingSessionById(getActivity().getIntent().getStringExtra(TripStatsActivity.EXTRA_SESSION_ID));
        } else {
            session = access.getLastTrackingSession();
        }

        access.close();

        if (session == null) {
            getActivity().finish();
            return;
        }

        DateFormat dateFormat = new SimpleDateFormat("kk:mm:ss", Locale.FRANCE);

        ((TextView) mRootView.findViewById(R.id.txtv_session_start)).setText(dateFormat.format(new Date(session.getStartTime())));
        ((TextView) mRootView.findViewById(R.id.txtv_session_end)).setText(dateFormat.format(new Date(session.getEndTime())));
        ((TextView) mRootView.findViewById(R.id.txtv_session_duration)).setText(Formatter.getFormattedTime(session.getEndTime() - session.getStartTime()));
        ((TextView) mRootView.findViewById(R.id.txtv_session_distance)).setText(Formatter.getFormattedDistance(session.getDistance()));
        ((TextView) mRootView.findViewById(R.id.txtv_session_speed)).setText(Formatter.getKilometersPerHour(session.getAverageSpeed()));

    }
}
