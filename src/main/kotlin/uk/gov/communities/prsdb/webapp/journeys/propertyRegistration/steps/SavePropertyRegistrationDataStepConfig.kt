package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps

import jakarta.persistence.EntityExistsException
import kotlinx.datetime.toJavaLocalDate
import org.springframework.security.core.context.SecurityContextHolder
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.config.managers.FeatureFlagManager
import uk.gov.communities.prsdb.webapp.constants.PROPERTY_REGISTRATION_RESTRUCTURE_AND_SKIPPING
import uk.gov.communities.prsdb.webapp.constants.enums.PropertyType
import uk.gov.communities.prsdb.webapp.exceptions.NotNullFormModelValueIsNullException.Companion.notNullValue
import uk.gov.communities.prsdb.webapp.journeys.AbstractInternalStepConfig
import uk.gov.communities.prsdb.webapp.journeys.Destination
import uk.gov.communities.prsdb.webapp.journeys.JourneyStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.PropertyRegistrationJourneyState
import uk.gov.communities.prsdb.webapp.journeys.shared.Complete
import uk.gov.communities.prsdb.webapp.journeys.shared.YesOrNo
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.LicensingTypeFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.NewNumberOfPeopleFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.NumberOfBedroomsFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.NumberOfHouseholdsFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.OccupancyFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.OwnershipTypeFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.PropertyTypeFormModel
import uk.gov.communities.prsdb.webapp.services.EpcCertificateUrlProvider
import uk.gov.communities.prsdb.webapp.services.PropertyRegistrationService

