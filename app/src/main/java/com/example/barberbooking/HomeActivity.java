package com.example.barberbooking;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.example.barberbooking.Common.Common;
import com.example.barberbooking.Fragment.HomeFragment;
import com.example.barberbooking.Fragment.ShoppingFragment;
import com.example.barberbooking.Model.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import butterknife.BindView;
import butterknife.ButterKnife;

public class HomeActivity extends AppCompatActivity {

    @BindView(R.id.bottom_navigation)
    BottomNavigationView bottomNavigationView;

    BottomSheetDialog bottomSheetDialog;

    CollectionReference userRef;

    FirebaseFirestore firebaseFirestore;

    AlertDialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        ButterKnife.bind(HomeActivity.this);

        firebaseFirestore = FirebaseFirestore.getInstance();
        bottomNavigationView = findViewById(R.id.bottom_navigation);

        userRef = FirebaseFirestore.getInstance().collection("Users");

        if(getIntent() != null){
            boolean isLogin = getIntent().getBooleanExtra(Common.IS_LOGIN, false);
            if(isLogin){
//                dialog.show();
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

                DocumentReference currentUser = userRef.document(user.getPhoneNumber());
                currentUser.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if(task.isSuccessful()){
                            DocumentSnapshot userSnapShot = task.getResult();
                            if (!userSnapShot.exists())
                            {
                                showUpdateDialog(user.getPhoneNumber());
                            }
                            else
                            {
                                //if user already available in our system
                                Common.currentUser = userSnapShot.toObject(User.class);
                                bottomNavigationView.setSelectedItemId(R.id.action_home);
                            }
                            //if (dialog.isShowing())
                                //dialog.dismiss();
                        }
                    }
                });
            }
        }

        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            Fragment fragment = null;
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                if (menuItem.getItemId() == R.id.action_home)
                    fragment = new HomeFragment();
                else if (menuItem.getItemId() == R.id.action_shopping)
                    fragment = new ShoppingFragment();
                return loadFragment(fragment);
            }
        });
    }

    private boolean loadFragment(Fragment fragment) {
        if (fragment != null)
        {
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,fragment)
                    .commit();
            return true;
        }
        return false;
    }

    private void showUpdateDialog(String phoneNumber) {
        bottomSheetDialog = new BottomSheetDialog(this);
        bottomSheetDialog.setCanceledOnTouchOutside(false);
        //bottomSheetDialog.setTitle("One more step!");
        bottomSheetDialog.setCancelable(false);
        View sheetView = getLayoutInflater().inflate(R.layout.layout_update_information,null);

        Button btn_update = sheetView.findViewById(R.id.btn_update);
        final TextInputEditText edt_name = sheetView.findViewById(R.id.edt_name);
        final TextInputEditText edt_address = sheetView.findViewById(R.id.edt_address);

        //update information
        btn_update.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final User user = new User(edt_name.getText().toString(),
                        edt_address.getText().toString(),
                        phoneNumber);
                userRef.document(phoneNumber).set(user).
                        addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                bottomSheetDialog.dismiss();
//                                if (dialog.isShowing())
//                                    dialog.dismiss();
                                Common.currentUser = user;
                                bottomNavigationView.setSelectedItemId(R.id.action_home);
                                Toast.makeText(HomeActivity.this, "Thank You", Toast.LENGTH_SHORT).show();
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        bottomSheetDialog.dismiss();
                        Toast.makeText(HomeActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });

        bottomSheetDialog.setContentView(sheetView);
        bottomSheetDialog.show();
    }
}
