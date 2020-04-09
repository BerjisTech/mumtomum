package tech.berjis.mumtomum;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class MainActivity extends AppCompatActivity {

    FirebaseDatabase firebaseDatabase;

    ImageView logo;

    FirebaseAuth mAuth;
    DatabaseReference dbRef;
    String UID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (firebaseDatabase == null) {
            firebaseDatabase = FirebaseDatabase.getInstance();
        }

        mAuth = FirebaseAuth.getInstance();
        dbRef = FirebaseDatabase.getInstance().getReference();
        dbRef.keepSynced(true);
        UID = mAuth.getCurrentUser().getUid();

        logo = findViewById(R.id.logo);

    }

    @Override
    protected void onStart() {
        super.onStart();

        final Animation myAnim = AnimationUtils.loadAnimation(MainActivity.this, R.anim.bounce);
        BounceInterpolator interpolator = new BounceInterpolator(0.1, 20);
        myAnim.setInterpolator(interpolator);
        myAnim.setRepeatMode(Animation.RESTART);
        myAnim.setRepeatCount(Animation.INFINITE);
        logo.startAnimation(myAnim);

        new android.os.Handler().postDelayed(
                new Runnable() {
                    public void run() {
                        unloggedState();
                    }
                }, 3000);

    }

    public void unloggedState() {
        if (mAuth.getCurrentUser() == null) {
            Intent intent = new Intent(MainActivity.this, ProductsActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        }else{
            Intent intent = new Intent(MainActivity.this, WalletActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        }
    }
}
