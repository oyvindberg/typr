package typo.openapi.codegen

import typo.jvm
import typo.internal.codegen._
import typo.openapi.{ApiMethod, ApiParameter, HttpMethod, ParameterIn, RequestBody}

/** Framework-specific annotation generation for API interfaces */
trait FrameworkSupport {

  /** Annotations to add to the API interface itself */
  def interfaceAnnotations(basePath: Option[String]): List[jvm.Annotation]

  /** Annotations to add to a method (HTTP method, path, produces/consumes) */
  def methodAnnotations(method: ApiMethod): List[jvm.Annotation]

  /** Annotations to add to a parameter (@PathParam, @QueryParam, etc) */
  def parameterAnnotations(param: ApiParameter): List[jvm.Annotation]

  /** Annotations to add to the request body parameter */
  def bodyAnnotations(body: RequestBody): List[jvm.Annotation]
}

/** No framework annotations - just generate plain interfaces */
object NoFrameworkSupport extends FrameworkSupport {
  override def interfaceAnnotations(basePath: Option[String]): List[jvm.Annotation] = Nil
  override def methodAnnotations(method: ApiMethod): List[jvm.Annotation] = Nil
  override def parameterAnnotations(param: ApiParameter): List[jvm.Annotation] = Nil
  override def bodyAnnotations(body: RequestBody): List[jvm.Annotation] = Nil
}

/** JAX-RS (Jakarta EE) framework support */
object JaxRsSupport extends FrameworkSupport {

  override def interfaceAnnotations(basePath: Option[String]): List[jvm.Annotation] = {
    basePath.map { path =>
      jvm.Annotation(
        Types.JaxRs.Path,
        List(jvm.Annotation.Arg.Positional(jvm.StrLit(path).code))
      )
    }.toList
  }

  override def methodAnnotations(method: ApiMethod): List[jvm.Annotation] = {
    val httpMethodAnnotation = httpMethodToAnnotation(method.httpMethod)

    val pathAnnotation = jvm.Annotation(
      Types.JaxRs.Path,
      List(jvm.Annotation.Arg.Positional(jvm.StrLit(method.path).code))
    )

    // Determine content types from request/response
    val consumesAnnotation = method.requestBody.map { body =>
      jvm.Annotation(
        Types.JaxRs.Consumes,
        List(jvm.Annotation.Arg.Positional(mediaTypeConstant(body.contentType)))
      )
    }

    val producesAnnotation = method.responses.headOption.flatMap(_.contentType).map { contentType =>
      jvm.Annotation(
        Types.JaxRs.Produces,
        List(jvm.Annotation.Arg.Positional(mediaTypeConstant(contentType)))
      )
    }

    List(httpMethodAnnotation, pathAnnotation) ++ consumesAnnotation.toList ++ producesAnnotation.toList
  }

  override def parameterAnnotations(param: ApiParameter): List[jvm.Annotation] = {
    val paramAnnotation = param.in match {
      case ParameterIn.Path =>
        jvm.Annotation(
          Types.JaxRs.PathParam,
          List(jvm.Annotation.Arg.Positional(jvm.StrLit(param.originalName).code))
        )
      case ParameterIn.Query =>
        jvm.Annotation(
          Types.JaxRs.QueryParam,
          List(jvm.Annotation.Arg.Positional(jvm.StrLit(param.originalName).code))
        )
      case ParameterIn.Header =>
        jvm.Annotation(
          Types.JaxRs.HeaderParam,
          List(jvm.Annotation.Arg.Positional(jvm.StrLit(param.originalName).code))
        )
      case ParameterIn.Cookie =>
        jvm.Annotation(
          Types.JaxRs.CookieParam,
          List(jvm.Annotation.Arg.Positional(jvm.StrLit(param.originalName).code))
        )
    }

    val defaultValueAnnotation = param.defaultValue.map { value =>
      jvm.Annotation(
        Types.JaxRs.DefaultValue,
        List(jvm.Annotation.Arg.Positional(jvm.StrLit(value).code))
      )
    }

    List(paramAnnotation) ++ defaultValueAnnotation.toList
  }

  override def bodyAnnotations(body: RequestBody): List[jvm.Annotation] = {
    // JAX-RS doesn't require special annotations for the body parameter
    // The unannotated parameter is treated as the request body
    Nil
  }

  private def httpMethodToAnnotation(method: HttpMethod): jvm.Annotation = {
    val tpe = method match {
      case HttpMethod.Get     => Types.JaxRs.GET
      case HttpMethod.Post    => Types.JaxRs.POST
      case HttpMethod.Put     => Types.JaxRs.PUT
      case HttpMethod.Delete  => Types.JaxRs.DELETE
      case HttpMethod.Patch   => Types.JaxRs.PATCH
      case HttpMethod.Head    => Types.JaxRs.HEAD
      case HttpMethod.Options => Types.JaxRs.OPTIONS
    }
    jvm.Annotation(tpe, Nil)
  }

  private def mediaTypeConstant(contentType: String): jvm.Code = {
    contentType match {
      case "application/json"                  => code"${Types.JaxRs.MediaType}.APPLICATION_JSON"
      case "application/xml"                   => code"${Types.JaxRs.MediaType}.APPLICATION_XML"
      case "text/plain"                        => code"${Types.JaxRs.MediaType}.TEXT_PLAIN"
      case "application/octet-stream"          => code"${Types.JaxRs.MediaType}.APPLICATION_OCTET_STREAM"
      case "application/x-www-form-urlencoded" => code"${Types.JaxRs.MediaType}.APPLICATION_FORM_URLENCODED"
      case "multipart/form-data"               => code"${Types.JaxRs.MediaType}.MULTIPART_FORM_DATA"
      case other                               => jvm.StrLit(other).code
    }
  }
}

