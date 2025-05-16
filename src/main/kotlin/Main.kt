import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color.Companion.Blue
import androidx.compose.ui.graphics.Color.Companion.Cyan
import androidx.compose.ui.graphics.Color.Companion.Magenta
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.onPointerEvent
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.window.WindowPosition
import androidx.compose.ui.window.rememberWindowState
import operations.FileRow


@OptIn(ExperimentalMaterial3Api::class, ExperimentalSharedTransitionApi::class, ExperimentalComposeUiApi::class)
@Composable
@Preview
fun AppUI(
    folderPath: String,
    files: List<IpswFileStatus>,
    isLoading: Boolean,
    onSelectFolder: () -> Unit,
    onCheckAndUpdate: () -> Unit,

) {
    // To handle description of icons
    var expandFolder by remember {mutableStateOf(false)}
    var expandRefresh by remember {mutableStateOf(false)}
    var expandDownload by remember {mutableStateOf(false)}

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
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        modifier = Modifier
                            .padding(horizontal = 16.dp),
                        verticalAlignment = Alignment.Bottom,
                        horizontalArrangement = Arrangement.Start
                    ) {
                        Text(
                            text = "Download & Update",
                            fontWeight = FontWeight.Bold,
                        )
                    }

                    Row(
                        modifier = Modifier
                            .padding(horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement =  Arrangement.End,
                    ) {

                        AnimatedVisibility(expandFolder) {
                            Text(
                                text = "Select folder",
                            )
                        }
                        IconButton(
                            modifier = Modifier
                                .onPointerEvent(PointerEventType.Enter){
                                    expandFolder = true
                                }
                                .onPointerEvent(PointerEventType.Exit) {
                                    expandFolder = false
                                },
                            onClick = onSelectFolder
                        ) {
                            Icon(Icons.Default.FolderOpen, contentDescription = "Select Folder")
                        }
                        AnimatedVisibility(expandRefresh) {
                            Text(
                                text = "Refresh",
                            )
                        }
                        IconButton(
                            modifier = Modifier
                                .onPointerEvent(PointerEventType.Enter){
                                    expandRefresh = true
                                }
                                .onPointerEvent(PointerEventType.Exit) {
                                    expandRefresh = false
                                },
                            onClick = onCheckAndUpdate,
                            enabled = !isLoading,
                            content = {
                                Icon(
                                    imageVector = Icons.Default.Refresh,
                                    contentDescription = "Check Folder")
                            },
                        )
                        AnimatedVisibility(expandDownload) {
                            Text(
                                text = "Download",
                            )
                        }
                        IconButton(
                            modifier = Modifier
                                .onPointerEvent(PointerEventType.Enter){
                                    expandDownload = true
                                }
                                .onPointerEvent(PointerEventType.Exit) {
                                    expandDownload = false
                                },
                            onClick = onCheckAndUpdate,
                            enabled = !isLoading,
                            content = {
                                Icon(
                                    imageVector = Icons.Default.Download,
                                    contentDescription = "Download Files")
                            },
                        )
//                    Button(onClick = onSelectFolder) {
//                        Icon(Icons.Default.FolderOpen, contentDescription = "Select Folder")
//                        Spacer(Modifier.width(8.dp))
//                        Text("Select Folder")
//                    }
//                    Spacer(Modifier.width(16.dp))
//                    Text(
//                        text = folderPath.ifBlank { "No folder selected" },
//                    )
                    }
                }
                Row (
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {

                    CustomCardLayout {
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

