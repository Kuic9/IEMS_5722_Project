package com.example.iems5722_project

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.util.Locale
import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts

import androidx.compose.foundation.clickable

import java.io.FileInputStream
import java.io.OutputStream
import java.net.HttpURLConnection
import java.net.URL
import java.io.BufferedWriter
import java.io.File
import java.io.OutputStreamWriter


import java.text.SimpleDateFormat
import java.util.Date

//uvicorn main:app --host 0.0.0.0 --port 55722
class ChatActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val chatroomId = intent.getIntExtra("chatroom_id", -1)
            if (chatroomId != -1) {
                ChatScreen(chatroomId)
            }
        }
    }
}

@Composable
fun ChatScreen(chatroomId: Int) {
    val context = LocalContext.current // 提取上下文
    val messages = remember {
        mutableStateListOf<Quadruple<String, String, String,Boolean,Uri?>>().apply {
//            addAll(initialMessages) // 将初始消息添加到列表中
        }
    }
    // 消息和时间的 Pair 列表
    var currentMessage by remember { mutableStateOf("") }
    var selectedFileUri by remember { mutableStateOf<Uri?>(null) } // 存储选中的文件 URI
    val coroutineScope = rememberCoroutineScope() // 创建一个协程作用域

    // 启动时获取消息列表
    LaunchedEffect(chatroomId) {
        val fetchedMessages = fetchMessages(chatroomId)
        messages.addAll(fetchedMessages)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White) // 设置聊天背景
    ) {
        Column(
            modifier = Modifier.fillMaxSize()

        ) {
            //页眉布局,横向排版
            Row(
                modifier = Modifier.fillMaxWidth().padding(10.dp),  // 设置一些填充
                verticalAlignment = Alignment.CenterVertically      // 垂直居中对齐
            ) {
                Button(onClick = {
                    val intent = Intent(context, MainActivity::class.java)
                    context.startActivity(intent)
                }) {
                    Text(text = "Back")
                }
                Spacer(modifier = Modifier.weight(1f))

                Text(
                    text = "Chatroom id:$chatroomId",
                    fontSize = 24.sp,
                    color = Color.DarkGray,
                    modifier = Modifier.align(Alignment.CenterVertically)
                )
                Spacer(modifier = Modifier.weight(1f))

                // 添加 Refresh 按钮
                Button(onClick = {
                    messages.clear() // 清空现有消息
                    coroutineScope.launch {
                        val fetchedMessages = fetchMessages(chatroomId)
                        Log.d("refreshh",fetchedMessages.toString())
                        messages.addAll(fetchedMessages) // 重新获取消息
                    }
                }) {
                    Text(text = "Refresh")
                }
            }
            // 分割线
            HorizontalDivider(modifier = Modifier.fillMaxWidth(), thickness = 1.dp, color = Color.Gray)

            // LazyColumn显示之前的信息
            Column(modifier = Modifier.fillMaxSize()) {
                // LazyColumn显示之前的消息
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.Bottom
                ) {
                    items(messages.size) { index ->
                        val (message, timestamp,name, isOwnMessage,fileUrl) = messages[index]
                        val verticalArrangement: Arrangement.Vertical = if (name == "gpt") {
                            Arrangement.Bottom// 确保这是垂直排列的有效值
                        } else {
                            Arrangement.Top // 同上，确保这是垂直排列的有效值
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth().padding(8.dp),
                            horizontalArrangement = if (isOwnMessage) Arrangement.End else Arrangement.Start, // 根据消息发送者对齐
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Spacer(modifier = Modifier.padding(5.dp))
                            if (name!="gpt") {
                                // 如果不是gpt的消息，name 和 timestamp 横向排列
                                //文本框
                                Box(
                                    modifier = Modifier.background(Color(0xFFE6E6FA), RoundedCornerShape(8.dp)) // 填充底色，圆角8.dp
                                        .border(1.dp, Color.DarkGray, RoundedCornerShape(8.dp)) .padding(8.dp) // 内边距
                                ) {
                                    // 显示消息内容
                                    Text(text = message,)
                                }

                                if (fileUrl.toString() != "file://null") {
                                    // 显示文件链接
                                    Log.d("fileurll",fileUrl.toString())
                                    Text(
                                        text = "View File",
                                        color = Color.Blue,
                                        modifier = Modifier.clickable {
                                            // 显示 fileUri 字符串
                                            val fileUriString = fileUrl.toString()
                                            Toast.makeText(context, fileUriString, Toast.LENGTH_LONG).show() // 以 Toast 弹窗显示文件 URI
                                        }

                                    )
                                }

                                Row(
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // 显示时间
                                    Text(text = timestamp, color = Color.Gray)

                                    Spacer(modifier = Modifier.padding(5.dp))

                                    // 显示发送者
                                    Text(text = name, color = Color.Gray)
                                }
                            } else {
                                // 如果是gpt的消息，name 和 timestamp 竖向排列
                                Column(horizontalAlignment = Alignment.Start) {
                                    Text(text = name, color = Color.Black, fontSize = 18.sp)
                                    Spacer(modifier = Modifier.height(4.dp))
//                                    Text(text = timestamp, color = Color.Gray, fontSize = 10.sp)
//                                    Spacer(modifier = Modifier.height(8.dp))
                                    //文本框
                                    Box(
                                        modifier = Modifier.background(
                                            Color(0xFFE6E6FA),
                                            RoundedCornerShape(8.dp)
                                        ) // 填充底色，圆角8.dp
                                            .border(1.dp, Color.DarkGray, RoundedCornerShape(8.dp))
                                            .padding(8.dp) // 内边距
                                    ) {
                                        // 显示消息内容
                                        Text(text = message,)
                                    }

                                }
                            }
                        }
                    }
                }

                // 添加分割线
                Spacer(modifier = Modifier.height(8.dp))
                HorizontalDivider(
                    modifier = Modifier.fillMaxWidth(),
                    color = Color.Gray
                )
                Spacer(modifier = Modifier.height(8.dp))
// 使用 ActivityResultContracts 来处理文件选择
                val getContent = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
                    selectedFileUri = uri // 设置选中的文件 URI
                }
                //输入
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    BasicTextField(
                        value = currentMessage,
                        onValueChange = { currentMessage = it },
                        modifier = Modifier.weight(1f).padding(8.dp)
                    )
                    // 文件选择按钮
                    IconButton(onClick = {
                        getContent.launch("*/*") // 启动文件选择器
                    }) {
                        Icon(
                            painter = painterResource(id = R.drawable.fileicon), // 替换发送图标的资源ID
                            contentDescription = "file",
                            tint = Color.Unspecified
                        )
                    }


                    //发送按钮
                    IconButton(onClick = {
                        if (currentMessage.isNotBlank() || selectedFileUri != null) {
                            val timestamp = getCurrentTime()

                            // 添加消息到本地列表
                            messages.add(
                                Quadruple(
                                    currentMessage,
                                    timestamp,
                                    displayName,
                                    true,
                                    selectedFileUri
                                )
                            )

                            coroutineScope.launch {
                                // 用户选择了文件，上传文件并发送带文件的消息
                                Log.d("msgcollection", selectedFileUri.toString())
                                sendMessageWithFile(
                                    context,
                                    chatroomId,
                                    currentMessage,
                                    selectedFileUri,
                                    timestamp
                                )

                                // 清空当前消息框
                                currentMessage = ""
                                selectedFileUri = null // 清空选中的文件
                            }
                        }
                    }) {
                        Icon(
                            painter = painterResource(id = R.drawable.send), // 替换发送图标的资源ID
                            contentDescription = "Send Message",
                            tint = Color.Unspecified
                        )
                    }
                    //gpt按钮
                    IconButton(onClick = {
                        if (currentMessage.isNotBlank() || selectedFileUri != null) {
                            val timestamp = getCurrentTime()
                            val msg = currentMessage
                            // 清空当前消息框
                            currentMessage = ""

                            // 添加消息到本地列表
                            messages.add(
                                Quadruple(
                                    msg,
                                    timestamp,
                                    displayName,
                                    true,
                                    selectedFileUri
                                )
                            )

                            coroutineScope.launch {
                                // 用户选择了文件，上传文件并发送带文件的消息
                                sendMessageTogpt(
                                    context,
                                    chatroomId,
                                    msg,
                                    selectedFileUri,
                                    timestamp
                                )


                                selectedFileUri = null // 清空选中的文件
                            }
                        }
                    }) {
                        Icon(
                            painter = painterResource(id = R.drawable.gpt), // 替换发送图标的资源ID
                            contentDescription = "gpt",
                            tint = Color.Unspecified
                        )
                    }


                }

                Spacer(modifier = Modifier.height(16.dp))



            }
        }
    }
}


