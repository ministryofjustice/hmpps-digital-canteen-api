# hmpps-digital-canteen-api

[![Ministry of Justice Repository Compliance Badge](https://github-community.service.justice.gov.uk/repository-standards/api/hmpps-digital-canteen-api/badge)](https://github-community.service.justice.gov.uk/repository-standards/hmpps-digital-canteen-api)
[![Docker Repository on ghcr](https://img.shields.io/badge/ghcr.io-repository-2496ED.svg?logo=docker)](https://ghcr.io/ministryofjustice/hmpps-digital-canteen-api)
[![API docs](https://img.shields.io/badge/API_docs_-view-85EA2D.svg?logo=swagger)](https://digital-canteen-dev.prison.service.justice.gov.uk/swagger-ui/index.html)

Template github repo used for new Kotlin based projects.

# Instructions

If this is a HMPPS project then the project will be created as part of bootstrapping -
see [hmpps-project-bootstrap](https://github.com/ministryofjustice/hmpps-project-bootstrap). You are able to specify a
template application using the `github_template_repo` attribute to clone without the need to manually do this yourself
within GitHub.

This project is community managed by the mojdt `#kotlin-dev` slack channel.
Please raise any questions or queries there. Contributions welcome!

Our security policy is located [here](https://github.com/ministryofjustice/hmpps-digital-canteen-api/security/policy).

Documentation to create new service is located [here](https://tech-docs.hmpps.service.justice.gov.uk/creating-new-services/).


## Running application locally

Ensure dependent services are running:

1. Medusa:https://github.com/ministryofjustice/hmpps-digital-canteen-medusa-service
2. UI: https://github.com/ministryofjustice/hmpps-digital-canteen-ui

### Running the application in Intellij

1. Get client secrets from DEV namespace. Export or add to IntelliJ run configuration:


    CLIENT_ID=hmpps-digital-canteen-api-1
    CLIENT_SECRET={secret}

2. Get Local medusa publishable key
```bash
curl -s -X POST http://localhost:9000/auth/user/emailpass \
-H "Content-Type: application/json" \
-d '{"email": "admin@admin.com", "password": "supersecret"}'
```

3. Get Publishable Key
```bash
curl -s "http://localhost:9000/admin/api-keys?type=publishable" \
-H "Authorization: Bearer {token}"
```

4. Export or add to intellij run configuration


    API_MEDUSA_PUBLISHABLE_KEY={key}


5. Spin up docker wiremock, as it is required for BT

Note: PrisonAPI and Prisoner Search can be ran against DEV, or wiremock, update application-dev.yml accordingly


    docker compose up wiremock -d


### Building and running the docker image locally


1. Get client secrets from DEV namespace.
Update docker compose 

    CLIENT_SECRET

2. Get Local medusa publishable key
Get token 
```bash
curl -s -X POST http://localhost:9000/auth/user/emailpass \
-H "Content-Type: application/json" \
-d '{"email": "admin@admin.com", "password": "supersecret"}'
```

3. Get Publishable Key
```bash
curl -s "http://localhost:9000/admin/api-keys?type=publishable" \
-H "Authorization: Bearer {token}"
```

4. Use "token" in docker compose

    API_MEDUSA_PUBLISHABLE_KEY

The `Dockerfile` relies on the application being built first. Steps to build the docker image:
5. Build the jar files
```
./gradlew clean assemble
```

6. Build the docker image with required arguments
```
docker build --build-arg BUILD_NUMBER=$(ls build/libs/hmpps-digital-canteen-api-*.jar | sed -E 's/.*api-(.*)\.jar/\1/') \
  -t ghcr.io/ministryofjustice/hmpps-digital-canteen-api:local .
```
7. Run the docker image, setting the auth url so that it starts up
```
docker compose up -d
```
## Common Kotlin patterns

Many patterns have evolved for HMPPS Kotlin applications. Using these patterns provides consistency across our suite of
Kotlin microservices and allows you to concentrate on building your business needs rather than reinventing the
technical approach.

Documentation for these patterns can be found in the [HMPPS tech docs](https://tech-docs.hmpps.service.justice.gov.uk/common-kotlin-patterns/).
If this documentation is incorrect or needs improving please report to [#ask-prisons-digital-sre](https://moj.enterprise.slack.com/archives/C06MWP0UKDE)
or [raise a PR](https://github.com/ministryofjustice/hmpps-tech-docs).