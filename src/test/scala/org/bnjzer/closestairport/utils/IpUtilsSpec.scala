package org.bnjzer.closestairport.utils

import org.bnjzer.closestairport.UnitSpec

import scala.util.Success

class IpUtilsSpec extends UnitSpec {

  "ipFieldToBinary" should "successfully process a valid IP field" in {
    IpUtils.ipFieldToBinary("192") shouldBe Success("11000000")
    IpUtils.ipFieldToBinary("255") shouldBe Success("11111111")
    IpUtils.ipFieldToBinary("0") shouldBe Success("00000000")
    IpUtils.ipFieldToBinary("3") shouldBe Success("00000011")
  }

  it should "fail when the IP field is not valid" in {
    assert(IpUtils.ipFieldToBinary("").isFailure)
    assert(IpUtils.ipFieldToBinary("-1").isFailure)
    assert(IpUtils.ipFieldToBinary("256").isFailure)
    assert(IpUtils.ipFieldToBinary("3.0").isFailure)
    assert(IpUtils.ipFieldToBinary("a").isFailure)
  }

  "ipToBinaryIp" should "successfully process a valid IP" in {
    IpUtils.ipToBinaryIp("192.192.192.192") shouldBe Success("11000000110000001100000011000000")
    IpUtils.ipToBinaryIp("255.0.0.255") shouldBe Success("11111111000000000000000011111111")
    IpUtils.ipToBinaryIp("10.128.1.255") shouldBe Success("00001010100000000000000111111111")
  }

  it should "fail when the input IP is not valid" in {
    assert(IpUtils.ipToBinaryIp("10.10.10.10a").isFailure)
    assert(IpUtils.ipToBinaryIp("e.25.47.52").isFailure)
    assert(IpUtils.ipToBinaryIp("57.75.653.,").isFailure)
    assert(IpUtils.ipToBinaryIp("256.2.2.2").isFailure)
    assert(IpUtils.ipToBinaryIp("256.2.2").isFailure)
  }

  "cidrToBinaryMask" should "correctly process a valid CIDR notation" in {
    IpUtils.cidrToBinaryMask("192.192.192.0/24") shouldBe Success("110000001100000011000000")
    IpUtils.cidrToBinaryMask("192.192.192.0/8") shouldBe Success("11000000")
    IpUtils.cidrToBinaryMask("1.0.1.0/24") shouldBe Success("000000010000000000000001")
    IpUtils.cidrToBinaryMask("1.0.1.0/23") shouldBe Success("00000001000000000000000")
    IpUtils.cidrToBinaryMask("192.128.0.0/9") shouldBe Success("110000001")
    IpUtils.cidrToBinaryMask("192.128.0.0/8") shouldBe Success("11000000")
  }

  it should "fail when the CIDR notation is not valid" in {
    assert(IpUtils.cidrToBinaryMask("10.10.10.10/33").isFailure)
    assert(IpUtils.cidrToBinaryMask("10.10.10.10/0").isFailure)
    assert(IpUtils.cidrToBinaryMask("10.10.a.10/24").isFailure)
    assert(IpUtils.cidrToBinaryMask("10.10.10.10.10/16").isFailure)
    assert(IpUtils.cidrToBinaryMask("256.10.10.10/24").isFailure)
  }

  "getPossibleBinaryMasks" should "return all the possible matching binary masks for an IP" in {
    IpUtils.getPossibleBinaryMasks("192.192.192.192") shouldBe Success(Seq("1", "11", "110", "1100", "11000", "110000",
      "1100000", "11000000", "110000001", "1100000011", "11000000110", "110000001100", "1100000011000", "11000000110000",
      "110000001100000", "1100000011000000", "11000000110000001", "110000001100000011", "1100000011000000110",
      "11000000110000001100", "110000001100000011000", "1100000011000000110000", "11000000110000001100000",
      "110000001100000011000000", "1100000011000000110000001", "11000000110000001100000011",
      "110000001100000011000000110", "1100000011000000110000001100", "11000000110000001100000011000",
      "110000001100000011000000110000", "1100000011000000110000001100000", "11000000110000001100000011000000").reverse)
  }
}
