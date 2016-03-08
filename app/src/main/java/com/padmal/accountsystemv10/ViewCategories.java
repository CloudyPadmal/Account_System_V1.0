package com.padmal.accountsystemv10;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;

/************************************ View Categories **********************************************
* This shows a list view of existing categories with their descriptions. On long press of an entry
* enables user to perform Delete that entry or Update the values of that entry.
 *
 * 1. Upon Delete selected user will be prompt to confirm action.
 * 2. Upon Update selected user will be directed to New Category view with details of that entry
 *
* Once user deleted an entry, the list should be updated i.e. deleted item must be gone.
***************************************************************************************************/

public class ViewCategories extends AppCompatActivity {

    // Defining variables
    private ListView viewcategorysummaryscrollview;
    private DBHelper dbhelp;
    private Bundle details_brought_in;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        // Get saved state
        super.onCreate(savedInstanceState);
        // Set view
        setContentView(R.layout.activity_view_categories);

        // Assigning variables to objects
        viewcategorysummaryscrollview = (ListView) findViewById(R.id.viewcatsummary);

        // Create a database object
        dbhelp = new DBHelper(this);

        // Registering for the context menu
        registerForContextMenu(viewcategorysummaryscrollview);

        // Get a bundle of information from previous view
        details_brought_in = getIntent().getExtras();

        // View categories carries in information from New Recory Entry, else it is from a normal view
        if (details_brought_in != null) {

            // "From" data will be sent only from New Record view
            if (details_brought_in.getString("From").equalsIgnoreCase("New Record")) {
                handleNewEntry();
            }
        }
        // Then this is invoked by main menu
        else {

            // Set the list of categories using AsyncTask
            new setCategoryList().execute("All");
        }
    }

    /*********************************Handle New Entry View Invokes *******************************/
    private void handleNewEntry() {

        // Get the type of categories wanted
        final String type_of_categories_wanted = details_brought_in.getString("Type");

        // Set the list accordingly
        new setCategoryList().execute(type_of_categories_wanted);

        /* If the previous view is from New Record, onItemSelected method will be called and a new
        intent action will be coded to go back to the new record view and I will change settings in
        New Record to set a special view to get details sent from here */

        viewcategorysummaryscrollview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                /* When an item is clicked, get the row and convert it to get the first word i.e.
                category name and send it to New Record view and set spinner there */
                String category_name = viewcategorysummaryscrollview.getItemAtPosition(position).toString().split("\\s+")[0];

                Intent back_to_new_entry = new Intent(getApplicationContext(), NewRecordEntry.class);

                back_to_new_entry.putExtra("From", "View Categories");
                back_to_new_entry.putExtra("Type", details_brought_in.getString("Type"));
                back_to_new_entry.putExtra("Date", details_brought_in.getString("Date"));
                back_to_new_entry.putExtra("Category", category_name);
                back_to_new_entry.putExtra("Amount", details_brought_in.getString("Amount"));

                ViewCategories.this.finish();
                startActivity(back_to_new_entry);
            }
        });
    }

    /************************************* Options Menu Stuff *************************************/
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_view_categories, menu);
        return true;
    }

    /************************************* Context Menu Stuff **************************************/
    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        MenuInflater inflater = getMenuInflater();

        // Set view to long click menu created by me
        inflater.inflate(R.menu.longclickmenu_update_or_delete, menu);
    }
    @Override
    public boolean onContextItemSelected(MenuItem item) {

        // Get adapter view's info
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        // Split the row into gettable items and set them in a string
        final String selected_row = (viewcategorysummaryscrollview.getItemAtPosition(info.position).toString().split("\\s+"))[0];

        switch (item.getItemId()) {

            // When Delete clicked ...
            case R.id.longmenudelete:

                // Build and create an alert view to get confirmation
                new AlertDialog.Builder(ViewCategories.this)
                        .setTitle("Delete Category")
                        .setMessage("Are you sure you want to delete this category?")
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {

                                // Perform deletion ...
                                if (dbhelp.deleteCategory(selected_row) > 0) {

                                    // Create a toast to show success
                                    Toast.makeText(getApplicationContext(), selected_row + " Deleted Successfully", Toast.LENGTH_LONG).show();

                                    // Calls AsyncTask to make the list again ...
                                    new setCategoryList().execute("All");
                                } else {
                                    Toast.makeText(getApplicationContext(), "Operation Failed", Toast.LENGTH_LONG).show();
                                }
                            }
                        })
                        .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                // Cancelled
                            }
                        })
                        .show();
                return true;

            // When Update clicked ...
            case R.id.longmenuupdate:

                // Create a new intent to go to the Update Category class
                Intent record_update = new Intent(getApplicationContext(), NewCategoryEntry.class);

                // Send Type, Category and Description with it
                record_update.putExtra("Category", selected_row);
                record_update.putExtra("Description", dbhelp.getCategoryDescription_String(selected_row));

                // Start update category
                startActivity(record_update);

                return true;

            default: return super.onContextItemSelected(item);
        }
    }

    /************************ ASYNC TASK to get totals from database ******************************/
    private class setCategoryList extends AsyncTask<String, Void, ArrayAdapter<String>> {

        @Override
        protected ArrayAdapter<String> doInBackground (String... Table_Name) {

            // This will be called in two places depending on how this view was started.
            // If it is from main menu, all categories must be shown ...
            if (Table_Name[0].equalsIgnoreCase("All")) {

                // Get the list of categories from database
                ArrayList<String> arrayList = dbhelp.getAllSummary_Array("Category");
                return new ArrayAdapter<String>(getApplicationContext(), android.R.layout.simple_list_item_1, arrayList);
            }
            // I invoke this view from New Record Entry sometimes. Then I need a specific type of
            // categories only unlike before.
            else {

                // Get the list of type_of categories from database
                ArrayList<String> arrayList = dbhelp.getThisTypeOfCategorySummary_Array(Table_Name[0]);
                return new ArrayAdapter<String>(getApplicationContext(), android.R.layout.simple_list_item_1, arrayList);
            }
        }

        @Override
        protected void onPostExecute (ArrayAdapter<String> Adapter) {

            // Assign the adapter to the list view
            viewcategorysummaryscrollview.setAdapter(Adapter);
        }
    }
    /**********************************************************************************************/
}
