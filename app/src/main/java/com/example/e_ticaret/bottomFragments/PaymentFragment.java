package com.example.e_ticaret.bottomFragments;

import android.app.AlertDialog;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.e_ticaret.DAO.ActiveUserDao;
import com.example.e_ticaret.DAO.AddressDao;
import com.example.e_ticaret.DAO.BasketDao;
import com.example.e_ticaret.DAO.OrderDao;
import com.example.e_ticaret.R;
import com.example.e_ticaret.dbModels.ProductDataModel;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.Map;

public class PaymentFragment extends Fragment {

    private LinearLayout containerAddresses;
    private MaterialButton btnAddAddress, btnConfirmOrder;
    private EditText edtCardNumber, edtCardHolder, edtExpiry, edtCvv;
    private TextView txtTotalPrice;

    private AddressDao addressDao;
    private BasketDao basketDao;
    private OrderDao orderDao;
    private int activeUserId;
    private int selectedAddressId = -1;

    public PaymentFragment() {}

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_payment, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initViewsAndDaos(view);

        ActiveUserDao activeUserDao = new ActiveUserDao(requireContext());
        activeUserDao.open();
        activeUserId = (int) activeUserDao.getActiveUserId();
        activeUserDao.close();

        loadUserAddresses();
        loadBasketTotal();

        setupCardNumberFormatting();

