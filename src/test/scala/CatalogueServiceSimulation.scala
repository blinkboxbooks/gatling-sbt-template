import io.gatling.core.Predef._
import io.gatling.http.Predef._
import scala.concurrent.duration._
import scala.language.postfixOps

// http://gatling.io/docs/

class CatalogueServiceSimulation extends Simulation {

// Various HTTP config
// http://gatling.io/docs/2.1.1/http/http_protocol.html

  val httpConf = http
    .acceptHeader("text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8")
    .doNotTrackHeader("1")
    //.connection("keep-alive")
    .connection("close")
    .acceptLanguageHeader("en-US,en;q=0.5")
    .acceptEncodingHeader("gzip, deflate")
    .userAgentHeader("Mozilla/5.0 (Macintosh; Intel Mac OS X 10.8; rv:16.0) Gecko/20100101 Firefox/16.0")
    .disableCaching
    .shareConnections

  val headers_10 = Map("Content-Type" -> """application/x-www-form-urlencoded""") // Note the headers specific to a given request

  val headers_5 = Map("""Accept""" -> """text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8""",
                      """Content-Type""" -> """application/x-www-form-urlencoded""")

// Example of multiple source addresses... or setting the source ip of the test requests
// when the load injector has more than one interface
//  val httpConf1 = httpConf.localAddress(java.net.InetAddress.getByName("192.168.1.100"))
//  val httpConf2 = httpConf.localAddress(java.net.InetAddress.getByName("192.168.1.101"))

// -----------------------
// http://jira.blinkbox.local/confluence/display/BBBOPS/Environment+Overview

// -- Local dev :
//val host = "172.17.190.27"
//val port = "7001"
//val scheme = "http"

val host = "harris.blinkbox.local"
val port = "7001"
val scheme = "http"

//val host = "api.dev.bbbtest2.com"
//val port = "443"
//val scheme = "https"

//val host =   "api.qa.bbbtest2.com"
//val port =   "443"
//val scheme = "https"

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

//val req_<service>_<http method>_<uri path> = http("name: <service>_<http method>_<uri path>") .<get/post/etc>(scheme+"://"+host+":"+port+"").check(status.is(200))

val req_catalogue_get_book_prices = feed(book).feed(book2).exec(http("catalogue_get_book_prices") .get(scheme+"://"+host+":"+port+"/service/catalogue/prices?book=${book}&book=${book2}").check(status.is(200)))
val req_catalogue_get_books_for_category = feed(category).exec(http("catalogue_get_books_for_category") .get(scheme+"://"+host+":"+port+"/service/catalogue/books?category=${category}").check(status.is(200)))
val req_catalogue_get_books_for_contributor = feed(contributor).exec(http("catalogue_get_books_contributor") .get(scheme+"://"+host+":"+port+"/service/catalogue/books?contributor=${contributor}") )
//.check(status.is(200)))
val req_catalogue_get_books_for_promotion = feed(promotion).exec(http("catalogue_get_books_for_promotion") .get(scheme+"://"+host+":"+port+"/service/catalogue/books?promotion=${promotion}&order=SEQUENTIAL").check(status.is(200)))
val req_catalogue_get_books_for_publisher = feed(publisher).exec(http("catalogue_get_books_for_publisher") .get(scheme+"://"+host+":"+port+"/service/catalogue/books?publisher=${publisher}").check(status.is(200)))
val req_catalogue_get_bulk_books = feed(book).feed(book2).exec(http("catalogue_get_bulk_books") .get(scheme+"://"+host+":"+port+"/service/catalogue/books?id=${book}&id=${book2}").check(status.is(200)))
val req_catalogue_get_categories = http("catalogue_get_categories") .get(scheme+"://"+host+":"+port+"/service/catalogue/categories").check(status.is(200))
val req_catalogue_get_category_by_slug = feed(slug).exec(http("catalogue_get_category_by_slug") .get(scheme+"://"+host+":"+port+"/service/catalogue/categories?slug=${slug}").check(status.is(200)))
val req_catalogue_get_individual_book = feed(book).exec(http("catalogue_get_individual_book") .get(scheme+"://"+host+":"+port+"/service/catalogue/books/${book}").check(status.is(200)))
val req_catalogue_get_individual_category = feed(category).exec(http("catalogue_get_individual_category") .get(scheme+"://"+host+":"+port+"/service/catalogue/categories/${category}").check(status.is(200)))
val req_catalogue_get_individual_contributor = feed(contributor).exec(http("catalogue_get_individual_contributor") .get(scheme+"://"+host+":"+port+"/service/catalogue/contributors/${contributor}").check(status.is(200)))
val req_catalogue_get_individual_publisher = feed(publisher).exec(http("catalogue_get_individual_publisher") .get(scheme+"://"+host+":"+port+"/service/catalogue/publishers/${publisher}").check(status.is(200)))
val req_catalogue_get_non_recommended_categories = http("catalogue_get_non_recommended_categories") .get(scheme+"://"+host+":"+port+"/service/catalogue/categories?recommended=false").check(status.is(200))
val req_catalogue_get_publishers = http("catalogue_get_publishers") .get(scheme+"://"+host+":"+port+"/service/catalogue/publishers").check(status.is(200))
val req_catalogue_get_recommended_categories = http("catalogue_get_recommended_categories") .get(scheme+"://"+host+":"+port+"/service/catalogue/categories?recommended=true").check(status.is(200))
val req_catalogue_get_related_book_for_isbn = feed(book).exec(http("catalogue_get_related_book_for_isbn") .get(scheme+"://"+host+":"+port+"/service/catalogue/books/${book}/related").check(status.is(200)))
val req_catalogue_get_synopsis = feed(book).exec(http("catalogue_get_synopsis") .get(scheme+"://"+host+":"+port+"/service/catalogue/books/${book}/synopsis").check(status.is(200)))

// could be useful to include healthcheck requests for various reasons - this will only be available on Dev and DevInt.......
val req_catalogue_pingpong = http("catalogue_pingpong") .get(scheme+"://"+host+":"+port+"/health/ping").headers(headers_5).check(status.is(200))

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

