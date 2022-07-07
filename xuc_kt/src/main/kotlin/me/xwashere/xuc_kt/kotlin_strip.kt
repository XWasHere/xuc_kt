package me.xwashere.xuc_kt

import org.jetbrains.kotlin.codegen.ClassBuilder
import org.jetbrains.kotlin.codegen.ClassBuilderFactory
import org.jetbrains.kotlin.codegen.DelegatingClassBuilder
import org.jetbrains.kotlin.codegen.DelegatingClassBuilderFactory
import org.jetbrains.kotlin.codegen.extensions.ClassBuilderInterceptorExtension
import org.jetbrains.kotlin.diagnostics.DiagnosticSink
import org.jetbrains.kotlin.resolve.BindingContext
import org.jetbrains.kotlin.resolve.jvm.diagnostics.JvmDeclarationOrigin
import org.jetbrains.org.objectweb.asm.AnnotationVisitor
import org.jetbrains.org.objectweb.asm.FieldVisitor
import org.jetbrains.org.objectweb.asm.MethodVisitor

open class kotlin_strip_ext : ClassBuilderInterceptorExtension {
    override fun interceptClassBuilderFactory(
        interceptedFactory: ClassBuilderFactory,
        bindingContext: BindingContext,
        diagnostics: DiagnosticSink
    ): ClassBuilderFactory {
        return object : DelegatingClassBuilderFactory(interceptedFactory) {
            override fun newClassBuilder(origin: JvmDeclarationOrigin): DelegatingClassBuilder {
                return kotlin_strip_ext_factory(interceptedFactory.newClassBuilder(origin));
            }
        }
    }

    open inner class kotlin_strip_ext_factory(
        open val _del: ClassBuilder
    ) : DelegatingClassBuilder() {
        override fun getDelegate(): ClassBuilder {
            return _del;
        }

        override fun newAnnotation(desc: String, visible: Boolean): AnnotationVisitor {
            if (desc == "Lkotlin/Metadata;") {
                return dead_annotation_visitor.create();
            } else return super.newAnnotation(desc, visible)
        }
    };
};