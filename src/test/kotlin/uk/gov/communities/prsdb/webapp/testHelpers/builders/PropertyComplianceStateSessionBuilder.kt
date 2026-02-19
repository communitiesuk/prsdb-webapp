package uk.gov.communities.prsdb.webapp.testHelpers.builders

import kotlinx.datetime.LocalDate
import kotlinx.serialization.json.Json
import uk.gov.communities.prsdb.webapp.constants.enums.EicrExemptionReason
import uk.gov.communities.prsdb.webapp.constants.enums.EpcExemptionReason
import uk.gov.communities.prsdb.webapp.constants.enums.GasSafetyExemptionReason
import uk.gov.communities.prsdb.webapp.constants.enums.HasEpc
import uk.gov.communities.prsdb.webapp.constants.enums.MeesExemptionReason
import uk.gov.communities.prsdb.webapp.journeys.propertyCompliance.steps.CheckMatchedEpcStep
import uk.gov.communities.prsdb.webapp.journeys.propertyCompliance.steps.EicrExemptionConfirmationStep
import uk.gov.communities.prsdb.webapp.journeys.propertyCompliance.steps.EicrExemptionMissingStep
import uk.gov.communities.prsdb.webapp.journeys.propertyCompliance.steps.EicrExemptionOtherReasonStep
import uk.gov.communities.prsdb.webapp.journeys.propertyCompliance.steps.EicrExemptionReasonStep
import uk.gov.communities.prsdb.webapp.journeys.propertyCompliance.steps.EicrExemptionStep
import uk.gov.communities.prsdb.webapp.journeys.propertyCompliance.steps.EicrIssueDateStep
import uk.gov.communities.prsdb.webapp.journeys.propertyCompliance.steps.EicrOutdatedStep
import uk.gov.communities.prsdb.webapp.journeys.propertyCompliance.steps.EicrStep
import uk.gov.communities.prsdb.webapp.journeys.propertyCompliance.steps.EicrUploadConfirmationStep
import uk.gov.communities.prsdb.webapp.journeys.propertyCompliance.steps.EicrUploadStep
import uk.gov.communities.prsdb.webapp.journeys.propertyCompliance.steps.EpcExemptionConfirmationStep
import uk.gov.communities.prsdb.webapp.journeys.propertyCompliance.steps.EpcExemptionReasonStep
import uk.gov.communities.prsdb.webapp.journeys.propertyCompliance.steps.EpcExpiredStep
import uk.gov.communities.prsdb.webapp.journeys.propertyCompliance.steps.EpcExpiryCheckStep
import uk.gov.communities.prsdb.webapp.journeys.propertyCompliance.steps.EpcMissingStep
import uk.gov.communities.prsdb.webapp.journeys.propertyCompliance.steps.EpcNotFoundStep
import uk.gov.communities.prsdb.webapp.journeys.propertyCompliance.steps.EpcQuestionStep
import uk.gov.communities.prsdb.webapp.journeys.propertyCompliance.steps.EpcSupersededStep
import uk.gov.communities.prsdb.webapp.journeys.propertyCompliance.steps.FireSafetyDeclarationStep
import uk.gov.communities.prsdb.webapp.journeys.propertyCompliance.steps.GasSafetyCertificateUploadStep
import uk.gov.communities.prsdb.webapp.journeys.propertyCompliance.steps.GasSafetyEngineerNumberStep
import uk.gov.communities.prsdb.webapp.journeys.propertyCompliance.steps.GasSafetyExemptionConfirmationStep
import uk.gov.communities.prsdb.webapp.journeys.propertyCompliance.steps.GasSafetyExemptionMissingStep
import uk.gov.communities.prsdb.webapp.journeys.propertyCompliance.steps.GasSafetyExemptionOtherReasonStep
import uk.gov.communities.prsdb.webapp.journeys.propertyCompliance.steps.GasSafetyExemptionReasonStep
import uk.gov.communities.prsdb.webapp.journeys.propertyCompliance.steps.GasSafetyExemptionStep
import uk.gov.communities.prsdb.webapp.journeys.propertyCompliance.steps.GasSafetyIssueDateStep
import uk.gov.communities.prsdb.webapp.journeys.propertyCompliance.steps.GasSafetyOutdatedStep
import uk.gov.communities.prsdb.webapp.journeys.propertyCompliance.steps.GasSafetyStep
import uk.gov.communities.prsdb.webapp.journeys.propertyCompliance.steps.GasSafetyUploadConfirmationStep
import uk.gov.communities.prsdb.webapp.journeys.propertyCompliance.steps.KeepPropertySafeStep
import uk.gov.communities.prsdb.webapp.journeys.propertyCompliance.steps.LowEnergyRatingStep
import uk.gov.communities.prsdb.webapp.journeys.propertyCompliance.steps.MeesExemptionCheckStep
import uk.gov.communities.prsdb.webapp.journeys.propertyCompliance.steps.MeesExemptionConfirmationStep
import uk.gov.communities.prsdb.webapp.journeys.propertyCompliance.steps.MeesExemptionReasonStep
import uk.gov.communities.prsdb.webapp.journeys.propertyCompliance.steps.ResponsibilityToTenantsStep
import uk.gov.communities.prsdb.webapp.journeys.propertyCompliance.steps.SearchForEpcStep
import uk.gov.communities.prsdb.webapp.models.dataModels.EpcDataModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.CheckMatchedEpcFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.EicrExemptionFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.EicrExemptionOtherReasonFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.EicrExemptionReasonFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.EicrFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.EicrUploadCertificateFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.EpcExemptionReasonFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.EpcExpiryCheckFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.EpcFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.EpcLookupFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.FireSafetyDeclarationFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.GasSafeEngineerNumFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.GasSafetyExemptionFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.GasSafetyExemptionOtherReasonFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.GasSafetyExemptionReasonFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.GasSafetyFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.GasSafetyUploadCertificateFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.KeepPropertySafeFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.MeesExemptionCheckFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.MeesExemptionReasonFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.NoInputFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.ResponsibilityToTenantsFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.TodayOrPastDateFormModel

