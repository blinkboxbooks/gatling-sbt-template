gatling-tests
=============

Gatling load test scripts and data files for the catalogue-service

Tested with Gatling 2.1.3

To run: ```$ sbt test```

Reports are generated into the ```/target/gatling``` folder

Tweak the test parameters directly in [CatalogueServiceSimulation.scala](/src/test/scala/CatalogueServiceSimulation.scala)

Gatling configuration can be modified in [gatling.conf](/src/test/resources/gatling.conf)