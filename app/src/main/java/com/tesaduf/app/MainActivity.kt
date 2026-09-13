package com.tesaduf.app

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            Log.e("TESADUF_CRASH","Uncaught exception on ${thread.name}",throwable)
        }
        setContent{
            TesadufTheme{
                var splashVisible by remember{mutableStateOf(true)}
                var appReady by remember{mutableStateOf(false)}
                LaunchedEffect(Unit){
                    delay(1800)
                    appReady=true
                    splashVisible=false
                }
                if(splashVisible){
                    TesadufSplash(onFinished={
                        if(!appReady){
                            appReady=true
                            splashVisible=false
                        }
                    })
                }else{
                    val vm:TesadufViewModel=viewModel()
                    TesadufApp(vm)
                }
            }
        }
    }
}
