package tech.berjis.mumtomum;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class GroupActivity extends AppCompatActivity {

    ViewPager groupViewPager;
    TabLayout groupTabs;
    TextView group_name;
    ImageView settings;

    FirebaseAuth mAuth;
    DatabaseReference dbRef;
    String UID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group);

        initLayout();
    }

    private void initLayout() {
        mAuth = FirebaseAuth.getInstance();
        dbRef = FirebaseDatabase.getInstance().getReference();
        dbRef.keepSynced(true);

        groupViewPager = findViewById(R.id.groupViewPager);
        groupTabs = findViewById(R.id.groupTabs);
        group_name = findViewById(R.id.group_name);
        settings = findViewById(R.id.settings);

        UID = Objects.requireNonNull(mAuth.getCurrentUser()).getUid();

        loadGroup();
    }

    private void loadGroup() {
        Intent groupIntent = getIntent();
        Bundle groupBundle = groupIntent.getExtras();
        final String group = groupBundle.getString("group_id");

        dbRef.child("Groups").child(group).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                group_name.setText(Objects.requireNonNull(snapshot.child("name").getValue()).toString());
                String owner = Objects.requireNonNull(snapshot.child("owner").getValue()).toString();

                if (owner.equals(UID)) {
                    settings.setVisibility(View.VISIBLE);
                    settings.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent g_i = new Intent(GroupActivity.this, GroupSettingsActivity.class);
                            Bundle g_b = new Bundle();
                            g_b.putString("group_id", group);
                            g_i.putExtras(g_b);
                            startActivity(g_i);
                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        initViewPager(group);
        initTabLayout();
    }

    private void initViewPager(String group) {
        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());
        adapter.addFrag(new GroupFragment(GroupActivity.this, group), "Summary");
        adapter.addFrag(new GroupFragmentChat(GroupActivity.this, group), "Chats");
        adapter.addFrag(new GroupFragmentTransactions(GroupActivity.this, group), "Transactions");
        groupViewPager.setAdapter(adapter);
    }

    private void initTabLayout() {
        groupTabs.setupWithViewPager(groupViewPager);
        groupTabs.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                groupViewPager.setCurrentItem(tab.getPosition());
                switch (tab.getPosition()) {
                    case 0:

                        break;
                    case 1:

                        break;
                    case 2:

                        break;
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
            }
        });
    }

    private static class ViewPagerAdapter extends FragmentPagerAdapter {
        private final List<Fragment> mFragmentList = new ArrayList<>();
        private final List<String> mFragmentTitleList = new ArrayList<>();

        ViewPagerAdapter(FragmentManager manager) {
            super(manager);
        }

        @NonNull
        @Override
        public Fragment getItem(int position) {
            return mFragmentList.get(position);
        }

        @Override
        public int getCount() {
            return mFragmentList.size();
        }

        void addFrag(Fragment fragment, String title) {
            mFragmentList.add(fragment);
            mFragmentTitleList.add(title);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mFragmentTitleList.get(position);
        }
    }
}
