package com.smartlifedigital.autodialer;

import android.app.ActionBar;
import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.ListView;

import java.sql.Date;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class CallListActivity extends ListActivity {

	private CallListAdapter mAdapter;
	private Context mContext;
	private Database dbHelper = new Database(this);
	private ListView list;


	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		mContext = this;

		requestWindowFeature(Window.FEATURE_ACTION_BAR);

		setContentView(R.layout.activity_call_list);

		ActionBar bar = getActionBar();
		bar.setBackgroundDrawable(new ColorDrawable(Color.parseColor("#263238")));

		mAdapter = new CallListAdapter(this, dbHelper.getcalls());

		setListAdapter(mAdapter);

		try {
			Collections.sort((List<MyObject>) list, new Comparator<MyObject>() {
				public int compare(MyObject o1, MyObject o2) {
					return o1.getDateTime().compareTo(o2.getDateTime());
				}
			});
		}catch(NullPointerException e){

		}
	}

	public static class MyObject implements Comparable<MyObject> {

		private Date dateTime;

		public Date getDateTime() {
			return dateTime;
		}

		public void setDateTime(Date datetime) {
			this.dateTime = datetime;
		}

		@Override
		public int compareTo(MyObject o) {
			return getDateTime().compareTo(o.getDateTime());
		}
	}




	@Override
	public void onContentChanged() {
		super.onContentChanged();

		View empty = findViewById(R.id.empty);
		ListView list = (ListView) findViewById(android.R.id.list);
		list.setEmptyView(empty);

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.call_list, menu);
		return true;
	}

	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		switch (item.getItemId()) {
			case R.id.action_add_new_call: {
				startcallDetailsActivity(-1);
				break;

			}

			case R.id.action_info: {
				Intent intent = new Intent(CallListActivity.this, About.class);
				startActivity(intent);
				break;

			}
		}

		return super.onOptionsItemSelected(item);
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		
		if (resultCode == RESULT_OK) {
	        mAdapter.setcalls(dbHelper.getcalls());
	        mAdapter.notifyDataSetChanged();
	    }
	}
	
	public void setcallEnabled(long id, boolean isEnabled) {
		CallManagerHelper.cancelcalls(this);
		
		Model model = dbHelper.getcall(id);
		model.isEnabled = isEnabled;
		dbHelper.updatecall(model);
		
		CallManagerHelper.setcalls(this);
	}

	public void startcallDetailsActivity(long id) {
		Intent intent = new Intent(this, CallDetailsActivity.class);
		intent.putExtra("id", id);
		startActivityForResult(intent, 0);
	}
	
	public void deletecall(long id) {
		final long callId = id;
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage("Would you like to delete this call?")
		.setTitle("Delete Call?")
		.setCancelable(true)
		.setNegativeButton("No", null)
		.setPositiveButton("Yes", new OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				//Cancel calls
				CallManagerHelper.cancelcalls(mContext);
				//Delete call from DB by id
				dbHelper.deletecall(callId);
				//Refresh the list of the scheduled calls in the adapter
				mAdapter.setcalls(dbHelper.getcalls());
				//Notify the adapter the data has changed
				mAdapter.notifyDataSetChanged();
				//Schedule the calls
				try {
					CallManagerHelper.setcalls(mContext);
				}
				catch (NullPointerException e){

				}
			}
		}).show();
	}
}
