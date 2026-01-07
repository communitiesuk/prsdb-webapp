package uk.gov.communities.prsdb.webapp.application

import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.boot.SpringApplication
import org.springframework.context.ApplicationContext
import uk.gov.communities.prsdb.webapp.annotations.taskAnnotations.PrsdbScheduledTask
import uk.gov.communities.prsdb.webapp.services.NgdAddressLoader
import kotlin.system.exitProcess

@PrsdbScheduledTask("ngd-address-update-scheduled-task")
class NgdAddressUpdateTaskApplicationRunner(
    private val context: ApplicationContext,
    private val ngdAddressLoader: NgdAddressLoader,
) : ApplicationRunner {
    override fun run(args: ApplicationArguments?) {
        println("Executing NGD address update scheduled task")

        ngdAddressLoader.loadNewDataPackageVersions()

        val code =
            SpringApplication.exit(context, { 0 }).also {
                println("Scheduled task executed. Application will exit now.")
            }
        exitProcess(code)
    }
}
