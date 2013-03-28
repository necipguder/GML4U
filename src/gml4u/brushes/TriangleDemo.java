package gml4u.brushes;

import gml4u.drawing.GmlStrokeDrawer;
import gml4u.model.GmlPoint;
import gml4u.model.GmlStroke;

import processing.core.PConstants;
import processing.core.PGraphics;
import toxi.geom.Vec3D;
import toxi.geom.mesh.Face;
import toxi.geom.mesh.TriangleMesh;

public class TriangleDemo  extends GmlStrokeDrawer {

	  public static final String ID = "GML4U_STYLE_TRIANGLES0000";

	  public TriangleDemo() {
	    super(ID);
	  }


	  @Override
	    public void draw(PGraphics g, GmlStroke stroke, float scale, float minTime, float maxTime) {
	    TriangleMesh mesh = buildMesh(stroke, minTime, maxTime);
	    mesh.scale(scale);

	    g.pushMatrix();
	    g.pushStyle();

	    g.beginShape(PConstants.TRIANGLES);
	    // iterate over all faces/triangles of the mesh
	    for (Face f : mesh.faces) {
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

	      GmlPoint pos = new GmlPoint();
	      Vec3D a = new Vec3D();
	      Vec3D b = new Vec3D();
	      Vec3D p = new Vec3D();
	      Vec3D q = new Vec3D();
	      float weight = 0.25f;

	      float curPoint = 1;

	      for (GmlPoint point: stroke.getPoints()) {
	        if (point.time < minTime) continue;
	        if (point.time > maxTime) break;

	        pos.set(point);

	        // define offset points for the triangle strip
	        a.set(pos);
	        b.set(pos);

	        float angle = point.rotation.z;

	        Vec3D aShift = new Vec3D(weight/2 * (float) Math.sin(Math.PI * angle - .25f), weight/2 * (float) Math.cos(Math.PI * angle - .25f), (curPoint+1) * .0005f);
	        Vec3D bShift = new Vec3D(weight/2 * (float) Math.sin(Math.PI * angle + .25f), weight/2 * (float) Math.cos(Math.PI * angle + .25f), (curPoint+1) * .0005f);

	        a.addSelf(aShift);
	        b.addSelf(bShift);

	        if (!q.isZeroVector() && !p.isZeroVector()) {
	          // add 2 faces to the mesh
	          mesh.addFace(p, b, q);
	          mesh.addFace(p, b, a);
	        }

	        // store current points for next iteration
	        p.set(a);
	        q.set(b);
	        ++curPoint;
	      }
	    }
	    mesh.computeVertexNormals();
	    return mesh;
	  }
	}
