package tech.berjis.mumtomum;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

public class UserWalletDetails extends AppCompatActivity {

    FirebaseAuth mAuth;
    DatabaseReference dbRef;
    String UID;

    EditText firstName, lastName, email;
    TextView cancelWallet, createWallet;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_wallet_details);

        mAuth = FirebaseAuth.getInstance();
        dbRef = FirebaseDatabase.getInstance().getReference();
        dbRef.keepSynced(true);
        UID = mAuth.getCurrentUser().getUid();

        firstName = findViewById(R.id.firstName);
        lastName = findViewById(R.id.lastName);
        email = findViewById(R.id.email);
        cancelWallet = findViewById(R.id.cancelWallet);
        createWallet = findViewById(R.id.createWallet);

        createWallet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String emailtext = email.getText().toString();
                String firstname = firstName.getText().toString();
                String lastname = lastName.getText().toString();

                if (firstname.equals("")) {
                    firstName.setError("Kindly enter your first name");
                    return;
                }
                if (lastname.equals("")) {
                    lastName.setError("Kindly enter your last name");
                    return;
                }
                if (emailtext.equals("") || !isValidEmail(emailtext)) {
                    email.setError("Kindly enter your email");
                    return;
                }

                HashMap<String, Object> walletHash = new HashMap<>();

                walletHash.put("email", emailtext);
                walletHash.put("first_name", firstname);
                walletHash.put("last_name", lastname);

                dbRef.child("Users").child(UID).updateChildren(walletHash).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(UserWalletDetails.this, "Successfully updated your information", Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(UserWalletDetails.this, WalletActivity.class));
                        } else {
                            Toast.makeText(UserWalletDetails.this, "Couldn't updated your information", Toast.LENGTH_LONG).show();
                        }
                    }
                });
            }
        });

        cancelWallet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(UserWalletDetails.this, ProfileActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
            }
        });

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent(UserWalletDetails.this, ProfileActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }

    public static boolean isValidEmail(CharSequence target) {
        return (!TextUtils.isEmpty(target) && Patterns.EMAIL_ADDRESS.matcher(target).matches());
    }
}
