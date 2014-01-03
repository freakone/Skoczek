package com.pwr.skoczek;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.Handler;
import android.util.Log;


public class BluetoothManager {
	
	public static final int DISCONNECTED = 1;
	public static final int CONNECTED = 2;
	public static final int CONNECTION_ERROR = 3;
	private int STATE = 1;
	
	private BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
	private BluetoothDevice mDevice;
	private BluetoothSocket mSocket;
	private final UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb"); //uuid dla wymiany danych
	final Handler handler = new Handler(); 
	private ConnectedThread mConnectedThread;
	
	private OnStateUpdate mOnStateUpdate;
	private OnDataReceivedInterface mOnDataReceived;
	
	public BluetoothManager()
	{
		
	}
	
	private void changeState(int state)
	{
		STATE = state;
		
		if(mOnStateUpdate != null)
     		mOnStateUpdate.onStateChange(state);
	}
	
	private class ConnectedThread extends Thread {
    	
        private InputStream mmInStream = null;
        private OutputStream mmOutStream = null;

        public ConnectedThread(BluetoothSocket socket) {
        	try
        	{
        		mmInStream = socket.getInputStream();
        		mmOutStream = socket.getOutputStream();
        	}
        	catch(IOException e)
        	{
        		 handler.post(new Runnable()
                 {
                     public void run()
                     {
                     	btDisconnect();
                     	changeState(CONNECTION_ERROR);
              			mConnectedThread = null;
                     }
                 });
        	}
         
        }

        public void run() {
 
            
            int bytes;

            while (true) {
                try {
                    // Read from the InputStream
                    bytes = mmInStream.read();  
                    
                    if(bytes > -1)
                    {              
                                 	
                    	final int b = bytes;
            		
	                    handler.post(new Runnable()
		                {
		                    public void run()
		                     {
		                        	if(mOnDataReceived != null)
		                        		mOnDataReceived.onDataReceived(b);
		                     }
		                    });
                    }                  
                } catch (IOException e) 
                {
                	Log.e("BT", "watcher", e);
                	break;
                }
            }
        }

      
        void write(int one)
        {
        	 if(STATE != CONNECTED)
          	   return;
             
             try{
            	 mmOutStream.write(one);
             } catch(IOException e){
            	 handler.post(new Runnable()
                 {
                     public void run()
                     {
                     	btDisconnect();
                     	changeState(CONNECTION_ERROR);
              			mConnectedThread = null;
                     }
                 });
             }    
        }
        
        void write(String str)
        {
           if(STATE != CONNECTED)
        	   return;
           
           try{
        	   mmOutStream.write(str.getBytes());
           } catch(IOException e){
        	  
        	   synchronized(this){
       			btDisconnect();
       			changeState(CONNECTION_ERROR);
       			mConnectedThread = null;
       		}
           }
           
        }   
    }
	
	public void btConnect()    {
	    	if(mDevice == null)
	    		return;    	
	    	try{   	  
	        	mSocket = mDevice.createRfcommSocketToServiceRecord(uuid);
	        	mSocket.connect();
	    	
	    	} catch(IOException e){
	    		Log.e("BT","point1", e); 		
	    		
	    		btDisconnect();    		
	    		changeState(CONNECTION_ERROR);
	    		return;
	    	}
	    	
	    	mConnectedThread = new ConnectedThread(mSocket);
	    	mConnectedThread.start();
	    
	    	changeState(CONNECTED);		    	
	    }

	public void btConnect(String address)
	    {    	
	    	mDevice = mBluetoothAdapter.getRemoteDevice(address);	 
	    	btConnect();
	    }
	    
	public void btDisconnect() 
	    {
	    	if(mSocket == null)
	    		return;
	    	
	    	if(mConnectedThread != null)
	    	{
	    		mConnectedThread.interrupt();
	    		mConnectedThread = null;
	    	}
	    	
	    	try{ 
	        	mSocket.close();    	
	    	} catch(IOException e){
	    		Log.e("BT","point3", e);  		
	    	}  	
	    	
	    	mSocket = null;
	    	
	    	changeState(DISCONNECTED);
	    }
	
	public void setOnStateUpdate(OnStateUpdate eventListener)
	{
		mOnStateUpdate = eventListener;
	}
	
	public void setOnDataReceivedInterface(OnDataReceivedInterface eventListener)
	{
		mOnDataReceived = eventListener;
	}
	
	public String getDeviceName()
	{
		if(mDevice == null)
			return "null";
		
		return mDevice.getName();
	}
	
	public int getState()
	{
		return STATE;
	}
	
	public void write(int i)
	{
		mConnectedThread.write(i);
	}
	
	public void write(String s)
	{
		mConnectedThread.write(s);
	}
	    


}


