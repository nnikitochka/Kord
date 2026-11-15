package su.nnedition.kord.ui.screen.main

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Composable
@Preview
fun MainViewScreen() {
    Row(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
    ) {
        Card(
            modifier = Modifier
                .fillMaxHeight()
                .padding(6.dp, 6.dp, 0.dp, 6.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxHeight()
                    .padding(8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                IconButton(onClick = { /* TODO: Home */ }) {
                    Icon(
                        imageVector = Icons.Default.Home,
                        contentDescription = "Домашняя страница"
                    )
                }
                IconButton(onClick = { /* TODO: Search */ }) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Поиск"
                    )
                }

                Spacer(modifier = Modifier.weight(1f))

                IconButton(onClick = { /* TODO: Settings */ }) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "Настройки"
                    )
                }
            }
        }

        Spacer(Modifier.padding(horizontal = 3.dp))

        Card(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .padding(0.dp, 6.dp, 6.dp, 6.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxHeight()
                    .padding(8.dp),
            ) {
                Text("52")
            }
        }
    }
}
