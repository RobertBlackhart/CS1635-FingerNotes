package net.mcdermotsoft.fingernotes;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.URLUtil;
import android.widget.ImageView;
import android.widget.Toast;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;

public class DrawingActivity extends ActionBarActivity
{
	DrawingActivity context = this;
	DrawingView drawingView;
	PaletteDialog paletteDialog;
	ImageView currentColor;
	ArrayList<LineSegment> redoList = new ArrayList<LineSegment>();
	boolean cleared = false, undoCleared = false;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_drawing);

		getSupportActionBar().setDisplayHomeAsUpEnabled(true);

		drawingView = (DrawingView) findViewById(R.id.drawingView);
		drawingView.activity = this;

		final ImageView undo = (ImageView) findViewById(R.id.undo);
		final ImageView redo = (ImageView) findViewById(R.id.redo);
		final ImageView clear = (ImageView) findViewById(R.id.clear);

		Bundle bundle = getIntent().getExtras();
		if(bundle != null)
		{
			ArrayList<LineSegment> lines = (ArrayList<LineSegment>)bundle.get("lines");
			drawingView.actionList = lines;
			if(drawingView.actionList.size() > 0)
			{
				undo.setImageResource(R.drawable.undo);
				clear.setImageResource(R.drawable.undo_all);
			}
			drawingView.invalidate();
		}

		paletteDialog = new PaletteDialog(context);
		ImageView colorPalette = (ImageView) findViewById(R.id.colorPalette);
		colorPalette.setOnClickListener(new View.OnClickListener()
		{
			public void onClick(View v)
			{
				if(paletteDialog.isShowing())
				{
					paletteDialog.hide();
				}
				else
				{
					paletteDialog.show();
				}
			}
		});

		currentColor = (ImageView) findViewById(R.id.currentColor);

		undo.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				if(cleared)
				{
					while(redoList.size() > 0)
						drawingView.actionList.add(redoList.remove(redoList.size()-1));
					cleared = false;
					undoCleared = true;
				}
				else if(drawingView.actionList.size() < 1)
				{
					Toast.makeText(context, "Nothing to undo", Toast.LENGTH_SHORT).show();
					return;
				}
				else
					redoList.add(drawingView.actionList.remove(drawingView.actionList.size()-1));
				redo.setImageResource(R.drawable.redo);
				if(drawingView.actionList.size() == 0)
				{
					clear.setImageResource(R.drawable.undo_all_disabled);
					undo.setImageResource(R.drawable.undo_disabled);
				}
				drawingView.invalidate();
			}
		});
		redo.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				if(undoCleared)
				{
					while(drawingView.actionList.size() > 0)
						redoList.add(drawingView.actionList.remove(drawingView.actionList.size()-1));
					undoCleared = false;
					cleared = true;
				}
				else if(redoList.size() < 1)
				{
					Toast.makeText(context, "Nothing to redo", Toast.LENGTH_SHORT).show();
					return;
				}
				else
					drawingView.actionList.add(redoList.remove(redoList.size()-1));
				undo.setImageResource(R.drawable.undo);
				clear.setImageResource(R.drawable.undo_all);
				if(redoList.size() == 0)
					redo.setImageResource(R.drawable.redo_disabled);
				drawingView.invalidate();
			}
		});
		clear.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				if(drawingView.actionList.size() == 0)
				{
					Toast.makeText(context, "Nothing to clear", Toast.LENGTH_SHORT).show();
					return;
				}
				while(drawingView.actionList.size() > 0)
					redoList.add(drawingView.actionList.remove(drawingView.actionList.size()-1));
				cleared = true;
				undo.setImageResource(R.drawable.undo);
				redo.setImageResource(R.drawable.redo_disabled);
				clear.setImageResource(R.drawable.undo_all_disabled);
				drawingView.invalidate();
			}
		});
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		getMenuInflater().inflate(R.menu.drawing_menu, menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		switch (item.getItemId())
		{
			case android.R.id.home:
				finish();
				return true;
			case R.id.saveNote:
				new UploadTask().execute();
				return true;
			case R.id.cancelNote:
				getFileStreamPath("tempSave").delete();
				paletteDialog.hide();
				finish();
				return true;
			default:
				return super.onOptionsItemSelected(item);
		}
	}

	private class UploadTask extends AsyncTask<Void,Void,String>
	{
		ProgressDialog progressDialog;

		@Override
		protected void onPreExecute()
		{
			progressDialog = ProgressDialog.show(context, "", "Uploading to server, please wait...", true);
		}

		@Override
		protected String doInBackground(Void...voids)
		{
			float largest = 0;
			for(LineSegment line : drawingView.actionList)
			{
				for(MyPoint point : line.getPath().points)
				{
					if(point.getY() > largest)
						largest = point.getY();
					if(point.getX() > largest)
						largest = point.getX();
				}
			}
			float scale = 255/largest;
			String q = "[";
			for(LineSegment line : drawingView.actionList)
			{
				for(MyPoint point : line.getPath().points)
				{
					int x = (int) (point.getX()*scale);
					int y = (int) (point.getY()*scale);
					q += x+", "+y+", ";
				}
				q += "255, 0";
			}
			q += ", 255, 255]";
			Log.d("FingerNotes",q);

			try
			{
				HttpClient httpclient = new DefaultHttpClient();
				HttpPost httppost = new HttpPost("http://cwritepad.appspot.com/reco/usen");

				List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
				nameValuePairs.add(new BasicNameValuePair("key", "11773edfd643f813c18d82f56a8104ed"));
				nameValuePairs.add(new BasicNameValuePair("q", q));
				httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));

				HttpResponse response = httpclient.execute(httppost);
				String resp = EntityUtils.toString(response.getEntity());

				return resp;
			}
			catch(IOException e)
			{
				Log.e("deScribe", e.getMessage() + "");
			}
			return null;
		}

		@Override
		protected void onPostExecute(String response)
		{
			progressDialog.dismiss();

			if(response.equals(""))
				response = "<no recognized text>";

			getFileStreamPath("tempSave").delete();

			Note note = new Note();
			drawingView.setDrawingCacheEnabled(true);
			drawingView.invalidate();
			Bitmap bmp = Bitmap.createBitmap(drawingView.getDrawingCache());
			drawingView.setDrawingCacheEnabled(false);
			note.image = new SerialBitmap(bmp);
			note.lines = drawingView.actionList;
			note.serverInterpretation = context.getString(R.string.recognized) + " " + response;

			SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
			int noteNum = prefs.getInt("numNotes",1);
			note.id = noteNum;

			try
			{
				File file = new File(context.getFilesDir(), noteNum+".note");
				ObjectOutputStream os = new ObjectOutputStream(new FileOutputStream(file));
				os.writeObject(note);
				os.close();
				SharedPreferences.Editor edit = prefs.edit();
				edit.putInt("numNotes", noteNum + 1);
				edit.commit();
			}
			catch(Exception ex)
			{
				Log.e("FingerNotes","couldn't save note file: " + ex.getMessage());
			}

			paletteDialog.hide();
			finish();
		}
	}
}
