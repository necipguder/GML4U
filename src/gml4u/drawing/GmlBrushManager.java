package gml4u.drawing;

import gml4u.brushes.BoxesDemo;
import gml4u.brushes.CurvesDemo;
import gml4u.brushes.MeshDemo;
import gml4u.brushes.RibbonDemo;
import gml4u.brushes.StrokeFatDemo;
import gml4u.brushes.TriangleDemo;
import gml4u.model.Gml;
import gml4u.model.GmlBrush;
import gml4u.model.GmlStroke;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import processing.core.PApplet;
import processing.core.PGraphics;

public class GmlBrushManager {

	private static final Logger LOGGER = Logger.getLogger(GmlBrushManager.class.getName());

	public static final String BRUSH_DEFAULT = CurvesDemo.ID;

	private String defaultId;
	private PApplet parent = null;

	private Map<String, GmlStrokeDrawer> drawers = new HashMap<String, GmlStrokeDrawer>();

	private static final String MISSING_PAPPLET =  "No PApplet passed to the GmlBrushManager. Use \"new GmlBrushManager(this);\" as a constructor";
	private static final String UNKNOWN_DRAWER = "Unknow drawer or no drawer found, using default instead";
	private static final String STYLE_NOT_FOUND = "Style not found, default style wasn't changed";
	private static final String DEFAULT_WASNT_CHANGED = "Returning default";
	private static final String RETURNING_DEFAULT = "Returning default";
	private static final String USING_DEFAULT = "Using default";
	private static final String REPLACING_EXISTING = "Name already exists. Replacing it";

	private static final String NULL_GML = "Gml is null";
	private static final String NULL_STROKE = "GmlStroke is null";
	private static final String NO_BRUSH = "GmlStroke has no GmlBrush";
	private static final String CANNOT_REMOVE_DEFAULT_STYLE = "Cannot remove a style when used as default style";

	/**
	 * GmlBrushManager constructor
	 */
	public GmlBrushManager() {
		init();
	}

	/**
	 * GmlBrushManager constructor
	 */
	public GmlBrushManager(PApplet p) {
		this.parent = p;
		init();
	}

	/**
	 * Init with default styles and sets defaultStyle
	 */
	private void init() {

		GmlStrokeDrawer curve = new CurvesDemo();
		add(curve);
		GmlStrokeDrawer mesh = new MeshDemo();
		add(mesh);
		GmlStrokeDrawer boxes = new BoxesDemo();
		add(boxes);
		GmlStrokeDrawer strokefat = new StrokeFatDemo();
		add(strokefat);
		GmlStrokeDrawer triangles = new TriangleDemo();
		add(triangles);
		GmlStrokeDrawer ribbon = new RibbonDemo();
		add(ribbon);

		defaultId = curve.getId();
	}

	/**
	 * Sets the default stroke drawer
	 * @param drawer - GmlStrokeDrawer
	 */
	public void setDefault(GmlStrokeDrawer drawer) {
		add(drawer);
		setDefault(drawer.getId());
	}

	/**
	 * Sets the default stroke drawer based on his styleID (if already exists)
	 * @param styleId - String
	 */
	public void setDefault(String styleId) {
		if (drawers.containsKey(styleId)) {
			defaultId = styleId;
		}
		else {
			LOGGER.log(Level.FINEST, STYLE_NOT_FOUND + ": " +DEFAULT_WASNT_CHANGED);
		}
	}

	/**
	 * Gets all drawers' Ids as a Collection
	 * @return Collection<String>
	 */
	public Collection<String> getStyles() {
		Collection<String> styles = new ArrayList<String>();
		styles.addAll(this.drawers.keySet());
		return styles;
	}

	/**
	 * Returns the amount of drawers registered
	 * @return int
	 */
	public int size() {
		return drawers.size();
	}

	/**
	 * Gets a drawer from its index
	 * @param index - int
	 * @return GmlStrokeDrawer
	 */
	public GmlStrokeDrawer get(int index) {
		if (null == drawers.get(index)) {
			LOGGER.log(Level.FINEST, "Style not found, returning default");
			return drawers.get(defaultId);
		}
		return drawers.get(index);
	}

