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
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.restaurantfinalproject.Adapter.ListEmployeeAdapter;
import com.example.restaurantfinalproject.Model.Users;
import com.example.restaurantfinalproject.R;
import com.example.restaurantfinalproject.databinding.FragmentEmployeeListBinding;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.List;

public class EmployeelistFragment extends Fragment implements ListEmployeeAdapter.OnDeleteButtonClickListener {
    private FragmentEmployeeListBinding binding;
    private RecyclerView recyclerView;
    private ListEmployeeAdapter listEmployeeAdapter;
    private List<Users> listUser;
    private DatabaseReference databaseReference;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if (binding != null) {
            // Fragment is already inflated, no need to inflate it again
            return binding.getRoot();
        }

        binding = FragmentEmployeeListBinding.inflate(inflater, container, false);
        View view = binding.getRoot();

        recyclerView = binding.recycListStaff;
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        listUser = new ArrayList<>();
        listEmployeeAdapter = new ListEmployeeAdapter(listUser, this);
        recyclerView.setAdapter(listEmployeeAdapter);

        FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
        databaseReference = firebaseDatabase.getReference().child("Users");

        SearchView searchView = binding.searchEm;
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }
            @Override
            public boolean onQueryTextChange(String newText) {
                // Clear the current list data
                listUser.clear();

                // If newText is not empty, perform the search query
                if (!newText.isEmpty()) {
                    searchUsers(newText);
                } else {
                    // Reload all users when the SearchView text is empty
                    loadAllUsers();
                }
                return true;
            }
        });

        Button btnRefresh = view.findViewById(R.id.btnRefresh);
        btnRefresh.setOnClickListener(new View.OnClickListener() {
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
                        listUser.clear();
                        loadAllUsers();
                        progressDialog.dismiss();
                        Toast.makeText(requireContext(), "Repload list Users successfully", Toast.LENGTH_SHORT).show();
                    }
                }, 2000);
            }
        });
        loadAllUsers();
        return view;
    }

    private void loadAllUsers() {
        databaseReference.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String previousChildName) {
                Users users = dataSnapshot.getValue(Users.class);
                if (users != null) {
                    users.setKey(dataSnapshot.getKey());
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            listUser.add(users);
                            listEmployeeAdapter.notifyDataSetChanged();
                        }
                    });
                }
            }
            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                Users updatedUser = snapshot.getValue(Users.class);
                if (updatedUser != null) {
                    String updatedUserKey = snapshot.getKey();
                    if (updatedUserKey != null) {
                        int index = -1;
                        for (int i = 0; i < listUser.size(); i++) {
                            String currentUserKey = listUser.get(i).getKey();
                            if (currentUserKey != null && currentUserKey.equals(updatedUserKey)) {
                                index = i;
                                break;
                            }
                        }
                        if (index != -1) {
                            listUser.set(index, updatedUser);
                            listEmployeeAdapter.notifyItemChanged(index);
                        }
                    }
                }
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {
                String deletedKey = snapshot.getKey();
                // Find the position of the item to be deleted in the listUser
                int position = -1;
                for (int i = 0; i < listUser.size(); i++) {
                    if (listUser.get(i).getKey().equals(deletedKey)) {
                        position = i;
                        break;
                    }
                }
                // If the position of the item to be deleted is found, delete it and update the RecyclerView
                if (position != -1) {
                    listUser.remove(position);
                    listEmployeeAdapter.notifyItemRemoved(position);
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

    // Search
    private void searchUsers(String searchText) {
        // Clear the list before adding search results
        listUser.clear();
        databaseReference.orderByChild("name").startAt(searchText).endAt(searchText + "\uf8ff")
                .addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String previousChildName) {
                        Users users = dataSnapshot.getValue(Users.class);
                        if (users != null) {
                            listUser.add(users);
                        }
                        listEmployeeAdapter.notifyDataSetChanged(); // Notify adapter after adding all search results
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

    // Delete
    @Override
    public void onDeleteButtonClicked(int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setMessage("Are you sure you want to delete this user?")
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // Get user information at the selected position
                        Users userToDelete = listUser.get(position);
                        String userKeyToDelete = userToDelete.getKey(); // Get the key of the user
                        // Access Firebase Database and delete the user corresponding to userKeyToDelete
                        databaseReference.child(userKeyToDelete).removeValue()
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        // Find the position of the item to be deleted
                                        int index = listUser.indexOf(userToDelete);
                                        if (index != -1) {
                                            // Remove the user from the list and notify adapter
                                            listUser.remove(index);
                                            listEmployeeAdapter.notifyItemRemoved(index);
                                        }
                                        Toast.makeText(getContext(), "User deleted successfully", Toast.LENGTH_SHORT).show();
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Toast.makeText(getContext(), "Failed to delete user: " + e.getMessage(), Toast.LENGTH_SHORT).show();
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

}
