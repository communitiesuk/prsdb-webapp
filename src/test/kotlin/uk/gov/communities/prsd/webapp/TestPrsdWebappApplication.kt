package uk.gov.communities.prsd.webapp

import org.springframework.boot.fromApplication
import org.springframework.boot.with


fun main(args: Array<String>) {
    fromApplication<PrsdWebappApplication>().with(TestcontainersConfiguration::class).run(*args)
}
