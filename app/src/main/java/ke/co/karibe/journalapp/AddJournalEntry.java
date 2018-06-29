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

import ke.co.karibe.journalapp.database.AppDatabase;
import ke.co.karibe.journalapp.database.JournalEntry;

public class AddJournalEntry extends AppCompatActivity {
    // Extra for the task ID to be received in the intent
    public static final String EXTRA_ENTRY_ID = "extraTaskId";
    // Extra for the task ID to be received after rotation
    public static final String INSTANCE_ENTRY_ID = "instanceTaskId";

    // Constant for default task id to be used when not in update mode
    private static final int DEFAULT_ENTRY_ID = -1;
    // Constant for logging
    private static final String TAG = AddJournalEntry.class.getSimpleName();
    //access to the database class
    private AppDatabase mDb;

    private int mEntryId = DEFAULT_ENTRY_ID;

    EditText mEditTitle;
    EditText mEditBody;
    TextView mDateText;
    Button mButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_journal_entry);

        initViews();

        mDb = AppDatabase.getInstance(getApplicationContext());

        if (savedInstanceState != null && savedInstanceState.containsKey(INSTANCE_ENTRY_ID)) {
            mEntryId = savedInstanceState.getInt(INSTANCE_ENTRY_ID, DEFAULT_ENTRY_ID);
        }

        Intent intent = getIntent();
        if (intent != null && intent.hasExtra(EXTRA_ENTRY_ID)) {
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
        mEditTitle = findViewById(R.id.editTextJournalTitle);
        mEditBody = findViewById(R.id.editTextJournalBody);
        mButton = findViewById(R.id.saveButton);

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

        mEditTitle.setText(journalEntry.getTitle());
        mEditBody.setText(journalEntry.getBody());
        mDateText.setText(journalEntry.getDate());
    }


    public void onClickAddEntry(View view){
        String title = mEditTitle.getText().toString();
        String body = mEditBody.getText().toString();
        String date = new SimpleDateFormat("dd-mmm-yyyy")
                .format(Calendar.getInstance().getTime());

        if (title.length() == 0){
            return;
        }

        finish();
        if (body.length() == 0) {
            return;
        }

        //create a new Journal Entry
        final JournalEntry journalEntry = new JournalEntry(date, title, body);

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
