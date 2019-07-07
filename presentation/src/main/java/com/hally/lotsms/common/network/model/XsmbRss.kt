package com.hally.lotsms.common.network.model

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

/**
 * Created by HallyTran on 4/6/2019.
 * transon97uet@gmail.com
 */
class XsmbRss {
    @SerializedName("status")
    @Expose
    val status: String? = null
    @SerializedName("feed")
    @Expose
    val feed: Feed? = null
    @SerializedName("items")
    @Expose
    val items: List<Item>? = null

    override fun toString(): String {
        return "XsmbRss{" +
                "status='" + status + '\''.toString() +
                ", feed=" + feed +
                ", items=" + items +
                '}'.toString()
    }

    inner class Feed {
        @SerializedName("url")
        @Expose
        val url: String? = null
        @SerializedName("title")
        @Expose
        val title: String? = null
        @SerializedName("link")
        @Expose
        val link: String? = null
        @SerializedName("author")
        @Expose
        val author: String? = null
        @SerializedName("description")
        @Expose
        val description: String? = null
        @SerializedName("image")
        @Expose
        val image: String? = null

        override fun toString(): String {
            return "Feed{" +
                    "url='" + url + '\''.toString() +
                    ", title='" + title + '\''.toString() +
                    ", link='" + link + '\''.toString() +
                    ", author='" + author + '\''.toString() +
                    ", description='" + description + '\''.toString() +
                    ", image='" + image + '\''.toString() +
                    '}'.toString()
        }
    }

    inner class Item {
        @SerializedName("title")
        @Expose
        val title: String? = null
        @SerializedName("pubDate")
        @Expose
        val pubDate: String? = null
        @SerializedName("link")
        @Expose
        val link: String? = null
        @SerializedName("guid")
        @Expose
        val guid: String? = null
        @SerializedName("author")
        @Expose
        val author: String? = null
        @SerializedName("thumbnail")
        @Expose
        val thumbnail: String? = null
        @SerializedName("description")
        @Expose
        val description: String? = null
        @SerializedName("content")
        @Expose
        val content: String? = null
        @SerializedName("categories")
        @Expose
        val categories: List<Any>? = null

        override fun toString(): String {
            return "Item{" +
                    "title='" + title + '\''.toString() +
                    ", pubDate='" + pubDate + '\''.toString() +
                    ", link='" + link + '\''.toString() +
                    ", guid='" + guid + '\''.toString() +
                    ", author='" + author + '\''.toString() +
                    ", thumbnail='" + thumbnail + '\''.toString() +
                    ", description='" + description + '\''.toString() +
                    ", content='" + content + '\''.toString() +
                    ", categories=" + categories +
                    '}'.toString()
        }
    }
}