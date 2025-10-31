package com.example.assignmentfiveq1

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.assignmentfiveq1.ui.theme.AssignmentFiveQ1Theme
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class MainActivity : ComponentActivity() {
    // Instantiate ViewModel using the activity-ktx delegate
    private val recipeViewModel: RecipeViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AssignmentFiveQ1Theme {
                // Main entry point for the app UI
                RecipeApp(recipeViewModel)
            }
        }
    }
}

// data class for one recipe
data class Recipe(
    val id: Int,
    val title: String,
    val ingd: List<String>,
    val steps: List<String>,
)

// sealed interface for nav routes
sealed interface Routes {
    val route: String

    data object Home : Routes {
        override val route: String = "home"
    }

    data object AddRecipe : Routes {
        override val route: String = "add_recipe"
    }

    data class RecipeDetail(val recipeId: Int) : Routes {
        // The route pattern for the NavHost
        override val route: String = "recipe_detail/$recipeId"

        // A companion object to hold the static route definition for the NavController
        companion object {
            const val routePattern = "recipe_detail/{recipeId}"
        }
    }

    data object Settings : Routes {
        override val route: String = "settings"
    }
}

// holds and manages recipe data
class RecipeViewModel : ViewModel() {

    // Private mutable state flow that can be modified only within the ViewModel
    private val _recipes = MutableStateFlow(
        listOf(
            Recipe(1, "Chicken Alfredo", listOf("Fettuccine", "Chicken Breast", "Heavy Cream", "Parmesan Cheese"), listOf("Boil pasta.", "Cook chicken.", "Make sauce and combine everything.")),
            Recipe(2, "Tacos", listOf("Ground Beef", "Taco Shells", "Lettuce", "Tomato", "Cheese"), listOf("Cook the ground beef.", "Warm the taco shells.", "Assemble tacos with toppings.")),
            Recipe(3, "Caesar Salad", listOf("Romaine Lettuce", "Croutons", "Parmesan Cheese", "Caesar Dressing"), listOf("Chop the lettuce.", "Toss all ingredients in a large bowl."))
        )
    )

    // Public, read-only state flow that UI can collect
    val recipes: StateFlow<List<Recipe>> = _recipes.asStateFlow()

    fun addRecipe(title: String, ingredients: String, steps: String) {
        val newRecipe = Recipe(
            // generate id
            id = (_recipes.value.maxOfOrNull { it.id } ?: 0) + 1,
            title = title.trim(),
            ingd = ingredients.lines().map { it.trim() }.filter { it.isNotEmpty() },
            steps = steps.lines().map { it.trim() }.filter { it.isNotEmpty() }
        )

        // use the 'update' function for thread-safe state modification
        _recipes.update { currentRecipes ->
            currentRecipes + newRecipe
        }
    }

    fun getRecipeById(id: Int): Recipe? {
        return _recipes.value.find { it.id == id }
    }
}

@Composable
fun RecipeApp(viewModel: RecipeViewModel) {
    val navController = rememberNavController()

    Scaffold(
        bottomBar = { AppBottomNavigation(navController = navController) }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Routes.Home.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            // Home Screen Route
            composable(Routes.Home.route) {
                HomeScreen(
                    viewModel = viewModel,
                    onRecipeClick = { recipeId ->
                        // Navigate to detail screen using the helper function
                        navController.navigate(Routes.RecipeDetail(recipeId).route)
                    }
                )
            }
            // Add Recipe Screen Route
            composable(Routes.AddRecipe.route) {
                AddRecipeScreen(
                    viewModel = viewModel,
                    onRecipeAdded = {
                        // Navigate back to Home and clear the back stack
                        navController.navigate(Routes.Home.route) {
                            popUpTo(Routes.Home.route) { inclusive = true }
                        }
                    }
                )
            }
            // Recipe Detail Screen Route with an argument
            composable(
                route = Routes.RecipeDetail.routePattern,
                arguments = listOf(navArgument("recipeId") {type = NavType.IntType})
            ) { backStackEntry ->
                // Extract the recipeId from the arguments
                val recipeId = backStackEntry.arguments?.getInt("recipeId")
                val recipe = recipeId?.let {viewModel.getRecipeById(it)}
                if (recipe != null) {
                    RecipeDetailScreen(recipe = recipe)
                } else {
                    Text("Recipe not found!")
                }
            }
            composable(Routes.Settings.route) {
                SettingsScreen()
            }
        }
    }
}

