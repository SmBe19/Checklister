package com.smeanox.apps.checklister;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;

import java.util.ArrayList;
import java.util.List;

public class ChecklistDB {

	private static ChecklistDB singleton;

	private Context context;
	private ChecklistDbHelper dbh;
	private Entry root;

	private ChecklistDB(){
	}

	public static ChecklistDB get(){
		if(singleton == null) {
			singleton = new ChecklistDB();
		}
		return singleton;
	}

	public void setContext(Context context){
		if(dbh != null) return;
		this.context = context;
		dbh = new ChecklistDbHelper(context);
	}

	public Entry addEntry(long parent, String text, boolean checked){
		SQLiteDatabase db = dbh.getWritableDatabase();

		ContentValues values = new ContentValues();
		values.put(ChecklistEntry.COLUMN_NAME_PARENT, parent);
		values.put(ChecklistEntry.COLUMN_NAME_TEXT, text);
		values.put(ChecklistEntry.COLUMN_NAME_CHECKED, checked ? 1 : 0);

		long newId = db.insert(ChecklistEntry.TABLE_NAME, null, values);
		return new Entry(newId);
	}

	public Entry getRoot(){
		if(root == null){
			SQLiteDatabase db = dbh.getReadableDatabase();
			String[] projection = {
					ChecklistEntry._ID
			};
			String selection = ChecklistEntry.COLUMN_NAME_PARENT + " = ?";
			String[] selectionArgs = {
					"-1"
			};
			Cursor c = db.query(ChecklistEntry.TABLE_NAME, projection, selection, selectionArgs, null, null, null);
			c.moveToFirst();
			if(c.getCount() > 0) {
				root = new Entry(c.getLong(c.getColumnIndexOrThrow(ChecklistEntry._ID)));
			} else {
				root = addEntry(-1, context.getResources().getString(R.string.app_name), false);
			}
			c.close();
		}
		return root;
	}

	public Entry getEntry(long id) {
		return new Entry(id);
	}

	public void resetDb(){
		root = null;

		SQLiteDatabase db = dbh.getWritableDatabase();
		db.execSQL(ChecklistEntry.SQL_DROP_TABLE);
		db.execSQL(ChecklistEntry.SQL_CREATE_TABLE);
	}

	public static abstract class ChecklistEntry implements BaseColumns {
		public static final String TABLE_NAME = "checklist_entry";
		public static final String COLUMN_NAME_PARENT = "parent";
		public static final String COLUMN_NAME_TEXT = "text";
		public static final String COLUMN_NAME_CHECKED = "checked";
		public static final String SQL_CREATE_TABLE = "CREATE TABLE " + TABLE_NAME + " ("
				+ _ID + " INTEGER PRIMARY KEY, "
				+ COLUMN_NAME_PARENT + " INTEGER, "
				+ COLUMN_NAME_TEXT + " TEXT, "
				+ COLUMN_NAME_CHECKED + " INTEGER"
				+ ")";
		public static final String SQL_DROP_TABLE = "DROP TABLE IF EXISTS " + TABLE_NAME;
	}

	public static class ChecklistDbHelper extends SQLiteOpenHelper{

		public static final int DATABASE_VERSION = 1;
		public static final String DATABASE_NAME = "Checklister.db";


		public ChecklistDbHelper(Context context) {
			super(context, DATABASE_NAME, null, DATABASE_VERSION);
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			db.execSQL(ChecklistEntry.SQL_CREATE_TABLE);
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		}
	}

	public class Entry{
		private long id;
		private long parent;
		private Entry parentEntry;
		private String text;
		private boolean checked;

		public Entry(long id){
			this.id = id;
			update();
		}

		public long getId() {
			return id;
		}

		public long getParent() {
			return parent;
		}

		public Entry getParentEntry() {
			if(parentEntry == null){
				parentEntry = new Entry(parent);
			}
			return parentEntry;
		}

		public String getText() {
			return text;
		}

		public boolean isChecked() {
			return checked;
		}

		public void setChecked(boolean checked) {
			this.checked = checked;
			ContentValues values = new ContentValues();
			values.put(ChecklistEntry.COLUMN_NAME_CHECKED, checked ? 1 : 0);
			updateDb(values);
		}

		public void setText(String text) {
			this.text = text;
			ContentValues values = new ContentValues();
			values.put(ChecklistEntry.COLUMN_NAME_TEXT, text);
			updateDb(values);
		}

		public void setParent(long parent) {
			this.parent = parent;
			this.parentEntry = null;
			ContentValues values = new ContentValues();
			values.put(ChecklistEntry.COLUMN_NAME_PARENT, parent);
			updateDb(values);
		}

		private void updateDb(ContentValues values) {
			SQLiteDatabase db = dbh.getWritableDatabase();
			String selection = ChecklistEntry._ID + " = ?";
			String[] selectionArgs = {
					String.valueOf(id)
			};
			db.update(ChecklistEntry.TABLE_NAME, values, selection, selectionArgs);
		}

		public List<Entry> getChildren(){
			List<Entry> res = new ArrayList<>();
			SQLiteDatabase db = dbh.getReadableDatabase();

			String[] projection = {
					ChecklistEntry._ID
			};
			String selection = ChecklistEntry.COLUMN_NAME_PARENT + " = ?";
			String[] selectionArgs = {
				String.valueOf(id)
			};

			Cursor c = db.query(ChecklistEntry.TABLE_NAME, projection, selection, selectionArgs, null, null, null);
			c.moveToFirst();
			if(c.getCount() > 0) {
				do {
					res.add(new Entry(c.getLong(c.getColumnIndexOrThrow(ChecklistEntry._ID))));
				} while (c.moveToNext());
			}
			c.close();
			return res;
		}

		public void update(){
			SQLiteDatabase db = dbh.getReadableDatabase();

			String[] projection = {
					ChecklistEntry._ID,
					ChecklistEntry.COLUMN_NAME_PARENT,
					ChecklistEntry.COLUMN_NAME_TEXT,
					ChecklistEntry.COLUMN_NAME_CHECKED
			};

			String selection = ChecklistEntry._ID + " = ?";
			String[] selectionArgs = {
					String.valueOf(id)
			};

			Cursor c = db.query(ChecklistEntry.TABLE_NAME, projection, selection, selectionArgs, null, null, null);
			c.moveToFirst();
			long oldParent = parent;
			parent = c.getLong(c.getColumnIndexOrThrow(ChecklistEntry.COLUMN_NAME_PARENT));
			if(oldParent != parent){
				parentEntry = null;
			}
			text = c.getString(c.getColumnIndexOrThrow(ChecklistEntry.COLUMN_NAME_TEXT));
			checked = c.getInt(c.getColumnIndexOrThrow(ChecklistEntry.COLUMN_NAME_CHECKED)) == 1;
			c.close();
		}

		public void delete() {
			for (Entry entry : getChildren()) {
				entry.delete();
			}

			SQLiteDatabase db = dbh.getWritableDatabase();

			String selection = ChecklistEntry._ID + " = ?";
			String[] selectionArgs = {
					String.valueOf(id)
			};
			db.delete(ChecklistEntry.TABLE_NAME, selection, selectionArgs);
		}
	}
}
