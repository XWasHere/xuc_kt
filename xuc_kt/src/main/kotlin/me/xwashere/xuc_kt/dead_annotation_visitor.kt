package me.xwashere.xuc_kt

import org.jetbrains.org.objectweb.asm.AnnotationVisitor

open class dead_annotation_visitor : AnnotationVisitor {
    companion object {
        fun create() : dead_annotation_visitor { // i have no idea what the api version is supposed to be, so ill just never call the constructor.
            return advanced.imalloc(dead_annotation_visitor::class.java);
        }
    }

    private constructor() : super(-1) {}

    override fun visitEnd() {}
    override fun visitArray(name: String?): AnnotationVisitor { return this; }
    override fun visit(name: String?, value: Any?) {}
    override fun visitAnnotation(name: String?, descriptor: String?): AnnotationVisitor { return this; }
    override fun visitEnum(name: String?, descriptor: String?, value: String?) {}
}