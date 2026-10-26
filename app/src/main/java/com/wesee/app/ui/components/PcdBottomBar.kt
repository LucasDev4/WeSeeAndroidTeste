package com.wesee.app.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.wesee.app.R
import com.wesee.app.ui.theme.WeSeeBlue

enum class PcdTab { Home, Chat, Maps, Account }

/** Navegação das 4 abas do modo PCD; fornecida pela navegação do app (Home e Chat não precisam conhecê-la). */
data class PcdNav(
    val onHome: () -> Unit = {}, val onChat: () -> Unit = {},
    val onMaps: () -> Unit = {}, val onAccount: () -> Unit = {},
)
val LocalPcdNav = staticCompositionLocalOf { PcdNav() }

private val Inactive = Color(0xFF7FB4F9)

@Composable
internal fun NavItem(label: String, icon: Int, selected: Boolean, onClick: (() -> Unit)?, modifier: Modifier) {
    Column(
        modifier.fillMaxHeight().then(if (onClick != null) Modifier.clickable(role = Role.Tab, onClick = onClick) else Modifier),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        if (selected) Box(Modifier.width(28.dp).height(4.dp).background(WeSeeBlue, RoundedCornerShape(bottomStart = 4.dp, bottomEnd = 4.dp)))
        else Spacer(Modifier.height(4.dp))
        Spacer(Modifier.height(9.dp))
        Image(painterResource(icon), null, Modifier.size(22.dp))
        Spacer(Modifier.height(3.dp))
        WeSeeText(label, if (selected) WeSeeBlue else Inactive, 10, if (selected) FontWeight.Bold else FontWeight.SemiBold)
    }
}

/** Barra inferior do PCD (Home, Chat, Maps, Account). */
@Composable
fun PcdBottomBar(current: PcdTab, onHome: () -> Unit, onChat: () -> Unit) {
    val nav = LocalPcdNav.current
    val shape = RoundedCornerShape(topStart = 18.dp, topEnd = 18.dp)
    Row(
        Modifier.fillMaxWidth().shadow(10.dp, shape).background(Color.White, shape).navigationBarsPadding().height(62.dp),
    ) {
        NavItem("Home", if (current == PcdTab.Home) R.drawable.nav_home_selected else R.drawable.nav_home,
            current == PcdTab.Home, onHome, Modifier.weight(1f))
        NavItem("Chat", if (current == PcdTab.Chat) R.drawable.nav_chat_selected else R.drawable.nav_chat,
            current == PcdTab.Chat, onChat, Modifier.weight(1f))
        NavItem("Maps", if (current == PcdTab.Maps) R.drawable.ic_pin_add else R.drawable.nav_maps,
            current == PcdTab.Maps, nav.onMaps, Modifier.weight(1f))
        NavItem("Account", if (current == PcdTab.Account) R.drawable.ic_account else R.drawable.nav_account,
            current == PcdTab.Account, nav.onAccount, Modifier.weight(1f))
    }
}
