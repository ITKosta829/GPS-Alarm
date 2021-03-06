package com.launchathon.deanc.gps_alarm;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.text.InputFilter;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

/**
 * Created by DeanC on 8/12/2016.
 */
public class DestinationAddress extends DialogFragment {

    DataHandler DH;

    EditText street, city, state, zip;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        LayoutInflater i = getActivity().getLayoutInflater();
        View v = i.inflate(R.layout.destination_address, null);

        DH = DataHandler.getInstance();

        street = (EditText) v.findViewById(R.id.street_entry);
        city = (EditText) v.findViewById(R.id.city_entry);
        state = (EditText) v.findViewById(R.id.state_entry);
        zip = (EditText) v.findViewById(R.id.zip_entry);

        street.requestFocus();
        state.setFilters(new InputFilter[]{new InputFilter.AllCaps()});
        zip.setInputType(InputType.TYPE_CLASS_NUMBER);

        AlertDialog.Builder b;
        b = new AlertDialog.Builder(getActivity());
        b.setView(v)

                .setPositiveButton("OK", new DialogInterface.OnClickListener() {

                            public void onClick(DialogInterface dialog, int whichButton) {

                                String strt = street.getText().toString();
                                String c = city.getText().toString();
                                String sta = state.getText().toString();
                                String z = zip.getText().toString();

                                if (strt.equals("") && c.equals("") && sta.equals("") && z.equals("")){

                                    dialog.dismiss();

                                } else {

                                    String full_address = strt + ",+" + c + ",+" + sta + ",+" + z;
                                    DH.user_address = full_address.replace(' ', '+');

                                    DH.alarmDistance = 500;

                                    DH.startAsyncTask();

                                    MainActivity.start_tracking.setVisibility(View.VISIBLE);
                                    MainActivity.start_tracking.setBackground(ContextCompat.getDrawable(DH.mContext, R.drawable.green_rounded_button));

                                    dialog.dismiss();
                                }
                            }
                        }
                );

        return b.create();
    }

}
