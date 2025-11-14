package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.tasks

import org.springframework.context.annotation.Scope
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.PrsdbWebComponent
import uk.gov.communities.prsdb.webapp.journeys.OrParents
import uk.gov.communities.prsdb.webapp.journeys.Task
import uk.gov.communities.prsdb.webapp.journeys.hasOutcome
import uk.gov.communities.prsdb.webapp.journeys.isComplete
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.LicensingState
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.LicensingTypeMode

@PrsdbWebComponent
@Scope("prototype")
class LicensingTask : Task<LicensingState>() {
    override fun makeSubJourney(state: LicensingState) =
        subJourney(state) {
            startingStep("licensing-type", journey.licensingTypeStep) {
                nextStep { mode ->
                    when (mode) {
                        LicensingTypeMode.SELECTIVE_LICENCE -> journey.selectiveLicenceStep
                        LicensingTypeMode.HMO_MANDATORY_LICENCE -> journey.hmoMandatoryLicenceStep
                        LicensingTypeMode.HMO_ADDITIONAL_LICENCE -> journey.hmoAdditionalLicenceStep
                        LicensingTypeMode.NO_LICENSING -> exitStep
                    }
                }
            }
            step("selective-licence", journey.selectiveLicenceStep) {
                parents { journey.licensingTypeStep.hasOutcome(LicensingTypeMode.SELECTIVE_LICENCE) }
                nextStep { exitStep }
            }
            step("hmo-mandatory-licence", journey.hmoMandatoryLicenceStep) {
                parents { journey.licensingTypeStep.hasOutcome(LicensingTypeMode.HMO_MANDATORY_LICENCE) }
                nextStep { exitStep }
            }
            step("hmo-additional-licence", journey.hmoAdditionalLicenceStep) {
                parents { journey.licensingTypeStep.hasOutcome(LicensingTypeMode.HMO_ADDITIONAL_LICENCE) }
                nextStep { exitStep }
            }
            exitStep {
                parents {
                    OrParents(
                        journey.licensingTypeStep.hasOutcome(LicensingTypeMode.NO_LICENSING),
                        journey.selectiveLicenceStep.isComplete(),
                        journey.hmoMandatoryLicenceStep.isComplete(),
                        journey.hmoAdditionalLicenceStep.isComplete(),
                    )
                }
            }
        }
}
