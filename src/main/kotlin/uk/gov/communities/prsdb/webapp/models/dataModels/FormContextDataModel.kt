package uk.gov.communities.prsdb.webapp.models.dataModels

import uk.gov.communities.prsdb.webapp.enums.JourneyType

data class FormContextDataModel(
    var id: Long,
    var journeyType: JourneyType,
    var context: String,
    var userId: String,
)
