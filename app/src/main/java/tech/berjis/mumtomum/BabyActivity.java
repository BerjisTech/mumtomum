package tech.berjis.mumtomum;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.flutterwave.raveandroid.RaveConstants;
import com.flutterwave.raveandroid.RavePayActivity;
import com.flutterwave.raveandroid.RavePayManager;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class BabyActivity extends AppCompatActivity {

    FirebaseAuth mAuth;
    DatabaseReference dbRef, babyRef;
    String UID, phone, amount;
    TextView deposit, amountText;
    EditText amountNumber;
    ConstraintLayout transactionPanel;
    ImageView closeTransactionPanel, back;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_baby);

        mAuth = FirebaseAuth.getInstance();
        dbRef = FirebaseDatabase.getInstance().getReference();
        UID = mAuth.getCurrentUser().getUid();
        phone = mAuth.getCurrentUser().getPhoneNumber();

        deposit = findViewById(R.id.deposit);
        amountNumber = findViewById(R.id.amountNumber);
        amountText = findViewById(R.id.amountText);
        transactionPanel = findViewById(R.id.transactionPanel);
        closeTransactionPanel = findViewById(R.id.closeTransactionPanel);
        back = findViewById(R.id.back);

        amountNumber.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEND) {
                    amount = amountNumber.getText().toString();
                    if (!amount.equals("")) {
                        new AlertDialog.Builder(BabyActivity.this)
                                .setTitle("Delete entry")
                                .setMessage("You are about to deposit " + amount + " to BabyWallet.")
                                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {
                                        validateEntries();
                                    }
                                })
                                .setNegativeButton("Cancel", null)
                                .show();
                    }else{
                        amountNumber.setError("Please enter a value greater than zero (0)");
                    }
                    return true;
                }
                return false;
            }
        });
        closeTransactionPanel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                transactionPanel.animate()
                        .translationY(transactionPanel.getHeight())
                        .alpha(1.0f)
                        .setDuration(300)
                        .setListener(new AnimatorListenerAdapter() {
                            @Override
                            public void onAnimationEnd(Animator animation) {
                                super.onAnimationEnd(animation);
                                transactionPanel.setVisibility(View.GONE);
                            }
                        });
            }
        });

        deposit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                transactionPanel.setVisibility(View.VISIBLE);
                transactionPanel.animate()
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
        });
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                BabyActivity.super.finish();
            }
        });
    }

    private void validateEntries() {
        babyRef = dbRef.child("BabyWallet").child(UID).push();
        String text_ref = babyRef.getKey();
        long time_start = System.currentTimeMillis() / 1000L;
        babyRef.child("time_start").setValue(time_start);
        Toast.makeText(this, text_ref, Toast.LENGTH_SHORT).show();

        String email = "bo.kouru@gmail.com";
        String publicKey = getString(R.string.public_key);
        String encryptionKey = getString(R.string.encryption_key);
        String narration = "BabyWallet savings";
        String currency = "KES";
        String country = "KE";
        String fName = "Benedict";
        String lName = "Ouma";
        String phoneNumber = phone; //phone.substring(phone.length() - 12);

        babyRef.child("user").setValue(UID);
        babyRef.child("narration").setValue(narration);
        babyRef.child("amount").setValue(amount);

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
                    .setTxRef(text_ref)
                    .acceptMpesaPayments(true)
                    .acceptAccountPayments(true)
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
                    .acceptBankTransferPayments(false)
                    .acceptUssdPayments(false)
                    .acceptBarterPayments(false)
                    .onStagingEnv(false)
                    .isPreAuth(false)
                    .showStagingLabel(false)
//                    .setMeta(meta)
//                    .withTheme(R.style.TestNewTheme)
                    .shouldDisplayFee(false);

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
                babyRef.child("end_time").setValue(end_time);
                babyRef.child("status").setValue("success");
                Toast.makeText(this, "SUCCESS " + message, Toast.LENGTH_SHORT).show();
            } else if (resultCode == RavePayActivity.RESULT_ERROR) {
                long end_time = System.currentTimeMillis() / 1000L;
                babyRef.child("end_time").setValue(end_time);
                babyRef.child("status").setValue("error");
                Toast.makeText(this, "ERROR " + message, Toast.LENGTH_SHORT).show();
            } else if (resultCode == RavePayActivity.RESULT_CANCELLED) {
                long end_time = System.currentTimeMillis() / 1000L;
                babyRef.child("end_time").setValue(end_time);
                babyRef.child("status").setValue("cancelled");
                Toast.makeText(this, "CANCELLED " + message, Toast.LENGTH_SHORT).show();
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }
}
