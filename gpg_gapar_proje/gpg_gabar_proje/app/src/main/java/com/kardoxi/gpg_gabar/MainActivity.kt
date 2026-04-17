package com.kardoxi.gpg_gabar

import android.os.Bundle
import android.app.Activity
import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import android.widget.Toast
import android.view.WindowManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.BackHandler
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.clickable
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.background
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.Divider
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.VpnKey
import androidx.compose.material.icons.filled.IosShare
import androidx.compose.material.icons.filled.FileOpen
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.RadioButton

import androidx.compose.material3.Text
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.material3.TextField
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.focus.FocusState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import com.kardoxi.gpg_gabar.ui.theme.AzadiTheme
import android.provider.OpenableColumns
import android.provider.DocumentsContract
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.window.DialogProperties
import android.net.Uri
import android.provider.Settings
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.withContext
import kotlinx.coroutines.delay
import androidx.compose.material3.LinearProgressIndicator
import android.os.Handler
import android.os.Looper
import android.os.FileObserver
import android.os.PowerManager
import java.text.SimpleDateFormat
import java.util.Date
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import com.kardoxi.gpg_gabar.PassphraseStore
import java.security.SecureRandom
import java.text.Collator
import java.util.Locale
import androidx.compose.animation.AnimatedVisibility
import com.kardoxi.gpg_gabar.ui.components.AppButton
import com.kardoxi.gpg_gabar.ui.components.ButtonStyle
import com.kardoxi.gpg_gabar.ui.components.AnimatedCard
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import androidx.documentfile.provider.DocumentFile
import org.apache.commons.compress.archivers.tar.TarArchiveEntry
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream

private data class LangOption(val tag: String, val label: String, val code: String)

// UI order requested by user:
// Kurmanji, Sorani, English, French, German, Russian, Spanish, Portuguese, Arabic, Persian, Chinese, Turkish
private val languageOptionsOrdered: List<LangOption> = listOf(
    LangOption("ku", "Kurmancî (KU)", "KU"),
    LangOption("ckb", "Soranî (CKB)", "CKB"),
    LangOption("en", "English (EN)", "EN"),
    LangOption("fr", "Français (FR)", "FR"),
    LangOption("de", "Deutsch (DE)", "DE"),
    LangOption("ru", "Русский (RU)", "RU"),
    LangOption("es", "Español (ES)", "ES"),
    LangOption("pt", "Português (PT)", "PT"),
    LangOption("ar", "العربية (AR)", "AR"),
    LangOption("fa", "فارسی (FA)", "FA"),
    LangOption("zh", "中文 (ZH)", "ZH"),
    LangOption("tr", "Türkçe (TR)", "TR"),
)

// Export mode for ExportScreen
enum class ExportMode { PUBLIC_ONLY, BOTH }

// Inserts a suffix before the last dot extension. If no extension, appends suffix at the end.
private fun insertSuffixBeforeExtension(name: String, suffix: String): String {
    val idx = name.lastIndexOf('.')
    return if (idx > 0 && idx < name.length - 1) {
        name.substring(0, idx) + suffix + name.substring(idx)
    } else {
        name + suffix
    }
}

// Move trailing copy index "(n)" from the very end to before the real extension.
// Examples:
//  - "vejin.zip (1)"  -> "vejin-1.zip"
//  - "vejin.zip(1)"   -> "vejin-1.zip"
//  - "vejin.zip.gpg (1)" -> "vejin-1.zip.gpg"
// Returns a fixed name or null if no trailing index pattern is found.
private fun moveTrailingCopyIndexBeforeExtension(name: String): String? {
    // Find trailing (n) with optional whitespace before it
    val m = Regex("\\s*\\((\\d+)\\)$").find(name) ?: return null
    val n = m.groupValues[1]
    val nameNoIndex = name.removeRange(m.range)

    val lastDot = nameNoIndex.lastIndexOf('.')
    if (lastDot <= 0) {
        return "$nameNoIndex-$n"
    }
    val ext = nameNoIndex.substring(lastDot + 1)

    // If outer ext is gpg/pgp and there is another dot, place the index before the inner extension
    if (ext.equals("gpg", true) || ext.equals("pgp", true)) {
        val prevDot = nameNoIndex.lastIndexOf('.', lastDot - 1)
        if (prevDot > 0) {
            val base = nameNoIndex.substring(0, prevDot)
            val innerExt = nameNoIndex.substring(prevDot + 1, lastDot)
            val outerExt = ext
            return "$base-$n.$innerExt.$outerExt"
        }
        // Fallback to placing before the last dot
    }

    val base = nameNoIndex.substring(0, lastDot)
    return "$base-$n.$ext"
}

// After CreateDocument saves a file, some providers add " (n)" at the end of the display name
// for duplicates (e.g., "file.zip (1)"). This renames it to keep the extension intact ("file-1.zip").
private fun renameIfProviderAddedSuffix(context: android.content.Context, uri: Uri) {
    try {
        val currentName = context.contentResolver
            .query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)
            ?.use { c -> if (c.moveToFirst()) c.getString(0) else null } ?: return
        val fixed = moveTrailingCopyIndexBeforeExtension(currentName) ?: return
        // Prefer DocumentsContract to rename; ignore returned Uri as we don't reuse it further here
        DocumentsContract.renameDocument(context.contentResolver, uri, fixed)
    } catch (_: Exception) { /* ignore */ }
}

private fun ensureFixedExtension(context: android.content.Context, uri: Uri, ext: String) {
    try {
        val currentName = context.contentResolver
            .query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)
            ?.use { c -> if (c.moveToFirst()) c.getString(0) else null } ?: return
        val lower = currentName.lowercase(Locale.getDefault())
        val desired = ext.lowercase(Locale.getDefault())
        if (lower.endsWith(".$desired")) return
        val dot = currentName.lastIndexOf('.')
        val base = if (dot > 0) currentName.substring(0, dot) else currentName
        DocumentsContract.renameDocument(context.contentResolver, uri, "$base.$ext")
    } catch (_: Exception) { }
}

class MainActivity : AppCompatActivity() {
    private var initialAction: String? = null
    private var initialUriString: String? = null
    private var initialSharedText: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.setFlags(
            WindowManager.LayoutParams.FLAG_SECURE,
            WindowManager.LayoutParams.FLAG_SECURE
        )
        enableEdgeToEdge()

        // Capture intent from share proxy activities (or direct)
        fun extractFromIntent(i: Intent?) {
            if (i == null) return
            var act = i.getStringExtra("share_action")
            var uriStr = i.getStringExtra("share_uri")
            var text = i.getStringExtra("share_text")
            if (!uriStr.isNullOrEmpty()) {
                // Try to take persistable permission if provided
                try {
                    val uri = Uri.parse(uriStr)
                    val flags = (i.flags and (Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION))
                    if (flags and Intent.FLAG_GRANT_READ_URI_PERMISSION != 0) {
                        contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    }
                } catch (_: Exception) { }
            } else {
                // Fallback: handle standard Android share/view intents
                try {
                    val action = i.action
                    var detectedUri: Uri? = null
                    if (Intent.ACTION_SEND == action) {
                        // Text share
                        if (text.isNullOrEmpty()) {
                            text = i.getStringExtra(Intent.EXTRA_TEXT)
                        }
                        // File share
                        runCatching {
                            val p: Uri? = i.getParcelableExtra(Intent.EXTRA_STREAM)
                            if (p != null) detectedUri = p
                        }
                    } else if (Intent.ACTION_VIEW == action) {
                        detectedUri = i.data
                    }

                    if (detectedUri != null) {
                        uriStr = detectedUri.toString()
                        // Guess encrypt vs decrypt from file name
                        var name: String? = null
                        // Avoid querying on main thread; rely on lastPathSegment
                        val fname = (name ?: detectedUri!!.lastPathSegment ?: "").lowercase()
                        act = if (fname.endsWith(".gpg") || fname.endsWith(".pgp")) {
                            "decrypt"
                        } else {
                            // Try lightweight detection: if recipient Key IDs are present, it's encrypted
                            try {
                                contentResolver.openInputStream(detectedUri!!)?.use { ins ->
                                    val ids = PgpUtils.extractRecipientKeyIds(ins)
                                    if (ids.isNotEmpty()) "decrypt" else "encrypt"
                                } ?: "encrypt"
                            } catch (_: Exception) { "encrypt" }
                        }
                        // Try to persist permission
                        val flags = (i.flags and (Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION))
                        if (flags and Intent.FLAG_GRANT_READ_URI_PERMISSION != 0) {
                            contentResolver.takePersistableUriPermission(detectedUri!!, Intent.FLAG_GRANT_READ_URI_PERMISSION)
                        }
                    } else if (!text.isNullOrEmpty()) {
                        act = if (text!!.contains("BEGIN PGP MESSAGE")) "text_decrypt" else "text"
                    }
                } catch (_: Exception) { }
            }
            initialAction = act
            initialUriString = uriStr
            initialSharedText = text
        }
        extractFromIntent(intent)

        // Set default app locale to Kurdish (Kurmanji) on first launch
        try {
            val appLocales = AppCompatDelegate.getApplicationLocales()
            if (appLocales.isEmpty) {
                AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags("ku"))
            }
        } catch (_: Exception) { }

        setContent {
            AzadiTheme {
                AppRoot(initialAction = initialAction, initialUriString = initialUriString, initialSharedText = initialSharedText)
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        // Handle both proxy extras and regular Android intents when activity is reused
        var act: String? = intent.getStringExtra("share_action")
        var uriStr: String? = intent.getStringExtra("share_uri")
        var text: String? = intent.getStringExtra("share_text")

        if (!uriStr.isNullOrEmpty()) {
            try {
                val uri = Uri.parse(uriStr)
                val flags = (intent.flags and (Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION))
                if (flags and Intent.FLAG_GRANT_READ_URI_PERMISSION != 0) {
                    contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
            } catch (_: Exception) { }
        } else {
            try {
                val action = intent.action
                var detectedUri: Uri? = null
                if (Intent.ACTION_SEND == action) {
                    if (text.isNullOrEmpty()) text = intent.getStringExtra(Intent.EXTRA_TEXT)
                    runCatching {
                        val p: Uri? = intent.getParcelableExtra(Intent.EXTRA_STREAM)
                        if (p != null) detectedUri = p
                    }
                } else if (Intent.ACTION_VIEW == action) {
                    detectedUri = intent.data
                }
                if (detectedUri != null) {
                    uriStr = detectedUri.toString()
                    val fname = (detectedUri!!.lastPathSegment ?: "").lowercase()
                    act = if (fname.endsWith(".gpg") || fname.endsWith(".pgp")) {
                        "decrypt"
                    } else {
                        try {
                            contentResolver.openInputStream(detectedUri!!)?.use { ins ->
                                val ids = PgpUtils.extractRecipientKeyIds(ins)
                                if (ids.isNotEmpty()) "decrypt" else "encrypt"
                            } ?: "encrypt"
                        } catch (_: Exception) { "encrypt" }
                    }
                    val flags = (intent.flags and (Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION))
                    if (flags and Intent.FLAG_GRANT_READ_URI_PERMISSION != 0) {
                        contentResolver.takePersistableUriPermission(detectedUri!!, Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    }
                }
            } catch (_: Exception) { }
        }

        initialAction = act
        initialUriString = uriStr
        initialSharedText = text
    }
}

@Composable
fun GradientLinearProgress(
    progress: Float?,
    modifier: Modifier = Modifier,
    height: Dp = 10.dp
) {
    val shape = RoundedCornerShape(8.dp)
    val trackColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
    val brush = Brush.horizontalGradient(
        colors = listOf(
            Color(0xFFD32F2F), // Red 700
            Color(0xFFFBC02D), // Yellow 700 (middle)
            Color(0xFF388E3C)  // Green 700
        )
    )
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
            .clip(shape)
            .background(trackColor)
    ) {
        if (progress != null) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(progress.coerceIn(0f, 1f))
                    .background(brush)
            )
        }
    }
}

@Composable
private fun HelpDialog(
    title: String,
    body: String,
    onDismiss: () -> Unit
) {
    val scroll = rememberScrollState()
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Box(
                modifier = Modifier
                    .heightIn(max = 420.dp)
                    .verticalScroll(scroll)
            ) {
                Text(body)
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(android.R.string.ok))
            }
        }
    )
}

@Composable
private fun HelpIconButton(onClick: () -> Unit) {
    IconButton(onClick = onClick) {
        androidx.compose.material3.Icon(
            imageVector = Icons.Filled.HelpOutline,
            contentDescription = stringResource(R.string.help)
        )
    }
}

