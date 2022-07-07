package me.xwashere.xuc_kt

import org.jetbrains.kotlin.backend.common.extensions.IrGenerationExtension
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.backend.common.lower.DeclarationIrBuilder
import org.jetbrains.kotlin.backend.common.lower.createIrBuilder
import org.jetbrains.kotlin.backend.jvm.ir.propertyIfAccessor
import org.jetbrains.kotlin.codegen.ClassBuilder
import org.jetbrains.kotlin.codegen.ClassBuilderFactory
import org.jetbrains.kotlin.codegen.DelegatingClassBuilder
import org.jetbrains.kotlin.codegen.DelegatingClassBuilderFactory
import org.jetbrains.kotlin.codegen.extensions.ClassBuilderInterceptorExtension
import org.jetbrains.kotlin.diagnostics.DiagnosticSink
import org.jetbrains.kotlin.extensions.AnnotationBasedExtension
import org.jetbrains.kotlin.fir.FirSession
import org.jetbrains.kotlin.fir.extensions.FirDeclarationGenerationExtension
import org.jetbrains.kotlin.ir.IrElement
import org.jetbrains.kotlin.ir.IrStatement
import org.jetbrains.kotlin.ir.backend.js.lower.serialization.ir.JsManglerIr.fqnString
import org.jetbrains.kotlin.ir.backend.jvm.serialization.JvmIrMangler.fqnString
import org.jetbrains.kotlin.ir.builders.declarations.buildClass
import org.jetbrains.kotlin.ir.builders.declarations.buildField
import org.jetbrains.kotlin.ir.builders.declarations.buildFun
import org.jetbrains.kotlin.ir.declarations.*
import org.jetbrains.kotlin.ir.expressions.*
import org.jetbrains.kotlin.ir.expressions.impl.IrGetFieldImpl
import org.jetbrains.kotlin.ir.expressions.impl.IrSetFieldImpl
import org.jetbrains.kotlin.ir.util.*
import org.jetbrains.kotlin.ir.visitors.IrElementTransformer
import org.jetbrains.kotlin.ir.visitors.IrElementTransformerVoid
import org.jetbrains.kotlin.ir.visitors.IrElementVisitorVoid
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.psi.KtModifierListOwner
import org.jetbrains.kotlin.psi.KtProperty
import org.jetbrains.kotlin.psi.psiUtil.containingClass
import org.jetbrains.kotlin.resolve.BindingContext
import org.jetbrains.kotlin.resolve.jvm.diagnostics.JvmDeclarationOrigin
import org.jetbrains.org.objectweb.asm.AnnotationVisitor
import org.jetbrains.org.objectweb.asm.FieldVisitor
import org.jetbrains.org.objectweb.asm.MethodVisitor
import kotlin.math.exp

open class kotlin_static_ext : IrGenerationExtension {
    override fun generate(moduleFragment: IrModuleFragment, pluginContext: IrPluginContext) {
        moduleFragment.transform(kotlin_static_ext_trans(pluginContext), null);
        // throw java.lang.Exception(moduleFragment.dump());
    }
}

open class kotlin_static_ext_trans(
    open val ctx : IrPluginContext
) : IrElementTransformerVoid() {
    override fun visitProperty(element: IrProperty): IrStatement {
        var st : IrStatement;
        if (element.hasAnnotation(FqName("me.xwashere.xuc_kt.static"))) {
            return ctx.irFactory.buildField({
                val field = element?.backingField;
                name     = element.name;
                isStatic = true;
                if (field != null) {
                    this.type = field.type;
                }
            }).also({
                val field_init = element?.backingField;
                it.parent = element.parent
                it.initializer = field_init?.initializer;
            });
        } else if (element.hasAnnotation(FqName("me.xwashere.xuc_kt.internal.static_companion"))) {
            return element; // todo: do shit here
        } else return super.visitProperty(element)
    }

    override fun visitCall(expression: IrCall): IrExpression {
        if (expression.symbol.owner.isAccessor && expression.symbol.owner.propertyIfAccessor.hasAnnotation(FqName("me.xwashere.xuc_kt.internal.static_companion"))) {
            val owner  = expression.symbol.owner.parentAsClass.parentAsClass;
            val field  = expression.symbol.owner.name.toString().removePrefix("<get-").removePrefix("<set-").removeSuffix(">")
            val afield = owner.fields.find({
                true
            })!!;

            if (expression.symbol.owner.isGetter) {
                return IrGetFieldImpl(
                    expression.startOffset, expression.endOffset,
                    afield.symbol,
                    afield.type
                )
            } else {
                return IrSetFieldImpl(
                    expression.startOffset, expression.endOffset,
                    afield.symbol,
                    null,
                    expression.getValueArgument(0)!!,
                    afield.type,
                    null,
                    null
                )
            }
        } else return super.visitCall(expression)
    }
}