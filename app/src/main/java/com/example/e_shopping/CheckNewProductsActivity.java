package com.example.e_shopping;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.e_shopping.Interface.ItemClickListner;
import com.example.e_shopping.Model.Products;
import com.example.e_shopping.ViewHolder.ProductViewHolder;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;

public class CheckNewProductsActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    RecyclerView.LayoutManager layoutManager;
    private DatabaseReference unVerifiedProducts;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_check_new_products);

        recyclerView = findViewById(R.id.admin_products_checklist);
        recyclerView.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        unVerifiedProducts = FirebaseDatabase.getInstance().getReference().child("Products");
    }

    @Override
    protected void onStart() {
        super.onStart();

        FirebaseRecyclerOptions<Products> options = new FirebaseRecyclerOptions.Builder<Products>()
                .setQuery(unVerifiedProducts.orderByChild("productState").
                        equalTo("not Approved"),Products.class).build();

        final FirebaseRecyclerAdapter<Products , ProductViewHolder> adapter=
                new FirebaseRecyclerAdapter<Products, ProductViewHolder>(options) {
                    @Override
                    protected void onBindViewHolder(@NonNull ProductViewHolder holder, int position, @NonNull final Products model)
                    {
                        holder.txtProductName.setText(model.getPname());
                        holder.txtProductDescription.setText(model.getDescription());
                        holder.txtProductPrice.setText("Price = " + model.getPrice() + "RS");
                        holder.txtProductQuantity.setText("Quantity= "+model.getQuantity());
                        Picasso.get().load(model.getImage()).into(holder.imageView);

                        holder.itemView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v)
                            {
                                final String productId = model.getPid();
                                CharSequence options[] =  new CharSequence []
                                        {
                                                "Yes",
                                                "No"
                                        };
                                AlertDialog .Builder builder = new AlertDialog.Builder(CheckNewProductsActivity.this);
                                builder.setTitle("Do you want to approve this product: Are you sure?");
                                builder.setItems(options, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int position)
                                    {
                                        if(position ==0)
                                        {
                                            ChangeProductState(productId);
                                        }
                                        if(position == 1)
                                        {

                                        }
                                    }
                                });
                                builder.show();
                            }
                        });

                    }

                    @NonNull
                    @Override
                    public ProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
                    {
                        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.product_items_layout, parent, false);
                        ProductViewHolder holder = new ProductViewHolder(view);
                        return holder;
                    }
                };
        recyclerView.setAdapter(adapter);
        adapter.startListening();

    }

    private void ChangeProductState(String productId)
    {
        unVerifiedProducts.child(productId).child("productState").setValue("Approved")
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task)
                    {
                        Toast.makeText(CheckNewProductsActivity.this,"That has been approved... it is now available for sale",Toast.LENGTH_SHORT).show();
                    }
                })
        ;
    }
}
