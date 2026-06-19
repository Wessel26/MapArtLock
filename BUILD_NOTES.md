# Build notes

Ik heb de permissions opnieuw opgebouwd volgens deze indeling:

- `mapart.*` = userrechten, geen adminrechten.
- `mapart.admin` = adminrechten, inclusief andermans mapart beheren en `/mapart version` plus `/mapart reload`.

Belangrijk: in deze sandbox kon ik geen nieuwe jar bouwen, omdat de Gradle wrapper de Gradle-distributie vanaf `services.gradle.org` wilde downloaden en er geen internettoegang beschikbaar was. Bouw lokaal of op je server met:

```bash
chmod +x gradlew
./gradlew clean build
```

De nieuwe jar komt daarna in `build/libs`.
