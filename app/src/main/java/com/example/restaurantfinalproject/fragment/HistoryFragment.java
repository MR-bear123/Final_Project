package com.example.restaurantfinalproject.fragment;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.restaurantfinalproject.Adapter.ListFoodAdapter;
import com.example.restaurantfinalproject.Adapter.ListHistoryAdapter;
import com.example.restaurantfinalproject.Model.Cart;
import com.example.restaurantfinalproject.Model.Food;
import com.example.restaurantfinalproject.Model.History;
import com.example.restaurantfinalproject.R;
import com.example.restaurantfinalproject.databinding.FragmentListHistoryBinding;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class HistoryFragment extends Fragment {
    private FragmentListHistoryBinding binding;
    private RecyclerView recyclerView;
    private List<History> historyList;
    private DatabaseReference databaseReference;
    private ListHistoryAdapter listHistoryAdapter;

    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if (binding != null) {
            return binding.getRoot();
        }
        binding = FragmentListHistoryBinding.inflate(inflater, container, false);
        View view = binding.getRoot();

        recyclerView = binding.recycListhis;
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        historyList = new ArrayList<>();
        listHistoryAdapter = new ListHistoryAdapter(historyList);
        recyclerView.setAdapter(listHistoryAdapter);

        ImageButton fu = binding.searchmuti;
        fu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showSearchDialog();
            }
        });
        ImageButton re = binding.resfreh;
        re.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ProgressDialog progressDialog = new ProgressDialog(requireContext());
                progressDialog.setMessage("Reload List Users...");
                progressDialog.setCancelable(false);
                progressDialog.show();

                Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        historyList.clear();
                        loadAllHis();
                        progressDialog.dismiss();
                        Toast.makeText(requireContext(), "Repload list Users successfully", Toast.LENGTH_SHORT).show();
                    }
                }, 2000);
            }
        });

        SearchView searchView = binding.searchHis;
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                // Clear the current list data
                historyList.clear();
                // If newText is not empty, perform the search query
                if (!newText.isEmpty()) {
                    searchFood(newText);
                } else {
                    // Reload all users when the SearchView text is empty
                    loadAllHis();
                }
                return true;
            }
        });
        loadAllHis();
        return view;
    }

    private void showSearchDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.search_history_dialog, null);
        builder.setView(dialogView);

        // Initialize EditText fields and search button
        EditText editTextCodeBill = dialogView.findViewById(R.id.editTextCodeBill);
        EditText editTextNumberTable = dialogView.findViewById(R.id.editTextNumberTable);
        EditText editTextStaffname = dialogView.findViewById(R.id.editTextUserStaff);
        Button buttonSearch = dialogView.findViewById(R.id.buttonSearch);

        // Create and show the dialog
        AlertDialog dialog = builder.create();
        dialog.show();

        // Set OnClickListener for the search button
        buttonSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Retrieve values from EditText fields
                String codeBill = editTextCodeBill.getText().toString().trim();
                String numberTable = editTextNumberTable.getText().toString().trim();
                String editTextStaff = editTextStaffname.getText().toString().trim();

                // Check if the EditText fields are not empty
                if (!codeBill.isEmpty() && !numberTable.isEmpty()) {
                    // Perform the search operation
                    searchHistory(codeBill, numberTable, editTextStaff);
                    // Dismiss the dialog
                    dialog.dismiss();
                } else {
                    // Display a toast message if any of the fields are empty
                    Toast.makeText(getContext(), "Please enter values for all fields", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void searchHistory(String codeBill, String numberTable, String username) {
        // Clear the current list data
        historyList.clear();

        // Get a reference to the Firebase database node containing payment history
        DatabaseReference historyRef = FirebaseDatabase.getInstance().getReference().child("payhis");

        // Create a query to filter the payment history based on the provided criteria
        Query query = historyRef.orderByChild("codeBill").equalTo(codeBill);

        // Add a listener to retrieve the filtered payment history data
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                // Check if the query result contains any data
                if (dataSnapshot.exists()) {
                    for (DataSnapshot historySnapshot : dataSnapshot.getChildren()) {
                        // Retrieve history details
                        String dbNumberTable = historySnapshot.child("numberTable").getValue(String.class);
                        String dbUsername = historySnapshot.child("userName").getValue(String.class);

                        // Check if the retrieved numberTable and username match the provided criteria
                        if (dbNumberTable.equals(numberTable) && dbUsername.equals(username)) {
                            // Retrieve cart items
                            List<Cart> cartList = new ArrayList<>();
                            for (DataSnapshot cartSnapshot : historySnapshot.child("cartList").getChildren()) {
                                String name = cartSnapshot.child("namefood").getValue(String.class);
                                int quantity = cartSnapshot.child("quantity").getValue(Integer.class);
                                double price = cartSnapshot.child("price").getValue(Double.class);
                                Cart cart = new Cart(name, price, quantity);
                                cartList.add(cart);
                            }

                            // Retrieve other history details
                            String userId = historySnapshot.child("userId").getValue(String.class);
                            String timestamp = historySnapshot.child("timestamp").getValue(String.class);
                            String date = historySnapshot.child("date").getValue(String.class);
                            double totalPrice = historySnapshot.child("totalPrice").getValue(Double.class);
                            String dbCodeBill = historySnapshot.child("codeBill").getValue(String.class);

                            // Create a new History object with the retrieved details
                            History history = new History();
                            history.setUserId(userId);
                            history.setUserName(username);
                            history.setTotalPrice(totalPrice);
                            history.setTimestamp(timestamp);
                            history.setDate(date);
                            history.setCodeBill(dbCodeBill);
                            history.setCartList(cartList);
                            history.setNumberTable(dbNumberTable);

                            // Add the history to the list
                            historyList.add(history);
                        }
                    }
                    // Notify the adapter of changes in the data set
                    listHistoryAdapter.notifyDataSetChanged();
                } else {
                    // No matching data found
                    Toast.makeText(getContext(), "No matching history found", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle database errors
                Toast.makeText(getContext(), "Database error: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }


    private void searchFood(String searchText) {
        historyList.clear();

        Query query = databaseReference.orderByChild("codeBill").startAt(searchText).endAt(searchText + "\uf8ff");
        Query query2 = databaseReference.orderByChild("numberTable").startAt(searchText).endAt(searchText + "\uf8ff");

        query.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String previousChildName) {
                History history = dataSnapshot.getValue(History.class);
                if (history != null) {
                    historyList.add(history);
                    listHistoryAdapter.notifyDataSetChanged();
                }
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

        query2.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String previousChildName) {
                History history = dataSnapshot.getValue(History.class);
                if (history != null && !historyList.contains(history)) {
                    historyList.add(history);
                    // Notify adapter after adding each search result
                    listHistoryAdapter.notifyDataSetChanged();
                }
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

    private void loadAllHis(){
        FirebaseDatabase.getInstance().getReference().child("payhis").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (DataSnapshot historySnapshot : dataSnapshot.getChildren()) {
                        List<Cart> cartList = new ArrayList<>();
                        for (DataSnapshot cartSnapshot : historySnapshot.child("cartList").getChildren()) {
                            String name = cartSnapshot.child("namefood").getValue(String.class);
                            int quantity = cartSnapshot.child("quantity").getValue(Integer.class);
                            double price = cartSnapshot.child("price").getValue(Double.class);
                            Cart cart = new Cart(name, price, quantity);
                            cartList.add(cart);
                        }


                        String userId = historySnapshot.child("userId").getValue(String.class);
                        String userName = historySnapshot.child("userName").getValue(String.class);
                        double totalPrice = historySnapshot.child("totalPrice").getValue(Double.class);
                        String timestamp = historySnapshot.child("timestamp").getValue(String.class);
                        String date = historySnapshot.child("date").getValue(String.class);
                        String codeBill = historySnapshot.child("codeBill").getValue(String.class);
                        String num = historySnapshot.child("numberTable").getValue(String.class);



                        History history = new History();
                        history.setUserId(userId);
                        history.setUserName(userName);
                        history.setTotalPrice(totalPrice);
                        history.setTimestamp(timestamp);
                        history.setDate(date);
                        history.setCodeBill(codeBill);
                        history.setCartList(cartList);
                        history.setNumberTable(num);
                        historyList.add(history);
                    }
                    listHistoryAdapter.notifyDataSetChanged();
                } else {
                    Toast.makeText(getContext(), "No data found", Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(getContext(), "Error! An error occurred. Please try again later: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
