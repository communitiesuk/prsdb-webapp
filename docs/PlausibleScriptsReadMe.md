# Plausible Scripts Usage Instructions

## Prerequisites
- Ensure Node.js is installed.
- Run `npm install` in the `scripts/plausible` directory if required.

## Configuration
- Add your Plausible API Key to a `.env` file in the `scripts` directory:
  ```
  PLAUSIBLE_API_KEY=your_api_key
  ```
  - You can get the API key from Keeper or create your own in Plausible under  `Settings > API`.

## Input Queries
- Store queries in `scripts/plausible/inputs` as a `.json` file.
- Each query should follow the Plausible API format.
- It is mandatory to include `"include": {"total_rows": true}` in your query, the script will error out if it doesn't include it.
- Example query structure:
  ```json
  {
    "queryName": {
      "site_id": "prod.register-home-to-rent.communities.gov.uk",
      "date_range": "14d",
      "metrics": ["metric1", "metric2"],
      "dimensions": ["dimension1", "dimension2"],
      "filters": [["operator", "dimension", ["clauses"]]],
      "include": {"total_rows": true}
    }
  }
  ```
  - `"queryName"` will be used as the output CSV filename.
  - `"site_id"` should always be `"prod.register-home-to-rent.communities.gov.uk"`.
  - `"date_range"` can be relative (e.g., `"14d"`, `"30d"`) or absolute (e.g., `"2023-01-01,2023-01-31"`).
    - relative date ranges are taken prior to the current date e.g. if today is 2024-06-15, `"14d"` means from 2024-06-01 to 2024-06-14.
  - `"metrics"` and `"dimensions"` should be valid Plausible metrics and dimensions, these will be used for the csv column headers.
  - `"filters"` is optional, but if used must follow the Plausible API format. It might seem like there is an extra set of brackets in the example above, but the API expects it to be an array of arrays.
- Each group of queries should be in it's own `.json` file, e.g., `dwellTimes.json`, `pageViews.json`.

## Running the Script
- Run the main script (e.g., `plausible.mjs`) from `scripts/plausible` using `npm run plausible`.
- The script will:
  - Read all `.json` files in `scripts/plausible/inputs`
  - Run each query from each `.json` file.
  - Write output CSVs to `scripts/plausible/outputs`.

## Output
- Find generated CSV files in `scripts/plausible/outputs`.
- Each file matches a query and contains results with correct headers.

## Notes
- Ensure `scripts/plausible/outputs` and `.env ` are added to .gitignore` to avoid uploading outputs and secrets.
- More information can be found in the [Plausible API documentation](https://plausible.io/docs/stats-api).

