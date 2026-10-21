package com.example.e_ticaret.bottomFragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.e_ticaret.Adapters.ProductAdapter;
import com.example.e_ticaret.DAO.ActiveUserDao;
import com.example.e_ticaret.DAO.FavoriteDao;
import com.example.e_ticaret.R;
import com.example.e_ticaret.dbModels.ProductDataModel;

import java.util.ArrayList;

public class FavoritesFragment extends Fragment {

    private RecyclerView rvFavorites;
    private ProductAdapter adapter;
    private ArrayList<ProductDataModel> favoriteProducts;

    private FavoriteDao favoriteDao;
    private ActiveUserDao activeUserDao;

    public FavoritesFragment() {
        // Gerekli boş kurucu metot
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_favorites, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        rvFavorites = view.findViewById(R.id.rv_favorites);

        // DAO nesnelerini başlatıyoruz
        favoriteDao = new FavoriteDao(requireContext());
        activeUserDao = new ActiveUserDao(requireContext());

        // Yan yana 2'li premium ızgara görünümü ayarlıyoruz
        rvFavorites.setLayoutManager(new GridLayoutManager(requireContext(), 2));

        loadFavorites();
    }

    private void loadFavorites() {
        // 1. Aktif kullanıcının ID'sini çekiyoruz
        activeUserDao.open();
        int activeUserId = (int) activeUserDao.getActiveUserId();
        activeUserDao.close();

        // 2. O kullanıcıya ait favori ürünleri veritabanından çekiyoruz
        favoriteDao.open();
        favoriteProducts = favoriteDao.getFavoriteProducts(activeUserId);
        favoriteDao.close();

        // 3. Mevcut ProductAdapter'ı listeye bağlıyoruz
        adapter = new ProductAdapter(favoriteProducts);
        rvFavorites.setAdapter(adapter);
    }

    @Override
    public void onResume() {
        super.onResume();
        // Kullanıcı başka sekmeye gidip favoriyi silerse, geri geldiğinde liste güncellensin diye onResume'da tazeliyoruz
        loadFavorites();
    }
}