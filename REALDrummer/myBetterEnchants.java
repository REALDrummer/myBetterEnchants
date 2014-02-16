package REALDrummer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.Server;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.craftbukkit.v1_7_R1.block.CraftCreatureSpawner;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.enchantment.EnchantItemEvent;
import org.bukkit.event.enchantment.PrepareItemEnchantEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

public class myBetterEnchants extends JavaPlugin implements Listener {

    public static Server server;
    public static ConsoleCommandSender console;
    public static final ChatColor COLOR = ChatColor.DARK_AQUA;
    public static String[] magic_words = { "Sha-ZAM!", "ALAKAZAM!", "POOF!", "BOOM!", "KA-POW!", "Sha-FWAAAH!", "Kali-kaPOW!", "TORTELLINI!", "Kras-TOPHALEMOTZ!",
            "Wah-SHAM!", "Wa-ZAM!", "Wha-ZOO!", "KERFUFFLE!", "WOOOOWOWOWOWOW!", "CREAMPUFF WADLEEDEE!", "FLUFFENNUGGET!", "FALALALALAAAAAA-lala-la-LAAAA!",
            "SHNITZ-LIEDERHOSEN!", "BWAAAAAAAAAAAAH!", "FEE-FI-FO-FUM!", "ROTISSERIE!", "LALA-BIBIAY!", "Kurlaka-FWAH!" };
    // enchanting_players = new HashMap<player's username, new Object[] { new item's name, first item, second item, new item, total cost}>();
    public HashMap<String, Object[]> enchanting_players = new HashMap<String, Object[]>();
    public static ArrayList<String> debugging_players = new ArrayList<String>();

    // TODO: fix getting Fortune and Silk Touch off the table on the same item
    // TODO: combining books with the same enchantment and level makes a book with the original enchantment and the next level of that enchantment; fix it!
    // TODO: fix durability costs
    // TODO: add a factor to the cost based on preexisting enchantments on the first item
    // TODO: add info to WU to see how many of the base material it takes to make a given item (e.g. 2 diamonds for a diamond sword)

    @Override
    public void onEnable() {
        server = getServer();
        console = server.getConsoleSender();
        // register this class as a listener
        getServer().getPluginManager().registerEvents(this, this);
        // done enabling
        String[] enable_messages =
                { "The wizarding world has come to Minecraft!", "Sharpness AND Smite? No problemo!", "I majored in Minecraft at Hogwarts University!",
                        "You would need Sharpness XVI on a diamond sword to one-shot an Enderman!",
                        "It's too bad there's no enchantment that works specifically on Creepers! Those things killed my parents!" };
        MU.tellOps(COLOR + enable_messages[(int) (Math.random() * enable_messages.length)], true);
    }

    @Override
    public void onDisable() {
        // done disabling
        String[] disable_messages =
                { "Right. Back to the Forbidden Forest for me....", "Time to go back to see my mas--I mean...friend Lord Vo--I mean...Harry Potter!",
                        "All right. I have some potions to brew.", "Ye Olde Tavern calls my name. That's where you get the REALLY powerful potions, if you know what I mean.",
                        "I think I might read up on necrophilia. ...I MEAN...NECROMANCY! Yeah...that's what I meant...." };
        MU.tellOps(COLOR + disable_messages[(int) (Math.random() * disable_messages.length)], true);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String command, String[] parameters) {
        if (command.equalsIgnoreCase("anvil")) {
            if (enchanting_players.containsKey(sender.getName())) {
                enchanting_players.remove(sender.getName());
                sender.sendMessage(COLOR + "Very well, my leige. The deal has been cancelled.");
            } else if (sender instanceof Player && sender.hasPermission("mybetterenchants.anvil")) {
                enchanting_players.put(sender.getName(), new Object[] { AU.combine(parameters, " "), null, null, null, null });
                sender.sendMessage(COLOR
                        + "If you would simply give an anvil a good whack with the two items you want to combine, I would gladly combine them for you in return for some experience.\nUse "
                        + ChatColor.ITALIC + "/anvil " + COLOR + "again to cancel this deal.");
            } else if (!(sender instanceof Player))
                console.sendMessage(ChatColor.RED + "You're not allowed to use /anvil! How can you point out items you want to combine?");
            else
                sender.sendMessage(ChatColor.RED + "Sorry, but you don't have permission to use " + COLOR + "/anvil" + ChatColor.RED + ".");
            return true;
        } else if (command.equalsIgnoreCase("superenchant") || command.equalsIgnoreCase("super") && parameters.length > 0 && parameters[0].equalsIgnoreCase("enchant")
                || command.equalsIgnoreCase("sench") || command.equalsIgnoreCase("se")) {
            if (!(sender instanceof Player))
                console.sendMessage(ChatColor.RED + "You're not able to superenchant stuff! You're a console!");
            else if (!sender.hasPermission("mybetterenchants.superenchant"))
                sender.sendMessage(ChatColor.RED + "Sorry, but you don't have permission to use " + COLOR + "/superenchant" + ChatColor.RED + ".");
            else if (parameters.length == 0)
                sender.sendMessage(ChatColor.RED + "You forgot to tell me what enchantments you want to add!");
            else {
                byte extra_param = 0;
                if (parameters[0].equalsIgnoreCase("enchant"))
                    extra_param++;
                superEnchant((Player) sender, AU.combine(parameters, " ", extra_param).split(","));
            }
            return true;
        } else if (command.equalsIgnoreCase("enchant") || command.equalsIgnoreCase("ench") || command.equalsIgnoreCase("e")) {
            if (!(sender instanceof Player))
                console.sendMessage(ChatColor.RED + "You're not able to enchant stuff! You're a console!");
            else if (!sender.hasPermission("mybetterenchants.enchant"))
                sender.sendMessage(ChatColor.RED + "Sorry, but you don't have permission to use " + COLOR + "/enchant" + ChatColor.RED + ".");
            else if (parameters.length == 0)
                sender.sendMessage(ChatColor.RED + "You forgot to tell me what enchantments you want to add!");
            else
                enchant((Player) sender, AU.combine(parameters, " ").split(","));
            return true;
        } else if ((command.equalsIgnoreCase("mBE") || command.equalsIgnoreCase("myBetterEnchants")) && parameters.length > 0
                && parameters[0].toLowerCase().startsWith("debug")) {
            if (sender instanceof Player && !sender.hasPermission("mybetterenchants.admin"))
                if (command.equalsIgnoreCase("myBetterEnchants"))
                    sender.sendMessage(ChatColor.RED + "Sorry, but you don't have permission to use " + COLOR + "/myBetterEnchants debug" + ChatColor.RED + ".");
                else
                    sender.sendMessage(ChatColor.RED + "Sorry, but you don't have permission to use " + COLOR + "/mBE debug" + ChatColor.RED + ".");
            else {
                String sender_name = "console";
                if (sender instanceof Player)
                    sender_name = ((Player) sender).getName();
                if (debugging_players.contains(sender_name)) {
                    debugging_players.remove(sender_name);
                    sender.sendMessage(COLOR + "The terrifying Bugs of Java have been defeated!");
                } else {
                    debugging_players.add(sender_name);
                    sender.sendMessage(COLOR + "Black magic from the terrifying Bugs of Java is ruining my magics! We must stop them!");
                }
            }
            return true;
        }
        return false;
    }

