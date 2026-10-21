package com.example.e_ticaret;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.e_ticaret.DAO.ActiveUserDao;
import com.example.e_ticaret.DAO.ProductDao;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

public class ProductAddFragment extends Fragment {

    // View Elemanları
    private FrameLayout frameSelectImage;
    private ImageView imgProductPreview;
    private TextInputEditText etTitle, etDescription, etPrice;
    private AutoCompleteTextView autoCompleteCategories;
    private MaterialButton btnSave;

    // Veritabanı Katmanları
    private ActiveUserDao activeUserDao;
    private ProductDao productDao;

    private Uri selectedImageUri = null;

    // Sabit Kategoriler Listesi
    private final String[] categories = {
            "Elektronik", "Moda ve Giyim", "Ev ve Yaşam",
            "Kozmetik ve Kişisel Bakım", "Spor ve Outdoor",
            "Kitap, Hobi ve Oyun", "Otomobil ve Motosiklet", "Diğer"
    };

    // Galeriden Görsel Seçme Mekanizması
    private final ActivityResultLauncher<Intent> imagePickerLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    selectedImageUri = result.getData().getData();
                    imgProductPreview.setImageURI(selectedImageUri);
                    imgProductPreview.setScaleType(ImageView.ScaleType.CENTER_CROP);
                }
            }
    );

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle bundle) {
        View view = inflater.inflate(R.layout.fragment_product_add, container, false);

        initializeViews(view);

        return view;
    }


    @SuppressLint("MissingInflatedId")
    private void initializeViews(View view) {
        // FindViewByID İşlemleri
        frameSelectImage = view.findViewById(R.id.frame_select_image);
        imgProductPreview = view.findViewById(R.id.img_product_preview);
        etTitle = view.findViewById(R.id.et_product_title);
        etDescription = view.findViewById(R.id.et_product_description);
        etPrice = view.findViewById(R.id.et_product_price);
        autoCompleteCategories = view.findViewById(R.id.auto_complete_categories);
        btnSave = view.findViewById(R.id.btn_save_product);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_dropdown_item_1line, categories);
        autoCompleteCategories.setAdapter(adapter);

        frameSelectImage.setOnClickListener(v -> openGallery());
        btnSave.setOnClickListener(v -> saveProduct());
    }

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        imagePickerLauncher.launch(intent);
    }

    private void saveProduct() {
        String title = etTitle.getText().toString().trim();
        String description = etDescription.getText().toString().trim();
        String strPrice = etPrice.getText().toString().trim();
        String category = autoCompleteCategories.getText().toString();

        if (title.isEmpty() || description.isEmpty() || strPrice.isEmpty() || category.isEmpty()) {
            Toast.makeText(getContext(), "Lütfen tüm alanları doldurun!", Toast.LENGTH_SHORT).show();
            return;
        }

        if (selectedImageUri == null) {
            Toast.makeText(getContext(), "Lütfen ürün için bir görsel seçin!", Toast.LENGTH_SHORT).show();
            return;
        }

        String imagePath = saveImageToInternalStorage(selectedImageUri);
        if (imagePath == null) {
            Toast.makeText(getContext(), "Görsel kaydedilirken bir hata oluştu!", Toast.LENGTH_SHORT).show();
            return;
        }

        // Tarih Oluşturma
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd.MM.yyyy", java.util.Locale.getDefault());
        String currentDate = sdf.format(new java.util.Date());

        int priceValue = Integer.parseInt(strPrice);

        // 4. Aktif Kullanıcı ID sini Çekme
        activeUserDao = new ActiveUserDao(requireContext());
        activeUserDao.open();
        int activeUser = activeUserDao.getActiveUserId();
        activeUserDao.close();

        productDao = new ProductDao(requireContext());
        productDao.open();
        long result = productDao.addProdcut(title, priceValue, description, category, currentDate, activeUser, imagePath);
        productDao.close();

        if (result != -1) {
            Toast.makeText(getContext(), "Ürün başarıyla eklendi!", Toast.LENGTH_SHORT).show();
            getParentFragmentManager().popBackStack();
        } else {
            Toast.makeText(getContext(), "Ürün kaydedilirken bir hata oluştu!", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Görseli kalıcı hafızaya kopyalar ve dosya yolunu döner.
     */
    private String saveImageToInternalStorage(Uri uri) {
        try {
            String fileName = "product_" + System.currentTimeMillis() + ".jpg";
            java.io.File file = new java.io.File(requireContext().getFilesDir(), fileName);

            java.io.InputStream inputStream = requireContext().getContentResolver().openInputStream(uri);
            java.io.FileOutputStream outputStream = new java.io.FileOutputStream(file);

            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }

            outputStream.close();
            inputStream.close();

            return file.getAbsolutePath();

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}