package com.codeprep.app.ui.ai

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.codeprep.app.ui.theme.ElectricCyan
import com.codeprep.app.ui.theme.IceWhite
import com.mikepenz.markdown.compose.LocalMarkdownPadding
import com.mikepenz.markdown.compose.LocalMarkdownTypography
import com.mikepenz.markdown.compose.components.MarkdownComponent
import com.mikepenz.markdown.compose.elements.MarkdownCodeBlock
import com.mikepenz.markdown.compose.elements.MarkdownCodeFence
import com.mikepenz.markdown.compose.elements.material.MarkdownBasicText
import dev.snipme.highlights.Highlights
import dev.snipme.highlights.model.BoldHighlight
import dev.snipme.highlights.model.ColorHighlight
import dev.snipme.highlights.model.SyntaxLanguage
import dev.snipme.highlights.model.SyntaxTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.intellij.markdown.ast.ASTNode

private val CodeBlockBackground = Color(0xFF161B22)
private val CodeBlockBorder = Color(0xFF30363D)
private val CodeBlockHeaderBackground = Color(0xFF0D1117)
private val CodeBlockHeaderText = Color(0xFFC9D1D9)
private val CodeBlockCopyBackground = Color(0xFF21262D)
private val CodeBlockCopyBorder = Color(0xFF3B434B)
private val CodeBlockCopyText = Color(0xFFE6EDF3)

private val CodePrepNeutralSyntaxTheme = SyntaxTheme(
    key = "codeprep-neutral",
    code = 0xE6EDF3,
    keyword = 0xC792EA,
    string = 0xA5D6A7,
    literal = 0xF6BD60,
    comment = 0x8B949E,
    metadata = 0x82AAFF,
    multilineComment = 0x6E7681,
    punctuation = 0xC9D1D9,
    mark = 0x56B6C2
)

@Composable
internal fun rememberCodePrepHighlightsBuilder(): Highlights.Builder {
    return remember {
        Highlights.Builder().theme(CodePrepNeutralSyntaxTheme)
    }
}

internal val codePrepHighlightedCodeFence: MarkdownComponent = {
    AiMarkdownHighlightedCodeFence(
        content = it.content,
        node = it.node
    )
}

internal val codePrepHighlightedCodeBlock: MarkdownComponent = {
    AiMarkdownHighlightedCodeBlock(
        content = it.content,
        node = it.node
    )
}

@Composable
internal fun AiMarkdownHighlightedCodeFence(
    content: String,
    node: ASTNode,
    style: TextStyle = LocalMarkdownTypography.current.code,
    highlightsBuilder: Highlights.Builder = rememberCodePrepHighlightsBuilder(),
    immediate: Boolean = LocalInspectionMode.current
) {
    MarkdownCodeFence(content, node, style) { code, language, resolvedStyle ->
        AiMarkdownHighlightedCode(
            code = code,
            language = language,
            style = resolvedStyle,
            highlightsBuilder = highlightsBuilder,
            immediate = immediate
        )
    }
}

@Composable
internal fun AiMarkdownHighlightedCodeBlock(
    content: String,
    node: ASTNode,
    style: TextStyle = LocalMarkdownTypography.current.code,
    highlightsBuilder: Highlights.Builder = rememberCodePrepHighlightsBuilder(),
    immediate: Boolean = LocalInspectionMode.current
) {
    MarkdownCodeBlock(content, node, style) { code, language, resolvedStyle ->
        AiMarkdownHighlightedCode(
            code = code,
            language = language,
            style = resolvedStyle,
            highlightsBuilder = highlightsBuilder,
            immediate = immediate
        )
    }
}

@Composable
private fun AiMarkdownHighlightedCode(
    code: String,
    language: String?,
    style: TextStyle,
    highlightsBuilder: Highlights.Builder,
    immediate: Boolean
) {
    val codeHighlights by produceHighlightsState(
        code = code,
        language = language,
        highlightsBuilder = highlightsBuilder,
        immediate = immediate
    )
    val codeBlockPadding = LocalMarkdownPadding.current.codeBlock

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(CodeBlockBackground)
            .border(width = 1.dp, color = CodeBlockBorder, shape = RoundedCornerShape(16.dp))
    ) {
        AiCodeBlockHeader(
            language = language,
            code = code
        )

        MarkdownBasicText(
            text = codeHighlights,
            style = style.copy(color = IceWhite),
            modifier = Modifier
                .horizontalScroll(rememberScrollState())
                .padding(codeBlockPadding)
        )
    }
}

@Composable
private fun AiCodeBlockHeader(
    language: String?,
    code: String
) {
    @Suppress("DEPRECATION")
    val clipboardManager = LocalClipboardManager.current
    val title = language
        ?.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
        ?: "Code"

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(CodeBlockHeaderBackground)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            color = CodeBlockHeaderText,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            fontFamily = FontFamily.Monospace
        )

        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(999.dp))
                .background(CodeBlockCopyBackground)
                .border(1.dp, CodeBlockCopyBorder, RoundedCornerShape(999.dp))
                .clickable {
                    clipboardManager.setText(AnnotatedString(code))
                }
                .padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(16.dp)
                    .clip(CircleShape)
                    .background(ElectricCyan.copy(alpha = 0.18f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "⧉",
                    color = ElectricCyan,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
            Text(
                text = "Copy",
                color = CodeBlockCopyText,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
private fun produceHighlightsState(
    code: String,
    language: String?,
    highlightsBuilder: Highlights.Builder,
    immediate: Boolean
): State<AnnotatedString> {
    if (immediate) {
        val highlighted = remember(code) {
            buildHighlightedAnnotatedString(code, language, highlightsBuilder)
        }
        return rememberUpdatedState(highlighted)
    }

    return produceState(
        initialValue = AnnotatedString(text = code),
        key1 = code
    ) {
        val job = launch(Dispatchers.Default) {
            value = buildHighlightedAnnotatedString(code, language, highlightsBuilder)
        }
        awaitDispose {
            job.cancel()
        }
    }
}

private fun buildHighlightedAnnotatedString(
    code: String,
    language: String?,
    highlightsBuilder: Highlights.Builder
): AnnotatedString {
    val syntaxLanguage = language?.let { SyntaxLanguage.getByName(it) }
    val codeHighlights = highlightsBuilder
        .code(code)
        .let { if (syntaxLanguage != null) it.language(syntaxLanguage) else it }
        .build()
        .getHighlights()

    return buildAnnotatedString {
        append(code)
        codeHighlights.forEach { highlight ->
            val style = when (highlight) {
                is ColorHighlight -> SpanStyle(color = Color(highlight.rgb).copy(alpha = 1f))
                is BoldHighlight -> SpanStyle(fontWeight = FontWeight.Bold)
            }
            addStyle(
                style = style,
                start = highlight.location.start,
                end = highlight.location.end
            )
        }
    }
}