@JourneyFrameworkComponent
class SavePropertyRegistrationDataStepConfig(
    private val propertyRegistrationService: PropertyRegistrationService,
    private val epcCertificateUrlProvider: EpcCertificateUrlProvider,
    private val featureFlagManager: FeatureFlagManager,
) : AbstractInternalStepConfig<Complete, PropertyRegistrationJourneyState>() {
    override fun mode(state: PropertyRegistrationJourneyState): Complete = Complete.COMPLETE

    override fun afterStepIsReached(state: PropertyRegistrationJourneyState) {
        try {
            registerProperty(state)
        } catch (_: EntityExistsException) {
            state.propertyDetailsTask.addressTask.isAddressAlreadyRegistered = true
            return
        }
    }

    override fun resolveNextDestination(
        state: PropertyRegistrationJourneyState,
        defaultDestination: Destination,
    ): Destination =
        if (state.propertyDetailsTask.addressTask.isAddressAlreadyRegistered == true) {
            Destination(state.propertyDetailsTask.addressTask.alreadyRegisteredStep)
        } else {
            state.deleteJourney()
            defaultDestination
        }

    private fun registerProperty(state: PropertyRegistrationJourneyState) {
        val isOccupied = state.occupied.formModel.notNullValue(OccupancyFormModel::occupied)
        val isRestructured = featureFlagManager.checkFeature(PROPERTY_REGISTRATION_RESTRUCTURE_AND_SKIPPING)
        val shouldRequireTenancyDetails = isOccupied && !state.provideTenancyDetailsLater
        val billsIncludedDataModel = state.rentIncludesBillsTask.getBillsIncludedOrNull()
        val jointLandlordsTask = state.ownershipAndLandlordsTask.jointLandlordsTask
        val jointLandlordEmails: List<String>? =
            jointLandlordsTask.inviteJointLandlordsTask.invitedJointLandlordEmailsMap
                ?.values
                ?.toList()
        val markedJointLandlord = jointLandlordsTask.hasJointLandlordsStep.formModel.hasJointLandlords == true

        propertyRegistrationService.registerProperty(
            addressModel = state.propertyDetailsTask.addressTask.getAddress(),
            propertyType = state.propertyDetailsTask.propertyTypeStep.formModel.notNullValue(PropertyTypeFormModel::propertyType),
            customPropertyType =
                if (state.propertyDetailsTask.propertyTypeStep.formModel.propertyType == PropertyType.OTHER) {
                    state.propertyDetailsTask.propertyTypeStep.formModel.customPropertyType
                } else {
                    null
                },
            licenseType = state.licensingTask.licensingTypeStep.formModel.notNullValue(LicensingTypeFormModel::licensingType),
            licenceNumber = state.licensingTask.getLicenceNumberOrNull() ?: "",
            ownershipType = state.ownershipAndLandlordsTask.ownershipTypeStep.formModel.notNullValue(OwnershipTypeFormModel::ownershipType),
            isOccupied = isOccupied,
            numberOfHouseholds =
                if (shouldRequireTenancyDetails) {
                    state.householdsAndTenantsTask.households.formModel
                        .notNullValue(NumberOfHouseholdsFormModel::numberOfHouseholds)
                        .toInt()
                } else {
                    0
                },
            numberOfPeople =
                if (shouldRequireTenancyDetails) {
                    state.householdsAndTenantsTask.tenants.formModel
                        .notNullValue(NewNumberOfPeopleFormModel::numberOfPeople)
                        .toInt()
                } else {
                    0
                },
            numBedrooms =
                if (isRestructured || shouldRequireTenancyDetails) {
                    state.bedrooms.formModel
                        .notNullValue(NumberOfBedroomsFormModel::numberOfBedrooms)
                        .toInt()
                } else {
                    null
                },
            billsIncludedList = if (shouldRequireTenancyDetails) billsIncludedDataModel?.standardBillsIncludedListAsString else null,
            customBillsIncluded = if (shouldRequireTenancyDetails) billsIncludedDataModel?.customBillsIncluded else null,
            furnishedStatus = if (shouldRequireTenancyDetails) state.furnishedStatus.formModel.furnishedStatus else null,
            rentFrequency =
                if (shouldRequireTenancyDetails) {
                    state.rentFrequencyAndAmountTask.rentFrequency.formModel.rentFrequency
                } else {
                    null
                },
            customRentFrequency =
                if (shouldRequireTenancyDetails) {
                    state.rentFrequencyAndAmountTask.getCustomRentFrequencyIfSelected()
                } else {
                    null
                },
            rentAmount =
                if (shouldRequireTenancyDetails) {
                    state.rentFrequencyAndAmountTask.rentAmount.formModel.rentAmount
                        .toBigDecimal()
                } else {
                    null
                },
            baseUserId = SecurityContextHolder.getContext().authentication.name,
            jointLandlordEmails = jointLandlordEmails,
            markedJointLandlord = markedJointLandlord,
            hasGasSupply = state.hasGasSupplyStep.outcome == YesOrNo.YES,
            gasSafetyCertIssueDate = state.getGasSafetyCertificateIssueDateIfReachable()?.toJavaLocalDate(),
            gasSafetyFileUploadIds = state.gasUploadIds,
            gasSafetyCertProvideLater = state.hasGasCertStep.outcome == HasGasCertMode.PROVIDE_THIS_LATER,
            electricalSafetyFileUploadIds = state.electricalUploadIds,
            electricalSafetyExpiryDate = state.getElectricalCertificateExpiryDateIfReachable()?.toJavaLocalDate(),
            electricalCertType = state.mapElectricalCertificateTypeToGlobalCertificateType(),
            electricalSafetyCertProvideLater = state.hasElectricalCertStep.outcome == HasElectricalCertMode.PROVIDE_THIS_LATER,
            epcCertificateUrl =
                state.acceptedEpcIfStillAccepted?.let {
                    epcCertificateUrlProvider.getEpcCertificateUrl(it.certificateNumber)
                },
            epcExpiryDate = state.acceptedEpcIfStillAccepted?.expiryDateAsJavaLocalDate,
            epcEnergyRating = state.acceptedEpcIfStillAccepted?.energyRating,
            tenancyStartedBeforeEpcExpiry =
                state.epcInDateAtStartOfTenancyCheckStep
                    .formModelIfReachableOrNull
                    ?.tenancyStartedBeforeExpiry,
            epcExemptionReason =
                state.epcExemptionStep
                    .formModelIfReachableOrNull
                    ?.exemptionReason,
            epcMeesExemptionReason =
                state.meesExemptionStep
                    .formModelIfReachableOrNull
                    ?.exemptionReason,
            epcProvideLater = state.hasEpcStep.outcome == HasEpcMode.PROVIDE_LATER,
            tenancyProvideLater = state.provideTenancyDetailsLater,
        )
    }
}

@JourneyFrameworkComponent
class SavePropertyRegistrationDataStep(
    stepConfig: SavePropertyRegistrationDataStepConfig,
) : JourneyStep.InternalStep<Complete, PropertyRegistrationJourneyState>(stepConfig)
