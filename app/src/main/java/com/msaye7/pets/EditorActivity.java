package com.msaye7.pets;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NavUtils;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.CursorLoader;
import androidx.loader.content.Loader;

import com.msaye7.pets.data.PetContract.PetEntry;

import java.util.Objects;

/**
 * Allows user to create a new pet or edit an existing one.
 */
public class EditorActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    /** EditText field to enter the pet's name */
    private EditText mNameEditText;

    /** EditText field to enter the pet's breed */
    private EditText mBreedEditText;

    /** EditText field to enter the pet's weight */
    private EditText mWeightEditText;

    /** EditText field to enter the pet's gender */
    private Spinner mGenderSpinner;

    boolean isDeleteVisible;

    private boolean mPetHasChanged = false;

    Uri editablePetURI;

    private static final int LOADER_ID = 0;

    // LOG_TAG of the class.
    private static final String LOG_TAG = EditorActivity.class.getSimpleName();

    private final View.OnTouchListener mOnTouchListener = new View.OnTouchListener() {
        @SuppressLint("ClickableViewAccessibility")
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            mPetHasChanged = true;
            return false;
        }
    };

    /**
     * Gender of the pet. The possible values are:
     * 0 for unknown gender, 1 for male, 2 for female.
     */
    private int mGender = 0;

    @SuppressLint({"Recycle", "ClickableViewAccessibility"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);

        editablePetURI = getIntent().getData();

        if(editablePetURI != null){
            Objects.requireNonNull(getSupportActionBar()).setTitle(R.string.edit_pet_activity_title);
            isDeleteVisible = true;
            getSupportLoaderManager().initLoader(LOADER_ID, null, this);
        }else{
            isDeleteVisible = false;
        }

        // Find all relevant views that we will need to read user input from
        mNameEditText = (EditText) findViewById(R.id.edit_pet_name);
        mBreedEditText = (EditText) findViewById(R.id.edit_pet_breed);
        mWeightEditText = (EditText) findViewById(R.id.edit_pet_weight);
        mGenderSpinner = (Spinner) findViewById(R.id.spinner_gender);

        mNameEditText.setOnTouchListener(mOnTouchListener);
        mBreedEditText.setOnTouchListener(mOnTouchListener);
        mWeightEditText.setOnTouchListener(mOnTouchListener);
        mGenderSpinner.setOnTouchListener(mOnTouchListener);

        setupSpinner();
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        menu.findItem(R.id.action_delete).setVisible(isDeleteVisible);
        return true;
    }

    private void showUnSavedChangesDialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.discard_and_quit);
        builder.setNegativeButton(R.string.discard, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                NavUtils.navigateUpFromSameTask(EditorActivity.this);
            }
        });
        builder.setPositiveButton(R.string.keep_editing, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if(dialog != null){
                    dialog.dismiss();
                }
            }
        });
        AlertDialog show = builder.show();
    }

    /**
     * Setup the dropdown spinner that allows the user to select the gender of the pet.
     */
    private void setupSpinner() {
        // Create adapter for spinner. The list options are from the String array it will use
        // the spinner will use the default layout
        ArrayAdapter genderSpinnerAdapter = ArrayAdapter.createFromResource(this,
                R.array.array_gender_options, android.R.layout.simple_spinner_item);

        // Specify dropdown layout style - simple list view with 1 item per line
        genderSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);

        // Apply the adapter to the spinner
        mGenderSpinner.setAdapter(genderSpinnerAdapter);

        // Set the integer mSelected to the constant values
        mGenderSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selection = (String) parent.getItemAtPosition(position);
                if (!TextUtils.isEmpty(selection)) {
                    if (selection.equals(getString(R.string.gender_male))) {
                        mGender = PetEntry.GENDER_MALE; // Male
                    } else if (selection.equals(getString(R.string.gender_female))) {
                        mGender = PetEntry.GENDER_FEMALE; // Female
                    } else {
                        mGender = PetEntry.GENDER_UNKNOWN; // Unknown
                    }
                }
            }

            // Because AdapterView is an abstract class, onNothingSelected must be defined
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                mGender = 0; // Unknown
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu options from the res/menu/menu_editor.xml file.
        // This adds menu items to the app bar.
        getMenuInflater().inflate(R.menu.menu_editor, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // User clicked on a menu option in the app bar overflow menu
        switch (item.getItemId()) {
            // Respond to a click on the "Save" menu option
            case R.id.action_save:
                String name = mNameEditText.getText().toString().trim();
                String breed = mBreedEditText.getText().toString().trim();
                String weight = mWeightEditText.getText().toString().trim();

                if(TextUtils.isEmpty(name) && TextUtils.isEmpty(breed) && TextUtils.isEmpty(weight)){
                    finish();
                    break;
                }

                ContentValues values = new ContentValues();
                values.put(PetEntry.COLUMN_PET_NAME, name);
                values.put(PetEntry.COLUMN_PET_BREED, breed);
                values.put(PetEntry.COLUMN_PET_GENDER, mGender);
                values.put(PetEntry.COLUMN_PET_WEIGHT, weight);

                if(editablePetURI == null){
                    Uri insert = getContentResolver().insert(PetEntry.CONTENT_URI, values);
                    if (insert == null)
                        Toast.makeText(this, getString(R.string.error_adding_pet), Toast.LENGTH_SHORT).show();
                    else {
                        Toast.makeText(this, getString(R.string.added_pet), Toast.LENGTH_SHORT).show();
                        finish();
                    }
                }else{
                    int update = getContentResolver().update(editablePetURI, values, null, null);
                    if (update != PetEntry.NOT_VALID_DATA) {
                        Toast.makeText(this, getString(R.string.update_success), Toast.LENGTH_SHORT).show();
                        finish();
                    }
                    else
                        Toast.makeText(this, getString(R.string.error_update), Toast.LENGTH_SHORT).show();

                }
                return true;
            // Respond to a click on the "Delete" menu option
            case R.id.action_delete:
                finish();
                getContentResolver().delete(editablePetURI, null);
                return true;
            // Respond to a click on the "Up" arrow button in the app bar
            case android.R.id.home:
                // Navigate back to parent activity (CatalogActivity)
                if(mPetHasChanged){
                    showUnSavedChangesDialog();
                }else{
                    NavUtils.navigateUpFromSameTask(this);
                }
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @NonNull
    @Override
    public Loader<Cursor> onCreateLoader(int id, @Nullable Bundle args) {
        String[] projection = new String[]{
                PetEntry._ID,
                PetEntry.COLUMN_PET_NAME,
                PetEntry.COLUMN_PET_BREED,
                PetEntry.COLUMN_PET_GENDER,
                PetEntry.COLUMN_PET_WEIGHT,
        };

        return new CursorLoader(
                this,
                editablePetURI,
                projection,
                null,
                null,
                null
        );
    }

    @Override
    public void onLoadFinished(@NonNull Loader<Cursor> loader, Cursor cursor) {
        if(cursor.moveToFirst()) {
            mNameEditText.setText(cursor.getString(cursor.getColumnIndex(PetEntry.COLUMN_PET_NAME)));
            mBreedEditText.setText(cursor.getString(cursor.getColumnIndex(PetEntry.COLUMN_PET_BREED)));
            mWeightEditText.setText(String.valueOf(cursor.getInt(cursor.getColumnIndex(PetEntry.COLUMN_PET_WEIGHT))));
            mGenderSpinner.setSelection(cursor.getInt(cursor.getColumnIndex(PetEntry.COLUMN_PET_GENDER)));
        }
    }

    @Override
    public void onLoaderReset(@NonNull Loader<Cursor> loader) {
        mNameEditText.setText("");
        mBreedEditText.setText("");
        mWeightEditText.setText("");
        mGenderSpinner.setSelection(PetEntry.GENDER_UNKNOWN);
    }
}