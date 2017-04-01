
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

void setup() {
  lut = initLut(MIN, MAX);
  textSize(10);
  colorMode(RGB, 255);
  textFont(createFont("Monospaced", 11));
}

void settings() {
  size(LUT_WINDOW_WIDTH, LUT_WINDOW_HEIGHT);
}

void draw() {
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

void mouseDragged() {
  if(mouseX < LUT_WINDOW_WIDTH) {
    calculateBars(lut);
  }
}

void mousePressed() {
  if(mouseX < LUT_WINDOW_WIDTH) {
    calculateBars(lut);
  }
}

void mouseReleased() {
  if(preview != null) {
    preview = useLut(original, colorState);
  }
}

void keyPressed() {
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

void fileSelected(File selection) {
  if (selection != null) {
    // Load image
    original = loadImage(selection.getAbsolutePath());
    original.resize(LUT_WINDOW_WIDTH-20, 0);
    preview = original.copy();
        
    // Increase the width of the application window
    surface.setSize(2 * LUT_WINDOW_WIDTH, LUT_WINDOW_HEIGHT);
  }  
}

void folderSelected(File selection) {
  if (selection != null) {
    File file = new File(selection, "lut.py");
    export(file.getAbsolutePath(), lut);
  }
}
