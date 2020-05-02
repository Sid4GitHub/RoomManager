package com.example.sid.roommanager;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Calendar;
import java.util.UUID;

public class BtIns extends AppCompatActivity {

    Button b1,b2,b3,b4,b5,b6,b7,b8;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bt_ins);


        b1=(Button)findViewById(R.id.bt_basic);
        b2=(Button)findViewById(R.id.bt_time_sync);
        b3=(Button)findViewById(R.id.bt_cam);
        b4=(Button)findViewById(R.id.bt_al);
        b5=(Button)findViewById(R.id.bt_col_ch);
        b6=(Button)findViewById(R.id.bt_new_scan);
        b7=(Button)findViewById(R.id.bt_ran_col);
        b8=(Button)findViewById(R.id.bt_voi);

        b1.setTransformationMethod(null);
        b2.setTransformationMethod(null);
        b3.setTransformationMethod(null);
        b4.setTransformationMethod(null);
        b5.setTransformationMethod(null);
        b6.setTransformationMethod(null);
        b7.setTransformationMethod(null);
        b8.setTransformationMethod(null);


        final String macId=getIntent().getExtras().getString("MAC");
        //Toast.makeText(getBaseContext(),macId,Toast.LENGTH_SHORT).show();


         b1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i=new Intent(getBaseContext(),BtBasicActivity.class);
                i.putExtra("MAC",macId);
                startActivity(i);
            }
        });

        b2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i=new Intent(getBaseContext(),BtTimeSync.class);
                i.putExtra("MAC",macId);
                startActivity(i);
            }
        });
       b3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i=new Intent(getBaseContext(),BtCam.class);
                i.putExtra("MAC",macId);
                startActivity(i);
            }
        });
       b4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i=new Intent(getBaseContext(),BtAlarm.class);
                i.putExtra("MAC",macId);
                startActivity(i);
            }
        });

        b5.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i=new Intent(getBaseContext(),BtColCh.class);
                i.putExtra("MAC",macId);
                startActivity(i);
            }
        });

        b6.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SharedPreferences.Editor editor = getSharedPreferences("Selected bt", MODE_PRIVATE).edit();
                editor.putString("mac","");
                editor.commit();

                Intent i = new Intent(getBaseContext(), BtScan.class);
                i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(i);

            }
        });

        b7.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i=new Intent(getBaseContext(),BtRanCol.class);
                i.putExtra("MAC",macId);
                startActivity(i);
            }
        });
        b8.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i=new Intent(getBaseContext(),BtVoice.class);
                i.putExtra("MAC",macId);
                startActivity(i);
            }
        });
    }
}
