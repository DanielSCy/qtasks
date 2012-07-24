package daniel.stanciu.quicktasks.db;

import java.util.ArrayList;
import java.util.List;

import daniel.stanciu.quicktasks.MyTask;
import daniel.stanciu.quicktasks.MyTaskBase;
import daniel.stanciu.quicktasks.MyTasksList;
import daniel.stanciu.quicktasks.QuickTasksActivity;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class DbManager {
	
	Context context;
	TasksOpenHelper dbOpenHelper;
	SQLiteDatabase db;
	QuickTasksActivity activity;
	public ArrayList<MyTaskBase> availableLists = new ArrayList<MyTaskBase>();
	private final static String[] fullListColumns = new String[] {
		TasksOpenHelper.LISTS_INTERNAL_ID_COLUMN,
		TasksOpenHelper.LISTS_ID_COLUMN,
		TasksOpenHelper.LISTS_TITLE_COLUMN,
		TasksOpenHelper.LISTS_DELETED_COLUMN
	};
	private final static String[] fullTaskColumns = new String[] {
		TasksOpenHelper.TASKS_INTERNAL_ID_COLUMN,
		TasksOpenHelper.TASKS_ID_COLUMN,
		TasksOpenHelper.TASKS_TITLE_COLUMN,
		TasksOpenHelper.TASKS_CHECKED_COLUMN,
		TasksOpenHelper.TASKS_LIST_ID_COLUMN,
		TasksOpenHelper.TASKS_DELETED_COLUMN
	};
	
	public DbManager(Context context, QuickTasksActivity activity) {
		this.activity = activity;
		this.context = context;
		dbOpenHelper = new TasksOpenHelper(context);
		db = dbOpenHelper.getWritableDatabase();
		populateAvailableLists();
	}
	
	public boolean isDbEmpty() {
		if (availableLists.size() == 0) {
			return true;
		}
		return false;
	}

	public void openDb() {
		if (!db.isOpen()) {
			db = dbOpenHelper.getWritableDatabase();
		}
	}
	
	public void closeDb() {
		if (db.isOpen()) {
			db.close();
		}
	}
	
	public MyTasksList getListFromCursor(Cursor cursor) {
		MyTasksList list = new MyTasksList(
				cursor.getString(1),
				cursor.getString(2));
		list.setInternalId(cursor.getInt(0));
		list.setDeleted(cursor.getInt(3) != 0);
		return list;
	}
	
	public void populateAvailableLists() {
		openDb();
		Cursor listsCursor = null;
		try {
			listsCursor = db.query(
					TasksOpenHelper.TASKLISTS_TABLE_NAME,
					fullListColumns,
					TasksOpenHelper.LISTS_DELETED_COLUMN + " = 0",
					null, null, null, null);
			availableLists.clear();

			while (listsCursor.moveToNext()) {
				MyTasksList list = getListFromCursor(listsCursor);
				availableLists.add(list);
			}
		} finally {
			if (listsCursor != null) {
				listsCursor.close();
			}
		}
	}
	
	public List<MyTaskBase> getLists() {
		ArrayList<MyTaskBase> result = new ArrayList<MyTaskBase>();
		result.addAll(availableLists);
		return result;
	}
	
	public ArrayList<MyTaskBase> getTasks(MyTasksList list) {
		ArrayList<MyTaskBase> result = new ArrayList<MyTaskBase>();
		
		openDb();
		String listId = list.getId();
		if (listId == null) {
			listId = Long.toString(list.getInternalId());
		}
		Cursor tasksCursor = null;

		try {
			tasksCursor = db.query(
					TasksOpenHelper.TASKS_TABLE_NAME,
					fullTaskColumns,
					TasksOpenHelper.TASKS_LIST_ID_COLUMN + " = ? AND " +
					TasksOpenHelper.TASKS_DELETED_COLUMN + " = 0",
					new String[] {
							listId
					},
					null, null, TasksOpenHelper.TASKS_CHECKED_COLUMN + " ASC");
			
			while(tasksCursor.moveToNext()) {
				MyTask task = getTaskFromCursor(tasksCursor);
				task.setParentListId(list.getId());
				result.add(task);
			}
		} finally {
			if (tasksCursor != null) {
				tasksCursor.close();
			}
		}
		
		return result;
	}

	private MyTask getTaskFromCursor(Cursor cursor) {
		MyTask result = new MyTask(cursor.getString(1),
				cursor.getString(2));
		result.setInternalId(cursor.getInt(0));
		result.setChecked(cursor.getInt(3) != 0);
		result.setParentListId(cursor.getString(4));
		result.setDeleted(cursor.getInt(5) != 0);
		return result;
	}

	public boolean isDbDirty() {
		openDb();
		// check for dirty lists
		Cursor cursor = null;
		try {
			cursor = db.query(
					TasksOpenHelper.TASKLISTS_TABLE_NAME,
					new String[] {"COUNT(1)"},
					TasksOpenHelper.LISTS_DIRTY_COLUMN + "<> 0",
					null, null, null, null);
			if (cursor.moveToFirst()) {
				if (cursor.getInt(0) != 0) {
					return true;
				}
			}
		} finally {
			if (cursor != null) {
				cursor.close();
				cursor = null;
			}
		}
		
		// check for dirty tasks
		try {
			cursor = db.query(
					TasksOpenHelper.TASKS_TABLE_NAME,
					new String[] {"COUNT(1)"},
					TasksOpenHelper.TASKS_DIRTY_COLUMN + "<> 0",
					null, null, null, null);
			if (cursor.moveToFirst()) {
				if (cursor.getInt(0) != 0) {
					return true;
				}
			}
		} finally {
			if (cursor != null) {
				cursor.close();
				cursor = null;
			}
		}
		
		return false;
	}
	
	public boolean insertList(MyTasksList list) {
		openDb();
		ContentValues values = new ContentValues();
		if (list.getId() == null) {
			// tasks list created now, not taken from web DB
			values.put(TasksOpenHelper.LISTS_DIRTY_COLUMN, 1);
		} else {
			// tasks list downloaded from web
			values.put(TasksOpenHelper.LISTS_ID_COLUMN, list.getId());
			values.put(TasksOpenHelper.LISTS_DIRTY_COLUMN, 0);
		}
		values.put(TasksOpenHelper.LISTS_TITLE_COLUMN, list.getTitle());
		long rowId = db.insert(TasksOpenHelper.TASKLISTS_TABLE_NAME, null, values );
		if (rowId != -1) {
			list.setInternalId(rowId);
			availableLists.add(list);
			return true;
		}
		
		return false;
	}
	
	public boolean insertTask(MyTask task, MyTasksList list) {
		openDb();
		ContentValues values = new ContentValues();
		if (task.getId() == null) {
			// task created now, not taken from web DB
			values.put(TasksOpenHelper.TASKS_DIRTY_COLUMN, 1);
		} else {
			// task downloaded from web
			values.put(TasksOpenHelper.TASKS_ID_COLUMN, task.getId());
			values.put(TasksOpenHelper.TASKS_DIRTY_COLUMN, 0);
		}
		values.put(TasksOpenHelper.TASKS_TITLE_COLUMN, task.getTitle());
		if (task.isChecked()) {
			values.put(TasksOpenHelper.TASKS_CHECKED_COLUMN, 1);
		}
		String listId = list.getId();
		if (listId == null) {
			listId = Long.toString(list.getInternalId());
		}
		values.put(TasksOpenHelper.TASKS_LIST_ID_COLUMN, listId);
		long rowId = db.insert(TasksOpenHelper.TASKS_TABLE_NAME, null, values );
		if (rowId != -1) {
			task.setInternalId(rowId);
			task.setParentListId(list.getId());
			return true;
		}
		
		return false;
	}
	
	public void checkForUpdateTask(MyTask task, MyTasksList list) {
		openDb();
		Cursor cursor = null;
		try {
			cursor = db.query(
					TasksOpenHelper.TASKS_TABLE_NAME,
					new String[] {
							TasksOpenHelper.TASKS_INTERNAL_ID_COLUMN,
							TasksOpenHelper.TASKS_DIRTY_COLUMN,
							TasksOpenHelper.TASKS_TITLE_COLUMN,
							TasksOpenHelper.TASKS_CHECKED_COLUMN
					},
					TasksOpenHelper.TASKS_ID_COLUMN + " = ?",
					new String[] {
							task.getId()
					}, null, null, null);
			if (cursor.moveToFirst()) {
				if (cursor.getInt(1) > 0) {
					// dirty, ignore web data (local takes precedence)
					return;
				} else {
					if (!task.getTitle().equals(cursor.getString(2)) ||
							(task.isChecked() != (cursor.getInt(3) != 0))) {
						task.setInternalId(cursor.getLong(0));
						updateTask(task, false);
					}
				}
			} else {
				insertTask(task, list);
			}
		} finally {
			if (cursor != null) {
				cursor.close();
			}
		}
	}
	
	public void checkForUpdateList(MyTasksList list) {
		openDb();
		Cursor cursor = null;
		try {
			cursor = db.query(
					TasksOpenHelper.TASKLISTS_TABLE_NAME,
					new String[] {
							TasksOpenHelper.LISTS_INTERNAL_ID_COLUMN,
							TasksOpenHelper.LISTS_DIRTY_COLUMN,
							TasksOpenHelper.LISTS_TITLE_COLUMN
					},
					TasksOpenHelper.LISTS_ID_COLUMN + " = ?",
					new String[] {
							list.getId()
					}, null, null, null);
			if (cursor.moveToFirst()) {
				if (cursor.getInt(1) > 0) {
					// dirty, ignore web data (local takes precedence)
					return;
				} else {
					if (!list.getTitle().equals(cursor.getString(2))) {
						list.setInternalId(cursor.getLong(0));
						updateList(list, false);
					}
				}
			} else {
				insertList(list);
			}
		} finally {
			if (cursor != null) {
				cursor.close();
			}
		}
		
	}
	
	public void updateTask(MyTask task) {
		updateTask(task, true);
	}
	
	public void updateTask(MyTask task, boolean setDirty) {
		openDb();
		db.execSQL("update " + TasksOpenHelper.TASKS_TABLE_NAME +
				" set " + TasksOpenHelper.TASKS_TITLE_COLUMN + " = ?, " +
				TasksOpenHelper.TASKS_CHECKED_COLUMN + " = ?, " +
				TasksOpenHelper.TASKS_DIRTY_COLUMN + " = ?, " +
				TasksOpenHelper.TASKS_ID_COLUMN + " = ? " +
				"where " + TasksOpenHelper.TASKS_INTERNAL_ID_COLUMN + " = ?",
				new Object[] {
					task.getTitle(),
					task.isChecked()?1:0,
					setDirty?1:0,
					task.getId(),
					task.getInternalId()
				}
		);
	}
	
	public void updateList(MyTasksList list) {
		updateList(list, true);
	}
	
	public void updateList(MyTasksList list, boolean setDirty) {
		openDb();
		db.execSQL("update " + TasksOpenHelper.TASKLISTS_TABLE_NAME +
				" set " + TasksOpenHelper.LISTS_TITLE_COLUMN + " = ?, " +
				TasksOpenHelper.LISTS_ID_COLUMN + " = ?, " +
				TasksOpenHelper.LISTS_DIRTY_COLUMN + " = ? " + 
				"where " + TasksOpenHelper.LISTS_INTERNAL_ID_COLUMN + " = ?",
				new Object[] {
					list.getTitle(),
					list.getId(),
					setDirty?1:0,
					list.getInternalId()
				}
		);
	}

	public ArrayList<MyTasksList> getDirtyLists() {
		ArrayList<MyTasksList> result = new ArrayList<MyTasksList>();
		openDb();

		Cursor cursor = null;
		try {
			cursor = db.query(
					TasksOpenHelper.TASKLISTS_TABLE_NAME,
					fullListColumns,
					TasksOpenHelper.LISTS_DIRTY_COLUMN + "<> 0",
					null, null, null, null);
			while (cursor.moveToNext()) {
				MyTasksList list = getListFromCursor(cursor);
				result.add(list);
			}
		} finally {
			if (cursor != null) {
				cursor.close();
				cursor = null;
			}
		}
		
		return result;
	}

	public void newListUploaded(MyTasksList list) {
		openDb();
		// update the google id and reset the dirty flag
		db.execSQL("update " + TasksOpenHelper.TASKLISTS_TABLE_NAME +
				" set " + TasksOpenHelper.LISTS_DIRTY_COLUMN + " = 0, " +
				TasksOpenHelper.LISTS_ID_COLUMN + " = ? where " +
				TasksOpenHelper.LISTS_INTERNAL_ID_COLUMN + " = ?",
				new Object[] {
					list.getId(),
					list.getInternalId()
				}
		);
		// tasks were linked with internal ID, change it to google id
		db.execSQL("update " + TasksOpenHelper.TASKS_TABLE_NAME +
				" set " + TasksOpenHelper.TASKS_LIST_ID_COLUMN + " = ? where " +
				TasksOpenHelper.TASKS_LIST_ID_COLUMN + " = ?",
				new Object[] {
					list.getId(),
					list.getInternalId()
				}
		);
		// update the list object in availableLists with the external ID
		MyTasksList cachedList = findListByInternalId(list.getInternalId());
		
		if (cachedList != null) {
			cachedList.setId(list.getId());
		}
	}

	public void existingListUploaded(MyTasksList list) {
		openDb();
		// reset the dirty flag
		db.execSQL("update " + TasksOpenHelper.TASKLISTS_TABLE_NAME +
				" set " + TasksOpenHelper.LISTS_DIRTY_COLUMN + " = 0 where " +
				TasksOpenHelper.LISTS_INTERNAL_ID_COLUMN + " = ?",
				new Object[] {
					list.getInternalId()
				}
		);
	}

	public ArrayList<MyTask> getDirtyTasks() {
		ArrayList<MyTask> result = new ArrayList<MyTask>();
		openDb();

		Cursor cursor = null;
		try {
			cursor = db.query(
					TasksOpenHelper.TASKS_TABLE_NAME,
					fullTaskColumns,
					TasksOpenHelper.TASKS_DIRTY_COLUMN + "<> 0",
					null, null, null, null);
			while (cursor.moveToNext()) {
				MyTask task = getTaskFromCursor(cursor);
				result.add(task);
			}
		} finally {
			if (cursor != null) {
				cursor.close();
				cursor = null;
			}
		}
		
		return result;
	}

	public void newTaskUploaded(MyTask task) {
		openDb();
		// update the google id and reset the dirty flag
		db.execSQL("update " + TasksOpenHelper.TASKS_TABLE_NAME +
				" set " + TasksOpenHelper.TASKS_DIRTY_COLUMN + " = 0, " +
				TasksOpenHelper.TASKS_ID_COLUMN + " = ? where " +
				TasksOpenHelper.TASKS_INTERNAL_ID_COLUMN + " = ?",
				new Object[] {
					task.getId(),
					task.getInternalId()
				}
		);
	}

	public void existingTaskUploaded(MyTask task) {
		openDb();
		// reset the dirty flag
		db.execSQL("update " + TasksOpenHelper.TASKS_TABLE_NAME +
				" set " + TasksOpenHelper.TASKS_DIRTY_COLUMN + " = 0 where " +
				TasksOpenHelper.TASKS_INTERNAL_ID_COLUMN + " = ?",
				new Object[] {
					task.getInternalId()
				}
		);
	}

	public void deleteTask(MyTask task) {
		openDb();
		// set the deleted flag
		db.execSQL("update " + TasksOpenHelper.TASKS_TABLE_NAME +
				" set " + TasksOpenHelper.TASKS_DELETED_COLUMN + " = 1, " +
				TasksOpenHelper.TASKS_DIRTY_COLUMN + " = 1 where " +
				TasksOpenHelper.TASKS_INTERNAL_ID_COLUMN + " = ?",
				new Object[] {
					task.getInternalId()
				}
		);
	}

	public void deleteList(MyTasksList list) {
		openDb();
		// set the deleted and dirty flags
		db.execSQL("update " + TasksOpenHelper.TASKLISTS_TABLE_NAME +
				" set " + TasksOpenHelper.LISTS_DELETED_COLUMN + " = 1, " +
				TasksOpenHelper.LISTS_DIRTY_COLUMN + " = 1 where " +
				TasksOpenHelper.LISTS_INTERNAL_ID_COLUMN + " = ?",
				new Object[] {
					list.getInternalId()
				}
		);
		String listId = list.getId();
		if (listId == null) {
			listId = Long.toString(list.getInternalId());
		}
		// mark all tasks part of this list as deleted and not dirty
		db.execSQL("update " + TasksOpenHelper.TASKS_TABLE_NAME +
				" set " + TasksOpenHelper.TASKS_DELETED_COLUMN + " = 1, " +
				TasksOpenHelper.TASKS_DIRTY_COLUMN + " = 0 where " +
				TasksOpenHelper.TASKS_LIST_ID_COLUMN + " = ?",
				new Object[] {
					listId
				}
		);
		availableLists.remove(list);
	}

	public void deleteCompletedTasks(MyTasksList currentList) {
		String listId = currentList.getId();
		if (listId == null) {
			listId = Long.toString(currentList.getInternalId());
		}
		openDb();
		db.execSQL("update " + TasksOpenHelper.TASKS_TABLE_NAME +
				" set " + TasksOpenHelper.TASKS_DELETED_COLUMN + " = 1 where " +
				TasksOpenHelper.TASKS_CHECKED_COLUMN + " <> 0 and " +
				TasksOpenHelper.TASKS_LIST_ID_COLUMN + " = ?",
				new Object[] {
					listId
				}
		);
	}

	public MyTasksList findListByInternalId(long listId) {
		for (MyTaskBase list : availableLists) {
			if (list.getInternalId() == listId) {
				return (MyTasksList)list;
			}
		}
		
		return null;
	}
}
