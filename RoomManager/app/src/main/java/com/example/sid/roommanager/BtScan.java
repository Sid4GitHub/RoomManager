package com.example.sid.roommanager;


import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Set;
import java.util.UUID;

public class BtScan extends AppCompatActivity {

    Button bt;
    ListView listView;
    BluetoothAdapter btAdapter;
    Set<BluetoothDevice> devicesArray;
    IntentFilter filter;
    BroadcastReceiver receiver;
    ArrayAdapter<String> listAdapter;
    ArrayList<String> pairedDevices;
    ArrayList<BluetoothDevice> devices;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_bluetooth_scan);

        BluetoothManager bluetoothManager =
                (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        btAdapter = bluetoothManager.getAdapter();

        if (!btAdapter.isEnabled()) {
            turnOnBT();
        }

        SharedPreferences prefs = getSharedPreferences("Selected bt", MODE_PRIVATE);
        String tmp=prefs.getString("mac","");
        if(!tmp.equals("")){
            Intent intent=new Intent(getBaseContext(),BtIns.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            intent.putExtra("MAC",tmp);
            startActivity(intent);
        }
        else {
            bt = (Button) findViewById(R.id.my_bT_scan);
            bt.setTransformationMethod(null);

            listView = (ListView) findViewById(R.id.my_listViewscan);

            init();

            registerReceiver(receiver, new IntentFilter(BluetoothDevice.ACTION_FOUND));
            registerReceiver(receiver, new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_STARTED));
            registerReceiver(receiver, new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED));
            registerReceiver(receiver, new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED));

            try {
                Thread.sleep(250);
            }
            catch (Exception e){}


            newScan();

            bt.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    newScan();
                }
            });


            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                    if (btAdapter.isDiscovering()) {
                        btAdapter.cancelDiscovery();
                    }

                    SharedPreferences.Editor editor = getSharedPreferences("Selected bt", MODE_PRIVATE).edit();
                    editor.putString("mac", devices.get(i).getAddress());
                    editor.commit();

                    if (!listAdapter.getItem(i).contains("Paired")) {
                        try {
                            BluetoothDevice selectedDevice = devices.get(i);
                            pairDevice(selectedDevice);
                            Thread.sleep(500);
                            newScan();
                        } catch (Exception e) {
                        }
                    } else {
                        BluetoothDevice selectedDevice = devices.get(i);
                        Intent intent = new Intent(getBaseContext(), BtIns.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        intent.putExtra("MAC", selectedDevice.getAddress());
                        startActivity(intent);
                    }
                }
            });
        }
    }

    private void newScan(){
        btAdapter.cancelDiscovery();
        Toast.makeText(getBaseContext(),"New Scan Start",Toast.LENGTH_SHORT ).show();

        listAdapter= new ArrayAdapter<String>(getBaseContext(), android.R.layout.simple_list_item_1,0);
        listView.setAdapter(listAdapter);

        devices = new ArrayList<BluetoothDevice>();
        btAdapter.startDiscovery();
    }
    private void getPairedDevices() {
        devicesArray = btAdapter.getBondedDevices();
        if(devicesArray.size()>0){
            for(BluetoothDevice device:devicesArray){
                pairedDevices.add(device.getName());

            }
        }
    }

    void turnOnBT(){
        Intent intent =new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        startActivity(intent);
    }

    void init(){

        receiver = new BroadcastReceiver(){
            @Override
            public void onReceive(Context context, Intent intent) {

                try {
                    String action = intent.getAction();

                    //Toast.makeText(getBaseContext(),"new br: "+action,Toast.LENGTH_LONG ).show();

                    if (BluetoothDevice.ACTION_FOUND.equals(action)) {

                        pairedDevices = new ArrayList<String>();
                        getPairedDevices();
                        BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

                        //Toast.makeText(getBaseContext(),"Dev: "+device.getName(),Toast.LENGTH_LONG ).show();
                        devices.add(device);
                        String s = "";
                        for (int a = 0; a < pairedDevices.size(); a++) {
                            if (device.getName().equals(pairedDevices.get(a))) {
                                //append
                                s = "(Paired)";
                                break;
                            }
                        }

                        listAdapter.add(device.getName() + " " + s + " " + "\n" + device.getAddress());

                    } else if (BluetoothAdapter.ACTION_STATE_CHANGED.equals(action)) {
                        if (btAdapter.getState() == btAdapter.STATE_OFF) {
                            turnOnBT();
                        }
                    }
                }
                catch (Exception e){
                    //newScan();
                }

            }
        };
    }

    private void pairDevice(BluetoothDevice device) {
        try {
            Method m = device.getClass().getMethod("createBond", (Class[]) null);
            m.invoke(device, (Object[]) null);

        } catch (Exception e) {
            Toast.makeText(getBaseContext(),"Exception: "+e.getMessage(),Toast.LENGTH_LONG ).show();
        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            btAdapter.cancelDiscovery();
            unregisterReceiver(receiver);
        }
        catch (Exception e){}
    }
/*
    @Override
    protected void onPause() {
        super.onPause();
        try {
            btAdapter.cancelDiscovery();
            unregisterReceiver(receiver);
        }
        catch (Exception e){}
    }


    @Override
    public void onResume() {
        super.onResume();
        try {

            init();

            registerReceiver(receiver, new IntentFilter(BluetoothDevice.ACTION_FOUND));
            registerReceiver(receiver, new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_STARTED));
            registerReceiver(receiver, new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED));
            registerReceiver(receiver, new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED));


            //Toast.makeText(getBaseContext(),"Registration",Toast.LENGTH_SHORT ).show();

        }
        catch (Exception e){}
    }*/
}





