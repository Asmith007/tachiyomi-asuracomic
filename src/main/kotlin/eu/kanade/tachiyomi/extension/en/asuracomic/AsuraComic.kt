package eu.kanade.tachiyomi.extension.en.asuracomic

import eu.kanade.tachiyomi.source.online.ParsedHttpSource
import eu.kanade.tachiyomi.network.GET
import eu.kanade.tachiyomi.source.model.*
import okhttp3.Request
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element

class AsuraComic : ParsedHttpSource() {

    override val name = "AsuraComic"
    override val baseUrl = "https://asuracomic.net"
    override val lang = "en"
    override val supportsLatest = true
    private val headers = mapOf("User-Agent" to "Mozilla/5.0")

    // Popular Manga
    override fun popularMangaRequest(page: Int): Request = GET("$baseUrl/series?page=$page", headers)
    override fun popularMangaSelector() = "div.series-card"
    override fun popularMangaFromElement(element: Element): SManga {
        return SManga.create().apply {
            title = element.selectFirst("div.text h3.title a")!!.text()
            url = element.selectFirst("div.text h3.title a")!!.attr("href")
            thumbnail_url = element.selectFirst("div.img img")!!.attr("abs:src")
        }
    }
    override fun popularMangaNextPageSelector() = "a.next"

    // Latest Updates
    override fun latestUpdatesRequest(page: Int): Request = popularMangaRequest(page)
    override fun latestUpdatesSelector() = popularMangaSelector()
    override fun latestUpdatesFromElement(element: Element) = popularMangaFromElement(element)
    override fun latestUpdatesNextPageSelector() = popularMangaNextPageSelector()

    // Search
    override fun searchMangaRequest(page: Int, query: String, filters: FilterList): Request {
        return GET("$baseUrl/search?query=$query&page=$page", headers)
    }
    override fun searchMangaSelector() = popularMangaSelector()
    override fun searchMangaFromElement(element: Element) = popularMangaFromElement(element)
    override fun searchMangaNextPageSelector() = popularMangaNextPageSelector()

    // Manga Details
    override fun mangaDetailsParse(document: Document): SManga {
        return SManga.create().apply {
            title = document.selectFirst("h1.series-title")?.text() ?: ""
            author = document.select("span.author a").joinToString { it.text() }
            genre = document.select("div.genres a").joinToString { it.text() }
            description = document.selectFirst("div.description")?.text() ?: ""
            thumbnail_url = document.selectFirst("div.series-cover img")?.attr("abs:src")
        }
    }

    // Chapters
    override fun chapterListSelector() = "ul.chapter-list li"
    override fun chapterFromElement(element: Element): SChapter {
        return SChapter.create().apply {
            name = element.selectFirst("a")!!.text()
            url = element.selectFirst("a")!!.attr("href")
        }
    }

    // Pages
    override fun pageListParse(document: Document): List<Page> {
        return document.select("div.reader-area img").mapIndexed { i, img ->
            val imgUrl = img.attr("data-src").ifEmpty { img.attr("src") }
            Page(i, "", imgUrl)
        }
    }

    override fun imageUrlParse(document: Document) = ""
}
