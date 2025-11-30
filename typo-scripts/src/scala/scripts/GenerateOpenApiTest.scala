package scripts

import typo.openapi.{OpenApiCodegen, OpenApiOptions}
import typo.jvm
import typo.internal.codegen.LangJava

import java.nio.file.{Files, Path}

object GenerateOpenApiTest {
  val buildDir: Path = Path.of(sys.props("user.dir"))

  def main(args: Array[String]): Unit = {
    val specPath = buildDir.resolve("typo/src/scala/typo/openapi/testdata/test-features.yaml")
    val outputDir = buildDir.resolve("openapi-test-output")

    println(s"Generating code from: $specPath")
    println(s"Output directory: $outputDir")

    // Clean output directory
    if (Files.exists(outputDir)) {
      Files.walk(outputDir).sorted(java.util.Comparator.reverseOrder()).forEach(Files.delete)
    }
    Files.createDirectories(outputDir)

    val options = OpenApiOptions.default(jvm.QIdent(List(jvm.Ident("testapi")))).copy(
      generateValidation = true
    )
    val lang = LangJava

    val result = OpenApiCodegen.generate(specPath, options, lang)

    if (result.errors.nonEmpty) {
      println("Errors:")
      result.errors.foreach(e => println(s"  - $e"))
      sys.exit(1)
    }

    println(s"Generated ${result.files.size} files:")
    result.files.foreach { file =>
      val relativePath = file.tpe.value.idents.map(_.value).mkString("/") + ".java"
      val fullPath = outputDir.resolve(relativePath)
      Files.createDirectories(fullPath.getParent)
      Files.writeString(fullPath, file.contents.render(lang).asString)
      println(s"  - $relativePath")
    }

    println("Done!")
  }
}
