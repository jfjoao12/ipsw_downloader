import androidx.compose.animation.AnimatedVisibility
import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
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
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.onClick
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ModalDrawer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.ModeStandby
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerEvent
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.onPointerEvent
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import layout.CustomCardLayout
import operations.Device
import operations.FileInfo
import operations.IpswFileStatus
import operations.Operations
import java.io.File
import java.util.prefs.Preferences
import javax.swing.JFileChooser
import javax.swing.UIManager
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Outline


@OptIn(ExperimentalMaterial3Api::class)
@Composable
@Preview
fun AppUI(
    folderPath: String,
    files: List<IpswFileStatus>,
    isLoading: Boolean,
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
                                .fillMaxWidth(),
                            horizontalAlignment =  Alignment.CenterHorizontally
                        ) {
                            if (files.isNotEmpty()) {
                                items(files) { status ->
                                    FileRow(status)
                                    HorizontalDivider(
                                        modifier = Modifier
                                            .fillMaxWidth(0.3f)
                                            .align(Alignment.CenterVertically),
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

@OptIn(ExperimentalComposeUiApi::class, ExperimentalFoundationApi::class)
@Composable
fun FileRow(device: IpswFileStatus) {
    val fileName = device.file.name
    val operations = Operations()

    var deviceInfo: Device? by remember { mutableStateOf<Device?>(null) }
    var active by remember { mutableStateOf(false) }

    LaunchedEffect(device.file) {
        deviceInfo = operations.buildDevice(device.file.name)
    }




        Box(
            modifier = Modifier
                .padding(vertical = 2.dp,  horizontal = 12.dp)

        ){

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp, horizontal = 12.dp)
                    .combinedClickable(
                        onClick = {
                            active = !active
                        },
                    )
                    .drawBehind {
                        // convert 1.dp into pixels
                        val strokeWidth = 1.dp.toPx()
                        // left edge
                        drawLine(
                            color = Color.Black,
                            start = Offset(0f, 0f),
                            end   = Offset(0f, size.height),
                            strokeWidth = strokeWidth
                        )
                    },
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(
                    modifier = Modifier
                        .padding(8.dp, 4.dp)

                ) {

                    Row(
                        modifier =  Modifier
                            .padding(horizontal = 6.dp),
                        horizontalArrangement =  Arrangement.Start,
                    ) {
                        Text(
                            modifier =  Modifier
                                .padding(horizontal = 6.dp),
                            text = deviceInfo?.apiDevice?.name ?: "Loading..",
                            color = Color.Black,
                            fontWeight =  FontWeight.Medium,
                            textAlign = TextAlign.Center,
                        )

                        Text(
                            modifier =  Modifier
                                .padding(end = 6.dp),
                            text = "${deviceInfo?.currentVersion} >> ${deviceInfo?.latestVersion}" ?: "Loading..",
                            color = Color.Gray,
                            fontWeight =  FontWeight.Medium,
                            fontStyle =  FontStyle.Italic,
                            textAlign = TextAlign.Center,
                        )
                    }
                    AnimatedVisibility(active) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp, horizontal = 12.dp),
                        ) {
                            ElevatedCard  (
                                Modifier,
                                shape = RoundedCornerShape(8.dp),
                            ){
                                Column{
                                    Text(
                                        text = deviceInfo?.apiDevice?.name ?: "Loading...",
                                        color = Color.Black,
                                        fontWeight =  FontWeight.Bold,
                                        textAlign = TextAlign.Center,
                                    )
                                    Text(
                                        text = ("${deviceInfo?.apiDevice?.identifier}"),
                                    )
                                    Text(
                                        text = ("Current version: ${deviceInfo?.currentVersion}"),
                                    )
                                    Text(
                                        text = ("Latest version: ${deviceInfo?.latestVersion}"),
                                    )
                                }
                            }
                        }
                    }
                }
                Row{

                    Text(
                        fileName,
                        color = Color.Black,
                    )
                    Box{

                        when (device.isUpToDate) {
                            true -> Icon(Icons.Default.CheckCircle, "Up-to-date", tint = Color(0xFF4CAF50))
                            false -> Icon(Icons.Default.Warning, "Needs update", tint = Color(0xFFF44336))
                            null -> Icon(Icons.Default.ModeStandby, "Not checked", tint = Color(0xFFF44336))
                        }
                    }
                }
            }
        }
//        Column {
//            AnimatedVisibility (!active) {
//                Box(
//
//                ){
//                    Text(
//                        fileName,
//                        color = Color.Black,
//                    )
//                }
//            }
//      }

}

@OptIn(DelicateCoroutinesApi::class)
fun main() = application {

    val prefs   = Preferences.userRoot().node("com.example.ipswdownloader")
    val lastPath= prefs.get("lastFolderPath", "")
    val ioScope = CoroutineScope(Dispatchers.IO)

    var isLoading by remember { mutableStateOf(false) }

//    GlobalScope.launch {
//        ApiCalls().fetchDevices()
//    }

    Window(
        onCloseRequest = ::exitApplication,
        title = "IPSW Manager"
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
            isLoading =  isLoading,
            onSelectFolder = {
                try {
                    UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName())
                } catch (_: Exception) { /* ignore */ }

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
            }
        )
    }
}

