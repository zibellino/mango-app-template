// No package declaration — this file lives flat in java/ with no folder
// nesting, and isn't tied to app.package in app.properties. Kotlin doesn't
// require source layout to mirror package structure, so this works fine.
//
// This IS your MainActivity, not a throwaway placeholder — build your real
// UI directly into this file rather than deleting it and starting a
// separate one. If you later want it under a real package, add a
// `package ...` line as the first line and move the file — but nothing
// here requires that.

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            Surface(modifier = Modifier.fillMaxSize(), color = Color.Black) {
                Box(modifier = Modifier.fillMaxSize())
            }
        }
    }
}
