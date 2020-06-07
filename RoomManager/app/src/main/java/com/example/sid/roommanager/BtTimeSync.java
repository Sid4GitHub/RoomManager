package com.example.sid.roommanager;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.WindowManager;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Calendar;
import java.util.UUID;


public class BtTimeSync extends AppCompatActivity {
    BluetoothAdapter btAdapter;
    public static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    protected static final int SUCCESS_CONNECT = 0;
    protected static final int MESSAGE_READ = 1;
    BluetoothDevice selectedDevice ;
    BluetoothSocket mmSocket;
    ConnectThread connect;
    ConnectedThread connectedThread;
    String dt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_bt_time_sync);
    }

    void init(){
        BluetoothManager bluetoothManager =
                (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        btAdapter = bluetoothManager.getAdapter();
        selectedDevice=btAdapter.getRemoteDevice(getIntent().getExtras().getString("MAC"));
    }


    Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {

            super.handleMessage(msg);
            switch(msg.what){
                case SUCCESS_CONNECT:
                    // DO something
                    //ConnectedThread connectedThread = new ConnectedThread((BluetoothSocket)msg.obj);

                    connectedThread = new ConnectedThread(mmSocket);
                    connectedThread.start();

                    Toast.makeText(getApplicationContext(), "CONNECT", Toast.LENGTH_SHORT).show();
                    //getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);


                    Calendar c = Calendar.getInstance();
                    dt = "!m0:" + c.get(Calendar.DAY_OF_MONTH) + "/" + c.get(Calendar.MONTH) + "/" + c.get(Calendar.YEAR) + "/" + c.get(Calendar.HOUR_OF_DAY) + "/" + c.get(Calendar.MINUTE) + "/" + c.get(Calendar.SECOND) + ";";
                    connectedThread.write(dt.getBytes());
                    //String s = "successfully connected";
                    //connectedThread.write(new String("@#"+s+"#@").getBytes());
                    //connectedThread.write(new String(s+";").getBytes());
                    //connectedThread.cancel();
                    break;
                case MESSAGE_READ:
                    byte[] readBuf = (byte[])msg.obj;
                    String string = new String(readBuf);

                    if(dt.compareToIgnoreCase(string)==0){
                        Toast.makeText(getApplicationContext(), "Time Synchronization was Successful", Toast.LENGTH_SHORT).show();
                    }
                    else{
                        Toast.makeText(getApplicationContext(), "Time Synchronization was Unsuccessful", Toast.LENGTH_SHORT).show();
                    }

                    Intent i = new Intent(BtTimeSync.this, BtIns.class);
                    i.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT | Intent.FLAG_ACTIVITY_PREVIOUS_IS_TOP);
                    startActivity(i);

                    break;
            }
        }
    };

    private class ConnectThread extends Thread {

        //private final BluetoothDevice mmDevice;

        public ConnectThread(BluetoothDevice device) {
            // Use a temporary object that is later assigned to mmSocket,
            // because mmSocket is final
            BluetoothSocket tmp = null;
            //mmDevice = device;
            // Get a BluetoothSocket to connect with the given BluetoothDevice
            try {
                // MY_UUID is the app's UUID string, also used by the server code
                tmp = device.createRfcommSocketToServiceRecord(MY_UUID);
            } catch (IOException e) {

            }
            mmSocket = tmp;
        }

        public void run() {
            // Cancel discovery because it will slow down the connection
            btAdapter.cancelDiscovery();
            try {
                // Connect the device through the socket. This will block
                // until it succeeds or throws an exception
                mmSocket.connect();
            } catch (IOException connectException) {
                // Unable to connect; close the socket and get out
                try {
                    mmSocket.close();
                } catch (IOException closeException) { }
                return;
            }

            // Do work to manage the connection (in a separate thread)

            mHandler.obtainMessage(SUCCESS_CONNECT, mmSocket).sendToTarget();
        }



        /** Will cancel an in-progress connection, and close the socket */
        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) { }
        }
    }

    private class ConnectedThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;

        public ConnectedThread(BluetoothSocket socket) {
            mmSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            // Get the input and output streams, using temp objects because
            // member streams are final
            try {
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException e) { }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }

        public void run() {

           while (true) {
                //String s="1";
                try {

                    //s+=2;
                    while (mmSocket.getInputStream().available() > 0) {
                        //mHandler.obtainMessage(MESSAGE_READ, "Data has come".length(), -1, "Data has come".getBytes()).sendToTarget();
                        String s1="";

                        //s += "5";
                        //flag=true;
                        int tmp=0;
                        while(true) {
                            if(tmp==';'){
                                break;
                            }
                            if(mmSocket.getInputStream().available()>0) {
                                tmp = mmSocket.getInputStream().read();
                                s1+=String.valueOf((char)tmp);
                                //baos.write(tmp);
                            }
                            //s += "6";
                            Thread.sleep(1);
                        }
                        while (mmSocket.getInputStream().available() > 0) {
                            Thread.sleep(1);
                            mmSocket.getInputStream().read();
                        }
                        mHandler.obtainMessage(MESSAGE_READ, s1.length(), -1, s1.getBytes()).sendToTarget();
                    }
                } catch (Exception e) {
                    //e.printStackTrace();
                    //mHandler.obtainMessage(MESSAGE_READ, ("Exception: "+e.getMessage()+"   ::  "+s).length(), -1, ("Exception: "+e.getMessage()+"   ::  "+s).getBytes()).sendToTarget();
                    break;
                }
            }
        }



        /* Call this from the main activity to send data to the remote device */
        public void write(byte[] bytes) {
            try {
                mmOutStream.write(bytes);
            } catch (IOException e) { }
        }

        /* Call this from the main activity to shutdown the connection */
        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) { }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        btAdapter.cancelDiscovery();
        connect.cancel();
        try {
            mmSocket.close();
        }
        catch (Exception e){}
    }

    @Override
    protected void onResume() {
        super.onResume();
        init();
        connect = new ConnectThread(selectedDevice);
        connect.start();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        connect.cancel();
        try {
            mmSocket.close();
        }
        catch (Exception e){}
    }
}
