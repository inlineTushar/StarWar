package com.tsaha.nucleus.navgraph

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavOptions
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navOptions
import com.tsaha.navigation.Navigable
import com.tsaha.navigation.NavigableGraph
import com.tsaha.navigation.ToBack
import com.tsaha.planetlist.PlanetListScreen
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.toRoute
import com.tsaha.navigation.NavigableGraph.*
import com.tsaha.nucleus.ui.theme.NucleusTheme
import com.tsaha.planetdetail.PlanetDetailsScreen

@Composable
internal fun MainNavGraph(
    navController: NavHostController = rememberNavController(),
    startDestination: NavigableGraph = PlanetListNavigable,
) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
    ) {
        composable<PlanetListNavigable> {
            PlanetListScreen(
                onNavigate = { destination, optionBuilder ->
                    navController.navigateTo(
                        destination = destination,
                        navOptions = navOptions(optionBuilder)
                    )
                }
            )
        }

        composable<PlanetDetailsNavigable> { backStackEntry ->
            val planetDetails = backStackEntry.toRoute<PlanetDetailsNavigable>()
            PlanetDetailsScreen(
                planetId = planetDetails.planetId
            ) { destination, optionBuilder ->
                navController.navigateTo(
                    destination = destination,
                    navOptions = navOptions(optionBuilder)
                )
            }
        }
    }
}

private fun NavHostController.navigateTo(
    destination: Navigable,
    navOptions: NavOptions?,
) {
    when (destination) {
        is ToBack -> if (previousBackStackEntry != null) popBackStack()
        else -> navigate(route = destination, navOptions = navOptions)
    }
}

@Preview(showBackground = true)
@Composable
private fun MainNavGraphPreview() {
    NucleusTheme {
        MainNavGraph()
    }
}