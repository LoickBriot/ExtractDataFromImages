package controllers

import externClasses.scala.Images
import org.opencv.core.Mat

/**
  * Created by LOICK on 17/11/2015.
  */
object processingBeforeOCR {

  def process(image: Mat): Mat = {
    var copy = Images.copyCVMat(image)
    return copy

  }
}
