package dev.cianjur.expense.di

import dev.cianjur.expense.data.remote.KtorClient
import dev.cianjur.expense.data.remote.SupabaseService
import io.github.jan.supabase.auth.Auth
import org.koin.dsl.module

val networkModule = module {
    single {
        SupabaseService(
            supabaseUrl = "https://fpgclxdyjltqlwohdzdn.supabase.co",
            supabaseKey = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImZwZ2NseGR5amx0cWx3b2hkemRuIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NDM1MTg2MjEsImV4cCI6MjA1OTA5NDYyMX0.6awwOVr793UnisfTlzL9g9s54mA3rS0NbiAKCahFRck",
        )
    }

    single { provideAuthService(get()) }

    factory { (token: String?) -> KtorClient.create(token) }
}

private fun provideAuthService(supabaseService: SupabaseService): Auth {
    return supabaseService.createAuthService()
}
