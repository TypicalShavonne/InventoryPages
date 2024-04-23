![logo](https://i.imgur.com/d9eYvao.png)

Forked from a project made by KevinNovak, InventoryPagesRecoded has been added more features, fixed bugs, and reorganize most of classes from the code. If you're a developer, I kindly request that you contribute via pull requests rather than creating numerous forks. Let's ensure updates are accessible to all!

# Description
As simple as its name suggests, this plugin gives players on the server additional inventory pages, as well as the ability to customize their inventory pages.

# What's new?
- Supporting multiple versions from 1.12.x to 1.20.x
- Fixed bugs from the original version
- Easier to manage config, message, and inventories, they are now separated
- Automatically updating files if there is a new update. 
- Adding more config and inventory options
- Supporting MySQL database
- Supporting PlaceholderAPI
- Adding debug
- Adding backup
- Bringing "max page" to the database instead of using permission
- Supporting Hex Color

# System requirements
This software runs on [Spigot](https://www.spigotmc.org/) and NMS.
Spigot forks without compiled NMS code are not supported.
Officially supported servers are [spigot](https://www.spigotmc.org/) and [paper](https://papermc.io/).
It is required to use [**Java 11**](https://www.oracle.com/java/technologies/javase/jdk11-archive-downloads.html) or newer.

# Soft-depend plugins
You might need these plugins to utilize my plugin resources totally.
- [PlaceholderAPI](https://www.spigotmc.org/resources/placeholderapi.6245/)
	-   **%inventorypagesrecoded_page%** - Get the number of players' current showing page
	-   **%inventorypagesrecoded_maxpage%** - Get the player's max page number

 # Command
 - **/inventorypagessrecoded** - Admin command
 - **/clear** - Clear all the items in the current showing page
 - **/clear all** - Clear all the items in all pages

# Permission
- **inventorypagesrecoded.admin** - Can use the command /inventorypagesrecoded
- **inventorypagesrecoded.clear** - Can use the command /clear
- **inventorypagesrecoded.clear.all** - Can use the sub-command /clear all

These permissions below will not work if **inventory-settings.keep-inventory** is true in config.yml
- **inventorypagesrecoded.keep.unopened** - When the player dies, keep unopened pages, and drop items in the current opening page
- **inventorypagesrecoded.keep.hotbar** - When the player dies, Keep all items in the hotbar	
- **inventorypagesrecoded.keep.all** - Keep all items when player die

# Contact

[![Discord Server](https://discord.com/api/guilds/1187827789664096267/widget.png?style=banner3)](https://discord.gg/XdJfN2X)


# 3rd party libraries
- [JetBrains Java Annotations](https://mvnrepository.com/artifact/org.jetbrains/annotations)
- [ConfigUpdater](https://github.com/tchristofferson/Config-Updater)
- [XSeries](https://github.com/CryptoMorin/XSeries)
- [NBTEditor](https://github.com/BananaPuncher714/NBTEditor)
- [Gson](https://github.com/google/gson)

# Special Thanks To
[<img src="https://user-images.githubusercontent.com/21148213/121807008-8ffc6700-cc52-11eb-96a7-2f6f260f8fda.png" alt="" width="150">](https://www.jetbrains.com)

Jetbrains supports InventoryPagesRecoded with their [Open Source Licenses](https://www.jetbrains.com/opensource/).
