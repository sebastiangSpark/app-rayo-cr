# rayoApp
Rayo Android App

### Upload to app distribution
To build and APK and upload to app distribution, just need run in terminal
(Remember before that update the version in `gradle.properties` and the `release-notes.txt`)

```bash
./gradlew assembleDevDebug; ./gradlew appDistributionUploadDevDebug
./gradlew assembleQaRelease; ./gradlew appDistributionUploadQaRelease 
```