	/**
	 * Gets a drawer from its name
	 * @param styleId - String
	 * @return GmlStrokeDrawer
	 */
	public GmlStrokeDrawer get(String styleId) {
		if (null == drawers.get(styleId)) {
			LOGGER.log(Level.FINEST, STYLE_NOT_FOUND + " : "+ RETURNING_DEFAULT);
			return drawers.get(defaultId);
		}
		return drawers.get(styleId);
	}

	/**
	 * Gets a drawer id from its index
	 * @param index - int
	 * @return String
	 */
	public String getID(int index) {
		if (index < 0 || index > drawers.size()-1) {
			LOGGER.log(Level.FINEST, STYLE_NOT_FOUND + " : " +USING_DEFAULT);
			return defaultId;
		}
		ArrayList<String> keys = new ArrayList<String>();
		keys.addAll(drawers.keySet());
		return keys.get(index);
	}

	/**
	 * Adds a new stroke drawer.
	 * If another drawer with the same name exists, it will be replaced.
	 * @param drawer - GmlStrokeDrawer
	 */
	public void add(GmlStrokeDrawer drawer) {
		if (null != drawers.get(drawer.getId())) {
			LOGGER.log(Level.FINEST, REPLACING_EXISTING + "("+drawer.getId()+")");
		}
		drawers.put(drawer.getId(), drawer);		
	}

	/**
	 * Adds a new stroke drawer and changes its ID in the same time
	 * @param id - String
	 * @param drawer - GmlStrokeDrawer
	 */
	public void add(String id, GmlStrokeDrawer drawer) {
		drawer.setId(id);
		add(drawer);
	}

	/**
	 * Removes a stroke drawer based on its id
	 * If this style is the default one, it won't be removed and you'll need to set another default one
	 * @param styleId - String
	 */
	public void remove(String styleId) {
		if (drawers.containsKey(styleId)) {
			if (!defaultId.equals(styleId)) {
				drawers.remove(styleId);
			}
			else {
				LOGGER.log(Level.FINEST, CANNOT_REMOVE_DEFAULT_STYLE);
			}
		}
		else {
			LOGGER.log(Level.FINEST, STYLE_NOT_FOUND + ": " +DEFAULT_WASNT_CHANGED);
		}
	}

	/**
	 * Returns the brush's color
	 * @param brush
	 * @return Integer
	 */
	private Integer getBrushColor(GmlBrush brush) {
		if (null != brush.getColor(GmlBrush.COLOR)) {
			return brush.getColor(GmlBrush.COLOR);
		}
		else {
			LOGGER.log(Level.FINEST, "No color defined, using default (green)");
			return (255 << 24) | (0 << 16) | (255 << 8) | 0;
		}
	}

	/**
	 * Draws each stroke according to its brush type
	 * @param g - PGraphics
	 * @param gml - Gml
	 * @param scale - float
	 */
	public void draw(PGraphics g, Gml gml, float scale) {
		draw(g, gml, scale, 0, Float.MAX_VALUE);
	}

	/**
	 * Draws each stroke according to its brush type and current time
	 * @param g - PGraphics
	 * @param gml - Gml
	 * @param scale - float
	 * @param time - float
	 */
	public void draw(PGraphics g, Gml gml, float scale, float time) {
		draw(g, gml, scale, 0, time);
	}

	/**
	 * Draws each stroke according to its brush type and current time
	 * @param g - PGraphics
	 * @param gml - Gml
	 * @param scale - float
	 * @param timeStart - float
	 * @param timeEnd - float
	 */
	public void draw(PGraphics g, Gml gml, float scale, float timeStart, float timeEnd) {
		if (null == gml) {
			LOGGER.log(Level.WARNING, NULL_GML);
		}
		for (GmlStroke currentStroke : gml.getStrokes()) {
			draw(g, currentStroke, scale, timeStart, timeEnd);
		}
	}

