package daniel.stanciu.quicktasks;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AccountManagerCallback;
import android.accounts.AccountManagerFuture;
import android.accounts.AuthenticatorException;
import android.accounts.OperationCanceledException;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.google.api.client.extensions.android2.AndroidHttp;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.extensions.android2.auth.GoogleAccountManager;
import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.client.googleapis.services.GoogleKeyInitializer;
import com.google.api.client.http.HttpTransport;
import com.google.api.services.tasks.Tasks;
import com.google.api.services.tasks.model.Task;
import com.google.api.services.tasks.model.TaskList;
import com.google.api.services.tasks.model.TaskLists;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson.JacksonFactory;

import daniel.stanciu.quicktasks.db.DbManager;


public class GoogleTasksUtils {
	private QuickTasksActivity activity;
	private static final Level LOGGING_LEVEL = Level.OFF;
	// Google API related members
	private Tasks service;
	private final HttpTransport transport = AndroidHttp.newCompatibleTransport();
	private final JsonFactory jsonFactory = new JacksonFactory();
	private GoogleCredential credential = new GoogleCredential();
	private String accountName;
	public GoogleAccountManager accountManager;
	// This must be the exact string, and is a special for alias OAuth 2 scope
	// "https://www.googleapis.com/auth/tasks"
	private static final String AUTH_TOKEN_TYPE = "Manage your tasks";

	// settings related members
	private SharedPreferences settings;
	public static final String PREF_ACCOUNT_NAME = "accountName";
	public static final String PREF_AUTH_TOKEN = "authToken";
	
	public GoogleTasksUtils(QuickTasksActivity activity) {
		this.activity = activity;
        service = Tasks.builder(transport, jsonFactory).setApplicationName("QuickTasks")
        		.setHttpRequestInitializer(credential)
        		.setJsonHttpRequestInitializer(new GoogleKeyInitializer(ClientCredentials.KEY))
        		.build();
        settings = activity.getPreferences(QuickTasksActivity.MODE_PRIVATE);
        accountName = settings.getString(PREF_ACCOUNT_NAME, null);
        credential.setAccessToken(settings.getString(PREF_AUTH_TOKEN, null));
        Logger.getLogger("com.google.api.client").setLevel(LOGGING_LEVEL);
        accountManager = new GoogleAccountManager(activity);

	}
	
	@SuppressWarnings("deprecation")
	public void gotAccount(boolean fromException, boolean isRetry) {
		if (QuickTasksActivity.isEmulator()) {
			onAuthToken(fromException, isRetry);
			return;
		}
		Account account = accountManager.getAccountByName(accountName);
		if (account == null) {
			chooseAccount(fromException);
			return;
		}
		
		if (credential.getAccessToken() != null) {
			onAuthToken(fromException, isRetry);
			return;
		}
		
		accountManager.getAccountManager().getAuthToken(
				account, AUTH_TOKEN_TYPE, true, new GetTokenAccountManagerCallback(fromException, isRetry), null);
	}

	public void chooseAccount(boolean fromException) {
		accountManager.getAccountManager().getAuthTokenByFeatures(GoogleAccountManager.ACCOUNT_TYPE,
				AUTH_TOKEN_TYPE,
				null,
				activity,
				null,
				null,
				new GetAccountAndTokenAccountManagerCallback(fromException), null);
	}

	void setAccountName(String accountName) {
		SharedPreferences.Editor editor = settings.edit();
		editor.putString(PREF_ACCOUNT_NAME, accountName);
		editor.commit();
		this.accountName = accountName;
	}

	void setAuthToken(String authToken) {
		SharedPreferences.Editor editor = settings.edit();
		editor.putString(PREF_AUTH_TOKEN, authToken);
		editor.commit();
		credential.setAccessToken(authToken);
	}

	private void onAuthToken(boolean fromException, boolean isRetry) {
		if (QuickTasksActivity.isEmulator()) {
			getDebugTaskLists();
			getDebugTasks();
			activity.getDataFromDB();
			activity.populateView();
		} else {
			if (fromException) {
				// not in main UI thread, execute in the same thread
				try {
					boolean result = activity.gtasksUtils.downloadUpdates();
					if (result) {
						result = activity.gtasksUtils.updateDirty();
					}
					if (result) {
						Toast.makeText(activity, "Synchronization successfull", Toast.LENGTH_SHORT).show();
						activity.getDataFromDB();
						activity.populateView();
					}
				} catch (IOException ex) {
					Toast.makeText(activity, "Synchronization failed: " + ex.getLocalizedMessage(), Toast.LENGTH_LONG).show();
				}
			} else {
				// in main UI thread, do the synchronization in new thread
				new GoogleTasksSyncTask(activity, isRetry).execute();
			}
//			downloadUpdates();
//			updateDirty();
//			getDataFromDB();
//			populateView();
		}
	}

