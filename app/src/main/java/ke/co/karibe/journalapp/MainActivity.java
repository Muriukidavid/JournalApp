package ke.co.karibe.journalapp;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.View;

import java.util.List;

import ke.co.karibe.journalapp.database.AppDatabase;
import ke.co.karibe.journalapp.database.JournalEntry;

import static android.support.v7.widget.DividerItemDecoration.VERTICAL;

public class MainActivity extends AppCompatActivity
        implements EntryAdapter.ItemClickListener {
    //constants for a unique loader
    private static final String TAG = MainActivity.class.getSimpleName();
    //Variables for the adapter and RecyclerView
    private EntryAdapter mAdapter;
    private RecyclerView mRecyclerView;

    private AppDatabase mDb;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Set the RecyclerView to its corresponding view
        mRecyclerView = findViewById(R.id.recyclerViewEntries);
        //Set the RecyclerView's layout as a linear layout - a list
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        //initialize the adapter and set it as RecyclerView's adapter
        mAdapter = new EntryAdapter(this, this);
        mRecyclerView.setAdapter(mAdapter);

        DividerItemDecoration decoration = new DividerItemDecoration(getApplicationContext(), VERTICAL);
        mRecyclerView.addItemDecoration(decoration);

        /*
         Add a touch helper to the RecyclerView to recognize when a user swipes to delete an item.
         An ItemTouchHelper enables touch behavior (like swipe and move) on each ViewHolder,
         and uses callbacks to signal when a user is performing these actions.
         */
        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                return false;
            }

            // Called when a user swipes left or right on a ViewHolder
            @Override
            public void onSwiped(final RecyclerView.ViewHolder viewHolder, int swipeDir) {
                // Here is where you'll implement swipe to delete
                AppExecutors.getInstance().diskIO().execute(new Runnable() {
                    @Override
                    public void run() {
                        int position = viewHolder.getAdapterPosition();
                        List<JournalEntry> journalEntries = mAdapter.getJournalEntries();
                        mDb.entryDao().deleteJournalEntry(journalEntries.get(position));
                    }
                });
            }
        }).attachToRecyclerView(mRecyclerView);


        FloatingActionButton fab = findViewById(R.id.addEntryFab);
        fab.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                //create a new Intent and launch addJournalEntry
                Intent intent = new Intent(MainActivity.this,AddJournalEntry.class);
                startActivity(intent);
            }
        });

        mDb = AppDatabase.getInstance(getApplicationContext());
        setupViewModel();
    }

    private void setupViewModel() {
        MainViewModel viewModel = ViewModelProviders.of(this).get(MainViewModel.class);
        viewModel.getJournalEntries().observe(this, new Observer<List<JournalEntry>>() {
            @Override
            public void onChanged(@Nullable List<JournalEntry> journalEntries) {
                Log.d(TAG, "Updating list of journal entries from LiveData in ViewModel");
                mAdapter.setJournalEntries(journalEntries);
            }
        });
    }

    @Override
    public void onItemClickListener(int itemId) {
        // Launch AddJournalEntry adding the itemId as an extra in the intent
        Intent intent = new Intent(MainActivity.this, AddJournalEntry.class);
        intent.putExtra(AddJournalEntry.EXTRA_ENTRY_ID, itemId);
        Log.d("onItemClickListener","trying to start Journal");
        startActivity(intent);
    }
}
