package com.example.restaurantfinalproject.Adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.restaurantfinalproject.Model.Table;
import com.example.restaurantfinalproject.R;

import java.util.List;

public class ListWarehousesAdapter extends RecyclerView.Adapter<ListWarehousesAdapter.ListWarehousesHolder>{
    private List<Table> mTable;
    private ListWarehousesAdapter.BookButtonClickListener mListener;
    public interface BookButtonClickListener {
        void onDeleteBookButtonClicked(int position);
        void onUpdateBookButtonClicked(int position);

        void onAcceptButtonClicked(int position);
        void onRejectButtonClicked(int position);
    }

    public ListWarehousesAdapter(List<Table> listTable, BookButtonClickListener mListener) {
        this.mTable = listTable;
        this.mListener = mListener;
    }
    @NonNull
    @Override
    public ListWarehousesAdapter.ListWarehousesHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_warehoues, parent, false);
        return new ListWarehousesAdapter.ListWarehousesHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ListWarehousesAdapter.ListWarehousesHolder holder, int position) {
        Table table = mTable.get(position);
        if (table == null) {
            return;
        }

        holder.name.setText("Name: " + table.getCuname());
        holder.time.setText("Time: " + table.getTime());
        holder.phone.setText("Phone: " + table.getCuphone());
        holder.date.setText("Date: " + table.getDate());
        holder.des.setText("Description: " + table.getDescription());
        holder.code.setText("Desk Code: " + table.getRandomCode());
        holder.status.setText("Status: " + table.getStatus());

    }

    @Override
    public int getItemCount() {
        if (mTable != null) {
            return mTable.size();
        }
        return 0;
    }

    public class ListWarehousesHolder extends RecyclerView.ViewHolder {
        TextView name, phone, time, date, des, code, status;
        private ImageButton mButtonBookDelete, mButtonBookUpdate,mButtonBookaccespt,mButtonBookcancle;
        public ListWarehousesHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.item_Book_name);
            phone = itemView.findViewById(R.id.item_Book_phone);
            time = itemView.findViewById(R.id.item_Book_time);
            date = itemView.findViewById(R.id.item_Book_date);
            des = itemView.findViewById(R.id.item_Book_des);
            code = itemView.findViewById(R.id.item_Book_Code);
            status = itemView.findViewById(R.id.item_Book_status);
            mButtonBookUpdate = itemView.findViewById(R.id.button_Book_update);
            mButtonBookDelete = itemView.findViewById(R.id.button_Book_delete);
            mButtonBookaccespt = itemView.findViewById(R.id.button_Book_accept);
            mButtonBookcancle = itemView.findViewById(R.id.button_Book_reject);
            mButtonBookDelete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mListener != null) {
                        int position = getAdapterPosition();
                        if (position != RecyclerView.NO_POSITION) {
                            mListener.onDeleteBookButtonClicked(position);
                        }
                    }
                }
            });

            mButtonBookUpdate.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mListener != null) {
                        int position = getAdapterPosition();
                        if (position != RecyclerView.NO_POSITION) {
                            mListener.onUpdateBookButtonClicked(position);
                        }
                    }
                }
            });

            mButtonBookcancle.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mListener != null) {
                        int position = getAdapterPosition();
                        if (position != RecyclerView.NO_POSITION) {
                            mListener.onRejectButtonClicked(position);
                        }
                    }
                }
            });

            mButtonBookaccespt.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mListener != null) {
                        int position = getAdapterPosition();
                        if (position != RecyclerView.NO_POSITION) {
                            mListener.onAcceptButtonClicked(position);
                        }
                    }
                }
            });
        }
    }
}
