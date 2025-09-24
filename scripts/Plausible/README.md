# Plausible Scripts Usage Instructions

## Prerequisites
- Ensure Node.js is installed.
- Run `npm install` in the `scripts/Plausible` directory if required.

## Configuration
- Add your Plausible API key and site ID to a `.env` file in the `scripts` directory:
  ```
  PLAUSIBLE_API_KEY=your_api_key
  PLAUSIBLE_SITE_ID=your_site_id
  PLAUSIBLE_BASE_URL=plausible_api_base_url
  ```

## Input Queries
- Store queries in `inputQueries.json` in `scripts/Plausible`.
- Each query should follow the Plausible API format.

## Running the Script
- Run the main script (e.g., `plausible.mjs`) from `scripts/Plausible` using `npm run plausible`.
- The script will:
  - Read `inputQueries.json`
  - Replace `SITE_ID` with your `.env` value
  - Run each query
  - Write output CSVs to `scripts/Plausible/Outputs`, named after each query

## Output
- Find generated CSV files in `scripts/Plausible/Outputs`.
- Each file matches a query and contains results with correct headers.

## Notes
- Add `scripts/Plausible/Outputs` to `.gitignore` to avoid uploading results.
- Edit `inputQueries.json` to change queries as needed.

---
If you need more details or examples, see the script comments or ask for help.
