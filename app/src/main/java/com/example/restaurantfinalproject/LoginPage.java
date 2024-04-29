package com.example.restaurantfinalproject;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.restaurantfinalproject.databinding.ActivityLoginPageBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class LoginPage extends AppCompatActivity {
    // Key for SharedPreferences
    private static final String PREF_EMAIL = "email";
    private static final String PREF_PASSWORD = "password";
    private static final String PREF_REMEMBER = "remember";
    ActivityLoginPageBinding binding;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_page);
        binding = ActivityLoginPageBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        // Load saved login information if "Remember Me" was checked previously
        SharedPreferences preferences = getSharedPreferences("loginPrefs", Context.MODE_PRIVATE);
        boolean remember = preferences.getBoolean(PREF_REMEMBER, false);
        if (remember) {
            String savedEmail = preferences.getString(PREF_EMAIL, "");
            String savedPassword = preferences.getString(PREF_PASSWORD, "");
            binding.SIEmail.setText(savedEmail);
            binding.SIPassword.setText(savedPassword);
            binding.cBoxRemember.setChecked(true);
        }
        binding.SIButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String SIemail = binding.SIEmail.getText().toString();
                String SIpassword = binding.SIPassword.getText().toString();
                if (SIemail.equals("") || SIpassword.equals("")) {
                    Toast.makeText(LoginPage.this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (!SIemail.contains("@") || !SIemail.contains(".")) {
                    Toast.makeText(LoginPage.this, "Please enter a valid email address", Toast.LENGTH_SHORT).show();
                    return;
                }
                // CheckBox onlick
                if (binding.cBoxRemember.isChecked()) {
                    // Save login information
                    SharedPreferences preferences = getSharedPreferences("loginPrefs", Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = preferences.edit();
                    editor.putString(PREF_EMAIL, SIemail);
                    editor.putString(PREF_PASSWORD, SIpassword);
                    editor.putBoolean(PREF_REMEMBER, true);
                    editor.apply();
                } else {
                    // Clear login information
                    SharedPreferences preferences = getSharedPreferences("loginPrefs", Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = preferences.edit();
                    editor.clear();
                    editor.apply();
                }
                //login logic
                if (!(SIemail.isEmpty() && SIpassword.isEmpty())) {
                    FirebaseAuth mAuthSI = FirebaseAuth.getInstance();
                    mAuthSI.signInWithEmailAndPassword(SIemail, SIpassword).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                FirebaseUser user = mAuthSI.getCurrentUser();
                                if (user != null && user.isEmailVerified()) {
                                    String uid = task.getResult().getUser().getUid();
                                    FirebaseDatabase fb = FirebaseDatabase.getInstance();
                                    fb.getReference().child("Users").child(uid).child("role").addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                                            String role = snapshot.getValue(String.class);
                                            if (role.equals("Staff")) {
                                                Intent intent = new Intent(LoginPage.this, MainActivity.class);
                                                startActivity(intent);
                                                Toast.makeText(LoginPage.this, "Staff Login successfully", Toast.LENGTH_SHORT).show();
                                            } else if (role.equals("Admin")) {
                                                Intent intent = new Intent(LoginPage.this, DashboardPage.class);
                                                startActivity(intent);
                                                Toast.makeText(LoginPage.this, "Admin Login successfully", Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                        @Override
                                        public void onCancelled(@NonNull DatabaseError error) {
                                        }
                                    });
                                } else {
                                    Toast.makeText(LoginPage.this, "Please verify your email before logging in", Toast.LENGTH_LONG).show();
                                    // Log users out to ensure they cannot access the application
                                    mAuthSI.signOut();
                                }
                            } else {
                                Toast.makeText(LoginPage.this, "Failed to sign in: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }

            }
        });
    }
}