@Composable
fun EncryptScreen(
    modifier: Modifier = Modifier,
    initialInUri: Uri? = null,
    onBusyChanged: (Boolean) -> Unit = {},
    onDone: () -> Unit,
) {
    val context = LocalContext.current
    val repo = remember { KeyRepository(context) }
    var names by remember { mutableStateOf(listOf<String>()) }
    var hasSecretByName by remember { mutableStateOf<Map<String, Boolean>>(emptyMap()) }
    val highlightColor = if (isSystemInDarkTheme()) Color(0xFFFFD54F) else Color(0xFFF57C00)

    var selectedNames by remember { mutableStateOf(setOf<String>()) }
    var pickedUri by remember { mutableStateOf<Uri?>(null) }
    var pickedDisplayName by remember { mutableStateOf<String?>(null) }
    var totalBytes by remember { mutableStateOf<Long?>(null) }
    var progressBytes by remember { mutableStateOf(0L) }
    var isEncrypting by remember { mutableStateOf(false) }
    var cancelEncrypt by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    var showHelp by remember { mutableStateOf(false) }

    // Disable system back while encrypting; user should press Cancel
    BackHandler(enabled = isEncrypting) { /* no-op to block back */ }

    // Forward-declared action to proceed with next export item; assigned later
    var startNextExport by remember { mutableStateOf({}) }

    LaunchedEffect(Unit) {
        val (loadedNames, secretMap) = withContext(Dispatchers.IO) {
            val ns = repo.listKeyNames()
            val m = ns.associateWith { n ->
                val sec = repo.getArmoredSecretByName(n)
                !sec.isNullOrEmpty()
            }
            ns to m
        }
        val collator = Collator.getInstance(Locale("tr", "TR")).apply { strength = Collator.PRIMARY }
        val ordered = loadedNames.sortedWith { a, b -> collator.compare(a, b) }
        names = ordered
        hasSecretByName = secretMap
    }

    // Prefill picked file if launched from share
    LaunchedEffect(initialInUri) {
        if (pickedUri == null && initialInUri != null) {
            try {
                pickedUri = initialInUri
                val info = withContext(Dispatchers.IO) {
                    var n: String? = null
                    var sz: Long? = null
                    context.contentResolver.query(initialInUri, null, null, null, null)?.use { c ->
                        val nameIdx = c.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                        val sizeIdx = c.getColumnIndex(OpenableColumns.SIZE)
                        if (nameIdx >= 0 && c.moveToFirst()) n = c.getString(nameIdx)
                        if (sizeIdx >= 0) sz = c.getLong(sizeIdx)
                    }
                    n to sz
                }
                pickedDisplayName = info.first
                totalBytes = info.second
            } catch (_: Exception) { }
        }
    }

    val openDoc = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            try {
                pickedUri = uri
                scope.launch {
                    val info = withContext(Dispatchers.IO) {
                        var n: String? = null
                        var sz: Long? = null
                        context.contentResolver.query(uri, null, null, null, null)?.use { c ->
                            val nameIdx = c.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                            val sizeIdx = c.getColumnIndex(OpenableColumns.SIZE)
                            if (nameIdx >= 0 && c.moveToFirst()) n = c.getString(nameIdx)
                            if (sizeIdx >= 0) sz = c.getLong(sizeIdx)
                        }
                        n to sz
                    }
                    pickedDisplayName = info.first
                    totalBytes = info.second
                }
                Toast.makeText(context, context.getString(R.string.file_selected), Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Toast.makeText(context, context.getString(R.string.read_failed_with_reason, e.message ?: ""), Toast.LENGTH_LONG).show()
            }
        }
    }

    val createDoc = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/octet-stream")
    ) { outUri ->
        val src = pickedUri
        val namesSel = selectedNames
        if (outUri != null && src != null && namesSel.isNotEmpty()) {
            isEncrypting = true
            onBusyChanged(true)
            progressBytes = 0L
            cancelEncrypt = false
            scope.launch {
                CryptoFgService.start(context, context.getString(R.string.encrypt_and_save))
                // Keep CPU awake during encryption
                val pm = context.getSystemService(android.content.Context.POWER_SERVICE) as PowerManager
                val wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "Azadi:Encrypt")
                try {
                    wakeLock.acquire()
                    val pubs = withContext(Dispatchers.IO) { namesSel.mapNotNull { repo.getArmoredPublicByName(it) } }
                    if (pubs.isEmpty()) {
                        Toast.makeText(context, context.getString(R.string.key_not_found), Toast.LENGTH_SHORT).show()
                        return@launch
                    }
                    withContext(Dispatchers.IO) {
                        val mainHandler = Handler(Looper.getMainLooper())
                        context.contentResolver.openInputStream(src)?.use { ins ->
                            context.contentResolver.openOutputStream(outUri)?.use { os ->
                                // Zip first, then encrypt (multi-recipient)
                                PgpUtils.encryptStreamZip(
                                    publicKeysArmored = pubs,
                                    input = ins,
                                    originalFileName = pickedDisplayName ?: "data.bin",
                                    output = os,
                                    shouldCancel = { cancelEncrypt },
                                    onProgress = { count -> mainHandler.post { progressBytes = count } }
                                )
                            } ?: throw IllegalStateException("Cannot open output stream")
                        } ?: throw IllegalStateException("Cannot open input stream")
                    }
                    runCatching {
                        withContext(Dispatchers.IO) { ensureFixedExtension(context, outUri, "gpg") }
                    }
                    // Ensure numbering stays before extension if provider added a copy index at the end
                    runCatching {
                        withContext(Dispatchers.IO) { renameIfProviderAddedSuffix(context, outUri) }
                    }
                    Toast.makeText(context, context.getString(R.string.encrypted_file_saved), Toast.LENGTH_SHORT).show()
                    onDone()
                } catch (e: java.util.concurrent.CancellationException) {
                    // Delete partial output if cancelled
                    runCatching { DocumentFile.fromSingleUri(context, outUri)?.delete() }
                    Toast.makeText(context, context.getString(R.string.operation_cancelled), Toast.LENGTH_SHORT).show()
                } catch (e: Exception) {
                    // Cleanup on failure
                    runCatching { DocumentFile.fromSingleUri(context, outUri)?.delete() }
                    Toast.makeText(context, context.getString(R.string.encrypt_failed_with_reason, e.message ?: ""), Toast.LENGTH_LONG).show()
                } finally {
                    CryptoFgService.stop(context)
                    if (wakeLock.isHeld) wakeLock.release()
                    isEncrypting = false
                    progressBytes = 0L
                    cancelEncrypt = false
                    onBusyChanged(false)
                }
            }
        }
    }

    if (showHelp) {
        HelpDialog(
            title = stringResource(R.string.help_encrypt_file_title),
            body = stringResource(R.string.help_encrypt_file_body),
            onDismiss = { showHelp = false }
        )
    }

    Column(modifier = modifier.padding(16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(id = if (pickedUri != null) R.string.encryption_file_selected else R.string.select_public_key_for_encryption),
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.weight(1f)
            )
            HelpIconButton(onClick = { showHelp = true })
        }

        // Choose file and run encryption
        Row(modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp)) {
            ElevatedButton(
                onClick = { if (!isEncrypting) openDoc.launch(arrayOf("*/*")) },
                modifier = Modifier.weight(1f),
                enabled = (!isEncrypting && pickedUri == null)
            ) {
                Text(stringResource(R.string.choose_file))
            }
            Spacer(modifier = Modifier.width(8.dp))
            ElevatedButton(onClick = {
                if (pickedUri == null) {
                    Toast.makeText(context, context.getString(R.string.please_select_file), Toast.LENGTH_SHORT).show(); return@ElevatedButton
                }
                if (selectedNames.isEmpty()) {
                    Toast.makeText(context, context.getString(R.string.please_select_key), Toast.LENGTH_SHORT).show(); return@ElevatedButton
                }
                val baseName = (pickedDisplayName ?: "data.bin")
                val dateStr = try {
                    SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(Date())
                } catch (_: Exception) { "" }
                val withDate = if (dateStr.isNotEmpty()) insertSuffixBeforeExtension(baseName, " " + dateStr) else baseName
                val lower = withDate.lowercase(Locale.getDefault())
                val suggested = if (lower.endsWith(".zip")) {
                    withDate + ".gpg"
                } else {
                    withDate + ".zip.gpg"
                }
                createDoc.launch(suggested)
            }, modifier = Modifier.weight(1f)) {
                Text(if (isEncrypting) "…" else stringResource(R.string.encrypt_and_save))
            }
        }

        // Progress
        AnimatedVisibility(visible = isEncrypting) {
            val t = totalBytes
            Spacer(modifier = Modifier.height(8.dp))
            if (t != null && t > 0) {
                val progress = (progressBytes.toFloat() / t.toFloat()).coerceIn(0f, 1f)
                AnimatedCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 20.dp)
                ) {
                    Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 12.dp)) {
                        GradientLinearProgress(progress = progress, height = 14.dp)
                        Text(text = String.format("%d%%", (progress * 100).toInt()), modifier = Modifier.padding(top = 6.dp))
                        Spacer(modifier = Modifier.height(12.dp))
                        ElevatedButton(
                            onClick = { cancelEncrypt = true },
                            modifier = Modifier.align(Alignment.CenterHorizontally)
                        ) {
                            Text(stringResource(R.string.cancel))
                        }
                    }
                }
            } else {
                AnimatedCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 20.dp)
                ) {
                    Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 12.dp)) {
                        LinearProgressIndicator(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(14.dp)
                                .clip(RoundedCornerShape(8.dp))
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        ElevatedButton(
                            onClick = { cancelEncrypt = true },
                            modifier = Modifier.align(Alignment.CenterHorizontally)
                        ) {
                            Text(stringResource(R.string.cancel))
                        }
                    }
                }
            }
        }

        // Key chooser
        Spacer(modifier = Modifier.width(8.dp))
        AnimatedCard(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 12.dp),
            elevation = 2
        ) {
            if (names.isEmpty()) {
                Text(text = stringResource(R.string.no_keys), modifier = Modifier.padding(12.dp))
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 320.dp)
                ) {
                    // Header
                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = stringResource(R.string.name),
                                style = MaterialTheme.typography.titleSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.weight(1f)
                            )
                            Text(
                                text = stringResource(R.string.type),
                                style = MaterialTheme.typography.titleSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.weight(1f)
                            )
                        }
                        Divider(thickness = 0.5.dp)
                    }

                    // Key list
                    itemsIndexed(names) { index, n ->
                        val isPrivate = hasSecretByName[n] == true
                        val rowBg = if (index % 2 == 1) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.15f) else Color.Transparent

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(rowBg)
                                .clickable(enabled = !isEncrypting) {
                                    selectedNames = if (selectedNames.contains(n)) selectedNames - n else selectedNames + n
                                }
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Checkbox
                            androidx.compose.material3.Checkbox(
                                checked = selectedNames.contains(n),
                                onCheckedChange = { isChecked ->
                                    selectedNames = if (isChecked) selectedNames + n else selectedNames - n
                                },
                                enabled = !isEncrypting
                            )

                            // Key name
                            val displayName = remember(n) {
                                val noAsc = if (n.endsWith(".asc", ignoreCase = true)) n.dropLast(4) else n
                                noAsc.replace(Regex("-(gizli|genel)$", RegexOption.IGNORE_CASE), "")
                            }
                            Text(
                                text = displayName,
                                style = if (isPrivate) MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold)
                                       else MaterialTheme.typography.bodyLarge,
                                color = if (isPrivate) highlightColor else MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.weight(1f)
                            )

                            // Key type
                            val typeText = if (isPrivate) stringResource(R.string.secret) + " + " + stringResource(R.string.public_)
                                         else stringResource(R.string.public_)
                            Text(
                                text = typeText,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.weight(1f)
                            )
                        }
                        Divider(thickness = 0.5.dp)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppRoot(initialAction: String? = null, initialUriString: String? = null, initialSharedText: String? = null) {
    var screen by rememberSaveable(stateSaver = ScreenSaver) { mutableStateOf<Screen>(Screen.Home) }
    var isBusy by remember { mutableStateOf(false) }
    val drawerState = androidx.compose.material3.rememberDrawerState(initialValue = androidx.compose.material3.DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    var showDeleteDbConfirm by remember { mutableStateOf(false) }
    var dataVersion by remember { mutableStateOf(0) }

    val prefs = remember {
        context.getSharedPreferences("gpg_gabar_prefs", android.content.Context.MODE_PRIVATE)
    }
    var showLanguagePicker by remember {
        mutableStateOf(!prefs.getBoolean("has_chosen_language", false))
    }

    fun applyLocale(tag: String) {
        AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(tag))
        prefs.edit().putBoolean("has_chosen_language", true).apply()
        showLanguagePicker = false
        (context as? Activity)?.recreate()
    }

    if (showLanguagePicker) {
        val initialSelectedTag = remember {
            val currentTag: String = try {
                val list = AppCompatDelegate.getApplicationLocales()
                if (!list.isEmpty) {
                    list.toLanguageTags().split(",").firstOrNull()?.lowercase() ?: ""
                } else {
                    @Suppress("DEPRECATION")
                    val sys = context.resources.configuration.locales.get(0)
                    sys?.toLanguageTag()?.lowercase() ?: ""
                }
            } catch (_: Exception) { "" }
            languageOptionsOrdered.firstOrNull { currentTag.startsWith(it.tag) }?.tag ?: "ku"
        }
        var pendingTag by remember { mutableStateOf(initialSelectedTag) }

        AlertDialog(
            onDismissRequest = { /* must choose */ },
            properties = DialogProperties(dismissOnBackPress = false, dismissOnClickOutside = false),
            title = {
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    androidx.compose.material3.Icon(
                        imageVector = Icons.Filled.Language,
                        contentDescription = null,
                        modifier = Modifier.size(32.dp)
                    )
                }
            },
            text = {
                Column {
                    LazyColumn(modifier = Modifier.heightIn(max = 360.dp)) {
                        items(languageOptionsOrdered.size) { idx ->
                            val opt = languageOptionsOrdered[idx]
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .selectable(
                                        selected = pendingTag == opt.tag,
                                        onClick = { pendingTag = opt.tag }
                                    )
                                    .padding(vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(selected = pendingTag == opt.tag, onClick = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(opt.label)
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { applyLocale(pendingTag) }) {
                    Text(stringResource(android.R.string.ok))
                }
            }
        )
    }

    // Watch database directory; if DB file is deleted/moved externally, refresh UI automatically
    androidx.compose.runtime.DisposableEffect(Unit) {
        val dbFile = context.getDatabasePath(DataKeyDbHelper.DATABASE_NAME)
        val dir = dbFile.parentFile
        val mask = FileObserver.DELETE or FileObserver.MOVED_FROM or FileObserver.DELETE_SELF
        val observer = if (dir != null && dir.exists()) object : FileObserver(dir.path, mask) {
            override fun onEvent(event: Int, path: String?) {
                // React only to our DB files
                val name = path ?: return
                if (name == dbFile.name || name.startsWith(DataKeyDbHelper.DATABASE_NAME)) {
                    // Only trigger UI refresh; avoid closing DB from observer thread to prevent race/native crashes
                    Handler(Looper.getMainLooper()).post {
                        dataVersion++
                    }
                }
            }
        } else null
        observer?.startWatching()
        onDispose { observer?.stopWatching() }
    }

    // If launched from share, navigate on first composition
    LaunchedEffect(initialAction, initialUriString, initialSharedText) {
        if (initialAction == "encrypt" && !initialUriString.isNullOrEmpty()) {
            screen = Screen.Encrypt
        } else if (initialAction == "decrypt" && !initialUriString.isNullOrEmpty()) {
            screen = Screen.Decrypt
        } else if ((initialAction == "text" || initialAction == "text_decrypt") && !initialSharedText.isNullOrEmpty()) {
            screen = Screen.TextFile
        }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.app_name).uppercase(),
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.weight(1f)
                    )

                    // Language selector (read current per-app locales; fallback to system locale)
                    val currentTag: String = try {
                        val list = AppCompatDelegate.getApplicationLocales()
                        if (!list.isEmpty) {
                            list.toLanguageTags().split(",").firstOrNull()?.lowercase() ?: ""
                        } else {
                            @Suppress("DEPRECATION")
                            val sys = context.resources.configuration.locales.get(0)
                            sys?.toLanguageTag()?.lowercase() ?: ""
                        }
                    } catch (_: Exception) { "" }

                    val selectedOption = languageOptionsOrdered.firstOrNull { currentTag.startsWith(it.tag) }
                        ?: languageOptionsOrdered.first()

                    var languageMenuExpanded by remember { mutableStateOf(false) }
                    Box {
                        AssistChip(
                            onClick = { languageMenuExpanded = true },
                            leadingIcon = {
                                androidx.compose.material3.Icon(Icons.Filled.Language, contentDescription = null)
                            },
                            label = { Text(selectedOption.code) },
                            colors = AssistChipDefaults.assistChipColors(
                                containerColor = MaterialTheme.colorScheme.surface,
                                labelColor = MaterialTheme.colorScheme.onSurface
                            )
                        )

                        DropdownMenu(
                            expanded = languageMenuExpanded,
                            onDismissRequest = { languageMenuExpanded = false }
                        ) {
                            for (opt in languageOptionsOrdered) {
                                DropdownMenuItem(
                                    text = { Text(opt.label) },
                                    leadingIcon = if (opt.tag == selectedOption.tag) {
                                        { androidx.compose.material3.Icon(Icons.Filled.Check, contentDescription = null) }
                                    } else null,
                                    onClick = {
                                        languageMenuExpanded = false
                                        AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(opt.tag))
                                        scope.launch { drawerState.close() }
                                        (context as? Activity)?.recreate()
                                    }
                                )
                            }
                        }
                    }
                }

                }

                NavigationDrawerItem(
                    label = { Text(stringResource(R.string.create_key)) },
                    icon = { androidx.compose.material3.Icon(Icons.Filled.VpnKey, contentDescription = null) },
                    selected = false,
                    onClick = {
                        scope.launch { drawerState.close() }
                        screen = Screen.CreateKey
                    },
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                )

                

                NavigationDrawerItem(
                    label = { Text(stringResource(R.string.export)) },
                    icon = { androidx.compose.material3.Icon(Icons.Filled.IosShare, contentDescription = null) },
                    selected = false,
                    onClick = {
                        scope.launch { drawerState.close() }
                        screen = Screen.Export
                    },
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                )

                Divider(modifier = Modifier.padding(horizontal = 16.dp))

                NavigationDrawerItem(
                    label = { Text(stringResource(R.string.import_)) },
                    icon = { androidx.compose.material3.Icon(Icons.Filled.FileOpen, contentDescription = null) },
                    selected = false,
                    onClick = {
                        scope.launch { drawerState.close() }
                        screen = Screen.Import
                    },
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                )

                Divider(modifier = Modifier.padding(horizontal = 16.dp))

                NavigationDrawerItem(
                    label = { Text(stringResource(R.string.delete)) },
                    icon = { androidx.compose.material3.Icon(Icons.Filled.DeleteForever, contentDescription = null) },
                    selected = false,
                    onClick = {
                        scope.launch { drawerState.close() }
                        screen = Screen.Delete
                    },
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                )

                Divider(modifier = Modifier.padding(horizontal = 16.dp))

                NavigationDrawerItem(
                    label = { Text(stringResource(R.string.delete_database)) },
                    icon = { androidx.compose.material3.Icon(Icons.Filled.DeleteForever, contentDescription = null) },
                    selected = false,
                    onClick = {
                        scope.launch { drawerState.close() }
                        showDeleteDbConfirm = true
                    },
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                )

                Divider(modifier = Modifier.padding(horizontal = 16.dp))

                // About item at the bottom (placed last)
                NavigationDrawerItem(
                    label = { Text(stringResource(R.string.about_label)) },
                    icon = { androidx.compose.material3.Icon(Icons.Filled.ArrowForward, contentDescription = null) },
                    selected = false,
                    onClick = {
                        scope.launch { drawerState.close() }
                        screen = Screen.About
                    },
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                )
            }
        }
    ) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            topBar = {
                TopAppBar(
                    title = {
                        val titleText = stringResource(R.string.app_name).uppercase()
                        Text(text = titleText)
                    },
                    navigationIcon = {
                        if (screen is Screen.Home) {
                            IconButton(onClick = { scope.launch { drawerState.open() } }) {
                                androidx.compose.material3.Icon(
                                    imageVector = Icons.Filled.Menu,
                                    contentDescription = "Menu"
                                )
                            }
                        } else {
                            val isRtl = LocalLayoutDirection.current == LayoutDirection.Rtl
                            val backIcon = if (isRtl) Icons.Filled.ArrowForward else Icons.Filled.ArrowBack
                            IconButton(onClick = { screen = Screen.Home }, enabled = !isBusy) {
                                androidx.compose.material3.Icon(
                                    imageVector = backIcon,
                                    contentDescription = "Back"
                                )
                            }
                        }
                    },
                    actions = {
                        if (screen is Screen.Home) {
                            Image(
                                painter = painterResource(id = R.mipmap.logo_1),
                                contentDescription = null,
                                modifier = Modifier
                                    .padding(end = 8.dp)
                                    .width(36.dp)
                            )
                        }
                    }
                )
            }
        ) { innerPadding ->
            androidx.compose.animation.Crossfade(targetState = screen, label = "screen") { current ->
                when (current) {
                    is Screen.Home -> HomeScreen(
                        modifier = Modifier
                            .padding(innerPadding)
                            .fillMaxSize()
                        ,
                        refreshKey = dataVersion,
                        onCreateKey = { screen = Screen.CreateKey },
                        onExport = { screen = Screen.Export },
                        onEncrypt = { screen = Screen.Encrypt },
                        onDecrypt = { screen = Screen.Decrypt },
                        onImport = { screen = Screen.Import },
                        onTextFile = { screen = Screen.TextFile }
                    )
                    is Screen.CreateKey -> CreateKeyScreen(
                        modifier = Modifier
                            .padding(innerPadding)
                            .fillMaxSize()
                    ) { screen = Screen.Home }
                    is Screen.Export -> ExportScreen(
                        modifier = Modifier
                            .padding(innerPadding)
                            .fillMaxSize()
                    ) { screen = Screen.Home }
                    is Screen.Encrypt -> EncryptScreen(
                        modifier = Modifier
                            .padding(innerPadding)
                            .fillMaxSize()
                        ,
                        initialInUri = initialUriString?.let { runCatching { Uri.parse(it) }.getOrNull() },
                        onBusyChanged = { isBusy = it }
                    ) { screen = Screen.Home }
                    is Screen.Decrypt -> DecryptScreen(
                        modifier = Modifier
                            .padding(innerPadding)
                            .fillMaxSize()
                        ,
                        initialEncUri = initialUriString?.let { runCatching { Uri.parse(it) }.getOrNull() },
                        onBusyChanged = { isBusy = it }
                    ) { screen = Screen.Home }
                    is Screen.Import -> ImportScreen(
                        modifier = Modifier
                            .padding(innerPadding)
                            .fillMaxSize()
                    ) { screen = Screen.Home }
                    is Screen.Delete -> DeleteScreen(
                        modifier = Modifier
                            .padding(innerPadding)
                            .fillMaxSize()
                    ) { screen = Screen.Home }
                    is Screen.About -> AboutScreen(
                        modifier = Modifier
                            .padding(innerPadding)
                            .fillMaxSize()
                    ) { screen = Screen.Home }
                    is Screen.TextFile -> TextFileScreen(
                        modifier = Modifier
                            .padding(innerPadding)
                            .fillMaxSize()
                        ,
                        initialText = initialSharedText,
                        autoDecrypt = initialAction == "text_decrypt"
                    ) { screen = Screen.Home }
                }
            }
        }
    }

    if (showDeleteDbConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteDbConfirm = false },
            title = { Text(stringResource(R.string.confirm_delete_db_title)) },
            text = { Text(stringResource(R.string.confirm_delete_db_message)) },
            confirmButton = {
                TextButton(onClick = {
                    showDeleteDbConfirm = false
                    try {
                        // Close any open shared DB connection before deleting files to avoid native crashes
                        DataKeyDbHelper.resetShared()
                        val main = context.getDatabasePath(DataKeyDbHelper.DATABASE_NAME)
                        var ok = true
                        fun delIfExists(path: java.io.File?) {
                            if (path != null && path.exists()) {
                                if (!path.delete()) ok = false
                            }
                        }
                        delIfExists(main)
                        delIfExists(java.io.File(main.parentFile, DataKeyDbHelper.DATABASE_NAME + "-journal"))
                        delIfExists(java.io.File(main.parentFile, DataKeyDbHelper.DATABASE_NAME + "-wal"))
                        delIfExists(java.io.File(main.parentFile, DataKeyDbHelper.DATABASE_NAME + "-shm"))
                        Toast.makeText(context, context.getString(if (ok) R.string.db_deleted_success else R.string.db_deleted_failed), Toast.LENGTH_LONG).show()
                        if (ok) {
                            // Navigate home and trigger UI refresh
                            screen = Screen.Home
                            dataVersion++
                        }
                    } catch (e: Exception) {
                        Toast.makeText(context, context.getString(R.string.db_deleted_failed), Toast.LENGTH_LONG).show()
                    }
                }) { Text(stringResource(android.R.string.ok)) }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDbConfirm = false }) { Text(stringResource(android.R.string.cancel)) }
            }
        )
    }
}

