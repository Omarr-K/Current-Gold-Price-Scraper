# Gold Price Scraper - Omar's Gold Exchange

## Overview

My family recently began investing in gold, sparking my interest in creating a simple tool to help track the real-time value of gold and determine how much their investment is worth based on current rates. I developed Omar's Gold Exchange using JavaFX and SceneBuilder, which provides a live feed of gold prices and allows the user to calculate the value of their holdings in various currencies.

## Project Description:

Omar's Gold Exchange is a JavaFX application that:

1. Scrapes the current price of gold per gram in multiple currencies.

2. Allows users to input the amount of gold they own (in grams).

3. Shows the equivalent value of their holdings in the chosen currency (USD, AED, or CAD).

This tool utilizes the Jsoup library for web scraping reliable price exchange sources specific to each region and employs a scheduled task to refresh gold prices every 5 minutes, ensuring that the data remains up-to-date.

## Features:

- **Real-time Gold Price Fetching**: Retrieves current gold prices in USD, AED, and CAD.

- **Currency Selection**: Allows users to choose a preferred currency.

- **Investment Value Calculation**: Users can input the weight of their gold holdings in grams to see their value in the selected currency.

- **Automatic Refresh**: Fetches updated prices every 5 minutes.

- **User-Friendly Interface**: Built with JavaFX and designed in SceneBuilder for a clean and accessible UI.

## Tech Stack:

- **JavaFX** for the graphical interface.

- **SceneBuilder** for creating the layout.

- **Jsoup** for web scraping.

## Installation and Setup:

### Prerequisites

- Java 8 or above

- JavaFX SDK

- SceneBuilder (for layout design)

## How It Works:

Gold prices are fetched from three sources:

- USD prices from CNBC

- AED prices from Dubai City of Gold

- USD to CAD conversion rate from XE Currency.

The data is retrieved in the background every 5 minutes to avoid freezing the user interface.

## User Inputs:

Users can input the weight of their gold holdings and select a currency (USD, AED, CAD).

Upon pressing "Calculate," the program multiplies the input weight by the current price per gram in the selected currency.
