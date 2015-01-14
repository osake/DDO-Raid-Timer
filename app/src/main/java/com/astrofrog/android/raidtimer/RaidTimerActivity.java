package com.astrofrog.android.raidtimer;

import android.app.ListActivity;
import android.os.Bundle;
import android.view.Menu;
import android.database.Cursor;
import android.widget.SimpleCursorAdapter;
import android.view.MenuItem;
import android.content.Intent;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.ListView;
import android.view.View;

// TODO: Fix bug where hitting back on edit results in an empty named record and it's not selectable, so you can't even remove it
public class RaidTimerActivity extends ListActivity {
    private static final int ACTIVITY_CREATE_TOON = 0;
    private static final int ACTIVITY_EDIT_TOON = 1;

    private static final int INSERT_TOON_ID = Menu.FIRST;
    private static final int DELETE_TOON_ID = Menu.FIRST + 1;

    private ToonsDbAdapter mToonsDbHelper;
//    private RaidsDbAdapter mRaidsDbHelper;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.toons_list);
        
        mToonsDbHelper = new ToonsDbAdapter(this);
//        mRaidsDbHelper = new RaidsDbAdapter(this);
        mToonsDbHelper.open();
//        mRaidsDbHelper.open();
        
        fillToonData();
        registerForContextMenu(getListView());
    }

    private void fillToonData() {
        Cursor toonsCursor = mToonsDbHelper.fetchAllToons();
        startManagingCursor(toonsCursor);

        // Create an array to specify the fields we want to display in the list (only AUTHOR)
        String[] from = new String[]{ToonsDbAdapter.KEY_TOON_NAME};

        // and an array of the fields we want to bind those fields to (in this case just text2)
        int[] to = new int[]{R.id.text2};

        // Now create a simple cursor adapter and set it to display
        SimpleCursorAdapter toons = 
            new SimpleCursorAdapter(this, R.layout.toons_row, toonsCursor, from, to);
        setListAdapter(toons);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        menu.add(0, INSERT_TOON_ID, 0, R.string.menu_toon_insert);
        return true;
    }

    @Override
    public boolean onMenuItemSelected(int featureId, MenuItem item) {
        switch(item.getItemId()) {
            case INSERT_TOON_ID:
                createToon();
                return true;
        }

        return super.onMenuItemSelected(featureId, item);
    }

    private void createToon() {
        Intent i = new Intent(this, ToonEdit.class);
        startActivityForResult(i, ACTIVITY_CREATE_TOON);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v,
            ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        menu.add(0, DELETE_TOON_ID, 0, R.string.menu_toon_delete);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case DELETE_TOON_ID:
                AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
                mToonsDbHelper.deleteToon(info.id);
                fillToonData();
                return true;
        }
        return super.onContextItemSelected(item);
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);
        Intent i = new Intent(this, ToonEdit.class);
        i.putExtra(ToonsDbAdapter.KEY_TOON_ROWID, id);
        startActivityForResult(i, ACTIVITY_EDIT_TOON);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        fillToonData();
    }
}
