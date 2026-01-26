// ! Bu araç @Kraptor123 tarafından | @Cs-GizliKeyif için yazılmıştır.
package com.kraptor

import android.annotation.SuppressLint
import com.lagradost.api.Log
import org.jsoup.nodes.Element
import com.lagradost.cloudstream3.*
import com.lagradost.cloudstream3.utils.*
import com.lagradost.cloudstream3.extractors.*
import com.lagradost.cloudstream3.network.CloudflareKiller
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import okhttp3.Interceptor
import okhttp3.Response
import org.jsoup.Jsoup
import java.io.IOException

private val cloudflareKiller by lazy { CloudflareKiller() }
private val interceptor      by lazy { CloudflareInterceptor(cloudflareKiller) }

class CloudflareInterceptor(private val cloudflareKiller: CloudflareKiller): Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request  = chain.request()
        val response = chain.proceed(request)
        val doc      = Jsoup.parse(response.peekBody(1024 * 1024).string())

        if (doc.html().contains("Just a moment") || response.code == 403) {
            response.close()
            return cloudflareKiller.intercept(chain)
        }

        return response
    }
}

class Hentaizm : MainAPI() {
    override var mainUrl = "https://www.hentaizm6.online"
    override var name = "Hentaizm"
    override val hasMainPage = true
    override var lang = "tr"
    override val hasQuickSearch = false
    override val supportedTypes = setOf(TvType.NSFW)
    val videoHu = VideoHu()

    override fun getVideoInterceptor(extractorLink: ExtractorLink): Interceptor {
        return object : Interceptor {
            override fun intercept(chain: Interceptor.Chain): Response {
                val request = chain.request()
                val cookies = SessionManager.getCachedCookies()
                
                val builder = request.newBuilder()
                    .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36")
                    .header("Referer", mainUrl + "/")

                if (cookies.isNotEmpty()) {
                    val cookieHeader = cookies.entries.joinToString("; ") { "${it.key}=${it.value}" }
                    builder.header("Cookie", cookieHeader)
                }

                return chain.proceed(builder.build())
            }
        }
    }

    override val mainPage = mainPageOf(
        "Live Action" to "Live Action",
        "JAV" to "JAV",
        "Sansürsüz" to "Sansürsüz",
        "Sansürlü" to "Sansürlü",
        "Ahegao" to "Ahegao",
        "Aldatma" to "Aldatma",
        "Anal" to "Anal",
        "Ayak Fetişi" to "Ayak Fetişi",
        "Asansör" to "Asansör",
        "Bakire" to "Bakire",
        "BDSM" to "BDSM",
        "Bondage" to "Bondage",
        "Büyük Memeler" to "Büyük Memeler",
        "Canavar" to "Canavar",
        "Cosplay" to "Cosplay",
        "Creampie" to "Creampie",
        "Dark Skin" to "Dark Skin",
        "Ecchi" to "Ecchi",
        "Elf" to "Elf",
        "Ensest" to "Ensest",
        "Ev Hanımı" to "Ev Hanımı",
        "Fantastik" to "Fantastik",
        "Fetiş" to "Fetiş",
        "Futanari" to "Futanari",
        "Gang Bang" to "Gang Bang",
        "Genç" to "Genç",
        "Gerilim" to "Gerilim",
        "Hamile" to "Hamile",
        "Harem" to "Harem",
        "Hastane" to "Hastane",
        "Hemşire" to "Hemşire",
        "Hentai" to "Hentai",
        "Hizmetçi" to "Hizmetçi",
        "İsekai" to "İsekai",
        "Jartiyer" to "Jartiyer",
        "Korku" to "Korku",
        "Köle" to "Köle",
        "Küçük Memeler" to "Küçük Memeler",
        "Loli" to "Loli",
        "Mastürbasyon" to "Mastürbasyon",
        "Manipülasyon" to "Manipülasyon",
        "Milf" to "Milf",
        "Netorare" to "Netorare",
        "Psikolojik" to "Psikolojik",
        "Ofis" to "Ofis",
        "Okul" to "Okul",
        "Oral" to "Oral",
        "Oyun" to "Oyun",
        "Öğrenci" to "Öğrenci",
        "Öğretmen" to "Öğretmen",
        "Romantizm" to "Romantizm",
        "Sekreter" to "Sekreter",
        "Shota" to "Shota",
        "Succubus" to "Succubus",
        "Süper Güç" to "Süper Güç",
        "Şantaj" to "Şantaj",
        "Şeytanlar" to "Şeytanlar",
        "Tecavüz" to "Tecavüz",
        "Tentacle" to "Tentacle",
        "Tren" to "Tren",
        "Vahşet" to "Vahşet",
        "Vampir" to "Vampir",
        "Yaoi" to "Yaoi",
        "Yuri" to "Yuri",
        "Yüze Oturma" to "Yüze Oturma"
    )

