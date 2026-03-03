package uk.gov.communities.prsdb.webapp.testHelpers.builders

import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.CheckAutomatchedEpcStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.CheckEpcAnswersStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.CheckMatchedEpcStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.EpcExemptionStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.EpcExpiredStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.EpcExpiryCheckStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.EpcMissingStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.EpcNotFoundStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.EpcSearchStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.EpcSuperseededStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.HasEpcExemptionStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.HasEpcStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.HasMeesExemptionStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.LowEnergyRatingStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.MeesExemptionStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.ProvideEpcLaterStep
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.FormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.NoInputFormModel

interface EpcStateBuilder<SelfType : EpcStateBuilder<SelfType>> {
    fun withSubmittedValue(
        key: String,
        value: FormModel,
    ): SelfType

    fun self(): SelfType

    // TODO PDJB-80: Update to use actual logic
    fun withNoEpc(): SelfType {
        withSubmittedValue(HasEpcStep.ROUTE_SEGMENT, NoInputFormModel())
        withSubmittedValue(CheckAutomatchedEpcStep.ROUTE_SEGMENT, NoInputFormModel())
        withSubmittedValue(EpcSearchStep.ROUTE_SEGMENT, NoInputFormModel())
        withSubmittedValue(CheckMatchedEpcStep.ROUTE_SEGMENT, NoInputFormModel())
        withSubmittedValue(EpcSuperseededStep.ROUTE_SEGMENT, NoInputFormModel())
        withSubmittedValue(EpcNotFoundStep.ROUTE_SEGMENT, NoInputFormModel())
        withSubmittedValue(EpcExpiryCheckStep.ROUTE_SEGMENT, NoInputFormModel())
        withSubmittedValue(HasMeesExemptionStep.ROUTE_SEGMENT, NoInputFormModel())
        withSubmittedValue(MeesExemptionStep.ROUTE_SEGMENT, NoInputFormModel())
        withSubmittedValue(LowEnergyRatingStep.ROUTE_SEGMENT, NoInputFormModel())
        withSubmittedValue(EpcExpiredStep.ROUTE_SEGMENT, NoInputFormModel())
        withSubmittedValue(HasEpcExemptionStep.ROUTE_SEGMENT, NoInputFormModel())
        withSubmittedValue(EpcExemptionStep.ROUTE_SEGMENT, NoInputFormModel())
        withSubmittedValue(EpcMissingStep.ROUTE_SEGMENT, NoInputFormModel())
        withSubmittedValue(ProvideEpcLaterStep.ROUTE_SEGMENT, NoInputFormModel())
        withSubmittedValue(CheckEpcAnswersStep.ROUTE_SEGMENT, NoInputFormModel())
        return self()
    }
}
