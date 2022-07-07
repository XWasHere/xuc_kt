package me.xwashere.xuc_kt

import org.jetbrains.kotlin.descriptors.DeclarationDescriptor
import org.jetbrains.kotlin.descriptors.Modality
import org.jetbrains.kotlin.extensions.DeclarationAttributeAltererExtension
import org.jetbrains.kotlin.lexer.KtTokens
import org.jetbrains.kotlin.psi.KtModifierListOwner

open class kotlin_dcl_alterer : DeclarationAttributeAltererExtension {
    override fun refineDeclarationModality(
        modifierListOwner: KtModifierListOwner,
        declaration: DeclarationDescriptor?,
        containingDeclaration: DeclarationDescriptor?,
        currentModality: Modality,
        isImplicitModality: Boolean
    ): Modality? {
        if (!isImplicitModality && modifierListOwner.hasModifier(KtTokens.FINAL_KEYWORD)) {
            return Modality.FINAL;
        } else {
            return Modality.OPEN;
        }
    }
}