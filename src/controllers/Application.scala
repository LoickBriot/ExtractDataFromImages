package controllers

import java.io.File

import externClasses.scala.{Files, Images}
import org.opencv.core._


/**
  * Created by LOICK on 16/11/2015.
  */

object Application {

  System.loadLibrary( Core.NATIVE_LIBRARY_NAME );
  var dataFolder = "C:\\Users\\LOICK\\IdeaProjects\\scala\\ExtractDataFromImages\\data\\"
  var inputFolder = "C:\\Users\\LOICK\\IdeaProjects\\scala\\ExtractDataFromImages\\data\\input\\"

  def main(args: Array[String]): Unit = {

    var fileList  =  Files.getListOfFiles(inputFolder)
    cropAndGetText(fileList,0,1,1,1,1)
    cropAndGetText(fileList,1,1,1,1,1)
    cropAndGetText(fileList,0.33,3,17,12,7)
    cropAndGetText(fileList,0.33,6,17,100,100)

  }


  def cropAndGetText( fileList: List[File], rectFactor:Double, x_lim1: Int, y_lim1: Int, x_lim2: Int, y_lim2: Int): Unit = {

    if (new File(dataFolder + s"crop--$rectFactor--$x_lim1--$y_lim1--$x_lim2--$y_lim2" + File.separator).exists()) {

      println("WARNING :  Le dossier ' " + dataFolder + s"crop--$rectFactor--$x_lim1--$y_lim1--$x_lim2--$y_lim2" + " ' existe dejà. Veuillez le supprimer ou le renommer pour relancer l'expérimentation.")

    } else {
      for (imageFile <- fileList) {
        var image = Images.cvReduceColorDim(Images.loadCVImage(imageFile.getAbsolutePath),40)
        image = processingBeforeCrop.process(image, 2000000)
        CropImages.cropTextAreas(image, imageFile, rectFactor, x_lim1, y_lim1, x_lim2, y_lim2)
      }

      var cropFold = dataFolder + s"crop--$rectFactor--$x_lim1--$y_lim1--$x_lim2--$y_lim2" + File.separator + "cropImages"
      var directories = Files.getSubfolders(cropFold)

      for (dir <- directories) {
        var content = dir.getAbsolutePath + "\n"
        var files = Files.getListOfFiles(dir.getAbsolutePath)
        for (file <- files) {
          var result = OCRImages.useTesseractOCR(file.getAbsolutePath)
          content += result + "\n"
        }
        content += "\n\n*****\n\n"
        Files.writeInFile(dataFolder + s"crop--$rectFactor--$x_lim1--$y_lim1--$x_lim2--$y_lim2\\content.txt", content)
      }
    }
  }
}



