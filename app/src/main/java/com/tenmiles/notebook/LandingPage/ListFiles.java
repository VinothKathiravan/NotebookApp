package com.tenmiles.notebook.LandingPage;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import com.dropbox.client2.DropboxAPI;
import com.dropbox.client2.DropboxAPI.Entry;
import com.dropbox.client2.exception.DropboxException;
import com.tenmiles.notebook.Model.Note;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;


/**
 * Created by vinothkathiravan on 15/08/15.
 */
public class ListFiles extends AsyncTask {
    private DropboxAPI dropboxApi;
    private String path;
    private Handler handler;
    private Context context;

    public ListFiles(DropboxAPI dropboxApi, String path, Handler handler, Context context) {
        this.dropboxApi = dropboxApi;
        this.path = path;
        this.handler = handler;
        this.context = context;
    }

    @Override
    protected void onPostExecute(Object o) {
        ArrayList<Note> result = (ArrayList<Note>) o;

        //populate the data to the handler in landing page view
        Message message = handler.obtainMessage();
        Bundle bundle = new Bundle();
        bundle.putParcelableArrayList("data", result);
        message.setData(bundle);
        handler.sendMessage(message);
    }

    @Override
    protected Object doInBackground(Object[] objects) {
        ArrayList<Note> files = new ArrayList<Note>();
        try {
            //Get all files associated to the path from dropbox
            Entry directory = dropboxApi.metadata(path, 1000, null, true, null);
            for (Entry entry : directory.contents) {
                //Upload every record in model
                Note note = new Note();
                note.setTitle(entry.fileName());
                SimpleDateFormat dateFormat = new SimpleDateFormat("EE, dd MMM yyyy hh:mm:ss z");
                SimpleDateFormat expectedFormat = new SimpleDateFormat("EE, hh:mm");
                Date editDate = dateFormat.parse(entry.modified);
                String finalDate = expectedFormat.format(editDate);
                note.setDateEdited(finalDate);
                note.setNotes(getNotes(entry.path));
                files.add(note);
            }
        } catch (DropboxException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return files;
    }

    //To read the text inside the file
    private String getNotes(String path) {
        String strNotes = "";
        try {
            File tempLocalDirectory = context.getCacheDir();
            File tempDownloadedFile = null;
            tempDownloadedFile = File.createTempFile("file", ".txt", tempLocalDirectory);
            FileOutputStream outputStream = new FileOutputStream(tempDownloadedFile);
            dropboxApi.getFile(path, null, outputStream, null);
            FileReader fileReader = new FileReader(tempDownloadedFile);
            BufferedReader bufferedReader = new BufferedReader(fileReader);
            StringBuffer stringBuffer = new StringBuffer();
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                stringBuffer.append(line);
                stringBuffer.append("\n");
            }
            fileReader.close();
            strNotes = stringBuffer.toString();
            tempDownloadedFile.delete();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (DropboxException e) {
            e.printStackTrace();
        }
        return strNotes;
    }
}
