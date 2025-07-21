package uk.gov.communities.prsdb.webapp.testHelpers.builders

import uk.gov.communities.prsdb.webapp.constants.enums.EicrExemptionReason
import uk.gov.communities.prsdb.webapp.constants.enums.GasSafetyExemptionReason
import uk.gov.communities.prsdb.webapp.constants.enums.HasEpc
import uk.gov.communities.prsdb.webapp.constants.enums.LicensingType
import uk.gov.communities.prsdb.webapp.models.dataModels.AddressDataModel
import uk.gov.communities.prsdb.webapp.models.dataModels.EpcDataModel
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.MockEpcData

class JourneyPageDataBuilder {
    companion object {
        fun beforeLandlordRegistrationConfirmIdentity() = JourneyDataBuilder().withVerifiedUser()

        fun beforeLandlordRegistrationName() = JourneyDataBuilder().withUnverifiedUser(name = null)

        fun beforeLandlordRegistrationDob() = JourneyDataBuilder().withUnverifiedUser(dob = null)

        fun beforeLandlordRegistrationEmail() = beforeLandlordRegistrationConfirmIdentity()

        fun beforeLandlordRegistrationPhoneNumber() = beforeLandlordRegistrationEmail().withEmailAddress()

        fun beforeLandlordRegistrationCountryOfResidence() = beforeLandlordRegistrationPhoneNumber().withPhoneNumber()

        fun beforeLandlordRegistrationLookupAddress() = beforeLandlordRegistrationCountryOfResidence().withEnglandOrWalesResidence()

        fun beforeLandlordRegistrationSelectAddress() =
            beforeLandlordRegistrationLookupAddress().withLookupAddress().withLookedUpAddresses()

        fun beforeLandlordRegistrationManualAddress() = beforeLandlordRegistrationSelectAddress().withManualAddressSelected()

        fun beforeLandlordRegistrationNonEnglandOrWalesAddress() =
            beforeLandlordRegistrationCountryOfResidence().withNonEnglandOrWalesAddress(nonEnglandOrWalesAddress = null)

        fun beforeLandlordRegistrationLookupContactAddress() =
            beforeLandlordRegistrationNonEnglandOrWalesAddress().withNonEnglandOrWalesAddress()

        fun beforeLandlordRegistrationSelectContactAddress() =
            beforeLandlordRegistrationLookupContactAddress().withLookupAddress(isContactAddress = true).withLookedUpAddresses()

        fun beforeLandlordRegistrationManualContactAddress() =
            beforeLandlordRegistrationSelectContactAddress().withManualAddressSelected(isContactAddress = true)

        fun beforeLandlordRegistrationCheckAnswers() = beforeLandlordRegistrationSelectAddress().withSelectedAddress()

        fun beforeLandlordRegistrationDeclaration() = beforeLandlordRegistrationCheckAnswers().withCheckedAnswers()

        fun beforeLaUserRegistrationName() = JourneyDataBuilder().withLandingPageReached()

        fun beforeLaUserRegistrationEmail() = beforeLaUserRegistrationName().withName()

        fun beforeLaUserRegistrationCheckAnswers() = beforeLaUserRegistrationEmail().withEmailAddress()

        fun beforePropertyRegistrationSelectAddress(customLookedUpAddresses: List<AddressDataModel>? = null) =
            JourneyDataBuilder().withLookupAddress().withLookedUpAddresses(customLookedUpAddresses)

        fun beforePropertyRegistrationManualAddress() = beforePropertyRegistrationSelectAddress().withManualAddressSelected()

        fun beforePropertyRegistrationSelectLocalAuthority() = beforePropertyRegistrationManualAddress().withManualAddress()

        fun beforePropertyRegistrationPropertyType() = JourneyDataBuilder().withLookupAddress().withSelectedAddress()

        fun beforePropertyRegistrationOwnershipType() = beforePropertyRegistrationPropertyType().withPropertyType()

        fun beforePropertyRegistrationLicensingType() = beforePropertyRegistrationOwnershipType().withOwnershipType()

        fun beforePropertyRegistrationSelectiveLicence() =
            beforePropertyRegistrationLicensingType().withLicensingType(LicensingType.SELECTIVE_LICENCE)

        fun beforePropertyRegistrationHmoMandatoryLicence() =
            beforePropertyRegistrationLicensingType().withLicensingType(LicensingType.HMO_MANDATORY_LICENCE)

        fun beforePropertyRegistrationHmoAdditionalLicence() =
            beforePropertyRegistrationLicensingType().withLicensingType(LicensingType.HMO_ADDITIONAL_LICENCE)

        fun beforePropertyRegistrationOccupancy() = beforePropertyRegistrationLicensingType().withLicensingType(LicensingType.NO_LICENSING)

        fun beforePropertyRegistrationHouseholds() = beforePropertyRegistrationOccupancy().withOccupancyStatus(true)

        fun beforePropertyRegistrationPeople() = beforePropertyRegistrationHouseholds().withHouseholds()

        fun beforePropertyRegistrationCheckAnswers() = beforePropertyRegistrationOccupancy().withOccupancyStatus(false)

        fun beforePropertyRegistrationDeclaration() = beforePropertyRegistrationCheckAnswers().withCheckedAnswers()

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

        // TODO PRSD-1312 - this should get built from originalJourneyData but currently tasks after GasSafety are unreachable
        fun beforePropertyComplianceUpdateEpc() =
            JourneyDataBuilder()
                .withUpdateGasSafetyCertificateStatus(false)
                .withGasSafetyUpdateCheckAnswers()
                .withUpdateEicrStatus(false)
                .withEicrUpdateCheckAnswers()

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

        fun beforePropertyComplianceKeepPropertySafe() = beforePropertyComplianceFireSafetyDeclaration().withFireSafetyDeclaration(true)

        fun beforePropertyComplianceResponsibilityToTenants() = beforePropertyComplianceKeepPropertySafe().withKeepPropertySafeDeclaration()

        fun beforePropertyComplianceCheckAnswers() =
            beforePropertyComplianceResponsibilityToTenants().withResponsibilityToTenantsDeclaration()

        fun beforeLandlordDetailsUpdateSelectAddress() = JourneyDataBuilder().withLookupAddress()

        fun beforePropertyDeregistrationReason() = JourneyDataBuilder().withWantsToProceed()
    }
}
