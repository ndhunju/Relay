package com.ndhunju.barcode.ui.generator

import android.graphics.Bitmap
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Preview
@Composable
fun BarcodeGeneratorScreen(
    body: State<String> = mutableStateOf("Scan this QR code from another device"),
    bitmap: State<Bitmap?> = mutableStateOf(null)
) {
    MaterialTheme {
        Surface(Modifier.fillMaxSize()) {
            Scaffold(topBar = { Image(
                imageVector = Icons.Default.ArrowBack,
                contentDescription = "Back",
                modifier = Modifier.padding(16.dp)
            )}) { innerPadding ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Show progress indicator until QR code is generated
                    if (bitmap.value == null) {
                        CircularProgressIndicator(
                            modifier = Modifier
                                .fillMaxWidth(0.5f)
                                .fillMaxHeight(0.3f),
                            )
                    }

                    AnimatedVisibility(visible = bitmap.value != null, enter = scaleIn()) {
                        Image(
                            modifier = Modifier
                                .fillMaxWidth(0.5f)
                                .fillMaxHeight(0.3f),
                            //imageVector = Icons.Default.DateRange,
                            bitmap = bitmap.value?.asImageBitmap() ?: ImageBitmap(0, 0),
                            contentDescription = ""
                        )
                    }

                    Text(text = body.value, Modifier.padding(48.dp))
                }
            }
        }
    }
}