/** Spring Boot / Spring MVC framework support */
object SpringBootSupport extends FrameworkSupport {

  override def interfaceAnnotations(basePath: Option[String]): List[jvm.Annotation] = {
    val restController = jvm.Annotation(Types.Spring.RestController, Nil)

    val requestMapping = basePath.map { path =>
      jvm.Annotation(
        Types.Spring.RequestMapping,
        List(jvm.Annotation.Arg.Positional(jvm.StrLit(path).code))
      )
    }

    List(restController) ++ requestMapping.toList
  }

  override def methodAnnotations(method: ApiMethod): List[jvm.Annotation] = {
    val mappingAnnotation = httpMethodToMapping(method.httpMethod, method.path, method.requestBody, method.responses.headOption.flatMap(_.contentType))

    List(mappingAnnotation)
  }

  override def parameterAnnotations(param: ApiParameter): List[jvm.Annotation] = {
    val paramAnnotation = param.in match {
      case ParameterIn.Path =>
        val args = List(jvm.Annotation.Arg.Positional(jvm.StrLit(param.originalName).code)) ++
          (if (!param.required) List(jvm.Annotation.Arg.Named(jvm.Ident("required"), code"false")) else Nil)
        jvm.Annotation(Types.Spring.PathVariable, args)

      case ParameterIn.Query =>
        val args = List.newBuilder[jvm.Annotation.Arg]
        args += jvm.Annotation.Arg.Named(jvm.Ident("name"), jvm.StrLit(param.originalName).code)
        if (!param.required) {
          args += jvm.Annotation.Arg.Named(jvm.Ident("required"), code"false")
        }
        param.defaultValue.foreach { value =>
          args += jvm.Annotation.Arg.Named(jvm.Ident("defaultValue"), jvm.StrLit(value).code)
        }
        jvm.Annotation(Types.Spring.RequestParam, args.result())

      case ParameterIn.Header =>
        val args = List(jvm.Annotation.Arg.Named(jvm.Ident("name"), jvm.StrLit(param.originalName).code)) ++
          (if (!param.required) List(jvm.Annotation.Arg.Named(jvm.Ident("required"), code"false")) else Nil)
        jvm.Annotation(Types.Spring.RequestHeader, args)

      case ParameterIn.Cookie =>
        val args = List(jvm.Annotation.Arg.Named(jvm.Ident("name"), jvm.StrLit(param.originalName).code)) ++
          (if (!param.required) List(jvm.Annotation.Arg.Named(jvm.Ident("required"), code"false")) else Nil)
        jvm.Annotation(Types.Spring.CookieValue, args)
    }

    List(paramAnnotation)
  }

  override def bodyAnnotations(body: RequestBody): List[jvm.Annotation] = {
    // Spring requires @RequestBody annotation
    List(jvm.Annotation(Types.Spring.RequestBody, Nil))
  }

  private def httpMethodToMapping(
      method: HttpMethod,
      path: String,
      requestBody: Option[RequestBody],
      responseContentType: Option[String]
  ): jvm.Annotation = {
    val mappingType = method match {
      case HttpMethod.Get     => Types.Spring.GetMapping
      case HttpMethod.Post    => Types.Spring.PostMapping
      case HttpMethod.Put     => Types.Spring.PutMapping
      case HttpMethod.Delete  => Types.Spring.DeleteMapping
      case HttpMethod.Patch   => Types.Spring.PatchMapping
      case HttpMethod.Head    => Types.Spring.GetMapping // Spring doesn't have HeadMapping
      case HttpMethod.Options => Types.Spring.RequestMapping // Use RequestMapping with method
    }

    val args = List.newBuilder[jvm.Annotation.Arg]

    // Path (using "value" for the path)
    args += jvm.Annotation.Arg.Named(jvm.Ident("value"), jvm.StrLit(path).code)

    // Consumes
    requestBody.foreach { body =>
      args += jvm.Annotation.Arg.Named(jvm.Ident("consumes"), springMediaTypeConstant(body.contentType))
    }

    // Produces
    responseContentType.foreach { contentType =>
      args += jvm.Annotation.Arg.Named(jvm.Ident("produces"), springMediaTypeConstant(contentType))
    }

    jvm.Annotation(mappingType, args.result())
  }

  private def springMediaTypeConstant(contentType: String): jvm.Code = {
    contentType match {
      case "application/json"                  => code"${Types.Spring.MediaType}.APPLICATION_JSON_VALUE"
      case "application/xml"                   => code"${Types.Spring.MediaType}.APPLICATION_XML_VALUE"
      case "text/plain"                        => code"${Types.Spring.MediaType}.TEXT_PLAIN_VALUE"
      case "application/octet-stream"          => code"${Types.Spring.MediaType}.APPLICATION_OCTET_STREAM_VALUE"
      case "application/x-www-form-urlencoded" => code"${Types.Spring.MediaType}.APPLICATION_FORM_URLENCODED_VALUE"
      case "multipart/form-data"               => code"${Types.Spring.MediaType}.MULTIPART_FORM_DATA_VALUE"
      case other                               => jvm.StrLit(other).code
    }
  }
}
