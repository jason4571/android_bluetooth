package com.example.terry.desktop_raze;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.widget.MediaController;
import android.widget.TextView;
import android.widget.VideoView;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

public class movie_warning extends Activity {

    private static String getpm2d5;
    private byte[] inter_read = new byte[0];
    TextView txtv_pm2d5;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.movie);

        txtv_pm2d5 = (TextView) findViewById(R.id.txtv_video_pm2d5);

        Intent intent = this.getIntent();
        getpm2d5 = intent.getStringExtra("pm2d5");
        txtv_pm2d5.setText("細懸浮微粒:" + getpm2d5);
        txtv_pm2d5.setTextColor(Color.parseColor("#FFFF00FF"));
        VideoView videoView = (VideoView) this.findViewById(R.id.videoView);
        MediaController movie = new MediaController(this);
        videoView.setMediaController(movie);

        internal_read("inter_video");
        Uri uri = Uri.parse(new String(inter_read));
        //videoView.setVideoURI(uri);
        videoView.setVideoURI(Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.warning));
        videoView.setVisibility(videoView.VISIBLE);
        videoView.start();

        videoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                movie_warning.this.finish();
            }
        });
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
}



