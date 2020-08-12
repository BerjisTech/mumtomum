package tech.berjis.mumtomum;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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
    DatabaseReference dbRef;
    FirebaseUser currentUser;
    List<Cart> listData;
    CartAdapter productsAdapter;
    RecyclerView rv;
    String UID;
    long cart_total;
    TextView payNow;
    long unixTime = System.currentTimeMillis() / 1000L;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart);

        initVars();
        checkTotal();
        loadProducts();
        staticOnClicks();
    }

    private void initVars() {
        mAuth = FirebaseAuth.getInstance();
        dbRef = FirebaseDatabase.getInstance().getReference();
        dbRef.keepSynced(true);
        currentUser = mAuth.getCurrentUser();
        UID = mAuth.getCurrentUser().getUid();
        payNow = findViewById(R.id.payNow);
    }

    private void checkTotal() {
        dbRef.child("Cart").child(UID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists() && dataSnapshot.hasChildren()) {
                    long total = 0;
                    long value;

                    for (DataSnapshot npsnapshot : dataSnapshot.getChildren()) {
                        if (npsnapshot.child("status").exists() && Objects.equals(npsnapshot.child("status").getValue(), 0)) {
                            value = (long) npsnapshot.child("price").getValue();
                            total = total + value;
                        }
                    }

                    cart_total = total;

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
                        if (npsnapshot.child("status").exists() && Objects.equals(npsnapshot.child("status").getValue(), 0)) {
                            Cart l = npsnapshot.getValue(Cart.class);
                            listData.add(l);
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

}
