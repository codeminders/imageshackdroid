package com.codeminders.imageshackdroid.activities;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.*;

import com.codeminders.imageshackdroid.Constants;
import com.codeminders.imageshackdroid.R;

/**
 * @author Igor Giziy <linsalion@gmail.com>
 */
public class PreferencesActivity extends Activity {
    private SharedPreferences prefs;
    private CheckBox checkBox, wifiCheckBox;
    private EditText editText;
    private Spinner spinner;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.preferences);
        setTitle(getString(R.string.sync_settings_title));

        prefs = getSharedPreferences(Constants.PREFS_NAME, Context.MODE_PRIVATE);
        checkBox = (CheckBox) findViewById(R.id.prefs_ckbx);
        wifiCheckBox = (CheckBox) findViewById(R.id.prefs_ckbx_wifi);

        String[] times = {
                getString(R.string.time_5_min),
                getString(R.string.time_15_min),
                getString(R.string.time_30_min),
                getString(R.string.time_1_hour),
                getString(R.string.time_2_hours),
                getString(R.string.time_4_hours),
                getString(R.string.time_8_hours),
                getString(R.string.time_12_hours),
                getString(R.string.time_24_hours),
        };
        spinner = (Spinner) findViewById(R.id.spinner1);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, times);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner .setAdapter(adapter);

        editText = (EditText) findViewById(R.id.prefs_tags);

        Button save = (Button) findViewById(R.id.prefs_save);
        save.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                SharedPreferences.Editor editor = prefs.edit();
                editor.putString(Constants.FIELD_PUBLIC, checkBox.isChecked() ? "private" : "public");
                String tags = editText.getText().toString();
                if (tags.length() > 0) {
                    editor.putString(Constants.FIELD_TAGS, tags);
                }
                editor.putInt(Constants.SYNC_INTERVAL, spinner.getSelectedItemPosition());
                editor.putBoolean(Constants.SYNC_TYPE, wifiCheckBox.isChecked());
                editor.commit();
                Toast.makeText(getApplicationContext(), R.string.conf_save, Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();

        String visibility = prefs.getString(Constants.FIELD_PUBLIC, null);
        if (visibility != null) {
            checkBox.setChecked(visibility.equals("private"));
        }
        String tags = prefs.getString(Constants.FIELD_TAGS, null);
        if (tags != null) {
            editText.setText(tags);
        }
        Boolean onlyWifi = prefs.getBoolean(Constants.SYNC_TYPE, false);
        wifiCheckBox.setChecked(onlyWifi);

        int itemPosition = prefs.getInt(Constants.SYNC_INTERVAL, 0);
        spinner.setSelection(itemPosition);
    }

}
