package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.states

import uk.gov.communities.prsdb.webapp.constants.enums.LicensingType
import uk.gov.communities.prsdb.webapp.journeys.JourneyState
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.HmoAdditionalLicenceStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.HmoMandatoryLicenceStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.LicensingTypeMode
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.LicensingTypeStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.ProvideLicensingLaterStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.SelectiveLicenceStep

interface LicensingState : JourneyState {
    val allowProvideLicensingLaterRoute: Boolean
    val isOccupied: Boolean?

    val licensingTypeStep: LicensingTypeStep
    val selectiveLicenceStep: SelectiveLicenceStep
    val hmoMandatoryLicenceStep: HmoMandatoryLicenceStep
    val hmoAdditionalLicenceStep: HmoAdditionalLicenceStep
    val provideLicensingLaterStep: ProvideLicensingLaterStep

    fun getLicenceNumberOrNull(): String? =
        when (licensingTypeStep.formModelOrNull?.licensingType) {
            LicensingType.SELECTIVE_LICENCE -> selectiveLicenceStep.formModelOrNull?.licenceNumber
            LicensingType.HMO_MANDATORY_LICENCE -> hmoMandatoryLicenceStep.formModelOrNull?.licenceNumber
            LicensingType.HMO_ADDITIONAL_LICENCE -> hmoAdditionalLicenceStep.formModelOrNull?.licenceNumber
            else -> null
        }

    fun getLicenceNumber(): String =
        getLicenceNumberOrNull() ?: throw IllegalStateException("Licence number is not available for the current licensing type")

    fun getLicensingType(): LicensingType =
        when (val outcome = licensingTypeStep.outcome) {
            LicensingTypeMode.SELECTIVE_LICENCE -> LicensingType.SELECTIVE_LICENCE
            LicensingTypeMode.HMO_MANDATORY_LICENCE -> LicensingType.HMO_MANDATORY_LICENCE
            LicensingTypeMode.HMO_ADDITIONAL_LICENCE -> LicensingType.HMO_ADDITIONAL_LICENCE
            LicensingTypeMode.NO_LICENSING -> LicensingType.NO_LICENSING
            LicensingTypeMode.PROVIDE_LATER -> LicensingType.PROVIDE_LATER
            null -> throw IllegalStateException("Licensing type has not been provided")
        }
}
