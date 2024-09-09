package raven.datetime.component.time;

import com.formdev.flatlaf.FlatClientProperties;
import com.formdev.flatlaf.extras.FlatSVGIcon;
import net.miginfocom.swing.MigLayout;
import raven.datetime.component.time.PanelClock.SelectionView;
import raven.datetime.util.InputUtils;
import raven.datetime.util.Utils;

import javax.swing.*;

import java.awt.*;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class TimePicker extends JPanel
{

	private final DateTimeFormatter format = DateTimeFormatter.ofPattern("HH:mm:ss.SSS");
	private final List<TimeSelectionListener> events = new ArrayList<>();
	private TimeSelectionListener timeSelectionListener;
	private InputUtils.ValueCallback valueCallback;
	private JFormattedTextField editor;
	private Icon editorIcon;
	private JPopupMenu popupMenu;
	private MigLayout layout;
	private Color color;
	private LookAndFeel oldThemes = UIManager.getLookAndFeel();
	private JButton editorButton;
	private LocalTime oldSelectedTime;

	private Header header;
	private PanelClock panelClock;

	public void setEditor(JFormattedTextField editor) {
		if (editor != this.editor) {
			if (this.editor != null) {
				uninstallEditor(this.editor);
			}
			if (editor != null) {
				installEditor(editor);
			}
			this.editor = editor;
		}
	}

	public void showPopup() {
		if (popupMenu == null) {
			popupMenu = new JPopupMenu();
			popupMenu.putClientProperty(FlatClientProperties.STYLE, "borderInsets:1,1,1,1");
			popupMenu.add(this);
		}
		if (UIManager.getLookAndFeel() != oldThemes) {
			// Component in popup not update UI when change themes, so need to update when popup show
			SwingUtilities.updateComponentTreeUI(popupMenu);
			oldThemes = UIManager.getLookAndFeel();
		}
		Point point = Utils.adjustPopupLocation(popupMenu, editor);
		popupMenu.show(editor, point.x, point.y);
	}

	public void closePopup() {
		if (popupMenu != null) {
			popupMenu.setVisible(false);
			repaint();
		}
	}

	public Color getColor() {
		return color;
	}

	public void setColor(Color color) {
		this.color = color;
		header.setColor(color);
		panelClock.setColor(color);
	}

	public Icon getEditorIcon() {
		return editorIcon;
	}

	public void setEditorIcon(Icon editorIcon) {
		this.editorIcon = editorIcon;
		if (editorButton != null) {
			editorButton.setIcon(editorIcon);
		}
	}

	public TimePicker() {
		init();
	}

	private void init() {
		putClientProperty(FlatClientProperties.STYLE,
				"[light]background:darken($Panel.background,2%);"
						+ "[dark]background:lighten($Panel.background,2%);");
		layout = new MigLayout("wrap,fill,insets 3", "fill", "fill");
		setLayout(layout);
		header = new Header(getEventHeader());
		panelClock = new PanelClock(getEventClock());
		add(header, "width 120:120");
		add(panelClock, "width 230:230, height 230:230");
	}

	/**
	 * Set time to current local time
	 */
	public void now() {
		setSelectedTime(LocalTime.now());
	}

	public void setSelectedTime(LocalTime time) {
		int hour = time.getHour();
		int minute = time.getMinute();
		int second = time.getSecond();
		int milliSecond = (time.getNano() / 1_000_000);
		panelClock.setMilliSecond(milliSecond);
		panelClock.setSecond(second);
		panelClock.setMinute(minute);
		panelClock.setHourAndFix(hour);
	}

	public void clearSelectedTime() {
		panelClock.setMilliSecond(-1);
		panelClock.setSecond(-1);
		panelClock.setMinute(-1);
		panelClock.setHour(-1);
		panelClock.setSelectionView(SelectionView.HOUR);
		header.clearTime();
	}

	public boolean isTimeSelected() {
		return panelClock.getHour() != -1 && panelClock.getMinute() != -1
				&& panelClock.getSecond() != -1 && panelClock.getMilliSecond() != -1;
	}

	public LocalTime getSelectedTime() {
		int hour = panelClock.getHour();
		int minute = panelClock.getMinute();
		int second = panelClock.getSecond();
		int milliSecond = panelClock.getMilliSecond();
		if (!isTimeSelected()) {
			return null;
		}
		return LocalTime.of(hour, minute, second, milliSecond * 1_000_000);
	}

	public String getSelectedTimeAsString() {
		if (isTimeSelected()) {
			return format.format(getSelectedTime());
		}
		else {
			return null;
		}
	}

	public void addTimeSelectionListener(TimeSelectionListener event) {
		events.add(event);
	}

	public void removeTimeSelectionListener(TimeSelectionListener event) {
		events.remove(event);
	}

	public void removeAllTimeSelectionListener() {
		events.clear();
	}

	private void installEditor(JFormattedTextField editor) {
		JToolBar toolBar = new JToolBar();
		editorButton = new JButton(editorIcon != null ?
				editorIcon :
				new FlatSVGIcon("raven/datetime/icon/clock.svg", 0.8f));
		toolBar.add(editorButton);
		editorButton.addActionListener(e -> showPopup());
		InputUtils.useTimeInput(editor, true, getValueCallback());
		editor.putClientProperty(FlatClientProperties.TEXT_FIELD_TRAILING_COMPONENT, toolBar);
		addTimeSelectionListener(getTimeSelectionListener());
	}

	private void uninstallEditor(JFormattedTextField editor) {
		if (editor != null) {
			editorButton = null;
			InputUtils.removePropertyChange(editor);
			if (timeSelectionListener != null) {
				removeTimeSelectionListener(timeSelectionListener);
			}
		}
	}

	private InputUtils.ValueCallback getValueCallback() {
		if (valueCallback == null) {
			valueCallback = value -> {
				if (value == null && isTimeSelected()) {
					clearSelectedTime();
				}
				else {
					if (value != null && !value.equals(getSelectedTimeAsString())) {
						LocalTime time = InputUtils.stringToTime(value.toString());
						if (time != null) {
							setSelectedTime(time);
						}
					}
				}
			};
		}
		return valueCallback;
	}

	private TimeSelectionListener getTimeSelectionListener() {
		if (timeSelectionListener == null) {
			timeSelectionListener = timeEvent -> {
				if (isTimeSelected()) {
					String value;
					value = format.format(getSelectedTime());
					if (!editor.getText().equalsIgnoreCase(value)) {
						editor.setValue(value);
					}
				}
				else {
					editor.setValue(null);
				}
			};
		}
		return timeSelectionListener;
	}

	private void runEventTimeChanged() {
		if (events.isEmpty()) {
			return;
		}
		LocalTime time = getSelectedTime();
		if ((time == null && oldSelectedTime == null)) {
			return;
		}
		else if (time != null && oldSelectedTime != null) {
			if (time.equals(oldSelectedTime)) {
				return;
			}
		}
		oldSelectedTime = time;
		EventQueue.invokeLater(() -> {
			for (TimeSelectionListener event : events) {
				event.timeSelected(new TimeEvent(this));
			}
		});
	}

	private Header.EventHeaderChanged getEventHeader() {
		return selectionView -> panelClock.setSelectionView(selectionView);
	}

	private PanelClock.EventClockChanged getEventClock() {
		return new PanelClock.EventClockChanged()
		{
			@Override
			public void hourChanged(int hour) {
				header.setHour(hour);
				runEventTimeChanged();
			}

			@Override
			public void minuteChanged(int minute) {
				header.setMinute(minute);
				runEventTimeChanged();
			}

			@Override
			public void secondChanged(int second) {
				header.setSecond(second);
				runEventTimeChanged();
			}

			@Override
			public void milliSecondChanged(int milliSecond) {
				header.setMilliSecond(milliSecond);
				runEventTimeChanged();
			}

			@Override
			public void selectionViewChanged(SelectionView selectionView) {
				header.setSelectionView(selectionView);
			}
		};
	}
}
