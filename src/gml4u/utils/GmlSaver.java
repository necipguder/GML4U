package gml4u.utils;

import gml4u.events.GmlEvent;
import gml4u.events.GmlSavingEvent;
import gml4u.model.Gml;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import java.util.logging.Level;

import processing.core.PApplet;

public class GmlSaver extends Thread {

	private static final Logger LOGGER = Logger.getLogger(GmlSaver.class.getName());
	
	private boolean running;           // Is the thread running?  Yes or no?
	private int wait;                  // How many milliseconds should we wait in between executions?
	private String threadId;           // Thread name
	private Map <String, Gml> gmlLocations = new HashMap<String, Gml>();
	private Object parent;
	private Method callback;

	/**
	 * Constructor for GmlSaver thread
	 * Checks for a callback in the meantime (gmlEvent with GmlEvent parameter)
	 * @param wait
	 * @param id
	 * @param parent
	 */
	public GmlSaver (int wait, String id, Object parent){
		try {
			// Looking for a method called "gmlEvent", with one argument of GmlEvent type
			callback = parent.getClass().getMethod("gmlEvent", new Class[] { GmlEvent.class });
		}
		catch (Exception e) {
			LOGGER.log(Level.WARNING, parent.getClass()+" shall implement a \"public void gmlEvent(GmlEvent event)\" method to be able to receive GmlEvent");
		}
		
		this.parent = parent;
		this.wait = wait;
		this.running = false;
		this.threadId = id;
		
		start();
	}

	/**
	 * Starts the thread
	 */
	public void start () {
		LOGGER.log(Level.FINEST, "Starting thread");
		if (!running) {
			running = true;
			super.start();
		}
	}

	/**
	 * Run method triggered by start()
	 */
	public void run () {
		while (running){
			try {
				if (gmlLocations.size() > 0) {

					LOGGER.log(Level.FINEST, "Start saving: "+ gmlLocations.size() +" file(s)");
					for (String location : gmlLocations.keySet()) {
						boolean successful = GmlSavingHelper.save(gmlLocations.get(location), location);
						
						if (callback != null) {
							try {
								// Call the method with this object as the argument!
								LOGGER.log(Level.FINEST, "Invoking callback");
								callback.invoke(parent, new GmlSavingEvent(location, successful) );
							}
							catch (Exception e) {
								LOGGER.log(Level.FINEST, "I couldn't invoke that method for some reason. "+e.getMessage());
							}
						}
					}
					
					LOGGER.log(Level.FINEST, "Finished saving");
				}
				gmlLocations.clear();						

				sleep((long)(wait));	
			}
			catch (Exception e) {
				LOGGER.log(Level.WARNING, e.getMessage());
			}
		}
		LOGGER.log(Level.FINEST, threadId + " thread is done!");  // The thread is done when we get to the end of run()
		quit();
	}
	
	/**
	 * Saves a GML file to root of the sketch
	 * @param gml - Gml
	 */
	public void save(Gml gml) {
		String sketchPath = ((PApplet) parent).sketchPath;
		String location = sketchPath+"/"+gml.getFileName();
		LOGGER.log(Level.FINEST, "About to save a file as" + location);
		this.gmlLocations.put(location, gml);
	}
	
	/**
	 * Saves a GML file to the given location (path shall include filename)
	 * If the folder is not found, it will attempt to create it
	 * @param gml - Gml
	 * @param location - String
	 */
	public void save(Gml gml, String location) {
		LOGGER.log(Level.FINEST, "About to save a Gml to " + location);
		this.gmlLocations.put(location, gml);
	}
	
	/**
	 * Saves a list of GML file to the given folder using the filename stored in each Gml object
	 * If the folder is not found, it will attempt to create it
	 * @param gmlList - List<Gml>
	 * @param folder - String
	 */
	public void save(List<Gml> gmlList, String folder) {
		LOGGER.log(Level.FINEST, "About to save "+gmlLocations.size()+" Gml files to" + folder);
		for (Gml gml : gmlList) {
			save(gml, folder+"/"+gml.getFileName());
		}
	}
	
	/**
	 * Saves GML files given a location for each file
	 * If the folder is not found, it will attempt to create it
	 * @param gmlLocations - Map<String, Gml>
	 */
	public void save(Map<String, Gml> gmlLocations) {
		LOGGER.log(Level.FINEST, "About to save "+gmlLocations.size() +" Gml files");
		this.gmlLocations.putAll(gmlLocations);
	}

	/**
	 * Quits the thread
	 */
	public void quit() {
		LOGGER.log(Level.FINEST, threadId + "Quitting.");
		running = false;
		interrupt(); // in case the thread is waiting. . .
	}
}