package uk.gov.communities.prsdb.webapp.application

import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.boot.SpringApplication
import org.springframework.context.ApplicationContext
import org.springframework.context.annotation.Profile
import org.springframework.core.Ordered
import org.springframework.core.annotation.Order
import org.springframework.stereotype.Component
import kotlin.system.exitProcess

@Component
@Profile("web-server-deactivated")
@Order(Ordered.LOWEST_PRECEDENCE)
class DefaultScheduledTaskApplicationRunner(
    private val context: ApplicationContext,
) : ApplicationRunner {
    override fun run(args: ApplicationArguments?) {
        println("The application was not configured for this scheduled task. Application will exit now.")

        val code =
            SpringApplication.exit(context, { 0 })
        exitProcess(code)
    }
}
