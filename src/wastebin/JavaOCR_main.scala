package wastebin

/**
  * Created by LOICK on 15/11/2015.
  */

import java.io._

import com.asprise.ocr.Ocr
import externClasses.scala.Files
import net.sourceforge.tess4j._;



/**
  * @author LOICK
  */
object JavaOCR_main {

  def main(args: Array[String]) {

    var imgFile = Files.getListOfFiles("C:\\Users\\LOICK\\IdeaProjects\\scala\\datasExtractionFromImages\\data\\crop")

    for (file <- imgFile) {
      println(file.getAbsolutePath)
      println()
      println(useTesseractOCR(file.getAbsolutePath))
      println("\n*************************\n\n")
    }
  }

  def useTesseractOCR(path: String): String ={

    var imageFile = new File(path);
    var tess = new Tesseract1(); //
    tess.setDatapath("C:\\Users\\LOICK\\Downloads\\Tess4J-2.0-src\\Tess4J\\tessdata");
    tess.setLanguage("fra");
    var result=""
    try {
      result = tess.doOCR(imageFile);
    } catch {
      case e: Throwable => e.printStackTrace
    }

    return result
  }
  /*
    merge_densityFactor = 0.5;
      merge_mass = 15;
      merge_dist1 = 4;
      merge_distfac = 1;
      merge_dist2 = 20;
   * */
  /*
  var in = new FileInputStream("C:/Users/LOICK/Documents/affiche2.jpg");
  var decoder = JPEGCodec.createJPEGDecoder(in);
  var image = decoder.decodeAsBufferedImage();
  in.close();

  var myget = new GetImageText(image,0.5,15,4,1,20);
  var boxes = myget.getTextBoxes();

  var out = new FileOutputStream("C:/Users/LOICK/Documents/affiche2_out.jpg");
  var encoder = JPEGCodec.createJPEGEncoder(out);
  encoder.encode(myget.isolateText(boxes));
  out.close();
  displayImage(MarvinImageIO.loadImage("C:/Users/LOICK/Documents/affiche2_out.jpg").getBufferedImage)
*/


  def useAspriseOCR(path: String) : String ={
    Ocr.setUp();
    var ocr = new Ocr(); // create a new OCR engine
    ocr.startEngine("fra", Ocr.SPEED_FASTEST); // English
    var s = ocr.recognize(path, -1, 0, 0, 1200, 1200, Ocr.RECOGNIZE_TYPE_TEXT, Ocr.OUTPUT_FORMAT_PLAINTEXT);
    ocr.stopEngine();
    return s
  }

}