//上传文件到服务器
private suspend fun uploadFileUsingHttpURLConnection(context: Context, uri: Uri): String? {
    return withContext(Dispatchers.IO) {
        val url = URL("http://10.0.2.2:55722/upload_file/") // 替换成你的上传 URL
        val boundary = "*****" // 你可以自定义一个分隔符
        val connection = url.openConnection() as HttpURLConnection

        connection.apply {
            requestMethod = "POST"
            doOutput = true
            setRequestProperty("Content-Type", "multipart/form-data; boundary=$boundary")
        }

        val parcelFileDescriptor = context.contentResolver.openFileDescriptor(uri, "r")
        if (parcelFileDescriptor == null) {
            return@withContext "Failed to open file descriptor for URI: $uri"
        }

        val fileInputStream = FileInputStream(parcelFileDescriptor.fileDescriptor)
        val outputStream: OutputStream = connection.outputStream
        val writer = BufferedWriter(OutputStreamWriter(outputStream))

        // 发送文件数据和其他字段
        try {
            writer.append("--$boundary\r\n")
            writer.append("Content-Disposition: form-data; name=\"file\"; filename=\"${File(uri.path).name}\"\r\n")
            writer.append("Content-Type: application/octet-stream\r\n") // 可以根据文件类型修改
            writer.append("\r\n")
            writer.flush()


            // 写文件内容
            val buffer = ByteArray(4096)
            var bytesRead: Int
            while (fileInputStream.read(buffer).also { bytesRead = it } != -1) {
                outputStream.write(buffer, 0, bytesRead)
            }

            outputStream.flush()

            // 添加结尾边界
            writer.append("\r\n")
            writer.append("--$boundary--\r\n")
            writer.flush()

            fileInputStream.close()

            // 获取服务器响应
            val responseCode = connection.responseCode
            return@withContext if (responseCode == HttpURLConnection.HTTP_OK) {
                // 服务器返回的文件 URL 假设在响应体中，你可以根据实际情况解析
                val responseMessage = connection.inputStream.bufferedReader().readText()
                Log.d("UploadFile", "Response: $responseMessage")
                return@withContext uri.toString()
            } else {
                "Error: $responseCode"
            }
        } catch (e: Exception) {
            return@withContext "Error during file upload: ${e.message}"
        } finally {
            connection.disconnect()
            parcelFileDescriptor?.close()
        }
    }
}




