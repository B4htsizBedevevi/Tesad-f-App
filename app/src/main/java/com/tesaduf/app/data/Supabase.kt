package com.tesaduf.app.data

import com.tesaduf.app.BuildConfig
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.realtime.Realtime

object Supabase {
    val client = createSupabaseClient(BuildConfig.SUPABASE_URL, BuildConfig.SUPABASE_KEY) {
        install(Auth) { alwaysAutoRefresh = true }
        install(Postgrest)
        install(Realtime)
    }
}
