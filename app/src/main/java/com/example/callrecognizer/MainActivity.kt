package com.example.callrecognizer

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.provider.ContactsContract
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CallMade
import androidx.compose.material.icons.rounded.CallMissed
import androidx.compose.material.icons.rounded.CallReceived
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.example.callrecognizer.contacts.Contact
import com.example.callrecognizer.contacts.getData
import com.example.callrecognizer.database.*
import com.example.callrecognizer.ui.theme.CallRecognizerTheme
import com.example.callrecognizer.ui.theme.Purple500
import com.example.callrecognizer.ui.viewmodel.MainViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.util.*


class MainActivity : ComponentActivity() {
    private val applicationScope = CoroutineScope(SupervisorJob())

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val appDatabase = AppDatabase.instance(applicationContext, applicationScope)

        installSplashScreen().apply {
            setKeepOnScreenCondition{
                viewModel.isLoading.value
            }
        }
        startForegroundService()
        setContent {
            CallRecognizerTheme(darkTheme = true) {
                val scaffoldState = rememberScaffoldState()
                val snackbarCoroutineScope = rememberCoroutineScope()
                val recordings = appDatabase.callRecordingsDao().list().collectAsState(initial = emptyList())
                val context = LocalContext.current

                Scaffold(
                    scaffoldState = scaffoldState,
                    topBar = { TopBar("Call Recognizer") }
                ) { innerPadding ->
                    Surface(
                        modifier = Modifier.padding(innerPadding),
                        color = MaterialTheme.colors.background
                    ) {
                        CallList(recordings.value) { _, call ->
                            snackbarCoroutineScope.launch {
                                scaffoldState.snackbarHostState
                                    .showSnackbar("Call recording ${call.id} clicked")
                            }
                        }
                    }
                }
            }
        }
    }
}


@Composable
fun TopBar(title: String) {
    TopAppBar(
        title = { Text(text = title) }
    )
}

@Composable
fun CallList(
    recordings: List<RegisteredCall>,
    onClick: (index: Int, recording: RegisteredCall) -> Unit,
) {
    val scrollState = rememberLazyListState()

    LazyColumn(state = scrollState) {
        itemsIndexed(recordings) { index, recording ->
            CallItem(recording) { onClick(index, recording) }
        }
    }
}

@Composable
fun CallItem(
    registeredCall: RegisteredCall,
    onClick: () -> Unit = {},
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(color = MaterialTheme.colors.background),
                contentAlignment = Alignment.Center
            ) {
                when(registeredCall.callType) {

                    CallType.INCOMING -> {
                        Icon(
                            imageVector = Icons.Rounded.CallReceived,
                            contentDescription = "INCOMING",
                            tint = Color.Green
                        )
                    }

                    CallType.OUTGOING -> {
                        Icon(
                            imageVector = Icons.Rounded.CallMade,
                            contentDescription = "OUTGOING",
                            tint = Purple500
                        )
                    }

                    CallType.MISSED -> {
                        Icon(
                            imageVector = Icons.Rounded.CallMissed,
                            contentDescription = "MISSED",
                            tint = Color.Red
                        )
                    }
                }
            }
            Column(modifier = Modifier.padding(8.dp)) {
                Text(text = registeredCall.source)
                Text(
                    text = humanReadableDuration(registeredCall.duration.toString()),
                    style = MaterialTheme.typography.caption
                )
            }
        }
    }
}

fun humanReadableDuration(s: String): String = java.time.Duration.parse(s).toString()
    .substring(2).lowercase().replace(Regex("[hms](?!\$)")) { "${it.value} " }

@Preview(showBackground = true)
@Composable
fun CallItemPreview() {
    CallItem(
        registeredCall = callFromId(0)
    )
}

@SuppressLint("Range")

fun getNamePhoneDetails(context: Context): MutableList<Contact> {
    val names = mutableListOf<Contact>()
    val cr = context.contentResolver
    val cur = cr.query(
        ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null,
        null, null, null)
    if (cur!!.count > 0) {
        while (cur.moveToNext()) {
            val id = cur.getString(cur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NAME_RAW_CONTACT_ID))
            val name = cur.getString(cur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME))
            val number = cur.getString(cur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER))
            names.add(Contact(id , name , number))
        }
    }
    cur.close()
    return names
}