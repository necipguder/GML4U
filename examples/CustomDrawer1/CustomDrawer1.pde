/**
* GML4U library
* Author Jerome Saint-Clair
* http://saint-clair.net
*
* This example shows how to iterate through GML strokes
* and points to draw GML with your own custom style
*
*/

import gml4u.brushes.*;
import gml4u.drawing.*;
import gml4u.utils.*;
import gml4u.utils.Timer;
import gml4u.model.*;

Gml gml;
Timer timer = new Timer();

int timeMax = 30;

void setup() {
  size(600, 400, P3D);
   
  gml = GmlParsingHelper.getGml(sketchPath+"/sample.gml.xml", false);
  
  GmlUtils.timeBox(gml, timeMax, true);
  timer.start();
}

void draw() {
  //randomSeed(1);
  
  background(0);
    timer.tick();  
    for (GmlStroke strok : gml.getStrokes()) {
      for (GmlPoint p : strok.getPoints()) {
        if (p.time > timer.getTime()) {
         continue; 
        }
          GmlPoint v = new GmlPoint(p);
          v.scaleSelf(width);
          noStroke();
          fill(random(255), random(255), random(255), 200);
          ellipse(v.x, v.y, random(10,20), random(10, 20));
      }
    }
}
