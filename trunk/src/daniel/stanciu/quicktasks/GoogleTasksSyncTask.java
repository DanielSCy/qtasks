package daniel.stanciu.quicktasks;

import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

public class GoogleTasksSyncTask extends AsyncTask<Void, Void, GoogleTasksSyncResult> {
	private QuickTasksActivity activity;
	private boolean isRetry = false;
	
	public GoogleTasksSyncTask(QuickTasksActivity act, boolean isRetry) {
		activity = act;
		this.isRetry = isRetry;
//		Log.d(QuickTasksActivity.TAG, "GoogleTasksSyncTask created from:");
//		for (StackTraceElement trace : Thread.currentThread().getStackTrace()) {
//			Log.d(QuickTasksActivity.TAG, "\t" + trace.toString());
//		}
	}

	@Override
	protected GoogleTasksSyncResult doInBackground(Void... params) {
		try {
			boolean result = activity.gtasksUtils.downloadUpdates();
			if (result) {
				result = activity.gtasksUtils.updateDirty();
			}
			if (result) {
				return GoogleTasksSyncResult.SUCCESS;
			} else {
				return GoogleTasksSyncResult.AUTH_RETRY;
			}
		} catch (Throwable th) {
			Log.e("QuickTasksActivity", "Failed to update on-line tasks", th);
			return new GoogleTasksSyncResult(GoogleTasksSyncResult.FAILED, th.getLocalizedMessage());
		}
	}

	@Override
	protected void onPostExecute(GoogleTasksSyncResult result) {
		if (result.getStatus() == GoogleTasksSyncResult.OK) {
			Toast.makeText(activity, "Synchronization successfull", Toast.LENGTH_SHORT).show();
			activity.getDataFromDB();
			activity.populateView();
		} else if (result.getStatus() == GoogleTasksSyncResult.RETRY) {
			//Toast.makeText(activity, "Synchronization failed, will retry: " + result.getMessage(), Toast.LENGTH_SHORT).show();
			activity.gtasksUtils.gotAccount(false, true);
		} else {
			Toast.makeText(activity, "Synchronization failed: " + result.getMessage(), Toast.LENGTH_LONG).show();
		}
	}

	@Override
	protected void onPreExecute() {
		if (!isRetry) {
			Toast.makeText(activity, "Starting synchronization", Toast.LENGTH_SHORT).show();
		}
		super.onPreExecute();
	}

}
