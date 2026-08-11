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

# OPA Sidecar Deployment Changes

## Overview

OPA (Open Policy Agent) has been introduced as a sidecar container within the application pod to externalize business rule evaluation from the hmpps-digital-canteen-api application.

Current deployment configuration consists of a single application replica:

```text
Pod
├── hmpps-digital-canteen-api - hmpps-digital-canteen-api Application
└── OPA Sidecar
```

---

# Helm Configuration Changes

OPA is deployed using the Helm chart's:

```yaml
  extraContainers:
    - name: opa
      image: openpolicyagent/opa:1.19.0

      args:
        - run
        - --server
        - --addr=0.0.0.0:8181

      ports:
        - name: opa
          containerPort: 8181

      securityContext:
        runAsNonRoot: true
        allowPrivilegeEscalation: false

        capabilities:
          drop:
            - ALL

        seccompProfile:
          type: RuntimeDefault

```

configuration.

This results in Kubernetes creating an additional container within the same pod as the hmpps-digital-canteen-api application.

---

# Communication Between hmpps-digital-canteen-api and OPA

Since both containers run in the same pod, they share the same network namespace.

The hmpps-digital-canteen-api application communicates with OPA using:

```text
http://localhost:8181
```

No additional Kubernetes Service is required.

---

# Security Configuration

The Kubernetes cluster enforces the Pod Security Standard:

```text
restricted
```

To comply with Pod Security requirements, the OPA container was configured with the following security settings.

## Run As Non-Root

```yaml
runAsNonRoot: true
```

### Purpose

Ensures the OPA process cannot run as the root user.

### Benefit

- Reduces security risk.
- Prevents unnecessary privileged execution.

---

## Disable Privilege Escalation

```yaml
allowPrivilegeEscalation: false
```

### Purpose

Prevents processes inside the OPA container from obtaining elevated privileges.

### Benefit

- Helps protect against privilege escalation attacks.
- Enforces least-privilege principles.

---

## Remove Linux Capabilities

```yaml
capabilities:
  drop:
    - ALL
```

### Purpose

Removes all default Linux capabilities from the OPA container.

### Benefit

- Reduces attack surface.
- Limits operating system level permissions.

---

## Runtime Seccomp Profile

```yaml
seccompProfile:
  type: RuntimeDefault
```

### Purpose

Enforces the container runtime's default syscall restrictions.

### Benefit

- Restricts access to potentially dangerous system calls.
- Improves compliance with Kubernetes security standards.

---

# Verification

## Verify OPA Sidecar Exists

```bash
kubectl get pod <pod-name> -o jsonpath='{.spec.containers[*].name}'
```

Expected output:

`*`text
hmpps-digital-canteen-api opa%
```

---
*## View OPA Logs

```bash
kubectl logs <pod-name> -c opa
```

---

##*Verify OPA Endpoint

From the application container:

```bash
curl http://localhost:8181/v1/data
```

Expected response:

```json
{
  "result": {}
}
```

---

# Benefits

- Business rule evaluation moved outside application code.
- OPA scales automatically with the application pod.
- hmpps-digital-canteen-api communicates with OPA locally using `localhost`.
- Kubernetes security standards are satisfied.
- No additional service-to-service network communication required.
- Foundation established for future dynamic policy and configuration updates.
````*