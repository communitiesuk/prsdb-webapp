package uk.gov.communities.prsdb.webapp.testHelpers.builders

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.serializer
import org.mockito.Mockito.mock
import org.mockito.kotlin.whenever
import uk.gov.communities.prsdb.webapp.constants.MANUAL_ADDRESS_CHOSEN
import uk.gov.communities.prsdb.webapp.constants.enums.LicensingType
import uk.gov.communities.prsdb.webapp.constants.enums.OwnershipType
import uk.gov.communities.prsdb.webapp.constants.enums.PropertyType
import uk.gov.communities.prsdb.webapp.database.entity.LocalCouncil
import uk.gov.communities.prsdb.webapp.forms.steps.LandlordRegistrationStepId
import uk.gov.communities.prsdb.webapp.forms.steps.RegisterPropertyStepId
import uk.gov.communities.prsdb.webapp.models.dataModels.AddressDataModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.CheckAnswersFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.CountryOfResidenceFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.FormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.HmoAdditionalLicenceFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.HmoMandatoryLicenceFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.LicensingTypeFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.LookupAddressFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.ManualAddressFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.NumberOfHouseholdsFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.NumberOfPeopleFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.OccupancyFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.OwnershipTypeFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.PropertyTypeFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.SelectAddressFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.SelectLocalCouncilFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.SelectiveLicenceFormModel
import uk.gov.communities.prsdb.webapp.services.LocalCouncilService
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.MockLocalCouncilData.Companion.createLocalCouncil

