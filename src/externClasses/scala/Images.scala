package externClasses.scala

/**
  * Created by LOICK on 15/11/2015.
  */

import Array._
import java.awt.image.BufferedImage
import java.awt.{FlowLayout, GraphicsEnvironment, RenderingHints}
import java.io.{ByteArrayInputStream, File}
import javax.imageio.ImageIO
import javax.swing.{ImageIcon, JFrame, JLabel}

import externClasses.scala.Files
import marvin.image.MarvinImage
import marvin.io.MarvinImageIO
import org.opencv.core._
import org.opencv.highgui.Highgui
import org.opencv.imgproc._
import externClasses.java.ConvertUtil

/**
  * @author LOICK
  */


object Images {

  var inputFolder = "C:\\Users\\LOICK\\IdeaProjects\\scala\\ExtractDataFromImages\\data\\input\\"
  var inputFolder4 = "C:\\Users\\LOICK\\IdeaProjects\\scala\\ExtractDataFromImages\\data\\input4\\"
  System.loadLibrary( Core.NATIVE_LIBRARY_NAME );

  def main(args: Array[String]): Unit = {
  /*
    var listFile = Files.getListOfFiles(inputFolder)
    for (file <- listFile){
      saveBufferedImage(convert4(loadBufferedImage(file.getAbsolutePath)),inputFolder4+file.getName)
    }*/
    var image = loadCVImage(inputFolder+"affiche-croods.jpg")

    displayCVImage(cvReduceColorDim(image,10))
  }


  def cvReduceColorDim(image: Mat, factor: Int): Mat ={

    def newVal(pix: Double, factor: Int): Double ={
      var y = 0.0
      if (pix<125){
        y = (pix / factor).toInt * factor.toDouble
      } else {
        y = ((pix / factor).toInt * factor) + 5.toDouble
      }
      return y
    }
    var copy = image.clone()
    for (y<-0 until copy.rows; x<- 0 until copy.cols){
      var b =  newVal(image.get(y,x)(0), factor)
      var g = newVal(image.get(y,x)(1), factor)
      var r = newVal(image.get(y,x)(2), factor)
      copy.put(y,x,b,g,r)
    }
    return copy
  }

  /*
  * Load an image
  */
  def loadBufferedImage(filePath: String): BufferedImage = {
    return ImageIO.read(new File(filePath))
  }
  def loadMarvinImage(filePath: String): MarvinImage = {
    return  MarvinImageIO.loadImage(filePath)
  }
  def loadCVImage(filePath: String): Mat ={
    Highgui.imread(filePath);
  }

  /*
  * Save a new image
  */
  def saveMarvinImage(image: MarvinImage, fileName: String): Unit ={
    MarvinImageIO.saveImage(image, fileName)
  }
  def saveBufferedImage(image: BufferedImage, fileName: String): Unit ={
    ImageIO.write(image, Files.getExtension(fileName), new File(fileName));
  }
  def saveCVImage(image: Mat, fileName: String): Unit ={
    Highgui.imwrite( fileName, image );
  }

  /*
  * Display an image
  */
  def displayBufferedImage(img: BufferedImage, size: Int = 500) {
    val frame = new JFrame();
    frame.setSize(size, size)
    val width = Math.min(img.getWidth, size)
    var height = 0
    if (width == img.getWidth) {
      height = img.getHeight
    } else {
      height = (img.getHeight * (width / img.getWidth.toFloat)).toInt
    }
    frame.getContentPane().setLayout(new FlowLayout());
    frame.getContentPane().add(new JLabel(new ImageIcon(img.getScaledInstance(width, height, 1))));
    frame.pack();
    frame.setVisible(true);
  }
  def displayCVImage(img: Mat, size: Int = 500): Unit ={
    displayBufferedImage(matToBufferedImage(img), size)
  }
  def displayMarvinImage(img: MarvinImage, size: Int = 500): Unit ={
    displayBufferedImage(img.getBufferedImage, size)
  }
  def displayImageFromPath(filePath: String, size: Int = 500): Unit ={
    displayBufferedImage(loadBufferedImage(filePath), size)
  }

  def copyCVMat(img: Mat): Mat ={
    var dst = new Mat()
    img.copyTo(dst);
    return dst
  }

  /*
  * Resize an image
  */
  def resizeBufferedImage(image: BufferedImage, nbMaxPixel: Int): BufferedImage = {
    var bImage = image
    if (bImage.getWidth * bImage.getHeight > nbMaxPixel) {
      var factor: Double = Math.min(Math.sqrt(nbMaxPixel / (bImage.getHeight * bImage.getWidth).toFloat), 1)
      var destWidth = (bImage.getWidth() * factor).toInt;
      var destHeight = (bImage.getHeight() * factor).toInt;
      var configuration = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDefaultConfiguration();
      var bImageNew = configuration.createCompatibleImage(destWidth, destHeight);
      var graphics = bImageNew.createGraphics();
      graphics.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
      graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
      graphics.drawImage(bImage, 0, 0, destWidth, destHeight, 0, 0, bImage.getWidth(), bImage.getHeight(), null);
      graphics.dispose();
      bImage=bImageNew
    }
    return bImage
  }

  def resizeCVImage(image: Mat, nbMaxPixel: Int): Mat ={
    var copy = Images.copyCVMat(image)
    var resizeImage = new Mat()
    if (copy.height()*copy.width > nbMaxPixel){
      var factor: Double = Math.min(Math.sqrt(nbMaxPixel / (copy.height() * copy.width).toFloat), 1)
      var destWidth = (copy.width() * factor).toInt;
      var destHeight = (copy.height() * factor).toInt;
      Imgproc.resize(copy,resizeImage,new Size(destWidth, destHeight))
      return resizeImage
    }
    return copy
  }

  def resizeImageFromPath(path: String, nbMaxPixel: Int): BufferedImage = {
    var bImage = loadBufferedImage(path);
    bImage = resizeBufferedImage(bImage, nbMaxPixel)
    saveBufferedImage(bImage, path);
    return bImage
  }


  def matToBufferedImage(image: Mat): BufferedImage= {
    var bytemat = new MatOfByte();
    Highgui.imencode(".jpg", image, bytemat);
    var bytes = bytemat.toArray();
    var in = new ByteArrayInputStream(bytes);
    return ImageIO.read(in);
  }

  def colorScaleImage(image: Mat): Mat ={

    var copy = copyCVMat(image)
    Imgproc.cvtColor(image,copy,Imgproc.COLOR_BGR2GRAY)
    return copy
  }



}
