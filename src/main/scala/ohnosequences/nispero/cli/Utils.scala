package ohnosequences.nispero.cli

import java.io.{PrintWriter, File}
import org.apache.commons.io.FileUtils

object Utils {
  def waitForResource[A](resource: => Option[A]) : Option[A] = {
    var iteration = 1
    var current: Option[A] = None
    val limit = 50

    do {
      current = resource
      iteration += 1
      Thread.sleep(1000)
    } while (current.isEmpty && iteration < limit)

    current
  }

  def createTempDir(attempt: Int = 0): File = {
    val baseDir = FileUtils.getTempDirectory()
    val tmp: File = new File(baseDir, System.nanoTime() + "-" + attempt)

    if(tmp.mkdir()) {
      tmp
    } else if(attempt < 1000) {
      createTempDir(attempt + 1)
    } else {
      null
    }
  }

  def recursiveListFiles(file: File, exclude: List[File] = Nil, root: Boolean = true): List[File] =
    if(file.exists()) { root match {
      case false =>
        if (exclude.contains(file)) {
          Nil
        } else {
          val these = file.listFiles.toList
          these ++ these.filter(_.isDirectory).flatMap(recursiveListFiles(_, exclude, false))
        }
      case true => file +: recursiveListFiles(file, exclude, false)
    }}
    else {
      List()
    }

  def findPattern(s: String): Option[(String, String)] = {
    val context = """.{0,5}\$([\w\-]+)\$.{0,5}""".r
    context.findFirstMatchIn(s).map {m => (m.group(1), m.matched)}
  }

  def writeStringToFile(s: String, file: File) {
    val writer = new PrintWriter(file)
    writer.print(s)
    writer.close()
  }

  def replace(s: String, mapping: Map[String, String]) = {
    var res = s
    for ((key, value) <- mapping) {
      res = res.replace(key, value)
    }
    res
  }

  def copyAndReplace(files: List[File], outputDir: File, mapping: Map[String, String], ignorePrefix: String = "", mapIgnore: File => Boolean = (file => false)) {
    for (file <- files) {
      //println(file.getAbsolutePath)
      val newFile = new File(outputDir, file.getPath.replace(ignorePrefix, "")).getCanonicalFile
      //println(newFile.getPath)
      if (file.isDirectory) {

        newFile.mkdir()
      } else {

        if(mapIgnore(file)) {
          // println("ignoring: " + file.getPath)
          FileUtils.copyFile(file, newFile)
        } else {
          val content = replace(scala.io.Source.fromFile(file).mkString, mapping)

          //content.
          if(content.contains("$")) {
            println("warning: " + newFile.getPath + " contains $")
          }
          // findPattern(content) match {
          //   case None => ()
          //   case Some((name, context)) => throw new Error("warning: " + name + " placeholder is free")
          // }
          writeStringToFile(content, newFile)
        }

      }
    }
  }
}
