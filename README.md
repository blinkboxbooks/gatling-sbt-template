gatling-sbt-template
=============

Gatling load test template your http web service

Tested with Gatling 2.1.3

To run: ```$ sbt test```

Reports are generated into the ```/target/gatling``` folder

Tweak the test parameters directly in [ServiceSimulation.scala](/src/test/scala/ServiceSimulation.scala)

Gatling configuration can be modified in [gatling.conf](/src/test/resources/gatling.conf)

## Contributors (blinkbox books 2015):
* [Alex Bagehot](https://github.com/ceeaspb)
* Jay McCure
* David Owen
* [Greg Beech](https://github.com/gregbeech)
