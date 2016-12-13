package org.bnjzer.closestairport.utils

import org.bnjzer.closestairport.UnitSpec

import scala.util.Success

class LatLongUtilsSpec extends UnitSpec {
  "parseLatitude" should "correctly parse a valid latitude" in {
    LatLongUtils.parseLatitude("-85.05") shouldBe Success(-85.05)
    LatLongUtils.parseLatitude("85.05") shouldBe Success(85.05)
    LatLongUtils.parseLatitude("0.0") shouldBe Success(0.0)
    LatLongUtils.parseLatitude("-15.41") shouldBe Success(-15.41)
    LatLongUtils.parseLatitude("41.52") shouldBe Success(41.52)
  }

  it should "fail if the latitude is too small or too big" in {
    assertThrows[IllegalArgumentException] {
      LatLongUtils.parseLatitude("-85.06").get
    }

    assertThrows[IllegalArgumentException] {
      LatLongUtils.parseLatitude("90.0").get
    }
  }

  it should "fail if the latitude is not a valid number" in {
    assertThrows[NumberFormatException] {
      LatLongUtils.parseLatitude("80.0foo").get
    }
  }

  "parseLongitude" should "correctly parse a valid longitude" in {
    LatLongUtils.parseLongitude("-180.0") shouldBe Success(-180.0)
    LatLongUtils.parseLongitude("180.0") shouldBe Success(180.0)
    LatLongUtils.parseLongitude("0.0") shouldBe Success(0.0)
    LatLongUtils.parseLongitude("-15.41") shouldBe Success(-15.41)
    LatLongUtils.parseLongitude("41.52") shouldBe Success(41.52)
  }

  it should "fail if the longitude is too small or too big" in {
    assertThrows[IllegalArgumentException] {
      LatLongUtils.parseLongitude("-180.1").get
    }

    assertThrows[IllegalArgumentException] {
      LatLongUtils.parseLongitude("180.1").get
    }
  }

  it should "fail if the longitude is not a valid number" in {
    assertThrows[NumberFormatException] {
      LatLongUtils.parseLongitude("40.0foo").get
    }
  }

  "parseCoordinates" should "succeed when latitude and longitude are both valid" in {
    LatLongUtils.parseCoordinates("6.52", "-8.12") shouldBe Success(Coordinates(6.52, -8.12))
  }

  it should "fail when latitude is not valid and longitude valid" in {
    assert(LatLongUtils.parseCoordinates("-200", "65.4").isFailure)
  }

  it should "fail when latitude is valid and longitude not valid" in {
    assert(LatLongUtils.parseCoordinates("-20.4", "6e5.4").isFailure)
  }

  it should "fail when both latitude and longitude are not valid" in {
    assert(LatLongUtils.parseCoordinates("aa", "400").isFailure)
  }
}
