package com.msaye7.pets.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.msaye7.pets.data.PetContract.PetEntry;

/**
 * {@link ContentProvider} for Pets app.
 */

public class PetProvider extends ContentProvider {

    /**
     * Database helper object.
     */
    private PetDbHelper mDbHelper;

    /**
     * LOG_TAG of the class.
     */
    private static final String LOG_TAG = PetProvider.class.getSimpleName();

    /**
     * Uri patterns codes
     */
    private static final int PETS = 100;
    private static final int PET_ID = 101;

    /**
     * Initializes the Uri matcher object that will make sure that the uri sent is
     * written correctly and returns its suitable code.
     */
    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    static {

        /* Adds the uri patterns to the Uri matcher to validate it's the correct Uri and
          gives it the suitable code to return when used. */

        // adds the Uri that will deal with the full table of pets
        sUriMatcher.addURI(PetContract.CONTENT_AUTHORITY, PetContract.PATH_PETS, PETS);

        // adds the Uri that will deal with a single row of the table of pets
        sUriMatcher.addURI(PetContract.CONTENT_AUTHORITY, PetContract.PATH_PETS + "/#", PET_ID);
    }

    /**
     * Initialize the provider and the database helper object.
     */
    @Override
    public boolean onCreate() {
        mDbHelper = new PetDbHelper(getContext());
        return true;
    }

