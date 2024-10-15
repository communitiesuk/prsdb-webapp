package uk.gov.communities.prsdb.webapp.formJourneys.journeys

import uk.gov.communities.prsdb.webapp.constants.enums.JourneyType
import uk.gov.communities.prsdb.webapp.database.entity.FormContext

class Journey(val journeyType: JourneyType) {

  val formContext: FormContext = FormContext()

  var previousStep: String? = null

//  This will have the next function (that gets the next step for a page based on previous step, form context and journey type)

}