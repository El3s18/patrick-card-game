package com.example.patrick


import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.patrick.ui.theme.PatrickTheme
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.ui.unit.dp
import com.example.patrick.model.Carte
import com.example.patrick.model.melangerPaquet
import com.example.patrick.ui.components.CarteVisuelle
import androidx.compose.foundation.layout.Row

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            PatrickTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    EcranDeTest(modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }
}

@Composable
fun EcranDeTest(modifier: Modifier = Modifier) {
    var paquet by remember { mutableStateOf(melangerPaquet().toMutableList()) }
    var main by remember { mutableStateOf(mutableListOf<Carte>()) }
    var selection by remember { mutableStateOf(listOf<Carte>()) }

    fun toggleSelection(carte: Carte) {
        selection = if (selection.contains(carte)) {
            selection - carte
        } else {
            selection + carte
        }
    }

    Column(modifier = modifier.padding(16.dp)) {
        Button(onClick = {
            val carte = paquet.removeAt(0)
            main = (main + carte).toMutableList()
        }) {
            Text("Piocher")
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row {
            for (carte in main) {
                CarteVisuelle(
                    carte = carte,
                    selectionnee = selection.contains(carte),
                    onClick = { toggleSelection(carte) }
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text("Cartes sélectionnées : ${selection.size}")
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    PatrickTheme {
        Greeting("Android")
    }
}
