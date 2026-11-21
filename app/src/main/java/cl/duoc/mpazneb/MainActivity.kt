package cl.duoc.mpazneb

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import cl.duoc.mpazneb.ui.navigation.MpazNavHost
import cl.duoc.mpazneb.ui.theme.MpazTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MpazTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    MpazNavHost()
                }
            }
        }
    }
}
