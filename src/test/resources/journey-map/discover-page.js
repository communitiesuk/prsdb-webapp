() => {
    const clean = (text) => (text || "").replace(/\s+/g, " ").trim();
    const main = document.querySelector("main") || document.body;
    const form = Array.from(main.querySelectorAll("form")).find((candidate) => (candidate.getAttribute("method") || "").toLowerCase() === "post");

    const labelFor = (element) => {
        if (!element.id) return "";
        const label = document.querySelector(`label[for="${CSS.escape(element.id)}"]`);
        return label ? clean(label.textContent) : "";
    };

    const result = {
        url: window.location.href,
        title: clean(document.title),
        h1: clean((document.querySelector("h1") || {}).textContent),
        hasForm: Boolean(form),
        hasTaskList: Boolean(main.querySelector(".govuk-task-list")),
        action: form ? new URL(form.getAttribute("action") || "", window.location.href).href : null,
        enctype: form ? form.getAttribute("enctype") : null,
        fields: [],
        buttons: [],
        errors: [],
        changeLinks: [],
        links: [],
        contentLinks: [],
    };

    if (form) {
        form.querySelectorAll("input, select, textarea").forEach((element) => {
            const tag = element.tagName.toLowerCase();
            const type = tag === "input" ? (element.getAttribute("type") || "text").toLowerCase() : tag;
            if (type === "submit" || type === "button") return;
            const conditional = element.closest(".govuk-radios__conditional, .govuk-checkboxes__conditional");
            const dateGroup = element.closest(".govuk-date-input");
            result.fields.push({
                tag,
                type,
                name: element.getAttribute("name"),
                id: element.id || null,
                value: element.value === undefined ? null : element.value,
                checked: Boolean(element.checked),
                label: labelFor(element),
                autocomplete: element.getAttribute("autocomplete"),
                inputmode: element.getAttribute("inputmode"),
                controls: element.getAttribute("aria-controls") || element.getAttribute("data-aria-controls"),
                conditional: conditional ? conditional.id : null,
                dateGroup: dateGroup ? dateGroup.id || "date" : null,
                options: tag === "select" ? Array.from(element.options).map((option) => ({ value: option.value, label: clean(option.textContent) })) : [],
            });
        });
        form.querySelectorAll("button, input[type=submit]").forEach((button) => {
            result.buttons.push({
                name: button.getAttribute("name"),
                value: button.getAttribute("value"),
                text: clean(button.textContent || button.value),
                type: (button.getAttribute("type") || "submit").toLowerCase(),
            });
        });
    }

    document.querySelectorAll(".govuk-error-summary__list a").forEach((link) => {
        result.errors.push({ href: link.getAttribute("href"), text: clean(link.textContent) });
    });

    main.querySelectorAll(".govuk-summary-list__actions a, .govuk-summary-card__actions a").forEach((link) => {
        result.changeLinks.push({ href: link.href, text: clean(link.textContent) });
    });

    const primaryLinks = Array.from(main.querySelectorAll("a.govuk-task-list__link, a.govuk-button"));
    primaryLinks.forEach((link) => {
        result.links.push({ href: link.href, text: clean(link.textContent) });
    });

    main.querySelectorAll("a[href]").forEach((link) => {
        if (primaryLinks.includes(link) || link.matches(".govuk-back-link")) return;
        if (link.closest(".govuk-summary-list__actions, .govuk-summary-card__actions")) return;
        result.contentLinks.push({ href: link.href, text: clean(link.textContent) });
    });

    return JSON.stringify(result);
}
