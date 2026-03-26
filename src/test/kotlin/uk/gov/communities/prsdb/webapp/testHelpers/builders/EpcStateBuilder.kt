package uk.gov.communities.prsdb.webapp.testHelpers.builders

import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.CheckEpcAnswersStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.CheckMatchedEpcMode
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.CheckMatchedEpcStep
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.FormModel
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
}
