package net.mcdermotsoft.fingernotes;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.view.ActionMode;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.OutputStream;
import java.util.ArrayList;

public class NoteList extends ActionBarActivity
{
	DrawerLayout drawerLayout;
	ListView drawerList, noteList;
	ActionBarDrawerToggle drawerToggle;
	NoteList context = this;
	TextView emptyView;
	ActionMode actionMode;
	NoteListAdapter adapter;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_note_list);

		drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
		drawerToggle = new ActionBarDrawerToggle(this, drawerLayout, R.drawable.ic_drawer, R.string.drawer_open, R.string.drawer_close)
		{
			public void onDrawerClosed(View view)
			{
			}

			public void onDrawerOpened(View drawerView)
			{
			}
		};
		drawerLayout.setDrawerListener(drawerToggle);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		getSupportActionBar().setHomeButtonEnabled(true);

		drawerList = (ListView) findViewById(R.id.navigation_drawer);
		String[] options = getResources().getStringArray(R.array.navigationArray);
		drawerList.setAdapter(new DrawerAdapter(this, android.R.layout.simple_dropdown_item_1line, options));
		drawerList.setOnItemClickListener(new AdapterView.OnItemClickListener()
		{
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id)
			{
				if(position == 0) //create note
				{
					drawerLayout.closeDrawers();
					Intent intent = new Intent(context, DrawingActivity.class);
					startActivity(intent);
				}
				if(position == 1) //about
				{
					drawerLayout.closeDrawers();
					Intent intent = new Intent(context, AboutActivity.class);
					startActivity(intent);
				}
			}
		});

		noteList = (ListView) findViewById(R.id.noteList);
		emptyView = (TextView) findViewById(R.id.empty);

		adapter = new NoteListAdapter(context, 1, new ArrayList<Note>());
		noteList.setAdapter(adapter);
		noteList.setOnItemClickListener(new AdapterView.OnItemClickListener()
		{
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id)
			{
				if(actionMode != null)
				{
					// if action mode, toggle checked state of item
					adapter.toggleChecked(position);
					actionMode.invalidate();
				}
				else
				{
					Intent intent = new Intent(context, DrawingActivity.class);
					Bundle bundle = new Bundle();
					bundle.putSerializable("lines", adapter.getItem(position).lines);
					intent.putExtras(bundle);
					context.startActivity(intent);
				}
			}
		});
		noteList.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener()
		{
			@Override
			public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id)
			{
				if(actionMode != null)
					return false;

				// set checked selected item and enter multi selection mode
				adapter.setChecked(position, true);
				startSupportActionMode(new ActionModeCallback());
				actionMode.invalidate();
				return true;
			}
		});
	}

	@Override
	public void onResume()
	{
		super.onResume();

		new SavedNotesTask().execute();
	}

	@Override
	protected void onPostCreate(Bundle savedInstanceState)
	{
		super.onPostCreate(savedInstanceState);
		// Sync the toggle state after onRestoreInstanceState has occurred.
		drawerToggle.syncState();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		getMenuInflater().inflate(R.menu.note_list, menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		// Pass the event to ActionBarDrawerToggle, if it returns
		// true, then it has handled the app icon touch event
		if(drawerToggle.onOptionsItemSelected(item))
		{
			return true;
		}
		int id = item.getItemId();
		if(id == R.id.newNote)
		{
			Intent intent = new Intent(context, DrawingActivity.class);
			startActivity(intent);
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	private class SavedNotesTask extends AsyncTask<Void, Void, ArrayList<Note>>
	{
		@Override
		protected void onPreExecute()
		{
			if(noteList.getAdapter() == null)
			{
				emptyView.setText(R.string.loading);
				emptyView.setVisibility(View.VISIBLE);
			}
		}

		@Override
		protected ArrayList<Note> doInBackground(Void... params)
		{
			ArrayList<Note> notes = new ArrayList<Note>();
			for(File note : getFilesDir().listFiles())
			{
				try
				{
					ObjectInputStream is = new ObjectInputStream(new FileInputStream(note));
					notes.add((Note) is.readObject());
					is.close();
				}
				catch(Exception ex)
				{
					Log.e("FingerNotes", "could not read file" + ex.getMessage());
				}
			}
			return notes;
		}

		@Override
		protected void onPostExecute(ArrayList<Note> notes)
		{
			if(notes.size() > 0)
			{
				emptyView.setVisibility(View.GONE);

				NoteListAdapter listAdapter = (NoteListAdapter) noteList.getAdapter();
				listAdapter.clear();
				for(Note note : notes) //addAll requires api level 11
					listAdapter.add(note);
				listAdapter.notifyDataSetChanged();
			}
			else
				emptyView.setText(R.string.empty);
		}
	}

	private class ActionModeCallback implements ActionMode.Callback
	{
		String selected = context.getString(R.string.selected);

		@Override
		public boolean onCreateActionMode(ActionMode mode, Menu menu)
		{
			adapter.enterMultiMode();
			// save global action mode
			actionMode = mode;

			// Inflate the menu for the CAB
			MenuInflater inflater = mode.getMenuInflater();
			inflater.inflate(R.menu.context, menu);
			return true;
		}

		@Override
		public boolean onPrepareActionMode(ActionMode mode, Menu menu)
		{
			// Here you can perform updates to the CAB due to
			// an invalidate() request
			final int checked = adapter.getCheckedItemCount();
			if(checked == 0)
				mode.finish();
			// update title with number of checked items
			mode.setTitle(checked + " " + selected);
			return false;
		}

		@Override
		public boolean onActionItemClicked(ActionMode mode, MenuItem item)
		{
			// Respond to clicks on the actions in the CAB
			switch(item.getItemId())
			{
				case R.id.delete:
					deleteSelectedItems();
					mode.finish(); // Action picked, so close the CAB
					return true;
				case R.id.share:
					shareSelectedItems();
					mode.finish();
					return true;
				default:
					return false;
			}
		}

		@Override
		public void onDestroyActionMode(ActionMode mode)
		{
			adapter.exitMultiMode();
			actionMode = null;
		}

		private void deleteSelectedItems()
		{
			ArrayList<Note> deleteList = new ArrayList<Note>();
			for(Integer pos : adapter.getCheckedItems())
			{
				File file = new File(context.getFilesDir(), adapter.getItem(pos).id+".note");
				file.delete();
				deleteList.add(adapter.getItem(pos));
			}
			for(Note note : deleteList)
				adapter.remove(note);
			adapter.notifyDataSetChanged();

			if(adapter.getCount() == 0)
			{
				emptyView.setVisibility(View.VISIBLE);
				emptyView.setText(R.string.empty);
			}
		}

		private void shareSelectedItems()
		{
			ArrayList<Uri> uris = new ArrayList<Uri>();
			Intent intent = new Intent(Intent.ACTION_SEND_MULTIPLE);
			intent.setType("image/png");

			for(Integer pos : adapter.getCheckedItems())
			{
				Note note = adapter.getItem(pos);
				File folder = new File(Environment.getExternalStorageDirectory().toString()+File.separator+"fingerNotes");
				if(!folder.exists())
					folder.mkdir();
				File file = new File(folder, "fingernote"+note.id+".png");
				try
				{
					FileOutputStream fOut = new FileOutputStream(file);
					note.image.bitmap.compress(Bitmap.CompressFormat.PNG, 100, fOut);
					fOut.flush();
					fOut.close();
					uris.add(Uri.fromFile(file));
				}
				catch(Exception ex)
				{
					Toast.makeText(context,"Error, could not save image file",Toast.LENGTH_SHORT).show();
					return;
				}
			}

			intent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, uris);
			startActivity(intent);
		}
	}
}
