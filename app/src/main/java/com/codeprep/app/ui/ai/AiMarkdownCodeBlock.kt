package com.codeprep.app.ui.ai

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import com.codeprep.app.ui.components.CodePrepCodeBlock
import com.codeprep.app.ui.components.rememberCodePrepHighlightsBuilder
import com.codeprep.app.ui.theme.IceWhite
import com.mikepenz.markdown.compose.LocalMarkdownPadding
import com.mikepenz.markdown.compose.LocalMarkdownTypography
import com.mikepenz.markdown.compose.components.MarkdownComponent
import com.mikepenz.markdown.compose.elements.MarkdownCodeBlock
import com.mikepenz.markdown.compose.elements.MarkdownCodeFence
import dev.snipme.highlights.Highlights
import org.intellij.markdown.ast.ASTNode

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
    val codeBlockPadding = LocalMarkdownPadding.current.codeBlock

    MarkdownCodeFence(content, node, style) { code, language, resolvedStyle ->
        CodePrepCodeBlock(
            code = code,
            language = language,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            textStyle = resolvedStyle.copy(color = IceWhite),
            contentPadding = codeBlockPadding,
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
    val codeBlockPadding = LocalMarkdownPadding.current.codeBlock

    MarkdownCodeBlock(content, node, style) { code, language, resolvedStyle ->
        CodePrepCodeBlock(
            code = code,
            language = language,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            textStyle = resolvedStyle.copy(color = IceWhite),
            contentPadding = codeBlockPadding,
            highlightsBuilder = highlightsBuilder,
            immediate = immediate
        )
    }
}
