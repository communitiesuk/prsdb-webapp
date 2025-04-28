package uk.gov.communities.prsdb.webapp.forms.steps

import org.junit.jupiter.api.BeforeEach
import org.mockito.Mock
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import uk.gov.communities.prsdb.webapp.constants.LOOKED_UP_ADDRESSES_JOURNEY_DATA_KEY
import uk.gov.communities.prsdb.webapp.forms.pages.Page
import uk.gov.communities.prsdb.webapp.services.AddressLookupService
import uk.gov.communities.prsdb.webapp.services.JourneyDataService
import kotlin.test.Test
import kotlin.test.assertEquals

class LookupAddressStepTests {
    @Mock
    val mockAddressLookupService: AddressLookupService = mock()

    @Mock
    val mockJourneyDataService: JourneyDataService = mock()

    @Mock
    val mockPage: Page = mock()

    enum class LookupStepTestIds(
        override val urlPathSegment: String,
    ) : StepId {
        LookupAddress("lookup-address"),
        NoAddressFound("no-address-found"),
        SelectAddress("select-address"),
    }

    private lateinit var lookupAddressStep: LookupAddressStep<LookupStepTestIds>

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
    }

    @Test
    fun `getNextStep returns nextStepIfAddressesFound if the cached lookedUpAddress list is not empty`() {
        val journeyData = mapOf(LOOKED_UP_ADDRESSES_JOURNEY_DATA_KEY to "[{\"singleLineAddress\":\"1 Street Address, City, AB1 2CD\"}]")

        val nextStep = lookupAddressStep.nextAction(journeyData, null)

        assertEquals(LookupStepTestIds.SelectAddress, nextStep.first)
    }

    @Test
    fun `getNextStep returns nextStepIfNoAddressesFound if the cached lookedUpAddress list is empty`() {
        val journeyData = mapOf(LOOKED_UP_ADDRESSES_JOURNEY_DATA_KEY to "[]")

        val nextStep = lookupAddressStep.nextAction(journeyData, null)

        assertEquals(LookupStepTestIds.NoAddressFound, nextStep.first)
    }

    @Test
    fun `handleSubmitAndRedirect looks up addresses, caches the result and redirects`() {
        // Arrange

        val houseNumber = "15"
        val postcode = "AB1 2CD"

        val originalJourneyData =
            mutableMapOf(
                LookupStepTestIds.LookupAddress.urlPathSegment to
                    mapOf(
                        "houseNameOrNumber" to houseNumber,
                        "postcode" to postcode,
                    ),
            )

        val expectedUpdatedJourneyData =
            originalJourneyData + (LOOKED_UP_ADDRESSES_JOURNEY_DATA_KEY to "[]")

        // Act
        val redirectedUrl = lookupAddressStep.handleSubmitAndRedirect?.let { it(originalJourneyData, null) }

        // Assert
        verify(mockAddressLookupService).search(houseNumber, postcode)
        verify(mockJourneyDataService).setJourneyDataInSession(expectedUpdatedJourneyData)
        assertEquals(LookupStepTestIds.NoAddressFound.urlPathSegment, redirectedUrl)
    }
}
