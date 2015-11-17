package externClasses.scala

/**
  * Created by LOICK on 17/11/2015.
  */



object Exceptions {
  case class printMessageException(message: String) extends Exception(message)

  //throw Exceptions.printMessageException("Le dossier ' " + dataFolder + s"crop--$rectFactor--$x_lim1--$y_lim1--$x_lim2--$y_lim2" + " ' existe dejà. Veuillez le supprimer ou le renommer.")

}
