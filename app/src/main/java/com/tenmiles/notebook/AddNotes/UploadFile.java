package com.tenmiles.notebook.AddNotes;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.widget.Toast;

import com.dropbox.client2.DropboxAPI;
import com.dropbox.client2.exception.DropboxException;
import com.tenmiles.notebook.Utils.Constants;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Created by vinothkathiravan on 15/08/15.
 */
public class UploadFile extends AsyncTask
{
    private DropboxAPI dropboxApi;
    private String path;
    private String notes;
    private Context context;
    private Handler handler;

    public UploadFile(Context context, DropboxAPI dropboxApi,Handler handler, String path, String notes) {
        this.context = context.getApplicationContext();
        this.dropboxApi = dropboxApi;
        this.path = path;
        this.notes = notes;
        this.handler = handler;
    }


    @Override
    protected Object doInBackground(Object[] objects) {


        File tempDropboxDirectory = context.getCacheDir();
        //temproary file in cache to hold the txt file util it uploads.
        File tempFileToUploadToDropbox;
        FileWriter fileWriter = null;
        try {
            // Creating a temporal file.
            tempFileToUploadToDropbox = File.createTempFile("file", ".txt", tempDropboxDirectory);
            fileWriter = new FileWriter(tempFileToUploadToDropbox);
            fileWriter.write(notes);
            fileWriter.close();
            // Uploading the newly created file to Dropbox.
            FileInputStream fileInputStream = new FileInputStream(tempFileToUploadToDropbox);
            dropboxApi.putFile(path, fileInputStream, tempFileToUploadToDropbox.length(), null, null);
            tempFileToUploadToDropbox.delete();
            return true;
        } catch (IOException e) {
            e.printStackTrace();
        } catch (DropboxException e) {
            e.printStackTrace();

        }
        return false;
    }

    @Override
    protected void onPostExecute(Object o) {

        boolean result = (boolean) o;
        if (result) {
            Toast.makeText(context, Constants.UPLOAD_SUCCESS_MESSAGE, Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(context, Constants.UPLOAD_ERROR_MESSAGE, Toast.LENGTH_LONG).show();
        }
        //inform view that the data is uploaded
        Message message = handler.obtainMessage();
        Bundle bundle = new Bundle();
        bundle.putBoolean("data", result);
        message.setData(bundle);
        handler.sendMessage(message);
    }


}
