package com.gizwits.opensource.appkit.ControlModule;

import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.IdRes;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.widget.TimePicker;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.ConcurrentHashMap;

import com.gizwits.gizwifisdk.api.GizWifiDevice;
import com.gizwits.gizwifisdk.enumration.GizWifiDeviceNetStatus;
import com.gizwits.gizwifisdk.enumration.GizWifiErrorCode;
import com.gizwits.opensource.appkit.R;
import com.gizwits.opensource.appkit.utils.HexStrUtils;
import com.gizwits.opensource.appkit.view.HexWatcher;

import static android.R.attr.visible;
import static com.gizwits.opensource.appkit.R.id.clock1;
import static com.gizwits.opensource.appkit.R.id.et_data_Remaining_Pack;
import static com.gizwits.opensource.appkit.utils.HexStrUtils.bytesToHexString;
import static java.lang.Boolean.FALSE;

public class GosDeviceControlActivity extends GosControlModuleBaseActivity
		implements OnClickListener, OnEditorActionListener {

	/** 设备列表传入的设备变量 */
	private GizWifiDevice mDevice;

	private EditText et_data_Remaining_Pack;
	private RadioGroup radioGroup;
	private LinearLayout linearLayout1;
	private Button btnSure;

	RelativeLayout relativeLayout1;
	LinearLayout linearLayout4;
	RelativeLayout layoutTime1;
	RelativeLayout layoutTime2;
	RelativeLayout layoutTime3;
	RelativeLayout layoutTime4;
	RelativeLayout layoutTime5;
	RelativeLayout setTimeLayout1;
	RelativeLayout setTimeLayout2;
	RelativeLayout setTimeLayout3;
	RelativeLayout setTimeLayout4;
	RelativeLayout setTimeLayout5;
	LinearLayout setupLayout;

	TextView alarm1;
	TextView alarm2;
	TextView alarm3;
	TextView alarm4;
	TextView alarm5;

	Switch switch1;
	Switch switch2;
	Switch switch3;
	Switch switch4;
	Switch switch5;

	int option;
	TextView hint1;
	TextView hint2;
	TextView hint3;
	TextView hint4;
	TextView hint5;

	Context context;
	Intent my_intent;
	Calendar now;
	Calendar calendar;
	String s_clock;
	int nowTime;
	int clockTime1;
	int clockTime2;
	int clockTime3;
	int clockTime4;
	int clockTime5;
	int LidState;
	int remainingPack;
	int totalPack;
	String dosingTime;

	TextView clock1;
	TimePicker alarm_timepicker1;
	AlarmManager alarm_manager1;
	PendingIntent pending_intent1;
	String s_clock1;

	TextView clock2;
	TimePicker alarm_timepicker2;
	AlarmManager alarm_manager2;
	PendingIntent pending_intent2;
	String s_clock2;

	TextView clock3;
	TimePicker alarm_timepicker3;
	AlarmManager alarm_manager3;
	PendingIntent pending_intent3;
	String s_clock3;

	TextView clock4;
	TimePicker alarm_timepicker4;
	AlarmManager alarm_manager4;
	PendingIntent pending_intent4;
	String s_clock4;

	TextView clock5;
	TimePicker alarm_timepicker5;
	AlarmManager alarm_manager5;
	PendingIntent pending_intent5;
	String s_clock5;


	private enum handler_key {

		/** 更新界面 */
		UPDATE_UI,

		DISCONNECT,
	}

	private Runnable mRunnable = new Runnable() {
		public void run() {
			if (isDeviceCanBeControlled()) {
				progressDialog.cancel();
			} else {
				toastDeviceNoReadyAndExit();
			}
		}

	};

	/** The handler. */
	Handler mHandler = new Handler() {
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			handler_key key = handler_key.values()[msg.what];
			switch (key) {
			case UPDATE_UI:
				updateUI();
				break;
			case DISCONNECT:
				toastDeviceDisconnectAndExit();
				break;
			}
		}
	};

	public static final int MSG_ONE = 1;
	private Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			switch (msg.what) {
				case MSG_ONE:
					checkTime();

					break;
				default:
					break;
			}
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_gos_device_control);
		initDevice();
		setActionBar(false, true, getDeviceName());
		initView();
		initEvent();
		new TimeThread().start();
	}

	public class TimeThread extends Thread {
		//重写run方法
		@Override
		public void run() {
			super.run();
			do {
				try {
					Thread.sleep(1000);
					Message msg = new Message();
					msg.what = MSG_ONE;
					handler.sendMessage(msg);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			} while (true);
		}
	}

	@Override
	public void onBackPressed() {
		if(linearLayout4.getVisibility() == View.VISIBLE){
			relativeLayout1.setVisibility(View.VISIBLE);
			linearLayout4.setVisibility(View.GONE);
			setupLayout.setVisibility(View.VISIBLE);
		} else if(linearLayout4.getVisibility() == View.GONE) {
			super.onBackPressed();
		}
	}

	private void initView() {
		this.context = this;
		my_intent = new Intent(this.context, Alarm_Receiver.class);
		now = Calendar.getInstance();

		LidState = 2;
		remainingPack = 0;
		totalPack = -1;
		dosingTime = "";

		relativeLayout1 = (RelativeLayout) findViewById(R.id.relativeLayout1);
		linearLayout4 = (LinearLayout) findViewById(R.id.linearLayout4);
		layoutTime1 = (RelativeLayout) findViewById(R.id.layoutTime1);
		layoutTime2 = (RelativeLayout) findViewById(R.id.layoutTime2);
		layoutTime3 = (RelativeLayout) findViewById(R.id.layoutTime3);
		layoutTime4 = (RelativeLayout) findViewById(R.id.layoutTime4);
		layoutTime5 = (RelativeLayout) findViewById(R.id.layoutTime5);
		setTimeLayout1 = (RelativeLayout) findViewById(R.id.setTimeLayout1);
		setTimeLayout2 = (RelativeLayout) findViewById(R.id.setTimeLayout2);
		setTimeLayout3 = (RelativeLayout) findViewById(R.id.setTimeLayout3);
		setTimeLayout4 = (RelativeLayout) findViewById(R.id.setTimeLayout4);
		setTimeLayout5 = (RelativeLayout) findViewById(R.id.setTimeLayout5);
		setupLayout = (LinearLayout) findViewById(R.id.setupLayout);

		alarm1 = (TextView) findViewById(R.id.alarm1);
		alarm2 = (TextView) findViewById(R.id.alarm2);
		alarm3 = (TextView) findViewById(R.id.alarm3);
		alarm4 = (TextView) findViewById(R.id.alarm4);
		alarm5 = (TextView) findViewById(R.id.alarm5);

		switch1 = (Switch) findViewById(R.id.switch1);
		switch2 = (Switch) findViewById(R.id.switch2);
		switch3 = (Switch) findViewById(R.id.switch3);
		switch4 = (Switch) findViewById(R.id.switch4);
		switch5 = (Switch) findViewById(R.id.switch5);

		hint1 = (TextView) findViewById(R.id.hint1);
		hint2 = (TextView) findViewById(R.id.hint2);
		hint3 = (TextView) findViewById(R.id.hint3);
		hint4 = (TextView) findViewById(R.id.hint4);
		hint5 = (TextView) findViewById(R.id.hint5);

		et_data_Remaining_Pack = (EditText) findViewById(R.id.et_data_Remaining_Pack);
		radioGroup = (RadioGroup) findViewById(R.id.radioGroup);
		linearLayout1 = (LinearLayout) findViewById(R.id.linearLayout1);
		clock1 = (TextView) findViewById(R.id.clock1);
		clock2 = (TextView) findViewById(R.id.clock2);
		clock3 = (TextView) findViewById(R.id.clock3);
		clock4 = (TextView) findViewById(R.id.clock4);
		clock5 = (TextView) findViewById(R.id.clock5);
		btnSure = (Button) findViewById(R.id.btnSure);
		alarm_manager1 = (AlarmManager) getSystemService(ALARM_SERVICE);
		alarm_manager2 = (AlarmManager) getSystemService(ALARM_SERVICE);
		alarm_manager3 = (AlarmManager) getSystemService(ALARM_SERVICE);
		alarm_manager4 = (AlarmManager) getSystemService(ALARM_SERVICE);
		alarm_manager5 = (AlarmManager) getSystemService(ALARM_SERVICE);

	}

	private void initEvent() {
		et_data_Remaining_Pack.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				et_data_Remaining_Pack.setCursorVisible(true);
			}
		});
		et_data_Remaining_Pack.setOnEditorActionListener(this);
		radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(RadioGroup group, @IdRes int checkedId) {
				linearLayout1.setVisibility(View.VISIBLE);
				btnSure.setVisibility(View.VISIBLE);
				switch (group.getCheckedRadioButtonId()){
					case R.id.option1:
						option = 1;
						setTimeLayout1.setVisibility(View.VISIBLE);
						setTimeLayout2.setVisibility(View.GONE);
						setTimeLayout3.setVisibility(View.GONE);
						setTimeLayout4.setVisibility(View.GONE);
						setTimeLayout5.setVisibility(View.GONE);
						layoutTime1.setVisibility(View.VISIBLE);
						layoutTime2.setVisibility(View.GONE);
						layoutTime3.setVisibility(View.GONE);
						layoutTime4.setVisibility(View.GONE);
						layoutTime5.setVisibility(View.GONE);
						clock2.setText("00:00");
						clock3.setText("00:00");
						clock4.setText("00:00");
						clock5.setText("00:00");
						alarm2.setText("00:00");
						alarm3.setText("00:00");
						alarm4.setText("00:00");
						alarm5.setText("00:00");
						s_clock2 = clock2.getText().toString();
						s_clock3 = clock3.getText().toString();
						s_clock4 = clock4.getText().toString();
						s_clock5 = clock5.getText().toString();
						s_clock = "0"+s_clock1.substring(0,1)+"0"+s_clock1.substring(1,2)
								+"0"+s_clock1.substring(3,4)+"0"+s_clock1.substring(4,5)
								+"0"+s_clock2.substring(0,1)+"0"+s_clock2.substring(1,2)
								+"0"+s_clock2.substring(3,4)+"0"+s_clock2.substring(4,5)
								+"0"+s_clock3.substring(0,1)+"0"+s_clock3.substring(1,2)
								+"0"+s_clock3.substring(3,4)+"0"+s_clock3.substring(4,5)
								+"0"+s_clock4.substring(0,1)+"0"+s_clock4.substring(1,2)
								+"0"+s_clock4.substring(3,4)+"0"+s_clock4.substring(4,5)
								+"0"+s_clock5.substring(0,1)+"0"+s_clock5.substring(1,2)
								+"0"+s_clock5.substring(3,4)+"0"+s_clock5.substring(4,5);
						sendCommand(KEY_DOSING_TIME, HexStrUtils.hexStringToBytes(s_clock));
						data_Dosing_Time = HexStrUtils.hexStringToBytes(s_clock);
						break;
					case R.id.option2:
						option = 2;
						setTimeLayout1.setVisibility(View.VISIBLE);
						setTimeLayout2.setVisibility(View.VISIBLE);
						setTimeLayout3.setVisibility(View.GONE);
						setTimeLayout4.setVisibility(View.GONE);
						setTimeLayout5.setVisibility(View.GONE);
						layoutTime1.setVisibility(View.VISIBLE);
						layoutTime2.setVisibility(View.VISIBLE);
						layoutTime3.setVisibility(View.GONE);
						layoutTime4.setVisibility(View.GONE);
						layoutTime5.setVisibility(View.GONE);
						clock3.setText("00:00");
						clock4.setText("00:00");
						clock5.setText("00:00");
						alarm3.setText("00:00");
						alarm4.setText("00:00");
						alarm5.setText("00:00");
						s_clock3 = clock3.getText().toString();
						s_clock4 = clock4.getText().toString();
						s_clock5 = clock5.getText().toString();
						s_clock = "0"+s_clock1.substring(0,1)+"0"+s_clock1.substring(1,2)
								+"0"+s_clock1.substring(3,4)+"0"+s_clock1.substring(4,5)
								+"0"+s_clock2.substring(0,1)+"0"+s_clock2.substring(1,2)
								+"0"+s_clock2.substring(3,4)+"0"+s_clock2.substring(4,5)
								+"0"+s_clock3.substring(0,1)+"0"+s_clock3.substring(1,2)
								+"0"+s_clock3.substring(3,4)+"0"+s_clock3.substring(4,5)
								+"0"+s_clock4.substring(0,1)+"0"+s_clock4.substring(1,2)
								+"0"+s_clock4.substring(3,4)+"0"+s_clock4.substring(4,5)
								+"0"+s_clock5.substring(0,1)+"0"+s_clock5.substring(1,2)
								+"0"+s_clock5.substring(3,4)+"0"+s_clock5.substring(4,5);
						sendCommand(KEY_DOSING_TIME, HexStrUtils.hexStringToBytes(s_clock));
						data_Dosing_Time = HexStrUtils.hexStringToBytes(s_clock);
						break;
					case R.id.option3:
						option = 3;
						setTimeLayout1.setVisibility(View.VISIBLE);
						setTimeLayout2.setVisibility(View.VISIBLE);
						setTimeLayout3.setVisibility(View.VISIBLE);
						setTimeLayout4.setVisibility(View.GONE);
						setTimeLayout5.setVisibility(View.GONE);
						layoutTime1.setVisibility(View.VISIBLE);
						layoutTime2.setVisibility(View.VISIBLE);
						layoutTime3.setVisibility(View.VISIBLE);
						layoutTime4.setVisibility(View.GONE);
						layoutTime5.setVisibility(View.GONE);
						clock4.setText("00:00");
						clock5.setText("00:00");
						alarm4.setText("00:00");
						alarm5.setText("00:00");
						s_clock4 = clock4.getText().toString();
						s_clock5 = clock5.getText().toString();
						s_clock = "0"+s_clock1.substring(0,1)+"0"+s_clock1.substring(1,2)
								+"0"+s_clock1.substring(3,4)+"0"+s_clock1.substring(4,5)
								+"0"+s_clock2.substring(0,1)+"0"+s_clock2.substring(1,2)
								+"0"+s_clock2.substring(3,4)+"0"+s_clock2.substring(4,5)
								+"0"+s_clock3.substring(0,1)+"0"+s_clock3.substring(1,2)
								+"0"+s_clock3.substring(3,4)+"0"+s_clock3.substring(4,5)
								+"0"+s_clock4.substring(0,1)+"0"+s_clock4.substring(1,2)
								+"0"+s_clock4.substring(3,4)+"0"+s_clock4.substring(4,5)
								+"0"+s_clock5.substring(0,1)+"0"+s_clock5.substring(1,2)
								+"0"+s_clock5.substring(3,4)+"0"+s_clock5.substring(4,5);
						sendCommand(KEY_DOSING_TIME, HexStrUtils.hexStringToBytes(s_clock));
						data_Dosing_Time = HexStrUtils.hexStringToBytes(s_clock);
						break;
					case R.id.option4:
						option = 4;
						setTimeLayout1.setVisibility(View.VISIBLE);
						setTimeLayout2.setVisibility(View.VISIBLE);
						setTimeLayout3.setVisibility(View.VISIBLE);
						setTimeLayout4.setVisibility(View.VISIBLE);
						setTimeLayout5.setVisibility(View.GONE);
						layoutTime1.setVisibility(View.VISIBLE);
						layoutTime2.setVisibility(View.VISIBLE);
						layoutTime3.setVisibility(View.VISIBLE);
						layoutTime4.setVisibility(View.VISIBLE);
						layoutTime5.setVisibility(View.GONE);
						clock5.setText("00:00");
						alarm5.setText("00:00");
						s_clock5 = clock5.getText().toString();
						s_clock = "0"+s_clock1.substring(0,1)+"0"+s_clock1.substring(1,2)
								+"0"+s_clock1.substring(3,4)+"0"+s_clock1.substring(4,5)
								+"0"+s_clock2.substring(0,1)+"0"+s_clock2.substring(1,2)
								+"0"+s_clock2.substring(3,4)+"0"+s_clock2.substring(4,5)
								+"0"+s_clock3.substring(0,1)+"0"+s_clock3.substring(1,2)
								+"0"+s_clock3.substring(3,4)+"0"+s_clock3.substring(4,5)
								+"0"+s_clock4.substring(0,1)+"0"+s_clock4.substring(1,2)
								+"0"+s_clock4.substring(3,4)+"0"+s_clock4.substring(4,5)
								+"0"+s_clock5.substring(0,1)+"0"+s_clock5.substring(1,2)
								+"0"+s_clock5.substring(3,4)+"0"+s_clock5.substring(4,5);
						sendCommand(KEY_DOSING_TIME, HexStrUtils.hexStringToBytes(s_clock));
						data_Dosing_Time = HexStrUtils.hexStringToBytes(s_clock);
						break;
					case R.id.option5:
						option = 5;
						setTimeLayout1.setVisibility(View.VISIBLE);
						setTimeLayout2.setVisibility(View.VISIBLE);
						setTimeLayout3.setVisibility(View.VISIBLE);
						setTimeLayout4.setVisibility(View.VISIBLE);
						setTimeLayout5.setVisibility(View.VISIBLE);
						layoutTime1.setVisibility(View.VISIBLE);
						layoutTime2.setVisibility(View.VISIBLE);
						layoutTime3.setVisibility(View.VISIBLE);
						layoutTime4.setVisibility(View.VISIBLE);
						layoutTime5.setVisibility(View.VISIBLE);
						break;
					default:
						break;
				}
			}
		});
		btnSure.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				relativeLayout1.setVisibility(View.GONE);
				linearLayout4.setVisibility(View.VISIBLE);
				setupLayout.setVisibility(View.GONE);
			}
		});
		// 时间设置
		clock1.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				AlertDialog.Builder builder = new AlertDialog.Builder(GosDeviceControlActivity.this);
				View timeSelect = View.inflate(GosDeviceControlActivity.this, R.layout.time_dialog1, null);
				alarm_timepicker1 = (TimePicker) timeSelect.findViewById(R.id.TimePicker1);
				alarm_timepicker1.setIs24HourView(true);
				builder.setIcon(R.mipmap.ic_launcher);
				builder.setTitle("设置闹钟");
				builder.setView(timeSelect);
				builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						String mHour;
						String mMinute;
						if (Build.VERSION.SDK_INT >= 23 ){
							alarm_timepicker1.getHour();
							mHour = String.valueOf(alarm_timepicker1.getHour());
							mMinute = String.valueOf(alarm_timepicker1.getMinute());
							clock1.setText(new StringBuilder().append(alarm_timepicker1.getHour() < 10 ? 0 + mHour : mHour)
									.append(":").append(alarm_timepicker1.getMinute() < 10 ? 0 + mMinute : mMinute));
							alarm1.setText(clock1.getText().toString());
						} else {
							alarm_timepicker1.getCurrentHour();
							mHour = String.valueOf(alarm_timepicker1.getCurrentHour());
							mMinute = String.valueOf(alarm_timepicker1.getCurrentMinute());
							clock1.setText(new StringBuilder().append(alarm_timepicker1.getCurrentHour() < 10 ? 0 + mHour : mHour)
									.append(":").append(alarm_timepicker1.getCurrentMinute() < 10 ? 0 + mMinute : mMinute));
							alarm1.setText(clock1.getText().toString());
						}
						s_clock1 = clock1.getText().toString();
						s_clock = "0"+s_clock1.substring(0,1)+"0"+s_clock1.substring(1,2)
								+"0"+s_clock1.substring(3,4)+"0"+s_clock1.substring(4,5)
								+"0"+s_clock2.substring(0,1)+"0"+s_clock2.substring(1,2)
								+"0"+s_clock2.substring(3,4)+"0"+s_clock2.substring(4,5)
								+"0"+s_clock3.substring(0,1)+"0"+s_clock3.substring(1,2)
								+"0"+s_clock3.substring(3,4)+"0"+s_clock3.substring(4,5)
								+"0"+s_clock4.substring(0,1)+"0"+s_clock4.substring(1,2)
								+"0"+s_clock4.substring(3,4)+"0"+s_clock4.substring(4,5)
								+"0"+s_clock5.substring(0,1)+"0"+s_clock5.substring(1,2)
								+"0"+s_clock5.substring(3,4)+"0"+s_clock5.substring(4,5);
						sendCommand(KEY_DOSING_TIME, HexStrUtils.hexStringToBytes(s_clock));
						data_Dosing_Time = HexStrUtils.hexStringToBytes(s_clock);
					}
				}).setNegativeButton("取消", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {

					}
				});
				AlertDialog dialog = builder.create();
				dialog.show();
			}
		});
		clock2.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				AlertDialog.Builder builder = new AlertDialog.Builder(GosDeviceControlActivity.this);
				View timeSelect = View.inflate(GosDeviceControlActivity.this, R.layout.time_dialog2, null);
				alarm_timepicker2 = (TimePicker) timeSelect.findViewById(R.id.TimePicker2);
				alarm_timepicker2.setIs24HourView(true);
				builder.setIcon(R.mipmap.ic_launcher);
				builder.setTitle("设置闹钟");
				builder.setView(timeSelect);
				builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						String mHour;
						String mMinute;
						if (Build.VERSION.SDK_INT >= 23 ){
							alarm_timepicker2.getHour();
							mHour = String.valueOf(alarm_timepicker2.getHour());
							mMinute = String.valueOf(alarm_timepicker2.getMinute());
							clock2.setText(new StringBuilder().append(alarm_timepicker2.getHour() < 10 ? 0 + mHour : mHour)
									.append(":").append(alarm_timepicker2.getMinute() < 10 ? 0 + mMinute : mMinute));
							alarm2.setText(clock2.getText().toString());
						} else {
							alarm_timepicker2.getCurrentHour();
							mHour = String.valueOf(alarm_timepicker2.getCurrentHour());
							mMinute = String.valueOf(alarm_timepicker2.getCurrentMinute());
							clock2.setText(new StringBuilder().append(alarm_timepicker2.getCurrentHour() < 10 ? 0 + mHour : mHour)
									.append(":").append(alarm_timepicker2.getCurrentMinute() < 10 ? 0 + mMinute : mMinute));
							alarm2.setText(clock2.getText().toString());
						}
						s_clock2 = clock2.getText().toString();
						s_clock = "0"+s_clock1.substring(0,1)+"0"+s_clock1.substring(1,2)
								+"0"+s_clock1.substring(3,4)+"0"+s_clock1.substring(4,5)
								+"0"+s_clock2.substring(0,1)+"0"+s_clock2.substring(1,2)
								+"0"+s_clock2.substring(3,4)+"0"+s_clock2.substring(4,5)
								+"0"+s_clock3.substring(0,1)+"0"+s_clock3.substring(1,2)
								+"0"+s_clock3.substring(3,4)+"0"+s_clock3.substring(4,5)
								+"0"+s_clock4.substring(0,1)+"0"+s_clock4.substring(1,2)
								+"0"+s_clock4.substring(3,4)+"0"+s_clock4.substring(4,5)
								+"0"+s_clock5.substring(0,1)+"0"+s_clock5.substring(1,2)
								+"0"+s_clock5.substring(3,4)+"0"+s_clock5.substring(4,5);
						sendCommand(KEY_DOSING_TIME, HexStrUtils.hexStringToBytes(s_clock));
						data_Dosing_Time = HexStrUtils.hexStringToBytes(s_clock);
					}
				}).setNegativeButton("取消", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {

					}
				});
				AlertDialog dialog = builder.create();
				dialog.show();
			}
		});
		clock3.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				AlertDialog.Builder builder = new AlertDialog.Builder(GosDeviceControlActivity.this);
				View timeSelect = View.inflate(GosDeviceControlActivity.this, R.layout.time_dialog3, null);
				alarm_timepicker3 = (TimePicker) timeSelect.findViewById(R.id.TimePicker3);
				alarm_timepicker3.setIs24HourView(true);
				builder.setIcon(R.mipmap.ic_launcher);
				builder.setTitle("设置闹钟");
				builder.setView(timeSelect);
				builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						String mHour;
						String mMinute;
						if (Build.VERSION.SDK_INT >= 23 ){
							alarm_timepicker3.getHour();
							mHour = String.valueOf(alarm_timepicker3.getHour());
							mMinute = String.valueOf(alarm_timepicker3.getMinute());
							clock3.setText(new StringBuilder().append(alarm_timepicker3.getHour() < 10 ? 0 + mHour : mHour)
									.append(":").append(alarm_timepicker3.getMinute() < 10 ? 0 + mMinute : mMinute));
							alarm3.setText(clock3.getText().toString());
						} else {
							alarm_timepicker3.getCurrentHour();
							mHour = String.valueOf(alarm_timepicker3.getCurrentHour());
							mMinute = String.valueOf(alarm_timepicker3.getCurrentMinute());
							clock3.setText(new StringBuilder().append(alarm_timepicker3.getCurrentHour() < 10 ? 0 + mHour : mHour)
									.append(":").append(alarm_timepicker3.getCurrentMinute() < 10 ? 0 + mMinute : mMinute));
							alarm3.setText(clock3.getText().toString());
						}
						s_clock3 = clock3.getText().toString();
						s_clock = "0"+s_clock1.substring(0,1)+"0"+s_clock1.substring(1,2)
								+"0"+s_clock1.substring(3,4)+"0"+s_clock1.substring(4,5)
								+"0"+s_clock2.substring(0,1)+"0"+s_clock2.substring(1,2)
								+"0"+s_clock2.substring(3,4)+"0"+s_clock2.substring(4,5)
								+"0"+s_clock3.substring(0,1)+"0"+s_clock3.substring(1,2)
								+"0"+s_clock3.substring(3,4)+"0"+s_clock3.substring(4,5)
								+"0"+s_clock4.substring(0,1)+"0"+s_clock4.substring(1,2)
								+"0"+s_clock4.substring(3,4)+"0"+s_clock4.substring(4,5)
								+"0"+s_clock5.substring(0,1)+"0"+s_clock5.substring(1,2)
								+"0"+s_clock5.substring(3,4)+"0"+s_clock5.substring(4,5);
						sendCommand(KEY_DOSING_TIME, HexStrUtils.hexStringToBytes(s_clock));
						data_Dosing_Time = HexStrUtils.hexStringToBytes(s_clock);
					}
				}).setNegativeButton("取消", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {

					}
				});
				AlertDialog dialog = builder.create();
				dialog.show();
			}
		});
		clock4.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				AlertDialog.Builder builder = new AlertDialog.Builder(GosDeviceControlActivity.this);
				View timeSelect = View.inflate(GosDeviceControlActivity.this, R.layout.time_dialog4, null);
				alarm_timepicker4 = (TimePicker) timeSelect.findViewById(R.id.TimePicker4);
				alarm_timepicker4.setIs24HourView(true);
				builder.setIcon(R.mipmap.ic_launcher);
				builder.setTitle("设置闹钟");
				builder.setView(timeSelect);
				builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						String mHour;
						String mMinute;
						if (Build.VERSION.SDK_INT >= 23 ){
							alarm_timepicker4.getHour();
							mHour = String.valueOf(alarm_timepicker4.getHour());
							mMinute = String.valueOf(alarm_timepicker4.getMinute());
							clock4.setText(new StringBuilder().append(alarm_timepicker4.getHour() < 10 ? 0 + mHour : mHour)
									.append(":").append(alarm_timepicker4.getMinute() < 10 ? 0 + mMinute : mMinute));
							alarm4.setText(clock4.getText().toString());
						} else {
							alarm_timepicker4.getCurrentHour();
							mHour = String.valueOf(alarm_timepicker4.getCurrentHour());
							mMinute = String.valueOf(alarm_timepicker4.getCurrentMinute());
							clock4.setText(new StringBuilder().append(alarm_timepicker4.getCurrentHour() < 10 ? 0 + mHour : mHour)
									.append(":").append(alarm_timepicker4.getCurrentMinute() < 10 ? 0 + mMinute : mMinute));
							alarm4.setText(clock4.getText().toString());
						}
						s_clock4 = clock4.getText().toString();
						s_clock = "0"+s_clock1.substring(0,1)+"0"+s_clock1.substring(1,2)
								+"0"+s_clock1.substring(3,4)+"0"+s_clock1.substring(4,5)
								+"0"+s_clock2.substring(0,1)+"0"+s_clock2.substring(1,2)
								+"0"+s_clock2.substring(3,4)+"0"+s_clock2.substring(4,5)
								+"0"+s_clock3.substring(0,1)+"0"+s_clock3.substring(1,2)
								+"0"+s_clock3.substring(3,4)+"0"+s_clock3.substring(4,5)
								+"0"+s_clock4.substring(0,1)+"0"+s_clock4.substring(1,2)
								+"0"+s_clock4.substring(3,4)+"0"+s_clock4.substring(4,5)
								+"0"+s_clock5.substring(0,1)+"0"+s_clock5.substring(1,2)
								+"0"+s_clock5.substring(3,4)+"0"+s_clock5.substring(4,5);
						sendCommand(KEY_DOSING_TIME, HexStrUtils.hexStringToBytes(s_clock));
						data_Dosing_Time = HexStrUtils.hexStringToBytes(s_clock);
					}
				}).setNegativeButton("取消", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {

					}
				});
				AlertDialog dialog = builder.create();
				dialog.show();
			}
		});
		clock5.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				AlertDialog.Builder builder = new AlertDialog.Builder(GosDeviceControlActivity.this);
				View timeSelect = View.inflate(GosDeviceControlActivity.this, R.layout.time_dialog5, null);
				alarm_timepicker5 = (TimePicker) timeSelect.findViewById(R.id.TimePicker5);
				alarm_timepicker5.setIs24HourView(true);
				builder.setIcon(R.mipmap.ic_launcher);
				builder.setTitle("设置闹钟");
				builder.setView(timeSelect);
				builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						String mHour;
						String mMinute;
						if (Build.VERSION.SDK_INT >= 23 ){
							alarm_timepicker5.getHour();
							mHour = String.valueOf(alarm_timepicker5.getHour());
							mMinute = String.valueOf(alarm_timepicker5.getMinute());
							clock5.setText(new StringBuilder().append(alarm_timepicker5.getHour() < 10 ? 0 + mHour : mHour)
									.append(":").append(alarm_timepicker5.getMinute() < 10 ? 0 + mMinute : mMinute));
							alarm5.setText(clock5.getText().toString());
						} else {
							alarm_timepicker5.getCurrentHour();
							mHour = String.valueOf(alarm_timepicker5.getCurrentHour());
							mMinute = String.valueOf(alarm_timepicker5.getCurrentMinute());
							clock5.setText(new StringBuilder().append(alarm_timepicker5.getCurrentHour() < 10 ? 0 + mHour : mHour)
									.append(":").append(alarm_timepicker5.getCurrentMinute() < 10 ? 0 + mMinute : mMinute));
							alarm5.setText(clock5.getText().toString());
						}
						s_clock5 = clock5.getText().toString();
						s_clock = "0"+s_clock1.substring(0,1)+"0"+s_clock1.substring(1,2)
								+"0"+s_clock1.substring(3,4)+"0"+s_clock1.substring(4,5)
								+"0"+s_clock2.substring(0,1)+"0"+s_clock2.substring(1,2)
								+"0"+s_clock2.substring(3,4)+"0"+s_clock2.substring(4,5)
								+"0"+s_clock3.substring(0,1)+"0"+s_clock3.substring(1,2)
								+"0"+s_clock3.substring(3,4)+"0"+s_clock3.substring(4,5)
								+"0"+s_clock4.substring(0,1)+"0"+s_clock4.substring(1,2)
								+"0"+s_clock4.substring(3,4)+"0"+s_clock4.substring(4,5)
								+"0"+s_clock5.substring(0,1)+"0"+s_clock5.substring(1,2)
								+"0"+s_clock5.substring(3,4)+"0"+s_clock5.substring(4,5);
						sendCommand(KEY_DOSING_TIME, HexStrUtils.hexStringToBytes(s_clock));
						data_Dosing_Time = HexStrUtils.hexStringToBytes(s_clock);
					}
				}).setNegativeButton("取消", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {

					}
				});
				AlertDialog dialog = builder.create();
				dialog.show();
			}
		});

		//闹钟开启
		switch1.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				if(isChecked){
					s_clock1 = clock1.getText().toString();
					int i = s_clock1.indexOf(":");
					int hour1 = Integer.parseInt(s_clock1.substring(0, i));
					int minute1 = Integer.parseInt(s_clock1.substring(i+1, s_clock1.length()));
					now = Calendar.getInstance();
					calendar = Calendar.getInstance();
					calendar.setTimeInMillis(System.currentTimeMillis());
					calendar.set(Calendar.HOUR_OF_DAY, hour1);
					calendar.set(Calendar.MINUTE, minute1);
					if (calendar.before(now)) {
						calendar.add(Calendar.DATE,1);
					}
					my_intent.putExtra("extra", "alarm on1");
					pending_intent1 = PendingIntent.getBroadcast(GosDeviceControlActivity.this, 0,
							my_intent, PendingIntent.FLAG_UPDATE_CURRENT);
					alarm_manager1.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(),
							AlarmManager.INTERVAL_DAY, pending_intent1);
				} else {
					alarm_manager1.cancel(pending_intent1);
					my_intent.putExtra("extra", "alarm off1");
					sendBroadcast(my_intent);
				}
			}
		});
		switch2.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				if(isChecked){
					s_clock2 = clock2.getText().toString();
					int i = s_clock2.indexOf(":");
					int hour2 = Integer.parseInt(s_clock2.substring(0, i));
					int minute2 = Integer.parseInt(s_clock2.substring(i+1, s_clock2.length()));
					now = Calendar.getInstance();
					calendar = Calendar.getInstance();
					calendar.setTimeInMillis(System.currentTimeMillis());
					calendar.set(Calendar.HOUR_OF_DAY, hour2);
					calendar.set(Calendar.MINUTE, minute2);
					if (calendar.before(now)) {
						calendar.add(Calendar.DATE,1);
					}
					my_intent.putExtra("extra", "alarm on2");
					pending_intent2 = PendingIntent.getBroadcast(GosDeviceControlActivity.this, 1,
							my_intent, PendingIntent.FLAG_UPDATE_CURRENT);
					alarm_manager2.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(),
							AlarmManager.INTERVAL_DAY, pending_intent2);
				} else {
					alarm_manager2.cancel(pending_intent2);
					my_intent.putExtra("extra", "alarm off2");
					sendBroadcast(my_intent);
				}
			}
		});
		switch3.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				if(isChecked){
					s_clock3 = clock3.getText().toString();
					int i = s_clock3.indexOf(":");
					int hour3 = Integer.parseInt(s_clock3.substring(0, i));
					int minute3 = Integer.parseInt(s_clock3.substring(i+1, s_clock3.length()));
					now = Calendar.getInstance();
					calendar = Calendar.getInstance();
					calendar.setTimeInMillis(System.currentTimeMillis());
					calendar.set(Calendar.HOUR_OF_DAY, hour3);
					calendar.set(Calendar.MINUTE, minute3);
					if (calendar.before(now)) {
						calendar.add(Calendar.DATE,1);
					}
					my_intent.putExtra("extra", "alarm on3");
					pending_intent3 = PendingIntent.getBroadcast(GosDeviceControlActivity.this, 2,
							my_intent, PendingIntent.FLAG_UPDATE_CURRENT);
					alarm_manager3.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(),
							AlarmManager.INTERVAL_DAY, pending_intent3);
				} else {
					alarm_manager3.cancel(pending_intent3);
					my_intent.putExtra("extra", "alarm off3");
					sendBroadcast(my_intent);
				}
			}
		});
		switch4.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				if(isChecked){
					s_clock4 = clock4.getText().toString();
					int i = s_clock4.indexOf(":");
					int hour4 = Integer.parseInt(s_clock4.substring(0, i));
					int minute4 = Integer.parseInt(s_clock4.substring(i+1, s_clock4.length()));
					now = Calendar.getInstance();
					calendar = Calendar.getInstance();
					calendar.setTimeInMillis(System.currentTimeMillis());
					calendar.set(Calendar.HOUR_OF_DAY, hour4);
					calendar.set(Calendar.MINUTE, minute4);
					if (calendar.before(now)) {
						calendar.add(Calendar.DATE,1);
					}
					my_intent.putExtra("extra", "alarm on4");
					pending_intent4 = PendingIntent.getBroadcast(GosDeviceControlActivity.this, 3,
							my_intent, PendingIntent.FLAG_UPDATE_CURRENT);
					alarm_manager4.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(),
							AlarmManager.INTERVAL_DAY, pending_intent4);
				} else {
					alarm_manager4.cancel(pending_intent4);
					my_intent.putExtra("extra", "alarm off4");
					sendBroadcast(my_intent);
				}
			}
		});
		switch5.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				if(isChecked){
					s_clock5 = clock5.getText().toString();
					int i = s_clock5.indexOf(":");
					int hour5 = Integer.parseInt(s_clock5.substring(0, i));
					int minute5 = Integer.parseInt(s_clock5.substring(i+1, s_clock5.length()));
					now = Calendar.getInstance();
					calendar = Calendar.getInstance();
					calendar.setTimeInMillis(System.currentTimeMillis());
					calendar.set(Calendar.HOUR_OF_DAY, hour5);
					calendar.set(Calendar.MINUTE, minute5);
					if (calendar.before(now)) {
						calendar.add(Calendar.DATE,1);
					}
					my_intent.putExtra("extra", "alarm on5");
					pending_intent5 = PendingIntent.getBroadcast(GosDeviceControlActivity.this, 4,
							my_intent, PendingIntent.FLAG_UPDATE_CURRENT);
					alarm_manager5.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(),
							AlarmManager.INTERVAL_DAY, pending_intent5);
				} else {
					alarm_manager5.cancel(pending_intent5);
					my_intent.putExtra("extra", "alarm off5");
					sendBroadcast(my_intent);
				}
			}
		});

	}

	void checkTime(){

		clockTime1 = Integer.parseInt(clock1.getText().toString().substring(0,2))*60
				+ Integer.parseInt(clock1.getText().toString().substring(3,5));
		clockTime2 = Integer.parseInt(clock2.getText().toString().substring(0,2))*60
				+ Integer.parseInt(clock2.getText().toString().substring(3,5));
		clockTime3 = Integer.parseInt(clock3.getText().toString().substring(0,2))*60
				+ Integer.parseInt(clock3.getText().toString().substring(3,5));
		clockTime4 = Integer.parseInt(clock4.getText().toString().substring(0,2))*60
				+ Integer.parseInt(clock4.getText().toString().substring(3,5));
		clockTime5 = Integer.parseInt(clock5.getText().toString().substring(0,2))*60
				+ Integer.parseInt(clock5.getText().toString().substring(3,5));
		now = Calendar.getInstance();
		now.setTimeInMillis(System.currentTimeMillis());
		nowTime = now.get(Calendar.HOUR)*60 + now.get(Calendar.MINUTE);

		switch (option){
			case 1:
				hint1.setVisibility(View.VISIBLE);
				if(Math.abs(nowTime-clockTime1) < 30){
					hint1.setText("本次服药");
				} else {
					hint1.setText("下次服药");
				}
				break;
			case 2:
				if(data_D_Remaining_Pack % 2 == data_Remaining_Pack % 2){
					hint1.setVisibility(View.VISIBLE);
					hint2.setVisibility(View.GONE);
					if(Math.abs(nowTime-clockTime1) < 30){
						hint1.setText("本次服药");
					} else {
						hint1.setText("下次服药");
					}
				} else if (data_D_Remaining_Pack % 2 == (data_Remaining_Pack+1) % 2){
					hint1.setVisibility(View.GONE);
					hint2.setVisibility(View.VISIBLE);
					if(Math.abs(nowTime-clockTime2) < 30){
						hint2.setText("本次服药");
					} else {
						hint2.setText("下次服药");
					}
				}
				break;
			case 3:
				if(data_D_Remaining_Pack % 3 == data_Remaining_Pack % 3){
					hint1.setVisibility(View.VISIBLE);
					hint2.setVisibility(View.GONE);
					hint3.setVisibility(View.GONE);
					if(Math.abs(nowTime-clockTime1) < 30){
						hint1.setText("本次服药");
					} else {
						hint1.setText("下次服药");
					}
				} else if (data_D_Remaining_Pack % 3 == (data_Remaining_Pack+2) % 3){
					hint1.setVisibility(View.GONE);
					hint2.setVisibility(View.VISIBLE);
					hint3.setVisibility(View.GONE);
					if(Math.abs(nowTime-clockTime2) < 30){
						hint2.setText("本次服药");
					} else {
						hint2.setText("下次服药");
					}
				} else if (data_D_Remaining_Pack % 3 == (data_Remaining_Pack+1) % 3){
					hint1.setVisibility(View.GONE);
					hint2.setVisibility(View.GONE);
					hint3.setVisibility(View.VISIBLE);
					if(Math.abs(nowTime-clockTime3) < 30){
						hint3.setText("本次服药");
					} else {
						hint3.setText("下次服药");
					}
				}
				break;
			case 4:
				if(data_D_Remaining_Pack % 4 == data_Remaining_Pack % 4){
					hint1.setVisibility(View.VISIBLE);
					hint2.setVisibility(View.GONE);
					hint3.setVisibility(View.GONE);
					hint4.setVisibility(View.GONE);
					if(Math.abs(nowTime-clockTime1) < 30){
						hint1.setText("本次服药");
					} else {
						hint1.setText("下次服药");
					}
				} else if (data_D_Remaining_Pack % 4 == (data_Remaining_Pack+3) % 4){
					hint1.setVisibility(View.GONE);
					hint2.setVisibility(View.VISIBLE);
					hint3.setVisibility(View.GONE);
					hint4.setVisibility(View.GONE);
					if(Math.abs(nowTime-clockTime2) < 30){
						hint2.setText("本次服药");
					} else {
						hint2.setText("下次服药");
					}
				} else if (data_D_Remaining_Pack % 4 == (data_Remaining_Pack+2) % 4){
					hint1.setVisibility(View.GONE);
					hint2.setVisibility(View.GONE);
					hint3.setVisibility(View.VISIBLE);
					hint4.setVisibility(View.GONE);
					if(Math.abs(nowTime-clockTime3) < 30){
						hint3.setText("本次服药");
					} else {
						hint3.setText("下次服药");
					}
				} else if (data_D_Remaining_Pack % 4 == (data_Remaining_Pack+1) % 4){
					hint1.setVisibility(View.GONE);
					hint2.setVisibility(View.GONE);
					hint3.setVisibility(View.GONE);
					hint4.setVisibility(View.VISIBLE);
					if(Math.abs(nowTime-clockTime4) < 30){
						hint4.setText("本次服药");
					} else {
						hint4.setText("下次服药");
					}
				}
				break;
			case 5:
				if(data_D_Remaining_Pack % 5 == data_Remaining_Pack % 5){
					hint1.setVisibility(View.VISIBLE);
					hint2.setVisibility(View.GONE);
					hint3.setVisibility(View.GONE);
					hint4.setVisibility(View.GONE);
					hint5.setVisibility(View.GONE);
					if(Math.abs(nowTime-clockTime1) < 30){
						hint1.setText("本次服药");
					} else {
						hint1.setText("下次服药");
					}
				} else if (data_D_Remaining_Pack % 5 == (data_Remaining_Pack+4) % 5){
					hint1.setVisibility(View.GONE);
					hint2.setVisibility(View.VISIBLE);
					hint3.setVisibility(View.GONE);
					hint4.setVisibility(View.GONE);
					hint5.setVisibility(View.GONE);
					if(Math.abs(nowTime-clockTime2) < 30){
						hint2.setText("本次服药");
					} else {
						hint2.setText("下次服药");
					}
				} else if (data_D_Remaining_Pack % 5 == (data_Remaining_Pack+3) % 5){
					hint1.setVisibility(View.GONE);
					hint2.setVisibility(View.GONE);
					hint3.setVisibility(View.VISIBLE);
					hint4.setVisibility(View.GONE);
					hint5.setVisibility(View.GONE);
					if(Math.abs(nowTime-clockTime3) < 30){
						hint3.setText("本次服药");
					} else {
						hint3.setText("下次服药");
					}
				} else if (data_D_Remaining_Pack % 5 == (data_Remaining_Pack+2) % 5){
					hint1.setVisibility(View.GONE);
					hint2.setVisibility(View.GONE);
					hint3.setVisibility(View.GONE);
					hint4.setVisibility(View.VISIBLE);
					hint5.setVisibility(View.GONE);
					if(Math.abs(nowTime-clockTime4) < 30){
						hint4.setText("本次服药");
					} else {
						hint4.setText("下次服药");
					}
				} else if (data_D_Remaining_Pack % 5 == (data_Remaining_Pack+1) % 5){
					hint1.setVisibility(View.GONE);
					hint2.setVisibility(View.GONE);
					hint3.setVisibility(View.GONE);
					hint4.setVisibility(View.GONE);
					hint5.setVisibility(View.VISIBLE);
					if(Math.abs(nowTime-clockTime5) < 30){
						hint5.setText("本次服药");
					} else {
						hint5.setText("下次服药");
					}
				}
				break;
			default:
				break;
		}
	}

	void checkLidState(){
		if(data_Lid_State != LidState){
			if(data_Lid_State == 1){
				TextView msg = new TextView(this);
				msg.setText("药盒被打开");
				msg.setPadding(0,20,0,0);
				msg.setTextSize(18);
				msg.setGravity(Gravity.CENTER);
				AlertDialog.Builder builder = new AlertDialog.Builder(GosDeviceControlActivity.this);
				builder.setView(msg)
						.setPositiveButton("确定", new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int which) {

							}
						})
						.setNegativeButton("取消", new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int which) {

							}
						})
						.show();
				NotificationManager notifyManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
				Notification.Builder builder2 = new Notification.Builder(this)
						.setSmallIcon(R.mipmap.ic_launcher)
						.setContentTitle("智能药盒")
						.setContentText("药盒被打开");
				notifyManager.notify(1, builder2.build());
			} else if(data_Lid_State == 2){
				TextView msg = new TextView(this);
				msg.setText("药盒已关闭");
				msg.setTextSize(18);
				msg.setPadding(0,20,0,0);
				msg.setGravity(Gravity.CENTER);
				AlertDialog.Builder builder = new AlertDialog.Builder(GosDeviceControlActivity.this);
				builder.setView(msg)
						.setPositiveButton("确定", new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int which) {

							}
						})
						.setNegativeButton("取消", new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int which) {

							}
						})
						.show();
				NotificationManager notifyManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
				Notification.Builder builder2 = new Notification.Builder(this)
						.setSmallIcon(R.mipmap.ic_launcher)
						.setContentTitle("智能药盒")
						.setContentText("药盒已关闭");
				notifyManager.notify(1, builder2.build());
			}
			LidState = data_Lid_State;
		}

		if(remainingPack != data_D_Remaining_Pack){
			if(remainingPack != 0){
				int num = remainingPack - data_D_Remaining_Pack;
				NotificationManager notifyManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
				Notification.Builder builder2 = new Notification.Builder(this)
						.setSmallIcon(R.mipmap.ic_launcher)
						.setContentTitle("智能药盒")
						.setContentText("取出"+num+"包药");
				notifyManager.notify(1, builder2.build());
			}
			remainingPack = data_D_Remaining_Pack;
		}
	}

	private void initDevice() {
		Intent intent = getIntent();
		mDevice = (GizWifiDevice) intent.getParcelableExtra("GizWifiDevice");
		mDevice.setListener(gizWifiDeviceListener);
		Log.i("Apptest", mDevice.getDid());
	}

	private String getDeviceName() {
		if (TextUtils.isEmpty(mDevice.getAlias())) {
			return mDevice.getProductName();
		}
		return mDevice.getAlias();
	}

	@Override
	protected void onResume() {
		super.onResume();
		getStatusOfDevice();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		mHandler.removeCallbacks(mRunnable);
		// 退出页面，取消设备订阅
		mDevice.setSubscribe(false);
		mDevice.setListener(null);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		default:
			break;
		}
	}

	/*
	 * ========================================================================
	 * EditText 点击键盘“完成”按钮方法
	 * ========================================================================
	 */
	@Override
	public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {

		switch (v.getId()) {
		case R.id.et_data_Remaining_Pack:
			et_data_Remaining_Pack.setCursorVisible(false);
			sendCommand(KEY_REMAINING_PACK, Integer.valueOf(v.getText().toString()));
			data_Remaining_Pack = Integer.valueOf(v.getText().toString());
			break;
		default:
			break;
		}
		hideKeyBoard();
		return false;

	}


	/*
	 * ========================================================================
	 * 菜单栏
	 * ========================================================================
	 */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.device_more, menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {

		case R.id.action_setDeviceInfo:
			setDeviceInfo();
			break;

		case R.id.action_getHardwareInfo:
			if (mDevice.isLAN()) {
				mDevice.getHardwareInfo();
			} else {
				myToast("只允许在局域网下获取设备硬件信息！");
			}
			break;

		case R.id.action_getStatu:
			mDevice.getDeviceStatus();
			break;

		default:
			break;
		}

		return super.onOptionsItemSelected(item);
	}

	/**
	 * Description:根据保存的的数据点的值来更新UI
	 */
	protected void updateUI() {
		if(totalPack != data_Remaining_Pack){
			totalPack = data_Remaining_Pack;
			et_data_Remaining_Pack.setText(data_Remaining_Pack+"");
			et_data_Remaining_Pack.setSelection(et_data_Remaining_Pack.getText().length());
		}
		if(!dosingTime.equals(bytesToHexString(data_Dosing_Time))){
			dosingTime = HexStrUtils.bytesToHexString(data_Dosing_Time).substring(0,40);
			clock1.setText(HexStrUtils.bytesToHexString(data_Dosing_Time).substring(1,2)
					+HexStrUtils.bytesToHexString(data_Dosing_Time).substring(3,4)+":"
					+HexStrUtils.bytesToHexString(data_Dosing_Time).substring(5,6)
					+HexStrUtils.bytesToHexString(data_Dosing_Time).substring(7,8));
			clock2.setText(HexStrUtils.bytesToHexString(data_Dosing_Time).substring(9,10)
					+HexStrUtils.bytesToHexString(data_Dosing_Time).substring(11,12)+":"
					+HexStrUtils.bytesToHexString(data_Dosing_Time).substring(13,14)
					+HexStrUtils.bytesToHexString(data_Dosing_Time).substring(15,16));
			clock3.setText(HexStrUtils.bytesToHexString(data_Dosing_Time).substring(17,18)
					+HexStrUtils.bytesToHexString(data_Dosing_Time).substring(19,20)+":"
					+HexStrUtils.bytesToHexString(data_Dosing_Time).substring(21,22)
					+HexStrUtils.bytesToHexString(data_Dosing_Time).substring(23,24));
			clock4.setText(HexStrUtils.bytesToHexString(data_Dosing_Time).substring(25,26)
					+HexStrUtils.bytesToHexString(data_Dosing_Time).substring(27,28)+":"
					+HexStrUtils.bytesToHexString(data_Dosing_Time).substring(29,30)
					+HexStrUtils.bytesToHexString(data_Dosing_Time).substring(31,32));
			clock5.setText(HexStrUtils.bytesToHexString(data_Dosing_Time).substring(33,34)
					+HexStrUtils.bytesToHexString(data_Dosing_Time).substring(35,36)+":"
					+HexStrUtils.bytesToHexString(data_Dosing_Time).substring(37,38)
					+HexStrUtils.bytesToHexString(data_Dosing_Time).substring(39,40));
			alarm1.setText(clock1.getText().toString());
			alarm2.setText(clock2.getText().toString());
			alarm3.setText(clock3.getText().toString());
			alarm4.setText(clock4.getText().toString());
			alarm5.setText(clock5.getText().toString());
			s_clock1 = clock1.getText().toString();
			s_clock2 = clock2.getText().toString();
			s_clock3 = clock3.getText().toString();
			s_clock4 = clock4.getText().toString();
			s_clock5 = clock5.getText().toString();
		}
		checkLidState();
	}

	private void setEditText(EditText et, Object value) {
		et.setText(value.toString());
		et.setSelection(value.toString().length());
		et.clearFocus();
	}

	/**
	 * Description:页面加载后弹出等待框，等待设备可被控制状态回调，如果一直不可被控，等待一段时间后自动退出界面
	 */
	private void getStatusOfDevice() {
		// 设备是否可控
		if (isDeviceCanBeControlled()) {
			// 可控则查询当前设备状态
			mDevice.getDeviceStatus();
		} else {
			// 显示等待栏
			progressDialog.show();
			if (mDevice.isLAN()) {
				// 小循环10s未连接上设备自动退出
				mHandler.postDelayed(mRunnable, 10000);
			} else {
				// 大循环20s未连接上设备自动退出
				mHandler.postDelayed(mRunnable, 20000);
			}
		}
	}

	/**
	 * 发送指令,下发单个数据点的命令可以用这个方法
	 * 
	 * <h3>注意</h3>
	 * <p>
	 * 下发多个数据点命令不能用这个方法多次调用，一次性多次调用这个方法会导致模组无法正确接收消息，参考方法内注释。
	 * </p>
	 * 
	 * @param key
	 *            数据点对应的标识名
	 * @param value
	 *            需要改变的值
	 */
	private void sendCommand(String key, Object value) {
		if (value == null) {
			return;
		}
		int sn = 5;
		ConcurrentHashMap<String, Object> hashMap = new ConcurrentHashMap<String, Object>();
		hashMap.put(key, value);
		// 同时下发多个数据点需要一次性在map中放置全部需要控制的key，value值
		// hashMap.put(key2, value2);
		// hashMap.put(key3, value3);
		mDevice.write(hashMap, sn);
		Log.i("liang", "下发命令：" + hashMap.toString());
	}

	private boolean isDeviceCanBeControlled() {
		return mDevice.getNetStatus() == GizWifiDeviceNetStatus.GizDeviceControlled;
	}

	private void toastDeviceNoReadyAndExit() {
		Toast.makeText(this, "设备无响应，请检查设备是否正常工作", Toast.LENGTH_SHORT).show();
		finish();
	}

	private void toastDeviceDisconnectAndExit() {
		Toast.makeText(GosDeviceControlActivity.this, "连接已断开", Toast.LENGTH_SHORT).show();
		finish();
	}

	/**
	 * 展示设备硬件信息
	 * 
	 * @param hardwareInfo
	 */
	private void showHardwareInfo(String hardwareInfo) {
		String hardwareInfoTitle = "设备硬件信息";
		new AlertDialog.Builder(this).setTitle(hardwareInfoTitle).setMessage(hardwareInfo)
				.setPositiveButton(R.string.besure, null).show();
	}

	/**
	 * Description:设置设备别名与备注
	 */
	private void setDeviceInfo() {

		final Dialog mDialog = new AlertDialog.Builder(this).setView(new EditText(this)).create();
		mDialog.show();

		Window window = mDialog.getWindow();
		window.setContentView(R.layout.alert_gos_set_device_info);

		final EditText etAlias;
		final EditText etRemark;
		etAlias = (EditText) window.findViewById(R.id.etAlias);
		etRemark = (EditText) window.findViewById(R.id.etRemark);

		LinearLayout llNo, llSure;
		llNo = (LinearLayout) window.findViewById(R.id.llNo);
		llSure = (LinearLayout) window.findViewById(R.id.llSure);

		if (!TextUtils.isEmpty(mDevice.getAlias())) {
			setEditText(etAlias, mDevice.getAlias());
		}
		if (!TextUtils.isEmpty(mDevice.getRemark())) {
			setEditText(etRemark, mDevice.getRemark());
		}

		llNo.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				mDialog.dismiss();
			}
		});

		llSure.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (TextUtils.isEmpty(etRemark.getText().toString())
						&& TextUtils.isEmpty(etAlias.getText().toString())) {
					myToast("请输入设备别名或备注！");
					return;
				}
				mDevice.setCustomInfo(etRemark.getText().toString(), etAlias.getText().toString());
				mDialog.dismiss();
				String loadingText = (String) getText(R.string.loadingtext);
				progressDialog.setMessage(loadingText);
				progressDialog.show();
			}
		});

		mDialog.setOnDismissListener(new OnDismissListener() {
			@Override
			public void onDismiss(DialogInterface dialog) {
				hideKeyBoard();
			}
		});
	}
	
	/*
	 * 获取设备硬件信息回调
	 */
	@Override
	protected void didGetHardwareInfo(GizWifiErrorCode result, GizWifiDevice device,
			ConcurrentHashMap<String, String> hardwareInfo) {
		super.didGetHardwareInfo(result, device, hardwareInfo);
		StringBuffer sb = new StringBuffer();
		if (GizWifiErrorCode.GIZ_SDK_SUCCESS != result) {
			myToast("获取设备硬件信息失败：" + result.name());
		} else {
			sb.append("Wifi Hardware Version:" + hardwareInfo.get(WIFI_HARDVER_KEY) + "\r\n");
			sb.append("Wifi Software Version:" + hardwareInfo.get(WIFI_SOFTVER_KEY) + "\r\n");
			sb.append("MCU Hardware Version:" + hardwareInfo.get(MCU_HARDVER_KEY) + "\r\n");
			sb.append("MCU Software Version:" + hardwareInfo.get(MCU_SOFTVER_KEY) + "\r\n");
			sb.append("Wifi Firmware Id:" + hardwareInfo.get(WIFI_FIRMWAREID_KEY) + "\r\n");
			sb.append("Wifi Firmware Version:" + hardwareInfo.get(WIFI_FIRMWAREVER_KEY) + "\r\n");
			sb.append("Product Key:" + "\r\n" + hardwareInfo.get(PRODUCT_KEY) + "\r\n");

			// 设备属性
			sb.append("Device ID:" + "\r\n" + mDevice.getDid() + "\r\n");
			sb.append("Device IP:" + mDevice.getIPAddress() + "\r\n");
			sb.append("Device MAC:" + mDevice.getMacAddress() + "\r\n");
		}
		showHardwareInfo(sb.toString());
	}
	
	/*
	 * 设置设备别名和备注回调
	 */
	@Override
	protected void didSetCustomInfo(GizWifiErrorCode result, GizWifiDevice device) {
		super.didSetCustomInfo(result, device);
		if (GizWifiErrorCode.GIZ_SDK_SUCCESS == result) {
			myToast("设置成功");
			progressDialog.cancel();
			finish();
		} else {
			myToast("设置失败：" + result.name());
		}
	}

	/*
	 * 设备状态改变回调，只有设备状态为可控才可以下发控制命令
	 */
	@Override
	protected void didUpdateNetStatus(GizWifiDevice device, GizWifiDeviceNetStatus netStatus) {
		super.didUpdateNetStatus(device, netStatus);
		if (netStatus == GizWifiDeviceNetStatus.GizDeviceControlled) {
			mHandler.removeCallbacks(mRunnable);
			progressDialog.cancel();
		} else {
			mHandler.sendEmptyMessage(handler_key.DISCONNECT.ordinal());
		}
	}
	
	/*
	 * 设备上报数据回调，此回调包括设备主动上报数据、下发控制命令成功后设备返回ACK
	 */
	@Override
	protected void didReceiveData(GizWifiErrorCode result, GizWifiDevice device,
			ConcurrentHashMap<String, Object> dataMap, int sn) {
		super.didReceiveData(result, device, dataMap, sn);
		Log.i("liang", "接收到数据");
		if (result == GizWifiErrorCode.GIZ_SDK_SUCCESS && dataMap.get("data") != null) {
			getDataFromReceiveDataMap(dataMap);
			mHandler.sendEmptyMessage(handler_key.UPDATE_UI.ordinal());
		}
	}

}