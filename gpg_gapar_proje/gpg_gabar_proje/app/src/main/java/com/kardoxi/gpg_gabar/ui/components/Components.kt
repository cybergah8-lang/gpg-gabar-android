package com.kardoxi.gpg_gabar.ui.components

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.kardoxi.gpg_gabar.ui.theme.LocalElevations
import com.kardoxi.gpg_gabar.ui.theme.LocalSpacing

@Composable
fun AppButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    fullWidth: Boolean = true,
    style: ButtonStyle = ButtonStyle.Filled,
    enabled: Boolean = true,
) {
    val sp = LocalSpacing.current
    val shape = MaterialTheme.shapes.medium
    val colors = when (style) {
        ButtonStyle.Filled -> ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
            disabledContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
            disabledContentColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.5f)
        )
        ButtonStyle.Outlined -> ButtonDefaults.outlinedButtonColors(
            contentColor = MaterialTheme.colorScheme.primary
        )
    }

    val baseModifier = modifier
        .then(if (fullWidth) Modifier.fillMaxWidth() else Modifier)
        .padding(vertical = sp.sm)

    when (style) {
        ButtonStyle.Filled -> Button(
            onClick = onClick,
            shape = shape,
            enabled = enabled,
            colors = colors,
            contentPadding = PaddingValues(horizontal = sp.lg, vertical = sp.sm),
            modifier = baseModifier
        ) { Text(text) }
        ButtonStyle.Outlined -> OutlinedButton(
            onClick = onClick,
            shape = shape,
            enabled = enabled,
            colors = colors,
            contentPadding = PaddingValues(horizontal = sp.lg, vertical = sp.sm),
            modifier = baseModifier
        ) { Text(text) }
    }
}

enum class ButtonStyle { Filled, Outlined }

@Composable
fun AnimatedCard(
    modifier: Modifier = Modifier,
    elevation: Int = 2,
    content: @Composable () -> Unit
) {
    val el = LocalElevations.current
    val elev = when (elevation) {
        0 -> el.level0
        1 -> el.level1
        2 -> el.level2
        3 -> el.level3
        4 -> el.level4
        else -> el.level5
    }
    Card(
        modifier = modifier.animateContentSize(),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = elev)
    ) {
        content()
    }
}
