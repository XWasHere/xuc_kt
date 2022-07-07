package me.xwashere.xuc_kt

import com.google.auto.service.AutoService
import com.google.devtools.ksp.KspExperimental
import com.google.devtools.ksp.isAnnotationPresent
import com.google.devtools.ksp.processing.*
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSPropertyDeclaration
import com.google.devtools.ksp.symbol.KSVisitorVoid
import com.google.devtools.ksp.validate
import org.jetbrains.kotlin.codegen.inline.addInlineMarker
import org.jetbrains.kotlin.codegen.inline.createEmptyMethodNode
import org.jetbrains.kotlin.psi.addRemoveModifier.addAnnotationEntry
import java.io.OutputStream

//@AutoService(SymbolProcessorProvider::class)
open class symbol_processor_provider : SymbolProcessorProvider {
    override fun create(env : SymbolProcessorEnvironment) : SymbolProcessor {
        return symbol_processor(env.codeGenerator, env.logger, env.options);
    }
}

open class symbol_processor(
    open val generator : CodeGenerator,
    open val logger    : KSPLogger,
    open val options   : Map<String, String>
) : SymbolProcessor { // ??????
    open operator fun OutputStream.plusAssign(dat : String) {
        this.write(dat.toByteArray());
    }

    open inner class symbol_visitor(
        open val file: OutputStream
    ) : KSVisitorVoid() {
        override fun visitPropertyDeclaration(property: KSPropertyDeclaration, data: Unit) {
            property.annotations.forEach({
                throw java.lang.Exception(it.annotationType.resolve().toString());
            })

            super.visitPropertyDeclaration(property, data)
        }
    }

    override fun process(resolver : Resolver) : List<KSAnnotated> {
        val syms = resolver.getSymbolsWithAnnotation("me.xwashere.xuc_kt.static");

        if (!syms.iterator().hasNext()) return emptyList();

        val file : OutputStream = generator.createNewFile(
            dependencies = Dependencies(false, *resolver.getAllFiles().toList().toTypedArray()),
            packageName  = "me.xwashere.xuc",
            fileName     = "xuc_out"
        );

        file += "package me.xwashere.xuc;\n";

        syms.forEach({
            //throw java.lang.Exception("ass");
        })

        file.close();

        return syms.filterNot({ it.validate() }).toList();
    }
}