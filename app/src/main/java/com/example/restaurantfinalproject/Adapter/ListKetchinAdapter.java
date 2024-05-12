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
    private ListKetchinAdapter.KetButtonClickListener mListener;
    public interface KetButtonClickListener{
        void onAcceptButtonClicked(int position);
        void onRejectButtonClicked(int position);
    }
public ListKetchinAdapter(List<Kitchen> listket, KetButtonClickListener listener) {
    this.KitchenList = listket;
    this.mListener = listener;

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

        holder.namefood.setText("Fodd: " + ket.getNamefood());
        holder.namestaff.setText("Staff: " + ket.getUserName());
        holder.quanlity.setText("Quanlity: "+ket.getQuanlity());
        holder.numbertabel.setText("Number Table: " + ket.getNumberTable());;
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
        TextView namefood, namestaff, numbertabel, quanlity, des, status;

        public ListKetchinHolder(@NonNull View itemView) {
            super(itemView);
            namefood = itemView.findViewById(R.id.item_ket_namefood);
            namestaff = itemView.findViewById(R.id.item_ket_namestaff);
            quanlity = itemView.findViewById(R.id.item_ket_quanlity);
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
                        mListener.onAcceptButtonClicked(position);
                    }
                }
            });

            mButtonketcancle.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        mListener.onRejectButtonClicked(position);
                    }
                }
            });
        }
    }
}
