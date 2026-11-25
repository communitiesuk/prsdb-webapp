package uk.gov.communities.prsdb.webapp.testHelpers.builders

import kotlinx.serialization.json.Json
import kotlinx.serialization.serializer
import uk.gov.communities.prsdb.webapp.constants.enums.EpcExemptionReason
import uk.gov.communities.prsdb.webapp.constants.enums.LicensingType
import uk.gov.communities.prsdb.webapp.constants.enums.MeesExemptionReason
import uk.gov.communities.prsdb.webapp.journeys.propertyReactivation.steps.ReactivationCheckAnswersStep
import uk.gov.communities.prsdb.webapp.journeys.propertyReactivation.steps.ReactivationInfoStep
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.CheckAnswersFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.NoInputFormModel
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.MockEpcData

class PropertyReactivationStateSessionBuilder :
    JourneyStateSessionBuilder<PropertyReactivationStateSessionBuilder>(),
    GasSafetyStateBuilder<PropertyReactivationStateSessionBuilder>,
    ElectricalSafetyStateBuilder<PropertyReactivationStateSessionBuilder>,
    EpcStateBuilder<PropertyReactivationStateSessionBuilder>,
    LicensingStateBuilder<PropertyReactivationStateSessionBuilder>,
    OccupancyStateBuilder<PropertyReactivationStateSessionBuilder> {
    fun withInfoCompleted(): PropertyReactivationStateSessionBuilder {
        withSubmittedValue(ReactivationInfoStep.ROUTE_SEGMENT, NoInputFormModel())
        return self()
    }

    fun withIsPropertyAlreadyRegistered(isRegistered: Boolean): PropertyReactivationStateSessionBuilder {
        withAdditionalData("isPropertyAlreadyRegistered", Json.Default.encodeToString(serializer(), isRegistered))
        return self()
    }

    fun withCheckAnswersSubmitted(): PropertyReactivationStateSessionBuilder {
        withSubmittedValue(ReactivationCheckAnswersStep.ROUTE_SEGMENT, CheckAnswersFormModel())
        return self()
    }

    companion object {
        fun beforeCheckAnswers() = beforeCheckAnswersWith()

        fun beforeConfirmMissingCompliance() = beforeCheckAnswers().withCheckAnswersSubmitted()

        fun beforeCheckAnswersGasCertUploaded() = beforeCheckAnswersWith(gasSafety = { withGasSafetyTaskCompletedWithUploadedCert() })

        fun beforeCheckAnswersEicUploaded() =
            beforeCheckAnswersWith(
                electricalSafety = { withEic().withEicIssueDate().withEicUploads().withCheckElectricalSafetyAnswersComplete() },
                epc = { withNoEpcExempt() },
            )

        // TODO PDJB-1679: Remove this when the EIC_ISSUE_DATE flag is removed
        fun beforeCheckAnswersEicWithExpiryDateUploaded() =
            beforeCheckAnswersWith(
                electricalSafety = { withEic().withEicExpiryDate().withEicUploads().withCheckElectricalSafetyAnswersComplete() },
                epc = { withNoEpcExempt() },
            )

        fun beforeCheckAnswersEicrUploaded() =
            beforeCheckAnswersWith(
                electricalSafety = { withEicr().withEicrExpiryDate().withEicrUploads().withCheckElectricalSafetyAnswersComplete() },
                epc = { withNoEpcExempt() },
            )

        fun beforeCheckAnswersEpcExpiredInDateAtTenancyStart() =
            beforeCheckAnswersWith(
                epc = {
                    withAcceptedEpcFoundByUprn(MockEpcData.createEpcDataModel(expiryDate = MockEpcData.expiryDateInThePast))
                        .withEpcInDateAtTenancyStart(true)
                        .withCheckEpcAnswersComplete()
                },
            )

        fun beforeCheckAnswersNoEpcExempt() = beforeCheckAnswersWith(epc = { withNoEpcExempt() })

        fun beforeCheckAnswersEpcLowRatingWithExemption() =
            beforeCheckAnswersWith(
                epc = {
                    withEpcLowEnergyRating()
                        .withHasMeesExemption(true)
                        .withMeesExemptionReason(MeesExemptionReason.HIGH_COST)
                        .withCheckEpcAnswersComplete()
                },
            )

        fun beforeCheckAnswersEpcFoundByCertificateNumber() =
            beforeCheckAnswersWith(epc = { withAcceptedEpcFoundByCertificateNumber().withCheckEpcAnswersComplete() })

        fun beforeCheckAnswersRentIncludesBills() = beforeCheckAnswersWith(rentIncludesBills = true)

        private fun PropertyReactivationStateSessionBuilder.withNoEpcExempt() =
            withPropertyHasNoEpc()
                .withIsEpcNotRequired()
                .withEpcExemptionReason(EpcExemptionReason.TEMPORARY_BUILDING)
                .withCheckEpcAnswersComplete()

        private fun beforeCheckAnswersWith(
            gasSafety: PropertyReactivationStateSessionBuilder.() -> Unit = { withGasSafetyTaskCompletedWithNoGasSupply() },
            electricalSafety: PropertyReactivationStateSessionBuilder.() -> Unit = { withElectricalSafetyCertificateMissing() },
            epc: PropertyReactivationStateSessionBuilder.() -> Unit = { withCompliantEpc() },
            rentIncludesBills: Boolean = false,
        ) = PropertyReactivationStateSessionBuilder()
            .withInfoCompleted()
            .withIsPropertyAlreadyRegistered(false)
            .withLicensing(LicensingType.SELECTIVE_LICENCE, "SL-12345")
            .apply(gasSafety)
            .apply(electricalSafety)
            .apply(epc)
            .withHouseholds(2)
            .withPeople(4)
            .withRentIncludesBills(rentIncludesBills)
            .apply { if (rentIncludesBills) withBillsIncluded() }
            .withFurnished()
            .withRentFrequency()
            .withRentAmount("400")
    }
}
