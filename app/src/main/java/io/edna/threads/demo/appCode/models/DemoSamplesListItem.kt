package io.edna.threads.demo.appCode.models

sealed class DemoSamplesListItem {
    object DIVIDER : DemoSamplesListItem()

    data class TITLE(val text: String) : DemoSamplesListItem() {
        override fun toString() = text
        companion object
    }

    data class TEXT(val text: String, val json: String) : DemoSamplesListItem() {
        override fun toString() = text
        companion object
    }
}
