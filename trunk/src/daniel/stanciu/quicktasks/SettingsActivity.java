package daniel.stanciu.quicktasks;

import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.PreferenceActivity;

public class SettingsActivity extends PreferenceActivity implements OnSharedPreferenceChangeListener {
	public static String NEW_TASK_PRIO_PREF_KEY = null;
	@SuppressWarnings("deprecation")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.preferences);
		if (NEW_TASK_PRIO_PREF_KEY == null) {
			NEW_TASK_PRIO_PREF_KEY = getString(R.string.new_task_prio_pref_key);
		}
		onSharedPreferenceChanged(getPreferenceScreen().getSharedPreferences(), NEW_TASK_PRIO_PREF_KEY);
	}

	@SuppressWarnings("deprecation")
	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
		if (key.equals(NEW_TASK_PRIO_PREF_KEY)) {
			ListPreference newTaskPrio = (ListPreference)findPreference(key);
			int valueIndex = newTaskPrio.findIndexOfValue(sharedPreferences.getString(key, ""));
			if (valueIndex != -1) {
				newTaskPrio.setSummary(newTaskPrio.getEntries()[valueIndex]);
			} else {
				newTaskPrio.setSummary("");
			}
		}
	}

	@SuppressWarnings("deprecation")
	@Override
	protected void onPause() {
		super.onPause();
		getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
	}

	@SuppressWarnings("deprecation")
	@Override
	protected void onResume() {
		super.onResume();
		getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
	}

}
