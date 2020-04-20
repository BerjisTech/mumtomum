package tech.berjis.mumtomum;

import android.os.Bundle;
import android.text.Html;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class ContactActivity extends AppCompatActivity {
    TextView agreementToTerms;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_terms);

        agreementToTerms = findViewById(R.id.agreementToTerms);
        agreementToTerms.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);

        agreementToTerms.setText(Html.fromHtml("<br />" +
                "<br />" +
                "<br />" +
                "<br />" +
                "<br />" +
                "<br />" +
                "<p>Berjis Technologies<br />" +
                "271<br />" +
                "Ruai, Nairobi 00520<br />" +
                "Kenya<br />" +
                "Phone: +254 725 227513<br />" +
                "berjistechnologies@gmail.com</p>"));
    }
}
