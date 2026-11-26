package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.journeys.JourneyStep.RequestableStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.PropertyRegistrationJourneyState
import uk.gov.communities.prsdb.webapp.journeys.shared.stepConfig.AbstractConfirmMissingComplianceStepConfig
import uk.gov.communities.prsdb.webapp.journeys.shared.stepConfig.ConfirmMissingComplianceMode
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.PropertyRegistrationConfirmMissingComplianceFormModel

@JourneyFrameworkComponent
class ConfirmMissingComplianceStepConfig :
    AbstractConfirmMissingComplianceStepConfig<PropertyRegistrationConfirmMissingComplianceFormModel>() {
    override val formModelClass = PropertyRegistrationConfirmMissingComplianceFormModel::class
}

@JourneyFrameworkComponent
final class ConfirmMissingComplianceStep(
    stepConfig: ConfirmMissingComplianceStepConfig,
) : RequestableStep<ConfirmMissingComplianceMode, PropertyRegistrationConfirmMissingComplianceFormModel, PropertyRegistrationJourneyState>(
        stepConfig,
    ) {
    companion object {
        const val ROUTE_SEGMENT = "confirm-missing-compliance"
    }
}
