package ai.rounds.speedmeter.db.access;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

import ai.rounds.speedmeter.db.DbWrapper;
import ai.rounds.speedmeter.models.Session;

/**
 * Database access for the tracking_session table
 */
public class SessionAccess extends DatabaseAccess {
    /**
     * @see DatabaseAccess#DatabaseAccess(Context)
     */
    public SessionAccess(Context context) {
        super(context);
    }

    /**
     * Gets the last tracking session recorded
     *
     * @return The last tracking session model
     */
    public Session getLastTrackingSession() {
        String query = "SELECT "
                + DbWrapper.SessionEntry.COLUMN_NAME_ID + ", "
                + DbWrapper.SessionEntry.COLUMN_NAME_START_TIME + ", "
                + DbWrapper.SessionEntry.COLUMN_NAME_END_TIME + ", "
                + DbWrapper.SessionEntry.COLUMN_NAME_DISTANCE + ", "
                + DbWrapper.SessionEntry.COLUMN_NAME_AVERAGE_SPEED + ", "
                + DbWrapper.SessionEntry.COLUMN_NAME_START_LATITUDE + ", "
                + DbWrapper.SessionEntry.COLUMN_NAME_START_LONGITUDE + ", "
                + DbWrapper.SessionEntry.COLUMN_NAME_END_LATITUDE + ", "
                + DbWrapper.SessionEntry.COLUMN_NAME_END_LONGITUDE
                + " FROM " + DbWrapper.SessionEntry.TABLE_NAME
                + " ORDER BY  " + DbWrapper.SessionEntry.COLUMN_NAME_START_TIME + " DESC "
                + " LIMIT 1 ;";

        Session session = null;
        Cursor c = db.rawQuery(query, null);

        if (c.moveToFirst()) {
            session = cursorToTrackingSession(c);
        }

        c.close();

        return session;
    }

    /**
     * Gets the tracking session with the given id
     *
     * @return The corresponding tracking session model
     */
    public Session getTrackingSessionById(String id) {

        String query = "SELECT "
                + DbWrapper.SessionEntry.COLUMN_NAME_ID + ", "
                + DbWrapper.SessionEntry.COLUMN_NAME_START_TIME + ", "
                + DbWrapper.SessionEntry.COLUMN_NAME_END_TIME + ", "
                + DbWrapper.SessionEntry.COLUMN_NAME_DISTANCE + ", "
                + DbWrapper.SessionEntry.COLUMN_NAME_AVERAGE_SPEED + ", "
                + DbWrapper.SessionEntry.COLUMN_NAME_START_LATITUDE + ", "
                + DbWrapper.SessionEntry.COLUMN_NAME_START_LONGITUDE + ", "
                + DbWrapper.SessionEntry.COLUMN_NAME_END_LATITUDE + ", "
                + DbWrapper.SessionEntry.COLUMN_NAME_END_LONGITUDE
                + " FROM " + DbWrapper.SessionEntry.TABLE_NAME
                + " WHERE " + DbWrapper.SessionEntry.COLUMN_NAME_ID + " = \"" + id + "\" "
                + " ORDER BY  " + DbWrapper.SessionEntry.COLUMN_NAME_START_TIME + " DESC "
                + " LIMIT 1 ;";

        Session session = null;
        Cursor c = db.rawQuery(query, null);

        if (c.moveToFirst()) {
            session = cursorToTrackingSession(c);
        }

        c.close();

        return session;
    }

    /**
     * Saves a tracking session model into the database
     *
     * @param mSession Tracking session model to save
     */
    public void saveTrackingSession(Session mSession) {

        ContentValues values = new ContentValues();

        values.put(DbWrapper.SessionEntry.COLUMN_NAME_ID, mSession.getId());
        values.put(DbWrapper.SessionEntry.COLUMN_NAME_START_TIME, mSession.getStartTime());
        values.put(DbWrapper.SessionEntry.COLUMN_NAME_END_TIME, mSession.getEndTime());
        values.put(DbWrapper.SessionEntry.COLUMN_NAME_DISTANCE, mSession.getDistance());
        values.put(DbWrapper.SessionEntry.COLUMN_NAME_AVERAGE_SPEED, mSession.getAverageSpeed());
        values.put(DbWrapper.SessionEntry.COLUMN_NAME_START_LATITUDE, mSession.getStartLatitude());
        values.put(DbWrapper.SessionEntry.COLUMN_NAME_START_LONGITUDE, mSession.getStartLongitude());
        values.put(DbWrapper.SessionEntry.COLUMN_NAME_END_LATITUDE, mSession.getEndLatitude());
        values.put(DbWrapper.SessionEntry.COLUMN_NAME_END_LONGITUDE, mSession.getEndLongitude());

        db.replace(DbWrapper.SessionEntry.TABLE_NAME, null, values);
    }


    /**
     * Converts a cursor to tracking session model
     *
     * @param cursor Cursor to convert
     * @return The tracking session model
     */
    @SuppressLint("Range")
    private Session cursorToTrackingSession(Cursor cursor) {

        @SuppressLint("Range") Session session = new Session(
                cursor.getString(cursor.getColumnIndex(DbWrapper.SessionEntry.COLUMN_NAME_ID)),
                cursor.getLong(cursor.getColumnIndex(DbWrapper.SessionEntry.COLUMN_NAME_START_TIME)),
                cursor.getLong(cursor.getColumnIndex(DbWrapper.SessionEntry.COLUMN_NAME_END_TIME))
        );

        session.setDistance(cursor.getFloat(cursor.getColumnIndex(DbWrapper.SessionEntry.COLUMN_NAME_DISTANCE)));
        session.setAverageSpeed(cursor.getFloat(cursor.getColumnIndex(DbWrapper.SessionEntry.COLUMN_NAME_AVERAGE_SPEED)));
        session.setStartLatitude(cursor.getDouble(cursor.getColumnIndex(DbWrapper.SessionEntry.COLUMN_NAME_START_LATITUDE)));
        session.setStartLongitude(cursor.getDouble(cursor.getColumnIndex(DbWrapper.SessionEntry.COLUMN_NAME_START_LONGITUDE)));
        session.setEndLatitude(cursor.getDouble(cursor.getColumnIndex(DbWrapper.SessionEntry.COLUMN_NAME_END_LATITUDE)));
        session.setEndLongitude(cursor.getDouble(cursor.getColumnIndex(DbWrapper.SessionEntry.COLUMN_NAME_END_LONGITUDE)));

        return session;
    }


}
