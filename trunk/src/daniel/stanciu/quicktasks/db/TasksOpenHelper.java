package daniel.stanciu.quicktasks.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class TasksOpenHelper extends SQLiteOpenHelper {
	// DB version 1 details
	public static final String DATABASE_NAME = "quickTasks";
	public static final int DATABASE_VERSION = 2;
	// tasklists table details
	public static final String TASKLISTS_TABLE_NAME = "tasklists";
	public static final String LISTS_ID_COLUMN = "id";
	public static final String LISTS_INTERNAL_ID_COLUMN = "internal_id";
	public static final String LISTS_TITLE_COLUMN = "title";
	public static final String LISTS_DIRTY_COLUMN = "dirty";
	public static final String LISTS_DELETED_COLUMN = "deleted";

	public static final String TASKLISTS_TABLE_CREATE =
			"CREATE TABLE " + TASKLISTS_TABLE_NAME + " (" +
					LISTS_INTERNAL_ID_COLUMN + " INTEGER PRIMARY KEY ASC, " +
					LISTS_ID_COLUMN + " TEXT UNIQUE, " +
					LISTS_TITLE_COLUMN + " TEXT, " +
					LISTS_DIRTY_COLUMN + " INTEGER DEFAULT 0," +
					LISTS_DELETED_COLUMN + " INTEGER DEFAULT 0" +
					");";
	// tasks table details
	public static final String TASKS_TABLE_NAME = "tasks";
	public static final String TASKS_ID_COLUMN = "id";
	public static final String TASKS_CHECKED_COLUMN = "checked";
	public static final String TASKS_TITLE_COLUMN = "title";
	public static final String TASKS_LIST_ID_COLUMN = "list_id";
	public static final String TASKS_INTERNAL_ID_COLUMN = "internal_id";
	public static final String TASKS_DIRTY_COLUMN = "dirty";
	public static final String TASKS_DELETED_COLUMN = "deleted";
	
	public static final String TASKS_PRIORITY_COLUMN = "priority";

	public static final String TASKS_TABLE_CREATE =
			"CREATE TABLE " + TASKS_TABLE_NAME + " (" +
					TASKS_INTERNAL_ID_COLUMN + " INTEGER PRIMARY KEY ASC, " +
					TASKS_ID_COLUMN + " TEXT UNIQUE, " +
					TASKS_TITLE_COLUMN + " TEXT, " +
					TASKS_CHECKED_COLUMN + " INTEGER DEFAULT 0, " +
					TASKS_LIST_ID_COLUMN + " TEXT, " +
					TASKS_DIRTY_COLUMN + " INTEGER DEFAULT 0," +
					TASKS_DELETED_COLUMN + " INTEGER DEFAULT 0," +
					TASKS_PRIORITY_COLUMN + " INTEGER NOT NULL DEFAULT 1" +
					");";
	

	public TasksOpenHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}
	
	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL(TASKLISTS_TABLE_CREATE);
		db.execSQL(TASKS_TABLE_CREATE);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// nothing for the first version
		if (oldVersion == 1 && newVersion == 2) {
			// add the position column to the tasks table
			db.execSQL("ALTER TABLE " + TASKS_TABLE_NAME
					+ " ADD COLUMN " + TASKS_PRIORITY_COLUMN
					+ " INTEGER NOT NULL DEFAULT 1;");
		}
	}
}
