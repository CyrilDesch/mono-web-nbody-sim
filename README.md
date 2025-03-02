# N-Body Simulation

![Build Status](https://github.com/CyrilDesch/mono-web-nbody-sim/actions/workflows/development.yml/badge.svg)
![Coverage](.github/badges/jacoco.svg)
![Branches](.github/badges/branches.svg)

This project uses Quarkus, the Supersonic Subatomic Java Framework.

If you want to learn more about Quarkus, please visit its website: <https://quarkus.io/>.

## Quality Assurance

This project uses several tools to ensure code quality:

- **JaCoCo**: Test coverage analysis
- **SpotBugs**: Static code analysis to find potential bugs
- **GitHub Actions**: Continuous integration and deployment

The badges at the top of this README show the current build status and code coverage.

## Running the application in dev mode

You can run your application in dev mode that enables live coding using:

```shell script
./gradlew quarkusDev
```

> **_NOTE:_**  Quarkus now ships with a Dev UI, which is available in dev mode only at <http://localhost:8080/q/dev/>.

## Packaging and running the application

The application can be packaged using:

```shell script
./gradlew build
```

It produces the `quarkus-run.jar` file in the `build/quarkus-app/` directory.
Be aware that it's not an _über-jar_ as the dependencies are copied into the `build/quarkus-app/lib/` directory.

The application is now runnable using `java -jar build/quarkus-app/quarkus-run.jar`.

If you want to build an _über-jar_, execute the following command:

```shell script
./gradlew build -Dquarkus.package.jar.type=uber-jar
```

The application, packaged as an _über-jar_, is now runnable using `java -jar build/*-runner.jar`.

## Creating a native executable

You can create a native executable using:

```shell script
./gradlew build -Dquarkus.native.enabled=true
```

Or, if you don't have GraalVM installed, you can run the native executable build in a container using:

```shell script
./gradlew build -Dquarkus.native.enabled=true -Dquarkus.native.container-build=true
```

You can then execute your native executable with: `./build/n-body-simulation-back-1.0.0-SNAPSHOT-runner`

If you want to learn more about building native executables, please consult <https://quarkus.io/guides/gradle-tooling>.

## Related Guides

- WebSockets ([guide](https://quarkus.io/guides/websockets)): WebSocket communication channel support

## Provided Code

### WebSockets

WebSocket communication channel starter code

[Related guide section...](https://quarkus.io/guides/websockets)
