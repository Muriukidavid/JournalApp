package ke.co.karibe.journalapp;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.ViewModel;

import ke.co.karibe.journalapp.database.AppDatabase;
import ke.co.karibe.journalapp.database.JournalEntry;

public class AddEntryViewModel extends ViewModel {

    private LiveData<JournalEntry> journalEntryLiveData;

    public AddEntryViewModel(AppDatabase database, int entryId) {
        journalEntryLiveData = database.entryDao().loadEntryById(entryId);
    }

    // Create a getter for the journalEntry variable
    public LiveData<JournalEntry> getJournalEntry() {
        return journalEntryLiveData;
    }
}
