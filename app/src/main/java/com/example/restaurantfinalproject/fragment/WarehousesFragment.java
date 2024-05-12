package com.example.restaurantfinalproject.fragment;


import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.telephony.SmsManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.restaurantfinalproject.Adapter.ListWarehousesAdapter;
import com.example.restaurantfinalproject.Model.Table;
import com.example.restaurantfinalproject.R;
import com.example.restaurantfinalproject.TimeandDate.DatePickerFragment;
import com.example.restaurantfinalproject.TimeandDate.TimePickerUtil;
import com.example.restaurantfinalproject.add_warehouses;
import com.example.restaurantfinalproject.databinding.FragmentWarehousesBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class WarehousesFragment extends Fragment implements ListWarehousesAdapter.BookButtonClickListener {
    private static final int PERMISSION_REQUEST_SEND_SMS = 2;
    FloatingActionButton floatingActionButton;
    private RecyclerView recyclerView;
    private List<Table> listTable;
    private ListWarehousesAdapter listWarehousesAdapter;
    private FragmentWarehousesBinding binding;

    private DatabaseReference databaseReference;

    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setRetainInstance(true); // Retain fragment instance upon rotation
    }
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if (binding == null) {
            binding = FragmentWarehousesBinding.inflate(inflater, container, false);
            View view = binding.getRoot();

            recyclerView = binding.recycListBook;
            recyclerView.setHasFixedSize(true);
            recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

            listTable = new ArrayList<>();
            listWarehousesAdapter = new ListWarehousesAdapter(listTable, this);
            recyclerView.setAdapter(listWarehousesAdapter);

            FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
            databaseReference = firebaseDatabase.getReference().child("bookings");

            floatingActionButton = binding.Addbook;
            floatingActionButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startActivity(new Intent(getActivity(), add_warehouses.class));
                }
            });
            SearchView searchView = binding.searchBook;
            searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                @Override
                public boolean onQueryTextSubmit(String query) {
                    return false;
                }
                @Override
                public boolean onQueryTextChange(String newText) {
                    // Clear the current list data
                    listTable.clear();

                    // If newText is not empty, perform the search query
                    if (!newText.isEmpty()) {
                        searchTable(newText);
                    } else {
                        // Reload all users when the SearchView text is empty
                        loadAllTable();
                    }
                    return true;
                }
            });
            Button btnRefresh = view.findViewById(R.id.btn_Book_Refresh);
            btnRefresh.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ProgressDialog progressDialog = new ProgressDialog(requireContext());
                    progressDialog.setMessage("Reload List Table...");
                    progressDialog.setCancelable(false);
                    progressDialog.show();

                    Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            listTable.clear();
                            loadAllTable();
                            progressDialog.dismiss();
                            Toast.makeText(requireContext(), "Reload list table successfully", Toast.LENGTH_SHORT).show();
                        }
                    }, 2000);
                }
            });
            loadAllTable();

            return view;
        } else {
            return binding.getRoot();
        }
    }

    private void updateTableStatus(int position, String status) {
        Table table = listTable.get(position);
        table.setStatus(status);

        DatabaseReference tableRef = FirebaseDatabase.getInstance().getReference().child("bookings").child(table.getId());
        tableRef.child("status").setValue(status)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(getContext(), "Table status updated successfully", Toast.LENGTH_SHORT).show();
                        listTable.set(position, table);
                        listWarehousesAdapter.notifyItemChanged(position);
                    } else {
                        Toast.makeText(getContext(), "Failed to update table status", Toast.LENGTH_SHORT).show();
                    }
                });
    }


    private void loadAllTable() {
        databaseReference.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String previousChildName) {
                String tableId = dataSnapshot.getKey();
                if (!isTableExists(tableId)) {
                    Table table = dataSnapshot.getValue(Table.class);
                    if (table != null) {
                        table.setId(tableId);
                        listTable.add(table);
                        listWarehousesAdapter.notifyDataSetChanged();
                        checkBookingTime(table);
                    }
                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                String changedKey = snapshot.getKey();
                for (int i = 0; i < listTable.size(); i++) {
                    if (listTable.get(i).getId().equals(changedKey)) {
                        Table updatedTable = snapshot.getValue(Table.class);
                        updatedTable.setId(changedKey);
                        listTable.set(i, updatedTable);
                        listWarehousesAdapter.notifyItemChanged(i);

                        if (updatedTable.getStatus().equals("rejected") && !updatedTable.isSMSSent()) {
                            if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) {
                                ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.SEND_SMS}, PERMISSION_REQUEST_SEND_SMS);
                            } else {
                                sendSMS(updatedTable.getCuphone(), "Welcome to Restaurant, Dear " +
                                        updatedTable.getCuname() + ", Your table has been booked" +
                                        "\nOn date" + updatedTable.getDate() + " At " + updatedTable.getTime() + ". \nYour booking code is: " + updatedTable.getRandomCode() +
                                        ".\nWe have canceled your dining reservation information. Please check if there is anything wrong, if anything is wrong, please contact us so we can fix it promptly. Thank you.");

                                updatedTable.setSMSSent(true);
                                databaseReference.child(updatedTable.getId()).child("isSMSSent").setValue(true);
                            }
                        }
                        break;
                    }
                }
            }



            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {
                String deletedKey = snapshot.getKey();
                for (int i = 0; i < listTable.size(); i++) {
                    if (listTable.get(i).getId().equals(deletedKey)) {
                        listTable.remove(i);
                        listWarehousesAdapter.notifyItemRemoved(i);
                        break;
                    }
                }
            }


            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }


    private boolean isTableExists(String tableId) {
        for (Table table : listTable) {
            if (table.getId().equals(tableId)) {
                return true;
            }
        }
        return false;
    }


    private void searchTable(String searchText) {
        // Clear the list before adding search results
        listTable.clear();
        databaseReference.orderByChild("cuname").startAt(searchText).endAt(searchText + "\uf8ff")
                .addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String previousChildName) {
                        Table table = dataSnapshot.getValue(Table.class);
                        if (table != null) {
                            listTable.add(table);
                        }
                        listWarehousesAdapter.notifyDataSetChanged(); // Notify adapter after adding all search results
                    }

                    @Override
                    public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                    }

                    @Override
                    public void onChildRemoved(@NonNull DataSnapshot snapshot) {
                    }

                    @Override
                    public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                    }
                });
    }

    @Override
    public void onDeleteBookButtonClicked(int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setMessage("Are you sure you want to delete this table?")
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // Get user information at the selected position
                        Table table = listTable.get(position);
                        String userKeyToDelete = table.getId(); // Get the key of the user
                        // Access Firebase Database and delete the user corresponding to userKeyToDelete
                        databaseReference.child(userKeyToDelete).removeValue()
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        // Find the position of the item to be deleted
                                        int index = listTable.indexOf(table);
                                        if (index != -1) {
                                            // Remove the user from the list and notify adapter
                                            listTable.remove(index);
                                            listWarehousesAdapter.notifyItemRemoved(index);
                                        }
                                        Toast.makeText(getContext(), "Table deleted successfully", Toast.LENGTH_SHORT).show();
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Toast.makeText(getContext(), "Failed to delete table: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                });
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });
        AlertDialog alert = builder.create();
        alert.show();
    }

    @Override
    public void onUpdateBookButtonClicked(int position) {
        Table table = listTable.get(position);

        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(requireContext());
        View dialogView = getLayoutInflater().inflate(R.layout.activity_update_warehouses_dialog, null);
        bottomSheetDialog.setContentView(dialogView);

        EditText Name = dialogView.findViewById(R.id.Book_Uname);
        EditText Phone = dialogView.findViewById(R.id.Book_Uphone);
        EditText Time = dialogView.findViewById(R.id.Book_Utime);
        EditText Date = dialogView.findViewById(R.id.Book_Udate);
        EditText Des = dialogView.findViewById(R.id.Book_Udescription);

        Time.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TimePickerUtil.showTimePickerDialog(getContext(), Time);
            }
        });
        Date.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DialogFragment newFragment = new DatePickerFragment(Date, "yyyy/MM/dd");
                newFragment.show(getChildFragmentManager(), "datePicker");
            }
        });

        Name.setText(table.getCuname());
        Phone.setText(table.getCuphone());
        Time.setText(table.getTime());
        Date.setText(table.getDate());
        Des.setText(table.getDescription());

        dialogView.findViewById(R.id.Book_update).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String newName = Name.getText().toString();
                String newPhone = Phone.getText().toString();
                String newTime = Time.getText().toString();
                String newDate = Date.getText().toString();
                String newDes = Des.getText().toString();

                String tableId = table.getId();
                DatabaseReference tableRef = databaseReference.child(tableId);
                Table updatedTable = new Table(tableId, newName, newPhone, newDes, newDate, newTime, table.getRandomCode(), table.getStatus());
                tableRef.setValue(updatedTable)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    listTable.set(position, updatedTable);
                                    listWarehousesAdapter.notifyDataSetChanged();
                                    Toast.makeText(requireContext(), "Updated booking table successfully", Toast.LENGTH_SHORT).show();
                                    if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) {
                                        ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.SEND_SMS}, PERMISSION_REQUEST_SEND_SMS);
                                    } else if (!table.isSMSSent()) {
                                        sendSMS(table.getCuphone(), "Welcome to Restaurant, Dear " +
                                                table.getCuname() + ", Your table has been booked" +
                                                "\nOn date" + table.getDate() + " At " + table.getTime() + ". \nYour booking code is: " + table.getRandomCode() +
                                                ".\nWe have updated your dining reservation information. Please check if there is anything wrong, if anything is wrong, please contact us so we can fix it promptly. Thank you.");
                                        table.setSMSSent(true);
                                        databaseReference.child(table.getId()).child("isSMSSent").setValue(true);
                                    }
                                }
                            }
                        });
                bottomSheetDialog.dismiss();
            }
        });

        dialogView.findViewById(R.id.Book_cancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bottomSheetDialog.dismiss();
            }
        });

        bottomSheetDialog.show();
    }


    @Override
    public void onAcceptButtonClicked(int position) {
        Table table = listTable.get(position);

        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm");
            Date currentTime = new Date();
            Date bookingTime = sdf.parse(table.getDate() + " " + table.getTime());

            if (currentTime.after(bookingTime)) {
                Toast.makeText(requireContext(), "Cannot accept booking, time has expired", Toast.LENGTH_SHORT).show();
                return;
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
        updateTableStatus(position, "Accepted");
    }




    @Override
    public void onRejectButtonClicked(int position) {
        updateTableStatus(position, "Rejected");
    }

    private void checkBookingTime(Table table) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm");
            Date currentTime = new Date();
            Date bookingTime = sdf.parse(table.getDate() + " " + table.getTime());
            long diffInMillies = Math.abs(currentTime.getTime() - bookingTime.getTime());
            long diffInMinutes = diffInMillies / (60 * 1000);

            if (!table.getStatus().equals("accepted") && diffInMinutes > 5 && !table.isSMSSent()) {
                databaseReference.child(table.getId()).child("status").setValue("rejected")
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Toast.makeText(getContext(), "The booking has been canceled due to timeout.", Toast.LENGTH_SHORT).show();
                            }
                        });
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    private void sendSMS(String phoneNumber, String message) {
        try {
            SmsManager smsManager = SmsManager.getDefault();
            smsManager.sendTextMessage(phoneNumber, null, message, null, null);
            Toast.makeText(getContext(), "SMS sent successfully", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(getContext(), "Failed to send SMS", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }
}
