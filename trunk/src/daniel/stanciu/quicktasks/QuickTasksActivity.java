package daniel.stanciu.quicktasks;

import java.util.List;

import daniel.stanciu.quicktasks.db.DbManager;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.GestureDetector;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;
import android.widget.ViewSwitcher;

public class QuickTasksActivity extends Activity {
	private static final String VIEWING_LISTS_PREF = "viewingLists";
	private static final String LIST_ID_PREF = "listId";
	public static final int REQUEST_AUTHENTICATE = 0;
	public static final int REQUEST_AUTHENTICATE_EXCEPTION = 1;

	protected static final String TAG = "QuickTasks";
	
	// logic
	public ViewType currentViewType = ViewType.LISTS;
	public MyTasksList currentList = new MyTasksList("@default", "Default list");
	
	public GestureDetector gestureDetector;
	private MyLinearLayout mainLayout;
	private MyArrayAdapter<MyTaskBase> arrayAdapter;
	private DbManager dbManager;
	private EditText newItemEditText;
	private ImageButton addItemButton;
	
	public GoogleTasksUtils gtasksUtils;
	private SharedPreferences settings;
	
	private boolean lastViewingListsPref;
	private long lastListIdPref;

    private ViewSwitcher viewSwitcher;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate()");
        gtasksUtils = new GoogleTasksUtils(this);
        
        setContentView(R.layout.main);
        MyOnGestureListener ogl = new MyOnGestureListener(this);
        gestureDetector = new GestureDetector(this.getApplicationContext(), ogl, null, false);
        gestureDetector.setOnDoubleTapListener(ogl);
        viewSwitcher = (ViewSwitcher)getWindow().findViewById(R.id.viewSwitcher);
        mainLayout = (MyLinearLayout)getWindow().findViewById(R.id.mainViewLayout1);
        ((MyScrollView)mainLayout.getParent()).setActivity(this);
        MyLinearLayout mainLayout2 = (MyLinearLayout)getWindow().findViewById(R.id.mainViewLayout2);
        ((MyScrollView)mainLayout2.getParent()).setActivity(this);
        newItemEditText = (EditText)getWindow().findViewById(R.id.newItemText);
        newItemEditText.clearFocus();
        addItemButton = (ImageButton)getWindow().findViewById(R.id.addItemButton);
        
