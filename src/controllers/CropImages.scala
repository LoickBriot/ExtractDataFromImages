package controllers

import java.io.File

import externClasses.scala.{Files, Images}
import org.opencv.core._
import org.opencv.imgproc.Imgproc

import scala.collection.immutable.ListMap

/**
  * Created by LOICK on 17/11/2015.
  */
object CropImages {

  var dataFolder = "C:\\Users\\LOICK\\IdeaProjects\\scala\\ExtractDataFromImages\\data\\"


  def cropTextAreas( image_init: Mat, imageFile: File, rectFactor:Double, x_lim1: Int, y_lim1: Int, x_lim2: Int, y_lim2: Int): Unit = {

    var cropFold = dataFolder + s"crop--$rectFactor--$x_lim1--$y_lim1--$x_lim2--$y_lim2"
    Files.createFolder(cropFold)

    var listRectBeforeProcess = CropImages.detectLetters(image_init, rectFactor);
    var listRectAfterProcess1 = CropImages.concatenateRectangles(listRectBeforeProcess, image_init.height() / y_lim1, image_init.width() / x_lim1)
    var listRectAfterProcess2 = CropImages.concatenateRectangles(listRectAfterProcess1, image_init.height() / y_lim2, image_init.width() / x_lim2)

    Files.createFolder(cropFold+ File.separator + "fullImages" + File.separator)
    CropImages.drawListOfRectangles(image_init, listRectAfterProcess2, cropFold+ File.separator + "fullImages" + File.separator + imageFile.getName)

    var newFold = cropFold +File.separator+ "cropImages"+File.separator + imageFile.getName.substring(0, imageFile.getName.size - 4)
    Files.createFolder(newFold)

    if (rectFactor!=0) {
      var i = 0
      for (rect <- listRectAfterProcess2) {
        i += 1
        var newImageRect = processingBeforeOCR.process(image_init.submat(rect))
        Images.saveCVImage(newImageRect, newFold + File.separator + imageFile.getName.substring(0, imageFile.getName.size - 4) + s"_$i.jpg")
      }
    } else {
      var newImageRect = processingBeforeOCR.process(image_init)
      Images.saveCVImage(newImageRect, newFold + File.separator + imageFile.getName)
    }
  }


  def detectLetters(img: Mat, factor : Double = 1.0): Vector[Rect] = {

    var boundRect: Vector[Rect] = Vector[Rect]();
    var dst = new Mat()
    img.copyTo(dst);

    var img_gray, img_sobel, img_threshold, element : Mat = new Mat();
    Imgproc.cvtColor(img, img_gray, Imgproc.COLOR_RGB2GRAY);
    Imgproc.Laplacian(img_gray, img_sobel,CvType.CV_8U)//, CvType.CV_8U, 1, 0, 1, 1, 0, Imgproc.BORDER_DEFAULT);

    // Highgui.imwrite("C:/Users/LOICK/Documents/laplacian_out.jpg", img_sobel);
    Imgproc.threshold(img_sobel, img_threshold, 45, 255, Imgproc.THRESH_BINARY);
    //Images.displayCVImage(img_sobel,400)
    // Highgui.imwrite("C:/Users/LOICK/Documents/threshold_out.jpg", img_threshold);

    element = Imgproc.getStructuringElement(Imgproc.MORPH_ELLIPSE, new Size(14, 5));
    Imgproc.morphologyEx(img_threshold, img_threshold, Imgproc.MORPH_DILATE, element); //Does the trick
    //element = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(3, 3));
    //Imgproc.morphologyEx(img_threshold, img_threshold, Imgproc.MORPH_OPEN, element); //Does the trick

    //Images.displayCVImage(img_threshold,400)
    var contours: java.util.List[MatOfPoint] = new java.util.ArrayList[MatOfPoint]();
    Imgproc.findContours(img_threshold, contours, new Mat(), 0, 1);
    var contours_poly: java.util.List[MatOfPoint] = new java.util.ArrayList[MatOfPoint](contours.size());
    contours_poly.addAll(contours);

    var mMOP2f1, mMOP2f2 = new MatOfPoint2f();
    for (i <- 0 until contours.size()) {

      if (contours.get(i).toList().size() > 200) {
        contours.get(i).convertTo(mMOP2f1, CvType.CV_32FC2);
        Imgproc.approxPolyDP(mMOP2f1, mMOP2f2, 3, true);
        mMOP2f2.convertTo(contours_poly.get(i), CvType.CV_32S);
        val appRect = Imgproc.boundingRect(contours_poly.get(i))
        if (appRect.width > appRect.height) {
          boundRect ++= Vector[Rect](appRect);
        }
      }
    }

    var newBoundRect = Vector[Rect]()
    for (rect <- boundRect){
      if(rect.area() < (factor*img.width()*img.height()).toInt) {
        newBoundRect++=Vector[Rect](rect)
      }
    }
    return newBoundRect;
  }




  def maxRect(rect1: Rect, rect2: Rect): Rect = {
    var x_ul = Math.min(rect1.x,rect2.x)
    var y_ul = Math.min(rect1.y,rect2.y)
    var x_br = Math.max(rect1.x+rect1.width,rect2.x+rect2.width)
    var y_br = Math.max(rect1.y+rect1.height,rect2.y+rect2.height)

    return new Rect(new Point(x_ul,y_ul), new Point(x_br,y_br))
  }