	/**
	 * Draws each stroke using the provided brush type
	 * @param g - PGraphics
	 * @param gml - Gml
	 * @param scale - float
	 * @param drawer - drawer id
	 */
	public void draw(PGraphics g, Gml gml, float scale, String drawer) {
		draw(g, gml, scale, 0, Float.MAX_VALUE, drawer);
	}

	/**
	 * Draws each stroke using the provided brush type and current time
	 * @param g - PGraphics
	 * @param gml - Gml
	 * @param scale - float
	 * @param time - float
	 * @param drawer - drawer id
	 */
	public void draw(PGraphics g, Gml gml, float scale, float time, String drawer) {
		draw(g, gml, scale, 0, time, drawer);
	}

	/**
	 * Draws each stroke using the provided brush type and current time
	 * @param g - PGraphics
	 * @param gml - Gml
	 * @param scale - float
	 * @param timeStart - float
	 * @param timeEnd - float
	 * @param drawer - drawer id
	 */
	public void draw(PGraphics g, Gml gml, float scale, float timeStart, float timeEnd, String drawer) {
		if (null == gml) {
			LOGGER.log(Level.WARNING, NULL_GML);
		}
		else {
			for (GmlStroke currentStroke : gml.getStrokes()) {
				draw(g, currentStroke, scale, timeStart, timeEnd, drawer);
			}
		}
	}

	/**
	 * Draws each stroke using the provided brush
	 * @param g - PGraphics
	 * @param gml - Gml
	 * @param scale - float
	 * @param brush - GmlBrush
	 */
	public void draw(PGraphics g, Gml gml, float scale, GmlBrush brush) {
		draw(g, gml, scale, 0, Float.MAX_VALUE, brush);
	}

	/**
	 * Draws each stroke using the provided brush and current time
	 * @param g - PGraphics
	 * @param gml - Gml
	 * @param scale - float
	 * @param time - float
	 * @param brush - GmlBrush
	 */
	public void draw(PGraphics g, Gml gml, float scale, float time, GmlBrush brush) {
		draw(g, gml, scale, 0, time, brush);
	}

	/**
	 * Draws each stroke using the provided brush and current time
	 * @param g - PGraphics
	 * @param gml - Gml
	 * @param scale - float
	 * @param timeStart - float
	 * @param timeEnd - float
	 * @param brush - GmlBrush
	 */
	public void draw(PGraphics g, Gml gml, float scale, float timeStart, float timeEnd, GmlBrush brush) {
		if (null == gml) {
			LOGGER.log(Level.WARNING, NULL_GML);
		}
		else {
			for (GmlStroke currentStroke : gml.getStrokes()) {
				draw(g, currentStroke, scale, timeStart, timeEnd, brush);
			}
		}
	}

	/**
	 * Draws the whole stroke according to its brush type
	 * @param g - PGraphics
	 * @param stroke - GmlStroke
	 * @param scale - float
	 */
	public void draw(PGraphics g, GmlStroke stroke, float scale) {
		draw(g, stroke, scale, 0, Float.MAX_VALUE);
	}

	/**
	 * Draws a stroke according to its brush type and current time
	 * @param g - PGraphics
	 * @param stroke - GmlStroke
	 * @param scale - float
	 * @param time - float
	 */
	public void draw(PGraphics g, GmlStroke stroke, float scale, float time) {
		draw(g, stroke, scale, 0, time);
	}

	/**
	 * Draws the stroke given a time interval and according to its brush type
	 * @param g - PGraphics
	 * @param stroke - GmlStroke
	 * @param scale - float
	 * @param timeStart - float
	 * @param timeEnd - float
	 */
	public void draw(PGraphics g, GmlStroke stroke, float scale, float timeStart, float timeEnd) {
		if (null != stroke) {
			String style = "";
			if (null == stroke.getBrush()) {
				LOGGER.log(Level.FINEST, NO_BRUSH);
			}
			else {
				style = stroke.getBrush().getStyleID();
			}
			draw(g, stroke, scale, timeStart, timeEnd, style);

		}
		else {
			LOGGER.log(Level.WARNING, NULL_STROKE);
		}
	}

