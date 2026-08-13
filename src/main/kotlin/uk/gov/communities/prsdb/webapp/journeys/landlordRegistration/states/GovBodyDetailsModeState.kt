package uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.states

import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.OrgGovBodyDetailsMode

// Hoisted answer of the organisation governing body details question. Held on the outer task's state so the
// answer is a proper piece of journey state rather than living only in the details step's own form data. This
// lets the Companies House change CYA interruption screen record "has details" centrally (it stands in for the
// details step on that flow), instead of writing form-model-shaped data into the details step's data key.
interface GovBodyDetailsModeState {
    var orgGovBodyDetailsMode: OrgGovBodyDetailsMode?
}