@Composable
fun DecryptScreen(
    modifier: Modifier = Modifier,
    initialEncUri: Uri? = null,
    onBusyChanged: (Boolean) -> Unit = {},
    onDone: () -> Unit,
) {
    val context = LocalContext.current
    val repo = remember { KeyRepository(context) }
    var pickedEncUri by remember { mutableStateOf<Uri?>(null) }
    var isDecrypting by remember { mutableStateOf(false) }
    var decryptTotalBytes by remember { mutableStateOf<Long?>(null) }
    var decryptProgressBytes by remember { mutableStateOf(0L) }
    val scope = rememberCoroutineScope()
    var cancelDecrypt by remember { mutableStateOf(false) }
    var showHelp by remember { mutableStateOf(false) }

    fun isValidEncryptedUri(u: Uri): Boolean {
        val t = runCatching { context.contentResolver.getType(u) }.getOrNull()
        if (!t.isNullOrBlank()) {
            val mt = t.lowercase(java.util.Locale.getDefault())
            if (mt == "application/pgp-encrypted" || mt == "application/x-gnupg") return true
        }
        var name: String? = null
        runCatching {
            context.contentResolver.query(u, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)?.use { c ->
                if (c.moveToFirst()) name = c.getString(0)
            }
        }
        if (name.isNullOrBlank()) name = u.lastPathSegment
        val lower = name?.lowercase(java.util.Locale.getDefault()) ?: ""
        return lower.endsWith(".gpg") || lower.endsWith(".pgp")
    }

    // Disable system back while decrypting; user should press Cancel
    BackHandler(enabled = isDecrypting) { /* block back */ }

    // Prefill encrypted file if launched from share
    LaunchedEffect(initialEncUri) {
        if (pickedEncUri == null && initialEncUri != null) {
            try {
                if (!isValidEncryptedUri(initialEncUri)) {
                    Toast.makeText(context, context.getString(R.string.not_a_valid_encrypted_file), Toast.LENGTH_LONG).show()
                    return@LaunchedEffect
                }
                pickedEncUri = initialEncUri
                val sz = withContext(Dispatchers.IO) {
                    var size: Long? = null
                    context.contentResolver.query(initialEncUri, null, null, null, null)?.use { c ->
                        val sizeIdx = c.getColumnIndex(OpenableColumns.SIZE)
                        if (sizeIdx >= 0 && c.moveToFirst()) size = c.getLong(sizeIdx)
                    }
                    size
                }
                decryptTotalBytes = sz
                Toast.makeText(context, context.getString(R.string.encrypted_file_selected), Toast.LENGTH_SHORT).show()
            } catch (_: Exception) { }
        }
    }

    val openEnc = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            try {
                if (!isValidEncryptedUri(uri)) {
                    Toast.makeText(context, context.getString(R.string.not_a_valid_encrypted_file), Toast.LENGTH_LONG).show()
                    return@rememberLauncherForActivityResult
                }
                pickedEncUri = uri
                scope.launch {
                    val sz = withContext(Dispatchers.IO) {
                        var size: Long? = null
                        context.contentResolver.query(uri, null, null, null, null)?.use { c ->
                            val sizeIdx = c.getColumnIndex(OpenableColumns.SIZE)
                            if (sizeIdx >= 0 && c.moveToFirst()) size = c.getLong(sizeIdx)
                        }
                        size
                    }
                    decryptTotalBytes = sz
                }
                Toast.makeText(context, context.getString(R.string.encrypted_file_selected), Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Toast.makeText(context, context.getString(R.string.read_failed_with_reason, e.message ?: ""), Toast.LENGTH_LONG).show()
            }
        }
    }

    data class Candidate(val name: String, val secret: String, val pass: CharArray)
    var decryptCandidate by remember { mutableStateOf<Candidate?>(null) }
    var pendingOutUri by remember { mutableStateOf<Uri?>(null) }

    // Helper to start decryption (scoped to this screen)
    fun funStartDecrypt(best: Candidate, src: Uri, outUri: Uri) {
        isDecrypting = true
        onBusyChanged(true)
        decryptProgressBytes = 0L
        cancelDecrypt = false
        scope.launch {
            CryptoFgService.start(context, context.getString(R.string.decrypt_and_save))
            // Keep CPU awake during decryption
            val pm = context.getSystemService(android.content.Context.POWER_SERVICE) as PowerManager
            val wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "Azadi:Decrypt")
            try {
                wakeLock.acquire()
                var writtenBytes: Long = 0
                var desiredExt: String? = null
                withContext(Dispatchers.IO) {
                    runCatching {
                        context.contentResolver.openInputStream(src)?.use { insPeek ->
                            val peekName = PgpUtils.decryptPeekFileName(best.secret, best.pass, insPeek)
                            if (!peekName.isNullOrBlank()) {
                                val dot = peekName.lastIndexOf('.')
                                if (dot > 0 && dot < peekName.length - 1) {
                                    desiredExt = peekName.substring(dot + 1)
                                }
                            }
                        }
                    }
                    val mainHandler = Handler(Looper.getMainLooper())
                    context.contentResolver.openInputStream(src)?.use { ins ->
                        context.contentResolver.openOutputStream(outUri)?.use { os ->
                            PgpUtils.decryptStream(
                                secretKeyArmored = best.secret,
                                passphrase = best.pass,
                                encInput = ins,
                                output = os,
                                shouldCancel = { cancelDecrypt },
                                onProgress = { count ->
                                    writtenBytes = count
                                    mainHandler.post { decryptProgressBytes = count }
                                }
                            )
                        } ?: throw IllegalStateException("Cannot open output stream")
                    } ?: throw IllegalStateException("Cannot open input stream")
                }
                if (writtenBytes <= 0L) {
                    // Best-effort cleanup: delete the empty document to avoid confusion
                    try { DocumentFile.fromSingleUri(context, outUri)?.delete() } catch (_: Exception) {}
                    throw IllegalStateException("Decryption produced no data")
                }
                runCatching {
                    if (!desiredExt.isNullOrBlank()) {
                        withContext(Dispatchers.IO) { ensureFixedExtension(context, outUri, desiredExt!!) }
                    }
                }
                // Ensure numbering stays before extension (e.g., vejin-1.zip) if provider added it after extension
                runCatching {
                    withContext(Dispatchers.IO) { renameIfProviderAddedSuffix(context, outUri) }
                }
                Toast.makeText(context, context.getString(R.string.decrypted_and_saved), Toast.LENGTH_SHORT).show()
                onDone()
            } catch (e: java.util.concurrent.CancellationException) {
                // Cleanup partial output if cancelled
                try { DocumentFile.fromSingleUri(context, outUri)?.delete() } catch (_: Exception) {}
                Toast.makeText(context, context.getString(R.string.operation_cancelled), Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                // Cleanup partial output if present
                try { DocumentFile.fromSingleUri(context, outUri)?.delete() } catch (_: Exception) {}
                Toast.makeText(context, context.getString(R.string.decrypt_failed_with_reason, e.message ?: ""), Toast.LENGTH_LONG).show()
            } finally {
                CryptoFgService.stop(context)
                if (wakeLock.isHeld) wakeLock.release()
                isDecrypting = false
                decryptProgressBytes = 0L
                cancelDecrypt = false
                onBusyChanged(false)
            }
        }
    }

    val createOutDoc = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/octet-stream")
    ) { outUri ->
        pendingOutUri = outUri
        val src = pickedEncUri
        val best = decryptCandidate
        if (outUri != null && src != null && best != null) {
            // Start immediately if candidate is ready
            funStartDecrypt(best, src, outUri)
            pendingOutUri = null
        }
    }

    val scrollState = rememberScrollState()
    if (showHelp) {
        HelpDialog(
            title = stringResource(R.string.help_decrypt_file_title),
            body = stringResource(R.string.help_decrypt_file_body),
            onDismiss = { showHelp = false }
        )
    }
    Column(modifier = modifier.fillMaxSize().verticalScroll(scrollState).padding(16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(id = if (pickedEncUri != null) R.string.encrypted_file_selected else R.string.select_private_key_for_decryption),
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.weight(1f)
            )
            HelpIconButton(onClick = { showHelp = true })
        }
        Row(modifier = Modifier.fillMaxWidth()) {
            ElevatedButton(
                onClick = { if (!isDecrypting) openEnc.launch(arrayOf("*/*")) },
                modifier = Modifier
                    .weight(1f)
                    .alpha(if (pickedEncUri != null) 0.6f else 1f),
                enabled = (!isDecrypting)
            ) {
                Text(stringResource(R.string.choose_gpg_file))
            }
            Spacer(modifier = Modifier.width(8.dp))
            ElevatedButton(onClick = {
                val src = pickedEncUri
                if (src == null) { Toast.makeText(context, context.getString(R.string.please_choose_gpg_file), Toast.LENGTH_SHORT).show(); return@ElevatedButton }

                scope.launch {
                    // 1) Compute a better suggested name from DISPLAY_NAME on IO, strip .gpg/.pgp
                    val quickSuggested = withContext(Dispatchers.IO) {
                        var display: String? = null
                        try {
                            context.contentResolver.query(src, null, null, null, null)?.use { c ->
                                val idx = c.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                                if (idx >= 0 && c.moveToFirst()) display = c.getString(idx)
                            }
                        } catch (_: Exception) { }
                        val base = (display ?: (src.lastPathSegment ?: "decrypted"))
                        base.removeSuffix(".gpg").removeSuffix(".pgp")
                    }
                    // Show Save dialog with a clean suggestion (no numbering by default)
                    val baseName = quickSuggested.ifBlank { "decrypted" }
                    createOutDoc.launch(baseName)

                    // 2) In parallel, quickly choose a candidate key
                    launch {
                        try {
                            var found: Candidate? = null

                            val recipientIds = withContext(Dispatchers.IO) {
                                context.contentResolver.openInputStream(src)?.use { ins ->
                                    PgpUtils.extractRecipientKeyIds(ins)
                                } ?: emptyList()
                            }

                            val names = withContext(Dispatchers.IO) { repo.listKeyNames() }
                            val allSecrets: Map<String, String?> = withContext(Dispatchers.IO) {
                                names.associateWith { n -> repo.getArmoredSecretByName(n) }
                            }

                            if (recipientIds.isNotEmpty()) {
                                // Prefer passphrase stored by keyId first
                                loop@ for (id in recipientIds) {
                                    val savedById = PassphraseStore.getByKeyId(context, id)
                                    if (!savedById.isNullOrEmpty()) {
                                        for (n in names) {
                                            val sec = allSecrets[n] ?: continue
                                            val hasId = try { PgpUtils.secretContainsKeyId(sec, id) } catch (_: Exception) { false }
                                            if (!hasId) continue
                                            found = Candidate(n, sec, savedById.toCharArray())
                                            break@loop
                                        }
                                    }
                                }
                                // Next: any name that matches recipientIds with saved pass by name
                                if (found == null) {
                                    for (n in names) {
                                        val sec = allSecrets[n] ?: continue
                                        val matchesId = recipientIds.any { id ->
                                            try { PgpUtils.secretContainsKeyId(sec, id) } catch (_: Exception) { false }
                                        }
                                        if (!matchesId) continue
                                        val savedPass = PassphraseStore.get(context, n)
                                        if (!savedPass.isNullOrEmpty()) { found = Candidate(n, sec, savedPass.toCharArray()); break }
                                    }
                                }
                                // Finally: try empty pass quickly for any matching name (we will validate during decrypt)
                                if (found == null) {
                                    for (n in names) {
                                        val sec = allSecrets[n] ?: continue
                                        val matchesId = recipientIds.any { id ->
                                            try { PgpUtils.secretContainsKeyId(sec, id) } catch (_: Exception) { false }
                                        }
                                        if (matchesId) { found = Candidate(n, sec, CharArray(0)); break } else { continue }
                                    }
                                }
                            } else {
                                // No recipient IDs: pick first with saved pass, else empty
                                for (n in names) {
                                    val sec = allSecrets[n] ?: continue
                                    val savedPass = PassphraseStore.get(context, n)
                                    if (!savedPass.isNullOrEmpty()) { found = Candidate(n, sec, savedPass.toCharArray()); break }
                                }
                                if (found == null) {
                                    for (n in names) {
                                        val sec = allSecrets[n] ?: continue
                                        found = Candidate(n, sec, CharArray(0)); break
                                    }
                                }
                            }

                            if (found != null) {
                                decryptCandidate = found
                                val out = pendingOutUri
                                if (out != null) {
                                    funStartDecrypt(found!!, src, out)
                                    pendingOutUri = null
                                }
                            } else {
                                Toast.makeText(context, context.getString(R.string.no_matching_secret_key), Toast.LENGTH_LONG).show()
                            }
                        } catch (e: Exception) {
                            Toast.makeText(context, context.getString(R.string.decrypt_failed_with_reason, e.message ?: ""), Toast.LENGTH_LONG).show()
                        }
                    }
                }
            }, modifier = Modifier.weight(1f)) {
                Text(if (isDecrypting) "…" else stringResource(R.string.decrypt_and_save))
            }
        }

        AnimatedVisibility(visible = isDecrypting) {
            val t = decryptTotalBytes
            Spacer(modifier = Modifier.height(8.dp))
            if (t != null && t > 0) {
                val progress = (decryptProgressBytes.toFloat() / t.toFloat()).coerceIn(0f, 1f)
                AnimatedCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 20.dp)
                ) {
                    Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 12.dp)) {
                        GradientLinearProgress(progress = progress, height = 14.dp)
                        Text(text = String.format("%d%%", (progress * 100).toInt()), modifier = Modifier.padding(top = 6.dp))
                        Spacer(modifier = Modifier.height(12.dp))
                        ElevatedButton(
                            onClick = { cancelDecrypt = true },
                            modifier = Modifier.align(Alignment.CenterHorizontally)
                        ) {
                            Text(stringResource(R.string.cancel))
                        }
                    }
                }
            } else {
                AnimatedCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 20.dp)
                ) {
                    Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 12.dp)) {
                        LinearProgressIndicator(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(14.dp)
                                .clip(RoundedCornerShape(8.dp))
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        ElevatedButton(
                            onClick = { cancelDecrypt = true },
                            modifier = Modifier.align(Alignment.CenterHorizontally)
                        ) {
                            Text(stringResource(R.string.cancel))
                        }
                    }
                }
            }
        }
    }
}

