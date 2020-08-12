package tech.berjis.mumtomum;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.hbb20.CountryCodePicker;

public class RegisterActivity extends AppCompatActivity {

    MediaPlayer musicPlayer;
    AudioManager audioManager;

    TextView greetings, description, join, sendPhone;
    CountryCodePicker ccp;
    EditText mPhone;
    ScrollView phoneVerificationScreen;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        greetings = findViewById(R.id.greetings);
        description = findViewById(R.id.description);
        join = findViewById(R.id.join);
        mPhone = findViewById(R.id.userphone);
        ccp = findViewById(R.id.country_code);
        sendPhone = findViewById(R.id.sendPhone);
        phoneVerificationScreen = findViewById(R.id.phoneVerificationScreen);


        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        musicPlayer = MediaPlayer.create(this, R.raw.blue_stone_alley);
        musicPlayer.setLooping(true);
        int MAX_VOLUME = 100;
        final float volume = (float) (1 - (Math.log(MAX_VOLUME - 50) / Math.log(MAX_VOLUME)));
        musicPlayer.setVolume(volume, volume);
        musicPlayer.start();

        join.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                join();
            }
        });

        sendPhone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String mobile = mPhone.getText().toString().trim();
                String countryCode = "254";
                String country = ccp.getSelectedCountryName();
                String country_code = ccp.getSelectedCountryNameCode();

                if(mobile.isEmpty() || mobile.length() < 9){
                    mPhone.setError("Enter a valid mobile");
                    mPhone.requestFocus();
                    return;
                }

                Intent intent = new Intent(RegisterActivity.this, VerifyPhoneActivity.class);
                intent.putExtra("mobile", mobile);
                intent.putExtra("countryCode", countryCode);
                intent.putExtra("country", country);
                intent.putExtra("country_code", country_code);
                startActivity(intent);
            }
        });

    }

    @Override
    protected void onStop() {
        super.onStop();
        musicPlayer.stop();
    }

    public void join() {
        greetings.animate()
                .translationX(-greetings.getWidth())
                .alpha(0.0f)
                .setDuration(150)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);
                        description.animate()
                                .translationX(-description.getWidth())
                                .alpha(0.0f)
                                .setDuration(150)
                                .setListener(new AnimatorListenerAdapter() {
                                    @Override
                                    public void onAnimationEnd(Animator animation) {
                                        super.onAnimationEnd(animation);
                                        join.animate()
                                                .translationX(-join.getWidth())
                                                .alpha(0.0f)
                                                .setDuration(150)
                                                .setListener(new AnimatorListenerAdapter() {
                                                    @Override
                                                    public void onAnimationEnd(Animator animation) {
                                                        super.onAnimationEnd(animation);
                                                        phoneVerificationScreen.setVisibility(View.VISIBLE);
                                                    }
                                                });
                                    }
                                });
                    }
                });
    }

    public void onCountryPickerClick(View view) {
        ccp.setOnCountryChangeListener(new CountryCodePicker.OnCountryChangeListener() {
            @Override
            public void onCountrySelected() {
                Toast.makeText(RegisterActivity.this, "Alert : " + ccp.getSelectedCountryCodeWithPlus(), Toast.LENGTH_SHORT).show();
                String selected_country_code = ccp.getSelectedCountryCodeWithPlus();
            }
        });
    }
}