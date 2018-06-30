package ke.co.karibe.journalapp.database;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.PrimaryKey;

import java.util.Date;

@Entity(tableName = "entry")
public class JournalEntry {
    @PrimaryKey(autoGenerate = true)
    private int id;
    private Date date;
    private String title;
    private String body;

    @Ignore
    public JournalEntry(Date date, String title, String body){
        this.date = date;
        this.title = title;
        this.body = body;
    }

    public JournalEntry(int id, Date date, String title, String body){
        this.id = id;
        this.date = date;
        this.title = title;
        this.body = body;
    }

    public int getId() {return id;}
    public void setId(int id) {this.id = id;}

    public Date getDate() {return this.date;}
    public void setDate(Date date) {this.date = date;}

    public String getTitle() {return this.title;}
    public void setTitle(String title) {this.title = title;}

    public String getBody() {return this.body;}
    public void setBody(String body) {this.body = body;}
}
