package sdk.controllers

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.scala.DefaultScalaModule

import java.io.{InputStream, OutputStream}

case class Ping(id: String)

case class PingResponse(id: String, status: String)

class LambdaController {

  val scalaMapper: ObjectMapper = {
    new ObjectMapper().registerModule(new DefaultScalaModule)
  }

  def handleRequest(input: InputStream, output: OutputStream): Unit = {
    val ping = scalaMapper.readValue(input, classOf[Ping])
    val response = PingResponse(ping.id, "ok")
    scalaMapper.writeValue(output, response)
  }

  def ping(input: InputStream, output: OutputStream): Unit = {
    val result = "Service is Healthy."
    output.write(result.getBytes("UTF-8"))
  }
}
