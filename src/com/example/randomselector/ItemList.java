package com.example.randomselector;

import java.util.Random;

import android.app.ListActivity;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

public class ItemList extends ListActivity {

	private static TextView mCategoryName;
	private static TextView mChosenItem;
	private static ListView mListView;

	private static final int ADD_ITEM = 0;

	private static final int DELETE_ID = Menu.FIRST + 1;

	private static String categoryName;

	private static DbAdapter mDbHelper;

	private int lastChosenIndex;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_item_list);

		mDbHelper = new DbAdapter(this);
		mDbHelper.open();

		Bundle extras = getIntent().getExtras();

		mCategoryName = (TextView) findViewById(R.id.item_list_title);

		// TODO: replace intent bundle strings with public static variables
		categoryName = extras.getString("category_name");

		System.out.println("ItemList:" + categoryName);
		if (!categoryName.equals(null)) {
			mCategoryName.setText(categoryName);
		}

		mListView = (ListView) getListView();
		mChosenItem = (TextView) findViewById(R.id.chosen_item);
		fillItemList();

		registerForContextMenu(mListView);
	}
	
	@Override
	protected void onDestroy(){
		super.onDestroy();
		mDbHelper.close();
	}

	public void addItem(View theButton) {
		Intent i = new Intent(this, EditList.class);
		Bundle b = new Bundle();
		b.putString("hintText", getString(R.string.item_hint));
		b.putString("categoryName", categoryName);
		i.putExtras(b);
		startActivityForResult(i, ADD_ITEM);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode,
			Intent intent) {
		super.onActivityResult(requestCode, resultCode, intent);
		fillItemList();
	}

	private void fillItemList() {
		Cursor ItemCursor = mDbHelper.fetchAllItems(categoryName);
		startManagingCursor(ItemCursor);

		String[] from = { DbAdapter.KEY_ITEM_NAME };
		int[] to = new int[] { R.id.category_row };

		SimpleCursorAdapter items = new SimpleCursorAdapter(this,
				R.layout.row, ItemCursor, from, to);

		setListAdapter(items);
	}

	public void chooseItem(View theButton) {
		if (mDbHelper.getNumItems(categoryName) > 0) {
			Random rand = new Random();
			int min = 0;
			int max = (int) mDbHelper.getNumItems(categoryName) - 1;
			int randomInt = rand.nextInt(max - min + 1) + min;
			while (randomInt == lastChosenIndex && (max - min) >= 1) {
				randomInt = rand.nextInt(max - min + 1) + min;
			}

			Cursor row = (Cursor) mListView.getItemAtPosition(randomInt);
			if (row.isNull(1)) {
				System.out.println("empty");
			} else {
				System.out.println(row.getString(1));
			}
			mChosenItem.setText(row.getString(1));
			lastChosenIndex = randomInt;
		}
	}

	public void deleteAllItems(View theButton) {
		mDbHelper.deleteAllItems(categoryName);
		mChosenItem.setText("");
		fillItemList();
	}
	
	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		menu.add(0, DELETE_ID, 0, R.string.delete_item);
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case DELETE_ID:
			AdapterContextMenuInfo info = (AdapterContextMenuInfo) item
					.getMenuInfo();
			mDbHelper.deleteItem(info.id);
			mChosenItem.setText("");
			fillItemList();
			return true;
		}
		return super.onContextItemSelected(item);
	}
}
