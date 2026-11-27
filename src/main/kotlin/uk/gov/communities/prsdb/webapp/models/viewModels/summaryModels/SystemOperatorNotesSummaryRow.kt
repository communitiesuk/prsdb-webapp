package uk.gov.communities.prsdb.webapp.models.viewModels.summaryModels

import uk.gov.communities.prsdb.webapp.helpers.extensions.addRow

fun MutableList<SummaryListRowViewModel>.addSystemOperatorNotesRow(
    notes: String?,
    changeLink: String,
) {
    addRow(
        key = "systemOperator.notes.key",
        value = notes?.lines() ?: "systemOperator.notes.notAdded",
        actionText = "forms.links.change",
        actionLink = changeLink,
    )
}
