package su.nnedition.kord.ui.screen.admin.page

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminCreateUserDialog(
    onDismissRequest: () -> Unit,
    onConfirm: (
        username: String,
        password: String,
        email: String,
        isAdmin: Boolean,
        canStream: Boolean,
        canDownload: Boolean,
        canUpload: Boolean,
        maxBitRate: Int?
    ) -> Unit
) {
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var isAdmin by remember { mutableStateOf(false) }
    var canStream by remember { mutableStateOf(true) }
    var canDownload by remember { mutableStateOf(false) }
    var canUpload by remember { mutableStateOf(false) }

    val bitRateOptions = listOf(
        0 to "Без ограничений",
        320 to "320 kbps (HQ)",
        256 to "256 kbps",
        192 to "192 kbps (Standard)",
        128 to "128 kbps (LQ)"
    )
    var selectedBitRateIndex by remember { mutableStateOf(0) }
    var bitRateExpanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismissRequest,
        shape = RoundedCornerShape(16.dp),
        title = {
            Text(
                text = "Создать пользователя",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = username,
                    onValueChange = { username = it },
                    label = { Text("Имя пользователя") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Пароль") },
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                ExposedDropdownMenuBox(
                    expanded = bitRateExpanded,
                    onExpandedChange = { bitRateExpanded = it }
                ) {
                    OutlinedTextField(
                        value = bitRateOptions[selectedBitRateIndex].second,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Лимит битрейта") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = bitRateExpanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable)
                    )
                    ExposedDropdownMenu(
                        expanded = bitRateExpanded,
                        onDismissRequest = { bitRateExpanded = false }
                    ) {
                        bitRateOptions.forEachIndexed { index, (_, label) ->
                            DropdownMenuItem(
                                text = { Text(label) },
                                onClick = {
                                    selectedBitRateIndex = index
                                    bitRateExpanded = false
                                }
                            )
                        }
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = isAdmin, onCheckedChange = { isAdmin = it })
                    Text("Права администратора", style = MaterialTheme.typography.bodyMedium)
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = canStream, onCheckedChange = { canStream = it })
                    Text("Стриминг музыки", style = MaterialTheme.typography.bodyMedium)
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = canDownload, onCheckedChange = { canDownload = it })
                    Text("Скачивание треков", style = MaterialTheme.typography.bodyMedium)
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = canUpload, onCheckedChange = { canUpload = it })
                    Text("Загрузка файлов на сервер", style = MaterialTheme.typography.bodyMedium)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (username.isNotBlank() && password.isNotBlank()) {
                        val bitRate = bitRateOptions[selectedBitRateIndex].first.takeIf { it > 0 }
                        onConfirm(
                            username.trim(),
                            password,
                            email.trim(),
                            isAdmin,
                            canStream,
                            canDownload,
                            canUpload,
                            bitRate
                        )
                    }
                },
                enabled = username.isNotBlank() && password.isNotBlank()
            ) {
                Text("Создать")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismissRequest) {
                Text("Отмена")
            }
        }
    )
}

@Preview
@Composable
private fun AdminCreateUserDialogPreview() {
    AdminCreateUserDialog(
        onDismissRequest = {},
        onConfirm = { _, _, _, _, _, _, _, _ -> }
    )
}