	/**
	 * Draws the whole stroke using a given drawer, bypassing its inner drawer id
	 * @param g - PGraphics
	 * @param stroke - GmlStroke
	 * @param scale - float
	 * @param drawer - drawer id
	 */
	public void draw(PGraphics g, GmlStroke stroke, float scale, String drawer) {
		draw(g, stroke, scale, 0, Float.MAX_VALUE, drawer);
	}

	/**
	 * Draws the stroke given a time and drawer, bypassing its inner brush style (only)
	 * @param g - PGraphics
	 * @param stroke - GmlStroke
	 * @param scale - float
	 * @param time - float
	 * @param drawer - drawer id
	 */
	public void draw(PGraphics g, GmlStroke stroke, float scale, float time, String drawer) {
		draw(g, stroke, scale, 0, time, drawer);
	}

	/**
	 * Draws the stroke given a time interval and drawer, bypassing its inner brush style (only)
	 * @param g - PGraphics
	 * @param stroke - GmlStroke
	 * @param scale - float
	 * @param timeStart - float
	 * @param timeEnd - float
	 * @param drawer - drawer id
	 */
	public void draw(PGraphics g, GmlStroke stroke, float scale, float timeStart, float timeEnd, String drawer) {
		if (null != stroke) {
			g.pushStyle();
			Integer c = getBrushColor(stroke.getBrush());
			g.fill(c);
			g.stroke(c);
			get(drawer).draw(g, stroke, scale, timeStart, timeEnd);
			g.popStyle();
		}
		else {
			LOGGER.log(Level.WARNING, NULL_STROKE);
		}
	}

	/**
	 * Draws the whole stroke using the provided brush
	 * @param g - PGraphics
	 * @param stroke - GmlStroke
	 * @param scale - float
	 * @param brush - GmlBrush
	 */
	public void draw(PGraphics g, GmlStroke stroke, float scale, GmlBrush brush) {
		draw(g, stroke, scale, 0, Float.MAX_VALUE, brush);
	}

	/**
	 * Draws a stroke using the provided brush and current time
	 * @param g - PGraphics
	 * @param stroke - GmlStroke
	 * @param scale - float
	 * @param time - float
	 * @param brush - GmlBrush
	 */
	public void draw(PGraphics g, GmlStroke stroke, float scale, float time, GmlBrush brush) {
		draw(g, stroke, scale, 0, time, brush);
	}

	/**
	 * Draws the stroke given a time interval and using the provided brush
	 * @param g - PGraphics
	 * @param stroke - GmlStroke
	 * @param scale - float
	 * @param timeStart - float
	 * @param timeEnd - float
	 * @param brush - GmlBrush
	 */
	public void draw(PGraphics g, GmlStroke stroke, float scale, float timeStart, float timeEnd, GmlBrush brush) {
		if (null != stroke) {
			String style = "";
			if (null == brush) {
				LOGGER.log(Level.FINEST, NO_BRUSH);
			}
			else {
				style = brush.getStyleID();
			}
			Integer c = getBrushColor(brush);
			g.pushStyle();
			g.fill(c);
			g.stroke(c);
			get(style).draw(g, stroke, scale, timeStart, timeEnd);
			g.popStyle();

		}
		else {
			LOGGER.log(Level.WARNING, NULL_STROKE);
		}
	}

	/**
	 * Draws each stroke according to its brush type
	 * @param gml - Gml
	 * @param scale - float
	 */
	public void draw(Gml gml, float scale) {
		if (null != parent) {
			draw(parent.g, gml, scale, 0, Float.MAX_VALUE);
		}
		else {
			LOGGER.log(Level.WARNING, MISSING_PAPPLET);
		}
	}

	/**
	 * Draws each stroke according to its brush type and current time
	 * @param gml - Gml
	 * @param scale - float
	 * @param time - float
	 */
	public void draw(Gml gml, float scale, float time) {
		if (null != parent) {
			draw(parent.g, gml, scale, 0, time);
		}
		else {
			LOGGER.log(Level.WARNING, MISSING_PAPPLET);
		}
	}

