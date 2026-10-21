package com.example.e_ticaret.Adapters;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.e_ticaret.DAO.ActiveUserDao;
import com.example.e_ticaret.DAO.BasketDao;
import com.example.e_ticaret.R;
import com.example.e_ticaret.dbModels.ProductDataModel;
import com.google.android.material.button.MaterialButton;

import java.io.File;
import java.util.List;

public class BasketAdapter extends RecyclerView.Adapter<BasketAdapter.BasketViewHolder> {

    private List<ProductDataModel> basketList;
    private OnBasketChangeListener changeListener;

    public interface OnBasketChangeListener {
        void onBasketChanged();
    }

    public BasketAdapter(List<ProductDataModel> basketList, OnBasketChangeListener changeListener) {
        this.basketList = basketList;
        this.changeListener = changeListener;
    }

    @NonNull
    @Override
    public BasketViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_basket_product, parent, false);
        return new BasketViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BasketViewHolder holder, int position) {
        ProductDataModel product = basketList.get(position);
        Context context = holder.itemView.getContext();
        int productId = product.getId();

        ActiveUserDao activeUserDao = new ActiveUserDao(context);
        activeUserDao.open();
        int activeUserId = (int) activeUserDao.getActiveUserId();
        activeUserDao.close();

        holder.txtTitle.setText(product.getName());
        holder.txtCategory.setText(product.getCategory());
        holder.txtPrice.setText(product.getPrice() + " TL");
        holder.txtQuantity.setText(String.valueOf(product.getQuantity()));

        // Görseli yükleme
        if (product.getImagePath() != null && !product.getImagePath().isEmpty()) {
            File imgFile = new File(product.getImagePath());
            if (imgFile.exists()) {
                holder.imgProduct.setImageURI(Uri.fromFile(imgFile));
            } else {
                holder.imgProduct.setImageResource(android.R.drawable.ic_menu_gallery);
            }
        } else {
            holder.imgProduct.setImageResource(android.R.drawable.ic_menu_gallery);
        }

        BasketDao basketDao = new BasketDao(context);

        holder.btnIncrease.setOnClickListener(v -> {
            basketDao.open();
            basketDao.addToCart(activeUserId, productId);
            basketDao.close();

            product.setQuantity(product.getQuantity() + 1);
            holder.txtQuantity.setText(String.valueOf(product.getQuantity()));
            if (changeListener != null) changeListener.onBasketChanged();
        });

        holder.btnDecrease.setOnClickListener(v -> {
            basketDao.open();
            basketDao.decreaseOrRemoveFromCart(activeUserId, productId);
            basketDao.close();

            int currentQty = product.getQuantity() - 1;
            int currentPosition = holder.getAdapterPosition();

            if (currentQty <= 0 && currentPosition != RecyclerView.NO_POSITION) {
                // Adet sıfırlandı, ürünü listeden uçur ve animasyonla sil
                basketList.remove(currentPosition);
                notifyItemRemoved(currentPosition);
                notifyItemRangeChanged(currentPosition, basketList.size());
            } else {
                product.setQuantity(currentQty);
                holder.txtQuantity.setText(String.valueOf(currentQty));
            }
            if (changeListener != null) changeListener.onBasketChanged();
        });
    }

    @Override
    public int getItemCount() {
        return basketList != null ? basketList.size() : 0;
    }

    static class BasketViewHolder extends RecyclerView.ViewHolder {
        ImageView imgProduct;
        TextView txtTitle, txtCategory, txtPrice, txtQuantity;
        MaterialButton btnDecrease, btnIncrease;

        public BasketViewHolder(@NonNull View itemView) {
            super(itemView);
            imgProduct = itemView.findViewById(R.id.img_basket_product);
            txtTitle = itemView.findViewById(R.id.txt_basket_title);
            txtCategory = itemView.findViewById(R.id.txt_basket_category);
            txtPrice = itemView.findViewById(R.id.txt_basket_price);
            txtQuantity = itemView.findViewById(R.id.txt_basket_quantity);
            btnDecrease = itemView.findViewById(R.id.btn_basket_decrease);
            btnIncrease = itemView.findViewById(R.id.btn_basket_increase);
        }
    }
}