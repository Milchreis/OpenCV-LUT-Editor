
void export(String filename, Integer[] lut) {
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

Integer[] initLut(int min, int max) {
  Integer[] lut = new Integer[MAX+1];
  for(int i=min; i<=max; i++) {
    lut[i] = i;
  } 
  return lut;
}

PImage useLut(PImage original, int state) {
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

void calculateBars(Integer[] lut) {
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

void smooth(Integer[] lut) {
  for(int i=0; i<lut.length; i++) {    
    int neighbourL = constrain(i-1, 0, lut.length);
    int neighbourR = constrain(i+1, 0, lut.length);
    lut[i] = (neighbourL + lut[i] + neighbourR)/3;
  }
}