    object SessionManager {
        private var cachedCookies: Map<String, String>? = null
        private val loginMutex = Mutex()

        fun getCachedCookies(): Map<String, String> {
            return cachedCookies ?: emptyMap()
        }

        suspend fun login(): Map<String, String> {
            cachedCookies?.let {
                return it
            }
            return loginMutex.withLock {
                cachedCookies?.let {
                    return@withLock it
                }

                val fresh = app.post(
                    "https://www.hentaizm6.online/giris",
                    data = mapOf(
                        "user" to "igtbyprzkxtigpoqbj@enotj.com",
                        "pass" to "AU#@d4524\$>yv#V",
                        "redirect_to" to "https://www.hentaizm6.online"
                    )
                ).cookies

                cachedCookies = fresh
                fresh
            }
        }
    }


    override suspend fun getMainPage(page: Int, request: MainPageRequest): HomePageResponse {
        val cookies = SessionManager.login()
        val document = app.get(
            "${mainUrl}/anime-ara?t=tur&q=$page&tur=${request.data}",
            referer = "${mainUrl}/kategoriler-2",
            cookies = cookies
        ).document
        val home = document.select("div.moviefilm").mapNotNull { it.toMainPageResult() }

        return newHomePageResponse(request.name, home)
    }

    private fun Element.toMainPageResult(): SearchResponse? {
        val title = this.selectFirst("div.movief")?.text() ?: return null
        val href = fixUrlNull(this.selectFirst("a")?.attr("href")?.replace("../..", "")) ?: return null
        val posterUrl = fixUrlNull(this.selectFirst("img")?.attr("src"))

        return newMovieSearchResponse(title, href, TvType.NSFW) { this.posterUrl = posterUrl }
    }

    override suspend fun search(query: String, page: Int): SearchResponseList {
        val cookies = SessionManager.login()
        val document = app.get("${mainUrl}/page/$page?s=${query}", cookies = cookies).document

        val aramaCevap = document.select("div.moviefilm").mapNotNull { it.toSearchResult() }
        return newSearchResponseList(aramaCevap, hasNext = true)
    }

    private fun Element.toSearchResult(): SearchResponse? {
        val title = this.selectFirst("div.movief")?.text() ?: return null
        val href = fixUrlNull(this.selectFirst("a")?.attr("href")?.replace("../..", "")) ?: return null
        val posterUrl = fixUrlNull(this.selectFirst("img")?.attr("src"))

        return newMovieSearchResponse(title, href, TvType.NSFW) { this.posterUrl = posterUrl }
    }

    override suspend fun quickSearch(query: String): List<SearchResponse>? = search(query)

