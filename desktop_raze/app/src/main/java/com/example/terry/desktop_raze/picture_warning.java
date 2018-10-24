package com.example.terry.desktop_raze;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

public class picture_warning extends Activity {

    private static String getpm2d5;
    private byte[] inter_read = new byte[0];
    private Handler mHandlerTime = new Handler();
    TextView txtv_pm2d5;
    ImageView imageView;
    int count =0;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.picture);
        imageView = (ImageView) findViewById(R.id.imageView);
        txtv_pm2d5 = (TextView)findViewById(R.id.txtv_image_pm2d5);

        Intent intent = this.getIntent();
        getpm2d5 = intent.getStringExtra("pm2d5");
        txtv_pm2d5.setText("細懸浮微粒:" + getpm2d5);
        txtv_pm2d5.setTextColor(Color.parseColor("#FFFF00FF"));

        internal_read("inter_picture");
        Uri uri = Uri.parse(new String(inter_read));
        imageView.setImageURI(uri);
        mHandlerTime.postDelayed(timerRun, 500);
    }

    //讀internal_data
    private String internal_read(String inter_alert){
        String result = null;
        try {
            StringBuilder stringBuilder = new StringBuilder();
            FileInputStream fin = this.openFileInput(inter_alert);
            inter_read = new byte[fin.available()];
            while (fin.read(inter_read) != -1) {
                stringBuilder.append(new String(inter_read));
            }
            fin.close();
            result = stringBuilder.toString();
        }
        catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }

    private final Runnable timerRun = new Runnable() {
        public void run() {
            if(count==30){
                picture_warning.this.finish();
            }
            mHandlerTime.postDelayed(this, 500);
            count++;
        }
    };
}



