package com.tesaduf.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewmodel.compose.viewModel
import com.tesaduf.app.ui.TesadufApp
import com.tesaduf.app.ui.TesadufSplash
import com.tesaduf.app.ui.TesadufViewModel
import com.tesaduf.app.ui.theme.TesadufTheme
import kotlinx.coroutines.delay

class MainActivity:ComponentActivity(){
    override fun onCreate(savedInstanceState:Bundle?){
        super.onCreate(savedInstanceState)
        setContent{
            TesadufTheme{
                var splashVisible by rememberSaveable{mutableStateOf(true)}
                LaunchedEffect(Unit){delay(1800);splashVisible=false}
                if(splashVisible){
                    TesadufSplash(onFinished={splashVisible=false})
                }else{
                    val vm:TesadufViewModel=viewModel()
                    TesadufApp(vm)
                }
            }
        }
    }
}