class PropertyComplianceStateSessionBuilder : JourneyStateSessionBuilder<PropertyComplianceStateSessionBuilder>() {
    // Gas Safety Certificate
    fun withGasSafetyCertStatus(hasGasSafetyCert: Boolean): PropertyComplianceStateSessionBuilder {
        val formModel = GasSafetyFormModel().apply { hasCert = hasGasSafetyCert }
        withSubmittedValue(GasSafetyStep.ROUTE_SEGMENT, formModel)
        return self()
    }

    fun withGasSafetyIssueDate(issueDate: LocalDate = LocalDate(2024, 1, 1)): PropertyComplianceStateSessionBuilder {
        val formModel =
            TodayOrPastDateFormModel().apply {
                day = issueDate.dayOfMonth.toString()
                month = issueDate.monthNumber.toString()
                year = issueDate.year.toString()
            }
        withSubmittedValue(GasSafetyIssueDateStep.ROUTE_SEGMENT, formModel)
        return self()
    }

    fun withGasSafeEngineerNum(engineerNum: String = "1234567"): PropertyComplianceStateSessionBuilder {
        val formModel = GasSafeEngineerNumFormModel().apply { engineerNumber = engineerNum }
        withSubmittedValue(GasSafetyEngineerNumberStep.ROUTE_SEGMENT, formModel)
        return self()
    }

    fun withGasCertFileUploadId(
        uploadId: Long,
        metadataOnly: Boolean = false,
    ): PropertyComplianceStateSessionBuilder {
        val formModel =
            GasSafetyUploadCertificateFormModel().apply {
                fileUploadId = uploadId
                isUserSubmittedMetadataOnly = metadataOnly
            }
        withSubmittedValue(GasSafetyCertificateUploadStep.ROUTE_SEGMENT, formModel)
        return self()
    }

    fun withGasSafetyCertUploadConfirmation(): PropertyComplianceStateSessionBuilder {
        withSubmittedValue(GasSafetyUploadConfirmationStep.ROUTE_SEGMENT, NoInputFormModel())
        return self()
    }

    fun withGasSafetyOutdatedConfirmation(): PropertyComplianceStateSessionBuilder {
        withSubmittedValue(GasSafetyOutdatedStep.ROUTE_SEGMENT, NoInputFormModel())
        return self()
    }

    fun withGasSafetyCertExemptionStatus(hasGasSafetyCertExemption: Boolean): PropertyComplianceStateSessionBuilder {
        val formModel = GasSafetyExemptionFormModel().apply { hasExemption = hasGasSafetyCertExemption }
        withSubmittedValue(GasSafetyExemptionStep.ROUTE_SEGMENT, formModel)
        return self()
    }

