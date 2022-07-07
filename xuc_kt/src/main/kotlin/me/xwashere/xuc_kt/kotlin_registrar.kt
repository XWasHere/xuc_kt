package me.xwashere.xuc_kt

import com.google.auto.service.AutoService
import org.jetbrains.kotlin.backend.common.extensions.IrGenerationExtension
import org.jetbrains.kotlin.codegen.extensions.ClassBuilderInterceptorExtension
import org.jetbrains.kotlin.com.intellij.mock.MockProject
import org.jetbrains.kotlin.compiler.plugin.ComponentRegistrar
import org.jetbrains.kotlin.config.CompilerConfiguration
import org.jetbrains.kotlin.extensions.AnnotationBasedExtension
import org.jetbrains.kotlin.extensions.DeclarationAttributeAltererExtension
import org.jetbrains.kotlin.extensions.PreprocessedVirtualFileFactoryExtension

@AutoService(ComponentRegistrar::class)
open class kotlin_registrar : ComponentRegistrar {
    override fun registerProjectComponents(project: MockProject, configuration: CompilerConfiguration) {
        DeclarationAttributeAltererExtension.registerExtension(project, kotlin_dcl_alterer());
        IrGenerationExtension.registerExtension(project, kotlin_static_ext());
        ClassBuilderInterceptorExtension.registerExtension(project, kotlin_strip_ext());
        PreprocessedVirtualFileFactoryExtension.registerExtension(project, preprocessor(project));
//        kotlin_mangle_bytecode.install();
    }
}