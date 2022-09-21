package sdk.models.exceptions

class HttpException(message: String, statusCode: Int = 400) extends Exception(message) {
  def this(message: String, statusCode: Int, cause: Throwable) {
    this(message, statusCode)
    initCause(cause)
  }

  def getStatusCode: Int = {
    statusCode
  }
}
