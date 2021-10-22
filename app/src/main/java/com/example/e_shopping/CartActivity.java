package com.example.e_shopping;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.e_shopping.Model.Cart;
import com.example.e_shopping.Prevalent.Prevalent;
import com.example.e_shopping.ViewHolder.CartViewHolder;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class CartActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private RecyclerView.LayoutManager layoutManager;

    private Button NextProcessBtn,txtTotalAmount;
    private TextView  txtMsg1,emptyCart;

    private int overTotalPrice = 0;
    public List<String> IDs = new ArrayList<>();



    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart);


        recyclerView = findViewById(R.id.cart_list);
        recyclerView.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        NextProcessBtn = (Button) findViewById(R.id.next_btn);
        //txtTotalAmount = (Button) findViewById(R.id.total_price);
        //txtMsg1 = (TextView) findViewById(R.id.msg1);
        emptyCart = (TextView) findViewById(R.id.empty_cart);



    }


    @Override
    protected void onStart()
    {
        super.onStart();

       //
        // CheckOrderState();


        final DatabaseReference cartListRef = FirebaseDatabase.getInstance().getReference().child("Cart List");

        cartListRef.child("User View").child(Prevalent.currentOnlineUser.getPhone()).child("Products")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot)
                    {
                     if (!dataSnapshot.exists())
                     {
                         emptyCart.setVisibility(View.VISIBLE);
                         NextProcessBtn.setVisibility(View.INVISIBLE);
                         txtTotalAmount.setVisibility(View.INVISIBLE);
                     }
                     else
                     {

                     }

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
        cartListRef.child("User View").child(Prevalent.currentOnlineUser.getPhone()).child("Products").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
            {

                if (dataSnapshot.exists()) {
                    for (final DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        if (snapshot.hasChild("pid"))
                        {
                            String id = snapshot.child("pid").getValue().toString();
                            //Toast.makeText(CartActivity.this,id,Toast.LENGTH_SHORT).show();
                            //ProductArray[i] = id;
                            IDs.add(id);

                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        FirebaseRecyclerOptions<Cart> options =
                new FirebaseRecyclerOptions.Builder<Cart>()
                        .setQuery(cartListRef.child("User View")
                                .child(Prevalent.currentOnlineUser.getPhone())
                                .child("Products"), Cart.class)
                        .build();

        FirebaseRecyclerAdapter<Cart, CartViewHolder> adapter
                = new FirebaseRecyclerAdapter<Cart, CartViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull final CartViewHolder holder, int position, @NonNull final Cart model)
            {
//                NextProcessBtn.setVisibility(View.VISIBLE);
//                txtTotalAmount.setVisibility(View.VISIBLE);
                final String url = model.getPid();
                DatabaseReference imageUrl  = FirebaseDatabase.getInstance().getReference().child("Products").child(url).child("image");
                imageUrl.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if(dataSnapshot.exists())
                        {
                            String temp = dataSnapshot.getValue().toString();
                            Picasso.get().load(temp).into(holder.txtProductImage);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
                holder.txtProductQuantity.setText("Quantity = " + model.getQuantity());
                holder.txtProductPrice.setText("Price " + model.getPrice() + "Rs");
                holder.txtProductName.setText(model.getPname());
                holder.total_amount.setText(model.getPrice());
                holder.next_process.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view)
                    {


                        Intent intent = new Intent(CartActivity.this, ConfirmFinalOrderActivity.class);
                        intent.putExtra("Total Price", String.valueOf(overTotalPrice));
                        intent.putExtra("Product Id",url);
                        intent.putExtra("Quantity",String.valueOf(holder.txtProductQuantity));

                       // intent.putExtra("ids", (Serializable) IDs);
                        startActivity(intent);
                        finish();
                    }
                });;

                int oneTyprProductTPrice = ((Integer.valueOf(model.getPrice()))) * Integer.valueOf(model.getQuantity());
                overTotalPrice = overTotalPrice + oneTyprProductTPrice;
                //txtTotalAmount.setText("Rs " + String.valueOf(overTotalPrice));
                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view)
                    {
                        CharSequence options[] = new CharSequence[]
                                {
                                        "Edit",
                                        "Remove"
                                };
                        AlertDialog.Builder builder = new AlertDialog.Builder(CartActivity.this);
                        builder.setTitle("Cart Options:");

                        builder.setItems(options, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i)
                            {
                                if (i == 0)
                                {
                                    Intent intent = new Intent(CartActivity.this, ProductDetailsActivity.class);
                                    intent.putExtra("pid", model.getPid());
                                    startActivity(intent);
                                }
                                if (i == 1)
                                {
                                    cartListRef.child("User View")
                                            .child(Prevalent.currentOnlineUser.getPhone())
                                            .child("Products")
                                            .child(model.getPid())
                                            .removeValue()
                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task)
                                                {
                                                    if (task.isSuccessful())
                                                    {
                                                        Toast.makeText(CartActivity.this, "Item removed successfully.", Toast.LENGTH_SHORT).show();

                                                        Intent intent = new Intent(CartActivity.this, HomeActivity.class);
                                                        startActivity(intent);
                                                    }
                                                }
                                            });
                                }
                            }
                        });
                        builder.show();
                    }
                });
            }

            @NonNull
            @Override
            public CartViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
            {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.cart_items_layout, parent, false);
                CartViewHolder holder = new CartViewHolder(view);
                return holder;
            }
        };

//            Toast.makeText(this,i,Toast.LENGTH_SHORT).show();

        recyclerView.setAdapter(adapter);
        adapter.startListening();



//        for(String element:IDs)
//        {
//            Toast.makeText(this,element,Toast.LENGTH_SHORT).show();
//        }
    }



//    private void CheckOrderState()
//    {
//        DatabaseReference ordersRef;
//        ordersRef = FirebaseDatabase.getInstance().getReference().child("Orders").child(Prevalent.currentOnlineUser.getPhone());
//
//        ordersRef.addValueEventListener(new ValueEventListener() {
//            @Override
//            public void onDataChange(DataSnapshot dataSnapshot)
//            {
//                if (dataSnapshot.exists())
//                {
//                    String shippingState = dataSnapshot.child("state").getValue().toString();
//                    String userName = dataSnapshot.child("name").getValue().toString();
//
//                    if (shippingState.equals("shipped"))
//                    {
//                        txtTotalAmount.setText("Dear " + userName + "\n order is shipped successfully.");
//                        recyclerView.setVisibility(View.GONE);
//
//                        txtMsg1.setVisibility(View.VISIBLE);
//                        txtMsg1.setText("Congratulations, your final order has been Shipped successfully. Soon you will received your order at your door step.");
//                        NextProcessBtn.setVisibility(View.GONE);
//
//                        Toast.makeText(CartActivity.this, "you can purchase more products, once you received your first final order.", Toast.LENGTH_SHORT).show();
//                    }
//                    else if(shippingState.equals("not shipped"))
//                    {
//                        txtTotalAmount.setText("Shipping State = Not Shipped");
//                        recyclerView.setVisibility(View.GONE);
//
//                        txtMsg1.setVisibility(View.VISIBLE);
//                        NextProcessBtn.setVisibility(View.GONE);
//
//                        Toast.makeText(CartActivity.this, "you can purchase more products, once you received your first final order.", Toast.LENGTH_SHORT).show();
//                    }
//                }
//            }
//
//            @Override
//            public void onCancelled(DatabaseError databaseError) {
//
//            }
//        });
//    }


}
