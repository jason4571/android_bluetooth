package com.example.terry.desktop_raze;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Set;


public class DeviceListActivity extends Activity {
    private static final String TAG = "DeviceListActivity";
    TextView txt_loading;
    public static String EXTRA_DEVICE_ADDRESS = "device_address";
    private BluetoothAdapter mBtAdapter;
    private ArrayAdapter<String> mPairedDevicesArrayAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.device_list);
    }
    
    @Override
    public void onResume() 
    {
    	super.onResume();
    	checkBTState();
        txt_loading = (TextView) findViewById(R.id.connecting);
        txt_loading.setTextSize(60);
        txt_loading.setText(" ");

    	mPairedDevicesArrayAdapter = new ArrayAdapter<String>(this, R.layout.device_name);

    	ListView pairedListView = (ListView) findViewById(R.id.paired_devices);
    	pairedListView.setAdapter(mPairedDevicesArrayAdapter);
    	pairedListView.setOnItemClickListener(mDeviceClickListener);
    	mBtAdapter = BluetoothAdapter.getDefaultAdapter();
    	Set<BluetoothDevice> pairedDevices = mBtAdapter.getBondedDevices();

    	if (pairedDevices.size() > 0) {
    		findViewById(R.id.title_paired_devices).setVisibility(View.VISIBLE);
    		for (BluetoothDevice device : pairedDevices) {
                mPairedDevicesArrayAdapter.add(device.getName() + "\n" + device.getAddress());
    		}
    	}
        else {
    		String noDevices = "沒有配對裝置";
    		mPairedDevicesArrayAdapter.add(noDevices);
    	}
  }
    private OnItemClickListener mDeviceClickListener = new OnItemClickListener() {
        public void onItemClick(AdapterView<?> av, View v, int arg2, long arg3) {
            txt_loading.setText("讀取中...");
            String info = ((TextView) v).getText().toString();
            String address = info.substring(info.length() - 17);

			Intent i = new Intent(DeviceListActivity.this, MainActivity.class);
            i.putExtra(EXTRA_DEVICE_ADDRESS, address);
			startActivity(i);   
        }
    };

    private void checkBTState() {
    	 mBtAdapter= BluetoothAdapter.getDefaultAdapter();
        if(mBtAdapter==null) { 
        	Toast.makeText(getBaseContext(), "沒有藍芽設備", Toast.LENGTH_SHORT).show();
        }
        else {
          if (mBtAdapter.isEnabled()) {
            Log.d(TAG, "藍芽開啟");
          }
          else {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, 1);
            }
          }
        }
}