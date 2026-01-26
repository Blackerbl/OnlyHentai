// ! Bu araç @Kraptor123 tarafından | @Cs-GizliKeyif için yazılmıştır.
package com.kraptor

import com.lagradost.cloudstream3.plugins.CloudstreamPlugin
import com.lagradost.cloudstream3.plugins.BasePlugin

@CloudstreamPlugin
class HentaizmPlugin: BasePlugin() {
    override fun load() {
        android.util.Log.d("Hentaizm", "Plugin loading - v16")
        val api = Hentaizm()
        registerMainAPI(api)
        registerExtractorAPI(api.videoHu)
        registerExtractorAPI(CloudMailRu())
        android.util.Log.d("Hentaizm", "Plugin loaded successfully")
    }
}