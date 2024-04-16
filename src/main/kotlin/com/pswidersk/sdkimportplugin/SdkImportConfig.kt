package com.pswidersk.sdkimportplugin

data class SdkImportConfig(
    val import: List<SdkImportConfigEntry> = emptyList()
)

data class SdkImportConfigEntry(
    val type: SdkType,
    val path: String,
    val module: String,
)

enum class SdkType {
    PYTHON
}