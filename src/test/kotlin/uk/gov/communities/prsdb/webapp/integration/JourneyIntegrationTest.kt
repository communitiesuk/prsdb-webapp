package uk.gov.communities.prsdb.webapp.integration

import org.flywaydb.core.Flyway
import org.junit.jupiter.api.AfterEach
import org.springframework.beans.factory.annotation.Autowired
import uk.gov.communities.prsdb.webapp.testHelpers.IntegrationTestHelper

class JourneyIntegrationTest : IntegrationTest() {
    @AfterEach
    fun resetDatabase(
        @Autowired flyway: Flyway,
    ) = IntegrationTestHelper.resetDatabase(flyway)
}