    fun withGasSafetyCertExemptionReason(gasSafetyCertExemptionReason: GasSafetyExemptionReason): PropertyComplianceStateSessionBuilder {
        val formModel = GasSafetyExemptionReasonFormModel().apply { exemptionReason = gasSafetyCertExemptionReason }
        withSubmittedValue(GasSafetyExemptionReasonStep.ROUTE_SEGMENT, formModel)
        return self()
    }

    fun withGasSafetyCertExemptionOtherReason(otherReason: String): PropertyComplianceStateSessionBuilder {
        withGasSafetyCertExemptionReason(GasSafetyExemptionReason.OTHER)
        val formModel = GasSafetyExemptionOtherReasonFormModel().apply { this.otherReason = otherReason }
        withSubmittedValue(GasSafetyExemptionOtherReasonStep.ROUTE_SEGMENT, formModel)
        return self()
    }

    fun withGasSafetyCertExemptionConfirmation(): PropertyComplianceStateSessionBuilder {
        withSubmittedValue(GasSafetyExemptionConfirmationStep.ROUTE_SEGMENT, NoInputFormModel())
        return self()
    }

    // TODO PDJB-467 - we might not need this
    fun withMissingGasSafetyExemption(): PropertyComplianceStateSessionBuilder {
        withGasSafetyCertStatus(false)
        withGasSafetyCertExemptionStatus(false)
        withSubmittedValue(GasSafetyExemptionMissingStep.ROUTE_SEGMENT, NoInputFormModel())
        return self()
    }

    // EICR
    fun withEicrStatus(hasEICR: Boolean): PropertyComplianceStateSessionBuilder {
        val formModel = EicrFormModel().apply { hasCert = hasEICR }
        withSubmittedValue(EicrStep.ROUTE_SEGMENT, formModel)
        return self()
    }

    fun withEicrIssueDate(issueDate: LocalDate = LocalDate(2024, 2, 1)): PropertyComplianceStateSessionBuilder {
        val formModel =
            TodayOrPastDateFormModel().apply {
                day = issueDate.dayOfMonth.toString()
                month = issueDate.monthNumber.toString()
                year = issueDate.year.toString()
            }
        withSubmittedValue(EicrIssueDateStep.ROUTE_SEGMENT, formModel)
        return self()
    }

    fun withEicrUploadId(
        uploadId: Long,
        metadataOnly: Boolean = false,
    ): PropertyComplianceStateSessionBuilder {
        val formModel =
            EicrUploadCertificateFormModel().apply {
                fileUploadId = uploadId
                isUserSubmittedMetadataOnly = metadataOnly
            }
        withSubmittedValue(EicrUploadStep.ROUTE_SEGMENT, formModel)
        return self()
    }

    fun withEicrUploadConfirmation(): PropertyComplianceStateSessionBuilder {
        withSubmittedValue(EicrUploadConfirmationStep.ROUTE_SEGMENT, NoInputFormModel())
        return self()
    }

    fun withEicrOutdatedConfirmation(): PropertyComplianceStateSessionBuilder {
        withSubmittedValue(EicrOutdatedStep.ROUTE_SEGMENT, NoInputFormModel())
        return self()
    }

    fun withEicrExemptionStatus(hasEicrExemption: Boolean): PropertyComplianceStateSessionBuilder {
        val formModel = EicrExemptionFormModel().apply { hasExemption = hasEicrExemption }
        withSubmittedValue(EicrExemptionStep.ROUTE_SEGMENT, formModel)
        return self()
    }

    fun withEicrExemptionReason(eicrExemptionReason: EicrExemptionReason): PropertyComplianceStateSessionBuilder {
        val formModel = EicrExemptionReasonFormModel().apply { exemptionReason = eicrExemptionReason }
        withSubmittedValue(EicrExemptionReasonStep.ROUTE_SEGMENT, formModel)
        return self()
    }

    fun withEicrExemptionOtherReason(otherReason: String): PropertyComplianceStateSessionBuilder {
        withEicrExemptionReason(EicrExemptionReason.OTHER)
        val formModel = EicrExemptionOtherReasonFormModel().apply { this.otherReason = otherReason }
        withSubmittedValue(EicrExemptionOtherReasonStep.ROUTE_SEGMENT, formModel)
        return self()
    }

    fun withEicrExemptionConfirmation(): PropertyComplianceStateSessionBuilder {
        withSubmittedValue(EicrExemptionConfirmationStep.ROUTE_SEGMENT, NoInputFormModel())
        return self()
    }

