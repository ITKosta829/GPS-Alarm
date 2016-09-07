package com.launchathon.deanc.gps_alarm;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

/**
 * Created by DeanC on 8/26/2016.
 */
public class WelcomeSplash extends Activity {

    TextView start, title, message;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.welcome_splash);

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        start = (TextView) findViewById(R.id.start_journey);

        setFonts();

        start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(WelcomeSplash.this, MainActivity.class);
                WelcomeSplash.this.startActivity(intent);
                finish();
            }
        });

    }

    public void setFonts() {
        Typeface tf = Typeface.createFromAsset(getBaseContext().getAssets(), "BernModB.ttf");

        start.setTypeface(tf);
        start.setTextSize(40);

        title = (TextView) findViewById(R.id.title);
        title.setTypeface(tf);
        title.setTextSize(50);

        message = (TextView) findViewById(R.id.message);
        message.setTypeface(tf);
        message.setTextSize(30);




    }
}
