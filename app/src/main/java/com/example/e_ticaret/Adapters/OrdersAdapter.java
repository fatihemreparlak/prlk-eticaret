package com.example.e_ticaret.Adapters;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.e_ticaret.R;
import com.example.e_ticaret.dbModels.ProductDataModel;

import java.util.ArrayList;

public class OrdersAdapter extends RecyclerView.Adapter<OrdersAdapter.OrderViewHolder> {

    private ArrayList<ProductDataModel> orderList;

    public OrdersAdapter(ArrayList<ProductDataModel> orderList) {
        this.orderList = orderList;
    }

    @NonNull
    @Override
    public OrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_order, parent, false);
        return new OrderViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OrderViewHolder holder, int position) {
        ProductDataModel product = orderList.get(position);
        if (product == null) return;

        holder.txtProductName.setText(product.getName());
        holder.txtProductQty.setText("Adet: " + product.getQuantity());

        int totalPrice = product.getPrice() * product.getQuantity();
        holder.txtProductPrice.setText(totalPrice + " TL");

        // Veritabanındaki URI yolunu alıyoruz
        String imageUriString = product.getImagePath();

        if (imageUriString != null && !imageUriString.isEmpty()) {
            try {
                Uri imageUri = Uri.parse(imageUriString);

                // Gerçek ürün resmi yükleneceği için varsayılan XML rengini (tint) temizliyoruz
                holder.imgProduct.setImageTintList(null);

                // Doğrudan URI üzerinden resmi bas
                holder.imgProduct.setImageURI(imageUri);

            } catch (Exception e) {
                // Hata durumunda kurtarma ikonu bas ve rengini prlk yap
                holder.imgProduct.setImageResource(android.content.res.Resources.getSystem().getIdentifier("ic_menu_archive", "drawable", "android"));
                int prlkColor = holder.itemView.getContext().getResources().getColor(R.color.prlk_primary);
                holder.imgProduct.setImageTintList(ColorStateList.valueOf(prlkColor));
                e.printStackTrace();
            }
        } else {
            // URI boşsa varsayılan ikon
            holder.imgProduct.setImageResource(android.content.res.Resources.getSystem().getIdentifier("ic_menu_archive", "drawable", "android"));
            int prlkColor = holder.itemView.getContext().getResources().getColor(R.color.prlk_primary);
            holder.imgProduct.setImageTintList(ColorStateList.valueOf(prlkColor));
        }
    }

    @Override
    public int getItemCount() {
        return orderList != null ? orderList.size() : 0;
    }

    public static class OrderViewHolder extends RecyclerView.ViewHolder {
        ImageView imgProduct;
        TextView txtProductName, txtProductQty, txtProductPrice;

        public OrderViewHolder(@NonNull View itemView) {
            super(itemView);
            imgProduct = itemView.findViewById(R.id.img_order_product);
            txtProductName = itemView.findViewById(R.id.txt_order_product_name);
            txtProductQty = itemView.findViewById(R.id.txt_order_product_qty);
            txtProductPrice = itemView.findViewById(R.id.txt_order_product_price);
        }
    }
}