    /**
     * Perform the query for the given URI. Use the given projection, selection, selection arguments, and sort order.
     */
    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection, @Nullable String[] selectionArgs, @Nullable String sortOrder) {
        // gets a readable copy of the database
        SQLiteDatabase database = mDbHelper.getReadableDatabase();

        // the cursor object that will be returned with the results.
        Cursor cursor;

        // gets the response code of the uri we received.
        int match = sUriMatcher.match(uri);

        // switch case to find out which uri we are dealing with.
        switch (match) {
            // case which will occur if the uri want to deal with the whole pets table.
            case PETS:
                cursor = database.query(PetEntry.TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder);
                break;

            // case which will occur if the uri want to deal with a single row of the pets table.
            case PET_ID:
                selection = PetEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                cursor = database.query(PetEntry.TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder);
                break;

            // default which case will occur if the response code doesn't match any of the available Uris.
            default:
                throw new IllegalArgumentException("Cannot query unknown Uri " + uri);
        }

        cursor.setNotificationUri(getContext().getContentResolver(), uri);

        // returns the cursor represents the results of the query.
        return cursor;
    }

    /**
     * Returns the MIME type of data for the content URI.
     */
    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {

        int match = sUriMatcher.match(uri);

        switch (match){
            case PETS:
                return PetEntry.CONTENT_LIST_TYPE;

            case PET_ID:
                return PetEntry.CONTENT_ITEM_TYPE;

            default:
                throw new IllegalArgumentException("Unknown Uri " + uri);
        }
    }

    /**
     * Insert new data into the provider with the given ContentValues.
     */
    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues values) {
        assert values != null;
        if (getValidData(values) == null) {
            return null;
        }

        values = getValidData(values);

        int match = sUriMatcher.match(uri);
        switch (match) {
            case PETS:
                return insertPet(uri, values);
            default:
                throw new IllegalArgumentException("Insertion is not supported for " + uri);
        }
    }

    /**
     * Insert a pet into the database with the given content values. Return the new content URI
     * for that specific row in the database.
     */
    private Uri insertPet(Uri uri, ContentValues values) {

        // gets a writable copy of the database.
        SQLiteDatabase database = mDbHelper.getWritableDatabase();

        // inserts the values into the database and returns its id.
        long id = database.insert(PetEntry.TABLE_NAME, null, values);

        if (id == -1) {
            Log.e(LOG_TAG, "Failed to insert a new row for uri " + uri);
            return null;
        }

        getContext().getContentResolver().notifyChange(uri, null);

        // Once we know the ID of the new row in the table,
        // return the new URI with the ID appended to the end of it
        return ContentUris.withAppendedId(uri, id);
    }

    /**
     * Delete the data at the given selection and selection arguments.
     */
    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {

        // gets a writable copy of the database.
        SQLiteDatabase database = mDbHelper.getWritableDatabase();

        int match = sUriMatcher.match(uri);

        switch (match) {
            case PETS:
                int delete = database.delete(PetEntry.TABLE_NAME, selection, selectionArgs);
                getContext().getContentResolver().notifyChange(uri, null);
                return delete;

            case PET_ID:
                selection = PetEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                int delete1 = database.delete(PetEntry.TABLE_NAME, selection, selectionArgs);
                getContext().getContentResolver().notifyChange(uri, null);
                return delete1;

            default:
                throw new IllegalArgumentException("Cannot delete unknown Uri " + uri);
        }
    }

    /**
     * Updates the data at the given selection and selection arguments, with the new ContentValues.
     */
    @Override
    public int update(Uri uri, ContentValues contentValues, String selection,
                      String[] selectionArgs) {
        final int match = sUriMatcher.match(uri);
        contentValues = getValidData(contentValues);
        if(contentValues == null){
            return PetEntry.NOT_VALID_DATA;
        }
        switch (match) {
            case PETS:
                return updatePet(uri, contentValues, selection, selectionArgs);
            case PET_ID:
                // For the PET_ID code, extract out the ID from the URI,
                // so we know which row to update. Selection will be "_id=?" and selection
                // arguments will be a String array containing the actual ID.
                selection = PetEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                return updatePet(uri, contentValues, selection, selectionArgs);
            default:
                throw new IllegalArgumentException("Update is not supported for " + uri);
        }
    }

    /**
     * Update pets in the database with the given content values. Apply the changes to the rows
     * specified in the selection and selection arguments (which could be 0 or 1 or more pets).
     * Return the number of rows that were successfully updated.
     */
    private int updatePet(Uri uri, ContentValues values, String selection, String[] selectionArgs) {

        // No need to check the breed, any value is valid (including null).

        // If there are no values to update, then don't try to update the database
        if (values.size() == 0) {
            return 0;
        }

        int match = sUriMatcher.match(uri);

        // Otherwise, get writeable database to update the data
        SQLiteDatabase database = mDbHelper.getWritableDatabase();

        switch (match){
            case PET_ID:
                // Returns the number of database rows affected by the update statement
                int update = database.update(PetEntry.TABLE_NAME, values, selection, selectionArgs);
                getContext().getContentResolver().notifyChange(uri, null);
                return update;
            default:
                throw new IllegalArgumentException("Cannot update pet with uri " + uri);
        }
    }

    private ContentValues getValidData(ContentValues values){
        switch (ValidationCode(values)){
            case PetEntry.NOT_VALID_GENDER:
                values.put(PetEntry.COLUMN_PET_GENDER, PetEntry.GENDER_UNKNOWN);
                return values;
            case PetEntry.NOT_VALID_WEIGHT:
                values.put(PetEntry.COLUMN_PET_WEIGHT, 0);
                return values;
            case PetEntry.NOT_VALID_BREED:
                values.put(PetEntry.COLUMN_PET_BREED, "Unknown");
                return values;
            case PetEntry.VALID_DATA:
                return values;
            default:
                return null;
        }
    }

    private int ValidationCode(ContentValues values) {
        String name;
        Integer gender;
        Integer weight;
        String breed;

        if (values.containsKey(PetEntry.COLUMN_PET_NAME)) {
            name = values.getAsString(PetEntry.COLUMN_PET_NAME).trim();
            if (TextUtils.isEmpty(name)) {
                return PetEntry.NOT_VALID_NAME;
            }
        }

        if (values.containsKey(PetEntry.COLUMN_PET_GENDER)) {
            gender = values.getAsInteger(PetEntry.COLUMN_PET_GENDER);
            if (gender == null || !PetEntry.isValidGender(gender)) {
                return PetEntry.NOT_VALID_GENDER;
            }
        }

        if (values.containsKey(PetEntry.COLUMN_PET_WEIGHT)) {
            weight = values.getAsInteger(PetEntry.COLUMN_PET_WEIGHT);
            if (weight != null && weight < 0) {
                return PetEntry.NOT_VALID_WEIGHT;
            }
        }

        if(values.containsKey(PetEntry.COLUMN_PET_BREED)){
            breed = values.getAsString(PetEntry.COLUMN_PET_BREED).trim();
            if(TextUtils.isEmpty(breed)){
                return PetEntry.NOT_VALID_BREED;
            }
        }

        return PetEntry.VALID_DATA;
    }
}