        btnAddAddress.setOnClickListener(v -> showAddAddressDialog());
        btnConfirmOrder.setOnClickListener(v -> validateAndFinalizeOrder());
    }

    private void initViewsAndDaos(View view) {
        containerAddresses = view.findViewById(R.id.container_payment_addresses);
        btnAddAddress = view.findViewById(R.id.btn_payment_add_address);
        btnConfirmOrder = view.findViewById(R.id.btn_payment_confirm_order);
        edtCardNumber = view.findViewById(R.id.edt_payment_card_number);
        edtCardHolder = view.findViewById(R.id.edt_payment_card_holder);
        edtExpiry = view.findViewById(R.id.edt_payment_expiry);
        edtCvv = view.findViewById(R.id.edt_payment_cvv);
        txtTotalPrice = view.findViewById(R.id.txt_payment_total_price);

        addressDao = new AddressDao(requireContext());
        basketDao = new BasketDao(requireContext());
        orderDao = new OrderDao(requireContext());
    }

    private void loadUserAddresses() {
        containerAddresses.removeAllViews();
        addressDao.open();
        Map<Integer, String[]> addresses = addressDao.getUserAddresses(activeUserId);
        addressDao.close();

        for (Map.Entry<Integer, String[]> entry : addresses.entrySet()) {
            int id = entry.getKey();
            String title = entry.getValue()[0];
            String details = entry.getValue()[1];

            com.google.android.material.card.MaterialCardView card = new com.google.android.material.card.MaterialCardView(requireContext());
            card.setRadius(24);
            card.setCardElevation(1);

            card.setStrokeWidth(id == selectedAddressId ? 4 : 0);
            card.setStrokeColor(getResources().getColor(R.color.prlk_primary));
            card.setCardBackgroundColor(ColorStateList.valueOf(Color.parseColor("#FFFFFF")));

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            params.setMargins(0, 8, 0, 8);
            card.setLayoutParams(params);

            LinearLayout layout = new LinearLayout(requireContext());
            layout.setOrientation(LinearLayout.VERTICAL);
            layout.setPadding(24, 20, 24, 20);

            TextView tTitle = new TextView(requireContext());
            tTitle.setText(title);
            tTitle.setTextSize(14);
            tTitle.setTypeface(android.graphics.Typeface.DEFAULT_BOLD);
            tTitle.setTextColor(getResources().getColor(R.color.text_dark));

            TextView tDetails = new TextView(requireContext());
            tDetails.setText(details);
            tDetails.setTextSize(12);
            tDetails.setTextColor(getResources().getColor(R.color.text_muted));
            tDetails.setPadding(0, 2, 0, 0);

            layout.addView(tTitle);
            layout.addView(tDetails);
            card.addView(layout);

            card.setOnClickListener(v -> {
                selectedAddressId = id;
                loadUserAddresses();
            });

            containerAddresses.addView(card);
        }
    }

    private void loadBasketTotal() {
        basketDao.open();
        double total = basketDao.getBasketTotalAmount(activeUserId);
        basketDao.close();
        txtTotalPrice.setText(String.format("%.2f TL", total));
    }

    private void setupCardNumberFormatting() {
        // Kart Numarası Canlı Boşluk Bırakma Algoritması (5555 4444 6666 3333)
        edtCardNumber.addTextChangedListener(new TextWatcher() {
            private boolean isUpdating = false;

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                if (isUpdating) return;

                isUpdating = true;

                String original = s.toString().replaceAll(" ", "");
                StringBuilder formatted = new StringBuilder();

                for (int i = 0; i < original.length(); i++) {
                    if (i > 0 && i % 4 == 0) {
                        formatted.append(" ");
                    }
                    formatted.append(original.charAt(i));
                }

                edtCardNumber.setText(formatted.toString());
                edtCardNumber.setSelection(formatted.length());

                isUpdating = false;
            }
        });

        //  Son Kullanma Tarihi Canlı Eğik Çizgi Bırakma Algoritması (AA/YY)
        edtExpiry.addTextChangedListener(new TextWatcher() {
            private boolean isUpdating = false;

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                if (isUpdating) return;

                isUpdating = true;

                String original = s.toString().replaceAll("/", "");
                StringBuilder formatted = new StringBuilder();

                for (int i = 0; i < original.length(); i++) {
                    if (i == 2) {
                        formatted.append("/");
                    }
                    formatted.append(original.charAt(i));
                }

                edtExpiry.setText(formatted.toString());
                edtExpiry.setSelection(formatted.length());

                isUpdating = false;
            }
        });

        edtCvv.setFilters(new android.text.InputFilter[]{new android.text.InputFilter.LengthFilter(3)});
    }

    private void showAddAddressDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Yeni Adres Ekle");

        LinearLayout layout = new LinearLayout(requireContext());
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(40, 20, 40, 20);

        final EditText inputTitle = new EditText(requireContext());
        inputTitle.setHint("Adres Başlığı (Örn: Evim)");
        layout.addView(inputTitle);

        final EditText inputDetails = new EditText(requireContext());
        inputDetails.setHint("Tam Adres Detayı");
        inputDetails.setPadding(0, 20, 0, 0);
        layout.addView(inputDetails);

        builder.setView(layout);

        builder.setPositiveButton("Kaydet", (dialog, which) -> {
            String title = inputTitle.getText().toString().trim();
            String details = inputDetails.getText().toString().trim();

            if (!title.isEmpty() && !details.isEmpty()) {
                addressDao.open();
                addressDao.addAddress(activeUserId, title, details);
                addressDao.close();
                loadUserAddresses();
                Toast.makeText(requireContext(), "Adres eklendi ve listelendi.", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(requireContext(), "Lütfen alanları boş bırakmayın!", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("İptal", (dialog, which) -> dialog.cancel());
        builder.show();
    }

    // Siparişi Kontrol Edip Sepeti Boşaltan Final Onay Yapısı
    private void validateAndFinalizeOrder() {
        String cardNo = edtCardNumber.getText().toString().trim();
        String holder = edtCardHolder.getText().toString().trim();
        String expiry = edtExpiry.getText().toString().trim();
        String cvv = edtCvv.getText().toString().trim();

        if (selectedAddressId == -1) {
            Toast.makeText(requireContext(), "Lütfen teslimat için bir adres seçin!", Toast.LENGTH_SHORT).show();
            return;
        }

        if (cardNo.length() < 19 || holder.isEmpty() || expiry.length() < 5 || cvv.length() < 3) {
            Toast.makeText(requireContext(), "Lütfen kart bilgilerinizi eksiksiz ve doğru girin!", Toast.LENGTH_SHORT).show();
            return;
        }

        ActiveUserDao activeUserDao = new ActiveUserDao(requireContext());
        activeUserDao.open();
        activeUserId = (int) activeUserDao.getActiveUserId();
        activeUserDao.close();

        basketDao.open();
        ArrayList<ProductDataModel> basketProducts = basketDao.getBasketProducts(activeUserId);
        basketDao.close();

        if (basketProducts != null && !basketProducts.isEmpty()) {
            orderDao.open();
            for (ProductDataModel product : basketProducts) {
                orderDao.addOrder(activeUserId, product.getId(), product.getQuantity());
            }
            orderDao.close();
        }

        // 3. Siparişi Tamamla, Sepeti Uçur
        basketDao.open();
        basketDao.clearBasket(activeUserId);
        basketDao.close();

        Toast.makeText(requireContext(), "Siparişiniz başarıyla alındı! PRLK premium alışveriş kalitesini seçtiğiniz için teşekkür ederiz.", Toast.LENGTH_LONG).show();

        // Ödeme bittiği için kullanıcıyı bir önceki ekran olan Sepetim'e geri güvenle yolla
        if (getParentFragmentManager() != null) {
            getParentFragmentManager().popBackStack();
        }
    }
}