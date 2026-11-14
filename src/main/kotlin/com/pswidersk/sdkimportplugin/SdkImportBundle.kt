package com.pswidersk.sdkimportplugin

import com.intellij.DynamicBundle
import org.jetbrains.annotations.NonNls
import org.jetbrains.annotations.PropertyKey

@NonNls
private const val BUNDLE = "messages.SdkImportBundle"

object SdkImportBundle {

    private val INSTANCE = DynamicBundle(SdkImportBundle::class.java, BUNDLE)

    @JvmStatic
    fun message(@PropertyKey(resourceBundle = BUNDLE) key: String, vararg params: Any): String =
        INSTANCE.getMessage(key, *params)

}