  val scn_req_get_book_prices = scenario("req_get_book_prices") .exec( req_catalogue_get_book_prices)
  val scn_req_catalogue_get_books_for_category = scenario("req_catalogue_get_books_for_category") .exec( req_catalogue_get_books_for_category)
  val scn_req_catalogue_get_books_for_contributor = scenario("req_catalogue_get_books_for_contributor") .exec( req_catalogue_get_books_for_contributor)
  val scn_req_catalogue_get_books_for_promotion = scenario("req_catalogue_get_books_for_promotion") .exec( req_catalogue_get_books_for_promotion)
  val scn_req_catalogue_get_books_for_publisher = scenario("req_catalogue_get_books_for_publisher") .exec( req_catalogue_get_books_for_publisher)
  val scn_req_catalogue_get_bulk_books = scenario("req_catalogue_get_bulk_books") .exec( req_catalogue_get_bulk_books)
  val scn_req_catalogue_get_categories = scenario("req_catalogue_get_categories") .exec( req_catalogue_get_categories)
  val scn_req_catalogue_get_category_by_slug = scenario("req_catalogue_get_category_by_slug") .exec( req_catalogue_get_category_by_slug)
  val scn_req_catalogue_get_individual_book = scenario("req_catalogue_get_individual_book") .exec( req_catalogue_get_individual_book)
  val scn_req_catalogue_get_individual_category = scenario("req_catalogue_get_individual_category") .exec( req_catalogue_get_individual_category)
  val scn_req_catalogue_get_individual_contributor = scenario("req_catalogue_get_individual_contributor") .exec( req_catalogue_get_individual_contributor)
  val scn_req_catalogue_get_individual_publisher = scenario("req_catalogue_get_individual_publisher") .exec( req_catalogue_get_individual_publisher)
  val scn_req_catalogue_get_non_recommended_categories = scenario("req_catalogue_get_non_recommended_categories") .exec( req_catalogue_get_non_recommended_categories)
  val scn_req_catalogue_get_publishers = scenario("req_catalogue_get_publishers") .exec( req_catalogue_get_publishers)
  val scn_req_catalogue_get_recommended_categories = scenario("req_catalogue_get_recommended_categories") .exec( req_catalogue_get_recommended_categories)
  val scn_req_catalogue_get_related_book_for_isbn = scenario("req_catalogue_get_related_book_for_isbn") .exec( req_catalogue_get_related_book_for_isbn)
  val scn_req_catalogue_get_synopsis = scenario("req_catalogue_get_synopsis") .exec( req_catalogue_get_synopsis)

// In a "closed" model scenarios can "loop" or re-execute the scenario forever...
// ONLY use a closed model for "terminal" type workloads like a call center staff, where the user litterally does repeat the same work over and over...
// there are other loops like "during(20 seconds){exec()}" etc
// closed workloads will also likely have some kind of pause() or pace() to avoid huge request rates if the response time is small.
// http://gatling.io/docs/2.1.1/general/scenario.html#pause
// http://gatling.io/docs/2.1.1/general/scenario.html#pace
  val scn_req_pingpong = scenario("ping_pong") .forever(exec( req_catalogue_pingpong).exec(pause(10 seconds)))
  

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
val rampDuration =60 
// the duration of the "steady state"
val testDuration =600 

