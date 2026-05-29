package com.finance.batch.domain.sample.row

import com.finance.batch.global.annotation.NoArg

@NoArg
data class SampleSource (
    val id: Long,
    var name: String,
)