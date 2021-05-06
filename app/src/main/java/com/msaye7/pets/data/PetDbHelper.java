package com.msaye7.pets.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.msaye7.pets.data.PetContract.PetEntry;

public class PetDbHelper extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 2;
    private static final String DATABASE_NAME = "pets.db";

    // LOG_TAG of the class.
    private static final String LOG_TAG = PetDbHelper.class.getSimpleName();

    public PetDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String PETS_TABLE_CREATE = "CREATE TABLE " + PetEntry.TABLE_NAME + "("
                + PetEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " + PetEntry.COLUMN_PET_NAME + " TEXT NOT NULL, "
                + PetEntry.COLUMN_PET_BREED + " TEXT, " + PetEntry.COLUMN_PET_GENDER + " INTEGER, "
                + PetEntry.COLUMN_PET_WEIGHT + " INTEGER DEFAULT 0);";
        db.execSQL(PETS_TABLE_CREATE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        String PETS_TABLE_DELETE = "DROP TABLE IF EXISTS " + PetEntry.TABLE_NAME + ";";
        db.execSQL(PETS_TABLE_DELETE);
        onCreate(db);
    }

}
