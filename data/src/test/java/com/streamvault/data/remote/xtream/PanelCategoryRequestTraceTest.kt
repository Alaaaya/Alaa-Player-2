package com.streamvault.data.remote.xtream

import com.streamvault.data.remote.http.HttpRequestProfile
import com.streamvault.domain.model.PanelCategoryPolicy
import com.streamvault.domain.model.PanelCategoryPolicyRegistry as P
import com.streamvault.domain.model.Result as DomainResult
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.*
import org.junit.Test

class PanelCategoryRequestTraceTest {
    private val artwork = mutableMapOf<Pair<String,Long>,MutableSet<String>>()
    private val id=23452345L
    private val server=MockWebServer()
    private fun setup(hidden:Map<String,Set<String>>) : XtreamProvider {
        server.start()
        P.install(id,PanelCategoryPolicy("device","playlist","generation","revision",hidden))
        val rawKeys=mutableMapOf<Pair<String,String>,Long>()
        P.attach(id,object:P.Persistence {
            override fun membership(providerId:Long,media:String,contentId:Long,categories:Set<String>,allowed:Boolean) {}
            override fun artwork(providerId:Long,media:String,contentId:Long,urls:List<String?>,expectedEpoch:Long) {
                urls.filterNotNull().forEach { url ->
                    artwork.getOrPut(media to contentId){mutableSetOf()}.add(url)
                    check(com.streamvault.domain.model.PanelArtworkPolicyRegistry.record(providerId,url,"$media:$contentId",true,expectedEpoch))
                }
            }
            override fun categoryKey(providerId:Long,media:String,rawId:String):Long = rawKeys.getOrPut(media to rawId){rawKeys.size.toLong()+1}
            override fun rawCategory(providerId:Long,media:String,key:Long):String? = rawKeys.entries.firstOrNull{it.key.first==media && it.value==key}?.key?.second
        })
        val http=OkHttpXtreamApiService(OkHttpClient(),Json{ignoreUnknownKeys=true},HttpRequestProfile(ownerTag="provider:$id/xtream"))
        return XtreamProvider(id,http,server.url("/").toString(),"dummy","dummy",useTextClassification=false)
    }
    private fun allHidden()=mapOf("live" to setOf("007","7"),"vod" to setOf("007","7"),"series" to setOf("007","7"))
    @After fun close() { P.remove(id);server.shutdown() }
    private fun categories()=MockResponse().setHeader("Content-Type","application/json").setBody("""[{"category_id":"007","category_name":"Hidden"},{"category_id":7,"category_name":"Visible"}]""")
    @Test fun emptyVisibleSetProducesOnlyThreeCategoryRequests()=runBlocking {
        val provider=setup(allHidden())
        repeat(3){server.enqueue(categories())}
        assertTrue(provider.getLiveStreams(null) is DomainResult.Success)
        assertTrue(provider.getVodStreams(null) is DomainResult.Success)
        assertTrue(provider.getSeriesList(null) is DomainResult.Success)
        assertEquals(3,server.requestCount)
        repeat(3){assertTrue(server.takeRequest().requestUrl!!.queryParameter("action")!!.endsWith("categories"))}
    }
    @Test fun exactRawIdsAndProviderIgnoringFilterNeverExposeHiddenOrSharedItems()=runBlocking {
        val provider=setup(mapOf("live" to setOf("007"),"vod" to emptySet(),"series" to emptySet()))
        server.enqueue(categories())
        server.enqueue(MockResponse().setHeader("Content-Type","application/json").setBody("""[
            {"stream_id":1,"name":"Allowed","category_id":"7"},
            {"stream_id":2,"name":"Shared","category_id":"7"},
            {"stream_id":2,"name":"Shared hidden alternate","category_id":"007"},
            {"stream_id":3,"name":"Hidden","category_id":"007"}]
        """))
        val result=provider.getLiveStreams(null)
        assertTrue(result is DomainResult.Success)
        assertEquals(listOf(1L),(result as DomainResult.Success).data.map{it.streamId})
        repeat(server.requestCount){
            val request=server.takeRequest().requestUrl!!
            if(request.queryParameter("action")=="get_live_streams") assertEquals("7",request.queryParameter("category_id"))
        }
        assertFalse(P.allowsContent(id,"live",2));assertFalse(P.allowsContent(id,"live",3))
    }

    @Test fun unseenDetailAndNestedArtIsRegisteredBeforePublicationAndParentAlternateRevokes()=runBlocking {
        val provider=setup(mapOf("live" to emptySet(),"vod" to emptySet(),"series" to setOf("007")))
        val token=P.snapshot(id)
        P.recordMembership(token,"vod",50,setOf("7"))
        P.recordMembership(token,"series",60,setOf("7","8"))
        server.enqueue(MockResponse().setBody("""{"info":{"movie_image":"https://art.test/movie","backdrop_path":["https://art.test/backdrop"]},"movie_data":{"stream_id":50,"name":"Movie","category_id":"7"}}"""))
        server.enqueue(MockResponse().setBody("[]"))
        val movie=provider.getVodInfo(50)
        assertTrue(movie is DomainResult.Success)
        assertEquals(setOf("https://art.test/movie","https://art.test/backdrop"),artwork["vod" to 50L])
        server.enqueue(MockResponse().setBody("""{"info":{"series_id":60,"name":"Series","category_id":"7"},"seasons":[{"season_number":1,"cover":"https://art.test/season"}],"episodes":{"1":[{"id":"70","episode_num":1,"title":"Episode","info":{"movie_image":"https://art.test/episode"}}]}}"""))
        server.enqueue(MockResponse().setBody("[]"))
        val series=provider.getSeriesInfo(60)
        assertTrue(series is DomainResult.Success)
        assertTrue(artwork["series" to 60L]!!.contains("https://art.test/season"))
        assertEquals(setOf("https://art.test/episode"),artwork["episode" to 70L])
        assertTrue(P.allowsContent(id,"episode",70))
        assertFalse(P.recordMembership(token,"series",60,setOf("007")))
        assertFalse(P.allowsContent(id,"episode",70))
    }
    @Test fun staleProviderCannotPublishNewDetailArtwork()=runBlocking {
        val provider=setup(mapOf("live" to emptySet(),"vod" to emptySet(),"series" to emptySet()))
        P.recordMembership(P.snapshot(id),"vod",50,setOf("7"))
        P.install(id,P.current(id)!!.copy(revision="next"))
        assertTrue(provider.getVodInfo(50) is DomainResult.Error)
        assertEquals(0,server.requestCount)
        assertTrue(artwork.isEmpty())
    }
}
