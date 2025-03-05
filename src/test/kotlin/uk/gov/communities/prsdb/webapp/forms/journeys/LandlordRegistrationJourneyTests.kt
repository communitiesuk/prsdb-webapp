package uk.gov.communities.prsdb.webapp.forms.journeys

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.mockito.Mock
import org.mockito.Mockito.mock
import org.mockito.kotlin.any
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.argThat
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.security.core.Authentication
import org.springframework.security.core.context.SecurityContext
import org.springframework.security.core.context.SecurityContextHolder
import uk.gov.communities.prsdb.webapp.database.entity.LocalAuthority
import uk.gov.communities.prsdb.webapp.forms.PageData
import uk.gov.communities.prsdb.webapp.forms.steps.LandlordRegistrationStepId
import uk.gov.communities.prsdb.webapp.models.viewModels.emailModels.LandlordRegistrationConfirmationEmail
import uk.gov.communities.prsdb.webapp.services.AbsoluteUrlProvider
import uk.gov.communities.prsdb.webapp.services.AddressDataService
import uk.gov.communities.prsdb.webapp.services.AddressLookupService
import uk.gov.communities.prsdb.webapp.services.EmailNotificationService
import uk.gov.communities.prsdb.webapp.services.JourneyDataService
import uk.gov.communities.prsdb.webapp.services.LandlordService
import uk.gov.communities.prsdb.webapp.testHelpers.builders.JourneyDataBuilder
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.MockLandlordData
import uk.gov.communities.prsdb.webapp.testHelpers.validators.AlwaysTrueValidator
import java.net.URI

class LandlordRegistrationJourneyTests {
    @Mock
    lateinit var mockJourneyDataService: JourneyDataService

    @Mock
    lateinit var addressDataService: AddressDataService

    @Mock
    lateinit var landlordService: LandlordService

    @Mock
    lateinit var addressLookupService: AddressLookupService

    @Mock
    lateinit var confirmationEmailSender: EmailNotificationService<LandlordRegistrationConfirmationEmail>

    @Mock
    lateinit var urlProvider: AbsoluteUrlProvider

    val alwaysTrueValidator: AlwaysTrueValidator = AlwaysTrueValidator()

    @BeforeEach
    fun setup() {
        mockJourneyDataService = mock()
        addressDataService = mock()
        landlordService = mock()
        addressLookupService = mock()
        confirmationEmailSender = mock()
        urlProvider = mock()
    }

    @Nested
    inner class HandleAndSubmitTests {
        private lateinit var testJourney: LandlordRegistrationJourney

        @BeforeEach
        fun beforeEach() {
            whenever(
                landlordService.createLandlord(
                    baseUserId = anyOrNull(),
                    name = anyOrNull(),
                    email = anyOrNull(),
                    phoneNumber = anyOrNull(),
                    addressDataModel = anyOrNull(),
                    countryOfResidence = anyOrNull(),
                    isVerified = anyOrNull(),
                    nonEnglandOrWalesAddress = anyOrNull(),
                    dateOfBirth = anyOrNull(),
                ),
            ).thenReturn(MockLandlordData.createLandlord())
            whenever(urlProvider.buildLandlordDashboardUri()).thenReturn(URI.create("https://gov.uk"))

            testJourney =
                LandlordRegistrationJourney(
                    validator = alwaysTrueValidator,
                    journeyDataService = mockJourneyDataService,
                    addressLookupService = addressLookupService,
                    addressDataService = addressDataService,
                    landlordService = landlordService,
                    emailNotificationService = confirmationEmailSender,
                    absoluteUrlProvider = urlProvider,
                )
            setMockUser()
        }

        private fun setMockUser() {
            val name = "a-user-name"
            val authentication = mock<Authentication>()
            whenever(authentication.name).thenReturn(name)
            val context = mock<SecurityContext>()
            whenever(context.authentication).thenReturn(authentication)
            SecurityContextHolder.setContext(context)
        }

        @Suppress("ktlint:standard:max-line-length")
        @Test
        fun `when an international address is provided but the user sets themselves as england or wales resident, the international address is not saved to the database`() {
            // Arrange
            val journeyData =
                JourneyDataBuilder
                    .landlordDefault(
                        addressDataService,
                        localAuthorityService = mock(),
                    ).withNonEnglandOrWalesAndSelectedContactAddress(
                        "Angola",
                        "non england or wales address",
                        "uk contact address only",
                    ).withSelectedAddress(
                        "uk residential address",
                        localAuthority = LocalAuthority(),
                    )

            whenever(mockJourneyDataService.getJourneyDataFromSession()).thenReturn(journeyData.build())

            // Act
            completeStep(LandlordRegistrationStepId.Declaration)

            // Assert
            verify(landlordService).createLandlord(
                baseUserId = any(),
                name = any(),
                email = any(),
                phoneNumber = any(),
                addressDataModel = any(),
                countryOfResidence = any(),
                isVerified = any(),
                nonEnglandOrWalesAddress = argThat { internationalAddress -> internationalAddress.isNullOrBlank() },
                dateOfBirth = any(),
            )
        }

        private fun completeStep(
            stepId: LandlordRegistrationStepId,
            pageData: PageData = mapOf(),
        ) {
            testJourney.completeStep(
                stepPathSegment = stepId.urlPathSegment,
                pageData = pageData,
                subPageNumber = null,
                principal = mock(),
            )
        }
    }
}