	private void getDebugTasks() {
		DbManager dbManager = activity.getDbManager();
		dbManager.insertTask(new MyTask("t101", "List 1 task 1 which is longer to see what happens with the editor"),
				(MyTasksList)dbManager.availableLists.get(0));
		dbManager.insertTask(new MyTask("t102", "List 1 task 2"),
				(MyTasksList)dbManager.availableLists.get(0));
		dbManager.insertTask(new MyTask("t103", "List 1 task 3"),
				(MyTasksList)dbManager.availableLists.get(0));
		dbManager.insertTask(new MyTask("t104", "List 1 task 4"),
				(MyTasksList)dbManager.availableLists.get(0));
		dbManager.insertTask(new MyTask("t105", "List 1 task 5"),
				(MyTasksList)dbManager.availableLists.get(0));
		dbManager.insertTask(new MyTask("t106", "List 1 task 6"),
				(MyTasksList)dbManager.availableLists.get(0));
		dbManager.insertTask(new MyTask("t107", "List 1 task 7"),
				(MyTasksList)dbManager.availableLists.get(0));
		dbManager.insertTask(new MyTask("t108", "List 1 task 8"),
				(MyTasksList)dbManager.availableLists.get(0));
		dbManager.insertTask(new MyTask("t109", "List 1 task 9"),
				(MyTasksList)dbManager.availableLists.get(0));
		dbManager.insertTask(new MyTask("t110", "List 1 task 10"),
				(MyTasksList)dbManager.availableLists.get(0));
		dbManager.insertTask(new MyTask("t111", "List 1 task 11"),
				(MyTasksList)dbManager.availableLists.get(0));
		dbManager.insertTask(new MyTask("t112", "List 1 task 12"),
				(MyTasksList)dbManager.availableLists.get(0));
		dbManager.insertTask(new MyTask("t113", "List 1 task 13"),
				(MyTasksList)dbManager.availableLists.get(0));
		dbManager.insertTask(new MyTask("t114", "List 1 task 14"),
				(MyTasksList)dbManager.availableLists.get(0));
		dbManager.insertTask(new MyTask("t115", "List 1 task 15"),
				(MyTasksList)dbManager.availableLists.get(0));
		dbManager.insertTask(new MyTask("t116", "List 1 task 16"),
				(MyTasksList)dbManager.availableLists.get(0));
		dbManager.insertTask(new MyTask("t117", "List 1 task 17"),
				(MyTasksList)dbManager.availableLists.get(0));
		dbManager.insertTask(new MyTask("t201", "List 2 task 1"),
				(MyTasksList)dbManager.availableLists.get(1));
		dbManager.insertTask(new MyTask("t202", "List 2 task 2"),
				(MyTasksList)dbManager.availableLists.get(1));
		dbManager.insertTask(new MyTask("t203", "List 2 task 3"),
				(MyTasksList)dbManager.availableLists.get(1));
		dbManager.insertTask(new MyTask("t204", "List 2 task 4"),
				(MyTasksList)dbManager.availableLists.get(1));
		dbManager.insertTask(new MyTask("t205", "List 2 task 5"),
				(MyTasksList)dbManager.availableLists.get(1));
		dbManager.insertTask(new MyTask("t206", "List 2 task 6"),
				(MyTasksList)dbManager.availableLists.get(1));
	}

	private void getDebugTaskLists() {
		MyTasksList myList = new MyTasksList("list1", "list1");
		activity.getDbManager().insertList(myList);
		myList = new MyTasksList("list2", "list2");
		activity.getDbManager().insertList(myList);
	}

	private void handleGoogleException(IOException e) throws IOException {
		if (e instanceof GoogleJsonResponseException) {
			GoogleJsonResponseException exception = (GoogleJsonResponseException) e;

			if (exception.getStatusCode() == 401) {
				accountManager.invalidateAuthToken(credential.getAccessToken());
				credential.setAccessToken(null);
				SharedPreferences.Editor editor2 = settings.edit();
				editor2.remove(PREF_AUTH_TOKEN);
				editor2.commit();
				return;
			}
		}
		Log.e(QuickTasksActivity.TAG, e.getMessage(), e);
		//Toast.makeText(activity, "Exception occurred: " + e.getLocalizedMessage(), Toast.LENGTH_LONG).show();
		throw e;
	}

