package externClasses.scala

/**
  * Created by LOICK on 15/11/2015.
  *
  * Three histogram classes : RGAHistogram, HSVHistrogram and HistogramEqualization
  *
  * Use function getHistogram to get the value lists of the histogram
  *
  * params:
  * a_imageIn: MarvinImage
  * step: Int = 10
  * a_previewMode: Boolean = false
  */


import java.awt.Color._
import java.awt._
import javax.swing._

import marvin.image._
import marvin.statistic._

import scala.Array._

object Histograms{


  def getRGAHistogram(a_imageIn: MarvinImage, step: Int = 10, a_previewMode: Boolean = false): scala.List[Int] = {

    var l_arrRed, l_arrGreen, l_arrBlue = ofDim[Int](256);
    var red, green, blue = ofDim[Float](256 / step)
    for (x <- 0 until a_imageIn.getWidth()) {
      for (y <- 0 until a_imageIn.getHeight()) {
        l_arrRed(a_imageIn.getIntComponent0(x, y)) += 1;
        l_arrGreen(a_imageIn.getIntComponent1(x, y)) += 1;
        l_arrBlue(a_imageIn.getIntComponent2(x, y)) += 1;
      }
    }

    var nb = 0
    for (i <- 0 until 256 / step) {
      for (j <- 0 until step) {
        red(i) += l_arrRed(j + nb * step)
        green(i) += l_arrGreen(j + nb * step)
        blue(i) += l_arrBlue(j + nb * step)
      }
      red(i) /= step
      green(i) /= step
      blue(i) /= step
      nb += 1
    }
    var Red = normalize(red)
    var Green = normalize(green)
    var Blue = normalize(blue)
    if (a_previewMode) {
      display(Red, Green, Blue)
    }

    return (Red.toList ++ Green.toList ++ Blue.toList)
  }


  def getHSBHistogram(a_imageIn: MarvinImage, step: Int = 10, a_previewMode: Boolean = false): scala.List[Int] = {

    var processStartTime: Long = System.currentTimeMillis()

    var l_arrRed = ofDim[Int](361);
    var red = ofDim[Float](361 / (2 * step))
    var l_arrGreen, l_arrBlue = ofDim[Int](101);
    var green, blue = ofDim[Float](101 / step);

    for (x <- 0 until a_imageIn.getWidth()) {
      for (y <- 0 until a_imageIn.getHeight()) {
        var res = RGBtoHSB(a_imageIn.getIntComponent0(x, y), a_imageIn.getIntComponent1(x, y), a_imageIn.getIntComponent2(x, y), null)
        l_arrRed((360 * res(0)).toInt) += 1;
        l_arrGreen((100 * res(1)).toInt) += 1;
        l_arrBlue((100 * res(2)).toInt) += 1;
      }
    }

    for (i <- 0 until 301 / (2 * step)) {
      for (j <- 0 until (2 * step)) {
        red(i) = l_arrRed(i)
      }
      red(i) /= (2 * step)
    }

    for (i <- 0 until 101 / step) {
      for (j <- 0 until step) {
        green(i) += l_arrGreen(i)
        blue(i) += l_arrBlue(i)
      }
      green(i) /= step
      blue(i) /= step
    }

    var Red = normalize(red)
    var Green = normalize(green)
    var Blue = normalize(blue)
    if (a_previewMode) {
      display(Red, Green, Blue)
    }

    return (Red.toList ++ Green.toList ++ Blue.toList)
  }


  def display(red: Array[Int], green: Array[Int], blue: Array[Int]) {
    var l_histoRed = new MarvinHistogram("Red Intensity");
    l_histoRed.setBarWidth(1);

    var l_histoGreen = new MarvinHistogram("Green Intensity");
    l_histoGreen.setBarWidth(1);

    var l_histoBlue = new MarvinHistogram("Blue Intensity");
    l_histoBlue.setBarWidth(1);

    for (i <- 0 until red.size) {
      l_histoRed.addEntry(new MarvinHistogramEntry(i, red(i), new Color(255, 0, 0)));
      l_histoGreen.addEntry(new MarvinHistogramEntry(i, green(i), new Color(0, 255, 0)));
      l_histoBlue.addEntry(new MarvinHistogramEntry(i, blue(i), new Color(0, 0, 255)));
    }

    var frame = new JFrame();
    frame.getContentPane().setLayout(new FlowLayout());
    frame.getContentPane().add(new JLabel(new ImageIcon(l_histoRed.getImage(400, 200))));
    frame.getContentPane().add(new JLabel(new ImageIcon(l_histoGreen.getImage(400, 200))));
    frame.getContentPane().add(new JLabel(new ImageIcon(l_histoBlue.getImage(400, 200))));
    frame.pack();
    frame.setVisible(true);
  }


  def normalize(list: Array[Float]): Array[Int] = {
    var min = list.min
    var max = list.max
    var res = ofDim[Int](list.size)
    if (max - min != 0) {
      for (x <- 0 until list.size) {
        res(x) = ((list(x) - min) * 1000 / (max - min)).toInt
      }
    }
    return res
  }


