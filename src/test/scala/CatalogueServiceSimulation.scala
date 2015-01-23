import io.gatling.core.Predef._
import io.gatling.http.Predef._
import scala.concurrent.duration._
import scala.language.postfixOps

// http://gatling.io/docs/

class CatalogueServiceSimulation extends Simulation {

  // -----------------------
  // http://jira.blinkbox.local/confluence/display/BBBOPS/Environment+Overview

  val host = "harris.blinkbox.local"
  val port = "7001"
  val scheme = "http"
  val path = "/service/catalogue/"
  val catalogueURL = scheme + "://" + host + ":" + port + path

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

  val book = csv("books.csv").circular
  val book2 = csv("books2.csv").circular
  val category = csv("categories.csv").circular
  val contributor = csv("contributors.csv").circular
  val promotion = csv("promotions.csv").circular
  val publisher = csv("publishers.csv").circular
  val slug = csv("slugs.csv").circular


  // ---------------------
  // Requests :
  // http://gatling.io/docs/2.1.1/http/http_request.html
  // these can be split out into separate files/ objects
  // val req_<service>_<http method>_<uri path> = http("name: <service>_<http method>_<uri path>")
  //                                                .<get/post/etc>(scheme+"://"+host+":"+port+"").check(status.is(200))

  val reqGetBookPrices = feed(book).feed(book2).exec(http("getBookPrices").get("prices?book=${book}&book=${book2}"))
  val reqGetBooksForCategory = feed(category).exec(http("getBooksForCategory").get("books?category=${category}"))
  val reqGetBooksForContributor = feed(contributor).exec(http("getBooksContributor").get("books?contributor=${contributor}"))
  val reqGetBooksForPromotion = feed(promotion).exec(http("getBooksForPromotion").get("books?promotion=${promotion}&order=SEQUENTIAL"))
  val reqGetBooksForPublisher = feed(publisher).exec(http("getBooksForPublisher").get("books?publisher=${publisher}"))
  val reqGetBulkBooks = feed(book).feed(book2).exec(http("getBulkBooks").get("books?id=${book}&id=${book2}"))
  val reqGetCategories = http("getCategories").get("categories")
  val reqGetCategoryBySlug = feed(slug).exec(http("getCategoryBySlug").get("categories?slug=${slug}"))
  val reqGetIndividualBook = feed(book).exec(http("getIndividualBook").get("books/${book}"))
  val reqGetIndividualCategory = feed(category).exec(http("getIndividualCategory").get("categories/${category}"))
  val reqGetIndividualContributor = feed(contributor).exec(http("getIndividualContributor").get("contributors/${contributor}"))
  val reqGetIndividualPublisher = feed(publisher).exec(http("getIndividualPublisher").get("publishers/${publisher}"))
  val reqGetNonRecommendedCategories = http("getNonRecommendedCategories").get("categories?recommended=false")
  val reqGetPublishers = http("getPublishers").get("publishers")
  val reqGetRecommendedCategories = http("getRecommendedCategories").get("categories?recommended=true")
  val reqGetRelatedBookForIsbn = feed(book).exec(http("getRelatedBookForIsbn").get("books/${book}/related"))
  val reqGetSynopsis = feed(book).exec(http("getSynopsis").get("books/${book}/synopsis"))

  // could be useful to include healthcheck requests for various reasons - this will only be available on Dev and DevInt.......
  val reqPingpong = http("pingpong").get(scheme + "://" + host + ":" + port + "/health/ping")

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

  val scnReqGetBookPrices = scenario("reqGetBookPrices").exec(reqGetBookPrices)
  val scnReqGetBooksForCategory = scenario("reqGetBooksForCategory").exec(reqGetBooksForCategory)
  val scnReqGetBooksForContributor = scenario("reqGetBooksForContributor").exec(reqGetBooksForContributor)
  val scnReqGetBooksForPromotion = scenario("reqGetBooksForPromotion").exec(reqGetBooksForPromotion)
  val scnReqGetBooksForPublisher = scenario("reqGetBooksForPublisher").exec(reqGetBooksForPublisher)
  val scnReqGetBulkBooks = scenario("reqGetBulkBooks").exec(reqGetBulkBooks)
  val scnReqGetCategories = scenario("reqGetCategories").exec(reqGetCategories)
  val scnReqGetCategoryBySlug = scenario("reqGetCategoryBySlug").exec(reqGetCategoryBySlug)
  val scnReqGetIndividualBook = scenario("reqGetIndividualBook").exec(reqGetIndividualBook)
  val scnReqGetIndividualCategory = scenario("reqGetIndividualCategory").exec(reqGetIndividualCategory)
  val scnReqGetIndividualContributor = scenario("reqGetIndividualContributor").exec(reqGetIndividualContributor)
  val scnReqGetIndividualPublisher = scenario("reqGetIndividualPublisher").exec(reqGetIndividualPublisher)
  val scnReqGetNonRecommendedCategories = scenario("reqGetNonRecommendedCategories").exec(reqGetNonRecommendedCategories)
  val scnReqGetPublishers = scenario("reqGetPublishers").exec(reqGetPublishers)
  val scnReqGetRecommendedCategories = scenario("reqGetRecommendedCategories").exec(reqGetRecommendedCategories)
  val scnReqGetRelatedBookForIsbn = scenario("reqGetRelatedBookForIsbn").exec(reqGetRelatedBookForIsbn)
  val scnReqGetSynopsis = scenario("reqGetSynopsis").exec(reqGetSynopsis)

