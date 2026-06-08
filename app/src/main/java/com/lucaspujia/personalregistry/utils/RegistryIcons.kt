package com.lucaspujia.personalregistry.utils

import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DirectionsBike
import androidx.compose.material.icons.automirrored.filled.DirectionsRun
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Brush
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Computer
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Fastfood
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Flight
import androidx.compose.material.icons.filled.HistoryEdu
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Hotel
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Medication
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Nature
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.Scale
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.SelfImprovement
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Store
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lucaspujia.personalregistry.R

enum class IconCategory(val labelRes: Int) {
    ALL(R.string.category_all),
    HEALTH(R.string.category_health),
    FINANCE(R.string.category_finance),
    LIFESTYLE(R.string.category_lifestyle),
    WORK(R.string.category_work)
}

data class RegistryIconInfo(
    val name: String,
    val vector: ImageVector,
    val category: IconCategory
)

object RegistryIcons {
    val icons = listOf(
        // Salud
        RegistryIconInfo("Scale", Icons.Default.Scale, IconCategory.HEALTH),
        RegistryIconInfo("Run", Icons.AutoMirrored.Filled.DirectionsRun, IconCategory.HEALTH),
        RegistryIconInfo("Water", Icons.Default.WaterDrop, IconCategory.HEALTH),
        RegistryIconInfo("Health", Icons.Default.Favorite, IconCategory.HEALTH),
        RegistryIconInfo("Mental", Icons.Default.SelfImprovement, IconCategory.HEALTH),
        RegistryIconInfo("Sleep", Icons.Default.Hotel, IconCategory.HEALTH),
        RegistryIconInfo("Bike", Icons.AutoMirrored.Filled.DirectionsBike, IconCategory.HEALTH),
        RegistryIconInfo("Medication", Icons.Default.Medication, IconCategory.HEALTH),
        
        // Finanzas
        RegistryIconInfo("Money", Icons.Default.AttachMoney, IconCategory.FINANCE),
        RegistryIconInfo("Trending", Icons.AutoMirrored.Filled.TrendingUp, IconCategory.FINANCE),
        RegistryIconInfo("Shopping", Icons.Default.ShoppingCart, IconCategory.FINANCE),
        RegistryIconInfo("Savings", Icons.Default.Savings, IconCategory.FINANCE),
        RegistryIconInfo("CreditCard", Icons.Default.CreditCard, IconCategory.FINANCE),
        RegistryIconInfo("Store", Icons.Default.Store, IconCategory.FINANCE),
        
        // Estilo de vida
        RegistryIconInfo("Food", Icons.Default.Restaurant, IconCategory.LIFESTYLE),
        RegistryIconInfo("FastFood", Icons.Default.Fastfood, IconCategory.LIFESTYLE),
        RegistryIconInfo("Pets", Icons.Default.Pets, IconCategory.LIFESTYLE),
        RegistryIconInfo("Home", Icons.Default.Home, IconCategory.LIFESTYLE),
        RegistryIconInfo("Music", Icons.Default.MusicNote, IconCategory.LIFESTYLE),
        RegistryIconInfo("Movie", Icons.Default.Movie, IconCategory.LIFESTYLE),
        RegistryIconInfo("Games", Icons.Default.SportsEsports, IconCategory.LIFESTYLE),
        RegistryIconInfo("Camera", Icons.Default.PhotoCamera, IconCategory.LIFESTYLE),
        RegistryIconInfo("Brush", Icons.Default.Brush, IconCategory.LIFESTYLE),
        RegistryIconInfo("Travel", Icons.Default.Flight, IconCategory.LIFESTYLE),
        RegistryIconInfo("Car", Icons.Default.DirectionsCar, IconCategory.LIFESTYLE),
        
        // Trabajo / Educación
        RegistryIconInfo("Work", Icons.Default.Work, IconCategory.WORK),
        RegistryIconInfo("Idea", Icons.Default.Lightbulb, IconCategory.WORK),
        RegistryIconInfo("Book", Icons.Default.Book, IconCategory.WORK),
        RegistryIconInfo("Code", Icons.Default.Code, IconCategory.WORK),
        RegistryIconInfo("Computer", Icons.Default.Computer, IconCategory.WORK),
        RegistryIconInfo("School", Icons.Default.School, IconCategory.WORK),
        RegistryIconInfo("History", Icons.Default.HistoryEdu, IconCategory.WORK),
        RegistryIconInfo("Build", Icons.Default.Build, IconCategory.WORK),
        
        // Otros
        RegistryIconInfo("Goal", Icons.Default.Star, IconCategory.LIFESTYLE),
        RegistryIconInfo("Calendar", Icons.Default.CalendarMonth, IconCategory.LIFESTYLE),
        RegistryIconInfo("Person", Icons.Default.Person, IconCategory.LIFESTYLE),
        RegistryIconInfo("Nature", Icons.Default.Nature, IconCategory.LIFESTYLE),
        RegistryIconInfo("Cloud", Icons.Default.Cloud, IconCategory.LIFESTYLE),
        RegistryIconInfo("Visibility", Icons.Default.Visibility, IconCategory.LIFESTYLE)
    )

    private val iconMap = icons.associate { it.name to it.vector }

    fun getIcon(name: String): ImageVector? = iconMap[name]
}

@Composable
fun RegistryIcon(
    iconIdentifier: String,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    tint: Color = LocalContentColor.current,
    textSize: TextUnit = 20.sp
) {
    val vector = RegistryIcons.getIcon(iconIdentifier)
    if (vector != null) {
        Icon(
            imageVector = vector,
            contentDescription = contentDescription,
            modifier = modifier.size(textSize.value.dp),
            tint = tint
        )
    } else {
        Text(
            text = iconIdentifier,
            modifier = modifier,
            color = tint,
            fontSize = textSize
        )
    }
}
