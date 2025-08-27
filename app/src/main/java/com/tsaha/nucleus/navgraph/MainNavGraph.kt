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
import com.tsaha.stardetail.StarDetailScreen
import com.tsaha.starlist.StarListScreen
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.toRoute
import com.tsaha.nucleus.ui.theme.NucleusTheme

@Composable
internal fun MainNavGraph(
    navController: NavHostController = rememberNavController(),
    startDestination: NavigableGraph = NavigableGraph.StarList,
) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
    ) {
        composable<NavigableGraph.StarList> {
            StarListScreen { destination, optionBuilder ->
                navController.navigateTo(
                    destination = destination,
                    navOptions = navOptions(optionBuilder)
                )
            }
        }

        composable<NavigableGraph.StarDetails> { backStackEntry ->
            val starDetails = backStackEntry.toRoute<NavigableGraph.StarDetails>()
            StarDetailScreen(
                starId = starDetails.starId
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