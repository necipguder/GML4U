/**
* GML4U library
* Author Jerome Saint-Clair
* http://saint-clair.net
*
* This example shows the simplest way to load and
* draw a GML file
*
*/

import gml4u.brushes.*;
import gml4u.drawing.*;
import gml4u.utils.*;
import gml4u.model.*;

Gml gml;
GmlBrush brush;

ArrayList<GmlBrush> brushes = new ArrayList<GmlBrush>();
int currentBrushIndex;

GmlBrushManager brushManager = new GmlBrushManager(this);



void setup() {
  size(600, 400, P3D);
   
  // Create 10 different random brushes
  for (int i=0; i<10; i++) {
    GmlBrush brush = getRandomBrush();
    brushes.add(brush);
  }
  currentBrushIndex = 0;
  
  // Load a GML file
  gml = GmlParsingHelper.getGml(sketchPath+"/sample.gml.xml", false);
}

void draw() {
    background(0);

    // Get the current brush
    GmlBrush brush = brushes.get(currentBrushIndex);
    // Draw the Gml by forcing the brush to the selected one
    brushManager.draw(gml, 600, brush);
}

void keyPressed() {
  if (keyCode == LEFT) {
    // Move to the next brush
    currentBrushIndex++;
    // If it was the last one, then use the first one of the list
    if (currentBrushIndex >= brushes.size()) {
      currentBrushIndex = 0;
    }
  }
  if (keyCode == RIGHT) {
    // Move to the previous brush
    currentBrushIndex--;
    // If it was first one, then use the last one of the list
    if (currentBrushIndex <0) {
      currentBrushIndex = brushes.size()-1;
    }    
  }
}

// Get a random brush
GmlBrush getRandomBrush() {
  // Create a new Brush
  GmlBrush randomBrush = new GmlBrush();
  // Pick a random style from the brush manager 
  String style = brushManager.getID((int) random(brushManager.getStyles().size()));
  // Set the brush's style
  randomBrush.set(GmlBrush.UNIQUE_STYLE_ID, style);
  // Pick a random color 
  int c = color(random(255), random(255), random(255));
  // Set the brush's color
  randomBrush.set(GmlBrush.COLOR, c);
  return randomBrush;
}
