// Bulk Passcode Generator
//
// Usage:
//   1. Log in as a system operator
//   2. Navigate to /system-operator/generate-passcode
//   3. Open the browser console (F12 → Console)
//   4. Paste this entire script and press Enter
//   5. Enter the number of passcodes when prompted

async function generatePasscodes(count) {
  const csrfToken = document.querySelector('input[name="_csrf"]')?.value;
  if (!csrfToken) {
    console.error(
      "CSRF token not found. Are you on the /system-operator/generate-passcode page?"
    );
    return;
  }

  const generateUrl = "/system-operator/generate-passcode";
  const passcodes = [];
  const parser = new DOMParser();
  let currentCsrfToken = csrfToken;

  console.log(`Generating ${count} passcodes...`);

  for (let i = 0; i < count; i++) {
    try {
      const response = await fetch(generateUrl, {
        method: "POST",
        headers: { "Content-Type": "application/x-www-form-urlencoded" },
        body: `_csrf=${encodeURIComponent(currentCsrfToken)}`,
        redirect: "follow",
      });

      if (!response.ok) {
        console.error(
          `Request failed with status ${response.status} after generating ${passcodes.length} passcodes`
        );
        break;
      }

      const html = parser.parseFromString(await response.text(), "text/html");
      const passcode = html
        .querySelector(".govuk-panel__body strong")
        ?.textContent?.trim();

      if (!passcode) {
        console.error(
          `Could not extract passcode from response after generating ${passcodes.length} passcodes. ` +
            "The passcode limit may have been reached."
        );
        break;
      }

      passcodes.push(passcode);

      const freshToken = html.querySelector('input[name="_csrf"]')?.value;
      if (freshToken) {
        currentCsrfToken = freshToken;
      }

      if ((i + 1) % 10 === 0 || i + 1 === count) {
        console.log(`Generated ${i + 1}/${count}`);
      }
    } catch (error) {
      console.error(
        `Error after generating ${passcodes.length} passcodes:`,
        error
      );
      break;
    }
  }

  if (passcodes.length === 0) {
    console.error("No passcodes were generated");
    return;
  }

  const csv = "passcode\n" + passcodes.join("\n") + "\n";
  const blob = new Blob([csv], { type: "text/csv" });
  const url = URL.createObjectURL(blob);
  const a = document.createElement("a");
  a.href = url;
  a.download = "passcodes.csv";
  document.body.appendChild(a);
  a.click();
  document.body.removeChild(a);
  URL.revokeObjectURL(url);

  console.log(
    `Done. ${passcodes.length} passcode(s) downloaded to passcodes.csv`
  );
}

(function () {
  const input = prompt("How many passcodes do you want to generate?");
  if (input === null) {
    console.log("Cancelled");
    return;
  }
  const count = parseInt(input, 10);
  if (!Number.isInteger(count) || count < 1) {
    console.error("Count must be a positive integer");
    return;
  }
  generatePasscodes(count);
})();
