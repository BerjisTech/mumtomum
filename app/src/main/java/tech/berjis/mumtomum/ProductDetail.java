package tech.berjis.mumtomum;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;
import com.vanniktech.emoji.EmojiTextView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

public class ProductDetail extends AppCompatActivity {

    DatabaseReference dbRef;
    FirebaseAuth mAuth;

    ImageView back, productImage, saveProduct;
    EmojiTextView productOwner;
    TextView productName, productPrice, productBuy, productDescriptionButton, productDescription, moreTitle, closeMoreProducts;
    ConstraintLayout moreProductsPanel;
    RecyclerView moreProducts;
    List<Products> listData;
    ProductsAdapter productsAdapter;
    String UID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_detail);

        mAuth = FirebaseAuth.getInstance();
        dbRef = FirebaseDatabase.getInstance().getReference();
        dbRef.keepSynced(true);
        UID = mAuth.getCurrentUser().getUid();

        back = findViewById(R.id.back);
        productImage = findViewById(R.id.productImage);
        saveProduct = findViewById(R.id.saveProduct);
        productName = findViewById(R.id.productName);
        productPrice = findViewById(R.id.productPrice);
        productOwner = findViewById(R.id.productOwner);
        productBuy = findViewById(R.id.productBuy);
        productDescriptionButton = findViewById(R.id.productDescriptionButton);
        productDescription = findViewById(R.id.productDescription);
        moreTitle = findViewById(R.id.moreTitle);
        moreProducts = findViewById(R.id.moreProducts);
        moreProductsPanel = findViewById(R.id.moreProductsPanel);
        closeMoreProducts = findViewById(R.id.closeMoreProducts);

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ProductDetail.super.finish();
            }
        });
        moreTitle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showMoreProducts();
            }
        });
        closeMoreProducts.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hideMoreProducts();
            }
        });
        loadProduct();

    }

    public void loadProduct() {
        Intent productIntent = getIntent();
        Bundle productBundle = productIntent.getExtras();
        assert productBundle != null;
        final String productID = productBundle.getString("product_id");

        assert productID != null;
        dbRef.child("Products").child(productID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                String name = Objects.requireNonNull(dataSnapshot.child("name").getValue()).toString();
                String description = Objects.requireNonNull(dataSnapshot.child("description").getValue()).toString();
                final String seller = Objects.requireNonNull(dataSnapshot.child("seller").getValue()).toString();
                String price = Objects.requireNonNull(dataSnapshot.child("price").getValue()).toString();
                final String product_id = Objects.requireNonNull(dataSnapshot.child("product_id").getValue()).toString();
                String image = Objects.requireNonNull(dataSnapshot.child("image").getValue()).toString();

                if (seller.equals(UID)) {
                    productBuy.setAlpha(0.5f);
                    productBuy.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Toast.makeText(ProductDetail.this, "You can't buy your own product", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
                if (!seller.equals(UID)) {
                    productBuy.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            placeOrder(seller, product_id);
                        }
                    });
                }

                Picasso.get().load(image).into(productImage);
                Glide.with(ProductDetail.this).load(image).into(productImage);
                productName.setText(name);
                productDescription.setText(description);
                productDescriptionButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        showDescription();
                    }
                });
                productPrice.setText("Kshs " + price);


                loaduser(seller);
                loadOtherProducts(seller);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    public void loadOtherProducts(String seller) {
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
        moreProducts.setLayoutManager(layoutManager);

        listData = new ArrayList<>();

        dbRef.child("Products").orderByChild("seller").equalTo(seller).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (DataSnapshot npsnapshot : dataSnapshot.getChildren()) {
                        Products l = npsnapshot.getValue(Products.class);
                        listData.add(l);
                    }
                    productsAdapter = new ProductsAdapter(listData, "");
                    moreProducts.setAdapter(productsAdapter);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    public void loaduser(final String seller) {
        dbRef.child("Users").child(seller).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String username = dataSnapshot.child("name").getValue().toString();
                productOwner.setText(username);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    public void showDescription() {
        productDescriptionButton.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null);
        productDescription.setVisibility(View.VISIBLE);
        productDescription
                .animate()
                .alpha(0.0f)
                .setDuration(0)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);
                        productDescription
                                .animate()
                                .alpha(1.0f)
                                .setDuration(300)
                                .setListener(new AnimatorListenerAdapter() {
                                    @Override
                                    public void onAnimationEnd(Animator animation) {
                                        super.onAnimationEnd(animation);
                                        productDescriptionButton.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {
                                                hideDescription();
                                            }
                                        });
                                    }
                                });
                    }
                });
    }

    public void hideDescription() {
        productDescriptionButton.setCompoundDrawablesWithIntrinsicBounds(null, null, ContextCompat.getDrawable(ProductDetail.this, R.drawable.ccp_down_arrow), null);
        productDescription
                .animate()
                .alpha(0.0f)
                .setDuration(300)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);
                        productDescription.setVisibility(View.GONE);
                        productDescriptionButton.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                showDescription();
                            }
                        });
                    }
                });
    }

    public void hideMoreProducts() {
        moreProductsPanel.animate()
                .translationY(moreProductsPanel.getHeight())
                .alpha(0.0f)
                .setDuration(300)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);
                        moreProductsPanel.setVisibility(View.GONE);
                    }
                });
    }

    public void showMoreProducts() {
        moreProductsPanel.setVisibility(View.VISIBLE);
        moreProductsPanel.animate()
                .translationY(0)
                .alpha(1.0f)
                .setDuration(300)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);
                    }
                });
    }

    public void placeOrder(final String seller, final String product) {
        if (mAuth.getCurrentUser() == null) {
            startActivity(new Intent(ProductDetail.this, RegisterActivity.class));
        } else {
            Calendar calendar = Calendar.getInstance();

            @SuppressLint("SimpleDateFormat") SimpleDateFormat currentDate = new SimpleDateFormat("MMM dd, yyyy");
            final String date = currentDate.format(calendar.getTime());

            @SuppressLint("SimpleDateFormat") SimpleDateFormat currentTime = new SimpleDateFormat("hh:mm a");
            final String time = currentTime.format(calendar.getTime());

            final String timeStamp = new SimpleDateFormat("dd MMM, yyyy ( h:mm a )").format(Calendar.getInstance().getTime());

            long unixTime = System.currentTimeMillis() / 1000L;
            HashMap<String, String> chatNotification = new HashMap<>();

            chatNotification.put("title", "New Message");
            chatNotification.put("from", UID);
            chatNotification.put("image", "");
            chatNotification.put("body", "");
            chatNotification.put("time", timeStamp);
            chatNotification.put("type", "order_chat");

            dbRef.child("Notifications").child(seller).push().setValue(chatNotification).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()) {

                    }
                }
            });

            dbRef.child("ChatsMetaData").child(UID).child(seller).child("last_update").setValue(unixTime);
            dbRef.child("ChatsMetaData").child(seller).child(UID).child("last_update").setValue(unixTime);

            DatabaseReference sender;
            final DatabaseReference receiver;


            sender = dbRef.child("Chats").child(UID).child(seller).push();
            receiver = dbRef.child("Chats").child(seller).child(UID).push();

            final String senderKey = sender.getKey();
            final String receiverKey = receiver.getKey();

            final HashMap<String, String> sendchats = new HashMap<>();

            sendchats.put("type", "order");
            sendchats.put("text", product);
            sendchats.put("sender", seller);
            sendchats.put("receiver", UID);
            sendchats.put("chat_id", senderKey);
            sendchats.put("date", date);
            sendchats.put("time", time);
            sendchats.put("read", "false");

            sender.setValue(sendchats).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()) {
                        HashMap<String, String> receivechats = new HashMap<>();

                        receivechats.put("type", "order");
                        receivechats.put("text", product);
                        receivechats.put("sender", UID);
                        receivechats.put("receiver", seller);
                        receivechats.put("chat_id", receiverKey);
                        receivechats.put("date", date);
                        receivechats.put("time", time);
                        receivechats.put("read", "false");

                        receiver.setValue(receivechats).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    Intent intent = new Intent(ProductDetail.this, ChatActivity.class);

                                    Bundle extras = new Bundle();

                                    String chatting_with = seller;

                                    extras.putString("chatting_with", chatting_with);

                                    intent.putExtras(extras);

                                    startActivity(intent);
                                }
                            }
                        });
                    }
                }
            });
        }
    }
}
