package org.bnjzer.closestairport.airports

import scala.util.{Failure, Success, Try}
import scala.util.matching.Regex

object AirportUtils {

  /**
    * Checks that the IATA code is valid
    *
    * @param iata IATA code that we want to check.
    * @return [[Success]] if the code is valid, [[Failure]] otherwise.
    */
  def checksIATA(iata: String): Try[String] = {
    val regex = new Regex("^[A-Z]{3}$")
    iata.trim() match {
      case regex() => Success(iata.trim)
      case _ => Failure(new IllegalArgumentException(s"$iata is not a valid IATA airport code"))
    }
  }
}
