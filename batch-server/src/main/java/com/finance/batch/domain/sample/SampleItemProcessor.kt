package com.finance.batch.domain.sample

import com.finance.batch.domain.finance.sample.row.Sample
import com.finance.batch.domain.finance.sample.row.SampleSource
import org.springframework.batch.item.ItemProcessor
import org.springframework.stereotype.Component

@Component
class SampleItemProcessor : ItemProcessor<SampleSource, Sample> {

    override fun process(item: SampleSource): Sample? {

        if (item.name == "ERROR") {
            throw RuntimeException("ERROR")
        }

        return Sample.of(item)
    }
}