package com.tenmiles.notebook.AddNotes;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.tenmiles.notebook.LandingPage.LandingActivity;
import com.tenmiles.notebook.R;
import com.tenmiles.notebook.Utils.Constants;

public class AddNotesActivity extends AppCompatActivity {

    private Toolbar _toolBar;
    private EditText titleEditText, notesEditText;
    private Button saveButton;
    private ProgressDialog ringProgressDialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_notes);

        //set the view elements
        setUpUserInterface();

        //catch the calue thrown from landing page (title and notes)
        Intent intent = getIntent();
        if (intent.getExtras() != null) {
            String title = intent.getExtras().getString(Constants.INTENT_TITLE);
            String notes = intent.getExtras().getString(Constants.INTENT_NOTES);
            //set notes and title
            titleEditText.setText(title);
            notesEditText.setText(notes);
            //disable edit mode
            titleEditText.setKeyListener(null);
            notesEditText.setKeyListener(null);
            //hide save button to avoid saving duplicate items in the drive
            saveButton.setVisibility(View.GONE);
        }
    }

    private void setUpUserInterface()
    {
        //setup toolbar (material design)
        _toolBar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(_toolBar);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        titleEditText = (EditText) findViewById(R.id.titleEditText);
        notesEditText = (EditText) findViewById(R.id.notesEditText);
        saveButton = (Button) findViewById(R.id.saveButton);
    }

    //upload response handler
    private final Handler handler = new Handler() {
        public void handleMessage(Message message) {
            boolean result = message.getData().getBoolean("data");
            if(result) {
                ringProgressDialog.dismiss();
                finish();
            }
        }
    };

    //handler for save button
    public void onSaveButtonClicked(View v) {

        //Upload file in dropbox
        UploadFile uploadFile = new UploadFile(this, LandingActivity.dropboxApi,handler,
                Constants.DROPBOX_FILE_DIR + titleEditText.getText(), notesEditText.getText().toString());

        uploadFile.execute();

        //show loading animation until it uploads.
        showLoadingAnimation();
    }

    public void showLoadingAnimation() {
        ringProgressDialog = ProgressDialog.show(this, Constants.DIALOG_TITLE, Constants.DIALOG_MESSAGE, true);
        ringProgressDialog.setCancelable(false);
    }
}
