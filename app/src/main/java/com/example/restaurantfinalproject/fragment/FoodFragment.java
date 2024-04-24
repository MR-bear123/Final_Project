package com.example.restaurantfinalproject.fragment;
import static android.app.Activity.RESULT_OK;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.restaurantfinalproject.Adapter.ListFoodAdapter;
import com.example.restaurantfinalproject.Model.Cart;
import com.example.restaurantfinalproject.Model.Food;
import com.example.restaurantfinalproject.R;
import com.example.restaurantfinalproject.add_food;
import com.example.restaurantfinalproject.databinding.FragmentFoodBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class FoodFragment extends Fragment implements ListFoodAdapter.MenuButtonClickListener {
    private static final int IMAGE_PICKER_REQUEST = 1;
    FloatingActionButton floatingActionButton;
    private RecyclerView recyclerView;
    private List<Food> listFood;
    private ListFoodAdapter listFoodAdapter;
    private DatabaseReference databaseReference;
    private DatabaseReference cartRef;
    private FragmentFoodBinding binding;
    ImageView imgAvatar;

    private Uri imageUri;
    private String foodId;


    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true); // Retain fragment instance upon rotation
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if (binding != null) {
            return binding.getRoot();
        }
        binding = FragmentFoodBinding.inflate(inflater, container, false);
        View view = binding.getRoot();

        recyclerView = binding.recycListMenu;
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        listFood = new ArrayList<>();
        listFoodAdapter = new ListFoodAdapter(listFood, this);
        recyclerView.setAdapter(listFoodAdapter);

        FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
        databaseReference = firebaseDatabase.getReference().child("FoodandDrink");
        floatingActionButton = binding.AddMenu;
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        String userId = currentUser.getUid();
        DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference().child("Users").child(userId);

        usersRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    String userRole = dataSnapshot.child("role").getValue(String.class);
                    if (userRole != null && userRole.equals("Admin")) {
                        floatingActionButton.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                startActivity(new Intent(getActivity(), add_food.class));
                            }
                        });
                    } else {
                        floatingActionButton.setVisibility(View.GONE);
                    }

                        listFoodAdapter.setAdmin(userRole);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });


        SearchView searchView = binding.searchMenu;
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                // Clear the current list data
                listFood.clear();
                // If newText is not empty, perform the search query
                if (!newText.isEmpty()) {
                    searchFood(newText);
                } else {
                    // Reload all users when the SearchView text is empty
                    loadAllFood();
                }
                return true;
            }
        });

        // Load all Users data when the fragment is created
        loadAllFood();
        return view;
    }





    private void loadAllFood() {
        clearListFood();
        databaseReference.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String previousChildName) {
                Food food = dataSnapshot.getValue(Food.class);
                if (food != null) {
                    food.setFid(dataSnapshot.getKey());
                    listFood.add(food);
                    int position = listFood.size() - 1;
                    listFoodAdapter.notifyItemInserted(position);
                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                String foodKey = snapshot.getKey();
                int position = getPositionByKey(foodKey);
                if (position != -1) {
                    Food updatedFood = snapshot.getValue(Food.class);
                    listFood.set(position, updatedFood);
                    recyclerView.removeAllViews();
                    listFoodAdapter.notifyItemChanged(position);
                }
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {
                String foodKey = snapshot.getKey();
                int position = getPositionByKey(foodKey);
                if (position != -1) {
                    listFood.remove(position);
                    listFoodAdapter.notifyItemRemoved(position);
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


    private void clearListFood() {
        listFood.clear();
        listFoodAdapter.notifyDataSetChanged();
    }

    private void searchFood(String searchText) {
        listFood.clear();

        Query query = databaseReference.orderByChild("name").startAt(searchText).endAt(searchText + "\uf8ff");
        Query query2 = databaseReference.orderByChild("type").startAt(searchText).endAt(searchText + "\uf8ff");

        query.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String previousChildName) {
                Food food = dataSnapshot.getValue(Food.class);
                if (food != null) {
                    listFood.add(food);
                    listFoodAdapter.notifyDataSetChanged();
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
                Food food = dataSnapshot.getValue(Food.class);
                if (food != null && !listFood.contains(food)) {
                    listFood.add(food);
                    // Notify adapter after adding each search result
                    listFoodAdapter.notifyDataSetChanged();
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

    @Override
    public void onDeleteMenuButtonClicked(int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setMessage("Are you sure you want to delete this table?")
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // Get user information at the selected position
                        Food food = listFood.get(position);
                        String userKeyToDelete = food.getFid(); // Get the key of the user
                        // Access Firebase Database and delete the user corresponding to userKeyToDelete
                        databaseReference.child(userKeyToDelete).removeValue()
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        // Find the position of the item to be deleted
                                        int index = listFood.indexOf(food);
                                        if (index != -1) {
                                            // Remove the user from the list and notify adapter
                                            listFood.remove(index);
                                            listFoodAdapter.notifyItemRemoved(index);
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
    public void repload(int position) {
        ProgressDialog progressDialog = new ProgressDialog(requireContext());
        progressDialog.setMessage("Reload List Food...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                refreshRecyclerView();
                progressDialog.dismiss();
                Toast.makeText(requireContext(), "Repload list food successfully", Toast.LENGTH_SHORT).show();
            }
        }, 2000);
    }

    @Override
    public void onUpdateMenuButtonClicked(int position) {
        Food food = listFood.get(position);
        foodId = food.getFid();
        DatabaseReference foodRef = databaseReference.child(food.getFid());
        foodRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String name = dataSnapshot.child("name").getValue(String.class);
                String type = dataSnapshot.child("type").getValue(String.class);
                double price = dataSnapshot.child("price").getValue(Double.class);
                String description = dataSnapshot.child("description").getValue(String.class);
                String imageUrl = dataSnapshot.child("imageUrl").getValue(String.class);

                Food updatedFood = new Food(food.getFid(), name, description, type, price, imageUrl);
                listFood.set(position, updatedFood);
                listFoodAdapter.notifyItemChanged(position);

                BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(requireContext());
                View dialogView = getLayoutInflater().inflate(R.layout.activity_update_food_dialog, null);

                bottomSheetDialog.setContentView(dialogView);

                EditText Name = dialogView.findViewById(R.id.Menu_Uname);
                EditText Type = dialogView.findViewById(R.id.Menu_Utype);
                EditText Price = dialogView.findViewById(R.id.Menu_Uprice);
                EditText Des = dialogView.findViewById(R.id.Menu_Udescription);
                imgAvatar = dialogView.findViewById(R.id.Menu_Uimgavatarmenu);

                Name.setText(name);
                Type.setText(type);
                Price.setText(String.valueOf(price));
                Des.setText(description);

                if (imageUrl != null && !imageUrl.isEmpty()) {
                    StorageReference storageReference = FirebaseStorage.getInstance().getReferenceFromUrl(imageUrl);
                    storageReference.getDownloadUrl().addOnSuccessListener(uri -> {
                        Glide.with(requireContext()).clear(imgAvatar);
                        Glide.with(requireContext())
                                .load(uri)
                                .apply(RequestOptions.circleCropTransform())
                                .into(imgAvatar);
                    }).addOnFailureListener(exception -> {});
                }

                ImageView chooseImageButton = dialogView.findViewById(R.id.Menu_Uimgavatarmenu);
                chooseImageButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent galleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                        startActivityForResult(galleryIntent, IMAGE_PICKER_REQUEST);
                    }
                });

                dialogView.findViewById(R.id.Menu_update).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String newName = Name.getText().toString();
                        String newType = Type.getText().toString();
                        double newPrice = Double.parseDouble(Price.getText().toString());
                        String newDes = Des.getText().toString();
                        Food updatedFood = new Food(food.getFid(), newName, newDes, newType, newPrice, imageUrl);
                        foodRef.setValue(updatedFood)
                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()) {
                                            uploadImage();
                                            Toast.makeText(requireContext(), "Updated food successfully", Toast.LENGTH_SHORT).show();
                                            new Handler().postDelayed(new Runnable() {
                                                @Override
                                                public void run() {
                                                    repload(position);
                                                }
                                            }, 4000);
                                        }
                                    }
                                });
                        bottomSheetDialog.dismiss();
                    }
                });

                dialogView.findViewById(R.id.Menu_cancel).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        bottomSheetDialog.dismiss();
                    }
                });
                bottomSheetDialog.show();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(requireContext(), "Failed to retrieve food data: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onAddToCartButtonClicked(int position) {
        Food selectedFood = listFood.get(position);
        addToCart(selectedFood);
    }

    private void addToCart(Food selectedFood) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        cartRef = FirebaseDatabase.getInstance().getReference("cart");
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference().child("Users").child(user.getUid());
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    String userName = dataSnapshot.child("name").getValue(String.class);
                    DatabaseReference foodRef = FirebaseDatabase.getInstance().getReference().child("FoodandDrink").child(selectedFood.getFid());
                    foodRef.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot foodSnapshot) {
                            if (foodSnapshot.exists()) {
                                String foodName = foodSnapshot.child("name").getValue(String.class);
                                double price = foodSnapshot.child("price").getValue(double.class);

                                String foodImageUrl = foodSnapshot.child("imageUrl").getValue(String.class);
                                String Cartid = cartRef.push().getKey();
                                Cart cartItem = new Cart(Cartid,userName, foodName, foodImageUrl, price, 1);

                                DatabaseReference cartRef = FirebaseDatabase.getInstance().getReference().child("cart");
                                cartRef.push().setValue(cartItem);

                                Toast.makeText(getContext(), "Item added to cart successfully", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(getContext(), "Food not found", Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError foodError) {
                            Toast.makeText(getContext(), "Failed to retrieve food data: " + foodError.getMessage(), Toast.LENGTH_SHORT).show();
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

    private void uploadImage() {
        ProgressDialog progressDialog = new ProgressDialog(requireContext());
        progressDialog.setTitle("Uploading Image...");
        progressDialog.setCancelable(false);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        progressDialog.setMax(100);
        progressDialog.show();
        StorageReference storageRef = FirebaseStorage.getInstance().getReference().child("food_images/" + foodId  + ".jpg");

        if (imageUri != null) {
            // If there is a new image selected, upload a new image
            UploadTask uploadTask = storageRef.putFile(imageUri);
            uploadTask.addOnProgressListener(taskSnapshot -> progressDialog.setProgress((int) ((100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount())));
            uploadTask.addOnSuccessListener(taskSnapshot -> {
                storageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                    updateFoodWithImage(uri.toString()); // Update image URL after successful download URL retrieval
                    progressDialog.dismiss();
                });
            }).addOnFailureListener(e -> {
                Toast.makeText(requireContext(), "Failed to upload image", Toast.LENGTH_SHORT).show();
                progressDialog.dismiss();
            });
        } else {
            // If there are no new images selected, get the URL of the current image from Firebase Storage and update it to the Realtime Database
            storageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                updateFoodWithImage(uri.toString());
                progressDialog.dismiss();
            }).addOnFailureListener(exception -> {
                Toast.makeText(requireContext(), "Failed to get current image URI", Toast.LENGTH_SHORT).show();
                progressDialog.dismiss();
            });
        }
    }

    private void updateFoodWithImage(String imageUrl) {
        DatabaseReference foodRef = databaseReference.child(foodId);
        foodRef.child("imageUrl").setValue(imageUrl)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(requireContext(), "Food image updated successfully", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(requireContext(), "Failed to update food image", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == IMAGE_PICKER_REQUEST && resultCode == RESULT_OK && data != null) {
            imageUri = data.getData();
            InputStream inputStream = null;
            try {
                inputStream = requireContext().getContentResolver().openInputStream(imageUri);
            } catch (FileNotFoundException e) {
                throw new RuntimeException(e);
            }
            Bitmap img = BitmapFactory.decodeStream(inputStream);

            imgAvatar.setImageBitmap(img);

        }
    }


    private void refreshRecyclerView() {
        listFood.clear();
        listFoodAdapter.notifyDataSetChanged();
        loadAllFood();
    }

    private int getPositionByKey(String foodKey) {
        for (int i = 0; i < listFood.size(); i++) {
            if (listFood.get(i).getFid().equals(foodKey)) {
                return i;
            }
        }
        return -1;
    }

}