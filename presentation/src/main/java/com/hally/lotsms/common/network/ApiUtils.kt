package com.hally.lotsms.common.network

import com.hally.lotsms.common.network.model.XsmbRss
import retrofit2.Callback


/**
 * Created by HallyTran on 4/6/2019.
 * transon97uet@gmail.com
 */
class ApiUtils {
    companion object {
        private val BASE_URL = "https://api.rss2json.com/"
        public val XSMB_URL = "https://xskt.com.vn/rss-feed/mien-bac-xsmb.rss"
        private val mApiService by lazy {
            ApiService.create(BASE_URL)
        }

        fun getXsmb(callback: Callback<XsmbRss>) {
            mApiService.getRss(XSMB_URL).enqueue(callback)
        }
    }

}