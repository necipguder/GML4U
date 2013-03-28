package gml4u.brushes;

import java.util.Iterator;

import gml4u.drawing.GmlStrokeDrawer;
import gml4u.model.GmlPoint;
import gml4u.model.GmlStroke;

import processing.core.PConstants;
import processing.core.PGraphics;
import toxi.geom.Vec3D;
import toxi.geom.mesh.Face;
import toxi.geom.mesh.TriangleMesh;

public class RibbonDemo extends GmlStrokeDrawer {


	  public static final String ID = "GML4U_STYLE_RIBBON0000"; 

	  /**
	   	 * MeshDemo constructor
	   	 */
	  public RibbonDemo() {
	    super(ID);
	  }

	  public void draw(PGraphics g, GmlStroke stroke, float scale, float minTime, float maxTime) {

	    TriangleMesh mesh = buildMesh(stroke, minTime, maxTime);
	    mesh.scale(scale);

	    g.pushMatrix();
	    g.pushStyle();

	    // Style
	    g.noStroke();

	    g.beginShape(PConstants.TRIANGLES);
	    // iterate over all faces/triangles of the mesh
	    //for(Face f : mesh.faces) {
	    for (Iterator<?> i=mesh.faces.iterator(); i.hasNext();) {
	      Face f=(Face)i.next();
	      if (g.is3D()) {
	        g.vertex(f.a.x, f.a.y, f.a.z);
	        g.vertex(f.b.x, f.b.y, f.b.z);
	        g.vertex(f.c.x, f.c.y, f.c.z);
	      }
	      else {
	        g.vertex(f.a.x, f.a.y);
	        g.vertex(f.b.x, f.b.y);
	        g.vertex(f.c.x, f.c.y);
	      }
	    }
	    g.endShape();

	    g.popStyle();
	    g.popMatrix();
	  }


	  // TODO move that to an Helper
	  private TriangleMesh buildMesh(GmlStroke stroke, float minTime, float maxTime) {
	    TriangleMesh mesh = new TriangleMesh("");

	    if (stroke.getPoints().size() > 0) {	

	      GmlPoint prev = new GmlPoint();
	      GmlPoint pos = new GmlPoint();
	      Vec3D a = new Vec3D();
	      Vec3D b = new Vec3D();
	      Vec3D p = new Vec3D();
	      Vec3D q = new Vec3D();
	      float weight = 0;

	      prev.set(stroke.getPoints().get(0));

	      float curPoint = 1;

	      for (GmlPoint point: stroke.getPoints()) {
	        if (point.time < minTime) continue;
	        if (point.time > maxTime) break;
	        pos.set(point);

	        // use distance to previous point as target stroke weight
	        //weight += (pos.distanceTo(prev)*4-weight)*0.1;
	        weight = 0.025f;

	        // define offset points for the triangle strip
	        a.set(pos);
	        b.set(pos);
	        a.addSelf(0, weight, curPoint * .0005f);
	        b.addSelf(0, -weight, curPoint * .0005f);

	        if (!q.isZeroVector() && !p.isZeroVector()) {
	          // add 2 faces to the mesh
	          mesh.addFace(p, b, q);
	          mesh.addFace(p, b, a);
	        }

	        // store current points for next iteration
	        prev.set(pos);
	        p.set(a);
	        q.set(b);
	        ++curPoint;
	      }
	    }
	    mesh.computeVertexNormals();
	    return mesh;
	  }
	}
