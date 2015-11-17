package controllers

import java.io.File

import net.sourceforge.tess4j.Tesseract1

/**
  * Created by LOICK on 17/11/2015.
  */
object OCRImages {

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
}
