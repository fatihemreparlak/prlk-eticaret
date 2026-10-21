package com.example.e_ticaret.DAO;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.e_ticaret.Helpers.DataBaseHelper;
import com.example.e_ticaret.dbModels.ActiveUserDbModel;

public class ActiveUserDao {

    private SQLiteDatabase database;
    private DataBaseHelper dbHelper;

    public ActiveUserDao(Context context) {
        dbHelper = new DataBaseHelper(context);
    }

    public void open() {
        database = dbHelper.getWritableDatabase();
    }

    public void close() {
        if (database != null && database.isOpen()) {
            database.close();
        }
        if (dbHelper != null) {
            dbHelper.close();
        }
    }

    //giriş yapan kullanıcı için aCTİVEuSERını ekliyoruz
    public long setActiveUser(long userId) {
        ContentValues values = new ContentValues();

        values.put(ActiveUserDbModel.ID, 1);
        values.put(ActiveUserDbModel.ACTIVEUSERID, userId);

        return database.insertWithOnConflict(
                ActiveUserDbModel.TABLE,
                null,
                values,
                SQLiteDatabase.CONFLICT_REPLACE
        );
    }

    //çıkış yapıldığında activeUserIdyi silme
    public void clearActiveUser() {
        database.delete(ActiveUserDbModel.TABLE, null, null);
    }

    public int getActiveUserId(){
        int activeUserId = 0 ;

        String query = "SELECT " + ActiveUserDbModel.ACTIVEUSERID + " FROM " + ActiveUserDbModel.TABLE + " WHERE " + ActiveUserDbModel.ID + " =1";
        Cursor cursor = database.rawQuery(query, null);

        if (cursor.moveToFirst()){
            activeUserId= cursor.getInt(0);
        }
        cursor.close();
        return activeUserId;
    }
}