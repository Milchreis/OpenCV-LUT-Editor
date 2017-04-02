import processing.core.*; 
import processing.data.*; 
import processing.event.*; 
import processing.opengl.*; 

import java.util.HashMap; 
import java.util.ArrayList; 
import java.io.File; 
import java.io.BufferedReader; 
import java.io.PrintWriter; 
import java.io.InputStream; 
import java.io.OutputStream; 
import java.io.IOException; 

public class OpenCV_LUT_Editor extends PApplet {


final int MIN = 0;
final int MAX = 255;
final int LUT_WINDOW_WIDTH = 509;
final int LUT_WINDOW_HEIGHT = 509;

// Application data
int barNeigbour = 3;
int colorState = 0;
int CS_ALL   = 0;
int CS_RED   = 1;
int CS_GREEN = 2;
int CS_BLUE  = 3;

// lookup table data
Integer[] lut;

// Variables for preview
PImage preview;
PImage original;
Integer[] histogram;

public void setup() {
  lut = initLut(MIN, MAX);
  textSize(10);
  colorMode(RGB, 255);
  textFont(createFont("Monospaced", 11));
}

public void settings() {
  size(LUT_WINDOW_WIDTH, LUT_WINDOW_HEIGHT);
}

public void draw() {
  background(51);
  
  // Define bar appearance
  if(colorState == CS_ALL) {
    fill(255, 207, 10);
  }
  
  if(colorState == CS_RED) {
    fill(255, 0, 0);
  }
  
  if(colorState == CS_GREEN) {
    fill(0, 255, 0);
  }
  
  if(colorState == CS_BLUE) {
    fill(0, 0, 255);
  }
  
  noStroke();
  
  // Draw each bar
  for(int i=0; i<=MAX; i++) {
    rect(i*2, LUT_WINDOW_HEIGHT-(2*lut[i]), 2, LUT_WINDOW_HEIGHT);
  }
  
  // Draw middle lines
  strokeWeight(1);
  stroke(75);
  line(0, LUT_WINDOW_HEIGHT/2, LUT_WINDOW_WIDTH, LUT_WINDOW_HEIGHT/2);
  line(LUT_WINDOW_WIDTH/2, 0, LUT_WINDOW_WIDTH/2, LUT_WINDOW_HEIGHT);
  line(0, LUT_WINDOW_HEIGHT, LUT_WINDOW_WIDTH, 0);

  // Draw help/usage
  text("Keys:\n" 
    + " 'space' - Export LUT\n"
    + " 'r'     - Reset curve\n"
    + " '+'/'-' - Brush size\n"
    + " 'c'     - Color state\n"
    + " 's'     - Smooth LUT\n"
    + " 'p'     - Preview image", 10, 15);
   
  // Draw possible position
  if(mouseX < LUT_WINDOW_HEIGHT) {
    
    stroke(255, 0, 0);
    strokeWeight(3);
    line(mouseX - 2*barNeigbour, mouseY, mouseX + 2*barNeigbour, mouseY);
  }
  
  if(preview != null) {
    fill(30);
    noStroke();
    rect(LUT_WINDOW_WIDTH, 0, LUT_WINDOW_WIDTH, LUT_WINDOW_HEIGHT);
    image(preview, LUT_WINDOW_WIDTH+10, LUT_WINDOW_HEIGHT/2 - preview.height/2);
  }
}

public void mouseDragged() {
  if(mouseX < LUT_WINDOW_WIDTH) {
    calculateBars(lut);
  }
}

public void mousePressed() {
  if(mouseX < LUT_WINDOW_WIDTH) {
    calculateBars(lut);
  }
}

public void mouseReleased() {
  if(preview != null) {
    preview = useLut(original, colorState);
  }
}

public void keyPressed() {
  if (key == ' ') {
    selectFolder("Select a folder to process:", "folderSelected");
  }
  
  if(key == 'r') {
    // Recreate a clean lookup table
    lut = initLut(MIN, MAX);
    
    // Reset the preview image to the original
    if(original != null) preview = original.copy();
  }
  
  if(key == '+' && barNeigbour <= MAX) {
    barNeigbour += 1;
  }
  
  if(key == '-' && barNeigbour > 0) {
    barNeigbour -= 1;
  }

  if(key == 'p') {
    selectInput("Select an image to preview", "fileSelected");
  }
  
  if(key == 'c') {
    colorState += 1;
    if(colorState > 3) {
      colorState = 0;
    }
    mouseReleased();
  }
  
  if(key == 's') {
    smooth(lut);
    mouseReleased();
  }
}

public void fileSelected(File selection) {
  if (selection != null) {
    // Load image
    original = loadImage(selection.getAbsolutePath());
    original.resize(LUT_WINDOW_WIDTH-20, 0);
    preview = original.copy();
        
    // Increase the width of the application window
    surface.setSize(2 * LUT_WINDOW_WIDTH, LUT_WINDOW_HEIGHT);
  }  
}

public void folderSelected(File selection) {
  if (selection != null) {
    File file = new File(selection, "lut.py");
    export(file.getAbsolutePath(), lut);
  }
}

public void export(String filename, Integer[] lut) {
  PrintWriter output = createWriter(filename);
 
  output.print("lut_sat =  np.array([");
  
  for(int i=0; i<=MAX; i++) {
    output.print(lut[i]);
         
    if(i < MAX) { 
     output.print(", ");
    }
    
    // Add a newline
    if(i > 0 && i % 10 == 0) {
      output.println("");
      output.print("\t");
    }
  }
  output.print("]).astype('uint8')");
  output.flush();
  output.close();
}

public Integer[] initLut(int min, int max) {
  Integer[] lut = new Integer[MAX+1];
  for(int i=min; i<=max; i++) {
    lut[i] = i;
  } 
  return lut;
}

public PImage useLut(PImage original, int state) {
  PImage preview = original.copy();
  preview.loadPixels();
  for (int i = 0; i < preview.pixels.length-1; i++) {
          
    int r = (int) red(preview.pixels[i]);
    int g = (int) green(preview.pixels[i]);
    int b = (int) blue(preview.pixels[i]);
    
    if(state == CS_ALL) {
      preview.pixels[i] = color(lut[r], lut[g], lut[b]);
    }
    
    if(state == CS_RED) {
      preview.pixels[i] = color(lut[r], g, b);
    }
    
    if(state == CS_GREEN) {
      preview.pixels[i] = color(r, lut[g], b);
    }
     
    if(state == CS_BLUE) {
      preview.pixels[i] = color(r, g, lut[b]);
    }
    
  }
  preview.updatePixels();
  return preview;
}

public void calculateBars(Integer[] lut) {
  int barIndex = (int) map(mouseX, 0, 509, MIN, MAX);
  barIndex = constrain(barIndex, MIN, MAX);

  int value = (int) map(LUT_WINDOW_HEIGHT-mouseY, 0, 509, MIN, MAX);
  value = constrain(value, MIN, MAX);
  
  lut[barIndex] = value;
  
  if(barNeigbour > 0) {
    for(int i=barNeigbour; i > 0; i--) {
      int neighbourLeft  = constrain(barIndex - i, MIN, MAX);
      int neighbourRight = constrain(barIndex + i, MIN, MAX);
       lut[neighbourLeft] = value;
       lut[neighbourRight] = value;
    }
  }
}

public void smooth(Integer[] lut) {
  for(int i=0; i<lut.length; i++) {    
    int neighbourL = constrain(i-1, 0, lut.length);
    int neighbourR = constrain(i+1, 0, lut.length);
    lut[i] = (neighbourL + lut[i] + neighbourR)/3;
  }
}

  static public void main(String[] passedArgs) {
    String[] appletArgs = new String[] { "OpenCV_LUT_Editor" };
    if (passedArgs != null) {
      PApplet.main(concat(appletArgs, passedArgs));
    } else {
      PApplet.main(appletArgs);
    }
  }
}
