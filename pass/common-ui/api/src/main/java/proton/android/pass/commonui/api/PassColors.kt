package proton.android.pass.commonui.api

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

@Immutable
data class PassColors(

    val interactionNormContrast: Color,
    val interactionNormMajor1: Color,
    val interactionNorm: Color,
    val interactionNormMinor1: Color,
    val interactionNormMinor2: Color,

    val loginInteractionNormMajor1: Color,
    val loginInteractionNorm: Color,
    val loginInteractionNormMinor1: Color,
    val loginInteractionNormMinor2: Color,

    val aliasInteractionNormMajor1: Color,
    val aliasInteractionNorm: Color,
    val aliasInteractionNormMinor1: Color,
    val aliasInteractionNormMinor2: Color,

    val noteInteractionNormMajor1: Color,
    val noteInteractionNorm: Color,
    val noteInteractionNormMinor1: Color,
    val noteInteractionNormMinor2: Color,

    val passwordInteractionNormMajor1: Color,
    val passwordInteractionNorm: Color,
    val passwordInteractionNormMinor1: Color,
    val passwordInteractionNormMinor2: Color,

    val textNorm: Color,
    val textWeak: Color,
    val textHint: Color,
    val textDisabled: Color,
    val textInvert: Color,

    val inputBackground: Color,
    val inputBorder: Color,
    val inputBorderFocused: Color,

    val backgroundNorm: Color,
    val backgroundWeak: Color,
    val backgroundStrong: Color,
    val backgroundStrongest: Color,

    val signalDanger: Color,
    val signalWarning: Color,
    val signalSuccess: Color,
    val signalInfo: Color,
    val signalNorm: Color,

    val backdrop: Color,

    val vaultColor1: Color,
    val vaultColor2: Color,
    val vaultColor3: Color,
    val vaultColor4: Color,
    val vaultColor5: Color,
    val vaultColor6: Color,
    val vaultColor7: Color,
    val vaultColor8: Color,
    val vaultColor9: Color,
    val vaultColor10: Color,
) {
    companion object {
        val Dark = PassColors(
            interactionNormContrast = PassPalette.White100,
            interactionNormMajor1 = PassPalette.VeryLightBlue100,
            interactionNorm = PassPalette.VeryLightBlue80,
            interactionNormMinor1 = PassPalette.VeryLightBlue16,
            interactionNormMinor2 = PassPalette.VeryLightBlue8,
            loginInteractionNormMajor1 = PassPalette.Lavender100,
            loginInteractionNorm = PassPalette.Lavender80,
            loginInteractionNormMinor1 = PassPalette.Lavender16,
            loginInteractionNormMinor2 = PassPalette.Lavender8,
            aliasInteractionNormMajor1 = PassPalette.GreenSheen100,
            aliasInteractionNorm = PassPalette.GreenSheen80,
            aliasInteractionNormMinor1 = PassPalette.GreenSheen16,
            aliasInteractionNormMinor2 = PassPalette.GreenSheen8,
            noteInteractionNormMajor1 = PassPalette.MacaroniAndCheese100,
            noteInteractionNorm = PassPalette.MacaroniAndCheese80,
            noteInteractionNormMinor1 = PassPalette.MacaroniAndCheese16,
            noteInteractionNormMinor2 = PassPalette.MacaroniAndCheese8,
            passwordInteractionNormMajor1 = PassPalette.VenetianRed100,
            passwordInteractionNorm = PassPalette.VenetianRed80,
            passwordInteractionNormMinor1 = PassPalette.VenetianRed16,
            passwordInteractionNormMinor2 = PassPalette.VenetianRed8,
            textNorm = PassPalette.White80,
            textWeak = PassPalette.White40,
            textHint = PassPalette.White24,
            textDisabled = PassPalette.White8,
            textInvert = PassPalette.EerieBlack,
            inputBackground = PassPalette.White4,
            inputBorder = PassPalette.SilverWhite,
            inputBorderFocused = PassPalette.Lavender8,
            backgroundNorm = PassPalette.EerieBlack,
            backgroundWeak = PassPalette.DarkGunmetal,
            backgroundStrong = PassPalette.ChineseBlack80,
            backgroundStrongest = PassPalette.SmokyBlack,
            signalDanger = PassPalette.VanillaIce,
            signalWarning = PassPalette.PastelOrange,
            signalSuccess = PassPalette.OceanGreen,
            signalInfo = PassPalette.PictonBlue,
            signalNorm = PassPalette.White100,
            backdrop = PassPalette.Black32,
            vaultColor1 = PassPalette.Heliotrope,
            vaultColor2 = PassPalette.Mauvelous,
            vaultColor3 = PassPalette.MarigoldYellow,
            vaultColor4 = PassPalette.DeYork,
            vaultColor5 = PassPalette.JordyBlue,
            vaultColor6 = PassPalette.LavenderMagenta,
            vaultColor7 = PassPalette.ChestnutRose,
            vaultColor8 = PassPalette.Porsche,
            vaultColor9 = PassPalette.Mercury,
            vaultColor10 = PassPalette.WaterLeaf,
        )
        val Light = PassColors(
            interactionNormContrast = PassPalette.SmokyBlack,
            interactionNormMajor1 = PassPalette.Iris,
            interactionNorm = PassPalette.Indigo,
            interactionNormMinor1 = PassPalette.Lilac,
            interactionNormMinor2 = PassPalette.Magnolia,
            loginInteractionNormMajor1 = PassPalette.BlueViolet,
            loginInteractionNorm = PassPalette.LavenderFloral,
            loginInteractionNormMinor1 = PassPalette.LavenderPink,
            loginInteractionNormMinor2 = PassPalette.LilacMist,
            aliasInteractionNormMajor1 = PassPalette.GreenCyan,
            aliasInteractionNorm = PassPalette.ShamrockGreen,
            aliasInteractionNormMinor1 = PassPalette.AzureishWhite,
            aliasInteractionNormMinor2 = PassPalette.Honeydew,
            noteInteractionNormMajor1 = PassPalette.Bronze,
            noteInteractionNorm = PassPalette.VeryLightTangelo,
            noteInteractionNormMinor1 = PassPalette.Flesh,
            noteInteractionNormMinor2 = PassPalette.OldLace,
            passwordInteractionNormMajor1 = PassPalette.Tulip,
            passwordInteractionNorm = PassPalette.LightSalmonPink,
            passwordInteractionNormMinor1 = PassPalette.Pink,
            passwordInteractionNormMinor2 = PassPalette.Linen,
            textNorm = PassPalette.DarkCharcoal,
            textWeak = PassPalette.GraniteGray,
            textHint = PassPalette.SilverChalice,
            textDisabled = PassPalette.BrightGray,
            textInvert = PassPalette.White100,

            inputBackground = PassPalette.Cultured,
            inputBorder = PassPalette.SilverWhite,
            inputBorderFocused = PassPalette.Lavender8,

            backgroundNorm = PassPalette.White100,
            backgroundWeak = PassPalette.Sapphire8,
            backgroundStrong = PassPalette.Whisper,
            backgroundStrongest = PassPalette.AliceBlue,

            signalDanger = PassPalette.DingyDungeon,
            signalWarning = PassPalette.Persimmon,
            signalSuccess = PassPalette.SpanishViridian,
            signalInfo = PassPalette.Cyan,
            signalNorm = PassPalette.RichBlack,
            backdrop = PassPalette.Black32,

            vaultColor1 = PassPalette.Lavender100,
            vaultColor2 = PassPalette.Begonia,
            vaultColor3 = PassPalette.AmericanYellow,
            vaultColor4 = PassPalette.JungleGreen,
            vaultColor5 = PassPalette.CornflowerBlue,
            vaultColor6 = PassPalette.RosePink,
            vaultColor7 = PassPalette.BrickRed,
            vaultColor8 = PassPalette.DeepSaffron,
            vaultColor9 = PassPalette.SpanishGray,
            vaultColor10 = PassPalette.MaximumBlueGreen,
        )
    }
}

