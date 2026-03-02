package uk.gov.communities.prsdb.webapp.testHelpers.builders

import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.CheckGasCertUploadsStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.CheckGasSafetyAnswersStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.GasCertExpiredStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.GasCertIssueDateStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.GasCertMissingStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.HasGasCertStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.HasGasSupplyStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.ProvideGasCertLaterStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.RemoveGasCertUploadStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.UploadGasCertStep
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.FormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.NoInputFormModel

interface GasSafetyStateBuilder<SelfType : GasSafetyStateBuilder<SelfType>> {
    fun withSubmittedValue(
        key: String,
        value: FormModel,
    ): SelfType

    fun self(): SelfType

    // TODO PDJB-628: Update to use actual logic
    fun withNoGasSupply(): SelfType {
        withSubmittedValue(HasGasSupplyStep.ROUTE_SEGMENT, NoInputFormModel())
        withSubmittedValue(HasGasCertStep.ROUTE_SEGMENT, NoInputFormModel())
        withSubmittedValue(GasCertIssueDateStep.ROUTE_SEGMENT, NoInputFormModel())
        withSubmittedValue(UploadGasCertStep.ROUTE_SEGMENT, NoInputFormModel())
        withSubmittedValue(CheckGasCertUploadsStep.ROUTE_SEGMENT, NoInputFormModel())
        withSubmittedValue(RemoveGasCertUploadStep.ROUTE_SEGMENT, NoInputFormModel())
        withSubmittedValue(GasCertExpiredStep.ROUTE_SEGMENT, NoInputFormModel())
        withSubmittedValue(GasCertMissingStep.ROUTE_SEGMENT, NoInputFormModel())
        withSubmittedValue(ProvideGasCertLaterStep.ROUTE_SEGMENT, NoInputFormModel())
        withSubmittedValue(CheckGasSafetyAnswersStep.ROUTE_SEGMENT, NoInputFormModel())
        return self()
    }
}
