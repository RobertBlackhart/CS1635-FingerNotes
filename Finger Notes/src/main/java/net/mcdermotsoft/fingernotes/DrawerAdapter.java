package net.mcdermotsoft.fingernotes;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by Robert McDermot on 1/21/14.
 */
public class DrawerAdapter extends ArrayAdapter<String>
{
	NoteList context;
	String[] actions;

	public DrawerAdapter(NoteList context, int textViewResourceId, String[] actions)
	{
		super(context, textViewResourceId, actions);
		this.actions = actions;
		this.context = context;
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
			v = vi.inflate(R.layout.drawer_row, null);
		}

		ImageView icon = (ImageView) v.findViewById(R.id.icon);
		if(position == 0)
			icon.setBackgroundResource(R.drawable.create_new_note);
		if(position == 1)
			icon.setBackgroundResource(R.drawable.about);

		TextView name = (TextView) v.findViewById(R.id.name);
		name.setText(actions[position]);

		return v;
	}
}
