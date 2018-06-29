package ke.co.karibe.journalapp;

import android.arch.lifecycle.ViewModel;
import android.arch.lifecycle.ViewModelProvider;

import ke.co.karibe.journalapp.database.AppDatabase;

public class AddEntryViewModelFactory extends ViewModelProvider.NewInstanceFactory {
    private final AppDatabase mDb;
    private final int mEntryId;

    public AddEntryViewModelFactory(AppDatabase database, int eventId) {
        mDb = database;
        mEntryId = eventId;
    }

    @Override
    public <T extends ViewModel> T create(Class<T> modelClass) {
        //noinspection unchecked
        return (T) new AddEntryViewModel(mDb, mEntryId);
    }
}
