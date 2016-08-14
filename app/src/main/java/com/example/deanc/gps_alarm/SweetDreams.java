package com.example.deanc.gps_alarm;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

/**
 * Created by dcsir on 8/14/2016.
 */
public class SweetDreams extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sweet_dreams);

        setFonts();


    }

    public void done(View view) {

        Intent startMain = new Intent(Intent.ACTION_MAIN);
        startMain.addCategory(Intent.CATEGORY_HOME);
        startMain.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(startMain);

    }

    public void setFonts() {
        Typeface tf = Typeface.createFromAsset(getBaseContext().getAssets(), "ANDYB.TTF");

        TextView tv1 = (TextView) findViewById(R.id.alarm_is_set);
        tv1.setTypeface(tf);
        Button b1 = (Button) findViewById(R.id.done);
        b1.setTypeface(tf);

    }
}
