package externClasses.scala

import java.io.{File, FileInputStream, FileOutputStream, FileWriter}

import org.apache.commons.io.IOUtils

/**
  * Created by LOICK on 15/11/2015.
  */
object Files {


  def createFolder(path:String): Unit ={
    var dir = new File(path)
    if (!dir.exists()) {
      dir.mkdirs();
    }
  }

  def writeInFile(path: String, content: String, resetBool : Boolean = true): Unit ={
    var fw = new FileWriter(path, resetBool);
    fw.write(content);
    fw.close();
  }

  def getSubfolders(path: String): List[File] ={
    var directories = List[File]()
    if (new File(path).exists()){
      var files = new File(path).listFiles()
      for (file <- files){
        if (file.isDirectory){
          directories++=List[File](file)
        }
      }
    }
    return directories
  }


  def copyFile(initialPath: String, finalPath : String): Unit ={
    var input = new FileInputStream(initialPath);
    var output = new FileOutputStream( finalPath );
    IOUtils.copy(input, output);
  }


  def deleteFile(filePath : String): Unit = {
    var tempFile = new File(filePath).delete()
  }


  def getExtension(inputName: String): String = {
    var extension = ""
    if (inputName.lastIndexOf('.') > 0) {
      extension = inputName.substring(inputName.lastIndexOf('.') + 1);
    }
    return extension
  }


  def getListOfFiles(dir: String): List[File] = {
    val d = new File(dir)
    if (d.exists && d.isDirectory) {
      d.listFiles.filter(_.isFile).toList
    } else {
      List[File]()
    }
  }

}
