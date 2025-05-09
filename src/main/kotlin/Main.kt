import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import layout.CustomCardLayout
import operations.ApiCalls
import operations.Device
import operations.FileInfo
import operations.IpswFileStatus
import operations.Operations
import javax.swing.JFileChooser
import javax.swing.UIManager


@OptIn(ExperimentalMaterial3Api::class)
@Composable
@Preview
fun AppUI(
    folderPath: String,
    files: List<IpswFileStatus>,
    onSelectFolder: () -> Unit,
    onCheckAndUpdate: () -> Unit
) {
    MaterialTheme(
        colorScheme = lightColorScheme()
    ) {
        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                    title = { Text("IPSW Manager") }
                )
            }
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp)
            ) {
                // 1) Folder picker row
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Button(onClick = onSelectFolder) {
                        Icon(Icons.Default.FolderOpen, contentDescription = "Select Folder")
                        Spacer(Modifier.width(8.dp))
                        Text("Select Folder")
                    }
                    Spacer(Modifier.width(16.dp))
                    Text(
                        text = folderPath.ifBlank { "No folder selected" },
                    )
                }

                Spacer(Modifier.height(24.dp))
                Row (
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {

                    CustomCardLayout(
                        "Files Found",
                    ) {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFFF5F5F5))
                        ) {
                            items(files) { status ->
                                FileRow(status)
                            }
                        }
                    }
                }


                Spacer(Modifier.height(16.dp))


                Button(
                    onClick = onCheckAndUpdate,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0078D7))
                ) {
                    Text("Check & Update", color = Color.White)
                }
            }
        }
    }
}

@Composable
fun FileRow(status: IpswFileStatus) {
    val fileName = status.file.name
    val operations = Operations()


    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp, horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = "ID: ${operations.extractIdentifierPart(fileName).name}" +
                    "Version: ${
                        operations.extractIdentifierPart(fileName).version}",
            modifier = Modifier.weight(1f),
        )

        Text(
            text = fileName,
        )

        Box{

            when (status.isUpToDate) {
                true -> Icon(Icons.Default.CheckCircle, "Up-to-date", tint = Color(0xFF4CAF50))
                false -> Icon(Icons.Default.Warning, "Needs update", tint = Color(0xFFF44336))
                null -> Text("–")
            }
        }
    }
}

@OptIn(DelicateCoroutinesApi::class)
fun main() = application {
    var folderPath by remember { mutableStateOf("") }
    var files by remember { mutableStateOf(listOf<IpswFileStatus>()) }
    var filesInfo by remember { mutableStateOf(listOf<FileInfo>()) }
    var devices by remember { mutableStateOf(listOf<Device>()) }
    var icon by remember {  mutableStateOf(Icons.Filled.Sync) }

    var operations = Operations()

    GlobalScope.launch {
        ApiCalls().grabAlliPhonesVersions()
    }

    Window(onCloseRequest = ::exitApplication, title = "IPSW Manager") {
        AppUI(
            folderPath = folderPath,
            files = files,
            onSelectFolder = {
                try {
                    UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName())
                } catch (_: Exception) { /* ignore */ }

                val chooser = JFileChooser().apply {
                    fileSelectionMode = JFileChooser.DIRECTORIES_ONLY
                    dialogTitle = "Select IPSW Folder"
                }

                if (chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
                    val folder = chooser.selectedFile
                    folderPath = folder.absolutePath
                    files = folder
                        .listFiles { f -> f.extension.equals("ipsw", ignoreCase = true) }
                        .orEmpty()
                        .map { IpswFileStatus(it) }
                }
            },
            onCheckAndUpdate = {
//                files.forEach { file ->
//                    val fileInfo = operations.extractIdentifierPart(file.file.name)
//                    filesInfo += fileInfo
//                }
                files.forEach { file ->
                    coroutineScope(Dispatchers.IO) {
                        file.isUpToDate = operations.isUpdated(file.file.name)

                    }
                }

            }
        )
    }
}

