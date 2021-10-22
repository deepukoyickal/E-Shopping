package com.example.e_shopping;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.e_shopping.Prevalent.Prevalent;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

public class PaymentActivity extends AppCompatActivity {

    EditText amount, note, name, upivirtualid;
    Button send;
    String TAG = "main";
   String totalAmount,Name,Address,Phone,City,ProductId,ProductQuantity;
    final int UPI_PAYMENT = 0;
    DatabaseReference orderKey,userRef,itemRef;
    public List<String> myList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment);
        totalAmount = getIntent().getStringExtra("Total Price");
        Name = getIntent().getStringExtra("Name");
        Phone = getIntent().getStringExtra("Phone");
        City = getIntent().getStringExtra("City");
        Address = getIntent().getStringExtra("Address");

        send = (Button) findViewById(R.id.send);
        amount = (EditText) findViewById(R.id.amount_et);
        note = (EditText) findViewById(R.id.note);
        name = (EditText) findViewById(R.id.name);
        upivirtualid = (EditText) findViewById(R.id.upi_id);
        amount.setText(totalAmount);
        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Getting the values from the EditTexts
                if (TextUtils.isEmpty(name.getText().toString().trim())) {
                    Toast.makeText(PaymentActivity.this, " Name is invalid", Toast.LENGTH_SHORT).show();
                } else if (TextUtils.isEmpty(upivirtualid.getText().toString().trim())) {
                    Toast.makeText(PaymentActivity.this, " UPI ID is invalid", Toast.LENGTH_SHORT).show();
                } else if (TextUtils.isEmpty(note.getText().toString().trim())) {
                    Toast.makeText(PaymentActivity.this, " Note is invalid", Toast.LENGTH_SHORT).show();
                } else if (TextUtils.isEmpty(totalAmount)) {
                    Toast.makeText(PaymentActivity.this, " Amount is invalid", Toast.LENGTH_SHORT).show();
                } else {
                    payUsingUpi(name.getText().toString(), upivirtualid.getText().toString(),
                            note.getText().toString(), totalAmount);
                }
            }
        });
    }

    void payUsingUpi(String name, String upiId, String note, String amount) {
        Log.e("main ", "name " + name + "--up--" + upiId + "--" + note + "--" + amount);
        Uri uri = Uri.parse("upi://pay").buildUpon()
                .appendQueryParameter("pa", upiId)
                .appendQueryParameter("pn", name)
                //.appendQueryParameter("mc", "")
                //.appendQueryParameter("tid", "02125412")
                //.appendQueryParameter("tr", "25584584")
                .appendQueryParameter("tn", note)
                .appendQueryParameter("am", amount)
                .appendQueryParameter("cu", "INR")
                //.appendQueryParameter("refUrl", "blueapp")
                .build();
        Intent upiPayIntent = new Intent(Intent.ACTION_VIEW);
        upiPayIntent.setData(uri);
        // will always show a dialog to user to choose an app
        Intent chooser = Intent.createChooser(upiPayIntent, "Pay with");
        // check if intent resolves
        if (null != chooser.resolveActivity(getPackageManager())) {
            startActivityForResult(chooser, UPI_PAYMENT);
        } else {
            Toast.makeText(PaymentActivity.this, "No UPI app found, please install one to continue", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.e("main ", "response " + resultCode);
        /*
       E/main: response -1
       E/UPI: onActivityResult: txnId=AXI4a3428ee58654a938811812c72c0df45&responseCode=00&Status=SUCCESS&txnRef=922118921612
       E/UPIPAY: upiPaymentDataOperation: txnId=AXI4a3428ee58654a938811812c72c0df45&responseCode=00&Status=SUCCESS&txnRef=922118921612
       E/UPI: payment successfull: 922118921612
         */
        switch (requestCode) {
            case UPI_PAYMENT:
                if ((RESULT_OK == resultCode) || (resultCode == 11)) {
                    if (data != null) {
                        String trxt = data.getStringExtra("response");
                        Log.e("UPI", "onActivityResult: " + trxt);
                        ArrayList<String> dataList = new ArrayList<>();
                        dataList.add(trxt);
                        upiPaymentDataOperation(dataList);
                    } else {
                        Log.e("UPI", "onActivityResult: " + "Return data is null");
                        ArrayList<String> dataList = new ArrayList<>();
                        dataList.add("nothing");
                        upiPaymentDataOperation(dataList);
                    }
                } else {
                    //when user simply back without payment
                    Log.e("UPI", "onActivityResult: " + "Return data is null");
                    ArrayList<String> dataList = new ArrayList<>();
                    dataList.add("nothing");
                    upiPaymentDataOperation(dataList);
                }
                break;
        }
    }

    private void upiPaymentDataOperation(ArrayList<String> data) {
        if (isConnectionAvailable(PaymentActivity.this)) {
            String str = data.get(0);
            Log.e("UPIPAY", "upiPaymentDataOperation: " + str);
            String paymentCancel = "";
            if (str == null) str = "discard";
            String status = "";
            String approvalRefNo = "";
            String response[] = str.split("&");
            for (int i = 0; i < response.length; i++) {
                String equalStr[] = response[i].split("=");
                if (equalStr.length >= 2) {
                    if (equalStr[0].toLowerCase().equals("Status".toLowerCase())) {
                        status = equalStr[1].toLowerCase();
                    } else if (equalStr[0].toLowerCase().equals("ApprovalRefNo".toLowerCase()) || equalStr[0].toLowerCase().equals("txnRef".toLowerCase())) {
                        approvalRefNo = equalStr[1];
                    }
                } else {
                    paymentCancel = "Payment cancelled by user.";
                }
            }
            if (status.equals("success")) {
                //Code to handle successful transaction here.
                Toast.makeText(PaymentActivity.this, "Transaction successful.", Toast.LENGTH_SHORT).show();
                Log.e("UPI", "payment successfull: " + approvalRefNo);
                Intent intent = new Intent(PaymentActivity.this, HomeActivity.class);

                final String saveCurrentDate, saveCurrentTime;

                Calendar calForDate = Calendar.getInstance();
                SimpleDateFormat currentDate = new SimpleDateFormat("MMM dd, yyyy");
                saveCurrentDate = currentDate.format(calForDate.getTime());

                SimpleDateFormat currentTime = new SimpleDateFormat("HH:mm:ss a");
                saveCurrentTime = currentDate.format(calForDate.getTime());

                final DatabaseReference ordersRef = FirebaseDatabase.getInstance().getReference()
                        .child("Orders");

                String OrderKEY = ordersRef.push().getKey();
                orderKey = ordersRef.child(OrderKEY);


                HashMap<String, Object> ordersMap = new HashMap<>();
                ordersMap.put("ID",OrderKEY);
                ordersMap.put("totalAmount", totalAmount);
                ordersMap.put("name", Name);
                ordersMap.put("phone", Phone);
                ordersMap.put("address",Address);
                ordersMap.put("city", City);
                ordersMap.put("date", saveCurrentDate);
                ordersMap.put("time", saveCurrentTime);
                ordersMap.put("state", "not shipped");
                ordersMap.put("paymentmethod","Google pay");

                orderKey.updateChildren(ordersMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task)
                    {
                        if (task.isSuccessful())
                        {
                            FirebaseDatabase.getInstance().getReference()
                                    .child("Cart List")
                                    .child("User View")
                                    .child(Prevalent.currentOnlineUser.getPhone())
                                    .removeValue()
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task)
                                        {
                                            if (task.isSuccessful())
                                            {
                                                String quantity = String.valueOf(itemRef.child(ProductId).child("quantity"));
                                                int temp = Integer.valueOf(quantity)-Integer.valueOf(ProductQuantity);
                                                itemRef.child(ProductId).child("quantity").setValue(temp);
                                        Toast.makeText(PaymentActivity.this, "your final order has been placed successfully.", Toast.LENGTH_SHORT).show();

                                        Intent intent = new Intent(PaymentActivity.this, HomeActivity.class);
                                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                        startActivity(intent);
                                        finish();
                                            }
                                        }
                                    });
                        }
                    }
                });
                for (String element : myList) {
                    ordersRef.child(OrderKEY).child(OrderKEY).child("IDs").child(element).child("id").setValue(element);

                }

                HashMap<String, Object> AdminordersMap = new HashMap<>();

                AdminordersMap.put("totalAmount", totalAmount);
                AdminordersMap.put("name", Name);
                AdminordersMap.put("phone", Phone);
                AdminordersMap.put("address", Address);
                AdminordersMap.put("city", City);
                AdminordersMap.put("date", saveCurrentDate);
                AdminordersMap.put("time", saveCurrentTime);
                AdminordersMap.put("state", "not shipped");
                AdminordersMap.put("paymentmethod", "Google Pay");

                userRef.child(OrderKEY).updateChildren(AdminordersMap);
                for (String element : myList) {
                    userRef.child(OrderKEY).child("IDs").child(element).child("id").setValue(element);

                }


                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
            } else if ("Payment cancelled by user.".equals(paymentCancel)) {
                Toast.makeText(PaymentActivity.this, "Payment cancelled by user.", Toast.LENGTH_SHORT).show();
                Log.e("UPI", "Cancelled by user: " + approvalRefNo);
            } else {
                Toast.makeText(PaymentActivity.this, "Transaction failed.Please try again", Toast.LENGTH_SHORT).show();
                Log.e("UPI", "failed payment: " + approvalRefNo);
            }
        } else {
            Log.e("UPI", "Internet issue: ");
            Toast.makeText(PaymentActivity.this, "Internet connection is not available. Please check and try again", Toast.LENGTH_SHORT).show();
        }
    }

    public static boolean isConnectionAvailable(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager != null) {
            NetworkInfo netInfo = connectivityManager.getActiveNetworkInfo();
            if (netInfo != null && netInfo.isConnected()
                    && netInfo.isConnectedOrConnecting()
                    && netInfo.isAvailable()) {
                return true;
            }
        }
        return false;
    }
}
