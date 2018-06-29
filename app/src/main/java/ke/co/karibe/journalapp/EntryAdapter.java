package ke.co.karibe.journalapp;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import ke.co.karibe.journalapp.database.JournalEntry;


public class EntryAdapter extends RecyclerView.Adapter<EntryAdapter.EntryViewHolder>{
    private Context mContext;

    private List<JournalEntry> mJournalEntries;

    // Member variable to handle item clicks
    final private ItemClickListener mItemClickListener;

    /**
     * Constructor for the EntryAdapter that initializes the Context.
     *
     * @param context the current Context
     */
    public EntryAdapter(Context context, ItemClickListener listener) {
        mContext = context;
        mItemClickListener = listener;
    }

    /**
     * Called when ViewHolders are created to fill a RecyclerView.
     *
     * @return A new TaskViewHolder that holds the view for each task
     */
    @Override
    public EntryViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext)
                .inflate(R.layout.entry_layout, parent,false);

        return new EntryViewHolder(view);
    }
    /**
     * Called by the RecyclerView to display data at a specified position in the Cursor.
     *
     * @param holder The ViewHolder to bind Cursor data to
     * @param position The position of the data in the Cursor
     */
    @Override
    public void onBindViewHolder(EntryViewHolder holder, int position) {
        //The values for UI from data
        JournalEntry journalEntry = mJournalEntries.get(position);

        //get the values of data
        int id = journalEntry.getId();
        String date = journalEntry.getDate();
        String title = journalEntry.getTitle();

        //set the values
        holder.entryDate.setText(date);
        holder.entryTitle.setText(title);
    }

    @Override
    public int getItemCount() {
        if (mJournalEntries == null) {
            return 0;
        }
        return mJournalEntries.size();
    }

    public List<JournalEntry> getJournalEntries() {
        return mJournalEntries;
    }
    /**
     * When data changes, this method updates the list of taskEntries
     * and notifies the adapter to use the new values on it
     */
    public void setJournalEntries(List<JournalEntry> journalEntries) {
        mJournalEntries = journalEntries;
        notifyDataSetChanged();
    }

    public interface ItemClickListener {
        void onItemClickListener(int itemId);
    }

    // Inner class for creating ViewHolders
    class EntryViewHolder extends RecyclerView.ViewHolder {

        // Class variables for the journal entry date and title TextViews
        TextView entryDate;
        TextView entryTitle;

        /**
         * Constructor for the EntryViewHolders.
         *
         * @param itemView The view inflated in onCreateViewHolder
         */
        public EntryViewHolder(View itemView) {
            super(itemView);
            entryDate = itemView.findViewById(R.id.TextViewDate);
            entryTitle = itemView.findViewById(R.id.TextViewTitle);
        }
    }
}
