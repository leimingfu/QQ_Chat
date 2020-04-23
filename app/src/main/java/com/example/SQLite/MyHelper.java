package com.example.SQLite;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class MyHelper extends SQLiteOpenHelper {
    public MyHelper(Context context, String name){
        super(context,name,null,1);
    }
    @Override
    public void onCreate(SQLiteDatabase db) {
        //db.execSQL("create table Noteinfo (_id integer primary " + " key autoincrement,user text,word text)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        System.out.println("数据库升级了");
    }
}
