package com.example.randomselector;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DbAdapter {
	public static final String KEY_ITEM_NAME = "item_name";
	public static final String KEY_DONE = "done";
	public static final String KEY_ITEM_ID = "_id";
	public static final String KEY_CONTAINED_IN_CID = "cid";

	public static final String KEY_CATEGORY_ID = "_id";
	public static final String KEY_CATEGORY_NAME = "category_name";

	private static final String TAG = "DbAdapter";
	private DatabaseHelper mDbHelper;
	private SQLiteDatabase mDb;

	/**
	 * Database creation sql statement
	 */

	private static final String DATABASE_NAME = "data";
	private static final String DATABASE_TABLE1 = "Category";
	private static final String DATABASE_TABLE2 = "Item";
	private static final int DATABASE_VERSION = 2;

	private static final String DATABASE_CREATE_CATEGORY = "create table "
			+ DATABASE_TABLE1 + "(" + KEY_CATEGORY_ID
			+ " integer primary key autoincrement, " + KEY_CATEGORY_NAME
			+ " text not null);";

	private static final String DATABASE_CREATE_ITEM = "create table "
			+ DATABASE_TABLE2 + "(" + KEY_ITEM_ID
			+ " integer primary key autoincrement, " + KEY_ITEM_NAME
			+ " text not null," + KEY_DONE + " integer not null,"
			+ KEY_CONTAINED_IN_CID + " integer not null," + "FOREIGN KEY ("
			+ KEY_CONTAINED_IN_CID + ") references " + DATABASE_TABLE1 + " ("
			+ KEY_CATEGORY_ID + ") on delete cascade);";

	private final Context mCtx;

	private static class DatabaseHelper extends SQLiteOpenHelper {

		DatabaseHelper(Context context) {
			super(context, DATABASE_NAME, null, DATABASE_VERSION);
		}

		@Override
		public void onCreate(SQLiteDatabase db) {

			System.out.println("SQL QUERY(CREATE_CATEGORY): "
					+ DATABASE_CREATE_CATEGORY);
			System.out.println("SQL QUERY(CREATE_ITEM): "
					+ DATABASE_CREATE_ITEM);

			db.execSQL(DATABASE_CREATE_CATEGORY);
			db.execSQL(DATABASE_CREATE_ITEM);
			// Log.v("INFO1", "creating db!!");
		}

		@Override
		public void onOpen(SQLiteDatabase db) {
			super.onOpen(db);

			if (!db.isReadOnly()) {
				// turn on foreign eky
				db.execSQL("PRAGMA foreign_keys=ON;");
			}
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			Log.w(TAG, "Upgrading database from version " + oldVersion + " to "
					+ newVersion + ", which will destroy all old data");
			db.execSQL("DROP TABLE IF EXISTS " + DATABASE_TABLE1);
			db.execSQL("DROP TABLE IF EXISTS " + DATABASE_TABLE2);

			onCreate(db);
		}
	}

	/**
	 * Constructor - takes the context to allow the database to be
	 * opened/created
	 * 
	 * @param ctx
	 *            the Context within which to work
	 */
	public DbAdapter(Context ctx) {
		this.mCtx = ctx;
	}

	/**
	 * Open the notes database. If it cannot be opened, try to create a new
	 * instance of the database. If it cannot be created, throw an exception to
	 * signal the failure
	 * 
	 * @return this (self reference, allowing this to be chained in an
	 *         initialization call)
	 * @throws SQLException
	 *             if the database could be neither opened or created
	 */
	public DbAdapter open() throws SQLException {
		mDbHelper = new DatabaseHelper(mCtx);
		mDb = mDbHelper.getWritableDatabase();
		return this;
	}

	public void close() {
		mDbHelper.close();
	}

	/**
	 * Create a new category using the name provided. If the category is
	 * successfully created return the new rowId for that note, otherwise return
	 * a -1 to indicate failure.
	 * 
	 * @param categoryName
	 *            : name of the new category
	 * @return rowId or -1 if failed
	 */
	public long createCategory(String categoryName) {
		ContentValues initialValues = new ContentValues();
		initialValues.put(KEY_CATEGORY_NAME, categoryName);

		return mDb.insert(DATABASE_TABLE1, null, initialValues);
	}

	/**
	 * Return the number of items in a category
	 * 
	 * @param categoryName
	 *            : name of the category
	 * @return number of Items in a given category
	 */
	public long getNumItems(String categoryName) {
		Cursor mCursor = mDb.rawQuery("select count(*) from " + DATABASE_TABLE2
				+ " where " + KEY_CONTAINED_IN_CID + " = "
				+ getAssociatedCid(categoryName), null);
		mCursor.moveToFirst();
		long count = (int) mCursor.getInt(0);
		mCursor.close();

		return count;
	}

	/**
	 * Create a new category using the name provided. If the category is
	 * successfully created return the new rowId for that note, otherwise return
	 * a -1 to indicate failure.
	 * 
	 * @param categoryName
	 *            : name of the new category
	 * @return rowId or -1 if failed
	 */
	public long createItem(String itemName, String categoryName, int done) {
		ContentValues initialValues = new ContentValues();
		initialValues.put(KEY_ITEM_NAME, itemName);
		initialValues.put(KEY_DONE, done);
		// now get the category id associated with the categoryName
		int category_id = getAssociatedCid(categoryName);
		initialValues.put(KEY_CONTAINED_IN_CID, category_id);

		return mDb.insert(DATABASE_TABLE2, null, initialValues);
	}

	/**
	 * fetch the id of the provided category name
	 * 
	 * @param categoryName
	 * @return Cid or -1 if failed
	 */
	private int getAssociatedCid(String categoryName) {

		Cursor mCursor = mDb.query(true, DATABASE_TABLE1,
				new String[] { KEY_CATEGORY_ID, }, KEY_CATEGORY_NAME + "= '"
						+ categoryName + "'", null, null, null, null, null);
		if (mCursor != null) {
			mCursor.moveToFirst();
		} else {
			return -1;
		}

		int returnVal = mCursor.getInt(0);
		System.out.println("DbAdapter:getAssociatedCid => Cid:" + returnVal);

		return returnVal;

	}

	/**
	 * Delete the category with the given rowId
	 * 
	 * @param rowId
	 *            id of category to delete
	 * @return true if deleted, false otherwise
	 */
	public boolean deleteCategory(long rowId) {

		return mDb.delete(DATABASE_TABLE1, KEY_CATEGORY_ID + "=" + rowId, null) > 0;
	}

	/**
	 * Delete the item with the given rowId
	 * 
	 * @param rowId
	 *            id of item to delete
	 * @return true if deleted, false otherwise
	 */
	public boolean deleteItem(long rowId) {
		return mDb.delete(DATABASE_TABLE2, KEY_ITEM_ID + "=" + rowId, null) > 0;
	}

	/**
	 * Delete all of the categories
	 * 
	 * @return true if deleted, false otherwise
	 */
	public boolean deleteAllCategories() {

		return mDb.delete(DATABASE_TABLE1, null, null) > 0;

	}

	/**
	 * Delete all of the items in a given category
	 * 
	 * @return true if deleted, false otherwise
	 */
	public boolean deleteAllItems(String categoryName) {

		return mDb.delete(DATABASE_TABLE2, KEY_CONTAINED_IN_CID + "=" + "'"
				+ getAssociatedCid(categoryName) + "'", null) > 0;

	}

	/**
	 * Return a Cursor over the list of all categories in the db
	 * 
	 * @return Cursor over all Categories
	 */
	public Cursor fetchAllCategories() {
		return mDb.query(DATABASE_TABLE1, new String[] { KEY_CATEGORY_ID,
				KEY_CATEGORY_NAME }, null, null, null, null, null);
	}

	/**
	 * Return a Cursor over the list of all item in the category
	 * 
	 * @return Cursor over all Categories
	 */
	public Cursor fetchAllItems(String categoryName) {
		return mDb.query(DATABASE_TABLE2, new String[] { KEY_ITEM_ID,
				KEY_ITEM_NAME }, KEY_CONTAINED_IN_CID + "=" + "'"
				+ getAssociatedCid(categoryName) + "'", null, null, null, null);
	}

	/**
	 * Return a Cursor positioned at the Category matched writhe the rowId
	 * 
	 * @return Cursor positioned to match a Category, if found
	 * @throws SQLExpection
	 *             if note could not be found/retrieved
	 */
	public Cursor fetchCategory(long rowId) throws SQLException {
		Cursor mCursor = mDb.query(true, DATABASE_TABLE1, new String[] {
				KEY_CATEGORY_ID, KEY_CATEGORY_NAME }, KEY_CATEGORY_ID + "="
				+ rowId, null, null, null, null, null);
		if (mCursor != null) {
			mCursor.moveToFirst();
		}

		return mCursor;

	}

}
