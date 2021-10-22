package com.example.e_shopping;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.example.e_shopping.Prevalent.Prevalent;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

public class ConfirmFinalOrderActivity extends AppCompatActivity {

    private EditText nameEditText, phoneEditText, addressEditText, cityEditText;
    private Button confirmOrderBtn;

    private String totalAmount = "",ProductId,ProductQuantity;
    private RadioButton cod;
    private RadioButton Gpay;
    private RadioGroup paymentGroup;
    String method;
    DatabaseReference orderKey,userRef,phoneRef,itemRef;
    public List<String> myList = new ArrayList<>();
    String  Name, Address,Phone,City;


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_confirm_final_order);


        totalAmount = getIntent().getStringExtra("Total Price");
        ProductId = getIntent().getStringExtra("Product Id");
        ProductQuantity = getIntent().getStringExtra("Quantity");
        myList = (ArrayList<String>) getIntent().getSerializableExtra("ids");





        userRef = FirebaseDatabase.getInstance().getReference().child("Users").child(Prevalent.currentOnlineUser.getPhone()).child("MyOrders");
        phoneRef = FirebaseDatabase.getInstance().getReference().child("Users").child(Prevalent.currentOnlineUser.getPhone());
        itemRef = FirebaseDatabase.getInstance().getReference().child("Products");
        confirmOrderBtn = (Button) findViewById(R.id.confirm_final_order_btn);
        nameEditText = (EditText) findViewById(R.id.shippment_name);
        phoneEditText = (EditText) findViewById(R.id.shippment_phone_number);
        addressEditText = (EditText) findViewById(R.id.shippment_address);
        cityEditText = (EditText) findViewById(R.id.shippment_city);
        cod = (RadioButton) findViewById(R.id.cash_on_delivery);
        Gpay = (RadioButton) findViewById(R.id.google_pay);
        paymentGroup = (RadioGroup) findViewById(R.id.payment_group);


        phoneRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
            {
                if(dataSnapshot.exists())
                {
                    if (dataSnapshot.hasChild("phone"))
                    {
                        phoneEditText.setText(dataSnapshot.child("phone").getValue().toString());
                        phoneEditText.setEnabled(false);
                    }
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        confirmOrderBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                Check();
            }
        });
    }



    private void Check()
    {
        try
        {
            int selctedRadioButtonId = paymentGroup.getCheckedRadioButtonId();
            RadioButton selectedRadioButton = (RadioButton) findViewById(selctedRadioButtonId);
            method = selectedRadioButton.getText().toString();
        }catch (Exception e){Toast.makeText(getApplicationContext(),"please select a payment method",Toast.LENGTH_SHORT).show();}

        if (TextUtils.isEmpty(nameEditText.getText().toString()))
        {
            Toast.makeText(this, "Please provide your full name.", Toast.LENGTH_SHORT).show();

        }
        else if (TextUtils.isEmpty(phoneEditText.getText().toString()))
        {
            Toast.makeText(this, "Please provide your phone number.", Toast.LENGTH_SHORT).show();
        }
        else if (TextUtils.isEmpty(addressEditText.getText().toString()))
        {
            Toast.makeText(this, "Please provide your address.", Toast.LENGTH_SHORT).show();
        }
        else if (TextUtils.isEmpty(cityEditText.getText().toString()))
        {
            Toast.makeText(this, "Please provide your city name.", Toast.LENGTH_SHORT).show();
        }
        else if(TextUtils.isEmpty(method))
        {
            Toast.makeText(this,"please choose a payment method",Toast.LENGTH_SHORT).show();
        }
        else
        {
            Name = nameEditText.getText().toString();
            Address = addressEditText.getText().toString();
            Phone = phoneEditText.getText().toString();
            City = cityEditText.getText().toString();
            ConfirmOrder();
        }
    }



    private void ConfirmOrder() {

        Toast.makeText(ConfirmFinalOrderActivity.this, method, Toast.LENGTH_SHORT).show();

        if (method.equals("Google pay")) {
            Intent intent = new Intent(ConfirmFinalOrderActivity.this, PaymentActivity.class);
            intent.putExtra("Total Price", totalAmount);
            intent.putExtra("Name", Name);
            intent.putExtra("Phone", Phone);
            intent.putExtra("Address", Address);
            intent.putExtra("City", City);
            intent.putExtra("Product Id",ProductId);
            intent.putExtra("Product Quantity",ProductQuantity);

            startActivity(intent);
        } else if (method.equals("COD")) {

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
            ordersMap.put("ID", OrderKEY);
            ordersMap.put("totalAmount", totalAmount);
            ordersMap.put("name", nameEditText.getText().toString());
            ordersMap.put("phone", phoneEditText.getText().toString());
            ordersMap.put("address", addressEditText.getText().toString());
            ordersMap.put("city", cityEditText.getText().toString());
            ordersMap.put("date", saveCurrentDate);
            ordersMap.put("time", saveCurrentTime);
            ordersMap.put("state", "not shipped");
            ordersMap.put("paymentmethod", method);

            orderKey.updateChildren(ordersMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()) {
                        String quantity = String.valueOf(itemRef.child(ProductId).child("quantity"));
                        int temp = Integer.valueOf(quantity)-Integer.valueOf(ProductQuantity);
                        itemRef.child(ProductId).child("quantity").setValue(temp);
                        FirebaseDatabase.getInstance().getReference()
                                .child("Cart List")
                                .child("User View")
                                .child(Prevalent.currentOnlineUser.getPhone())
                                .removeValue()
                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()) {

                                        Toast.makeText(ConfirmFinalOrderActivity.this, "your final order has been placed successfully.", Toast.LENGTH_SHORT).show();

                                        Intent intent = new Intent(ConfirmFinalOrderActivity.this, HomeActivity.class);
                                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                        startActivity(intent);
                                        finish();
                                        }
                                    }
                                });
                    }
                }
            });
            //userRef.child(OrderKEY).child("OrderID").setValue(OrderKEY);

            for (String element : myList) {
                ordersRef.child(OrderKEY).child(OrderKEY).child("IDs").child(element).child("id").setValue(element);

            }

            HashMap<String, Object> AdminordersMap = new HashMap<>();

            AdminordersMap.put("totalAmount", totalAmount);
            AdminordersMap.put("name", nameEditText.getText().toString());
            AdminordersMap.put("phone", phoneEditText.getText().toString());
            AdminordersMap.put("address", addressEditText.getText().toString());
            AdminordersMap.put("city", cityEditText.getText().toString());
            AdminordersMap.put("date", saveCurrentDate);
            AdminordersMap.put("time", saveCurrentTime);
            AdminordersMap.put("state", "not shipped");
            AdminordersMap.put("paymentmethod", method);

            userRef.child(OrderKEY).updateChildren(AdminordersMap);
            for (String element : myList) {
                userRef.child(OrderKEY).child("IDs").child(element).child("id").setValue(element);

            }


        }
    }
}
