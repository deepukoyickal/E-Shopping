package com.example.e_shopping;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import com.example.e_shopping.Prevalent.Prevalent;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class OrderDetailsActivity extends AppCompatActivity {

    String OrderId;
    private TextView address,amount,status,date;
    DatabaseReference orderRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_details);

        OrderId = getIntent().getExtras().get("OrderID").toString();

        Toast.makeText(this,OrderId,Toast.LENGTH_SHORT).show();

        address = (TextView) findViewById(R.id.shipping_address);
        amount = (TextView) findViewById(R.id.total_amount);
        status = (TextView) findViewById(R.id.order_item_status);
        date = (TextView) findViewById(R.id.order_date_view);

        orderRef = FirebaseDatabase.getInstance().getReference().child("Users").child(Prevalent.currentOnlineUser.getPhone()).child("MyOrders");


    }

    @Override
    protected void onStart()
    {
        super.onStart();

        orderRef.child(OrderId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
            {
                if(dataSnapshot.exists())
                {
                    address.setText("Shipping address \n"+ dataSnapshot.child("name").getValue().toString()+" \n"
                            +dataSnapshot.child("address").getValue().toString() + " , " + dataSnapshot.child("city").getValue().toString()
                            +"\n phone number:"+ dataSnapshot.child("phone").getValue().toString());
                    amount.setText("Total amount \n" + dataSnapshot.child("totalAmount").getValue().toString());
                    status.setText("Order status \n" + dataSnapshot.child("state").getValue().toString());
                    date.setText("Order date and time: \n" + dataSnapshot.child("date").getValue().toString());

                    String temp = dataSnapshot.child("name").getValue().toString();
                    Toast.makeText(OrderDetailsActivity.this,temp,Toast.LENGTH_SHORT);

                }
                else
                {
                    Toast.makeText(OrderDetailsActivity.this,"cannot find data ",Toast.LENGTH_SHORT);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }
}