    // intra-command methods
    public static void debug(String message) {
        if (debugging_players.size() == 0)
            return;
        if (debugging_players.contains("console")) {
            console.sendMessage(COLOR + message);
            if (debugging_players.size() == 1)
                return;
        }
        for (Player player : server.getOnlinePlayers())
            if (debugging_players.contains(player.getName()))
                player.sendMessage(COLOR + message);
    }

    // listeners
    @EventHandler
    public void makeTheFirstTwoLevelCostOptionsMoreReasonable(PrepareItemEnchantEvent event) {
        // the third cost option is the maximum level cost allowed by the enchanting table
        double half_max_cost = event.getExpLevelCostsOffered()[2] / 2.0;
        event.getExpLevelCostsOffered()[0] = (int) (Math.random() * half_max_cost);
        event.getExpLevelCostsOffered()[1] = (int) (Math.random() * half_max_cost + half_max_cost);
    }

    @EventHandler
    public void fixEnchanting(EnchantItemEvent event) {
        /* enchanted books are stupid and weird, so I can't make one and if I try to just modify the enchantments to add like usual, it causes crashes, so myBetterEnchants
         * can't handle enchanting books */
        if (event.getItem().getType() == Material.BOOK)
            return;

        // remove the old enchantments
        event.getEnchantsToAdd().clear();

        // calculate the number of enchantments that will be given
        double max_number_value = 3 / 29.0 * (event.getExpLevelCost() - 1.0);
        double number_value = Math.pow(Math.random() + 0.00393, -0.25) - 0.999 + max_number_value * event.getExpLevelCost() / 60;
        if (number_value > max_number_value)
            number_value = max_number_value;
        int number_of_enchantments = (int) (number_value + 0.999);
        if (number_of_enchantments == 0)
            number_of_enchantments++;

        // add the enchantments
        for (int i = 0; i < number_of_enchantments; i++) {
            // get random enchantments until we find one that works.
            Enchantment enchantment = null;
            // Make sure that...
            while (
            // ...the enchantment isn't null (which can happen if the randomized I.D. does not coordinate with an Enchantment),...
            enchantment == null
                    // ...the enchantment works on the item,...
                    || !enchantment.canEnchantItem(event.getItem())
                    // ...the item isn't already going to have that enchantment on it,...
                    || event.getEnchantsToAdd().containsKey(enchantment)
                    // ...and you can't get Fortune (LOOT_BONUS_BLOCKS) and Silk Touch on the same item (for obvious reasons).
                    || enchantment.equals(Enchantment.LOOT_BONUS_BLOCKS) && AU.contains(event.getEnchantsToAdd().keySet().toArray(), Enchantment.SILK_TOUCH)
                    || enchantment.equals(Enchantment.SILK_TOUCH) && AU.contains(event.getEnchantsToAdd().keySet().toArray(), Enchantment.LOOT_BONUS_BLOCKS)) {
                if (enchantment == null)
                    debug("rejected null enchantment");
                else
                    debug("rejected " + WU.getEnchantmentName(enchantment));
                enchantment = Enchantment.getById((int) (Math.random() * 63));
            }

            // calculate the level of the enchantment
            /* Through trial and error on a graphing calculator (because I wasn't smart enough to actually calculate it), I found the two values that I need for "a" and "b" in
             * the equation "y = (x+a)^1.25 - b" (where x is the level of the enchantment and y is a value between 0 and 1 that will be used to determine the level you get on
             * the enchantment using Math.random()) for enchantment with different maximum levels. */
            /* So, based on the max level of this enchantment, I will plug the appropriate values of a and b into this equation and use it to find the level you receive based
             * on the value of Math.random() (using the rearranged form of the equation "x = (y - b)^-0.8 - a"). */
            /* Once x (the level of the enchantment) is calculated, it will be rounded up to the nearest integer to get the level of the enchantment. */
            int level = 1;
            if (enchantment.getMaxLevel() > 1) {
                double a, b, max_level_value = enchantment.getMaxLevel() / 29.0 * (event.getExpLevelCost() - 1.0);
                // at cost=0, max_leve_value=0.0, but the max level value must be above 0 or the max level for the enchantment will be 0!
                if (max_level_value == 0.0)
                    max_level_value = 0.01;
                if (enchantment.getMaxLevel() == 5) {
                    a = 0.922;
                    b = -0.10825;
                } else if (enchantment.getMaxLevel() == 4) {
                    a = 0.903;
                    b = -0.13705;
                } else if (enchantment.getMaxLevel() == 3) {
                    a = 0.8745;
                    b = -0.18395;
                } else {
                    a = 0.8245;
                    b = -0.2731;
                }
                /* level value is the decimal value of the level that was calculated; it will be rounded up to the nearest integer to get the actual level, but first, we must
                 * ensure that it is less than or equal to the artificial ceiling created based on the level cost (known as max_level_value) */
                double level_value = Math.pow(Math.random() - b, -0.8) - a + max_level_value * event.getExpLevelCost() / 40 + 1;
                if (level_value > max_level_value)
                    level_value = max_level_value;
                level = (int) (level_value + 0.999);
            }

            // add the enchantment to the EnchantsToAdd list
            event.getEnchantsToAdd().put(enchantment, level);
            debug("added " + WU.getEnchantmentFullName(enchantment, level) + " to " + WU.getItemName(event.getItem(), false, true, true));
            ArrayList<String> enchantments_to_add = new ArrayList<String>();
            for (Enchantment _enchantment : event.getEnchantsToAdd().keySet())
                // TODO: should use WU's getEnchantmentFullName()
                enchantments_to_add.add(WU.getEnchantmentName(_enchantment));
            debug("enchantments to add: " + AU.writeArray(enchantments_to_add.toArray()));
        }
    }

