package com.padmal.accountsystemv10;


import android.app.backup.BackupAgentHelper;
import android.app.backup.BackupDataOutput;
import android.app.backup.FileBackupHelper;
import android.os.ParcelFileDescriptor;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;

public class BackupHelper extends BackupAgentHelper{

   static final String DATABASE_FILE_NAME = "Accounts.db";

   // Allocate a helper and add it to the backup agent
   @Override
   public void onCreate() {
      FileBackupHelper Filehelper = new FileBackupHelper(this, DATABASE_FILE_NAME);
      Log.d("Padmal", "Path of the file = " + getDatabasePath(DATABASE_FILE_NAME).getPath());
      addHelper("Database File", Filehelper);
   }

   @Override
   public File getFilesDir() {

      File db_path = getDatabasePath(DATABASE_FILE_NAME);
      return db_path.getParentFile();
   }

   @Override
   public void onBackup(ParcelFileDescriptor oldState, BackupDataOutput data, ParcelFileDescriptor newState) throws IOException {
      super.onBackup(oldState, data, newState);
      Log.d("Padmal", getFilesDir().toString());
      Log.d("Padmal", getDatabasePath(DATABASE_FILE_NAME).toString());
   }


}
