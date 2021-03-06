package com.example.barberbooking.Adapter;

import android.content.Context;
import android.content.Intent;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.barberbooking.Common.Common;
import com.example.barberbooking.Interface.IRecyclerItemSelectedListener;
import com.example.barberbooking.Model.TimeSlot;
import com.example.barberbooking.R;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class MyTimeSlotAdapter extends RecyclerView.Adapter<MyTimeSlotAdapter.MyViewHolder> {

    Context context;
    List<TimeSlot> timeSlotList;
    List<CardView> cardViewList;
    LocalBroadcastManager localBroadcastManager;

    public MyTimeSlotAdapter(Context context) {
        this.context = context;
        this.timeSlotList = new ArrayList<>();
        this.localBroadcastManager = LocalBroadcastManager.getInstance(context);
        cardViewList = new ArrayList<>();
    }

    public MyTimeSlotAdapter(Context context, List<TimeSlot> timeSlotList) {
        this.context = context;
        this.timeSlotList = timeSlotList;
        this.localBroadcastManager = LocalBroadcastManager.getInstance(context);
        cardViewList = new ArrayList<>();
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(context).inflate(R.layout.layout_time_slot, parent, false);
        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        holder.txt_time_slot.setText(new StringBuilder(Common.convertTimeSlotToString(position)));

        String[] convertTime = holder.txt_time_slot.getText().toString().split("-");

        String[] startTimeConvert = convertTime[0].split(":");
        int hourInt = Integer.parseInt(startTimeConvert[0].trim());
        int minInt = Integer.parseInt(startTimeConvert[1].trim());

        if (timeSlotList.size() == 0) {
            if (Calendar.getInstance().getTime().getDate() == Common.bookingDate.getTime().getDate() &&
                    (hourInt < Calendar.getInstance().getTime().getHours() ||
                            (hourInt == Calendar.getInstance().getTime().getHours() && minInt < Calendar.getInstance().getTime().getMinutes()))) {
                holder.card_time_slot.setEnabled(false);
                holder.card_time_slot.setTag(Common.DISABLE_TAG);

                holder.card_time_slot.setCardBackgroundColor(context.getResources().getColor(android.R.color.darker_gray));

                holder.txt_time_slot_description.setText("Not available");
                holder.txt_time_slot_description.setTextColor(context.getResources().getColor(android.R.color.white));
                holder.txt_time_slot.setTextColor(context.getResources().getColor(android.R.color.white));
            }
            else {
                holder.card_time_slot.setCardBackgroundColor(context.getResources().getColor(android.R.color.white));

                holder.txt_time_slot_description.setText("Available");
                holder.txt_time_slot_description.setTextColor(context.getResources().getColor(android.R.color.black));
                holder.txt_time_slot.setTextColor(context.getResources().getColor(android.R.color.black));
            }
        } else { //if have time slot is full(booked)

            for (TimeSlot slotValue : timeSlotList) {
                //loop all time from server and ser different color
                int slot = Integer.parseInt(slotValue.getSlot().toString());
                if (slot == position) {
                    holder.card_time_slot.setEnabled(false);
                    holder.card_time_slot.setTag(Common.DISABLE_TAG);

                    holder.card_time_slot.setCardBackgroundColor(context.getResources().getColor(android.R.color.darker_gray));

                    holder.txt_time_slot_description.setText("Full");
                    holder.txt_time_slot_description.setTextColor(context.getResources().getColor(android.R.color.white));
                    holder.txt_time_slot.setTextColor(context.getResources().getColor(android.R.color.white));
                }
                else if (Calendar.getInstance().getTime().getDate() == Common.bookingDate.getTime().getDate() &&
                        (hourInt < Calendar.getInstance().getTime().getHours() ||
                                (hourInt == Calendar.getInstance().getTime().getHours() && minInt < Calendar.getInstance().getTime().getMinutes()))) {
                    holder.card_time_slot.setEnabled(false); //b xem t lấy giờ phút hiện tại có đúng k
                    holder.card_time_slot.setTag(Common.DISABLE_TAG);

                    holder.card_time_slot.setCardBackgroundColor(context.getResources().getColor(android.R.color.darker_gray));

                    holder.txt_time_slot_description.setText("Not available");
                    holder.txt_time_slot_description.setTextColor(context.getResources().getColor(android.R.color.white));
                    holder.txt_time_slot.setTextColor(context.getResources().getColor(android.R.color.white));
                }
            }
        }
        //add all card to list, no add card already in cardViewList
        if(!cardViewList.contains(holder.card_time_slot))
            cardViewList.add(holder.card_time_slot);

        //Check if card time slot is available
        holder.setiRecyclerItemSelectedListener(new IRecyclerItemSelectedListener() {
            @Override
            public void onItemSelectedListener(View view, int position) {
                //loop all card in card list
                for(CardView cardView:cardViewList){
                    if(cardView.getTag() == null)
                        cardView.setCardBackgroundColor(context.getResources().getColor(android.R.color.white));
                }
                holder.card_time_slot.setCardBackgroundColor(context.getResources().getColor(android.R.color.holo_orange_dark));

                //after that send broadcast to enable button next
                Intent intent = new Intent(Common.KEY_ENABLE_BUTTON_NEXT);
                intent.putExtra(Common.KEY_TIME_SLOT, position); //put index of time slot we have selected
                intent.putExtra(Common.KEY_STEP, 3);
                localBroadcastManager.sendBroadcast(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return Common.TIME_SLOT_TOTAL;
    }

    public class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView txt_time_slot, txt_time_slot_description;
        CardView card_time_slot;

        IRecyclerItemSelectedListener iRecyclerItemSelectedListener;

        public void setiRecyclerItemSelectedListener(IRecyclerItemSelectedListener iRecyclerItemSelectedListener) {
            this.iRecyclerItemSelectedListener = iRecyclerItemSelectedListener;
        }

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);

            txt_time_slot = itemView.findViewById(R.id.txt_time_slot);
            txt_time_slot_description = itemView.findViewById(R.id.txt_time_slot_description);
            card_time_slot = itemView.findViewById(R.id.card_time_slot);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            iRecyclerItemSelectedListener.onItemSelectedListener(view, getAdapterPosition());
        }
    }
}
