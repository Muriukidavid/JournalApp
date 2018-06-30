package ke.co.karibe.journalapp;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import ke.co.karibe.journalapp.database.AppDatabase;
import ke.co.karibe.journalapp.database.JournalEntry;

public class AddJournalEntry extends AppCompatActivity {
    // Extra for the task ID to be received in the intent
    public static final String EXTRA_ENTRY_ID = "extraTaskId";
    // Extra for the task ID to be received after rotation
    public static final String INSTANCE_ENTRY_ID = "instanceTaskId";

    // Constant for default task id to be used when not in update mode
    private static final int DEFAULT_ENTRY_ID = -1;
    //date to show early
    private static Date mDate;
    //access to the database class
    private AppDatabase mDb;

    private int mEntryId = DEFAULT_ENTRY_ID;

    EditText mEditTitle;
    EditText mEditBody;
    TextView mDateText;
    Button mButton;
/*
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_settings:
                // User chose the "Settings" item, show the app settings UI...
                return true;

            case R.id.action_favorite:
                // User chose the "Favorite" action, mark the current item
                // as a favorite...
                return true;

            default:
                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it.
                return super.onOptionsItemSelected(item);

        }
    }
*/
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_journal_entry);

        mDate =  Calendar.getInstance().getTime();
        initViews();

        mDb = AppDatabase.getInstance(getApplicationContext());

        if (savedInstanceState != null && savedInstanceState.containsKey(INSTANCE_ENTRY_ID)) {
            mEntryId = savedInstanceState.getInt(INSTANCE_ENTRY_ID, DEFAULT_ENTRY_ID);
        }

        Intent intent = getIntent();
        if (intent != null && intent.hasExtra(EXTRA_ENTRY_ID)) {
            getSupportActionBar().setTitle(R.string.edit_entry_name);
            mButton.setText(R.string.update_button);
            if (mEntryId == DEFAULT_ENTRY_ID) {
                //populate the UI
                mEntryId = intent.getIntExtra(EXTRA_ENTRY_ID, DEFAULT_ENTRY_ID);
                AddEntryViewModelFactory factory = new AddEntryViewModelFactory(mDb, mEntryId);
                final AddEntryViewModel viewModel
                        = ViewModelProviders.of(this, factory).get(AddEntryViewModel.class);
                viewModel.getJournalEntry().observe(this, new Observer<JournalEntry>() {
                    @Override
                    public void onChanged(@Nullable JournalEntry journalEntry) {
                        viewModel.getJournalEntry().removeObserver(this);
                        populateUI(journalEntry);
                    }
                });
            }
        }

    }

    /**    private Cursor mCursor;
     * initViews is called from onCreate to init the member variable views
     */
    private void initViews() {
        mDateText = findViewById(R.id.textViewItemDate);
        String date = new SimpleDateFormat("dd-MM-YYYY HH:mm (z)").format(mDate);
        mDateText.setText(date);
        mEditTitle = findViewById(R.id.editTextJournalTitle);
        mEditBody = findViewById(R.id.editTextJournalBody);
        mButton = findViewById(R.id.saveButton);
        getSupportActionBar().setTitle(R.string.add_entry_name);
        mButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onClickAddEntry(view);
            }
        });
    }

    /**
     * populateUI would be called to populate the UI when in update mode
     *
     * @param journalEntry the journalEntry to populate the UI
     */
    private void populateUI(JournalEntry journalEntry) {
        if (journalEntry == null) {
            return;
        }
        Calendar c = Calendar.getInstance();
        SimpleDateFormat dfmt = new SimpleDateFormat("dd-MM-YYYY HH:mm (z)");
        dfmt.setTimeZone(c.getTimeZone());
        String myDate = dfmt.format(journalEntry.getDate());
        mEditTitle.setText(journalEntry.getTitle());
        mEditBody.setText(journalEntry.getBody());
        mDateText.setText("Created: "+myDate);
    }


    public void onClickAddEntry(View view){
        String title = mEditTitle.getText().toString();
        String body = mEditBody.getText().toString();


        if (title.length() == 0){
            return;
        }

        finish();
        if (body.length() == 0) {
            return;
        }

        //create a new Journal Entry
        final JournalEntry journalEntry = new JournalEntry(mDate, title, body);

        //insert new entry
        AppExecutors.getInstance().diskIO().execute(new Runnable() {
            @Override
            public void run() {
                if (mEntryId == DEFAULT_ENTRY_ID) {
                    // insert new task
                    mDb.entryDao().insertJournalEntry(journalEntry);
                } else {
                    //update task
                    journalEntry.setId(mEntryId);
                    mDb.entryDao().updateJournalEntry(journalEntry);
                }
                //return back to MainActivity
                finish();
            }
        });
    }
}
