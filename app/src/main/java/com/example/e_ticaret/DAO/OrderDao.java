package com.example.e_ticaret.DAO;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.e_ticaret.Helpers.DataBaseHelper;
import com.example.e_ticaret.dbModels.OrderDbModel;
import com.example.e_ticaret.dbModels.ProductDataModel;

import java.util.ArrayList;

public class OrderDao {
    private SQLiteDatabase database;
    private DataBaseHelper dbHelper;

    public OrderDao(Context context) {
        dbHelper = new DataBaseHelper(context);
    }

    public void open() {
        database = dbHelper.getWritableDatabase();
    }

    public void close() {
        if (database != null && database.isOpen()) {
            database.close();
        }
    }

    // Sepetteki her bir ürünü döngüyle buraya gönderip ORDERS tablosuna basacağız
    public void addOrder(int buyerId, int productId, int quantity) {
        ContentValues values = new ContentValues();
        values.put(OrderDbModel.BUYER_ID, buyerId);
        values.put(OrderDbModel.PRODUCT_ID, productId);
        values.put(OrderDbModel.QUANTITY, quantity);

        database.insert(OrderDbModel.TABLE, null, values);
    }

    // ORDERS tablosu ile PRODUCT tablosunu birleştirerek sipariş geçmişi ekranı için verileri hazırlar
    public ArrayList<ProductDataModel> getUserOrders(int activeUserId) {
        ArrayList<ProductDataModel> orderList = new ArrayList<>();

        // SQLite INNER JOIN sorgusu: Alınan ürünün detaylarını (Ad, Fiyat, Resim vb.) PRODUCT tablosundan çeker
        String query = "SELECT p.*, o." + OrderDbModel.QUANTITY + " FROM PRODUCT p " +
                "INNER JOIN " + OrderDbModel.TABLE + " o ON p.ID = o." + OrderDbModel.PRODUCT_ID + " " +
                "WHERE o." + OrderDbModel.BUYER_ID + " = ?";

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
                int qtyIndex = cursor.getColumnIndex(OrderDbModel.QUANTITY);

                if (idIndex != -1) product.setId(cursor.getInt(idIndex));
                if (nameIndex != -1) product.setName(cursor.getString(nameIndex));
                if (priceIndex != -1) product.setPrice(cursor.getInt(priceIndex));
                if (descIndex != -1) product.setDescription(cursor.getString(descIndex));
                if (catIndex != -1) product.setCategory(cursor.getString(catIndex));
                if (imageIndex != -1) product.setImagePath(cursor.getString(imageIndex));

                // Sipariş edilen adet bilgisini set ediyoruz
                if (qtyIndex != -1) product.setQuantity(cursor.getInt(qtyIndex));

                orderList.add(product);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return orderList;
    }
}