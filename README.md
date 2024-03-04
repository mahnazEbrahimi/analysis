## analysis

### Running application in different profiles

The application can be run in different profile configuration:
- **_deployment (default)_** `[application-deployed.conf]` - default profile used for production and staging
- **_development_** `[application-development.conf]` - used for local development

To run the application in specific profile you need to add name of the configuration file as program parameter `-config=application-development.conf` like:
```sh
./gradlew run --args='-config=application-development.conf'