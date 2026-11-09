package uk.gov.communities.prsdb.webapp.testHelpers.builders

import kotlinx.serialization.json.Json
import kotlinx.serialization.serializer
import uk.gov.communities.prsdb.webapp.constants.MANUAL_ADDRESS_CHOSEN
import uk.gov.communities.prsdb.webapp.constants.enums.FreeholderContactType
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.AnyoneInOwnershipChainStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.FreeholdType
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.FreeholdTypeStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.FreeholderContactTypeStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.FreeholderNameStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.FreeholdersInfoStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.FreeholdersListStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.OwnershipKind
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.OwnershipKindStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.OwnsPropertyStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.PropertyManagerStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.ProvideFreeholderDetailsStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.ProvideSuperiorLandlordDetailsStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.SuperiorLandlordNameStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.tasks.FreeholderAddressTask
import uk.gov.communities.prsdb.webapp.journeys.shared.stepConfig.LookupAddressStep
import uk.gov.communities.prsdb.webapp.journeys.shared.stepConfig.SelectAddressStep
import uk.gov.communities.prsdb.webapp.models.dataModels.AddressDataModel
import uk.gov.communities.prsdb.webapp.models.dataModels.FreeholderDataModel
import uk.gov.communities.prsdb.webapp.models.dataModels.SuperiorLandlordDataModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.AnyoneInOwnershipChainFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.FormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.FreeholdTypeFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.FreeholderContactTypeFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.FreeholderNameFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.FreeholderProvideDetailsFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.LookupAddressFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.NoInputFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.OwnershipKindFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.OwnsPropertyFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.PropertyManagerFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.SelectAddressFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.SuperiorLandlordNameFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.SuperiorLandlordProvideDetailsFormModel

interface AssociatedPartiesStateBuilder<SelfType : AssociatedPartiesStateBuilder<SelfType>> {
    val additionalDataMap: MutableMap<String, String>

    fun withSubmittedValue(
        key: String,
        value: FormModel,
    ): SelfType

    fun self(): SelfType

    fun withOwnsProperty(ownsProperty: Boolean = true): SelfType {
        val ownsPropertyFormModel =
            OwnsPropertyFormModel().apply {
                this.ownsProperty = ownsProperty
            }
        withSubmittedValue(OwnsPropertyStep.ROUTE_SEGMENT, ownsPropertyFormModel)
        return self()
    }

    fun withOwnershipKind(ownershipKind: OwnershipKind = OwnershipKind.FREEHOLD_ONLY): SelfType {
        val ownershipKindFormModel =
            OwnershipKindFormModel().apply {
                this.ownershipKind = ownershipKind
            }
        withSubmittedValue(OwnershipKindStep.ROUTE_SEGMENT, ownershipKindFormModel)
        return self()
    }

    fun withFreeholdType(freeholdType: FreeholdType = FreeholdType.SOLE_OWNERSHIP): SelfType {
        val freeholdTypeFormModel =
            FreeholdTypeFormModel().apply {
                this.freeholdType = freeholdType
            }
        withSubmittedValue(FreeholdTypeStep.ROUTE_SEGMENT, freeholdTypeFormModel)
        return self()
    }

    fun withFreeholders(freeholders: Map<Int, FreeholderDataModel>): SelfType {
        additionalDataMap["freeholdersMap"] = Json.encodeToString(serializer(), freeholders)
        additionalDataMap["nextFreeholderId"] = Json.encodeToString(serializer(), (freeholders.keys.maxOrNull() ?: 0) + 1)
        return self()
    }

    fun withOwnershipStructure(): SelfType {
        withOwnsProperty()
        withOwnershipKind()
        withFreeholdType()
        return self()
    }

    fun withNoPropertyManager(): SelfType {
        val propertyManagerFormModel =
            PropertyManagerFormModel().apply {
                usingPropertyManager = false
            }
        withSubmittedValue(PropertyManagerStep.ROUTE_SEGMENT, propertyManagerFormModel)
        return self()
    }

