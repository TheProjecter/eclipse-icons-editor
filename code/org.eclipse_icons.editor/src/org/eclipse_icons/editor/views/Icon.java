package org.eclipse_icons.editor.views;

import org.eclipse.swt.graphics.Image;
import org.eclipse_icons.editor.utils.image.ImageProcessor;

public class Icon {
	
	public static final String CENTERED_OVERLAY_ICON = "Centered overlay icon";
	
	public static final String TOP_LEFT_CORNER_OVERLAY_ICON = "Top left corner overlay icon";
	public static final String TOP_RIGHT_CORNER_OVERLAY_ICON = "Top right corner overlay icon";
	public static final String BOTTOM_LEFT_CORNER_OVERLAY_ICON = "Bottom left corner overlay icon";
	public static final String BOTTOM_RIGHT_CORNER_OVERLAY_ICON = "Bottom right corner overlay icon";
	
	public static final String TOP_SIDE_OVERLAY_ICON = "Top side overlay icon";
	public static final String RIGHT_SIDE_OVERLAY_ICON = "Right side overlay icon";
	public static final String LEFT_SIDE_OVERLAY_ICON = "Left side overlay icon";
	public static final String BOTTOM_SIDE_OVERLAY_ICON = "Bottom side overlay icon";
	
	private String name;
	private String id;
	private IconCategory parent;
	
	public Icon(String id, String name) {
		this.name = name;
		this.id = id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getId() {
		return id;
	}
	public void setParent(IconCategory parent) {
		this.parent = parent;
	}
	public IconCategory getParent() {
		return parent;
	}
	public String toString() {
		return getName();
	}
	public Image processImage(String[] baseIcons, String[] overlayIcons) {
		return ImageProcessor.process(getId(),baseIcons,overlayIcons);
	}
}
