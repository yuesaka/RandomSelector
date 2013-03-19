package com.example.randomselector;

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

/*
 * TODO: Refactor code
 * TODO: Allow title Edits
 * TODO: Allow combinations of categories
 */

public class CategoryList extends ListActivity {

	private static final int ADD_CATEGORY = 0;

	private static final int DELETE_ID = Menu.FIRST + 1;

	private DbAdapter mDbHelper;

	private static ListView mCategoryList;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_category_list);

		mDbHelper = new DbAdapter(this);

		mDbHelper.open();

		mCategoryList = (ListView) getListView();

		fillCategoryList();

		registerForContextMenu(mCategoryList);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		mDbHelper.close();
	};

	public void addCategory(View theButton) {
		Intent i = new Intent(this, EditList.class);
		Bundle b = new Bundle();
		b.putString("hintText", getString(R.string.category_hint));
		i.putExtras(b);
		startActivityForResult(i, ADD_CATEGORY);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode,
			Intent intent) {
		super.onActivityResult(requestCode, resultCode, intent);
		fillCategoryList();
	}

	private void fillCategoryList() {
		Cursor CategoryCursor = mDbHelper.fetchAllCategories();
		startManagingCursor(CategoryCursor);

		String[] from = { DbAdapter.KEY_CATEGORY_NAME };
		int[] to = new int[] { R.id.category_row };

		SimpleCursorAdapter categories = new SimpleCursorAdapter(this,
				R.layout.row, CategoryCursor, from, to);

		setListAdapter(categories);
	}

	@Override
	protected void onListItemClick(ListView I, View v, int position, long id) {
		System.out.println("CategoryList: Clicked on :"
				+ I.getItemAtPosition(position).toString());
		Intent i = new Intent(this, ItemList.class);
		Bundle b = new Bundle();
		TextView category_text = (TextView) v.findViewById(R.id.category_row);
		String value = category_text.getText().toString();
		b.putString("category_name", value);
		i.putExtras(b);

		startActivity(i);

	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		menu.add(0, DELETE_ID, 0, R.string.delete_category);
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		//System.out.println("in onContextItemSelect");
		switch (item.getItemId()) {
		case DELETE_ID:
			AdapterContextMenuInfo info = (AdapterContextMenuInfo) item
					.getMenuInfo();
			mDbHelper.deleteCategory(info.id);
			fillCategoryList();
			return true;
		}

		return super.onContextItemSelected(item);
	}

	/*
	 * No menu yet...
	 * 
	 * @Override public boolean onCreateOptionsMenu(Menu menu) { // Inflate the
	 * menu; this adds items to the action bar if it is present.
	 * getMenuInflater().inflate(R.menu.activity_category_list, menu); return
	 * true; }
	 */

}
