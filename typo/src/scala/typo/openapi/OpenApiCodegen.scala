package typo.openapi

import typo.{jvm, Lang}
import typo.openapi.codegen.{
  ApiCodegen,
  FrameworkSupport,
  JacksonSupport,
  JaxRsSupport,
  Jsr380ValidationSupport,
  JsonLibSupport,
  ModelCodegen,
  NoFrameworkSupport,
  NoValidationSupport,
  TypeMapper,
  ValidationSupport
}
import typo.openapi.parser.OpenApiParser

import java.nio.file.Path

/** Main entry point for OpenAPI code generation */
object OpenApiCodegen {

  case class Result(
      files: List[jvm.File],
      errors: List[String]
  )

  /** Generate code from an OpenAPI spec file */
  def generate(
      specPath: Path,
      options: OpenApiOptions,
      lang: Lang
  ): Result = {
    OpenApiParser.parseFile(specPath) match {
      case Left(errors) =>
        Result(Nil, errors)
      case Right(parsedSpec) =>
        generateFromSpec(parsedSpec, options, lang)
    }
  }

  /** Generate code from an OpenAPI spec URL or file path */
  def generate(
      specLocation: String,
      options: OpenApiOptions,
      lang: Lang
  ): Result = {
    OpenApiParser.parse(specLocation) match {
      case Left(errors) =>
        Result(Nil, errors)
      case Right(parsedSpec) =>
        generateFromSpec(parsedSpec, options, lang)
    }
  }

  /** Generate code from YAML/JSON content */
  def generateFromContent(
      content: String,
      options: OpenApiOptions,
      lang: Lang
  ): Result = {
    OpenApiParser.parseContent(content) match {
      case Left(errors) =>
        Result(Nil, errors)
      case Right(parsedSpec) =>
        generateFromSpec(parsedSpec, options, lang)
    }
  }

  /** Generate code from a parsed spec */
  def generateFromSpec(
      spec: ParsedSpec,
      options: OpenApiOptions,
      lang: Lang
  ): Result = {
    val modelPkg = options.pkg / jvm.Ident(options.modelPackage)
    val apiPkg = options.pkg / jvm.Ident(options.apiPackage)

    val jsonLib: JsonLibSupport = options.jsonLib match {
      case OpenApiJsonLib.Jackson => JacksonSupport
      case _                      => JacksonSupport // Default to Jackson for now
    }

    val frameworkSupport: FrameworkSupport = options.framework match {
      case OpenApiFramework.JaxRs => JaxRsSupport
      case OpenApiFramework.None  => NoFrameworkSupport
      case _                      => NoFrameworkSupport // TODO: implement Http4s, Tapir
    }

    val validationSupport: ValidationSupport =
      if (options.generateValidation) Jsr380ValidationSupport
      else NoValidationSupport

    // Collect sum type names for nested sum type detection
    val sumTypeNames = spec.sumTypes.map(_.name).toSet

    val typeMapper = new TypeMapper(modelPkg, options.typeOverrides, lang)
    val modelCodegen = new ModelCodegen(modelPkg, typeMapper, lang, jsonLib, validationSupport)
    val apiCodegen = new ApiCodegen(apiPkg, typeMapper, lang, jsonLib, frameworkSupport, sumTypeNames)

    val files = List.newBuilder[jvm.File]

    // Generate model classes
    spec.models.foreach { model =>
      files += modelCodegen.generate(model)
    }

    // Generate sum types
    spec.sumTypes.foreach { sumType =>
      files += modelCodegen.generateSumType(sumType)
    }

    // Generate API interfaces (and response sum types)
    if (options.generateApiInterfaces) {
      spec.apis.foreach { api =>
        files ++= apiCodegen.generate(api)
      }
    }

    Result(files.result(), Nil)
  }
}
