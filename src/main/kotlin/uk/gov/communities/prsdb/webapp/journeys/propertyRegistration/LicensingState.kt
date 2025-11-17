package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration

import uk.gov.communities.prsdb.webapp.constants.enums.LicensingType
import uk.gov.communities.prsdb.webapp.exceptions.NotNullFormModelValueIsNullException
import uk.gov.communities.prsdb.webapp.journeys.JourneyState
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.HmoAdditionalLicenceStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.HmoMandatoryLicenceStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.LicensingTypeStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.SelectiveLicenceStep

interface LicensingState : JourneyState {
    val licensingTypeStep: LicensingTypeStep
    val selectiveLicenceStep: SelectiveLicenceStep
    val hmoMandatoryLicenceStep: HmoMandatoryLicenceStep
    val hmoAdditionalLicenceStep: HmoAdditionalLicenceStep

    fun getLicenceNumberOrNull(): String? =
        when (licensingTypeStep.formModelOrNull?.licensingType) {
            LicensingType.SELECTIVE_LICENCE -> selectiveLicenceStep.formModelOrNull?.licenceNumber
            LicensingType.HMO_MANDATORY_LICENCE -> hmoMandatoryLicenceStep.formModelOrNull?.licenceNumber
            LicensingType.HMO_ADDITIONAL_LICENCE -> hmoAdditionalLicenceStep.formModelOrNull?.licenceNumber
            else -> null
        }

    fun getLicenceNumber(): String =
        getLicenceNumberOrNull() ?: throw NotNullFormModelValueIsNullException("No Licence number found in LicensingState")
}
