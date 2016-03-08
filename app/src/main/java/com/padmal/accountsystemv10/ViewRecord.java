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
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.Toast;
import android.widget.ToggleButton;

import java.util.ArrayList;

public class ViewRecord extends AppCompatActivity {

    // Define variable types of views
    private DBHelper dbhelp;
    private ListView viewrecordlistView;
    private ToggleButton viewrecordtoggleType;

    /**************************************** ON CREATE *******************************************/
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_record);

        // Initiate a database object
        dbhelp = new DBHelper(this);

        // Initiate buttons for views
        viewrecordtoggleType = (ToggleButton) findViewById(R.id.deleterecordtoggletype);

        // Initiate the view
        viewrecordlistView = (ListView) findViewById(R.id.deleterecordlistview);

        // Initially view a list of records in the list view
        new setRecordList().execute(viewrecordtoggleType.isChecked());

        // On change of state of toggle button, change the list
        viewrecordtoggleType.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                new setRecordList().execute(isChecked);
            }
        });

        // Register list view for a context menu
        registerForContextMenu(viewrecordlistView);
    }

    /************************************ Options Menu Stuff **************************************/
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_view_record, menu);
        return true;
    }

    /************************************ Context Menu Stuff **************************************/
    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.longclickmenu_update_or_delete, menu);
    }
    @Override
    public boolean onContextItemSelected(MenuItem item) {

        // Get adapter view's info
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        // Generate a string array of gettable items
        final String string_of_record[] = viewrecordlistView.getItemAtPosition(info.position).toString().split("\\s+");
        // Get the respective table name
        final String Table_name_to_get = viewrecordtoggleType.isChecked() ? "Income" : "Expense";

        switch (item.getItemId()) {

            // Deleting an item
            case R.id.longmenudelete:

                // Build and create an alert dialog with YES NO options
                new AlertDialog.Builder(ViewRecord.this)
                        .setTitle("Delete Entry")
                        .setMessage("Are you sure you want to delete this entry?")
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {

                                // Perform deletion ...
                                if (dbhelp.deleteRecord(Table_name_to_get, string_of_record[0], string_of_record[1], string_of_record[2].substring(1, string_of_record[2].length() - 1)) == 1) {

                                    // Toast action to show success
                                    Toast.makeText(ViewRecord.this, "Entry Deleted", Toast.LENGTH_LONG).show();
                                    new setRecordList().execute(viewrecordtoggleType.isChecked());
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

            case R.id.longmenuupdate:

                // Create an intent to update entries from new record entry
                Intent update_record_from = new Intent(ViewRecord.this, NewRecordEntry.class);

                // Put valued parameters into the intent
                update_record_from.putExtra("From", "Update Records");
                update_record_from.putExtra("Table", Table_name_to_get);
                update_record_from.putExtra("Date", string_of_record[0]);
                update_record_from.putExtra("Category", string_of_record[1]);
                update_record_from.putExtra("Amount", string_of_record[2].substring(1, string_of_record[2].length() - 1));

                // Start the activity
                finish();
                startActivity(update_record_from);

                return true;

            default: return super.onContextItemSelected(item);
        }
    }

    /********************************* AsyncTask to set the list **********************************/
    private class setRecordList extends AsyncTask<Boolean, Void, ArrayAdapter<String>> {

        @Override
        protected ArrayAdapter<String> doInBackground(Boolean... Table) {

            // Get an array list of records in this type
            ArrayList<String> Adapter = dbhelp.getAllRecords_Array(Table[0] ? "Income" : "Expense");
            return new ArrayAdapter<String>(ViewRecord.this, android.R.layout.simple_list_item_1, Adapter);
        }

        @Override
        protected void onPostExecute(ArrayAdapter<String> Adapter) {

            // Make an adapter and set it to the list
            viewrecordlistView.setAdapter(Adapter);
        }
    }

    /**********************************************************************************************/
}
