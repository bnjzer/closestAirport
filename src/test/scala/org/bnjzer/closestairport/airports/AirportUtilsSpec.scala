package org.bnjzer.closestairport.airports

import org.bnjzer.closestairport.UnitSpec

import scala.util.Success

class AirportUtilsSpec extends UnitSpec {
  "checksIATA" should "return Success when IATA code is valid" in {
    AirportUtils.checksIATA("AAA") shouldBe Success("AAA")
    AirportUtils.checksIATA(" AAA") shouldBe Success("AAA")
    AirportUtils.checksIATA("GSR") shouldBe Success("GSR")
  }

  it should "return Failure when IATA code is not valid" in {
    assert(AirportUtils.checksIATA("AAAA").isFailure)
    assert(AirportUtils.checksIATA("A3A").isFailure)
    assert(AirportUtils.checksIATA("AaA").isFailure)
    assert(AirportUtils.checksIATA("AA A").isFailure)
  }
}
