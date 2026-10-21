package uk.gov.communities.prsdb.webapp.integration.pageObjects.components

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.journeys.shared.states.CheckYourAnswersJourneyState.Companion.CYA_SNAPSHOT_DELEGATE_KEY_PREFIX
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.CheckAnswersFormModel

class CyaAnswersSnapshot(
    page: Page,
) : BaseComponent(page.locator("input[name='submittedFilteredJourneyData']")) {
    val delegateKeys: Set<String>
        get() =
            CheckAnswersFormModel
                .deserializeJourneyDataKeys(locator.inputValue())
                .filter { it.startsWith(CYA_SNAPSHOT_DELEGATE_KEY_PREFIX) }
                .map { it.removePrefix(CYA_SNAPSHOT_DELEGATE_KEY_PREFIX) }
                .toSortedSet()
}
