package su.nnedition.kord.ui.screen.admin.page

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import su.nnedition.kord.data.model.User
import su.nnedition.kord.data.remote.KordClient
import su.nnedition.kord.ui.screen.common.Page

object AdminUsersPage : Page {
    override val title: String = "Пользователи"
    override val icon: ImageVector = Icons.Default.People

    @Composable
    override fun Content(modifier: Modifier) {
        val scope = rememberCoroutineScope()
        var users by remember { mutableStateOf<List<User>>(emptyList()) }
        var isLoading by remember { mutableStateOf(false) }
        var errorMessage by remember { mutableStateOf<String?>(null) }
        var showCreateDialog by remember { mutableStateOf(false) }
        var userToDelete by remember { mutableStateOf<User?>(null) }

        fun loadUsers() {
            scope.launch {
                isLoading = true
                errorMessage = null
                KordClient.default.getUsers()
                    .onSuccess {
                        users = it
                        isLoading = false
                    }
                    .onFailure {
                        errorMessage = it.message ?: "Ошибка загрузки списка пользователей"
                        isLoading = false
                    }
            }
        }

        LaunchedEffect(Unit) {
            loadUsers()
        }

        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Пользователи сервера",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Управление учётными записями и правами доступа",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { loadUsers() }) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Обновить список"
                        )
                    }

                    Button(
                        onClick = { showCreateDialog = true }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.size(6.dp))
                        Text("Создать")
                    }
                }
            }

            if (isLoading && users.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else if (errorMessage != null && users.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = errorMessage ?: "",
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Spacer(modifier = Modifier.size(8.dp))
                        Button(onClick = { loadUsers() }) {
                            Text("Повторить")
                        }
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(bottom = 16.dp)
                ) {
                    items(users, key = { it.username }) { user ->
                        AdminUserItem(
                            user = user,
                            onDeleteClick = { userToDelete = it }
                        )
                    }
                }
            }
        }

        if (showCreateDialog) {
            AdminCreateUserDialog(
                onDismissRequest = { showCreateDialog = false },
                onConfirm = { username, password, email, isAdmin, canStream, canDownload ->
                    scope.launch {
                        KordClient.default.createUser(
                            username = username,
                            password = password,
                            email = email,
                            adminRole = isAdmin,
                            streamRole = canStream,
                            downloadRole = canDownload
                        ).onSuccess {
                            showCreateDialog = false
                            loadUsers()
                        }.onFailure {
                            errorMessage = it.message ?: "Ошибка создания пользователя"
                        }
                    }
                }
            )
        }

        userToDelete?.let { user ->
            AlertDialog(
                onDismissRequest = { userToDelete = null },
                title = { Text("Удаление пользователя") },
                text = { Text("Вы действительно хотите удалить пользователя '${user.username}'?") },
                confirmButton = {
                    Button(
                        onClick = {
                            val target = user.username
                            userToDelete = null
                            scope.launch {
                                KordClient.default.deleteUser(target)
                                    .onSuccess { loadUsers() }
                                    .onFailure { errorMessage = it.message }
                            }
                        }
                    ) {
                        Text("Удалить")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { userToDelete = null }) {
                        Text("Отмена")
                    }
                }
            )
        }
    }
}

@Preview
@Composable
private fun AdminUsersPagePreview() {
    AdminUsersPage.Content(modifier = Modifier.fillMaxSize())
}
