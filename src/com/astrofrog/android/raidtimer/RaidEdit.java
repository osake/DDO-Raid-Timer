package com.astrofrog.android.raidtimer;

import java.util.Calendar;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.ToggleButton;

public class RaidEdit extends Activity {
    private EditText mNameText;
    private TextView mStartDateText;
    private ToggleButton mFlagged;
    private Long mRowId;
    private Long mToonId;
    private RaidsDbAdapter mDbHelper;

    // date and time
    private int mYear;
    private int mMonth;
    private int mDay;
    private int mHour;
    private int mMinute;
 
    static final int TIME_DIALOG_ID = 0;
    static final int DATE_DIALOG_ID = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mDbHelper = new RaidsDbAdapter(this);
        mDbHelper.open();

        setContentView(R.layout.raid_edit);
        setTitle(R.string.edit_raid);

        mNameText = (EditText) findViewById(R.id.name);
        mStartDateText = (TextView) findViewById(R.id.start_date);
        mFlagged = (ToggleButton) findViewById(R.id.flaggedToggle);

        Button setDate = (Button) findViewById(R.id.setDate);
        setDate.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                showDialog(DATE_DIALOG_ID);
            }
        });

        Button setTime = (Button) findViewById(R.id.setTime);
        setTime.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                showDialog(TIME_DIALOG_ID);
            }
        });

        calendarSetup();

        Button confirmButton = (Button) findViewById(R.id.confirm_raid);
        Button completeButton = (Button) findViewById(R.id.setComplete);

        mRowId = (savedInstanceState == null) ? null :
            (Long) savedInstanceState.getSerializable(RaidsDbAdapter.KEY_ROWID);
		if (mRowId == null) {
			Bundle extras = getIntent().getExtras();
			mRowId = extras.containsKey(RaidsDbAdapter.KEY_ROWID) ? extras.getLong(RaidsDbAdapter.KEY_ROWID)
									: null;
		}
		
        mToonId = (savedInstanceState == null) ? null :
            (Long) savedInstanceState.getSerializable(RaidsDbAdapter.KEY_TOON_ID);
		if (mToonId == null) {
			Bundle extras = getIntent().getExtras();
			mToonId = extras.containsKey(RaidsDbAdapter.KEY_TOON_ID) ? extras.getLong(RaidsDbAdapter.KEY_TOON_ID)
									: null;
		}

		populateFields();

        confirmButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                setResult(RESULT_OK);
                finish();
            }
        });
        
        completeButton.setOnClickListener(new View.OnClickListener() {
        	public void onClick(View view) {
        		calendarSetup();
        		updateDisplay();
        	}
        });
    }

    private void populateFields() {
    	int name_column;
        if (mRowId != null) {
            Cursor raid = mDbHelper.fetchRaid(mRowId);
            startManagingCursor(raid);
            if(raid.getColumnIndex(RaidsDbAdapter.KEY_NAME) != -1) {
            	name_column = raid.getColumnIndex(RaidsDbAdapter.KEY_NAME);
            	mNameText.setText(raid.getString(name_column));
            }
            mStartDateText.setText(raid.getString(
            		raid.getColumnIndexOrThrow(RaidsDbAdapter.KEY_START_DATE)));
            if(raid.getInt(raid.getColumnIndex(RaidsDbAdapter.KEY_FLAGGED)) == 1) {
            	mFlagged.setChecked(true);
            } else {
            	mFlagged.setChecked(false);
            }
        }
    }

    private void calendarSetup() {
        final Calendar c = Calendar.getInstance();
        mYear = c.get(Calendar.YEAR);
        mMonth = c.get(Calendar.MONTH);
        mDay = c.get(Calendar.DAY_OF_MONTH);
        mHour = c.get(Calendar.HOUR_OF_DAY);
        mMinute = c.get(Calendar.MINUTE);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        saveState();
        outState.putSerializable(RaidsDbAdapter.KEY_ROWID, mRowId);
    }

    @Override
    protected void onPause() {
        super.onPause();
        saveState();
    }

    @Override
    protected void onResume() {
        super.onResume();
        populateFields();
    }

    private void saveState() {
        String name = mNameText.getText().toString();
        String start_date = mStartDateText.getText().toString();
        String flagged = mFlagged.getText().toString();
        Long toon_id = mToonId;

        if (mRowId == null) {
            long id = mDbHelper.createRaid(name, start_date, toon_id);
            if (id > 0) {
                mRowId = id;
            }
        } else {
            mDbHelper.updateRaid(mRowId, name, start_date, (flagged.matches("YES") ? 1 : 0));
        }
    }

    @Override
    protected Dialog onCreateDialog(int id) {
        switch (id) {
            case DATE_DIALOG_ID:
                return new DatePickerDialog(this,
                        mDateSetListener, mYear, mMonth, mDay);
            case TIME_DIALOG_ID:
            	return new TimePickerDialog(this,
            			mTimeSetListener, mHour, mMinute, false);
        }
        return null;
    }

    @Override
    protected void onPrepareDialog(int id, Dialog dialog) {
        switch (id) {
            case DATE_DIALOG_ID:
                ((DatePickerDialog) dialog).updateDate(mYear, mMonth, mDay);
                break;
            case TIME_DIALOG_ID:
            	((TimePickerDialog) dialog).updateTime(mHour, mMinute);
            	break;
        }
    }

    private void updateDisplay() {
        mStartDateText.setText(
                new StringBuilder()
                        // Month is 0 based so add 1
                        .append(mMonth + 1).append("-")
                        .append(mDay).append("-")
                        .append(mYear).append(" ")
                        .append(pad(mHour)).append(":")
                		.append(pad(mMinute)));
    }

    private DatePickerDialog.OnDateSetListener mDateSetListener =
        new DatePickerDialog.OnDateSetListener() {

            public void onDateSet(DatePicker view, int year, int monthOfYear,
                    int dayOfMonth) {
                mYear = year;
                mMonth = monthOfYear;
                mDay = dayOfMonth;
                updateDisplay();
            }
        };
        
    private TimePickerDialog.OnTimeSetListener mTimeSetListener =
    	new TimePickerDialog.OnTimeSetListener() {
			
			public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
				mHour = hourOfDay;
				mMinute = minute;
				updateDisplay();
			}
		};

    private static String pad(int c) {
        if (c >= 10)
            return String.valueOf(c);
        else
            return "0" + String.valueOf(c);
    }
}
