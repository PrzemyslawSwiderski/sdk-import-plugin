package com.pswidersk.sdkimportplugin

class SdkImportConfig {
    var import: MutableList<SdkImportConfigEntry> = mutableListOf()
}

class SdkImportConfigEntry {
    var type: String = ""
    var path: String = ""
    var module: String = ""
}

const val PYTHON_SDK_TYPE = "PYTHON"
const val JAVA_SDK_TYPE = "JAVA"
