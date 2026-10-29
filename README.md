![Studbook Logo](src/main/resources/studbook_logo.png)

# Studbook Mod

 Minecraft mod for tracking lineage of your animals

## Features

- **Dynamic genealogical trees** - GUI with interactive node graph showcasing entity's ancestry and offspring
- **Data tracking** - Server records entity's name, state, player who bred it, time of birth and death and any additional information that can be customized with JSON configs
- **Datapack support** - JSON configs for entity types can determine which NBT data to show and how it should be displayed. Those can be tuned either though the config folder, or server/world can override those using datapacks.
- **Mod support** - This mod potentially can work with any entity inherited from AgeableMob class, and supports some mods out of the box, including ones that have pregnancy mechanics and entities with genders

## Compatibility

### Supported mods
- **Jade** by snownee
- **Quark** by Vazkii
- **Realistic Horse Genetics** by sekelsta
- **DragN's Livestock Overhaul** by DragN0007
- **DragN's Livestock: Pets Addon** by DragN0007
- **Genetic Animals** by mokiyoki

### Versions

This mod is currently for Forge only, Minecraft v1.20.1

## Overview

To quickly get started with a mod, just take two animal entities, name at least one of them using Name Tag (configurable: you can disable this requirement in common config) and breed them! Once the baby animal is born, you can grab paper in a main hand and right-click on a born baby or one of the parents. Lineage GUI will show up with genealogical tree viewport and details panel.

![Lineage Screen Screenshot](/other/screenshot_lineage_screen.png)

By the left, targeted entity/node information will be shown. It includes name, status, gender, breeder's player name, birth time (configurable), and any additional information in a scroll box depending on entity's type. On the right lineage viewport shows records linked to targeted entity via node graph. You can pan it and zoom in and out, and select other nodes as well to preview additional information.

Horse stats attributes are retrieved by the mod automatically, and display in a user-friendly units. Their values are highlighted with colored text depending on the quality: red for low, yellow for average and green for good.

## Configuration

**Common Config** - controls entity tracking rules and can be changed in _/config/studbook-common.toml_

**Client Config** - config controls display settings for GUI and can be changed in _/config/studbook-client.toml_

Additional data displayed in details panel scrollbox in lineage GUI is driven by datapacks. Mod generates default datapack dynamically and uses that in worlds unless the world/server overrides this data. JSON source files which used for default datapack are generated from files in _/config/studbook/datapack_.

