package com.example.memecollection;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

public class DBOpenHandler extends SQLiteOpenHelper {

    public static final String DB_NAME = "PictureDesc";

    private final String TABLE_NAME1 = "property";
    public DBOpenHandler(@Nullable Context context, @Nullable String name, @Nullable SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_TABLE = "CREATE TABLE property(id integer primary key autoincrement, url varchar(128) unique)"; //数据库中只记录图片在本机中的位置。
        db.execSQL(CREATE_TABLE);

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
