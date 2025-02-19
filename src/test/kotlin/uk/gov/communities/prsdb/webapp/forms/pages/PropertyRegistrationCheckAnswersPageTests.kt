package uk.gov.communities.prsdb.webapp.forms.pages

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import org.springframework.ui.ExtendedModelMap
import org.springframework.ui.Model
import org.springframework.validation.Validator
import uk.gov.communities.prsdb.webapp.constants.enums.LandlordType
import uk.gov.communities.prsdb.webapp.constants.enums.LicensingType
import uk.gov.communities.prsdb.webapp.constants.enums.OwnershipType
import uk.gov.communities.prsdb.webapp.constants.enums.PropertyType
import uk.gov.communities.prsdb.webapp.forms.steps.RegisterPropertyStepId
import uk.gov.communities.prsdb.webapp.mockObjects.JourneyDataBuilder
import uk.gov.communities.prsdb.webapp.mockObjects.MockLocalAuthorityData.Companion.createLocalAuthority
import uk.gov.communities.prsdb.webapp.models.dataModels.AddressDataModel
import uk.gov.communities.prsdb.webapp.models.viewModels.SummaryListRowViewModel
import uk.gov.communities.prsdb.webapp.services.AddressDataService
import uk.gov.communities.prsdb.webapp.services.LocalAuthorityService

class PropertyRegistrationCheckAnswersPageTests {
    private lateinit var page: PropertyRegistrationCheckAnswersPage
    private lateinit var addressService: AddressDataService
    private lateinit var localAuthorityService: LocalAuthorityService
    private lateinit var validator: Validator
    private lateinit var model: Model
    private lateinit var pageData: Map<String, Any?>
    private lateinit var prevStepUrl: String
    private lateinit var journeyDataBuilder: JourneyDataBuilder

    @BeforeEach
    fun setup() {
        addressService = mock()
        localAuthorityService = mock()
        page = PropertyRegistrationCheckAnswersPage(addressService, localAuthorityService)
        validator = mock()
        whenever(validator.supports(any<Class<*>>())).thenReturn(true)
        model = ExtendedModelMap()
        pageData = mock()
        prevStepUrl = "mock"
        journeyDataBuilder = JourneyDataBuilder.propertyDefault(addressService, localAuthorityService)
    }

    private fun getPropertyDetails(journeyData: MutableMap<String, Any?>): List<SummaryListRowViewModel> {
        page.populateModelAndGetTemplateName(validator, model, pageData, prevStepUrl, journeyData)

        val propertyDetails = model.asMap()["propertyDetails"] as List<*>
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
                RegisterPropertyStepId.LookupAddress.urlPathSegment,
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
                RegisterPropertyStepId.ManualAddress.urlPathSegment,
            ),
            propertyDetails.single {
                it.fieldHeading == "forms.checkPropertyAnswers.propertyDetails.address"
            },
        )
        assertEquals(
            SummaryListRowViewModel(
                "forms.checkPropertyAnswers.propertyDetails.localAuthority",
                localAuthority.name,
                RegisterPropertyStepId.LocalAuthority.urlPathSegment,
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
                RegisterPropertyStepId.PropertyType.urlPathSegment,
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
                RegisterPropertyStepId.PropertyType.urlPathSegment,
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
                RegisterPropertyStepId.OwnershipType.urlPathSegment,
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
                RegisterPropertyStepId.LicensingType.urlPathSegment,
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
                RegisterPropertyStepId.LicensingType.urlPathSegment,
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
                RegisterPropertyStepId.Occupancy.urlPathSegment,
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
                RegisterPropertyStepId.Occupancy.urlPathSegment,
            ),
            propertyDetails.single {
                it.fieldHeading == "forms.checkPropertyAnswers.propertyDetails.occupied"
            },
        )
        assertEquals(
            SummaryListRowViewModel(
                "forms.checkPropertyAnswers.propertyDetails.households",
                households,
                RegisterPropertyStepId.NumberOfHouseholds.urlPathSegment,
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
                RegisterPropertyStepId.NumberOfPeople.urlPathSegment,
            ),
            propertyDetails.single {
                it.fieldHeading == "forms.checkPropertyAnswers.propertyDetails.people"
            },
        )
    }

    @Test
    fun `propertyDetails has a single line landlord type row`() {
        // Arrange
        val journeyData = journeyDataBuilder.withLandlordType(LandlordType.SOLE).build()

        // Act
        val propertyDetails = getPropertyDetails(journeyData)

        // Assert
        assertEquals(
            SummaryListRowViewModel(
                "forms.checkPropertyAnswers.propertyDetails.landlordType",
                LandlordType.SOLE,
                RegisterPropertyStepId.LandlordType.urlPathSegment,
            ),
            propertyDetails.single {
                it.fieldHeading == "forms.checkPropertyAnswers.propertyDetails.landlordType"
            },
        )
    }
}