    @EventHandler
    public void trackAnvilHits(PlayerInteractEvent event) {
        if (!enchanting_players.containsKey(event.getPlayer().getName()) || !event.getAction().equals(Action.LEFT_CLICK_BLOCK))
            return;
        if (event.getClickedBlock().getType() != Material.ANVIL) {
            event.getPlayer().sendMessage(ChatColor.RED + "Both sacrificial items must strike an anvil.");
            return;
        }
        event.setCancelled(true);
        Object[] items = enchanting_players.get(event.getPlayer().getName());
        if (items[1] == null) {
            enchanting_players.put(event.getPlayer().getName(), new Object[] { items[0], event.getPlayer().getItemInHand(), null, null, null });
            event.getPlayer().sendMessage(
                    COLOR + "Ah, a fine " + WU.getItemName(event.getPlayer().getItemInHand(), false, true, true)
                            + " for this spell.\n...and what would you like to combine it with?");
        } else if (items[2] == null) {
            // the cost will be in xp, not levels.
            // E.L.E.s are "Early Level Equivalents". E.L.E.s are equivalent to 17xp, which is how much xp it takes to get from one level to the next in the
            // first 16 levels. Note that costs are added in the number of levels that should be added *17; in this way, we are adding that number of E.L.E.s to
            // the cost.
            // The initial flat cost of using an anvil is 4 levels
            short cost = 4 * 17;
            ItemStack first_item, second_item, new_item;
            debug("Calculating combination cost...");
            // if the first item given is an enchanted book or can be used to repair the second item, switch the first and second items
            if (((ItemStack) items[1]).getType() == Material.ENCHANTED_BOOK || WU.isRepairableWith(event.getPlayer().getItemInHand(), (ItemStack) items[1])) {
                first_item = event.getPlayer().getItemInHand();
                second_item = (ItemStack) items[1];
            } else if (event.getPlayer().getItemInHand().getType() == Material.ENCHANTED_BOOK || WU.isRepairableWith((ItemStack) items[1], event.getPlayer().getItemInHand())
                    || ((ItemStack) items[1]).getType() == event.getPlayer().getItemInHand().getType()) {
                first_item = (ItemStack) items[1];
                second_item = event.getPlayer().getItemInHand();
            } else {
                event.getPlayer().sendMessage(
                        ChatColor.RED + "I cannot combine " + WU.getItemName((ItemStack) items[1], false, true, false) + " and "
                                + WU.getItemName(event.getPlayer().getItemInHand(), false, true, false) + "! They are not compatible items!");
                if (GU.getEnchantments(event.getPlayer().getItemInHand()).size() > 0) {
                    first_item = (ItemStack) items[1];
                    second_item = event.getPlayer().getItemInHand();
                    event.getPlayer().sendMessage(
                            COLOR + "...but... for a little extra experience, I could take all the enchantments on your "
                                    + WU.getItemName(event.getPlayer().getItemInHand(), false, true, true) + " and put them on your "
                                    + WU.getItemName((ItemStack) items[1], false, true, true)
                                    + ". It'll cost ye, though, and ye can't tell anyone. It'll have to be... off the books. It will also destroy the "
                                    + WU.getItemName((ItemStack) items[1], false, true, true) + ".");
                    debug("+5 for non-compatible items...");
                    cost += 5 * 17;
                } else {
                    enchanting_players.remove(event.getPlayer().getName());
                    return;
                }
            }
            final Map<Enchantment, Integer> first_item_enchantments = GU.getEnchantments(first_item), second_item_enchantments = GU.getEnchantments(second_item);
            new_item = first_item.clone();
            if (items[0] != null) {
                ItemMeta meta = new_item.getItemMeta();
                meta.setDisplayName((String) items[0]);
                new_item.setItemMeta(meta);
                debug("+7 E.L.E.s for name...");
                cost += 7 * 17;
            }
            // add enchantments from second_item
            if (second_item_enchantments.size() > 0)
                for (Enchantment enchantment : second_item_enchantments.keySet()) {
                    if (first_item_enchantments.containsKey(Enchantment.LOOT_BONUS_BLOCKS) && second_item_enchantments.containsKey(Enchantment.SILK_TOUCH)
                            || first_item_enchantments.containsKey(Enchantment.SILK_TOUCH) && second_item_enchantments.containsKey(Enchantment.LOOT_BONUS_BLOCKS)) {
                        event.getPlayer().sendMessage(
                                ChatColor.RED + "You can't put Fortune and Silk Touch on the same item! It would destroy the entire space-time continimubum!");
                        enchanting_players.remove(event.getPlayer().getName());
                        return;
                    } // if the first item doesn't already have this enchantment at a higher level, then prepare to add the enchantment to the first item
                    else if (!(first_item_enchantments.containsKey(enchantment) && first_item_enchantments.get(enchantment) > GU.getEnchantments(second_item).get(enchantment))) {
                        int enchantment_level = second_item_enchantments.get(enchantment);
                        if (first_item_enchantments.containsKey(enchantment) && first_item_enchantments.get(enchantment) == second_item_enchantments.get(enchantment))
                            if (enchantment_level != enchantment.getMaxLevel() * 2) {
                                debug("Combined " + WU.getEnchantmentFullName(enchantment, first_item_enchantments.get(enchantment)) + "s to "
                                        + WU.getEnchantmentFullName(enchantment, first_item_enchantments.get(enchantment) + 1) + "...");
                                enchantment_level++;
                            } else {
                                debug("Did not combine " + WU.getEnchantmentFullName(enchantment, first_item_enchantments.get(enchantment)) + "s; already max level...");
                                continue;
                            }
                        int level_cost = (int) (enchantment_level / enchantment.getMaxLevel() * Math.pow(1.25, enchantment_level) * 10);
                        cost += level_cost * 17;
                        debug("+" + level_cost + " E.L.E.s for adding " + WU.getEnchantmentFullName(enchantment, enchantment_level) + "...");
                        // if the first item already has a lower level of the enchantment being added, decrease the price accordingly
                        if (first_item_enchantments.containsKey(enchantment) && first_item_enchantments.get(enchantment) < second_item_enchantments.get(enchantment)) {
                            int level_cost_reduction =
                                    (int) (second_item_enchantments.get(enchantment) / enchantment.getMaxLevel() * Math.pow(1.25, second_item_enchantments.get(enchantment)) * 10);
                            debug("-" + level_cost_reduction + " E.L.E.s for already having " + WU.getEnchantmentName(enchantment) + " "
                                    + SU.writeRomanNumeral(second_item_enchantments.get(enchantment)) + "...");
                            cost -= level_cost_reduction * 17;
                            new_item.removeEnchantment(enchantment);
                        }
                        new_item.addUnsafeEnchantment(enchantment, enchantment_level);
                    }
                }
            // add durability
            if (new_item.getDurability() > 0) {
                Short new_durability = null;
                if (first_item.getType() == second_item.getType())
                    new_durability = (short) (first_item.getDurability() - (second_item.getType().getMaxDurability() - second_item.getDurability()));
                else if (WU.isRepairableWith(first_item, second_item))
                    // for repairing with raw materials, it will take 5 pieces of raw material to fully repair the item
                    new_durability = (short) (first_item.getDurability() - first_item.getType().getMaxDurability() * second_item.getAmount() / 5);
                if (new_durability != null) {
                    if (new_durability < 0)
                        new_durability = 0;
                    new_item.setDurability(new_durability);
                    // the cost for adding to the durability will be equal to the total of all the levels of all the enchantments on the new item *3 multiplied
                    // by the fraction of durability that is restored by the repair
                    int durability_cost = 4;
                    for (Enchantment enchantment : GU.getEnchantments(new_item).keySet())
                        durability_cost += GU.getEnchantments(new_item).get(enchantment);
                    durability_cost *= (first_item.getDurability() - new_durability) / new_item.getType().getMaxDurability() * 20;
                    debug("+" + durability_cost + " E.L.E.s for adding durability...");
                    cost += durability_cost * 17;
                }
            }
            if (event.getPlayer().getGameMode() == GameMode.CREATIVE) {
                ItemStack[] contents = event.getPlayer().getInventory().getContents();
                // we have to do two things: replace the first item with the new item and remove the second item; the success booleans below track the status of
                // these two actions
                boolean first_item_replaced = false, second_item_removed = false;
                for (int i = 0; i < contents.length; i++)
                    if (contents[i] != null)
                        if (!first_item_replaced && contents[i].equals(first_item)) {
                            contents[i] = new_item;
                            first_item_replaced = true;
                            if (second_item_removed)
                                break;
                        } else if (!second_item_removed && contents[i].equals(second_item)) {
                            short remaining_amount = 0;
                            // if the first item is being repaired with its base material (e.g. a diamond sword is being repaired with diamonds), make sure we
                            // only take the diamonds we need from the ItemStack to fully repair the sword or finish off the diamonds (whichever comes first).
                            // That way, if someone's second item is a full stack of diamonds, we don't use all the diamonds; we only use the ones needed to
                            // make the repairs
                            if (contents[i].getAmount() > 1 && WU.isRepairableWith((ItemStack) items[1], contents[i])) {
                                short durability = ((ItemStack) items[1]).getDurability();
                                remaining_amount = (short) contents[i].getAmount();
                                while (durability > 0 && remaining_amount > 0) {
                                    durability -= ((ItemStack) items[1]).getType().getMaxDurability() / 5;
                                    remaining_amount--;
                                }
                            }
                            if (remaining_amount == 0)
                                contents[i] = null;
                            else
                                contents[i].setAmount(remaining_amount);
                            second_item_removed = true;
                            if (first_item_replaced)
                                break;
                        }
                if (!first_item_replaced || !second_item_removed) {
                    event.getPlayer()
                            .sendMessage(
                                    ChatColor.RED
                                            + "Hey, you! What are you trying to pull, now? I need both of those items you wanted to combine. If you dropped them, please pick them up and try again.\nNow, are you willing to pay?");
                    return;
                }
                event.getPlayer().getInventory().setContents(contents);
                String first_item_name = WU.getItemName(first_item, false, true, true), second_item_name = WU.getItemName(second_item, false, true, true);
                if (first_item_name.equals(second_item_name))
                    second_item_name = "other " + second_item_name;
                enchanting_players.remove(event.getPlayer().getName());
                event.getPlayer().sendMessage(
                        ChatColor.GOLD + magic_words[(int) (Math.random() * magic_words.length)] + "! " + COLOR + "I have combined your " + first_item_name + " with your "
                                + second_item_name + " to create a brand new " + WU.getItemName(new_item, false, true, true) + "! Enjoy!");
            } else if (GU.calcLevel(cost) > event.getPlayer().getLevel()) {
                enchanting_players.remove(event.getPlayer().getName());
                event.getPlayer().sendMessage(
                        ChatColor.RED + "Oooh, I'm afraid this is no good.\nThis spell will cost you " + GU.calcLevel(cost)
                                + " levels, but you don't have enough to pay!\nCome back when you've done some more mining and mob killing!");
            } else {
                enchanting_players.put(event.getPlayer().getName(), new Object[] { items[0], items[1], event.getPlayer().getItemInHand(), new_item, GU.calcLevel(cost) });
                event.getPlayer().sendMessage(COLOR + "This spell will cost you " + GU.calcLevel(cost) + " levels. Are you willing to pay?");
            }
        } else {
            enchanting_players.put(event.getPlayer().getName(), new Object[] { items[0], event.getPlayer().getItemInHand(), null, null, null });
            event.getPlayer()
                    .sendMessage(
                            COLOR
                                    + "Oh. Are the two items you indicated before not the ones you wish to use now?\nVery well. Let us begin again.\nIf you would simply give an anvil a good whack with the two items you want to combine, I would gladly combine them for you. Keep in mind that this will still cost levels. Use "
                                    + ChatColor.ITALIC + "/anvil " + COLOR + "again to cancel this deal.");
        }
    }

