package com.example.iems5722_project

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height

import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider

import androidx.compose.material3.Text

import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

import androidx.compose.ui.unit.dp
import com.example.iems5722_project.ui.theme.IEMS5722_projectTheme

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Response
import org.json.JSONObject
const val displayName = "Jiang zirui"
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            IEMS5722_projectTheme {
                val chatrooms = remember { mutableStateListOf<Pair<Int, String>>() }
                val coroutineScope = rememberCoroutineScope() // 创建协程作用域

                // 启动时获取聊天室列表
                LaunchedEffect(Unit) {
                    coroutineScope.launch { // 使用协程作用域
                        val fetchedChatrooms = fetchChatrooms() // 网络请求函数
                        chatrooms.addAll(fetchedChatrooms)
                    }
                }


                Column(
                    modifier = Modifier.fillMaxSize(),
//                    verticalArrangement = Arrangement.Center
                    horizontalAlignment = Alignment.CenterHorizontally // 水平居中
                ) {
                    Text(text = "IEMS 5722")
                    // 添加分割线
                    Spacer(modifier = Modifier.height(8.dp))
                    HorizontalDivider(
                        modifier = Modifier.fillMaxWidth(0.8f),
                        thickness = 1.dp,
                        color = androidx.compose.ui.graphics.Color.Gray
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    // 动态生成聊天室按钮
                    chatrooms.forEach { (id, name) ->
                        Button(onClick = {
                            val intent = Intent(this@MainActivity, ChatActivity::class.java)
                            intent.putExtra("chatroom_id", id)
                            startActivity(intent)
                        }) {
                            Text(text = name)
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                    }


                }
        }
    }
}

    // 获取聊天室的网络请求
    private suspend fun fetchChatrooms(): List<Pair<Int, String>> {
        val url = "http://10.0.2.2:55722/get_chatrooms/"
        val client = OkHttpClient()
        val request = okhttp3.Request.Builder().url(url).build()

        return withContext(Dispatchers.IO) {
            client.newCall(request).execute().use { response: Response ->
                if (response.isSuccessful) {
                    val json = response.body?.string() ?: "{}"  // 如果 json 为 null，则使用空 JSON
                    val jsonObject = JSONObject(json)
                    val data = jsonObject.getJSONArray("data")
                    (0 until data.length()).map {
                        val obj = data.getJSONObject(it)
                        obj.getInt("id") to obj.getString("name")
                    }
                } else {
                    emptyList()
                }
            }
        }
    }
}