package uk.gov.communities.prsdb.webapp.testHelpers.builders

import kotlinx.datetime.LocalDate
import kotlinx.serialization.json.Json
import kotlinx.serialization.serializer
import uk.gov.communities.prsdb.webapp.constants.CONTINUE_BUTTON_ACTION_NAME
import uk.gov.communities.prsdb.webapp.constants.PROVIDE_THIS_LATER_BUTTON_ACTION_NAME
import uk.gov.communities.prsdb.webapp.constants.enums.HasElectricalSafetyCertificate
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.states.CertificateUpload
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.CheckEicUploadsStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.CheckEicrUploadsStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.CheckElectricalSafetyAnswersStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.EicExpiredStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.EicExpiryDateStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.EicrExpiredStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.EicrExpiryDateStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.ElectricalCertMissingStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.HasElectricalCertStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.ProvideElectricalCertLaterStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.UploadEicStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.UploadEicrStep
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

    fun withCheckElectricalSafetyAnswersComplete(): SelfType {
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

    fun withEicExpiryDate(expiryDate: LocalDate = LocalDate(2030, 1, 1)): SelfType =
        withElectricalCertExpiryDate(EicExpiryDateStep.ROUTE_SEGMENT, expiryDate)

    fun withEicrExpiryDate(expiryDate: LocalDate = LocalDate(2030, 1, 1)): SelfType =
        withElectricalCertExpiryDate(EicrExpiryDateStep.ROUTE_SEGMENT, expiryDate)

    fun withEicUploads(uploads: Map<Int, CertificateUpload> = defaultElectricalCertUploads): SelfType {
        additionalDataMap["eicUploadMap"] = Json.encodeToString(serializer(), uploads)
        additionalDataMap["highestAssignedEicMemberId"] = Json.encodeToString(serializer(), uploads.keys.max())
        withSubmittedValue(UploadEicStep.ROUTE_SEGMENT, NoInputFormModel())
        withSubmittedValue(CheckEicUploadsStep.ROUTE_SEGMENT, NoInputFormModel())
        return self()
    }

    fun withEicrUploads(uploads: Map<Int, CertificateUpload> = defaultElectricalCertUploads): SelfType {
        additionalDataMap["eicrUploadMap"] = Json.encodeToString(serializer(), uploads)
        additionalDataMap["highestAssignedEicrMemberId"] = Json.encodeToString(serializer(), uploads.keys.max())
        withSubmittedValue(UploadEicrStep.ROUTE_SEGMENT, NoInputFormModel())
        withSubmittedValue(CheckEicrUploadsStep.ROUTE_SEGMENT, NoInputFormModel())
        return self()
    }

    fun withEicExpiredAcknowledged(): SelfType {
        withSubmittedValue(EicExpiredStep.ROUTE_SEGMENT, NoInputFormModel())
        return self()
    }

    fun withEicrExpiredAcknowledged(): SelfType {
        withSubmittedValue(EicrExpiredStep.ROUTE_SEGMENT, NoInputFormModel())
        return self()
    }

    private fun withElectricalCertExpiryDate(
        routeSegment: String,
        expiryDate: LocalDate,
    ): SelfType {
        val formModel =
            AnyDateFormModel().apply {
                day = expiryDate.dayOfMonth.toString()
                month = expiryDate.monthNumber.toString()
                year = expiryDate.year.toString()
            }
        withSubmittedValue(routeSegment, formModel)
        return self()
    }

    private val defaultElectricalCertUploads: Map<Int, CertificateUpload>
        get() = mapOf(1 to CertificateUpload(fileUploadId = 1L, fileName = "electrical-safety-cert.pdf"))
}
