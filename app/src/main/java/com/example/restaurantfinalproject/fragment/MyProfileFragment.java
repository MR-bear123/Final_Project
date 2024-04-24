package com.example.restaurantfinalproject.fragment;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.restaurantfinalproject.R;
import com.example.restaurantfinalproject.Model.Users;
import com.example.restaurantfinalproject.databinding.FragmentMyProfileBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

public class MyProfileFragment extends Fragment {
    private static final int MY_REQUEST_CODE = 10;
    private static final int PICK_IMAGE_REQUEST = 1;
    private FragmentMyProfileBinding binding;
    private DatabaseReference databaseReference;
    private Uri imageUri;
    private static final String PREF_PROFILE_IMAGE_URI = "profile_image_uri";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentMyProfileBinding.inflate(inflater, container, false);
        //get table databas Users
        databaseReference = FirebaseDatabase.getInstance().getReference("Users");
        setUserInformation();
        Btlistener();
        checkProfileImageUri();
        logout();
        return binding.getRoot();
    }

    //Onlick img and updateprofile
    private void Btlistener() {
        binding.imgavatar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onClickRequestPermission();
            }
        });

        binding.UpdateProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                UpdateProfile();
            }
        });

    }
    //get information
    private void setUserInformation() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            String uid = currentUser.getUid();
            databaseReference.child(uid).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {

                        Users user = dataSnapshot.getValue(Users.class);
                        if (user != null) {
                            binding.ProfEmail.setText(user.getEmail());
                            binding.ProfName.setText(user.getName());
                            binding.ProfPhone.setText(user.getPhoneNumber());
                            binding.ProfRole.setText(String.valueOf(user.getRole()));

                            String imageUrl = dataSnapshot.child("imageUrl").getValue(String.class);
                            if (imageUrl != null && !imageUrl.isEmpty()) {
                                StorageReference storageReference = FirebaseStorage.getInstance().getReferenceFromUrl(imageUrl);
                                storageReference.getDownloadUrl().addOnSuccessListener(uri -> {
                                    Glide.with(requireContext())
                                            .load(uri)
                                            .apply(RequestOptions.circleCropTransform())
                                            .into(binding.imgavatar);
                                }).addOnFailureListener(exception -> {});
                           }
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                }
            });
        }
    }

//    update information  database Users
    private void UpdateProfile() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if (user != null) {
            String uid = user.getUid();
            Users currentUser = new Users(uid);
            currentUser.setName(binding.ProfName.getText().toString());
            currentUser.setEmail(binding.ProfEmail.getText().toString());
            currentUser.setPhoneNumber(binding.ProfPhone.getText().toString());
            currentUser.setRole(binding.ProfRole.getText().toString());

            DatabaseReference userRef = databaseReference.child(uid);
            userRef.setValue(currentUser)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                Toast.makeText(requireContext(), "Profile information updated successfully", Toast.LENGTH_SHORT).show();
                                if (imageUri != null) {
                                    uploadImage();
                                }
                            }
                        }
                    });
        }
    }

    //upload img
    private void uploadImage() {
        ProgressDialog progressDialog = new ProgressDialog(requireContext());
        progressDialog.setTitle("Uploading Image...");
        progressDialog.setCancelable(false);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        progressDialog.setMax(100);
        progressDialog.show();
        StorageReference storageRef = FirebaseStorage.getInstance().getReference().child("profile_images/" + FirebaseAuth.getInstance().getCurrentUser().getUid() + ".jpg");

        if (imageUri != null) {
            // If there is a new image selected, upload a new image
            UploadTask uploadTask = storageRef.putFile(imageUri);
            uploadTask.addOnProgressListener(taskSnapshot -> progressDialog.setProgress((int) ((100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount())));
            uploadTask.addOnSuccessListener(taskSnapshot -> {
                storageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                    updateProfileWithImage(uri.toString());
                    progressDialog.dismiss();
                });
            }).addOnFailureListener(e -> {
                Toast.makeText(requireContext(), "Failed to upload image", Toast.LENGTH_SHORT).show();
                progressDialog.dismiss();
            });
        } else {
            // If there are no new images selected, get the URL of the current image from Firebase Storage and update it to the Realtime Database
            storageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                updateProfileWithImage(uri.toString());
                progressDialog.dismiss();
            }).addOnFailureListener(exception -> {
                Toast.makeText(requireContext(), "Failed to get current image URI", Toast.LENGTH_SHORT).show();
                progressDialog.dismiss();
            });
        }
    }
    // user update img firebase in realtime database
    private void updateProfileWithImage(String imageUrl) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference().child("Users").child(user.getUid());
            usersRef.child("imageUrl").setValue(imageUrl)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Toast.makeText(requireContext(), "Profile image updated successfully", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(requireContext(), "Failed to update profile image", Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }


    //Method to check if SharedPreferences contains the URL of the profile image
    private void checkProfileImageUri() {
        SharedPreferences sharedPreferences = requireActivity().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        String uri = sharedPreferences.getString(PREF_PROFILE_IMAGE_URI, "");
        if (!uri.isEmpty()) {
            //If SharedPreferences contains an image URI, display the image from this URI
            imageUri = Uri.parse(uri);
            binding.imgavatar.setImageURI(imageUri);
        } else {
            //If SharedPreferences does not contain an image URL, display the default image
            Glide.with(requireContext())
                    .load(R.drawable.defaulavatar)
                    .apply(RequestOptions.circleCropTransform())
                    .into(binding.imgavatar);
        }
    }
    //Logout
    private void logout() {
        //Remove profile image URL from SharedPreferences when user logs out
        SharedPreferences sharedPreferences = requireActivity().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.remove(PREF_PROFILE_IMAGE_URI);
        editor.apply();
    }

    // Function Img
    private void onClickRequestPermission() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            openGallery();
            return;
        }
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            openGallery();
        } else {
            ActivityCompat.requestPermissions(requireActivity(), new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, MY_REQUEST_CODE);
        }
    }
    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == getActivity().RESULT_OK && data != null && data.getData() != null) {
            imageUri = data.getData();
            binding.imgavatar.setImageURI(imageUri);
            saveProfileImageUri(imageUri.toString());
        }
    }
    private void saveProfileImageUri(String uri) {
        SharedPreferences sharedPreferences = requireActivity().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(PREF_PROFILE_IMAGE_URI, uri);
        editor.apply();
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == MY_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openGallery();
            } else {
                Toast.makeText(requireContext(), "Permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }
    public void setBitmapImgView(Bitmap bitmapImgView){
        binding.imgavatar.setImageBitmap(bitmapImgView);
    }
}

