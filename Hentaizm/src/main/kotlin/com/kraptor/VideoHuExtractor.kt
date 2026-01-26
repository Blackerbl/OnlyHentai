package com.kraptor

import com.lagradost.api.Log
import com.lagradost.cloudstream3.*
import com.lagradost.cloudstream3.utils.*
import org.w3c.dom.*
import javax.xml.parsers.DocumentBuilderFactory
import android.util.Base64
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

open class VideoHu : ExtractorApi() {
    override val name = "Videa"
    override val mainUrl = "https://videa.hu"
    override val requiresReferer = false

    companion object {
        private const val STATIC_SECRET = "xHb0ZvME5q8CBcoQi6AngerDu3FGO9fkUlwPmLVY_RTzj2hJIS4NasXWKy1td7p"

        // RC4 decryption
        fun rc4(cipher: ByteArray, key: String): String {
            val S = ByteArray(256) { it.toByte() }
            val K = key.toByteArray(Charsets.UTF_8)
            var j = 0
            for (i in 0 until 256) {
                j = (j + S[i] + K[i % K.size]) and 0xFF
                S[i] = S[j].also { S[j] = S[i] }
            }
            val result = ByteArray(cipher.size)
            var i = 0
            j = 0
            for (n in cipher.indices) {
                i = (i + 1) and 0xFF
                j = (j + S[i]) and 0xFF
                S[i] = S[j].also { S[j] = S[i] }
                val Kbyte = S[(S[i] + S[j]) and 0xFF]
                result[n] = (cipher[n].toInt() xor Kbyte.toInt()).toByte()
            }
            return result.toString(Charsets.UTF_8)
        }
    }

    override suspend fun getUrl(
        url: String,
        referer: String?,
        subtitleCallback: (SubtitleFile) -> Unit,
        callback: (ExtractorLink) -> Unit
    ) = withContext(Dispatchers.IO) {
        try {
            Log.d("kraptor_$name", "Fetching URL: $url")
            // Download initial page or player
            val pageContent = app.get(url, referer = referer).text

            // Determine player URL
            val playerUrl = if (url.contains("videa.hu/player")) {
                url
            } else {
                Regex("<iframe.*?src=\\\"(/player\\?[^\\\"]+)\\\"")
                    .find(pageContent)?.groupValues?.get(1)
                    ?.let { url.substringBefore("/videa.hu") + it } ?: url
            }

            // Download player page
            val playerResp = app.get(playerUrl, referer = url)
            val playerHtml = playerResp.text

            // Extract nonce
            val nonceMatch = Regex("_xt\\s*=\\s*\\\"([^\\\"]+)\\\"").find(playerHtml)
            if (nonceMatch == null) {
                Log.e("kraptor_$name", "Nonce not found in player HTML")
                return@withContext
            }
            val nonce = nonceMatch.groupValues[1]
            val l = nonce.substring(0, 32)
            val s = nonce.substring(32)
            var result = ""
            for (i in 0 until 32) {
                val idx = STATIC_SECRET.indexOf(l[i])
                result += s[i - (idx - 31)]
            }

            // Build query parameters
            val randomSeed = (1..8).map { ('A'..'Z') + ('a'..'z') + ('0'..'9') }
                .joinToString("") { it[(0 until 62).random()].toString() }
            val tParam = result.substring(0, 16)
            val query = mapOf(
                "v" to url.substringAfterLast("v="),
                "_s" to randomSeed,
                "_t" to tParam
            )

            // Request XML info
            val xmlResp = app.get("https://videa.hu/player/xml", referer = playerUrl, params = query)
            val xmlBody = xmlResp.text
            Log.d("kraptor_$name", "XML Response status: ${xmlResp.code}, body length: ${xmlBody.length}")

            val xmlString = if (xmlBody.trimStart().startsWith("<?xml")) {
                Log.d("kraptor_$name", "XML is plain text")
                xmlBody
            } else {
                // Encrypted: base64 -> rc4
                try {
                    Log.d("kraptor_$name", "XML is encrypted, decrypting...")
                    val b64 = Base64.decode(xmlBody, Base64.DEFAULT)
                    val key = result.substring(16) + randomSeed + (xmlResp.headers["x-videa-xs"] ?: "")
                    rc4(b64, key)
                } catch (e: Exception) {
                    Log.e("kraptor_$name", "Decryption failed: ${e.message}")
                    ""
                }
            }

            if (xmlString.isBlank()) {
                Log.e("kraptor_$name", "XML string is blank after processing")
                return@withContext
            }
            
            if (!xmlString.contains("video")) {
                Log.e("kraptor_$name", "Invalid XML response (length=${xmlString.length}): ${xmlString.take(500)}")
                return@withContext
            }

            // Parse XML
            val db = DocumentBuilderFactory.newInstance().newDocumentBuilder()
            val doc = db.parse(xmlString.byteInputStream())
            val videoTags = doc.getElementsByTagName("video")
            if (videoTags.length == 0) {
                Log.e("kraptor_$name", "No video tag found in XML. Content: ${xmlString.take(500)}")
                return@withContext
            }
            val video = videoTags.item(0) as Element
            val title = video.getElementsByTagName("title").item(0)?.textContent ?: "Videa Video"

            val sources = doc.getElementsByTagName("video_source")
            val hashValues = doc.getElementsByTagName("hash_values").item(0) as? Element

            for (i in 0 until sources.length) {
                val src = sources.item(i) as Element
                var videoUrl = src.textContent
                val srcName = src.getAttribute("name")
                val exp = src.getAttribute("exp")
                if (hashValues != null && hashValues.getElementsByTagName("hash_value_$srcName").length > 0) {
                    val hash = hashValues.getElementsByTagName("hash_value_$srcName").item(0)?.textContent
                    videoUrl = updateUrl(videoUrl, mapOf("md5" to hash, "expires" to exp))
                }
                callback(
                    newExtractorLink(
                        source = srcName,
                        name = title,
                        url = fixUrl(videoUrl)
                    ) {
                        this.referer = url
                        this.quality = Qualities.Unknown.value
                        this.type = ExtractorLinkType.VIDEO
                    }
                )
            }
        } catch (e: Exception) {
            Log.e("kraptor_$name", "General error in getUrl: ${e.message}")
            e.printStackTrace()
        }
    }

    // Helpers
    private fun randomSeedIndex(): Int = (0 until 62).random()
    private fun updateUrl(url: String, params: Map<String, String?>): String =
        url + params.entries.joinToString("&", prefix = if (url.contains("?")) "&" else "?") { "${it.key}=${it.value}" }
    private fun fixUrl(url: String): String = if (url.startsWith("//")) "https:$url" else url
}
