package uk.gov.communities.prsdb.webapp.forms.steps

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.junit.jupiter.MockitoExtension
import uk.gov.communities.prsdb.webapp.forms.pages.Page
import uk.gov.communities.prsdb.webapp.services.AddressLookupService
import uk.gov.communities.prsdb.webapp.services.JourneyDataService
import uk.gov.communities.prsdb.webapp.testHelpers.builders.JourneyDataBuilder
import kotlin.test.Test
import kotlin.test.assertEquals

@ExtendWith(MockitoExtension::class)
class LookupAddressStepTests {
    @Mock
    private lateinit var mockAddressLookupService: AddressLookupService

    @Mock
    private lateinit var mockJourneyDataService: JourneyDataService

    @Mock
    private lateinit var mockPage: Page

    private lateinit var journeyDataBuilder: JourneyDataBuilder

    private lateinit var lookupAddressStep: LookupAddressStep<LookupStepTestIds>

    enum class LookupStepTestIds(
        override val urlPathSegment: String,
    ) : StepId {
        LookupAddress("lookup-address"),
        NoAddressFound("no-address-found"),
        SelectAddress("select-address"),
    }

    @BeforeEach
    fun setup() {
        lookupAddressStep =
            LookupAddressStep(
                id = LookupStepTestIds.LookupAddress,
                page = mockPage,
                nextStepIfAddressesFound = LookupStepTestIds.SelectAddress,
                nextStepIfNoAddressesFound = LookupStepTestIds.NoAddressFound,
                addressLookupService = mockAddressLookupService,
                journeyDataService = mockJourneyDataService,
            )

        journeyDataBuilder = JourneyDataBuilder(mock())
    }

    @Test
    fun `getNextStep returns nextStepIfAddressesFound if the cached lookedUpAddress list is not empty`() {
        val journeyData =
            journeyDataBuilder
                .withLookedUpAddresses()
                .build()

        val nextStep = lookupAddressStep.nextAction(journeyData, null)

        assertEquals(LookupStepTestIds.SelectAddress, nextStep.first)
    }

    @Test
    fun `getNextStep returns nextStepIfNoAddressesFound if the cached lookedUpAddress list is empty`() {
        val journeyData =
            journeyDataBuilder
                .withEmptyLookedUpAddresses()
                .build()

        val nextStep = lookupAddressStep.nextAction(journeyData, null)

        assertEquals(LookupStepTestIds.NoAddressFound, nextStep.first)
    }

    @Test
    fun `handleSubmitAndRedirect looks up addresses, caches the result and redirects`() {
        // Arrange

        val houseNumber = "15"
        val postcode = "AB1 2CD"

        val originalJourneyData =
            journeyDataBuilder
                .withLookupAddress(houseNumber, postcode)
                .build()

        val expectedUpdatedJourneyData =
            journeyDataBuilder
                .withLookupAddress(houseNumber, postcode)
                .withEmptyLookedUpAddresses()
                .build()

        // Act
        val redirectedUrl = lookupAddressStep.handleSubmitAndRedirect?.let { it(originalJourneyData, null, null) }

        // Assert
        verify(mockAddressLookupService).search(houseNumber, postcode)
        verify(mockJourneyDataService).setJourneyDataInSession(expectedUpdatedJourneyData)
        assertEquals(LookupStepTestIds.NoAddressFound.urlPathSegment, redirectedUrl)
    }
}
