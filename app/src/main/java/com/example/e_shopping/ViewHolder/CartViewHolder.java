package com.example.e_shopping.ViewHolder;

import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.example.e_shopping.Interface.ItemClickListner;
import com.example.e_shopping.R;

public class CartViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener
{
    public TextView txtProductName, txtProductPrice, txtProductQuantity,total_amount;
    private ItemClickListner itemClickListner;
    public ImageView txtProductImage;
    public Button next_process;


    public CartViewHolder(View itemView)
    {
        super(itemView);

        txtProductName = itemView.findViewById(R.id.cart_product_name);
        txtProductPrice = itemView.findViewById(R.id.cart_product_price);
        txtProductQuantity = itemView.findViewById(R.id.cart_product_quantity);
        txtProductImage = itemView.findViewById(R.id.product_image_cart);
        total_amount = itemView.findViewById(R.id.total_price);
        next_process = itemView.findViewById(R.id.next_btn);

    }

    @Override
    public void onClick(View view)
    {
        itemClickListner.onClick(view, getAdapterPosition(), false);
    }

    public void setItemClickListner(ItemClickListner itemClickListner)
    {
        this.itemClickListner = itemClickListner;
    }
}