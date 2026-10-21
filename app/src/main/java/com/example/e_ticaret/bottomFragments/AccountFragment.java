package com.example.e_ticaret.bottomFragments;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
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
import com.example.e_ticaret.Helpers.DataBaseHelper;
import com.example.e_ticaret.LoginRegister.LoginActivity;
import com.example.e_ticaret.OrdersFragment;
import com.example.e_ticaret.R;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;

import java.util.Map;

public class AccountFragment extends Fragment {

    private TextView txtUsername, txtEmail;
    private MaterialButton btnLogout, btnAddAddress;
    private LinearLayout containerAddresses;

    private ActiveUserDao activeUserDao;
    private AddressDao addressDao;
    private int activeUserId;
    private MaterialCardView btnMaterialCardView;
    private OrdersFragment ordersFragment;

    public AccountFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_account, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        txtUsername = view.findViewById(R.id.txt_profile_username);
        txtEmail = view.findViewById(R.id.txt_profile_email);
        btnLogout = view.findViewById(R.id.btn_logout);
        btnAddAddress = view.findViewById(R.id.btn_add_address);
        containerAddresses = view.findViewById(R.id.container_addresses);
        btnMaterialCardView = view.findViewById(R.id.card_my_orders);

        activeUserDao = new ActiveUserDao(requireContext());
        addressDao = new AddressDao(requireContext());

        // Kullanıcı Kimliğini Al
        activeUserDao.open();
        activeUserId = (int) activeUserDao.getActiveUserId();
        activeUserDao.close();

        loadUserInfo();
        loadAddresses();
        orderClickListener();

        // Çıkış Yap Butonu - DOĞRUDAN LOGİNACTİVİTY YÖNLENDİRMESİ
        btnLogout.setOnClickListener(v -> {
            activeUserDao.open();
            activeUserDao.clearActiveUser();
            activeUserDao.close();

            Toast.makeText(requireContext(), "Başarıyla çıkış yapıldı.", Toast.LENGTH_SHORT).show();

            // Burası arka plan geçmişini silerek doğrudan LoginActivity'ye paslar
            Intent intent = new Intent(requireActivity(), LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            requireActivity().finish();
        });

        // Adres Ekleme Pop-up'ı
        btnAddAddress.setOnClickListener(v -> showAddAddressDialog());
    }

    private void loadUserInfo() {
        android.database.sqlite.SQLiteDatabase db = new DataBaseHelper(requireContext()).getReadableDatabase();
        android.database.Cursor cursor = db.rawQuery("SELECT Username, Email FROM USER WHERE ID = ?", new String[]{String.valueOf(activeUserId)});
        if (cursor.moveToFirst()) {
            txtUsername.setText(cursor.getString(0));
            txtEmail.setText(cursor.getString(1));
        }
        cursor.close();
        db.close();
    }

    private void loadAddresses() {
        containerAddresses.removeAllViews();
        addressDao.open();
        Map<Integer, String[]> addresses = addressDao.getUserAddresses(activeUserId);
        addressDao.close();

        for (Map.Entry<Integer, String[]> entry : addresses.entrySet()) {
            int addressId = entry.getKey();
            String title = entry.getValue()[0];
            String details = entry.getValue()[1];

            com.google.android.material.card.MaterialCardView card = new com.google.android.material.card.MaterialCardView(requireContext());
            card.setRadius(24);
            card.setCardElevation(2);
            card.setStrokeWidth(0);

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            params.setMargins(0, 8, 0, 8);
            card.setLayoutParams(params);

            LinearLayout layout = new LinearLayout(requireContext());
            layout.setOrientation(LinearLayout.VERTICAL);
            layout.setPadding(32, 24, 32, 24);

            TextView tTitle = new TextView(requireContext());
            tTitle.setText(title);
            tTitle.setTextSize(15);
            tTitle.setTypeface(android.graphics.Typeface.DEFAULT_BOLD);
            tTitle.setTextColor(getResources().getColor(R.color.text_dark));

            TextView tDetails = new TextView(requireContext());
            tDetails.setText(details);
            tDetails.setTextSize(13);
            tDetails.setTextColor(getResources().getColor(R.color.text_muted));
            tDetails.setPadding(0, 4, 0, 12);

            MaterialButton btnDelete = new MaterialButton(requireContext(), null, com.google.android.material.R.attr.borderlessButtonStyle);
            btnDelete.setText("Bu Adresi Sil");
            btnDelete.setTextColor(android.graphics.Color.parseColor("#E53935"));
            btnDelete.setTextSize(11);
            btnDelete.setGravity(android.view.Gravity.END);

            btnDelete.setOnClickListener(v -> {
                addressDao.open();
                addressDao.deleteAddress(addressId);
                addressDao.close();
                loadAddresses();
                Toast.makeText(requireContext(), "Adres silindi.", Toast.LENGTH_SHORT).show();
            });

            layout.addView(tTitle);
            layout.addView(tDetails);
            layout.addView(btnDelete);
            card.addView(layout);

            containerAddresses.addView(card);
        }
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
                loadAddresses();
                Toast.makeText(requireContext(), "Adres başarıyla eklendi.", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(requireContext(), "Alanlar boş bırakılamaz!", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("İptal", (dialog, which) -> dialog.cancel());
        builder.show();
    }

    private void orderClickListener(){
        btnMaterialCardView.setOnClickListener(v -> {
            OrdersFragment ordersFragment = new OrdersFragment();

            getParentFragmentManager().beginTransaction()
                    .setCustomAnimations(
                            android.R.anim.fade_in,
                            android.R.anim.fade_out,
                            android.R.anim.fade_in,
                            android.R.anim.fade_out
                    )
                    .replace(R.id.fragment_container, ordersFragment)
                    .addToBackStack(null)
                    .commit();
        });
    }
}