package net.mcdermotsoft.fingernotes;

import android.content.Context;
import android.content.res.Resources;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;

public class PaletteDialog
{
	PopupWindow dialog;
	DrawingActivity context;
	private boolean isShowing = false;
	Resources r;
	View presetPressed;

	public PaletteDialog(final DrawingActivity context)
	{
		this.context = context;

		LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View pop = inflater.inflate(R.layout.color_palette_dialog, null, false);

		r = context.getResources();
		pop.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
		pop.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
		int height = pop.getMeasuredHeight();
		int width = pop.getMeasuredWidth();
		dialog = new PopupWindow(pop,width,height);

		final RelativeLayout presetLayout = (RelativeLayout) pop.findViewById(R.id.presetLayout);
		for(int i = 1; i < presetLayout.getChildCount(); i++)
		{
			FrameLayout layout = (FrameLayout) presetLayout.getChildAt(i);
			final ImageView preset = (ImageView) layout.getChildAt(1);
			preset.setOnClickListener(new OnClickListener()
			{
				public void onClick(View v)
				{
					context.drawingView.getCurrentPaint().setColor(getColor(preset));
					context.currentColor.setBackgroundColor(getColor(preset));
				}
			});

		}
	}

	private int getColor(ImageView v)
	{
		int thisColor;

		if(v.getTag() instanceof MyPaint)
			thisColor = ((MyPaint) v.getTag()).getColor();
		else
			thisColor = Integer.parseInt((String) v.getTag(), 16) + 0xFF000000;
		
		return thisColor;
	}

	public void show()
	{
		int height = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 48, r.getDisplayMetrics());
		dialog.showAtLocation(context.findViewById(R.id.window), Gravity.BOTTOM | Gravity.RIGHT, 0, height);
		setShowing(true);
	}

	public void hide()
	{
		dialog.dismiss();
		setShowing(false);
	}

	public boolean isShowing()
	{
		return isShowing;
	}

	public void setShowing(boolean isShowing)
	{
		this.isShowing = isShowing;
	}
}
