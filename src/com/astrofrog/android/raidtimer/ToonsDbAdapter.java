package com.astrofrog.android.raidtimer;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class ToonsDbAdapter {
    public static final String KEY_TOON_ROWID = "_id";
    public static final String KEY_TOON_NAME = "name";
    
    private static final String TAG = "ToonsDbAdapter";
    private DatabaseHelper mDbHelper;
    private SQLiteDatabase mDb;

    private static final String DATABASE_NAME = "data";
    private static final int DATABASE_VERSION = 1;

    private static final String DATABASE_TOONS_TABLE = "toons";
    private static final String DATABASE_CREATE_TOONS_TABLE =
        "create table " + DATABASE_TOONS_TABLE
        + " (_id integer primary key autoincrement, "
        + "name text not null);";

    private static final String DATABASE_RAIDS_TABLE = "raids";
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
            db.execSQL(DATABASE_CREATE_TOONS_TABLE);
            db.execSQL(DATABASE_CREATE_RAIDS_TABLE);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            Log.w(TAG, "Upgrading database from version " + oldVersion + " to "
                    + newVersion + ", which will destroy all old data");
            db.execSQL("DROP TABLE IF EXISTS " + DATABASE_TOONS_TABLE);
            onCreate(db);
        }
    }

    public ToonsDbAdapter(Context ctx) {
        this.mCtx = ctx;
    }

    public ToonsDbAdapter open() throws SQLException {
        mDbHelper = new DatabaseHelper(mCtx);
        mDb = mDbHelper.getWritableDatabase();
        return this;
    }

    public void close() {
        mDbHelper.close();
    }

    public long createToon(String author) {
        ContentValues initialValues = new ContentValues();
        initialValues.put(KEY_TOON_NAME, author);

        return mDb.insert(DATABASE_TOONS_TABLE, null, initialValues);
    }

    public boolean deleteToon(long rowId) {
    	// TODO: Open a toons adapter and delete rows with the toon id
        return mDb.delete(DATABASE_TOONS_TABLE, KEY_TOON_ROWID + "=" + rowId, null) > 0;
    }

    public Cursor fetchAllToons() {
        return mDb.query(DATABASE_TOONS_TABLE, new String[] {KEY_TOON_ROWID, KEY_TOON_NAME}, 
                null, null, null, null, null);
    }

    public Cursor fetchToon(long rowId) throws SQLException {
        Cursor mCursor =
            mDb.query(true, DATABASE_TOONS_TABLE, new String[] {KEY_TOON_ROWID,
                    KEY_TOON_NAME}, 
                    KEY_TOON_ROWID + "=" + rowId, null,
                    null, null, null, null);
        if (mCursor != null) {
            mCursor.moveToFirst();
        }
        return mCursor;
    }

    public boolean updateToon(long rowId, String author) {
        ContentValues args = new ContentValues();
        args.put(KEY_TOON_NAME, author);

        return mDb.update(DATABASE_TOONS_TABLE, args, KEY_TOON_ROWID + "=" + rowId, null) > 0;
    }
}
