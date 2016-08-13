package com.example.deanc.gps_alarm;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import butterknife.Bind;

/**
 * Created by DeanC on 8/12/2016.
 */
public class DestinationAddress extends DialogFragment{

    @Bind(R.id.street_entry) EditText street;
    @Bind(R.id.city_entry) EditText city;
    @Bind(R.id.state_entry) EditText state;
    @Bind(R.id.zip_entry) EditText zip;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        LayoutInflater i = getActivity().getLayoutInflater();
        View v = i.inflate(R.layout.destination_address, null);

        AlertDialog.Builder b;
        b = new AlertDialog.Builder(getActivity());
        b.setView(v)

                .setPositiveButton("OK",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {

                                String strt, c, sta, z, full_address;

                                strt = street.getText().toString();
                                c = city.getText().toString();
                                sta = state.getText().toString();
                                z = zip.getText().toString();

                                full_address = strt + ",+" + c + ",+" + sta + ",+" + z;
                                MainActivity.user_address = full_address.replace(' ', '+');

                                dialog.dismiss();
                            }
                        }
                );

        return b.create();
    }
}
