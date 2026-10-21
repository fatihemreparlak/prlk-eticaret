package com.example.e_ticaret.DAO;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import com.example.e_ticaret.Helpers.DataBaseHelper;
import com.example.e_ticaret.dbModels.AddressDbModel;
import java.util.HashMap;
import java.util.Map;

public class AddressDao {
    private SQLiteDatabase database;
    private DataBaseHelper dbHelper;

    public AddressDao(Context context) {
        dbHelper = new DataBaseHelper(context);
    }

    public void open() { database = dbHelper.getWritableDatabase(); }
    public void close() { dbHelper.close(); }

    public long addAddress(int userId, String title, String details) {
        ContentValues values = new ContentValues();
        values.put(AddressDbModel.USER_ID, userId);
        values.put(AddressDbModel.TITLE, title);
        values.put(AddressDbModel.DETAILS, details);
        return database.insert(AddressDbModel.TABLE, null, values);
    }

    public void deleteAddress(int addressId) {
        database.delete(AddressDbModel.TABLE, AddressDbModel.ID + " = ?", new String[]{String.valueOf(addressId)});
    }

    public Map<Integer, String[]> getUserAddresses(int userId) {
        Map<Integer, String[]> addresses = new HashMap<>();
        Cursor cursor = database.rawQuery("SELECT * FROM " + AddressDbModel.TABLE + " WHERE " + AddressDbModel.USER_ID + " = ?", new String[]{String.valueOf(userId)});

        if (cursor.moveToFirst()) {
            do {
                int id = cursor.getInt(cursor.getColumnIndexOrThrow(AddressDbModel.ID));
                String title = cursor.getString(cursor.getColumnIndexOrThrow(AddressDbModel.TITLE));
                String details = cursor.getString(cursor.getColumnIndexOrThrow(AddressDbModel.DETAILS));
                addresses.put(id, new String[]{title, details});
            } while (cursor.moveToNext());
        }
        cursor.close();
        return addresses;
    }
}