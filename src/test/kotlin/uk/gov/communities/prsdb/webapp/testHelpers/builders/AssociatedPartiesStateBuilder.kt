package uk.gov.communities.prsdb.webapp.testHelpers.builders

import kotlinx.serialization.json.Json
import kotlinx.serialization.serializer
import uk.gov.communities.prsdb.webapp.constants.enums.FreeholderContactType
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.AnyoneInOwnershipChainStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.FreeholdType
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.FreeholdTypeStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.FreeholderContactTypeStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.FreeholderNameStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.FreeholdersInfoStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.FreeholdersListStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.HasAnySuperiorLandlordsStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.OwnershipKind
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.OwnershipKindStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.OwnsPropertyStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.PropertyManagerStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.ProvideFreeholderDetailsStep
import uk.gov.communities.prsdb.webapp.journeys.shared.YesOrNo
import uk.gov.communities.prsdb.webapp.models.dataModels.FreeholderDataModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.AnyoneInOwnershipChainFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.FormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.FreeholdTypeFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.FreeholderContactTypeFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.FreeholderNameFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.FreeholderProvideDetailsFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.NoInputFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.OwnershipKindFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.OwnsPropertyFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.PropertyManagerFormModel

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

    // TODO PDJB-1962: update this when the real freeholders intro page is added
    fun withFreeholdersInfoComplete(): SelfType {
        withSubmittedValue(FreeholdersInfoStep.ROUTE_SEGMENT, NoInputFormModel())
        return self()
    }

    fun withFreeholdersListComplete(): SelfType {
        withSubmittedValue(FreeholdersListStep.ROUTE_SEGMENT, NoInputFormModel())
        return self()
    }

    fun withFreeholderProvideDetails(provideDetails: YesOrNo = YesOrNo.YES): SelfType {
        val freeholderProvideDetailsFormModel = FreeholderProvideDetailsFormModel(provideDetails = provideDetails)
        withSubmittedValue(ProvideFreeholderDetailsStep.ROUTE_SEGMENT, freeholderProvideDetailsFormModel)
        return self()
    }

    fun withFreeholderContactType(contactType: FreeholderContactType = FreeholderContactType.EMAIL): SelfType {
        val freeholderContactTypeFormModel = FreeholderContactTypeFormModel(contactType = contactType)
        withSubmittedValue(FreeholderContactTypeStep.ROUTE_SEGMENT, freeholderContactTypeFormModel)
        return self()
    }

    fun withAnyoneInOwnershipChain(anyoneInOwnershipChain: Boolean = true): SelfType {
        val anyoneInOwnershipChainFormModel = AnyoneInOwnershipChainFormModel(anyoneInOwnershipChain = anyoneInOwnershipChain)
        withSubmittedValue(AnyoneInOwnershipChainStep.ROUTE_SEGMENT, anyoneInOwnershipChainFormModel)
        return self()
    }

    // TODO PDJB-1978: remove this when has any superior landlords becomes an internal step
    fun withHasAnySuperiorLandlordsComplete(): SelfType {
        withSubmittedValue(HasAnySuperiorLandlordsStep.ROUTE_SEGMENT, NoInputFormModel())
        return self()
    }
}
