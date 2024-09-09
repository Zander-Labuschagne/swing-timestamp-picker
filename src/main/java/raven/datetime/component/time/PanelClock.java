package raven.datetime.component.time;

import com.formdev.flatlaf.FlatClientProperties;
import com.formdev.flatlaf.FlatLaf;
import com.formdev.flatlaf.ui.FlatUIUtils;
import com.formdev.flatlaf.util.ColorFunctions;
import com.formdev.flatlaf.util.UIScale;

import javax.swing.*;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.*;

public class PanelClock extends JPanel
{

	public enum SelectionView
	{
		HOUR, MINUTE, SECOND, MILLISECOND
	}

	private final EventClockChanged clockChanged;
	private SelectionView selectionView = SelectionView.HOUR;
	private int hour = -1;
	private int minute = -1;
	private int second = -1;
	private int milliSecond = -1;

	//  graphics option
	private AnimationChange animationChange;
	private static final int MARGIN_OUTER_CIRCLE = 20;
	private static final int MARGIN_INNER_CIRCLE = 50;
	private Color color;

	//  public method

	public void setHourAndFix(int hour) {
		if (hour == 24) {
			hour = 0;
		}
		setHour(hour);
	}

	public void setHour(int hour) {
		if (this.hour != hour) {
			this.hour = hour;
			if (selectionView == SelectionView.HOUR) {
				animationChange.set(getAngleOf(hour, selectionView), getTargetMargin());
			}
			clockChanged.hourChanged(hour);
			repaint();
		}
	}

	public void setMinute(int minute) {
		if (this.minute != minute) {
			this.minute = minute;
			if (selectionView == SelectionView.MINUTE) {
				if (hour == -1) {
					setHour(12);
				}
				animationChange.set(getAngleOf(minute, selectionView), getTargetMargin());
			}
			clockChanged.minuteChanged(minute);
			repaint();
		}
	}

	public void setSecond(int second) {
		if (this.second != second) {
			this.second = second;
			if (selectionView == SelectionView.SECOND) {
				if (minute == -1) {
					setMinute(60);
				}
				animationChange.set(getAngleOf(second, selectionView), getTargetMargin());
			}
			clockChanged.secondChanged(second);
			repaint();
		}
	}

	public void setMilliSecond(int milliSecond) {
		if (this.milliSecond != milliSecond) {
			this.milliSecond = milliSecond;
			if (selectionView == SelectionView.MILLISECOND) {
				if (second == -1) {
					setSecond(60);
				}
				animationChange.set(getAngleOf(milliSecond, selectionView), getTargetMargin());
			}
			clockChanged.milliSecondChanged(milliSecond);
			repaint();
		}
	}

	public void setSelectionView(SelectionView selectionView) {
		if (this.selectionView != selectionView) {
			this.selectionView = selectionView;
			repaint();
			runAnimation();
		}
	}

	public int getHour() {
		return hour;
	}

	public int getMinute() {
		return minute;
	}

	public int getSecond() {
		return second;
	}

	public int getMilliSecond() {
		return milliSecond;
	}

	public PanelClock(EventClockChanged clockChanged) {
		this.clockChanged = clockChanged;
		init();
	}

	private void init() {
		animationChange = new AnimationChange(this);
		putClientProperty(FlatClientProperties.STYLE, "border:5,15,5,15;" + "background:null;"
				+ "foreground:contrast($Component.accentColor,$Panel.background,#fff)");
		MouseAdapter mouseAdapter = new MouseAdapter()
		{
			@Override
			public void mousePressed(MouseEvent e) {
				mouseChanged(e);
			}

			@Override
			public void mouseReleased(MouseEvent e) {
				switch (selectionView) {
					case HOUR -> {
						selectionView = SelectionView.MINUTE;
						clockChanged.selectionViewChanged(SelectionView.MINUTE);
						runAnimation();
						repaint();
					}
					case MINUTE -> {
						selectionView = SelectionView.SECOND;
						clockChanged.selectionViewChanged(SelectionView.SECOND);
						runAnimation();
						repaint();
					}
					case SECOND -> {
						selectionView = SelectionView.MILLISECOND;
						clockChanged.selectionViewChanged(SelectionView.MILLISECOND);
						runAnimation();
						repaint();
					}
					default -> {
						// Do nothing
					}
				}
			}

			@Override
			public void mouseDragged(MouseEvent e) {
				mouseChanged(e);
			}

			private void mouseChanged(MouseEvent e) {
				switch (selectionView) {
					case HOUR:
						int h = getValueOf(e.getPoint(), selectionView);
						setHour(h);
						break;
					case MINUTE:
						int m = getValueOf(e.getPoint(), selectionView);
						setMinute(m);
						break;
					case SECOND:
						int s = getValueOf(e.getPoint(), selectionView);
						setSecond(s);
						break;
					case MILLISECOND:
						int ms = getValueOf(e.getPoint(), selectionView);
						setMilliSecond(ms);
						break;
				}
			}
		};
		addMouseListener(mouseAdapter);
		addMouseMotionListener(mouseAdapter);
	}

	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		Graphics2D g2 = (Graphics2D) g.create();
		FlatUIUtils.setRenderingHints(g2);
		Insets insets = getInsets();
		int width = getWidth() - (insets.left + insets.right);
		int height = getHeight() - (insets.top + insets.bottom);

