package com.example.e_ticaret;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.e_ticaret.Adapters.OrdersAdapter;
import com.example.e_ticaret.DAO.ActiveUserDao;
import com.example.e_ticaret.DAO.OrderDao;
import com.example.e_ticaret.dbModels.ProductDataModel;

import java.util.ArrayList;

public class OrdersFragment extends Fragment {

    private RecyclerView rvOrders;
    private TextView txtEmptyMessage;
    private OrderDao orderDao;
    private int activeUserId;

    public OrdersFragment() {}

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_orders, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        rvOrders = view.findViewById(R.id.rv_orders);
        txtEmptyMessage = view.findViewById(R.id.txt_empty_orders_message);
        rvOrders.setLayoutManager(new LinearLayoutManager(requireContext()));

        orderDao = new OrderDao(requireContext());

        // 1. Aktif Kullanıcı ID'sini Çek
        ActiveUserDao activeUserDao = new ActiveUserDao(requireContext());
        activeUserDao.open();
        activeUserId = (int) activeUserDao.getActiveUserId();
        activeUserDao.close();

        // 2. Siparişleri Veritabanından Yükle ve Listele
        loadOrders();
    }

    private void loadOrders() {
        orderDao.open();
        ArrayList<ProductDataModel> userOrders = orderDao.getUserOrders(activeUserId);
        orderDao.close();

        if (userOrders == null || userOrders.isEmpty()) {
            rvOrders.setVisibility(View.GONE);
            txtEmptyMessage.setVisibility(View.VISIBLE);
        } else {
            rvOrders.setVisibility(View.VISIBLE);
            txtEmptyMessage.setVisibility(View.GONE);

            OrdersAdapter adapter = new OrdersAdapter(userOrders);
            rvOrders.setAdapter(adapter);
        }
    }
}