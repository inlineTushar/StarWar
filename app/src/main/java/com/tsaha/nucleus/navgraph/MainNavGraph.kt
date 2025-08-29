package com.tsaha.nucleus.navgraph

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.tsaha.navigation.Route
import com.tsaha.nucleus.ui.theme.NucleusTheme
import com.tsaha.planetdetail.PlanetDetailsScreen
import com.tsaha.planetlist.PlanetListScreen

@Composable
internal fun MainNavGraph(
    navController: NavHostController = rememberNavController(),
    startDestination: Route = Route.PlanetList,
) {
    NavHost(
        navController = navController,
        startDestination = startDestination.value,
    ) {
        composable(route = Route.PlanetList.value) {
            PlanetListScreen(navController = navController)
        }
        composable(
            route = Route.PlanetDetails.value,
            arguments = listOf(
                navArgument("planetId") {
                    type = NavType.StringType
                    nullable = false
                }
            )
        ) { backStackEntry ->
            val planetId = backStackEntry.arguments?.getString("planetId")
            requireNotNull(planetId) { "planetId cannot be null" }
            PlanetDetailsScreen(planetId, navController = navController)
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun MainNavGraphPreview() {
    NucleusTheme {
        MainNavGraph()
    }
}