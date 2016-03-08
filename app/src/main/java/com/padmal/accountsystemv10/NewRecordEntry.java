package com.padmal.accountsystemv10;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;
import android.widget.ToggleButton;

import java.util.ArrayList;
import java.util.Calendar;

/**************************************** New Record Entry *****************************************
 * This is the view used to enter a new record to the database. This view can be accessed from many
 * other views
 *
 * 1. Main Activity Menu
 * 2. View Categories (Selecting a category will redirect user to this view again)
 * 3. Update Record
 *
 * Threads are used to update and insert operations.
 **************************************************************************************************/

public class NewRecordEntry extends AppCompatActivity {

    // Creating variable types ...
    private static Button newrecordentryDate;
    private Button newrecordentryButtonUpdate, newrecordentryDescription;

    private ToggleButton newrecordentryType;

    private DBHelper dbhelp;

    private Spinner newrecordentryCategorySpinner;

    private EditText newrecordentryAmount;

    private Bundle details_from_up;

    private String NRE_Date, NRE_Type, NRE_Category, NRE_Amount;

    private int position_of_category = 0;

    /*************************************** ON CREATE ********************************************/
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_record_entry);

        /********************************* Initiate Views *****************************************/

        // Create a new DBHelper object to access the database ...
        dbhelp = new DBHelper(this);

        // Initiate Date field
        newrecordentryDate = (Button) findViewById(R.id.newrecordentrydate);

        // Initiate Type toggle button
        newrecordentryType = (ToggleButton) findViewById(R.id.newrecordentrytype);

        // Initiate the category list to the spinner ...
        newrecordentryCategorySpinner = (Spinner) findViewById(R.id.newrecordentrycategory);

        // Initiate text view Description
        newrecordentryDescription = (Button) findViewById(R.id.newrecordentrydescription);
        newrecordentryDescription.setClickable(false);

        // Initiate amount field ...
        newrecordentryAmount = (EditText) findViewById(R.id.newrecordentryamount);

        // Initiate buttons UPDATE ...
        newrecordentryButtonUpdate = (Button) findViewById(R.id.newrecordentrybuttonupdate);

        // Gets a bundle of information from the previous activity
        details_from_up = getIntent().getExtras();

        // Get the selected category from category list and show it in the description view
        newrecordentryCategorySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            // When an item is selected from the spinner, respective description will be shown
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                String description_to_show = dbhelp.getCategoryDescription_String(newrecordentryCategorySpinner.getSelectedItem().toString());
                NewRecordEntry.this.newrecordentryDescription.setText(description_to_show);
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {/**/}
        });

        // If there are stuff coming from a previous view,
        if (details_from_up != null) {

            // If the view is created after browsing of categories, the selected item has to be selected
            if (details_from_up.getString("From").equalsIgnoreCase("View Categories")) {
                handleAfterBrowsing();
            }
            // I've sent "Update Records" as extra but no need to check if View Categories is checked
            else {handleUpdateRecords();}
        }
        // If no bundle came in, go alone with New entry ...
        else {handleNewRecords();}
    }

    /**************************************** New Records *****************************************/
    private void handleNewRecords () {

        new setSpinner().execute("Expense", "0");

        // Set the onCheckedChange listener to grab type changes and change category list accordingly
        newrecordentryType.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                if (isChecked) {
                    new setSpinner().execute("Income" , "0");
                } else {
                    new setSpinner().execute("Expense", "0");
                }
            }
        });

        newrecordentryButtonUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // Get input data from views
                getInputData();

                // Try to update entries
                if (dbhelp.insertNewEntry(NRE_Type, NRE_Date, NRE_Category, NRE_Amount)) {

                    uponSuccess("Entry Added Successfully");
                }
                // Fails if SQLite sends an error ...
                else {

                    Toast.makeText(NewRecordEntry.this, "Check again", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    /************************************** Updating Records **************************************/
    private void handleUpdateRecords () {

        // Setting onCheckedChange Lister to grab type changes and change the category list accordingly
        newrecordentryType.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                if (isChecked) {
                    new setSpinner().execute("Income" , "0");
                } else {
                    new setSpinner().execute("Expense", "0");
                }
            }
        });

        // Get details from view records
        final String Old_Date = details_from_up.getString("Date");
        final String Old_Table = details_from_up.getString("Table");
        final String Old_Amount = details_from_up.getString("Amount");
        final String Old_Category = details_from_up.getString("Category");

        // Change button name to update
        newrecordentryButtonUpdate.setText("Update");

        // Set Date to Old one
        newrecordentryDate.setText(Old_Date);

        // Set type to Old one
        newrecordentryType.setChecked(Old_Table.equalsIgnoreCase("Income"));
        newrecordentryType.setClickable(false);

        // Set Amount to Old one
        newrecordentryAmount.setText(Old_Amount);

        // Set the spinner
        new setSpinner().execute(Old_Table, Old_Category);

        newrecordentryButtonUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // Get input from user
                getInputData();

                // Try updating that entry
                if (dbhelp.updateRecord(NRE_Type, Old_Date, Old_Category, NRE_Date, NRE_Category, NRE_Amount) > 0) {

                    uponSuccess("Updated Successfully");
                }
                // Fails if SQLite sends an error ...
                else {

                    Toast.makeText(NewRecordEntry.this, "Update Failed", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    /************************************** Browse Categories *************************************/
    private void handleAfterBrowsing () {

        // Setting onCheckedChange Lister to grab type changes and change the category list accordingly
        newrecordentryType.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                if (isChecked) {
                    new setSpinner().execute("Income", "0");
                } else {
                    new setSpinner().execute("Expense", "0");
                }
            }
        });

        // Get the selected category
        final String category_browsed = details_from_up.getString("Category");
        final String type_browsed = details_from_up.getString("Type");
        final String date_came_in = details_from_up.getString("Date");
        final String amount_came_in = details_from_up.getString("Amount");

        // Set date and amount if there weren't any before
        if (date_came_in != null) {
            newrecordentryDate.setText(date_came_in);
        }
        if (amount_came_in != null) {
            newrecordentryAmount.setText(amount_came_in);
        }

        // Type of categories must be the browsed type categories
        newrecordentryType.setChecked(type_browsed.equalsIgnoreCase("Income"));

        // Set the spinner and browsed item as selected
        new setSpinner().execute(type_browsed, category_browsed);

        // Once the button UPDATE is clicked .......
        newrecordentryButtonUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // Get input from views
                getInputData();

                // Insert those values to the respective database table ...
                if (dbhelp.insertNewEntry(NRE_Type, NRE_Date, NRE_Category, NRE_Amount)) {

                    uponSuccess("Entry Added Successfully");
                }
                // Fails if SQLite sends an error ...
                else {

                    Toast.makeText(NewRecordEntry.this, "Check again", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    /*********************************** Get Input from User **************************************/
    private void getInputData () {

        // Get date,
        NewRecordEntry.this.NRE_Date = newrecordentryDate.getText().toString();
        // Get type
        NewRecordEntry.this.NRE_Type = newrecordentryType.isChecked() ? "Income" : "Expense";
        // Get category
        NewRecordEntry.this.NRE_Category = newrecordentryCategorySpinner.getSelectedItem().toString();
        // Get amount
        NewRecordEntry.this.NRE_Amount = newrecordentryAmount.getText().toString();
    }

    /************************************ Clean Up Process ****************************************/
    private void uponSuccess (String Msg) {

        // Closes the DB handler
        NewRecordEntry.this.dbhelp.close();

        // Display successful message
        Toast.makeText(getApplicationContext(), Msg, Toast.LENGTH_SHORT).show();

        // Go back to main screen
        Intent go_to_main_page = new Intent(NewRecordEntry.this, MainActivity.class);
        NewRecordEntry.this.finish();
        startActivity(go_to_main_page);
    }

    /************************************* Options Menu Stuff *************************************/
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_new_record_entry, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);

        switch (item.getItemId()) {

            // View category summary. This will view a list of categories and create a path back.
            case R.id.new_record_entry_view_cat_summary:

                // Go for browse categories
                Intent view_cat_summary = new Intent(NewRecordEntry.this, ViewCategories.class);

                // Send that to the next view
                view_cat_summary.putExtra("Type", newrecordentryType.isChecked() ? "Income" : "Expense");
                view_cat_summary.putExtra("From", "New Record");
                view_cat_summary.putExtra("Date", newrecordentryDate.getText().toString());
                view_cat_summary.putExtra("Amount", newrecordentryAmount.getText().toString());

                // Start activity
                NewRecordEntry.this.finish();
                startActivity(view_cat_summary);
                return true;

            case R.id.new_record_entry_new_cat:

                // New category entry view
                Intent new_category = new Intent(NewRecordEntry.this, NewCategoryEntry.class);

                // Start activity
                NewRecordEntry.this.finish();
                startActivity(new_category);
                return true;

            default: return true;
        }
    }

    /********************************* Clear Button Click Handler *********************************/
    public void clearButtonClicked(View view) {

        newrecordentryAmount.setText("");
    }

    /*********************************** Date Picker Fragment *************************************/
    public static class DatePickerFragment extends DialogFragment
            implements DatePickerDialog.OnDateSetListener {

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            // Use the current date as the default date in the picker
            final Calendar c = Calendar.getInstance();
            int year = c.get(Calendar.YEAR);
            int month = c.get(Calendar.MONTH);
            int day = c.get(Calendar.DAY_OF_MONTH);

            // Create a new instance of DatePickerDialog and return it
            return new DatePickerDialog(getActivity(), this, year, month, day);
        }

        public void onDateSet(DatePicker view, int year, int month, int day) {
            String[] month_list = {"Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"};
            newrecordentryDate.setText(Integer.toString(day) + "-" + month_list[month] + "-" + Integer.toString(year).substring(2));
        }
    }
    public void showDatePickerDialog(View view) {

        DialogFragment newFragment = new DatePickerFragment();
        newFragment.show(getFragmentManager(), "DatePickerFrag");
    }

    /***************************************** AsyncTask ******************************************/
    private class setSpinner extends AsyncTask<String, Void, ArrayAdapter<String>> {

        @Override
        protected ArrayAdapter<String> doInBackground(String... Table_details) {

            // Makes an array list and calculate the position of the old category if there's any
            ArrayList<String> List_to_set = dbhelp.getThisTypeOfCategoryList_Array(Table_details[0]);
            position_of_category = List_to_set.indexOf(Table_details[1]);

            return new ArrayAdapter<>(NewRecordEntry.this, android.R.layout.simple_list_item_1, List_to_set);
        }

        @Override
        protected void onPostExecute(ArrayAdapter<String> category_list) {

            newrecordentryCategorySpinner.setAdapter(category_list);
            newrecordentryCategorySpinner.setSelection(position_of_category);
        }
    }

    /**********************************************************************************************/
}