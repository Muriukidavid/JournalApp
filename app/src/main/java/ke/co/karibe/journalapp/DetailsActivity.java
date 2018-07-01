package ke.co.karibe.journalapp;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import ke.co.karibe.journalapp.database.AppDatabase;
import ke.co.karibe.journalapp.database.JournalEntry;

public class DetailsActivity extends AppCompatActivity {

    private TextView mTitleView, mBodyView, mDate;
    private FloatingActionButton mEditFab;
    private AppDatabase mDb;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);

        Toolbar myToolbar = findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(R.string.note_details);

        mDate = findViewById(R.id.details_date);
        mTitleView = findViewById(R.id.details_title);
        mBodyView = findViewById(R.id.details_body);
        mEditFab = findViewById(R.id.edit_fab);

        Intent intent = getIntent();
        final int entryId = intent.getExtras().getInt(AddJournalEntry.EXTRA_ENTRY_ID);

        mEditFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(DetailsActivity.this,AddJournalEntry.class);
                intent.putExtra(AddJournalEntry.EXTRA_ENTRY_ID,entryId);
                startActivity(intent);
            }
        });

        mDb = AppDatabase.getInstance(getApplicationContext());


        if ( intent.hasExtra(AddJournalEntry.EXTRA_ENTRY_ID) ) {

            AddEntryViewModelFactory factory = new AddEntryViewModelFactory(mDb, entryId);
            AddEntryViewModel model = ViewModelProviders.of(this,factory).get(AddEntryViewModel.class);

            model.getJournalEntry().observe(this, new Observer<JournalEntry>() {
                @Override
                public void onChanged(@Nullable JournalEntry journalEntry) {
                    Calendar c = Calendar.getInstance();
                    SimpleDateFormat dfmt = new SimpleDateFormat("dd-MM-YYYY HH:mm (z)");
                    dfmt.setTimeZone(c.getTimeZone());
                    String myDate = dfmt.format(journalEntry.getDate());
                    mDate.setText("Created: "+myDate);
                    mTitleView.setText(journalEntry.getTitle());
                    mBodyView.setText(journalEntry.getBody());
                }
            });
        }

    }
}
