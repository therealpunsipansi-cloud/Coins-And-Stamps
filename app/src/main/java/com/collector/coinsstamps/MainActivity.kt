package com.collector.coinsstamps

import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import coil.compose.rememberAsyncImagePainter
import com.collector.coinsstamps.data.ItemEntity
import com.collector.coinsstamps.ui.theme.CoinsStampsCollectorTheme
import com.collector.coinsstamps.viewmodel.CollectorViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            CoinsStampsCollectorTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MainApp()
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainApp() {
    val navController = rememberNavController()
    val viewModel: CollectorViewModel = viewModel()

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    Scaffold(
        bottomBar = {
            if (currentRoute == "coins" || currentRoute == "stamps" || currentRoute == "stats") {
                NavigationBar {
                    NavigationBarItem(
                        icon = { Icon(Icons.Default.Star, contentDescription = "Coins") },
                        label = { Text("Coins") },
                        selected = currentRoute == "coins",
                        onClick = {
                            navController.navigate("coins") {
                                popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                    NavigationBarItem(
                        icon = { Icon(Icons.Default.Email, contentDescription = "Stamps") },
                        label = { Text("Stamps") },
                        selected = currentRoute == "stamps",
                        onClick = {
                            navController.navigate("stamps") {
                                popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                    NavigationBarItem(
                        icon = { Icon(Icons.Default.Info, contentDescription = "Stats") },
                        label = { Text("Stats") },
                        selected = currentRoute == "stats",
                        onClick = {
                            navController.navigate("stats") {
                                popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
            }
        },
        floatingActionButton = {
            if (currentRoute == "coins" || currentRoute == "stamps") {
                FloatingActionButton(onClick = {
                    navController.navigate("addEdit?itemId=-1")
                }) {
                    Icon(Icons.Default.Add, contentDescription = "Add Item")
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = "coins",
            modifier = Modifier.padding(innerPadding)
        ) {
            composable("coins") {
                CategoryListScreen(
                    category = "Coin",
                    viewModel = viewModel,
                    onItemClick = { id -> navController.navigate("addEdit?itemId=$id") }
                )
            }
            composable("stamps") {
                CategoryListScreen(
                    category = "Stamp",
                    viewModel = viewModel,
                    onItemClick = { id -> navController.navigate("addEdit?itemId=$id") }
                )
            }
            composable("stats") {
                StatsScreen(viewModel = viewModel)
            }
            composable(
                route = "addEdit?itemId={itemId}",
                arguments = listOf(navArgument("itemId") { type = NavType.LongType; defaultValue = -1L })
            ) { backStackEntry ->
                val itemId = backStackEntry.arguments?.getLong("itemId") ?: -1L
                AddEditScreen(
                    itemId = itemId,
                    viewModel = viewModel,
                    onBack = { navController.popBackStack() }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryListScreen(
    category: String,
    viewModel: CollectorViewModel,
    onItemClick: (Long) -> Unit
) {
    val allItems by (if (category == "Coin") viewModel.coins else viewModel.stamps).collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()

    val displayedItems = remember(allItems, searchQuery) {
        if (searchQuery.isBlank()) allItems
        else allItems.filter {
            it.title.contains(searchQuery, ignoreCase = true) ||
            it.country.contains(searchQuery, ignoreCase = true)
        }
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text(
            text = if (category == "Coin") "Coin Collection" else "Stamp Collection",
            style = MaterialTheme.typography.headlineMedium
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { viewModel.setSearchQuery(it) },
            label = { Text("Search by name or country") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) }
        )
        Spacer(modifier = Modifier.height(16.dp))

        if (displayedItems.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No items found.")
            }
        } else {
            LazyColumn(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(displayedItems, key = { it.id }) { item ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onItemClick(item.id) },
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (!item.photoUri.isNullOrEmpty()) {
                                Image(
                                    painter = rememberAsyncImagePainter(Uri.parse(item.photoUri)),
                                    contentDescription = null,
                                    modifier = Modifier.size(60.dp),
                                    contentScale = ContentScale.Crop
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Text(text = item.title, style = MaterialTheme.typography.titleMedium)
                                Text(text = "${item.country} (${item.year}) - ${item.denomination}", style = MaterialTheme.typography.bodyMedium)
                                Text(text = "Value: $${item.estimatedValue} | Grade: ${item.condition}", style = MaterialTheme.typography.bodySmall)
                            }
                            IconButton(onClick = { viewModel.delete(item) }) {
                                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun StatsScreen(viewModel: CollectorViewModel) {
    val coins by viewModel.coins.collectAsState()
    val stamps by viewModel.stamps.collectAsState()

    val totalCoins = coins.size
    val totalStamps = stamps.size
    val totalCoinValue = coins.sumOf { it.estimatedValue }
    val totalStampValue = stamps.sumOf { it.estimatedValue }
    val totalValue = totalCoinValue + totalStampValue

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(text = "Collection Statistics", style = MaterialTheme.typography.headlineMedium)

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(text = "Total Coins: $totalCoins", style = MaterialTheme.typography.bodyLarge)
                Text(text = "Total Stamps: $totalStamps", style = MaterialTheme.typography.bodyLarge)
                Divider()
                Text(text = "Total Estimated Value: $${String.format("%.2f", totalValue)}", style = MaterialTheme.typography.titleLarge)
            }
        }
    }
}

@Composable
fun AddEditScreen(
    itemId: Long,
    viewModel: CollectorViewModel,
    onBack: () -> Unit
) {
    var title by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Coin") }
    var country by remember { mutableStateOf("") }
    var year by remember { mutableStateOf("") }
    var denomination by remember { mutableStateOf("") }
    var condition by remember { mutableStateOf("") }
    var estimatedValueStr by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var photoUri by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(itemId) {
        if (itemId != -1L) {
            viewModel.getItem(itemId)?.let { item ->
                title = item.title
                category = item.category
                country = item.country
                year = item.year
                denomination = item.denomination
                condition = item.condition
                estimatedValueStr = item.estimatedValue.toString()
                notes = item.notes
                photoUri = item.photoUri
            }
        }
    }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        photoUri = uri?.toString()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = if (itemId == -1L) "Add Item" else "Edit Item",
            style = MaterialTheme.typography.headlineMedium
        )

        OutlinedTextField(
            value = title,
            onValueChange = { title = it },
            label = { Text("Title") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Category:")
            RadioButton(selected = category == "Coin", onClick = { category = "Coin" })
            Text("Coin")
            RadioButton(selected = category == "Stamp", onClick = { category = "Stamp" })
            Text("Stamp")
        }

        OutlinedTextField(
            value = country,
            onValueChange = { country = it },
            label = { Text("Country") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
                value = year,
                onValueChange = { year = it },
                label = { Text("Year") },
                modifier = Modifier.weight(1f),
                singleLine = true
            )
            OutlinedTextField(
                value = denomination,
                onValueChange = { denomination = it },
                label = { Text("Denomination") },
                modifier = Modifier.weight(1f),
                singleLine = true
            )
        }

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
                value = condition,
                onValueChange = { condition = it },
                label = { Text("Condition / Grade") },
                modifier = Modifier.weight(1f),
                singleLine = true
            )
            OutlinedTextField(
                value = estimatedValueStr,
                onValueChange = { estimatedValueStr = it },
                label = { Text("Estimated Value ($)") },
                modifier = Modifier.weight(1f),
                singleLine = true
            )
        }

        OutlinedTextField(
            value = notes,
            onValueChange = { notes = it },
            label = { Text("Notes") },
            modifier = Modifier.fillMaxWidth()
        )

        Button(
            onClick = {
                photoPickerLauncher.launch(
                    androidx.activity.result.contract.ActivityResultContracts.PickVisualMediaRequest(
                        ActivityResultContracts.PickVisualMedia.ImageOnly
                    )
                )
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(if (photoUri == null) "Select Photo from Gallery" else "Change Photo")
        }

        Button(
            onClick = {
                val value = estimatedValueStr.toDoubleOrNull() ?: 0.0
                val newItem = ItemEntity(
                    id = if (itemId == -1L) 0L else itemId,
                    title = title,
                    category = category,
                    country = country,
                    year = year,
                    denomination = denomination,
                    condition = condition,
                    estimatedValue = value,
                    notes = notes,
                    photoUri = photoUri
                )
                if (itemId == -1L) {
                    viewModel.insert(newItem)
                } else {
                    viewModel.update(newItem)
                }
                onBack()
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Save")
        }
    }
}
