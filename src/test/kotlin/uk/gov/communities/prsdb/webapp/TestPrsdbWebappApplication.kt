package uk.gov.communities.prsdb.webapp

import org.springframework.boot.fromApplication
import org.springframework.boot.with

fun main(args: Array<String>) {
    fromApplication<PrsdbWebappApplication>().with(TestcontainersConfiguration::class).run(*args)
}
