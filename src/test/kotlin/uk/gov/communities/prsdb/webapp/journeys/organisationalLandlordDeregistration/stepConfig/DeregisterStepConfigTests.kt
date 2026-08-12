package uk.gov.communities.prsdb.webapp.journeys.organisationalLandlordDeregistration.stepConfig

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.eq
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import uk.gov.communities.prsdb.webapp.journeys.organisationalLandlordDeregistration.OrganisationalLandlordDeregistrationJourneyState
import uk.gov.communities.prsdb.webapp.models.viewModels.emailModels.OrganisationalLandlordDeregistrationConfirmationEmail
import uk.gov.communities.prsdb.webapp.services.EmailNotificationService
import uk.gov.communities.prsdb.webapp.services.UserToLandlordService
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.MockLandlordData
import kotlin.test.assertEquals

@ExtendWith(MockitoExtension::class)
class DeregisterStepConfigTests {
    @Mock
    private lateinit var mockUserToLandlordService: UserToLandlordService

    @Mock
    private lateinit var mockConfirmationEmailSender: EmailNotificationService<OrganisationalLandlordDeregistrationConfirmationEmail>

    @Mock
    private lateinit var mockState: OrganisationalLandlordDeregistrationJourneyState

    private lateinit var stepConfig: DeregisterStepConfig

    @BeforeEach
    fun setUp() {
        stepConfig = DeregisterStepConfig(mockUserToLandlordService, mockConfirmationEmailSender)
    }

    @Test
    fun `afterStepIsReached sends the confirmation email to the organisation email`() {
        val landlord =
            MockLandlordData.createOrgLandlord(
                name = "Example Housing Association",
                email = "organisation@example.com",
                registrantName = "Sam Smith",
                registrantEmail = "sam.smith@example.com",
            )
        whenever(mockUserToLandlordService.getCurrentOrganisationLandlordForUser()).thenReturn(landlord)

        stepConfig.afterStepIsReached(mockState)

        val emailCaptor = argumentCaptor<OrganisationalLandlordDeregistrationConfirmationEmail>()
        verify(mockConfirmationEmailSender).sendEmail(
            eq("sam.smith@example.com"),
            emailCaptor.capture(),
        )

        assertEquals(
            OrganisationalLandlordDeregistrationConfirmationEmail(
                registrantName = "Sam Smith",
                organisationName = "Example Housing Association",
            ),
            emailCaptor.firstValue,
        )
    }
}
