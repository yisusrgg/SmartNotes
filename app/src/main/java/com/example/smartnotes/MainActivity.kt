package com.example.smartnotes

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.rememberNavController
import com.example.smartnotes.ui.navigation.SmartNotesNavHost
import com.example.smartnotes.ui.theme.SmartNotesTheme

class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            //CALCULAR LA CLASE DE TAMAÑO DE VENTANA
            val windowSizeClass = calculateWindowSizeClass(this)
            //Extraer solo la clase de ancho, que es la que usa mi lógica adaptativa
            val windowWidthSizeClass = windowSizeClass.widthSizeClass
            SmartNotesTheme {
                App(windowWidthSizeClass)
            }
        }
    }
}


@Composable
fun App(windowWidthSizeClass: androidx.compose.material3.windowsizeclass.WindowWidthSizeClass) {
    val navController = rememberNavController()
    SmartNotesNavHost(navController = navController, windowSizeClass = windowWidthSizeClass)
}
