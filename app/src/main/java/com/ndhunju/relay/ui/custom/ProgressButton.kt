package com.ndhunju.relay.ui.custom

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.ndhunju.relay.ui.theme.LocalDimens

@Composable
fun ProgressButton(
    onClick: () -> Unit,
    labelStrRes: Int,
    showSpinner: State<Boolean>
) {
    //Log.d("TAG", "ProgressButton: called")
    Button(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = LocalDimens.current.itemPaddingVertical),
        onClick = onClick
    ) {
        if (showSpinner.value) {
            CircularProgressIndicator()
        } else {
            Text(text = stringResource(id = labelStrRes))
        }
    }
}