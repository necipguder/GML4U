package gml4u.utils;

import gml4u.model.Gml;
import gml4u.model.GmlBrush;
import gml4u.model.GmlClient;
import gml4u.model.GmlConstants;
import gml4u.model.GmlEnvironment;
import gml4u.model.GmlGenericContainer;
import gml4u.model.GmlInfo;
import gml4u.model.GmlLocation;
import gml4u.model.GmlPoint;
import gml4u.model.GmlStroke;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.jdom.Document;
import org.jdom.Element;

import toxi.geom.Vec2D;
import toxi.geom.Vec3D;

public class GmlParsingHelper {

	private static final Logger LOGGER = Logger.getLogger(GmlParsingHelper.class.getName());
	
	/**
	 * Parses a Gml file given its location
	 * @param file - String
	 */
	public static Gml getGml(String file) {
		return getGml(file, true);
	}

	/**
	 * Parses a Gml file given its location and, if requested, does the appropriate<br/>
	 * normalisations to match the Gml 1.0 specs
	 * @param file - String
	 * @param normalize - boolean
	 * @return Gml
	 */
	public static Gml getGml(String file, boolean normalize) {

		LOGGER.log(Level.FINEST, "Parsing from file: "+ file);

		Gml gml = parseGml(file, true);
		gml = cleanGml(gml, normalize);
		
		return gml;

	}

	/**
	* Parses a Gml input String and normalizes it
	* 
	* @param inputString - String
	* @return Gml
	*/
	public static Gml getGmlFromString(String inputString) {
		return getGmlFromString(inputString, true);
	}

	/**
	* Parses a Gml input String, and, if requested, does the appropriate<br/>
	* normalisations to match the Gml 1.0 specs
	* 
	* @param inputString - String
	* @param normalize - boolean
	* @return Gml
	*/
	public static Gml getGmlFromString(String inputString, boolean normalize) {
		LOGGER.log(Level.FINEST, "Parsing from String");
		
		Gml gml = parseGml(inputString, false);
		gml = cleanGml(gml, normalize);
		
		return gml;
	}

	
	/**
	 * Cleans the GML and normalize it if specified
	 * @param gml - Gml
	 * @param normalize - boolean
	 * @return Gml
	 */
	public static Gml cleanGml(Gml gml, boolean normalize) {
		// Fix differences between the various Gml clients 
		GmlHomogenizer.autoFix(gml);

		if (normalize) {

			LOGGER.log(Level.FINEST, "normalize "+ normalize);
			

			// At this point all Gml points should
			// be scaled according to the screenBounds

			// Normalize to fit within 0, 0, 0 at min and 1, 1, 1 at max
			GmlUtils.normalize(gml);

			// Reorient to match up  0, -1, 0
			GmlUtils.reorient(gml, true);

			// Change the Gml client version
			gml.client.set("version", GmlConstants.DEFAULT_CLIENT_VERSION);
			// Change the Gml client name
			gml.client.set("name",  GmlConstants.DEFAULT_CLIENT_NAME);
			
			// TODO Also remove unnecessary information once normalized
		}
		return gml;
	}
	
	
	/**
	 * Parses a Gml file given its location without doing any normalization.
	 * @param file - String
	 * @return Gml
	 */
	@SuppressWarnings("unchecked")
	private static Gml parseGml(String input, boolean isFile) {
		Gml gml = null;
		
		// TODO return null if the gml is a file and its path or type is incorrect

		// TODO xsd validation ???

		// Get document
		Document document = getDocument(input, isFile);
		String root = "/*[name()='GML' or name()='gml']";

		// Get version
		Element rootNode = (Element) JDomParsingUtils.selectSingleNode(document, root);
		String version = rootNode.getAttributeValue("spec");

		// TODO parsing factory with version

		String expression = root+"/tag/header/client/child::*";
		List<Element> elements = (List<Element>) JDomParsingUtils.selectNodes(document, expression);

		gml = new Gml();

		
		// Get Client
		GmlClient client = new GmlClient();
		if (null != elements) {
			setGmlGenericContainer(elements, client);
		}
		gml.client = client;

		
		// Get Environment
		if (null == version || version.equalsIgnoreCase("0.1a") || version.equalsIgnoreCase("0.1b")) {
			expression = root+"/tag/environment/child::*";
		}
		else {
			expression = root+"/tag/header/environment/child::*";
		}

		elements = (List<Element>) JDomParsingUtils.selectNodes(document, expression);
		GmlEnvironment environment = getGmlEnvironment(elements);
		gml.environment = environment;

		// TODO loop through all tags or skip multiple tags

		// Get Drawing
		expression = root+"/tag/drawing/child::*";
		elements = (List<Element>) JDomParsingUtils.selectNodes(document, expression);

		ArrayList<GmlStroke> gmlStrokes = new ArrayList<GmlStroke>();
		gmlStrokes.addAll(getGmlDrawing(elements));
		gml.addStrokes(gmlStrokes);

		return gml;
	}

