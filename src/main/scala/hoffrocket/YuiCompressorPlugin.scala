package hoffrocket
import sbt._

import com.yahoo.platform.yui.compressor.CssCompressor
import com.yahoo.platform.yui.compressor.JavaScriptCompressor
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import org.mozilla.javascript.ErrorReporter
import org.mozilla.javascript.EvaluatorException

trait YuiCompressorPlugin extends BasicScalaProject {
 
  trait YuiOptions {
    val encoding:String = "UTF-8"
    val nomunge:Boolean = false
    val jswarn:Boolean = false
    val preserveAllSemiColons:Boolean = false
    val disableOptimizations:Boolean = false
    val lineBreaks:Int = -1
  }
  
  def yuiCompressorTask(
        product:Path
        , sources:Iterable[Path]
        , options:YuiOptions
  ) = 
    fileTask(Seq(product) from sources) {

      val errorReporter = new YuiErrorReporter()
      val inFiles = sources.map( (s)=>{new File(s.toString)} )
      val maxLastModified = inFiles.foldLeft(-1l)( (r,f) => { 
            if(f.lastModified > r) f.lastModified
            else r
      })
      val outFile = new File(product.toString)

      if (!outFile.exists || outFile.lastModified < maxLastModified) {
	if (!outFile.getParentFile.exists && !outFile.getParentFile.mkdirs) {
	  throw new Exception( "Cannot create resource output directory: " + outFile.getParentFile() )
	}
	val out = new OutputStreamWriter(new FileOutputStream(outFile), options.encoding)
        try {

          for (file <- inFiles) {
            val in = new InputStreamReader(new FileInputStream(file), options.encoding)
            try {

	      val fileName = file.toString//.substring(webappPath.toString.length)
              log.info("Compressing: " + fileName)
	      val extension = fileName.substring(fileName.lastIndexOf("."))

              if (".js".equalsIgnoreCase(extension)) {
	        val compressor = new JavaScriptCompressor(in, errorReporter)
	        compressor.compress(
                  out
                  , options.lineBreaks
                  , !options.nomunge
                  , options.jswarn
                  , options.preserveAllSemiColons
                  , options.disableOptimizations
                )
	      } else if (".css".equalsIgnoreCase(extension)) {
	        val compressor = new CssCompressor(in)
	        compressor.compress(out, options.lineBreaks)
	      }

            } finally { in.close }
          }

        } finally { out.close }
      }

      None
    }

  class YuiErrorReporter extends ErrorReporter {
    def logit(level:Level.Value, message:String, sourceName:String, line:Int, lineSource:String, lineOffset:Int) = 
      log.log(level, "%s in %s:%d,%d at %s".format(message, sourceName,line,lineOffset, lineSource))
            
    def error(message:String, sourceName:String, line:Int, lineSource:String, lineOffset:Int) =
      logit(Level.Error, message, sourceName, line,lineSource,lineOffset)

    def runtimeError(message:String, sourceName:String, line:Int, lineSource:String, lineOffset:Int):EvaluatorException = {
      error(message, sourceName, line,lineSource,lineOffset)
      new EvaluatorException(message, sourceName, line, lineSource, lineOffset)
    }

    def warning(message:String, sourceName:String, line:Int, lineSource:String, lineOffset:Int) =
      logit(Level.Warn, message, sourceName, line,lineSource,lineOffset) 
  }
  
}