@Composable
fun HomeScreen(viewModel: RecipeViewModel, onRecipeClick: (Int) -> Unit) {
    // Collect the state from the ViewModel
    val recipes by viewModel.recipes.collectAsState()

    // Use a Column to stack the title and the list vertically
    Column(modifier = Modifier.padding(16.dp)) {
        // Title for the Home screen
        Text(
            text = "My Recipes (Click to expand recipe)",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        LazyColumn {
            items(recipes) { recipe ->
                Text(
                    text = recipe.title,
                    fontSize = 20.sp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onRecipeClick(recipe.id) }
                        .padding(vertical = 12.dp)
                )
                HorizontalDivider()
            }
        }
    }
}


@Composable
fun RecipeDetailScreen(recipe: Recipe) {
    Column(modifier = Modifier.padding(16.dp)) {
        Text(recipe.title, fontSize = 24.sp, fontWeight = FontWeight.Bold)

        Text(
            text = "Ingredients",
            fontSize = 20.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(top = 16.dp)
        )
        recipe.ingd.forEach { ingredient ->
            Text("• $ingredient", modifier = Modifier.padding(start = 8.dp))
        }

        Text(
            text = "Steps",
            fontSize = 20.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(top = 16.dp)
        )
        recipe.steps.forEachIndexed { index, step ->
            Text("${index + 1}. $step", modifier = Modifier.padding(start = 8.dp, top = 4.dp))
        }
    }
}

@Composable
fun AddRecipeScreen(viewModel: RecipeViewModel, onRecipeAdded: () -> Unit) {
    var title by remember { mutableStateOf("") }
    var ingredients by remember { mutableStateOf("") }
    var steps by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text("Add a New Recipe", fontSize = 24.sp, fontWeight = FontWeight.Bold)

        OutlinedTextField(
            value = title,
            onValueChange = { title = it },
            label = { Text("Recipe Title") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = ingredients,
            onValueChange = { ingredients = it },
            label = { Text("Ingredients (one per line)") },
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        )

        OutlinedTextField(
            value = steps,
            onValueChange = { steps = it },
            label = { Text("Steps (one per line)") },
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f) // Give more space
        )

        Button(
            onClick = {
                viewModel.addRecipe(title, ingredients, steps)
                onRecipeAdded() // Trigger navigation
            },
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF3DDC84) // Classic Android Green
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "Add Recipe",
                color = Color.Black // Change text to black for better contrast
            )
        }
    }
}


// Composable for the Settings Screen
@Composable
fun SettingsScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Settings", fontSize = 24.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun AppBottomNavigation(navController: NavHostController) {
    NavigationBar {
        // Get the current route to highlight the correct item
        val currentBackStackEntry by navController.currentBackStackEntryAsState()
        val currentRoute = currentBackStackEntry?.destination?.route

        NavigationBarItem(
            icon = { Icon(Icons.Filled.Home, contentDescription = "Home") },
            label = { Text("Home") },
            selected = currentRoute == Routes.Home.route,
            onClick = {
                navController.navigate(Routes.Home.route) {
                    // Prevents multiple copies of the home screen
                    launchSingleTop = true
                }
            }
        )
        NavigationBarItem(
            icon = { Icon(Icons.Filled.Add, contentDescription = "Add Recipe") },
            label = { Text("Add") },
            selected = currentRoute == Routes.AddRecipe.route,
            onClick = {
                navController.navigate(Routes.AddRecipe.route) {
                    launchSingleTop = true
                }
            }
        )
        NavigationBarItem(
            icon = { Icon(Icons.Filled.Settings, contentDescription = "Settings") },
            label = { Text("Settings") },
            selected = currentRoute == Routes.Settings.route, // Check if this is the current route
            onClick = {
                navController.navigate(Routes.Settings.route) { // Navigate to Settings
                    launchSingleTop = true
                }
            }
        )
    }
}
