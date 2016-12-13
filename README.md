# Closest airport challenge

## build

```
sbt assembly
```

## run

**Requirements:** having a running REDIS server (one can be found at the root of the project)

```
spark-submit --master <master> target/scala-2.10/closestAirport-assembly-1.0.0.jar <ipsFile> <airportsFile> <usersFile> <redisHost> <redisPort>

ipsFile: path to the CSV file containing IP ranges' geolocalisation
airportsFile: path to the CSV file containing airports' geolocalisation
usersFile: path to the CSV file containing users' IP
redisHost: URL of the REDIS instance we want to use
redisPort: port to connect to REDIS
```
### Example

```

```

The program was sucessfully executed on a laptop with 4 cores and 8 GB.

## Output

The resulting files will be written in `/tmp/closestAirport_results_timestamp/`.

## Improvements

It should be noticed that I didn't have much time to do the assignement and that I had to finish it in a hurry. Therefore a lot of things can be improved. Below is a non exhaustive list of improvements that should be done.  

- Add unit tests for the functions that transform RDD (`parseAirportsFile` and `parseIpsFile`).
- `main` function is huge and hardly testable. It should be splited into functions that can be unit tested.
- Although only one connection to REDIS is created by RDD partition, right now one request is executed for each line of input csv. In order to optimize bandwidth usage it would be wise to perform bulk requests.
- Add error handling to REDIS requests/responses.
- Right now, a single request is performed to get all the airports within a range of distance (5000 km). Further studying of data should be performed in order to adapt the requesting process at best. For instance, if the number of airports is really huge, it could be wise to perform successive request with increasing range until at least one airport is found.
- ...
