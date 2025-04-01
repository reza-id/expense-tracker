package dev.cianjur.expense.di

import dev.cianjur.expense.data.remote.KtorClient
import dev.cianjur.expense.data.remote.SupabaseService
import org.koin.dsl.module

val networkModule = module {
    single {
        SupabaseService(
            supabaseUrl = "YOUR_SUPABASE_URL",
            supabaseKey = "YOUR_SUPABASE_KEY",
        )
    }

    factory { (token: String?) -> KtorClient.create(token) }
}
