package me.xwashere.xuc_kt;

import org.jetbrains.kotlin.com.intellij.openapi.project.Project
import org.jetbrains.kotlin.com.intellij.openapi.util.Key
import org.jetbrains.kotlin.com.intellij.openapi.vfs.VirtualFile
import org.jetbrains.kotlin.com.intellij.psi.PsiElement
import org.jetbrains.kotlin.com.intellij.psi.PsiElementVisitor
import org.jetbrains.kotlin.com.intellij.psi.PsiManager
import org.jetbrains.kotlin.com.intellij.psi.impl.source.tree.LeafPsiElement
import org.jetbrains.kotlin.com.intellij.psi.tree.IElementType
import org.jetbrains.kotlin.com.intellij.testFramework.LightVirtualFile
import org.jetbrains.kotlin.extensions.PreprocessedVirtualFileFactoryExtension
import org.jetbrains.kotlin.fir.resolve.initialTypeOfCandidate
import org.jetbrains.kotlin.lexer.KtTokens
import org.jetbrains.kotlin.psi.*
import org.jetbrains.kotlin.psi.psiUtil.*
import org.jetbrains.kotlin.resolve.calls.util.getType
import org.jetbrains.kotlin.resolve.calls.util.isFakePsiElement
import org.jetbrains.kotlin.util.isAnnotated
import javax.security.auth.kerberos.KerberosTicket
import kotlin.math.exp

open class static_metadata {
    open val name : String;
    open val type : String?;
    open val nowr : Boolean;

    constructor(n : String, t : String?, r : Boolean) {
        name = n;
        type = t;
        nowr = r;
    }
};

open class preprocessor : PreprocessedVirtualFileFactoryExtension {
    open val proj : Project;

    companion object {
        open val CMPS : Key<Set<static_metadata>> = Key("cmps");
    };

    constructor(project: Project) {
        proj = project;
    }

    override fun createPreprocessedFile(file: VirtualFile?): VirtualFile? {
        if (file != null) {
            var out : String = ""; // this isnt automatically initialized
            val src : KtFile = PsiManager.getInstance(proj).findFile(file) as KtFile;

            src.accept(object : KtVisitorVoid() {
                open var known_companions = emptySet<KtObjectDeclaration>();

                override fun visitElement(element: PsiElement) {
                    if (element is LeafPsiElement) {
                        val e = element as LeafPsiElement;
                        /*if (element.elementType == KtTokens.OPEN_QUOTE || element.elementType == KtTokens.CLOSING_QUOTE) {
                            out += "\""
                        }*/
                    }else {
                        element.acceptChildren(this);
                        super.visitElement(element)
                    }
                    //out += "/* ${element} */" // - ${element.javaClass.name}*/"
                }

                override fun visitImportDirective(importDirective: KtImportDirective) {
                    out += "import ${importDirective.importPath};";
                }

                override fun visitStringTemplateExpression(expression: KtStringTemplateExpression) {
                    out += "\""
                    expression.entries.forEach({
                        it.accept(this)
                    });
                    out += "\""
                }

                override fun visitLiteralStringTemplateEntry(entry: KtLiteralStringTemplateEntry) {
                    out += entry.text;
                }

                override fun visitClass(klass: KtClass) {
                    out += "class ${klass.name}";
                    super.visitClass(klass);
                    out += ";";
                }

                override fun visitClassBody(classBody: KtClassBody) {
                    out += "{";
                    super.visitClassBody(classBody);

                    out += "companion object{"
                    classBody.allCompanionObjects.forEach({

                    });
                    classBody.parent.getUserData<Set<static_metadata>>(CMPS)?.forEach({
                                             out += "@me.xwashere.xuc_kt.internal.deleted @me.xwashere.xuc_kt.internal.static_companion ${if(it.nowr) { "val" } else { "var" }} ${it.name}";
                        if (it.type != null) out += ":${it.type}";
                                             out += " get(){return Object()";
                        if (it.type != null) out += " as ${it.type}"
                                             out += ";}";
                        if (!it.nowr) {      out += " set(v";
                        if (it.type != null) out += ":${it.type}";
                                             out += "){};"; // thats right, it does nothing :thumbsup:
                        }
                    });
                    out += "}}";
                }

                override fun visitProperty(property: KtProperty) {
                    if (property.isAnnotated) {
                        property.annotationEntries.forEach({
                            out += "@" + it.shortName + " "

                            if (it.shortName.toString() == "static") {
                                var companions : Set<static_metadata> = property.containingClassOrObject?.getUserData(CMPS) ?: emptySet();
                                companions = companions + static_metadata(property.name ?: "", property.typeReference?.nameForReceiverLabel(), !property.isVar);
                                property.containingClassOrObject?.putUserData(CMPS, companions);
                            }
                        })
                    }

                    out += "${if (property.isVar) { "var" } else { "val" } /* i miss the ternary operator this is horrifying */} ${property.name}";
                    if (property.colon != null) {
                        out += ":" + property.typeReference?.nameForReceiverLabel();
                    }

                    val initializer = property.initializer;
                    if (initializer != null) {
                        out += "="
                        initializer.accept(this)
                    }

                    out += ";"
                }

                override fun visitAnnotation(annotation: KtAnnotation) {
                    out += "@"
                    super.visitAnnotation(annotation)
                }

                override fun visitNamedFunction(function: KtNamedFunction) {
                    out += "fun ${function.nameAsSafeName}";
                    visitParameterList(function.valueParameterList!!);
                    if (function.hasDeclaredReturnType()) {
                        out += ":"
                        function.typeReference?.accept(this);
                    }
                    if (function.hasBody()) {
                        function.bodyExpression?.accept(this);
                    }
                    out += ";"
                }

                override fun visitParameterList(list: KtParameterList) {
                    out += "(";
                    super.visitParameterList(list)
                    out += ")";
                }

                override fun visitParameter(parameter: KtParameter) {
                    out += parameter.nameAsSafeName.toString();
                    if (parameter.colon != null) {
                        out += ":${parameter.typeReference?.nameForReceiverLabel()}";
                    }
                    out += ","
                }

                override fun visitBlockExpression(expression: KtBlockExpression) {
                    out += "{"
                    super.visitBlockExpression(expression)
                    out += "}"
                }

                override fun visitCallExpression(expression: KtCallExpression) {
                    expression.calleeExpression!!.accept(this)
                    visitValueArgumentList(expression.valueArgumentList!!)
                }

                override fun visitValueArgumentList(list: KtValueArgumentList) {
                    out += "(";
                    super.visitValueArgumentList(list)
                    out += ")";
                }

                override fun visitArgument(argument: KtValueArgument) {
                    super.visitArgument(argument)
                    out += ","
                }

                override fun visitReferenceExpression(expression: KtReferenceExpression) {
                    out += expression.firstChild.text
                }

                override fun visitDotQualifiedExpression(expression: KtDotQualifiedExpression) {
                    expression.firstChild.accept(this);
                    out += "."
                    expression.lastChild.accept(this);
                }

                override fun visitReturnExpression(expression: KtReturnExpression) {
                    out += "return "
                    expression.returnedExpression?.accept(this);
                }
            })


            //throw Exception("src:\n" + file.contentsToByteArray().decodeToString() + "\nout:\n" + out);

            return LightVirtualFile(file.name, out);
        }

        return file;
    }

    override fun createPreprocessedLightFile(file: LightVirtualFile?): LightVirtualFile? {
        return file;
    }

    override fun isPassThrough(): Boolean {
        return false;
    }
}