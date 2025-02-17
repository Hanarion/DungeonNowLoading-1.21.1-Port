# Give and equip armor directly (use "item replace" instead of "replaceitem")
item replace entity @p armor.head with diamond_helmet{Unbreakable:1b}
item replace entity @p armor.chest with diamond_chestplate{Unbreakable:1b}
item replace entity @p armor.legs with diamond_leggings{Unbreakable:1b}
item replace entity @p armor.feet with diamond_boots{Unbreakable:1b}

# Give tools and weapons
give @p diamond_sword{Unbreakable:1b}
give @p diamond_pickaxe{Unbreakable:1b}
give @p diamond_axe{Unbreakable:1b}

# Give enchanted bow and an arrow
give @p bow{Unbreakable:1b,Enchantments:[{id:"minecraft:infinity",lvl:1},{id:"minecraft:power",lvl:3}]} 1
give @p arrow 1

# Give food
give @p cooked_beef 64

# Give blocks (Cobblestone and Oak Logs)
give @p cobblestone 64
give @p oak_log 64