		int size = Math.min(width, height);
		g2.translate(insets.left, insets.top);
		int x = (width - size) / 2;
		int y = (height - size) / 2;

		//  create clock background
		g2.setColor(getClockBackground());
		g2.fill(new Ellipse2D.Double(x, y, size, size));

		//  create selection
		paintSelection(g2, x, y, size);

		//  create clock number
		paintClockNumber(g2, x, y, size);
		g2.dispose();
	}

	protected void paintSelection(Graphics2D g2, int x, int y, int size) {
		AffineTransform tran = g2.getTransform();
		size = size / 2;
		final float margin = UIScale.scale(animationChange.getMargin());
		float centerSize = UIScale.scale(8f);
		float lineSize = UIScale.scale(3);
		float selectSize = UIScale.scale(25f);
		float unselectSize = UIScale.scale(4);
		float lineHeight = size - margin;
		Area area = new Area(
				new Ellipse2D.Float(x + size - (centerSize / 2), y + size - (centerSize / 2),
						centerSize, centerSize));
		if ((selectionView == SelectionView.HOUR && hour != -1) || (
				selectionView == SelectionView.MINUTE && minute != -1) || (
				selectionView == SelectionView.SECOND && second != -1) || (
				selectionView == SelectionView.MILLISECOND && milliSecond != -1)) {
			area.add(new Area(
					new RoundRectangle2D.Float(x + size - (lineSize / 2), y + margin, lineSize,
							lineHeight, lineSize, lineSize)));
			area.add(new Area(
					new Ellipse2D.Float(x + size - (selectSize / 2), y + margin - selectSize / 2,
							selectSize, selectSize)));
			if ((selectionView == SelectionView.MINUTE && !animationChange.isRunning() && (
					minute % 5 != 0)) || (selectionView == SelectionView.SECOND
					&& !animationChange.isRunning() && (second % 5 != 0)) || (
					selectionView == SelectionView.MILLISECOND && !animationChange.isRunning() && (
							milliSecond % 100 != 0))) {
				area.subtract(new Area(new Ellipse2D.Float(x + size - (unselectSize / 2),
						y + margin - unselectSize / 2, unselectSize, unselectSize)));
			}
		}
		g2.setColor(getSelectedColor());
		float angle = animationChange.getAngle();
		g2.rotate(Math.toRadians(angle), x + size, y + size);
		g2.fill(area);
		g2.setTransform(tran);
	}

	protected void paintClockNumber(Graphics2D g2, int x, int y, int size) {
		int increment = switch (selectionView) {
			case HOUR -> 1;
			case MINUTE, SECOND -> 5;
			case MILLISECOND -> 100;
		};
		int sectors = switch (selectionView) {
			case HOUR, MINUTE, SECOND -> 12;
			case MILLISECOND -> 10;
		};
		paintClockNumber(g2, x, y, size, MARGIN_OUTER_CIRCLE, 0, increment, sectors);
		if (selectionView == SelectionView.HOUR) {
			paintClockNumber(g2, x, y, size, MARGIN_INNER_CIRCLE, 12, 1, sectors);
		}
	}

	protected void paintClockNumber(Graphics2D g2, int x, int y, int size, int margin, int start,
			int add, int sectorCount) {
		final int mg = UIScale.scale(margin);
		float center = size / 2f;
		float angle = 360f / sectorCount;
		for (int i = 1; i <= sectorCount; i++) {
			float ag = angle * i - 90;
			int num = fixHour((start + i * add), selectionView);
			float nx = (float) (center + (Math.cos(Math.toRadians(ag)) * (center - mg)));
			float ny = (float) (center + (Math.sin(Math.toRadians(ag)) * (center - mg)));
			paintNumber(g2, x + nx, y + ny, fixNumberAndToString(num), isSelected(num));
		}
	}

	protected void paintNumber(Graphics2D g2, float x, float y, String num, boolean isSelected) {
		FontMetrics fm = g2.getFontMetrics();
		Rectangle2D rec = fm.getStringBounds(num, g2);
		x -= rec.getWidth() / 2f;
		y -= rec.getHeight() / 2f;
		if (isSelected) {
			g2.setColor(getSelectedForeground());
		}
		else {
			g2.setColor(UIManager.getColor("Panel.foreground"));
		}
		g2.drawString(num, x, y + fm.getAscent());
	}

	protected Color getClockBackground() {
		if (FlatLaf.isLafDark()) {
			return ColorFunctions.lighten(getBackground(), 0.03f);
		}
		else {
			return ColorFunctions.darken(getBackground(), 0.03f);
		}
	}

	protected boolean isSelected(int num) {
		return switch (selectionView) {
			case HOUR -> num == hour;
			case MINUTE -> num == minute;
			case SECOND -> num == second;
			case MILLISECOND -> num == milliSecond;
		};
	}

	protected Color getSelectedColor() {
		if (color != null) {
			return color;
		}
		return UIManager.getColor("Component.accentColor");
	}

	protected Color getSelectedForeground() {
		return getForeground();
	}

	/**
	 * Convert angle to hour or minute base on the hourView
	 * Return value hour or minute
	 */
	private int getValueOf(float angle, SelectionView selectionView) {
		int f = switch (selectionView) {
			case HOUR -> 12;
			case MINUTE, SECOND -> 60;
			case MILLISECOND -> 1000;
		};

		float ag = angle / 360;
		int value = (int) (ag * f);

		return switch (selectionView) {
			case HOUR -> value == 0 ? 12 : value;
			case MINUTE, SECOND -> value == 60 ? 0 : value;
			case MILLISECOND -> value == 1000 ? 0 : value;
		};
	}

	/**
	 * Convert point location to the value hour or minute base on the {@code selectionView}
	 * Return value hour, minute, second or millisecond.
	 */
	private int getValueOf(Point point, SelectionView selectionView) {
		float f = switch (selectionView) {
			case HOUR -> 360f / 12f / 2f;
			case MINUTE, SECOND -> 360f / 60f / 2f;
			case MILLISECOND -> 360f / 1000f / 2f;
		};

		float angle = getAngleOf(point) + f;
		int value = getValueOf(angle, selectionView);

		if (selectionView == SelectionView.HOUR && is24hourSelect(point)) {
			return fixHour(value + 12, SelectionView.HOUR);
		}
		else {
			return value;
		}
	}

	private boolean is24hourSelect(Point point) {
		Insets insets = getInsets();
		int width = getWidth() - (insets.left + insets.right);
		int height = getHeight() - (insets.top + insets.bottom);
		int size = Math.min(width, height) / 2;
		int distanceTarget = (size - UIScale.scale(MARGIN_OUTER_CIRCLE + 20));
		int centerX = insets.left + size;
		int centerY = insets.top + size;
		double distance = Math.sqrt(
				Math.pow((point.x - centerX), 2) + Math.pow((point.y - centerY), 2));
		return distance < distanceTarget;
	}

	/**
	 * Convert hour, minute, second or millisecond to the angle base on the hourView
	 * Return angle vales
	 */
	private float getAngleOf(int number, SelectionView selectionView) {
		float f = switch (selectionView) {
			case HOUR -> 12f;
			case MINUTE, SECOND -> 60f;
			case MILLISECOND -> 1000f;
		};

		float ag = 360f / f;
		return fixAngle(ag * number);
	}

	/**
	 * Convert point location to angle
	 * Return angle
	 */
	private float getAngleOf(Point point) {
		Insets insets = getInsets();
		int width = getWidth() - (insets.left + insets.right);
		int height = getHeight() - (insets.top + insets.bottom);
		float centerX = insets.left + width / 2f;
		float centerY = insets.top + height / 2f;
		float x = point.x - centerX;
		float y = point.y - centerY;
		double angle = Math.toDegrees(Math.atan2(y, x)) + 90;
		if (angle < 0) {
			angle += 360;
		}
		return (float) angle;
	}

	/**
	 * Make the angle is between 0 and 360-1
	 */

	private float fixAngle(float angle) {
		if (angle > 360) {
			angle -= 360;
		}
		if (angle == 360) {
			return 0;
		}
		return angle;
	}

	/**
	 * Fix hour or minute base on the hourView
	 * If hour ( return 0 to 23 )
	 * If minute ( return 0 to 59 )
	 * If second ( return 0 to 59 )
	 * If millisecond ( return 0 to 999 )
	 */
	private int fixHour(int value, SelectionView selectionView) {
		return switch (selectionView) {
			case HOUR -> value == 24 ? 0 : value;
			case MINUTE, SECOND -> value == 60 ? 0 : value;
			case MILLISECOND -> value == 1000 ? 0 : value;
		};
	}

	private String fixNumberAndToString(int num) {
		return switch (selectionView) {
			case HOUR, MINUTE, SECOND -> num == 0 ? "00" : num + "";
			case MILLISECOND -> num == 0 ? "000" : num + "";
		};
	}

	private boolean is24hour() {
		return (hour == 0 || hour > 12);
	}

	private int getTargetMargin() {
		return is24hour() && selectionView == SelectionView.HOUR ?
				MARGIN_INNER_CIRCLE :
				MARGIN_OUTER_CIRCLE;
	}

	/**
	 * Start animation selection change
	 */
	private void runAnimation() {
		float angleTarget = getAngleOf(switch (selectionView) {
			case HOUR -> hour;
			case MINUTE -> minute;
			case SECOND -> second;
			case MILLISECOND -> milliSecond;
		}, selectionView);
		float marginTarget = getTargetMargin();
		animationChange.start(angleTarget, marginTarget);
	}

	public void setColor(Color color) {
		this.color = color;
	}

	protected interface EventClockChanged
	{
		void hourChanged(int hour);

		void minuteChanged(int minute);

		void secondChanged(int second);

		void milliSecondChanged(int milliSecond);

		void selectionViewChanged(SelectionView selectionView);
	}
}
