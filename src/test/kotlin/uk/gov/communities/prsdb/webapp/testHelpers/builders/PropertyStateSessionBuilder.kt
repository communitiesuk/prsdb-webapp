package uk.gov.communities.prsdb.webapp.testHelpers.builders

import kotlinx.datetime.LocalDate
import kotlinx.serialization.json.Json
import kotlinx.serialization.serializer
import org.mockito.Mockito.mock
import uk.gov.communities.prsdb.webapp.constants.enums.EpcExemptionReason
import uk.gov.communities.prsdb.webapp.constants.enums.LicensingType
import uk.gov.communities.prsdb.webapp.constants.enums.MeesExemptionReason
import uk.gov.communities.prsdb.webapp.constants.enums.OwnershipType
import uk.gov.communities.prsdb.webapp.constants.enums.PropertyType
import uk.gov.communities.prsdb.webapp.constants.enums.RentFrequency
import uk.gov.communities.prsdb.webapp.models.dataModels.AddressDataModel
import uk.gov.communities.prsdb.webapp.models.dataModels.EpcDataModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.CheckAnswersFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.OwnershipTypeFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.PropertyTypeFormModel
import uk.gov.communities.prsdb.webapp.services.LocalCouncilService
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.MockEpcData

class PropertyStateSessionBuilder(
    override val mockLocalCouncilService: LocalCouncilService = mock(),
) : JourneyStateSessionBuilder<PropertyStateSessionBuilder>(),
    AddressStateBuilder<PropertyStateSessionBuilder>,
    LicensingStateBuilder<PropertyStateSessionBuilder>,
    OccupancyStateBuilder<PropertyStateSessionBuilder>,
    GasSafetyStateBuilder<PropertyStateSessionBuilder>,
    ElectricalSafetyStateBuilder<PropertyStateSessionBuilder>,
    EpcStateBuilder<PropertyStateSessionBuilder>,
    JointLandlordsStateBuilder<PropertyStateSessionBuilder> {
    fun withIsAddressAlreadyRegistered(isRegistered: Boolean): PropertyStateSessionBuilder {
        additionalDataMap["isAddressAlreadyRegistered"] = Json.Default.encodeToString(serializer(), isRegistered)
        return this
    }

    fun withCheckedAnswers(): PropertyStateSessionBuilder {
        val checkAnswersFormModel = CheckAnswersFormModel()
        withSubmittedValue("check-answers", checkAnswersFormModel)
        return this
    }

    fun withPropertyType(
        type: PropertyType = PropertyType.DETACHED_HOUSE,
        customType: String = "type",
    ): PropertyStateSessionBuilder {
        val propertyTypeFormModel =
            PropertyTypeFormModel().apply {
                propertyType = type
                if (type == PropertyType.OTHER) {
                    customPropertyType = customType
                }
            }
        withSubmittedValue("property-type", propertyTypeFormModel)
        return this
    }

    fun withOwnershipType(ownershipType: OwnershipType = OwnershipType.FREEHOLD): PropertyStateSessionBuilder {
        val ownershipTypeFormModel =
            OwnershipTypeFormModel().apply {
                this.ownershipType = ownershipType
            }
        withSubmittedValue("ownership-type", ownershipTypeFormModel)
        return this
    }

    companion object {
        fun beforePropertyRegistrationSelectAddress(customLookedUpAddresses: List<AddressDataModel>? = null) =
            if (customLookedUpAddresses != null) {
                PropertyStateSessionBuilder()
                    .withLookupAddress()
                    .withCachedAddresses(customLookedUpAddresses)
            } else {
                PropertyStateSessionBuilder().withLookupAddress()
            }

        fun beforePropertyRegistrationManualAddress() = beforePropertyRegistrationSelectAddress().withManualAddressSelected()

        fun beforePropertyRegistrationSelectLocalCouncil() = beforePropertyRegistrationManualAddress().withManualAddress()

        fun beforePropertyRegistrationPropertyType() = PropertyStateSessionBuilder().withLookupAddress().withSelectedAddress()

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

        fun beforePropertyRegistrationBedrooms() = beforePropertyRegistrationPeople().withPeople()

        fun beforePropertyRegistrationRentIncludesBills() = beforePropertyRegistrationBedrooms().withBedrooms()

        fun beforePropertyRegistrationBillsIncluded() = beforePropertyRegistrationRentIncludesBills().withRentIncludesBills(true)

        fun beforePropertyRegistrationFurnished() = beforePropertyRegistrationBillsIncluded().withBillsIncluded()

        fun beforePropertyRegistrationRentFrequency() = beforePropertyRegistrationFurnished().withFurnished()

        fun beforePropertyRegistrationRentAmount(rentFrequency: RentFrequency) =
            beforePropertyRegistrationRentFrequency().withRentFrequency(
                rentFrequency,
            )

        fun beforePropertyRegistrationHasJointLandlords() = beforePropertyRegistrationRentAmount(RentFrequency.MONTHLY).withRentAmount()

        fun beforePropertyRegistrationInviteJointLandlords(alreadyInvitedEmails: MutableList<String>? = null) =
            if (alreadyInvitedEmails != null) {
                beforePropertyRegistrationHasJointLandlords().withHasJointLandlords(true).withInvitedJointLandlords(alreadyInvitedEmails)
            } else {
                beforePropertyRegistrationHasJointLandlords().withHasJointLandlords(true)
            }

        fun beforePropertyRegistrationHasGasSupply(propertyIsOccupied: Boolean = true) =
            beforePropertyRegistrationInviteJointLandlords()
                .withHasNoJointLandlords()
                .withOccupancyStatus(propertyIsOccupied)

        fun beforePropertyRegistrationHasGasCert() = beforePropertyRegistrationHasGasSupply().withGasSupply()

        fun beforePropertyRegistrationGasCertIssueDate() = beforePropertyRegistrationHasGasCert().withGasCertificate()

        fun beforePropertyRegistrationCheckGasSafetyAnswersNoGasSupply() = beforePropertyRegistrationHasGasSupply().withNoGasSupply()

        fun beforePropertyRegistrationCheckGasSafetyAnswersUploadedCert() =
            beforePropertyRegistrationHasGasCert()
                .withGasCertificate()
                .withGasCertIssueDate()
                .withGasCertUploads()

        fun beforePropertyRegistrationCheckGasSafetyAnswersProvideLater() = beforePropertyRegistrationHasGasCert().withProvideGasCertLater()

        fun beforePropertyRegistrationCheckGasSafetyAnswersNoCert() = beforePropertyRegistrationHasGasCert().withNoGasCertificate()

        fun beforePropertyRegistrationCheckGasSafetyAnswersCertExpired() =
            beforePropertyRegistrationHasGasCert()
                .withGasCertificate()
                .withGasCertIssueDate(issueDate = LocalDate(2020, 1, 1))
                .withGasCertUploads()
                .withGasCertExpiredAcknowledged()

        fun beforePropertyRegistrationHasElectricalCert() =
            beforePropertyRegistrationHasGasSupply().withGasSafetyTaskCompletedWithNoGasSupply()

        fun beforePropertyRegistrationEicExpiryDate() =
            beforePropertyRegistrationHasElectricalCert()
                .withEic()

        fun beforePropertyRegistrationHasEpc() =
            beforePropertyRegistrationHasElectricalCert()
                .withElectricalSafetyCertificateMissing()
                .withEpcNotFoundByUprn()

        fun beforePropertyRegistrationFindYourEpc(propertyIsOccupied: Boolean = true) =
            beforePropertyRegistrationHasElectricalCert()
                .withElectricalSafetyCertificateMissing()
                .withPropertyHasEpc()
                .withOccupancyStatus(propertyIsOccupied)

        fun beforePropertyRegistrationConfirmEpcDetailsRetrievedByCertificateNumber(
            epcDataModel: EpcDataModel = MockEpcData.createEpcDataModel(),
        ) = beforePropertyRegistrationFindYourEpc()
            .withFindYourEpc(epcDataModel)

        fun beforePropertyRegistrationIsEpcRequired() =
            beforePropertyRegistrationHasElectricalCert()
                .withElectricalSafetyCertificateMissing()
                .withPropertyHasNoEpc()

        fun beforePropertyRegistrationEpcExemption() = beforePropertyRegistrationIsEpcRequired().withIsEpcNotRequired()

        fun beforePropertyRegistrationConfirmEpcDetailsByUprn(epcDataModel: EpcDataModel = MockEpcData.createEpcDataModel()) =
            beforePropertyRegistrationHasElectricalCert()
                .withElectricalSafetyCertificateMissing()
                .withEpcRetrievedByUprn(epcDataModel)

        fun beforePropertyRegistrationProvideEpcLater(propertyIsOccupied: Boolean = true) =
            beforePropertyRegistrationHasElectricalCert()
                .withElectricalSafetyCertificateMissing()
                .withEpcProvideLater()
                .withOccupancyStatus(propertyIsOccupied)

        fun beforePropertyRegistrationEpcInDateAtStartOfTenancyCheck() =
            beforePropertyRegistrationHasElectricalCert()
                .withElectricalSafetyCertificateMissing()
                .withAcceptedEpcFoundByUprn(MockEpcData.createEpcDataModel(expiryDate = MockEpcData.expiryDateInThePast))
                .withOccupancyStatus(true)

        fun beforePropertyRegistrationHasMeesExemption() =
            beforePropertyRegistrationHasElectricalCert()
                .withElectricalSafetyCertificateMissing()
                .withEpcLowEnergyRating()

        fun beforePropertyRegistrationMeesExemptionReason() =
            beforePropertyRegistrationHasMeesExemption()
                .withHasMeesExemption(true)

        fun beforePropertyRegistrationEpcExpired(propertyIsOccupied: Boolean = true) =
            beforePropertyRegistrationFindYourEpc(propertyIsOccupied)
                .withEpcExpired()
                .apply { if (propertyIsOccupied) withEpcNotInDateAtStartOfTenancy() }

        fun beforePropertyRegistrationEpcMissing(propertyIsOccupied: Boolean = true) =
            beforePropertyRegistrationFindYourEpc(propertyIsOccupied)
                .withEpcMissing()

        fun beforePropertyRegistrationLowEnergyRating(propertyIsOccupied: Boolean = true) =
            beforePropertyRegistrationHasMeesExemption()
                .withHasMeesExemption(false)
                .withOccupancyStatus(propertyIsOccupied)

        fun beforePropertyRegistrationCheckEpcAnswersCompliantEpc() =
            beforePropertyRegistrationHasElectricalCert()
                .withElectricalSafetyCertificateMissing()
                .withAcceptedEpcFoundByUprn()
                .withOccupancyStatus(true)

        fun beforePropertyRegistrationCheckEpcAnswersLowRatingWithExemption(
            exemptionReason: MeesExemptionReason = MeesExemptionReason.HIGH_COST,
        ) = beforePropertyRegistrationHasElectricalCert()
            .withElectricalSafetyCertificateMissing()
            .withEpcLowEnergyRating()
            .withHasMeesExemption(true)
            .withMeesExemptionReason(exemptionReason)

        fun beforePropertyRegistrationCheckEpcAnswersExpiredEpcInDateAtTenancyStart() =
            beforePropertyRegistrationHasElectricalCert()
                .withElectricalSafetyCertificateMissing()
                .withAcceptedEpcFoundByUprn(MockEpcData.createEpcDataModel(expiryDate = MockEpcData.expiryDateInThePast))
                .withOccupancyStatus(true)
                .withEpcInDateAtTenancyStart(true)

        fun beforePropertyRegistrationCheckEpcAnswersExpiredEpcLowRatingWithExemption(
            exemptionReason: MeesExemptionReason = MeesExemptionReason.HIGH_COST,
        ) = beforePropertyRegistrationHasElectricalCert()
            .withElectricalSafetyCertificateMissing()
            .withAcceptedEpcFoundByUprn(
                MockEpcData.createEpcDataModel(
                    expiryDate = MockEpcData.expiryDateInThePast,
                    energyRating = "F",
                ),
            ).withOccupancyStatus(true)
            .withEpcInDateAtTenancyStart(true)
            .withHasMeesExemption(true)
            .withMeesExemptionReason(exemptionReason)

        fun beforePropertyRegistrationCheckEpcAnswersProvideLaterOccupied() =
            beforePropertyRegistrationHasElectricalCert()
                .withElectricalSafetyCertificateMissing()
                .withEpcProvideLater()
                .withOccupancyStatus(true)
                .withProvideEpcLaterComplete()

        fun beforePropertyRegistrationCheckEpcAnswersProvideLaterUnoccupied() =
            beforePropertyRegistrationHasElectricalCert()
                .withElectricalSafetyCertificateMissing()
                .withEpcProvideLater()
                .withOccupancyStatus(false)
                .withProvideEpcLaterComplete()

        fun beforePropertyRegistrationCheckEpcAnswersLowRatingNoExemptionOccupied() =
            beforePropertyRegistrationHasElectricalCert()
                .withElectricalSafetyCertificateMissing()
                .withEpcLowEnergyRating()
                .withHasMeesExemption(false)
                .withOccupancyStatus(true)
                .withLowEnergyRatingComplete()

        fun beforePropertyRegistrationCheckEpcAnswersLowRatingNoExemptionUnoccupied() =
            beforePropertyRegistrationHasElectricalCert()
                .withElectricalSafetyCertificateMissing()
                .withEpcLowEnergyRating()
                .withHasMeesExemption(false)
                .withOccupancyStatus(false)
                .withLowEnergyRatingComplete()

        fun beforePropertyRegistrationCheckEpcAnswersNoEpcExempt(
            exemptionReason: EpcExemptionReason = EpcExemptionReason.TEMPORARY_BUILDING,
        ) = beforePropertyRegistrationHasElectricalCert()
            .withElectricalSafetyCertificateMissing()
            .withPropertyHasNoEpc()
            .withIsEpcNotRequired()
            .withEpcExemptionReason(exemptionReason)

        fun beforePropertyRegistrationCheckEpcAnswersNoEpcOccupiedNotExempt() =
            beforePropertyRegistrationHasElectricalCert()
                .withElectricalSafetyCertificateMissing()
                .withEpcMissing()
                .withOccupancyStatus(true)
                .withEpcMissingComplete()

        fun beforePropertyRegistrationCheckAnswers() =
            beforePropertyRegistrationOccupancy()
                .withOccupancyStatus(false)
                .withHasNoJointLandlords()
                .withGasSafetyTaskCompletedWithNoGasSupply()
                .withElectricalSafetyCertificateMissing()
                .withCompliantEpc()

        fun beforePropertyRegistrationDeclaration() = beforePropertyRegistrationCheckAnswers().withCheckedAnswers()
    }
}
