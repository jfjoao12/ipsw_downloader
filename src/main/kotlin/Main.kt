import androidx.compose.animation.ExperimentalSharedTransitionApi
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
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material3.*
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import layout.CustomCardLayout
import operations.IpswFileStatus
import operations.Operations
import java.io.File
import java.util.prefs.Preferences
import javax.swing.JFileChooser
import javax.swing.UIManager
import androidx.compose.material.IconButton
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.window.WindowPosition
import androidx.compose.ui.window.rememberWindowState
import operations.FileRow


@OptIn(ExperimentalMaterial3Api::class, ExperimentalSharedTransitionApi::class)
@Composable
@Preview
fun AppUI(
    folderPath: String,
    files: List<IpswFileStatus>,
    isLoading: Boolean,
    onSelectFolder: () -> Unit,
    onCheckAndUpdate: () -> Unit,

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
                        Row(
                            modifier = Modifier
                                .fillMaxWidth(),

                        ){


                            Row(
                                horizontalArrangement = Arrangement.End,
                            ) {
                                IconButton(
                                    onClick = onCheckAndUpdate,
                                    enabled = !isLoading,
                                    content = {
                                        Icon(
                                            imageVector = Icons.Default.Refresh,
                                            contentDescription = "Check Folder")
                                    },
                                )
                                IconButton(
                                    onClick = onCheckAndUpdate,
                                    enabled = !isLoading,
                                    content = {
                                        Icon(
                                            imageVector = Icons.Default.Download,
                                            contentDescription = "Check Folder")
                                    },
                                )

                            }
                        }

                        LazyColumn(
                            modifier = Modifier
                                .fillMaxWidth(),
                            horizontalAlignment =  Alignment.CenterHorizontally
                        ) {
                            if (files.isNotEmpty()) {
                                items(files) { status ->
                                    FileRow(
                                        status,
                                        isLoading,
                                    )
                                }

                            } else {
                                item {
                                    Text("No files found")
                                }
                            }
                        }
                    }
                }


                Spacer(
                    Modifier.height(16.dp)
                )
                if (isLoading){
                    CircularProgressIndicator()
                } else {
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
}




@OptIn(DelicateCoroutinesApi::class)
fun main() = application {

    val prefs   = Preferences.userRoot().node("com.example.ipswdownloader")
    val lastPath= prefs.get("lastFolderPath", "")
    val ioScope = CoroutineScope(Dispatchers.IO)

    var isLoading by remember { mutableStateOf(false) }

    val fixedState = rememberWindowState(
        position = WindowPosition.Aligned(Alignment.Center),
        size     = DpSize(width = 800.dp, height = 600.dp)
    )

//    GlobalScope.launch {
//        ApiCalls().fetchDevices()
//    }

    Window(
        onCloseRequest = ::exitApplication,
        title = "IPSW Manager",
        state = fixedState,
        resizable = false,
    ) {
        var folderPath by remember { mutableStateOf(lastPath) }
        var files      by remember { mutableStateOf(listOf<IpswFileStatus>()) }

        LaunchedEffect(folderPath) {
            if (folderPath.isNotBlank()) {
                val dir = File(folderPath)
                files = if (dir.exists() && dir.isDirectory) {
                    dir.listFiles { f -> f.extension.equals("ipsw", ignoreCase = true) }
                        .orEmpty()
                        .map { IpswFileStatus(it) }
                } else {
                    emptyList()
                }
            }
        }

        AppUI(
            folderPath = folderPath,
            files = files,
            isLoading = isLoading,
            onSelectFolder = {
                try {
                    UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName())
                } catch (_: Exception) { /* ignore */
                }

                val chooser = JFileChooser().apply {
                    fileSelectionMode = JFileChooser.DIRECTORIES_ONLY
                    dialogTitle = "Select IPSW Folder"
                }

                if (chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {

                    folderPath = chooser.selectedFile.absolutePath

                    // *** Persist immediately ***
                    prefs.put("lastFolderPath", folderPath)

                    // refresh file list
                    files = chooser.selectedFile
                        .listFiles { f -> f.extension.equals("ipsw", ignoreCase = true) }
                        .orEmpty()
                        .map { IpswFileStatus(it) }
                }
            },
            onCheckAndUpdate = {
                ioScope.launch {
                    isLoading = true

                    val operations = Operations()

                    withContext(Dispatchers.Main) {

                        val updatedList = files.map { file ->
                            file.copy(isUpToDate = operations.buildDevice(file.file.name).isUpToDate)
                        }

                        files = updatedList
                        isLoading = false
                    }
                }
            },
        )
    }
}

