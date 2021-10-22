package com.example.e_shopping;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.e_shopping.Model.AdminOrders;
import com.example.e_shopping.Model.UserOrders;
import com.example.e_shopping.Prevalent.Prevalent;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import io.paperdb.Paper;

public class UserOrdersActivity extends AppCompatActivity {

    private DatabaseReference UserOrdersRef, OrdersRef;
    private RecyclerView recyclerView;
    RecyclerView.LayoutManager layoutManager;
    public List<String> Array = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_orders);

        UserOrdersRef = FirebaseDatabase.getInstance().getReference().child("Users").child(Prevalent.currentOnlineUser.getPhone()).child("MyOrders");
        OrdersRef = FirebaseDatabase.getInstance().getReference().child("Orders");
        //Toast.makeText(this,Prevalent.currentOnlineUser.getPhone(),Toast.LENGTH_SHORT).show();


//        Paper.init(this);


//        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
//        toolbar.setTitle("Home");
//        setSupportActionBar(toolbar);

        recyclerView = findViewById(R.id.recycler_menu1);
        recyclerView.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);


    }

    @Override
    protected void onStart() {
        super.onStart();



                            FirebaseRecyclerOptions<AdminOrders> options =
                                    new FirebaseRecyclerOptions.Builder<AdminOrders>()
                                            .setQuery(UserOrdersRef, AdminOrders.class)
                                            .build();
                          //  Toast.makeText(UserOrdersActivity.this,snapshot.getKey(),Toast.LENGTH_SHORT).show();

                            FirebaseRecyclerAdapter<AdminOrders, UserOrdersViewHolder> adapter =
                                    new FirebaseRecyclerAdapter<AdminOrders, UserOrdersViewHolder>(options) {
                                        String ids, imageUrl;

                                        @Override
                                        protected void onBindViewHolder(@NonNull final UserOrdersViewHolder holder, final int position, @NonNull final AdminOrders model) {


                                            holder.userTotalPrice.setText("Total Amount =  Rs" + model.getTotalAmount());
                                            holder.userDateTime.setText("Order at: " + model.getDate() + "  " + model.getTime());
                                            //holder.userShippingAddress.setText("Shipping Address: " + model.getAddress() + ", " + model.getCity());
                                            holder.state.setText("Current Status: " + model.getState());
                                            //holder.payment.setText("Payment method :"+model.getPaymentmethod());
                                            UserOrdersRef.child("IDs").addValueEventListener(new ValueEventListener() {
                                                @Override
                                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                    if (dataSnapshot.exists()) {
                                                        for (final DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                                            if (snapshot.hasChild("id")) {
                                                                ids = snapshot.child("id").getValue().toString();
                                                                DatabaseReference url = FirebaseDatabase.getInstance().getReference()
                                                                        .child("Products").child(ids);
                                                                url.addValueEventListener(new ValueEventListener() {
                                                                    @Override
                                                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                                        if (dataSnapshot.hasChild("image")) {
                                                                            imageUrl = dataSnapshot.child("image").getValue().toString();
                                                                            Picasso.get().load(imageUrl).into(holder.orderImage);
                                                                        }
                                                                    }

                                                                    @Override
                                                                    public void onCancelled(@NonNull DatabaseError databaseError) {

                                                                    }
                                                                });
                                                            }


                                                        }

                                                    } else {
                                                        // Toast.makeText(UserOrdersActivity.this,"error",Toast.LENGTH_SHORT).show();
                                                    }

                                                }

                                                @Override
                                                public void onCancelled(@NonNull DatabaseError databaseError) {

                                                }
                                            });
                                            holder.itemView.setOnClickListener(new View.OnClickListener() {
                                                @Override
                                                public void onClick(View v) {
                                                    String OrderID = getRef(position).getKey();
                                                    Intent intent = new Intent(UserOrdersActivity.this, OrderDetailsActivity.class);
                                                    intent.putExtra("OrderID", OrderID);
                                                    startActivity(intent);
                                                    //Toast.makeText(UserOrdersActivity.this,OrderID,Toast.LENGTH_SHORT).show();

                                                }
                                            });

                                            //Toast.makeText(UserOrdersActivity.this,imageUrl,Toast.LENGTH_SHORT).show();

                                        }

                                        @NonNull
                                        @Override
                                        public UserOrdersViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                                            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.user_orders, parent, false);
                                            return new UserOrdersViewHolder(view);
                                        }
                                    };
                            recyclerView.setAdapter(adapter);
                            adapter.startListening();


    }


    public static class UserOrdersViewHolder extends RecyclerView.ViewHolder
    {
        public TextView userTotalPrice, userDateTime, userShippingAddress, state,payment;
        public ImageView orderImage;


        public UserOrdersViewHolder(View itemView)
        {
            super(itemView);
            userTotalPrice = itemView.findViewById(R.id.user_order_total_price);
            userDateTime = itemView.findViewById(R.id.user_order_date_time);
            //userShippingAddress = itemView.findViewById(R.id.user_order_address_city);
            state = itemView.findViewById(R.id.user_order_state);
            //payment = itemView.findViewById(R.id.user_payment_method);
            orderImage = itemView.findViewById(R.id.order_image);


        }

    }
}
