package com.example.e_ticaret.bottomFragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.e_ticaret.Adapters.BasketAdapter;
import com.example.e_ticaret.DAO.ActiveUserDao;
import com.example.e_ticaret.DAO.BasketDao;
// ÖNEMLİ: Ana dizinde serbest duran PaymentFragment'ı klasörün içine doğru paket yoluyla import ettik!
import com.example.e_ticaret.R;
import com.example.e_ticaret.dbModels.ProductDataModel;

import java.util.ArrayList;

public class BasketFragment extends Fragment implements BasketAdapter.OnBasketChangeListener {

    private RecyclerView rvBasket;
    private TextView txtTotalPrice, txtEmptyMessage;
    private View layoutCheckoutBar;

    private BasketAdapter adapter;
    private ArrayList<ProductDataModel> basketProducts;

    private BasketDao basketDao;
    private ActiveUserDao activeUserDao;
    private Button btnConfirmBasket;

    public BasketFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_basket, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        rvBasket = view.findViewById(R.id.rv_basket);
        txtTotalPrice = view.findViewById(R.id.txt_basket_total_price);
        txtEmptyMessage = view.findViewById(R.id.txt_empty_basket_message);
        layoutCheckoutBar = view.findViewById(R.id.layout_checkout_bar);
        btnConfirmBasket = view.findViewById(R.id.btn_checkout);

        basketDao = new BasketDao(requireContext());
        activeUserDao = new ActiveUserDao(requireContext());

        rvBasket.setLayoutManager(new LinearLayoutManager(requireContext()));

        loadBasket();
        clickListener();
    }

    private void clickListener(){
        btnConfirmBasket.setOnClickListener(v -> {
            // Doğru import sayesinde artık nesne hatasız bir şekilde üretilir
            PaymentFragment paymentFragment = new PaymentFragment();

            getParentFragmentManager().beginTransaction()
                    .setCustomAnimations(
                            android.R.anim.fade_in,
                            android.R.anim.fade_out,
                            android.R.anim.fade_in,
                            android.R.anim.fade_out
                    )
                    .replace(R.id.fragment_container, paymentFragment)
                    .addToBackStack(null)
                    .commit();
        });
    }

    private void loadBasket() {
        activeUserDao.open();
        int activeUserId = (int) activeUserDao.getActiveUserId();
        activeUserDao.close();

        basketDao.open();
        basketProducts = basketDao.getBasketProducts(activeUserId);
        basketDao.close();

        if (basketProducts.isEmpty()) {
            rvBasket.setVisibility(View.GONE);
            layoutCheckoutBar.setVisibility(View.GONE);
            txtEmptyMessage.setVisibility(View.VISIBLE);
        } else {
            rvBasket.setVisibility(View.VISIBLE);
            layoutCheckoutBar.setVisibility(View.VISIBLE);
            txtEmptyMessage.setVisibility(View.GONE);

            adapter = new BasketAdapter(basketProducts, this);
            rvBasket.setAdapter(adapter);
            calculateTotalPrice();
        }
    }

    private void calculateTotalPrice() {
        int total = 0;
        for (ProductDataModel product : basketProducts) {
            total += (product.getPrice() * product.getQuantity());
        }
        txtTotalPrice.setText(total + " TL");

        if (basketProducts.isEmpty()) {
            rvBasket.setVisibility(View.GONE);
            layoutCheckoutBar.setVisibility(View.GONE);
            txtEmptyMessage.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onBasketChanged() {
        calculateTotalPrice();
    }

    @Override
    public void onResume() {
        super.onResume();
        loadBasket();
    }
}