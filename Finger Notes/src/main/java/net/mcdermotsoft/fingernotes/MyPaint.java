package net.mcdermotsoft.fingernotes;

import android.graphics.LightingColorFilter;
import android.graphics.Paint;

import java.io.IOException;
import java.io.Serializable;


public class MyPaint extends Paint implements Serializable
{
	/**
	 * 
	 */
	private static final long serialVersionUID = -5618944681817429787L;
	
	public MyPaint(Paint paint)
	{
		super(paint);
	}
	
	public MyPaint()
	{
		super();
	}
	
	private void writeObject(java.io.ObjectOutputStream out) throws IOException
	{
		out.writeInt(getColor());
		out.writeBoolean(isDither());
		out.writeBoolean(isAntiAlias());
		if(getStyle().equals(Style.STROKE))
			out.writeObject("stroke");
		else
			out.writeObject("fill");
		out.writeFloat(getStrokeWidth());
		out.writeFloat(getTextSize());
		if(getColorFilter() != null)
			out.writeBoolean(true);
		else
			out.writeBoolean(false);
	}
	
	private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException
	{
		setColor(in.readInt());
		setDither(in.readBoolean());
		setAntiAlias(in.readBoolean());
		String style = (String)in.readObject();
		if(style.equals("stroke"))
			setStyle(Style.STROKE);
		else
			setStyle(Style.FILL);
		setStrokeWidth(in.readFloat());
		setTextSize(in.readFloat());
		setStrokeJoin(Join.ROUND);
		setStrokeCap(Cap.ROUND);
		if(in.readBoolean()) //if it has a colorfilter
			setColorFilter(new LightingColorFilter(getColor(), 1));
	}
}
