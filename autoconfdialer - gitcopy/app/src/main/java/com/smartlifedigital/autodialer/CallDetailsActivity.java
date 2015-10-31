package com.smartlifedigital.autodialer;

import android.content.Intent;
import android.database.Cursor;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class CallDetailsActivity extends AppCompatActivity {
	private Database dbHelper = new Database(this);
	
	private Model callDetails;
	
	private TimePicker timePicker;
	private EditText edtName, edtNumber;
	private CustomSwitch chkWeekly;
	private CustomSwitch chkSunday;
	private CustomSwitch chkMonday;
	private CustomSwitch chkTuesday;
	private CustomSwitch chkWednesday;
	private CustomSwitch chkThursday;
	private CustomSwitch chkFriday;
	private CustomSwitch chkSaturday;
	private TextView txtToneSelection, t2;
    @Bind(R.id.coordinator_layout) CoordinatorLayout snackbar;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_details);
        ButterKnife.bind(this);

		getSupportActionBar().setTitle("Schedule a Call");
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);

		timePicker = (TimePicker) findViewById(R.id.call_details_time_picker);
		edtName = (EditText) findViewById(R.id.call_details_name);
		edtNumber = (EditText) findViewById(R.id.autodial_number);
		chkWeekly = (CustomSwitch) findViewById(R.id.call_details_repeat_weekly);
		chkSunday = (CustomSwitch) findViewById(R.id.call_details_repeat_sunday);
		chkMonday = (CustomSwitch) findViewById(R.id.call_details_repeat_monday);
		chkTuesday = (CustomSwitch) findViewById(R.id.call_details_repeat_tuesday);
		chkWednesday = (CustomSwitch) findViewById(R.id.call_details_repeat_wednesday);
		chkThursday = (CustomSwitch) findViewById(R.id.call_details_repeat_thursday);
		chkFriday = (CustomSwitch) findViewById(R.id.call_details_repeat_friday);
		chkSaturday = (CustomSwitch) findViewById(R.id.call_details_repeat_saturday);
		txtToneSelection = (TextView) findViewById(R.id.call_label_tone_selection);
		t2 = (TextView)findViewById(R.id.textView2);

		long id = getIntent().getExtras().getLong("id");
		
		if (id == -1) {
			callDetails = new Model();
		} else {
			callDetails = dbHelper.getcall(id);
			
			timePicker.setCurrentMinute(callDetails.timeMinute);
			timePicker.setCurrentHour(callDetails.timeHour);
			
			edtName.setText(callDetails.name);
			edtNumber.setText(callDetails.phonenumber);
			
			chkWeekly.setChecked(callDetails.repeatWeekly);
			chkSunday.setChecked(callDetails.getRepeatingDay(Model.SUNDAY));
			chkMonday.setChecked(callDetails.getRepeatingDay(Model.MONDAY));
			chkTuesday.setChecked(callDetails.getRepeatingDay(Model.TUESDAY));
			chkWednesday.setChecked(callDetails.getRepeatingDay(Model.WEDNESDAY));
			chkThursday.setChecked(callDetails.getRepeatingDay(Model.THURSDAY));
			chkFriday.setChecked(callDetails.getRepeatingDay(Model.FRDIAY));
			chkSaturday.setChecked(callDetails.getRepeatingDay(Model.SATURDAY));

			txtToneSelection.setText(RingtoneManager.getRingtone(this, callDetails.callTone).getTitle(this));
		}

		final LinearLayout ringToneContainer = (LinearLayout) findViewById(R.id.call_ringtone_container);
		ringToneContainer.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(RingtoneManager.ACTION_RINGTONE_PICKER);
				startActivityForResult(intent , 1);
			}
		});

		Button contacts = (Button) findViewById(R.id.contacts);
		contacts.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View view) {
				Intent pickContactIntent = new Intent(Intent.ACTION_PICK, Uri.parse("content://contacts"));
				pickContactIntent.setType(ContactsContract.CommonDataKinds.Phone.CONTENT_TYPE); // Show user only contacts w/ phone numbers
				startActivityForResult(pickContactIntent, 1);
			}
		});
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (data != null) {
			Uri uri = data.getData();

			if (uri != null) {
				Cursor c = null;
				try {
					c = getContentResolver().query(uri, new String[]{
									ContactsContract.CommonDataKinds.Phone.NUMBER,
									ContactsContract.CommonDataKinds.Phone.TYPE },
							null, null, null);

					if (c != null && c.moveToFirst()) {
						String number = c.getString(0);
						int type = c.getInt(1);
						showSelectedNumber(type, number);
					}
				} finally {
					if (c != null) {
						c.close();
					}
				}
			}
		}
		super.onActivityResult(requestCode, resultCode, data);
		
		if (resultCode == RESULT_OK) {
	        switch (requestCode) {
		        case 1: {
		        	callDetails.callTone = data.getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI);
		        	txtToneSelection.setText(RingtoneManager.getRingtone(this, callDetails.callTone).getTitle(this));
		            break;
		        }
				default: {
		            break;
		        }
	        }
	    }
	}

	public void showSelectedNumber(int type, String number) {
			edtNumber.setText(number);
	}

    @OnClick(R.id.save_call)
    public void saveCall(View button) {
        t2.setText(edtNumber.getText().toString());
        if (t2 == null || t2.getText().toString().length() < 1) {
            Snackbar.make(snackbar, "Please enter a phone number first!", Snackbar.LENGTH_LONG).show();
        }
        else if (t2.getText().toString().trim().startsWith("911")){
            Snackbar.make(snackbar, "911 calls are not allowed!", Snackbar.LENGTH_LONG).show();
        }
        else {
            updateModelFromLayout();
            CallManagerHelper.cancelcalls(this);
            if (callDetails.id < 0) {
                dbHelper.createcall(callDetails);
            } else {
                dbHelper.updatecall(callDetails);
            }
            CallManagerHelper.setcalls(this);
            setResult(RESULT_OK);
            finish();
        }
    }

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.blank_menu, menu);
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case android.R.id.home: {
				finish();
				break;
			}
		}
		return super.onOptionsItemSelected(item);
	}
	
	private void updateModelFromLayout() {		
		callDetails.timeMinute = timePicker.getCurrentMinute().intValue();
		callDetails.timeHour = timePicker.getCurrentHour().intValue();
		callDetails.name = edtName.getText().toString();
		callDetails.phonenumber = edtNumber.getText().toString();
		callDetails.repeatWeekly = chkWeekly.isChecked();	
		callDetails.setRepeatingDay(Model.SUNDAY, chkSunday.isChecked());
		callDetails.setRepeatingDay(Model.MONDAY, chkMonday.isChecked());
		callDetails.setRepeatingDay(Model.TUESDAY, chkTuesday.isChecked());
		callDetails.setRepeatingDay(Model.WEDNESDAY, chkWednesday.isChecked());
		callDetails.setRepeatingDay(Model.THURSDAY, chkThursday.isChecked());
		callDetails.setRepeatingDay(Model.FRDIAY, chkFriday.isChecked());
		callDetails.setRepeatingDay(Model.SATURDAY, chkSaturday.isChecked());
		callDetails.isEnabled = true;
	}
}
