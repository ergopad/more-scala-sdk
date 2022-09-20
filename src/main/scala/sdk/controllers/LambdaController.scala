package sdk.controllers

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import sdk.api.ExampleAPI
import sdk.models.ExampleRequest

import java.io.{ByteArrayInputStream, InputStream, OutputStream}
import scala.io.Source

case class PingRequest(method: String, id: String)

case class PingResponse(method: String, id: String, status: String)

/**
 * Default Controller for AWS Lambda
 */
class LambdaController {
  val scalaMapper: ObjectMapper = {
    new ObjectMapper().registerModule(new DefaultScalaModule)
  }

  def handleRequest(input: InputStream, output: OutputStream): Unit = {
    try {
      val rawInput = Source.fromInputStream(input).mkString
      val requestMap = scalaMapper.readValue(new ByteArrayInputStream(rawInput.getBytes), classOf[Any])
        .asInstanceOf[Map[String, _]]
      val request = new ByteArrayInputStream(rawInput.getBytes)
      requestMap("method") match {
        case "ping" => handleRoutedRequest(request, output, classOf[PingRequest], handlePing)
        case "example" => handleRoutedRequest(request, output, classOf[ExampleRequest], ExampleAPI.handle)
        case _ => scalaMapper.writeValue(output, "Method Not Found")
      }
    } catch {
      case e: Throwable => scalaMapper.writeValue(output, e.toString)
    }
  }

  def handlePing(ping: PingRequest): PingResponse = {
    PingResponse(ping.method, ping.id, "ok")
  }

  def handleRoutedRequest[T](input: InputStream, output: OutputStream, inputType: Class[T], handler: (T) => _): Unit = {
    scalaMapper.writeValue(output, handler(scalaMapper.readValue(input, inputType)))
  }
}
