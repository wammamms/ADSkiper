package com.seventyseven.adskiper.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

public class DbHelper extends SQLiteOpenHelper {

    private static final String DB_NAME = "ADSkiper";
    private static final int DB_VERSION = 1;

    //单例模式
    private static DbHelper HELPER = null;
    private static SQLiteDatabase db = null;

    private static DbHelper getInstance(Context context){
        if(HELPER == null){
            HELPER = new DbHelper(context,DB_NAME,null,DB_VERSION);
        }
        return HELPER;
    }

    public static SQLiteDatabase getDb(Context context){
        if(db == null){
            db = getInstance(context).getWritableDatabase();
        }
        return db;
    }

    private DbHelper(@Nullable Context context, @Nullable String name, @Nullable SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(RecordTable.CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(RecordTable.CREATE_TABLE);
    }

}
