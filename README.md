# Assignment Five – Q1: What’s for Dinner?

## Overview
**What’s for Dinner?** is a **multi-screen recipe app** built with **Jetpack Compose Navigation**, **ViewModel**, and **state management**.  
Users can **browse**, **view**, and **add recipes**, with navigation handled using **NavHostController**, **sealed route classes**, and **controlled backstack behavior**.

---

## Features
- 🍽️ **Home Screen** – Displays a list of recipes using `LazyColumn`  
- 📖 **Detail Screen** – Shows recipe details (title, ingredients, steps) via **route arguments**  
- ✏️ **Add Recipe Screen** – Form for entering a new recipe with **basic state management**  
- 🧭 **Navigation Architecture:**
  - `NavHost` and `NavHostController` with a **sealed `Routes` class**
  - Argument passing with `navigate(route + "/{id}")` and retrieval via `backStackEntry`
  - `popUpTo()` and `launchSingleTop` to prevent duplicate destinations
- 💾 **ViewModel** – Stores recipes in in-memory state to persist during configuration changes  
- 🧩 **Bottom Navigation Bar** – Tabs for **Home**, **Add**, and **Settings**  
- 🎨 **UI** – Uses **Scaffold** for consistent structure and Material 3 components  

---

## How It Works
1. **Home Screen**  
   Displays all recipes. Selecting one navigates to the **Detail Screen** with the recipe’s ID as a route argument.

2. **Detail Screen**  
   Fetches and displays recipe data (title, ingredients, and steps) from the ViewModel using the provided ID.

3. **Add Recipe Screen**  
   Lets users input and save a new recipe. After adding, the navigation uses `popUpTo("home")` to return to the Home screen while keeping the backstack clean.

4. **Settings (Placeholder)**  
   Demonstrates bottom navigation and future extensibility.

---

## How to Run and Use
```bash
- git clone https://github.com/cangokmen/CS501-AssignmentFiveQ1
# Open in Android Studio and run on an emulator or device
```
# Usage flow
- Home tab: scroll the recipe list and tap to view details
- Add tab: enter title, ingredients, steps; tap Save; returns to Home with updated list
- Detail: review the full recipe
- Settings tab: placeholder
- Recipes persist in memory during config changes via ViewModel

# AI assistance documentation

I created Recipe, Routes, and RecipeViewModel manually. I then asked Gemini to create the rest. It had a gradle issue that I fixed. It created the rest perfectly other than the settings tab. I customized the UIs, and at the end asked Gemini to create the README according to instructions. It generated all the README other than this paragraph. . 