    fun withFreeholderName(name: String = "Test Freeholder"): SelfType {
        val freeholderNameFormModel = FreeholderNameFormModel().apply { this.name = name }
        withSubmittedValue(FreeholderNameStep.ROUTE_SEGMENT, freeholderNameFormModel)
        return self()
    }

    fun withFreeholdersInfoComplete(): SelfType {
        withSubmittedValue(FreeholdersInfoStep.ROUTE_SEGMENT, NoInputFormModel())
        return self()
    }

    fun withFreeholdersListComplete(): SelfType {
        withSubmittedValue(FreeholdersListStep.ROUTE_SEGMENT, NoInputFormModel())
        return self()
    }

    fun withFreeholderProvideDetails(provideDetails: Boolean = true): SelfType {
        val freeholderProvideDetailsFormModel = FreeholderProvideDetailsFormModel(provideDetails = provideDetails)
        withSubmittedValue(ProvideFreeholderDetailsStep.ROUTE_SEGMENT, freeholderProvideDetailsFormModel)
        return self()
    }

    fun withFreeholderContactType(contactType: FreeholderContactType = FreeholderContactType.EMAIL): SelfType {
        val freeholderContactTypeFormModel = FreeholderContactTypeFormModel(contactType = contactType)
        withSubmittedValue(FreeholderContactTypeStep.ROUTE_SEGMENT, freeholderContactTypeFormModel)
        return self()
    }

    fun withFreeholderLookupAddress(
        houseNameOrNumber: String = "1",
        postcode: String = "FA1 1AA",
    ): SelfType {
        // The freeholder address is a routed instance of the shared address task, so its data is stored
        // under keys prefixed with the task route.
        val routePrefix = FreeholderAddressTask.ROUTE_SEGMENT
        val address = AddressDataModel("$houseNameOrNumber Fictional Road, $postcode", localCouncilId = 22, uprn = 44)
        additionalDataMap["$routePrefix/cachedAddresses"] = Json.encodeToString(serializer(), listOf(address))
        withSubmittedValue(
            "$routePrefix/${LookupAddressStep.ROUTE_SEGMENT}",
            LookupAddressFormModel().apply {
                this.houseNameOrNumber = houseNameOrNumber
                this.postcode = postcode
            },
        )
        return self()
    }

    fun withFreeholderManualAddressSelected(): SelfType {
        withSubmittedValue(
            "${FreeholderAddressTask.ROUTE_SEGMENT}/${SelectAddressStep.ROUTE_SEGMENT}",
            SelectAddressFormModel().apply { address = MANUAL_ADDRESS_CHOSEN },
        )
        return self()
    }

    fun withAnyoneInOwnershipChain(anyoneInOwnershipChain: Boolean = true): SelfType {
        val anyoneInOwnershipChainFormModel = AnyoneInOwnershipChainFormModel(anyoneInOwnershipChain = anyoneInOwnershipChain)
        withSubmittedValue(AnyoneInOwnershipChainStep.ROUTE_SEGMENT, anyoneInOwnershipChainFormModel)
        return self()
    }

    fun withSuperiorLandlordProvideDetails(provideDetails: Boolean = true): SelfType {
        val superiorLandlordProvideDetailsFormModel = SuperiorLandlordProvideDetailsFormModel(provideDetails = provideDetails)
        withSubmittedValue(ProvideSuperiorLandlordDetailsStep.ROUTE_SEGMENT, superiorLandlordProvideDetailsFormModel)
        return self()
    }

    fun withSuperiorLandlordNameComplete(name: String = "Test Superior Landlord"): SelfType {
        val superiorLandlordNameFormModel = SuperiorLandlordNameFormModel(name = name)
        withSubmittedValue(SuperiorLandlordNameStep.ROUTE_SEGMENT, superiorLandlordNameFormModel)
        return self()
    }

    fun withSuperiorLandlords(superiorLandlords: Map<Int, SuperiorLandlordDataModel>): SelfType {
        additionalDataMap["superiorLandlordsMap"] = Json.encodeToString(serializer(), superiorLandlords)
        additionalDataMap["nextSuperiorLandlordId"] = Json.encodeToString(serializer(), (superiorLandlords.keys.maxOrNull() ?: 0) + 1)
        return self()
    }
}
