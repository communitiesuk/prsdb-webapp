package uk.gov.communities.prsdb.webapp.testHelpers.builders

import uk.gov.communities.prsdb.webapp.constants.enums.MeesExemptionReason
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.CheckEpcAnswersStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.CheckMatchedEpcMode
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.CheckMatchedEpcStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.HasMeesExemptionStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.MeesExemptionStep
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.FormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.MeesExemptionCheckFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.MeesExemptionReasonFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.NoInputFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.TemporaryCheckMatchedEpcFormModel

interface EpcStateBuilder<SelfType : EpcStateBuilder<SelfType>> {
    fun withSubmittedValue(
        key: String,
        value: FormModel,
    ): SelfType

    fun self(): SelfType

    // TODO PDJB-656: Update to use actual logic
    fun withNoEpc(): SelfType {
        withSubmittedValue(
            CheckMatchedEpcStep.MATCHED_ROUTE_SEGMENT,
            TemporaryCheckMatchedEpcFormModel().apply { checkMatchedEpcMode = CheckMatchedEpcMode.EPC_COMPLIANT.name },
        )
        withSubmittedValue(CheckEpcAnswersStep.ROUTE_SEGMENT, NoInputFormModel())
        return self()
    }

    fun withEpcLowEnergyRating(): SelfType {
        withSubmittedValue(
            CheckMatchedEpcStep.MATCHED_ROUTE_SEGMENT,
            TemporaryCheckMatchedEpcFormModel().apply { checkMatchedEpcMode = CheckMatchedEpcMode.EPC_LOW_ENERGY_RATING.name },
        )
        return self()
    }

    fun withHasMeesExemption(hasExemption: Boolean): SelfType {
        val formModel = MeesExemptionCheckFormModel().apply { propertyHasExemption = hasExemption }
        withSubmittedValue(HasMeesExemptionStep.ROUTE_SEGMENT, formModel)
        return self()
    }

    fun withMeesExemptionReason(exemptionReason: MeesExemptionReason): SelfType {
        val formModel = MeesExemptionReasonFormModel().apply { this.exemptionReason = exemptionReason }
        withSubmittedValue(MeesExemptionStep.ROUTE_SEGMENT, formModel)
        return self()
    }
}
