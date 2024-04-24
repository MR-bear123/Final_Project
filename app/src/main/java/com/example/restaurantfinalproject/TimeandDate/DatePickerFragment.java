package com.example.restaurantfinalproject.TimeandDate;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.viewmodel.CreationExtras;

import java.text.SimpleDateFormat;
import java.util.Calendar;

public class DatePickerFragment extends DialogFragment implements
        DatePickerDialog.OnDateSetListener {

    TextView txtTextViewMapped;
    EditText edtEditTextMapped;
    String dateFormat;//"dd/MM/yyyy"


    public DatePickerFragment(TextView txtTextViewMapped, String dateFormat){
         this.txtTextViewMapped=txtTextViewMapped;
         this.dateFormat=dateFormat;
    }
    public DatePickerFragment(EditText edtEditTextMapped, String dateFormat){
        this.edtEditTextMapped=edtEditTextMapped;
        this.dateFormat=dateFormat;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {

        Calendar calendar=Calendar.getInstance();
        int year=calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DATE);
       return new DatePickerDialog(getActivity(), this, year, month, day);
    }

    @Override
    public void onDateSet(DatePicker datePicker, int i, int i1, int i2) {
        Calendar calendar=Calendar.getInstance();
        calendar.set(i,i1,i2);
        SimpleDateFormat simpleDateFormat=new SimpleDateFormat(dateFormat);
        String sdate=simpleDateFormat.format(calendar.getTime());
        if (txtTextViewMapped!=null){
            txtTextViewMapped.setText(sdate);
        }else if(edtEditTextMapped!=null){
            edtEditTextMapped.setText(sdate);
        }

    }

    @NonNull
    @Override
    public CreationExtras getDefaultViewModelCreationExtras() {
        return super.getDefaultViewModelCreationExtras();
    }
}
