package uk.gov.communities.prsdb.webapp.forms.pages

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import org.springframework.validation.Validator
import uk.gov.communities.prsdb.webapp.constants.CHECKING_ANSWERS_FOR_PARAMETER_NAME
import uk.gov.communities.prsdb.webapp.constants.enums.LicensingType
import uk.gov.communities.prsdb.webapp.constants.enums.OwnershipType
import uk.gov.communities.prsdb.webapp.constants.enums.PropertyType
import uk.gov.communities.prsdb.webapp.forms.JourneyData
import uk.gov.communities.prsdb.webapp.forms.PageData
import uk.gov.communities.prsdb.webapp.forms.steps.RegisterPropertyStepId
import uk.gov.communities.prsdb.webapp.models.dataModels.AddressDataModel
import uk.gov.communities.prsdb.webapp.models.viewModels.SectionHeaderViewModel
import uk.gov.communities.prsdb.webapp.models.viewModels.summaryModels.SummaryListRowActionViewModel
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
        page = PropertyRegistrationCheckAnswersPage(journeyDataService, localAuthorityService, "/redirect")
        validator = mock()
        whenever(validator.supports(any<Class<*>>())).thenReturn(true)
        pageData = mock()
        prevStepUrl = "mock"
        journeyDataBuilder = JourneyDataBuilder.propertyDefault(localAuthorityService)
    }

    private fun getPropertyDetails(journeyData: JourneyData): List<SummaryListRowViewModel> {
        whenever(journeyDataService.getJourneyDataFromSession()).thenReturn(journeyData)

        val bindingResult = page.bindDataToFormModel(validator, pageData)
        val result = page.getModelAndView(bindingResult, prevStepUrl, journeyData, SectionHeaderViewModel("any-key", 0, 0))

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
                SummaryListRowActionViewModel(
                    "forms.links.change",
                    RegisterPropertyStepId.LookupAddress.urlPathSegment +
                        "?$CHECKING_ANSWERS_FOR_PARAMETER_NAME=${RegisterPropertyStepId.LookupAddress.urlPathSegment}",
                ),
            ),
            propertyDetails.single {
                it.fieldHeading == "forms.checkPropertyAnswers.propertyDetails.address"
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
                SummaryListRowActionViewModel(
                    "forms.links.change",
                    RegisterPropertyStepId.ManualAddress.urlPathSegment +
                        "?$CHECKING_ANSWERS_FOR_PARAMETER_NAME=${RegisterPropertyStepId.ManualAddress.urlPathSegment}",
                ),
            ),
            propertyDetails.single {
                it.fieldHeading == "forms.checkPropertyAnswers.propertyDetails.address"
            },
        )
        assertEquals(
            SummaryListRowViewModel(
                "forms.checkPropertyAnswers.propertyDetails.localAuthority",
                localAuthority.name,
                SummaryListRowActionViewModel(
                    "forms.links.change",
                    RegisterPropertyStepId.LocalAuthority.urlPathSegment +
                        "?$CHECKING_ANSWERS_FOR_PARAMETER_NAME=${RegisterPropertyStepId.LocalAuthority.urlPathSegment}",
                ),
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
                SummaryListRowActionViewModel(
                    "forms.links.change",
                    RegisterPropertyStepId.PropertyType.urlPathSegment +
                        "?$CHECKING_ANSWERS_FOR_PARAMETER_NAME=${RegisterPropertyStepId.PropertyType.urlPathSegment}",
                ),
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
                SummaryListRowActionViewModel(
                    "forms.links.change",
                    RegisterPropertyStepId.PropertyType.urlPathSegment +
                        "?$CHECKING_ANSWERS_FOR_PARAMETER_NAME=${RegisterPropertyStepId.PropertyType.urlPathSegment}",
                ),
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
                SummaryListRowActionViewModel(
                    "forms.links.change",
                    RegisterPropertyStepId.OwnershipType.urlPathSegment +
                        "?$CHECKING_ANSWERS_FOR_PARAMETER_NAME=${RegisterPropertyStepId.OwnershipType.urlPathSegment}",
                ),
            ),
            propertyDetails.single {
                it.fieldHeading == "forms.checkPropertyAnswers.propertyDetails.ownership"
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
                SummaryListRowActionViewModel(
                    "forms.links.change",
                    RegisterPropertyStepId.Occupancy.urlPathSegment +
                        "?$CHECKING_ANSWERS_FOR_PARAMETER_NAME=${RegisterPropertyStepId.Occupancy.urlPathSegment}",
                ),
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
                SummaryListRowActionViewModel(
                    "forms.links.change",
                    RegisterPropertyStepId.Occupancy.urlPathSegment +
                        "?$CHECKING_ANSWERS_FOR_PARAMETER_NAME=${RegisterPropertyStepId.Occupancy.urlPathSegment}",
                ),
            ),
            propertyDetails.single {
                it.fieldHeading == "forms.checkPropertyAnswers.propertyDetails.occupied"
            },
        )
        assertEquals(
            SummaryListRowViewModel(
                "forms.checkPropertyAnswers.propertyDetails.households",
                households,
                SummaryListRowActionViewModel(
                    "forms.links.change",
                    RegisterPropertyStepId.NumberOfHouseholds.urlPathSegment +
                        "?$CHECKING_ANSWERS_FOR_PARAMETER_NAME=${RegisterPropertyStepId.NumberOfHouseholds.urlPathSegment}",
                ),
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
                SummaryListRowActionViewModel(
                    "forms.links.change",
                    RegisterPropertyStepId.NumberOfPeople.urlPathSegment +
                        "?$CHECKING_ANSWERS_FOR_PARAMETER_NAME=${RegisterPropertyStepId.NumberOfPeople.urlPathSegment}",
                ),
            ),
            propertyDetails.single {
                it.fieldHeading == "forms.checkPropertyAnswers.propertyDetails.people"
            },
        )
    }

    @Nested
    inner class LicensingDetailsTests {
        private fun getLicensingDetails(journeyData: JourneyData): List<SummaryListRowViewModel> {
            whenever(journeyDataService.getJourneyDataFromSession()).thenReturn(journeyData)

            val bindingResult = page.bindDataToFormModel(validator, pageData)
            val result =
                page.getModelAndView(bindingResult, prevStepUrl, journeyData, SectionHeaderViewModel("any-key", 0, 0))

            val licensingDetails = result.model["licensingDetails"] as List<*>
            return licensingDetails.filterIsInstance<SummaryListRowViewModel>()
        }

        @Test
        fun `licensingDetails only has the licensing type summary row when there is no licensing`() {
            // Arrange
            val journeyData = journeyDataBuilder.withLicensing(LicensingType.NO_LICENSING).build()

            // Act
            val licensingDetails = getLicensingDetails(journeyData)

            // Assert
            assertEquals(
                SummaryListRowViewModel(
                    "forms.checkPropertyAnswers.propertyDetails.licensingType",
                    LicensingType.NO_LICENSING,
                    SummaryListRowActionViewModel(
                        "forms.links.change",
                        RegisterPropertyStepId.LicensingType.urlPathSegment +
                            "?$CHECKING_ANSWERS_FOR_PARAMETER_NAME=${RegisterPropertyStepId.LicensingType.urlPathSegment}",
                    ),
                ),
                licensingDetails.singleOrNull(),
            )
        }

        @Test
        fun `licensingDetails has the correct summary rows for HMO mandatory licensing`() {
            // Arrange
            val licenceNumber = "123456789012"
            val journeyData =
                journeyDataBuilder.withLicensing(LicensingType.HMO_MANDATORY_LICENCE, licenceNumber).build()

            // Act
            val licensingDetails = getLicensingDetails(journeyData)

            // Assert
            assertEquals(
                SummaryListRowViewModel(
                    "forms.checkPropertyAnswers.propertyDetails.licensingType",
                    LicensingType.HMO_MANDATORY_LICENCE,
                    SummaryListRowActionViewModel(
                        "forms.links.change",
                        RegisterPropertyStepId.LicensingType.urlPathSegment +
                            "?$CHECKING_ANSWERS_FOR_PARAMETER_NAME=${RegisterPropertyStepId.LicensingType.urlPathSegment}",
                    ),
                ),
                licensingDetails.single {
                    it.fieldHeading == "forms.checkPropertyAnswers.propertyDetails.licensingType"
                },
            )
            assertEquals(
                SummaryListRowViewModel(
                    "propertyDetails.propertyRecord.licensingInformation.licensingNumber",
                    licenceNumber,
                    SummaryListRowActionViewModel(
                        "forms.links.change",
                        RegisterPropertyStepId.HmoMandatoryLicence.urlPathSegment +
                            "?$CHECKING_ANSWERS_FOR_PARAMETER_NAME=${RegisterPropertyStepId.HmoMandatoryLicence.urlPathSegment}",
                    ),
                ),
                licensingDetails.single {
                    it.fieldHeading == "propertyDetails.propertyRecord.licensingInformation.licensingNumber"
                },
            )
        }
    }
}