    fun withMissingEicrExemption(): PropertyComplianceStateSessionBuilder {
        withEicrStatus(false)
        withEicrExemptionStatus(false)
        withSubmittedValue(EicrExemptionMissingStep.ROUTE_SEGMENT, NoInputFormModel())
        return self()
    }

    // EPC
    fun withEpcStatus(hasEpc: HasEpc): PropertyComplianceStateSessionBuilder {
        val formModel = EpcFormModel().apply { hasCert = hasEpc }
        withSubmittedValue(EpcQuestionStep.ROUTE_SEGMENT, formModel)
        return self()
    }

    fun withAutoMatchedEpcDetails(epcDetails: EpcDataModel): PropertyComplianceStateSessionBuilder {
        additionalDataMap["automatchedEpc"] = Json.encodeToString(EpcDataModel.serializer(), epcDetails)
        return self()
    }

    fun withCheckAutoMatchedEpcResult(matchedEpcIsCorrect: Boolean): PropertyComplianceStateSessionBuilder {
        val formModel = CheckMatchedEpcFormModel().apply { this.matchedEpcIsCorrect = matchedEpcIsCorrect }
        withSubmittedValue(CheckMatchedEpcStep.AUTOMATCHED_ROUTE_SEGMENT, formModel)
        return self()
    }

    fun withCheckMatchedEpcResult(matchedEpcIsCorrect: Boolean): PropertyComplianceStateSessionBuilder {
        val formModel = CheckMatchedEpcFormModel().apply { this.matchedEpcIsCorrect = matchedEpcIsCorrect }
        withSubmittedValue(CheckMatchedEpcStep.ROUTE_SEGMENT, formModel)
        return self()
    }

    fun withAcceptedEpcDetails(epcDetails: EpcDataModel): PropertyComplianceStateSessionBuilder {
        additionalDataMap["acceptedEpc"] = Json.encodeToString(EpcDataModel.serializer(), epcDetails)
        return self()
    }

    fun withEpcLookupCertificateNumber(certificateNumber: String = "0000-0000-0000-0000-0001"): PropertyComplianceStateSessionBuilder {
        val formModel = EpcLookupFormModel().apply { this.certificateNumber = certificateNumber }
        withSubmittedValue(SearchForEpcStep.ROUTE_SEGMENT, formModel)
        return self()
    }

    fun withLookedUpEpcDetails(epcDetails: EpcDataModel): PropertyComplianceStateSessionBuilder {
        additionalDataMap["searchedEpc"] = Json.encodeToString(EpcDataModel.serializer(), epcDetails)
        return self()
    }

    fun withEpcSuperseded(): PropertyComplianceStateSessionBuilder {
        withSubmittedValue(EpcSupersededStep.ROUTE_SEGMENT, NoInputFormModel())
        return self()
    }

    fun withEpcExemptionReason(epcExemptionReason: EpcExemptionReason): PropertyComplianceStateSessionBuilder {
        val formModel = EpcExemptionReasonFormModel().apply { exemptionReason = epcExemptionReason }
        withSubmittedValue(EpcExemptionReasonStep.ROUTE_SEGMENT, formModel)
        return self()
    }

    fun withEpcExemptionConfirmation(): PropertyComplianceStateSessionBuilder {
        withSubmittedValue(EpcExemptionConfirmationStep.ROUTE_SEGMENT, NoInputFormModel())
        return self()
    }

    fun withEpcMissing(): PropertyComplianceStateSessionBuilder {
        withSubmittedValue(EpcMissingStep.ROUTE_SEGMENT, NoInputFormModel())
        return self()
    }

    fun withEpcExpiryCheck(tenancyStartedBeforeExpiry: Boolean): PropertyComplianceStateSessionBuilder {
        val formModel = EpcExpiryCheckFormModel().apply { this.tenancyStartedBeforeExpiry = tenancyStartedBeforeExpiry }
        withSubmittedValue(EpcExpiryCheckStep.ROUTE_SEGMENT, formModel)
        return self()
    }

    fun withEpcExpired(): PropertyComplianceStateSessionBuilder {
        withSubmittedValue(EpcExpiredStep.ROUTE_SEGMENT, NoInputFormModel())
        return self()
    }

    fun withEpcNotFound(): PropertyComplianceStateSessionBuilder {
        withSubmittedValue(EpcNotFoundStep.ROUTE_SEGMENT, NoInputFormModel())
        return self()
    }

