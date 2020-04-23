package com.example.SQLite;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.Fragment.ChatDBOpenHelper;
import com.example.Login.Register;

import org.apache.http.client.fluent.Content;

public class sql_exist {
    /**
     * 判断某张表是否存在
     * @param DBname  表名
     * @return
     */
    public static boolean tabbleIsExist(MyHelper myHelper,String DBname) {
        boolean result = false;
        if (DBname == null) {
            return false;
        }
        SQLiteDatabase db = null;
        Cursor cursor = null;
        try {
            db = myHelper.getReadableDatabase();
            String sql = "select count(*) as c from Sqlite_master  where type ='table' and name ='" + DBname.trim() + "' ";
            cursor = db.rawQuery(sql, null);
            if (cursor.moveToNext()) {
                int count = cursor.getInt(0);
                if (count > 0) {
                    result = true;
                }
            }

        } catch (Exception e) {
        }
        return result;
    }
}
