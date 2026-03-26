package uk.gov.communities.prsdb.webapp.testHelpers.builders

import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.CheckEpcAnswersStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.CheckMatchedEpcMode
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.CheckMatchedEpcStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.HasEpcStep
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.FormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.HasEpcFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.NoInputFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.TemporaryCheckMatchedEpcFormModel

interface EpcStateBuilder<SelfType : EpcStateBuilder<SelfType>> {
    fun withSubmittedValue(
        key: String,
        value: FormModel,
    ): SelfType

    fun self(): SelfType

    fun withEpcNotFoundByUprn(): SelfType {
        // TODO PDJB-662 - how to do this for an internal step?
        return self()
    }

    fun withPropertyHasEpc(): SelfType {
        withSubmittedValue(
            HasEpcStep.ROUTE_SEGMENT,
            HasEpcFormModel().apply { hasCert = true },
        )
        return self()
    }

    // TODO PDJB-656: Update to use actual logic
    fun withNoEpc(): SelfType {
        withSubmittedValue(
            CheckMatchedEpcStep.MATCHED_ROUTE_SEGMENT,
            TemporaryCheckMatchedEpcFormModel().apply { checkMatchedEpcMode = CheckMatchedEpcMode.EPC_COMPLIANT.name },
        )
        withSubmittedValue(CheckEpcAnswersStep.ROUTE_SEGMENT, NoInputFormModel())
        return self()
    }
}