private sealed class Screen {
    data object Home : Screen()
    data object CreateKey : Screen()
    data object Export : Screen()
    data object About : Screen()
    data object Encrypt : Screen()
    data object Decrypt : Screen()
    data object Import : Screen()
    data object Delete : Screen()
    data object TextFile : Screen()
}

// Saver to persist the current Screen across configuration changes
private val ScreenSaver: Saver<Screen, String> = Saver(
    save = { s ->
        when (s) {
            is Screen.Home -> "home"
            is Screen.CreateKey -> "create_key"
            is Screen.Export -> "export"
            is Screen.About -> "about"
            is Screen.Encrypt -> "encrypt"
            is Screen.Decrypt -> "decrypt"
            is Screen.Import -> "import"
            is Screen.Delete -> "delete"
            is Screen.TextFile -> "text_file"
        }
    },
    restore = { key ->
        when (key) {
            "home" -> Screen.Home
            "create_key" -> Screen.CreateKey
            "export" -> Screen.Export
            "about" -> Screen.About
            "encrypt" -> Screen.Encrypt
            "decrypt" -> Screen.Decrypt
            "import" -> Screen.Import
            "delete" -> Screen.Delete
            "text_file" -> Screen.TextFile
            else -> Screen.Home
        }
    }
)


@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    refreshKey: Int = 0,
    onCreateKey: () -> Unit,
    onExport: () -> Unit,
    onEncrypt: () -> Unit,
    onDecrypt: () -> Unit,
    onImport: () -> Unit,
    onTextFile: () -> Unit
) {
    val context = LocalContext.current
    val repo = remember { KeyRepository(context) }
    var names by remember { mutableStateOf(listOf<String>()) }
    var hasSecretByName by remember { mutableStateOf<Map<String, Boolean>>(emptyMap()) }
    val scope = rememberCoroutineScope()
    val highlightColor = if (isSystemInDarkTheme()) Color(0xFFFFD54F) else Color(0xFFF57C00)
    var showHelp by remember { mutableStateOf(false) }

    // Load names when we enter this screen and when refreshKey changes
    androidx.compose.runtime.LaunchedEffect(refreshKey) {
        try {
            val (loadedNames, secretMap) = withContext(Dispatchers.IO) {
                val ns = repo.listKeyNames()
                val m = ns.associateWith { n ->
                    val sec = repo.getArmoredSecretByName(n)
                    !sec.isNullOrEmpty()
                }
                ns to m
            }
            val collator = Collator.getInstance(Locale("tr", "TR")).apply { strength = Collator.PRIMARY }
            val ordered = loadedNames.sortedWith { a, b -> collator.compare(a, b) }
            names = ordered
            hasSecretByName = secretMap
        } catch (_: Exception) {
            // If DB is unavailable or just deleted, show empty state gracefully
            names = emptyList()
            hasSecretByName = emptyMap()
        }
    }

    if (showHelp) {
        HelpDialog(
            title = stringResource(R.string.help_home_title),
            body = stringResource(R.string.help_home_body),
            onDismiss = { showHelp = false }
        )
    }

    Column(
        modifier = modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.Start
    ) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
            HelpIconButton(onClick = { showHelp = true })
        }

        Row(modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp)) {
            AppButton(
                text = stringResource(R.string.encrypt),
                onClick = onEncrypt,
                modifier = Modifier.weight(1f)
            )
            Spacer(modifier = Modifier.width(8.dp))
            AppButton(
                text = stringResource(R.string.decrypt),
                onClick = onDecrypt,
                modifier = Modifier.weight(1f)
            )
        }
        Row(modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp)) {
            AppButton(
                text = stringResource(R.string.text_file),
                onClick = onTextFile,
                modifier = Modifier.weight(1f),
                style = ButtonStyle.Outlined
            )
        }

        AnimatedCard(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp),
            elevation = 2
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
            ) {
                // Header as first item
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = stringResource(R.string.name),
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.weight(1f)
                        )
                        Text(
                            text = stringResource(R.string.type),
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.weight(1f)
                        )
                    }
                    Divider(thickness = 0.5.dp)
                }
                itemsIndexed(names) { index, name ->
                    val rowBg = if (index % 2 == 1) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.15f) else Color.Transparent
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(rowBg)
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val displayName = remember(name) {
                            val noAsc = if (name.endsWith(".asc", ignoreCase = true)) name.dropLast(4) else name
                            // Also strip export suffixes like -gizli / -genel from display only
                            noAsc.replace(Regex("-(gizli|genel)$", RegexOption.IGNORE_CASE), "")
                        }
                        // Name (no leading icon)
                        Text(
                            text = displayName,
                            style = if (hasSecretByName[name] == true) {
                                MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold)
                            } else {
                                MaterialTheme.typography.bodyLarge
                            },
                            color = if (hasSecretByName[name] == true) highlightColor else MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.weight(1f)
                        )

                        // Type: plain text, right aligned
                        val hasSecret = hasSecretByName[name] == true
                        val typeText = if (hasSecret) stringResource(R.string.secret) + " + " + stringResource(R.string.public_) else stringResource(R.string.public_)
                        Text(
                            text = typeText,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.weight(1f),
                        )
                    }
                    Divider(thickness = 0.5.dp)
                }
            }
        }
    }
}

