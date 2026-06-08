package com.sujana

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.getValue
import com.sujana.core.navigation.RootNavGraph
import com.sujana.core.theme.SujanaTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val mainViewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SujanaTheme {
                val session by mainViewModel.session.collectAsStateWithLifecycle()
                RootNavGraph(
                    sessionUser = session.user,
                    sessionLoaded = session.loaded,
                )
            }
        }
    }
}
