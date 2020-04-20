package tech.berjis.mumtomum;

import android.os.Bundle;
import android.text.Html;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class Faq extends AppCompatActivity {
    TextView agreementToTerms;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_terms);

        agreementToTerms = findViewById(R.id.agreementToTerms);

        agreementToTerms.setText(Html.fromHtml(
                "<h1>Frequently Asked Questions</h1>" +
                        "<br />" +
                        "<p>" +
                        "<strong>What is Mum To Mum</strong><br />" +
                        "<em>text</em>" +
                        "<br /><br />" +
                        "<strong>What is MumWallet</strong><br />" +
                        "<em>text</em>" +
                        "<br /><br />" +
                        "<strong>What is BabyWallet</strong><br />" +
                        "<em>text</em>" +
                        "<br /><br />" +
                        "<strong>What is the difference between MumWallet and BabyWallet</strong><br />" +
                        "<em>text</em>" +
                        "<br /><br />" +
                        "<strong>How do I apply for a financial boost</strong><br />" +
                        "<em>text</em>" +
                        "<br /><br />" +
                        "<strong>How do I know if I qualify for a financial boost</strong><br />" +
                        "<em>text</em>" +
                        "<br /><br />" +
                        "<strong>How do I know if I qualify for a loan</strong><br />" +
                        "<em>text</em>" +
                        "<br /><br />" +
                        "<strong>How do I qualify for a loan</strong><br />" +
                        "<em>text</em>" +
                        "<br /><br />" +
                        "<strong>How do I upload a product</strong><br />" +
                        "<em>text</em>" +
                        "<br /><br />" +
                        "<strong>How do I contact my customers or sellers</strong><br />" +
                        "<em>text</em>" +
                        "<br /><br />" +
                        "<strong>What happens if I get conned?</strong><br />" +
                        "<em>text</em>" +
                        "<br /><br />" +
                        "<strong>How do I deposit money to my Wallets</strong><br />" +
                        "<em>text</em>" +
                        "<br /><br />" +
                        "<strong>How do I withdraw?</strong><br />" +
                        "<em>text</em>" +
                        "<br /><br />" +
                        "<strong>How long does withdrawal take?</strong><br />" +
                        "<em>text</em>" +
                        "<br /><br />" +
                        "<strong>Why is the withdrawal limit kshs 1000?</strong><br />" +
                        "<em>text</em><br /><br />" +
                        "<strong>What is the deposit minimum</strong><br />" +
                        "<em>text</em>" +
                        "<br /><br />" +
                        "<strong>What are the transaction charges of depositing or withdrawing</strong><br />" +
                        "<em>text</em>" +
                        "<br /><br />" +
                        "</p>"
        ));
    }
}
