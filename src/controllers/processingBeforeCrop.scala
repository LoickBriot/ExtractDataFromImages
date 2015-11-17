package controllers

import externClasses.scala.Images
import org.opencv.core._

/**
  * Created by LOICK on 17/11/2015.
  */
object processingBeforeCrop {

  def process(image: Mat, nbMaxPixel: Int): Mat ={
    var copy = Images.copyCVMat(image)
    copy = Images.resizeCVImage(copy, nbMaxPixel)
    return copy

  }


}
