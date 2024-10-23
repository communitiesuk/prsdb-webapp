package uk.gov.communities.prsdb.webapp.models.journeyModels

import uk.gov.communities.prsdb.webapp.database.entity.FormContext
import uk.gov.communities.prsdb.webapp.services.JourneyService

class Journey(
    val journeyService: JourneyService,
    val steps: List<JourneyStep>,
    val previousStep: JourneyStep,
) {
    fun resolveNext(
        formContext: FormContext,
        currentStep: JourneyStep,
    ): JourneyStep {
        currentStep.nextStep(formContext.context)
        // TODO remove default return
        return currentStep
    }

    // TODO add something?? to iterate through collection of Steps?

    // TODO getStep (equivalent of journeyService.getJourneyView)
    // validates journey yp to requested step
    // returns populated view model and template name
    // updates previousStep

    // TODO makes calls to Journey Service methods??? ::
    // To validate form input
    // Either returns validated and update context to controller to update session OR updates session
    // getRedirectForNextStep (resolve next function (arrow function))

//  TODO Consider if we want breadcrumbs here - how are we keeping track of previous step/are we?
}

class JourneyStep(
    val id: Int,
    val page: JourneyPage,
) {
    // TODO id? yes/no

    fun nextStep(context: String) {
        // TODO take context do some logic return next step
    }
}

class JourneyPage(
    val dto: JourneyPageDTO,
    val contentKeys: String,
) {
//    TODO add isValid function to run validation on DTO based on form context (maybe this will be on the DTO?)
}

class JourneyPageDTO(
    val formFields: List<String>,
    val annotations: List<String>,
)
