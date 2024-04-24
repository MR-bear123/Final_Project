package com.example.restaurantfinalproject.Adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.restaurantfinalproject.Model.Cart;
import com.example.restaurantfinalproject.Model.History;
import com.example.restaurantfinalproject.R;

import java.util.List;


public class ListHistoryAdapter extends RecyclerView.Adapter<ListHistoryAdapter.ListHistoryHolder>{
    private List<History> historyList;

    public ListHistoryAdapter(List<History> historyList) {
        this.historyList = historyList;
    }
    @NonNull
    @Override
    public ListHistoryAdapter.ListHistoryHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_list_history, parent, false);
        return new ListHistoryHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ListHistoryAdapter.ListHistoryHolder holder, int position) {
        History history = historyList.get(position);
        holder.userName.setText("Name Staff: " + history.getUserName());
        holder.totalPrice.setText(String.valueOf("Total price: "+ history.getTotalPrice()));
        holder.timestamp.setText("Time: "+history.getTimestamp());
        holder.date.setText("Date: "+history.getDate());
        holder.codeBill.setText("Code bill: "+history.getCodeBill());
        holder.tablenum.setText("Number Table: "+history.getNumberTable());

        List<Cart> cartList = history.getCartList();
        StringBuilder cartDetails = new StringBuilder();
        for (Cart cart : cartList) {
            cartDetails.append("Food: "+ cart.getNamefood()).append(" - ");
            cartDetails.append("Quantity: ").append(cart.getQuantity()).append("+");
            cartDetails.append("Price: ").append(cart.getPrice()).append("\n");
        }
        holder.cartList.setText(cartDetails.toString());
    }

    @Override
    public int getItemCount() {
        if (historyList != null) {
            return historyList.size();
        }
        return 0;
    }

    public class ListHistoryHolder extends RecyclerView.ViewHolder {
        public TextView userName;
        public TextView totalPrice;
        public TextView timestamp;
        public TextView date;
        public TextView codeBill;
        public TextView cartList;
        public TextView tablenum;
        public ListHistoryHolder(@NonNull View itemView) {
            super(itemView);
            userName = itemView.findViewById(R.id.userNameTextView);
            totalPrice = itemView.findViewById(R.id.totalPriceTextView);
            timestamp = itemView.findViewById(R.id.timestampTextView);
            date = itemView.findViewById(R.id.dateTextView);
            codeBill = itemView.findViewById(R.id.codeBillTextView);
            cartList = itemView.findViewById(R.id.cartListTextView);
            tablenum = itemView.findViewById(R.id.table);
        }
    }
}
