package tech.berjis.mumtomum;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.SearchView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

public class GroupsActivity extends AppCompatActivity {

    TextView bigTitle, smallTitle, chamaButton, nextButton, noGroupMessage, joinGroup, createGroup, orText;
    EditText chamaText;
    ImageView chamaVector, backButton;
    RecyclerView groupsRecycler;
    SearchView searchGroups;
    View half;
    String tab = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_groups);

        bigTitle = findViewById(R.id.bigTitle);
        smallTitle = findViewById(R.id.smallTitle);
        chamaButton = findViewById(R.id.chamaButton);
        nextButton = findViewById(R.id.nextButton);
        noGroupMessage = findViewById(R.id.noGroupMessage);
        joinGroup = findViewById(R.id.joinGroup);
        createGroup = findViewById(R.id.createGroup);
        orText = findViewById(R.id.orText);
        chamaText = findViewById(R.id.chamaText);
        chamaVector = findViewById(R.id.chamaVector);
        backButton = findViewById(R.id.backButton);
        half = findViewById(R.id.half);
        groupsRecycler = findViewById(R.id.groupsRecycler);
        searchGroups = findViewById(R.id.searchGroup);

        chamaButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showCreateOrJoin();
            }
        });
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showWelcomeMessage();
            }
        });
        createGroup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showCreateGroup();
            }
        });
        joinGroup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showGroupsRecycler();
            }
        });
    }

    private void showWelcomeMessage() {
        tab = "welcome";
        bigTitle.setVisibility(View.VISIBLE);
        smallTitle.setVisibility(View.VISIBLE);
        chamaButton.setVisibility(View.VISIBLE);
        nextButton.setVisibility(View.GONE);
        noGroupMessage.setVisibility(View.GONE);
        joinGroup.setVisibility(View.GONE);
        createGroup.setVisibility(View.GONE);
        orText.setVisibility(View.GONE);
        chamaText.setVisibility(View.GONE);
        chamaVector.setVisibility(View.VISIBLE);
        backButton.setVisibility(View.GONE);
        half.setVisibility(View.GONE);
        groupsRecycler.setVisibility(View.GONE);
        searchGroups.setVisibility(View.GONE);
    }

    private void showCreateOrJoin() {
        tab = "createJoin";
        bigTitle.setVisibility(View.GONE);
        smallTitle.setVisibility(View.GONE);
        chamaButton.setVisibility(View.GONE);
        nextButton.setVisibility(View.GONE);
        noGroupMessage.setVisibility(View.VISIBLE);
        joinGroup.setVisibility(View.VISIBLE);
        createGroup.setVisibility(View.VISIBLE);
        orText.setVisibility(View.VISIBLE);
        chamaText.setVisibility(View.GONE);
        chamaVector.setVisibility(View.VISIBLE);
        backButton.setVisibility(View.GONE);
        half.setVisibility(View.VISIBLE);
        groupsRecycler.setVisibility(View.GONE);
        searchGroups.setVisibility(View.GONE);
    }

    private void showCreateGroup() {
        tab = "createGroup";
        bigTitle.setVisibility(View.GONE);
        smallTitle.setVisibility(View.GONE);
        chamaButton.setVisibility(View.GONE);
        nextButton.setVisibility(View.VISIBLE);
        noGroupMessage.setVisibility(View.GONE);
        joinGroup.setVisibility(View.GONE);
        createGroup.setVisibility(View.GONE);
        orText.setVisibility(View.GONE);
        chamaText.setVisibility(View.VISIBLE);
        chamaVector.setVisibility(View.VISIBLE);
        backButton.setVisibility(View.VISIBLE);
        half.setVisibility(View.GONE);
        groupsRecycler.setVisibility(View.GONE);
        searchGroups.setVisibility(View.GONE);
    }


    private void showGroupsRecycler() {
        tab = "joinGroup";
        bigTitle.setVisibility(View.GONE);
        smallTitle.setVisibility(View.GONE);
        chamaButton.setVisibility(View.GONE);
        nextButton.setVisibility(View.VISIBLE);
        noGroupMessage.setVisibility(View.GONE);
        joinGroup.setVisibility(View.GONE);
        createGroup.setVisibility(View.GONE);
        orText.setVisibility(View.GONE);
        chamaText.setVisibility(View.VISIBLE);
        chamaVector.setVisibility(View.VISIBLE);
        backButton.setVisibility(View.VISIBLE);
        half.setVisibility(View.GONE);
        groupsRecycler.setVisibility(View.VISIBLE);
        searchGroups.setVisibility(View.VISIBLE);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();

        if(tab.equals("welcome")){
            GroupsActivity.super.finish();
        }
        if(tab.equals("createJoin")){
            showWelcomeMessage();
            return;
        }
        if(tab.equals("createGroup")){
            showCreateOrJoin();
            return;
        }
        if(tab.equals("joinGroup")){
            showCreateOrJoin();
            return;
        }
        if (tab.equals("")){
            GroupsActivity.super.finish();
        }
    }
}
