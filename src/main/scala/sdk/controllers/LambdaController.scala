package sdk.controllers

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import com.google.gson.Gson
import sdk.api.ExampleAPI
import sdk.models.ExampleRequest
import sdk.models.exceptions.HttpException

import java.io.{ByteArrayInputStream, InputStream, OutputStream}
import scala.io.Source

case class PingRequest(method: String, id: String)

case class PingResponse(method: String, id: String, status: String)

case class ErrorResponse(statusCode: Int, headers: Map[String, String], body: String)

case class SuccessResponse(isBase64Encoded: Boolean, statusCode: Int, headers: Map[String, String], body: Any)

/**
 * Default Controller for AWS Lambda
 */
class LambdaController {
  val gson: Gson = new Gson()
  val scalaMapper: ObjectMapper = {
    new ObjectMapper().registerModule(new DefaultScalaModule)
  }

  def handleLambdaProxyRequest(input: InputStream, output: OutputStream): Unit = {
    try {
      val rawRequestBody = scalaMapper.readValue(input, classOf[Map[String, _]])
        .getOrElse("body", "{}")
        .toString
      val parsedRequestBody = scalaMapper.readValue(new ByteArrayInputStream(rawRequestBody.getBytes), classOf[Map[String, _]])
      val requestBody = scalaMapper.writeValueAsBytes(parsedRequestBody)
      handleRequest(new ByteArrayInputStream(requestBody), output, httpWrap = true)
    } catch {
      case t: Throwable => scalaMapper.writeValue(output, generateErrorResponse(t.toString, 500))
    }
  }

  def handleRequest(input: InputStream, output: OutputStream, httpWrap: Boolean = false): Unit = {
    try {
      val rawInput = Source.fromInputStream(input).mkString
      val requestMap = scalaMapper.readValue(new ByteArrayInputStream(rawInput.getBytes), classOf[Map[String, _]])
      val request = new ByteArrayInputStream(rawInput.getBytes)
      if (!requestMap.contains("method")) {
        throw new HttpException("Method is not defined", 400)
      }
      requestMap("method") match {
        case "ping" => handleRoutedRequest(request, output, classOf[PingRequest], handlePing, httpWrap)
        case "example" => handleRoutedRequest(request, output, classOf[ExampleRequest], ExampleAPI.handle, httpWrap)
        case _ => throw new HttpException("Method not found", 404)
      }
    } catch {
      case e: HttpException => scalaMapper.writeValue(output, generateErrorResponse(e.getMessage, e.getStatusCode))
      case t: Throwable => scalaMapper.writeValue(output, generateErrorResponse(t.toString, 500))
    }
  }

  def handlePing(ping: PingRequest): PingResponse = {
    PingResponse(ping.method, ping.id, "ok")
  }

  def handleRoutedRequest[T](input: InputStream, output: OutputStream, inputType: Class[T], handler: T => _,
                             httpWrap: Boolean): Unit = {
    if (httpWrap)
      scalaMapper.writeValue(output, generateSuccessResponse(handler(scalaMapper.readValue(input, inputType))))
    else
      scalaMapper.writeValue(output, handler(scalaMapper.readValue(input, inputType)))
  }

  def generateSuccessResponse(body: Any): SuccessResponse = {
    val headers = Map("Content-Type" -> "application/json")
    SuccessResponse(isBase64Encoded = false, 200, headers, scalaMapper.writeValueAsString(body))
  }

  def generateErrorResponse(message: String, statusCode: Int): ErrorResponse = {
    val headers = Map("Content-Type" -> "application/json")
    ErrorResponse(statusCode, headers, message)
  }
}
