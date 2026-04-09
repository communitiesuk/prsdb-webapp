package uk.gov.communities.prsdb.webapp.application

import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.boot.SpringApplication
import org.springframework.context.ApplicationContext
import org.springframework.context.annotation.Profile
import uk.gov.communities.prsdb.webapp.annotations.taskAnnotations.PrsdbTask
import uk.gov.communities.prsdb.webapp.services.NftDataSeeder
import kotlin.system.exitProcess

@PrsdbTask("nft-data-seeder")
@Profile("local", "nft")
class NftDataSeedingTaskApplicationRunner(
    private val context: ApplicationContext,
    private val nftDataSeeder: NftDataSeeder,
) : ApplicationRunner {
    override fun run(args: ApplicationArguments?) {
        println("Executing NFT data seeding task")
        nftDataSeeder.seedDatabase()
        println("NFT data seeding task executed successfully. Application will exit now.")
        exitProcess(SpringApplication.exit(context, { 0 }))
    }
}
