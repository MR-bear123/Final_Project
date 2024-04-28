package com.example.restaurantfinalproject.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.restaurantfinalproject.R;
import com.example.restaurantfinalproject.Model.Users;
import com.example.restaurantfinalproject.databinding.FragmentAccountBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;

public class AccountFragment extends Fragment {
    FragmentAccountBinding binding;
    FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
    private FirebaseAuth mAuth;
    EditText SU_name, SU_email, SU_password, SU_confirm, SU_phone;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentAccountBinding.inflate(inflater, container, false);
        View view = binding.getRoot();
        mAuth = FirebaseAuth.getInstance();
        SU_name = view.findViewById(R.id.SU_name);
        SU_email = view.findViewById(R.id.SU_email);
        SU_password = view.findViewById(R.id.SU_password);
        SU_confirm = view.findViewById(R.id.SU_confirm);
        SU_phone = view.findViewById(R.id.SU_phone);

        binding.SUButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String userName = SU_name.getText().toString();
                String userEmail = SU_email.getText().toString();
                String userPassword = SU_password.getText().toString();
                String userConfirm = SU_confirm.getText().toString();
                String userPhone = SU_phone.getText().toString();

                if (userName.equals("") || userPassword.equals("") || userConfirm.equals("") || userEmail.equals("") || userPhone.equals("")) {
                    Toast.makeText(getContext(), "Please fill in all fields", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (!userEmail.contains("@") || !userEmail.contains(".")) {
                    Toast.makeText(getContext(), "Please enter a valid email address", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (!userPassword.equals(userConfirm)) {
                    Toast.makeText(getContext(), "Password and confirmation password do not match", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Inside the onClick method of the register button:
                mAuth.createUserWithEmailAndPassword(userEmail, userPassword).addOnCompleteListener(getActivity(), task -> {
                    if (task.isSuccessful()) {
                        // Send verification email
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            user.sendEmailVerification()
                                    .addOnCompleteListener(taskEmail -> {
                                        if (taskEmail.isSuccessful()) {
                                            Toast.makeText(getContext(), "Verification email sent to " + user.getEmail(), Toast.LENGTH_SHORT).show();
                                        } else {
                                            Toast.makeText(getContext(), "Failed to send verification email", Toast.LENGTH_SHORT).show();
                                        }
                                    });

                            // Save user information to Firebase Database
                            String userId = user.getUid();
                            Users userObject = new Users(userId, userName, userPhone, userEmail, userPassword, "Staff");
                            firebaseDatabase.getReference().child("Users").child(userId).setValue(userObject);
                            Toast.makeText(getContext(), "" +
                                    "", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(getContext(), "Registration failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });

            }
        });
        return view;
    }
}
