package com.example.sid.roommanager;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

public class BtBasicActivity extends AppCompatActivity {
    BluetoothAdapter btAdapter;
    public static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    protected static final int SUCCESS_CONNECT = 0;
    protected static final int MESSAGE_READ = 1;
    BluetoothDevice selectedDevice ;
    BluetoothSocket mmSocket;
    ConnectThread connect;
    ListView lv;
    ArrayAdapter<String> listAdapter;

    String key[];
    String cmd[];


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);

        setContentView(R.layout.activity_bt_basic);
        String macId=getIntent().getExtras().getString("MAC");
        lv=(ListView) findViewById(R.id.listview_cmd);

        BluetoothManager bluetoothManager =
                (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        btAdapter = bluetoothManager.getAdapter();
        selectedDevice=btAdapter.getRemoteDevice(macId);

        key=new String[21];
        cmd=new String[21];
        listViewCMD();

        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                try {
                    ConnectedThread connectedThread = new ConnectedThread(mmSocket);
                    //connectedThread.write(new String("@#"+cmd[i]+"#@").getBytes());
                    connectedThread.write(new String(cmd[i]+";").getBytes());
                }
                catch (Exception e){
                    Toast.makeText(getBaseContext(),"Exc: "+e.getMessage(),Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    void listViewCMD(){

        key[0]="power";
        cmd[0]="1FE48B7";

        key[1]="mode";
        cmd[1]="1FE58A7";

        key[2]="mute";
        cmd[2]="1FE7887";

        key[3]="play";
        cmd[3]="1FE807F";

        key[4]="pre";
        cmd[4]="1FE40BF";

        key[5]="next";
        cmd[5]="1FEC03F";

        key[6]="eq";
        cmd[6]="1FE20DF";

        key[7]="vol+";
        cmd[7]="1FE609F";

        key[8]="vol-";
        cmd[8]="1FEA05F";

        key[9]="0";
        cmd[9]="1FEE01F";

        key[10]="rpt";
        cmd[10]="1FE10EF";

        key[11]="sd";
        cmd[11]="1FE906F";

        key[12]="1";
        cmd[12]="1FE50AF";

        key[13]="2";
        cmd[13]="1FED827";

        key[14]="3";
        cmd[14]="1FEF807";

        key[15]="4";
        cmd[15]="1FE30CF";

        key[16]="5";
        cmd[16]="1FEB04F";

        key[17]="6";
        cmd[17]="1FE708F";

        key[18]="7";
        cmd[18]="1FE00FF";

        key[19]="8";
        cmd[19]="1FEF00F";

        key[20]="9";
        cmd[20]="1FE9867";

        listAdapter=new ArrayAdapter<String>(getBaseContext(),android.R.layout.simple_list_item_1,0);
        for(int i=0;i<21;i++){
            listAdapter.add(key[i]+" :  "+cmd[i]);
        }
        lv.setAdapter(listAdapter);
    }

    Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {

            super.handleMessage(msg);
            switch(msg.what){
                case SUCCESS_CONNECT:
                    // DO something
                    //ConnectedThread connectedThread = new ConnectedThread((BluetoothSocket)msg.obj);
                    Toast.makeText(getApplicationContext(), "CONNECT", Toast.LENGTH_SHORT).show();
                    getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                    //String s = "successfully connected";
                    //connectedThread.write(new String("@#"+s+"#@").getBytes());
                    //connectedThread.write(new String(s+";").getBytes());
                    //connectedThread.cancel();
                    break;
                case MESSAGE_READ:
                    byte[] readBuf = (byte[])msg.obj;
                    String string = new String(readBuf);
                    Toast.makeText(getApplicationContext(), string, Toast.LENGTH_SHORT).show();
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
                try {

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
                    //mHandler.obtainMessage(MESSAGE_READ, "Exception".length(), -1, "Exception".getBytes()).sendToTarget();
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
        connect.cancel();
        try {
            mmSocket.close();
        }
        catch (Exception e){}
    }

    @Override
    protected void onResume() {
        super.onResume();
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