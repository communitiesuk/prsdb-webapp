package uk.gov.communities.prsd.webapp

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class PrsdWebappApplication

fun main(args: Array<String>) {
    runApplication<PrsdWebappApplication>(*args)
}
