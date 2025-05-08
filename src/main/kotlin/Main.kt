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
import androidx.compose.material.icons.filled.Downloading
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.io.File
import java.net.URL
import javax.swing.JFileChooser
import javax.swing.UIManager

@Serializable
data class Device(
    val name: String,
    val identifier: String
)


suspend fun grabAlliPhonesVersions(): List<Device> {
    val url = "https://api.ipsw.me/v4/devices"

    val jsonString = withContext(Dispatchers.IO) {
        URL(url).readText()
    }

    val json = Json { ignoreUnknownKeys = true }

    val devices: List<Device> = json.decodeFromString(jsonString)

    val filterForIphones = "iPhone*".toRegex()

    val devicesList: MutableList<Device>  = mutableListOf()

    devices.forEach { device ->
        if(device.name.contains("iPad")){
            println("> ${device.name} (id: ${device.identifier})")

            devicesList += device
        }
    }

    return devicesList
}

fun extractIdentifierPart(file: String): String {
    // 1) get just the filename (no path)
    val name = file
        .removeSuffix(".ipsw")   // drop extension

    // 2) pull out everything up to the underscore before the version (which starts with a digit)
    val regex = Regex("^(.*?)(?=_\\d)")
    val splitName = name.split("_")
    return regex.find(name)?.groups?.get(1)?.value
        ?: name  // fallback if the pattern somehow doesn’t match
}

data class IpswFileStatus(
    val file: File,
    var isUpToDate: Boolean? = null  // null = not yet checked
)

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
                                .weight(1f)
                                .fillMaxWidth()
                                .background(Color(0xFFF5F5F5))
                        ) {
                            items(files) { status ->
                                FileRow(status)
                            }
                        }
                    }

                    CustomCardLayout("Files Downloaded") {
                        LazyColumn(
                            modifier = Modifier
                                .weight(1f)
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


    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp, horizontal = 12.dp)
            .background(color = MaterialTheme.colorScheme.primary),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = extractIdentifierPart(status.file.name),
            modifier = Modifier.weight(1f),
        )

        when (status.isUpToDate) {
            true -> Icon(Icons.Default.CheckCircle, "Up-to-date", tint = Color(0xFF4CAF50))
            false -> Icon(Icons.Default.Warning, "Needs update", tint = Color(0xFFF44336))
            null -> Text("–")
        }
    }
}

@OptIn(DelicateCoroutinesApi::class)
fun main() = application {
    var folderPath by remember { mutableStateOf("") }
    var files by remember { mutableStateOf(listOf<IpswFileStatus>()) }
    var devices by remember { mutableStateOf(listOf<Device>()) }
    var icon by remember {  mutableStateOf(Icons.Filled.Sync) }

    GlobalScope.launch {
        grabAlliPhonesVersions()
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
                // TODO: Loop through 'files', check versions via your API
                // and update each IpswFileStatus.isUpToDate accordingly.
                // Then trigger downloads for those where isUpToDate == false.
            }
        )
    }
}

@Composable
fun CustomCardLayout(title: String = "", content: @Composable () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth(0.5f)
            .padding(16.dp)
    ){
        ElevatedCard(
            shape = RoundedCornerShape(12.dp),
            elevation = CardDefaults.cardElevation(4.dp),
            modifier = Modifier
                .fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 28.dp, start = 16.dp, end = 8.dp, bottom = 16.dp),
            ) {
                // Content here
                content()
            }
        }

        if(title != ""){
            Surface(
                color = MaterialTheme.colorScheme.secondaryContainer,
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .offset(y = (-12).dp)
                    .zIndex(1f)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }

    }
}

