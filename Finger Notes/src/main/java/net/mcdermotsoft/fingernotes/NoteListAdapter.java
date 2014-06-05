package net.mcdermotsoft.fingernotes;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by Robert McDermot on 1/21/14.
 */
public class NoteListAdapter extends ArrayAdapter<Note>
{
	NoteList context;
	ArrayList<Note> notes;
	// all our checked indexes go here
	private HashSet<Integer> checkedItems;

	// multi selection mode flag
	private boolean multiMode;

	public NoteListAdapter(NoteList context, int textViewResourceId, ArrayList<Note> notes)
	{
		super(context, textViewResourceId, notes);
		this.notes = notes;
		this.context = context;
		checkedItems = new HashSet<Integer>();
	}

	public void enterMultiMode()
	{
		multiMode = true;
		notifyDataSetChanged();
	}

	public void exitMultiMode()
	{
		checkedItems.clear();
		multiMode = false;
		notifyDataSetChanged();
	}

	public void setChecked(int pos, boolean checked)
	{
		if(checked)
			checkedItems.add(Integer.valueOf(pos));
		else
			checkedItems.remove(Integer.valueOf(pos));

		if(this.multiMode)
			notifyDataSetChanged();
	}

	public boolean isChecked(int pos)
	{
		return checkedItems.contains(Integer.valueOf(pos));
	}

	public void toggleChecked(int pos)
	{
		if(checkedItems.contains(Integer.valueOf(pos)))
			checkedItems.remove(Integer.valueOf(pos));
		else
			checkedItems.add(Integer.valueOf(pos));

		notifyDataSetChanged();
	}

	public int getCheckedItemCount()
	{
		return checkedItems.size();
	}

	public Set<Integer> getCheckedItems()
	{
		return checkedItems;
	}

	@Override
	public View getView(final int position, View convertView, ViewGroup parent)
	{
		View v = null;
		if(convertView != null)
			v = convertView;
		else
		{
			LayoutInflater vi = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			v = vi.inflate(R.layout.note_row, null);
		}

		if(isChecked(position))
			v.setBackgroundColor(context.getResources().getColor(R.color.holo_light_selected));
		else
			v.setBackgroundColor(context.getResources().getColor(R.color.holo_light_background));

		ImageView noteImage = (ImageView) v.findViewById(R.id.noteImage);
		noteImage.setImageBitmap(notes.get(position).image.bitmap);

		TextView serverResponse = (TextView) v.findViewById(R.id.serverResponse);
		serverResponse.setText(notes.get(position).serverInterpretation);

		return v;
	}
}
