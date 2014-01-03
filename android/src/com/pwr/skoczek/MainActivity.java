package com.pwr.skoczek;

import java.io.IOException;
import java.util.Locale;
import java.util.Set;

import android.widget.Button;
import android.content.Intent;
import android.bluetooth.*;
import android.app.ActionBar;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ListView;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.RadioButton;
import android.widget.SeekBar;


public class MainActivity extends FragmentActivity implements
		ActionBar.TabListener {

	public static final int REQUEST_ENABLE_BT = 101;
	SectionsPagerAdapter mSectionsPagerAdapter;
	ViewPager mViewPager;
	private SensorManager mSensorManager;
	private Sensor mSensorMag;
	private Sensor mSensorAcc;
	
	private static BluetoothManager mBluetooth = new BluetoothManager();
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		final ActionBar actionBar = getActionBar();
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
		mSectionsPagerAdapter = new SectionsPagerAdapter(
				getSupportFragmentManager());
		mViewPager = (ViewPager) findViewById(R.id.pager);
		mViewPager.setAdapter(mSectionsPagerAdapter);
		mViewPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
					@Override
					public void onPageSelected(int position) {
						actionBar.setSelectedNavigationItem(position);
					}
				});
		
		for (int i = 0; i < mSectionsPagerAdapter.getCount(); i++) {

			actionBar.addTab(actionBar.newTab()
					.setText(mSectionsPagerAdapter.getPageTitle(i))
					.setTabListener(this));
		}
		
		 mSensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);
		 mSensorMag = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
		 mSensorAcc = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
		 
		 BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
		 
		 if (mBluetoothAdapter == null) {
			   Toast.makeText(this, "Brak Bluetootha", Toast.LENGTH_LONG).show();
			   finish();
			   return;
			}
		 
		 if (!mBluetoothAdapter.isEnabled()) {
			    Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);			    
				startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);				
			}

	        mBluetooth.setOnStateUpdate(mUpdateEvent);
	        mBluetooth.setOnDataReceivedInterface(mOnDataReceived);
	        
	        
	}
	 
	protected void onActivityResult(int requestCode, int resultCode,
	             Intent data) {
	         if (requestCode == REQUEST_ENABLE_BT) {
	             if (resultCode == RESULT_CANCELED) {	                 
	            	 finish();
	             }
	         }
	     }
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public void onTabSelected(ActionBar.Tab tab,
			FragmentTransaction fragmentTransaction) {

		mViewPager.setCurrentItem(tab.getPosition());
		
	}

	@Override
	public void onTabUnselected(ActionBar.Tab tab,
			FragmentTransaction fragmentTransaction) {
		
		if(tab.getPosition() == 0)
		{
			if(AutoControlFragment.mSwitchControl != null)
			{
				AutoControlFragment.mSwitchControl.setChecked(false);
			}
		}else if(tab.getPosition() == 1)
		{
			if(ManualControlFragment.mSwitchSensory != null)
			{
				ManualControlFragment.mSwitchSensory.setChecked(false);
			}			
		}
	}

	@Override
	public void onTabReselected(ActionBar.Tab tab,
			FragmentTransaction fragmentTransaction) {
	}

	public class SectionsPagerAdapter extends FragmentPagerAdapter {

		public SectionsPagerAdapter(FragmentManager fm) {
			super(fm);
		}

		@Override
		public Fragment getItem(int position) {
			switch(position)
			{
			case 0:
				return new AutoControlFragment();
			case 1:
				return new ManualControlFragment();
			case 2:
				return new  BluetoothControlFragment();
			}
			return null;
		}

		@Override
		public int getCount() {
			// Show 3 total pages.
			return 3;
		}

		@Override
		public CharSequence getPageTitle(int position) {
			Locale l = Locale.getDefault();
			switch (position) {
			case 0:
				return getString(R.string.title_section1).toUpperCase(l);
			case 1:
				return getString(R.string.title_section2).toUpperCase(l);
			case 2:
				return getString(R.string.title_section3).toUpperCase(l);
			}
			return null;
		}
	}

	public static class AutoControlFragment extends Fragment {


		private static float[] accVals = new float[3];
		private static float[] magVals = new float[3];
		private static float[] m_rotationMatrix = new float[16];
		private static float[] m_orientation = new float[3];
		private static Button mButtonJump;
		private static Button mButtonPosition;
		private static TextView mTextOrientation;
		private static Switch mSwitchControl;
		private static RadioButton mRadio10;
		private static RadioButton mRadio15;
		private static RadioButton mRadio25;
		private static int last_left_speed_value = 0;
		private static int last_right_speed_value = 0;

		public static SensorEventListener sensorListener = new SensorEventListener() {

		
			@Override
			public void onSensorChanged(SensorEvent event) {
				
				
				switch(event.sensor.getType())
				{
				case Sensor.TYPE_ACCELEROMETER:
					 System.arraycopy(event.values, 0, accVals, 0, 3);
					break;
				case Sensor.TYPE_MAGNETIC_FIELD:
					 System.arraycopy(event.values, 0, magVals, 0, 3);
					break;
				}
				
				if(magVals != null && accVals != null)
				{			
					if(SensorManager.getRotationMatrix(m_rotationMatrix, null,
                        accVals, magVals))
					{
						SensorManager.getOrientation(m_rotationMatrix, m_orientation);
						int speedo_angle = (int)(m_orientation[2]*57.29);	
						int driving_angle = (int)(m_orientation[1]*57.29);
						
						if(speedo_angle < 0)
						{
							speedo_angle *= -1;
							driving_angle *= -1;
						}	
						
						int left = 50, right = 50;
						
						 if(speedo_angle < 70)
							{
							 int val = (speedo_angle - 70)/2;
								left -= val;
								right -= val;
							}
						 else if(speedo_angle > 100)
							{
							 int val = (speedo_angle - 100)/2;
								left -= val;
								right -= val;
							}
						 
						 
							if(left > 50 && right > 50)
							{
								if(driving_angle < -10)
								{
									driving_angle +=10;
									
									if(left + driving_angle > 50)
										left += driving_angle;
									else
										left = 50;
								}
								
								if(driving_angle > 10)
								{
									driving_angle -= 10;
									
									if(right - driving_angle > 50)
										right -= driving_angle;
									else
										right = 50;
								}					
								
							}
							else if(left < 50 && right < 50)
							{
								if(driving_angle < -10)
								{
									driving_angle +=10;
									
									if(left + driving_angle > 0)
										left += driving_angle;
									else
										left = 0;
								}
								
								if(driving_angle > 10)
								{
									driving_angle -= 10;
									
									if(right - driving_angle > 0)
										right -= driving_angle;
									else
										right = 50;
								}													
							}
						
							if(mTextOrientation!=null)
								mTextOrientation.setText("X: " + String.valueOf(speedo_angle) + " Y: " + String.valueOf(driving_angle) + "\nLewy silnik: " + String.valueOf(left)+ " Prawy silnik: " + String.valueOf(right));
							
							
						if(mBluetooth.getState() == BluetoothManager.CONNECTED)
						{
							if(mSwitchControl!=null && mSwitchControl.isChecked())
							{
								
								if(last_left_speed_value != left)
								{
									mBluetooth.write(0xFF);
									mBluetooth.write(0x12);
									mBluetooth.write(Integer.toHexString(left).toUpperCase());
									mBluetooth.write(0x0A);
									last_left_speed_value = left;
								}
								
								if(last_right_speed_value != right)
								{
									mBluetooth.write(0xFF);
									mBluetooth.write(0x13);
									mBluetooth.write(Integer.toHexString(right).toUpperCase());
									mBluetooth.write(0x0A);
									last_right_speed_value = right;
								}
								
							}
							else // zatrzymanie po wylaczeniu switcha
							{
								if(last_left_speed_value != 50)
								{
									mBluetooth.write(0xFF);
									mBluetooth.write(0x12);
									mBluetooth.write("32");
									mBluetooth.write(0x0A);
									last_left_speed_value = 50;
								}
								
								if(last_right_speed_value != 50)
								{
									mBluetooth.write(0xFF);
									mBluetooth.write(0x13);
									mBluetooth.write("32");
									mBluetooth.write(0x0A);
									last_right_speed_value = 50;
								}
							}
						}
					}
				}
			}
			
			@Override
			public void onAccuracyChanged(Sensor sensor, int accuracy) {
				// TODO Auto-generated method stub
				
			}
		};
	
		public AutoControlFragment()
		{
			
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			
			View rootView = inflater.inflate(R.layout.auto_control,
					container, false);		
			mTextOrientation = (TextView)rootView.findViewById(R.id.textView1);
			mButtonJump = (Button)rootView.findViewById(R.id.button2);
			mButtonPosition = (Button)rootView.findViewById(R.id.button1);
			mRadio10 = (RadioButton)rootView.findViewById(R.id.radio0);
			mRadio15 = (RadioButton)rootView.findViewById(R.id.radio1);
			mRadio25 = (RadioButton)rootView.findViewById(R.id.radio2);
			mSwitchControl = (Switch)rootView.findViewById(R.id.switch1);
			
			mButtonJump.setOnClickListener(mButtonJumpListener);
			mButtonPosition.setOnClickListener(mButtonPositionListener);
			return rootView;
		}	
		
		private static OnClickListener mButtonJumpListener = new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				
				mBluetooth.write(0xFF);
				mBluetooth.write(0x18);
				
				if(mRadio10.isChecked())
					mBluetooth.write("0A");
				else if(mRadio15.isChecked())
					mBluetooth.write("0F");
				else if(mRadio25.isChecked())
					mBluetooth.write("19");
				
				mBluetooth.write(0x0A);
				
			}
		};
		
		private static OnClickListener mButtonPositionListener = new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				
				mBluetooth.write(0xFF);
				mBluetooth.write(0x17);
				mBluetooth.write(0x0A);
				
			}
		};
	}

	public static class ManualControlFragment extends Fragment {

		private static SeekBar mSeekKat;
		private static SeekBar mSeekNaciaganie;
		private static SeekBar mSeekOdlaczenie;
		private static SeekBar mSeekPrawe;
		private static SeekBar mSeekLewe;
		private static TextView mTextSensory;
		private static Switch mSwitchSensory;
		private static Thread mReadSensorsThread;
		
		public ManualControlFragment() {
		}
		
		private static class ReadSensorsThread extends Thread {
	    	
	    	public void run() {
	    		try
	    		{ 		
	    			while(true)
	    			{
		    			mBluetooth.write(0xFF);
						mBluetooth.write(0x19);
						mBluetooth.write(0x0A);
						
						Thread.sleep(500);
	    			}
	    		}
	    		catch(InterruptedException e)
	    		{
	    			Log.e("BT","pointomobnmobmbo", e);  	    		
	    		}
	    	}
	    	
	    }
		
		

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			View rootView = inflater.inflate(R.layout.manual_control,
					container, false);		
			
			mSeekKat = (SeekBar)rootView.findViewById(R.id.seekBar1);
			mSeekNaciaganie = (SeekBar)rootView.findViewById(R.id.seekBar2);
			mSeekOdlaczenie = (SeekBar)rootView.findViewById(R.id.seekBar3);
			mSeekPrawe = (SeekBar)rootView.findViewById(R.id.seekBar4);
			mSeekLewe = (SeekBar)rootView.findViewById(R.id.seekBar5);
			mTextSensory = (TextView)rootView.findViewById(R.id.textView6);
			mSwitchSensory = (Switch)rootView.findViewById(R.id.switch2);
			
			mSwitchSensory.setOnCheckedChangeListener(mSwitchOnChange);
			
			mSeekKat.setOnSeekBarChangeListener(mSeekListener);
			mSeekNaciaganie.setOnSeekBarChangeListener(mSeekListener);
			mSeekOdlaczenie.setOnSeekBarChangeListener(mSeekListener);
			mSeekPrawe.setOnSeekBarChangeListener(mSeekListener);
			mSeekLewe.setOnSeekBarChangeListener(mSeekListener);
			return rootView;
		}
		
		private static OnCheckedChangeListener mSwitchOnChange = new OnCheckedChangeListener() {
			
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

				if(isChecked)
				{
					if(mReadSensorsThread == null || !mReadSensorsThread.isAlive())
					{
						mReadSensorsThread = new ReadSensorsThread();
						mReadSensorsThread.start();
					}					
				}
				else
				{
					if(mReadSensorsThread != null && mReadSensorsThread.isAlive())
	  					mReadSensorsThread.interrupt();					
				}
			}
		};
		
		private static OnSeekBarChangeListener mSeekListener = new OnSeekBarChangeListener() {
			
			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
				
			}
			
			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void onProgressChanged(SeekBar seekBar, int progress,
					boolean fromUser) {

				if(mBluetooth.getState() == BluetoothManager.CONNECTED && fromUser)
				{
					mBluetooth.write(0xFF);			
					switch(seekBar.getId())
					{				
					case 2131230731: //kat nachylenia
						mBluetooth.write(0x11);	
					break;
					case 2131230733: // naciaganie
						mBluetooth.write(0x14);
					break;
					case 2131230735: // odlaczanie
						mBluetooth.write(0x15);
						progress +=85;
					break;		
					case 2131230737: // prawe
						mBluetooth.write(0x13);
					break;
					case 2131230739: // lewe
						mBluetooth.write(0x12);
					break;
					}
					
					String prg = Integer.toHexString(progress);
					
					if(prg.length() == 1)
						prg = "0" + prg;
					
					mBluetooth.write(prg.toUpperCase());
					mBluetooth.write(0x0A);
				}
			}
		};
	}
	
	public static class BluetoothControlFragment extends Fragment {
		
		private static View rootView;
		private static TextView mTextState;
		private ArrayAdapter<String> mDevices;
		private static Button mButtonConnect;
		private static String address;
		
		public BluetoothControlFragment() {
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			rootView = inflater.inflate(R.layout.activity_bluetooth,
					container, false);	
			mTextState = (TextView)rootView.findViewById(R.id.textView1);
			mButtonConnect = (Button)rootView.findViewById(R.id.button1);
			mButtonConnect.setOnClickListener(mButtonConnectClickListener);
			changeState();
			
			 mDevices = new ArrayAdapter<String>(rootView.getContext(), R.layout.device_style);
		     ListView pairedListView = (ListView)rootView.findViewById(R.id.listView1);
		     pairedListView.setAdapter(mDevices);
		     pairedListView.setOnItemClickListener(mDeviceClickListener);
			
			
			 BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
		        Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
		       
		        for(BluetoothDevice device : pairedDevices)
		        {
		           mDevices.add(device.getName() + "\n" + device.getAddress());
		        }
			
			return rootView;
		}
		
		private OnClickListener mButtonConnectClickListener = new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				
				if(mBluetooth.getState() != BluetoothManager.CONNECTED && address != null)
				{
					mBluetooth.btConnect(address);					
				}
				else
				{
					mBluetooth.btDisconnect();					
				}
				
			}
		};
		
		 private OnItemClickListener mDeviceClickListener = new OnItemClickListener() {
		        public void onItemClick(AdapterView<?> av, View v, int arg2, long arg3) {           
		        	
		        	
		            String info = ((TextView) v).getText().toString();
		            address = info.substring(info.length() - 17);
		            
		            if(mBluetooth.getState() != BluetoothManager.CONNECTED)
		            	mButtonConnect.setText("Po³¹cz z "  + info);
		            
		        }
		    };
		
		public static void changeState(){
		    			    	
		    	if(mTextState == null)
		    		return;
		    	
		    	switch(mBluetooth.getState())
		    	{
		    	case BluetoothManager.CONNECTED:	
		        	mTextState.setTextColor(Color.BLUE);
		        	mButtonConnect.setText("Roz³¹cz");
		        	mTextState.setText("Po³¹czony z " + mBluetooth.getDeviceName());        	
		        	break;        	
		    	case BluetoothManager.DISCONNECTED:    		
		        	mTextState.setTextColor(Color.BLACK);
		        	mButtonConnect.setText("Po³¹cz");
		        	mTextState.setText("Roz³¹czony");        	
		        	break;
		    	case BluetoothManager.CONNECTION_ERROR:
		        	mTextState.setTextColor(Color.RED);
		        	mTextState.setText("B³¹d po³¹czenia");
		        	break;
		    	
		    	}  	
		    	
		    }
	}
	
	public OnStateUpdate mUpdateEvent = new OnStateUpdate() {
		
		@Override
		public void onStateChange(int state) {
			BluetoothControlFragment.changeState();					
		}
	};
	
	private static char[] buffer = new char[40];
	private static int pos = 0;
	public OnDataReceivedInterface mOnDataReceived = new OnDataReceivedInterface() {
		
		@Override
		public void onDataReceived(int data) {
						
			if((pos > 0 && buffer[0] != 0xff )|| pos > 35) 
		         pos = 0;
			
			buffer[pos] = (char)data;        
		    pos++;
			
			if(buffer[pos-1] == 0x0A)
			{
				switch(buffer[1])
				{
				case 0x19:
					
					int naciag = Integer.parseInt(String.copyValueOf(buffer,  2,  3), 16);
					int sensr = Integer.parseInt(String.copyValueOf(buffer,  14,  3), 16);
					int sensl = Integer.parseInt(String.copyValueOf(buffer,  17,  3), 16);
					
					if(ManualControlFragment.mTextSensory != null)
						ManualControlFragment.mTextSensory.setText("Prawy sensor: " + String.valueOf(sensr) + " Lewy sensor: " + String.valueOf(sensl) + " Sensor sprê¿yny: "+String.valueOf(naciag));
					break;					
				
				}
				
				pos = 0;
			}
			
		}
	};
	
	 @Override
	protected void onResume() {
	    super.onResume();
		 mSensorManager.registerListener(AutoControlFragment.sensorListener, mSensorMag, SensorManager.SENSOR_DELAY_NORMAL);
		 mSensorManager.registerListener(AutoControlFragment.sensorListener, mSensorAcc, SensorManager.SENSOR_DELAY_NORMAL);
	 }

	  @Override
	protected void onPause() {
	    // unregister listener
	    super.onPause();
	    mSensorManager.unregisterListener(AutoControlFragment.sensorListener);
	    AutoControlFragment.mSwitchControl.setChecked(false);
	    ManualControlFragment.mSwitchSensory.setChecked(false);
	  }

	 @Override
	 public void onBackPressed() {
	 
	 }

}
