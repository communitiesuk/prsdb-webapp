package uk.gov.communities.prsdb.webapp.testHelpers.builders

import uk.gov.communities.prsdb.webapp.constants.enums.EicrExemptionReason
import uk.gov.communities.prsdb.webapp.constants.enums.GasSafetyExemptionReason
import uk.gov.communities.prsdb.webapp.constants.enums.HasEpc
import uk.gov.communities.prsdb.webapp.models.dataModels.EpcDataModel
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.MockEpcData
import java.time.LocalDate

class JourneyPageDataBuilder {
    companion object {
        fun beforeLocalCouncilUserRegistrationName() = JourneyDataBuilder().withLandingPageReached().withPrivacyNoticeConfirmed()

        fun beforeLocalCouncilUserRegistrationEmail() = beforeLocalCouncilUserRegistrationName().withName()

        fun beforeLocalCouncilUserRegistrationCheckAnswers() = beforeLocalCouncilUserRegistrationEmail().withEmailAddress()

        fun beforePropertyComplianceGasSafetyIssueDate() = JourneyDataBuilder().withGasSafetyCertStatus(true)

        fun beforePropertyComplianceGasSafetyEngineerNum() = beforePropertyComplianceGasSafetyIssueDate().withGasSafetyIssueDate()

        fun beforePropertyComplianceGasSafetyUpload() = beforePropertyComplianceGasSafetyEngineerNum().withGasSafeEngineerNum()

        fun beforePropertyComplianceGasSafetyExemption() = JourneyDataBuilder().withGasSafetyCertStatus(false)

        fun beforePropertyComplianceGasSafetyExemptionReason() =
            beforePropertyComplianceGasSafetyExemption().withGasSafetyCertExemptionStatus(true)

        fun beforePropertyComplianceGasSafetyExemptionOtherReason() =
            beforePropertyComplianceGasSafetyExemptionReason().withGasSafetyCertExemptionReason(GasSafetyExemptionReason.OTHER)

        fun beforePropertyComplianceEicr() = JourneyDataBuilder().withMissingGasSafetyExemption()

        fun beforePropertyComplianceEicrIssueDate() = beforePropertyComplianceEicr().withEicrStatus(true)

        fun beforePropertyComplianceEicrUpload() = beforePropertyComplianceEicrIssueDate().withEicrIssueDate()

        fun beforePropertyComplianceEicrExemption() = beforePropertyComplianceEicr().withEicrStatus(false)

        fun beforePropertyComplianceEicrExemptionReason() = beforePropertyComplianceEicrExemption().withEicrExemptionStatus(true)

        fun beforePropertyComplianceEicrExemptionOtherReason() =
            beforePropertyComplianceEicrExemptionReason().withEicrExemptionReason(EicrExemptionReason.OTHER)

        fun beforePropertyComplianceEpc() = beforePropertyComplianceEicr().withMissingEicrExemption()

        fun beforePropertyComplianceEpcExemptionReason() = beforePropertyComplianceEpc().withEpcStatus(HasEpc.NOT_REQUIRED)

        fun beforePropertyComplianceCheckAutoMatchedEpc(epcDetails: EpcDataModel = MockEpcData.createEpcDataModel()) =
            beforePropertyComplianceEpc()
                .withEpcStatus(HasEpc.YES)
                .withAutoMatchedEpcDetails(epcDetails)

        fun beforePropertyComplianceEpcLookup() = beforePropertyComplianceCheckAutoMatchedEpc().withCheckAutoMatchedEpcResult(false)

        fun beforePropertyComplianceCheckMatchedEpc(epcDetails: EpcDataModel = MockEpcData.createEpcDataModel()) =
            beforePropertyComplianceEpcLookup().withEpcLookupCertificateNumber().withLookedUpEpcDetails(epcDetails)

        fun beforePropertyComplianceEpcExpiryCheck(epcRating: String) =
            beforePropertyComplianceCheckAutoMatchedEpc(
                MockEpcData.createEpcDataModel(energyRating = epcRating, expiryDate = MockEpcData.expiryDateInThePast),
            ).withCheckAutoMatchedEpcResult(true)

        fun beforePropertyComplianceEpcExpired(epcRating: String) =
            beforePropertyComplianceEpcExpiryCheck(epcRating)
                .withEpcExpiryCheckStep(false)

        fun beforePropertyComplianceMeesExemptionCheck() =
            beforePropertyComplianceCheckAutoMatchedEpc(MockEpcData.createEpcDataModel(energyRating = "G"))
                .withCheckAutoMatchedEpcResult(true)

        fun beforePropertyComplianceMeesExemptionReason() =
            beforePropertyComplianceMeesExemptionCheck()
                .withMeesExemptionCheckStep(true)

        fun beforePropertyComplianceLowEnergyRating() = beforePropertyComplianceMeesExemptionCheck().withMeesExemptionCheckStep(false)

        fun beforePropertyComplianceFireSafetyDeclaration() = beforePropertyComplianceEpc().withMissingEpcExemption()

        fun beforePropertyComplianceKeepPropertySafe() = beforePropertyComplianceFireSafetyDeclaration().withFireSafetyDeclaration()

        fun beforePropertyComplianceResponsibilityToTenants() = beforePropertyComplianceKeepPropertySafe().withKeepPropertySafeDeclaration()

        fun beforePropertyComplianceCheckAnswers() =
            beforePropertyComplianceResponsibilityToTenants().withResponsibilityToTenantsDeclaration()

        fun beforeLandlordDetailsUpdateSelectAddress() = JourneyDataBuilder().withLookupAddress()

        fun beforePropertyDeregistrationReason() = JourneyDataBuilder().withWantsToProceed()

        fun beforePropertyComplianceEicrUpdate(
            gasSafetyIssueDate: LocalDate = LocalDate.now(),
            gasSafeEngineerNumber: String = "1234567",
            gasCertificatefileUploadId: Long = 2L,
        ) = JourneyDataBuilder()
            .withExistingCompliance()
            .withNewGasSafetyCertStatus(true)
            .withGasSafetyIssueDate(gasSafetyIssueDate)
            .withGasSafeEngineerNum(gasSafeEngineerNumber)
            .withGasCertFileUploadId(gasCertificatefileUploadId)
            .withGasSafetyCertUploadConfirmation()
            .withGasSafetyUpdateCheckYourAnswers()

        fun beforePropertyComplianceEpcUpdate(
            gasSafetyIssueDate: LocalDate = LocalDate.now(),
            gasSafeEngineerNumber: String = "1234567",
            gasCertificatefileUploadId: Long = 2L,
            eicrIssueDate: LocalDate = LocalDate.now(),
            eicrFileUploadId: Long = 1L,
        ) = beforePropertyComplianceEicrUpdate(gasSafetyIssueDate, gasSafeEngineerNumber, gasCertificatefileUploadId)
            .withNewEicrStatus(true)
            .withEicrIssueDate(eicrIssueDate)
            .withEicrUploadId(eicrFileUploadId)
            .withEicrUploadConfirmation()
            .withEicrUpdateCheckYourAnswers()
    }
}