@Composable
fun CreateKeyScreen(modifier: Modifier = Modifier, onDone: () -> Unit) {
    val context = LocalContext.current
    val repo = remember { KeyRepository(context) }
    var name by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var generatedOnce by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    var isCreating by remember { mutableStateOf(false) }
    var showHelp by remember { mutableStateOf(false) }

    if (showHelp) {
        HelpDialog(
            title = stringResource(R.string.help_create_key_title),
            body = stringResource(R.string.help_create_key_body),
            onDismiss = { showHelp = false }
        )
    }

    Column(
        modifier = modifier.padding(16.dp),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.Start
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(R.string.create_key),
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.weight(1f)
            )
            HelpIconButton(onClick = { showHelp = true })
        }
        Spacer(modifier = Modifier.height(12.dp))

        TextField(
            value = name,
            onValueChange = { name = it },
            label = { Text(stringResource(R.string.name)) },
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp)
        )
        TextField(
            value = password,
            onValueChange = { password = it },
            label = { Text(stringResource(R.string.password)) },
            singleLine = true,
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            trailingIcon = {
                val clipboard = LocalClipboardManager.current
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Order: length -> copy -> show/hide
                    val isStrong = password.length >= 100
                    val chipColors = AssistChipDefaults.assistChipColors(
                        labelColor = if (isStrong) Color(0xFF2E7D32) else Color(0xFFB71C1C)
                    )
                    AssistChip(
                        onClick = {},
                        label = { Text(password.length.toString()) },
                        colors = chipColors
                    )
                    Spacer(modifier = Modifier.width(2.dp))
                    IconButton(onClick = {
                        if (password.isNotEmpty()) {
                            clipboard.setText(AnnotatedString(password))
                            // Localized toast: Kurdish vs Turkish vs default English
                            val currentTag: String = try {
                                val list = AppCompatDelegate.getApplicationLocales()
                                if (!list.isEmpty) list.toLanguageTags().split(",").firstOrNull()?.lowercase() ?: "" else {
                                    @Suppress("DEPRECATION")
                                    val sys = context.resources.configuration.locales.get(0)
                                    sys?.toLanguageTag()?.lowercase() ?: ""
                                }
                            } catch (_: Exception) { "" }
                            val msg = when {
                                currentTag.startsWith("ku") || currentTag.startsWith("ckb") -> "Kopî"
                                currentTag.startsWith("tr") -> "Kopyalandı"
                                else -> "Copied"
                            }
                            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                        }
                    }) {
                        androidx.compose.material3.Icon(
                            imageVector = Icons.Filled.ContentCopy,
                            contentDescription = "Copy"
                        )
                    }
                    Spacer(modifier = Modifier.width(2.dp))
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        androidx.compose.material3.Icon(
                            imageVector = if (passwordVisible) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                            contentDescription = if (passwordVisible) "Hide" else "Show"
                        )
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
                .onFocusChanged { f: FocusState ->
                    if (f.isFocused && password.isBlank() && !generatedOnce) {
                        password = generateStrongPassword100()
                        generatedOnce = true
                        Toast.makeText(context, context.getString(R.string.generated_password_message), Toast.LENGTH_SHORT).show()
                    }
                }
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp)
        ) {
        Button(
            enabled = !isCreating,
            onClick = {
            if (name.isBlank() || password.isBlank()) {
                Toast.makeText(context, context.getString(R.string.enter_name_and_password), Toast.LENGTH_SHORT).show()
                return@Button
            }
            if (password.length < 100) {
                Toast.makeText(context, context.getString(R.string.min_password_length), Toast.LENGTH_LONG).show()
                return@Button
            }
            // Prevent duplicate key names
            val existing = repo.listKeyNames()
            if (existing.any { it.equals(name, ignoreCase = true) }) {
                Toast.makeText(context, context.getString(R.string.key_name_exists), Toast.LENGTH_LONG).show()
                return@Button
            }
            scope.launch {
                try {
                    isCreating = true
                    val armored = withContext(Dispatchers.IO) {
                        PgpUtils.generateArmoredKeyPair(identity = name, passphrase = password.toCharArray())
                    }
                    val rowId = withContext(Dispatchers.IO) {
                        repo.insertKey(name, armored.publicKeyArmored, armored.secretKeyArmored)
                    }
                    if (rowId != -1L) {
                        // Save passphrase for auto-decrypt (by name and by keyId)
                        PassphraseStore.save(context, name, password)
                        try {
                            val kid = withContext(Dispatchers.IO) { PgpUtils.firstSecretKeyId(armored.secretKeyArmored) }
                            if (kid != null) PassphraseStore.saveByKeyId(context, kid, password)
                        } catch (_: Exception) { }
                        Toast.makeText(context, context.getString(R.string.key_created_and_saved), Toast.LENGTH_SHORT).show()
                        onDone()
                    } else {
                        Toast.makeText(context, context.getString(R.string.db_save_failed), Toast.LENGTH_SHORT).show()
                    }
                } catch (e: Exception) {
                    Toast.makeText(context, context.getString(R.string.error_with_reason, e.message ?: ""), Toast.LENGTH_LONG).show()
                } finally {
                    isCreating = false
                }
            }
        },
            modifier = Modifier.align(Alignment.Center)
        ) {
            Text(stringResource(R.string.create_new_key))
        }
        // External length chip removed; length is now shown inside the password field trailing icon
        }
    }
}

// Generates a strong 100-character password with diverse character sets
private fun generateStrongPassword100(): String {
    val rng = SecureRandom()
    val digits = "0123456789"
    val symbols = "!@#${'$'}%^&*()-_=+[]{};:'\",.<>/?\\|`~"
    val english = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ"
    val russian = "абвгдеёжзийклмнопрстуфхцчшщъыьэюя" +
            "АБВГДЕЁЖЗИЙКЛМНОПРСТУФХЦЧШЩЪЫЬЭЮЯ"
    val greek = "αβγδεζηθικλμνξοπρστυφχψω" +
            "ΑΒΓΔΕΖΗΘΙΚΛΜΝΞΟΠΡΣΤΥΦΧΨΩ"
    val german = "äöüßÄÖÜ"

    fun randomChinese(): Char {
        // Common CJK Unified Ideographs block
        val start = 0x4E00
        val end = 0x9FFF
        return (start + rng.nextInt(end - start + 1)).toChar()
    }

    val bucket = mutableListOf<Char>()
    fun addFrom(set: String) { bucket += set[rng.nextInt(set.length)] }

    // Ensure at least one from each category
    addFrom(digits)
    addFrom(symbols)
    addFrom(english)
    addFrom(russian)
    addFrom(greek)
    addFrom(german)
    bucket += randomChinese()

    val allSets = digits + symbols + english + russian + greek + german
    while (bucket.size < 100) {
        // Mix between unicode CJK and other sets
        if (rng.nextInt(5) == 0) {
            bucket += randomChinese()
        } else {
            bucket += allSets[rng.nextInt(allSets.length)]
        }
    }
    // Shuffle
    for (i in bucket.indices.reversed()) {
        val j = rng.nextInt(i + 1)
        val tmp = bucket[i]
        bucket[i] = bucket[j]
        bucket[j] = tmp
    }
    return bucket.joinToString("")
}

