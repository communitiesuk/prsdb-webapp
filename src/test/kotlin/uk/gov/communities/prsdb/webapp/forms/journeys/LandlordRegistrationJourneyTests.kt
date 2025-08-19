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
import uk.gov.communities.prsdb.webapp.database.entity.LocalAuthority
import uk.gov.communities.prsdb.webapp.forms.steps.LandlordRegistrationStepId
import uk.gov.communities.prsdb.webapp.services.AddressLookupService
import uk.gov.communities.prsdb.webapp.services.JourneyDataService
import uk.gov.communities.prsdb.webapp.services.LandlordService
import uk.gov.communities.prsdb.webapp.testHelpers.JourneyTestHelper
import uk.gov.communities.prsdb.webapp.testHelpers.builders.JourneyDataBuilder
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.AlwaysTrueValidator
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.MockLandlordData

class LandlordRegistrationJourneyTests {
    @Mock
    lateinit var mockJourneyDataService: JourneyDataService

    @Mock
    lateinit var landlordService: LandlordService

    @Mock
    lateinit var addressLookupService: AddressLookupService

    val alwaysTrueValidator: AlwaysTrueValidator = AlwaysTrueValidator()

    @BeforeEach
    fun setup() {
        mockJourneyDataService = mock()
        landlordService = mock()
        addressLookupService = mock()
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
                    hasAcceptedPrivacyNotice = anyOrNull(),
                    nonEnglandOrWalesAddress = anyOrNull(),
                    dateOfBirth = anyOrNull(),
                ),
            ).thenReturn(MockLandlordData.createLandlord())

            testJourney =
                LandlordRegistrationJourney(
                    validator = alwaysTrueValidator,
                    journeyDataService = mockJourneyDataService,
                    addressLookupService = addressLookupService,
                    landlordService = landlordService,
                    securityContextService = mock(),
                )
            JourneyTestHelper.setMockUser("a-user-name")
        }

        @Suppress("ktlint:standard:max-line-length")
        @Test
        fun `when an international address is provided but the user sets themselves as england or wales resident, the international address is not saved to the database`() {
            // Arrange
            val journeyData =
                JourneyDataBuilder
                    .landlordDefault(
                        localAuthorityService = mock(),
                    ).withNonEnglandOrWalesAndSelectedContactAddress(
                        "Angola",
                        "non england or wales address",
                        "uk contact address only",
                    ).withSelectedAddress(
                        "uk residential address",
                        localAuthority = LocalAuthority(),
                    ).build()

            whenever(mockJourneyDataService.getJourneyDataFromSession()).thenReturn(journeyData)

            // Act
            testJourney.completeStep(
                stepPathSegment = LandlordRegistrationStepId.Declaration.urlPathSegment,
                formData = mapOf(),
                subPageNumber = null,
                principal = mock(),
            )

            // Assert
            verify(landlordService).createLandlord(
                baseUserId = any(),
                name = any(),
                email = any(),
                phoneNumber = any(),
                addressDataModel = any(),
                countryOfResidence = any(),
                isVerified = any(),
                hasAcceptedPrivacyNotice = any(),
                nonEnglandOrWalesAddress = argThat { internationalAddress -> internationalAddress.isNullOrBlank() },
                dateOfBirth = any(),
            )
        }
    }
}