        addItemButton.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				String title = newItemEditText.getText().toString().trim();
				if (title.isEmpty()) {
					Toast.makeText(QuickTasksActivity.this, R.string.empty_item_title, Toast.LENGTH_SHORT).show();
					return;
				}
				MyTaskBase newItem = null;
				if(currentViewType == ViewType.LISTS) {
					newItem = new MyTasksList(null, title);
					dbManager.insertList((MyTasksList)newItem);
					arrayAdapter.add(newItem);
					addView(arrayAdapter.getCount() - 1);
				} else {
					newItem = new MyTask(null, title);
					// set priority
					int priority = Integer.parseInt(PreferenceManager.getDefaultSharedPreferences(QuickTasksActivity.this).getString(QuickTasksActivity.this.getString(R.string.new_task_prio_pref_key), "1"));
					((MyTask)newItem).setPriority(priority);
					dbManager.insertTask((MyTask)newItem, currentList);
					int targetPos = arrayAdapter.getFirstPositionForPriority(((MyTask)newItem).getPriority() + 1);
					if (targetPos >= 0) {
						arrayAdapter.insert(newItem, targetPos);
						addView(targetPos);
					} else {
						arrayAdapter.add(newItem);
						addView(arrayAdapter.getCount() - 1);
					}
				}
				newItemEditText.clearComposingText();
				newItemEditText.setText("");
			}
		});
        
		dbManager = new DbManager(getApplicationContext(), this);
		
		settings = getPreferences(QuickTasksActivity.MODE_PRIVATE);
		lastViewingListsPref = settings.getBoolean(VIEWING_LISTS_PREF, true);
		if (!lastViewingListsPref) {
			lastListIdPref = settings.getLong(LIST_ID_PREF, -1);
			if (lastListIdPref != -1) {
				currentList = dbManager.findListByInternalId(lastListIdPref);
				if (currentList != null) {
					currentViewType = ViewType.TASKS;
				}
			}
		}
		
        getDataFromDB();
        populateView();
        if (shouldUpdate()) {
        	gtasksUtils.gotAccount(false, false);
        }
        
    }
    
    public void getDataFromDB() {
		List<MyTaskBase> taskTitles = null;
		if (currentViewType == ViewType.LISTS) {
			if (Build.VERSION.SDK_INT >= 11) {
				setTitle("Available Lists");
			} else {
				setTitle("QuickTasks - Available Lists");
			}
			taskTitles = dbManager.getLists();
		} else {
			if (Build.VERSION.SDK_INT >= 11) {
				setTitle(currentList.getTitle());
			} else {
				setTitle("QuickTasks - " + currentList.getTitle());
			}
			taskTitles = dbManager.getTasks(currentList);
		}
		
		if (arrayAdapter == null) {
			arrayAdapter = new MyArrayAdapter<MyTaskBase>(this, android.R.layout.simple_list_item_1, taskTitles, gestureDetector);
		} else {
			arrayAdapter.setNotifyOnChange(false);
			arrayAdapter.clear();
			for (MyTaskBase base : taskTitles) {
				arrayAdapter.add(base);
			}
			arrayAdapter.setNotifyOnChange(true);
			arrayAdapter.notifyDataSetChanged();
		}
	}

	private boolean shouldUpdate() {
		boolean dbEmpty = dbManager.isDbEmpty();
		if ((isEmulator() && dbEmpty) || (!isEmulator() && isNetworkAvailable() && (dbEmpty || dbManager.isDbDirty())))
			return true;
		
		return false;
	}

	private boolean isNetworkAvailable() {
		ConnectivityManager conMgr = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo netInfo = conMgr.getActiveNetworkInfo();
		if (netInfo != null && netInfo.isConnected()) {
			return true;
		}
		return false;
	}

	public static boolean isEmulator() {
    	return "sdk".equals(Build.PRODUCT) || "google_sdk".equals(Build.PRODUCT);
    }

	@Override
	public void onBackPressed() {
		if (currentViewType == ViewType.TASKS) {
			mainLayout.clearFocus();
			if (arrayAdapter.exitEditMode()) {
				return;
			}
			currentViewType = ViewType.LISTS;
			getDataFromDB();
			populateView();
			//gotAccount();
		} else {
			super.onBackPressed();
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
        Log.d(TAG, "onResume()");
		getDataFromDB();
		populateView();
		//gotAccount();
	}

	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		if (savedInstanceState.containsKey("new_task_text")) {
			newItemEditText.setText(savedInstanceState.getCharSequence("new_task_text"));
		}
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		outState.putString("new_task_text", newItemEditText.getText().toString());
	}

	@Override
	protected void onPause() {
        Log.d(TAG, "onPause()");
		dbManager.closeDb();
		if (currentViewType == ViewType.LISTS) {
			if (!lastViewingListsPref) {
				Log.d(TAG, "onPause() - saving new view details - lists");
				// save only if something changed
				Editor editor = settings.edit();
				editor.putBoolean(VIEWING_LISTS_PREF, true);
				editor.apply();
				lastViewingListsPref = true;
			}
		} else {
			if (lastViewingListsPref || lastListIdPref != currentList.getInternalId()) {
				// save only if something changed
				Log.d(TAG, "onPause() - saving new view details - tasks");
				Editor editor = settings.edit();
				editor.putBoolean(VIEWING_LISTS_PREF, false);
				editor.putLong(LIST_ID_PREF, currentList.getInternalId());
				editor.apply();
				lastViewingListsPref = false;
				lastListIdPref = currentList.getInternalId();
			}
		}
		super.onPause();
	}


	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.main_menu, menu);
		if (gtasksUtils.accountManager.getAccounts().length < 2) {
			menu.findItem(R.id.switch_acc_item).setVisible(false);
		}
		
