package com.example.restaurantfinalproject;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.DialogFragment;

import com.example.restaurantfinalproject.Model.Table;
import com.example.restaurantfinalproject.TimeandDate.DatePickerFragment;
import com.example.restaurantfinalproject.TimeandDate.TimePickerUtil;
import com.example.restaurantfinalproject.databinding.ActivityAddWarehousesBinding;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;


import android.content.pm.PackageManager;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import android.Manifest;
import java.util.Random;

public class add_warehouses extends AppCompatActivity {
    private static final int PERMISSION_REQUEST_SEND_SMS = 1;
    private DatabaseReference bookingRef;
    ActivityAddWarehousesBinding binding;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAddWarehousesBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        Toolbar toolbar = binding.toolbarbook;
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Booking Table");

        bookingRef = FirebaseDatabase.getInstance().getReference("bookings");

        binding.BookButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bookTable();
            }
        });

        binding.BookTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TimePickerUtil.showTimePickerDialog(add_warehouses.this, binding.BookTime);
            }
        });

        binding.BookDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DialogFragment newFragment = new DatePickerFragment(binding.BookDate, "yyyy/MM/dd");
                newFragment.show(getSupportFragmentManager(), "datePicker");
            }
        });
    }

    private void bookTable() {
        String tableId = bookingRef.push().getKey();
        String name = binding.BookName.getText().toString();
        String phone = binding.BookPhone.getText().toString();
        String time = binding.BookTime.getText().toString();
        String date = binding.BookDate.getText().toString();
        String des = binding.BookDescription.getText().toString();
        String randomCode = generateRandomCode();
        Table table = new Table(tableId, name, phone, des, date, time, randomCode,"pending");

        if (name.equals("") || phone.equals("")|| time.equals("")|| date.equals("")|| des.equals("")) {
            Toast.makeText(add_warehouses.this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
            return;
        }
        bookingRef.child(tableId).setValue(table);
        Toast.makeText(add_warehouses.this, "Table added successfully", Toast.LENGTH_SHORT).show();

        binding.BookName.setText("");
        binding.BookPhone.setText("");
        binding.BookTime.setText("");
        binding.BookDate.setText("");
        binding.BookDescription.setText("");
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.SEND_SMS}, PERMISSION_REQUEST_SEND_SMS);
        } else {
            sendSMS(phone, "Welcome to Restaurant, Dear " + name + ", Your table has been booked successfully on " + date + " at " + time + ".Your booking code is: " + randomCode);
        }
    }

    //back page
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    private String generateRandomCode() {
        String prefix = "RES";
        String ss = "DB";
        Random rand = new Random();
        int randomNumber = rand.nextInt(10000);
        String formattedRandomNumber = String.format("%04d", randomNumber);
        return prefix + formattedRandomNumber + ss;
    }

    private void sendSMS(String phoneNumber, String message) {
        try {
            SmsManager smsManager = SmsManager.getDefault();
            smsManager.sendTextMessage(phoneNumber, null, message, null, null);
            Toast.makeText(add_warehouses.this, "SMS sent successfully", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(add_warehouses.this, "Failed to send SMS", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }
}
