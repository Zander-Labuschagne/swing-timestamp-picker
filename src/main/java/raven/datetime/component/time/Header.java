package raven.datetime.component.time;

import com.formdev.flatlaf.FlatClientProperties;
import net.miginfocom.swing.MigLayout;
import raven.datetime.component.time.PanelClock.SelectionView;

import javax.swing.*;

import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.text.DecimalFormat;

public class Header extends JComponent
{

	private final EventHeaderChanged headerChanged;
	private final DecimalFormat format = new DecimalFormat("00");
	private final DecimalFormat msFormat = new DecimalFormat("000");

	private Color color;

	private JToggleButton buttonHour;
	private JToggleButton buttonMinute;
	private JToggleButton buttonSecond;
	private JToggleButton buttonMilliSecond;

	private ButtonGroup group;

	public void setHour(int hour) {
		buttonHour.setText(format.format(hour));
	}

	public void setMinute(int minute) {
		buttonMinute.setText(format.format(minute));
	}

	public void setSecond(int second) {
		buttonSecond.setText(format.format(second));
	}

	public void setMilliSecond(int milliSecond) {
		buttonMilliSecond.setText(msFormat.format(milliSecond));
	}

	public void clearTime() {
		group.clearSelection();
		buttonHour.setText("--");
		buttonMinute.setText("--");
		buttonSecond.setText("--");
		buttonMilliSecond.setText("---");
		buttonHour.setSelected(true);
	}

	public void setSelectionView(SelectionView selectionView) {
		switch (selectionView) {
			case HOUR -> buttonHour.setSelected(true);
			case MINUTE -> buttonMinute.setSelected(true);
			case SECOND -> buttonSecond.setSelected(true);
			case MILLISECOND -> buttonMilliSecond.setSelected(true);
		}
	}

	public Header(EventHeaderChanged headerChanged) {
		this.headerChanged = headerChanged;
		init();
	}

	private void init() {
		setOpaque(true);
		MigLayout layout = new MigLayout("fill,insets 10", "center");
		setLayout(layout);
		add(createToolBar(), "id b1");
	}

	protected JToolBar createToolBar() {
		JToolBar toolBar = new JToolBar();
		toolBar.putClientProperty(FlatClientProperties.STYLE,
				"background:null;" + "hoverButtonGroupBackground:null");

		buttonHour = createButton();
		buttonMinute = createButton();
		buttonSecond = createButton();
		buttonMilliSecond = createButton();

		ButtonGroup group = new ButtonGroup();
		group.add(buttonHour);
		group.add(buttonMinute);
		group.add(buttonSecond);
		group.add(buttonMilliSecond);
		buttonHour.setSelected(true);

		buttonHour.addActionListener(e -> headerChanged.selectionViewChanged(SelectionView.HOUR));
		buttonMinute.addActionListener(
				e -> headerChanged.selectionViewChanged(SelectionView.MINUTE));
		buttonSecond.addActionListener(
				e -> headerChanged.selectionViewChanged(SelectionView.SECOND));
		buttonMilliSecond.addActionListener(
				e -> headerChanged.selectionViewChanged(SelectionView.MILLISECOND));

		toolBar.add(buttonHour);
		toolBar.add(createSplit());
		toolBar.add(buttonMinute);
		toolBar.add(createSplit());
		toolBar.add(buttonSecond);
		toolBar.add(createMSSplit());
		toolBar.add(buttonMilliSecond);

		return toolBar;
	}

	protected JToggleButton createButton() {
		JToggleButton button = new JToggleButton("--");
		button.putClientProperty(FlatClientProperties.STYLE,
				"" + "font:+15;" + "toolbar.margin:3,5,3,5;"
						+ "foreground:contrast($Component.accentColor,$ToggleButton.background,#fff);"
						+ "background:null;" + "toolbar.hoverBackground:null");
		return button;
	}

	protected JLabel createSplit(String splitter) {
		JLabel label = new JLabel(splitter);
		label.putClientProperty(FlatClientProperties.STYLE, "" + "font:+10;"
				+ "foreground:contrast($Component.accentColor,$Label.background,#fff)");
		return label;
	}

	protected JLabel createSplit() {
		return createSplit(":");
	}

	protected JLabel createMSSplit() {
		return createSplit(".");
	}

	/**
	 * Override this method to paint the background color
	 * Do not use the component background because the background reset while change themes
	 */
	@Override
	protected void paintComponent(Graphics g) {
		Graphics2D g2 = (Graphics2D) g.create();
		Color color = this.color;
		if (color == null) {
			color = UIManager.getColor("Component.accentColor");
		}
		g2.setColor(color);
		g2.fill(new Rectangle2D.Double(0, 0, getWidth(), getHeight()));
		g2.dispose();
		super.paintComponent(g);
	}

	public void setColor(Color color) {
		this.color = color;
	}

	/**
	 * Override this method to return the background color to the JToolBar
	 * When JToolBar use null background, so it will paint the parent background.
	 */
	@Override
	public Color getBackground() {
		if (color != null) {
			return color;
		}
		return UIManager.getColor("Component.accentColor");
	}

	protected interface EventHeaderChanged
	{
		void selectionViewChanged(SelectionView selectionView);
	}
}
