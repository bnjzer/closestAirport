package org.bnjzer.closestairport.airports

import org.apache.spark.rdd.RDD
import org.bnjzer.closestairport.utils.LatLongUtils

import scala.util.{Failure, Success, Try}

object AirportsLoader {

  /**
    * Parses the lines containing airports information.
    *
    * @param inputLines [[RDD]] with the lines of the csv file containing airports information.
    * @return Each RDD element has 2 values : a string containing the input line in the csv
    *         and a [[Success]] with the corresponding [[AirportLine]] when it was correctly parsed
    *         or a [[Failure]] when there was a problem.
    *         The utility of having the input line in the RDD is for logging purposes when something went wrong.
    */
  def parseAirportsFile(inputLines: RDD[String]): RDD[(String, Try[AirportLine])] = {
    inputLines
      // split the lines by ","
      .map(l => (l, l.split(",")))
      // assure that the lines have 3 elements
      .map(tuple2 => (tuple2._1, {
      if (tuple2._2.length == 3)
        Success(tuple2._2)
      else
        Failure(new IllegalArgumentException(s"There aren't 3 elements separated by comma: ${tuple2._1}"))
      }))
      // parse IATA notation and coordinates
      .map(tuple2 => (tuple2._1, Try {
        (AirportUtils.checksIATA(tuple2._2.get.head).get,
          LatLongUtils.parseCoordinates(tuple2._2.get(1), tuple2._2.get(2)).get)
      }))
      // create IpLine
      .map(tuple2 => (tuple2._1, Try(AirportLine(tuple2._2.get._1, tuple2._2.get._2))))
  }
}
