package tech.berjis.mumtomum;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class UserProducts extends AppCompatActivity {

    ImageView back, newProduct;
    RecyclerView products;
    DatabaseReference dbRef;
    List<Object> listData;
    ProductsAdapter productsAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_products);

        dbRef = FirebaseDatabase.getInstance().getReference();
        dbRef.keepSynced(true);
        products = findViewById(R.id.products);
        back = findViewById(R.id.back);
        newProduct = findViewById(R.id.newProduct);

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                UserProducts.super.finish();
            }
        });
        newProduct.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(UserProducts.this, NewProductActivity.class));
            }
        });


        products.setLayoutManager(new GridLayoutManager(this, 2));

        listData = new ArrayList<>();
        loadUser();
    }

    public void loadUser() {

        Intent userIntent = getIntent();
        Bundle userBundle = userIntent.getExtras();
        assert userBundle != null;
        String userID = userBundle.getString("user_id");
        loadProducts(userID);
    }

    public void loadProducts(final String seller) {

        dbRef.child("Products").orderByChild("seller").equalTo(seller).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (DataSnapshot npsnapshot : dataSnapshot.getChildren()) {
                        if (!npsnapshot.child("status").exists()) {
                            String p_id = Objects.requireNonNull(npsnapshot.child("product_id").getValue()).toString();
                            dbRef.child("Products").child(p_id).removeValue();
                        } else {
                            Products l = npsnapshot.getValue(Products.class);
                            listData.add(l);
                        }
                    }
                    productsAdapter = new ProductsAdapter(listData, "");
                    products.setAdapter(productsAdapter);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

}
