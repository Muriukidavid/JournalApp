package ke.co.karibe.journalapp;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.IdpResponse;
import com.shobhitpuri.custombuttons.GoogleSignInButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.Arrays;
import java.util.List;

import ke.co.karibe.journalapp.database.AppDatabase;
import ke.co.karibe.journalapp.database.JournalEntry;

public class MainActivity extends AppCompatActivity
        implements EntryAdapter.ItemClickListener, View.OnClickListener {
    //constants for a unique loader
    private static final String TAG = MainActivity.class.getSimpleName();
    //Variables for the adapter and RecyclerView
    private EntryAdapter mAdapter;
    private RecyclerView mRecyclerView;
    private FloatingActionButton mLogoutFab;
    private GoogleSignInButton mSignInButton;
    private ImageView mIcJournal;
    private ImageView mParallaxImage;
    private Button mButtonAdd;

    private AppDatabase mDb;

    private static final int RC_SIGN_IN = 21;
    // Choose authentication providers
    List<AuthUI.IdpConfig> providers = Arrays.asList(
            new AuthUI.IdpConfig.GoogleBuilder().build());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mSignInButton = findViewById(R.id.sign_in_button);
        mIcJournal = findViewById(R.id.ic_journal);
        mParallaxImage = findViewById(R.id.parallaxImage);
        mButtonAdd = findViewById(R.id.button_add);
        mLogoutFab = findViewById(R.id.logoutFab);

        mSignInButton.setOnClickListener(this);

        //Set the RecyclerView to its corresponding view
        mRecyclerView = findViewById(R.id.recyclerViewEntries);
        //Set the RecyclerView's layout as a linear layout - a list
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        //initialize the adapter and set it as RecyclerView's adapter
        mAdapter = new EntryAdapter(this, this);
        mRecyclerView.setAdapter(mAdapter);
        //DividerItemDecoration decoration = new DividerItemDecoration(getApplicationContext(), VERTICAL);
        //mRecyclerView.addItemDecoration(decoration);

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


        //mFab = findViewById(R.id.addEntryFab);

        mButtonAdd.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                //create a new Intent and launch addJournalEntry
                Intent intent = new Intent(MainActivity.this,AddJournalEntry.class);
                startActivity(intent);
            }
        });

        mLogoutFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseAuth.getInstance().signOut();
                updateUI(null);
            }
        });

        mDb = AppDatabase.getInstance(getApplicationContext());
        setupViewModel();
    }

    @Override
    protected void onStart(){
        super.onStart();

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        updateUI(user);
    }

    private void signIn(){
        startActivityForResult(
                AuthUI.getInstance()
                        .createSignInIntentBuilder()
                        .setAvailableProviders(providers)
                        .build(),
                RC_SIGN_IN);

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data){
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {

            IdpResponse response = IdpResponse.fromResultIntent(data);

            if (resultCode == RESULT_OK) {
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                updateUI(user);
            } else {
                //sign in failed
                updateUI(null);
                if (response == null) {
                    Log.d("onActivityResult","User cancelled the sign-in flow using the back button!");
                } else {
                    int errorCode = response.getError().getErrorCode();
                    Log.d("onActivityResult","errorCode: "+Integer.toString(errorCode));
                }
            }

        }
    }

    private void updateUI(FirebaseUser user){
        if(user != null) {
            LinearLayout lyt = findViewById(R.id.mainLayout);
            lyt.setBackground(null);
            mIcJournal.setVisibility(View.INVISIBLE);
            mSignInButton.setVisibility(View.INVISIBLE);
            mRecyclerView.setVisibility(View.VISIBLE);
            mParallaxImage.setVisibility(View.VISIBLE);
            mLogoutFab.setVisibility(View.VISIBLE);
            mButtonAdd.setVisibility(View.VISIBLE);

        }else {
            LinearLayout lyt = findViewById(R.id.mainLayout);
            lyt.setBackground(getDrawable(R.drawable.gradient));
            mIcJournal.setVisibility(View.VISIBLE);
            mRecyclerView.setVisibility(View.INVISIBLE);
            mLogoutFab.setVisibility(View.INVISIBLE);
            mParallaxImage.setVisibility(View.INVISIBLE);
            mButtonAdd.setVisibility(View.INVISIBLE);
            mSignInButton.setVisibility(View.VISIBLE);
        }
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
        // Launch DetailsActivity to show a Journal Entry passing the itemId as an extra in the intent
        Intent intent = new Intent(MainActivity.this, DetailsActivity.class);
        intent.putExtra(AddJournalEntry.EXTRA_ENTRY_ID, itemId);
        startActivity(intent);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.sign_in_button:
                signIn();
                break;
            default:
                Log.d("MainActivity onClick","Not Sign in button");
        };
    }
}
