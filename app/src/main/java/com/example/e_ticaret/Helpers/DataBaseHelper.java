package com.example.e_ticaret.Helpers;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.e_ticaret.dbModels.ActiveUserDbModel;
import com.example.e_ticaret.dbModels.AddressDbModel;
import com.example.e_ticaret.dbModels.BasketDbModel;
import com.example.e_ticaret.dbModels.FavoriteDbModel;
import com.example.e_ticaret.dbModels.OrderDbModel;
import com.example.e_ticaret.dbModels.ProductDbModel;
import com.example.e_ticaret.dbModels.UserDbModel;

import java.security.PublicKey;

public class DataBaseHelper extends SQLiteOpenHelper {


    public static final String DATABASE_NAME = "PRLKEticaretDatabase.db";
    public static final int DATABASE_VERSION = 7 ;

    public DataBaseHelper(Context context){
        super(context, DATABASE_NAME,null ,DATABASE_VERSION);

    }

    private static final String CREATE_USER_TABLE = "CREATE TABLE IF NOT EXISTS " + UserDbModel.TABLE + " (" +
            UserDbModel.ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            UserDbModel.USERNAME + " TEXT NOT NULL UNIQUE , " +
            UserDbModel.NAME + " TEXT NOT NULL , " +
            UserDbModel.SURNAME + " TEXT NOT NULL , " +
            UserDbModel.PASSWORD + " TEXT NOT NULL , " +
            UserDbModel.EMAIL + " TEXT NOT NULL UNIQUE ," +
            UserDbModel.PHONE + " TEXT NOT NULL UNIQUE , " +
            UserDbModel.AGE + " INTEGER NOT NULL, " +
            UserDbModel.GENDER + " TEXT DEFAULT 'gender not specified' );";

    private static final String CREATE_TABLE_ACTIVE_USER = "CREATE TABLE " + ActiveUserDbModel.TABLE + " ("
            + ActiveUserDbModel.ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + ActiveUserDbModel.ACTIVEUSERID + " INTEGER NOT NULL"
            + ");";

    private static final String CREATE_PRODUCT_TABLE = "CREATE TABLE IF NOT EXISTS " + ProductDbModel.TABLE + " (" +
            ProductDbModel.ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            ProductDbModel.NAME + " TEXT NOT NULL, " +
            ProductDbModel.PRICE + " INTEGER NOT NULL, " +
            ProductDbModel.DESCRIPTION + " TEXT NOT NULL, " +
            ProductDbModel.CATEGORY + " TEXT NOT NULL, " +
            ProductDbModel.CREATE_DATE + " TEXT NOT NULL, " +
            ProductDbModel.OWNER_ID + " INTEGER NOT NULL, " +
            ProductDbModel.IMAGE + " TEXT, " +
            "FOREIGN KEY (" + ProductDbModel.OWNER_ID + ") REFERENCES " + UserDbModel.TABLE + "(" + UserDbModel.ID + "));";

    private static final String CREATE_FAVORITE_TABLE = "CREATE TABLE IF NOT EXISTS " + FavoriteDbModel.TABLE + " (" +
            FavoriteDbModel.ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            FavoriteDbModel.PRODUCTID + " INTEGER NOT NULL, " +
            FavoriteDbModel.USERID + " INTEGER NOT NULL, " +
            "FOREIGN KEY (" + FavoriteDbModel.USERID + ") REFERENCES " + UserDbModel.TABLE + "(" + UserDbModel.ID + ") ON DELETE CASCADE, " +
            "FOREIGN KEY (" + FavoriteDbModel.PRODUCTID + ") REFERENCES " + ProductDbModel.TABLE + "(" + ProductDbModel.ID + ") ON DELETE CASCADE);  " ;

    private static final String CREATE_BASKET_TABLE = "CREATE TABLE IF NOT EXISTS " + BasketDbModel.TABLE + " (" +
            BasketDbModel.ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            BasketDbModel.USERID + " INTEGER NOT NULL, " +
            BasketDbModel.PRODUCTID + " INTEGER NOT NULL, " +
            BasketDbModel.QUANTITY + " INTEGER NOT NULL, " +
            "FOREIGN KEY (" + BasketDbModel.USERID + ") REFERENCES " + UserDbModel.TABLE + "(" + UserDbModel.ID + ") ON DELETE CASCADE, " +
            "FOREIGN KEY (" + BasketDbModel.PRODUCTID + ") REFERENCES " + ProductDbModel.TABLE + "(" + ProductDbModel.ID + ") ON DELETE CASCADE); " ;

    private static final String CREATE_ADDRESS_TABLE = "CREATE TABLE IF NOT EXISTS " + AddressDbModel.TABLE + " (" +
            AddressDbModel.ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            AddressDbModel.USER_ID + " INTEGER NOT NULL, " +
            AddressDbModel.TITLE + " TEXT NOT NULL, " +
            AddressDbModel.DETAILS + " TEXT NOT NULL, " +
            "FOREIGN KEY (" + AddressDbModel.USER_ID + ") REFERENCES " + UserDbModel.TABLE + "(" + UserDbModel.ID + ") ON DELETE CASCADE);";

    public static final String CREATE_ORDER_TABLE = "CREATE TABLE IF NOT EXISTS " + OrderDbModel.TABLE + " (" +
            OrderDbModel.ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            OrderDbModel.BUYER_ID + " INTEGER NOT NULL, " +
            OrderDbModel.PRODUCT_ID + " INTEGER NOT NULL, " +
            OrderDbModel.QUANTITY + " INTEGER NOT NULL DEFAULT 1, " +
            "FOREIGN KEY (" + OrderDbModel.BUYER_ID + ") REFERENCES " + UserDbModel.TABLE + "(" + UserDbModel.ID + ") ON DELETE CASCADE); " ;

    @Override
    public void onCreate(SQLiteDatabase db) {
        //sadece ilk kurulumda çalışacak!! haricinde çalışırsa db sıfırlanır
        db.execSQL(CREATE_USER_TABLE);
        db.execSQL(CREATE_TABLE_ACTIVE_USER);
        db.execSQL(CREATE_PRODUCT_TABLE);
        db.execSQL(CREATE_FAVORITE_TABLE);
        db.execSQL(CREATE_BASKET_TABLE);
        db.execSQL(CREATE_ADDRESS_TABLE);
        db.execSQL(CREATE_ORDER_TABLE);
    }

    //KODDA BAZI ŞEYLERİ YAZDIKÇA YENİ TABLOLAR YAZDIM VE TÜM VERİ TABANI SİLİNMESİN DİYE VERSİYONU GÜNCELLEDİKKÇE BU ŞEKİLDE YENİ TABLO EKLEDİM
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 2) {
            db.execSQL(CREATE_TABLE_ACTIVE_USER);
        } else if (oldVersion < 3) {
            db.execSQL(CREATE_PRODUCT_TABLE);
        } else if (oldVersion < 4) {
            db.execSQL(CREATE_FAVORITE_TABLE);
        } else if (oldVersion < 5) {
            db.execSQL(CREATE_BASKET_TABLE);
        } else if (oldVersion < 6) {
            db.execSQL(CREATE_ADDRESS_TABLE);
        } else if (oldVersion < 7) {
            db.execSQL(CREATE_ORDER_TABLE);
        }
    }
}
