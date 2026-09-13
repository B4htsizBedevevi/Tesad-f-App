package com.tesaduf.app.data

import com.tesaduf.app.BuildConfig
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.signInAnonymously
import io.github.jan.supabase.createSupabaseClient

val supabase = createSupabaseClient(
    supabaseUrl = BuildConfig.SUPABASE_URL,
    supabaseKey = BuildConfig.SUPABASE_PUBLISHABLE_KEY
) {
    install(Auth)
}

suspend fun ensureAnonymousSession(): Result<String> = runCatching {
    supabase.auth.currentUserOrNull()?.id ?: run {
        supabase.auth.signInAnonymously()
        supabase.auth.currentUserOrNull()?.id
            ?: error("Anonim oturum oluşturulamadı")
    }
}