	/**
	 * Builds the XML Document based on the the input type (GML file or String)
	 * @param input
	 * @param fileInput
	 * @return
	 */
	private static Document getDocument(String input, boolean fileInput){
 
 		// Get document
		Document document = null;
		if(fileInput){
			document = JDomParsingUtils.buildDocument(input);
		}else{
			document = JDomParsingUtils.buildDocumentFromString(input);
		}
		return document;
	}

	/**
	 * Returns a GmlEnvironment based on the provided nodes (subnodes of /gml/tag/header/environment)
	 * @param elements - List<Element>
	 * @return GmlEnvironment
	 */
	@SuppressWarnings("unchecked")
	private static GmlEnvironment getGmlEnvironment(List<Element> elements) {

		GmlEnvironment environment = new GmlEnvironment(new Vec3D(1, 1, 1));

		// TODO GmlGenericContainer
		
		// loop through environment nodes
		for (Element e: elements) {
			String name = e.getName();

			if (e.getChildren().size() == 1) {
				String value = e.getValue();
				environment.set(name, value);
				LOGGER.log(Level.FINEST, name+"="+ value);				
			}
			
			// loop through sub nodes to create vectors
			else if (e.getChildren().size() >= 3) {

				Vec3D v = getGmlVec3D(e.getChildren());
				environment.set(name, v);
				LOGGER.log(Level.FINEST, name+"="+ v);				
				
				// Specific case for realScale "unit" subnode
				if (e.getName().equalsIgnoreCase("realScale")) {
					String realScaleUnit;
					try {
						realScaleUnit = e.getChild("unit").getValue();
					}
					catch (Exception ex) {
						realScaleUnit = "";
					}

					environment.set("realScaleUnit", realScaleUnit);
					LOGGER.log(Level.FINEST, "realScaleUnit"+"="+ realScaleUnit);				
				}
			}
		}

		// Force an up Vector if not defined (ie: x,y,z = 0,0,0)
		// TODO check consistency (at least one 1 and 0 for others)
		if (environment.up.x == 0 && environment.up.y == 0 && environment.up.z == 0) {
			environment.up.x = 1;
		}

		return environment;
	}

	/**
	 * Returns a list of GmlStrokes given the provided stroke nodes
	 * @param elements - List<Element>
	 * @return ArrayList<GmlStroke>
	 */
	private static ArrayList<GmlStroke> getGmlDrawing(List<Element> elements) {
		// TODO support brushes merge
		// TODO layers absolute and relative
		ArrayList<GmlStroke> list = new ArrayList<GmlStroke>();

		for (Element element: elements) {
			// Get the GmlStroke
			GmlStroke stroke = getGmlStroke(element);
			list.add(stroke);
		}
		return list;
	}

