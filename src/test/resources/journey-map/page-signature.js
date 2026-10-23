() => {
    const main = document.querySelector("main") || document.body;
    const copy = main.cloneNode(true);
    copy.querySelectorAll("input[type=hidden], script, style, [data-journey-map-ignore]").forEach((element) => element.remove());
    copy.querySelectorAll("textarea, .govuk-summary-list__value").forEach((element) => {
        element.textContent = "";
    });

    const months = "January|February|March|April|May|June|July|August|September|October|November|December";
    const text = (copy.textContent || "")
        .replace(/\s+/g, " ")
        .replace(new RegExp(`\\b\\d{1,2} (${months}) \\d{4}\\b`, "g"), "<date>")
        .replace(/\b\d{1,2}\/\d{1,2}\/\d{2,4}\b/g, "<date>")
        .replace(/\b[A-Z]{1,3}-[A-Z0-9]{4}-[A-Z0-9]{4}\b/g, "<reference>")
        .trim();

    const choiceTypes = ["radio", "checkbox", "submit", "button"];
    const controls = Array.from(copy.querySelectorAll("input, select, textarea, button"))
        .map((element) => {
            const type = (element.getAttribute("type") || "").toLowerCase();
            const keepsValue = element.tagName === "BUTTON" || choiceTypes.includes(type);
            return [element.tagName, type, element.getAttribute("name") || "", keepsValue ? element.getAttribute("value") || "" : ""].join(":");
        })
        .join("|");

    return `${text}#${controls}`;
}
