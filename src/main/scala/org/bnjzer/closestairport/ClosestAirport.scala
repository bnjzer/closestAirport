package org.bnjzer.closestairport

import java.io.File

import com.redis.RedisClient
import org.apache.spark.{SparkConf, SparkContext}
import org.bnjzer.closestairport.airports.AirportsLoader
import org.bnjzer.closestairport.ips.IpsLoader
import org.bnjzer.closestairport.utils.{Coordinates, IpUtils}

import scala.util.{Failure, Success, Try}

object ClosestAirport {
  val ipIndex = "ip"
  val airportIndex = "airport"

  def usage(): Unit = {
    System.err.println(
      s"""
         |Usage : ${getClass.getSimpleName} <ipsFile> <airportsFile> <usersFile> <redisHost> <redisPort>
         |
         |ipsFile: path to the CSV file containing IP ranges' geolocalisation
         |airportsFile: path to the CSV file containing airports' geolocalisation
         |usersFile: path to the CSV file containing users' IP
         |redisHost: URL of the REDIS instance we want to use
         |redisPort: port to connect to REDIS
      """.stripMargin)
  }

  /**
    * Checks that the number or arguments is correct and that the REDIS port is a number
    */
  def parseArgs(args: Array[String]): Option[(String, String, String, String, Int)] = {
    if (args.length != 5)
      None
    else
      Try(args.last.toInt) match {
        case Success(p) => Some(args.head, args(1), args(2), args(3), p)
        case Failure(e) =>
          System.err.println(s"Error: ${args.last} is not a number")
          None
      }
  }

  /**
    * Loads all the data from the csv file with IP information into REDIS
    */
  def loadIpsIntoRedis(sc: SparkContext, ipsFile: String, redisHost: String, redisPort: Int): Unit = {
    val ipLines = IpsLoader.parseIpsFile(sc.textFile(ipsFile))
    ipLines.cache()

    ipLines
      .filter(_._2.isFailure)
      .foreach(tuple2 => System.err.println(s"${tuple2._1} couldn't be parsed"))

    ipLines
      .filter(_._2.isSuccess)
      .map(_._2.get)
      .foreachPartition(iter => {
        val rc = new RedisClient(redisHost, redisPort)
        while (iter.hasNext) {
          val ipLine = iter.next()
          rc.geoadd(ipIndex, Seq((ipLine.coord.longitude.toString, ipLine.coord.latitude.toString, ipLine.binaryMask)))
        }
      })
  }

  /**
    * Loads all the data from the csv file with airports information into REDIS
    */
  def loadAirportsIntoRedis(sc: SparkContext, airportsFile: String, redisHost: String, redisPort: Int): Unit = {
    val airportsLines = AirportsLoader.parseAirportsFile(sc.textFile(airportsFile))
    airportsLines.cache()

    airportsLines
      .filter(_._2.isFailure)
      .foreach(tuple2 => System.err.println(s"${tuple2._1} couldn't be parsed"))

    airportsLines
      .filter(_._2.isSuccess)
      .map(_._2.get)
      .foreachPartition(iter => {
        val rc = new RedisClient(redisHost, redisPort)
        while (iter.hasNext) {
          val airportLine = iter.next()
          rc.geoadd(airportIndex,
            Seq((airportLine.coord.longitude.toString, airportLine.coord.latitude.toString, airportLine.iata)))
        }
      })
  }

  def main(args: Array[String]): Unit = {
    val parsedArgs = parseArgs(args)

    if (parsedArgs.isEmpty) {
      System.err.println("Invalid arguments.")
      usage()
      System.exit(1)
    }

    val (ipsFile, airportsFile, usersFile, redisHost, redisPort) = parsedArgs.get
    val airportRangeValue = 5000
    val airportRangeUnit = "km"
    val outputDir = "/tmp/closestAirport_results_" + System.currentTimeMillis().toString

    new File(outputDir).delete()

    val conf = new SparkConf().setAppName("Closest airport")
    val sc = new SparkContext(conf)

    // 1) load IP information into REDIS
    loadIpsIntoRedis(sc, ipsFile, redisHost, redisPort)

    // 2) load airports information into REDIS
    loadAirportsIntoRedis(sc, airportsFile, redisHost, redisPort)

    // 3) find closest airport for each user
    val uuidCoordinates = sc.textFile(usersFile)
      // remove lines that don't have 2 elements
      .map(_.split(","))
      .filter(_.length == 2)
      // get all the possible masks for each IP
      .map(arr => (arr.head, IpUtils.getPossibleBinaryMasks(arr(1))))
      // remove invalid IPs
      .filter(_._2.isSuccess)
      // REDIS request + processing to get the latitude and longitude of the longest matching mask
      .mapPartitions(iter => {
        val rc = new RedisClient(redisHost, redisPort)
        iter.map { elem =>
          // REDIS request with all the netmasks
          rc.geopos(ipIndex, elem._2.get) match {
            case Some(l) => l.filter(_.isDefined) match {
              case Nil => (elem._1, Failure(new NoSuchElementException("Couldn't find any matching mask in REDIS")))
              // keep the first result, corresponding to the longest mask
              case results =>
                (elem._1, Try(new Coordinates(results.head.get(1).get.toDouble, results.head.get.head.get.toDouble)))
            }
            case None => (elem._1, Failure(new NoSuchElementException("Problem while executing request to REDIS")))
          }
        }
      })
    // at this point, couples with the UUID and the coordinates of the IP address if it was found

    uuidCoordinates.cache()

    // print info the UUIDs whose mask can't be found
    uuidCoordinates
      .filter(_._2.isFailure)
      .foreach(tuple2 => println(s"Couldn't find a matching mask for UUID ${tuple2._1}"))

    // go on with the UUID that coordinates
    uuidCoordinates
      .filter(_._2.isSuccess)
      .mapPartitions(iter => {
        val rc = new RedisClient(redisHost, redisPort)
        iter.map { elem =>
          // request to get all the airports within a range of 5000 km, sorted by ascending distance
          rc.georadius(airportIndex, elem._2.get.longitude.toString, elem._2.get.latitude.toString,
            airportRangeValue, airportRangeUnit, false, false, false, None, Some("ASC"), None, None) match {
            case Some(l) => l.filter(_.isDefined) match {
              case Nil =>
                (elem._1, Failure(
                  new NoSuchElementException(s"Couldn't find any airport closer to $airportRangeValue $airportRangeUnit")))
              // get the IATA code of the first result, corresponding to the closest airport
              case results =>
                (elem._1, Success(results.head.get.member.get))
            }
            case None => (elem._1, Failure(new NoSuchElementException("Problem while executing request to REDIS")))
          }
         }
       })
      .map(tuple2 => tuple2._2 match {
        case Success(iata) => tuple2._1 + "," + iata
        case Failure(e) => tuple2._1 + ","
      })
      .saveAsTextFile(outputDir)

    sc.stop()
  }
}
