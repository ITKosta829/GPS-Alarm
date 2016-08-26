package com.launchathon.deanc.gps_alarm;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

/**
 * Created by DeanC on 8/26/2016.
 */
public class WelcomeSplash extends Activity {

    TextView start;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.welcome_splash);

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
        Typeface tf = Typeface.createFromAsset(getBaseContext().getAssets(), "ANDYB.TTF");

        TextView tv1 = (TextView)findViewById(R.id.start_journey);
        tv1.setTypeface(tf);
        tv1.setTextSize(40);

        TextView tv2 = (TextView) findViewById(R.id.title);
        tv2.setTypeface(tf);
        tv2.setTextSize(40);


    }
}
