import sbt._
class YuiCompressorPluginProject(info: ProjectInfo) extends PluginProject(info) {
  val yuiCompressor = "com.yahoo.platform.yui" % "yuicompressor" % "2.4.4" % "compile->default" from "http://cloud.github.com/downloads/vspy/yuicompressor/yuicompressor-2.4.4.jar"
  val rhinoYui = "rhino" % "js" % "1.6R7yui" from "http://cloud.github.com/downloads/vspy/rhino1.6R7-yui/js.jar"
  
  override def managedStyle = ManagedStyle.Maven
  val publishTo = "Scala Tools Nexus" at "http://nexus.scala-tools.org/content/repositories/releases/"
  Credentials(Path.userHome / ".ivy2" / ".credentials", log)
  
}
