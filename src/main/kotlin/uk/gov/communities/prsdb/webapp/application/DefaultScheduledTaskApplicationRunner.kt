package uk.gov.communities.prsdb.webapp.application

import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.boot.SpringApplication
import org.springframework.context.ApplicationContext
import org.springframework.core.Ordered
import uk.gov.communities.prsdb.webapp.annotations.taskAnnotations.PrsdbScheduledTask
import kotlin.system.exitProcess

@PrsdbScheduledTask(precedence = Ordered.LOWEST_PRECEDENCE)
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