    @EventHandler
    public void receiveAnswerToDoYouWantToCombineQuestions(AsyncPlayerChatEvent event) {
        // only check for question responses if the player is enchanting and they've already registered both items
        if (!enchanting_players.containsKey(event.getPlayer().getName()) || enchanting_players.get(event.getPlayer().getName())[2] == null)
            return;
        Boolean response = SU.readResponse(event.getMessage());
        if (response == null)
            return;
        event.setCancelled(true);
        if (response) {
            Object[] items = enchanting_players.get(event.getPlayer().getName());
            // re-confirm that the player has enough levels and subtract the cost
            if (event.getPlayer().getGameMode() != GameMode.CREATIVE)
                if (event.getPlayer().getLevel() >= (Integer) items[4])
                    event.getPlayer().setLevel(event.getPlayer().getLevel() - (Integer) items[4]);
                else {
                    event.getPlayer()
                            .sendMessage(
                                    ChatColor.RED
                                            + "My word! You have spent levels since I last saw you! You no longer have enough levels to pay for this service!\nPlease return when you have "
                                            + (Integer) items[4] + " levels again.");
                    enchanting_players.remove(event.getPlayer().getName());
                    return;
                }
            ItemStack[] contents = event.getPlayer().getInventory().getContents();
            // we have to do two things: replace the first item (items[1]) with the new item (items[3]) and remove the second item (items[2]).
            // The success booleans below track the status of these two actions
            boolean first_item_replaced = false, second_item_removed = false;
            for (int i = 0; i < contents.length; i++) {
                if (contents[i] != null)
                    if (!first_item_replaced && contents[i].equals((ItemStack) items[1])) {
                        contents[i] = (ItemStack) items[3];
                        first_item_replaced = true;
                        if (second_item_removed)
                            break;
                    } else if (!second_item_removed && contents[i].equals((ItemStack) items[2])) {
                        short remaining_amount = 0;
                        // if the first item is being repaired with its base material (e.g. a diamond sword is being repaired with diamonds), make sure we only
                        // take the diamonds we need from the ItemStack to fully repair the sword or finish off the diamonds (whichever comes first). That way,
                        // if someone's second item is a full stack of diamonds, we don't use all the diamonds; we only use the ones needed to make the repairs
                        if (contents[i].getAmount() > 1 && WU.isRepairableWith((ItemStack) items[1], contents[i])) {
                            short durability = ((ItemStack) items[1]).getDurability();
                            remaining_amount = (short) contents[i].getAmount();
                            while (durability > 0 && remaining_amount > 0) {
                                durability -= ((ItemStack) items[1]).getType().getMaxDurability() / 5;
                                remaining_amount--;
                            }
                        }
                        if (remaining_amount == 0)
                            contents[i] = null;
                        else
                            contents[i].setAmount(remaining_amount);
                        second_item_removed = true;
                        if (first_item_replaced)
                            break;
                    }
            }
            if (!first_item_replaced || !second_item_removed) {
                event.getPlayer()
                        .sendMessage(
                                ChatColor.RED
                                        + "Hey, you! What are you trying to pull, now? I need both of those items you wanted to combine. If you dropped them, please pick them up and try again.\nNow, are you willing to pay?");
                return;
            }
            event.getPlayer().getInventory().setContents(contents);
            String first_item_name = WU.getItemName((ItemStack) items[1], false, true, true), second_item_name = WU.getItemName((ItemStack) items[2], false, true, true);
            if (first_item_name.equals(second_item_name))
                second_item_name = "other " + second_item_name;
            event.getPlayer().sendMessage(
                    ChatColor.GOLD + magic_words[(int) (Math.random() * magic_words.length)] + "! " + COLOR + "I have combined your " + first_item_name + " with your "
                            + second_item_name + " to create a brand new " + WU.getItemName((ItemStack) items[3], false, true, true) + "! Enjoy!");
        } else
            event.getPlayer().sendMessage(COLOR + "Oh, very well. Come back any time if you change your mind.");
        enchanting_players.remove(event.getPlayer().getName());
    }

