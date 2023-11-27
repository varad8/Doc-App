package com.vrnitsolution.healthapp.DoctorUI;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import com.vrnitsolution.healthapp.DoctorUI.model.Dosage;
import com.vrnitsolution.healthapp.R;

public class ViewDosageDialog {

    public interface OnDosageEditListener {
        void onDosageEdited(int position, Dosage editedDosage);
        void onDosageDeleted(int position);
    }

    public static void showDialog(Context context, final Dosage dosage, final int position, final OnDosageEditListener listener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Dosage Details");

        View view = LayoutInflater.from(context).inflate(R.layout.dialog_dosage, null);
        builder.setView(view);

        final EditText dosageNameEditText = view.findViewById(R.id.editDosageName);
        final EditText dosageTimeEditText = view.findViewById(R.id.editDosageTime);
        final EditText dosageRemarkEditText = view.findViewById(R.id.editDosageRemark);
        final EditText dosageAfterMillEditText = view.findViewById(R.id.editDosageAfterMill);

        dosageNameEditText.setText(dosage.getDosageName());
        dosageTimeEditText.setText(dosage.getDosageTime());
        dosageRemarkEditText.setText(dosage.getDosageRemark());
        dosageAfterMillEditText.setText(dosage.getDosageAfterMill());

        builder.setPositiveButton("Save Edits", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String editedName = dosageNameEditText.getText().toString();
                String editedTime = dosageTimeEditText.getText().toString();
                String editedRemark = dosageRemarkEditText.getText().toString();
                String editedAfterMill = dosageAfterMillEditText.getText().toString();

                Dosage editedDosage = new Dosage(editedName, editedTime, editedRemark, editedAfterMill);
                if (listener != null) {
                    listener.onDosageEdited(position, editedDosage);
                }
            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Do nothing, dialog will be dismissed
            }
        });

        builder.setNeutralButton("Delete", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (listener != null) {
                    listener.onDosageDeleted(position);
                }
            }
        });

        builder.show();
    }
}
