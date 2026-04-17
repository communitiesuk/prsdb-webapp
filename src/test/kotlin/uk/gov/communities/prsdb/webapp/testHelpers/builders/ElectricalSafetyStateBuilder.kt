package uk.gov.communities.prsdb.webapp.testHelpers.builders

import kotlinx.datetime.LocalDate
import kotlinx.serialization.json.Json
import kotlinx.serialization.serializer
import uk.gov.communities.prsdb.webapp.constants.CONTINUE_BUTTON_ACTION_NAME
import uk.gov.communities.prsdb.webapp.constants.PROVIDE_THIS_LATER_BUTTON_ACTION_NAME
import uk.gov.communities.prsdb.webapp.constants.enums.HasElectricalSafetyCertificate
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.states.CertificateUpload
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.CheckElectricalCertUploadsStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.CheckElectricalSafetyAnswersStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.ElectricalCertExpiredStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.ElectricalCertExpiryDateStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.ElectricalCertMissingStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.HasElectricalCertStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.ProvideElectricalCertLaterStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.UploadElectricalCertStep
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.AnyDateFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.FormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.HasElectricalCertFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.NoInputFormModel

interface ElectricalSafetyStateBuilder<SelfType : ElectricalSafetyStateBuilder<SelfType>> {
    val additionalDataMap: MutableMap<String, String>

    fun withSubmittedValue(
        key: String,
        value: FormModel,
    ): SelfType

    fun self(): SelfType

    fun withNoElectricalSafetyCertificate(): SelfType {
        val hasElectricalCertFormModel =
            HasElectricalCertFormModel().apply {
                electricalCertType = HasElectricalSafetyCertificate.NO_CERTIFICATE
                action = CONTINUE_BUTTON_ACTION_NAME
            }
        withSubmittedValue(HasElectricalCertStep.ROUTE_SEGMENT, hasElectricalCertFormModel)
        withSubmittedValue(ElectricalCertMissingStep.ROUTE_SEGMENT, NoInputFormModel())
        return self()
    }

    fun withEicr(): SelfType {
        val hasElectricalCertFormModel =
            HasElectricalCertFormModel().apply {
                electricalCertType = HasElectricalSafetyCertificate.HAS_EICR
                action = CONTINUE_BUTTON_ACTION_NAME
            }
        withSubmittedValue(HasElectricalCertStep.ROUTE_SEGMENT, hasElectricalCertFormModel)
        return self()
    }

    fun withEic(): SelfType {
        val hasElectricalCertFormModel =
            HasElectricalCertFormModel().apply {
                electricalCertType = HasElectricalSafetyCertificate.HAS_EIC
                action = CONTINUE_BUTTON_ACTION_NAME
            }
        withSubmittedValue(HasElectricalCertStep.ROUTE_SEGMENT, hasElectricalCertFormModel)
        return self()
    }

    fun withElectricalSafetyCertificateMissing(): SelfType {
        withNoElectricalSafetyCertificate()
        withSubmittedValue(CheckElectricalSafetyAnswersStep.ROUTE_SEGMENT, NoInputFormModel())
        return self()
    }

    fun withProvideElectricalCertLater(): SelfType {
        val hasElectricalCertFormModel =
            HasElectricalCertFormModel().apply {
                action = PROVIDE_THIS_LATER_BUTTON_ACTION_NAME
            }
        withSubmittedValue(HasElectricalCertStep.ROUTE_SEGMENT, hasElectricalCertFormModel)
        withSubmittedValue(ProvideElectricalCertLaterStep.ROUTE_SEGMENT, NoInputFormModel())
        return self()
    }

    fun withElectricalCertExpiryDate(expiryDate: LocalDate = LocalDate(2030, 1, 1)): SelfType {
        val formModel =
            AnyDateFormModel().apply {
                day = expiryDate.dayOfMonth.toString()
                month = expiryDate.monthNumber.toString()
                year = expiryDate.year.toString()
            }
        withSubmittedValue(ElectricalCertExpiryDateStep.ROUTE_SEGMENT, formModel)
        return self()
    }

    fun withElectricalCertUploads(
        uploads: Map<Int, CertificateUpload> = mapOf(1 to CertificateUpload(fileUploadId = 1L, fileName = "electrical-safety-cert.pdf")),
    ): SelfType {
        additionalDataMap["electricalUploadMap"] = Json.encodeToString(serializer(), uploads)
        additionalDataMap["nextElectricalUploadMemberId"] = Json.encodeToString(serializer(), (uploads.keys.max()) + 1)
        withSubmittedValue(UploadElectricalCertStep.ROUTE_SEGMENT, NoInputFormModel())
        withSubmittedValue(CheckElectricalCertUploadsStep.ROUTE_SEGMENT, NoInputFormModel())
        return self()
    }

    fun withElectricalCertExpiredAcknowledged(): SelfType {
        withSubmittedValue(ElectricalCertExpiredStep.ROUTE_SEGMENT, NoInputFormModel())
        return self()
    }
}
