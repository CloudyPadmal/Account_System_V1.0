package com.padmal.accountsystemv10;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import android.widget.ToggleButton;

/************************************ New Category Entry ******************************************
* This view is used to insert a new category entry or update an existing category. Two paths can
* access this view
*
 * 1. Main Activity
 * 2. Update Category (Menu)
*
* AsyncTask is used to perform Database operations and finally gets back to main activity
***************************************************************************************************/

public class NewCategoryEntry extends AppCompatActivity {

    // Defining types
    private EditText newcategoryTextCategory, newcategoryTextDescription;
    private ToggleButton newcategoryType;
    private DBHelper dbhelp;
    private String NCEType, NCECategory, NCEDescription;
    private Bundle details_from_up;

    /*********************************** ON CREATE ************************************************/
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        // Implement the super method
        super.onCreate(savedInstanceState);
        // Set view
        setContentView(R.layout.activity_new_category_entry);

        /********************************** INITIATE VIEWS ****************************************/

        // Opens a database helper
        dbhelp = new DBHelper(this);

        // Initiating Type toggle button
        newcategoryType = (ToggleButton) findViewById(R.id.newcategorytype);
        // Initiating Category text view
        newcategoryTextCategory = (EditText) findViewById(R.id.newcategorytextcategory);
        // Initiating Description text view
        newcategoryTextDescription = (EditText) findViewById(R.id.newcategorytextdescription);
        // Initiating buttons UPDATE
        Button newcategoryButtonUpdate = (Button) findViewById(R.id.newcategorybuttonupdate);

        // Get Bundle from if this is called from a previous view
        details_from_up = getIntent().getExtras();

        // If bundle has extras, it is invoked by update an existing category action
        if (details_from_up != null) {

            final String Old_category = details_from_up.getString("Category");
            final String Old_description = details_from_up.getString("Description");

            newcategoryButtonUpdate.setText("Update");

            newcategoryTextCategory.setText(Old_category);
            newcategoryTextDescription.setText(Old_description);

            // Once the button UPDATE is clicked ....
            newcategoryButtonUpdate.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    getUserInput();
                    new handleDBActions().execute("Update");
                }
            });
        }
        /* Then this view is called by the main activity directly where user creates a new category */
        else {

            // Once the button UPDATE is clicked ....
            newcategoryButtonUpdate.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    getUserInput();
                    new handleDBActions().execute("New");
                }
            });
        }
    }

    /******************************* Get User input from User *************************************/
    private void getUserInput() {

        // Get Type
        NCEType = newcategoryType.isChecked() ? "Income" : "Expense";
        // Get Category
        NCECategory = newcategoryTextCategory.getText().toString();
        // Get Description
        NCEDescription = newcategoryTextDescription.getText().toString();
    }

    /********************************* Options Menu Stuff *****************************************/
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_new_category_entry, menu);
        return true;
    }

    /******************************** Clear Button Click Handler **********************************/
    public void clearButtonClicked(View view) {

        newcategoryTextCategory.setText("");
        newcategoryTextDescription.setText("");
        newcategoryTextCategory.setHint("");
        newcategoryTextDescription.setHint("");
    }

    /************************ Handle Database actions with AsyncTask ******************************/
    private class handleDBActions extends AsyncTask<String, Void, Integer> {

        @Override
        protected Integer doInBackground(String... Entries) {

            String ASType = NCEType;
            String ASCategory = NCECategory;
            String ASDescription = NCEDescription;

            if (Entries[0].equalsIgnoreCase("New")){

                // Insert this category into the table and return result received
                return dbhelp.insertNewCategory(ASType, ASCategory, ASDescription) ? 1 : 0;
            } else {
                String OldCategory = details_from_up.getString("Category");
                // Else this is Updating, Update this and return result
                return dbhelp.updateCategory(OldCategory, NCEType, NCECategory, NCEDescription) > 0 ? 2 : 0;
            }
        }

        @Override
        protected void onPostExecute(Integer result) {

            if (result == 1){

                dbhelp.close();
                Toast.makeText(NewCategoryEntry.this, "Added Successfully", Toast.LENGTH_SHORT).show();

                // Go back to main page
                Intent intent = new Intent(NewCategoryEntry.this, MainActivity.class);
                NewCategoryEntry.this.finish();
                startActivity(intent);

            } else if (result == 2) {

                dbhelp.close();
                Toast.makeText(NewCategoryEntry.this, "Updated Successfully", Toast.LENGTH_SHORT).show();

                // Go back to main page
                Intent intent = new Intent(NewCategoryEntry.this, MainActivity.class);
                NewCategoryEntry.this.finish();
                startActivity(intent);
            } else {

                Toast.makeText(NewCategoryEntry.this, "Operation Failed", Toast.LENGTH_SHORT).show();
            }
        }
    }

    /**********************************************************************************************/
}