  def minXDistBetweenTwoRect(rect1: Rect, rect2: Rect): Int ={
    var dist1 = Math.abs(rect1.x - rect2.x)
    var dist2 = Math.abs((rect1.x+rect1.width) - rect2.x)
    var dist3 = Math.abs(rect1.x - (rect2.x+rect2.width))
    var dist4 = Math.abs((rect1.x+rect1.width) - (rect2.x+rect2.width))

    var min = Math.min(dist1, dist2)
    min = Math.min(min, dist3)
    min = Math.min(min, dist4)

    return min
  }




  def minYDistBetweenTwoRect(rect1: Rect, rect2: Rect): Int ={
    var dist1 = Math.abs(rect1.y - rect2.y)
    var dist2 = Math.abs((rect1.y+rect1.height) - rect2.y)
    var dist3 = Math.abs(rect1.y - (rect2.y+rect2.height))
    var dist4 = Math.abs((rect1.y+rect1.height) - (rect2.y+rect2.height))

    var min = Math.min(dist1, dist2)
    min = Math.min(min, dist3)
    min = Math.min(min, dist4)

    return min
  }




  def concatenateRectangles(rectList: Vector[Rect], y_lim : Int, x_lim: Int): Vector[Rect] ={
    var map = Map[Rect,Int]()
    for (rect <- rectList){
      map++=Map[Rect,Int](rect -> (rect.y+rect.height/2))
    }
    map=ListMap(map.toSeq.sortBy(_._2):_*)

    var newRes = Vector[Rect]()
    var inter = Vector[Rect]()

    for (i <- 0 until map.keySet.size){
      var delta_y = 0

      if (i!=map.keySet.size-1) {
        delta_y = Math.abs((map.keys.toList(i).y+map.keys.toList(i).height/2) - (map.keys.toList(i+1).y+map.keys.toList(i+1).height/2))//minYDistBetweenTwoRect(map.keys.toList(i), map.keys.toList(i+1))
      } else {
        delta_y = 0
      }

      if ((i!=map.keySet.size-1) && (delta_y < y_lim)){
        inter++=Vector[Rect](map.keys.toList(i))
        inter++=Vector[Rect](map.keys.toList(i+1))
      } else if (inter.nonEmpty) {
        /*
        var max = inter(0)
        for (el <- inter){
          max = maxRect(max, el)
        }
        newRes++=Vector[Rect](max)
        inter = Vector[Rect]()
        */
        newRes++=sortRectListOnX(inter, x_lim)
        inter = Vector[Rect]()
      } else {
        newRes++=Vector[Rect](map.keys.toList(i))
      }
    }
    if (inter.nonEmpty) {
      /*var max = inter(0)
      for (el <- inter){
        max = maxRect(max, el)
      }
      newRes++=Vector[Rect](max)*/
      newRes++=sortRectListOnX(inter, x_lim)
      inter = Vector[Rect]()
    }
    return newRes
  }




  def sortRectListOnX(listRect : Vector[Rect], x_lim: Int): Vector[Rect]={
    var map = Map[Rect,Int]()
    for (rect <- listRect){
      map++=Map[Rect,Int](rect -> (rect.x+rect.width/2))
    }
    map=ListMap(map.toSeq.sortBy(_._2):_*)

    var newRes = Vector[Rect]()
    var inter = Vector[Rect]()

    for (i <- 0 until map.keySet.size){
      var delta_x = 0

      if (i!=map.keySet.size-1) {
        delta_x = Math.abs((map.keys.toList(i).x+map.keys.toList(i).width/2) - (map.keys.toList(i+1).x+map.keys.toList(i+1).width/2))//minYDistBetweenTwoRect(map.keys.toList(i), map.keys.toList(i+1))
        //minXDistBetweenTwoRect(map.keys.toList(i), map.keys.toList(i+1))
      } else {
        delta_x = 0
      }

      if ((i!=map.keySet.size-1) && (delta_x < x_lim)){
        inter++=Vector[Rect](map.keys.toList(i))
        inter++=Vector[Rect](map.keys.toList(i+1))
      } else if (inter.nonEmpty) {
        var max = inter(0)
        for (el <- inter){
          max = maxRect(max, el)
        }
        newRes++=Vector[Rect](max)
        inter = Vector[Rect]()
      } else {
        newRes++=Vector[Rect](map.keys.toList(i))
      }
    }
    if (inter.nonEmpty) {
      var max = inter(0)
      for (el <- inter){
        max = maxRect(max, el)
      }
      newRes++=Vector[Rect](max)
      inter = Vector[Rect]()
    }
    return newRes
  }




  def drawOneRectangle(img1: Mat, letterBBoxes1: Rect, coul: Scalar = new Scalar(0,0,255)): Unit ={
    Core.rectangle(img1, new Point(letterBBoxes1.x, letterBBoxes1.y), new Point(letterBBoxes1.x + letterBBoxes1.width, letterBBoxes1.y + letterBBoxes1.height), coul , 4)
  }




  def drawListOfRectangles(dst: Mat, letterBBoxes1: Vector[Rect], outputFile: String, factor: Double = 1.0): Mat ={
    var img1 = new Mat()
    dst.copyTo(img1);
    for (i <- 0 until letterBBoxes1.size) {
      drawOneRectangle(img1,letterBBoxes1(i),new Scalar(0,0,255))
    }
    Images.saveCVImage(img1, outputFile);
    return img1
  }

}
