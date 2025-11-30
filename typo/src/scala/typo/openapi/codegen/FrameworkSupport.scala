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