@Composable
fun ImportScreen(modifier: Modifier = Modifier, onDone: () -> Unit) {
    val context = LocalContext.current
    val repo = remember { KeyRepository(context) }
    var pubText by remember { mutableStateOf<String?>(null) }
    var secText by remember { mutableStateOf<String?>(null) }
    var passphrase by remember { mutableStateOf("") }
    var suggestedName by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()

    // --- Multi-import state ---
    data class PendingImport(
        val uri: Uri,
        var pub: String?,
        var sec: String?,
        var name: String,
        var pass: String? = null
    )
    var queue by remember { mutableStateOf<List<PendingImport>>(emptyList()) }
    var currentIndex by remember { mutableStateOf<Int?>(null) }
    var passDialogVisible by remember { mutableStateOf(false) }
    var passDialogText by remember { mutableStateOf("") }
    var isBatchImporting by remember { mutableStateOf(false) }
    var showHelp by remember { mutableStateOf(false) }

    fun sanitizeImportedName(input: String): String {
        var base = input.ifBlank { "ImportedKey" }.trim()
        // Strip multiple known extensions repeatedly
        val knownExt = setOf("asc", "key", "pgp", "gpg", "pub", "sec")
        while (true) {
            val idx = base.lastIndexOf('.')
            if (idx <= 0 || idx >= base.length - 1) break
            val ext = base.substring(idx + 1).lowercase()
            if (ext in knownExt) {
                base = base.substring(0, idx)
            } else {
                break
            }
        }
        // Remove trailing type suffixes like -gizli / -genel / -public / -secret / -private (with optional spaces/hyphens)
        val suffixRegex = Regex("[\\s-]*(gizli|genel|public|secret|private)$", RegexOption.IGNORE_CASE)
        while (true) {
            val newBase = base.replace(suffixRegex, "")
            if (newBase == base) break
            base = newBase
        }
        return base.trimEnd('-', ' ').trim()
    }

    val openDocs = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenMultipleDocuments()
    ) { uris ->
        if (uris.isNullOrEmpty()) return@rememberLauncherForActivityResult
        scope.launch {
            try {
                val items = withContext(Dispatchers.IO) {
                    uris.mapNotNull { uri ->
                        val text = context.contentResolver.openInputStream(uri)?.use { it.readBytes().toString(Charsets.UTF_8) } ?: ""
                        fun extractBlock(all: String, begin: String, end: String): String? {
                            val s = all.indexOf(begin)
                            if (s < 0) return null
                            val e = all.indexOf(end, startIndex = s)
                            if (e < 0) return null
                            return all.substring(s, e + end.length)
                        }
                        val pubBlock = extractBlock(text, "-----BEGIN PGP PUBLIC KEY BLOCK-----", "-----END PGP PUBLIC KEY BLOCK-----")
                        val secBlockPrivate = extractBlock(text, "-----BEGIN PGP PRIVATE KEY BLOCK-----", "-----END PGP PRIVATE KEY BLOCK-----")
                        val secBlockSecret = extractBlock(text, "-----BEGIN PGP SECRET KEY BLOCK-----", "-----END PGP SECRET KEY BLOCK-----")
                        val secBlock = secBlockPrivate ?: secBlockSecret
                        var n: String? = null
                        context.contentResolver.query(uri, null, null, null, null)?.use { c ->
                            val idx = c.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                            if (idx >= 0 && c.moveToFirst()) n = c.getString(idx)
                        }
                        val name = sanitizeImportedName(n ?: (pubBlock?.let { "ImportedPublic" } ?: "ImportedKey"))
                        if (pubBlock == null && secBlock == null) null else PendingImport(uri, pubBlock, secBlock, name)
                    }
                }
                queue = items
                if (queue.isEmpty()) {
                    Toast.makeText(context, context.getString(R.string.not_a_valid_public_key), Toast.LENGTH_LONG).show(); return@launch
                }
                // Start processing queue
                isBatchImporting = true
                currentIndex = 0
            } catch (e: Exception) {
                Toast.makeText(context, context.getString(R.string.read_failed_with_reason, e.message ?: ""), Toast.LENGTH_LONG).show()
            }
        }
    }

    val scrollState = rememberScrollState()
    if (showHelp) {
        HelpDialog(
            title = stringResource(R.string.help_import_title),
            body = stringResource(R.string.help_import_body),
            onDismiss = { showHelp = false }
        )
    }
    Column(modifier = modifier.fillMaxSize().verticalScroll(scrollState).padding(16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(R.string.import_public_key_title),
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.weight(1f)
            )
            HelpIconButton(onClick = { showHelp = true })
        }
        Spacer(modifier = Modifier.width(8.dp))
        Row(modifier = Modifier.fillMaxWidth()) {
            ElevatedButton(onClick = { openDocs.launch(arrayOf("*/*")) }, modifier = Modifier.weight(1f)) {
                Text(stringResource(R.string.choose_file))
            }
        }
        if (queue.isNotEmpty()) {
            Spacer(modifier = Modifier.width(8.dp))
            val idx = currentIndex ?: 0
            val cur = queue.getOrNull(idx)
            if (cur != null) {
                Text(text = stringResource(R.string.suggested_name, cur.name))
            }
            Spacer(modifier = Modifier.width(8.dp))
            ElevatedButton(enabled = isBatchImporting, onClick = {
                scope.launch {
                    var i = currentIndex ?: 0
                    while (i < queue.size) {
                        var item = queue[i]
                        var pub = item.pub
                        var sec = item.sec
                        if (sec != null && pub == null) {
                            pub = try { PgpUtils.derivePublicFromSecret(sec) } catch (_: Exception) { null }
                            if (pub == null) {
                                Toast.makeText(context, context.getString(R.string.public_key_missing_for_secret), Toast.LENGTH_LONG).show()
                                i++; currentIndex = i; continue
                            } else {
                                val q2 = queue.toMutableList(); q2[i] = q2[i].copy(pub = pub); queue = q2
                                item = queue[i]
                            }
                        }
                        if (sec != null && (item.pass.isNullOrEmpty())) {
                            passDialogText = ""
                            passDialogVisible = true
                            currentIndex = i
                            while (passDialogVisible) { delay(50) }
                            item = queue[i]
                        }
                        // If we have a secret, verify the provided passphrase before inserting
                        if (sec != null) {
                            val pass = item.pass
                            if (pass.isNullOrEmpty()) {
                                // No passphrase provided; skip this item to avoid incorrect imports
                                Toast.makeText(context, context.getString(R.string.please_enter_passphrase), Toast.LENGTH_SHORT).show()
                                i++; currentIndex = i; continue
                            }
                            // Ensure we have a public key (derive if needed)
                            if (pub == null) {
                                pub = try { PgpUtils.derivePublicFromSecret(sec) } catch (_: Exception) { null }
                            }
                            if (pub == null) {
                                Toast.makeText(context, context.getString(R.string.public_key_missing_for_secret), Toast.LENGTH_LONG).show()
                                i++; currentIndex = i; continue
                            }
                            val passChars = pass.toCharArray()
                            val verifyOk = try {
                                withContext(Dispatchers.IO) {
                                    val probePlain = "probe".toByteArray()
                                    val encOut = ByteArrayOutputStream()
                                    PgpUtils.encryptStream(pub, ByteArrayInputStream(probePlain), "probe.txt", encOut) { }
                                    val encBytes = encOut.toByteArray()
                                    val decOut = ByteArrayOutputStream()
                                    PgpUtils.decryptStream(sec, passChars, ByteArrayInputStream(encBytes), decOut) { }
                                    val dec = decOut.toByteArray()
                                    dec.contentEquals(probePlain)
                                }
                            } catch (_: Exception) { false }
                            java.util.Arrays.fill(passChars, '\u0000')
                            if (!verifyOk) {
                                Toast.makeText(context, context.getString(R.string.passphrase_incorrect), Toast.LENGTH_LONG).show()
                                // Skip inserting this item entirely when passphrase is wrong
                                i++; currentIndex = i; continue
                            }
                        }
                        val nameBase = item.name
                        val existing = withContext(Dispatchers.IO) { repo.listKeyNames() }
                        if (existing.any { it.equals(nameBase, ignoreCase = true) }) {
                            // If an entry with this name exists: if it lacks a secret, upgrade it by setting the secret
                            if (sec != null) {
                                val hasSec = withContext(Dispatchers.IO) { repo.hasSecret(nameBase) }
                                if (!hasSec) {
                                    val rows = withContext(Dispatchers.IO) { repo.updateKeysByName(nameBase, pub, sec) }
                                    if (rows > 0) {
                                        if (!item.pass.isNullOrEmpty()) {
                                            PassphraseStore.save(context, nameBase, item.pass!!)
                                            try {
                                                val kid = withContext(Dispatchers.IO) { PgpUtils.firstSecretKeyId(sec) }
                                                if (kid != null) PassphraseStore.saveByKeyId(context, kid, item.pass!!)
                                            } catch (_: Exception) { }
                                        }
                                        Toast.makeText(context, context.getString(R.string.import_success), Toast.LENGTH_SHORT).show()
                                    } else {
                                        Toast.makeText(context, context.getString(R.string.import_failed), Toast.LENGTH_SHORT).show()
                                    }
                                } else {
                                    Toast.makeText(context, context.getString(R.string.key_name_exists), Toast.LENGTH_LONG).show()
                                }
                            } else {
                                Toast.makeText(context, context.getString(R.string.key_name_exists), Toast.LENGTH_LONG).show()
                            }
                            i++; currentIndex = i; continue
                        }
                        val inserted = withContext(Dispatchers.IO) { repo.insertKey(nameBase, pub ?: "", sec ?: "") }
                        if (inserted != -1L) {
                            if (!item.pass.isNullOrEmpty() && sec != null) {
                                PassphraseStore.save(context, nameBase, item.pass!!)
                                try {
                                    val kid = withContext(Dispatchers.IO) { PgpUtils.firstSecretKeyId(sec) }
                                    if (kid != null) PassphraseStore.saveByKeyId(context, kid, item.pass!!)
                                } catch (_: Exception) { }
                            }
                            Toast.makeText(context, context.getString(R.string.import_success), Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(context, context.getString(R.string.import_failed), Toast.LENGTH_SHORT).show()
                        }
                        i++
                        currentIndex = i
                    }
                    isBatchImporting = false
                    onDone()
                }
            }, modifier = Modifier.fillMaxWidth()) {
                Text(stringResource(R.string.save_to_table))
            }
        }
        // Passphrase dialog for batch import of secret keys
        if (passDialogVisible) {
            AlertDialog(
                onDismissRequest = { passDialogVisible = false },
                title = { Text(text = stringResource(R.string.enter_passphrase)) },
                text = {
                    Column {
                        val idxNow = currentIndex ?: -1
                        val curName = queue.getOrNull(idxNow)?.name ?: ""
                        if (curName.isNotBlank()) {
                            Text(text = curName, modifier = Modifier.fillMaxWidth())
                            Spacer(modifier = Modifier.width(4.dp))
                        }
                        TextField(
                            value = passDialogText,
                            onValueChange = { passDialogText = it },
                            label = { Text(stringResource(R.string.passphrase)) },
                            singleLine = true,
                            visualTransformation = PasswordVisualTransformation(),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                },
                confirmButton = {
                    TextButton(onClick = {
                        val idx = currentIndex ?: -1
                        if (idx >= 0 && idx < queue.size) {
                            val updated = queue.toMutableList()
                            updated[idx] = updated[idx].copy(pass = passDialogText)
                            queue = updated
                        }
                        passDialogText = ""
                        passDialogVisible = false
                    }) { Text(stringResource(android.R.string.ok)) }
                },
                dismissButton = {
                    TextButton(onClick = {
                        // If cancelled, clear secret to import public-only
                        val idx = currentIndex ?: -1
                        if (idx >= 0 && idx < queue.size) {
                            val updated = queue.toMutableList()
                            val cur = updated[idx]
                            updated[idx] = cur.copy(sec = null)
                            queue = updated
                        }
                        passDialogText = ""
                        passDialogVisible = false
                    }) { Text(stringResource(android.R.string.cancel)) }
                }
            )
        }
        else if (pubText != null || secText != null) {
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = stringResource(R.string.suggested_name, suggestedName))
            if (secText != null) {
                Spacer(modifier = Modifier.width(8.dp))
                TextField(
                    value = passphrase,
                    onValueChange = { passphrase = it },
                    label = { Text(stringResource(R.string.passphrase)) },
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp)
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            val scope = rememberCoroutineScope()
            var isVerifying by remember { mutableStateOf(false) }
            ElevatedButton(onClick = {
                // Sanitize final name before saving as well (strip multi-extensions and trailing type suffixes)
                val name = sanitizeImportedName(suggestedName.ifBlank { "ImportedKey" })
                // Prevent duplicates unless upgrading a public-only entry with a secret
                val existing = repo.listKeyNames()
                val nameExists = existing.any { it.trim().equals(name, ignoreCase = true) }
                val sec = secText
                var pub = pubText
                if (sec != null) {
                    if (nameExists) {
                        // If the name exists, allow upgrade only if it currently has no secret
                        val hasSec = repo.hasSecret(name)
                        if (!hasSec) {
                            // verify passphrase before update (below)
                        } else {
                            Toast.makeText(context, context.getString(R.string.key_name_exists), Toast.LENGTH_LONG).show()
                            return@ElevatedButton
                        }
                    }
                    if (passphrase.isBlank()) {
                        Toast.makeText(context, context.getString(R.string.please_enter_passphrase), Toast.LENGTH_SHORT).show()
                        return@ElevatedButton
                    }
                    if (pub == null) {
                        // Try to derive public key from secret ring
                        try {
                            pub = PgpUtils.derivePublicFromSecret(sec)
                        } catch (e: Exception) {
                            pub = null
                        }
                        if (pub == null) {
                            Toast.makeText(context, context.getString(R.string.public_key_missing_for_secret), Toast.LENGTH_LONG).show()
                            return@ElevatedButton
                        }
                    }
                    if (isVerifying) return@ElevatedButton
                    isVerifying = true
                    val passChars = passphrase.toCharArray()
                    scope.launch {
                        try {
                            // First, verify passphrase can actually decrypt the secret key
                            val ok = withContext(Dispatchers.IO) { PgpUtils.verifySecretPassphrase(sec, passChars) }
                            if (!ok) throw org.bouncycastle.openpgp.PGPException("Incorrect passphrase")
                            // Redundant safeguard: probe encrypt/decrypt
                            withContext(Dispatchers.IO) {
                                val probePlain = "probe".toByteArray()
                                val encOut = ByteArrayOutputStream()
                                PgpUtils.encryptStream(pub, ByteArrayInputStream(probePlain), "probe.txt", encOut) { }
                                val encBytes = encOut.toByteArray()
                                val decOut = ByteArrayOutputStream()
                                PgpUtils.decryptStream(sec, passChars, ByteArrayInputStream(encBytes), decOut) { }
                                val dec = decOut.toByteArray()
                                if (!dec.contentEquals(probePlain)) throw IllegalStateException("Passphrase verify mismatch")
                            }
                            val success = if (nameExists) {
                                repo.updateKeysByName(name, pub!!, sec) > 0
                            } else {
                                repo.insertKey(name, pub!!, sec) != -1L
                            }
                            if (success) {
                                // Save passphrase for automatic decryption later
                                PassphraseStore.save(context, name, passphrase)
                                try {
                                    val kid = PgpUtils.firstSecretKeyId(sec)
                                    if (kid != null) PassphraseStore.saveByKeyId(context, kid, passphrase)
                                } catch (_: Exception) { }
                                Toast.makeText(context, context.getString(R.string.import_success), Toast.LENGTH_SHORT).show()
                                onDone()
                            } else {
                                Toast.makeText(context, context.getString(R.string.import_failed), Toast.LENGTH_SHORT).show()
                            }
                        } catch (e: Exception) {
                            val msg = if (e is org.bouncycastle.openpgp.PGPException) R.string.key_incorrect else R.string.passphrase_incorrect
                            Toast.makeText(context, context.getString(msg), Toast.LENGTH_LONG).show()
                        } finally {
                            isVerifying = false
                        }
                    }
                } else if (pub != null) {
                    if (!pub.contains("BEGIN PGP PUBLIC KEY BLOCK")) {
                        Toast.makeText(context, context.getString(R.string.not_a_valid_public_key), Toast.LENGTH_LONG).show()
                        return@ElevatedButton
                    }
                    val row = repo.insertPublicKey(name, pub)
                    if (row != -1L) {
                        Toast.makeText(context, context.getString(R.string.import_success), Toast.LENGTH_SHORT).show()
                        onDone()
                    } else {
                        Toast.makeText(context, context.getString(R.string.import_failed), Toast.LENGTH_SHORT).show()
                    }
                }
            }, modifier = Modifier.fillMaxWidth()) {
                Text(if (isVerifying) stringResource(R.string.verifying_passphrase) else stringResource(R.string.save_to_table))
            }
        }
    }
}

@Composable
fun ExportScreen(modifier: Modifier = Modifier, onDone: () -> Unit) {
    val context = LocalContext.current
    val repo = remember { KeyRepository(context) }
    var names by remember { mutableStateOf(listOf<String>()) }
    var selected by remember { mutableStateOf(setOf<String>()) }
    var mode by remember { mutableStateOf(ExportMode.PUBLIC_ONLY) }
    var hasSecretByName by remember { mutableStateOf<Map<String, Boolean>>(emptyMap()) }
    val highlightColor = if (isSystemInDarkTheme()) Color(0xFFFFD54F) else Color(0xFFF57C00)

    data class PendingExport(
        val name: String,
        val pub: String?,
        val sec: String?,
        var pass: String? = null
    )
    var queue by remember { mutableStateOf<List<PendingExport>>(emptyList()) }
    var currentIndex by remember { mutableStateOf(0) }
    var isBatchExporting by remember { mutableStateOf(false) }

    var showPassDialog by remember { mutableStateOf(false) }
    var passphrase by remember { mutableStateOf("") }
    var isCheckingPass by remember { mutableStateOf(false) }
    var showHelp by remember { mutableStateOf(false) }
    var pendingPub by remember { mutableStateOf<String?>(null) }
    var pendingSec by remember { mutableStateOf<String?>(null) }
    var pendingExportName by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    // Forward-declared function to proceed to the next export item
    var startNextExport by remember { mutableStateOf({}) }

    // Destination directory for batch export (optional). If set, we save files directly without per-file prompts
    var destDirUri by remember { mutableStateOf<Uri?>(null) }

    // Folder picker to choose a destination directory once
    val openTreeLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocumentTree()
    ) { uri ->
        if (uri != null) {
            try {
                val flags = Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                context.contentResolver.takePersistableUriPermission(uri, flags)
            } catch (_: Exception) { }
            destDirUri = uri
            if (queue.isNotEmpty()) startNextExport()
        } else {
            Toast.makeText(context, "Please select a destination folder", Toast.LENGTH_SHORT).show()
        }
    }

    // Helper to create/write a file inside the selected folder (single definition)
    fun writeToDestDir(fileName: String, data: String): Boolean {
        val dir = destDirUri?.let { DocumentFile.fromTreeUri(context, it) } ?: return false
        // If file exists, overwrite by deleting then creating
        dir.findFile(fileName)?.delete()
        val doc = dir.createFile("application/pgp-keys", fileName) ?: return false
        return try {
            context.contentResolver.openOutputStream(doc.uri)?.use { os ->
                os.write(data.trim().toByteArray())
                os.write('\n'.code)
            }
            true
        } catch (_: Exception) { false }
    }

    LaunchedEffect(Unit) {
        val (loadedNames, secretMap) = withContext(Dispatchers.IO) {
            val ns = repo.listKeyNames()
            val m = ns.associateWith { n ->
                val sec = repo.getArmoredSecretByName(n)
                !sec.isNullOrEmpty()
            }
            ns to m
        }
        names = loadedNames
        hasSecretByName = secretMap
    }

    // Launcher to save the public key
    val createPublicDocLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/pgp-keys")
    ) { uri ->
        val pub = pendingPub
        if (uri != null && pub != null) {
            try {
                context.contentResolver.openOutputStream(uri)?.use { os ->
                    os.write(pub.trim().toByteArray())
                    os.write('\n'.code)
                }
            } catch (e: Exception) {
                Toast.makeText(context, context.getString(R.string.export_failed_with_reason, e.message ?: ""), Toast.LENGTH_LONG).show()
            }
        }
        // Advance to next item in queue
        val next = currentIndex + 1
        if (next < queue.size) {
            currentIndex = next
            startNextExport()
        } else {
            // Done
            isBatchExporting = false
            pendingExportName = null
            pendingPub = null
            pendingSec = null
            Toast.makeText(context, context.getString(R.string.export_success_asc), Toast.LENGTH_SHORT).show()
            onDone()
        }
    }

    // Launcher to save the secret key, and then chain to public if needed
    val createSecretDocLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/pgp-keys")
    ) { uri ->
        val sec = pendingSec
        val name = pendingExportName
        if (uri != null && sec != null) {
            try {
                context.contentResolver.openOutputStream(uri)?.use { os ->
                    os.write(sec.trim().toByteArray())
                    os.write('\n'.code)
                }
            } catch (e: Exception) {
                Toast.makeText(context, context.getString(R.string.export_failed_with_reason, e.message ?: ""), Toast.LENGTH_LONG).show()
            }
        }
        // After saving secret, prompt for public
        val pub = pendingPub
        if (pub != null && name != null) {
            val suggested = "${name}-public.asc"
            createPublicDocLauncher.launch(suggested)
        } else {
            val next = currentIndex + 1
            if (next < queue.size) {
                currentIndex = next
                startNextExport()
            } else {
                isBatchExporting = false
                pendingExportName = null
                pendingPub = null
                pendingSec = null
                Toast.makeText(context, context.getString(R.string.export_success_asc), Toast.LENGTH_SHORT).show()
                onDone()
            }
        }
    }

    if (showHelp) {
        HelpDialog(
            title = stringResource(R.string.help_export_title),
            body = stringResource(R.string.help_export_body),
            onDismiss = { showHelp = false }
        )
    }

    Column(modifier = modifier.fillMaxSize().padding(16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(R.string.choose_key_to_export_title),
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.weight(1f)
            )
            HelpIconButton(onClick = { showHelp = true })
        }
        if (names.isEmpty()) {
            Text(text = stringResource(R.string.no_keys), modifier = Modifier.padding(top = 12.dp))
        } else {
            // Export mode chooser
            Row(modifier = Modifier.fillMaxWidth().padding(top = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                androidx.compose.material3.AssistChip(onClick = { mode = ExportMode.PUBLIC_ONLY }, label = { Text(stringResource(R.string.export_public_only)) },
                    colors = androidx.compose.material3.AssistChipDefaults.assistChipColors(
                        containerColor = if (mode == ExportMode.PUBLIC_ONLY) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surface,
                        labelColor = if (mode == ExportMode.PUBLIC_ONLY) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                    )
                )
                Spacer(modifier = Modifier.width(8.dp))
                androidx.compose.material3.AssistChip(onClick = { mode = ExportMode.BOTH }, label = { Text(stringResource(R.string.export_both_keys)) },
                    colors = androidx.compose.material3.AssistChipDefaults.assistChipColors(
                        containerColor = if (mode == ExportMode.BOTH) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surface,
                        labelColor = if (mode == ExportMode.BOTH) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                    )
                )
            }

            // Multi-select key list fills available space
            LazyColumn(modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(top = 12.dp)) {
                itemsIndexed(names) { _, n ->
                    val checked = selected.contains(n)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                selected = if (checked) selected - n else selected + n
                            }
                            .padding(vertical = 4.dp, horizontal = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        androidx.compose.material3.Checkbox(
                            checked = checked,
                            onCheckedChange = { isChecked ->
                                selected = if (isChecked) selected + n else selected - n
                            }
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        val isPrivate = hasSecretByName[n] == true
                        Text(
                            text = n,
                            style = if (isPrivate) MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold) else MaterialTheme.typography.bodyLarge,
                            color = if (isPrivate) highlightColor else MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.weight(1f)
                        )
                    }
                    Divider()
                }
            }

            Spacer(modifier = Modifier.width(8.dp))
            ElevatedButton(
                onClick = {
                    if (selected.isEmpty()) {
                        Toast.makeText(context, context.getString(R.string.please_select_key), Toast.LENGTH_SHORT).show(); return@ElevatedButton
                    }
                    scope.launch {
                        try {
                            // Build queue
                            val items = withContext(Dispatchers.IO) {
                                selected.map { n ->
                                    val pub = repo.getArmoredPublicByName(n)
                                    val sec = repo.getArmoredSecretByName(n)
                                    PendingExport(n, pub, if (mode == ExportMode.BOTH) sec else null)
                                }
                            }
                            queue = items
                            currentIndex = 0
                            isBatchExporting = true
                            // If destination folder chosen, write directly; otherwise ask user to choose folder or fallback to per-file create
                            if (destDirUri == null) {
                                openTreeLauncher.launch(null)
                            } else {
                                startNextExport()
                            }
                        } catch (e: Exception) {
                            Toast.makeText(context, context.getString(R.string.export_failed_with_reason, e.message ?: ""), Toast.LENGTH_LONG).show()
                        }
                    }
                },
                enabled = selected.isNotEmpty(),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(R.string.export))
            }
        }
    }

    // Dialog to require passphrase before exporting secret key (batch aware, shows key name)
    if (showPassDialog) {
        val cur = queue.getOrNull(currentIndex)
        AlertDialog(
            onDismissRequest = { if (!isCheckingPass) showPassDialog = false },
            title = { Text(text = stringResource(R.string.enter_passphrase)) },
            text = {
                Column {
                    if (cur != null) {
                        Text(text = cur.name, modifier = Modifier.fillMaxWidth())
                        Spacer(modifier = Modifier.width(4.dp))
                    }
                    TextField(
                        value = passphrase,
                        onValueChange = { passphrase = it },
                        label = { Text(stringResource(R.string.passphrase)) },
                        singleLine = true,
                        visualTransformation = PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    val item = queue.getOrNull(currentIndex)
                    if (item == null || item.pub.isNullOrBlank() || item.sec.isNullOrBlank()) { showPassDialog = false; return@TextButton }
                    if (passphrase.isBlank()) { Toast.makeText(context, context.getString(R.string.please_enter_passphrase), Toast.LENGTH_SHORT).show(); return@TextButton }
                    if (isCheckingPass) return@TextButton
                    isCheckingPass = true
                    val passChars = passphrase.toCharArray()
                    scope.launch {
                        try {
                            // Strict passphrase verify
                            val ok = withContext(Dispatchers.IO) { PgpUtils.verifySecretPassphrase(item.sec!!, passChars) }
                            if (!ok) throw org.bouncycastle.openpgp.PGPException("Incorrect passphrase")
                            // Also probe
                            withContext(Dispatchers.IO) {
                                val probePlain = "probe".toByteArray()
                                val encOut = java.io.ByteArrayOutputStream()
                                PgpUtils.encryptStream(item.pub, java.io.ByteArrayInputStream(probePlain), "probe.txt", encOut) { }
                                val encBytes = encOut.toByteArray()
                                val decOut = java.io.ByteArrayOutputStream()
                                PgpUtils.decryptStream(item.sec, passChars, java.io.ByteArrayInputStream(encBytes), decOut) { }
                                val dec = decOut.toByteArray()
                                if (!dec.contentEquals(probePlain)) throw IllegalStateException("Passphrase verify mismatch")
                            }
                            // Save pass into queue and continue
                            val updated = queue.toMutableList()
                            updated[currentIndex] = updated[currentIndex].copy(pass = passphrase)
                            queue = updated

                            // Export secret (and public) depending on chosen destination method
                            showPassDialog = false
                            if (destDirUri != null) {
                                // Write directly into selected folder without filename prompts
                                val secOk = if (item.sec != null) writeToDestDir("${item.name}-secret.asc", item.sec) else true
                                val pubOk = if (item.pub != null) writeToDestDir("${item.name}-public.asc", item.pub) else true
                                if (!secOk || !pubOk) {
                                    Toast.makeText(context, "Export failed", Toast.LENGTH_SHORT).show()
                                }
                                // Advance queue
                                val next = currentIndex + 1
                                if (next < queue.size) {
                                    currentIndex = next
                                    startNextExport()
                                } else {
                                    isBatchExporting = false
                                    pendingExportName = null
                                    pendingPub = null
                                    pendingSec = null
                                    Toast.makeText(context, context.getString(R.string.export_success_asc), Toast.LENGTH_SHORT).show()
                                    onDone()
                                }
                            } else {
                                // Fallback to CreateDocument flow (prompts filename)
                                pendingExportName = item.name
                                pendingPub = item.pub
                                pendingSec = item.sec
                                val suggested = "${item.name}-secret.asc"
                                createSecretDocLauncher.launch(suggested)
                            }
                        } catch (e: Exception) {
                            val msg = if (e is org.bouncycastle.openpgp.PGPException) R.string.passphrase_incorrect else R.string.passphrase_incorrect
                            Toast.makeText(context, context.getString(msg), Toast.LENGTH_LONG).show()
                        } finally {
                            isCheckingPass = false
                            java.util.Arrays.fill(passChars, '\u0000')
                            passphrase = ""
                        }
                    }
                }) { Text(stringResource(android.R.string.ok)) }
            },
            dismissButton = {
                TextButton(onClick = { if (!isCheckingPass) { showPassDialog = false; passphrase = "" } }) { Text(stringResource(android.R.string.cancel)) }
            }
        )
    }

    // Assign the action to proceed with next export item
    startNextExport = {
        val item = queue.getOrNull(currentIndex)
        if (item != null) {
            if (item.sec != null) {
                pendingExportName = item.name
                pendingPub = item.pub
                pendingSec = item.sec
                passphrase = ""
                showPassDialog = true
            } else {
                // Public-only export
                if (destDirUri != null && item.pub != null) {
                    val ok = writeToDestDir("${item.name}-public.asc", item.pub)
                    val next = currentIndex + 1
                    if (next < queue.size) {
                        currentIndex = next
                        startNextExport()
                    } else {
                        isBatchExporting = false
                        pendingExportName = null
                        pendingPub = null
                        pendingSec = null
                        Toast.makeText(context, context.getString(R.string.export_success_asc), Toast.LENGTH_SHORT).show()
                        onDone()
                    }
                } else {
                    // Fallback: per-file create dialog
                    pendingExportName = item.name
                    pendingPub = item.pub
                    pendingSec = null
                    val suggested = "${item.name}-public.asc"
                    createPublicDocLauncher.launch(suggested)
                }
            }
        }
    }

}

