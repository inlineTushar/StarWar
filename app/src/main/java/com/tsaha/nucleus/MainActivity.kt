package com.tsaha.nucleus

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.tsaha.nucleus.navgraph.MainNavGraph
import com.tsaha.nucleus.ui.theme.NucleusTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            NucleusTheme {
                MainNavGraph()
            }
        }
    }
}
