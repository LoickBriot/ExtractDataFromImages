package externClasses.scala

import java.sql.{Connection, DriverManager}
import java.util.UUID

import externClasses.scala.Files._
import externClasses.scala.Histograms._
import externClasses.scala.Images._
import marvin.image.MarvinImage
import marvin.io.MarvinImageIO

/**
  * Created by LOICK on 15/11/2015.
  */
object otherMethods {


  def generateAnUniqueName(extension: String): String = {

    val driver = "com.mysql.jdbc.Driver"
    val url = "jdbc:mysql://localhost/festalorproject"
    val username = "root"
    val password = ""

    var connection: Connection = DriverManager.getConnection(url, username, password)

    var name = UUID.randomUUID().toString().replaceAll("-", "") + extension
    var query = connection.createStatement().executeQuery(s"SELECT id FROM image WHERE uniqueName = '$name'")

    while (query.isBeforeFirst()) {
      name = UUID.randomUUID().toString().replaceAll("-", "") + extension
      query = connection.createStatement().executeQuery(s"SELECT id FROM image WHERE uniqueName = '$name'")
    }

    connection.close()

    return name;
  }


  def searchInDB(path: String, nbBins: Integer, nbSearch: Integer): List[String] = {

    var image = MarvinImageIO.loadImage(path)
    var feature = getHSBFeature(image)

    val driver = "com.mysql.jdbc.Driver"
    val url = "jdbc:mysql://localhost/festalorproject"
    val username = "root"
    val password = ""
    var connection: Connection = DriverManager.getConnection(url, username, password)
    var query = connection.createStatement().executeQuery(s"SELECT inputName,vector FROM image")
    var distList: List[Double] = List[Double]()
    var nameList: List[String] = List[String]()

    while (query.next()) {
      var vector = query.getString("vector").split(", ").toList.map(_.toString.toInt)
      distList ++= List[Double](euclidianDistance(feature, vector))
      nameList ++= List[String](query.getString("inputName"))
    }

    var distListSorted = distList.toList.sorted

    var listResult: List[String] = List[String]()
    for (i <- 0 until nbSearch) {
      listResult ++= List[String](nameList(distList.indexOf(distList.min)))
      nameList = nameList.diff(List[String](nameList(distList.indexOf(distList.min))))
      distList = distList.diff(List[Double](distList(distList.indexOf(distList.min))))

    }

    println(listResult)
    return listResult
  }

  def fullDB(path: String, nbBins: Integer) {

    var listImage = getListOfFiles(path)
    for (img <- listImage) {

      var image1 = resizeImageFromPath(img.getAbsolutePath, 1000000)
      var feature1 = getHSBFeature(loadMarvinImage(img.getAbsolutePath))
      var extension = ""
      if (img.getName().lastIndexOf('.') > 0) {
        extension = "." + img.getName().substring(img.getName().lastIndexOf('.') + 1);
      }
      var uniqueName = generateAnUniqueName(extension)
      insertImageInDB(uniqueName, img.getAbsolutePath(), img.getName(), "", feature1.toString().substring(5, feature1.toString().size - 1))
    }
  }

  def insertImageInDB(uniqueName: String, path: String, inputName: String, description: String, vector: String) {

    val driver = "com.mysql.jdbc.Driver"
    val url = "jdbc:mysql://localhost/festalorproject"
    val username = "root"
    val password = ""

    try {
      var connection: Connection = DriverManager.getConnection(url, username, password)

      val prep = connection.prepareStatement(s"INSERT INTO image (uniqueName,path,inputName,description,vector) VALUES ('$uniqueName','$path','$inputName','$description','$vector')")
      prep.executeUpdate
      connection.close()
    } catch {
      case e: Throwable => e.printStackTrace
    }
  }

  def euclidianDistance(vect1: List[Int], vect2: List[Int]): Double = {

    var res: Double = 0
    for (i <- 0 until vect1.size) {
      res += Math.pow(vect1(i) - vect2(i), 2)
    }
    res = Math.sqrt(res)
    return res
  }


  def getRGAFeature(image: MarvinImage): List[Int] = {
    var left = image.subimage(0, 0, (image.getWidth * 0.2).toInt, image.getHeight)
    var right = image.subimage((image.getWidth * 0.8).toInt, 0, (image.getWidth * 0.2).toInt, image.getHeight)
    var up = image.subimage((image.getWidth * 0.2).toInt, 0, (image.getWidth * 0.6).toInt, (image.getHeight * 0.2).toInt)
    var down = image.subimage((image.getWidth * 0.2).toInt, (image.getHeight * 0.8).toInt, (image.getWidth * 0.6).toInt, (image.getHeight * 0.2).toInt)
    var center = image.subimage((image.getWidth * 0.2).toInt, (image.getHeight * 0.2).toInt, (image.getWidth * 0.6).toInt, (image.getHeight * 0.6).toInt)

    var histoRGA = getRGAHistogram(left, 20)
    histoRGA ++= getRGAHistogram(right, 20)
    histoRGA ++= getRGAHistogram(up, 20)
    histoRGA ++= getRGAHistogram(down, 20)
    histoRGA ++= getRGAHistogram(center, 20)
    return histoRGA
  }

  def getHSBFeature(image: MarvinImage): List[Int] = {
    var left = image.subimage(0, 0, (image.getWidth * 0.2).toInt, image.getHeight)
    var right = image.subimage((image.getWidth * 0.8).toInt, 0, (image.getWidth * 0.2).toInt, image.getHeight)
    var up = image.subimage((image.getWidth * 0.2).toInt, 0, (image.getWidth * 0.6).toInt, (image.getHeight * 0.2).toInt)
    var down = image.subimage((image.getWidth * 0.2).toInt, (image.getHeight * 0.8).toInt, (image.getWidth * 0.6).toInt, (image.getHeight * 0.2).toInt)
    var center = image.subimage((image.getWidth * 0.2).toInt, (image.getHeight * 0.2).toInt, (image.getWidth * 0.6).toInt, (image.getHeight * 0.6).toInt)


    var histoHSB = getHSBHistogram(left, 6)
    histoHSB ++= getHSBHistogram(right, 6)
    histoHSB ++= getHSBHistogram(up, 6)
    histoHSB ++= getHSBHistogram(down, 6)
    histoHSB ++= getHSBHistogram(center, 4)
    return histoHSB
  }

  def displayList(res: List[String]) {
    for (el <- res) {
      displayMarvinImage(loadMarvinImage("C:\\Users\\LOICK\\scalaWorkspace\\test_Opencv\\image\\" + el))
    }
  }


}
