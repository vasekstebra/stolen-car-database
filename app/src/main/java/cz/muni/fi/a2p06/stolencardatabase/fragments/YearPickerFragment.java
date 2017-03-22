package cz.muni.fi.a2p06.stolencardatabase.fragments;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.NumberPicker;

import java.util.Calendar;

import cz.muni.fi.a2p06.stolencardatabase.R;

/**
 * Created by robert on 20.3.2017.
 */

public class YearPickerFragment extends DialogFragment {

    interface OnYearSetListener {
        void onYearSet(int number);
    }

    private OnYearSetListener mListener;

    public void setOnYearSetListener(OnYearSetListener listener) {
        this.mListener = listener;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        View layout = getActivity().getLayoutInflater().inflate(R.layout.fragment_year_picker, null);
        final NumberPicker picker = (NumberPicker) layout.findViewById(R.id.number_picker);
        picker.setMinValue(0); // TODO: Set min value
        picker.setMaxValue(Calendar.getInstance().get(Calendar.YEAR));
        picker.setValue(picker.getMaxValue());
        picker.setWrapSelectorWheel(false);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Production Year")
                .setCancelable(true)
                .setView(layout)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (mListener != null) {
                            mListener.onYearSet(picker.getValue());
                        }
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //
                    }
                });

        return builder.create();
    }
}