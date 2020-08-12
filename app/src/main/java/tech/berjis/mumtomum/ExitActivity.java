package tech.berjis.mumtomum;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.NumberFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

public class ExitActivity extends AppCompatActivity {

    FirebaseAuth mAuth;
    DatabaseReference dbRef, withdrawRef, transactionRef;

    TextView amountNumber, currency, username, groupName, withdraw, exit;
    CircleImageView userimage, groupImage;
    String country = "", g_name = "", g_code = "", g_symbol = "", UID, amount, group_id;
    long balance = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_exit);

        initLayouts();
        loadGroupData();
        loadUserDetails();
        setMyContribution();
        staticOnClicks();
    }

    private void initLayouts() {
        mAuth = FirebaseAuth.getInstance();
        dbRef = FirebaseDatabase.getInstance().getReference();
        dbRef.keepSynced(true);

        UID = mAuth.getCurrentUser().getUid();

        currency = findViewById(R.id.currency);
        username = findViewById(R.id.username);
        userimage = findViewById(R.id.userImage);
        amountNumber = findViewById(R.id.amount);
        groupName = findViewById(R.id.groupName);
        groupImage = findViewById(R.id.groupImage);
        withdraw = findViewById(R.id.withdraw);
        exit = findViewById(R.id.exit);
    }

    private void staticOnClicks() {
        withdraw.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startWithdraw();
            }
        });
        exit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                exitGroup();
            }
        });
    }

    private void loadGroupData() {
        Intent g_i = getIntent();
        Bundle g_b = g_i.getExtras();
        group_id = g_b.getString("group_id");

        dbRef.child("Groups").child(group_id).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                String symbol = Objects.requireNonNull(snapshot.child("symbol").getValue()).toString();
                String code = Objects.requireNonNull(snapshot.child("code").getValue()).toString();
                String name = Objects.requireNonNull(snapshot.child("name").getValue()).toString();
                String logo = Objects.requireNonNull(snapshot.child("logo").getValue()).toString();

                g_name = name;
                g_code = code;
                g_symbol = symbol;

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    currency.setText(Html.fromHtml(symbol + " <small>(" + code + ")</small>", Html.FROM_HTML_MODE_COMPACT));
                } else {
                    currency.setText(Html.fromHtml(symbol + " <small>(" + code + ")</small>"));
                }
                groupName.setText(name);
                Glide.with(ExitActivity.this).load(logo).thumbnail(0.25f).into(groupImage);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void loadUserDetails() {
        dbRef.child("Users").child(UID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String name = Objects.requireNonNull(snapshot.child("name").getValue()).toString();
                String image = Objects.requireNonNull(snapshot.child("image").getValue()).toString();
                country = Objects.requireNonNull(snapshot.child("country_code").getValue()).toString();

                username.setText(name);
                if (!image.equals("")) {
                    Glide.with(ExitActivity.this).load(image).thumbnail(0.25f).into(userimage);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void setMyContribution() {
        dbRef.child("GroupWallet").child(group_id).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists() && dataSnapshot.hasChildren()) {
                    long total = 0;
                    Long value;

                    for (DataSnapshot npsnapshot : dataSnapshot.getChildren()) {
                        if (!npsnapshot.child("status").exists() && npsnapshot.child("text_ref").exists()) {
                            String text_ref = Objects.requireNonNull(npsnapshot.child("text_ref").getValue()).toString();
                            long end_time = System.currentTimeMillis() / 1000L;
                            dbRef.child("GroupWallet").child(group_id).child(text_ref).child("end_time").setValue(end_time);
                            dbRef.child("GroupWallet").child(group_id).child(text_ref).child("status").setValue("cancelled");
                        }
                        if (npsnapshot.child("status").exists() && Objects.equals(npsnapshot.child("status").getValue(), "success") && Objects.equals(npsnapshot.child("user").getValue(), UID)) {
                            value = (Long) npsnapshot.child("amount").getValue();

                            if (Objects.equals(npsnapshot.child("type").getValue(), "deposit")) {
                                total = total + value;
                            } else {
                                total = total - value;
                            }
                        }
                    }

                    balance = total;

                    NumberFormat nf = NumberFormat.getInstance();
                    nf.setMinimumFractionDigits(0);
                    nf.setMaximumFractionDigits(0);
                    String output = nf.format(total);

                    amountNumber.setText(output);
                } else {

                    amountNumber.setText("0");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void startWithdraw() {
        amount = amountNumber.getText().toString();

        long maxWithdraw = balance - 50;
        final long withDrawAmount = Long.parseLong(amount);
        if (withDrawAmount < 500) {
            new AlertDialog.Builder(ExitActivity.this)
                    .setTitle("GroupWallet Withdrawal")
                    .setMessage("Minimum withdrawal amount is kshs 500")
                    .setPositiveButton("Ok", null)
                    .show();
            return;
        }
        if (Long.parseLong(amount) > maxWithdraw) {
            new AlertDialog.Builder(ExitActivity.this)
                    .setTitle("GroupWallet Withdrawal")
                    .setMessage("You cannot withdraw more than kshs " + maxWithdraw)
                    .setPositiveButton("Ok", null)
                    .show();
            return;
        }
        if (Long.parseLong(amount) <= maxWithdraw && withDrawAmount >= 500) {
            new AlertDialog.Builder(ExitActivity.this)
                    .setTitle("GroupWallet Withdrawal")
                    .setMessage("You are about to withdraw " + amount + " from your GroupWallet account.\n\nTransaction fees kshs 50")
                    .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            checkWithdrawViability(withDrawAmount);
                        }
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        }
    }

    public void checkWithdrawViability(final Long withdrawAmount) {
        dbRef.child("Users").child(UID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                final String firstname = dataSnapshot.child("first_name").getValue().toString();
                final String lastname = dataSnapshot.child("last_name").getValue().toString();
                final String phone_number = dataSnapshot.child("regs_phone").getValue().toString();

                new AlertDialog.Builder(ExitActivity.this)
                        .setTitle("GroupWallet Withdrawal")
                        .setMessage("Withdraw " + withdrawAmount + " to " + phone_number + " ?")
                        .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                withdrawRequest(firstname, lastname, phone_number, withdrawAmount);
                            }
                        })
                        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {

                            }
                        })
                        .show();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void withdrawRequest(final String firstname, final String lastname, final String phone_number, final Long withdrawAmount) {
        withdrawRef = dbRef.child("GroupWallet").child(UID).push();
        final String reference = withdrawRef.getKey();

        transactionRef = dbRef.child("Transactions").push();
        final String transaction_code = transactionRef.getKey();

        long time_start = System.currentTimeMillis() / 1000L;
        final String narration = "Withdrawal from " + firstname + " " + lastname + "'s GroupWallet";

        withdrawRef.child("time_start").setValue(time_start);
        withdrawRef.child("user").setValue(UID);
        withdrawRef.child("group").setValue(group_id);
        withdrawRef.child("type").setValue("withdraw");
        withdrawRef.child("narration").setValue(narration);
        withdrawRef.child("amount").setValue(withdrawAmount + 50);
        withdrawRef.child("text_ref").setValue(reference);

        transactionRef.child("time_start").setValue(time_start);
        transactionRef.child("user").setValue(UID);
        withdrawRef.child("group").setValue(group_id);
        transactionRef.child("type").setValue("withdraw");
        transactionRef.child("narration").setValue("GroupWallet");
        transactionRef.child("amount").setValue(withdrawAmount + 50);
        transactionRef.child("text_ref").setValue(transaction_code);

        String requestUrl = "https://mumwallet.herokuapp.com/api/request_withdrawal";
        StringRequest stringRequest = new StringRequest(Request.Method.POST, requestUrl, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.e("Volley Result", "" + response);
                //Toast.makeText(BabyActivity.this, response, Toast.LENGTH_SHORT).show();
                long end_time = System.currentTimeMillis() / 1000L;
                withdrawRef.child("end_time").setValue(end_time);
                withdrawRef.child("status").setValue("success");

                transactionRef.child("end_time").setValue(end_time);
                transactionRef.child("status").setValue("success");
                Toast.makeText(ExitActivity.this, "You have succesfully withdrawn kshs " + withdrawAmount, Toast.LENGTH_LONG).show();
                startActivity(new Intent(ExitActivity.this, WalletActivity.class));
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
                long end_time = System.currentTimeMillis() / 1000L;
                withdrawRef.child("end_time").setValue(end_time);
                withdrawRef.child("status").setValue("error");

                transactionRef.child("end_time").setValue(end_time);
                transactionRef.child("status").setValue("error");
                Toast.makeText(ExitActivity.this, "Error processing your withdrawal", Toast.LENGTH_LONG).show();
            }
        }) {

            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> postMap = new HashMap<>();
                postMap.put("sender", "mumtomum");
                postMap.put("account_number", phone_number);
                postMap.put("amount", amount);
                postMap.put("narration", narration);
                postMap.put("reference", reference);
                postMap.put("beneficiary_name", firstname + " " + lastname);
                return postMap;
            }
        };

        Volley.newRequestQueue(ExitActivity.this).add(stringRequest);
    }

    private void exitGroup() {
        new AlertDialog.Builder(ExitActivity.this)
                .setTitle("Confirm")
                .setMessage("Are you sure you want to exit " + g_name + "?")
                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        approveWithdrawal();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void approveWithdrawal() {
        dbRef.child("GroupMembers").child(group_id).child(UID).child("status").setValue(0);
        dbRef.child("MyGroups").child(UID).child(group_id).child("status").setValue(0);
        Toast.makeText(this, "Successfully left " + g_name, Toast.LENGTH_LONG).show();
        startActivity(new Intent(ExitActivity.this, GroupsActivity.class));
    }
}
