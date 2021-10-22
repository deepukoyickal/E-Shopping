package com.example.e_shopping.Sellers;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.example.e_shopping.CheckNewProductsActivity;
import com.example.e_shopping.MainActivity;
import com.example.e_shopping.Model.Products;
import com.example.e_shopping.R;
import com.example.e_shopping.SellerProductCategoryActivity;
import com.example.e_shopping.Sellers.ui.home.HomeFragment;
import com.example.e_shopping.ViewHolder.ItemViewHolder;
import com.example.e_shopping.ViewHolder.ProductViewHolder;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class SellerHomeActivity extends AppCompatActivity {

//    private TextView mTextMessage;
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_seller_home);
//        BottomNavigationView navView = findViewById(R.id.nav_view);
//        // Passing each menu ID as a set of Ids because each
//        // menu should be considered as top level destinations.
////        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
////                R.id.navigation_home, R.id.navigation_add, R.id.navigation_logout)
////                .build();
////        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
////        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
////        NavigationUI.setupWithNavController(navView, navController);
//    }
//    BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener =
//            new BottomNavigationView.OnNavigationItemSelectedListener() {
//                @Override public boolean onNavigationItemSelected(@NonNull MenuItem item) {
//                    switch (item.getItemId()) {
//                        case R.id.navigation_home:
//                            mTextMessage.setText(R.string.title_home);
//                            return true;
//                        case R.id.navigation_add:
//                            mTextMessage.setText((R.string.title_dashboard));
//                            return true;
//                        case R.id.navigation_logout:
//                            final FirebaseAuth mAuth;
//                            mAuth=FirebaseAuth.getInstance();
//                            mAuth.signOut();
//                            Intent intent = new Intent(SellerHomeActivity.this, MainActivity.class);
//                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK );
//                            startActivity(intent);
//                            finish();
//                            return true;
//                    }
//                    return false;
//                }
//            };
//private ActionBar toolbar;
    private RecyclerView recyclerView;
    RecyclerView.LayoutManager layoutManager;
    private DatabaseReference unVerifiedProducts;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_seller_home);

       // toolbar = getSupportActionBar();
        recyclerView = findViewById(R.id.seller_home_recyclerview);
        recyclerView.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        unVerifiedProducts = FirebaseDatabase.getInstance().getReference().child("Products");

        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.nav_view);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

    }

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
           // Fragment fragment;
            switch (item.getItemId()) {
                case R.id.navigation_home:
                    //toolbar.setTitle("Home");
                    return true;
                case R.id.navigation_add:
                    //toolbar.setTitle("Add");
                    Intent intentcate = new Intent(SellerHomeActivity.this, SellerProductCategoryActivity.class);
                    startActivity(intentcate);
                    return true;
                case R.id.navigation_logout:
                    //toolbar.setTitle("Logout");
                    final FirebaseAuth mAuth;
                    mAuth=FirebaseAuth.getInstance();
                            mAuth.signOut();
                            Intent intent = new Intent(SellerHomeActivity.this, MainActivity.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK );
                            startActivity(intent);
                            finish();
                    return true;
            }
            return false;
        }
    };

    @Override
    protected void onStart()
    {
        super.onStart();
        FirebaseRecyclerOptions<Products> options = new FirebaseRecyclerOptions.Builder<Products>()
                .setQuery(unVerifiedProducts.orderByChild("sellerId").
                        equalTo(FirebaseAuth.getInstance().getCurrentUser().getUid()),Products.class).build();

        final FirebaseRecyclerAdapter<Products , ItemViewHolder> adapter=
                new FirebaseRecyclerAdapter<Products, ItemViewHolder>(options) {
                    @SuppressLint("ResourceAsColor")
                    @Override
                    protected void onBindViewHolder(@NonNull ItemViewHolder holder, int position, @NonNull final Products model)
                    {
                        holder.txtProductName.setText(model.getPname());
                        holder.txtProductDescription.setText(model.getDescription());
                        holder.txtProductStatus.setText("State: "+model.getProductState());
                        if(model.getProductState().equals("Approved")) {
                            holder.txtProductStatus.setTextColor(R.color.green);
                        }
                        if (model.getProductState().equals("not Approved"))
                        {
                            holder.txtProductStatus.setTextColor(R.color.red);
                        }
                        holder.txtProductPrice.setText("Price = " + model.getPrice() + "Rs");
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
                                AlertDialog.Builder builder = new AlertDialog.Builder(SellerHomeActivity.this);
                                builder.setTitle("Do you want to delete this product: Are you sure?");
                                builder.setItems(options, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int position)
                                    {
                                        if(position ==0)
                                        {
                                            DeletProduct(productId);
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
                    public ItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
                    {
                        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.seller_item_view, parent, false);
                        ItemViewHolder holder = new ItemViewHolder(view);
                        return holder;
                    }
                };
        recyclerView.setAdapter(adapter);
        adapter.startListening();


    }

    private void DeletProduct(String productId)
    {
        unVerifiedProducts.child(productId).
               removeValue()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task)
                    {
                        Toast.makeText(SellerHomeActivity.this,"That product has been deleted successfully",Toast.LENGTH_SHORT).show();
                    }
                })
        ;
    }
}
