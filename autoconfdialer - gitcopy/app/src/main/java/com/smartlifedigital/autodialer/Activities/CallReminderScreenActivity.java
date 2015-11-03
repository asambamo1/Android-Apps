package com.smartlifedigital.autodialer.Activities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import com.smartlifedigital.autodialer.Helper.CallManagerHelper;
import com.smartlifedigital.autodialer.R;

public class CallReminderScreenActivity extends Activity {
	
	public final String TAG = this.getClass().getSimpleName();

	private WakeLock mWakeLock;
	private MediaPlayer mPlayer;

	private static final int WAKELOCK_TIMEOUT = 60 * 1000;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		//Setup layout
		this.setContentView(R.layout.activity_call_screen);

		String name = getIntent().getStringExtra(CallManagerHelper.NAME);
		final String number = getIntent().getStringExtra(CallManagerHelper.PHONENUMBER);
		int timeHour = getIntent().getIntExtra(CallManagerHelper.TIME_HOUR, 0);
		int timeMinute = getIntent().getIntExtra(CallManagerHelper.TIME_MINUTE, 0);
		final String tone = getIntent().getStringExtra(CallManagerHelper.TONE);


		//time in 12 hour format
		int hour = timeHour;
		int minutes = timeMinute;
		String timeSet = "";
		if (hour > 12) {
			hour -= 12;
			timeSet = "PM";
		} else if (hour == 0) {
			hour += 12;
			timeSet = "AM";
		} else if (hour == 12)
			timeSet = "PM";
		else
			timeSet = "AM";

		String min = "";
		if (minutes < 10)
			min = "0" + minutes ;
		else
			min = String.valueOf(minutes);

		// Append in a StringBuilder
		String aTime = new StringBuilder().append(hour).append(':')
				.append(min ).append(" ").append(timeSet).toString();


		TextView tvName = (TextView) findViewById(R.id.call_screen_name);
		tvName.setText(name);

		TextView tvNumber = (TextView) findViewById(R.id.call_screen_number);
 		tvNumber.setText(number);

		TextView tvTime = (TextView) findViewById(R.id.call_screen_time);
		tvTime.setText(aTime);


		final CountDownTimer countDownTimer;
		


		final TextView countdown = (TextView) findViewById(R.id.textView);

		countDownTimer = new CountDownTimer(15000, 1000) {

			public void onTick(long millisUntilFinished) {
				countdown.setText(millisUntilFinished / 1000 + " Seconds till Autodial Starts");
			}

			public void onFinish() {
				countdown.setText("Initiating Call...");
				mPlayer.stop();
				finish();
				Intent intent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + Uri.encode(number)));
				startActivity(intent);
			}
		}.start();


		Button dismissButton = (Button) findViewById(R.id.call_screen_button);
		dismissButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View view) {
				mPlayer.stop();
				countDownTimer.cancel();
				finish();



				Intent intent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + Uri.encode(number)));
				startActivity(intent);


			}
		});

		Button postponeButton = (Button) findViewById(R.id.button21);
		postponeButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View view) {
				mPlayer.stop();
				countDownTimer.cancel();
				finish();

			}
		});

		Button dismissButton1 = (Button) findViewById(R.id.call_screen_button2);
		dismissButton1.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View view) {
				mPlayer.stop();
				countDownTimer.cancel();
				finish();

			}
		});




		//Play call tone
		mPlayer = new MediaPlayer();
		try {
			if (tone != null && !tone.equals("")) {
				Uri toneUri = Uri.parse(tone);
				if (toneUri != null) {
					mPlayer.setDataSource(this, toneUri);
					mPlayer.setAudioStreamType(AudioManager.STREAM_ALARM);
					mPlayer.setLooping(true);
					mPlayer.prepare();
					mPlayer.start();
				}
				else{
					//Intent intent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + Uri.encode(number)));
					//startActivity(intent);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		//Ensure wakelock release
		Runnable releaseWakelock = new Runnable() {

			@Override
			public void run() {
				getWindow().clearFlags(WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
				getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
				getWindow().clearFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
				getWindow().clearFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);

				if (mWakeLock != null && mWakeLock.isHeld()) {
					mWakeLock.release();
				}
			}
		};

		new Handler().postDelayed(releaseWakelock, WAKELOCK_TIMEOUT);
	}

	@SuppressWarnings("deprecation")
	@Override
	protected void onResume() {
		super.onResume();

		// Set the window to keep the screen on
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);

		// Acquire wakelock
		PowerManager pm = (PowerManager) getApplicationContext().getSystemService(Context.POWER_SERVICE);
		if (mWakeLock == null) {
			mWakeLock = pm.newWakeLock((PowerManager.FULL_WAKE_LOCK | PowerManager.SCREEN_BRIGHT_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP), TAG);
		}

		if (!mWakeLock.isHeld()) {
			mWakeLock.acquire();
			Log.i(TAG, "Wakelock aquired!!");
		}

	}

	@Override
	protected void onPause() {
		super.onPause();

		if (mWakeLock != null && mWakeLock.isHeld()) {
			mWakeLock.release();
		}
	}
}
