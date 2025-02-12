package ai.rounds.speedmeter.db.access;


import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import ai.rounds.speedmeter.db.DbWrapper;

/**
 * Parent class for database access classes
 */
public abstract class DatabaseAccess {

    /**
     * Database use to perform queries
     */
    protected SQLiteDatabase db;
    /**
     * Helper used to open the database
     */
    protected DbWrapper wrapper;

    /**
     * Constructor
     *
     * @param context Context of the instanciation.
     */
    public DatabaseAccess(Context context) {
        wrapper = DbWrapper.getInstance(context);
    }

    /**
     * Opens the database in read-only mode
     */
    public void openToRead() {
        db = wrapper.getReadableDatabase();
    }

    /**
     * Opens the database in read and write mode
     */
    public void openToWrite() {
        db = wrapper.getWritableDatabase();
    }

    /**
     * Close the database if opened.
     */
    public void close() {
        if (db != null) {
            db.close();
            db = null;
        }
    }
}
