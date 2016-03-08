package com.padmal.accountsystemv10;

import android.app.backup.BackupManager;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

/***************************************** Main Activity *******************************************
* Creates a welcome view to show total income and expense of the database
* records available when the user holds the button and clears the screen
* as he releases it. Main menu provides access to
*
 * 1. New Record Entry
 * 2. New Category Entry
 * 3. View Records
 * 4. View Categories
 * 5. View Summary
*
* ** Accessing database activity is handled through AsyncTask
***************************************************************************************************/

public class MainActivity extends AppCompatActivity {

    // Defining variable types of views
    private Button totalincomeview;
    private Button totalexpenseview;
    private DBHelper dbhelp;
    private int backButtonCount = 0;
    BackupManager bckup;

    /*************************************** ON CREATE ********************************************/
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        // Get the saved instance State
        super.onCreate(savedInstanceState);
        // Set view
        setContentView(R.layout.activity_main);

        /********************************** INITIATE VIEWS ****************************************/

        // Initiate HOLD button
        Button mainholdbutton = (Button) findViewById(R.id.mainholdbutton);
        // Initiate database handler
        dbhelp = new DBHelper(this);
        bckup = new BackupManager(this);

        // Initiate INCOME & EXPENSE Button views
        totalincomeview = (Button) findViewById(R.id.mainincomebutton);
        totalincomeview.setClickable(false);
        totalexpenseview = (Button) findViewById(R.id.mainexpensebutton);
        totalexpenseview.setClickable(false);

        // Set a long click listener to show totals and when released, on click happens and content gets blank
        mainholdbutton.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick (View v) {

                // Calls background process to get totals and set view values ...
                new setTotals().execute("Income", "Expense");
                return false;
            }
        });

        mainholdbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick (View v) {

                // Clears the fields
                totalincomeview.setText("");
                totalexpenseview.setText("");
            }
        });
    }

    /************************************ OPTIONS MENU ********************************************/
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);

        switch (item.getItemId()) {

            /**** New Record Entries ****/
            case R.id.New_Record_Entry_Menu:
                Intent new_record = new Intent(getApplicationContext(), NewRecordEntry.class);
                startActivity(new_record);
                return true;

            /**** New Category Entries ****/
            case R.id.New_Category_Entry_Menu:
                Intent new_category = new Intent(getApplicationContext(), NewCategoryEntry.class);
                startActivityForResult(new_category, 200);
                return true;

            /**** View Records ****/
            case R.id.View_Record_Menu:
                Intent view_record = new Intent(getApplicationContext(), ViewRecord.class);
                startActivity(view_record);
                return true;

            /**** View Categories ****/
            case R.id.View_Category_Menu:
                Intent view_category = new Intent(getApplicationContext(), ViewCategories.class);
                startActivity(view_category);
                return true;

            /**** View Summaries ****/
            case R.id.View_Summary_Menu:
                Intent view_summary = new Intent(getApplicationContext(), ViewSummary.class);
                startActivity(view_summary);
                return true;

            default: return super.onOptionsItemSelected(item);
        }
    }

    /************************ ASYNC TASK to get totals from database ******************************/
    private class setTotals extends AsyncTask<String, Integer, String> {

        // Initiate variable name types to be used inside this AsyncTask
        private String total_income, total_expense;

        @Override
        protected String doInBackground (String... Table_Names) {

            // Table_Names[0] is INCOME and Table_Names[1] will be EXPENSE
            try {
                // Get totals from the DB helper
                total_income = dbhelp.getCategoryTotal_String(Table_Names[0]);
                total_expense = dbhelp.getCategoryTotal_String(Table_Names[1]);
            }
            // If the app is started for the first time, there won't be any entries ...
            catch (NullPointerException e) {
                // Makes it all zeros so ...
                total_income = "0.00";
                total_expense = "0.00";
            }

            // Create and return (I will have to split this into two by spaces to set in view buttons)
            return total_income + " " + total_expense;
        }

        @Override
        protected void onPostExecute (String Totals) {

            // Get totals from above and divide them into two Strings
            String[] Income_Expense = Totals.split("\\s+");

            // Make button text income and expense
            totalincomeview.setText(Income_Expense[0]);
            totalexpenseview.setText(Income_Expense[1]);
            bckup.dataChanged();
            Log.d("Padmal", "Backup called");

        }
    }

    /******************************* Get out from here ********************************************/
    @Override
    public void onBackPressed () {

        if(backButtonCount >= 1)
        {
            Intent intent = new Intent(Intent.ACTION_MAIN);
            intent.addCategory(Intent.CATEGORY_HOME);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        }
        else
        {
            Toast.makeText(this, "Press Back again to close this", Toast.LENGTH_SHORT).show();
            backButtonCount++;
        }
    }

    /**********************************************************************************************/
}
