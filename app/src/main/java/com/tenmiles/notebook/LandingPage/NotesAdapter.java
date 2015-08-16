package com.tenmiles.notebook.LandingPage;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.tenmiles.notebook.Model.Note;
import com.tenmiles.notebook.R;

import java.util.ArrayList;

/**
 * Created by vinothkathiravan on 15/08/15.
 */
public class NotesAdapter extends ArrayAdapter<Note>
{

    Context context;
    ArrayList<Note> lstNotes;
    public NotesAdapter(Context context, ArrayList<Note> lstNotes)
    {
        super(context,0,lstNotes);
        this.context = context;
        this.lstNotes = lstNotes;

    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater layoutInflater;
        Holder holder;
        if (convertView == null) {
            layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = layoutInflater.inflate(R.layout.notes_adapter, null);

            //Create holder object to reuse the cell
            holder = new Holder();
            holder.titleText = (TextView) convertView.findViewById(R.id.titleText);
            holder.dateText = (TextView) convertView.findViewById(R.id.dateText);
            holder.notesText = (TextView) convertView.findViewById(R.id.notesText);
            convertView.setTag(holder);
        }else {
            holder = (Holder) convertView.getTag();
        }

        Note note = getItem(position);
        holder.titleText.setText(note.getTitle());
        holder.dateText.setText(note.getDateEdited());
        holder.notesText.setText(note.getNotes());

        return convertView;
    }

    private class Holder
    {
        TextView titleText;
        TextView dateText;
        TextView notesText;
    }
}
