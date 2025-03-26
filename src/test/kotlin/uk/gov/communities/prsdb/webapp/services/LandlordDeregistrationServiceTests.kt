package uk.gov.communities.prsdb.webapp.services

import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import uk.gov.communities.prsdb.webapp.database.repository.LandlordWithListedPropertyCountRepository

@ExtendWith(MockitoExtension::class)
class LandlordDeregistrationServiceTests {
    @Mock
    private lateinit var mockLandlordWithListedPropertyCountRepository: LandlordWithListedPropertyCountRepository

    @InjectMocks
    private lateinit var landlordDeregistrationService: LandlordDeregistrationService
}
