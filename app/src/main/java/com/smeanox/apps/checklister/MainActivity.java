package com.smeanox.apps.checklister;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);

		FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
		fab.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				addMenuItem(view.getRootView());
			}
		});
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.menu_main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();

		//noinspection SimplifiableIfStatement
		if (id == R.id.action_settings) {
			Intent intent = new Intent(this, SettingsActivity.class);
			startActivity(intent);
			return true;
		}

		return super.onOptionsItemSelected(item);
	}

	public void addMenuItem(View view) {
		LayoutInflater layoutInflater = getLayoutInflater();
		LinearLayout checkBox = (LinearLayout) layoutInflater.inflate(R.layout.checklist_item, null);
		((CheckBox) checkBox.findViewById(R.id.checklistItem)).setText(R.string.new_item);

		LinearLayout layout = (LinearLayout) findViewById(R.id.mainLayout);
		layout.addView(checkBox);
	}

	private View oldEditCheckbox;

	public void editCheckbox(View view) {
		View parent = (View) view.getParent();
		if(oldEditCheckbox == parent){
			endEditCheckbox(parent);
			return;
		}
		if(oldEditCheckbox != null){
			endEditCheckbox(oldEditCheckbox);
		}
		startEditCheckbox(parent);
	}

	private void startEditCheckbox(View view) {
		((EditText) view.findViewById(R.id.checklistEdit)).setText(((TextView) view.findViewById(R.id.checklistItem)).getText());
		view.findViewById(R.id.checklistItem).setVisibility(View.GONE);
		view.findViewById(R.id.checklistEdit).setVisibility(View.VISIBLE);
		oldEditCheckbox = view;
	}

	private void endEditCheckbox(View view){
		((TextView) view.findViewById(R.id.checklistItem)).setText(((EditText) view.findViewById(R.id.checklistEdit)).getText().toString());
		view.findViewById(R.id.checklistItem).setVisibility(View.VISIBLE);
		view.findViewById(R.id.checklistEdit).setVisibility(View.GONE);
		oldEditCheckbox = null;
	}
}