    fun withLowEnergyRating(): PropertyComplianceStateSessionBuilder {
        withSubmittedValue(LowEnergyRatingStep.ROUTE_SEGMENT, NoInputFormModel())
        return self()
    }

    fun withMeesExemptionCheck(hasExemption: Boolean): PropertyComplianceStateSessionBuilder {
        val formModel = MeesExemptionCheckFormModel().apply { propertyHasExemption = hasExemption }
        withSubmittedValue(MeesExemptionCheckStep.ROUTE_SEGMENT, formModel)
        return self()
    }

    fun withMeesExemptionReason(exemptionReason: MeesExemptionReason): PropertyComplianceStateSessionBuilder {
        val formModel = MeesExemptionReasonFormModel().apply { this.exemptionReason = exemptionReason }
        withSubmittedValue(MeesExemptionReasonStep.ROUTE_SEGMENT, formModel)
        return self()
    }

    fun withMeesExemptionConfirmation(): PropertyComplianceStateSessionBuilder {
        withSubmittedValue(MeesExemptionConfirmationStep.ROUTE_SEGMENT, NoInputFormModel())
        return self()
    }

    fun withMissingEpcExemption(): PropertyComplianceStateSessionBuilder {
        withEpcStatus(HasEpc.NO)
        withSubmittedValue(EpcMissingStep.ROUTE_SEGMENT, NoInputFormModel())
        return self()
    }

    // Landlord Responsibilities
    fun withFireSafetyDeclaration(): PropertyComplianceStateSessionBuilder {
        val formModel = FireSafetyDeclarationFormModel().apply { hasDeclared = true }
        withSubmittedValue(FireSafetyDeclarationStep.ROUTE_SEGMENT, formModel)
        return self()
    }

    fun withKeepPropertySafeDeclaration(): PropertyComplianceStateSessionBuilder {
        val formModel = KeepPropertySafeFormModel().apply { agreesToResponsibility = true }
        withSubmittedValue(KeepPropertySafeStep.ROUTE_SEGMENT, formModel)
        return self()
    }

    fun withResponsibilityToTenantsDeclaration(): PropertyComplianceStateSessionBuilder {
        val formModel = ResponsibilityToTenantsFormModel().apply { agreesToResponsibility = true }
        withSubmittedValue(ResponsibilityToTenantsStep.ROUTE_SEGMENT, formModel)
        return self()
    }

    companion object {
        // Common journey states (similar to LandlordStateSessionBuilder.beforeName(), etc.)
        fun beforeGasSafetyIssueDate() = PropertyComplianceStateSessionBuilder().withGasSafetyCertStatus(true)

        fun beforeGasSafetyEngineerNum() = beforeGasSafetyIssueDate().withGasSafetyIssueDate()

        fun beforeGasSafetyUpload() = beforeGasSafetyEngineerNum().withGasSafeEngineerNum()

        fun beforeGasSafetyExemption() = PropertyComplianceStateSessionBuilder().withGasSafetyCertStatus(false)

        fun beforeGasSafetyExemptionReason() = beforeGasSafetyExemption().withGasSafetyCertExemptionStatus(true)

        fun beforeGasSafetyExemptionOtherReason() =
            beforeGasSafetyExemptionReason().withGasSafetyCertExemptionReason(GasSafetyExemptionReason.OTHER)

        fun beforeEicr() = PropertyComplianceStateSessionBuilder().withMissingGasSafetyExemption()

        fun beforeEicrIssueDate() = beforeEicr().withEicrStatus(true)

        fun beforeEicrUpload() = beforeEicrIssueDate().withEicrIssueDate()

        fun beforeEicrExemption() = beforeEicr().withEicrStatus(false)

        fun beforeEicrExemptionReason() = beforeEicrExemption().withEicrExemptionStatus(true)

        fun beforeEpc() = PropertyComplianceStateSessionBuilder().withMissingGasSafetyExemption().withMissingEicrExemption()

        fun beforeFireSafetyDeclaration() = beforeEpc().withMissingEpcExemption()

        fun beforeKeepPropertySafe() = beforeFireSafetyDeclaration().withFireSafetyDeclaration()

        fun beforeResponsibilityToTenants() = beforeKeepPropertySafe().withKeepPropertySafeDeclaration()

        fun beforeCheckAnswers() = beforeResponsibilityToTenants().withResponsibilityToTenantsDeclaration()
    }
}
