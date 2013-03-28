class CustomDrawer extends GmlStrokeDrawerBasic {

  public static final String ID = "My Custom Brush";

  public CustomDrawer() {
    super(ID);
    is3D(true);
  }

  /**
   * Implementation of the abstract method defined in GmlStrokeDrawerBasic
   */
  void draw (PGraphics g, GmlPoint prev, GmlPoint cur) {
    g.pushMatrix();
    
    float dist = cur.distanceTo(prev); 
    
    g.translate(cur.x, cur.y);
    g.rotate(dist);

    g.noStroke();
    g.fill(random(255));
    
    if (g.is3D()) {
      g.box(dist);
    }
    else {
      g.ellipse(0, 0, dist, dist);
    }
    g.popMatrix();
  }
}

