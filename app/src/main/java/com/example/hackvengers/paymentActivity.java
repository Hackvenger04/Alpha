package com.example.hackvengers;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import com.razorpay.Checkout;
import com.razorpay.PaymentResultListener;

import org.json.JSONObject;

public class paymentActivity extends AppCompatActivity implements PaymentResultListener {
    Button payBtn;
    EditText amountEt;
    int famount;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.payment);
//        Toast.makeText(this,"Hello",Toast.LENGTH_SHORT).show();
        Checkout.preload(getApplicationContext());
//        Toast.makeText(this,"Hello2",Toast.LENGTH_SHORT).show();
        payBtn=findViewById(R.id.payBtn);
//        Toast.makeText(this,"Hello3",Toast.LENGTH_SHORT).show();
        amountEt=findViewById(R.id.amountEt);
//        Toast.makeText(this,"Hello3",Toast.LENGTH_SHORT).show();

        payBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
            makePayment();
            }


        });
    }
    private void makePayment() {
        String amount=amountEt.getText().toString();
        famount = Math.round(Float.parseFloat(amount) * 100);
        Checkout checkout = new Checkout();

        checkout.setKeyID("rzp_test_bfAlsdUIR0H8pc");
        checkout.setImage(R.drawable.logo);
        final Activity activity = this;
        try {
            JSONObject options = new JSONObject();

            options.put("name", "ALPHA Donation");
            options.put("description", "Reference No. #123456");
            options.put("image", "https://s3.amazonaws.com/rzp-mobile/images/rzp.png");
            //  options.put("order_id", "order_DBJOWzybf0sJbb");//from response of step 3.
            options.put("theme.color", "#3399cc");
            options.put("currency", "INR");
            options.put("amount",famount);//pass amount in currency subunits
            options.put("prefill.email", "hackvengers01@gmail.com");
            options.put("prefill.contact","8275087353");
            JSONObject retryObj = new JSONObject();
            retryObj.put("enabled", true);
            retryObj.put("max_count", 4);
            options.put("retry", retryObj);

            checkout.open(activity, options);

        } catch(Exception e) {
            Log.e("Tag", "Error in starting Razorpay Checkout", e);
        }
    }

    @Override
    public void onPaymentSuccess(String s) {
        Toast toast = Toast.makeText(paymentActivity.this, "Payment Successful", Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.show();
    }

    @Override
    public void onPaymentError(int i, String s) {
        Toast toast = Toast.makeText(paymentActivity.this, "Payment Failure", Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.show();
    }
}