//		if (gtasksUtils.accountManager.getAccounts().length >= 2) {
//			menu.add(0, MENU_ACCOUNTS, 0, R.string.switch_acc_item);
//		}
//		menu.add(0, MENU_SYNC, 0, R.string.sync_item);
//		menu.add(0, MENU_DEL_DONE, 0, R.string.delete_done_item);
		return true;
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		if (currentViewType == ViewType.LISTS) {
			menu.findItem(R.id.uncheck_all_item).setVisible(false);
		} else {
			menu.findItem(R.id.uncheck_all_item).setVisible(true);
		}
		
		return super.onPrepareOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.switch_acc_item:
			gtasksUtils.chooseAccount(false);
			return true;
		case R.id.sync_item:
			if (!isEmulator()) {
				if (isNetworkAvailable()) {
					gtasksUtils.gotAccount(false, false);
				} else {
					Toast.makeText(this, R.string.no_network, Toast.LENGTH_LONG).show();
				}
			}
			return true;
		case R.id.delete_done_item:
			deleteCompletedTasks();
			return true;
		case R.id.settings_item:
			Intent settingsIntent = new Intent();
			settingsIntent.setClass(this, SettingsActivity.class);
			startActivity(settingsIntent);
			return true;
		case R.id.uncheck_all_item:
			uncheckCompletedTasks();
			return true;
		}
		return false;
	}

	private void deleteCompletedTasks() {
		if (currentViewType == ViewType.LISTS) {
			return;
		}
		dbManager.deleteCompletedTasks(currentList);
		getDataFromDB();
		populateView();
	}
	
	private void uncheckCompletedTasks() {
		if (currentViewType == ViewType.LISTS) {
			return;
		}
		dbManager.uncheckCompletedTasks(currentList);
		getDataFromDB();
		populateView();
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		switch (requestCode) {
		case REQUEST_AUTHENTICATE:
			if (resultCode == RESULT_OK) {
				gtasksUtils.gotAccount(false, false);
			} else {
				gtasksUtils.chooseAccount(false);
			}
			break;
		case REQUEST_AUTHENTICATE_EXCEPTION:
			if (resultCode == RESULT_OK) {
				gtasksUtils.gotAccount(true, true);
			} else {
				gtasksUtils.chooseAccount(true);
			}
			break;
		}
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		return gestureDetector.onTouchEvent(event);
	}

	public void populateView() {
		mainLayout.removeAllViews();
		for (int i = 0; i < arrayAdapter.getCount(); ++i) {
			View v = createView(i);
			mainLayout.addView(v);
		}
		mainLayout.requestLayout();
		mainLayout.invalidate();
	}

	private View createView(int position) {
		View result = arrayAdapter.getView(position, null, mainLayout);
		return result;
	}
	
	public void selectNextList() {
		viewSwitcher.setInAnimation(AnimationUtils.makeInAnimation(this, false));
		viewSwitcher.setOutAnimation(AnimationUtils.makeOutAnimation(this, false));
		int i = dbManager.availableLists.indexOf(currentList);
		if (i == -1 || i == dbManager.availableLists.size() - 1) {
			currentList = (MyTasksList)dbManager.availableLists.get(0);
		} else {
			currentList = (MyTasksList)dbManager.availableLists.get(i + 1);
		}
		View nextView = viewSwitcher.getNextView();
		mainLayout = (MyLinearLayout)nextView.findViewById(R.id.mainViewLayout1);
		if (mainLayout == null) {
			mainLayout = (MyLinearLayout)nextView.findViewById(R.id.mainViewLayout2);
		}
		getDataFromDB();
		populateView();
		//gotAccount();
		viewSwitcher.showNext();
	}
	
	public void selectPrevList() {
		viewSwitcher.setInAnimation(AnimationUtils.makeInAnimation(this, true));
		viewSwitcher.setOutAnimation(AnimationUtils.makeOutAnimation(this, true));
		int i = dbManager.availableLists.indexOf(currentList);
		if (i == -1) {
			currentList = (MyTasksList)dbManager.availableLists.get(0);
		} else if (i == 0) {
			currentList = (MyTasksList)dbManager.availableLists.get(dbManager.availableLists.size() - 1);
		} else {
			currentList = (MyTasksList)dbManager.availableLists.get(i - 1);
		}
		View nextView = viewSwitcher.getNextView();
		mainLayout = (MyLinearLayout)nextView.findViewById(R.id.mainViewLayout1);
		if (mainLayout == null) {
			mainLayout = (MyLinearLayout)nextView.findViewById(R.id.mainViewLayout2);
		}
		getDataFromDB();
		populateView();
		//gotAccount();
		viewSwitcher.showNext();
	}
	
	public void onRightToLeftSwipe() {
		selectNextList();
	}
	
	public void onLeftToRightSwipe() {
		selectPrevList();
	}
	
	public void clearFocus() {
		mainLayout.clearFocus();
	}
	
	public void replaceView(int position) {
		if (mainLayout.getChildAt(position) != null) {
			mainLayout.removeViewAt(position);
			mainLayout.addView(createView(position), position);
			mainLayout.requestLayout();
			mainLayout.invalidate();
		}
	}

	public DbManager getDbManager() {
		return dbManager;
	}

	public void deleteView(int position) {
		if (mainLayout.getChildAt(position) != null) {
			mainLayout.removeViewAt(position);
			mainLayout.requestLayout();
			mainLayout.invalidate();
		}
	}
	
	public void addView(int position) {
		mainLayout.addView(createView(position), position);
		mainLayout.requestLayout();
		mainLayout.invalidate();
	}
	
	public void moveViewToEnd(int position) {
		View view = mainLayout.getChildAt(position);
		if (view != null) {
			mainLayout.removeViewAt(position);
			mainLayout.addView(view);
			mainLayout.requestLayout();
			mainLayout.invalidate();
		}
	}
	
	public void moveViewToPosition(int startPos, int targetPos) {
		View view = mainLayout.getChildAt(startPos);
		
		if (view != null) {
			mainLayout.removeViewAt(startPos);
			if (startPos < targetPos) {
				targetPos --;
			}
			mainLayout.addView(view, targetPos);
			mainLayout.requestLayout();
			mainLayout.invalidate();
		}
	}
}