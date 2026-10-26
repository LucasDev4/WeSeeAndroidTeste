package com.wesee.app.navigation

import androidx.activity.compose.BackHandler
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.platform.LocalContext
import com.wesee.app.data.UserStore
import com.wesee.app.ui.screens.disabled.DisabledLoginScreen
import com.wesee.app.ui.screens.disabled.DisabledRegisterScreen
import com.wesee.app.ui.screens.guardian.GuardianLoginScreen
import com.wesee.app.ui.screens.guardian.home.GuardianHomeScreen
import com.wesee.app.ui.screens.guardian.profile.GuardianProfileScreen
import com.wesee.app.ui.screens.guardian.home.GuardianMapsScreen
import com.wesee.app.ui.screens.guardian.GuardianRegisterScreen
import com.wesee.app.ui.screens.pcd.chat.PcdChatScreen
import com.wesee.app.ui.screens.pcd.home.PcdHomeScreen
import com.wesee.app.ui.screens.pcd.maps.PcdMapsScreen
import com.wesee.app.ui.screens.pcd.profile.PcdProfileScreen
import com.wesee.app.ui.components.LocalPcdNav
import com.wesee.app.ui.components.PcdNav
import com.wesee.app.ui.screens.profile.WhoAreYouScreen
import com.wesee.app.ui.screens.welcome.WelcomeScreen
import com.wesee.app.network.ApiResult
import com.wesee.app.network.AtlasApi
import com.wesee.app.network.UserType

/** PcdHome / PcdChat só são alcançadas pelo fluxo do "PCD visual" (login por voz ou biometria). */
enum class Route {
    Welcome, WhoAreYou, GuardianLogin, GuardianRegister, DisabledLogin, DisabledRegister, PcdHome, PcdChat, PcdMaps, PcdProfile,
    GuardianHome, GuardianMaps, GuardianProfile
}

/** Destino ao pressionar "voltar" (null = sai do app). */
private fun Route.back(): Route? = when (this) {
    Route.Welcome -> null
    Route.WhoAreYou -> Route.Welcome
    Route.GuardianLogin -> Route.WhoAreYou
    Route.GuardianRegister -> Route.GuardianLogin
    Route.DisabledLogin, Route.DisabledRegister -> Route.WhoAreYou
    Route.PcdHome -> null
    Route.PcdChat, Route.PcdMaps, Route.PcdProfile -> Route.PcdHome
    Route.GuardianHome -> null
    Route.GuardianMaps, Route.GuardianProfile -> Route.GuardianHome
}

@Composable
fun WeSeeApp() {
    val context = LocalContext.current
    val userStore = remember { UserStore(context) }
    // Sessão salva (token do backend) de um usuário PCD → abre direto na Home
    var route by rememberSaveable {
        mutableStateOf(if (userStore.isLoggedIn && userStore.tipo == UserType.PCD_VISUAL) Route.PcdHome else Route.Welcome)
    }
    // Valida o token no backend ao abrir o app; se expirou/foi revogado, encerra a sessão.
    LaunchedEffect(Unit) {
        val token = userStore.token ?: return@LaunchedEffect
        when (val r = AtlasApi.me(token)) {
            is ApiResult.Success -> userStore.updateName(r.value.nome)
            is ApiResult.Error -> if (r.code == 401 || r.code == 403) {
                userStore.logout()
                when (route) {
                    Route.PcdHome, Route.PcdChat, Route.PcdMaps, Route.PcdProfile -> route = Route.DisabledLogin
                    Route.GuardianHome, Route.GuardianMaps, Route.GuardianProfile -> route = Route.GuardianLogin
                    else -> Unit
                }
            }
        }
    }
    BackHandler(enabled = route.back() != null) { route = route.back()!! }

    val pcdNav = PcdNav(
        onHome = { route = Route.PcdHome }, onChat = { route = Route.PcdChat },
        onMaps = { route = Route.PcdMaps }, onAccount = { route = Route.PcdProfile },
    )
    CompositionLocalProvider(LocalPcdNav provides pcdNav) {
    when (route) {
        Route.Welcome -> WelcomeScreen(onLogin = {}, onRegister = { route = Route.WhoAreYou })
        Route.WhoAreYou -> WhoAreYouScreen(
            onSelectPcd = { route = Route.DisabledRegister },
            onSelectGuardian = { route = Route.GuardianLogin },
        )
        Route.GuardianLogin -> GuardianLoginScreen(
            onLoggedIn = { userStore.saveSession(it); route = Route.GuardianHome },
            onCreateAccount = { route = Route.GuardianRegister },
        )
        Route.GuardianRegister -> GuardianRegisterScreen(
            onRegistered = { route = Route.GuardianLogin },
            onGoToLogin = { route = Route.GuardianLogin },
        )
        Route.DisabledRegister -> DisabledRegisterScreen(
            onRegistered = { session ->
                if (session != null) { userStore.saveSession(session); route = Route.PcdHome } else { route = Route.DisabledLogin }
            },
            canUseBiometric = userStore.isLoggedIn,
            onBiometricLogin = { route = Route.PcdHome },
        )
        Route.DisabledLogin -> DisabledLoginScreen(
            onLoggedIn = { userStore.saveSession(it); route = Route.PcdHome },
            canUseBiometric = userStore.isLoggedIn && userStore.tipo == UserType.PCD_VISUAL,
            onBiometricLogin = { route = Route.PcdHome },
        )
        Route.PcdHome -> PcdHomeScreen(firstName = userStore.firstName, onOpenChat = { route = Route.PcdChat })
        Route.PcdChat -> PcdChatScreen(onOpenHome = { route = Route.PcdHome })
        Route.GuardianHome -> GuardianHomeScreen(
            responsavelName = userStore.name,
            onOpenMaps = { route = Route.GuardianMaps },
            onAccount = { route = Route.GuardianProfile },
        )
        Route.GuardianMaps -> GuardianMapsScreen(
            responsavelName = userStore.name,
            onOpenHome = { route = Route.GuardianHome },
            onAccount = { route = Route.GuardianProfile },
        )
        Route.GuardianProfile -> GuardianProfileScreen(
            userStore = userStore,
            onBack = { route = Route.GuardianHome },
            onHome = { route = Route.GuardianHome },
            onMaps = { route = Route.GuardianMaps },
            onLogout = { userStore.logout(); route = Route.GuardianLogin },
        )
        Route.PcdMaps -> PcdMapsScreen()
        Route.PcdProfile -> PcdProfileScreen(
            userStore = userStore,
            onBack = { route = Route.PcdHome },
            onLogout = { userStore.logout(); route = Route.DisabledLogin },
        )
    }
    }
}
