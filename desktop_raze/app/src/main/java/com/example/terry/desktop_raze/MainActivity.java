package com.example.terry.desktop_raze;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

public class MainActivity extends Activity {

    Button btnOn, btnOff, btn_stor, btn_dia, btn_temple;
    TextView txt_respm2d5, txt_pm2d5, txt_slogan;
    ImageView imageView;
    private static boolean bool_stor_check = false;
    private EditText etxt_dialog, etxt_dialog_temple,etxt_dialog_storage_text;
    public BluetoothAdapter btAdapter = null;
    public BluetoothSocket btSocket = null;
    public ConnectedThread mConnectedThread;
    public static final UUID BTMODULEUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    private static String address, storage_name;
    private String[] conv_str = new String[18];
    private int[] int_res = new int[9];
    private int[] int_res_fin = new int[9];
    private static byte[] buffer = new byte[1024];
    private byte[] inter_read = new byte[0];
    private byte[] inter_temple = new byte[0];
    private byte[] inter_alert_type = new byte[0];
    private byte[] inter_alert_text = new byte[0];
    private Handler mHandlerTime = new Handler();
    private static int count = 235, count_stop, alert_num;
    public static boolean bt_check = false;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);                      //10吋平板用格式activity_main，5吋用activity_main_5inch

        btnOn = (Button) findViewById(R.id.buttonOn);
        btnOff = (Button) findViewById(R.id.buttonOff);
        btn_stor = (Button) findViewById(R.id.btn_stor);
        btn_dia = (Button) findViewById(R.id.btn_dia);
        btn_temple = (Button) findViewById(R.id.btn_temple);
        txt_respm2d5 = (TextView) findViewById(R.id.txtv_respm2d5);
        txt_pm2d5 = (TextView) findViewById(R.id.txtvideo_pm2d5);
        txt_slogan = (TextView) findViewById(R.id.txtv_slogan);
        imageView = (ImageView) findViewById(R.id.imageView);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON, WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);  //保持螢幕不待機

        btAdapter = BluetoothAdapter.getDefaultAdapter();
        checkBTState();
        btnOff.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {

                mHandlerTime.removeCallbacks(timerRun);
                txt_respm2d5.setText("0");
                internal_read("inter_temple");
                txt_slogan.setText(new String(inter_temple) + "關心您");
                txt_respm2d5.setTextColor(Color.parseColor("#FFFFFF"));
                txt_slogan.setTextColor(Color.parseColor("#FFFFFF"));
                Toast.makeText(getBaseContext(), "停止掃描", Toast.LENGTH_SHORT).show();
            }
        });

        btnOn.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {

                mHandlerTime.postDelayed(timerRun, 2000);                                                 //timer
                Toast.makeText(getBaseContext(), "開始掃描", Toast.LENGTH_SHORT).show();

            }
        });

        etxt_dialog = new EditText(this);                   //警告範圍設定
        etxt_dialog_storage_text = new EditText(this);      //txt檔的名稱

        final AlertDialog set_storage_name = insert_storage_name();

        btn_stor.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if(bool_stor_check == false){
                    set_storage_name.show();
                }
                else{
                    bool_stor_check = false;
                    Toast.makeText(MainActivity.this,"已停止儲存資料",Toast.LENGTH_LONG).show();
                }
            }
        });



        final AlertDialog alertDialog = getAlertDialog();
        btn_dia.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                alertDialog.show();
            }
        });

        internal_read("inter_read");
        if (inter_read.length != 0) {
            etxt_dialog.setText(new String(inter_read));
        }

        etxt_dialog_temple = new EditText(this);                //廟宇名稱設定
        final AlertDialog alertDialog_stop = temple_set();
        btn_temple.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {alertDialog_stop.show();}
        });

        internal_read("inter_temple");                                              //Initial廟方名稱
        if (inter_temple.length != 0) {
            etxt_dialog_temple.setText(new String(inter_temple));
        }
        txt_slogan.setText(new String(inter_temple) + "關心您");
        txt_slogan.setTextColor(Color.parseColor("#FF00FF00"));
    }

    private BluetoothSocket createBluetoothSocket(BluetoothDevice device)
            throws
            IOException {

        return device.createRfcommSocketToServiceRecord(BTMODULEUUID);
    }

    @Override
    public void onResume() {
        super.onResume();

        if(bt_check == false) {
            checkBTState();                                              //確認是否有藍芽開啟
            Intent intent = getIntent();
            address = intent.getStringExtra(DeviceListActivity.EXTRA_DEVICE_ADDRESS);              //get data from device activity
            BluetoothDevice device = btAdapter.getRemoteDevice(address);

            try {
                btSocket = createBluetoothSocket(device);              //建立連線
            } catch (IOException e) {

            }
            try {
                if (btSocket != null) {
                    btSocket.connect();
                } else {
                    btSocket = createBluetoothSocket(device);
                }
            } catch (IOException e) {

                try {
                    btSocket.close();
                } catch (IOException e2) {

                }
            }
            mConnectedThread = new ConnectedThread(btSocket);
            mConnectedThread.start();
            bt_check = true;
        }
    }

    @Override
    public void onPause() {
        super.onPause();

        /*try {
            btSocket.close();
        } catch (IOException e2) {

        }*/
    }

    @Override
    public void onDestroy() {
        mHandlerTime.removeCallbacks(timerRun);
        super.onDestroy();
    }

    private void checkBTState() {

        if (btAdapter == null) {
            Toast.makeText(getBaseContext(), "您的手機沒有藍芽", Toast.LENGTH_LONG).show();
        } else {
            if (btAdapter.isEnabled()) {

            } else {
                btAdapter.enable();
            }
        }
    }

    @Override
    public void onStart() {                   //google建立
        super.onStart();
    }

    @Override
    public void onStop() {
        super.onStop();
    }


    private class ConnectedThread extends Thread {                               //connect thread of bluetooth
        private final InputStream mmInStream;

        public ConnectedThread(BluetoothSocket socket) {
            InputStream tmpIn = null;
            try {
                tmpIn = socket.getInputStream();
            } catch (IOException e) {}
            mmInStream = tmpIn;
        }

        public void run() {
            int bytes;
            while (true) {                               //arduino段的讀取格式
                try {
                    bytes = mmInStream.read(buffer);
                    for (int i=bytes-1; i>19; i--) {
                        if (buffer[i] == 10 && buffer[i-19] == 58) {
                            for (int i2 = 0; i2 < 18; i2++) {
                                conv_str[i2] = String.format("%8s", Integer.toBinaryString(buffer[i + i2 -18] & 0xFF)).replace(' ', '0');
                            }
                            //break;
                            i=18;
                        }
                    }
                    for (int i=0; i < 9; i++) {
                        if (conv_str[i * 2] != conv_str[i * 2 + 1]) {
                            int_res[i] = Integer.parseInt((conv_str[i * 2] + conv_str[i * 2 + 1]), 2);
                        }
                    }
                    for (int i=0; i < 9; i++) {                               //避免出現outlier
                        if (Math.abs(int_res_fin[i] - int_res[i]) <= 2500) {
                            int_res_fin[i] = int_res[i];
                            count_stop = 0;
                        }
                    }
                    Thread.sleep(2000);
                } catch (Exception e) {}
                buffer = new byte[1024];
            }
        }
    }

    private final Runnable timerRun = new Runnable() {
        public void run() {             //定時讀取的timer

            internal_read("inter_alert_type");
            Intent intent = new Intent(MainActivity.this, movie_warning.class);
            intent.putExtra("pm2d5", String.valueOf(int_res_fin[1]));

            //txt_respm2d5.setTextSize(150);         //5吋手機用格式

            if (int_res_fin[1] < 100) {                                  //10吋平板用格式
                txt_respm2d5.setTextSize(620);
            } else if (int_res_fin[1] < 1000) {
                txt_respm2d5.setTextSize(440);
            } else if (int_res_fin[1] >= 1000) {
                txt_respm2d5.setTextSize(320);
            }

            txt_slogan.setTextColor(Color.parseColor("#FF00FF00"));
            if (int_res_fin[1] < 36) {
                try {
                    internal_read("inter_temple");
                    txt_slogan.setText(new String(inter_temple) + "關心您");
                }
                catch (Exception e){}
                    txt_respm2d5.setTextColor(Color.parseColor("#FF00FF00"));

            }
            if (int_res_fin[1] >= 36 && int_res_fin[1] <= 53) {
                txt_respm2d5.setTextColor(Color.parseColor("#FFFFFF00"));
                txt_slogan.setText("空氣品質普通");
            }
            if (int_res_fin[1] >= 54 && int_res_fin[1] <= 70) {
                txt_respm2d5.setTextColor(Color.parseColor("#FFFF0000"));
                txt_slogan.setText("請注意空氣品質");
            }
            if (int_res_fin[1] >= 71) {
                txt_respm2d5.setTextColor(Color.parseColor("#FFFF00FF"));
                txt_slogan.setText("空氣品質危險");
            }
            int type = 0;

            try {
                internal_read("inter_alert_type");                              //android internal storage read
                type = Integer.valueOf(new String(inter_alert_type));
            }
            catch (Exception e){

            }
            if (type == 0) {                                               //選項為影片的情況
                intent = new Intent(MainActivity.this, movie_warning.class);
                intent.putExtra("pm2d5", String.valueOf(int_res_fin[1]));
            } else if (type == 1) {                                        //選項為圖片的情況，有小bug不影響使用
                intent = new Intent(MainActivity.this, picture_warning.class);
                intent.putExtra("pm2d5", String.valueOf(int_res_fin[1]));
            } else {}

            if (type == 0 || type == 1) {                                 //警告使用的計數器
                try {
                    internal_read("inter_read");
                }
                catch (Exception e){
                }

                if (inter_read.length != 0) {
                    if (int_res_fin[1] >= Integer.valueOf(new String(inter_read))) {
                        if (count % 241 == 240) {
                            startActivity(intent);
                            count = 0;
                        } else {

                        }
                        count++;
                    }
                } else {
                    if (int_res_fin[1] >= 71) {
                        if (count % 241 == 240) {
                            startActivity(intent);
                        }
                        count++;
                    }
                }
            }
            /*else if(type==2) {
                if (int_res_fin[1] >= 71) {
                    if (count % 120 == 0) {
                        txt_slogan.setText("");
                    }
                    count++;
                }
                else {
                    if (int_res_fin[1] >= 71) {
                        if (count % 120 == 0) {
                            internal_read("inter_alert_text");
                            txt_slogan.setText(""+new String(inter_alert_text));
                        }
                        count++;
                    }
                }
            }*/
            else {

            }

            txt_respm2d5.setText("" + int_res_fin[1]);
            count_stop++;

            mHandlerTime.postDelayed(this, 2000);

            if (count_stop % 6 == 5) {          //斷線重連
                try {
                    bt_check = false;
                    count_stop = 0;
                    mHandlerTime.removeCallbacks(timerRun);
                    btSocket.close();
                    mConnectedThread.interrupt();
                    onResume();
                    mHandlerTime.postDelayed(this, 2000);
                } catch (Exception e) {

                }
            }

            if (bool_stor_check == true) {
                Date date = new Date();
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                String date_time = sdf.format(date);

                try {
                    File file = new File(getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), storage_name + ".txt");
                    OutputStream fileOutputStream = new FileOutputStream(file, true);
                    fileOutputStream.write((date_time + "," + int_res_fin[0] + "," + int_res_fin[1] + "," + int_res_fin[2] + "\n").getBytes());
                    fileOutputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    };

    final private AlertDialog getAlertDialog() {        //設定警報方式
        Builder builder = new Builder(MainActivity.this);
        builder.setTitle("設定警報方式");
        String[] str_alert = {"影片", "圖片", "預設文字"};
        builder.setSingleChoiceItems(str_alert, 0, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int alert_choice) {
                // TODO Auto-generated method stub
                alert_num = alert_choice;
            }
        });
        builder.setView(etxt_dialog);

        builder.setPositiveButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Toast.makeText(MainActivity.this, "取消設定", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("確定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (alert_num == 0) {
                    Toast.makeText(MainActivity.this, "已更改警報方式為影片", Toast.LENGTH_SHORT).show();
                    internal_write("inter_alert_type", "0");
                    internal_write("inter_read", etxt_dialog.getText().toString());
                    Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                    //intent.setType("audio/*");
                    //startActivityForResult ( intent , 1) ;

                }
                if (alert_num == 1) {
                    Intent intent = new Intent(Intent.ACTION_PICK);
                    intent.setType("image/*");
                    Intent destIntent = Intent.createChooser(intent, "選擇圖片");
                    startActivityForResult(destIntent, 0);
                    Toast.makeText(MainActivity.this, "請選擇圖片", Toast.LENGTH_SHORT).show();
                    internal_write("inter_alert_type", "1");
                    internal_write("inter_read", etxt_dialog.getText().toString());
                }
                if (alert_num == 2) {
                    Toast.makeText(MainActivity.this, "已更改警報方式為預設文字", Toast.LENGTH_SHORT).show();
                    internal_write("inter_alert_type", "3");
                    internal_write("inter_read", etxt_dialog.getText().toString());
                }
            }
        });
        return builder.create();
    }

    final private AlertDialog temple_set() {
        Builder builder = new Builder(MainActivity.this);
        builder.setTitle("請輸入名稱");
        builder.setView(etxt_dialog_temple);

        builder.setPositiveButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Toast.makeText(MainActivity.this, "取消設定", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("確定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                internal_write("inter_temple", etxt_dialog_temple.getText().toString());
                Toast.makeText(MainActivity.this, "已更改名稱", Toast.LENGTH_SHORT).show();
            }
        });
        return builder.create();
    }

    final private AlertDialog insert_storage_name() {
        Builder builder = new Builder(MainActivity.this);
        builder.setTitle("儲存資料");
        builder.setView(etxt_dialog_storage_text);
        etxt_dialog_storage_text.setHint("請輸入想儲存的檔案名稱");

        builder.setPositiveButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Toast.makeText(MainActivity.this, "取消儲存資料", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("確定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if(etxt_dialog_storage_text.getText().toString().length()==0){
                    Toast.makeText(MainActivity.this,"請輸入檔案名稱，否則無法儲存資料",Toast.LENGTH_LONG).show();
                }
                else {
                    storage_name = etxt_dialog_storage_text.getText().toString();
                    bool_stor_check = true;
                    Toast.makeText(MainActivity.this, "已開始儲存資料", Toast.LENGTH_SHORT).show();
                }
            }
        });
        return builder.create();
    }

    //寫入data到internal
    private void internal_write(String inter_alert, String data) {
        try {
            FileOutputStream fout = this.openFileOutput(inter_alert, Context.MODE_PRIVATE);
            fout.write(data.getBytes());
            fout.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //讀internal_data
    private String internal_read(String inter_alert) {
        String result = null;
        try {
            StringBuilder stringBuilder = new StringBuilder();
            FileInputStream fin = this.openFileInput(inter_alert);
            if (inter_alert == "inter_read") {
                inter_read = new byte[fin.available()];
                while (fin.read(inter_read) != -1) {
                    stringBuilder.append(new String(inter_read));
                }
                fin.close();
                result = stringBuilder.toString();
            } else if (inter_alert == "inter_temple") {
                inter_temple = new byte[fin.available()];
                while (fin.read(inter_temple) != -1) {
                    stringBuilder.append(new String(inter_temple));
                }
                fin.close();
                result = stringBuilder.toString();
            } else if (inter_alert == "inter_alert_type") {
                inter_alert_type = new byte[fin.available()];
                while (fin.read(inter_alert_type) != -1) {
                    stringBuilder.append(new String(inter_alert_type));
                }
                fin.close();
                result = stringBuilder.toString();
            } else if (inter_alert == "inter_alert_text") {
                inter_alert_text = new byte[fin.available()];
                while (fin.read(inter_alert_text) != -1) {
                    stringBuilder.append(new String(inter_alert_text));
                }
                fin.close();
                result = stringBuilder.toString();
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {          //抓取相簿的影片和圖片data

        // TODO Auto-generated method stub
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1) {
            if (resultCode == RESULT_OK) {
                Uri uri = data.getData();
                if (uri != null) {
                    internal_write("inter_video", uri.toString());
                } else {
                    Toast.makeText(MainActivity.this, "無效的檔案路徑", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(MainActivity.this, "取消影片設定", Toast.LENGTH_SHORT).show();
            }
        } else {
            if (resultCode == RESULT_OK) {
                Uri uri = data.getData();
                if (uri != null) {
                    internal_write("inter_picture", uri.toString());
                } else {
                    Toast.makeText(MainActivity.this, "無效的檔案路徑", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(MainActivity.this, "取消圖片設定", Toast.LENGTH_SHORT).show();
            }
        }
    }
}

