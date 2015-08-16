package com.tenmiles.notebook.LandingPage;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.dropbox.client2.DropboxAPI;
import com.dropbox.client2.android.AndroidAuthSession;
import com.dropbox.client2.session.AccessTokenPair;
import com.dropbox.client2.session.AppKeyPair;
import com.getbase.floatingactionbutton.FloatingActionButton;
import com.tenmiles.notebook.AddNotes.AddNotesActivity;
import com.tenmiles.notebook.Model.Note;
import com.tenmiles.notebook.R;
import com.tenmiles.notebook.Utils.Constants;

import java.util.ArrayList;

public class LandingActivity extends AppCompatActivity
{
    public static DropboxAPI<AndroidAuthSession> dropboxApi;
    private boolean isUserLoggedIn;
    private Menu menu;
    private ListView filesListView;
    private ProgressDialog ringProgressDialog;
    private Toolbar _toolBar;
    private boolean addNewFlag = false;
    private TextView noResultTextView;
    private FloatingActionButton addNotesButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_landing);

        setUpUserInterface();

        loggedIn(false);

        setUpDropboxSession();


    }

    //Load UI controls
    private void setUpUserInterface()
    {
        _toolBar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(_toolBar);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        filesListView = (ListView) findViewById(R.id.filesListView);
        noResultTextView = (TextView) findViewById(R.id.noResultText);
        addNotesButton = (FloatingActionButton) findViewById(R.id.addNotesButton);
    }

    //setup dropbox login/logout session
    private void setUpDropboxSession()
    {
        AppKeyPair appKeyPair = new AppKeyPair(Constants.ACCESS_KEY, Constants.ACCESS_SECRET);

        AndroidAuthSession session;
        SharedPreferences prefs = getSharedPreferences(Constants.DROPBOX_NAME, 0);

        String key = prefs.getString(Constants.ACCESS_KEY, null);
        String secret = prefs.getString(Constants.ACCESS_SECRET, null);
        String accessToken = prefs.getString(Constants.ACCESS_TOKEN, null);

        if (key != null && secret != null) {
            AccessTokenPair token = new AccessTokenPair(key, secret);
            session = new AndroidAuthSession(appKeyPair,token);
        } else {
            session = new AndroidAuthSession(appKeyPair);
        }
        dropboxApi = new DropboxAPI<AndroidAuthSession>(session);
        if(accessToken != null)
        {
            dropboxApi.getSession().setOAuth2AccessToken(accessToken);
            loggedIn(true);
            listAllFiles();
        }
    }


    public void showLoadingAnimation() {
        ringProgressDialog = ProgressDialog.show(LandingActivity.this, Constants.DIALOG_TITLE, Constants.DIALOG_MESSAGE, true);
        ringProgressDialog.setCancelable(false);
    }

    @Override
    protected void onResume() {
        super.onResume();
        AndroidAuthSession session = dropboxApi.getSession();

        if (session.authenticationSuccessful()) {
            try {
                session.finishAuthentication();

                AppKeyPair appKeyPair = session.getAppKeyPair();
                String accessToken = session.getOAuth2AccessToken();

                SharedPreferences prefs = getSharedPreferences(Constants.DROPBOX_NAME, 0);
                Editor editor = prefs.edit();

                editor.putString(Constants.ACCESS_TOKEN, accessToken);

                editor.putString(Constants.ACCESS_KEY, appKeyPair.key);

                editor.putString(Constants.ACCESS_SECRET, appKeyPair.secret);

                editor.commit();
                loggedIn(true);


            } catch (IllegalStateException e) {
                Toast.makeText(this, Constants.DROPBOX_ERROR_MESSAGE,
                        Toast.LENGTH_SHORT).show();
            }
        }
        if(addNewFlag)
        {
            listAllFiles();
            addNewFlag = false;
        }

    }

    //Handler for list files request to dropbox
    private final Handler handler = new Handler() {
        public void handleMessage(Message message) {
            final ArrayList<Note> result = message.getData().getParcelableArrayList("data");
            if(result.size()==0)
            {
                noResultTextView.setVisibility(View.VISIBLE);
            }
            NotesAdapter adapter = new NotesAdapter(LandingActivity.this, result);
            filesListView.setAdapter(adapter);
            if(ringProgressDialog!=null) {
                ringProgressDialog.dismiss();
            }
            filesListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {

                    Intent intent = new Intent(LandingActivity.this, AddNotesActivity.class);
                    intent.putExtra(Constants.INTENT_TITLE,result.get(position).getTitle());
                    intent.putExtra(Constants.INTENT_NOTES,result.get(position).getNotes());
                    startActivity(intent);
                }
            });
        }
    };

    //handle add notes button
    public void onAddButtonClicked(View v)
    {
        Intent intent = new Intent(LandingActivity.this, AddNotesActivity.class);
        startActivity(intent);
        addNewFlag  = true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_landing, menu);
        this.menu = menu;
        loggedIn(isUserLoggedIn);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_signin)
        {
            if (isUserLoggedIn) {
                //Signout from dropbox
                dropboxApi.getSession().unlink();
                loggedIn(false);
            } else {
                //Signin to sropbox
                dropboxApi.getSession().startOAuth2Authentication(this);
            }
        }

        return super.onOptionsItemSelected(item);
    }

    //load adapter with list of files comes from dropbox
    private void listAllFiles()
    {
        ListFiles listFiles = new ListFiles(dropboxApi, Constants.DROPBOX_FILE_DIR, handler, this);
        listFiles.execute();
        showLoadingAnimation();
    }

    //handle view controlls using dropbox login session
    public void loggedIn(boolean userLoggedIn) {
        isUserLoggedIn = userLoggedIn;
        if(menu!=null) {
            MenuItem bedMenuItem = menu.findItem(R.id.action_signin);
            if (userLoggedIn) {
                bedMenuItem.setTitle(Constants.LOGOUT);
                addNotesButton.setVisibility(View.VISIBLE);
                noResultTextView.setVisibility(View.INVISIBLE);
            } else {
                bedMenuItem.setTitle(Constants.LOGIN);
                noResultTextView.setVisibility(View.VISIBLE);
                addNotesButton.setVisibility(View.INVISIBLE);
            }
        }
    }


}
