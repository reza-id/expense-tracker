package dev.cianjur.expense.data.repository

import android.util.Log
import dev.cianjur.expense.domain.repository.AuthenticationRepository
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.auth.providers.builtin.Email

class AuthenticationRepositoryImpl(
    private val auth: Auth,
) : AuthenticationRepository {

    override suspend fun signIn(email: String, password: String): Boolean {
        return try {
            auth.signInWith(Email) {
                this.email = email
                this.password = password
            }
            true
        } catch (e: Exception) {
            Log.e("AGUSSS", "Error signing in", e)
            false
        }
    }

    override suspend fun signUp(email: String, password: String): Boolean {
        return try {
            auth.signUpWith(Email) {
                this.email = email
                this.password = password
            }
            true
        } catch (e: Exception) {
            Log.e("AGUSSS", "Error signing up", e)
            false
        }
    }

}