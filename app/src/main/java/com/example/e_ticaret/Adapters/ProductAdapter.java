package com.example.e_ticaret.Adapters;

import android.content.Context;
import android.content.res.ColorStateList;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.e_ticaret.DAO.ActiveUserDao;
import com.example.e_ticaret.DAO.FavoriteDao;
import com.example.e_ticaret.DAO.BasketDao;
import com.example.e_ticaret.R;
import com.example.e_ticaret.dbModels.ProductDataModel;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.ProductViewHolder> {

    private List<ProductDataModel> productList;
    private final Map<Integer, Integer> cartQuantities = new HashMap<>();

    public ProductAdapter(List<ProductDataModel> productList) {
        this.productList = productList;
    }

    @NonNull
    @Override
    public ProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_product, parent, false);
        return new ProductViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProductViewHolder holder, int position) {
        ProductDataModel product = productList.get(position);
        int productId = product.getId();
        Context context = holder.itemView.getContext();

        // 1. Aktif Kullanıcı ID'sini Alıyoruz
        ActiveUserDao activeUserDao = new ActiveUserDao(context);
        activeUserDao.open();
        int activeUserId = (int) activeUserDao.getActiveUserId();
        activeUserDao.close();

        // Verileri şemaya uygun bağlıyoruz
        holder.txtTitle.setText(product.getName());
        holder.txtCategory.setText(product.getCategory());
        holder.txtPrice.setText(product.getPrice() + " TL");

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

        // 2. Favori Durumunu Kontrol Edip Yıldızı Başlatma
        FavoriteDao favoriteDao = new FavoriteDao(context);
        favoriteDao.open();
        boolean[] isFav = {favoriteDao.isFavorite(activeUserId, productId)};

        if (isFav[0]) {
            holder.btnFavorite.setImageResource(android.R.drawable.btn_star_big_on);
            holder.btnFavorite.setImageTintList(ColorStateList.valueOf(ContextCompat.getColor(context, R.color.prlk_primary)));
        } else {
            holder.btnFavorite.setImageResource(android.R.drawable.btn_star_big_off);
            holder.btnFavorite.setImageTintList(ColorStateList.valueOf(ContextCompat.getColor(context, R.color.text_muted)));
        }
        favoriteDao.close();

        // 3. Favori Tıklama Mekanizması (Güvenli Bağlantı Yönetimi)
        holder.btnFavorite.setOnClickListener(v -> {
            favoriteDao.open(); // Tıklama anında taze bağlantı açılır
            int currentPosition = holder.getAdapterPosition();

            if (currentPosition == RecyclerView.NO_POSITION) {
                favoriteDao.close();
                return;
            }

            if (!isFav[0]) {
                long result = favoriteDao.addFavorite(activeUserId, productId);
                if (result != -1) {
                    isFav[0] = true;
                    holder.btnFavorite.setImageResource(android.R.drawable.btn_star_big_on);
                    holder.btnFavorite.setImageTintList(ColorStateList.valueOf(ContextCompat.getColor(context, R.color.prlk_primary)));
                    Toast.makeText(context, product.getName() + " Favorilere Eklendi!", Toast.LENGTH_SHORT).show();
                }
            } else {
                favoriteDao.removeFavorite(activeUserId, productId);
                isFav[0] = false;
                holder.btnFavorite.setImageResource(android.R.drawable.btn_star_big_off);
                holder.btnFavorite.setImageTintList(ColorStateList.valueOf(ContextCompat.getColor(context, R.color.text_muted)));
                Toast.makeText(context, product.getName() + " Favorilerden Çıkarıldı!", Toast.LENGTH_SHORT).show();

                if (context.toString().contains("FavoritesFragment") || v.getRootView().findViewById(R.id.rv_favorites) != null) {
                    productList.remove(currentPosition);
                    notifyItemRemoved(currentPosition);
                    notifyItemRangeChanged(currentPosition, productList.size());
                }
            }
            favoriteDao.close(); // İş bitince bağlantı kapatılır
        });

        // 4. Sepet Durumunu Veritabanından Çekme ve Başlatma
        BasketDao basketDao = new BasketDao(context);
        basketDao.open();
        int dbQty = basketDao.getProductQuantity(activeUserId, productId);
        basketDao.close();

        cartQuantities.put(productId, dbQty);
        updateCartVisibility(holder, dbQty);

        // İlk kez "Sepete Ekle" Butonuna Basıldığında
        holder.btnAddToCart.setOnClickListener(v -> {
            basketDao.open(); // Taze açılış
            basketDao.addToCart(activeUserId, productId);
            basketDao.close(); // Güvenli kapanış

            cartQuantities.put(productId, 1);
            updateCartVisibility(holder, 1);
        });

        // Miktar Arttırma (+) Tıklaması
        holder.btnIncrease.setOnClickListener(v -> {
            basketDao.open(); // Taze açılış
            basketDao.addToCart(activeUserId, productId);
            basketDao.close(); // Güvenli kapanış

            int qty = cartQuantities.get(productId) + 1;
            cartQuantities.put(productId, qty);
            holder.txtQuantity.setText(String.valueOf(qty));
        });

        // Miktar Azaltma (-) Tıklaması
        holder.btnDecrease.setOnClickListener(v -> {
            basketDao.open(); // Taze açılış
            basketDao.decreaseOrRemoveFromCart(activeUserId, productId);
            basketDao.close(); // Güvenli kapanış

            int qty = cartQuantities.get(productId) - 1;
            if (qty <= 0) {
                cartQuantities.remove(productId);
                updateCartVisibility(holder, 0);
            } else {
                cartQuantities.put(productId, qty);
                holder.txtQuantity.setText(String.valueOf(qty));
            }
        });
    }

    private void updateCartVisibility(ProductViewHolder holder, int qty) {
        if (qty > 0) {
            holder.btnAddToCart.setVisibility(View.GONE);
            holder.layoutCounter.setVisibility(View.VISIBLE);
            holder.txtQuantity.setText(String.valueOf(qty));
        } else {
            holder.btnAddToCart.setVisibility(View.VISIBLE);
            holder.layoutCounter.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return productList != null ? productList.size() : 0;
    }

    static class ProductViewHolder extends RecyclerView.ViewHolder {
        ImageView imgProduct;
        FloatingActionButton btnFavorite;
        TextView txtTitle, txtCategory, txtPrice, txtQuantity;
        MaterialButton btnAddToCart, btnDecrease, btnIncrease;
        LinearLayout layoutCounter;

        public ProductViewHolder(@NonNull View itemView) {
            super(itemView);
            imgProduct = itemView.findViewById(R.id.img_item_product);
            btnFavorite = itemView.findViewById(R.id.btn_item_favorite);
            txtTitle = itemView.findViewById(R.id.txt_item_title);
            txtCategory = itemView.findViewById(R.id.txt_item_category);
            txtPrice = itemView.findViewById(R.id.txt_item_price);
            txtQuantity = itemView.findViewById(R.id.txt_quantity);
            btnAddToCart = itemView.findViewById(R.id.btn_add_to_cart);
            btnDecrease = itemView.findViewById(R.id.btn_decrease);
            btnIncrease = itemView.findViewById(R.id.btn_increase);
            layoutCounter = itemView.findViewById(R.id.layout_quantity_counter);
        }
    }
}