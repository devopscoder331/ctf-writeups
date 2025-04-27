package app.blackhol3.navigation

sealed class Screen(
    val path: String,
) {
    object Intro : Screen("intro")

    object KeyGen : Screen("keygen")

    object KeyRegen : Screen("keyregen/{keyId}") {
        fun createRoute(keyId: String): String = "keyregen/$keyId"
    }

    object ChatList : Screen("chatList")

    object Chat : Screen("chat/{chatId}") {
        fun createRoute(chatId: String): String = "chat/$chatId"
    }

    object ImportPubKey : Screen("importPubKey")
}
