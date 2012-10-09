package daniel.stanciu.quicktasks;

import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.PreferenceActivity;
import android.support.v4.app.NavUtils;
import android.view.MenuItem;

public class SettingsActivity extends PreferenceActivity implements OnSharedPreferenceChangeListener {
	public static String NEW_TASK_PRIO_PREF_KEY = null;
	public static String THEME_PREF_KEY = null;
	@SuppressWarnings("deprecation")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        getActionBar().setDisplayHomeAsUpEnabled(true);
		addPreferencesFromResource(R.xml.preferences);
		if (NEW_TASK_PRIO_PREF_KEY == null) {
			NEW_TASK_PRIO_PREF_KEY = getString(R.string.new_task_prio_pref_key);
		}
		if (THEME_PREF_KEY == null) {
			THEME_PREF_KEY = getString(R.string.theme_pref_key);
		}
		onSharedPreferenceChanged(getPreferenceScreen().getSharedPreferences(), NEW_TASK_PRIO_PREF_KEY);
		onSharedPreferenceChanged(getPreferenceScreen().getSharedPreferences(), THEME_PREF_KEY);
	}

	@SuppressWarnings("deprecation")
	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
		if (key.equals(NEW_TASK_PRIO_PREF_KEY) || key.equals(THEME_PREF_KEY)) {
			ListPreference newPref = (ListPreference)findPreference(key);
			int valueIndex = newPref.findIndexOfValue(sharedPreferences.getString(key, ""));
			if (valueIndex != -1) {
				newPref.setSummary(newPref.getEntries()[valueIndex]);
			} else {
				newPref.setSummary("");
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

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(this);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
    
    @Override
    public void onBackPressed() {
        NavUtils.navigateUpFromSameTask(this);
    }
}
