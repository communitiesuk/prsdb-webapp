package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.journeys.AbstractRequestableStepConfig
import uk.gov.communities.prsdb.webapp.journeys.JourneyState
import uk.gov.communities.prsdb.webapp.journeys.JourneyStep.RequestableStep
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.OwnershipKindFormModel
import uk.gov.communities.prsdb.webapp.models.viewModels.formModels.CheckboxButtonViewModel

@JourneyFrameworkComponent
class OwnershipKindStepConfig : AbstractRequestableStepConfig<OwnershipKind, OwnershipKindFormModel, JourneyState>() {
    override val formModelClass = OwnershipKindFormModel::class

    override fun getStepSpecificContent(state: JourneyState): Map<String, Any?> =
        mapOf(
            "fieldSetHeading" to "registerProperty.ownershipKind.fieldSetHeading",
            "fieldSetHint" to "registerProperty.ownershipKind.fieldSetHint",
            "checkboxOptions" to
                listOf(
                    CheckboxButtonViewModel(
                        value = OwnershipKindOption.FREEHOLD,
                        labelMsgKey = "registerProperty.ownershipKind.checkbox.freehold.label",
                        hintMsgKey = "registerProperty.ownershipKind.checkbox.freehold.hint",
                    ),
                    CheckboxButtonViewModel(
                        value = OwnershipKindOption.LEASEHOLD,
                        labelMsgKey = "registerProperty.ownershipKind.checkbox.leasehold.label",
                        hintMsgKey = "registerProperty.ownershipKind.checkbox.leasehold.hint",
                    ),
                ),
        )

    override fun chooseTemplate(state: JourneyState) = "forms/ownershipKindForm"

    override fun mode(state: JourneyState) = getFormModelFromStateOrNull(state)?.toOwnershipKind()
}

@JourneyFrameworkComponent
final class OwnershipKindStep(
    stepConfig: OwnershipKindStepConfig,
) : RequestableStep<OwnershipKind, OwnershipKindFormModel, JourneyState>(stepConfig) {
    companion object {
        const val ROUTE_SEGMENT = "ownership-kind"
    }
}

enum class OwnershipKindOption {
    FREEHOLD,
    LEASEHOLD,
}

enum class OwnershipKind(
    val options: Set<OwnershipKindOption>,
) {
    FREEHOLD_ONLY(setOf(OwnershipKindOption.FREEHOLD)),
    LEASEHOLD_ONLY(setOf(OwnershipKindOption.LEASEHOLD)),
    FREEHOLD_AND_LEASEHOLD(setOf(OwnershipKindOption.FREEHOLD, OwnershipKindOption.LEASEHOLD)),
}
