package com.example.restaurantfinalproject.fragment;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.restaurantfinalproject.Adapter.ListKetchinAdapter;
import com.example.restaurantfinalproject.Model.Kitchen;
import com.example.restaurantfinalproject.Model.Table;
import com.example.restaurantfinalproject.databinding.FragmentListKetchinBinding;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.List;

public class FragmentListKitchen extends Fragment implements ListKetchinAdapter.KetButtonClickListener {
    private FragmentListKetchinBinding binding;
    private RecyclerView recyclerView;
    private List<Kitchen> listket;
    private ListKetchinAdapter listKetchinAdapter;
    private DatabaseReference databaseReference;
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true); // Retain fragment instance upon rotation
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        if (binding == null) {
            binding = FragmentListKetchinBinding.inflate(inflater, container, false);
            View view = binding.getRoot();

            recyclerView = binding.recycLisKet;
            recyclerView.setHasFixedSize(true);
            recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

            listket = new ArrayList<>();
            listKetchinAdapter= new ListKetchinAdapter(listket, this);
            recyclerView.setAdapter(listKetchinAdapter);

            FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
            databaseReference = firebaseDatabase.getReference().child("kitchen");

            loadListKetchin();
            return view;
        } else {
            return binding.getRoot();
        }
    }

    private void loadListKetchin() {
        databaseReference.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String previousChildName) {
                String ketId = dataSnapshot.getKey();
                if (!isTableExists(ketId)) {
                    Kitchen ket = dataSnapshot.getValue(Kitchen.class);
                    if (ket != null) {
                        ket.setId(ketId);
                        listket.add(ket);
                        listKetchinAdapter.notifyDataSetChanged();
                    }
                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                String changedKey = snapshot.getKey();
                for (int i = 0; i < listket.size(); i++) {
                    if (listket.get(i).getId().equals(changedKey)) {
                        Kitchen updatedKet = snapshot.getValue(Kitchen.class);
                        updatedKet.setId(changedKey);
                        listket.set(i, updatedKet);
                        listKetchinAdapter.notifyItemChanged(i);
                        break;
                    }
                }
            }
            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {
                String deletedKey = snapshot.getKey();
                for (int i = 0; i < listket.size(); i++) {
                    if (listket.get(i).getId().equals(deletedKey)) {
                        listket.remove(i);
                        listKetchinAdapter.notifyItemRemoved(i);
                        break;
                    }
                }
            }
            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {}
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private boolean isTableExists(String ketid) {
        for (Kitchen ket : listket) {
            if (ket.getId().equals(ketid)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void onAcceptButtonClicked(int position) {
        updateTableStatus(position,"Accepted");
    }

    @Override
    public void onRejectButtonClicked(int position) {
        updateTableStatus(position,"Rejected");
    }

    private void updateTableStatus(int position, String status) {
        Kitchen kitchen = listket.get(position);
        kitchen.setStastu(status);
        DatabaseReference tableRef = FirebaseDatabase.getInstance().getReference().child("kitchen").child(kitchen.getId());
        tableRef.child("stastu").setValue(status)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(getContext(), "Kitchen status updated successfully", Toast.LENGTH_SHORT).show();
                        listket.set(position, kitchen);
                        listKetchinAdapter.notifyItemChanged(position);
                    } else {
                        Toast.makeText(getContext(), "Failed to update kitchen status", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
