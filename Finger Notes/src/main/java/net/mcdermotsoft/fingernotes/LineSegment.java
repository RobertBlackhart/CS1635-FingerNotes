package net.mcdermotsoft.fingernotes;

import android.graphics.DashPathEffect;
import android.graphics.Paint.Style;
import android.graphics.Path;
import android.graphics.Rect;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;

public class LineSegment implements Serializable
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 9110799860746365945L;
	private MyPaint paint;
	private MyPath path;
	private boolean drawFinished;
	private SerialBitmap brushBitmap;

	public MyPath getPath()
	{
		return path;
	}

	public void setPath(MyPath path)
	{
		this.path = path;
	}
	public MyPaint getPaint() {
		return paint;
	}

	public void setPaint(MyPaint paint) {
		this.paint = new MyPaint(paint);
	}

	public boolean isDrawFinished() {
		return drawFinished;
	}

	public void setDrawFinished(boolean drawFinished) {
		this.drawFinished = drawFinished;
	}

	public SerialBitmap getBrushBitmap() {
		return brushBitmap;
	}

	public void setBrushBitmap(SerialBitmap brushBitmap) {
		this.brushBitmap = brushBitmap;
	}
	
	private void writeObject(java.io.ObjectOutputStream out) throws IOException
	{
		out.writeObject(paint);
		out.writeBoolean(drawFinished);
		out.writeObject(path);
		out.writeObject(brushBitmap);
	}
	
	private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException
	{
		paint = (MyPaint)in.readObject();
		drawFinished = in.readBoolean();
		path = (MyPath)in.readObject();
		brushBitmap = (SerialBitmap)in.readObject();
	}
}
