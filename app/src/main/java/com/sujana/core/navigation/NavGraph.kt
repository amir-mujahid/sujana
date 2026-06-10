package com.sujana.core.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import androidx.navigation.navDeepLink
import com.sujana.domain.model.User
import com.sujana.feature.auth.AuthViewModel
import com.sujana.feature.auth.ui.LoginScreen
import com.sujana.feature.auth.ui.RegisterScreen
import com.sujana.feature.dispatch.DispatchViewModel
import com.sujana.feature.dispatch.ui.DispatchQueueScreen
import com.sujana.feature.home.ContributorHomeScreen
import com.sujana.feature.home.MpsAdminHomeScreen
import com.sujana.feature.home.MpsDispatcherHomeScreen
import com.sujana.feature.home.RiderHomeScreen
import com.sujana.feature.home.SchoolAdminHomeScreen
import com.sujana.feature.home.SchoolStaffHomeScreen
import com.sujana.feature.home.SuperAdminHomeScreen
import com.sujana.feature.school.SchoolRequestViewModel
import com.sujana.feature.school.SchoolRequestsViewModel
import com.sujana.feature.school.ui.SchoolRequestScreen
import com.sujana.feature.school.ui.SchoolRequestsScreen
import com.sujana.feature.notification.NotificationUiState
import com.sujana.feature.notification.NotificationViewModel
import com.sujana.feature.notification.ui.NotificationCenterScreen
import com.sujana.feature.notification.ui.NotificationPrefsScreen
import com.sujana.feature.request.CreateRequestViewModel
import com.sujana.feature.request.MyRequestsViewModel
import com.sujana.feature.request.RequestDetailViewModel
import com.sujana.feature.request.ui.CreateRequestScreen
import com.sujana.feature.request.ui.MyRequestsScreen
import com.sujana.feature.request.ui.RequestDetailScreen
import com.sujana.feature.rider.RiderTasksViewModel
import com.sujana.feature.rider.TaskDetailViewModel
import com.sujana.feature.rider.ui.RiderTasksScreen
import com.sujana.feature.rider.ui.TaskDetailScreen
import com.sujana.feature.tracking.LiveTrackingViewModel
import com.sujana.feature.tracking.ui.LiveTrackingScreen
import com.sujana.feature.tracking.ui.LocationPermissionGate
import com.sujana.shared.Role

private object Routes {
    const val LOGIN                = "login"
    const val REGISTER             = "register"
    const val HOME_SUPER_ADMIN     = "home/super_admin"
    const val HOME_MPS_ADMIN       = "home/mps_admin"
    const val HOME_MPS_DISPATCHER  = "home/mps_dispatcher"
    const val HOME_SCHOOL_ADMIN    = "home/school_admin"
    const val HOME_SCHOOL_STAFF    = "home/school_staff"
    const val HOME_RIDER           = "home/rider"
    const val HOME_CONTRIBUTOR     = "home/contributor"
    // Contributor sub-routes
    const val MY_REQUESTS          = "contributor/requests"
    const val CREATE_REQUEST       = "contributor/requests/create"
    const val REQUEST_DETAIL       = "contributor/requests/{requestId}"
    // School sub-routes
    const val SCHOOL_REQUESTS      = "school/requests"
    const val SCHOOL_CREATE_REQUEST = "school/requests/create"
    const val SCHOOL_REQUEST_DETAIL = "school/requests/{requestId}"
    // Dispatcher sub-routes
    const val DISPATCH_QUEUE       = "dispatcher/queue"
    // Rider sub-routes
    const val RIDER_TASKS          = "rider/tasks"
    const val TASK_DETAIL          = "rider/tasks/{assignmentId}"
    // Tracking
    const val LIVE_TRACKING        = "tracking/{assignmentId}"
    // Notifications
    const val NOTIFICATIONS        = "notifications"
    const val NOTIFICATION_PREFS   = "notifications/prefs"

    fun requestDetail(id: String)       = "contributor/requests/$id"
    fun schoolRequestDetail(id: String) = "school/requests/$id"
    fun taskDetail(id: String)          = "rider/tasks/$id"
    fun liveTracking(id: String)        = "tracking/$id"
}

private fun User.homeRoute(): String = when (role) {
    Role.SUPER_ADMIN     -> Routes.HOME_SUPER_ADMIN
    Role.MPS_ADMIN       -> Routes.HOME_MPS_ADMIN
    Role.MPS_DISPATCHER  -> Routes.HOME_MPS_DISPATCHER
    Role.SCHOOL_ADMIN    -> Routes.HOME_SCHOOL_ADMIN
    Role.SCHOOL_STAFF    -> Routes.HOME_SCHOOL_STAFF
    Role.RIDER           -> Routes.HOME_RIDER
    Role.CONTRIBUTOR     -> Routes.HOME_CONTRIBUTOR
}

