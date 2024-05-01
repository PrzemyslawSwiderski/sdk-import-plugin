// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.pswidersk.sdkimportplugin.python

import com.intellij.ide.fileTemplates.FileTemplateGroupDescriptor
import com.intellij.ide.fileTemplates.FileTemplateGroupDescriptorFactory
import com.pswidersk.sdkimportplugin.PLUGIN_NAME
import icons.SdkImportIcons.ToolIcon


internal class GradleTemplateGroup : FileTemplateGroupDescriptorFactory {

    override fun getFileTemplatesDescriptor(): FileTemplateGroupDescriptor {
        val root = FileTemplateGroupDescriptor(PLUGIN_NAME, ToolIcon)

        with(root) {
            addTemplate(SETTINGS_GRADLE)
        }

        return root
    }

    companion object {
        const val SETTINGS_GRADLE = "settings.gradle.kts"
    }
}