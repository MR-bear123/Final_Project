package com.example.restaurantfinalproject;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.restaurantfinalproject.Model.Food;
import com.example.restaurantfinalproject.databinding.ActivityAddFoodBinding;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.UUID;

public class add_food extends AppCompatActivity {
    private ProgressDialog progressDialog;
    private Uri selectedImageUri;
    private static final int PICK_IMAGE_REQUEST = 1;

    private DatabaseReference MenuRef;
    ActivityAddFoodBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_food);
        binding = ActivityAddFoodBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        Toolbar toolbar = binding.toolbarmenu;
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Creat Food and Drink");

        MenuRef = FirebaseDatabase.getInstance().getReference("FoodandDrink");

        binding.imgavatarmenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {pickImage();}
        });
        binding.MenuButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveFood();
            }
        });
    }

    private void pickImage() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            selectedImageUri = data.getData();
            Glide.with(this).load(selectedImageUri).into(binding.imgavatarmenu);
        }
    }
    private void saveFood() {
        String name = binding.MenuName.getText().toString().trim();
        String description = binding.MenuDescription.getText().toString().trim();
        String type = binding.MenuType.getText().toString().trim();
        String priceStr = binding.MenuPrice.getText().toString().trim();

        if (name.isEmpty() || description.isEmpty() || type.isEmpty() || priceStr.isEmpty() || selectedImageUri == null) {
            Toast.makeText(this, "Please fill in all fields and select an image", Toast.LENGTH_SHORT).show();
            return;
        }

        double price = Double.parseDouble(priceStr);

        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Uploading Image...");
        progressDialog.setCancelable(false);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        progressDialog.setMax(100);
        progressDialog.show();
        uploadImageToStorage(selectedImageUri, name, description, type, price);
    }

    private void uploadImageToStorage(Uri imageUri, String name, String description, String type, double price) {
        StorageReference storageRef = FirebaseStorage.getInstance().getReference().child("food_images/" + UUID.randomUUID().toString() + ".jpg");

        if (imageUri != null) {
            UploadTask uploadTask = storageRef.putFile(imageUri);
            uploadTask.addOnProgressListener(taskSnapshot -> progressDialog.setProgress((int) ((100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount())));
            uploadTask.addOnSuccessListener(taskSnapshot -> {
                storageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                    String imageUrl = uri.toString();
                    String fid = MenuRef.push().getKey();
                    if (fid != null) {
                        Food food = new Food(fid, name, description, type, price, imageUrl);
                        MenuRef.child(fid).setValue(food);
                        Toast.makeText(this, "Food added successfully", Toast.LENGTH_SHORT).show();
                        progressDialog.dismiss(); // Dismiss ProgressDialog after successful upload
                        finish();
                    }
                });
            }).addOnFailureListener(e -> {
                Toast.makeText(this, "Failed to upload image", Toast.LENGTH_SHORT).show();
                progressDialog.dismiss(); // Dismiss ProgressDialog if there's an error
            });
        } else {
            storageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                String imageUrl = uri.toString();
                String fid = MenuRef.push().getKey();
                if (fid != null) {
                    Food food = new Food(fid, name, description, type, price, imageUrl);
                    MenuRef.child(fid).setValue(food);
                    Toast.makeText(this, "Food added successfully", Toast.LENGTH_SHORT).show();
                    progressDialog.dismiss(); // Dismiss ProgressDialog after successful upload
                    finish();
                }
            }).addOnFailureListener(exception -> {
                Toast.makeText(this, "Failed to get current image URI", Toast.LENGTH_SHORT).show();
                progressDialog.dismiss(); // Dismiss ProgressDialog if there's an error
            });
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

}