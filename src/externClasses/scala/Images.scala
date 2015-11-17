package externClasses.scala

/**
  * Created by LOICK on 15/11/2015.
  */

import java.awt.image.BufferedImage
import java.awt.{FlowLayout, GraphicsEnvironment, RenderingHints}
import java.io.{ByteArrayInputStream, File}
import javax.imageio.ImageIO
import javax.swing.{ImageIcon, JFrame, JLabel}

import externClasses.scala.Files._
import marvin.image.MarvinImage
import marvin.io.MarvinImageIO
import org.opencv.core._
import org.opencv.highgui.Highgui
import org.opencv.imgproc._


/**
  * @author LOICK
  */


object Images {

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
    ImageIO.write(image, getExtension(fileName), new File(fileName));
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

}