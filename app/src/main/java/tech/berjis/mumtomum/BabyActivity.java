package tech.berjis.mumtomum;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.flutterwave.raveandroid.RaveConstants;
import com.flutterwave.raveandroid.RavePayActivity;
import com.flutterwave.raveandroid.RavePayManager;
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

public class BabyActivity extends AppCompatActivity {

    FirebaseAuth mAuth;
    DatabaseReference dbRef, depositRef, withdrawRef;
    String UID, phone, amount, balance;
    TextView deposit, withdraw, amountText, balanceAmount;
    EditText amountNumber;
    ConstraintLayout transactionPanel;
    ImageView closeTransactionPanel, back;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_baby);

        mAuth = FirebaseAuth.getInstance();
        dbRef = FirebaseDatabase.getInstance().getReference();
        UID = mAuth.getCurrentUser().getUid();
        phone = mAuth.getCurrentUser().getPhoneNumber();

        deposit = findViewById(R.id.deposit);
        withdraw = findViewById(R.id.withdraw);
        amountNumber = findViewById(R.id.amountNumber);
        amountText = findViewById(R.id.amountText);
        transactionPanel = findViewById(R.id.transactionPanel);
        closeTransactionPanel = findViewById(R.id.closeTransactionPanel);
        back = findViewById(R.id.back);
        balanceAmount = findViewById(R.id.balanceAmount);

        closeTransactionPanel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                transactionPanel.animate()
                        .translationY(transactionPanel.getHeight())
                        .alpha(1.0f)
                        .setDuration(300)
                        .setListener(new AnimatorListenerAdapter() {
                            @Override
                            public void onAnimationEnd(Animator animation) {
                                super.onAnimationEnd(animation);
                                transactionPanel.setVisibility(View.GONE);
                            }
                        });
            }
        });

        deposit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                transactionPanel.setVisibility(View.VISIBLE);
                transactionPanel.animate()
                        .translationY(0)
                        .alpha(1.0f)
                        .setDuration(300)
                        .setListener(new AnimatorListenerAdapter() {
                            @Override
                            public void onAnimationEnd(Animator animation) {
                                super.onAnimationEnd(animation);
                                amountText.setText("How much do you want to deposit?\n\nmax KES 100,000");
                                amountNumber.setOnEditorActionListener(new TextView.OnEditorActionListener() {
                                    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                                        if (actionId == EditorInfo.IME_ACTION_SEND) {
                                            amount = amountNumber.getText().toString();
                                            if (!amount.equals("")) {
                                                new AlertDialog.Builder(BabyActivity.this)
                                                        .setTitle("BabyWallet Deposit")
                                                        .setMessage("You are about to deposit " + amount + " to BabyActivity.")
                                                        .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                                            public void onClick(DialogInterface dialog, int which) {
                                                                checkUser();
                                                            }
                                                        })
                                                        .setNegativeButton("Cancel", null)
                                                        .show();
                                            } else {
                                                amountNumber.setError("Please enter a value greater than zero (0)");
                                            }
                                            return true;
                                        }
                                        return false;
                                    }
                                });
                            }
                        });
            }
        });
        withdraw.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /*HashMap<String, String> data = new HashMap<>();

                data.put("sender", "babytobaby");
                data.put("account_number", "254725227513");
                data.put("amount", "3000");
                data.put("narration", "Test BabyWallet Android volley to Heroku app calls");
                data.put("reference", "234efregf2456");
                data.put("beneficiary_name", "Benedict Ouma");

                String url = "https:babywallet.herokuapp.com/api/request_withdrawal";*/
                transactionPanel.setVisibility(View.VISIBLE);
                transactionPanel.animate()
                        .translationY(0)
                        .alpha(1.0f)
                        .setDuration(300)
                        .setListener(new AnimatorListenerAdapter() {
                            @Override
                            public void onAnimationEnd(Animator animation) {
                                super.onAnimationEnd(animation);
                                amountText.setText("How much do you want to withdraw?\n\nmin KES 500");
                                amountNumber.setOnEditorActionListener(new TextView.OnEditorActionListener() {
                                    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                                        if (actionId == EditorInfo.IME_ACTION_SEND) {
                                            amount = amountNumber.getText().toString();
                                            balance = balanceAmount.getText().toString();
                                            long maxWithdraw = Long.parseLong(balance) - 50;
                                            final long withDrawAmount = Long.parseLong(amount);
                                            if (withDrawAmount < 500) {
                                                amountNumber.setError("Minibaby withdrawal amount is kshs 500");
                                            }
                                            if (Long.parseLong(amount) > maxWithdraw) {
                                                amountNumber.setError("You cannot withdraw more than kshs " + maxWithdraw);
                                            }
                                            if (amount.equals("")) {
                                                amountNumber.setError("Enter your withdrawal amount");
                                            }
                                            if (!amount.equals("") && Long.parseLong(amount) < maxWithdraw && withDrawAmount >= 500) {
                                                new AlertDialog.Builder(BabyActivity.this)
                                                        .setTitle("BabyWallet Withdrawal")
                                                        .setMessage("You are about to withdraw " + amount + " from your BabyWallet account.\n\nTransaction fees kshs 50")
                                                        .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                                            public void onClick(DialogInterface dialog, int which) {
                                                                Toast.makeText(BabyActivity.this, "Sending withdrawal request", Toast.LENGTH_LONG).show();
                                                                checkWithdrawViability(withDrawAmount);
                                                            }
                                                        })
                                                        .setNegativeButton("Cancel", null)
                                                        .show();
                                            }
                                            return true;
                                        }
                                        return false;
                                    }
                                });
                            }
                        });
            }
        });
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                BabyActivity.super.finish();
            }
        });
        BabyTotalBalance();
    }

    public void checkWithdrawViability(final Long withdrawAmount) {
        dbRef.child("Users").child(UID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String firstname = dataSnapshot.child("first_name").getValue().toString();
                String lastname = dataSnapshot.child("last_name").getValue().toString();
                String phone_number = phone.substring(phone.length() - 12);

                withdrawRequest(firstname, lastname, phone_number, withdrawAmount);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void withdrawRequest(final String firstname, final String lastname, final String phone_number, final Long withdrawAmount) {
        withdrawRef = dbRef.child("BabyWallet").child(UID).push();
        final String reference = withdrawRef.getKey();

        long time_start = System.currentTimeMillis() / 1000L;
        final String narration = "Withdrawal from " + firstname + " " + lastname + "'s Babywallet";

        withdrawRef.child("time_start").setValue(time_start);
        withdrawRef.child("user").setValue(UID);
        withdrawRef.child("type").setValue("withdraw");
        withdrawRef.child("narration").setValue(narration);
        withdrawRef.child("amount").setValue(withdrawAmount + 50);
        withdrawRef.child("text_ref").setValue(reference);

        String requestUrl = "https:mumwallet.herokuapp.com/api/request_withdrawal";
        StringRequest stringRequest = new StringRequest(Request.Method.POST, requestUrl, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.e("Volley Result", "" + response);
                //Toast.makeText(BabyActivity.this, response, Toast.LENGTH_SHORT).show();
                if(response.equals("{\"status\":\"cancelled\"}")){
                    long end_time = System.currentTimeMillis() / 1000L;
                    withdrawRef.child("end_time").setValue(end_time);
                    withdrawRef.child("status").setValue("cancelled");
                    Toast.makeText(BabyActivity.this, "We couldn't process your withdrawal request. try again later", Toast.LENGTH_LONG).show();
                }else{
                    long end_time = System.currentTimeMillis() / 1000L;
                    withdrawRef.child("end_time").setValue(end_time);
                    withdrawRef.child("status").setValue("success");
                    Toast.makeText(BabyActivity.this, "You have succesfully withdrawn kshs " + withdrawAmount, Toast.LENGTH_LONG).show();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
                long end_time = System.currentTimeMillis() / 1000L;
                withdrawRef.child("end_time").setValue(end_time);
                withdrawRef.child("status").setValue("error");
                Toast.makeText(BabyActivity.this, "Error processing your withdrawal", Toast.LENGTH_LONG).show();
            }
        }) {

            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> postMap = new HashMap<>();
                postMap.put("sender", "babytobaby");
                postMap.put("account_number", phone_number);
                postMap.put("amount", String.valueOf(withdrawAmount));
                postMap.put("narration", narration);
                postMap.put("reference", reference);
                postMap.put("beneficiary_name", firstname + " " + lastname);
                return postMap;
            }
        };

        Volley.newRequestQueue(BabyActivity.this).add(stringRequest);
    }


    private void checkUser() {
        dbRef.child("Users").child(UID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if (!dataSnapshot.child("email").exists() ||
                        !dataSnapshot.child("first_name").exists() ||
                        !dataSnapshot.child("last_name").exists()) {
                    startActivity(new Intent(BabyActivity.this, UserWalletDetails.class));
                } else {

                    String firstname = dataSnapshot.child("first_name").getValue().toString();
                    String lastname = dataSnapshot.child("last_name").getValue().toString();
                    String email = dataSnapshot.child("email").getValue().toString();
                    validateEntries(firstname, lastname, email);

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void BabyTotalBalance() {
        dbRef.child("BabyWallet").child(UID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists() && dataSnapshot.hasChildren()) {
                    long total = 0;
                    Long value;

                    for (DataSnapshot npsnapshot : dataSnapshot.getChildren()) {
                        if (!npsnapshot.child("status").exists() && npsnapshot.child("text_ref").exists()) {
                            String text_ref = Objects.requireNonNull(npsnapshot.child("text_ref").getValue()).toString();
                            long end_time = System.currentTimeMillis() / 1000L;
                            dbRef.child("BabyWallet").child(UID).child(text_ref).child("end_time").setValue(end_time);
                            dbRef.child("BabyWallet").child(UID).child(text_ref).child("status").setValue("cancelled");
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
                    String output = nf.format(total);
                    balanceAmount.setText(output);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void validateEntries(String fName, String lName, String email) {
        depositRef = dbRef.child("BabyWallet").child(UID).push();
        String text_ref = depositRef.getKey();
        long time_start = System.currentTimeMillis() / 1000L;
        long final_amount = Long.parseLong(amount);
        depositRef.child("time_start").setValue(time_start);

        String publicKey = getString(R.string.public_key);
        String encryptionKey = getString(R.string.encryption_key);
        String narration = fName + " " + lName + "'s BabyWallet savings";

        depositRef.child("user").setValue(UID);
        depositRef.child("type").setValue("deposit");
        depositRef.child("narration").setValue(narration);
        depositRef.child("amount").setValue(final_amount);
        depositRef.child("text_ref").setValue(text_ref);

        boolean valid = true;

        if (amount.length() == 0) {
            amount = "0";
        }

        //isAmountValid for compulsory fields

        if (valid) {
            RavePayManager ravePayManager = new RavePayManager(this).setAmount(Double.parseDouble(amount))
                    .setCountry("KE")
                    .setCurrency("KES")
                    .setEmail(email)
                    .setfName(fName)
                    .setlName(lName)
                    .setPhoneNumber(phone)
                    .setNarration(narration)
                    .setPublicKey(publicKey)
                    .setEncryptionKey(encryptionKey)
                    .setTxRef(text_ref)
                    .acceptMpesaPayments(true)
                    .acceptAccountPayments(true)
                    .acceptCardPayments(true)
                    .allowSaveCardFeature(true)
                    .acceptAchPayments(false)
                    .acceptGHMobileMoneyPayments(false)
                    .acceptUgMobileMoneyPayments(false)
                    .acceptZmMobileMoneyPayments(false)
                    .acceptRwfMobileMoneyPayments(false)
                    .acceptUkPayments(false)
                    .acceptSaBankPayments(false)
                    .acceptFrancMobileMoneyPayments(false)
                    .acceptBankTransferPayments(false)
                    .acceptUssdPayments(false)
                    .acceptBarterPayments(false)
                    .onStagingEnv(false)
                    .isPreAuth(true)
                    .showStagingLabel(false)
//                    .setMeta(meta)
//                    .withTheme(R.style.TestNewTheme)
                    .shouldDisplayFee(true);

            ravePayManager.initialize();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == RaveConstants.RAVE_REQUEST_CODE && data != null) {

            amountNumber.setText("");
            String message = data.getStringExtra("response");

            if (message != null) {
                Log.d("rave response", message);
            }

            transactionPanel.animate()
                    .translationY(transactionPanel.getHeight())
                    .alpha(1.0f)
                    .setDuration(300)
                    .setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            super.onAnimationEnd(animation);
                            transactionPanel.setVisibility(View.GONE);
                        }
                    });

            if (resultCode == RavePayActivity.RESULT_SUCCESS) {
                long end_time = System.currentTimeMillis() / 1000L;
                depositRef.child("end_time").setValue(end_time);
                depositRef.child("status").setValue("success");
                //Toast.makeText(this, "SUCCESS " + message, Toast.LENGTH_SHORT).show();
            } else if (resultCode == RavePayActivity.RESULT_ERROR) {
                long end_time = System.currentTimeMillis() / 1000L;
                depositRef.child("end_time").setValue(end_time);
                depositRef.child("status").setValue("error");
                //Toast.makeText(this, "ERROR " + message, Toast.LENGTH_SHORT).show();
            } else if (resultCode == RavePayActivity.RESULT_CANCELLED) {
                long end_time = System.currentTimeMillis() / 1000L;
                depositRef.child("end_time").setValue(end_time);
                depositRef.child("status").setValue("cancelled");
                //Toast.makeText(this, "CANCELLED " + message, Toast.LENGTH_SHORT).show();
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

}
