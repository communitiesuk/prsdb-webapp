package uk.gov.communities.prsdb.webapp.testHelpers.builders

import uk.gov.communities.prsdb.webapp.constants.enums.HasElectricalSafetyCertificate
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.CheckElectricalSafetyAnswersStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.ElectricalCertMissingStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.HasElectricalCertStep
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.FormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.HasElectricalCertFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.NoInputFormModel

interface ElectricalSafetyStateBuilder<SelfType : ElectricalSafetyStateBuilder<SelfType>> {
    fun withSubmittedValue(
        key: String,
        value: FormModel,
    ): SelfType

    fun self(): SelfType

    fun withNoElectricalSafetyCertificate(): SelfType {
        val hasElectricalCertFormModel =
            HasElectricalCertFormModel().apply {
                electricalCertType = HasElectricalSafetyCertificate.NO_CERTIFICATE
            }
        withSubmittedValue(HasElectricalCertStep.ROUTE_SEGMENT, hasElectricalCertFormModel)
        return self()
    }

    fun withEicr(): SelfType {
        val hasElectricalCertFormModel =
            HasElectricalCertFormModel().apply {
                electricalCertType = HasElectricalSafetyCertificate.HAS_EICR
            }
        withSubmittedValue(HasElectricalCertStep.ROUTE_SEGMENT, hasElectricalCertFormModel)
        return self()
    }

    fun withEic(): SelfType {
        val hasElectricalCertFormModel =
            HasElectricalCertFormModel().apply {
                electricalCertType = HasElectricalSafetyCertificate.HAS_EIC
            }
        withSubmittedValue(HasElectricalCertStep.ROUTE_SEGMENT, hasElectricalCertFormModel)
        return self()
    }

    fun withElectricalSafetyCertificateMissing(): SelfType {
        withNoElectricalSafetyCertificate()
        withSubmittedValue(ElectricalCertMissingStep.ROUTE_SEGMENT, NoInputFormModel())
        withSubmittedValue(CheckElectricalSafetyAnswersStep.ROUTE_SEGMENT, NoInputFormModel())
        return self()
    }
}
