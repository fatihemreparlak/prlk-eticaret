package com.example.e_ticaret.DAO;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.e_ticaret.Helpers.DataBaseHelper;
import com.example.e_ticaret.dbModels.BasketDbModel;
import com.example.e_ticaret.dbModels.ProductDataModel;

import java.util.ArrayList;

public class BasketDao {
    private SQLiteDatabase database;
    private DataBaseHelper dbHelper;

    public BasketDao(Context context) {
        dbHelper = new DataBaseHelper(context);
    }

    public void open() {
        database = dbHelper.getWritableDatabase();
    }

    public void close() {
        if (database != null && database.isOpen()) {
            database.close();
        }
        // dbHelper.close() burada çağrılmamalıdır, SQLite bağlantı havuzunu korumak daha stabildir.
    }

    // SEPETE ÜRÜN EKLE VEYA MİKTARINI GÜNCELLE
    public void addToCart(int userId, int productId) {
        int currentQty = getProductQuantity(userId, productId);

        if (currentQty > 0) {
            updateQuantity(userId, productId, currentQty + 1);
        } else {
            ContentValues values = new ContentValues();
            values.put(BasketDbModel.USERID, userId);
            values.put(BasketDbModel.PRODUCTID, productId);
            values.put(BasketDbModel.QUANTITY, 1);
            database.insert(BasketDbModel.TABLE, null, values);
        }
    }

    // SEPETTEN ÜRÜNÜN MİKTARINI AZALT VEYA TAMAMEN SİL
    public void decreaseOrRemoveFromCart(int userId, int productId) {
        int currentQty = getProductQuantity(userId, productId);

        if (currentQty > 1) {
            updateQuantity(userId, productId, currentQty - 1);
        } else if (currentQty == 1) {
            removeFromCart(userId, productId);
        }
    }

    // BELİRLİ BİR ÜRÜNÜN SEPETTEKİ ADEDİNİ GETİR
    public int getProductQuantity(int userId, int productId) {
        Cursor cursor = database.rawQuery("SELECT " + BasketDbModel.QUANTITY + " FROM " + BasketDbModel.TABLE +
                        " WHERE " + BasketDbModel.USERID + " = ? AND " + BasketDbModel.PRODUCTID + " = ?",
                new String[]{String.valueOf(userId), String.valueOf(productId)});

        int quantity = 0;
        if (cursor.moveToFirst()) {
            int qtyIndex = cursor.getColumnIndex(BasketDbModel.QUANTITY);
            if (qtyIndex != -1) {
                quantity = cursor.getInt(qtyIndex);
            }
        }
        cursor.close();
        return quantity;
    }

    // MİKTARI DOĞRUDAN GÜNCELLE
    private void updateQuantity(int userId, int productId, int newQty) {
        ContentValues values = new ContentValues();
        values.put(BasketDbModel.QUANTITY, newQty);

        database.update(BasketDbModel.TABLE, values,
                BasketDbModel.USERID + " = ? AND " + BasketDbModel.PRODUCTID + " = ?",
                new String[]{String.valueOf(userId), String.valueOf(productId)});
    }

    // ÜRÜNÜ SEPETTEN TAMAMEN SİL
    public void removeFromCart(int userId, int productId) {
        database.delete(BasketDbModel.TABLE,
                BasketDbModel.USERID + " = ? AND " + BasketDbModel.PRODUCTID + " = ?",
                new String[]{String.valueOf(userId), String.valueOf(productId)});
    }

    //  SEPETTEKİ ÜRÜNLERİN TOPLAM TUTARINI HESAPLA
    public double getBasketTotalAmount(int userId) {
        double totalAmount = 0.0;

        String query = "SELECT p.Price, b." + BasketDbModel.QUANTITY + " FROM PRODUCT p " +
                "INNER JOIN " + BasketDbModel.TABLE + " b ON p.ID = b." + BasketDbModel.PRODUCTID + " " +
                "WHERE b." + BasketDbModel.USERID + " = ?";

        Cursor cursor = database.rawQuery(query, new String[]{String.valueOf(userId)});

        if (cursor.moveToFirst()) {
            do {
                double price = cursor.getDouble(cursor.getColumnIndexOrThrow("Price"));
                int quantity = cursor.getInt(cursor.getColumnIndexOrThrow(BasketDbModel.QUANTITY));
                totalAmount += (price * quantity);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return totalAmount;
    }

    public void clearBasket(int userId) {
        database.delete(BasketDbModel.TABLE, BasketDbModel.USERID + " = ?", new String[]{String.valueOf(userId)});
    }

    public ArrayList<ProductDataModel> getBasketProducts(int activeUserId) {
        ArrayList<ProductDataModel> basketList = new ArrayList<>();

        String query = "SELECT p.*, b." + BasketDbModel.QUANTITY + " FROM PRODUCT p " +
                "INNER JOIN " + BasketDbModel.TABLE + " b ON p.ID = b." + BasketDbModel.PRODUCTID + " " +
                "WHERE b." + BasketDbModel.USERID + " = ?";

        Cursor cursor = database.rawQuery(query, new String[]{String.valueOf(activeUserId)});

        if (cursor.moveToFirst()) {
            do {
                ProductDataModel product = new ProductDataModel();

                int idIndex = cursor.getColumnIndex("ID");
                int nameIndex = cursor.getColumnIndex("Name");
                int priceIndex = cursor.getColumnIndex("Price");
                int descIndex = cursor.getColumnIndex("Description");
                int catIndex = cursor.getColumnIndex("Category");
                int imageIndex = cursor.getColumnIndex("Image");
                int qtyIndex = cursor.getColumnIndex(BasketDbModel.QUANTITY);

                if (idIndex != -1) product.setId(cursor.getInt(idIndex));
                if (nameIndex != -1) product.setName(cursor.getString(nameIndex));
                if (priceIndex != -1) product.setPrice(cursor.getInt(priceIndex));
                if (descIndex != -1) product.setDescription(cursor.getString(descIndex));
                if (catIndex != -1) product.setCategory(cursor.getString(catIndex));
                if (imageIndex != -1) product.setImagePath(cursor.getString(imageIndex));

                if (qtyIndex != -1) product.setQuantity(cursor.getInt(qtyIndex));

                basketList.add(product);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return basketList;
    }
}