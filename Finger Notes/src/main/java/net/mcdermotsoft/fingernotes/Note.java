package net.mcdermotsoft.fingernotes;

import android.graphics.Point;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Created by Robert McDermot on 1/21/14.
 */
public class Note implements Serializable
{
	SerialBitmap image;
	ArrayList<LineSegment> lines;
	int id;
	String serverInterpretation;
}
