package com.example.barberbooking.Fragment;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.CalendarContract;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.example.barberbooking.Common.Common;
import com.example.barberbooking.Model.BookingInformation;
import com.example.barberbooking.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import dmax.dialog.SpotsDialog;
import io.paperdb.Paper;

public class BookingStep4Fragment extends Fragment {

    SimpleDateFormat simpleDateFormat;
    LocalBroadcastManager localBroadcastManager;
    Unbinder unbinder;

    AlertDialog dialog;

    @BindView(R.id.txt_booking_time)
    TextView txt_booking_time;
    @BindView(R.id.txt_booking_barber_name)
    TextView txt_booking_barber_name;
    @BindView(R.id.txt_booking_salon_name)
    TextView txt_booking_salon_name;
    @BindView(R.id.txt_salon_phone)
    TextView txt_salon_phone;
    @BindView(R.id.txt_salon_open_hours)
    TextView txt_salon_open_hours;
    @BindView(R.id.txt_booking_salon_address)
    TextView txt_booking_salon_address;
    @BindView(R.id.txt_booking_barber)
    TextView txt_booking_barber;

    @OnClick(R.id.btn_confirm)
    void confirmBooking(){

        dialog.show();
        //process Timestamp
        //ues timestamp to filter all booking with date is greater to day
        //for only display all future booking
        String startTime = Common.convertTimeSlotToString(Common.currentTimeSlot);
        String[] convertTime = startTime.split("-");
        //get start time : get 9:00
        String[] startTimeConvert = convertTime[0].split(":");
        int startHourInt = Integer.parseInt(startTimeConvert[0].trim()); //get 9
        int startMinInt = Integer.parseInt(startTimeConvert[1].trim());

        Calendar bookingDateWithoutHours = Calendar.getInstance();
        bookingDateWithoutHours.setTimeInMillis(Common.bookingDate.getTimeInMillis());
        bookingDateWithoutHours.set(Calendar.HOUR_OF_DAY, startHourInt);
        bookingDateWithoutHours.set(Calendar.MINUTE, startMinInt);

        //creat timestamp object and apply to BookingInformation
        Timestamp timestamp = new Timestamp(bookingDateWithoutHours.getTime());

        BookingInformation bookingInformation = new BookingInformation();
        bookingInformation.setCityBook(Common.city);

        bookingInformation.setTimestamp(timestamp);
        bookingInformation.setDone(false);

        bookingInformation.setBaberId(Common.currentBarber.getBarberId());
        bookingInformation.setBarberName(Common.currentBarber.getName());
        bookingInformation.setCustomerName(Common.currentUser.getName());
        bookingInformation.setCustomerPhone(Common.currentUser.getPhoneNumber());
        bookingInformation.setSalonAdress(Common.currentSalon.getAddress());
        bookingInformation.setSalonId(Common.currentSalon.getSalonId());
        bookingInformation.setSalonName(Common.currentSalon.getName());
        bookingInformation.setTime(new StringBuilder(Common.convertTimeSlotToString(Common.currentTimeSlot))
                .append(" at ")
                .append(simpleDateFormat.format(bookingDateWithoutHours.getTime())).toString());
        bookingInformation.setSlot(Long.valueOf(Common.currentTimeSlot));

        //submit to barber document
        DocumentReference bookingDate = FirebaseFirestore.getInstance()
                .collection("AllSalon")
                .document(Common.city)
                .collection("Branch")
                .document(Common.currentSalon.getSalonId())
                .collection("Barber")
                .document(Common.currentBarber.getBarberId())
                .collection(Common.simpleDateFormat.format(Common.bookingDate.getTime()))
                .document(String.valueOf(Common.currentTimeSlot));

        //write data
        bookingDate.set(bookingInformation)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        //here we can write an function to check
                        //if already exist an booking, we will prevent new booking
                        addToUserBooking(bookingInformation);
                    }

                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void addToUserBooking(final BookingInformation bookingInformation) {


        //first, creat new Collection
        final CollectionReference userBooking = FirebaseFirestore.getInstance()
                .collection("Users")
                .document(Common.currentUser.getPhoneNumber())
                .collection("Booking");

        //check if exist document in this collection

        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DATE,0);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);

        Timestamp toDayTimeStamp = new Timestamp(calendar.getTime());

        userBooking.whereGreaterThanOrEqualTo("timestamp", toDayTimeStamp).whereEqualTo("done", false)//if have any document with field done = false
    .get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if(task.getResult().isEmpty()){
                    //set data
                    userBooking.document()
                            .set(bookingInformation)
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    if(dialog.isShowing())
                                        dialog.dismiss();
                                    addToCalendar(Common.bookingDate, Common.convertTimeSlotToString(Common.currentTimeSlot));
                                    resetStaticData();
                                    getActivity().finish();
                                    Toast.makeText(getContext(), "Success", Toast.LENGTH_SHORT).show();
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            if(dialog.isShowing())
                                dialog.dismiss();
                            Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
                }else {
                    if(dialog.isShowing())
                        dialog.dismiss();
                    resetStaticData();
                    getActivity().finish();
                    Toast.makeText(getContext(), "Success", Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

    private void addToCalendar(Calendar bookingDate, String startDate) {
        String startTime = Common.convertTimeSlotToString(Common.currentTimeSlot);
        String[] convertTime = startTime.split("-");
        //get start time : get 9:00
        String[] startTimeConvert = convertTime[0].split(":");
        int startHourInt = Integer.parseInt(startTimeConvert[0].trim()); //get 9
        int startMinInt = Integer.parseInt(startTimeConvert[1].trim()); //get 00

        String[] endTimeConvert = convertTime[1].split(":");
        int endHourInt = Integer.parseInt(endTimeConvert[0].trim()); //get 9
        int endMinInt = Integer.parseInt(endTimeConvert[1].trim()); //get 00

        Calendar startEvent = Calendar.getInstance();
        startEvent.setTimeInMillis(bookingDate.getTimeInMillis());
        startEvent.set(Calendar.HOUR_OF_DAY, startHourInt);//set event start hour
        startEvent.set(Calendar.MINUTE, startMinInt); //set event start min

        Calendar endEvent = Calendar.getInstance();
        endEvent.setTimeInMillis(bookingDate.getTimeInMillis());
        endEvent.set(Calendar.HOUR_OF_DAY, endHourInt);//set event start hour
        endEvent.set(Calendar.MINUTE, endMinInt); //set event start min

        //after we have startEvent and andEvent, convert it ti format string
        SimpleDateFormat calendarDateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm");
        String startEventTime = calendarDateFormat.format(startEvent.getTime());
        String endEventTime = calendarDateFormat.format(endEvent.getTime());

        addToDeviceCalendar(startEventTime, endEventTime, "Haircut Booking", 
                new StringBuilder("Haircut from ")
        .append(startTime)
        .append(" with ")
        .append(Common.currentBarber.getName())
        .append(" at ")
        .append(Common.currentSalon.getName()).toString(),
                new StringBuilder("Address: ").append(Common.currentSalon.getAddress()).toString());
    }

    private void addToDeviceCalendar(String startEventTime, String endEventTime, String title, String description, String location) {
        SimpleDateFormat calendarDateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm");

        try{
            Date start = calendarDateFormat.parse(startEventTime);
            Date end = calendarDateFormat.parse(endEventTime);

            ContentValues event = new ContentValues();

            //put
            event.put(CalendarContract.Events.CALENDAR_ID, getCalendar(getContext()));
            event.put(CalendarContract.Events.TITLE, title);
            event.put(CalendarContract.Events.DESCRIPTION, description);
            event.put(CalendarContract.Events.EVENT_LOCATION, location);

            //Time
            event.put(CalendarContract.Events.DTSTART, start.getTime());
            event.put(CalendarContract.Events.DTEND, end.getTime());
            event.put(CalendarContract.Events.ALL_DAY, 0);
            event.put(CalendarContract.Events.HAS_ALARM, 1);

            String timeZone = TimeZone.getDefault().getID();
            event.put(CalendarContract.Events.EVENT_TIMEZONE, timeZone);

            Uri calendars;
            if(Build.VERSION.SDK_INT >= 8)
                calendars = Uri.parse("content://com.android.calendar/events");
            else
                calendars = Uri.parse("content://calendar/events");
            Uri uri_save = getActivity().getContentResolver().insert(calendars, event);
            //Save to cache
            Paper.init(getActivity());
            Paper.book().write(Common.EVENT_URI_CACHE, uri_save.toString());

        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    private String getCalendar(Context context) {
        //get default calendar ID of Calendar of Gmail
        String gmailIdCalendar = "";
        String projection[] = {"_id", "calendar_displayName"};
        Uri calendar = Uri.parse("content://com.android.calendar/calendars");

        ContentResolver contentResolver = context.getContentResolver();
        Cursor managedCursor = contentResolver.query(calendar, projection, null, null, null);
        if(managedCursor.moveToFirst()){
            String calName;
            int nameCol = managedCursor.getColumnIndex(projection[1]);
            int idCol = managedCursor.getColumnIndex(projection[0]);
            do{
                calName = managedCursor.getString(nameCol);
                if(calName.contains("@gmail.com")){
                    gmailIdCalendar = managedCursor.getString(idCol);
                    break;
                }
            }while (managedCursor.moveToNext());
            managedCursor.close();
        }
        return gmailIdCalendar;
    }

    private void resetStaticData() {
        Common.step = 0;
        Common.currentTimeSlot = -1;
        Common.currentSalon = null;
        Common.currentBarber = null;
        Common.bookingDate.add(Calendar.DATE, 0);
    }

    BroadcastReceiver confirmBookingReceive = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            setData();
        }
    };

    private void setData() {
        txt_booking_barber_name.setText(Common.currentBarber.getName());
        txt_booking_time.setText(new StringBuilder(Common.convertTimeSlotToString(Common.currentTimeSlot))
        .append(" at ")
        .append(simpleDateFormat.format(Common.bookingDate.getTime())));

        txt_booking_salon_address.setText(Common.currentSalon.getAddress());
        txt_salon_phone.setText(Common.currentSalon.getPhone());
        txt_salon_open_hours.setText(Common.currentSalon.getOpenHours());
        txt_booking_salon_name.setText(Common.currentSalon.getName());
        txt_booking_barber.setText(Common.currentBarber.getName());
    }

    static BookingStep4Fragment instance;
    public static BookingStep4Fragment getInstance() {
        if(instance == null)
            instance = new BookingStep4Fragment();
        return instance;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        simpleDateFormat = new SimpleDateFormat("dd/MM/yyyy");
        localBroadcastManager = LocalBroadcastManager.getInstance(getContext());
        localBroadcastManager.registerReceiver(confirmBookingReceive, new IntentFilter(Common.KEY_CONFIRM_BOOKING));

        dialog = new SpotsDialog.Builder().setContext(getContext()).setCancelable(false).build();
    }

    @Override
    public void onDestroy() {
        localBroadcastManager.unregisterReceiver(confirmBookingReceive);
        super.onDestroy();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View itemView = inflater.inflate(R.layout.fragment_booking_step_4, container, false);
        unbinder = ButterKnife.bind(this, itemView);

        return itemView;
    }
}
