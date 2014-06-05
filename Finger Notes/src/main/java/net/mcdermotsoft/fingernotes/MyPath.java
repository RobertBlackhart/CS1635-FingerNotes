package net.mcdermotsoft.fingernotes;

import android.graphics.Matrix;
import android.graphics.Path;
import android.graphics.PathMeasure;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.ArrayList;

public class MyPath extends Path implements Serializable
{

	/**
	 * 
	 */
	private static final long serialVersionUID = 3089000372419399108L;
	private ArrayList<PathAction> actions = new ArrayList<PathAction>();
	private MyPoint startPoint;
	public ArrayList<MyPoint> points = new ArrayList<MyPoint>();

	private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException
	{
		in.defaultReadObject();
		drawThisPath();
	}

	@Override
	public void reset()
	{
		actions.clear();
		points.clear();
		super.reset();
	}
	
	@Override
	public void offset(float dx, float dy)
	{
		for(PathAction action : actions)
		{
			action.setX(action.getX()+dx);
			action.setY(action.getY()+dy);
		}
		for(MyPoint point : points)
		{
			point.setX(point.getX()+dx);
			point.setY(point.getY()+dy);
		}
		super.offset(dx, dy);
	}
	
	@Override
	public void transform(Matrix matrix)
	{		
		float[] pos = new float[2];
		PathMeasure measure = new PathMeasure((Path)this,false);
		measure.getPosTan((float) (.5*measure.getLength()), pos, null);
		float[] values = new float[9];
		matrix.getValues(values);
		
		for(int i=0; i<actions.size(); i++)
		{
			PathAction action = actions.get(i);
			MyPoint point = points.get(i);
			
			float x = (action.getX()-pos[0])*values[0]+pos[0]+values[2];
			float y = (action.getY()-pos[1])*values[4]+pos[1]+values[5];
			
			action.setX(x);
			action.setY(y);
			point.setX(x);
			point.setY(y);
		}
		
		super.reset();
		drawThisPath();
	}
	
	@Override
	public void moveTo(float x, float y)
	{
		actions.add(new ActionMove(x, y));
		points.add(new MyPoint(x,y));
		startPoint = new MyPoint(x,y);
		super.moveTo(x, y);
	}

	@Override
	public void lineTo(float x, float y)
	{
		actions.add(new ActionLine(x, y));
		points.add(new MyPoint(x,y));
		super.lineTo(x, y);
	}

	private void drawThisPath()
	{
		for(PathAction p : actions)
		{
			if(p.getType().equals(PathAction.PathActionType.MOVE_TO))
			{
				super.moveTo(p.getX(), p.getY());
			}
			else if(p.getType().equals(PathAction.PathActionType.LINE_TO))
			{
				super.lineTo(p.getX(), p.getY());
			}
		}
	}

	public MyPoint getStartPoint()
	{
		return startPoint;
	}

	public void setStartPoint(MyPoint startPoint)
	{
		this.startPoint = startPoint;
	}

	public interface PathAction
	{
		public enum PathActionType
		{
			LINE_TO, MOVE_TO
		};

		public PathActionType getType();

		public float getX();
		public float getY();
		
		public void setX(float x);
		public void setY(float y);
	}

	public class ActionMove implements PathAction, Serializable
	{
		private static final long serialVersionUID = -7198142191254133295L;

		private float x, y;

		public ActionMove(float x, float y)
		{
			setX(x);
			setY(y);
		}

		@Override
		public PathActionType getType()
		{
			return PathActionType.MOVE_TO;
		}

		@Override
		public float getX()
		{
			return x;
		}

		@Override
		public float getY()
		{
			return y;
		}
		
		@Override
		public void setX(float x)
		{
			this.x = x;
		}
		
		@Override
		public void setY(float y)
		{
			this.y = y;
		}

	}

	public class ActionLine implements PathAction, Serializable
	{
		private static final long serialVersionUID = 8307137961494172589L;

		private float x, y;

		public ActionLine(float x, float y)
		{
			setX(x);
			setY(y);
		}

		@Override
		public PathActionType getType()
		{
			return PathActionType.LINE_TO;
		}

		@Override
		public float getX()
		{
			return x;
		}

		@Override
		public float getY()
		{
			return y;
		}
		
		@Override
		public void setX(float x)
		{
			this.x = x;
		}
		
		@Override
		public void setY(float y)
		{
			this.y = y;
		}
	}
}