package com.tesaduf.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.lifecycle.viewmodel.compose.viewModel
import com.tesaduf.app.ui.TesadufApp
import com.tesaduf.app.ui.TesadufViewModel
import com.tesaduf.app.ui.theme.TesadufTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            TesadufTheme {
                val vm: TesadufViewModel = viewModel()
                TesadufApp(vm)
            }
        }
    }
}
