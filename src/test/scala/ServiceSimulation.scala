import io.gatling.core.Predef._
import io.gatling.http.Predef._
import scala.concurrent.duration._
import scala.language.postfixOps

// http://gatling.io/docs/

class ServiceSimulation extends Simulation {

  val host = ""
  val port = ""
  val scheme = "http"
  val path = "/path/to/endpoint"
  val serviceURL = scheme + "://" + host + ":" + port + path

  // Various HTTP config
  // http://gatling.io/docs/2.1.1/http/http_protocol.html

  val httpConf = http
    .acceptHeader("text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8")
    .doNotTrackHeader("1")
    .connection("close")
    .acceptLanguageHeader("en-US,en;q=0.5")
    .acceptEncodingHeader("gzip, deflate")
    .userAgentHeader("Mozilla/5.0 (Macintosh; Intel Mac OS X 10.8; rv:16.0) Gecko/20100101 Firefox/16.0")
    .disableCaching
    .shareConnections
    .baseURL(catalogueURL)

  // -------------------
  // Data files
  // http://gatling.io/docs/2.1.1/session/feeder.html

  val book = csv("data.csv").circular


  // ---------------------
  // Requests :
  // http://gatling.io/docs/2.1.1/http/http_request.html
  // these can be split out into separate files/ objects
  // val req_<service>_<http method>_<uri path> = http("name: <service>_<http method>_<uri path>")
  //                                                .<get/post/etc>(scheme+"://"+host+":"+port+"").check(status.is(200))

  val reqYourHTTPRequest = feed(data).exec(http("requestDescription").get("foo?bar=${data}"))


  // ----------------------------
  // scenarios: this defines what requests a user will make
  // http://gatling.io/docs/2.1.1/general/scenario.html
  //
  // here it's a simplification where each user only makes 1 request
  //
  // the workload can either be "closed" or "open". most API testing will be "open" and this will provide a "prudent" load test where it should fail hard like most web facing app do...
  //
  // .exec() uses the builder pattern so requests can be composed and chained... very flexible
  //val scn_<scenario name> = scenario("<name>") .exec( <chanined requests> )

  val scnYourScenarioHere = scenario("scenarioDescription").exec(reqYourHTTPRequest)


  // In a "closed" model scenarios can "loop" or re-execute the scenario forever...
  // ONLY use a closed model for "terminal" type workloads like a call center staff, where the user literally does repeat the same work over and over...
  // there are other loops like "during(20 seconds){exec()}" etc
  // closed workloads will also likely have some kind of pause() or pace() to avoid huge request rates if the response time is small.
  // http://gatling.io/docs/2.1.1/general/scenario.html#pause
  // http://gatling.io/docs/2.1.1/general/scenario.html#pace

  //  val scnYourScenarioHere = scenario("scenarioDescription").forever(exec(reqYourHTTPRequest))

  // ----------------------------
  // The load injection : where the test "executes"
  // http://gatling.io/docs/2.1.1/general/simulation_setup.html
  //
  // setUp( ... )
  //
  // EG.
  //
  // In the below there are 2 scenarios, one open and one closed workload.
  // They also have different httpConf's - there may be different source ip addresses etc
  //
  // 1) "scn.inject(...)" will apply some load from scenario "scn".
  //
  // 2) "open" workload: users arrive at a rate: eg. "constantUsersPerSec(20) during(15 seconds)"
  //
  // 3) "closed" workload: users loop around so we only inject a fixed number: eg. "atOnceUsers(10)"
  //
  //  setUp(
  //    scn.inject(rampUsersPerSec(initUPS) to(50) during(600 seconds) ).protocols(httpConf1)
  //     ,
  //    scnYourScenarioHere.inject(atOnceUsers(10) ).protocols(httpConf2)
  //
  //  )

  // the arrival rate at the start of the test
  val initUPS = 0.1
  // the final arrival rate
  val upsRate = 0.5
  // the time it takes to gradually increase from start to max load
  val rampDuration = 60
  // the duration of the "steady state"
  val testDuration = 600

  setUp(

    //  <scn>.inject(rampUsersPerSec(initUPS) to(upsRate) during(rampDuration seconds),  // the ramp up
    //               constantUsersPerSec(upsRate) during(testDuration minutes)           // the steady state
    //              ) .protocols(httpConf)

    scnYourScenarioHere.inject(rampUsersPerSec(initUPS) to (upsRate) during (rampDuration seconds), constantUsersPerSec(upsRate) during (testDuration minutes)).protocols(httpConf)

    // Or if you are using forever()
    //scnYourScenarioHere.inject(atOnceUsers(1))
  )
}