	/**
	 * Returns a GmlStroke from a given stroke element<br/>
	 * Note: the merge between two consecutive brushes is not supported
	 * @param element - Element
	 * @return GmlStroke
	 */
	@SuppressWarnings("unchecked")
	private static GmlStroke getGmlStroke(Element element) {

		// TODO brush merge only if brushes are the same kind
		// TODO have a "reset brush" parameter
		
		GmlStroke gmlStroke = new GmlStroke();

		// Get isDrawing value
		try {
			String isDrawing =  element.getAttributeValue("isDrawing");
			if (isDrawing.equalsIgnoreCase("false")) {
				gmlStroke.setIsDrawing(false);
			}
		}
		catch (Exception ex) {
		}

		try {
			String layer =  element.getAttributeValue("layer");
			gmlStroke.setLayer(Integer.parseInt(layer));
		}
		catch (Exception ex) {
			gmlStroke.setLayer(Integer.MIN_VALUE);
		}

		// Get info
		GmlInfo gmlInfo = new GmlInfo();
		Element infoElement = element.getChild("info");
		if (null != infoElement) {
			setGmlGenericContainer(infoElement.getChildren(), gmlInfo);
		}
		gmlStroke.setInfo(gmlInfo);

		// Get Brush
		GmlBrush gmlBrush = new GmlBrush();
		Element brushElement = element.getChild("brush");
		if (null != brushElement) {
			setGmlGenericContainer(brushElement.getChildren(), gmlBrush);
		}
		gmlStroke.setBrush(gmlBrush);
		
		// Get points
		List<Element> points = element.getChildren("pt");
		List<GmlPoint> gmlPoints = getGmlPoints(points);

		gmlStroke.addPoints(gmlPoints);
		return gmlStroke;
	}

	/**
	 * Returns a list of GmlPoints from a list of point elements
	 * @param elements - List<Element>
	 * @return List<GmlPoint>
	 */
	private static List<GmlPoint> getGmlPoints(List<Element> elements) {

		List<GmlPoint> pointsList = new LinkedList<GmlPoint>();
		for (Element point: elements) {
			GmlPoint gmlPoint = new GmlPoint();
			gmlPoint = getGmlPoint(point);
			pointsList.add(gmlPoint);
		}
		return pointsList;
	}

	/**
	 * Returns a GmlPoint from the given point element
	 * @param element - Element
	 * @return GmlPoint
	 */
	@SuppressWarnings("unchecked")
	private static GmlPoint getGmlPoint(Element element) {
		GmlPoint point = new GmlPoint();

		// x, y, z
		Vec3D v = getGmlVec3D(element.getChildren());
		point.set(v);
		
		// Time
		float timeVal = 0;
		try { // As gml v0.1b and v0.1c "time" element name differs ...
			Element time = element.getChild("t");
			if (null == time) {
				time = element.getChild("time");
			}
			timeVal = Float.parseFloat(time.getValue());
		}
		catch (Exception ex) {
		}
		point.time = timeVal;
		
		// Rotation
		Vec3D rot = new Vec3D();
		List<Element> rotation = element.getChildren("rot");
		if (rotation.size() > 0) {; 
			rot.set(getGmlVec3D(rotation));
		}
		point.rotation.set(rot);
		
		// Direction
		Vec3D dir = new Vec3D();
		List<Element> direction = element.getChildren("dir");
		if (rotation.size() > 0) {; 
		dir.set(getGmlVec3D(direction));
		}
		point.rotation.set(dir);
		
		// Pressure
		float presVal = GmlPoint.DEFAULT_PRESSURE;
		try {
			Element pressure = element.getChild("pres");
			if (null != pressure.getValue()) {
				presVal = Float.parseFloat(pressure.getValue());					
			}
		}
		catch (Exception ex) {
		}
		point.preasure = presVal ;

		// Thickness
		float thicknessVal = GmlPoint.DEFAULT_THICKNESS;
		try {
			Element thickness = element.getChild("thick");
			if (null != thickness.getValue()) {
				thicknessVal = Float.parseFloat(thickness.getValue());					
			}
		}
		catch (Exception ex) {
		}
		point.thickness = thicknessVal ;
		
		return point;
	}

