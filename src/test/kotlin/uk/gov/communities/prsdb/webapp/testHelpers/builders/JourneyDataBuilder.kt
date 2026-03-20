package uk.gov.communities.prsdb.webapp.testHelpers.builders

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.mockito.Mockito.mock
import org.mockito.kotlin.whenever
import uk.gov.communities.prsdb.webapp.constants.MANUAL_ADDRESS_CHOSEN
import uk.gov.communities.prsdb.webapp.constants.enums.LicensingType
import uk.gov.communities.prsdb.webapp.constants.enums.NonStepJourneyDataKey
import uk.gov.communities.prsdb.webapp.constants.enums.OwnershipType
import uk.gov.communities.prsdb.webapp.constants.enums.PropertyType
import uk.gov.communities.prsdb.webapp.database.entity.LocalCouncil
import uk.gov.communities.prsdb.webapp.journeys.JourneyData
import uk.gov.communities.prsdb.webapp.journeys.propertyCompliance.steps.CheckMatchedEpcStep
import uk.gov.communities.prsdb.webapp.journeys.propertyCompliance.steps.EicrExemptionMissingStep
import uk.gov.communities.prsdb.webapp.journeys.propertyCompliance.steps.EicrExemptionStep
import uk.gov.communities.prsdb.webapp.journeys.propertyCompliance.steps.EicrIssueDateStep
import uk.gov.communities.prsdb.webapp.journeys.propertyCompliance.steps.EicrStep
import uk.gov.communities.prsdb.webapp.journeys.propertyCompliance.steps.EicrUploadConfirmationStep
import uk.gov.communities.prsdb.webapp.journeys.propertyCompliance.steps.EpcExemptionConfirmationStep
import uk.gov.communities.prsdb.webapp.journeys.propertyCompliance.steps.EpcExpiredStep
import uk.gov.communities.prsdb.webapp.journeys.propertyCompliance.steps.EpcExpiryCheckStep
import uk.gov.communities.prsdb.webapp.journeys.propertyCompliance.steps.EpcNotFoundStep
import uk.gov.communities.prsdb.webapp.journeys.propertyCompliance.steps.GasSafetyExemptionMissingStep
import uk.gov.communities.prsdb.webapp.journeys.propertyCompliance.steps.GasSafetyExemptionStep
import uk.gov.communities.prsdb.webapp.journeys.propertyCompliance.steps.GasSafetyIssueDateStep
import uk.gov.communities.prsdb.webapp.journeys.propertyCompliance.steps.GasSafetyOutdatedStep
import uk.gov.communities.prsdb.webapp.journeys.propertyCompliance.steps.GasSafetyStep
import uk.gov.communities.prsdb.webapp.journeys.propertyCompliance.steps.GasSafetyUploadConfirmationStep
import uk.gov.communities.prsdb.webapp.journeys.propertyCompliance.steps.LowEnergyRatingStep
import uk.gov.communities.prsdb.webapp.journeys.propertyCompliance.steps.MeesExemptionConfirmationStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.HouseholdStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.LocalCouncilStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.OccupiedStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.PropertyRegistrationCyaStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.TenantsStep
import uk.gov.communities.prsdb.webapp.models.dataModels.AddressDataModel
import uk.gov.communities.prsdb.webapp.models.dataModels.EpcDataModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.CheckMatchedEpcFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.EicrExemptionFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.EicrFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.EpcExpiryCheckFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.GasSafetyExemptionFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.GasSafetyFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.NumberOfHouseholdsFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.NumberOfPeopleFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.OccupancyFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.TodayOrPastDateFormModel
import uk.gov.communities.prsdb.webapp.services.LocalCouncilService
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.MockLocalCouncilData.Companion.createLocalCouncil
import java.time.LocalDate

