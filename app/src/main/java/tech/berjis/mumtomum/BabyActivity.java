package tech.berjis.mumtomum;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.flutterwave.raveandroid.Meta;
import com.flutterwave.raveandroid.RaveConstants;
import com.flutterwave.raveandroid.RavePayActivity;
import com.flutterwave.raveandroid.RavePayManager;
import com.flutterwave.raveandroid.Utils;
import com.flutterwave.raveandroid.responses.SubAccount;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.List;

public class BabyActivity extends AppCompatActivity {

    long unixTime = System.currentTimeMillis() / 1000L;
    List<Meta> meta = new ArrayList<>();
    FirebaseAuth mAuth;
    DatabaseReference dbRef, babyRef;
    String UID, phone, textRef;
    TextView deposit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_baby);

        mAuth = FirebaseAuth.getInstance();
        dbRef = FirebaseDatabase.getInstance().getReference();
        UID = mAuth.getCurrentUser().getUid();
        phone = mAuth.getCurrentUser().getPhoneNumber();

        deposit = findViewById(R.id.deposit);

        deposit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                validateEntries();
            }
        });
    }

    private void validateEntries() {
        babyRef = dbRef.child("BabyWallet").child(UID).push();
        String text_ref = babyRef.getKey();
        String email = "bo.kouru@gmail.com";
        String amount = "100";
        String publicKey = RaveConstants.PUBLIC_KEY;
        String encryptionKey = RaveConstants.ENCRYPTION_KEY;
        String txRef = text_ref;
        String narration = "BabyWallet savings";
        String currency = "KES";
        String country = "KE";
        String fName = "Benedict";
        String lName = "Ouma";
        String phoneNumber = phone; //phone.substring(phone.length() - 12);

        boolean valid = true;

        if (amount.length() == 0) {
            amount = "0";
        }

        //isAmountValid for compulsory fields

        if (valid) {
            RavePayManager ravePayManager = new RavePayManager(this).setAmount(Double.parseDouble(amount))
                    .setCountry(country)
                    .setCurrency(currency)
                    .setEmail(email)
                    .setfName(fName)
                    .setlName(lName)
                    .setPhoneNumber(phoneNumber)
                    .setNarration(narration)
                    .setPublicKey(publicKey)
                    .setEncryptionKey(encryptionKey)
                    .setTxRef(txRef)
                    .acceptMpesaPayments(true)
                    .acceptAccountPayments(false)
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
                    .acceptBankTransferPayments(true)
                    .acceptUssdPayments(true)
                    .acceptBarterPayments(true)
                    .onStagingEnv(true)
                    .showStagingLabel(false)
//                    .setMeta(meta)
//                    .withTheme(R.style.TestNewTheme)
                    .shouldDisplayFee(true)
                    .acceptBankTransferPayments(true, true);


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
                Toast.makeText(this, "SUCCESS " + message, Toast.LENGTH_SHORT).show();
            } else if (resultCode == RavePayActivity.RESULT_ERROR) {
                Toast.makeText(this, "ERROR " + message, Toast.LENGTH_SHORT).show();
            } else if (resultCode == RavePayActivity.RESULT_CANCELLED) {
                Toast.makeText(this, "CANCELLED " + message, Toast.LENGTH_SHORT).show();
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }
}
