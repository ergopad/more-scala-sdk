package sdk.api

import sdk.models.{ExampleRequest, ExampleResponse}

object ExampleAPI {
  def handle(exampleRequest: ExampleRequest): ExampleResponse = {
    ExampleResponse(exampleRequest.method, exampleRequest.example_key)
  }
}
