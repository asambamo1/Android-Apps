package com.smartlifedigital.autodialer;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;

import com.smartlifedigital.autodialer.Activities.SettingsActivity;

import java.sql.Date;

import butterknife.Bind;
import butterknife.ButterKnife;

public class CallListActivity extends AppCompatActivity {

	private CallListAdapter mAdapter;
	private Context mContext;
	private Database dbHelper = new Database(this);
    @Bind(R.id.empty) View noCalls;
    @Bind(R.id.calls_list) ListView callsList;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		mContext = this;
		setContentView(R.layout.activity_call_list);
        ButterKnife.bind(this);
		mAdapter = new CallListAdapter(this, dbHelper.getcalls());
        callsList.setAdapter(mAdapter);
	}

    @Override
    protected void onResume() {
        super.onResume();
        if (mAdapter.getCount() != 0) {
            noCalls.setVisibility(View.GONE);
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
                } catch (NullPointerException e) {
                }
                if (mAdapter.getCount() == 0) {
                    noCalls.setVisibility(View.VISIBLE);
                }
            }
        }).show();
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
                return true;
            }
            case R.id.settings: {
                Intent intent = new Intent(this, SettingsActivity.class);
                startActivity(intent);
                return true;
            }
        }
        return super.onOptionsItemSelected(item);
    }
}
