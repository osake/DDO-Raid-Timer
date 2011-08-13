package com.astrofrog.android.raidtimer;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class RaidsDbAdapter {
    public static final String KEY_NAME = "name";
    public static final String KEY_START_DATE = "start_date";
    public static final String KEY_TOON_ID = "toon_id";
    public static final String KEY_ROWID = "_id";
    
    private static final String TAG = "RaidsDbAdapter";
    private DatabaseHelper mDbHelper;
    private SQLiteDatabase mDb;

    private static final String DATABASE_NAME = "data";
    private static final String DATABASE_RAIDS_TABLE = "raids";
    private static final int DATABASE_VERSION = 1;

    private static final String DATABASE_CREATE_RAIDS_TABLE =
        "create table " + DATABASE_RAIDS_TABLE 
    	+ " (_id integer primary key autoincrement, "
        + "name text not null, toon_id integer, start_date text not null);";

    private final Context mCtx;

    private static class DatabaseHelper extends SQLiteOpenHelper {

        DatabaseHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
        	Log.w("DB", DATABASE_CREATE_RAIDS_TABLE);
            db.execSQL(DATABASE_CREATE_RAIDS_TABLE);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            Log.w(TAG, "Upgrading database from version " + oldVersion + " to "
                    + newVersion + ", which will destroy all old data");
            db.execSQL("DROP TABLE IF EXISTS " + DATABASE_RAIDS_TABLE);
            onCreate(db);
        }
    }

    public RaidsDbAdapter(Context ctx) {
        this.mCtx = ctx;
    }

    public RaidsDbAdapter open() throws SQLException {
        mDbHelper = new DatabaseHelper(mCtx);
        mDb = mDbHelper.getWritableDatabase();
        return this;
    }

    public void close() {
        mDbHelper.close();
    }

    public long createRaid(String name, String start_date, Long toon_id) {
        ContentValues initialValues = new ContentValues();
        initialValues.put(KEY_NAME, name);
        initialValues.put(KEY_START_DATE, start_date);
        initialValues.put(KEY_TOON_ID, toon_id);

        return mDb.insert(DATABASE_RAIDS_TABLE, null, initialValues);
    }

    public boolean deleteRaid(long rowId) {
        return mDb.delete(DATABASE_RAIDS_TABLE, KEY_ROWID + "=" + rowId, null) > 0;
    }

    public Cursor fetchAllRaids() {
        return mDb.query(DATABASE_RAIDS_TABLE, new String[] {KEY_ROWID, KEY_NAME, KEY_START_DATE}, 
                null, null, null, null, null);
    }
    
    public Cursor fetchToonsRaids(long toonId) throws SQLException {
        Cursor mCursor =
            mDb.query(true, DATABASE_RAIDS_TABLE, new String[] {KEY_ROWID,
                    KEY_NAME, KEY_START_DATE}, 
                    KEY_TOON_ID + "=" + toonId, null,
                    null, null, null, null);
        if (mCursor != null) {
            mCursor.moveToFirst();
        }
        return mCursor;
    }
    
    public Cursor fetchRaid(long rowId) throws SQLException {
        Cursor mCursor =
            mDb.query(true, DATABASE_RAIDS_TABLE, new String[] {KEY_ROWID,
                    KEY_NAME, KEY_START_DATE}, 
                    KEY_ROWID + "=" + rowId, null,
                    null, null, null, null);
        if (mCursor != null) {
            mCursor.moveToFirst();
        }
        return mCursor;
    }

    public boolean updateRaid(long rowId, String name, String start_date) {
        ContentValues args = new ContentValues();
        args.put(KEY_NAME, name);
        args.put(KEY_START_DATE, start_date);

        return mDb.update(DATABASE_RAIDS_TABLE, args, KEY_ROWID + "=" + rowId, null) > 0;
    }
}
