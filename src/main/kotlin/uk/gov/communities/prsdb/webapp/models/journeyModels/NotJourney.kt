package uk.gov.communities.prsdb.webapp.models.journeyModels

open class NotJourney(
    val steps: List<JourneyStep>,
) {
    open lateinit var previousStep: JourneyStep

    fun resolveNext(
        context: String,
        currentStep: JourneyStep,
    ): JourneyStep {
        val nextStep = currentStep.nextStep(context)
        return currentStep
    }

    fun validateFormContextForStep(
        journeyStep: uk.gov.communities.prsdb.webapp.constants.enums.JourneyStep,
        context: String,
    ): Boolean {
        // TODO get step
        // validate context rules for step
        return true
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

open class JourneyStep(
    val id: Int,
    val name: String,
    val page: JourneyPage,
) {
    // TODO id? yes/no

    open fun nextStep(context: String): Int {
        // TodO this should iterate through journey.steps - getting the next one unless there are conditions for which is the next step
        return id + 1
    }
}

open class JourneyPage(
    val dto: JourneyPageDTO,
    val contentKeys: String,
) {
    open fun isValid(formData: String): Boolean {
        //    TODO add isValid function to run validation on DTO based on form context - e.g. if phonenumber validate by phone number, this will use bean  annotation
        return true
    }
}

open class JourneyPageDTO(
    val formFields: List<String>,
    val annotations: List<String>,
)
