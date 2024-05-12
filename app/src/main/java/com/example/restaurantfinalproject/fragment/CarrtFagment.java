package com.example.restaurantfinalproject.fragment;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.restaurantfinalproject.Adapter.ListCartAdapter;
import com.example.restaurantfinalproject.Model.Cart;
import com.example.restaurantfinalproject.Model.Food;
import com.example.restaurantfinalproject.Model.History;
import com.example.restaurantfinalproject.Model.Kitchen;
import com.example.restaurantfinalproject.Model.Users;
import com.example.restaurantfinalproject.R;
import com.example.restaurantfinalproject.databinding.FragmentListCartBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class CarrtFagment extends Fragment implements ListCartAdapter.CartButtonClickListener {
    private static final long REFRESH_INTERVAL = 60000;
    FragmentListCartBinding binding;
    private RecyclerView recyclerView;
    private ListCartAdapter listCartAdapter;
    private List<Cart> listCart;
    private DatabaseReference cartKe;
    private DatabaseReference databaseReference;

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
        binding = FragmentListCartBinding.inflate(inflater, container, false);
        View view = binding.getRoot();

        recyclerView = binding.recycListCart;
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        listCart = new ArrayList<>();
        listCartAdapter = new ListCartAdapter(listCart, this);
        recyclerView.setAdapter(listCartAdapter);

        FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
        databaseReference = firebaseDatabase.getReference().child("cart");

        Button btnRefresh = binding.btnRefreshcart;
        btnRefresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ProgressDialog progressDialog = new ProgressDialog(requireContext());
                progressDialog.setMessage("Reload List Order...");
                progressDialog.setCancelable(false);
                progressDialog.show();

                Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        listCart.clear();
                        loadAllCart();
                        progressDialog.dismiss();
                        Toast.makeText(requireContext(), "Repload list Order successfully", Toast.LENGTH_SHORT).show();
                    }
                }, 10000);
            }
        });

        Button pay = binding.payment;
        pay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fetchFoodInformationAndTotalPrice();
            }
        });
        scheduleCartListRefreshing();
        return view;
    }

    private void scheduleCartListRefreshing() {
        ProgressDialog progressDialog = new ProgressDialog(requireContext());
        progressDialog.setMessage("Reload List Order...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                listCart.clear();
                // Load the list of carts
                loadAllCart();
                progressDialog.dismiss();
                Toast.makeText(requireContext(), "Auto Reload list order successfully", Toast.LENGTH_SHORT).show();
                // Schedule the next refresh
                handler.postDelayed(this, REFRESH_INTERVAL);
            }
        }, 6000);
    }


    private void loadAllCart() {
        clearListFood();
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            String currentUserId = currentUser.getUid();
            DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference().child("Users").child(currentUserId);
            usersRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        Users user = dataSnapshot.getValue(Users.class);
                        if (user != null) {
                            String currentUserName = user.getName();
                            databaseReference.orderByChild("nameStaff").equalTo(currentUserName).addChildEventListener(new ChildEventListener() {
                                @Override
                                public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String previousChildName) {
                                    Cart cart = dataSnapshot.getValue(Cart.class);
                                    if (cart != null) {
                                        cart.setIdcart(dataSnapshot.getKey());
                                        listCart.add(cart);
                                        int position = listCart.size() - 1;
                                        recyclerView.removeAllViews();
                                        listCartAdapter.notifyItemInserted(position);
                                    }
                                }
                                @Override
                                public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                                    String foodKey = snapshot.getKey();
                                    int position = getPositionByKey(foodKey);
                                    if (position != -1) {
                                        Cart updatedFood = snapshot.getValue(Cart.class);
                                        listCart.set(position, updatedFood);
                                        recyclerView.removeAllViews();
                                        listCartAdapter.notifyItemChanged(position);
                                    }
                                }
                                @Override
                                public void onChildRemoved(@NonNull DataSnapshot snapshot) {
                                    String foodKey = snapshot.getKey();
                                    int position = getPositionByKey(foodKey);
                                    if (position != -1) {
                                        listCart.remove(position);
                                        listCartAdapter.notifyItemRemoved(position);
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
                    }
                }
                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                }
            });
            calculateTotalPriceFromFirebase();
        }
    }

    private void clearListFood() {
        listCart.clear();
        listCartAdapter.notifyDataSetChanged();
    }

    private int getPositionByKey(String foodKey) {
        for (int i = 0; i < listCart.size(); i++) {
            if (listCart.get(i).getIdcart().equals(foodKey)) {
                return i;
            }
        }
        return -1;
    }

    @Override
    public void onDeleteCartButtonClicked(int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setMessage("Are you sure you want to delete this order?")
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // Get user information at the selected position
                        Cart cart = listCart.get(position);
                        String userKeyToDelete = cart.getIdcart(); // Get the key of the user
                        // Access Firebase Database and delete the user corresponding to userKeyToDelete
                        databaseReference.child(userKeyToDelete).removeValue()
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        // Find the position of the item to be deleted
                                        int index = listCart.indexOf(cart);
                                        if (index != -1) {
                                            // Remove the user from the list and notify adapter
                                            listCart.remove(index);
                                            listCartAdapter.notifyItemRemoved(index);
                                        }
                                        Toast.makeText(getContext(), "Order deleted successfully", Toast.LENGTH_SHORT).show();
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
    public void onUpdateCartButtonClicked(int position) {
        Cart cart = listCart.get(position);

        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(requireContext());
        View dialogView = getLayoutInflater().inflate(R.layout.activity_update_cart_dialog, null);
        bottomSheetDialog.setContentView(dialogView);

        EditText quantityEditText = dialogView.findViewById(R.id.Cart_Uquanlity);
        quantityEditText.setText(String.valueOf(cart.getQuanlity()));

        dialogView.findViewById(R.id.Cart_update).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String quantityString = quantityEditText.getText().toString();
                int quantity = Integer.parseInt(quantityString);

                String cartId = cart.getIdcart();
                DatabaseReference cartRef = databaseReference.child(cartId);
                Cart updatedCart = new Cart(cartId, cart.getNameStaff(), cart.getNamefood(), cart.getImageUrl(), cart.getPrice(), quantity);
                cartRef.setValue(updatedCart)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    listCart.set(position, updatedCart);
                                    listCartAdapter.notifyDataSetChanged();
                                    Toast.makeText(requireContext(), "Updated order successfully", Toast.LENGTH_SHORT).show();
                                    calculateTotalPriceFromFirebase();
                                }
                            }
                        });
                bottomSheetDialog.dismiss();
            }
        });

        dialogView.findViewById(R.id.Cart_cancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bottomSheetDialog.dismiss();
            }
        });
        bottomSheetDialog.show();
    }

    @Override
    public void onBellCartButtonClicked(int position) {
        Cart selectedCart = listCart.get(position);
        String des = binding.CartDescription.getText().toString();
        if (des.isEmpty()) {
            Toast.makeText(getContext(), "Please fill in the table description", Toast.LENGTH_SHORT).show();
            return;
        }
        addToBell(selectedCart,des);
    }

    private void addToBell(Cart selectedCart, String Des) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        cartKe = FirebaseDatabase.getInstance().getReference("kitchen");
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference().child("Users").child(user.getUid());
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    String userName = dataSnapshot.child("name").getValue(String.class);
                    DatabaseReference cartRef = FirebaseDatabase.getInstance().getReference().child("cart").child(selectedCart.getIdcart());
                    cartRef.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot cartSnapshot) {
                            if (cartSnapshot.exists()) {
                                String foodName = cartSnapshot.child("namefood").getValue(String.class);
                                String number = binding.numbertabel.getText().toString().trim();
                                if (number.isEmpty()) {
                                    Toast.makeText(getContext(), "Please fill in the table number", Toast.LENGTH_SHORT).show();
                                    return;
                                }
                                int quanlity = cartSnapshot.child("quanlity").getValue(Integer.class);

                                String kettid = cartKe.push().getKey();
                                Kitchen ketItem = new Kitchen(kettid,number,userName,foodName,quanlity,Des,"Processing");

                                DatabaseReference kettRef = FirebaseDatabase.getInstance().getReference().child("kitchen");
                                kettRef.push().setValue(ketItem);

                                Toast.makeText(getContext(), "Item added to Ketchin successfully", Toast.LENGTH_SHORT).show();

//                                binding.numbertabel.setText("");
                                binding.CartDescription.setText("");
                            } else {
                                Toast.makeText(getContext(), "Ketchin not found", Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError foodError) {
                            Toast.makeText(getContext(), "Failed to retrieve ketchin data: " + foodError.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
                } else {
                    Toast.makeText(getContext(), "User not found", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError userError) {
                Toast.makeText(getContext(), "Failed to retrieve user data: " + userError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void calculateTotalPriceFromFirebase() {
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            String currentUserId = currentUser.getUid();
            DatabaseReference userRef = FirebaseDatabase.getInstance().getReference().child("Users").child(currentUserId);
            userRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        String currentUserName = dataSnapshot.child("name").getValue(String.class);
                        DatabaseReference totalPriceRef = FirebaseDatabase.getInstance().getReference().child("cart");

                        totalPriceRef.orderByChild("nameStaff").equalTo(currentUserName).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                double totalPrice = 0;
                                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                    Cart cart = snapshot.getValue(Cart.class);
                                    if (cart != null) {
                                        totalPrice += cart.getPrice() * cart.getQuanlity();
                                    }
                                }
                                TextView totalPriceTextView = binding.textTotalPrice;
                                totalPriceTextView.setText("Total Price: " + totalPrice);
                                totalPriceTextView.setVisibility(View.VISIBLE);
                            }
                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {
                                Toast.makeText(getContext(), "Failed to calculate total price: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                }
                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Toast.makeText(getContext(), "Failed to retrieve user data: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void fetchFoodInformationAndTotalPrice() {
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            String currentUserId = currentUser.getUid();
            DatabaseReference userRef = FirebaseDatabase.getInstance().getReference().child("Users").child(currentUserId);
            userRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        String currentUserName = dataSnapshot.child("name").getValue(String.class);
                        DatabaseReference totalPriceRef = FirebaseDatabase.getInstance().getReference().child("cart");
                        totalPriceRef.orderByChild("nameStaff").equalTo(currentUserName).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                // Create a list to store dish information and total price
                                List<Cart> cartList = new ArrayList<>();
                                double totalPrice = 0;
                                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                    Cart cart = snapshot.getValue(Cart.class);
                                    if (cart != null) {
                                        Cart carts = new Cart(cart.getNamefood(), cart.getPrice(),cart.getQuanlity());
                                        cartList.add(carts);
                                        totalPrice += cart.getPrice() * cart.getQuanlity();
                                    }
                                }

                                String number = binding.numbertabel.getText().toString().trim();
                                if (number.isEmpty()) {
                                    Toast.makeText(getContext(), "Please fill in the table number", Toast.LENGTH_SHORT).show();
                                    return;
                                }
                                // Display dish information and total price and quanlity
                                showFoodInformationAndTotalPrice(cartList, totalPrice);
                                savePaymentHistory(currentUserId, currentUserName, cartList, totalPrice,number);
                                clearCartDataForCurrentUser(currentUserName);
                                binding.numbertabel.setText("");
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {
                                Toast.makeText(getContext(), "Failed to calculate total price: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Toast.makeText(getContext(), "Failed to retrieve user data: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void showFoodInformationAndTotalPrice(List<Cart> cartList, double totalPrice) {
        StringBuilder dialogMessage = new StringBuilder();
        dialogMessage.append("Food Information:\n");
        for (Cart cart : cartList) {
            dialogMessage.append("- ").append(cart.getNamefood()).append(": $").append(cart.getPrice()).append("+").append(cart.getQuanlity()).append("\n");
        }
        dialogMessage.append("\nTotal Price: $").append(totalPrice);
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Order Summary")
                .setMessage(dialogMessage.toString())
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void savePaymentHistory(String userId, String userName, List<Cart> cartList, double totalPrice, String numbertable) {
        DatabaseReference paymentHistoryRef = FirebaseDatabase.getInstance().getReference().child("payhis");
        Date currentTime = Calendar.getInstance().getTime();
        String invoiceNumber = generateInvoiceNumber();

        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());

        String formattedDate = dateFormat.format(currentTime);
        String formattedTime = timeFormat.format(currentTime);

        History paymentHistory = new History();
        paymentHistory.setNumberTable(numbertable);
        paymentHistory.setUserId(userId);
        paymentHistory.setUserName(userName);
        paymentHistory.setCartList(cartList);
        paymentHistory.setTotalPrice(totalPrice);
        paymentHistory.setTimestamp(formattedTime);
        paymentHistory.setDate(formattedDate);
        paymentHistory.setCodeBill(invoiceNumber);
        paymentHistoryRef.push().setValue(paymentHistory)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(getContext(), "Payment history saved successfully", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(getContext(), "Failed to save payment history: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private String generateInvoiceNumber() {
        return "RESDB" + System.currentTimeMillis();
    }

    private void clearCartDataForCurrentUser(String currentUserName) {
        DatabaseReference cartRef = FirebaseDatabase.getInstance().getReference().child("cart");
        cartRef.orderByChild("nameStaff").equalTo(currentUserName).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    snapshot.getRef().removeValue();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(getContext(), "Failed to clear cart data: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

}

