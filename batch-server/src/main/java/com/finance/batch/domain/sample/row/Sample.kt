package com.finance.batch.domain.sample.row

data class Sample(

    val id: Long = 0L,
    var name: String,
) {
    companion object Factory {
        fun of(sampleSource: SampleSource): Sample {
            return Sample(name = sampleSource.name)
        }
    }
}