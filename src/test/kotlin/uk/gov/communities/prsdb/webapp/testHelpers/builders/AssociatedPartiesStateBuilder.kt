package uk.gov.communities.prsdb.webapp.testHelpers.builders

import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.FreeholdType
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.FreeholdTypeStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.FreeholderNameStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.FreeholdersInfoStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.HasAnyFreeholdersStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.OwnershipKind
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.OwnershipKindStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.OwnsPropertyStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.PropertyManagerStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.ProvideFreeholderDetailsStep
import uk.gov.communities.prsdb.webapp.journeys.shared.YesOrNo
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.FormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.FreeholdTypeFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.FreeholderNameFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.FreeholderProvideDetailsFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.NoInputFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.OwnershipKindFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.OwnsPropertyFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.PropertyManagerFormModel

interface AssociatedPartiesStateBuilder<SelfType : AssociatedPartiesStateBuilder<SelfType>> {
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

    // TODO PDJB-1969: remove this when has any freeholders becomes an internal step
    fun withHasAnyFreeholdersComplete(): SelfType {
        withSubmittedValue(HasAnyFreeholdersStep.ROUTE_SEGMENT, NoInputFormModel())
        return self()
    }

    fun withFreeholderProvideDetails(provideDetails: YesOrNo = YesOrNo.YES): SelfType {
        val freeholderProvideDetailsFormModel = FreeholderProvideDetailsFormModel(provideDetails = provideDetails)
        withSubmittedValue(ProvideFreeholderDetailsStep.ROUTE_SEGMENT, freeholderProvideDetailsFormModel)
        return self()
    }
}
