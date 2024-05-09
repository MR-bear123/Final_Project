package com.example.restaurantfinalproject;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.view.MenuItem;
import android.view.View;

import com.bumptech.glide.Glide;
import com.example.restaurantfinalproject.Model.Users;
import com.example.restaurantfinalproject.databinding.LayoutHeaderBinding;
import com.example.restaurantfinalproject.fragment.AccountFragment;
import com.example.restaurantfinalproject.fragment.CarrtFagment;
import com.example.restaurantfinalproject.fragment.ChangPasswordFragment;
import com.example.restaurantfinalproject.fragment.EmployeelistFragment;
import com.example.restaurantfinalproject.fragment.FoodFragment;
import com.example.restaurantfinalproject.fragment.FragmentChart;
import com.example.restaurantfinalproject.fragment.FragmentListKitchen;
import com.example.restaurantfinalproject.fragment.HistoryFragment;
import com.example.restaurantfinalproject.fragment.MyProfileFragment;
import com.example.restaurantfinalproject.fragment.WarehousesFragment;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.io.IOException;

public class DashboardPage extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private static final int FRAGMENT_ACCOUNT = 0;
    private static final int FRAGMENT_WAREHOUSES = 1;
    private static final int FRAGMENT_FOOD = 2;
    private static final int FRAGMENT_MYPROFILE = 3;
    private static final int FRAGMENT_CHANGEPASSWORD = 4;
    private static final int FRAGMENT_EMPLOYEELIST = 5;
    private static final int FRAGMENT_CARTLIST = 6;
    private static final int FRAGMENT_HISTORYLIST = 7;
    private static final int FRAGMENT_CHART = 8;
    private static final int FRAGMENT_KITCHEN = 9;
    private int mCurrentFragment = FRAGMENT_ACCOUNT;
    private DrawerLayout dralay;
    private static final int MY_REQUEST_CODE = 10;
    private LayoutHeaderBinding binding;
    private DatabaseReference databaseReference;
    final private MyProfileFragment mMyProfileFragment = new MyProfileFragment();
    final private AccountFragment mAccountFragment =  new AccountFragment();
    final private FoodFragment mFoodFragment = new FoodFragment();
    final private WarehousesFragment mWarehousesFragment = new WarehousesFragment();
    final private ChangPasswordFragment mChangPasswordFragment = new ChangPasswordFragment();
    final private EmployeelistFragment mEmployeelistFragment = new EmployeelistFragment();
    final private CarrtFagment mCartFragment = new CarrtFagment();
    final private HistoryFragment mHistoryFragment = new HistoryFragment();
    final private FragmentChart mChart = new FragmentChart();
    final private FragmentListKitchen mKitchen = new FragmentListKitchen();
    NavigationView navigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard_page);

        Toolbar toolbar = findViewById(R.id.toolbaradmin);
        setSupportActionBar(toolbar);
        //navigation
        dralay = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, dralay, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        dralay.addDrawerListener(toggle);
        toggle.syncState();
        navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        //biding
        binding = LayoutHeaderBinding.inflate(getLayoutInflater());
        View headerView = navigationView.getHeaderView(0);
        binding = LayoutHeaderBinding.bind(headerView);
        //databaseReference
        databaseReference = FirebaseDatabase.getInstance().getReference("Users");

        //reload database
        addDatabaseListener();

    }
    //reload database
    private void addDatabaseListener() {
        databaseReference.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                showUserInformation();
            }
            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                showUserInformation();
            }
            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
                showUserInformation();
            }
            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }
    // show information
    public void showUserInformation() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            String uid = user.getUid();
            databaseReference.child(uid).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        Users userProfile = dataSnapshot.getValue(Users.class);
                        if (userProfile != null) {
                            binding.email.setText(userProfile.getEmail());
                            if (userProfile.getName() == null || userProfile.getName().isEmpty()) {
                                binding.name.setVisibility(View.GONE);
                            } else {
                                binding.name.setVisibility(View.VISIBLE);
                                binding.name.setText(userProfile.getName());
                            }
                            String imageUrl = userProfile.getImageUrl();
                            if (imageUrl != null && !imageUrl.isEmpty()) {
                                Glide.with(getApplicationContext())
                                        .load(imageUrl)
                                        .into(binding.img);
                            } else {
                                binding.img.setImageResource(R.drawable.defaulavatar);
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
    //navigation
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.Account) {
            if (mCurrentFragment != FRAGMENT_ACCOUNT) {
                replaceFragment(mAccountFragment);
                mCurrentFragment = FRAGMENT_ACCOUNT;
            }
        } else if (id == R.id.Food) {
            if (mCurrentFragment != FRAGMENT_FOOD) {
                replaceFragment(mFoodFragment);
                mCurrentFragment = FRAGMENT_FOOD;
            }
        } else if (id == R.id.Warehouses) {
            if (mCurrentFragment != FRAGMENT_WAREHOUSES) {
                replaceFragment(mWarehousesFragment);
                mCurrentFragment = FRAGMENT_WAREHOUSES;
            }
        } else if (id == R.id.Profile) {
            if (mCurrentFragment != FRAGMENT_MYPROFILE) {
                replaceFragment(mMyProfileFragment);
                mCurrentFragment = FRAGMENT_MYPROFILE;
            }
        } else if (id == R.id.Chang) {
            if (mCurrentFragment != FRAGMENT_CHANGEPASSWORD) {
                replaceFragment(mChangPasswordFragment);
                mCurrentFragment = FRAGMENT_CHANGEPASSWORD;
            }
        }else if (id == R.id.Listemloyee) {
            if (mCurrentFragment != FRAGMENT_EMPLOYEELIST) {
                replaceFragment(mEmployeelistFragment);
                mCurrentFragment = FRAGMENT_EMPLOYEELIST;
            }
        } else if (id == R.id.Cart) {
            if (mCurrentFragment != FRAGMENT_CARTLIST) {
                replaceFragment(mCartFragment);
                mCurrentFragment = FRAGMENT_CARTLIST;
            }
        } else if (id == R.id.Carthis) {
            if (mCurrentFragment != FRAGMENT_HISTORYLIST) {
                replaceFragment(mHistoryFragment);
                mCurrentFragment = FRAGMENT_HISTORYLIST;
            }
        }else if (id == R.id.Kitchen) {
            if (mCurrentFragment != FRAGMENT_KITCHEN) {
                replaceFragment(mKitchen);
                mCurrentFragment = FRAGMENT_KITCHEN;
            }
        }else if (id == R.id.chart) {
            if (mCurrentFragment != FRAGMENT_CHART) {
                replaceFragment(mChart);
                mCurrentFragment = FRAGMENT_CHART;
            }
        } else if (id == R.id.Logout) {
            FirebaseAuth.getInstance().signOut();
            Intent intent = new Intent(this, LoginPage.class);
            startActivity(intent);
            finish();
        }

        dralay.closeDrawer(GravityCompat.START);
        return true;
    }
    @Override
    public void onBackPressed() {
        if (dralay.isDrawerOpen(GravityCompat.START)) {
            dralay.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }
    private void replaceFragment(Fragment fragment){
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.content_frame, fragment);
        transaction.commit();
    }

    //Function choose img into My Profile
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode == MY_REQUEST_CODE){
            if (grantResults.length>0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                openGallery();
            }
        }
    }
    public void openGallery() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent,"select Picture"), MY_REQUEST_CODE);
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == MY_REQUEST_CODE && resultCode == RESULT_OK){
            if (data != null) {
                Uri uri = data.getData();
                try {
                    Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
                    mMyProfileFragment.setBitmapImgView(bitmap);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
