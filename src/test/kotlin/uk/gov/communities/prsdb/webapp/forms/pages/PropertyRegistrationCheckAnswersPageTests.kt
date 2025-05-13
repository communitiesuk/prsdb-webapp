package uk.gov.communities.prsdb.webapp.forms.pages

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import org.springframework.validation.Validator
import uk.gov.communities.prsdb.webapp.constants.enums.LicensingType
import uk.gov.communities.prsdb.webapp.constants.enums.OwnershipType
import uk.gov.communities.prsdb.webapp.constants.enums.PropertyType
import uk.gov.communities.prsdb.webapp.forms.JourneyData
import uk.gov.communities.prsdb.webapp.forms.PageData
import uk.gov.communities.prsdb.webapp.forms.steps.RegisterPropertyStepId
import uk.gov.communities.prsdb.webapp.models.dataModels.AddressDataModel
import uk.gov.communities.prsdb.webapp.models.viewModels.summaryModels.SummaryListRowViewModel
import uk.gov.communities.prsdb.webapp.services.JourneyDataService
import uk.gov.communities.prsdb.webapp.services.LocalAuthorityService
import uk.gov.communities.prsdb.webapp.testHelpers.builders.JourneyDataBuilder
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.MockLocalAuthorityData.Companion.createLocalAuthority

class PropertyRegistrationCheckAnswersPageTests {
    private lateinit var page: PropertyRegistrationCheckAnswersPage
    private lateinit var localAuthorityService: LocalAuthorityService
    private lateinit var journeyDataService: JourneyDataService
    private lateinit var validator: Validator
    private lateinit var pageData: PageData
    private lateinit var prevStepUrl: String
    private lateinit var journeyDataBuilder: JourneyDataBuilder

    @BeforeEach
    fun setup() {
        localAuthorityService = mock()
        journeyDataService = mock()
        page = PropertyRegistrationCheckAnswersPage(localAuthorityService, journeyDataService)
        validator = mock()
        whenever(validator.supports(any<Class<*>>())).thenReturn(true)
        pageData = mock()
        prevStepUrl = "mock"
        journeyDataBuilder = JourneyDataBuilder.propertyDefault(localAuthorityService)
    }

    private fun getPropertyDetails(journeyData: JourneyData): List<SummaryListRowViewModel> {
        whenever(journeyDataService.getJourneyDataFromSession()).thenReturn(journeyData)

        val bindingResult = page.bindDataToFormModel(validator, pageData)
        val result = page.getModelAndView(bindingResult, prevStepUrl, journeyData, null)

        val propertyDetails = result.model["propertyDetails"] as List<*>
        return propertyDetails.filterIsInstance<SummaryListRowViewModel>()
    }

    @Test
    fun `propertyDetails has the correct address summary rows for a selected address`() {
        // Arrange
        val addressName = "4, Example Road, EG"
        val uprn: Long = 1002001
        val localAuthority = createLocalAuthority()
        val journeyData = journeyDataBuilder.withSelectedAddress(addressName, uprn, localAuthority).build()

        // Act
        val propertyDetails = getPropertyDetails(journeyData)

        assertEquals(
            SummaryListRowViewModel(
                "forms.checkPropertyAnswers.propertyDetails.address",
                addressName,
                RegisterPropertyStepId.LookupAddress.urlPathSegment +
                    "?changingAnswerFor=${RegisterPropertyStepId.LookupAddress.urlPathSegment}",
            ),
            propertyDetails.single {
                it.fieldHeading == "forms.checkPropertyAnswers.propertyDetails.address"
            },
        )
        assertEquals(
            SummaryListRowViewModel(
                "forms.checkPropertyAnswers.propertyDetails.uprn",
                uprn,
                null,
            ),
            propertyDetails.single {
                it.fieldHeading == "forms.checkPropertyAnswers.propertyDetails.uprn"
            },
        )
        assertEquals(
            SummaryListRowViewModel(
                "forms.checkPropertyAnswers.propertyDetails.localAuthority",
                localAuthority.name,
                null,
            ),
            propertyDetails.single {
                it.fieldHeading == "forms.checkPropertyAnswers.propertyDetails.localAuthority"
            },
        )
    }

