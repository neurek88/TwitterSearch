package com.example.nico.myapplication;

import java.util.ArrayList;
import java.util.Collections;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
// the two below will be needed to complete your work
//import android.content.DialogInterface;
//import android.widget.AdapterView.OnItemLongClickListener;

public class MainActivity extends ListActivity
{
    // name of SharedPreferences XML file that stores the saved searches
    private static final String SEARCHES = "searches";

    private EditText queryEditText; // EditText where user enters a query
    private EditText tagEditText; // EditText where user tags a query
    private SharedPreferences savedSearches; // user's favorite searches
    private ArrayList<String> tags; // list of tags for saved searches
    private ArrayAdapter<String> adapter; // binds tags to ListView

    // called when MainActivity is first created
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // get references to the EditTexts
        queryEditText = (EditText) findViewById(R.id.queryEditText);
        tagEditText = (EditText) findViewById(R.id.tagEditText);

        // get the SharedPreferences containing the user's saved searches
        savedSearches = getSharedPreferences(SEARCHES, MODE_PRIVATE);

        // store the saved tags in an ArrayList then sort them
        tags = new ArrayList<>(savedSearches.getAll().keySet());
        Collections.sort(tags, String.CASE_INSENSITIVE_ORDER);

        // create ArrayAdapter and use it to bind tags to the ListView
        adapter = new ArrayAdapter<>(this, R.layout.list_item, tags);
        setListAdapter(adapter);

        // register listener to save a new or edited search
        ImageButton saveButton =
                (ImageButton) findViewById(R.id.saveButton);
        saveButton.setOnClickListener(saveButtonListener);

        // register listener that searches Twitter when user touches a tag
        getListView().setOnItemClickListener(itemClickListener);

        // set listener that allows user to delete or edit a search
        //getListView().setOnItemLongClickListener(itemLongClickListener);
    } // end method onCreate

    // saveButtonListener saves a tag-query pair into SharedPreferences
    public OnClickListener saveButtonListener = new OnClickListener()
    {
        @Override
        public void onClick(View v)
        {
            // create tag if neither queryEditText nor tagEditText is empty
            if (queryEditText.getText().length() > 0 &&
                    tagEditText.getText().length() > 0)
            {
                addTaggedSearch(queryEditText.getText().toString(),
                        tagEditText.getText().toString());
                queryEditText.setText(""); // clear queryEditText
                tagEditText.setText(""); // clear tagEditText

                ((InputMethodManager) getSystemService(
                        Context.INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(
                        tagEditText.getWindowToken(), 0);
            }
            else // display message asking user to provide a query and a tag
            {
                // create a new AlertDialog Builder
                AlertDialog.Builder builder =
                        new AlertDialog.Builder(MainActivity.this);

                // set dialog's message to display
                builder.setMessage(R.string.missingMessage);

                // provide an OK button that simply dismisses the dialog
                builder.setPositiveButton(R.string.OK, null);

                // create AlertDialog from the AlertDialog.Builder
                AlertDialog errorDialog = builder.create();
                errorDialog.show(); // display the modal dialog
            }
        } // end method onClick
    }; // end OnClickListener anonymous inner class

    // add new search to the save file, then refresh all Buttons
    private void addTaggedSearch(String query, String tag)
    {
        // get a SharedPreferences.Editor to store new tag/query pair
        SharedPreferences.Editor preferencesEditor = savedSearches.edit();
        preferencesEditor.putString(tag, query); // store current search
        preferencesEditor.apply(); // store the updated preferences

        // if tag is new, add to and sort tags, then display updated list
        if (!tags.contains(tag))
        {
            tags.add(tag); // add new tag
            Collections.sort(tags, String.CASE_INSENSITIVE_ORDER);
            adapter.notifyDataSetChanged(); // rebind tags to ListView
        }
    }

    OnItemClickListener itemClickListener = new OnItemClickListener()
    {
        @Override
        public void onItemClick(AdapterView<?> parent, View view,
                                int position, long id)
        {
            // get query string and create a URL representing the search
            String tag = ((TextView) view).getText().toString();
            String urlString = getString(R.string.searchURL) +
                    Uri.encode(savedSearches.getString(tag, ""), "UTF-8");

            // create an Intent to launch a web browser
            Intent webIntent = new Intent(Intent.ACTION_VIEW,
                    Uri.parse(urlString));

            startActivity(webIntent); // launches web browser to view results
        }
    }; // end itemClickListener declaration

    // itemLongClickListener displays a dialog allowing the user to delete
    // or edit a saved search
}

//<string name="searchURL">http://mobile.twitter.com/search/</string>
//<string name="OK">OK</string>
//<string name="missingMessage">Enter both Twitter search query and a tag</string>
