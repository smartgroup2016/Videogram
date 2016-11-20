package org.telegram.SQLite;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import org.telegram.messenger.FileLog;
import org.telegram.ui.Components.Suggest;

public class DatabaseHandler2 extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "Suggests";
    private static final int DATABASE_VERSION = 1;
    private static final String KEY_CHAT_ID = "chat_id";
    private static final String KEY_ID = "id";
    private static final String TABLE_SUGS = "tbl_suggest";

    public DatabaseHandler2(Context paramContext) {
        super(paramContext, DATABASE_NAME, null, DATABASE_VERSION);
    }

    public void addSuggest(Suggest paramSuggest) {
        SQLiteDatabase localSQLiteDatabase = getWritableDatabase();
        ContentValues localContentValues = new ContentValues();
        localContentValues.put(KEY_CHAT_ID, Long.valueOf(paramSuggest.getChatID()));
        localSQLiteDatabase.insert(TABLE_SUGS, null, localContentValues);
        localSQLiteDatabase.close();
    }

    public void deleteSuggest(Long paramLong) {
        SQLiteDatabase localSQLiteDatabase = getWritableDatabase();
        String[] strArr = new String[DATABASE_VERSION];
        strArr[0] = String.valueOf(paramLong);
        localSQLiteDatabase.delete(TABLE_SUGS, "chat_id = ?", strArr);
        localSQLiteDatabase.close();
    }

    public Suggest getSuggestByChatId(long paramLong) {
        Suggest localSuggest;
        Throwable th;
        Cursor cursor = null;
        try {
            SQLiteDatabase readableDatabase = getReadableDatabase();
            String str = TABLE_SUGS;
            String[] strArr = new String[]{KEY_ID, KEY_CHAT_ID};
            String[] strArr2 = new String[DATABASE_VERSION];
            strArr2[0] = String.valueOf(paramLong);
            Cursor localObject4 = readableDatabase.query(str, strArr, "chat_id=?", strArr2, null, null, null);
            if (localObject4 != null) {
                Cursor localObject3 = localObject4;
                cursor = localObject4;
                if (localObject4.moveToFirst()) {
                    localObject3 = localObject4;
                    cursor = localObject4;
                    localSuggest = new Suggest(localObject4.getLong(DATABASE_VERSION));
                    if (cursor != null) {
                        cursor.close();
                    }
                    return localSuggest;
                }
            }
            localSuggest = null;
            if (cursor != null) {
                cursor.close();
            }
        } catch (Throwable localException) {
            if (cursor != null) {
                cursor.close();
            }
            FileLog.e("tmessages", localException);
            localSuggest = null;
            if (null != null) {
                ((Cursor) null).close();
            }

        }
        return localSuggest;
    }
    @Override
    public void onCreate(SQLiteDatabase paramSQLiteDatabase) {
        paramSQLiteDatabase.execSQL("CREATE TABLE tbl_suggest(id INTEGER PRIMARY KEY AUTOINCREMENT,chat_id INTEGER)");
    }
    @Override
    public void onUpgrade(SQLiteDatabase paramSQLiteDatabase, int paramInt1, int paramInt2) {
        paramSQLiteDatabase.execSQL("DROP TABLE IF EXISTS tbl_suggest");
        onCreate(paramSQLiteDatabase);
    }
}
