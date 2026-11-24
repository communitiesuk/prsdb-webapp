package uk.gov.communities.prsdb.webapp.journeys.propertyReactivation.steps

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.journeys.JourneyStep.RequestableStep
import uk.gov.communities.prsdb.webapp.journeys.propertyReactivation.PropertyReactivationJourneyState
import uk.gov.communities.prsdb.webapp.journeys.shared.stepConfig.AbstractConfirmMissingComplianceStepConfig
import uk.gov.communities.prsdb.webapp.journeys.shared.stepConfig.ConfirmMissingComplianceMode
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.ReactivationConfirmMissingComplianceFormModel

@JourneyFrameworkComponent
class ReactivationConfirmMissingComplianceStepConfig :
    AbstractConfirmMissingComplianceStepConfig<ReactivationConfirmMissingComplianceFormModel>() {
    override val formModelClass = ReactivationConfirmMissingComplianceFormModel::class

    override val areYouSureHeadingKey = "reactivateProperty.confirmMissingCompliance.areYouSure.heading"

    override val submitButtonKey = "forms.buttons.confirmAndContinue"
}

@JourneyFrameworkComponent
final class ReactivationConfirmMissingComplianceStep(
    stepConfig: ReactivationConfirmMissingComplianceStepConfig,
) : RequestableStep<ConfirmMissingComplianceMode, ReactivationConfirmMissingComplianceFormModel, PropertyReactivationJourneyState>(
        stepConfig,
    ) {
    companion object {
        const val ROUTE_SEGMENT = "confirm-missing-compliance"
    }
}