	/**
	 * Draws each stroke according to its brush type and current time
	 * @param gml - Gml
	 * @param scale - float
	 * @param timeStart - float
	 * @param timeEnd - float
	 */
	public void draw(Gml gml, float scale, float timeStart, float timeEnd) {
		if (null != parent) {
			if (null == gml) {
				LOGGER.log(Level.WARNING, NULL_GML);
			}
			for (GmlStroke currentStroke : gml.getStrokes()) {
				draw(parent.g, currentStroke, scale, timeStart, timeEnd);
			}
		}
		else {
			LOGGER.log(Level.WARNING, MISSING_PAPPLET);
		}
	}

	/**
	 * Draws each stroke according to its brush type
	 * @param gml - Gml
	 * @param scale - float
	 */
	public void draw(Gml gml, float scale, String drawer) {
		if (null != parent) {
			draw(parent.g, gml, scale, 0, Float.MAX_VALUE, drawer);
		}
		else {
			LOGGER.log(Level.WARNING, MISSING_PAPPLET);
		}
	}

	/**
	 * Draws each stroke according to its brush type and current time
	 * @param gml - Gml
	 * @param scale - float
	 * @param time - float
	 */
	public void draw(Gml gml, float scale, float time, String drawer) {
		if (null != parent) {
			draw(parent.g, gml, scale, 0, time, drawer);
		}
		else {
			LOGGER.log(Level.WARNING, MISSING_PAPPLET);
		}
	}

	/**
	 * Draws each stroke according to its brush type and current time
	 * @param gml - Gml
	 * @param scale - float
	 * @param timeStart - float
	 * @param timeEnd - float
	 */
	public void draw(Gml gml, float scale, float timeStart, float timeEnd, String drawer) {
		if (null != parent) {
			if (null == gml) {
				LOGGER.log(Level.WARNING, NULL_GML);
			}
			for (GmlStroke currentStroke : gml.getStrokes()) {
				draw(parent.g, currentStroke, scale, timeStart, timeEnd, drawer);
			}
		}
		else {
			LOGGER.log(Level.WARNING, MISSING_PAPPLET);
		}
	}

	/**
	 * Draws each stroke using the provided brush
	 * @param gml - Gml
	 * @param scale - float
	 * @param brush - GmlBrush
	 */
	public void draw(Gml gml, float scale, GmlBrush brush) {
		if (null != parent) {
			draw(parent.g, gml, scale, 0, Float.MAX_VALUE, brush);
		}
		else {
			LOGGER.log(Level.WARNING, MISSING_PAPPLET);
		}
	}

	/**
	 * Draws each stroke according to its brush type and current time
	 * @param gml - Gml
	 * @param scale - float
	 * @param time - float
	 * @param brush - GmlBrush
	 */
	public void draw(Gml gml, float scale, float time, GmlBrush brush) {
		if (null != parent) {
			draw(parent.g, gml, scale, 0, time, brush);
		}
		else {
			LOGGER.log(Level.WARNING, MISSING_PAPPLET);
		}
	}

	/**
	 * Draws each stroke using the provided brush and current time
	 * @param gml - Gml
	 * @param scale - float
	 * @param timeStart - float
	 * @param timeEnd - float
	 * @param brush - GmlBrush
	 */
	public void draw(Gml gml, float scale, float timeStart, float timeEnd, GmlBrush brush) {
		if (null != parent) {
			if (null == gml) {
				LOGGER.log(Level.WARNING, NULL_GML);
			}
			for (GmlStroke currentStroke : gml.getStrokes()) {
				draw(parent.g, currentStroke, scale, timeStart, timeEnd, brush);
			}
		}
		else {
			LOGGER.log(Level.WARNING, MISSING_PAPPLET);
		}
	}
	/**
	 * Draws the whole stroke according to its brush type
	 * @param stroke - GmlStroke
	 * @param scale - float
	 */
	public void draw(GmlStroke stroke, float scale) {
		if (null != parent) {
			draw(parent.g, stroke, scale, 0, Float.MAX_VALUE);
		}
		else {
			LOGGER.log(Level.WARNING, MISSING_PAPPLET);
		}
	}

