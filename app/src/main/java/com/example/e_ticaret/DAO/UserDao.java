package com.example.e_ticaret.DAO;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.e_ticaret.Helpers.DataBaseHelper;
import com.example.e_ticaret.dbModels.UserDbModel;

public class UserDao {

    private SQLiteDatabase database;
    private DataBaseHelper dbHelper;

    public UserDao(Context context){
        dbHelper = new DataBaseHelper(context);
    }

    public void open(){
        database = dbHelper.getWritableDatabase();
    }

    public void close(){
        if (database != null && database.isOpen()) {
            database.close(); // Asıl veritabanı bağlantısını kapatan satır budur!
        }
        if (dbHelper != null){
            dbHelper.close();
        }
    }

    public long addUser(String name, String surname, String username, String email, String phone, int age, String gender, String password){

        ContentValues values = new ContentValues();
        values.put(UserDbModel.NAME, name);
        values.put(UserDbModel.SURNAME, surname);
        values.put(UserDbModel.USERNAME, username);
        values.put(UserDbModel.EMAIL, email);
        values.put(UserDbModel.PHONE, phone);
        values.put(UserDbModel.AGE, age);
        values.put(UserDbModel.GENDER, gender);
        values.put(UserDbModel.PASSWORD, password);

        return database.insert(UserDbModel.TABLE,null,values);

    }
    public long checkUserLogin(String username, String password) {
        long userId = -1;

        String selection = UserDbModel.USERNAME + " = ? AND " + UserDbModel.PASSWORD + " = ?";
        String[] selectionArgs = {username, password};

        String[] columns = {UserDbModel.ID};

        Cursor cursor = database.query(
                UserDbModel.TABLE,
                columns,
                selection,
                selectionArgs,
                null,
                null,
                null
        );

        if (cursor != null && cursor.moveToFirst()) {
            userId = cursor.getLong(0);
            cursor.close();
        }

        return userId;
    }
}

