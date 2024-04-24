package com.example.restaurantfinalproject.Adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.restaurantfinalproject.Model.Cart;
import com.example.restaurantfinalproject.R;
import com.squareup.picasso.Picasso;

import java.util.List;

public class ListCartAdapter extends RecyclerView.Adapter<ListCartAdapter.ListCartHolder>{
    private List<Cart> mCarts;
    private ListCartAdapter.CartButtonClickListener mListener;
    public interface CartButtonClickListener {
        void onDeleteCartButtonClicked(int position);

//        void repload(int position);
        void onUpdateCartButtonClicked(int position);

    }
    public ListCartAdapter(List<Cart> mCarts, ListCartAdapter.CartButtonClickListener listener) {
        this.mCarts = mCarts;
        this.mListener = listener;
    }
    @NonNull
    @Override
    public ListCartHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_list_cart, parent, false);
        return new ListCartHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ListCartAdapter.ListCartHolder holder, int position) {
        Cart cart = mCarts.get(position);
        if (cart != null) {
            holder.namestaff.setText("Name: " + cart.getNameStaff());
            holder.namefood.setText("Name Food: " + cart.getNamefood());
            holder.price.setText("Price: " + cart.getPrice() + "$");
            holder.qua.setText("Quanlity: " + cart.getQuantity());
            holder.textItemPrice.setText("Price to the dish: " + (cart.getPrice() * cart.getQuantity()));
            String imageUrl = cart.getImageUrl();
            if (imageUrl != null && !imageUrl.isEmpty()) {
                Picasso.get().load(imageUrl).into(holder.avatarImageViewcart);
            } else {
                holder.avatarImageViewcart.setImageResource(R.drawable.defaulavatar);
            }
        }
    }


    @Override
    public int getItemCount() {
        if (mCarts != null) {
            return mCarts.size();
        }
        return 0;
    }

    public class ListCartHolder extends RecyclerView.ViewHolder {
        TextView namestaff, namefood, price, qua, textItemPrice;
        ImageView avatarImageViewcart;
        private ImageButton mButtonCartDelete, mButtonCartUpdate;
        public ListCartHolder(@NonNull View itemView) {
            super(itemView);

            namestaff = itemView.findViewById(R.id.item_cart_staffname);
            namefood = itemView.findViewById(R.id.item_cart_namefood);
            price = itemView.findViewById(R.id.item_cart_meprice);
            qua = itemView.findViewById(R.id.item_cart_quanlity);
            avatarImageViewcart = itemView.findViewById(R.id.cart_item_avatarImageView);
            mButtonCartUpdate = itemView.findViewById(R.id.button_Cart_update);
            mButtonCartDelete = itemView.findViewById(R.id.button_cart_delete);
            textItemPrice = itemView.findViewById(R.id.text_item_price);

            mButtonCartDelete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mListener != null) {
                        int position = getAdapterPosition();
                        if (position != RecyclerView.NO_POSITION) {
                            mListener.onDeleteCartButtonClicked(position);
                        }
                    }
                }
            });

            mButtonCartUpdate.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mListener != null) {
                        int position = getAdapterPosition();
                        if (position != RecyclerView.NO_POSITION) {
                            mListener.onUpdateCartButtonClicked(position);
                        }
                    }
                }
            });
        }
    }
}
