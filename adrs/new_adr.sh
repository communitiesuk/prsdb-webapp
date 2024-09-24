#!/bin/bash

# Define the ADR template filename and path
TEMPLATE="0000-adr-template.md"
ADR_DIR="."

# Check if template exists
if [ ! -f "$ADR_DIR/$TEMPLATE" ]; then
  echo "Template file '$TEMPLATE' not found!"
  exit 1
fi

# Check if title is provided
if [ -z "$1" ]; then
  echo "Usage: $0 \"Title of the ADR\""
  exit 1
fi

# Generate a safe file name from the title by converting to lowercase and replacing spaces with dashes
TITLE="$1"
SAFE_TITLE=$(echo "$TITLE" | tr '[:upper:]' '[:lower:]' | tr -s ' ' '-' | tr -cd '[:alnum:]-' | sed 's/--/-/g')

# Find the next available ADR number
NEXT_NUMBER=$(find "$ADR_DIR" -name "*.md" | grep -E '[0-9]{4}-' | sed 's|.*/\([0-9]\{4\}\)-.*|\1|' | sort -n | tail -1)

# Handle the case where no existing ADRs are found
if [ -z "$NEXT_NUMBER" ]; then
  NEXT_NUMBER=0
else
  NEXT_NUMBER=$((10#$NEXT_NUMBER))  # Ensure it is treated as a decimal number
fi

NEXT_NUMBER=$((NEXT_NUMBER + 1))

# Zero-padded number for the filename
PADDED_NUMBER=$(printf "%04d" "$NEXT_NUMBER")

# Create the ADR filename with the next number and safe title
ADR_FILENAME="${PADDED_NUMBER}-${SAFE_TITLE}.md"

# Copy the template to the new ADR file
cp "$ADR_DIR/$TEMPLATE" "$ADR_DIR/$ADR_FILENAME"

# Update the first line of the new ADR with the padded number and title
sed -i "1s/# ADR-0000: ADR Template/# ADR-$PADDED_NUMBER: $TITLE/" "$ADR_DIR/$ADR_FILENAME"

# Confirmation message
echo "ADR created: $ADR_FILENAME"
