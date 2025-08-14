package co.touchlab.dogify.domain.model

import kandalabs.commander.domain.model.Item

data class ItemWithCount(
    val item: Item,
    val count: Int = 0
)