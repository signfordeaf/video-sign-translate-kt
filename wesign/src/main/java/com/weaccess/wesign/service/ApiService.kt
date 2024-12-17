package com.weaccess.wesign.service

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.google.gson.Gson
import com.weaccess.wesign.WeAccessConfig
import com.weaccess.wesign.model.VideoSignModel
import okhttp3.Call
import okhttp3.Callback
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.Response
import org.json.JSONObject
import java.io.File
import java.io.IOException

class ApiService {
    private val client = OkHttpClient()
    private val retryDelayMillis = 1000L
    private var currentCall: Call? = null
    private val gson = Gson()

    fun videoUpload(file: File, videoBundleId: String, context: Context) {
        val apiUrl = "https://pl.weaccess.com/wesign-upload/"
        val fileBody = file.asRequestBody("video/mp4".toMediaTypeOrNull())
        val requestBody = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart("api_key", WeAccessConfig.requestKey?:"")
            .addFormDataPart("file", file.name, fileBody)
            .addFormDataPart("videoBundleId", videoBundleId)
            .build()
        val request = Request.Builder()
            .url(apiUrl)
            .post(requestBody)
            .build()
        currentCall = client.newCall(request)
        currentCall?.enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                return
            }
            override fun onResponse(call: Call, response: Response) {
                response.use {
                    if (!it.isSuccessful) {
                        Log.e("DEVOPS-WA", "Unexpected code $response")
                        return
                    }

                    val responseBody = response.body?.string()
                    responseBody?.let { body ->
                        val jsonObject = JSONObject(body)
                        val fileUrl = jsonObject.optString("file_url")
                        val filePath = jsonObject.optString("file_path")
                        createSignVideo(videoBundleId, filePath, context)
                    }
                }
            }
        })
    }

    fun createSignVideo(videoBundleId: String, videoPath: String, context: Context) {
        val apiUrl = "https://pl.weaccess.com/wesign-create/"
        val requestBody = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart("api_key", WeAccessConfig.requestKey?:"")
            .addFormDataPart("videoBundleId", videoBundleId)
            .addFormDataPart("videoPath", videoPath)
            .build()
        val request = Request.Builder()
            .url(apiUrl)
            .post(requestBody)
            .build()
        currentCall = client.newCall(request)
        currentCall?.enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                return
            }
            override fun onResponse(call: Call, response: Response) {
                response.use {
                    if (!it.isSuccessful) {
                        Log.e("DEVOPS-WA", "Unexpected code $response")
                        return
                    }
                    val responseBody = response.body?.string()
                    val responseStatusCode = response.code
                    if (responseStatusCode == 201) {
                        Log.d("DEVOPS-WA", "Sign video created successfully")
                    }
                    responseBody?.let { _ ->
                        Thread {
                            getWeSignData(videoBundleId, videoPath, context)
                        }.start()
                    }
                }
            }
        })
    }

    fun getWeSignData(videoBundleId: String, videoPath: String, context: Context) {
        val apiUrl = "https://pl.weaccess.com/wesign-create/"
        val urlWithParams = apiUrl.toHttpUrlOrNull()?.newBuilder()?.apply {
            addQueryParameter("video_bundle_id",videoBundleId)
            addQueryParameter("api_key", WeAccessConfig.requestKey?:"")
            addQueryParameter("video_path", videoPath)
        }?.build().toString()
        val request = Request.Builder()
            .url(urlWithParams)
            .get()
            .build()
        currentCall = client.newCall(request)
        currentCall?.enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                return
            }
            override fun onResponse(call: Call, response: Response) {
                response.use {
                    if (!it.isSuccessful) {
                        Log.e("DEVOPS-WA", "Unexpected code $response")
                        return
                    }
                    val responseBody = response.body?.string()
                    val responseStatusCode = response.code
                    if (responseStatusCode == 201) {
                        Log.d("DEVOPS-WA", "Sign video created successfully")
                    }
                    val videoSignModel: VideoSignModel? = responseBody.let { json ->
                        gson.fromJson(json, VideoSignModel::class.java)
                    }
                    videoSignModel?.let { model ->
                        if (model.status == true) {
                            saveModelToDevice(context,model, videoBundleId)
                        } else if (model.status == null) {
                            Log.e("DEVOPS-WA", "Sign video creation failed")
                        } else {
                            getWeSignData(videoBundleId, videoPath, context)
                        }
                    }
                }
            }
        })
    }

    private fun saveModelToDevice(context: Context, model: VideoSignModel, videoBundleId: String) {
        val sharedPreferences: SharedPreferences = context.getSharedPreferences("VideoSignModelPrefs", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        val gson = Gson()
        val jsonModel = gson.toJson(model)
        editor.putString(videoBundleId, jsonModel)
        editor.apply()
        Log.d("DEVOPS-WA", "Model kaydedildi: $model")
    }

    fun cancelRequest() {
        currentCall?.cancel()
        currentCall = null
    }
}