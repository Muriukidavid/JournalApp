package ke.co.karibe.journalapp;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.TextView;

import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import ke.co.karibe.journalapp.database.AppDatabase;
import ke.co.karibe.journalapp.database.JournalEntry;


public class AddJournalEntry extends AppCompatActivity {
    // Extra for the task ID to be received in the intent
    public static final String EXTRA_ENTRY_ID = "extraEntryId";
    // Extra for the task ID to be received after rotation
    public static final String INSTANCE_ENTRY_ID = "instanceEntryId";

    // Constant for default task id to be used when not in update mode
    private static final int DEFAULT_ENTRY_ID = -1;
    //date to show early
    private static Date mDate;
    //access to the database class
    private AppDatabase mDb;

    private int mEntryId = DEFAULT_ENTRY_ID;

    private FloatingActionButton mLogoutFab;

    private EditText mEditTitle;
    private EditText mEditBody;
    private TextView mDateText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_journal_entry);

        Toolbar myToolbar = findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(R.string.add_entry_name);

        mDateText = findViewById(R.id.textViewItemDate);
        mEditTitle = findViewById(R.id.editTextJournalTitle);
        mEditBody = findViewById(R.id.editTextJournalBody);

        Calendar cal = Calendar.getInstance();
        mDate = cal.getTime();
        TimeZone t = cal.getTimeZone();
        SimpleDateFormat format = new SimpleDateFormat("dd-MM-YYYY HH:mm (z)");
        format.setTimeZone(t);
        String date = "Created: "+format.format(mDate);
        mDateText.setText(date);

        mDb = AppDatabase.getInstance(getApplicationContext());

        if (savedInstanceState != null && savedInstanceState.containsKey(INSTANCE_ENTRY_ID)) {
            mEntryId = savedInstanceState.getInt(INSTANCE_ENTRY_ID, DEFAULT_ENTRY_ID);
        }

        Intent intent = getIntent();
        if (intent != null && intent.hasExtra(EXTRA_ENTRY_ID)) {
            getSupportActionBar().setTitle(R.string.edit_entry_name);
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
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
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-YYYY HH:mm (z)");
        dateFormat.setTimeZone(c.getTimeZone());
        String myDate = dateFormat.format(journalEntry.getDate());
        mEditTitle.setText(journalEntry.getTitle());
        mEditBody.setText(journalEntry.getBody());
        mDateText.setText("Created: "+myDate);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_save:
                //String Date = mDateText.getText().toString();
                String title = mEditTitle.getText().toString();
                String body = mEditBody.getText().toString();

               /* Calendar c = Calendar.getInstance();
                SimpleDateFormat format = new SimpleDateFormat("dd-MM-YYYY HH:mm");
                format.setTimeZone(c.getTimeZone());
                Log.d("dateString",Date);
                //Date myDate = format.parse(Date, new ParsePosition(9));
                //Log.d("Date",myDate.toString());
                */
                if (title.length() == 0){
                    return super.onOptionsItemSelected(item);
                }

                if (body.length() == 0) {
                    return super.onOptionsItemSelected(item);
                }

                //create a new Journal Entry
                final JournalEntry journalEntry = new JournalEntry(mDate, title, body);

                //insert new entry
                AppExecutors.getInstance().diskIO().execute(new Runnable() {
                    @Override
                    public void run() {
                        if (mEntryId == DEFAULT_ENTRY_ID) {
                            // insert new entry
                            Log.d("AddJournalEntry","Inserting entry to DB");
                            mDb.entryDao().insertJournalEntry(journalEntry);
                        } else {
                            //update entry
                            journalEntry.setId(mEntryId);
                            Log.d("AddJournalEntry","Updating entry to DB");
                            mDb.entryDao().updateJournalEntry(journalEntry);
                        }

                    }
                });
                //return back to previous activity
                finish();
                return true;
            case android.R.id.home:
                finish();
            default:
                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it.
                return super.onOptionsItemSelected(item);

        }
    }

}
