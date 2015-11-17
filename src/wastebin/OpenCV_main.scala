package wastebin

/**
  * Created by LOICK on 15/11/2015.
  */

import java.io.File

import externClasses.scala.{Files, Images}
import org.opencv.core._
import org.opencv.imgproc._

/**
* @author LOICK
*/

object OpenCV_main {

  System.loadLibrary( Core.NATIVE_LIBRARY_NAME );

  def main(args: Array[String]){

    var inputFolder = "C:\\Users\\LOICK\\IdeaProjects\\scala\\datasExtractionFromImages\\data\\input\\"
    var outputFolder = "C:\\Users\\LOICK\\IdeaProjects\\scala\\datasExtractionFromImages\\data\\output\\"

    var fileList = Files.getListOfFiles(inputFolder)
    var res : Vector[Rect] = null
    for (file <- fileList) {
      res = drawRectangles(file , outputFolder)
      //Images.displayImageFromPath(outputFolder+file.getName)
    }

  }


  def drawRectangles(file: File, outputFolder: String ): Vector[Rect] ={
    //Images.resizeImageFromPath(file.getAbsolutePath,2000000)
    var img1 = Images.loadCVImage(file.getAbsolutePath)
    var letterBBoxes1: Vector[Rect] = detectLetters(img1);

    for (i <- 0 until letterBBoxes1.size) {
      if(letterBBoxes1(i).area()< 0.33*img1.width()*img1.height()) {
        Core.rectangle(img1, new Point(letterBBoxes1(i).x, letterBBoxes1(i).y), new Point(letterBBoxes1(i).x + letterBBoxes1(i).width, letterBBoxes1(i).y + letterBBoxes1(i).height), new Scalar(0,0,255), 4)
      }
    }
    Images.saveCVImage(img1, outputFolder + file.getName);
    return letterBBoxes1
  }

  def detectLetters(img: Mat): Vector[Rect] = {

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
    element = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(3, 3));
    Imgproc.morphologyEx(img_threshold, img_threshold, Imgproc.MORPH_OPEN, element); //Does the trick

    Images.displayCVImage(img_threshold,400)
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
    return boundRect;
  }
}



//Imgproc.Scharr(img_sobel, img_sobel,CvType.CV_8U,1,0)
//Imgproc.Sobel(img_sobel, img_sobel, CvType.CV_8U, 1,0 , 1, 1, 0, Imgproc.BORDER_DEFAULT);

//Images.displayCVImage(img_sobel,400)
