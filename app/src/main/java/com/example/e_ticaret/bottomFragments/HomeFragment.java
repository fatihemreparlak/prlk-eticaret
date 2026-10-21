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
import com.example.e_ticaret.R;
import com.example.e_ticaret.DAO.ProductDao;
import com.example.e_ticaret.dbModels.ProductDataModel;

import java.util.List;

public class HomeFragment extends Fragment {

    private RecyclerView rvProducts;
    private ProductAdapter adapter;
    private ProductDao productDao;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle bundle) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        rvProducts = view.findViewById(R.id.rv_products);

        // Yan yana tam 2 ürün göstermek için 2 parametreli GridLayoutManager kuruyoruz
        rvProducts.setLayoutManager(new GridLayoutManager(requireContext(), 2));

        // 1. Veritabanından verileri çekiyoruz
        productDao = new ProductDao(requireContext());
        productDao.open();
        List<ProductDataModel> allProducts = productDao.getAllProducts();
        productDao.close();

        // 2. Çekilen listeyi yeni adaptörümüze veriyoruz
        adapter = new ProductAdapter(allProducts);

        // 3. Adaptörü RecyclerView'a bağlıyoruz
        rvProducts.setAdapter(adapter);

        return view;
    }
}