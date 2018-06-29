package ke.co.karibe.journalapp.database;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import java.util.List;

@Dao
public interface EntryDao {
    @Query("SELECT * FROM entry order by date")
    LiveData<List<JournalEntry>> loadAllEntries();

    @Query("SELECT * FROM entry where id = :id")
    LiveData<JournalEntry> loadEntryById(int id);

    @Insert
    void insertJournalEntry(JournalEntry journalEntry);

    @Update(onConflict = OnConflictStrategy.REPLACE)
    void updateJournalEntry(JournalEntry journalEntry);

    @Delete
    void deleteJournalEntry(JournalEntry journalEntry);
}
