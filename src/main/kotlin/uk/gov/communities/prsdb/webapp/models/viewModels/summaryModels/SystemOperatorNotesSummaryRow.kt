package uk.gov.communities.prsdb.webapp.models.viewModels.summaryModels

import uk.gov.communities.prsdb.webapp.controllers.UpdateSystemOperatorLandlordNotesController
import uk.gov.communities.prsdb.webapp.database.entity.Landlord
import uk.gov.communities.prsdb.webapp.helpers.extensions.addRow

fun MutableList<SummaryListRowViewModel>.addSystemOperatorNotesRow(landlord: Landlord) {
    val notes = landlord.systemOperatorNotes
    addRow(
        key = "landlordDetails.systemOperatorNotes.key",
        value = notes?.lines() ?: "landlordDetails.systemOperatorNotes.notAdded",
        actionText = "forms.links.change",
        actionLink = UpdateSystemOperatorLandlordNotesController.getUpdateNotesStepPath(landlord.id),
    )
}
