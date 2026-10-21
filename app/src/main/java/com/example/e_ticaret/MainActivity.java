package com.example.e_ticaret;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationBarView;

import com.example.e_ticaret.bottomFragments.HomeFragment;
import com.example.e_ticaret.bottomFragments.FavoritesFragment;
import com.example.e_ticaret.bottomFragments.BasketFragment;
import com.example.e_ticaret.bottomFragments.AccountFragment;

public class MainActivity extends AppCompatActivity {

    private BottomNavigationView bottomNav;
    private FloatingActionButton fabAddProduct;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bottomNav = findViewById(R.id.bottom_navigation);
        fabAddProduct = findViewById(R.id.fab_add_product);

        // 1. Ekran Boyutuna Göre Alt Menüyü Oranlama (WindowInsets Entegrasyonu)
        ViewCompat.setOnApplyWindowInsetsListener(bottomNav, (v, windowInsets) -> {
            // Sistemin en alttaki sanal gezinti çubuğunun yüksekliğini alıyoruz
            Insets systemBarsInsets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars());
            int bottomInset = systemBarsInsets.bottom;

            // Üst tarafa oval köşelerin altına inmesi için 16px, alt tarafa ise sistem çubuğu + 32px ek dolgunluk veriyoruz
            v.setPadding(0, 16, 0, bottomInset + 32);
            return windowInsets;
        });

        // 2. Uygulama ilk açıldığında varsayılan olarak HomeFragment'ı yükle
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new HomeFragment())
                    .commit();
        }

        // 3. Bottom Navigation Tıklama Olayları Kontrolü
        bottomNav.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                Fragment selectedFragment = null;
                int id = item.getItemId();

                if (id == R.id.nav_home) {
                    selectedFragment = new HomeFragment();
                    fabAddProduct.show(); // Sadece ana sayfada ürün ekleme butonunu göster
                } else if (id == R.id.nav_favorites) {
                    selectedFragment = new FavoritesFragment();
                    fabAddProduct.hide(); // Diğer sekmelerde gizle
                } else if (id == R.id.nav_cart) {
                    selectedFragment = new BasketFragment();
                    fabAddProduct.hide();
                } else if (id == R.id.nav_account) {
                    selectedFragment = new AccountFragment();
                    fabAddProduct.hide();
                }

                if (selectedFragment != null) {
                    getSupportFragmentManager().beginTransaction()
                            .replace(R.id.fragment_container, selectedFragment)
                            .commit();
                }
                return true;
            }
        });

        // 4. FloatingActionButton Tıklaması (Ürün Ekleme Sayfası)
        fabAddProduct.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fabAddProduct.hide();

                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, new ProductAddFragment())
                        .addToBackStack(null) // Geri tuşuyla ana sayfaya dönülebilmesi için hafızaya al
                        .commit();
            }
        });

        // 5. Modern Geri Tuşu Yönetimi
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
                    getSupportFragmentManager().popBackStack();

                    bottomNav.post(new Runnable() {
                        @Override
                        public void run() {
                            Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.fragment_container);
                            if (currentFragment instanceof HomeFragment) {
                                fabAddProduct.show();
                            }
                        }
                    });
                } else {
                    setEnabled(false);
                    getOnBackPressedDispatcher().onBackPressed();
                }
            }
        });
    }
}