    override suspend fun load(url: String): LoadResponse? {
        val cookies = SessionManager.login()

        val document = app.get(
            url,
            cookies = cookies
        ).document

        val title = document.selectFirst("div.filmcontent h1")?.text()?.trim() ?: return null
        val poster = fixUrlNull(document.selectFirst("div.filmcontent img")?.attr("src"))
        val description =
            document.selectFirst("table.anime-detay > tbody:nth-child(1) > tr:nth-child(5) > td:nth-child(1)")?.text()
                ?.trim()?.replace("\\","")
        val year =
            document.selectFirst("dıv.anime-detay > tbody:nth-child(1) > tr:nth-child(3) > td:nth-child(1) > b:nth-child(1)")
                ?.text()?.trim()?.toIntOrNull()
        val tags = document.select("div.filmborder td a").map { it.text() }
        val episodes = document.select("div.overview li").map {
            val linkElement = it.selectFirst("a")
            val name = linkElement?.text() ?: ""
            val url = linkElement?.absUrl("href") ?: ""
            newEpisode(url, {
                this.name = name.substringAfter(".")
                val match = Regex("(\\d+)(?=\\.)").find(name)
                this.episode = match?.value?.toInt() ?: 1
            }
            )
        }

        return newAnimeLoadResponse(title, url, TvType.NSFW, true) {
            this.posterUrl = poster
            this.plot = description
            this.year = year
            this.tags = tags
            this.episodes = mutableMapOf(
                DubStatus.Subbed to episodes
            )
        }
    }

