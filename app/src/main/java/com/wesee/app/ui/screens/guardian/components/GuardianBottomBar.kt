package com.wesee.app.ui.screens.guardian.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.wesee.app.R
import com.wesee.app.ui.components.NavItem

enum class GuardianTab { Home, Maps, Account }

/** Barra inferior do Responsável: Home, Maps e Account. Account ainda não tem tela (fora do escopo). */
@Composable
fun GuardianBottomBar(current: GuardianTab, onHome: () -> Unit, onMaps: () -> Unit, onAccount: (() -> Unit)? = null) {
    val shape = RoundedCornerShape(topStart = 18.dp, topEnd = 18.dp)
    Row(Modifier.fillMaxWidth().shadow(10.dp, shape).background(Color.White, shape).navigationBarsPadding().height(62.dp)) {
        NavItem("Home", if (current == GuardianTab.Home) R.drawable.ic_home_blue else R.drawable.nav_home,
            current == GuardianTab.Home, onHome, Modifier.weight(1f))
        NavItem("Maps", if (current == GuardianTab.Maps) R.drawable.ic_pin_add else R.drawable.nav_maps,
            current == GuardianTab.Maps, onMaps, Modifier.weight(1f))
        NavItem("Account", if (current == GuardianTab.Account) R.drawable.ic_account else R.drawable.nav_account,
            current == GuardianTab.Account, onAccount, Modifier.weight(1f))
    }
}
