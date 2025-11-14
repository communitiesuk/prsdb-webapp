package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration

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
}
