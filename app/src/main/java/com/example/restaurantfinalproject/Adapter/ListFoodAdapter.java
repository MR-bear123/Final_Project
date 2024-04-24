package com.example.restaurantfinalproject.Adapter;

import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.restaurantfinalproject.Model.Food;
import com.example.restaurantfinalproject.R;
import com.squareup.picasso.Picasso;


import org.greenrobot.eventbus.EventBus;

import java.util.EventListener;
import java.util.List;

public class ListFoodAdapter extends RecyclerView.Adapter<ListFoodAdapter.ListFoodHolder>{
    private List<Food> mFood;
    private String role;
    public void setAdmin(String role) {
        this.role = role;
        notifyDataSetChanged(); // Notify the adapter that the data set has changed
    }

    private ListFoodAdapter.MenuButtonClickListener mListener;
    public interface MenuButtonClickListener {
        void onDeleteMenuButtonClicked(int position);

        void repload(int position);

        void onUpdateMenuButtonClicked(int position);

        void onAddToCartButtonClicked(int position);
    }

    public ListFoodAdapter(List<Food> listFood, ListFoodAdapter.MenuButtonClickListener mListener) {
        this.mFood = listFood;
        this.mListener = mListener;
    }
    @NonNull
    @Override
    public ListFoodHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_list_food, parent, false);
        return new ListFoodHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ListFoodAdapter.ListFoodHolder holder, int position) {
        Food food = mFood.get(position);
        if (food == null) {
            return;
        }
        holder.name.setText("Name: " + food.getName());
        holder.type.setText("Type: " + food.getType());
        holder.price.setText("Price: " + food.getPrice() + "$");
        holder.des.setText("Description: " + food.getDescription());
        String imageUrl = food.getImageUrl();
        if (imageUrl != null && !imageUrl.isEmpty()) {
            Picasso.get().load(imageUrl).into(holder.avatarImageViewmeu);
        } else {
            holder.avatarImageViewmeu.setImageResource(R.drawable.defaulavatar);
        }

        if (!role.equals("Admin")) {
            // Hide the update and delete buttons if the user is not an admin
            holder.mButtonMenuDelete.setVisibility(View.GONE);
            holder.mButtonMenuUpdate.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        if (mFood != null) {
            return mFood.size();
        }
        return 0;
    }

    public class ListFoodHolder extends RecyclerView.ViewHolder {
        TextView name, type, price, des;
        ImageView avatarImageViewmeu;
        Button maddtocart;
        private ImageButton mButtonMenuDelete, mButtonMenuUpdate, mrep;
        public ListFoodHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.item_mename);
            type = itemView.findViewById(R.id.item_metype);
            price = itemView.findViewById(R.id.item_meprice);
            des = itemView.findViewById(R.id.item_medes);
            avatarImageViewmeu = itemView.findViewById(R.id.menu_item_avatarImageView);
            mButtonMenuUpdate = itemView.findViewById(R.id.button_Menu_update);
            mrep = itemView.findViewById(R.id.button_Menu_repload);
            mButtonMenuDelete = itemView.findViewById(R.id.button_Menu_delete);
            maddtocart = itemView.findViewById(R.id.button_AddToCart);

            mButtonMenuDelete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mListener != null) {
                        int position = getAdapterPosition();
                        if (position != RecyclerView.NO_POSITION) {
                            mListener.onDeleteMenuButtonClicked(position);
                        }
                    }
                }
            });

            mButtonMenuUpdate.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mListener != null) {
                        int position = getAdapterPosition();
                        if (position != RecyclerView.NO_POSITION) {
                            mListener.onUpdateMenuButtonClicked(position);
                        }
                    }
                }
            });

            mrep.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mListener != null) {
                        int position = getAdapterPosition();
                        if (position != RecyclerView.NO_POSITION) {
                            mListener.repload(position);
                        }
                    }
                }
            });

            maddtocart.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        mListener.onAddToCartButtonClicked(position);
                    }
                }
            });
        }
    }
}