	/**
	 * Gets the xyz values from the given elements and returns the corresponding Vec3D
	 * @param elements - List<Element>
	 * @return Vec3D
	 */
	private static Vec3D getGmlVec3D(List<Element> elements) {
		Vec3D v = new Vec3D();
		
		for (Element e : elements) {
			String name = e.getName();
			String value = e.getValue();

			try {
				if (name.equalsIgnoreCase("x")) {
					v.x = Float.parseFloat(value);
				}
				else if (name.equalsIgnoreCase("y")) {
					v.y = Float.parseFloat(value);
				}
				else if (name.equalsIgnoreCase("z")) {
					v.z = Float.parseFloat(value);
				}
			}
			catch (NumberFormatException ex) {
				LOGGER.log(Level.WARNING, ex.getMessage());
			}
		}
		return v;
	}

	/**
	 * Gets the xy values from the provided elements and returns the corresponding Vec2D
	 * @param elements - List<Element>
	 * @return Vec2D
	 */
	private static Vec2D getGmlVec2D(List<Element> elements) {
		Vec2D v = new Vec2D();
		
		for (Element e : elements) {
			String name = e.getName();
			String value = e.getValue();

			if (name.equalsIgnoreCase("x")) {
				v.x = Float.parseFloat(value);
			}
			else if (name.equalsIgnoreCase("y")) {
				v.y = Float.parseFloat(value);
			}
		}
		return v;
	}

	/**
	 * Gets the color values from the provided rgb(a) elements and returns the corresponding TColor
	 * @param elements - List<Element>
	 * @return Color
	 */
	private static Integer getGmlColor(List<Element> elements) {
		
		int red = 0;
		int green = 255;
		int blue = 0;
		int alpha = 255;
		
		for (Element e : elements) {
			String name = e.getName();
			String value = e.getValue();

			if (name.equalsIgnoreCase("r")) {
				red = Integer.parseInt(value);
			}
			else if (name.equalsIgnoreCase("g")) {
				green = Integer.parseInt(value);
			}
			else if (name.equalsIgnoreCase("b")) {
				blue = Integer.parseInt(value);
			}
			else if (name.equalsIgnoreCase("a")) {
				alpha = Integer.parseInt(value);
			}
		}
		
		Integer color = (alpha << 24) | (red << 16) | (green << 8) | blue;
		return color;
	}

	/**
	 * Gets the xyz values from the given elements and returns the corresponding Vec3D
	 * @param elements - List<Element>
	 * @return GmlLocation
	 */
	private static GmlLocation getGmlLocation(List<Element> elements) {
		GmlLocation loc = new GmlLocation();
		
		for (Element e : elements) {
			String name = e.getName();
			String value = e.getValue();

			try {
				if (name.equalsIgnoreCase("alt")) {
					loc.setLat(Long.parseLong(value));
				}
				else if (name.equalsIgnoreCase("y")) {
					loc.setLon(Long.parseLong(value));
				}
				else if (name.equalsIgnoreCase("z")) {
					loc.setAlt(Long.parseLong(value));
				}
			}
			catch (NumberFormatException ex) {
				LOGGER.log(Level.WARNING, ex.getMessage());
			}
		}
		return loc;
	}

	/**
	 * Populates a GmlGenericContainer from the given children elements
	 * @param elements - List<Element>
	 * @param container - GmlGenericContainer
	 */
	private static void setGmlGenericContainer(List<Element> elements, GmlGenericContainer container) {
	
		if (null != elements && elements.size() > 0) {
			Map<String, Object> map = new HashMap<String, Object>();
			
			// Loop through subnodes
			for(Element e: elements) {
				String name = e.getName();
		
				Object o = getObject(e);
				if (null != o) {
					map.put(name, o);
				}
			}
			container.setParameters(map);
		}
		else {
			LOGGER.log(Level.WARNING, "Doing nothing. Reason: elements is null or empty");
		}
	}

