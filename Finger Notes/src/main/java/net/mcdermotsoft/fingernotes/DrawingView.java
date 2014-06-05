package net.mcdermotsoft.fingernotes;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.os.AsyncTask;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Date;

/**
 * Created by Robert McDermot on 1/22/14.
 */
public class DrawingView extends View
{
	ArrayList<LineSegment> actionList = new ArrayList<LineSegment>();
	MyPaint currentPaint = new MyPaint(), clearPaint;
	int currentColor = 0;
	DrawingActivity activity;
	DrawingView view = this;

	public DrawingView(Context context)
	{
		this(context,null);
	}

	public DrawingView(Context context, AttributeSet attrs)
	{
		this(context, attrs, 0);
	}

	public DrawingView(Context context, AttributeSet attrs, int defStyle)
	{
		super(context, attrs, defStyle);
		clearPaint = new MyPaint();
		clearPaint.setColor(Color.WHITE);
		currentPaint = new MyPaint();
		currentPaint.setColor(Color.BLACK);
		currentPaint.setStyle(Paint.Style.STROKE);
		currentPaint.setStrokeWidth(5);

		File file = getContext().getFileStreamPath("tempSave");
		if(!isInEditMode() && file.exists())
		{
			showContinueDialog();
		}
	}

	private void showContinueDialog()
	{
		AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
		builder.setMessage("Would you like to continue from your last drawing?").setPositiveButton("Yes", new DialogInterface.OnClickListener()
		{
			@SuppressWarnings("unchecked")
			public void onClick(DialogInterface dialog, int id)
			{
				try
				{
					FileInputStream fis = activity.openFileInput("tempSave");
					ObjectInputStream is = new ObjectInputStream(fis);
					actionList = (ArrayList<LineSegment>) is.readObject();
					is.close();
					view.invalidate();
				}
				catch(Exception ex)
				{
					Log.e("describe", "could not read file: " + ex.getMessage());
					ex.printStackTrace();
				}
			}
		}).setNegativeButton("No", null);

		// Create the AlertDialog object and show it
		builder.create().show();
	}

	@Override
	public boolean onTouchEvent(MotionEvent event)
	{
		super.onTouchEvent(event);

		drawLine(event);

		return true;
	}

	private void drawLine(MotionEvent event)
	{
		int action = event.getAction();
		float x = event.getX();
		float y = event.getY();

		LineSegment currentLine = null;
		if(actionList.size() > 0)
		{
			currentLine = actionList.get(actionList.size() - 1);
			if(currentLine.isDrawFinished())
				currentLine = null;
		}

		if(action == MotionEvent.ACTION_DOWN)
		{
			if(currentLine != null)
			{
				//this is probably a second finger down, ignore
				return;
			}

			LineSegment line = new LineSegment();
			line.setPaint(getCurrentPaint());
			MyPath path = new MyPath();
			path.moveTo(x,y);
			line.setPath(path);
			actionList.add(line);
			invalidate();
		}
		if(action == MotionEvent.ACTION_MOVE)
		{
			if(currentLine != null)
			{
				currentLine.getPath().lineTo(x,y);
				invalidate();
			}
		}

		if(action == MotionEvent.ACTION_UP)
		{
			if(currentLine != null)
			{
				currentLine.setDrawFinished(true);
				invalidate();
			}
			saveTempDrawing();
		}
	}

	@Override
	public void onDraw(Canvas canvas)
	{
		super.onDraw(canvas);

		canvas.drawRect(0, 0, canvas.getWidth(), canvas.getHeight(), clearPaint);

		for(LineSegment line : actionList)
		{
			canvas.drawPath(line.getPath(),line.getPaint());
		}
	}

	protected void saveTempDrawing()
	{
		new saveDrawingTask().execute();
	}

	private class saveDrawingTask extends AsyncTask<Void, Void, Void>
	{
		@Override
		protected Void doInBackground(Void... arg0)
		{
			try
			{
				FileOutputStream fos = activity.openFileOutput("tempSave", Context.MODE_PRIVATE);
				ObjectOutputStream os = new ObjectOutputStream(fos);
				os.writeObject(actionList);
				os.close();
			}
			catch(Exception ex)
			{
				Log.e("FingerNotes", "could not write file: " + ex.getMessage());
			}
			return null;
		}
	}

	public MyPaint getCurrentPaint()
	{
		return currentPaint;
	}

	public void setCurrentPaint(MyPaint paint)
	{
		currentPaint = paint;
		currentColor = currentPaint.getColor();
	}
}
