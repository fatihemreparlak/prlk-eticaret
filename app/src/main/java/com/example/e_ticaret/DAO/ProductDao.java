package com.example.e_ticaret.DAO;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.e_ticaret.Helpers.DataBaseHelper;
import com.example.e_ticaret.dbModels.ProductDataModel;
import com.example.e_ticaret.dbModels.ProductDbModel;

import java.util.ArrayList;
import java.util.List;

public class ProductDao {

    private SQLiteDatabase database;
    private final DataBaseHelper dbHelper;

    public ProductDao(Context context){
        dbHelper = new DataBaseHelper(context);
    }

    public void open(){
        database = dbHelper.getWritableDatabase();
    }

    public void close(){
        if (database != null && database.isOpen()) {
            database.close();
        }
        if (dbHelper != null){
            dbHelper.close();
        }
    }

    /**
     * Veritabanına yeni bir ürün ekler.
     */
    public long addProdcut(String name, int price , String description , String category, String createDate, int ownerId, String imageUrl){
        ContentValues values = new ContentValues();
        values.put(ProductDbModel.NAME, name);
        values.put(ProductDbModel.PRICE, price);
        values.put(ProductDbModel.DESCRIPTION, description);
        values.put(ProductDbModel.CATEGORY, category);
        values.put(ProductDbModel.CREATE_DATE, createDate);
        values.put(ProductDbModel.OWNER_ID, ownerId);
        values.put(ProductDbModel.IMAGE, imageUrl);

        return database.insert(ProductDbModel.TABLE, null, values);
    }

    /**
     * Veritabanındaki tüm ürünleri 'ProductDataModel' listesi olarak döndürür.
     * En yeni eklenen ürünün en üstte görünmesi için ID değerine göre azalan (DESC) sıralar.
     */
    @SuppressLint("Range")
    public List<ProductDataModel> getAllProducts() {
        List<ProductDataModel> list = new ArrayList<>();

        // Tablodaki tüm sütunları sorguluyoruz
        Cursor cursor = database.query(
                ProductDbModel.TABLE,
                null,
                null,
                null,
                null,
                null,
                ProductDbModel.ID + " DESC" // Sıralama: Yeniden eskiye
        );

        if (cursor != null && cursor.moveToFirst()) {
            do {
                ProductDataModel product = new ProductDataModel();

                // Cursor üzerindeki verileri senin şemana (ProductDbModel) göre çekip modele dolduruyoruz
                product.setId(cursor.getInt(cursor.getColumnIndex(ProductDbModel.ID)));
                product.setName(cursor.getString(cursor.getColumnIndex(ProductDbModel.NAME)));
                product.setPrice(cursor.getInt(cursor.getColumnIndex(ProductDbModel.PRICE)));
                product.setDescription(cursor.getString(cursor.getColumnIndex(ProductDbModel.DESCRIPTION)));
                product.setCategory(cursor.getString(cursor.getColumnIndex(ProductDbModel.CATEGORY)));
                product.setCreateDate(cursor.getString(cursor.getColumnIndex(ProductDbModel.CREATE_DATE)));
                product.setOwnerId(cursor.getInt(cursor.getColumnIndex(ProductDbModel.OWNER_ID)));
                product.setImagePath(cursor.getString(cursor.getColumnIndex(ProductDbModel.IMAGE)));

                list.add(product);
            } while (cursor.moveToNext());

            cursor.close();
        }

        return list;
    }
}