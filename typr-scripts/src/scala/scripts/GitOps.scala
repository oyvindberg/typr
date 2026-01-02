package scripts

import bleep.cli
import ryddig.Logger

import java.nio.file.Path
import scala.annotation.nowarn

object GitOps {
  private val lock = new Object()

  def gitAdd(description: String, workDir: Path, paths: List[String], logger: Logger): Unit = {
    val _ = lock.synchronized {
      cli(
        description,
        workDir,
        List("git", "add", "-f") ++ paths,
        logger = logger,
        cli.Out.Raw
      )
    }
  }

  def gitAddSimple(paths: List[String]): Unit = {
    import scala.sys.process.*
    val _ = lock.synchronized {
      (List("git", "add", "-f") ++ paths).!! : @nowarn
    }
  }
}
