package dev.cianjur.expense.util

import java.text.NumberFormat
import java.util.Currency
import java.util.Locale

class CurrencyFormatter(private val locale: Locale = Locale.getDefault()) {

    private val numberFormat = NumberFormat.getCurrencyInstance(locale).apply {
        currency = Currency.getInstance(locale)
    }

    fun format(amount: Double): String {
        return numberFormat.format(amount)
    }

    fun formatWithoutSymbol(amount: Double): String {
        return numberFormat.format(amount).replace(numberFormat.currency?.symbol ?: "$", "").trim()
    }

    fun formatCompact(amount: Double): String {
        return when {
            amount >= 1_000_000 -> String.format("%.1fM", amount / 1_000_000)
            amount >= 1_000 -> String.format("%.1fK", amount / 1_000)
            else -> format(amount)
        }
    }

    fun getSign(): String {
        return numberFormat.currency?.symbol ?: "$"
    }
}
