package tech.berjis.mumtomum;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.toptoche.searchablespinnerlibrary.SearchableSpinner;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ProductsActivity extends AppCompatActivity {

    FirebaseAuth mAuth;
    DatabaseReference dbRef;
    FirebaseUser currentUser;
    List<Products> listData;
    ProductsAdapter productsAdapter;
    RecyclerView rv;
    ImageView groups, chats, profile, home, refresh;
    TextView chatsCount, updateProfiletext;
    SearchableSpinner productCategory;
    String UID, category;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_products);

        mAuth = FirebaseAuth.getInstance();
        dbRef = FirebaseDatabase.getInstance().getReference();
        dbRef.keepSynced(true);
        currentUser = mAuth.getCurrentUser();

        listData = new ArrayList<>();

        groups = findViewById(R.id.groups);
        chats = findViewById(R.id.chats);
        profile = findViewById(R.id.profile);
        home = findViewById(R.id.home);
        chatsCount = findViewById(R.id.chatsCount);
        rv = findViewById(R.id.products);
        productCategory = findViewById(R.id.productCategory);
        updateProfiletext = findViewById(R.id.updateProfiletext);
        refresh = findViewById(R.id.refresh);

        /*StaggeredGridLayoutManager sgManager = new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL);
        sgManager.setReverseLayout(true);
        sgManager.scrollToPositionWithOffset(0, 0);
        rv.setLayoutManager(sgManager);
        rv.setAdapter(productsAdapter);*/

        /*SpannedGridLayoutManager layoutManager = new SpannedGridLayoutManager(
                new SpannedGridLayoutManager.GridSpanLookup() {
                    @Override
                    public SpannedGridLayoutManager.SpanInfo getSpanInfo(int position) {
                        // Conditions for 2x2 items
                        if (position % 12 == 0 || position % 12 == 7) {
                            return new SpannedGridLayoutManager.SpanInfo(2, 2);
                        } else {
                            return new SpannedGridLayoutManager.SpanInfo(1, 1);
                        }
                    }
                },
                2, // number of columns
                1f // how big is default item
        );*/

        rv.setLayoutManager(new LinearLayoutManager(getApplicationContext(), LinearLayoutManager.VERTICAL, false));

        unloggedState();
        loadProducts();
        loadSpinner();
        refresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                refreshPage();
            }
        });
    }

    public void loadProducts() {
        dbRef.child("Products").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (DataSnapshot npsnapshot : dataSnapshot.getChildren()) {
                        if (!npsnapshot.child("status").exists()) {
                            String p_id = Objects.requireNonNull(npsnapshot.child("product_id").getValue()).toString();
                            dbRef.child("Products").child(p_id).removeValue();
                        }else{
                            Products l = npsnapshot.getValue(Products.class);
                            listData.add(l);
                        }
                    }
                    productsAdapter = new ProductsAdapter(listData, "");
                    rv.setAdapter(productsAdapter);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    public void unloggedState() {
        if (mAuth.getCurrentUser() == null) {
            chatsCount.setText("❌");
            chatsCount.setTextColor(Color.RED);

            groups.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startActivity(new Intent(ProductsActivity.this, RegisterActivity.class));
                }
            });
            chats.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startActivity(new Intent(ProductsActivity.this, RegisterActivity.class));
                }
            });
            profile.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startActivity(new Intent(ProductsActivity.this, RegisterActivity.class));
                }
            });
            home.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startActivity(new Intent(ProductsActivity.this, RegisterActivity.class));
                }
            });
        }else{
            loadChatCount();
            groups.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startActivity(new Intent(ProductsActivity.this, CommunityActivity.class));
                }
            });
            chats.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startActivity(new Intent(ProductsActivity.this, Messaging.class));
                }
            });
            profile.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startActivity(new Intent(ProductsActivity.this, ProfileActivity.class));
                }
            });
            home.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startActivity(new Intent(ProductsActivity.this, WalletActivity.class));
                }
            });
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (currentUser != null) {
            UID = mAuth.getCurrentUser().getUid();

            dbRef.child("Users").child(UID).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (!dataSnapshot.exists()) {
                        updateProfiletext.setVisibility(View.VISIBLE);
                        updateProfiletext.animate()
                                .alpha(0.0f)
                                .setDuration(0)
                                .setListener(new AnimatorListenerAdapter() {
                                    @Override
                                    public void onAnimationEnd(Animator animation) {
                                        super.onAnimationEnd(animation);
                                        updateProfiletext.animate()
                                                .alpha(0.0f)
                                                .setDuration(10000)
                                                .setListener(new AnimatorListenerAdapter() {
                                                    @Override
                                                    public void onAnimationEnd(Animator animation) {
                                                        super.onAnimationEnd(animation);
                                                        updateProfiletext.animate()
                                                                .alpha(1.0f)
                                                                .setDuration(300)
                                                                .setListener(new AnimatorListenerAdapter() {
                                                                    @Override
                                                                    public void onAnimationEnd(Animator animation) {
                                                                        super.onAnimationEnd(animation);

                                                                        updateProfiletext.setOnClickListener(new View.OnClickListener() {
                                                                            @Override
                                                                            public void onClick(View v) {
                                                                                startActivity(new Intent(ProductsActivity.this, ProfileActivity.class));
                                                                            }
                                                                        });

                                                                        updateProfiletext.animate()
                                                                                .alpha(1.0f)
                                                                                .setDuration(30000)
                                                                                .setListener(new AnimatorListenerAdapter() {
                                                                                    @Override
                                                                                    public void onAnimationEnd(Animator animation) {
                                                                                        super.onAnimationEnd(animation);
                                                                                        updateProfiletext.animate()
                                                                                                .alpha(0.0f)
                                                                                                .setDuration(300)
                                                                                                .setListener(new AnimatorListenerAdapter() {
                                                                                                    @Override
                                                                                                    public void onAnimationEnd(Animator animation) {
                                                                                                        super.onAnimationEnd(animation);
                                                                                                        updateProfiletext.setVisibility(View.GONE);
                                                                                                    }
                                                                                                });
                                                                                    }
                                                                                });
                                                                    }
                                                                });
                                                    }
                                                });
                                    }
                                });
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }
    }

    public void loadChatCount(){
        UID = mAuth.getCurrentUser().getUid();
        dbRef.child("Chats").child(UID).orderByChild("read").equalTo("false").addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                Long chatCount = dataSnapshot.getChildrenCount();
                chatsCount.setText(String.valueOf(chatCount));
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                Long chatCount = dataSnapshot.getChildrenCount();
                chatsCount.setText(String.valueOf(chatCount));
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
                Long chatCount = dataSnapshot.getChildrenCount();
                chatsCount.setText(String.valueOf(chatCount));
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                Long chatCount = dataSnapshot.getChildrenCount();
                chatsCount.setText(String.valueOf(chatCount));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }

    public void loadSpinner() {
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.categories, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        productCategory.setAdapter(adapter);
        productCategory.setTitle("Search by category");
        productCategory.setPositiveButton("Cancel");
        productCategory.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if(position > 0){
                    category = productCategory.getSelectedItem().toString();
                    loadProductsPerCategory(category);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }


    public void loadProductsPerCategory(String productCategory) {
        listData.clear();
        dbRef.child("Products").orderByChild("category").equalTo(productCategory).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (DataSnapshot npsnapshot : dataSnapshot.getChildren()) {
                        Products l = npsnapshot.getValue(Products.class);
                        listData.add(l);
                    }
                    productsAdapter = new ProductsAdapter(listData, "");
                    rv.setAdapter(productsAdapter);
                }else{
                    Toast.makeText(ProductsActivity.this, "There are no items in this category", Toast.LENGTH_SHORT).show();
                    loadProducts();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    public void refreshPage(){
        listData.clear();
        productCategory.setSelection(0);
        loadProducts();
    }
}