    @Test
    fun `propertyDetails has the correct address summary rows for a manual address`() {
        // Arrange
        val addressLineOne = "3 Example Road"
        val townOrCity = "Townville"
        val postcode = "EG1 2AB"
        val localAuthority = createLocalAuthority()

        val journeyData =
            journeyDataBuilder
                .withManualAddress(addressLineOne, townOrCity, postcode, localAuthority)
                .build()

        // Act
        val propertyDetails = getPropertyDetails(journeyData)

        // Assert
        assertEquals(
            SummaryListRowViewModel(
                "forms.checkPropertyAnswers.propertyDetails.address",
                AddressDataModel.manualAddressDataToSingleLineAddress(addressLineOne, townOrCity, postcode),
                RegisterPropertyStepId.ManualAddress.urlPathSegment +
                    "?changingAnswerFor=${RegisterPropertyStepId.ManualAddress.urlPathSegment}",
            ),
            propertyDetails.single {
                it.fieldHeading == "forms.checkPropertyAnswers.propertyDetails.address"
            },
        )
        assertEquals(
            SummaryListRowViewModel(
                "forms.checkPropertyAnswers.propertyDetails.localAuthority",
                localAuthority.name,
                RegisterPropertyStepId.LocalAuthority.urlPathSegment +
                    "?changingAnswerFor=${RegisterPropertyStepId.LocalAuthority.urlPathSegment}",
            ),
            propertyDetails.single {
                it.fieldHeading == "forms.checkPropertyAnswers.propertyDetails.localAuthority"
            },
        )

        Assertions.assertFalse(
            propertyDetails.any {
                it.fieldHeading == "forms.checkPropertyAnswers.propertyDetails.uprn"
            },
        )
    }

    @Test
    fun `propertyDetails has a simple property type summary row for a selected type`() {
        // Arrange
        val journeyData = journeyDataBuilder.withPropertyType(PropertyType.DETACHED_HOUSE).build()

        // Act
        val propertyDetails = getPropertyDetails(journeyData)

        assertEquals(
            SummaryListRowViewModel(
                "forms.checkPropertyAnswers.propertyDetails.type",
                PropertyType.DETACHED_HOUSE,
                RegisterPropertyStepId.PropertyType.urlPathSegment +
                    "?changingAnswerFor=${RegisterPropertyStepId.PropertyType.urlPathSegment}",
            ),
            propertyDetails.single {
                it.fieldHeading == "forms.checkPropertyAnswers.propertyDetails.type"
            },
        )
    }

    @Test
    fun `propertyDetails has the correct multiline property type summary row for other`() {
        // Arrange
        val customType = "custom type name"
        val journeyData = journeyDataBuilder.withPropertyType(PropertyType.OTHER, customType).build()

        // Act
        val propertyDetails = getPropertyDetails(journeyData)

        // Assert
        assertEquals(
            SummaryListRowViewModel(
                "forms.checkPropertyAnswers.propertyDetails.type",
                listOf(PropertyType.OTHER, customType),
                RegisterPropertyStepId.PropertyType.urlPathSegment +
                    "?changingAnswerFor=${RegisterPropertyStepId.PropertyType.urlPathSegment}",
            ),
            propertyDetails.single {
                it.fieldHeading == "forms.checkPropertyAnswers.propertyDetails.type"
            },
        )
    }

    @Test
    fun `propertyDetails has the correct ownership type summary row`() {
        // Arrange
        val journeyData = journeyDataBuilder.withOwnershipType(OwnershipType.FREEHOLD).build()

        // Act
        val propertyDetails = getPropertyDetails(journeyData)

        // Assert
        assertEquals(
            SummaryListRowViewModel(
                "forms.checkPropertyAnswers.propertyDetails.ownership",
                OwnershipType.FREEHOLD,
                RegisterPropertyStepId.OwnershipType.urlPathSegment +
                    "?changingAnswerFor=${RegisterPropertyStepId.OwnershipType.urlPathSegment}",
            ),
            propertyDetails.single {
                it.fieldHeading == "forms.checkPropertyAnswers.propertyDetails.ownership"
            },
        )
    }

