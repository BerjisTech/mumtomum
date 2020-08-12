package tech.berjis.mumtomum;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
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

public class WithdrawActivity extends AppCompatActivity {

    FirebaseAuth mAuth;
    DatabaseReference dbRef, withdrawRef, transactionRef;
    ;

    EditText amountNumber;
    TextView currency, username, withdraw;
    CircleImageView userimage;
    String country = "", c_name = "", c_code = "", c_symbol = "", UID, amount, balance;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_withdraw);

        initLayouts();
        totalBalance();
        loadUserDetails();
        staticOnClicks();
    }

    private void initLayouts() {
        mAuth = FirebaseAuth.getInstance();
        dbRef = FirebaseDatabase.getInstance().getReference();
        dbRef.keepSynced(true);

        UID = mAuth.getCurrentUser().getUid();

        currency = findViewById(R.id.currency);
        username = findViewById(R.id.username);
        userimage = findViewById(R.id.userimage);
        amountNumber = findViewById(R.id.amount);
        withdraw = findViewById(R.id.withdraw);
    }

    private void loadUserDetails() {
        dbRef.child("Users").child(UID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String symbol = Objects.requireNonNull(snapshot.child("currency_symbol").getValue()).toString();
                String code = Objects.requireNonNull(snapshot.child("currency_code").getValue()).toString();
                String name = Objects.requireNonNull(snapshot.child("name").getValue()).toString();
                String image = Objects.requireNonNull(snapshot.child("image").getValue()).toString();
                country = Objects.requireNonNull(snapshot.child("country_code").getValue()).toString();

                c_name = name;
                c_code = code;
                c_symbol = symbol;

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    currency.setText(Html.fromHtml(symbol + " <small>(" + code + ")</small>", Html.FROM_HTML_MODE_COMPACT));
                } else {
                    currency.setText(Html.fromHtml(symbol + " <small>(" + code + ")</small>"));
                }
                username.setText(name);
                Glide.with(WithdrawActivity.this).load(image).thumbnail(0.25f).into(userimage);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void staticOnClicks() {
//        currency.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                final CurrencyPicker picker = CurrencyPicker.newInstance("Select Currency");  // dialog title
//                picker.setListener(new CurrencyPickerListener() {
//                    @Override
//                    public void onSelectCurrency(String name, String code, String symbol, int flagDrawableResID) {
//                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
//                            currency.setText(Html.fromHtml(symbol + " <small>(" + code + ")</small>", Html.FROM_HTML_MODE_COMPACT));
//                        } else {
//                            currency.setText(Html.fromHtml(symbol + " <small>(" + code + ")</small>"));
//                        }
//                        c_name = name;
//                        c_code = code;
//                        c_symbol = symbol;
//
//                        picker.dismiss();
//                    }
//                });
//                picker.show(getSupportFragmentManager(), "CURRENCY_PICKER");
//            }
//        });
        withdraw.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startWithdraw();
            }
        });
    }

    private void totalBalance() {
        dbRef.child("PersonalWallet").child(UID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists() && dataSnapshot.hasChildren()) {
                    long total = 0;
                    Long value;

                    for (DataSnapshot npsnapshot : dataSnapshot.getChildren()) {
                        if (!npsnapshot.child("status").exists() && npsnapshot.child("text_ref").exists()) {
                            String text_ref = Objects.requireNonNull(npsnapshot.child("text_ref").getValue()).toString();
                            long end_time = System.currentTimeMillis() / 1000L;
                            dbRef.child("PersonalWallet").child(UID).child(text_ref).child("end_time").setValue(end_time);
                            dbRef.child("PersonalWallet").child(UID).child(text_ref).child("status").setValue("cancelled");
                        }
                        if (npsnapshot.child("status").exists() && Objects.equals(npsnapshot.child("status").getValue(), "success")) {
                            value = (Long) npsnapshot.child("amount").getValue();

                            if (Objects.equals(npsnapshot.child("type").getValue(), "deposit")) {
                                total = total + value;
                            } else {
                                total = total - value;
                            }
                        }
                    }
                    NumberFormat nf = NumberFormat.getInstance();
                    nf.setMinimumFractionDigits(0);
                    nf.setMaximumFractionDigits(0);
                    balance = nf.format(total).replace(",", "");
                    amountNumber.setText(balance);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void startWithdraw() {
        amount = amountNumber.getText().toString();
        if (amount.equals("")) {
            amountNumber.setError("Enter your withdrawal amount");
            return;
        }

        long maxWithdraw = Long.parseLong(balance) - 50;
        final long withDrawAmount = Long.parseLong(amount);
        if (withDrawAmount < 500) {
            amountNumber.setError("Minimum withdrawal amount is kshs 500");
            return;
        }
        if (Long.parseLong(amount) > maxWithdraw) {
            amountNumber.setError("You cannot withdraw more than kshs " + maxWithdraw);
            return;
        }
        if (!amount.equals("") && Long.parseLong(amount) <= maxWithdraw && withDrawAmount >= 500) {
            new AlertDialog.Builder(WithdrawActivity.this)
                    .setTitle("PersonalWallet Withdrawal")
                    .setMessage("You are about to withdraw " + amount + " from your PersonalWallet account.\n\nTransaction fees kshs 50")
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

                new AlertDialog.Builder(WithdrawActivity.this)
                        .setTitle("PersonalWallet Withdrawal")
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
        withdrawRef = dbRef.child("PersonalWallet").child(UID).push();
        final String reference = withdrawRef.getKey();

        transactionRef = dbRef.child("Transactions").push();
        final String transaction_code = transactionRef.getKey();

        long time_start = System.currentTimeMillis() / 1000L;
        final String narration = "Withdrawal from " + firstname + " " + lastname + "'s PersonalWallet";

        withdrawRef.child("time_start").setValue(time_start);
        withdrawRef.child("user").setValue(UID);
        withdrawRef.child("group").setValue("");
        withdrawRef.child("type").setValue("withdraw");
        withdrawRef.child("narration").setValue(narration);
        withdrawRef.child("amount").setValue(withdrawAmount + 50);
        withdrawRef.child("text_ref").setValue(reference);

        transactionRef.child("time_start").setValue(time_start);
        transactionRef.child("user").setValue(UID);
        withdrawRef.child("group").setValue("");
        transactionRef.child("type").setValue("withdraw");
        transactionRef.child("narration").setValue("PersonalWallet");
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
                Toast.makeText(WithdrawActivity.this, "You have succesfully withdrawn kshs " + withdrawAmount, Toast.LENGTH_LONG).show();
                startActivity(new Intent(WithdrawActivity.this, WalletActivity.class));
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
                Toast.makeText(WithdrawActivity.this, "Error processing your withdrawal", Toast.LENGTH_LONG).show();
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

        Volley.newRequestQueue(WithdrawActivity.this).add(stringRequest);
    }
}
