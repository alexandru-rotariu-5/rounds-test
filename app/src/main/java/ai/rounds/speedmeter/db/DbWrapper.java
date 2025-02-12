package ai.rounds.speedmeter.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class  DbWrapper extends SQLiteOpenHelper {

    private static final int DB_VERSION = 1;

    private static final String DB_NAME = "speed_meter.db";

    private static final String COMMA_SEPARATOR = ", ";
    private static final String TYPE_INTEGER = " INTEGER ";
    private static final String TYPE_TEXT = " TEXT ";
    private static final String TYPE_REAL = " REAL ";

    private static DbWrapper INSTANCE = null;


    private DbWrapper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    public static DbWrapper getInstance(Context context) {
        if (INSTANCE == null) {
            INSTANCE = new DbWrapper(context);
        }
        return INSTANCE;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(getCreateEntriesSQL());
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }

    public static String getCreateEntriesSQL() {
        return "CREATE TABLE " + SessionEntry.TABLE_NAME +
                " (" +
                SessionEntry.COLUMN_NAME_ID + TYPE_TEXT + " PRIMARY KEY" + COMMA_SEPARATOR +
                SessionEntry.COLUMN_NAME_START_TIME + TYPE_INTEGER + COMMA_SEPARATOR +
                SessionEntry.COLUMN_NAME_END_TIME + TYPE_INTEGER + COMMA_SEPARATOR +
                SessionEntry.COLUMN_NAME_DISTANCE + TYPE_REAL + COMMA_SEPARATOR +
                SessionEntry.COLUMN_NAME_AVERAGE_SPEED + TYPE_REAL +
                " );";
    }

    /**
     * Tracking session table schema
     */
    public static class SessionEntry {
        public static final String TABLE_NAME = "session";
        public static final String COLUMN_NAME_ID = "id";
        public static final String COLUMN_NAME_START_TIME = "start_time";
        public static final String COLUMN_NAME_END_TIME = "end_time";
        public static final String COLUMN_NAME_DISTANCE = "distance";
        public static final String COLUMN_NAME_AVERAGE_SPEED = "average_speed";
    }
}