val LocalPassColors = staticCompositionLocalOf {
    PassColors(
        interactionNormContrast = Color.Unspecified,
        interactionNormMajor1 = Color.Unspecified,
        interactionNorm = Color.Unspecified,
        interactionNormMinor1 = Color.Unspecified,
        interactionNormMinor2 = Color.Unspecified,
        loginInteractionNormMajor1 = Color.Unspecified,
        loginInteractionNorm = Color.Unspecified,
        loginInteractionNormMinor1 = Color.Unspecified,
        loginInteractionNormMinor2 = Color.Unspecified,
        aliasInteractionNormMajor1 = Color.Unspecified,
        aliasInteractionNorm = Color.Unspecified,
        aliasInteractionNormMinor1 = Color.Unspecified,
        aliasInteractionNormMinor2 = Color.Unspecified,
        noteInteractionNormMajor1 = Color.Unspecified,
        noteInteractionNorm = Color.Unspecified,
        noteInteractionNormMinor1 = Color.Unspecified,
        noteInteractionNormMinor2 = Color.Unspecified,
        passwordInteractionNormMajor1 = Color.Unspecified,
        passwordInteractionNorm = Color.Unspecified,
        passwordInteractionNormMinor1 = Color.Unspecified,
        passwordInteractionNormMinor2 = Color.Unspecified,
        textNorm = Color.Unspecified,
        textWeak = Color.Unspecified,
        textHint = Color.Unspecified,
        textDisabled = Color.Unspecified,
        textInvert = Color.Unspecified,
        inputBackground = Color.Unspecified,
        inputBorder = Color.Unspecified,
        inputBorderFocused = Color.Unspecified,
        backgroundNorm = Color.Unspecified,
        backgroundWeak = Color.Unspecified,
        backgroundStrong = Color.Unspecified,
        backgroundStrongest = Color.Unspecified,
        signalDanger = Color.Unspecified,
        signalWarning = Color.Unspecified,
        signalSuccess = Color.Unspecified,
        signalInfo = Color.Unspecified,
        signalNorm = Color.Unspecified,
        backdrop = Color.Unspecified,
        vaultColor1 = Color.Unspecified,
        vaultColor2 = Color.Unspecified,
        vaultColor3 = Color.Unspecified,
        vaultColor4 = Color.Unspecified,
        vaultColor5 = Color.Unspecified,
        vaultColor6 = Color.Unspecified,
        vaultColor7 = Color.Unspecified,
        vaultColor8 = Color.Unspecified,
        vaultColor9 = Color.Unspecified,
        vaultColor10 = Color.Unspecified
    )
}
