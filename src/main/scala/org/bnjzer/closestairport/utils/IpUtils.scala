package org.bnjzer.closestairport.utils

import scala.util.matching.Regex
import scala.util.{Failure, Success, Try}

object IpUtils {

  /**
    * Converts a number that must be between 0 and 255 in its binary representation.
    *
    * @param ipField String that must represents an integer between 0 and 255.
    * @return [[Success]] with the string with 8 digits that corresponds to the binary representation of the integer
    *         or [[Failure]] if the field is not valid.
    */
  def ipFieldToBinary(ipField: String): Try[String] = {
    val fieldFormat = new Regex("^\\d+$")
    ipField.trim match {
      case fieldFormat() => {
        val iField = ipField.toInt
        if (iField < 0 || 255 < iField)
          Failure(new IllegalArgumentException(s"$iField is not between 0 and 255"))
        else
          Success(f"${iField.toBinaryString.toInt}%08d")
      }
      case _ => Failure(new IllegalArgumentException(s"IP field $ipField doesn't contain only numbers"))
    }
  }

  /**
    * Converts an IP in the form 192.168.1.10 in its binary representation, i.e 11000000101010000000000100001010.
    *
    * @param ip IP in the form 192.168.1.10
    * @return [[Success]] with the binary representation of the IP
    *         or [[Failure]] if it couln't be correctly treated, because it was not a valid IP
    */
  def ipToBinaryIp(ip: String): Try[String] = {
    ip.split("\\.") match {
      case Array(i, j, k, l) => Try(Seq(i, j, k, l).map(ipFieldToBinary).map(_.get)) match {
        case Success(fields) => Success(fields.mkString(""))
        case Failure(e) =>
          Failure(new IllegalArgumentException(s"Problem with at least one of the field : ${e.getMessage}"))
      }
      case _ => Failure(new IllegalArgumentException(s"IP $ip not in the form w.x.y.z"))
    }
  }

  /**
    * Transforms an IPv4 range block in CIDR notation into the corresponding binary mask.
    *
    * For example 1.0.1.0/24 would become 000000010000000000000001.
    *
    * @param cidr IPv4 range in CIDR notation, e.g 1.0.1.0/24.
    * @return [[Success]] with the string representation of the binary mask, e.g 000000010000000000000001
    *         or [[Failure]] if the input string couldn't be correctly treated.
    *
    */
  def cidrToBinaryMask(cidr: String): Try[String] = {
    cidr.split("/") match {
      case Array(ip, len) => {
        val lengthFormat = new Regex("^\\d{1,2}+$")
        val parsedLength = len.trim match {
          case lengthFormat() => {
            val iLength = len.toInt
            if (iLength < 1 || 32 < iLength)
              Failure(new IllegalArgumentException(s"Length of CIDR notation ($iLength) isn't between 1 and 32"))
            else
              Success(iLength)
          }
          case _ =>
            Failure(new IllegalArgumentException(s"Length of CIDR notation ($len} must be a number with 1 or 2 digits"))
        }
        parsedLength match {
          case Success(iLen) => IpUtils.ipToBinaryIp(ip).flatMap(binIp => Try(binIp.substring(0, iLen)))
          case Failure(e) => Failure(e)
        }
      }
      case _ => Failure(new IllegalArgumentException(s"CIDR notation $cidr not in the form IP/length"))
    }
  }

  /**
    * Gets all the possible IP masks (with a binary format) that could match a given IP.
    *
    * @return [[Success]] with all the binary masks if the IP is valid and [[Failure]] otherwise.
    */
  def getPossibleBinaryMasks(ip: String): Try[Seq[String]] = {
    ipToBinaryIp(ip) match {
      case Success(binIp) => Success((1 to 32).reverse.map(binIp.substring(0, _)))
      case Failure(e) => Failure(e)
    }
  }
}
