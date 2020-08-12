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

import com.bumptech.glide.Glide;
import com.flutterwave.raveandroid.RaveConstants;
import com.flutterwave.raveandroid.RavePayActivity;
import com.flutterwave.raveandroid.RavePayManager;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

public class WalletActivity extends AppCompatActivity {

    FirebaseAuth mAuth;
    DatabaseReference dbRef, depositRef, transactionRef;

    EditText amountNumber;
    TextView currency, username, local, card;
    CircleImageView userimage;
    String country = "", c_name = "", c_code = "", c_symbol = "", UID, amount, balance;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wallet);

        initLayouts();
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
        local = findViewById(R.id.local);
        card = findViewById(R.id.card);
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
                Glide.with(WalletActivity.this).load(image).thumbnail(0.25f).into(userimage);
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
        card.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                submitPayment();
            }
        });
    }

    private void submitPayment() {
        amount = amountNumber.getText().toString();
        if (!amount.isEmpty()) {
            new AlertDialog.Builder(WalletActivity.this)
                    .setTitle("Personal Wallet Deposit")
                    .setMessage("You are about to deposit " + amount + " to your personal amount.")
                    .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            checkUser();
                        }
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        } else
            Toast.makeText(this, "Enter a valid amount for payment", Toast.LENGTH_SHORT).show();

    }

    private void checkUser() {
        dbRef.child("Users").child(UID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if (!dataSnapshot.child("email").exists() ||
                        !dataSnapshot.child("first_name").exists() ||
                        !dataSnapshot.child("last_name").exists()) {
                    startActivity(new Intent(WalletActivity.this, ProfileActivity.class));
                    Toast.makeText(WalletActivity.this, "You need to first update your profile before using the Wallets", Toast.LENGTH_SHORT).show();
                } else {

                    String firstname = dataSnapshot.child("first_name").getValue().toString();
                    String lastname = dataSnapshot.child("last_name").getValue().toString();
                    String email = dataSnapshot.child("email").getValue().toString();
                    String phone_number = dataSnapshot.child("regs_phone").getValue().toString();
                    validateEntries(firstname, lastname, email, phone_number);

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void validateEntries(String fName, String lName, String email, String phone_number) {
        depositRef = dbRef.child("PersonalWallet").child(UID).push();
        transactionRef = dbRef.child("Transactions").push();
        String text_ref = depositRef.getKey();
        String trans_ref = transactionRef.getKey();
        long time_start = System.currentTimeMillis() / 1000L;
        long final_amount = Long.parseLong(amount);
        depositRef.child("time_start").setValue(time_start);
        transactionRef.child("time_start").setValue(time_start);

        String publicKey = getString(R.string.public_key);
        String encryptionKey = getString(R.string.encryption_key);
        String narration = fName + " " + lName + "'s PersonalWallet savings";

        depositRef.child("user").setValue(UID);
        depositRef.child("group").setValue("");
        depositRef.child("type").setValue("deposit");
        depositRef.child("narration").setValue(narration);
        depositRef.child("amount").setValue(final_amount);
        depositRef.child("text_ref").setValue(text_ref);

        transactionRef.child("user").setValue(UID);
        depositRef.child("group").setValue("");
        transactionRef.child("type").setValue("deposit");
        transactionRef.child("narration").setValue("PersonalWallet");
        transactionRef.child("amount").setValue(final_amount);
        transactionRef.child("text_ref").setValue(trans_ref);

        boolean valid = true;

        if (amount.length() == 0) {
            amount = "0";
        }

        //isAmountValid for compulsory fields

        if (valid) {
            RavePayManager ravePayManager = new RavePayManager(this).setAmount(Double.parseDouble(amount))
                    .setCountry(country)
                    .setCurrency(c_code)
                    .setEmail(email)
                    .setfName(fName)
                    .setlName(lName)
                    .setPhoneNumber(phone_number)
                    .setNarration(narration)
                    .setPublicKey(publicKey)
                    .setEncryptionKey(encryptionKey)
                    .setTxRef(text_ref)
                    .acceptAccountPayments(true)
                    .acceptCardPayments(true)
                    .allowSaveCardFeature(true)
                    .acceptAchPayments(false)
                    .acceptFrancMobileMoneyPayments(false)
                    .acceptBankTransferPayments(false)
                    .acceptUssdPayments(false)
                    .acceptBarterPayments(true)
                    .onStagingEnv(false)
                    .isPreAuth(true)
                    .showStagingLabel(false)
//                    .setMeta(meta)
//                    .withTheme(R.style.TestNewTheme)
                    .shouldDisplayFee(true);

            if (country.equals("KE")) {
                ravePayManager.acceptMpesaPayments(true);
            } else {
                ravePayManager.acceptMpesaPayments(false);
            }

            if (country.equals("GH")) {
                ravePayManager.acceptGHMobileMoneyPayments(true);
            } else {
                ravePayManager.acceptGHMobileMoneyPayments(false);
            }

            if (country.equals("UG")) {
                ravePayManager.acceptUgMobileMoneyPayments(true);
            } else {
                ravePayManager.acceptUgMobileMoneyPayments(false);
            }

            if (country.equals("ZM")) {
                ravePayManager.acceptZmMobileMoneyPayments(true);
            } else {
                ravePayManager.acceptZmMobileMoneyPayments(false);
            }

            if (country.equals("RW")) {
                ravePayManager.acceptRwfMobileMoneyPayments(true);
            } else {
                ravePayManager.acceptRwfMobileMoneyPayments(false);
            }

            if (country.equals("UK")) {
                ravePayManager.acceptUkPayments(true);
            } else {
                ravePayManager.acceptUkPayments(false);
            }

            if (country.equals("ZA")) {
                ravePayManager.acceptSaBankPayments(true);
            } else {
                ravePayManager.acceptSaBankPayments(false);
            }

            ravePayManager.initialize();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == RaveConstants.RAVE_REQUEST_CODE && data != null) {

            String message = data.getStringExtra("response");

            if (message != null) {
                Log.d("rave response", message);
            }

            if (resultCode == RavePayActivity.RESULT_SUCCESS) {
                amountNumber.setText("");
                long end_time = System.currentTimeMillis() / 1000L;
                depositRef.child("end_time").setValue(end_time);
                depositRef.child("status").setValue("success");

                transactionRef.child("end_time").setValue(end_time);
                transactionRef.child("status").setValue("success");
                Toast.makeText(this, "Success", Toast.LENGTH_SHORT).show();
            } else if (resultCode == RavePayActivity.RESULT_ERROR) {
                long end_time = System.currentTimeMillis() / 1000L;
                depositRef.child("end_time").setValue(end_time);
                depositRef.child("status").setValue("error");

                transactionRef.child("end_time").setValue(end_time);
                transactionRef.child("status").setValue("error");
                Toast.makeText(this, "There has been an error completing this transaction", Toast.LENGTH_LONG).show();
            } else if (resultCode == RavePayActivity.RESULT_CANCELLED) {
                long end_time = System.currentTimeMillis() / 1000L;
                depositRef.child("end_time").setValue(end_time);
                depositRef.child("status").setValue("cancelled");

                transactionRef.child("end_time").setValue(end_time);
                transactionRef.child("status").setValue("cancelled");
                Toast.makeText(this, "This transaction has been cancelled", Toast.LENGTH_LONG).show();
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }
}