package com.tesaduf.app.data

import com.tesaduf.app.BuildConfig
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest

object Supabase {
    // V1 uses direct HTTP calls for Edge Functions, so Realtime is intentionally
    // not initialized at app startup. This keeps startup lighter and avoids an
    // unnecessary websocket dependency until push updates are introduced.
    val client = createSupabaseClient(BuildConfig.SUPABASE_URL, BuildConfig.SUPABASE_KEY) {
        install(Auth) { alwaysAutoRefresh = true }
        install(Postgrest)
    }
}