	/**
	 * Analyzes the type of element to return the appropriate Java Object
	 * @param element - Element
	 * @return Object
	 */
	@SuppressWarnings("unchecked")
	private static Object getObject(Element element) {
				
		int nbElements = element.getChildren().size();
		
		if (nbElements == 0) {
			try {
				float value = Float.parseFloat(element.getValue());
				 // Float
				return value;
			}
			catch (NumberFormatException e) {
				// TODO Integer
				
				 // String
				return element.getValue();
			}		
		}
		else {
			if (isVec3D((List<Element>) element.getChildren())) {
				Vec3D v = getGmlVec3D((List<Element>) element.getChildren());
				return v;
			}
			else if (isVec2D((List<Element>) element.getChildren())) {
				Vec2D v = getGmlVec2D((List<Element>) element.getChildren());
				return v;
			}
			else if (isColor((List<Element>) element.getChildren())) {
				Integer c = getGmlColor((List<Element>) element.getChildren());
				return c;
			}
			else if (isLocation((List<Element>) element.getChildren())) {
				GmlLocation loc = getGmlLocation((List<Element>) element.getChildren());
				return loc;
			}
			else {
				// Nothing found, returning null
				LOGGER.log(Level.WARNING, "Unrecognized element type: "+element.toString());
				return null;
			}
		}
	}

	/**
	 * Checks if the provided element is a Vec3D
	 * @param elements - List<Element>
	 * @return boolean 
	 */
	private static boolean isVec3D(List<Element> elements) {
		String result = "";
		for (Element e : elements) {
			result += "_;_"+e.getName();
		}
		// TODO better regex if xyz order is changed
		// Or sort the elements names prior to check
		String Vec3DRegex = "(.*)_;_x(.*)_;_y(.*)_;_z(.*)";
		if (result.matches(Vec3DRegex)) {
			return true;
		}
		return false;
	}
	
	/**
	 * Checks if the provided element is a Vec2D
	 * @param elements - List<Element>
	 * @return boolean 
	 */
	private static boolean isVec2D(List<Element> elements) {
		String result = "";
		for (Element e : elements) {
			result += "_;_"+e.getName();
		}
		// TODO better regex if xy order is changed
		// Or sort the elements names prior to check
		String Vec2DRegex = "(.*)_;_x(.*)_;_y(.*)";
		if (result.matches(Vec2DRegex)) {
			return true;
		}
		return false;
	}

	/**
	 * Checks if the provided element is a Color
	 * @param elements - List<Element>
	 * @return boolean 
	 */
	private static boolean isColor(List<Element> elements) {
		String result = "";
		for (Element e : elements) {
			result += "_;_"+e.getName();
		}
		// TODO better regex if rgba order is changed
		// Or sort the elements names prior to check
		String ColorRegex = "(.*)_;_r(.*)_;_g(.*)_;_b(.*)(_;_a)?(.*)";
		if (result.matches(ColorRegex)) {
			return true;
		}
		return false;
	}
		
	
	/**
	 * Checks if the provided element is a Location
	 * @param elements - List<Element>
	 * @return boolean 
	 */
	private static boolean isLocation(List<Element> elements) {
		String result = "";
		for (Element e : elements) {
			result += "_;_"+e.getName();
		}
		// TODO better regex if lat,lon,alt order is changed
		// Or sort the elements names prior to check
		String LocationRegex = "(.*)_;_(lon|lat)(.*)_;_(lat|lon)(.*)(_;_alt)?(.*)";
		if (result.matches(LocationRegex)) {
			return true;
		}
		return false;
	}
}