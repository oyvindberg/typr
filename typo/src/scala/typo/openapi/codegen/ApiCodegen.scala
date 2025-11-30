package typo.openapi.codegen

import typo.{jvm, Lang, Scope}
import typo.internal.codegen._
import typo.openapi._

/** Generates jvm.File for API interfaces */
class ApiCodegen(
    apiPkg: jvm.QIdent,
    typeMapper: TypeMapper,
    lang: Lang,
    jsonLib: JsonLibSupport,
    frameworkSupport: FrameworkSupport,
    sumTypeNames: Set[String]
) {

  /** Generate API interface file and any associated response sum type files */
  def generate(api: ApiInterface): List[jvm.File] = {
    val tpe = jvm.Type.Qualified(apiPkg / jvm.Ident(api.name))
    val comments = api.description.map(d => jvm.Comments(List(d))).getOrElse(jvm.Comments.Empty)

    // Generate response sum types for methods that have multiple response variants
    val responseSumTypeFiles = api.methods.flatMap { method =>
      method.responseVariants.map { variants =>
        generateResponseSumType(method.name, variants)
      }
    }

    // Find common base path for all methods in this API
    val basePath = findCommonBasePath(api.methods.map(_.path))

    // Generate methods with paths relative to base path
    val methods = api.methods.map(m => generateMethod(m, basePath))

    // Framework annotations on interface (e.g., @Path for JAX-RS)
    val interfaceAnnotations = frameworkSupport.interfaceAnnotations(basePath)

    val interface = jvm.Adt.Sum(
      annotations = interfaceAnnotations,
      comments = comments,
      name = tpe,
      tparams = Nil,
      members = methods,
      implements = Nil,
      subtypes = Nil,
      staticMembers = Nil
    )

    val generatedCode = lang.renderTree(interface, lang.Ctx.Empty)
    val apiFile = jvm.File(tpe, generatedCode, secondaryTypes = Nil, scope = Scope.Main)

    apiFile :: responseSumTypeFiles
  }

  /** Find the common base path prefix for a list of paths */
  private def findCommonBasePath(paths: List[String]): Option[String] = {
    if (paths.isEmpty) return None

    // Split paths into segments
    val segments = paths.map(_.split("/").filter(_.nonEmpty).toList)
    if (segments.exists(_.isEmpty)) return None

    // Find common prefix segments (but not path parameters like {id})
    val firstSegments = segments.head
    val commonSegments = firstSegments.takeWhile { seg =>
      !seg.startsWith("{") && segments.forall(_.headOption.contains(seg))
    }

    if (commonSegments.isEmpty) None
    else Some("/" + commonSegments.mkString("/"))
  }

  /** Calculate relative path from base path */
  private def relativePath(fullPath: String, basePath: Option[String]): String = {
    basePath match {
      case Some(base) if fullPath.startsWith(base) =>
        val relative = fullPath.stripPrefix(base)
        if (relative.isEmpty) "/" else relative
      case _ => fullPath
    }
  }

  /** Generate a response sum type for methods with multiple response variants */
  private def generateResponseSumType(methodName: String, variants: List[ResponseVariant]): jvm.File = {
    // Check for nested sum types
    variants.foreach { variant =>
      variant.typeInfo match {
        case TypeInfo.Ref(name) if sumTypeNames.contains(name) =>
          throw new IllegalArgumentException(
            s"Nested sum types are not supported: method '${methodName}' has response status '${variant.statusCode}' " +
              s"with type '$name' which is a sum type. Consider inlining the sum type or using a wrapper."
          )
        case _ => // OK
      }
    }

    val responseName = capitalize(methodName) + "Response"
    val tpe = jvm.Type.Qualified(apiPkg / jvm.Ident(responseName))

    // Generate subtypes for each status code
    val subtypes = variants.map { variant =>
      val statusName = "Status" + normalizeStatusCode(variant.statusCode)
      val subtypeTpe = jvm.Type.Qualified(apiPkg / jvm.Ident(responseName) / jvm.Ident(statusName))
      val valueType = typeMapper.map(variant.typeInfo)

      val valueParam = jvm.Param[jvm.Type](
        annotations = jsonLib.propertyAnnotations("value"),
        comments = jvm.Comments.Empty,
        name = jvm.Ident("value"),
        tpe = valueType,
        default = None
      )

      // Override status() method to return the status code
      // isLazy=true makes LangJava render this as a method override instead of a field
      val statusOverride = jvm.Value(
        annotations = Nil,
        name = jvm.Ident("status"),
        tpe = Types.String,
        body = Some(jvm.StrLit(variant.statusCode).code),
        isLazy = true,
        isOverride = true
      )

      jvm.Adt.Record(
        annotations = Nil,
        isWrapper = false,
        comments = variant.description.map(d => jvm.Comments(List(d))).getOrElse(jvm.Comments.Empty),
        name = subtypeTpe,
        tparams = Nil,
        params = List(valueParam),
        implicitParams = Nil,
        `extends` = None,
        implements = List(tpe),
        members = List(statusOverride),
        staticMembers = Nil
      )
    }

    // Abstract status method in the sealed interface
    val statusMethod = jvm.Method(
      annotations = jsonLib.propertyAnnotations("status"),
      comments = jvm.Comments.Empty,
      tparams = Nil,
      name = jvm.Ident("status"),
      params = Nil,
      implicitParams = Nil,
      tpe = Types.String,
      throws = Nil,
      body = Nil
    )

    // Jackson annotations for polymorphic deserialization
    val jacksonAnnotations = generateResponseSumTypeAnnotations(tpe, variants)

    val sumAdt = jvm.Adt.Sum(
      annotations = jacksonAnnotations,
      comments = jvm.Comments.Empty,
      name = tpe,
      tparams = Nil,
      members = List(statusMethod),
      implements = Nil,
      subtypes = subtypes,
      staticMembers = Nil
    )

    val generatedCode = lang.renderTree(sumAdt, lang.Ctx.Empty)
    jvm.File(tpe, generatedCode, secondaryTypes = Nil, scope = Scope.Main)
  }

  private def generateResponseSumTypeAnnotations(tpe: jvm.Type.Qualified, variants: List[ResponseVariant]): List[jvm.Annotation] = {
    val typeInfoAnnotation = jvm.Annotation(
      Types.Jackson.JsonTypeInfo,
      List(
        jvm.Annotation.Arg.Named(jvm.Ident("use"), code"${Types.Jackson.JsonTypeInfo}.Id.NAME"),
        jvm.Annotation.Arg.Named(jvm.Ident("include"), code"${Types.Jackson.JsonTypeInfo}.As.PROPERTY"),
        jvm.Annotation.Arg.Named(jvm.Ident("property"), jvm.StrLit("status").code)
      )
    )

    val subTypesArgs = variants.map { variant =>
      val statusName = "Status" + normalizeStatusCode(variant.statusCode)
      code"@${Types.Jackson.JsonSubTypes}.Type(value = $tpe.$statusName.class, name = ${jvm.StrLit(variant.statusCode)})"
    }

    val subTypesAnnotation = jvm.Annotation(
      Types.Jackson.JsonSubTypes,
      List(jvm.Annotation.Arg.Positional(code"{ ${jvm.Code.Combined(subTypesArgs.flatMap(c => List(c, code", ")).dropRight(1))} }"))
    )

    List(typeInfoAnnotation, subTypesAnnotation)
  }

  private def normalizeStatusCode(statusCode: String): String = {
    // Convert status codes like "2XX", "default" to valid identifiers
    statusCode.toLowerCase match {
      case "default" => "Default"
      case "2xx"     => "2XX"
      case "4xx"     => "4XX"
      case "5xx"     => "5XX"
      case s         => s
    }
  }

  private def capitalize(s: String): String =
    if (s.isEmpty) s else s.head.toUpper.toString + s.tail

  private def generateMethod(method: ApiMethod, basePath: Option[String]): jvm.Method = {
    val comments = method.description.map(d => jvm.Comments(List(d))).getOrElse(jvm.Comments.Empty)

    // Generate parameters
    val params = generateParams(method)

    // Determine return type
    val returnType = inferReturnType(method)

    // Create method with relative path for framework annotations
    val methodWithRelativePath = method.copy(path = relativePath(method.path, basePath))

    // Framework annotations (@GET, @POST, @Path, @Produces, @Consumes)
    val frameworkAnnotations = frameworkSupport.methodAnnotations(methodWithRelativePath)

    // Add deprecation annotation if needed
    val deprecationAnnotation = if (method.deprecated) {
      List(jvm.Annotation(jvm.Type.Qualified("java.lang.Deprecated"), Nil))
    } else {
      Nil
    }

    jvm.Method(
      annotations = frameworkAnnotations ++ deprecationAnnotation,
      comments = comments,
      tparams = Nil,
      name = jvm.Ident(method.name),
      params = params,
      implicitParams = Nil,
      tpe = returnType,
      throws = Nil,
      body = Nil // Interface method - no body
    )
  }

  private def generateParams(method: ApiMethod): List[jvm.Param[jvm.Type]] = {
    // Add path, query, header parameters
    val methodParams = method.parameters.map { param =>
      val paramType = typeMapper.map(param.typeInfo)
      val annotations = generateParamAnnotations(param)

      jvm.Param[jvm.Type](
        annotations = annotations,
        comments = param.description.map(d => jvm.Comments(List(d))).getOrElse(jvm.Comments.Empty),
        name = jvm.Ident(param.name),
        tpe = paramType,
        default = None
      )
    }

    // Add request body as parameter if present
    val bodyParam = method.requestBody.toList.map { body =>
      val bodyType = typeMapper.map(body.typeInfo)
      val bodyAnnotations = generateBodyAnnotations(body)

      jvm.Param[jvm.Type](
        annotations = bodyAnnotations,
        comments = body.description.map(d => jvm.Comments(List(d))).getOrElse(jvm.Comments.Empty),
        name = jvm.Ident("body"),
        tpe = bodyType,
        default = None
      )
    }

    methodParams ++ bodyParam
  }

  private def generateParamAnnotations(param: ApiParameter): List[jvm.Annotation] = {
    // Framework annotations (@PathParam, @QueryParam, etc.)
    val frameworkAnnotations = frameworkSupport.parameterAnnotations(param)

    // JSON property annotation for name mapping if different
    val jsonAnnotations = if (param.name != param.originalName) {
      jsonLib.propertyAnnotations(param.originalName)
    } else {
      Nil
    }

    frameworkAnnotations ++ jsonAnnotations
  }

  private def generateBodyAnnotations(body: RequestBody): List[jvm.Annotation] = {
    frameworkSupport.bodyAnnotations(body)
  }

  private def inferReturnType(method: ApiMethod): jvm.Type = {
    // If there are response variants, return the response sum type
    method.responseVariants match {
      case Some(_) =>
        val responseName = capitalize(method.name) + "Response"
        jvm.Type.Qualified(apiPkg / jvm.Ident(responseName))

      case None =>
        // Find the success response (2xx or default)
        val successResponse = method.responses
          .find(r => isSuccessStatus(r.statusCode))
          .orElse(method.responses.find(_.statusCode == ResponseStatus.Default))

        successResponse.flatMap(_.typeInfo) match {
          case Some(typeInfo) => typeMapper.map(typeInfo)
          case None           => Types.Void
        }
    }
  }

  private def isSuccessStatus(status: ResponseStatus): Boolean = status match {
    case ResponseStatus.Specific(code) => code >= 200 && code < 300
    case ResponseStatus.Success2XX     => true
    case _                             => false
  }
}

/** Types used in API generation */
object ApiTypes {
  val Void = jvm.Type.Qualified("java.lang.Void")
}