	/**
	 * Draws a stroke according to its brush type and current time
	 * @param stroke - GmlStroke
	 * @param scale - float
	 * @param time - float
	 */
	public void draw(GmlStroke stroke, float scale, float time) {
		if (null != parent) {
			draw(parent.g, stroke, scale, 0, time);
		}
		else {
			LOGGER.log(Level.WARNING, MISSING_PAPPLET);
		}
	}

	/**
	 * Draws the stroke given a time interval and according to its brush type
	 * @param stroke - GmlStroke
	 * @param scale - float
	 * @param timeStart - float
	 * @param timeEnd - float
	 */
	public void draw(GmlStroke stroke, float scale, float timeStart, float timeEnd) {
		if (null != parent) {
			draw(parent.g, stroke, scale, timeStart, timeEnd);
		}
		else {
			LOGGER.log(Level.WARNING, MISSING_PAPPLET);
		}
	}

	/**
	 * Draws the whole stroke using a given drawer, bypassing its inner drawer id
	 * @param stroke - GmlStroke
	 * @param scale - float
	 * @param drawer - drawer id
	 */
	public void draw(GmlStroke stroke, float scale, String drawer) {
		if (null != parent) {
			draw(parent.g, stroke, scale, 0, Float.MAX_VALUE, drawer);
		}
		else {
			LOGGER.log(Level.WARNING, MISSING_PAPPLET);
		}
	}

	/**
	 * Draws the stroke up given a time and drawer, bypassing its inner brush style
	 * @param stroke - GmlStroke
	 * @param scale - float
	 * @param time - float
	 * @param drawer - drawer id
	 */
	public void draw(GmlStroke stroke, float scale, float time, String drawer) {
		if (null != parent) {
			draw(parent.g, stroke, scale, 0, time, drawer);
		}
		else {
			LOGGER.log(Level.WARNING, MISSING_PAPPLET);
		}
	}

	/**
	 * Draws the stroke given a time interval and drawer, bypassing its inner brush style
	 * @param stroke - GmlStroke
	 * @param scale - float
	 * @param timeStart - float
	 * @param timeEnd - float
	 * @param drawer - drawer id
	 */
	public void draw(GmlStroke stroke, float scale, float timeStart, float timeEnd, String drawer) {
		if (null != parent) {
			draw(parent.g, stroke, scale, timeStart, timeEnd, drawer);
		}
		else {
			LOGGER.log(Level.WARNING, MISSING_PAPPLET);
		}
	}	

	/**
	 * Draws the whole stroke using the provided brush
	 * @param stroke - GmlStroke
	 * @param scale - float
	 * @param brush - GmlBrush
	 */
	public void draw(GmlStroke stroke, float scale, GmlBrush brush) {
		if (null != parent) {
			draw(parent.g, stroke, scale, 0, Float.MAX_VALUE, brush);
		}
		else {
			LOGGER.log(Level.WARNING, MISSING_PAPPLET);
		}
	}

	/**
	 * Draws the stroke up given a time and using the provided brush
	 * @param stroke - GmlStroke
	 * @param scale - float
	 * @param time - float
	 * @param brush - GmlBrush
	 */
	public void draw(GmlStroke stroke, float scale, float time, GmlBrush brush) {
		if (null != parent) {
			draw(parent.g, stroke, scale, 0, time, brush);
		}
		else {
			LOGGER.log(Level.WARNING, MISSING_PAPPLET);
		}
	}

	/**
	 * Draws the stroke given a time interval and using the provided brush
	 * @param stroke - GmlStroke
	 * @param scale - float
	 * @param timeStart - float
	 * @param timeEnd - float
	 * @param brush - GmlBrush
	 */
	public void draw(GmlStroke stroke, float scale, float timeStart, float timeEnd, GmlBrush brush) {
		if (null != parent) {
			draw(parent.g, stroke, scale, timeStart, timeEnd, brush);
		}
		else {
			LOGGER.log(Level.WARNING, MISSING_PAPPLET);
		}
	}	
}
