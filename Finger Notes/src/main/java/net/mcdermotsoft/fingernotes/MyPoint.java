package net.mcdermotsoft.fingernotes;

import java.io.Serializable;

public class MyPoint implements Serializable
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 3743035342466071994L;
	private float x, y;

	public MyPoint(float x, float y)
	{
		setX(x);
		setY(y);
	}
	
	public float getX() {
		return x;
	}

	public void setX(float x) {
		this.x = x;
	}

	public float getY() {
		return y;
	}

	public void setY(float y) {
		this.y = y;
	}
}
