package com.example.restaurantfinalproject.Adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.restaurantfinalproject.Model.Kitchen;
import com.example.restaurantfinalproject.Model.Table;
import com.example.restaurantfinalproject.R;

import java.util.List;

public class ListKetchinAdapter extends RecyclerView.Adapter<ListKetchinAdapter.ListKetchinHolder> {

    private List<Kitchen> KitchenList;
//    private ListKetchinAdapter.BookButtonClickListener mListener;
public ListKetchinAdapter(List<Kitchen> listket) {
    this.KitchenList = listket;

}
    @NonNull
    @Override
    public ListKetchinAdapter.ListKetchinHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_ketchin, parent, false);
        return new ListKetchinAdapter.ListKetchinHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ListKetchinAdapter.ListKetchinHolder holder, int position) {
        Kitchen ket = KitchenList.get(position);
        if (ket == null) {
            return;
        }

        holder.namefood.setText("Name: " + ket.getNamefood());
        holder.namestaff.setText("Time: " + ket.getUserName());
        holder.numbertabel.setText("Phone: " + ket.getNumberTable());;
        holder.des.setText("Description: " + ket.getDescription());
        holder.status.setText("Status: " + ket.getStastu());
    }

    @Override
    public int getItemCount() {
        if (KitchenList != null) {
            return KitchenList.size();
        }
        return 0;
    }

    public class ListKetchinHolder extends RecyclerView.ViewHolder {
        TextView namefood, namestaff, numbertabel, des, status;

        public ListKetchinHolder(@NonNull View itemView) {
            super(itemView);
            namefood = itemView.findViewById(R.id.item_ket_namefood);
            namestaff = itemView.findViewById(R.id.item_ket_namestaff);
            numbertabel = itemView.findViewById(R.id.item_Ket_numbertable);
            des = itemView.findViewById(R.id.item_Ket_des);
            status = itemView.findViewById(R.id.item_Ket_status);
            ImageButton mButtonketaccespt = itemView.findViewById(R.id.button_Ket_accept);
            ImageButton mButtonketcancle = itemView.findViewById(R.id.button_Ket_reject);
            mButtonketaccespt.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
//                        mListener.onRejectButtonClicked(position);
                    }
                }
            });

            mButtonketcancle.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
//                        mListener.onAcceptButtonClicked(position);
                    }
                }
            });
        }
    }
}
