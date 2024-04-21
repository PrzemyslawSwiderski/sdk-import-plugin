# SDK-Import Intellij Plugin

![Build](https://github.com/PrzemyslawSwiderski/sdk-import-plugin/workflows/Build/badge.svg)
[![Version](https://img.shields.io/jetbrains/plugin/v/com.pswidersk.sdkimportplugin.svg)](https://plugins.jetbrains.com/plugin/com.pswidersk.sdkimportplugin)
[![Downloads](https://img.shields.io/jetbrains/plugin/d/com.pswidersk.sdkimportplugin.svg)](https://plugins.jetbrains.com/plugin/com.pswidersk.sdkimportplugin)

## Template ToDo list

- [x] Create a new [IntelliJ Platform Plugin Template][template] project.
- [x] Get familiar with the [template documentation][template].
- [x] Adjust the [pluginGroup](./gradle.properties), [plugin ID](./src/main/resources/META-INF/plugin.xml)
  and [sources package](./src/main/kotlin).
- [ ] Adjust the plugin description in `README` (see [Tips][docs:plugin-description])
- [x] Review
  the [Legal Agreements](https://plugins.jetbrains.com/docs/marketplace/legal-agreements.html?from=IJPluginTemplate).
- [ ] [Publish a plugin manually](https://plugins.jetbrains.com/docs/intellij/publishing-plugin.html?from=IJPluginTemplate)
  for the first time.
- [x] Set the `PLUGIN_ID` in the above README badges.
- [ ] Set the [Plugin Signing](https://plugins.jetbrains.com/docs/intellij/plugin-signing.html?from=IJPluginTemplate)
  related [secrets](https://github.com/JetBrains/intellij-platform-plugin-template#environment-variables).
- [ ] Set
  the [Deployment Token](https://plugins.jetbrains.com/docs/marketplace/plugin-upload.html?from=IJPluginTemplate).
- [ ] Click the <kbd>Watch</kbd> button on the top of the [IntelliJ Platform Plugin Template][template] to be notified
  about releases containing new features and fixes.

<!-- Plugin description -->

## Description

Plugin to add support for the project level SDK definitions.

### Configuration file

In order to import custom SDKs to IDE, the `sdk-import.yml` file has to be created in the parent `.idea` directory.

For example:

```yml
import:
  - module: "python-data-science-samples"
    path: "C:/PYTHON/Miniconda3-py312_24.1.2-0/envs/python-3.12.2/python.exe"
    type: "PYTHON"
  - module: "jupyter-notebook"
    path: "C:/PYTHON/Miniconda3-py312_24.1.2-0/envs/python-3.9.1/python.exe"
    type: "PYTHON"
```

Where:

* `module` should match the Intellij's module name,
* `path` is the location of SDK on files system,
* `type` should be one of the supported SDKs enumerate values,

### Import action

In order to load the config file, developer has to choose **"Tools" -> "Reimport SDK"** action.

After Intellij will load the SDK and index the files, code autocompletion should be enabled.

### Supported SDKs

The Plugin supports the following SDKs:

* Python (`PYTHON` import type)

### Python Support

This plugin can cooperate with [**Python Gradle Plugin**](https://github.com/PrzemyslawSwiderski/python-gradle-plugin)
to
make working with multi-module projects easier.

<!-- Plugin description end -->

## Installation

- Using the IDE built-in plugin system:

  <kbd>Settings/Preferences</kbd> > <kbd>Plugins</kbd> > <kbd>Marketplace</kbd> > <kbd>Search for "SDK-Import"</kbd> >
  <kbd>Install</kbd>

- Manually:

  Download the [latest release](https://github.com/PrzemyslawSwiderski/sdk-import-plugin/releases/latest) and install it
  manually using
  <kbd>Settings/Preferences</kbd> > <kbd>Plugins</kbd> > <kbd>⚙️</kbd> > <kbd>Install plugin from disk...</kbd>

---
Plugin based on the [IntelliJ Platform Plugin Template][template].

[template]: https://github.com/JetBrains/intellij-platform-plugin-template

[docs:plugin-description]: https://plugins.jetbrains.com/docs/intellij/plugin-user-experience.html#plugin-description-and-presentation