    @EventHandler
    public void makeMonsterSpawnersDropWithSilkTouch(BlockBreakEvent event) {
        try {
            if (event.getPlayer().hasPermission("mybetterenchants.spawners")
                    && event.getPlayer().getGameMode() != GameMode.CREATIVE
                    && event.getBlock().getType() == Material.MOB_SPAWNER
                    && (event.getPlayer().getItemInHand().getType() == Material.DIAMOND_PICKAXE || event.getPlayer().getItemInHand().getType() == Material.IRON_PICKAXE
                            || event.getPlayer().getItemInHand().getType() == Material.GOLD_PICKAXE || event.getPlayer().getItemInHand().getType() == Material.STONE_PICKAXE || event
                            .getPlayer().getItemInHand().getType() == Material.WOOD_PICKAXE) && event.getPlayer().getItemInHand().containsEnchantment(Enchantment.SILK_TOUCH)) {
                // make sure it doesn't drop xp; if it did, people could just place and break spawners and get xp for free
                event.setExpToDrop(0);
                EntityType type = ((CreatureSpawner) event.getBlock().getState()).getSpawnedType();
                String mob_name = WU.getEntityName(type, false, true, true);
                if (mob_name == null) {
                    if (!event.getPlayer().isOp())
                        event.getPlayer()
                                .sendMessage(
                                        ChatColor.DARK_RED
                                                + "Uh...sorry, but don't break that. There's something wrong. Tell your admins that myOpAids has a problem breaking monster spawners with Silk Touch.");
                    else
                        event.getPlayer().sendMessage(
                                ChatColor.DARK_RED + "Hold on just a second. This spawner...spawns something with the I.D. " + type.getTypeId() + ". ...What has the I.D. "
                                        + type.getTypeId() + "?");
                    event.setCancelled(true);
                    MU.tellOps(ChatColor.DARK_RED + "Someone tried to pick up a spawner that spawns something with the I.D. " + type.getTypeId()
                            + ", but I have no idea what has the I.D. " + type.getTypeId() + "! Is WU up to date?", true, event.getPlayer().getName());
                    return;
                }
                // construct the mob spawner item
                ItemStack item = new ItemStack(Material.MOB_SPAWNER, 1);
                // set the display name and the lore
                ItemMeta metadata = item.getItemMeta();
                ArrayList<String> lore = new ArrayList<String>();
                if (metadata.hasLore())
                    for (String lore_line : metadata.getLore())
                        lore.add(lore_line);
                lore.add(0, "I spawn " + WU.getEntityName(type, false, false, true) + ".");
                metadata.setLore(lore);
                metadata.setDisplayName(mob_name.substring(0, 1).toUpperCase() + mob_name.substring(1) + " Spawner");
                item.setItemMeta(metadata);
                // drop the monster spawner item
                event.getBlock().getWorld().dropItemNaturally(event.getBlock().getLocation(), item);
            }
        } catch (Exception exception) {
            event.setCancelled(true);
            event.getPlayer().sendMessage(ChatColor.DARK_RED + "There was a problem trying to get this spawner to break! Please let your admins know!");
            MU.err(this,
                    "There was an issue that occurred when " + event.getPlayer().getName() + " tried to break the spawner at " + SU.writeLocation(event.getBlock()) + ".",
                    exception);
        }
    }

