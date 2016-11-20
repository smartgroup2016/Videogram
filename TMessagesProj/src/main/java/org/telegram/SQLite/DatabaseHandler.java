package org.telegram.SQLite;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import org.telegram.messenger.FileLog;
import org.telegram.ui.Components.Favourite;

public class DatabaseHandler extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "favourites";
    private static final int DATABASE_VERSION = 1;
    private static final String KEY_CHAT_ID = "chat_id";
    private static final String KEY_ID = "id";
    private static final String TABLE_FAVS = "tbl_favs";

    public DatabaseHandler(Context paramContext) {
        super(paramContext, DATABASE_NAME, null, DATABASE_VERSION);
    }

    public void addFavourite(Favourite paramFavourite) {
        SQLiteDatabase localSQLiteDatabase = getWritableDatabase();
        ContentValues localContentValues = new ContentValues();
        localContentValues.put(KEY_CHAT_ID, Long.valueOf(paramFavourite.getChatID()));
        localSQLiteDatabase.insert(TABLE_FAVS, null, localContentValues);
        localSQLiteDatabase.close();
    }

    public void deleteFavourite(Long paramLong) {
        SQLiteDatabase localSQLiteDatabase = getWritableDatabase();
        String[] strArr = new String[DATABASE_VERSION];
        strArr[0] = String.valueOf(paramLong);
        localSQLiteDatabase.delete(TABLE_FAVS, "chat_id = ?", strArr);
        localSQLiteDatabase.close();
    }

    public Favourite getFavouriteByChatId(long paramLong) {
        Favourite localFavourite;
        Throwable th;
        Cursor cursor = null;
        try {
            SQLiteDatabase readableDatabase = getReadableDatabase();
            String str = TABLE_FAVS;
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
                    localFavourite = new Favourite(localObject4.getLong(DATABASE_VERSION));
                    if (cursor != null) {
                        cursor.close();
                    }
                    return localFavourite;
                }
            }
            localFavourite = null;
            if (cursor != null) {
                cursor.close();
            }
        } catch (Throwable localException) {
            if (cursor != null) {
                cursor.close();
            }
            FileLog.e("tmessages", localException);
            localFavourite = null;
            if (null != null) {
                ((Cursor) null).close();
            }

        }
        return localFavourite;
    }
    @Override
    public void onCreate(SQLiteDatabase paramSQLiteDatabase) {
        paramSQLiteDatabase.execSQL("CREATE TABLE tbl_favs(id INTEGER PRIMARY KEY AUTOINCREMENT,chat_id INTEGER)");
    }
    @Override
    public void onUpgrade(SQLiteDatabase paramSQLiteDatabase, int paramInt1, int paramInt2) {
        paramSQLiteDatabase.execSQL("DROP TABLE IF EXISTS tbl_favs");
        onCreate(paramSQLiteDatabase);
    }
}
