package com.example.restaurantfinalproject.fragment;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.restaurantfinalproject.R;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FragmentChart extends Fragment {
    private DatabaseReference database;
    private BarChart barChart1, barChart2;
    private ValueEventListener listener1, listener2;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_chart, container, false);

        barChart1 = view.findViewById(R.id.firstBarChart);
        barChart2 = view.findViewById(R.id.secondBarChart);

        database = FirebaseDatabase.getInstance().getReference("payhis");

        listener1 = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Map<String, Double> userTotalPrices = new HashMap<>();

                // Browse through the database to calculate the total amount for each user
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    String userName = snapshot.child("userName").getValue(String.class);
                    Double totalPrice = snapshot.child("totalPrice").getValue(Double.class);

                    if (userName != null && totalPrice != null) {
                        userTotalPrices.put(userName, userTotalPrices.getOrDefault(userName, 0.0) + totalPrice);
                    }
                }

                // Convert data to BarEntries for MPAndroidChart
                List<BarEntry> entries = new ArrayList<>();
                List<String> labels = new ArrayList<>();
                List<Integer> colors = new ArrayList<>();
                int index = 0;

                int[] chartColors = {
                        Color.RED,
                        Color.BLUE,
                        Color.GREEN,
                        Color.YELLOW,
                        Color.MAGENTA,
                        Color.CYAN,
                        Color.LTGRAY,
                        Color.DKGRAY
                };

                for (Map.Entry<String, Double> entry : userTotalPrices.entrySet()) {
                    entries.add(new BarEntry(index, entry.getValue().floatValue()));
                    labels.add(entry.getKey());
                    colors.add(chartColors[index % chartColors.length]);
                    index++;
                }

                BarDataSet dataSet = new BarDataSet(entries, "Total amount paid by employee");
                dataSet.setColors(colors);

                BarData barData = new BarData(dataSet);

                barChart1.setData(barData);
                barChart1.getXAxis().setGranularity(1);
                barChart1.getXAxis().setValueFormatter(new IndexAxisValueFormatter(labels));
                barChart1.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);
                barChart1.invalidate(); // Refresh the chart to display data

                Description description = new Description();
                description.setText("Total amount paid by employee");
                description.setTextSize(12);
                description.setTextColor(Color.BLACK);
                barChart1.setDescription(description);
                barChart1.invalidate();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Handle errors
            }
        };

        listener2 = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Map<String, Double> totalPricesByDate  = new HashMap<>();

                // Browse through the database to calculate the total amount for each user on each date
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Double totalPrice = snapshot.child("totalPrice").getValue(Double.class);
                    String date = snapshot.child("date").getValue(String.class);

                    if (totalPrice != null && date != null) {
                        totalPricesByDate.put(date, totalPricesByDate.getOrDefault(date, 0.0) + totalPrice);
                    }
                }

                List<BarEntry> entries = new ArrayList<>();
                List<String> labels = new ArrayList<>();
                List<Integer> colors = new ArrayList<>();
                int index = 0;

                int[] chartColors = {
                        Color.RED,
                        Color.BLUE,
                        Color.GREEN,
                        Color.YELLOW,
                        Color.MAGENTA,
                        Color.CYAN,
                        Color.LTGRAY,
                        Color.DKGRAY
                };

                for (Map.Entry<String, Double> dateEntry : totalPricesByDate.entrySet()) {
                    entries.add(new BarEntry(index, dateEntry.getValue().floatValue()));
                    labels.add(dateEntry.getKey());
                    colors.add(chartColors[index % chartColors.length]);
                    index++;
                }

                BarDataSet dataSet = new BarDataSet(entries, "Total Amount by Date");
                dataSet.setColors(colors);

                BarData barData = new BarData(dataSet);

                barChart2.setData(barData);
                barChart2.getXAxis().setGranularity(1);
                barChart2.getXAxis().setValueFormatter(new IndexAxisValueFormatter(labels));
                barChart2.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);
                barChart2.invalidate(); // Refresh the chart to display data

                Description description = new Description();
                description.setText("Total Amount by Date");
                description.setTextSize(12);
                description.setTextColor(Color.BLACK);
                barChart2.setDescription(description);
                barChart2.invalidate();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Handle errors
            }
        };

        database.addValueEventListener(listener1);
        database.addValueEventListener(listener2);

        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        database.removeEventListener(listener1);
        database.removeEventListener(listener2);
        database.removeEventListener(listener2);

    }
}