    @EventHandler
    public void fixMonstersSpawnersWhenTheyArePlaced(BlockPlaceEvent event) {
        if (event.getPlayer().getItemInHand().getType() != Material.MOB_SPAWNER || !event.getPlayer().getItemInHand().getItemMeta().hasLore())
            return;
        try {
            String our_lore = event.getPlayer().getItemInHand().getItemMeta().getLore().get(0);
            Integer[] id_and_data = WU.getEntityIdAndData(our_lore.substring(8, our_lore.length() - 1));
            EntityType type = null;
            if (id_and_data != null)
                type = EntityType.fromId(id_and_data[0]);
            if (type == null) {
                event.setCancelled(true);
                if (!event.getPlayer().isOp())
                    event.getPlayer()
                            .sendMessage(
                                    ChatColor.DARK_RED
                                            + "Wait. ...Uh...don't place that. Sorry. I'm confused. Tell your admins that myOpAids has a problem placing monster spawners.");
                else
                    event.getPlayer().sendMessage(
                            ChatColor.DARK_RED + "Hold on just a second. This spawner...spawns something with the I.D. "
                                    + event.getPlayer().getItemInHand().getData().getData() + ". ...What has the I.D. "
                                    + event.getPlayer().getItemInHand().getData().getData() + "?");
                event.setCancelled(true);
                MU.tellOps(ChatColor.DARK_RED + "Someone tried to pick up a spawner that spawns something with the I.D. "
                        + event.getPlayer().getItemInHand().getData().getData() + ", but I have no idea what has the I.D. "
                        + event.getPlayer().getItemInHand().getData().getData() + "!", true, event.getPlayer().getName());
                return;
            }
            ((CraftCreatureSpawner) event.getBlock().getState()).setSpawnedType(type);
            event.getBlock().getState().update(true);
        } catch (Exception e) {
            event.setCancelled(true);
            event.getPlayer().sendMessage(
                    ChatColor.DARK_RED + "Whoa, whoa, whoa. Something went wrong. Sorry. Let your administrators know there's a problem when you see them next.");
            MU.err(this, "There was a problem when " + event.getPlayer().getName() + " tried to Silk Touch the spawner at " + SU.writeLocation(event.getBlock()) + ".", e);
        }
    }