	public boolean updateDirty() throws IOException {
		try {
			DbManager dbManager = activity.getDbManager();
			ArrayList<MyTasksList> dirtyLists = dbManager.getDirtyLists();
			for (MyTasksList list : dirtyLists) {
				if (list.getId() == null) {
					if (list.isDeleted()) {
						// list not uploaded yet but already deleted, just mark as not dirty
						dbManager.existingListUploaded(list);
					} else {
						TaskList webList = new TaskList();
						webList.setTitle(list.getTitle());
						Log.d(QuickTasksActivity.TAG, "About to insert list");
						TaskList result = service.tasklists().insert(webList).execute();
						list.setId(result.getId());
						dbManager.newListUploaded(list);
					}
				} else {
					if (list.isDeleted()) {
						Log.d(QuickTasksActivity.TAG, "About to delete list");
						service.tasklists().delete(list.getId()).execute();
					} else {
						TaskList result = service.tasklists().get(list.getId()).execute();
						result.setTitle(list.getTitle());
						Log.d(QuickTasksActivity.TAG, "About to update list");
						service.tasklists().update(list.getId(), result).execute();
					}
					dbManager.existingListUploaded(list);
				}
			}
			
			ArrayList<MyTask> dirtyTasks = dbManager.getDirtyTasks();
			for (MyTask task : dirtyTasks) {
				if (task.getId() == null) {
					if (task.isDeleted()) {
						// task not uploaded yet but already deleted, just mark as not dirty
						dbManager.existingTaskUploaded(task);
					} else {
						Task webTask = new Task();
						webTask.setTitle(task.getTitle());
						if (task.isChecked()) {
							webTask.setStatus(MyTask.STATUS_COMPLETED);
						} else {
							webTask.setStatus(MyTask.NEEDS_ACTION);
						}
						Log.d(QuickTasksActivity.TAG, "About to insert task");
						Task result = service.tasks().insert(task.getParentListId(), webTask).execute();
						task.setId(result.getId());
						dbManager.newTaskUploaded(task);
					}
				} else {
					if (task.isDeleted()) {
						Log.d(QuickTasksActivity.TAG, "About to delete task");
						service.tasks().delete(task.getParentListId(), task.getId()).execute();
					} else {
						Task result = service.tasks().get(task.getParentListId(), task.getId()).execute();
						result.setTitle(task.getTitle());
						if (task.isChecked()) {
							result.setStatus(MyTask.STATUS_COMPLETED);
						} else {
							result.setStatus(MyTask.NEEDS_ACTION);
							result.setCompleted(null);
						}
						Log.d(QuickTasksActivity.TAG, "About to update task");
						service.tasks().update(task.getParentListId(), task.getId(), result).execute();
					}
					dbManager.existingTaskUploaded(task);
				}
			}
		} catch (IOException e) {
			handleGoogleException(e);
			return false;
		}
		
		return true;		
	}

	public boolean downloadUpdates() throws IOException {
		try {
			DbManager dbManager = activity.getDbManager();
			TaskLists tasklists = service.tasklists().list().execute();
			if (tasklists != null && !tasklists.isEmpty()) {
				for (TaskList list : tasklists.getItems()) {
					MyTasksList myList = new MyTasksList(list.getId(), list.getTitle());
					dbManager.checkForUpdateList(myList);
				}
			}

			for (MyTaskBase base : dbManager.availableLists) {
				if (base.getId() != null) {
					// existing list, check if the tasks inside need updating
					List<Task> tasks = service.tasks().list(base.getId()).execute().getItems();
					if (tasks != null) {
						for (Task task : tasks) {
							MyTask myTask = new MyTask(task.getId(), task.getTitle());
							myTask.setChecked(MyTask.STATUS_COMPLETED.equals(task.getStatus()));
							dbManager.checkForUpdateTask(myTask, (MyTasksList)base);
						}
					}
				}
			}
		} catch (IOException e) {
			handleGoogleException(e);
			return false;
		}
		return true;
	}

	private final class GetAccountAndTokenAccountManagerCallback implements
			AccountManagerCallback<Bundle> {
		boolean fromException = false;
		
		public GetAccountAndTokenAccountManagerCallback(boolean fromException) {
			this.fromException = fromException;
		}
		
		@Override
		public void run(AccountManagerFuture<Bundle> future) {
			try {
				Bundle bundle = future.getResult();
				setAccountName(bundle.getString(AccountManager.KEY_ACCOUNT_NAME));
				setAuthToken(bundle.getString(AccountManager.KEY_AUTHTOKEN));
				onAuthToken(fromException, false);
			} catch (OperationCanceledException e) {
				
			} catch (AuthenticatorException e) {
				Log.e(QuickTasksActivity.TAG, e.getMessage(), e);
			} catch (IOException e) {
				Log.e(QuickTasksActivity.TAG, e.getMessage(), e);
			}
		}
	}

	private final class GetTokenAccountManagerCallback implements
			AccountManagerCallback<Bundle> {
		boolean fromException = false;
		boolean isRetry = false;

		public GetTokenAccountManagerCallback(boolean fromException, boolean isRetry) {
			super();
			this.fromException = fromException;
			this.isRetry = isRetry;
		}

		@Override
		public void run(AccountManagerFuture<Bundle> future) {
			try {
				Bundle bundle = future.getResult();
				if (bundle.containsKey(AccountManager.KEY_INTENT)) {
					Intent intent = bundle
							.getParcelable(AccountManager.KEY_INTENT);
					intent.setFlags(intent.getFlags()
							& ~Intent.FLAG_ACTIVITY_NEW_TASK);
					if (fromException) {
						activity.startActivityForResult(intent,
								QuickTasksActivity.REQUEST_AUTHENTICATE);
					} else {
						activity.startActivityForResult(
								intent,
								QuickTasksActivity.REQUEST_AUTHENTICATE_EXCEPTION);
					}
				} else if (bundle.containsKey(AccountManager.KEY_AUTHTOKEN)) {
					setAuthToken(bundle.getString(AccountManager.KEY_AUTHTOKEN));
					onAuthToken(fromException, isRetry);
				}
			} catch (Exception e) {
				Log.e(QuickTasksActivity.TAG, e.getMessage(), e);
			}

		}
	}

}
