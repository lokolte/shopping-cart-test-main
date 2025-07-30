package com.siriusxm.example.clients

import com.siriusxm.example.util.BaseTest
import weaver._

object HttpClientSpec extends SimpleIOSuite with BaseTest {
  test("Succeed when fetch product") {
    for {
      result <- httpClient.getProduct("cheerios")
    } yield expect(result == cheeriosResult)
  }

  test("Fail when fetch product with an invalid field value") {
    for {
      result <- httpClient.getProduct("invalid")
    } yield expect(result.left.map(_.message) == invalidResult.left.map(_.message))
  }

  test("Fail when fetch product with an invalid field name") {
    for {
      result <- httpClient.getProduct("parseerror")
    } yield expect(result == parseErrorResult)
  }

  test("Fail when fetch unknown product") {
    for {
      result <- httpClient.getProduct("unknown")
    } yield expect(result == notFoundResult)
  }

  test("Fail when fetch product but an error occur in the server") {
    for {
      result <- httpClient.getProduct("error")
    } yield expect(result == errorResult)
  }
}