    @EventHandler
    public void preventExcessWearingByArmorEnchants(EntityDamageEvent event) {
        // only pay attention to player damage
        if (event.getEntityType() == null || event.getEntityType() != EntityType.PLAYER)
            return;

        // get the player's name
        final String player_name = ((Player) event.getEntity()).getName();

        debug(player_name + " damaged; armor durabilities:");

        // retrieve the armor durability
        ItemStack[] armor = ((Player) event.getEntity()).getInventory().getArmorContents();
        final short[] old_durabilities = new short[4];
        final Material[] armor_types = new Material[4];
        final double damage = event.getDamage();
        for (byte i = 0; i < 4; i++) {
            old_durabilities[i] = armor[i].getDurability();
            armor_types[i] = armor[i].getType();
            debug(old_durabilities[i] + "/" + armor_types[i].getMaxDurability() + " (" + armor_types[i].name() + ")");
        }

        server.getScheduler().scheduleSyncDelayedTask(this, new Runnable() {

            @Override
            public void run() {
                debug("fixing durabilities...");

                // get the player
                Player player = server.getPlayerExact(player_name);
                if (player == null)
                    myBetterEnchants.debug(player_name + " not found");
                else {
                    // get the player's armor
                    ItemStack[] armor = player.getInventory().getArmorContents();

                    // determine the damage that should occur to the armor
                    byte intended_uses = 1;
                    if (damage == 0.0)
                        intended_uses = 0;
                    debug("damage = " + damage + "; intended uses = " + intended_uses);

                    // if the old items were broken, replace them if the old durability was greater than 1
                    for (byte i = 0; i < 4; i++)
                        if (armor[i] == null && old_durabilities[i] > intended_uses) {
                            debug(armor_types[i].name() + " broken w/ old durability " + old_durabilities[i] + "; replacing...");
                            armor[i] = new ItemStack(armor_types[i]);
                        }

                    // set the new durabilities
                    for (byte i = 0; i < 4; i++)
                        if (armor[i] != null && armor[i].getType().getMaxDurability() > old_durabilities[i] + intended_uses) {
                            // first, determine the new durability for this specific item in order to account for Unbreaking advantages
                            short new_durability = (short) (old_durabilities[i] + intended_uses);
                            debug("new durability: " + new_durability);
                            if (armor[i].containsEnchantment(Enchantment.DURABILITY)) {
                                debug("Unbreaking " + SU.writeRomanNumeral(armor[i].getEnchantmentLevel(Enchantment.DURABILITY)) + " found");
                                double random = Math.random();
                                if (random > (1.0 / ((double) armor[i].getEnchantmentLevel(Enchantment.DURABILITY) + 1.0))) {
                                    // cancel the use if random < 1/[Unbreaking level + 1]
                                    debug(random + " > 1/" + (armor[i].getEnchantmentLevel(Enchantment.DURABILITY) + 1) + "; intended uses = 0");
                                    new_durability = old_durabilities[i];
                                } else
                                    // otherwise, kep the intended uses constant
                                    debug(random + " > 1/" + (armor[i].getEnchantmentLevel(Enchantment.DURABILITY) + 1) + "; intended uses = " + intended_uses);
                            }

                            // set the new durability
                            armor[i].setDurability(new_durability);
                        }

                    // set it as the new armor
                    player.getInventory().setArmorContents(armor);

                    // display the new durabilities for debugging
                    for (ItemStack item : armor)
                        debug(item.getDurability() + "/" + item.getType().getMaxDurability() + " (" + item.getType().name() + ")");
                }
            }
        }, 0);
    }

