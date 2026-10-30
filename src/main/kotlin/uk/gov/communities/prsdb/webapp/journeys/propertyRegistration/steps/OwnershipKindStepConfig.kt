package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.journeys.AbstractRequestableStepConfig
import uk.gov.communities.prsdb.webapp.journeys.JourneyState
import uk.gov.communities.prsdb.webapp.journeys.JourneyStep.RequestableStep
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.OwnershipKindFormModel
import uk.gov.communities.prsdb.webapp.models.viewModels.formModels.RadiosButtonViewModel

// TODO PDJB-1960: replace this placeholder with the real "What kind of ownership do you have?" question
//  (content, validation and persistence). Rendered with the shared forms/todoWithRadios placeholder page.
//  The real question uses checkboxes - these radios stand in for the 3 valid checkbox combinations.
@JourneyFrameworkComponent
class OwnershipKindStepConfig : AbstractRequestableStepConfig<OwnershipKind, OwnershipKindFormModel, JourneyState>() {
    override val formModelClass = OwnershipKindFormModel::class

    override fun getStepSpecificContent(state: JourneyState): Map<String, Any?> =
        mapOf(
            "fieldName" to "ownershipKind",
            "fieldSetHeading" to "registerProperty.ownershipKind.fieldSetHeading",
            "radioOptions" to OwnershipKind.entries.map { RadiosButtonViewModel(value = it) },
        )

    override fun chooseTemplate(state: JourneyState) = "forms/todoWithRadios"

    override fun mode(state: JourneyState) = getFormModelFromStateOrNull(state)?.ownershipKind
}

@JourneyFrameworkComponent
final class OwnershipKindStep(
    stepConfig: OwnershipKindStepConfig,
) : RequestableStep<OwnershipKind, OwnershipKindFormModel, JourneyState>(stepConfig) {
    companion object {
        const val ROUTE_SEGMENT = "ownership-kind"
    }
}

enum class OwnershipKind {
    FREEHOLD_ONLY,
    LEASEHOLD_ONLY,
    FREEHOLD_AND_LEASEHOLD,
}
