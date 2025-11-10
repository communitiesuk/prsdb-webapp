package uk.gov.communities.prsdb.webapp

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class PrsdbWebappApplication

fun main(args: Array<String>) {
    runApplication<PrsdbWebappApplication>(*args)
}