@Suppress("UNCHECKED_CAST")
open class JourneyStateSessionBuilder<SelfType : JourneyStateSessionBuilder<SelfType>>(
    private val mockLocalCouncilService: LocalCouncilService = mock(),
) {
    val additionalDataMap = mutableMapOf<String, String>()
    val submittedValueMap = mutableMapOf<String, FormModel>()

    fun withSubmittedValue(
        key: String,
        value: FormModel,
    ): SelfType {
        submittedValueMap[key] = value
        return this as SelfType
    }

    fun build(): Map<String, Any> {
        val sessionData = mutableMapOf<String, Any>()
        sessionData.putAll(additionalDataMap)
        sessionData["journeyData"] = submittedValueMap.mapValues { it.value.toPageData() }
        return sessionData
    }

    companion object {
        const val DEFAULT_ADDRESS = "4, Example Road, EG"
    }

    fun withLookupAddress(
        houseNameOrNumber: String = "4",
        postcode: String = "EG1 2AB",
        isContactAddress: Boolean = false,
        wasFound: Boolean = true,
    ): SelfType {
        val addressFormModel =
            LookupAddressFormModel().apply {
                this.houseNameOrNumber = houseNameOrNumber
                this.postcode = postcode
            }
        withSubmittedValue(
            if (isContactAddress) "lookup-contact-address" else "lookup-address",
            addressFormModel,
        )
        if (wasFound) {
            withCachedAddresses(
                listOf(AddressDataModel("$houseNameOrNumber Street Address, City, $postcode", localCouncilId = 22, uprn = 44)),
            )
        }
        return this as SelfType
    }

    fun withManualAddressSelected(isContactAddress: Boolean = false): SelfType {
        withCachedAddresses(listOf(AddressDataModel("singleLineAddress", localCouncilId = 22, uprn = 44)))
        val selectAddressKey = if (isContactAddress) "select-contact-address" else "select-address"
        val selectAddressFormModel =
            SelectAddressFormModel().apply {
                address = MANUAL_ADDRESS_CHOSEN
            }
        withSubmittedValue(selectAddressKey, selectAddressFormModel)
        return this as SelfType
    }

    fun withCachedAddresses(addresses: List<AddressDataModel>): SelfType {
        additionalDataMap["cachedAddresses"] = Json.encodeToString(serializer(), addresses)
        return this as SelfType
    }

    fun withSelectedAddress(
        singleLineAddress: String = "1 Street Address, City, AB1 2CD",
        uprn: Long? = null,
        localCouncil: LocalCouncil? = createLocalCouncil(),
        isContactAddress: Boolean = false,
    ): SelfType {
        localCouncil?.let {
            whenever(mockLocalCouncilService.retrieveLocalCouncilById(localCouncil.id)).thenReturn(localCouncil)
        }

        if (!isContactAddress) {
            withEnglandOrWalesResidence()
        }

        withCachedAddresses(listOf(AddressDataModel(singleLineAddress, localCouncilId = localCouncil?.id, uprn = uprn)))

        val selectAddressKey = if (isContactAddress) "select-contact-address" else "select-address"
        val selectAddressFormModel =
            SelectAddressFormModel().apply {
                address = singleLineAddress
            }
        withSubmittedValue(selectAddressKey, selectAddressFormModel)
        return this as SelfType
    }

    fun withManualAddress(
        addressLineOne: String = "1 Street Address",
        townOrCity: String = "City",
        postcode: String = "AB1 2CD",
        localCouncil: LocalCouncil? = null,
        isContactAddress: Boolean = false,
    ): SelfType {
        withManualAddressSelected(isContactAddress)
        val manualAddressKey = if (isContactAddress) "manual-contact-address" else "manual-address"
        val manualAddressFormModel =
            ManualAddressFormModel().apply {
                this.addressLineOne = addressLineOne
                this.townOrCity = townOrCity
                this.postcode = postcode
            }
        withSubmittedValue(manualAddressKey, manualAddressFormModel)

        if (!isContactAddress) {
            withEnglandOrWalesResidence()
        }

        if (localCouncil != null) {
            whenever(mockLocalCouncilService.retrieveLocalCouncilById(localCouncil.id)).thenReturn(localCouncil)
            val selectLocalCouncilFormModel =
                SelectLocalCouncilFormModel().apply {
                    localCouncilId = localCouncil.id
                }
            withSubmittedValue(RegisterPropertyStepId.LocalCouncil.urlPathSegment, selectLocalCouncilFormModel)
        }

        return this as SelfType
    }

    fun withEnglandOrWalesResidence(): SelfType {
        val countryOfResidenceFormModel =
            CountryOfResidenceFormModel().apply {
                livesInEnglandOrWales = true
            }
        withSubmittedValue(LandlordRegistrationStepId.CountryOfResidence.urlPathSegment, countryOfResidenceFormModel)
        return this as SelfType
    }

    fun withCheckedAnswers(): SelfType {
        val checkAnswersFormModel = CheckAnswersFormModel()
        withSubmittedValue("check-answers", checkAnswersFormModel)
        return this as SelfType
    }

    fun withPropertyType(
        type: PropertyType = PropertyType.DETACHED_HOUSE,
        customType: String = "type",
    ): SelfType {
        val propertyTypeFormModel =
            PropertyTypeFormModel().apply {
                propertyType = type
                if (type == PropertyType.OTHER) {
                    customPropertyType = customType
                }
            }
        withSubmittedValue("property-type", propertyTypeFormModel)
        return this as SelfType
    }

    fun withOwnershipType(ownershipType: OwnershipType = OwnershipType.FREEHOLD): SelfType {
        val ownershipTypeFormModel =
            OwnershipTypeFormModel().apply {
                this.ownershipType = ownershipType
            }
        withSubmittedValue("ownership-type", ownershipTypeFormModel)
        return this as SelfType
    }

    fun withLicensingType(licensingType: LicensingType): SelfType {
        val licensingTypeFormModel =
            LicensingTypeFormModel().apply {
                this.licensingType = licensingType
            }
        withSubmittedValue("licensing-type", licensingTypeFormModel)
        return this as SelfType
    }

    fun withLicensing(
        licensingType: LicensingType,
        licenseNumber: String? = null,
    ): SelfType {
        withLicensingType(licensingType)
        when (licensingType) {
            LicensingType.SELECTIVE_LICENCE -> withLicenceNumber("selective-licence", licenseNumber)
            LicensingType.HMO_MANDATORY_LICENCE -> withLicenceNumber("hmo-mandatory-licence", licenseNumber)
            LicensingType.HMO_ADDITIONAL_LICENCE -> withLicenceNumber("hmo-additional-licence", licenseNumber)
            LicensingType.NO_LICENSING -> {}
        }
        return this as SelfType
    }

    private fun withLicenceNumber(
        urlPathSegment: String,
        licenceNumber: String?,
    ): SelfType {
        val formModel =
            when (urlPathSegment) {
                "selective-licence" -> SelectiveLicenceFormModel().apply { this.licenceNumber = licenceNumber }
                "hmo-mandatory-licence" -> HmoMandatoryLicenceFormModel().apply { this.licenceNumber = licenceNumber }
                "hmo-additional-licence" -> HmoAdditionalLicenceFormModel().apply { this.licenceNumber = licenceNumber }
                else -> throw IllegalArgumentException("Unknown licence type: $urlPathSegment")
            }
        withSubmittedValue(urlPathSegment, formModel)
        return this as SelfType
    }

    fun withNoTenants(): SelfType {
        submittedValueMap.remove("number-of-households")
        submittedValueMap.remove("number-of-people")
        return withOccupiedSetToFalse()
    }

    fun withOccupiedSetToFalse(): SelfType {
        val occupancyFormModel =
            OccupancyFormModel().apply {
                occupied = false
            }
        withSubmittedValue("occupancy", occupancyFormModel)
        return this as SelfType
    }

    fun withOccupancyStatus(occupied: Boolean): SelfType {
        val occupancyFormModel =
            OccupancyFormModel().apply {
                this.occupied = occupied
            }
        withSubmittedValue(RegisterPropertyStepId.Occupancy.urlPathSegment, occupancyFormModel)
        return this as SelfType
    }

    fun withHouseholds(households: Int = 2): SelfType {
        val numberOfHouseholdsFormModel =
            NumberOfHouseholdsFormModel().apply {
                numberOfHouseholds = households.toString()
            }
        withSubmittedValue(RegisterPropertyStepId.NumberOfHouseholds.urlPathSegment, numberOfHouseholdsFormModel)
        return this as SelfType
    }

    fun withTenants(
        households: Int = 2,
        people: Int = 4,
    ): SelfType {
        withOccupancyStatus(true)
        withHouseholds(households)
        val numberOfPeopleFormModel =
            NumberOfPeopleFormModel().apply {
                numberOfPeople = people.toString()
            }
        withSubmittedValue(RegisterPropertyStepId.NumberOfPeople.urlPathSegment, numberOfPeopleFormModel)
        return this as SelfType
    }
}
