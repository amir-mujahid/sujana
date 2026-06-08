package com.sujana.core.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
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
    // Dispatcher sub-routes
    const val DISPATCH_QUEUE       = "dispatcher/queue"
    // Rider sub-routes
    const val RIDER_TASKS          = "rider/tasks"
    const val TASK_DETAIL          = "rider/tasks/{assignmentId}"

    fun requestDetail(id: String) = "contributor/requests/$id"
    fun taskDetail(id: String)    = "rider/tasks/$id"
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

@Composable
fun RootNavGraph(
    sessionUser: User?,
    sessionLoaded: Boolean,
) {
    if (!sessionLoaded) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    val navController = rememberNavController()
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
            SuperAdminHomeScreen(onLogout = {
                authViewModel.logout()
                navController.navigate(Routes.LOGIN) { popUpTo(0) { inclusive = true } }
            })
        }
        composable(Routes.HOME_MPS_ADMIN) { backStackEntry ->
            val authViewModel: AuthViewModel = hiltViewModel(backStackEntry)
            MpsAdminHomeScreen(onLogout = {
                authViewModel.logout()
                navController.navigate(Routes.LOGIN) { popUpTo(0) { inclusive = true } }
            })
        }
        composable(Routes.HOME_MPS_DISPATCHER) { backStackEntry ->
            val authViewModel: AuthViewModel = hiltViewModel(backStackEntry)
            MpsDispatcherHomeScreen(
                onLogout = {
                    authViewModel.logout()
                    navController.navigate(Routes.LOGIN) { popUpTo(0) { inclusive = true } }
                },
                onNavigateToDispatchQueue = { navController.navigate(Routes.DISPATCH_QUEUE) },
            )
        }
        composable(Routes.HOME_SCHOOL_ADMIN) { backStackEntry ->
            val authViewModel: AuthViewModel = hiltViewModel(backStackEntry)
            SchoolAdminHomeScreen(onLogout = {
                authViewModel.logout()
                navController.navigate(Routes.LOGIN) { popUpTo(0) { inclusive = true } }
            })
        }
        composable(Routes.HOME_SCHOOL_STAFF) { backStackEntry ->
            val authViewModel: AuthViewModel = hiltViewModel(backStackEntry)
            SchoolStaffHomeScreen(onLogout = {
                authViewModel.logout()
                navController.navigate(Routes.LOGIN) { popUpTo(0) { inclusive = true } }
            })
        }
        composable(Routes.HOME_RIDER) { backStackEntry ->
            val authViewModel: AuthViewModel = hiltViewModel(backStackEntry)
            RiderHomeScreen(
                onLogout = {
                    authViewModel.logout()
                    navController.navigate(Routes.LOGIN) { popUpTo(0) { inclusive = true } }
                },
                onNavigateToMyTasks = { navController.navigate(Routes.RIDER_TASKS) },
            )
        }

        // --- Contributor routes ---
        composable(Routes.HOME_CONTRIBUTOR) { backStackEntry ->
            val authViewModel: AuthViewModel = hiltViewModel(backStackEntry)
            ContributorHomeScreen(
                onLogout = {
                    authViewModel.logout()
                    navController.navigate(Routes.LOGIN) { popUpTo(0) { inclusive = true } }
                },
                onNavigateToMyRequests    = { navController.navigate(Routes.MY_REQUESTS) },
                onNavigateToCreateRequest = { navController.navigate(Routes.CREATE_REQUEST) },
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
                onSubmitSuccess = { navController.navigateUp() },
                viewModel       = viewModel,
            )
        }
        composable(
            route     = Routes.REQUEST_DETAIL,
            arguments = listOf(navArgument("requestId") { type = NavType.StringType }),
        ) {
            val viewModel: RequestDetailViewModel = hiltViewModel()
            RequestDetailScreen(
                onNavigateUp = { navController.navigateUp() },
                viewModel    = viewModel,
            )
        }

        // --- Dispatcher routes ---
        composable(Routes.DISPATCH_QUEUE) {
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
        ) {
            val viewModel: TaskDetailViewModel = hiltViewModel()
            TaskDetailScreen(
                onNavigateUp = { navController.navigateUp() },
                viewModel    = viewModel,
            )
        }
    }
}
