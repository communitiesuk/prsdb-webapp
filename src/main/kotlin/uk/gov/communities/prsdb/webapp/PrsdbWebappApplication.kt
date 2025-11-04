package uk.gov.communities.prsdb.webapp

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.EnableAspectJAutoProxy

@SpringBootApplication(scanBasePackages = ["uk.gov.communities.prsdb.webapp", "org.ff4j.aop"])
@EnableAspectJAutoProxy(proxyTargetClass = true)
class PrsdbWebappApplication

fun main(args: Array<String>) {
    runApplication<PrsdbWebappApplication>(*args)
}