  def equalizeHistogram(imageIn: MarvinImage, imageOut: MarvinImage): MarvinImage = {

    var bmask = null;

    // histogram
    var histRed = ofDim[Int](256);
    var histGreen = ofDim[Int](256);
    var histBlue = ofDim[Int](256);
    var red, green, blue = 0;
    for (y <- 0 until imageIn.getHeight()) {
      for (x <- 0 until imageIn.getWidth()) {
        if (bmask != null) {

        } else {

          red = imageIn.getIntComponent0(x, y);
          green = imageIn.getIntComponent1(x, y);
          blue = imageIn.getIntComponent2(x, y);

          histRed(red) += 1;
          histGreen(green) += 1;
          histBlue(blue) += 1;
        }
      }
    }

    // Cumulative Distribution Function
    var cdfRed = ofDim[Int](256);
    var cdfGreen = ofDim[Int](256);
    var cdfBlue = ofDim[Int](256);
    cdfRed(0) = histRed(0);
    cdfGreen(0) = histGreen(0);
    cdfBlue(0) = histBlue(0);
    for (i <- 1 until histRed.length) {
      cdfRed(i) = cdfRed(i - 1) + histRed(i);
      cdfGreen(i) = cdfGreen(i - 1) + histGreen(i);
      cdfBlue(i) = cdfBlue(i - 1) + histBlue(i);
    }

    // Equalization
    var numberOfPixels = imageIn.getWidth() * imageIn.getHeight();
    var minRed = min(cdfRed);
    var minGreen = min(cdfGreen);
    var minBlue = min(cdfBlue);
    for (x <- 0 until imageIn.getWidth()) {
      for (y <- 0 until imageIn.getHeight()) {
        if (bmask != null) {

        } else {

          red = imageIn.getIntComponent0(x, y);
          green = imageIn.getIntComponent1(x, y);
          blue = imageIn.getIntComponent2(x, y);

          red = (((cdfRed(red) - minRed).toDouble / (numberOfPixels - minRed)) * 255).toInt;
          green = (((cdfGreen(green) - minGreen).toDouble / (numberOfPixels - minGreen)) * 255).toInt;
          blue = (((cdfBlue(blue) - minBlue).toDouble / (numberOfPixels - minBlue)) * 255).toInt;
          imageOut.setIntColor(x, y, imageIn.getAlphaComponent(x, y), red, green, blue);
        }
      }
    }
    if (true) {
      var frame = new JFrame();
      frame.getContentPane().setLayout(new FlowLayout());
      frame.getContentPane().add(new JLabel(new ImageIcon(imageOut.getBufferedImage)));
      frame.pack();
      frame.setVisible(true);
    }
    return imageOut
  }

  def min(arr: Array[Int]): Int = {
    var min: Int = 0
    for (i <- 0 until arr.length) {
      if (min == -1 || arr(i) < min) {
        min = arr(i);
      }
    }
    return min;
  }

}

/*
      var a= new ThreadResult()
      var b= new ThreadResult()
      var c= new ThreadResult()

      var thread1 = new MyThread(a_imageIn, a, 0, a_imageIn.getWidth() /3 )
      var thread2 = new MyThread(a_imageIn,b, a_imageIn.getWidth() / 3 + 1, 2 * a_imageIn.getWidth() / 3)
      var thread3 = new MyThread(a_imageIn, c, 2 * a_imageIn.getWidth() / 3 + 1, a_imageIn.getWidth())

      T1.start
      T2.start
      T3.start

      T1.join()
      T2.join()
      T3.join()
     listThreadResult(0).getResult()
      var resThread1 = a.getResult()
      var resThread2 =  b.getResult()
      var resThread3 =  c.getResult()
      var res = addVector(resThread1, resThread2, resThread3)
      l_arrRed = res(0)
      l_arrGreen = res(1)
      l_arrBlue = res(2)


var listThreadResult = ofDim[ThreadResult](nbThread)
    var listthread = ofDim[MyThread](nbThread)
    var listThread = ofDim[Thread](nbThread)
    var listResult = ofDim[List[Array[Int]]](nbThread)

    for (i <- 0 until nbThread) {
      if (i == 0) {
        listthread(i) = new MyThread(a_imageIn, listThreadResult(i), 0, a_imageIn.getWidth() / nbThread)
      } else {
        listthread(i) = new MyThread(a_imageIn, listThreadResult(i), i * a_imageIn.getWidth() / nbThread + 1, (i + 1) * a_imageIn.getWidth() / nbThread)
      }
      listThread(i) = new Thread(listthread(i))
      listThread(i).start
    }

    for (i <- 0 until nbThread) {
      listThread(i).join
    }

    for (i <- 0 until nbThread) {
      listResult(i) = listThreadResult(i).getResult()
    }

    for (i <- 0 until nbThread) {
      for (j <- 0 until listResult(0)(0).size) {
        l_arrRed(j) += listResult(i)(0)(j)
        l_arrGreen(j) += listResult(i)(1)(j)
        l_arrBlue(j) += listResult(i)(2)(j)
      }
    }

class ThreadResult {
  var res = List[Array[Int]]()

  def getResult(): List[Array[Int]] = {
    return this.res
  }

  def setResult(result: List[Array[Int]]) {
    this.res = result
  }
}

class MyThread(a_imageIn: MarvinImage, threadRes: ThreadResult, start: Int, end: Int, hsvBool: Boolean = false, maxValue: Int = 256) extends Runnable {
  def run {

    var l_arrRed = ofDim[Int](360)
    var l_arrGreen, l_arrBlue = ofDim[Int](100);
    for (x <- start until end) {
      for (y <- 0 until a_imageIn.getHeight()) {
        if (hsvBool) {
          var res = RGBtoHSB(a_imageIn.getIntComponent0(x, y), a_imageIn.getIntComponent1(x, y), a_imageIn.getIntComponent2(x, y), null)

          l_arrRed((360 * res(0)).toInt) += 1;
          l_arrGreen((100 * res(1)).toInt) += 1;
          l_arrBlue((100 * res(2)).toInt) += 1;
          threadRes.setResult(List[Array[Int]](l_arrRed, l_arrGreen, l_arrBlue))
        } else {

          //threadRes.setResult(List[Array[Int]](l_arrRed, l_arrGreen, l_arrBlue))
        }
      }
    }
  }
}

*/