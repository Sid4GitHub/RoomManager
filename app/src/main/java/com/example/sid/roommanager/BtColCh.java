package com.example.sid.roommanager;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.larswerkman.holocolorpicker.ColorPicker;
import com.larswerkman.holocolorpicker.SVBar;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Calendar;
import java.util.UUID;

public class BtColCh extends AppCompatActivity {

    BluetoothAdapter btAdapter;
    public static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    protected static final int SUCCESS_CONNECT = 0;
    protected static final int MESSAGE_READ = 1;
    protected static final int CODE_SEND = 2;
    BluetoothDevice selectedDevice ;
    BluetoothSocket mmSocket;
    ConnectThread connect;
    ConnectedThread connectedThread;

    Button b1;
    EditText evr,evg,evb;


    ColorPicker picker;
    SVBar svBar;
    int colPrev,colNext;
    String colStr;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bt_col_ch);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);

        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        picker = (com.larswerkman.holocolorpicker.ColorPicker) findViewById(R.id.picker);
        picker.setShowOldCenterColor(false);
        svBar = (SVBar) findViewById(R.id.svbar);
        picker.addSVBar(svBar);

        b1=(Button) findViewById(R.id.bt_led_manu);
        b1.setTransformationMethod(null);

        evr=(EditText)findViewById(R.id.ev_r);
        evg=(EditText)findViewById(R.id.ev_g);
        evb=(EditText)findViewById(R.id.ev_b);

        evr.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                InputMethodManager imm =  (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
            }
        });


        SharedPreferences prefs = getSharedPreferences("RGB code", MODE_PRIVATE);
        try {
            int rgb = prefs.getInt("rgb", 0);
            picker.setColor(rgb);
            evr.setText(String.valueOf(Color.red(rgb)));
            evg.setText(String.valueOf(Color.green(rgb)));
            evb.setText(String.valueOf(Color.blue(rgb)));
        }
        catch (Exception e){}

        //picker.setOldCenterColor(Color.rgb(prefs.getInt("r",0),prefs.getInt("g",0),prefs.getInt("b",0)));
        //picker.setOldCenterColor(Color.rgb(255,0,0));
        //Toast.makeText(getBaseContext(),"Prev rgb: "+String.valueOf(prefs.getInt("rgb",0)),Toast.LENGTH_SHORT).show();
        picker.setShowOldCenterColor(false);


        picker.setOnColorChangedListener(new ColorPicker.OnColorChangedListener() {
            @Override
            public void onColorChanged(int color) {
                colNext=color;
                SharedPreferences.Editor editor = getSharedPreferences("RGB code", MODE_PRIVATE).edit();
                editor.putInt("rgb", color);
                editor.putInt("r",Color.red(color));
                editor.putInt("g",Color.green(color));
                editor.putInt("b",Color.blue(color));
                editor.commit();
                //Toast.makeText(getBaseContext()," rgb: "+String.valueOf(colNext),Toast.LENGTH_SHORT).show();
            }
        });

        b1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                int red=0;
                int gree=0;
                int blue=0;

                try {
                    red = Integer.parseInt(evr.getText().toString());
                }
                catch (NumberFormatException e){}
                catch (Exception e){}
                try {
                    gree = Integer.parseInt(evg.getText().toString());
                }
                catch (NumberFormatException e){}
                catch (Exception e){}
                try {
                    blue = Integer.parseInt(evb.getText().toString());
                }
                catch (NumberFormatException e){}
                catch (Exception e){}

                colStr= ("!m1:" + red + "/" + gree + "/" + blue + ";");
                connectedThread.write(colStr.getBytes());

                picker.setColor(Color.rgb(red,gree,blue));

            }
        });


        new Thread(){
            @Override
            public void run() {
                super.run();
                while(true) {
                    if (colPrev != colNext) {
                        //String rgbString = "R: " + Color.red(col) + " B: " + Color.blue(col) + " G: " + Color.green(col);
                        //mHandler.obtainMessage(MESSAGE_READ, rgbString.length(), -1, rgbString.getBytes()).sendToTarget();
                        int col=colNext;

                        int red=Color.red(col);
                        int gree=Color.green(col);
                        int blue=Color.blue(col);

                        colStr= ("!m1:" + red + "/" + gree + "/" + blue + ";");
                        connectedThread.write(colStr.getBytes());
                        colPrev=col;

                        String r=String.valueOf(red);
                        String g=String.valueOf(gree);
                        String b=String.valueOf(blue);

                        mHandler.obtainMessage(5, r.length(), -1, r.getBytes()).sendToTarget();
                        mHandler.obtainMessage(6, g.length(), -1, g.getBytes()).sendToTarget();
                        mHandler.obtainMessage(7, b.length(), -1, b.getBytes()).sendToTarget();

                        try {
                            this.sleep(250);
                        }
                        catch (Exception e){
                            //mHandler.obtainMessage(MESSAGE_READ, "Exception".length(), -1, "Exception".getBytes()).sendToTarget();
                        }
                    }
                }
            }
        }.start();
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

                    connectedThread = new ConnectedThread(mmSocket);
                    connectedThread.start();

                    Toast.makeText(getApplicationContext(), "CONNECT", Toast.LENGTH_SHORT).show();
                    getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);

                    break;
                case MESSAGE_READ:
                    byte[] readBuf = (byte[])msg.obj;
                    String string = new String(readBuf);

                    if(colStr.compareToIgnoreCase(string)==0){
                        Toast.makeText(getApplicationContext(), "Color Setting was Successful", Toast.LENGTH_SHORT).show();
                    }
                    else{
                        Toast.makeText(getApplicationContext(), "Color Setting was Unsuccessful", Toast.LENGTH_SHORT).show();
                    }

                    break;
                case 5:
                    byte[] readBufr = (byte[])msg.obj;
                    String stringr = new String(readBufr);
                    evr.setText(stringr);
                    break;
                case 6:
                    byte[] readBufg = (byte[])msg.obj;
                    String stringg = new String(readBufg);
                    evg.setText(stringg);
                    break;
                case 7:
                    byte[] readBufb = (byte[])msg.obj;
                    String stringb = new String(readBufb);
                    evb.setText(stringb);
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

