package uk.gov.communities.prsdb.webapp.forms

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.mockito.Mock
import org.mockito.Mockito.mock
import org.mockito.kotlin.any
import org.mockito.kotlin.argThat
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.security.core.Authentication
import org.springframework.security.core.context.SecurityContext
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.validation.Errors
import org.springframework.validation.Validator
import uk.gov.communities.prsdb.webapp.database.entity.LocalAuthority
import uk.gov.communities.prsdb.webapp.forms.journeys.LandlordRegistrationJourney
import uk.gov.communities.prsdb.webapp.forms.journeys.PageData
import uk.gov.communities.prsdb.webapp.forms.steps.LandlordRegistrationStepId
import uk.gov.communities.prsdb.webapp.mockObjects.JourneyDataBuilder
import uk.gov.communities.prsdb.webapp.mockObjects.MockLandlordData
import uk.gov.communities.prsdb.webapp.models.viewModels.emailModels.LandlordRegistrationConfirmationEmail
import uk.gov.communities.prsdb.webapp.services.AddressDataService
import uk.gov.communities.prsdb.webapp.services.AddressLookupService
import uk.gov.communities.prsdb.webapp.services.EmailNotificationService
import uk.gov.communities.prsdb.webapp.services.JourneyDataService
import uk.gov.communities.prsdb.webapp.services.LandlordService

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

    val alwaysTrueValidator: AlwaysTrueValidator = AlwaysTrueValidator()

    class AlwaysTrueValidator : Validator {
        override fun supports(clazz: Class<*>): Boolean = true

        override fun validate(
            target: Any,
            errors: Errors,
        ) {}
    }

    @BeforeEach
    fun setup() {
        mockJourneyDataService = mock()
        addressDataService = mock()
        landlordService = mock()
        addressLookupService = mock()
        confirmationEmailSender = mock()
    }

    @Nested
    inner class HandleAndSubmitTests {
        private lateinit var testJourney: LandlordRegistrationJourney

        @BeforeEach
        fun beforeEach() {
            whenever(
                landlordService.createLandlord(any(), any(), any(), any(), any(), any(), any(), any(), any()),
            ).thenReturn(MockLandlordData.createLandlord())

            testJourney =
                LandlordRegistrationJourney(
                    validator = alwaysTrueValidator,
                    journeyDataService = mockJourneyDataService,
                    addressLookupService = addressLookupService,
                    addressDataService = addressDataService,
                    landlordService = landlordService,
                    emailNotificationService = confirmationEmailSender,
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
            testJourney.updateJourneyDataAndGetViewNameOrRedirect(
                stepId = stepId,
                pageData = pageData,
                subPageNumber = null,
                principal = mock(),
                model = mock(),
            )
        }
    }
}
