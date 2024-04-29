package com.example.restaurantfinalproject.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.restaurantfinalproject.databinding.FragmentChangPasswordBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class ChangPasswordFragment extends Fragment {
    private FragmentChangPasswordBinding binding;

    EditText emailEditText;
    Button changePasswordButton;
    FirebaseAuth mAuth;
    DatabaseReference usersRef;
    FirebaseUser currentUser;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentChangPasswordBinding.inflate(inflater, container, false);
        View view = binding.getRoot();

        emailEditText = binding.CPEmail;
        changePasswordButton = binding.CPBTNewPass;

        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser(); // Initialize currentUser

        // Check if currentUser is not null before using it
        if (currentUser != null) {
            usersRef = FirebaseDatabase.getInstance().getReference().child("Users").child(currentUser.getUid());
            Btlistener();
        } else {
            Toast.makeText(requireContext(), "User not logged in", Toast.LENGTH_SHORT).show();
        }

        return view;
    }

    private void Btlistener() {
        changePasswordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String email = emailEditText.getText().toString();
                if (email.isEmpty() ) {
                    Toast.makeText(requireContext(), "Please enter email", Toast.LENGTH_SHORT).show();
                    return;
                }
                DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference().child("Users");
                usersRef.orderByChild("email").equalTo(email).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                final String userId = snapshot.getKey();
                                mAuth.sendPasswordResetEmail(email)
                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if (task.isSuccessful()) {
                                                    Toast.makeText(getActivity(), "Reset Password successfully", Toast.LENGTH_SHORT).show();
                                                } else {
                                                    Toast.makeText(getActivity(), "Failed to send reset email: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                                }
                                            }
                                        });
                            }
                        } else {
                            Toast.makeText(requireContext(), "User with this email does not exist", Toast.LENGTH_SHORT).show();
                        }
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        Toast.makeText(requireContext(), "Database error: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

}