    // command methods
    private void enchant(Player player, String[] parameters) {
        try {
            ArrayList<String> enchantments = new ArrayList<String>(), failed_enchantments = new ArrayList<String>(), conflicting_enchantments = new ArrayList<String>(), mislevelled_enchantments =
                    new ArrayList<String>(), wrong_item_enchantments = new ArrayList<String>();
            for (String parameter : parameters) {
                int level = 1, begin_level_index = parameter.length() - 1;
                // first, try finding the level by read numbers at the end of the parameter
                try {
                    while (begin_level_index > 0 && Integer.parseInt(parameter.substring(begin_level_index)) > 0)
                        begin_level_index--;
                } catch (NumberFormatException e) {
                    // terminate the loop if we get a NumberFormatException; it means we went backward past the level number
                }
                if (begin_level_index < parameter.length() - 1) {
                    level = Integer.parseInt(parameter.substring(begin_level_index + 1));
                    debug("got level by reading number...");
                } else {
                    // if there are no true numbers at the beginning of the enchantment name, look for roman numerals instead
                    begin_level_index = parameter.length() - 1;
                    while (begin_level_index > 0 && SU.readRomanNumeral(parameter.substring(begin_level_index)) > 0)
                        begin_level_index--;
                    if (begin_level_index < parameter.length() - 1) {
                        level = SU.readRomanNumeral(parameter.substring(begin_level_index + 1));
                        debug("got level by reading roman numeral...");
                    }
                }
                Enchantment enchantment = WU.getEnchantment(parameter.substring(0, begin_level_index + 1));
                debug("enchantment name: \"" + parameter.substring(0, begin_level_index + 1) + "\"");
                if (enchantment == null)
                    failed_enchantments.add("\"" + parameter.substring(0, begin_level_index + 1) + "\"");
                // prevent enchantments above the normal max level
                else if (enchantment.getMaxLevel() < level)
                    mislevelled_enchantments.add(WU.getEnchantmentName(enchantment) + " can only go up to level " + SU.writeRomanNumeral(enchantment.getMaxLevel()) + ", not "
                            + SU.writeRomanNumeral(level));
                // prevent enchantments that don't work on the item given
                else if (!enchantment.canEnchantItem(player.getItemInHand()))
                    wrong_item_enchantments.add(WU.getEnchantmentName(enchantment));
                else {
                    // prevent conflicting enchantments from being added to the same item
                    boolean conflicts = false;
                    for (Enchantment present_enchantment : player.getItemInHand().getEnchantments().keySet())
                        if (present_enchantment.conflictsWith(enchantment)) {
                            conflicting_enchantments.add(WU.getEnchantmentName(enchantment) + " conflicts with " + WU.getEnchantmentName(present_enchantment));
                            conflicts = true;
                            break;
                        }
                    if (conflicts)
                        continue;
                    // finally, if everything checks out, add the enchantment to the successful enchants list
                    player.getItemInHand().addEnchantment(enchantment, level);
                    enchantments.add(ChatColor.GRAY + WU.getEnchantmentFullName(enchantment, level));
                }
            }
            if (enchantments.size() > 0)
                player.sendMessage(ChatColor.GOLD + magic_words[(int) (Math.random() * magic_words.length)] + "! " + ChatColor.DARK_AQUA + "I enchanted your "
                        + WU.getItemName(player.getItemInHand(), false, true, true) + " with " + AU.writeArrayList(enchantments) + ChatColor.DARK_AQUA + ".");
            if (failed_enchantments.size() > 0) {
                String beginning = "What in the blazes ";
                if (enchantments.size() > 0)
                    beginning = "...but...what in the blazes ";
                if (failed_enchantments.size() > 1)
                    beginning += "are";
                else
                    beginning += "is";
                player.sendMessage(ChatColor.RED + beginning + " " + AU.writeArrayList(failed_enchantments) + "?");
            }
            if (mislevelled_enchantments.size() > 0) {
                String beginning = "Unfortunately,";
                if (failed_enchantments.size() > 0)
                    beginning = "...and, unfortunately, ";
                else if (enchantments.size() > 0)
                    beginning = "...but, unfortunately,";
                player.sendMessage(ChatColor.RED + beginning + " " + AU.writeArrayList(mislevelled_enchantments, "; ") + ".");
            }
            if (wrong_item_enchantments.size() > 0) {
                String beginning = "Sorry, but";
                if (failed_enchantments.size() > 0 || mislevelled_enchantments.size() > 0)
                    beginning = "Also,";
                else if (enchantments.size() > 0)
                    beginning = "However,";
                player.sendMessage(ChatColor.RED + beginning + " " + AU.writeArrayList(wrong_item_enchantments) + " can't be put on "
                        + WU.getItemName(player.getItemInHand(), false, true, false) + ".");
            }
            if (conflicting_enchantments.size() > 0) {
                String beginning = "Yeah, about that... you see,";
                if (failed_enchantments.size() > 0 || mislevelled_enchantments.size() > 0 || wrong_item_enchantments.size() > 0)
                    beginning = "Oh, yeah, and";
                else if (enchantments.size() > 0)
                    beginning = "...but, um,";
                player.sendMessage(ChatColor.RED + beginning + " " + AU.writeArrayList(conflicting_enchantments) + ".");
            }
        } catch (Exception e) {
            MU.err(this, "There was a problem trying to enchant " + player.getName() + "'s " + WU.getItemName(player.getItemInHand(), false, true, true) + "!", e);
        }
    }

    private void superEnchant(Player player, String[] parameters) {
        try {
            ArrayList<String> enchantments = new ArrayList<String>(), failed_enchantments = new ArrayList<String>();
            for (String parameter : parameters) {
                int level = 1, begin_level_index = parameter.length() - 1;
                // first, try finding the level by read numbers at the end of the parameter
                try {
                    while (begin_level_index > 0 && Integer.parseInt(parameter.substring(begin_level_index)) > 0)
                        begin_level_index--;
                } catch (NumberFormatException e) {
                    // terminate the loop if we get a NumberFormatException; it means we went backward past the level number
                }
                if (begin_level_index < parameter.length() - 1) {
                    level = Integer.parseInt(parameter.substring(begin_level_index + 1));
                    debug("got level by reading number...");
                } else {
                    // if there are no true numbers at the beginning of the enchantment name, look for roman numerals instead
                    begin_level_index = parameter.length() - 1;
                    while (begin_level_index > 0 && SU.readRomanNumeral(parameter.substring(begin_level_index)) > 0)
                        begin_level_index--;
                    if (begin_level_index < parameter.length() - 1) {
                        level = SU.readRomanNumeral(parameter.substring(begin_level_index + 1));
                        debug("got level by reading roman numeral...");
                    }
                }
                Enchantment enchantment = WU.getEnchantment(parameter.substring(0, begin_level_index + 1));
                debug("enchantment name: \"" + parameter.substring(0, begin_level_index + 1) + "\"");
                if (enchantment == null)
                    failed_enchantments.add("\"" + parameter.substring(0, begin_level_index + 1) + "\"");
                else {
                    player.getItemInHand().addUnsafeEnchantment(enchantment, level);
                    enchantments.add(ChatColor.GRAY + WU.getEnchantmentFullName(enchantment, level));
                }
            }
            if (enchantments.size() > 0)
                player.sendMessage(ChatColor.GOLD + magic_words[(int) (Math.random() * magic_words.length)] + "! " + ChatColor.DARK_AQUA + "I enchanted your "
                        + WU.getItemName(player.getItemInHand(), false, true, true) + " with " + AU.writeArray(enchantments.toArray()) + ChatColor.DARK_AQUA + ".");
            if (failed_enchantments.size() > 0) {
                String beginning = "What in the blazes ";
                if (enchantments.size() > 0)
                    beginning = "...but...what in the blazes ";
                if (failed_enchantments.size() > 1)
                    beginning += "are";
                else
                    beginning += "is";
                player.sendMessage(ChatColor.RED + beginning + " " + AU.writeArray(failed_enchantments.toArray()) + "?");
            }
        } catch (Exception e) {
            MU.err(this, "There was a problem trying to enchant " + player.getName() + "'s " + WU.getItemName(player.getItemInHand(), false, true, true) + "!", e);
        }
    }
}
