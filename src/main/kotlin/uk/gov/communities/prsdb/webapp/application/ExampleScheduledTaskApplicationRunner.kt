package uk.gov.communities.prsdb.webapp.application

import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.boot.SpringApplication
import org.springframework.context.ApplicationContext
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component
import kotlin.system.exitProcess

@Component
@Profile("web-server-deactivated & scheduled-task & example-scheduled-task")
class ExampleScheduledTaskApplicationRunner(
    private val context: ApplicationContext,
) : ApplicationRunner {
    override fun run(args: ApplicationArguments?) {
        println("Executing example scheduled task")

        val code =
            SpringApplication.exit(context, { 0 }).also {
                println("Scheduled task executed. Application will exit now.")
            }
        exitProcess(code)
    }
}