@Composable
fun DeleteScreen(modifier: Modifier = Modifier, onDone: () -> Unit) {
    val context = LocalContext.current
    val repo = remember { KeyRepository(context) }
    var names by remember { mutableStateOf(listOf<String>()) }
    var selected by remember { mutableStateOf(setOf<String>()) }
    var showConfirm by remember { mutableStateOf(false) }
    var hasSecretByName by remember { mutableStateOf<Map<String, Boolean>>(emptyMap()) }
    val highlightColor = if (isSystemInDarkTheme()) Color(0xFFFFD54F) else Color(0xFFF57C00)
    var showHelp by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        val (loadedNames, secretMap) = withContext(Dispatchers.IO) {
            val ns = repo.listKeyNames()
            val m = ns.associateWith { n ->
                val sec = repo.getArmoredSecretByName(n)
                !sec.isNullOrEmpty()
            }
            ns to m
        }
        names = loadedNames
        hasSecretByName = secretMap
    }

    if (showConfirm) {
        AlertDialog(
            onDismissRequest = { showConfirm = false },
            title = { Text(text = stringResource(R.string.confirm_delete_title)) },
            text = {
                val msg = if (selected.size == 1) {
                    stringResource(R.string.confirm_delete_message, selected.first())
                } else {
                    stringResource(R.string.confirm_delete_multiple_message, selected.size)
                }
                Text(text = msg)
            },
            confirmButton = {
                TextButton(onClick = {
                    showConfirm = false
                    try {
                        var total = 0
                        selected.forEach { name ->
                            total += repo.deleteByName(name)
                        }
                        if (total > 0) {
                            Toast.makeText(context, context.getString(R.string.delete_success), Toast.LENGTH_SHORT).show()
                            names = repo.listKeyNames()
                            selected = emptySet()
                            onDone()
                        } else {
                            Toast.makeText(context, context.getString(R.string.delete_failed), Toast.LENGTH_SHORT).show()
                        }
                    } catch (e: Exception) {
                        Toast.makeText(context, context.getString(R.string.delete_failed), Toast.LENGTH_SHORT).show()
                    }
                }) { Text(stringResource(android.R.string.ok)) }
            },
            dismissButton = {
                TextButton(onClick = { showConfirm = false }) { Text(stringResource(android.R.string.cancel)) }
            }
        )
    }

    if (showHelp) {
        HelpDialog(
            title = stringResource(R.string.help_delete_title),
            body = stringResource(R.string.help_delete_body),
            onDismiss = { showHelp = false }
        )
    }

    Column(modifier = modifier.padding(16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(R.string.choose_key_to_delete_title),
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.weight(1f)
            )
            HelpIconButton(onClick = { showHelp = true })
        }
        if (names.isEmpty()) {
            Text(text = stringResource(R.string.no_keys), modifier = Modifier.padding(top = 12.dp))
        } else {
            LazyColumn(modifier = Modifier
                .fillMaxWidth()
                .weight(1f, fill = false)
                .padding(top = 12.dp)) {
                itemsIndexed(names) { _, n ->
                    val checked = selected.contains(n)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                selected = if (checked) selected - n else selected + n
                            }
                            .padding(vertical = 12.dp, horizontal = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        androidx.compose.material3.Checkbox(
                            checked = checked,
                            onCheckedChange = { isChecked ->
                                selected = if (isChecked) selected + n else selected - n
                            }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        val isPrivate = hasSecretByName[n] == true
                        Text(
                            text = n,
                            style = if (isPrivate) MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold) else MaterialTheme.typography.bodyLarge,
                            color = if (isPrivate) highlightColor else MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.weight(1f)
                        )
                    }
                    Divider()
                }
            }
            Spacer(modifier = Modifier.width(8.dp))
            ElevatedButton(
                onClick = { showConfirm = true },
                enabled = selected.isNotEmpty(),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = stringResource(R.string.delete_selected, selected.size))
            }
        }
    }
}

