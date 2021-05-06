package com.msaye7.pets.data;

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;

public final class PetContract {

    /** content authority of the content provider of the app. */
    public static final String CONTENT_AUTHORITY = "com.msaye7.pets";

    /** making the Uri of the content authority */
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    /** the path of the pets table */
    public static final String PATH_PETS = "pets";


    private PetContract(){ }

    public final static class PetEntry implements BaseColumns {
        /** identify the table name. */
        public static final String TABLE_NAME = PATH_PETS;

        /** identify the content uri of the pets table */
        public static final Uri CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI, PATH_PETS);

        /** identify the required columns of the table. */
        public static final String _ID = BaseColumns._ID;
        public static final String COLUMN_PET_NAME = "name";
        public static final String COLUMN_PET_BREED = "breed";
        public static final String COLUMN_PET_GENDER = "gender";
        public static final String COLUMN_PET_WEIGHT = "weight";

        /** identify gender constants. */
        public static final int GENDER_UNKNOWN = 0;
        public static final int GENDER_MALE = 1;
        public static final int GENDER_FEMALE = 2;

        /** identify the MIME type constants */
        public static final String CONTENT_LIST_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_PETS;
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_PETS;

        /** Data validation codes */
        public static final int VALID_DATA = 1000;
        public static final int NOT_VALID_DATA = 1010;
        public static final int NOT_VALID_NAME = 1020;
        public static final int NOT_VALID_BREED = 1030;
        public static final int NOT_VALID_GENDER = 1040;
        public static final int NOT_VALID_WEIGHT = 1050;


        public static boolean isValidGender(int gender){
            switch (gender){
                case GENDER_UNKNOWN:
                case GENDER_MALE:
                case GENDER_FEMALE:
                    return true;
                default:
                    return false;
            }
        }
    }
}
