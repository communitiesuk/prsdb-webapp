package uk.gov.communities.prsdb.webapp.testHelpers.builders

import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.FreeholdType
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.FreeholdTypeStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.OwnershipKind
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.OwnershipKindStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.OwnsPropertyStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.PropertyManagerStep
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.FormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.FreeholdTypeFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.NoInputFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.OwnershipKindFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.OwnsPropertyFormModel

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

    fun withPropertyManager(): SelfType {
        withSubmittedValue(PropertyManagerStep.ROUTE_SEGMENT, NoInputFormModel())
        return self()
    }

    fun withOwnershipStructureAndPropertyManager(): SelfType {
        withOwnsProperty()
        withOwnershipKind()
        withFreeholdType()
        withPropertyManager()
        return self()
    }
}