@Composable
fun TextFileScreen(modifier: Modifier = Modifier, initialText: String? = null, autoDecrypt: Boolean = false, onDone: () -> Unit) {
    val context = LocalContext.current
    val repo = remember { KeyRepository(context) }
    var names by remember { mutableStateOf(listOf<String>()) }
    var selectedNames by remember { mutableStateOf(setOf<String>()) }
    var hasSecretByName by remember { mutableStateOf<Map<String, Boolean>>(emptyMap()) }
    var textContent by remember { mutableStateOf("") }
    var fileName by remember { mutableStateOf("data.txt") }
    var isBusy by remember { mutableStateOf(false) }
    var isEncrypting by remember { mutableStateOf(false) }
    var isDecrypting by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val highlightColor = if (isSystemInDarkTheme()) Color(0xFFFFD54F) else Color(0xFFF57C00)
    var showHelp by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        val (loadedNames, secretMap) = withContext(Dispatchers.IO) {
            val ns = repo.listKeyNames()
            val m = ns.associateWith { n ->
                val sec = repo.getArmoredSecretByName(n)
                !sec.isNullOrEmpty()
            }
            ns to m
        }
        names = loadedNames
        hasSecretByName = secretMap
    }

    // Prefill from share and optionally auto-decrypt if armored
    LaunchedEffect(initialText, autoDecrypt) {
        if (!initialText.isNullOrEmpty()) {
            textContent = initialText
            if (autoDecrypt) {
                try {
                    isBusy = true
                    val inputBytes = textContent.toByteArray(Charsets.UTF_8)
                    val recipientIds = try {
                        ByteArrayInputStream(inputBytes).use { ins ->
                            PgpUtils.extractRecipientKeyIds(ins)
                        }
                    } catch (_: Exception) { emptyList() }

                    val names = withContext(Dispatchers.IO) { repo.listKeyNames() }
                    // Preload secrets once to avoid repetitive DB calls in loops
                    val allSecrets: Map<String, String?> = withContext(Dispatchers.IO) {
                        names.associateWith { n -> repo.getArmoredSecretByName(n) }
                    }
                    data class Candidate(val name: String, val secret: String, val pass: CharArray)
                    var needPassFor: String? = null

                    // 1) Prefer stored passphrases by keyId
                    if (recipientIds.isNotEmpty()) {
                        loop@ for (id in recipientIds) {
                            val savedById = PassphraseStore.getByKeyId(context, id)
                            if (!savedById.isNullOrEmpty()) {
                                for (n in names) {
                                    val sec = allSecrets[n] ?: continue
                                    val hasId = try { PgpUtils.secretContainsKeyId(sec, id) } catch (_: Exception) { false }
                                    if (!hasId) continue
                                    try {
                                        val res = withContext(Dispatchers.IO) { PgpUtils.decryptBytes(sec, savedById.toCharArray(), inputBytes) }
                                        textContent = res.bytes.toString(Charsets.UTF_8)
                                        if (!res.fileName.isNullOrBlank()) fileName = res.fileName
                                        Toast.makeText(context, "Şifre çözüldü", Toast.LENGTH_SHORT).show()
                                        break@loop
                                    } catch (_: Exception) { /* try next */ }
                                }
                            }
                        }
                    }

                    // 2) Try name-based stored passphrases or empty pass for secrets that match recipient IDs
                    if (recipientIds.isNotEmpty()) {
                        for (n in names) {
                            val sec = allSecrets[n] ?: continue
                            val matchesId = recipientIds.any { id ->
                                try { PgpUtils.secretContainsKeyId(sec, id) } catch (_: Exception) { false }
                            }
                            if (!matchesId) continue
                            val savedPass = PassphraseStore.get(context, n)
                            if (!savedPass.isNullOrEmpty()) {
                                try {
                                    val res = withContext(Dispatchers.IO) { PgpUtils.decryptBytes(sec, savedPass.toCharArray(), inputBytes) }
                                    textContent = res.bytes.toString(Charsets.UTF_8)
                                    if (!res.fileName.isNullOrBlank()) fileName = res.fileName
                                    Toast.makeText(context, "Şifre çözüldü", Toast.LENGTH_SHORT).show()
                                    return@LaunchedEffect
                                } catch (_: Exception) { /* try empty or next */ }
                            }
                            try {
                                val res = withContext(Dispatchers.IO) { PgpUtils.decryptBytes(sec, CharArray(0), inputBytes) }
                                textContent = res.bytes.toString(Charsets.UTF_8)
                                if (!res.fileName.isNullOrBlank()) fileName = res.fileName
                                Toast.makeText(context, "Şifre çözüldü", Toast.LENGTH_SHORT).show()
                                return@LaunchedEffect
                            } catch (_: Exception) { needPassFor = n }
                        }
                    }

                    // 3) Fallback when no recipient IDs: try all secrets with stored pass, then empty pass
                    if (recipientIds.isEmpty()) {
                        for (n in names) {
                            val sec = allSecrets[n] ?: continue
                            val savedPass = PassphraseStore.get(context, n)
                            if (!savedPass.isNullOrEmpty()) {
                                try {
                                    val res = withContext(Dispatchers.IO) { PgpUtils.decryptBytes(sec, savedPass.toCharArray(), inputBytes) }
                                    textContent = res.bytes.toString(Charsets.UTF_8)
                                    if (!res.fileName.isNullOrBlank()) fileName = res.fileName
                                    Toast.makeText(context, "Şifre çözüldü", Toast.LENGTH_SHORT).show()
                                    return@LaunchedEffect
                                } catch (_: Exception) { /* continue */ }
                            }
                            try {
                                val res = withContext(Dispatchers.IO) { PgpUtils.decryptBytes(sec, CharArray(0), inputBytes) }
                                textContent = res.bytes.toString(Charsets.UTF_8)
                                if (!res.fileName.isNullOrBlank()) fileName = res.fileName
                                Toast.makeText(context, "Şifre çözüldü", Toast.LENGTH_SHORT).show()
                                return@LaunchedEffect
                            } catch (_: Exception) { /* continue */ }
                        }
                    }

                    if (needPassFor != null) {
                        Toast.makeText(context, "Şifre çözülemedi: Bu anahtar için parola gerekli", Toast.LENGTH_LONG).show()
                    }
                } catch (e: Exception) {
                    Toast.makeText(context, "Şifre çözme başarısız: ${e.message}", Toast.LENGTH_LONG).show()
                } finally {
                    isBusy = false
                }
            }
        }
    }

    val openDoc = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            try {
                scope.launch {
                    val (n, t) = withContext(Dispatchers.IO) {
                        val nm = context.contentResolver.query(uri, null, null, null, null)?.use { c ->
                            val idx = c.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                            if (idx >= 0 && c.moveToFirst()) c.getString(idx) else null
                        }
                        val txt = context.contentResolver.openInputStream(uri)?.use { it.readBytes().toString(Charsets.UTF_8) } ?: ""
                        nm to txt
                    }
                    fileName = (n ?: "data.txt").ifBlank { "data.txt" }
                    textContent = t
                }
                Toast.makeText(context, context.getString(R.string.text_file_opened), Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Toast.makeText(context, context.getString(R.string.read_failed_with_reason, e.message ?: ""), Toast.LENGTH_LONG).show()
            }
        }
    }

    val saveDoc = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("text/plain")
    ) { uri ->
        if (uri != null) {
            try {
                scope.launch(Dispatchers.IO) {
                    context.contentResolver.openOutputStream(uri)?.use { os ->
                        os.write(textContent.toByteArray(Charsets.UTF_8))
                    }
                    runCatching { ensureFixedExtension(context, uri, "txt") }
                    runCatching { renameIfProviderAddedSuffix(context, uri) }
                    withContext(Dispatchers.Main) {
                        Toast.makeText(context, context.getString(R.string.file_saved), Toast.LENGTH_SHORT).show()
                        onDone()
                    }
                }
            } catch (e: Exception) {
                Toast.makeText(context, context.getString(R.string.save_failed_with_reason, e.message ?: ""), Toast.LENGTH_LONG).show()
            }
        }
    }

    if (showHelp) {
        HelpDialog(
            title = stringResource(R.string.help_text_title),
            body = stringResource(R.string.help_text_body),
            onDismiss = { showHelp = false }
        )
    }

    Column(modifier = modifier.fillMaxSize().padding(16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(R.string.text_file_title),
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.weight(1f)
            )
            HelpIconButton(onClick = { showHelp = true })
        }
        Spacer(modifier = Modifier.width(8.dp))

        Row(modifier = Modifier.fillMaxWidth()) {
            ElevatedButton(onClick = {
                openDoc.launch(
                    arrayOf(
                        "text/*",
                        "application/msword", // .doc
                        "application/vnd.openxmlformats-officedocument.wordprocessingml.document", // .docx
                        "application/rtf", // .rtf
                        "application/vnd.oasis.opendocument.text" // .odt
                    )
                )
            }, modifier = Modifier.weight(1f)) {
                Text(stringResource(R.string.choose_file))
            }
            Spacer(modifier = Modifier.width(8.dp))
            ElevatedButton(onClick = {
                val base0 = if (fileName.endsWith(".txt", true)) fileName.dropLast(4) else if (fileName.endsWith(".asc", true)) fileName.dropLast(4) else fileName
                val suggested = "$base0.txt"
                saveDoc.launch(suggested)
            }, modifier = Modifier.weight(1f)) {
                Text(stringResource(R.string.save_textfile_action))
            }
        }

        Spacer(modifier = Modifier.width(8.dp))

        Text(text = stringResource(R.string.select_keys_label))
        Spacer(modifier = Modifier.width(4.dp))
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            if (names.isEmpty()) {
                Text(stringResource(R.string.no_keys), modifier = Modifier.padding(12.dp))
            } else {
                LazyColumn(modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 288.dp) // roughly 6 rows visible
                    .padding(4.dp)) {
                    itemsIndexed(names) { _, n ->
                        val selected = selectedNames.contains(n)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    selectedNames = if (selected) selectedNames - n else selectedNames + n
                                }
                                .padding(horizontal = 12.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            androidx.compose.material3.Checkbox(
                                checked = selected,
                                onCheckedChange = { isChecked ->
                                    selectedNames = if (isChecked) selectedNames + n else selectedNames - n
                                }
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            val isPrivate = hasSecretByName[n] == true
                            Text(
                                text = n,
                                style = if (isPrivate) MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold) else MaterialTheme.typography.bodyLarge,
                                color = if (isPrivate) highlightColor else MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.weight(1f)
                            )
                        }
                        Divider()
                    }
                }
            }
        }

        Spacer(modifier = Modifier.width(8.dp))
        Row(modifier = Modifier.fillMaxWidth()) {
            ElevatedButton(
                enabled = !isEncrypting,
                onClick = {
                    if (textContent.isBlank()) {
                        Toast.makeText(context, context.getString(R.string.no_text_to_encrypt), Toast.LENGTH_SHORT).show(); return@ElevatedButton
                    }
                    val chosen = selectedNames
                    if (chosen.isEmpty()) {
                        Toast.makeText(context, context.getString(R.string.please_select_at_least_one_public_key), Toast.LENGTH_SHORT).show(); return@ElevatedButton
                    }
                    scope.launch {
                        try {
                            isEncrypting = true
                            val nameForPayload = if (fileName.isBlank()) "data.txt" else fileName
                            val result = withContext(Dispatchers.IO) {
                                val pubs = chosen.mapNotNull { repo.getArmoredPublicByName(it) }
                                if (pubs.isEmpty()) throw IllegalStateException(context.getString(R.string.key_not_found))
                                val armored = PgpUtils.encryptTextArmored(pubs, textContent, nameForPayload)
                                armored
                            }
                            textContent = result
                            val baseName = if (nameForPayload.endsWith(".asc", true)) nameForPayload.dropLast(4) else nameForPayload
                            fileName = if (baseName.endsWith(".txt", true)) baseName else "$baseName.txt"
                            Toast.makeText(context, context.getString(R.string.encrypted_success), Toast.LENGTH_SHORT).show()
                        } catch (e: Exception) {
                            Toast.makeText(context, context.getString(R.string.encrypt_failed_with_reason, e.message ?: ""), Toast.LENGTH_LONG).show()
                        } finally {
                            isEncrypting = false
                        }
                    }
                },modifier = Modifier.weight(1f)
            ) { Text(stringResource(R.string.encrypt_text)) }
            Spacer(modifier = Modifier.width(8.dp))

            ElevatedButton(
                enabled = !isDecrypting,
                onClick = {
                    // Auto-detect secret key and passphrase similar to DecryptScreen
                    scope.launch {
                        try {
                            isDecrypting = true
                            val inputBytes = textContent.toByteArray(Charsets.UTF_8)
                            val recipientIds = try {
                                withContext(Dispatchers.IO) {
                                    ByteArrayInputStream(inputBytes).use { ins ->
                                        PgpUtils.extractRecipientKeyIds(ins)
                                    }
                                }
                            } catch (_: Exception) { emptyList() }

                            val names = withContext(Dispatchers.IO) { repo.listKeyNames() }
                            var needPassFor: String? = null

                            // 1) Prefer stored passphrases by keyId
                            if (recipientIds.isNotEmpty()) {
                                loop@ for (id in recipientIds) {
                                    val savedById = PassphraseStore.getByKeyId(context, id)
                                    if (!savedById.isNullOrEmpty()) {
                                        for (n in names) {
                                            val sec = withContext(Dispatchers.IO) { repo.getArmoredSecretByName(n) } ?: continue
                                            val hasId = try { PgpUtils.secretContainsKeyId(sec, id) } catch (_: Exception) { false }
                                            if (!hasId) continue
                                            try {
                                                val res = withContext(Dispatchers.IO) { PgpUtils.decryptBytes(sec, savedById.toCharArray(), inputBytes) }
                                                textContent = res.bytes.toString(Charsets.UTF_8)
                                                if (!res.fileName.isNullOrBlank()) fileName = res.fileName
                                                Toast.makeText(context, context.getString(R.string.decrypted_success), Toast.LENGTH_SHORT).show()
                                                return@launch
                                            } catch (_: Exception) { /* try next */ }
                                        }
                                    }
                                }
                            }

                            // 2) Try name-based stored passphrases or empty pass for secrets that match recipient IDs
                            if (recipientIds.isNotEmpty()) {
                                for (n in names) {
                                    val sec = withContext(Dispatchers.IO) { repo.getArmoredSecretByName(n) } ?: continue
                                    val matchesId = recipientIds.any { id ->
                                        try { PgpUtils.secretContainsKeyId(sec, id) } catch (_: Exception) { false }
                                    }
                                    if (!matchesId) continue
                                    val savedPass = PassphraseStore.get(context, n)
                                    if (!savedPass.isNullOrEmpty()) {
                                        try {
                                            val res = withContext(Dispatchers.IO) { PgpUtils.decryptBytes(sec, savedPass.toCharArray(), inputBytes) }
                                            textContent = res.bytes.toString(Charsets.UTF_8)
                                            if (!res.fileName.isNullOrBlank()) fileName = res.fileName
                                            Toast.makeText(context, context.getString(R.string.decrypted_success), Toast.LENGTH_SHORT).show()
                                            return@launch
                                        } catch (_: Exception) { /* try empty or next */ }
                                    }
                                    try {
                                        val res = withContext(Dispatchers.IO) { PgpUtils.decryptBytes(sec, CharArray(0), inputBytes) }
                                        textContent = res.bytes.toString(Charsets.UTF_8)
                                        if (!res.fileName.isNullOrBlank()) fileName = res.fileName
                                        Toast.makeText(context, context.getString(R.string.decrypted_success), Toast.LENGTH_SHORT).show()
                                        return@launch
                                    } catch (_: Exception) { needPassFor = n }
                                }
                            }

                            // 3) Fallback when no recipient IDs: try all secrets with stored pass, then empty pass
                            if (recipientIds.isEmpty()) {
                                for (n in names) {
                                    val sec = withContext(Dispatchers.IO) { repo.getArmoredSecretByName(n) } ?: continue
                                    val savedPass = PassphraseStore.get(context, n)
                                    if (!savedPass.isNullOrEmpty()) {
                                        try {
                                            val res = withContext(Dispatchers.IO) { PgpUtils.decryptBytes(sec, savedPass.toCharArray(), inputBytes) }
                                            textContent = res.bytes.toString(Charsets.UTF_8)
                                            if (!res.fileName.isNullOrBlank()) fileName = res.fileName
                                            Toast.makeText(context, context.getString(R.string.decrypted_success), Toast.LENGTH_SHORT).show()
                                            return@launch
                                        } catch (_: Exception) { /* continue */ }
                                    }
                                    try {
                                        val res = withContext(Dispatchers.IO) { PgpUtils.decryptBytes(sec, CharArray(0), inputBytes) }
                                        textContent = res.bytes.toString(Charsets.UTF_8)
                                        if (!res.fileName.isNullOrBlank()) fileName = res.fileName
                                        Toast.makeText(context, context.getString(R.string.decrypted_success), Toast.LENGTH_SHORT).show()
                                        return@launch
                                    } catch (_: Exception) { /* continue */ }
                                }
                            }

                            if (needPassFor != null) {
                                Toast.makeText(context, context.getString(R.string.passphrase_required_auto_decrypt), Toast.LENGTH_LONG).show()
                            } else {
                                Toast.makeText(context, context.getString(R.string.no_matching_secret_key), Toast.LENGTH_LONG).show()
                            }
                        } catch (e: Exception) {
                            Toast.makeText(context, context.getString(R.string.decrypt_failed_with_reason, e.message ?: ""), Toast.LENGTH_LONG).show()
                        } finally {
                            isDecrypting = false
                        }
                    }
                }, modifier = Modifier.weight(1f)
            ) { Text(stringResource(R.string.decrypt_text)) }
        }

        Spacer(modifier = Modifier.width(8.dp))
        Text(text = stringResource(R.string.content_label))
        TextField(
            value = textContent,
            onValueChange = { textContent = it },
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp)
                .heightIn(min = 160.dp, max = 360.dp),
            minLines = 8,
            maxLines = 20
        )
    }
}

@Preview(showBackground = true)
@Composable
fun DeleteScreenPreview() {
    AzadiTheme {
        DeleteScreen(onDone = {})
    }
}
@Preview(showBackground = true)
@Composable
fun HomePreview() {
    AzadiTheme {
        HomeScreen(onCreateKey = {}, onExport = {}, onEncrypt = {}, onDecrypt = {}, onImport = {}, onTextFile = {})
    }
}

@Composable
fun AboutScreen(modifier: Modifier = Modifier, onDone: () -> Unit = {}) {
    val context = LocalContext.current
    val resId = remember {
        // Try to resolve an image resource for "Heval Gabar.jpg"; fallback to logo
        val candidates = listOf("heval_gabar", "heval_gabar_jpg", "hevalgabar", "heval_gabar__1")
        var id = 0
        for (c in candidates) {
            id = context.resources.getIdentifier(c, "mipmap", context.packageName)
            if (id != 0) break
        }
        if (id != 0) id else R.mipmap.logo
    }
    Column(
        modifier = modifier
            .padding(16.dp)
            .fillMaxSize()
            .let { it.verticalScroll(androidx.compose.foundation.rememberScrollState()) },
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        androidx.compose.foundation.Image(
            painter = painterResource(id = resId),
            contentDescription = null,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
        )
        Text(
            text = stringResource(R.string.about_title),
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            modifier = Modifier.padding(bottom = 12.dp)
        )
        Text(
            text = stringResource(R.string.about_text),
            style = MaterialTheme.typography.bodyMedium
        )
    }
}
