package com.pswidersk.sdkimportplugin

class SdkImportConfig {
    lateinit var import: List<SdkImportConfigEntry>
}

class SdkImportConfigEntry {
    lateinit var type: SdkType
    lateinit var path: String
    lateinit var module: String
}

enum class SdkType {
    PYTHON, JAVA
}