package tech.berjis.mumtomum;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.flutterwave.raveandroid.RaveConstants;
import com.flutterwave.raveandroid.RavePayActivity;
import com.flutterwave.raveandroid.RavePayManager;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class CartActivity extends AppCompatActivity {

    FirebaseAuth mAuth;
    DatabaseReference dbRef, transactionRef, depositRef;
    FirebaseUser currentUser;
    List<Cart> listData;
    CartAdapter productsAdapter;
    RecyclerView rv;
    String country = "", c_name = "", c_code = "", c_symbol = "", UID, u_name;
    long cart_total;
    TextView payNow;
    long unixTime = System.currentTimeMillis() / 1000L;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart);

        initVars();
        loadProducts();
        staticOnClicks();
    }

    private void initVars() {
        mAuth = FirebaseAuth.getInstance();
        dbRef = FirebaseDatabase.getInstance().getReference();
        dbRef.keepSynced(true);
        currentUser = mAuth.getCurrentUser();
        UID = Objects.requireNonNull(mAuth.getCurrentUser()).getUid();
        payNow = findViewById(R.id.payNow);

        dbRef.child("Users").child(UID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String symbol = Objects.requireNonNull(dataSnapshot.child("currency_symbol").getValue()).toString();
                String code = Objects.requireNonNull(dataSnapshot.child("currency_code").getValue()).toString();
                String name = Objects.requireNonNull(dataSnapshot.child("name").getValue()).toString();
                country = Objects.requireNonNull(dataSnapshot.child("country_code").getValue()).toString();

                c_name = name;
                c_code = code;
                c_symbol = symbol;
                u_name = Objects.requireNonNull(dataSnapshot.child("name").getValue()).toString();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void checkTotal() {
        dbRef.child("Cart").child(UID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists() && dataSnapshot.hasChildren()) {
                    long total = 0;
                    long value;

                    for (DataSnapshot npsnapshot : dataSnapshot.getChildren()) {
                        if (npsnapshot.child("status").getValue().toString().equals("0")) {
                            value = (long) npsnapshot.child("price").getValue();
                            total = total + value;
                        }
                    }

                    cart_total = total;

                    new AlertDialog.Builder(CartActivity.this)
                            .setMessage("Checking out Kshs " + cart_total)
                            .setPositiveButton("Checkout", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    checkout();
                                }
                            })
                            .setNegativeButton("Keep Shopping", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    startActivity(new Intent(CartActivity.this, ProductsActivity.class));
                                }
                            })
                            .show();

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void staticOnClicks() {
        payNow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkOut();
            }
        });
    }

    public void loadProducts() {
        listData = new ArrayList<>();
        rv = findViewById(R.id.products);
        rv.setLayoutManager(new LinearLayoutManager(getApplicationContext(), LinearLayoutManager.VERTICAL, false));
        dbRef.child("Cart").child(UID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (DataSnapshot npsnapshot : dataSnapshot.getChildren()) {
                        if (npsnapshot.child("status").getValue().toString().equals("0")) {
                            Cart l = npsnapshot.getValue(Cart.class);
                            listData.add(l);
                        } else {
                            Toast.makeText(CartActivity.this, Objects.requireNonNull(npsnapshot.child("status").getValue()).toString(), Toast.LENGTH_SHORT).show();
                        }
                    }
                    productsAdapter = new CartAdapter(listData);
                    rv.setAdapter(productsAdapter);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void checkOut() {
        checkTotal();
    }

    private void checkout() {

        dbRef.child("Cart").child(UID).addListenerForSingleValueEvent(new ValueEventListener() {

            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                for (DataSnapshot npsnapshot : dataSnapshot.getChildren()) {
                    if (npsnapshot.child("status").exists() && Objects.equals(npsnapshot.child("status").getValue(), 0)) {
                        String item_id = Objects.requireNonNull(npsnapshot.child("status").getValue()).toString();
                        String product_id = Objects.requireNonNull(npsnapshot.child("product_id").getValue()).toString();
                        String seller = Objects.requireNonNull(npsnapshot.child("seller").getValue()).toString();
                        outGoingOrder(product_id, seller);
                        inComingOrder(product_id, seller);
                        dbRef.child("Cart").child(UID).child(item_id).child("status").setValue(1);
                        submitPayment();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void outGoingOrder(String product_id, String seller) {
        dbRef.child("OutGoingOrders").child(UID).child("Order_" + unixTime).child("buyer").setValue(UID);
        dbRef.child("OutGoingOrders").child(UID).child("Order_" + unixTime).child("product").setValue(product_id);
        dbRef.child("OutGoingOrders").child(UID).child("Order_" + unixTime).child("seller").setValue(seller);
        dbRef.child("OutGoingOrders").child(UID).child("Order_" + unixTime).child("time").setValue(unixTime);
        dbRef.child("OutGoingOrders").child(UID).child("Order_" + unixTime).child("status").setValue(0);
    }

    private void inComingOrder(String product_id, String seller) {
        dbRef.child("InComingOrders").child(UID).child("Order_" + unixTime).child("buyer").setValue(UID);
        dbRef.child("InComingOrders").child(UID).child("Order_" + unixTime).child("product").setValue(product_id);
        dbRef.child("InComingOrders").child(UID).child("Order_" + unixTime).child("seller").setValue(seller);
        dbRef.child("InComingOrders").child(UID).child("Order_" + unixTime).child("time").setValue(unixTime);
        dbRef.child("InComingOrders").child(UID).child("Order_" + unixTime).child("status").setValue(0);
    }

    private void submitPayment() {
        new AlertDialog.Builder(CartActivity.this)
                .setTitle("Personal Wallet Deposit")
                .setMessage("You are about to pay " + cart_total + " to MumToMum")
                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        checkUser();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void checkUser() {
        dbRef.child("Users").child(UID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if (!dataSnapshot.child("email").exists() ||
                        !dataSnapshot.child("first_name").exists() ||
                        !dataSnapshot.child("last_name").exists()) {
                    startActivity(new Intent(CartActivity.this, ProfileActivity.class));
                    Toast.makeText(CartActivity.this, "You need to first update your profile before using the Wallets", Toast.LENGTH_SHORT).show();
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
        depositRef = dbRef.child("PaymentWallet").child(UID).push();
        transactionRef = dbRef.child("Transactions").push();
        String text_ref = depositRef.getKey();
        String trans_ref = transactionRef.getKey();
        long time_start = System.currentTimeMillis() / 1000L;
        long final_amount = cart_total;
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
        transactionRef.child("narration").setValue(narration);
        transactionRef.child("amount").setValue(final_amount);
        transactionRef.child("text_ref").setValue(trans_ref);

        boolean valid = true;

        //isAmountValid for compulsory fields

        if (valid) {
            RavePayManager ravePayManager = new RavePayManager(this).setAmount(cart_total)
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
