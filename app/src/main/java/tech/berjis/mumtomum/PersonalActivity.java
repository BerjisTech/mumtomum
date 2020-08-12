package tech.berjis.mumtomum;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class PersonalActivity extends AppCompatActivity {

    FirebaseAuth mAuth;
    DatabaseReference dbRef;

    RecyclerView transactions;
    List<Transactions> listData;
    TransactionsAdapter transactionsAdapter;

    TextView deposit, withdraw, balanceAmount, currencyText;
    String UID, uPhone, symbol;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_personal);

        initLayout();
        loadUserData();
        staticOnclicks();
        loadTransactions();
        totalBalance();
    }

    private void loadUserData() {
        dbRef.child("Users").child(UID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                symbol = Objects.requireNonNull(snapshot.child("currency_symbol").getValue()).toString();
                currencyText.setText(symbol);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void initLayout() {
        mAuth = FirebaseAuth.getInstance();
        dbRef = FirebaseDatabase.getInstance().getReference();
        dbRef.keepSynced(true);

        UID = Objects.requireNonNull(mAuth.getCurrentUser()).getUid();
        uPhone = mAuth.getCurrentUser().getPhoneNumber();

        deposit = findViewById(R.id.deposit);
        withdraw = findViewById(R.id.withdraw);
        transactions = findViewById(R.id.transactions);
        balanceAmount = findViewById(R.id.balanceAmount);
        currencyText = findViewById(R.id.currencyText);
    }

    private void staticOnclicks() {
        deposit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(PersonalActivity.this, WalletActivity.class));
            }
        });
        withdraw.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(PersonalActivity.this, WithdrawActivity.class));
            }
        });
    }

    private void loadTransactions() {
        listData = new ArrayList<>();
        listData.clear();
        transactions.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));

        dbRef.child("PersonalWallet").child(UID).limitToLast(10).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    listData.clear();
                    for (DataSnapshot npsnapshot : dataSnapshot.getChildren()) {
                        Transactions l = npsnapshot.getValue(Transactions.class);
                        listData.add(l);
                    }
                    Collections.reverse(listData);
                    transactionsAdapter = new TransactionsAdapter(listData, "wallet", symbol);
                    transactions.setAdapter(transactionsAdapter);

                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(PersonalActivity.this, "Kuna shida mahali", Toast.LENGTH_SHORT).show();
            }
        });

    }

    private void totalBalance() {
        dbRef.child("PersonalWallet").child(UID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists() && dataSnapshot.hasChildren()) {
                    long total = 0;
                    Long value;

                    for (DataSnapshot npsnapshot : dataSnapshot.getChildren()) {
                        if (!npsnapshot.child("status").exists() && npsnapshot.child("text_ref").exists()) {
                            String text_ref = Objects.requireNonNull(npsnapshot.child("text_ref").getValue()).toString();
                            long end_time = System.currentTimeMillis() / 1000L;
                            dbRef.child("PersonalWallet").child(UID).child(text_ref).child("end_time").setValue(end_time);
                            dbRef.child("PersonalWallet").child(UID).child(text_ref).child("status").setValue("cancelled");
                        }
                        if (npsnapshot.child("status").exists() && Objects.equals(npsnapshot.child("status").getValue(), "success")) {
                            value = (Long) npsnapshot.child("amount").getValue();

                            if (Objects.equals(npsnapshot.child("type").getValue(), "deposit")) {
                                total = total + value;
                            } else {
                                total = total - value;
                            }
                        }
                    }
                    NumberFormat nf = NumberFormat.getInstance();
                    nf.setMinimumFractionDigits(0);
                    nf.setMaximumFractionDigits(0);
                    String output = nf.format(total);
                    balanceAmount.setText(output);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
}
