package com.example.sid.roommanager;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothSocket;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Handler;
import android.os.Message;
import android.speech.RecognizerIntent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Locale;
import java.util.UUID;

public class BtVoice extends AppCompatActivity implements SensorEventListener {

    //TextView txtOutput,tvFormat;
    Button b1;
    String key[];
    String cmd[];
    BluetoothAdapter btAdapter;
    public static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    protected static final int SUCCESS_CONNECT = 0;
    protected static final int MESSAGE_READ = 1;
    BluetoothDevice selectedDevice ;
    BluetoothSocket mmSocket;
    ConnectThread connect;
    ConnectedThread connectedThread;
    String cmdSent="";
    private SensorManager mSensorManager;
    private Sensor mProximity;
    boolean b;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bt_voice);

        b=false;

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);

        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mProximity = mSensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY);

        init();
        connect = new ConnectThread(selectedDevice);
        connect.start();

        key=new String[21];
        cmd=new String[21];
        listViewCMD();

        //txtOutput = (TextView) findViewById(R.id.textView4Speech);
        //tvFormat= (TextView) findViewById(R.id.tvFormat);

        b1 = (Button) findViewById(R.id.btSpeech);

        b1.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                startSpeechToText();
            }
        });
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
                    b=true;

                    break;
                case MESSAGE_READ:
                    byte[] readBuf = (byte[])msg.obj;
                    String string = new String(readBuf);

                    if(cmdSent.compareToIgnoreCase(string)==0){
                        Toast.makeText(getApplicationContext(), "Ation is done", Toast.LENGTH_SHORT).show();
                    }
                    else{
                        Toast.makeText(getApplicationContext(), "Action is faild", Toast.LENGTH_SHORT).show();
                    }
                    break;
            }
        }
    };

    void init(){
        BluetoothManager bluetoothManager =
                (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        btAdapter = bluetoothManager.getAdapter();
        selectedDevice=btAdapter.getRemoteDevice(getIntent().getExtras().getString("MAC"));
    }

    void listViewCMD() {
        key[0] = "power";
        cmd[0] = "1FE48B7";

        key[1] = "mode";
        cmd[1] = "1FE58A7";

        key[2] = "mute";
        cmd[2] = "1FE7887";

        key[3] = "play";
        cmd[3] = "1FE807F";

        key[4] = "pre";
        cmd[4] = "1FE40BF";

        key[5] = "next";
        cmd[5] = "1FEC03F";

        key[6] = "eq";
        cmd[6] = "1FE20DF";

        key[7] = "vol+";
        cmd[7] = "1FE609F";

        key[8] = "vol-";
        cmd[8] = "1FEA05F";

        key[9] = "0";
        cmd[9] = "1FEE01F";

        key[10] = "rpt";
        cmd[10] = "1FE10EF";

        key[11] = "sd/scn";
        cmd[11] = "1FE906F";

        key[12] = "1";
        cmd[12] = "1FE50AF";

        key[13] = "2";
        cmd[13] = "1FED827";

        key[14] = "3";
        cmd[14] = "1FEF807";

        key[15] = "4";
        cmd[15] = "1FE30CF";

        key[16] = "5";
        cmd[16] = "1FEB04F";

        key[17] = "6";
        cmd[17] = "1FE708F";

        key[18] = "7";
        cmd[18] = "1FE00FF";

        key[19] = "8";
        cmd[19] = "1FEF00F";

        key[20] = "9";
        cmd[20] = "1FE9867";
    }

    void action(String s){
        if(s.contains("power")){
            try {
                //connectedThread.write(new String("@#"+cmd[i]+"#@").getBytes());
                cmdSent=cmd[0]+";";
                connectedThread.write(cmdSent.getBytes());
            }
            catch (Exception e){
                Toast.makeText(getBaseContext(),"Exc: "+e.getMessage(),Toast.LENGTH_SHORT).show();
            }
        }
        else if(s.contains("change display mode")){
            try {
                //connectedThread.write(new String("@#"+cmd[i]+"#@").getBytes());
                cmdSent=cmd[1]+";";
                connectedThread.write(cmdSent.getBytes());
            }
            catch (Exception e){
                Toast.makeText(getBaseContext(),"Exc: "+e.getMessage(),Toast.LENGTH_SHORT).show();
            }
        }
        else if(s.contains("mute")){
            try {
                //connectedThread.write(new String("@#"+cmd[i]+"#@").getBytes());
                cmdSent=cmd[2]+";";
                connectedThread.write(cmdSent.getBytes());
            }
            catch (Exception e){
                Toast.makeText(getBaseContext(),"Exc: "+e.getMessage(),Toast.LENGTH_SHORT).show();
            }
        }
        else if(s.contains("LED switch")){
            try {
                //connectedThread.write(new String("@#"+cmd[i]+"#@").getBytes());
                cmdSent=cmd[3]+";";
                connectedThread.write(cmdSent.getBytes());
            }
            catch (Exception e){
                Toast.makeText(getBaseContext(),"Exc: "+e.getMessage(),Toast.LENGTH_SHORT).show();
            }
        }
        else if(s.contains("previous LED color")||s.contains("previous LED colour")){
            try {
                //connectedThread.write(new String("@#"+cmd[i]+"#@").getBytes());
                cmdSent=cmd[4]+";";
                connectedThread.write(cmdSent.getBytes());
            }
            catch (Exception e){
                Toast.makeText(getBaseContext(),"Exc: "+e.getMessage(),Toast.LENGTH_SHORT).show();
            }
        }
        else if(s.contains("next LED color")|| s.contains("next LED colour")){
            try {
                //connectedThread.write(new String("@#"+cmd[i]+"#@").getBytes());
                cmdSent=cmd[5]+";";
                connectedThread.write(cmdSent.getBytes());
            }
            catch (Exception e){
                Toast.makeText(getBaseContext(),"Exc: "+e.getMessage(),Toast.LENGTH_SHORT).show();
            }
        }
        else if(s.contains("flash LED")){
            try {
                //connectedThread.write(new String("@#"+cmd[i]+"#@").getBytes());
                cmdSent=cmd[6]+";";
                connectedThread.write(cmdSent.getBytes());
            }
            catch (Exception e){
                Toast.makeText(getBaseContext(),"Exc: "+e.getMessage(),Toast.LENGTH_SHORT).show();
            }
        }
        else if(s.contains("set LED flashing time ")){
            try {
                //connectedThread.write(new String("@#"+cmd[i]+"#@").getBytes());
                int c=100;
                for (int i=0;i<s.length();i++) {
                    if (s.charAt(i) == 's' || s.charAt(i) == 'e' || s.charAt(i) == 't' || s.charAt(i) == 'L' || s.charAt(i) == 'E' || s.charAt(i) == 'D'
                            ||s.charAt(i) == 'f' || s.charAt(i) == 'l' || s.charAt(i) == 'a' || s.charAt(i) == 's' || s.charAt(i) == 'h' || s.charAt(i) == 'i' || s.charAt(i) == 'n'
                            ||s.charAt(i) == 'g' || s.charAt(i) == 't' || s.charAt(i) == 'i' || s.charAt(i) == 'm' || s.charAt(i) == ' ' ) {
                        continue;
                    }
                    try {
                        c = Integer.parseInt(s.substring(i, s.length()));
                        //tvFormat.setText(String.valueOf(c));
                        //tvFormat.setText(s.substring(i, s.length()));
                        break;
                    }
                    catch (Exception e){}
                }
                cmdSent="!m4:"+c+";";
                connectedThread.write(cmdSent.getBytes());
            }
            catch (Exception e){
                Toast.makeText(getBaseContext(),"Exc: "+e.getMessage(),Toast.LENGTH_SHORT).show();
            }
        }
        else if(s.contains("switch")){
            for (int i=0;i<s.length();i++){
                if(s.charAt(i)=='s'||s.charAt(i)=='w'||s.charAt(i)=='i'||s.charAt(i)=='t'||s.charAt(i)=='c'||s.charAt(i)=='h'||s.charAt(i)==' '){
                    continue;
                }
                try {
                    int c=Integer.parseInt(s.substring(i,s.length()));
                    //tvFormat.setText(String.valueOf(c));
                    if(c==0){
                        c=9;
                    }
                    else{
                        c+=11;
                    }
                    try {
                        //connectedThread.write(new String("@#"+cmd[i]+"#@").getBytes());
                        cmdSent=cmd[c]+";";
                        connectedThread.write(cmdSent.getBytes());
                    }
                    catch (Exception e){
                        Toast.makeText(getBaseContext(),"Exc: "+e.getMessage(),Toast.LENGTH_SHORT).show();
                    }
                    break;
                }
                catch (Exception e){
                    Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                }

            }
        }
    }


    private void startSpeechToText() {
        if(b) {
            Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                    RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
            intent.putExtra(RecognizerIntent.EXTRA_PROMPT,
                    "Speak something...");
            try {
                startActivityForResult(intent, 1);
            } catch (ActivityNotFoundException a) {
                Toast.makeText(getApplicationContext(),
                        "Sorry! Speech recognition is not supported in this device.",
                        Toast.LENGTH_SHORT).show();
            }
        }
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case 1: {
                if (resultCode == RESULT_OK && null != data) {

                    ArrayList<String> result = data
                            .getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                    String text = result.get(0);
                    action(text);
                    //txtOutput.setText(text);
                }
                break;
            }

        }
    }



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
        mSensorManager.unregisterListener(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mSensorManager.registerListener(this, mProximity, SensorManager.SENSOR_DELAY_NORMAL);
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

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_PROXIMITY) {

            //tvFormat.setText(String.valueOf(event.values[0]));
            if (event.values[0] >= -0.01 && event.values[0]<= 0.01) {
                //near
                //Toast.makeText(getApplicationContext(), "near", Toast.LENGTH_SHORT).show();
                startSpeechToText();
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        //tvFormat.setText(String.valueOf(accuracy));
    }



}
