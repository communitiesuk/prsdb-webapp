package uk.gov.communities.prsdb.webapp.models.viewModels.summaryModels.propertyComplianceViewModels

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.PrsdbFlip
import uk.gov.communities.prsdb.webapp.constants.COMPLIANCE_ACTIONS_MAY2026_REDESIGN
import uk.gov.communities.prsdb.webapp.database.entity.PropertyCompliance
import uk.gov.communities.prsdb.webapp.models.viewModels.summaryModels.SummaryListRowViewModel

interface ElectricalSafetyViewModelService {
    @PrsdbFlip(name = COMPLIANCE_ACTIONS_MAY2026_REDESIGN, alterBean = "electricalSafetyViewModelServiceRedesign")
    fun getInsetTextKey(propertyCompliance: PropertyCompliance): String?

    @PrsdbFlip(name = COMPLIANCE_ACTIONS_MAY2026_REDESIGN, alterBean = "electricalSafetyViewModelServiceRedesign")
    fun fromEntity(propertyCompliance: PropertyCompliance): List<SummaryListRowViewModel>
}
