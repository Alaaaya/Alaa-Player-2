package com.streamvault.data.manager.recording

import com.google.common.truth.Truth.assertThat
import okio.Buffer
import okio.BufferedSource
import okio.Source
import okio.Timeout
import okio.buffer
import org.junit.Test

class HlsRecordingParsingTest {
    @Test
    fun quotedCommaStaysInsideKeyUri() {
        val attrs = parseHlsAttributeList("""METHOD=AES-128,URI="https://k.example/key?a=1,b=2",IV=0x1234""")
        assertThat(attrs["METHOD"]).isEqualTo("AES-128")
        assertThat(attrs["URI"]).isEqualTo("https://k.example/key?a=1,b=2")
        assertThat(attrs["IV"]).isEqualTo("0x1234")
    }

    @Test
    fun repeatedUriAtNewSequenceIsDistinct() {
        assertThat(hlsSegmentIdentity(10, "seg.ts")).isNotEqualTo(hlsSegmentIdentity(11, "seg.ts"))
    }

    @Test
    fun boundedPrefixDoesNotDrainEndlessStream() {
        var served = 0L
        val endless: BufferedSource = object : Source {
            override fun read(sink: Buffer, byteCount: Long): Long {
                val n = minOf(byteCount, 256L); repeat(n.toInt()) { sink.writeByte('G'.code) }; served += n; return n
            }
            override fun timeout() = Timeout.NONE
            override fun close() = Unit
        }.buffer()
        val prefix = readBoundedPrefix(endless, 1024)
        assertThat(prefix.length).isEqualTo(1024)
        assertThat(served).isLessThan(64 * 1024L)
    }
}
