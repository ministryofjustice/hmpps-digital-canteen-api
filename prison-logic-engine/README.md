# Prison Catalogue Rules Engine

## Overview

This document describes the business scenarios currently supported by the Prison Catalogue Rules Engine for DHL-managed prisoner ordering.

The engine evaluates product eligibility based on:

- Prisoner attributes
- Product attributes
- Prison-specific rules
- Global product rules
- Credit limits
- Quantity limits
- Product restrictions

---

# Rule Types

## Prison-Specific Rules

Rules that only apply to one or more configured prisons.

Examples:

- Adjudication restrictions
- Arson charge restrictions
- Prison-specific BT credit limits

---

## Global Rules

Rules that apply across all prisons.

Examples:

- Under 18 vape restrictions
- Religious vape allowances
- Religious artefact allowances

Global products can be ordered regardless of whether the prison has specific product configuration.

---

# Business Scenarios

---

# R001 - Under 18 Vape Restriction

## Scenario

Prisoners under 18 years of age are not permitted to order vape-related products.

## Conditions

- Product has attribute `VAPE`
- Prisoner age is less than 18

## Outcome

**DENY**

### Examples

#### Allowed

- Vape product
- Prisoner age = 18

#### Denied

- Vape product
- Prisoner age = 17

---

# R002 - Prisoner On Adjudication

## Scenario

Prisoners currently on adjudication cannot order certain products.

## Applicable Prisons

- MSI
- LEI

## Products Covered

- PIN Credit
- BAR Stamp

## Conditions

- Prisoner is on adjudication

## Outcome

**DENY**

### Examples

#### Allowed

- BT Phone Credit
- Prisoner not on adjudication

#### Denied

- BT Phone Credit
- Prisoner on adjudication

---

# R003 - Prisoner On Arson Charge

## Scenario

Prisoners with an active arson-related restriction cannot order products capable of creating a flame.

## Applicable Prisons

- MSI

## Products Covered

Products with attribute:

- CAN_PRODUCE_FLAME

Examples:

- Lighters
- Matches

## Conditions

- Prisoner has arson charge

## Outcome

**DENY**

### Examples

#### Allowed

- Soap
- Hygiene products

#### Denied

- Lighter
- Matches

---

# R004 - Religious Artefact Allowance For Arson Prisoners

## Scenario

Arson-charged prisoners with a recognised religious affiliation may order religious artefacts.

## Applicable Prisons

Configured prison list.

## Product Type

Products with attribute:

- RELIGIOUS_ARTEFACT

Examples:

- Religious books
- Religious artefacts
- Religious celebration items

## Conditions

- Prisoner has arson charge
- Prisoner has religious affiliation

## Quantity Restriction

Maximum:

- 5 items per cycle

## Outcome

**ALLOW**

### Examples

#### Allowed

- Quantity = 5
- Religious artefact

#### Denied

- Quantity = 6
- Religious artefact

---

# R005 - Religious Vape Allowance For Arson Prisoners

## Scenario

Arson-charged prisoners may order approved religious vaping products.

## Scope

All prisons.

## Product Type

Products with attribute:

- RELIGIOUS_VAPE

## Conditions

- Prisoner has arson charge

## Quantity Restriction

Maximum:

- 10 items per cycle

## Outcome

**ALLOW**

### Examples

#### Allowed

- Quantity = 10

#### Denied

- Quantity = 11

---

# BT Phone Credit Rules

## Standard Prisoners

### Conditions

- Not Foreign National (FN)
- Not LG

### Outcome

- Credit