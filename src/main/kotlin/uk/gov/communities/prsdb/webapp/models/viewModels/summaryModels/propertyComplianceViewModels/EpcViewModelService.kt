package uk.gov.communities.prsdb.webapp.models.viewModels.summaryModels.propertyComplianceViewModels

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.PrsdbFlip
import uk.gov.communities.prsdb.webapp.constants.COMPLIANCE_ACTIONS_MAY2026_REDESIGN
import uk.gov.communities.prsdb.webapp.database.entity.PropertyCompliance
import uk.gov.communities.prsdb.webapp.models.viewModels.summaryModels.SummaryCardSupplementarySection
import uk.gov.communities.prsdb.webapp.models.viewModels.summaryModels.SummaryListRowViewModel

interface EpcViewModelService {
    @PrsdbFlip(name = COMPLIANCE_ACTIONS_MAY2026_REDESIGN, alterBean = "epcViewModelServiceRedesign")
    fun getInsetTextKey(propertyCompliance: PropertyCompliance): String?

    @PrsdbFlip(name = COMPLIANCE_ACTIONS_MAY2026_REDESIGN, alterBean = "epcViewModelServiceRedesign")
    fun getInsetTextHtml(propertyCompliance: PropertyCompliance): String?

    @PrsdbFlip(name = COMPLIANCE_ACTIONS_MAY2026_REDESIGN, alterBean = "epcViewModelServiceRedesign")
    fun getSupplementarySections(propertyCompliance: PropertyCompliance): List<SummaryCardSupplementarySection>

    @PrsdbFlip(name = COMPLIANCE_ACTIONS_MAY2026_REDESIGN, alterBean = "epcViewModelServiceRedesign")
    fun fromEntity(propertyCompliance: PropertyCompliance): List<SummaryListRowViewModel>
}
