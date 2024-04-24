package com.example.restaurantfinalproject.Adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.restaurantfinalproject.Model.Users;
import com.example.restaurantfinalproject.R;
import com.squareup.picasso.Picasso;

import java.util.List;

public class ListEmployeeAdapter extends RecyclerView.Adapter<ListEmployeeAdapter.ListEmployeeHolder> {

    private List<Users> mUsers;
    private OnDeleteButtonClickListener mListener;
    public interface OnDeleteButtonClickListener {
        void onDeleteButtonClicked(int position);
    }

    public ListEmployeeAdapter(List<Users> mUsers, OnDeleteButtonClickListener listener) {
        this.mUsers = mUsers;
        this.mListener = listener;
    }

    @NonNull
    @Override
    public ListEmployeeHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_list_employee, parent, false);
        return new ListEmployeeHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ListEmployeeHolder holder, int position) {
        Users users = mUsers.get(position);
        if (users == null) {
            return;
        }
        holder.name.setText("Name: " + users.getName());
        holder.email.setText("Email: " + users.getEmail());
        holder.phone.setText("Phone: " + users.getPhoneNumber());
        holder.role.setText("Role: " + users.getRole());



        String imageUrl = users.getImageUrl();
        if (imageUrl != null && !imageUrl.isEmpty()) {
            Picasso.get().load(imageUrl).into(holder.avatarImageView);
        } else {
            holder.avatarImageView.setImageResource(R.drawable.defaulavatar);
        }
    }

    @Override
    public int getItemCount() {
        if (mUsers != null) {
            return mUsers.size();
        }
        return 0;
    }

    public class ListEmployeeHolder extends RecyclerView.ViewHolder {
        TextView name, phone, email, role;
        ImageView avatarImageView;
        private ImageButton mButtonDelete;

        public ListEmployeeHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.item_name);
            phone = itemView.findViewById(R.id.item_phone);
            avatarImageView = itemView.findViewById(R.id.item_avatarImageView);
            email = itemView.findViewById(R.id.item_email);
            role = itemView.findViewById(R.id.item_role);
            mButtonDelete = itemView.findViewById(R.id.button_delete);
            mButtonDelete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mListener != null) {
                        int position = getAdapterPosition();
                        if (position != RecyclerView.NO_POSITION) {
                            mListener.onDeleteButtonClicked(position);
                        }
                    }
                }
            });
        }
    }
}
