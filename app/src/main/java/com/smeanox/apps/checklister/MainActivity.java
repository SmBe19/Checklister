package com.smeanox.apps.checklister;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.view.inputmethod.InputMethodManager;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

	private ChecklistDB.Entry currentRootEntry;
	public static final String EXTRA_ROOT_ENTRY = "com.smeanox.apps.checklister.rootentry";

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

		ChecklistDB.get().setContext(this);

		Intent intent = getIntent();
		Long rootId = intent.getLongExtra(EXTRA_ROOT_ENTRY, -1);
		if(rootId == -1){
			currentRootEntry = ChecklistDB.get().getRoot();
		} else {
			currentRootEntry = ChecklistDB.get().getEntry(rootId);
			getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		}

		for(ChecklistDB.Entry entry : currentRootEntry.getChildren()){
			addChecklistEntry(entry);
		}

		getSupportActionBar().setTitle(currentRootEntry.getText());
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

		//noinspection SimplifiableIfStatement
		switch (item.getItemId()){
			case R.id.action_settings:
				Intent intent = new Intent(this, SettingsActivity.class);
				startActivity(intent);
				return true;
			case android.R.id.home:
				onBackPressed();
		}

		return super.onOptionsItemSelected(item);
	}

	@Override
	protected void onPause() {
		super.onPause();

		if(oldEditCheckbox != null){
			endEditCheckbox(oldEditCheckbox);
		}
	}

	public void addMenuItem(View view) {
		addChecklistEntry(ChecklistDB.get().addEntry(currentRootEntry.getId(), getResources().getString(R.string.new_item), false));
	}

	public void addChecklistEntry(ChecklistDB.Entry entry){
		LayoutInflater layoutInflater = getLayoutInflater();
		RelativeLayout checkBox = (RelativeLayout) layoutInflater.inflate(R.layout.checklist_item, null);
		((CheckBox) checkBox.findViewById(R.id.checklistItem)).setText(entry.getText());
		((CheckBox) checkBox.findViewById(R.id.checklistItem)).setChecked(entry.isChecked());


		LinearLayout layout = (LinearLayout) findViewById(R.id.mainLayout);
		layout.addView(checkBox);

		checkBox.findViewById(R.id.checklistItem).setOnTouchListener(new View.OnTouchListener() {
			float startX=0, startY=0;
			boolean reachedMin;
			final float minWidth = 0.1f;
			final float delWidth = 0.8f;

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				float distX = event.getRawX() - startX;
				if (distX > v.getWidth() * minWidth) {
					reachedMin = true;
				}
				switch (event.getAction()) {
					case MotionEvent.ACTION_DOWN:
						reachedMin = false;
						startX = event.getRawX();
						startY = event.getRawY();
						return false;
					case MotionEvent.ACTION_UP:
						if(reachedMin) {
							if (distX > v.getWidth() * delWidth) {
								deleteEntry(v);
							}
							setOffsetX(v, 0);
							return true;
						}
						break;
					case MotionEvent.ACTION_MOVE:
						if(reachedMin) {
							if (distX >= 0) {
								setOffsetX(v, (int) distX);
							} else {
								startX = event.getRawX();
							}
							return false;
						}
						break;
				}
				return false;
			}

			private void setOffsetX(View v, int distX){
				RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) ((LinearLayout) v.getParent()).getLayoutParams();
				layoutParams.leftMargin = distX;
				layoutParams.rightMargin = -distX;
				((LinearLayout) v.getParent()).setLayoutParams(layoutParams);
				if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
					((View) v.getParent()).setAlpha(1 - Math.min(1, distX / (v.getWidth() * delWidth)));
				}
			}
		});

		RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) checkBox.findViewById(R.id.checklistWrapper).getLayoutParams();
		layoutParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT);

		checkBox.setTag(R.integer.CheckboxEntryTag, entry);
	}

	private View oldEditCheckbox;

	public void editCheckbox(View view) {
		View parent = (View) view.getParent().getParent();
		if(oldEditCheckbox == parent){
			endEditCheckbox(parent);
			return;
		}
		if(oldEditCheckbox != null){
			endEditCheckbox(oldEditCheckbox);
		}
		startEditCheckbox(parent);
	}

	private void startEditCheckbox(View parent) {
		EditText editText = (EditText) parent.findViewById(R.id.checklistEdit);
		CheckBox textView = (CheckBox) parent.findViewById(R.id.checklistItem);
		editText.setText(textView.getText());
		textView.setVisibility(View.GONE);
		editText.setVisibility(View.VISIBLE);
		oldEditCheckbox = parent;

		editText.requestFocus();
		InputMethodManager mgr = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
		mgr.showSoftInput(editText, InputMethodManager.SHOW_IMPLICIT);
	}

	private void endEditCheckbox(View parent){
		String text = ((EditText) parent.findViewById(R.id.checklistEdit)).getText().toString();
		ChecklistDB.Entry entry = (ChecklistDB.Entry) parent.getTag(R.integer.CheckboxEntryTag);
		entry.setText(text);
		((TextView) parent.findViewById(R.id.checklistItem)).setText(text);
		parent.findViewById(R.id.checklistItem).setVisibility(View.VISIBLE);
		parent.findViewById(R.id.checklistEdit).setVisibility(View.GONE);
		oldEditCheckbox = null;
	}

	public void viewChilds(View view) {
		View parent = (View) view.getParent().getParent();
		ChecklistDB.Entry entry = (ChecklistDB.Entry) parent.getTag(R.integer.CheckboxEntryTag);
		Intent intent = new Intent(this, MainActivity.class);
		intent.putExtra(EXTRA_ROOT_ENTRY, entry.getId());
		startActivity(intent);
	}

	public void toggleChecked(View view) {
		View parent = (View) view.getParent().getParent();
		ChecklistDB.Entry entry = (ChecklistDB.Entry) parent.getTag(R.integer.CheckboxEntryTag);
		entry.setChecked(((CheckBox) parent.findViewById(R.id.checklistItem)).isChecked());
	}

	public void deleteEntry(View view){
		View parent = (View) view.getParent().getParent();
		ChecklistDB.Entry entry = (ChecklistDB.Entry) parent.getTag(R.integer.CheckboxEntryTag);
		entry.delete();
		((LinearLayout) parent.getParent()).removeView(parent);
	}
}
