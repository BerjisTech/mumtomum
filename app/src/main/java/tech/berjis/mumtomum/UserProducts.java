package tech.berjis.mumtomum;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class UserProducts extends AppCompatActivity {

    RecyclerView products;
    DatabaseReference dbRef;
    List<Products> listData;
    ProductsAdapter productsAdapter;
    String UID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_products);

        dbRef = FirebaseDatabase.getInstance().getReference();
        dbRef.keepSynced(true);
        products = findViewById(R.id.products);

        loadUser();
    }

    public void loadUser(){

        Intent userIntent = getIntent();
        Bundle userBundle = userIntent.getExtras();
        assert userBundle != null;
        String userID = userBundle.getString("user_id");
        loadProducts(userID);
    }

    public void loadProducts(String seller) {
        SpannedGridLayoutManager layoutManager = new SpannedGridLayoutManager(
                new SpannedGridLayoutManager.GridSpanLookup() {
                    @Override
                    public SpannedGridLayoutManager.SpanInfo getSpanInfo(int position) {
                        /* Conditions for 2x2 items
                        if (position % 12 == 0 || position % 12 == 7) {
                            return new SpannedGridLayoutManager.SpanInfo(2, 2);
                        } else {
                            return new SpannedGridLayoutManager.SpanInfo(1, 1);
                        }*/
                        return new SpannedGridLayoutManager.SpanInfo(1, 1);
                    }
                },
                2, // number of columns
                1f // how big is default item
        );
        products.setLayoutManager(layoutManager);

        listData = new ArrayList<>();

        dbRef.child("Products").orderByChild("seller").equalTo(seller).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (DataSnapshot npsnapshot : dataSnapshot.getChildren()) {
                        Products l = npsnapshot.getValue(Products.class);
                        listData.add(l);
                    }
                    productsAdapter = new ProductsAdapter(listData, "show");
                    products.setAdapter(productsAdapter);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

}
