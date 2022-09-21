package sdk.controllers

import org.scalatest.flatspec.AnyFlatSpec

import java.io.{ByteArrayInputStream, ByteArrayOutputStream, InputStream, OutputStream}

class LambdaControllerTest extends AnyFlatSpec {
  val lambdaController = new LambdaController()

  "Example test" should "always pass" in {
    assert(true)
  }

  "LambdaController" should "parse proxy body correctly [manual testing]" in {
    val sampleJson = "{}"
    val input: InputStream = new ByteArrayInputStream(sampleJson.getBytes)
    val output: OutputStream = new ByteArrayOutputStream()
    lambdaController.handleLambdaProxyRequest(input, output)
    assert(false)
  }

  "LambdaController" should "invoke ping correctly" in {
    val sampleJson = "{\"method\":\"ping\",\"id\":\"1\"}"
    val input: InputStream = new ByteArrayInputStream(sampleJson.getBytes)
    val output: OutputStream = new ByteArrayOutputStream()
    lambdaController.handleRequest(input, output)
    assert(output.toString == "{\"method\":\"ping\",\"id\":\"1\",\"status\":\"ok\"}")
  }

  "LambdaController" should "invoke example api correctly" in {
    val sampleJson = "{\"method\":\"example\",\"example_key\":\"key\"}"
    val input: InputStream = new ByteArrayInputStream(sampleJson.getBytes)
    val output: OutputStream = new ByteArrayOutputStream()
    lambdaController.handleRequest(input, output)
    assert(output.toString == "{\"method\":\"example\",\"example_key\":\"key\",\"example_value\":\"value\"}")
  }

  "LambdaController" should "return error message" in {
    val sampleJson = "{\"method\":\"unknown\"}"
    val input: InputStream = new ByteArrayInputStream(sampleJson.getBytes)
    val output: OutputStream = new ByteArrayOutputStream()
    lambdaController.handleRequest(input, output)
    assert(output.toString == "{\"statusCode\":404,\"headers\":{\"Content-Type\":\"application/json\"},\"body\":\"Method not found\"}")
  }

  "LambdaController" should "throw error on undefined method" in {
    val sampleJson = "{}"
    val input: InputStream = new ByteArrayInputStream(sampleJson.getBytes)
    val output: OutputStream = new ByteArrayOutputStream()
    lambdaController.handleRequest(input, output)
    assert(output.toString == "{\"statusCode\":400,\"headers\":{\"Content-Type\":\"application/json\"},\"body\":\"Method is not defined\"}")
  }
}