class JourneyDataBuilder(
    private val mockLocalCouncilService: LocalCouncilService = mock(),
    initialJourneyData: JourneyData? = null,
) {
    private val journeyData = initialJourneyData?.toMutableMap() ?: mutableMapOf()

    fun build() = journeyData

    companion object {
        const val DEFAULT_ADDRESS = "4, Example Road, EG"

        private val defaultPropertyJourneyData: JourneyData =
            mapOf(
                "lookup-address" to
                    mapOf(
                        "postcode" to "EG",
                        "houseNameOrNumber" to "4",
                    ),
                "select-address" to
                    mapOf(
                        "address" to DEFAULT_ADDRESS,
                    ),
                "property-type" to
                    mapOf(
                        "propertyType" to "OTHER",
                        "customPropertyType" to "Bungalow",
                    ),
                "ownership-type" to
                    mapOf(
                        "ownershipType" to "FREEHOLD",
                    ),
                "occupancy" to
                    mapOf(
                        "occupied" to "true",
                    ),
                "number-of-households" to
                    mapOf(
                        "numberOfHouseholds" to "2",
                    ),
                "number-of-people" to
                    mapOf(
                        "numberOfPeople" to "4",
                    ),
                "licensing-type" to
                    mapOf(
                        "licensingType" to "HMO_MANDATORY_LICENCE",
                    ),
                "hmo-mandatory-licence" to
                    mapOf(
                        "licenceNumber" to "test1234",
                    ),
                PropertyRegistrationCyaStep.ROUTE_SEGMENT to emptyMap(),
            )

        fun propertyDefault(localCouncilService: LocalCouncilService) =
            JourneyDataBuilder(localCouncilService, defaultPropertyJourneyData).withSelectedAddress(
                DEFAULT_ADDRESS,
                709902,
                createLocalCouncil(),
            )
    }

    fun withLookupAddress(
        houseNameOrNumber: String = "4",
        postcode: String = "EG1 2AB",
        isContactAddress: Boolean = false,
    ): JourneyDataBuilder {
        val lookupAddressKey = if (isContactAddress) "lookup-contact-address" else "lookup-address"
        journeyData[lookupAddressKey] = mapOf("houseNameOrNumber" to houseNameOrNumber, "postcode" to postcode)
        return this
    }

    fun withEmptyLookedUpAddresses(): JourneyDataBuilder {
        journeyData[NonStepJourneyDataKey.LookedUpAddresses.key] = "[]"
        return this
    }

    fun withLookedUpAddresses(customLookedUpAddresses: List<AddressDataModel>? = null): JourneyDataBuilder {
        val defaultLookedUpAddresses = listOf(AddressDataModel("1 Street Address, City, AB1 2CD"))
        journeyData[NonStepJourneyDataKey.LookedUpAddresses.key] = Json.encodeToString(customLookedUpAddresses ?: defaultLookedUpAddresses)
        return this
    }

    fun withManualAddressSelected(isContactAddress: Boolean = false): JourneyDataBuilder {
        val selectAddressKey = if (isContactAddress) "select-contact-address" else "select-address"
        journeyData[selectAddressKey] = mapOf("address" to MANUAL_ADDRESS_CHOSEN)
        return this
    }

    fun withSelectedAddress(
        singleLineAddress: String = "1 Street Address, City, AB1 2CD",
        uprn: Long? = null,
        localCouncil: LocalCouncil? = createLocalCouncil(),
        isContactAddress: Boolean = false,
    ): JourneyDataBuilder {
        localCouncil?.let {
            whenever(mockLocalCouncilService.retrieveLocalCouncilById(localCouncil.id)).thenReturn(localCouncil)
        }

        if (!isContactAddress) {
            withEnglandOrWalesResidence()
        }

        journeyData[NonStepJourneyDataKey.LookedUpAddresses.key] =
            Json.encodeToString(listOf(AddressDataModel(singleLineAddress, localCouncilId = localCouncil?.id, uprn = uprn)))

        val selectAddressKey = if (isContactAddress) "select-contact-address" else "select-address"
        journeyData[selectAddressKey] = mapOf("address" to singleLineAddress)
        return this
    }

    fun withManualAddress(
        addressLineOne: String = "1 Street Address",
        townOrCity: String = "City",
        postcode: String = "AB1 2CD",
        localCouncil: LocalCouncil? = null,
        isContactAddress: Boolean = false,
    ): JourneyDataBuilder {
        val selectAddressKey = if (isContactAddress) "select-contact-address" else "select-address"
        journeyData[selectAddressKey] = mapOf("address" to MANUAL_ADDRESS_CHOSEN)

        val manualAddressKey = if (isContactAddress) "manual-contact-address" else "manual-address"
        journeyData[manualAddressKey] =
            mapOf(
                "addressLineOne" to addressLineOne,
                "townOrCity" to townOrCity,
                "postcode" to postcode,
            )

        if (!isContactAddress) {
            withEnglandOrWalesResidence()
        }

        if (localCouncil != null) {
            whenever(mockLocalCouncilService.retrieveLocalCouncilById(localCouncil.id)).thenReturn(localCouncil)
        }

        journeyData[LocalCouncilStep.ROUTE_SEGMENT] =
            mapOf("localCouncilId" to localCouncil?.id)

        return this
    }

    fun withEnglandOrWalesResidence(): JourneyDataBuilder {
        journeyData["country-of-residence"] =
            mapOf(
                "livesInEnglandOrWales" to true,
            )
        return this
    }

    fun withPropertyType(
        type: PropertyType = PropertyType.DETACHED_HOUSE,
        customType: String = "type",
    ): JourneyDataBuilder {
        journeyData["property-type"] =
            if (type == PropertyType.OTHER) {
                mapOf("propertyType" to type.name, "customPropertyType" to customType)
            } else {
                mapOf("propertyType" to type.name)
            }
        return this
    }

    fun withOwnershipType(ownershipType: OwnershipType = OwnershipType.FREEHOLD): JourneyDataBuilder {
        journeyData["ownership-type"] = mapOf("ownershipType" to ownershipType.name)
        return this
    }

    fun withLicensingType(licensingType: LicensingType): JourneyDataBuilder {
        journeyData["licensing-type"] = mapOf("licensingType" to licensingType.name)
        return this
    }

    fun withLicensing(
        licensingType: LicensingType,
        licenseNumber: String? = null,
    ): JourneyDataBuilder {
        withLicensingType(licensingType)
        when (licensingType) {
            LicensingType.SELECTIVE_LICENCE -> {
                withLicenceNumber("selective-licence", licenseNumber)
            }

            LicensingType.HMO_MANDATORY_LICENCE -> {
                withLicenceNumber("hmo-mandatory-licence", licenseNumber)
            }

            LicensingType.HMO_ADDITIONAL_LICENCE -> {
                withLicenceNumber("hmo-additional-licence", licenseNumber)
            }

            LicensingType.NO_LICENSING -> {}
        }
        return this
    }

    private fun withLicenceNumber(
        urlPathSegment: String,
        licenceNumber: String?,
    ): JourneyDataBuilder {
        journeyData[urlPathSegment] = mapOf("licenceNumber" to licenceNumber)
        return this
    }

    fun withNoTenants(): JourneyDataBuilder {
        journeyData.remove("number-of-households")
        journeyData.remove("number-of-people")
        return withOccupiedSetToFalse()
    }

    fun withOccupiedSetToFalse(): JourneyDataBuilder {
        journeyData["occupancy"] =
            mapOf(
                "occupied" to "false",
            )
        return this
    }

    fun withOccupancyStatus(occupied: Boolean): JourneyDataBuilder {
        journeyData[OccupiedStep.ROUTE_SEGMENT] = mapOf(OccupancyFormModel::occupied.name to occupied)
        return this
    }

    fun withHouseholds(households: Int = 2): JourneyDataBuilder {
        journeyData[HouseholdStep.ROUTE_SEGMENT] =
            mapOf(NumberOfHouseholdsFormModel::numberOfHouseholds.name to households.toString())
        return this
    }

    fun withTenants(
        households: Int = 2,
        people: Int = 4,
    ): JourneyDataBuilder {
        withOccupancyStatus(true)
        withHouseholds(households)
        journeyData[TenantsStep.ROUTE_SEGMENT] =
            mapOf(NumberOfPeopleFormModel::numberOfPeople.name to people.toString())

        return this
    }

    fun withGasSafetyCertStatus(hasGasSafetyCert: Boolean): JourneyDataBuilder {
        journeyData[GasSafetyStep.ROUTE_SEGMENT] =
            mapOf(GasSafetyFormModel::hasCert.name to hasGasSafetyCert)
        return this
    }

    fun withGasSafetyIssueDate(issueDate: LocalDate = LocalDate.now()): JourneyDataBuilder {
        journeyData[GasSafetyIssueDateStep.ROUTE_SEGMENT] =
            mapOf(
                TodayOrPastDateFormModel::day.name to issueDate.dayOfMonth,
                TodayOrPastDateFormModel::month.name to issueDate.monthValue,
                TodayOrPastDateFormModel::year.name to issueDate.year,
            )
        return this
    }

    fun withGasSafetyCertUploadConfirmation(): JourneyDataBuilder {
        journeyData[GasSafetyUploadConfirmationStep.ROUTE_SEGMENT] = emptyMap<String, Any>()
        return this
    }

    fun withGasSafetyOutdatedConfirmation(): JourneyDataBuilder {
        journeyData[GasSafetyOutdatedStep.ROUTE_SEGMENT] = emptyMap<String, Any>()
        return this
    }

    fun withGasSafetyCertExemptionStatus(hasGasSafetyCertExemption: Boolean): JourneyDataBuilder {
        journeyData[GasSafetyExemptionStep.ROUTE_SEGMENT] =
            mapOf(GasSafetyExemptionFormModel::hasExemption.name to hasGasSafetyCertExemption)
        return this
    }

    fun withMissingGasSafetyExemption(): JourneyDataBuilder {
        withGasSafetyCertStatus(false)
        withGasSafetyCertExemptionStatus(false)
        journeyData[GasSafetyExemptionMissingStep.ROUTE_SEGMENT] = emptyMap<String, Any?>()
        return this
    }

    fun withEicrStatus(hasEICR: Boolean): JourneyDataBuilder {
        journeyData[EicrStep.ROUTE_SEGMENT] = mapOf(EicrFormModel::hasCert.name to hasEICR)
        return this
    }

    fun withEicrIssueDate(issueDate: LocalDate = LocalDate.now()): JourneyDataBuilder {
        journeyData[EicrIssueDateStep.ROUTE_SEGMENT] =
            mapOf(
                TodayOrPastDateFormModel::day.name to issueDate.dayOfMonth,
                TodayOrPastDateFormModel::month.name to issueDate.monthValue,
                TodayOrPastDateFormModel::year.name to issueDate.year,
            )
        return this
    }

    fun withEicrUploadConfirmation(): JourneyDataBuilder {
        journeyData[EicrUploadConfirmationStep.ROUTE_SEGMENT] = emptyMap<String, Any>()
        return this
    }

    fun withEicrExemptionStatus(hasEicrExemption: Boolean): JourneyDataBuilder {
        journeyData[EicrExemptionStep.ROUTE_SEGMENT] =
            mapOf(EicrExemptionFormModel::hasExemption.name to hasEicrExemption)
        return this
    }

    fun withMissingEicrExemption(): JourneyDataBuilder {
        withEicrStatus(false)
        withEicrExemptionStatus(false)
        journeyData[EicrExemptionMissingStep.ROUTE_SEGMENT] = emptyMap<String, Any?>()
        return this
    }

    fun withAutoMatchedEpcDetails(epcDetails: EpcDataModel?): JourneyDataBuilder {
        journeyData[NonStepJourneyDataKey.AutoMatchedEpc.key] = Json.encodeToString(epcDetails)
        return this
    }

    fun withCheckAutoMatchedEpcResult(
        matchedEpcIsCorrect: Boolean,
        meesOnlyUpdate: Boolean = false,
    ): JourneyDataBuilder {
        val stepUrlPathSegment =
            if (meesOnlyUpdate) {
                "update-mees-check-auto-matched-epc"
            } else {
                CheckMatchedEpcStep.AUTOMATCHED_ROUTE_SEGMENT
            }

        journeyData[stepUrlPathSegment] =
            mapOf(CheckMatchedEpcFormModel::matchedEpcIsCorrect.name to matchedEpcIsCorrect)
        return this
    }

    fun withCheckMatchedEpcResult(
        matchedEpcIsCorrect: Boolean,
        meesOnlyUpdate: Boolean = false,
    ): JourneyDataBuilder {
        val stepUrlPathSegment =
            if (meesOnlyUpdate) {
                "update-mees-check-matched-epc"
            } else {
                CheckMatchedEpcStep.ROUTE_SEGMENT
            }

        journeyData[stepUrlPathSegment] =
            mapOf(CheckMatchedEpcFormModel::matchedEpcIsCorrect.name to matchedEpcIsCorrect)
        return this
    }

    fun withLookedUpEpcDetails(epcDetails: EpcDataModel): JourneyDataBuilder {
        journeyData[NonStepJourneyDataKey.LookedUpEpc.key] = Json.encodeToString(epcDetails)
        return this
    }

    fun withEpcExemptionConfirmationStep(meesOnlyUpdate: Boolean = false): JourneyDataBuilder {
        val stepUrlPathSegment =
            if (meesOnlyUpdate) {
                "update-mees-epc-exemption-confirmation"
            } else {
                EpcExemptionConfirmationStep.ROUTE_SEGMENT
            }

        journeyData[stepUrlPathSegment] = emptyMap<String, Any?>()
        return this
    }

    fun withEpcExpiryCheckStep(
        tenancyStartedBeforeExpiry: Boolean,
        meesOnlyUpdate: Boolean = false,
    ): JourneyDataBuilder {
        val stepUrlPathSegment =
            if (meesOnlyUpdate) {
                "update-mees-epc-expiry-check"
            } else {
                EpcExpiryCheckStep.ROUTE_SEGMENT
            }

        journeyData[stepUrlPathSegment] =
            mapOf(EpcExpiryCheckFormModel::tenancyStartedBeforeExpiry.name to tenancyStartedBeforeExpiry)
        return this
    }

    fun withEpcExpiredStep(meesOnlyUpdate: Boolean = false): JourneyDataBuilder {
        val stepUrlPathSegment =
            if (meesOnlyUpdate) {
                "update-mees-epc-expired"
            } else {
                EpcExpiredStep.ROUTE_SEGMENT
            }

        journeyData[stepUrlPathSegment] = emptyMap<String, Any?>()
        return this
    }

    fun withEpcNotFoundStep(meesOnlyUpdate: Boolean = false): JourneyDataBuilder {
        val stepUrlPathSegment =
            if (meesOnlyUpdate) {
                "update-mees-epc-not-found"
            } else {
                EpcNotFoundStep.ROUTE_SEGMENT
            }

        journeyData[stepUrlPathSegment] = emptyMap<String, Any?>()
        return this
    }

    fun withLowEnergyRatingStep(meesOnlyUpdate: Boolean = false): JourneyDataBuilder {
        val stepUrlPathSegment =
            if (meesOnlyUpdate) {
                "update-mees-low-energy-rating"
            } else {
                LowEnergyRatingStep.ROUTE_SEGMENT
            }

        journeyData[stepUrlPathSegment] = emptyMap<String, Any?>()
        return this
    }

    fun withMeesExemptionConfirmationStep(meesOnlyUpdate: Boolean = false): JourneyDataBuilder {
        val stepUrlPathSegment =
            if (meesOnlyUpdate) {
                "update-mees-mees-exemption-confirmation"
            } else {
                MeesExemptionConfirmationStep.ROUTE_SEGMENT
            }

        journeyData[stepUrlPathSegment] = emptyMap<String, Any?>()
        return this
    }
}
