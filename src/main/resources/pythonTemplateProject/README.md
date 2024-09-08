# Gradle Python Generated project

## Installing Python and PIP dependencies

Run:

```commandline
./gradlew condaInstall 
```

or on Windows:

```commandline
gradlew condaInstall
```

## Integrating with SDK-Import plugin

After successfully executing `condaInstall` task, in order to load the config file, developer has to choose **"Tools" -> "
Reimport SDK"** action.

After Intellij will load the SDK and index the files, code autocompletion should be enabled.

## Running Python script directly

To install Python environment and run `script.py` directly execute:

```commandline
./gradlew runScript
```

or on Windows:

```commandline
gradlew runScript
```
