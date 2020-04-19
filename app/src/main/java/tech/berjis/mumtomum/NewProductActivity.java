package tech.berjis.mumtomum;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.ViewPager;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;
import com.toptoche.searchablespinnerlibrary.SearchableSpinner;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public class NewProductActivity extends AppCompatActivity {

    FirebaseAuth mAuth;
    DatabaseReference dbRef, productRef;
    StorageReference storageReference;
    Uri filePath;
    String UID, category, productID = "", pName, pPrice, pDescription, hasImage = "";

    SearchableSpinner productCategory;
    ImageView addProduct, newImage, goBack;
    ViewPager imageRecycler;
    EditText productName, productPrice, productDescription;
    TextView newImageText, publish;
    List<GossipImages> gossipImagesData;
    GossipImagesPagerAdapter imagesAdapter;
    CheckBox pickup, delivery;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_product);


        mAuth = FirebaseAuth.getInstance();
        dbRef = FirebaseDatabase.getInstance().getReference();
        dbRef.keepSynced(true);
        UID = mAuth.getCurrentUser().getUid();
        storageReference = FirebaseStorage.getInstance().getReference();

        createProductNode();
        gossipImagesData = new ArrayList<>();

        addProduct = findViewById(R.id.addProduct);
        productCategory = findViewById(R.id.productCategory);
        newImage = findViewById(R.id.newImage);
        imageRecycler = findViewById(R.id.imageRecycler);
        productName = findViewById(R.id.productName);
        productPrice = findViewById(R.id.productPrice);
        productDescription = findViewById(R.id.productDescription);
        newImageText = findViewById(R.id.newImageText);
        pickup = findViewById(R.id.pickUp);
        delivery = findViewById(R.id.delivery);
        publish = findViewById(R.id.publish);
        goBack = findViewById(R.id.goBack);

        loadSpinners();

        addProduct.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                publishProduct();
            }
        });
        publish.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                publishProduct();
            }
        });
        newImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadCamera();
            }
        });
        newImageText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadCamera();
            }
        });
        goBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
    }

    private void loadCamera() {
        /*Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);*/

        CropImage.activity()
                .setGuidelines(CropImageView.Guidelines.ON)
                .start(this);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        /*if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK
                && data != null && data.getData() != null) {
            filePath = data.getData();
        }*/

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                filePath = result.getUri();
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
                Toast.makeText(this, "Error : " + error, Toast.LENGTH_SHORT).show();
            }
        }

        postImage();
    }

    public void postImage() {

        long unixTime = System.currentTimeMillis() / 1000L;
        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Uploading...");
        progressDialog.show();

        if (filePath != null) {

            final StorageReference ref = storageReference.child("Product Images/" + productID + unixTime + ".jpg");
            ref.putFile(filePath)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            ref.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    Uri downloadUrl = uri;
                                    final String image_url = downloadUrl.toString();

                                    DatabaseReference imageRef = dbRef.child("ProductImages").child(productID).push();
                                    String image_id = imageRef.getKey();
                                    imageRef.child("image_id").setValue(image_id);
                                    imageRef.child("parent_id").setValue(productID);
                                    imageRef.child("image").setValue(image_url).addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {
                                                hasImage = "hasImage";
                                                loadImages();
                                                progressDialog.dismiss();
                                            }
                                        }
                                    });
                                }
                            });
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            progressDialog.dismiss();
                            Toast.makeText(NewProductActivity.this, "Failed " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                            double progress = (100.0 * taskSnapshot.getBytesTransferred() / taskSnapshot
                                    .getTotalByteCount());
                            progressDialog.setMessage("Uploaded " + (int) progress + "%");
                        }
                    });
        } else {
            progressDialog.dismiss();
        }
    }

    public void loadImages() {
        imageRecycler.setVisibility(View.VISIBLE);
        gossipImagesData.clear();
        dbRef.child("ProductImages").child(productID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (DataSnapshot npsnapshot : dataSnapshot.getChildren()) {
                        GossipImages l = npsnapshot.getValue(GossipImages.class);
                        gossipImagesData.add(l);
                    }
                }
                Collections.reverse(gossipImagesData);
                imagesAdapter = new GossipImagesPagerAdapter(gossipImagesData, "small", "edit", "product");
                imageRecycler.setAdapter(imagesAdapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(NewProductActivity.this, "Kuna shida mahali", Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void loadSpinners() {
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.categories, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        productCategory.setAdapter(adapter);
        productCategory.setTitle("Search by category");
        productCategory.setPositiveButton("Cancel");
        productCategory.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position > 0) {
                    category = productCategory.getSelectedItem().toString();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    public void publishProduct() {

        pName = productName.getText().toString();
        pPrice = productPrice.getText().toString();
        pDescription = productDescription.getText().toString();
        if (pName.isEmpty()) {
            productName.setError("Enter a product name");
            productName.requestFocus();
            return;
        }
        if (pPrice.isEmpty()) {
            productPrice.setError("Enter a product price");
            productPrice.requestFocus();
            return;
        }
        if (pDescription.isEmpty()) {
            productDescription.setError("Enter a product description");
            productDescription.requestFocus();
            return;
        }
        if (category.equals("Choose Category")) {
            Toast.makeText(this, "Please choose a category first", Toast.LENGTH_SHORT).show();
            return;
        }

        if(!pickup.isChecked() && !delivery.isChecked()){
            Toast.makeText(this, "How do you plan to deliver this product?", Toast.LENGTH_LONG).show();
            return;
        }

        long unixTime = System.currentTimeMillis() / 1000L;
        final HashMap<String, Object> productHash = new HashMap<>();
        productHash.put("name", pName);
        productHash.put("category", category);
        productHash.put("description", pDescription);
        productHash.put("price", Long.parseLong(pPrice));
        productHash.put("date", unixTime);
        productHash.put("status", "available");
        productHash.put("pickup", pickup.isChecked());
        productHash.put("deliver", delivery.isChecked());

        productRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (hasImage.equals("")) {
                    Toast.makeText(NewProductActivity.this, "You need to add an Image First", Toast.LENGTH_SHORT).show();
                } else {
                    productRef.updateChildren(productHash).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                Toast.makeText(NewProductActivity.this, pName + " has successfully been added to your list of products", Toast.LENGTH_LONG).show();
                                startActivity(new Intent(NewProductActivity.this, ProductsActivity.class));
                            }
                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    public void createProductNode() {
        if (productID.equals("")) {
            productRef = dbRef.child("Products").push();
            productID = productRef.getKey();
            productRef.child("product_id").setValue(productID).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {

                }
            });
            productRef.child("seller").setValue(UID).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {

                }
            });
        }
    }

    @Override
    public void onBackPressed() {
        new AlertDialog.Builder(this)
                .setTitle("Delete entry")
                .setMessage("Do you want to cancel this product?")
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dbRef.child("Products").child(productID).removeValue();
                        NewProductActivity.super.finish();
                    }
                })
                .setNegativeButton(android.R.string.no, null)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }

}
