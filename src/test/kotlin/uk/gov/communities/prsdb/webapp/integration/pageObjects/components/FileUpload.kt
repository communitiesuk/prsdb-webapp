package uk.gov.communities.prsdb.webapp.integration.pageObjects.components

import com.microsoft.playwright.Locator
import com.microsoft.playwright.Page
import kotlin.io.path.Path

class FileUpload(
    parentLocator: Locator,
) : BaseComponent(parentLocator.locator("input[type='file']")) {
    constructor(page: Page) : this(page.locator("html"))

    fun stageFile(filePath: String) {
        locator.setInputFiles(Path(filePath))
    }
}