  // In a "closed" model scenarios can "loop" or re-execute the scenario forever...
  // ONLY use a closed model for "terminal" type workloads like a call center staff, where the user literally does repeat the same work over and over...
  // there are other loops like "during(20 seconds){exec()}" etc
  // closed workloads will also likely have some kind of pause() or pace() to avoid huge request rates if the response time is small.
  // http://gatling.io/docs/2.1.1/general/scenario.html#pause
  // http://gatling.io/docs/2.1.1/general/scenario.html#pace
  val scnReqPingpong = scenario("pingPong").forever(exec(reqPingpong).exec(pause(10 seconds)))

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
  //    scn_req_pingpong.inject(atOnceUsers(10) ).protocols(httpConf2)
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
    //              ) .protocols(httpConf),

    scnReqGetBookPrices.inject(rampUsersPerSec(initUPS) to (upsRate) during (rampDuration seconds), constantUsersPerSec(upsRate) during (testDuration minutes)).protocols(httpConf),
    scnReqGetBooksForCategory.inject(rampUsersPerSec(initUPS) to (upsRate) during (rampDuration seconds), constantUsersPerSec(upsRate) during (testDuration minutes)).protocols(httpConf),
    scnReqGetBooksForContributor.inject(rampUsersPerSec(initUPS) to (upsRate) during (rampDuration seconds), constantUsersPerSec(upsRate) during (testDuration minutes)).protocols(httpConf),
    scnReqGetBooksForPromotion.inject(rampUsersPerSec(initUPS) to (upsRate) during (rampDuration seconds), constantUsersPerSec(upsRate) during (testDuration minutes)).protocols(httpConf),
    scnReqGetBooksForPublisher.inject(rampUsersPerSec(initUPS) to (upsRate) during (rampDuration seconds), constantUsersPerSec(upsRate) during (testDuration minutes)).protocols(httpConf),
    scnReqGetBulkBooks.inject(rampUsersPerSec(initUPS) to (upsRate) during (rampDuration seconds), constantUsersPerSec(upsRate) during (testDuration minutes)).protocols(httpConf),
    scnReqGetCategories.inject(rampUsersPerSec(initUPS) to (upsRate) during (rampDuration seconds), constantUsersPerSec(upsRate) during (testDuration minutes)).protocols(httpConf),
    scnReqGetCategoryBySlug.inject(rampUsersPerSec(initUPS) to (upsRate) during (rampDuration seconds), constantUsersPerSec(upsRate) during (testDuration minutes)).protocols(httpConf),
    scnReqGetIndividualBook.inject(rampUsersPerSec(initUPS) to (upsRate) during (rampDuration seconds), constantUsersPerSec(upsRate) during (testDuration minutes)).protocols(httpConf),
    scnReqGetIndividualCategory.inject(rampUsersPerSec(initUPS) to (upsRate) during (rampDuration seconds), constantUsersPerSec(upsRate) during (testDuration minutes)).protocols(httpConf),
    scnReqGetIndividualContributor.inject(rampUsersPerSec(initUPS) to (upsRate) during (rampDuration seconds), constantUsersPerSec(upsRate) during (testDuration minutes)).protocols(httpConf),
    scnReqGetIndividualPublisher.inject(rampUsersPerSec(initUPS) to (upsRate) during (rampDuration seconds), constantUsersPerSec(upsRate) during (testDuration minutes)).protocols(httpConf),
    scnReqGetNonRecommendedCategories.inject(rampUsersPerSec(initUPS) to (upsRate) during (rampDuration seconds), constantUsersPerSec(upsRate) during (testDuration minutes)).protocols(httpConf),
    scnReqGetPublishers.inject(rampUsersPerSec(initUPS) to (upsRate) during (rampDuration seconds), constantUsersPerSec(upsRate) during (testDuration minutes)).protocols(httpConf),
    scnReqGetRecommendedCategories.inject(rampUsersPerSec(initUPS) to (upsRate) during (rampDuration seconds), constantUsersPerSec(upsRate) during (testDuration minutes)).protocols(httpConf),
    scnReqGetRelatedBookForIsbn.inject(rampUsersPerSec(initUPS) to (upsRate) during (rampDuration seconds), constantUsersPerSec(upsRate) during (testDuration minutes)).protocols(httpConf),
    scnReqGetSynopsis.inject(rampUsersPerSec(initUPS) to (upsRate) during (rampDuration seconds), constantUsersPerSec(upsRate) during (testDuration minutes)).protocols(httpConf)

    //scnReqPingpong.inject(atOnceUsers(1))
  )
}