    @SuppressLint("SuspiciousIndentation")
    override suspend fun loadLinks(
        data: String,
        isCasting: Boolean,
        subtitleCallback: (SubtitleFile) -> Unit,
        callback: (ExtractorLink) -> Unit
    ): Boolean {
        val cookies = SessionManager.login()
        val document = app.get(
            data,
            cookies = cookies
        ).document

        val anchors = document.select("div.filmicerik a")

        anchors.forEachIndexed { index, element ->
            try {
                val url = if (index == 0) {
                    fixUrlNull(element.attr("href").substringAfter("url="))
                } else {
                    val onclick = element.attr("onclick")
                    val rawUrl = onclick.substringAfter("ajxget('").substringBefore("'").replace("../../","")
                    val fixRaw = fixUrlNull(rawUrl).toString()
                    val rawGet = app.get(fixRaw, referer = data, cookies = cookies).document
                    val vidUrl = rawGet.selectFirst("a")?.attr("href")?.replace("../../","")
                    if (vidUrl?.contains("ay.live") == true) {
                        vidUrl.substringAfter("url=")
                    } else {
                        fixUrlNull(rawGet.selectFirst("iframe")?.attr("src"))
                    }
                }

                url?.let { iframe ->
                    val finalUrl = if (iframe.contains("short.icu") || iframe.contains("ay.live")) {
                        val response = app.get(iframe, allowRedirects = true, interceptor = interceptor)
                        response.url
                    } else {
                        iframe
                    }
                    loadExtractor(finalUrl, "$mainUrl/", subtitleCallback, callback)
                }
            } catch (e: Exception) {
                Log.e("Hentaizm", "Error loading link at index $index: ${e.message}")
            }
        }
        return true
    }
    }

    inner class VideoHu : ExtractorApi() {
        override val name = "Videa"
        override val mainUrl = "https://videa.hu"
        override val requiresReferer = false

        // RC4 decryption helpers
        private fun rc4(cipher: ByteArray, key: String): String {
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

        override suspend fun getUrl(
            url: String,
            referer: String?,
            subtitleCallback: (SubtitleFile) -> Unit,
            callback: (ExtractorLink) -> Unit
        ) {
            try {
                Log.d("kraptor_Videa", "Fetching URL: $url")
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
                    Log.e("kraptor_Videa", "Nonce not found in player HTML")
                    return
                }
                val nonce = nonceMatch.groupValues[1]
                val l = nonce.substring(0, 32)
                val s = nonce.substring(32)
                var result = ""
                val STATIC_SECRET = "xHb0ZvME5q8CBcoQi6AngerDu3FGO9fkUlwPmLVY_RTzj2hJIS4NasXWKy1td7p"
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
                Log.d("kraptor_Videa", "XML Response status: ${xmlResp.code}, body length: ${xmlBody.length}")

                val xmlString = if (xmlBody.trimStart().startsWith("<?xml")) {
                    Log.d("kraptor_Videa", "XML is plain text")
                    xmlBody
                } else {
                    // Encrypted: base64 -> rc4
                    try {
                        Log.d("kraptor_Videa", "XML is encrypted, decrypting...")
                        val b64 = android.util.Base64.decode(xmlBody, android.util.Base64.DEFAULT)
                        val key = result.substring(16) + randomSeed + (xmlResp.headers["x-videa-xs"] ?: "")
                        rc4(b64, key)
                    } catch (e: Exception) {
                        Log.e("kraptor_Videa", "Decryption failed: ${e.message}")
                        ""
                    }
                }

                if (xmlString.isBlank()) {
                    Log.e("kraptor_Videa", "XML string is blank after processing")
                    return
                }
                
                // Log XML for debugging
                Log.d("kraptor_Videa", "Received XML (${xmlString.length} chars): ${xmlString.take(500)}")

                // Parse XML
                val db = javax.xml.parsers.DocumentBuilderFactory.newInstance().newDocumentBuilder()
                val doc = db.parse(xmlString.byteInputStream())
                val videoTags = doc.getElementsByTagName("video")
                if (videoTags.length == 0) {
                     // Check for error tag with redirect URL
                    val errorTags = doc.getElementsByTagName("error")
                    if (errorTags.length > 0) {
                        val errorUrl = errorTags.item(0).textContent
                        Log.d("kraptor_Videa", "Error tag found with URL: $errorUrl, trying web scraping fallback")
                        
                        if (errorUrl.isNotEmpty() && errorUrl.startsWith("http")) {
                            try {
                                val pageHtml = app.get(errorUrl).text
                                // Try to find video URL in page source
                                val mp4Match = Regex("""\"file\"\s*:\s*\"(https?:.*?.mp4)\"""").find(pageHtml)
                                if (mp4Match != null) {
                                    val videoUrl = mp4Match.groupValues[1].replace("\\/", "/")
                                    Log.d("kraptor_Videa", "Found fallback video URL: $videoUrl")
                                    callback.invoke(
                                        ExtractorLink(
                                            name,
                                            name,
                                            videoUrl,
                                            referer = errorUrl,
                                            quality = Qualities.Unknown.value,
                                            type = INFER_TYPE
                                        )
                                    )
                                    return
                                } else {
                                    Log.e("kraptor_Videa", "No video URL found in fallback page")
                                }
                            } catch (e: Exception) {
                                Log.e("kraptor_Videa", "Error in fallback scraping: ${e.message}")
                            }
                        }
                    }
                    Log.e("kraptor_Videa", "No video tag found in XML")
                    return
                }
                val video = videoTags.item(0) as org.w3c.dom.Element
                val title = video.getElementsByTagName("title").item(0)?.textContent ?: "Videa Video"

                val sources = doc.getElementsByTagName("video_source")
                val hashValues = doc.getElementsByTagName("hash_values").item(0) as? org.w3c.dom.Element

                for (i in 0 until sources.length) {
                    val src = sources.item(i) as org.w3c.dom.Element
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
                            url = if (videoUrl.startsWith("//")) "https:$videoUrl" else videoUrl
                        ) {
                            this.referer = url
                            this.quality = Qualities.Unknown.value
                            this.type = ExtractorLinkType.VIDEO
                        }
                    )
                }
            } catch (e: Exception) {
                Log.e("kraptor_Videa", "General error in getUrl: ${e.message}")
                e.printStackTrace()
            }
        }
        
        private fun updateUrl(url: String, params: Map<String, String?>): String =
            url + params.entries.joinToString("&", prefix = if (url.contains("?")) "&" else "?") { "${it.key}=${it.value}" }
    }
}