private fun handleSujanaDeepLink(deeplink: String, navController: androidx.navigation.NavController) {
    val path = deeplink.removePrefix("sujana://")
    when {
        path.startsWith("request/")    -> {
            val id = path.removePrefix("request/")
            navController.navigate(Routes.requestDetail(id))
        }
        path.startsWith("assignment/") -> {
            val id = path.removePrefix("assignment/")
            navController.navigate(Routes.taskDetail(id))
        }
        path == "dispatch"             -> navController.navigate(Routes.DISPATCH_QUEUE)
        path == "notifications"        -> navController.navigate(Routes.NOTIFICATIONS)
        else -> { /* unknown — ignore */ }
    }
}

@Composable
fun RootNavGraph(
    navController: NavHostController,
    sessionUser: User?,
    sessionLoaded: Boolean,
) {
    if (!sessionLoaded) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    val startDestination = sessionUser?.homeRoute() ?: Routes.LOGIN

    NavHost(navController = navController, startDestination = startDestination) {
        composable(Routes.LOGIN) { backStackEntry ->
            val authViewModel: AuthViewModel = hiltViewModel(backStackEntry)
            LoginScreen(
                onLoginSuccess = { user ->
                    navController.navigate(user.homeRoute()) {
                        popUpTo(Routes.LOGIN) { inclusive = true }
                    }
                },
                onNavigateToRegister = { navController.navigate(Routes.REGISTER) },
                viewModel = authViewModel,
            )
        }
        composable(Routes.REGISTER) { backStackEntry ->
            val authViewModel: AuthViewModel = hiltViewModel(backStackEntry)
            RegisterScreen(
                onRegisterSuccess = { user ->
                    navController.navigate(user.homeRoute()) {
                        popUpTo(Routes.LOGIN) { inclusive = true }
                    }
                },
                onNavigateToLogin = { navController.popBackStack() },
                viewModel = authViewModel,
            )
        }

        composable(Routes.HOME_SUPER_ADMIN) { backStackEntry ->
            val authViewModel: AuthViewModel = hiltViewModel(backStackEntry)
            val notifVm: NotificationViewModel = hiltViewModel(backStackEntry)
            val notifState by notifVm.uiState.collectAsState()
            val unreadCount = (notifState as? NotificationUiState.Content)?.unreadCount ?: 0
            SuperAdminHomeScreen(
                onLogout = {
                    authViewModel.logout()
                    navController.navigate(Routes.LOGIN) { popUpTo(0) { inclusive = true } }
                },
                onNavigateToNotifications = { navController.navigate(Routes.NOTIFICATIONS) },
                unreadCount               = unreadCount,
            )
        }
        composable(Routes.HOME_MPS_ADMIN) { backStackEntry ->
            val authViewModel: AuthViewModel = hiltViewModel(backStackEntry)
            val notifVm: NotificationViewModel = hiltViewModel(backStackEntry)
            val notifState by notifVm.uiState.collectAsState()
            val unreadCount = (notifState as? NotificationUiState.Content)?.unreadCount ?: 0
            MpsAdminHomeScreen(
                onLogout = {
                    authViewModel.logout()
                    navController.navigate(Routes.LOGIN) { popUpTo(0) { inclusive = true } }
                },
                onNavigateToNotifications = { navController.navigate(Routes.NOTIFICATIONS) },
                unreadCount               = unreadCount,
            )
        }
        composable(Routes.HOME_MPS_DISPATCHER) { backStackEntry ->
            val authViewModel: AuthViewModel = hiltViewModel(backStackEntry)
            val notifVm: NotificationViewModel = hiltViewModel(backStackEntry)
            val notifState by notifVm.uiState.collectAsState()
            val unreadCount = (notifState as? NotificationUiState.Content)?.unreadCount ?: 0
            MpsDispatcherHomeScreen(
                onLogout = {
                    authViewModel.logout()
                    navController.navigate(Routes.LOGIN) { popUpTo(0) { inclusive = true } }
                },
                onNavigateToDispatchQueue = { navController.navigate(Routes.DISPATCH_QUEUE) },
                onNavigateToNotifications = { navController.navigate(Routes.NOTIFICATIONS) },
                unreadCount               = unreadCount,
            )
        }
        composable(Routes.HOME_SCHOOL_ADMIN) { backStackEntry ->
            val authViewModel: AuthViewModel = hiltViewModel(backStackEntry)
            val notifVm: NotificationViewModel = hiltViewModel(backStackEntry)
            val notifState by notifVm.uiState.collectAsState()
            val unreadCount = (notifState as? NotificationUiState.Content)?.unreadCount ?: 0
            SchoolAdminHomeScreen(
                onLogout = {
                    authViewModel.logout()
                    navController.navigate(Routes.LOGIN) { popUpTo(0) { inclusive = true } }
                },
                onNavigateToRequests      = { navController.navigate(Routes.SCHOOL_REQUESTS) },
                onNavigateToCreateRequest = { navController.navigate(Routes.SCHOOL_CREATE_REQUEST) },
                onNavigateToNotifications = { navController.navigate(Routes.NOTIFICATIONS) },
                unreadCount               = unreadCount,
            )
        }
        composable(Routes.HOME_SCHOOL_STAFF) { backStackEntry ->
            val authViewModel: AuthViewModel = hiltViewModel(backStackEntry)
            val notifVm: NotificationViewModel = hiltViewModel(backStackEntry)
            val notifState by notifVm.uiState.collectAsState()
            val unreadCount = (notifState as? NotificationUiState.Content)?.unreadCount ?: 0
            SchoolStaffHomeScreen(
                onLogout = {
                    authViewModel.logout()
                    navController.navigate(Routes.LOGIN) { popUpTo(0) { inclusive = true } }
                },
                onNavigateToRequests      = { navController.navigate(Routes.SCHOOL_REQUESTS) },
                onNavigateToCreateRequest = { navController.navigate(Routes.SCHOOL_CREATE_REQUEST) },
                onNavigateToNotifications = { navController.navigate(Routes.NOTIFICATIONS) },
                unreadCount               = unreadCount,
            )
        }
        composable(Routes.HOME_RIDER) { backStackEntry ->
            val authViewModel: AuthViewModel = hiltViewModel(backStackEntry)
            val notifVm: NotificationViewModel = hiltViewModel(backStackEntry)
            val notifState by notifVm.uiState.collectAsState()
            val unreadCount = (notifState as? NotificationUiState.Content)?.unreadCount ?: 0
            RiderHomeScreen(
                onLogout = {
                    authViewModel.logout()
                    navController.navigate(Routes.LOGIN) { popUpTo(0) { inclusive = true } }
                },
                onNavigateToMyTasks       = { navController.navigate(Routes.RIDER_TASKS) },
                onNavigateToNotifications = { navController.navigate(Routes.NOTIFICATIONS) },
                unreadCount               = unreadCount,
            )
        }

        // --- Contributor routes ---
        composable(Routes.HOME_CONTRIBUTOR) { backStackEntry ->
            val authViewModel: AuthViewModel = hiltViewModel(backStackEntry)
            val notifVm: NotificationViewModel = hiltViewModel(backStackEntry)
            val notifState by notifVm.uiState.collectAsState()
            val unreadCount = (notifState as? NotificationUiState.Content)?.unreadCount ?: 0
            ContributorHomeScreen(
                onLogout = {
                    authViewModel.logout()
                    navController.navigate(Routes.LOGIN) { popUpTo(0) { inclusive = true } }
                },
                onNavigateToMyRequests    = { navController.navigate(Routes.MY_REQUESTS) },
                onNavigateToCreateRequest = { navController.navigate(Routes.CREATE_REQUEST) },
                onNavigateToNotifications = { navController.navigate(Routes.NOTIFICATIONS) },
                unreadCount               = unreadCount,
            )
        }
        composable(Routes.MY_REQUESTS) {
            val viewModel: MyRequestsViewModel = hiltViewModel()
            MyRequestsScreen(
                onNavigateUp       = { navController.navigateUp() },
                onNavigateToDetail = { id -> navController.navigate(Routes.requestDetail(id)) },
                onNavigateToCreate = { navController.navigate(Routes.CREATE_REQUEST) },
                viewModel          = viewModel,
            )
        }
        composable(Routes.CREATE_REQUEST) {
            val viewModel: CreateRequestViewModel = hiltViewModel()
            CreateRequestScreen(
                onNavigateUp    = { navController.navigateUp() },
                onSubmitSuccess = { id ->
                    navController.navigate(Routes.requestDetail(id)) {
                        popUpTo(Routes.CREATE_REQUEST) { inclusive = true }
                    }
                },
                viewModel       = viewModel,
            )
        }
        composable(
            route     = Routes.REQUEST_DETAIL,
            arguments = listOf(navArgument("requestId") { type = NavType.StringType }),
            deepLinks = listOf(navDeepLink { uriPattern = "sujana://request/{requestId}" }),
        ) {
            val viewModel: RequestDetailViewModel = hiltViewModel()
            RequestDetailScreen(
                onNavigateUp         = { navController.navigateUp() },
                onNavigateToTracking = { assignmentId -> navController.navigate(Routes.liveTracking(assignmentId)) },
                viewModel            = viewModel,
            )
        }

        // --- School routes ---
        composable(Routes.SCHOOL_REQUESTS) {
            val viewModel: SchoolRequestsViewModel = hiltViewModel()
            SchoolRequestsScreen(
                onNavigateUp       = { navController.navigateUp() },
                onNavigateToDetail = { id -> navController.navigate(Routes.schoolRequestDetail(id)) },
                onNavigateToCreate = { navController.navigate(Routes.SCHOOL_CREATE_REQUEST) },
                viewModel          = viewModel,
            )
        }
        composable(Routes.SCHOOL_CREATE_REQUEST) {
            val viewModel: SchoolRequestViewModel = hiltViewModel()
            SchoolRequestScreen(
                onNavigateUp    = { navController.navigateUp() },
                onSubmitSuccess = { id ->
                    navController.navigate(Routes.schoolRequestDetail(id)) {
                        popUpTo(Routes.SCHOOL_CREATE_REQUEST) { inclusive = true }
                    }
                },
                viewModel       = viewModel,
            )
        }
        composable(
            route     = Routes.SCHOOL_REQUEST_DETAIL,
            arguments = listOf(navArgument("requestId") { type = NavType.StringType }),
        ) {
            val viewModel: RequestDetailViewModel = hiltViewModel()
            RequestDetailScreen(
                onNavigateUp         = { navController.navigateUp() },
                onNavigateToTracking = { assignmentId -> navController.navigate(Routes.liveTracking(assignmentId)) },
                viewModel            = viewModel,
            )
        }

        // --- Dispatcher routes ---
        composable(
            route     = Routes.DISPATCH_QUEUE,
            deepLinks = listOf(navDeepLink { uriPattern = "sujana://dispatch" }),
        ) {
            val viewModel: DispatchViewModel = hiltViewModel()
            DispatchQueueScreen(
                onNavigateUp = { navController.navigateUp() },
                viewModel    = viewModel,
            )
        }

        // --- Rider routes ---
        composable(Routes.RIDER_TASKS) {
            val viewModel: RiderTasksViewModel = hiltViewModel()
            RiderTasksScreen(
                onNavigateUp     = { navController.navigateUp() },
                onNavigateToTask = { id -> navController.navigate(Routes.taskDetail(id)) },
                viewModel        = viewModel,
            )
        }
        composable(
            route     = Routes.TASK_DETAIL,
            arguments = listOf(navArgument("assignmentId") { type = NavType.StringType }),
            deepLinks = listOf(navDeepLink { uriPattern = "sujana://assignment/{assignmentId}" }),
        ) {
            val viewModel: TaskDetailViewModel = hiltViewModel()
            TaskDetailScreen(
                onNavigateUp         = { navController.navigateUp() },
                onNavigateToTracking = { assignmentId -> navController.navigate(Routes.liveTracking(assignmentId)) },
                viewModel            = viewModel,
            )
        }

        // --- Live tracking ---
        composable(
            route     = Routes.LIVE_TRACKING,
            arguments = listOf(navArgument("assignmentId") { type = NavType.StringType }),
        ) {
            val viewModel: LiveTrackingViewModel = hiltViewModel()
            LocationPermissionGate {
                LiveTrackingScreen(
                    onNavigateUp = { navController.navigateUp() },
                    viewModel    = viewModel,
                )
            }
        }

        // --- Notification routes ---
        composable(
            route     = Routes.NOTIFICATIONS,
            deepLinks = listOf(navDeepLink { uriPattern = "sujana://notifications" }),
        ) {
            val viewModel: NotificationViewModel = hiltViewModel()
            NotificationCenterScreen(
                onNavigateUp       = { navController.navigateUp() },
                onNavigateToPrefs  = { navController.navigate(Routes.NOTIFICATION_PREFS) },
                onDeepLink         = { deeplink -> handleSujanaDeepLink(deeplink, navController) },
                viewModel          = viewModel,
            )
        }
        composable(Routes.NOTIFICATION_PREFS) {
            val viewModel: NotificationViewModel = hiltViewModel()
            NotificationPrefsScreen(
                onNavigateUp = { navController.navigateUp() },
                viewModel    = viewModel,
            )
        }
    }
}
