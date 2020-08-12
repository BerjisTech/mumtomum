package tech.berjis.mumtomum;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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

public class GroupFragmentTransactions extends Fragment {

    DatabaseReference dbRef;
    private Context mContext;
    private String group, currency;
    private List<Transactions> listData;
    private TransactionsAdapter transactionsAdapter;
    private RecyclerView transactions;

    public GroupFragmentTransactions(Context mContext, String group) {
        this.mContext = mContext;
        this.group = group;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_group_transactions, container, false);

        initLayout(view);
        loadGroupData();
        loadTransactions();
        return view;
    }

    private void initLayout(View view) {

        dbRef = FirebaseDatabase.getInstance().getReference();
        transactions = view.findViewById(R.id.transactions);
    }

    private void loadGroupData() {
        dbRef.child("Groups").child(group).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                currency = snapshot.child("symbol").getValue().toString();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void loadTransactions() {
        listData = new ArrayList<>();
        listData.clear();
        transactions.setLayoutManager(new LinearLayoutManager(mContext, LinearLayoutManager.VERTICAL, false));

        dbRef.child("GroupWallet").child(group).limitToLast(10).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    listData.clear();
                    for (DataSnapshot npsnapshot : dataSnapshot.getChildren()) {
                        Transactions l = npsnapshot.getValue(Transactions.class);
                        listData.add(l);
                    }
                    Collections.reverse(listData);
                    transactionsAdapter = new TransactionsAdapter(listData, "group", currency);
                    transactions.setAdapter(transactionsAdapter);

                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(mContext, "Kuna shida mahali", Toast.LENGTH_SHORT).show();
            }
        });

    }
}
