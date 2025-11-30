package typo.openapi

import typo.jvm

/** Configuration options for OpenAPI code generation */
case class OpenApiOptions(
    /** Base package for generated code */
    pkg: jvm.QIdent,
    /** Sub-package for model classes (default: "model") */
    modelPackage: String,
    /** Sub-package for API interfaces (default: "api") */
    apiPackage: String,
    /** JSON library to use for serialization annotations */
    jsonLib: OpenApiJsonLib,
    /** Framework for API generation */
    framework: OpenApiFramework,
    /** Whether to generate wrapper types for ID-like fields */
    generateWrapperTypes: Boolean,
    /** Custom type mappings: schema name -> qualified type name */
    typeOverrides: Map[String, jvm.Type.Qualified],
    /** Whether to generate nullable fields as Optional instead of using @Nullable */
    useOptionalForNullable: Boolean,
    /** Whether to generate deprecated annotations */
    includeDeprecated: Boolean,
    /** Whether to add validation annotations (JSR-380) */
    generateValidation: Boolean,
    /** Whether to generate API interface files */
    generateApiInterfaces: Boolean
)

object OpenApiOptions {
  def default(pkg: jvm.QIdent): OpenApiOptions =
    OpenApiOptions(
      pkg = pkg,
      modelPackage = "model",
      apiPackage = "api",
      jsonLib = OpenApiJsonLib.Jackson,
      framework = OpenApiFramework.JaxRs,
      generateWrapperTypes = true,
      typeOverrides = Map.empty,
      useOptionalForNullable = false,
      includeDeprecated = true,
      generateValidation = false,
      generateApiInterfaces = true
    )
}

/** JSON library options for OpenAPI generation */
sealed trait OpenApiJsonLib
object OpenApiJsonLib {
  case object Jackson extends OpenApiJsonLib
  case object Circe extends OpenApiJsonLib
  case object PlayJson extends OpenApiJsonLib
  case object ZioJson extends OpenApiJsonLib
}

/** Framework for API generation */
sealed trait OpenApiFramework
object OpenApiFramework {

  /** JAX-RS annotations (Quarkus, Jersey, etc.) */
  case object JaxRs extends OpenApiFramework

  /** Spring Boot / Spring MVC */
  case object Spring extends OpenApiFramework

  /** http4s client/server */
  case object Http4s extends OpenApiFramework

  /** Tapir endpoints */
  case object Tapir extends OpenApiFramework

  /** Base interface only (no framework annotations) */
  case object None extends OpenApiFramework
}
