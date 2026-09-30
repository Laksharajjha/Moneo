package com.moneo.app.presentation.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import com.moneo.app.ai.parser.AmountParser
import com.moneo.app.domain.model.TransactionType
import com.moneo.app.presentation.theme.MoneoGreen
import com.moneo.app.presentation.theme.MoneoGreenDark
import com.moneo.app.presentation.theme.MoneoRed
import com.moneo.app.presentation.theme.MoneoRedDark
import androidx.compose.foundation.isSystemInDarkTheme

/**
 * Displays a monetary amount with appropriate styling.
 * Colors are applied based on transaction type.
 */
@Composable
fun AmountText(
    amountInPaise: Long,
    modifier: Modifier = Modifier,
    type: TransactionType? = null,
    style: TextStyle = MaterialTheme.typography.titleLarge,
    applyTypeColor: Boolean = true
) {
    val isDark = isSystemInDarkTheme()
    val color: Color = when {
        !applyTypeColor -> MaterialTheme.colorScheme.onSurface
        type == TransactionType.INCOME || type == TransactionType.REFUND || type == TransactionType.REPAYMENT -> if (isDark) MoneoGreenDark else MoneoGreen
        else -> MaterialTheme.colorScheme.onSurface
    }
    val prefix = when (type) {
        TransactionType.INCOME, TransactionType.REFUND, TransactionType.REPAYMENT -> "+"
        TransactionType.EXPENSE, TransactionType.LEND -> "-"
        else -> ""
    }
    Text(
        text = "$prefix${AmountParser.formatPaise(amountInPaise)}",
        style = style.copy(fontWeight = FontWeight.Medium),
        color = color,
        modifier = modifier
    )
}