// 获取消息的网络请求
private suspend fun fetchMessages(chatroomId: Int): List<Quadruple<String, String, String, Boolean,Uri?>> {
    val url = "http://10.0.2.2:55722/get_messages/?chatroom_id=$chatroomId" // 使用本地地址
    val client = OkHttpClient()
    val request = Request.Builder().url(url).build()

    return withContext(Dispatchers.IO) {
        client.newCall(request).execute().use { response ->
            if (response.isSuccessful) {
                val json = response.body?.string() ?: "{}"  // 如果 json 为 null，则使用空 JSON
                val jsonObject = JSONObject(json)
                val data = jsonObject.getJSONObject("data").getJSONArray("messages")

                (0 until data.length()).map {
                    val obj = data.getJSONObject(it)
                    val fileUriString=obj.getString("file_url")
                    Quadruple(
                        obj.getString("message"),
                        obj.getString("message_time"),
                        obj.getString("name"),
                        false,
                        Uri.parse("file://$fileUriString") // 添加文件 URI
                    ) // 假设这些消息是从其他用户发送的
                }
            } else {
                emptyList()
            }
        }
    }
}

data class Quadruple<A, B, C, D,E>(
    val first: A,
    val second: B,
    val third: C,
    val fourth: D,
    val fifth: E?
)
// 发送消息到服务器
private suspend fun sendMessageWithFile(context: Context,
                                        chatroomId: Int,
                                        message: String,
                                        fileUri: Uri?,
                                        messageTime: String) {

    Log.d("sendMessageWithFile", fileUri.toString())
    // 异步上传文件
    val fileUrl = fileUri?.let {
        uploadFileUsingHttpURLConnection(context, it)
    }
    Log.d("sendMessageWithFile", fileUrl.toString())

    val url = "http://10.0.2.2:55722/send_message/"
    val client = OkHttpClient()
    val json = JSONObject().apply {
        put("chatroom_id", chatroomId)
        put("user_id", "1155219390") // ID
        put("name",displayName .take(20)) // 显示名称
        put("message", message.take(200))
        put("message_time",messageTime)
        put("file_url", fileUri.toString() ?: JSONObject.NULL) // 如果有文件，则添加文件 URL，否则为 null

    }
    val request = Request.Builder()
        .url(url)
        .post(
            json.toString()
                .toRequestBody("application/json; charset=utf-8".toMediaType())
        )
        .build()

    withContext(Dispatchers.IO) {
        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                val errorJson = JSONObject(response.body?.string() ?: "{}")
                Log.e("ChatActivity", "Error: ${errorJson.optString("message")}")
            }
        }
    }
}

// 发送消息到gpt
private suspend fun sendMessageTogpt(context: Context,
                                     chatroomId: Int,
                                     message: String,
                                     fileUri: Uri?,
                                     messageTime: String) {
    val url = "http://10.0.2.2:55722/send_message_gpt/"
    val client = OkHttpClient()
    val json = JSONObject().apply {
        put("chatroom_id", chatroomId)
        put("user_id", "1155219390") // ID
        put("name",displayName .take(20)) // 显示名称
        put("message", message.take(200))
        put("message_time",messageTime)
        put("file_url", fileUri.toString() ?: JSONObject.NULL) // 如果有文件，则添加文件 URL，否则为 null

    }
    val request = Request.Builder()
        .url(url)
        .post(
            json.toString()
                .toRequestBody("application/json; charset=utf-8".toMediaType())
        )
        .build()

    withContext(Dispatchers.IO) {
        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                val errorJson = JSONObject(response.body?.string() ?: "{}")
                Log.e("ChatActivity", "Error: ${errorJson.optString("message")}")
            }
        }
    }
}

//时间函数
fun getCurrentTime(): String {
    val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
    return sdf.format(Date())
}

