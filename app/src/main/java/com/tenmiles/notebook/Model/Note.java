package com.tenmiles.notebook.Model;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by vinothkathiravan on 15/08/15.
 */
public class Note implements Parcelable {
    private String title;
    private String notes;
    private String dateEdited;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public String getDateEdited() {
        return dateEdited;
    }

    public void setDateEdited(String dateEdited) {
        this.dateEdited = dateEdited;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {

    }
}