    @Test
    fun `propertyDetails has a simple licensing type summary row when there is no licensing`() {
        // Arrange
        val journeyData = journeyDataBuilder.withLicensingType(LicensingType.NO_LICENSING).build()

        // Act
        val propertyDetails = getPropertyDetails(journeyData)

        // Assert
        assertEquals(
            SummaryListRowViewModel(
                "forms.checkPropertyAnswers.propertyDetails.licensing",
                LicensingType.NO_LICENSING,
                RegisterPropertyStepId.LicensingType.urlPathSegment +
                    "?changingAnswerFor=${RegisterPropertyStepId.LicensingType.urlPathSegment}",
            ),
            propertyDetails.single {
                it.fieldHeading == "forms.checkPropertyAnswers.propertyDetails.licensing"
            },
        )
    }

    @Test
    fun `propertyDetails has the correct multiline licensing type summary row when there is licensing`() {
        // Arrange
        val licenceNumber = "entered licence number"
        val journeyData = journeyDataBuilder.withLicensingType(LicensingType.SELECTIVE_LICENCE, licenceNumber).build()

        // Act
        val propertyDetails = getPropertyDetails(journeyData)

        // Assert
        assertEquals(
            SummaryListRowViewModel(
                "forms.checkPropertyAnswers.propertyDetails.licensing",
                listOf(LicensingType.SELECTIVE_LICENCE, licenceNumber),
                RegisterPropertyStepId.LicensingType.urlPathSegment +
                    "?changingAnswerFor=${RegisterPropertyStepId.LicensingType.urlPathSegment}",
            ),
            propertyDetails.single {
                it.fieldHeading == "forms.checkPropertyAnswers.propertyDetails.licensing"
            },
        )
    }

    @Test
    fun `propertyDetails has a single line occupation summary row when there are no tenants`() {
        // Arrange
        val journeyData = journeyDataBuilder.withNoTenants().build()

        // Act
        val propertyDetails = getPropertyDetails(journeyData)

        // Assert
        assertEquals(
            SummaryListRowViewModel(
                "forms.checkPropertyAnswers.propertyDetails.occupied",
                false,
                RegisterPropertyStepId.Occupancy.urlPathSegment +
                    "?changingAnswerFor=${RegisterPropertyStepId.Occupancy.urlPathSegment}",
            ),
            propertyDetails.single {
                it.fieldHeading == "forms.checkPropertyAnswers.propertyDetails.occupied"
            },
        )
    }

    @Test
    fun `propertyDetails has the correct three occupation summary rows when there are tenants`() {
        // Arrange
        val households = 3
        val people = 5
        val journeyData = journeyDataBuilder.withTenants(households, people).build()

        // Act
        val propertyDetails = getPropertyDetails(journeyData)

        // Assert
        assertEquals(
            SummaryListRowViewModel(
                "forms.checkPropertyAnswers.propertyDetails.occupied",
                true,
                RegisterPropertyStepId.Occupancy.urlPathSegment +
                    "?changingAnswerFor=${RegisterPropertyStepId.Occupancy.urlPathSegment}",
            ),
            propertyDetails.single {
                it.fieldHeading == "forms.checkPropertyAnswers.propertyDetails.occupied"
            },
        )
        assertEquals(
            SummaryListRowViewModel(
                "forms.checkPropertyAnswers.propertyDetails.households",
                households,
                RegisterPropertyStepId.NumberOfHouseholds.urlPathSegment +
                    "?changingAnswerFor=${RegisterPropertyStepId.NumberOfHouseholds.urlPathSegment}",
            ),
            propertyDetails
                .single {
                    it.fieldHeading == "forms.checkPropertyAnswers.propertyDetails.households"
                },
        )
        assertEquals(
            SummaryListRowViewModel(
                "forms.checkPropertyAnswers.propertyDetails.people",
                people,
                RegisterPropertyStepId.NumberOfPeople.urlPathSegment +
                    "?changingAnswerFor=${RegisterPropertyStepId.NumberOfPeople.urlPathSegment}",
            ),
            propertyDetails.single {
                it.fieldHeading == "forms.checkPropertyAnswers.propertyDetails.people"
            },
        )
    }
}
