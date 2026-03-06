package uk.gov.communities.prsdb.webapp.testHelpers.builders

import kotlinx.serialization.json.Json
import kotlinx.serialization.serializer
import org.mockito.kotlin.whenever
import uk.gov.communities.prsdb.webapp.constants.MANUAL_ADDRESS_CHOSEN
import uk.gov.communities.prsdb.webapp.database.entity.LocalCouncil
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig.CountryOfResidenceStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.LocalCouncilStep
import uk.gov.communities.prsdb.webapp.models.dataModels.AddressDataModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.CountryOfResidenceFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.FormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.LookupAddressFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.ManualAddressFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.SelectFromListFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.SelectLocalCouncilFormModel
import uk.gov.communities.prsdb.webapp.services.LocalCouncilService
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.MockLocalCouncilData.Companion.createLocalCouncil

interface AddressStateBuilder<out SelfType : AddressStateBuilder<SelfType>> {
    val additionalDataMap: MutableMap<String, String>
    val mockLocalCouncilService: LocalCouncilService

    fun self(): SelfType

    fun withSubmittedValue(
        key: String,
        value: FormModel,
    ): SelfType

    fun withEnglandOrWalesResidence(): SelfType {
        val countryOfResidenceFormModel =
            CountryOfResidenceFormModel().apply {
                livesInEnglandOrWales = true
            }
        withSubmittedValue(CountryOfResidenceStep.ROUTE_SEGMENT, countryOfResidenceFormModel)
        return self()
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
        return self()
    }

    fun withManualAddressSelected(isContactAddress: Boolean = false): SelfType {
        withCachedAddresses(listOf(AddressDataModel("singleLineAddress", localCouncilId = 22, uprn = 44)))
        val selectAddressKey = if (isContactAddress) "select-contact-address" else "select-address"
        val selectAddressFormModel =
            SelectFromListFormModel().apply {
                selectedOption = MANUAL_ADDRESS_CHOSEN
            }
        withSubmittedValue(selectAddressKey, selectAddressFormModel)

        return self()
    }

    fun withCachedAddresses(addresses: List<AddressDataModel>): SelfType {
        additionalDataMap["cachedAddresses"] = Json.encodeToString(serializer(), addresses)

        return self()
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
            SelectFromListFormModel().apply {
                selectedOption = singleLineAddress
            }
        withSubmittedValue(selectAddressKey, selectAddressFormModel)

        return self()
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
            withSubmittedValue(LocalCouncilStep.ROUTE_SEGMENT, selectLocalCouncilFormModel)
        }

        return self()
    }
}
