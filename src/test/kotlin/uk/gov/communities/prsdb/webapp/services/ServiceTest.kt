package uk.gov.communities.prsdb.webapp.services

import org.junit.jupiter.api.BeforeEach
import org.mockito.Mockito.mock
import uk.gov.communities.prsdb.webapp.database.repository.LandlordRepository
import uk.gov.communities.prsdb.webapp.database.repository.RegistrationNumberRepository

abstract class ServiceTest {
    lateinit var mockRegNumRepository: RegistrationNumberRepository
    lateinit var mockLandlordRepository: LandlordRepository

    @BeforeEach
    fun setUp() {
        mockRegNumRepository = mock()
        mockLandlordRepository = mock()
    }
}
