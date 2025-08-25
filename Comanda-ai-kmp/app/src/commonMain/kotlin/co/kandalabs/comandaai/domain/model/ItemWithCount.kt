package co.kandalabs.comandaai.domain.model

import co.kandalabs.comandaai.domain.Item

data class ItemWithCount(
    val item: Item,
    val count: Int = 0
)