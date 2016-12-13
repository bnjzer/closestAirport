package org.bnjzer.closestairport.utils

import scala.util.{Try, Success, Failure}

object LatLongUtils {

  /**
    * Parses a string containing a latitude.
    *
    * @param strLat String containing a latitude.
    * @return [[Success]] with a [[Double]] if the latitude could be correctly parsed
    *         or [[Failure]] otherwise.
    */
  def parseLatitude(strLat: String): Try[Double] = Try {
    val min = -85.05
    val max = 85.05
    val doubleLat = strLat.toDouble
    if (doubleLat < min || max < doubleLat)
      throw new IllegalArgumentException(s"Latitude must be between $min and $max")
    doubleLat
  }

  /**
    * Parses a string containing a longitude.
    *
    * @param strLong String containing a longitude.
    * @return [[Success]] with a [[Double]] if the longitude could be correctly parsed
    *         or [[Failure]] otherwise.
    */
  def parseLongitude(strLong: String): Try[Double] = Try {
    val min = -180.0
    val max = 180.0
    val doubleLong = strLong.toDouble
    if (doubleLong < min || max < doubleLong)
      throw new IllegalArgumentException(s"Longitude must be between $min and $max")
    doubleLong
  }

  /**
    * Creates [[Coordinates]] from a latitude and a longitude, provided that they can both be well parsed from string.
    *
    * @param lat  String containing the latitude.
    * @param long String containing the longitude.
    * @return [[Success]] with [[Coordinates]] if both latitude and longitude could be well parsed
    *         or [[Failure]] if at least one of them couldn't.
    */
  def parseCoordinates(lat: String, long: String): Try[Coordinates] = {
    for (parsedLat <- parseLatitude(lat); parsedLong <- parseLongitude(long))
      yield Coordinates(parsedLat, parsedLong)
  }
}
