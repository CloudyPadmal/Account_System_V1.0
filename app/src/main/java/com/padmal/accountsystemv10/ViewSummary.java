package com.padmal.accountsystemv10;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

import java.util.ArrayList;

/******************************************** View Summary *****************************************
* Shows the sum of each element in category table and the list of categories available. Need to try
* and implement the tab view for this one and that will remain...
*
* This has three views
 * 1. Income
 * 2. Expense
 * 3. Categories
 *
* Accessing database activities are assigned to AsyncTask
***************************************************************************************************/

public class ViewSummary extends AppCompatActivity {

    // Define variable types to views
    private ListView viewsummaryList;
    private DBHelper dbhelp;
    private ArrayList<String> Income_List, Expense_List, Category_List;

    /************************************* ON CREATE **********************************************/
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        // Get the saved state
        super.onCreate(savedInstanceState);
        // Set view
        setContentView(R.layout.activity_view_summary);

        /********************************** INITIATE VIEWS ****************************************/

        // Initiate database handler
        dbhelp = new DBHelper(this);
        // Initiate tab-like buttons
        Button incomelistb = (Button) findViewById(R.id.btnincomelist);
        Button expenselistb = (Button) findViewById(R.id.btnexpenselist);
        Button categorylistb = (Button) findViewById(R.id.btncategorylist);
        // Initiate the list view
        viewsummaryList = (ListView) findViewById(R.id.viewsummarylist);

        // Generate and keep lists of records
        new setAdapters().execute();

        // Generate and display Income Records ...
        incomelistb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // Changes the list view to Income_List
                viewsummaryList.setAdapter(new ArrayAdapter<>(getApplicationContext(), android.R.layout.simple_list_item_1, Income_List));
            }
        });

        // Generate and display Expense Records ...
        expenselistb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // Changes the list view to Expense_List
                viewsummaryList.setAdapter(new ArrayAdapter<>(getApplicationContext(), android.R.layout.simple_list_item_1, Expense_List));
            }
        });

        // Generate and display Categories ...
        categorylistb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // Changes the list view to Category_List
                viewsummaryList.setAdapter(new ArrayAdapter<>(getApplicationContext(), android.R.layout.simple_list_item_1, Category_List));
            }
        });
    }

    /************************************ OPTIONS MENU ********************************************/
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_view_summary, menu);
        return true;
    }

    /************************ ASYNC TASK to get totals from database ******************************/
    private class setAdapters extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground (Void... params) {

            Income_List = dbhelp.getAllSummary_Array("Income");
            Expense_List = dbhelp.getAllSummary_Array("Expense");
            Category_List = dbhelp.getAllSummary_Array("Category");

            return null;
        }
    }

    /**********************************************************************************************/
}