 setUp(

//  <scn>.inject(rampUsersPerSec(initUPS) to(upsRate) during(rampDuration seconds),  // the ramp up
//               constantUsersPerSec(upsRate) during(testDuration minutes)           // the steady state 
//              ) .protocols(httpConf),

scn_req_get_book_prices.inject(rampUsersPerSec(initUPS) to(upsRate) during(rampDuration seconds),constantUsersPerSec(upsRate) during(testDuration minutes) ) .protocols(httpConf),
scn_req_catalogue_get_books_for_category.inject(rampUsersPerSec(initUPS) to(upsRate) during(rampDuration seconds),constantUsersPerSec(upsRate) during(testDuration minutes) ) .protocols(httpConf),
scn_req_catalogue_get_books_for_contributor.inject(rampUsersPerSec(initUPS) to(upsRate) during(rampDuration seconds),constantUsersPerSec(upsRate) during(testDuration minutes) ) .protocols(httpConf),
scn_req_catalogue_get_books_for_promotion.inject(rampUsersPerSec(initUPS) to(upsRate) during(rampDuration seconds),constantUsersPerSec(upsRate) during(testDuration minutes) ) .protocols(httpConf),
scn_req_catalogue_get_books_for_publisher.inject(rampUsersPerSec(initUPS) to(upsRate) during(rampDuration seconds),constantUsersPerSec(upsRate) during(testDuration minutes) ) .protocols(httpConf),
scn_req_catalogue_get_bulk_books.inject(rampUsersPerSec(initUPS) to(upsRate) during(rampDuration seconds),constantUsersPerSec(upsRate) during(testDuration minutes) ) .protocols(httpConf),
scn_req_catalogue_get_categories.inject(rampUsersPerSec(initUPS) to(upsRate) during(rampDuration seconds),constantUsersPerSec(upsRate) during(testDuration minutes) ) .protocols(httpConf),
scn_req_catalogue_get_category_by_slug.inject(rampUsersPerSec(initUPS) to(upsRate) during(rampDuration seconds),constantUsersPerSec(upsRate) during(testDuration minutes) ) .protocols(httpConf),
scn_req_catalogue_get_individual_book.inject(rampUsersPerSec(initUPS) to(upsRate) during(rampDuration seconds),constantUsersPerSec(upsRate) during(testDuration minutes) ) .protocols(httpConf),
scn_req_catalogue_get_individual_category.inject(rampUsersPerSec(initUPS) to(upsRate) during(rampDuration seconds),constantUsersPerSec(upsRate) during(testDuration minutes) ) .protocols(httpConf),
scn_req_catalogue_get_individual_contributor.inject(rampUsersPerSec(initUPS) to(upsRate) during(rampDuration seconds),constantUsersPerSec(upsRate) during(testDuration minutes) ) .protocols(httpConf),
scn_req_catalogue_get_individual_publisher.inject(rampUsersPerSec(initUPS) to(upsRate) during(rampDuration seconds),constantUsersPerSec(upsRate) during(testDuration minutes) ) .protocols(httpConf),
scn_req_catalogue_get_non_recommended_categories.inject(rampUsersPerSec(initUPS) to(upsRate) during(rampDuration seconds),constantUsersPerSec(upsRate) during(testDuration minutes) ) .protocols(httpConf),
scn_req_catalogue_get_publishers.inject(rampUsersPerSec(initUPS) to(upsRate) during(rampDuration seconds),constantUsersPerSec(upsRate) during(testDuration minutes) ) .protocols(httpConf),
scn_req_catalogue_get_recommended_categories.inject(rampUsersPerSec(initUPS) to(upsRate) during(rampDuration seconds),constantUsersPerSec(upsRate) during(testDuration minutes) ) .protocols(httpConf),
scn_req_catalogue_get_related_book_for_isbn.inject(rampUsersPerSec(initUPS) to(upsRate) during(rampDuration seconds),constantUsersPerSec(upsRate) during(testDuration minutes) ) .protocols(httpConf),
scn_req_catalogue_get_synopsis.inject(rampUsersPerSec(initUPS) to(upsRate) during(rampDuration seconds),constantUsersPerSec(upsRate) during(testDuration minutes) ) .protocols(httpConf),
//.inject(rampUsersPerSec(initUPS) to(upsRate) during(rampDuration seconds),constantUsersPerSec(upsRate) during(testDuration minutes) ) .protocols(httpConf),

scn_req_pingpong.inject(atOnceUsers(1))
)
}
