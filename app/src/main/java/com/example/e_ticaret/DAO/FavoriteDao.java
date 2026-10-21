package com.example.e_ticaret.DAO;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.e_ticaret.Helpers.DataBaseHelper;
import com.example.e_ticaret.dbModels.FavoriteDbModel;
import com.example.e_ticaret.dbModels.ProductDataModel;

import java.util.ArrayList;

public class FavoriteDao {
    private SQLiteDatabase database;
    private DataBaseHelper dbHelper;

    public FavoriteDao(Context context){
        dbHelper = new DataBaseHelper(context);
    }

    public void open(){
        database = dbHelper.getWritableDatabase();
    }

    public void close(){
        if (database != null && database.isOpen()) {
            database.close();
        }
        if (dbHelper != null) {
            dbHelper.close();
        }
    }

    public long addFavorite(int userId, int productId){
        ContentValues values = new ContentValues();
        values.put(FavoriteDbModel.USERID, userId);
        values.put(FavoriteDbModel.PRODUCTID, productId);

        return database.insert(FavoriteDbModel.TABLE, null, values);
    }

    public void removeFavorite(int userId, int productId) {
        database.delete(FavoriteDbModel.TABLE,
                FavoriteDbModel.USERID + " = ? AND " + FavoriteDbModel.PRODUCTID + " = ?",
                new String[]{String.valueOf(userId), String.valueOf(productId)});
    }

    public boolean isFavorite(int userId, int productId) {
        Cursor cursor = database.rawQuery("SELECT * FROM " + FavoriteDbModel.TABLE + " WHERE "
                        + FavoriteDbModel.USERID + " = ? AND " + FavoriteDbModel.PRODUCTID + " = ?",
                new String[]{String.valueOf(userId), String.valueOf(productId)});

        boolean exists = cursor.getCount() > 0;
        cursor.close();
        return exists;
    }

    public ArrayList<ProductDataModel> getFavoriteProducts(int activeUserId) {
        ArrayList<ProductDataModel> favoriteList = new ArrayList<>();

        // PRODUCT tablosu ile FAVORITE tablosunu ortak ürün ID'leri üzerinden birbirine bağlıyoruz
        String query = "SELECT p.* FROM PRODUCT p " +
                "INNER JOIN " + FavoriteDbModel.TABLE + " f ON p.ID = f." + FavoriteDbModel.PRODUCTID + " " +
                "WHERE f." + FavoriteDbModel.USERID + " = ?";

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

                if (idIndex != -1) product.setId(cursor.getInt(idIndex));
                if (nameIndex != -1) product.setName(cursor.getString(nameIndex));
                if (priceIndex != -1) product.setPrice(cursor.getInt(priceIndex));
                if (descIndex != -1) product.setDescription(cursor.getString(descIndex));
                if (catIndex != -1) product.setCategory(cursor.getString(catIndex));
                if (imageIndex != -1) product.setImagePath(cursor.getString(imageIndex));

                favoriteList.add(product);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return favoriteList;
    }
}