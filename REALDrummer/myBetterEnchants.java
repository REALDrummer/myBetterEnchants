package REALDrummer;

import java.util.HashMap;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

public class myBetterEnchants extends JavaPlugin implements Listener {

	public static ConsoleCommandSender console;
	public HashMap<String, ItemStack> enchanting_players = new HashMap<String, ItemStack>();

	public void onEnable() {
		console = getServer().getConsoleSender();
		// register this class as a listener
		getServer().getPluginManager().registerEvents(this, this);
	}

	public void onDisable() {

	}

	public boolean onCommand(CommandSender sender, Command cmd, String command, String[] parameters) {
		if (command.equalsIgnoreCase("anvil")) {
			if (enchanting_players.containsKey(sender.getName())) {
				enchanting_players.remove(sender.getName());
				sender.sendMessage(ChatColor.DARK_AQUA + "Very well, my leige. The process has been cancelled.");
			} else if (sender instanceof Player && sender.hasPermission("mybetterenchants.anvil"))
				if (((Player) sender).getLevel() >= 50) {
					enchanting_players.put(sender.getName(), null);
					sender.sendMessage(ChatColor.DARK_AQUA
							+ "If you would simply give an anvil a whack with the two items you want to combine, I would gladly combine them for you. This process will cost you 50 levels. Use /anvil again to cancel this process.");
				} else
					sender.sendMessage(ChatColor.RED + "Sorry, but you need 50 levels to use this super special command! Go kill more monsters!");
			else if (!(sender instanceof Player))
				console.sendMessage(ChatColor.RED + "You're not allowed to superenchant stuff! How can you point out items you want to superenchant?");
			else
				sender.sendMessage(ChatColor.RED + "Sorry, but you don't have permission to use " + ChatColor.DARK_AQUA + "/anvil" + ChatColor.RED + ".");
			return true;
		}
		return false;
	}

	// the listener
	@EventHandler
	public void trackAnvilHits(PlayerInteractEvent event) {
		if (!enchanting_players.containsKey(event.getPlayer().getName()) || !event.getAction().equals(Action.LEFT_CLICK_BLOCK))
			return;
		// anvil = 145
		if (event.getClickedBlock().getTypeId() != 145) {
			event.getPlayer().sendMessage(ChatColor.RED + "Pardon me, but if you would like to enchant two powerful items, you must hit an anvil with each one.");
			return;
		}
		event.setCancelled(true);
		if (event.getPlayer().getLevel() < 50) {
			event.getPlayer().sendMessage(
					ChatColor.RED + "Huh? Oh, well...I'm sorry, " + event.getPlayer().getName()
							+ ", but you need 50 levels to perform this smithing job. I'm gonna have to cancel this process. Please try again when you have 50 levels.");
			enchanting_players.remove(event.getPlayer().getName());
			return;
		}
		ItemStack first_item = enchanting_players.get(event.getPlayer().getName());
		if (first_item != null) {
			// combine the items
			ItemStack second_item = event.getPlayer().getItemInHand();
			// enchanted book = 403
			if (second_item.getTypeId() != first_item.getTypeId() && second_item.getTypeId() != 403) {
				event.getPlayer()
						.sendMessage(
								ChatColor.RED
										+ "My sincerest apologies, but I'm afraid that at the moment, I can only combine the same kind of items or items with enchanted books. I'm afraid I'll have to cancel the process. If you you would like to combine two of the same item, I would be happy to do so. Just use /anvil again when you require my services!");
				enchanting_players.remove(event.getPlayer().getName());
				return;
			}
			// first, get a list of all the first item's enchantments
			HashMap<Enchantment, Integer> enchantments = new HashMap<Enchantment, Integer>();
			for (Enchantment ench : first_item.getEnchantments().keySet())
				enchantments.put(ench, first_item.getEnchantments().get(ench));
			// then, compare all the enchantments on the second item
			HashMap<Enchantment, Integer> second_item_enchantments = new HashMap<Enchantment, Integer>();
			// the only way I can find to get the Enchantment on an enchanted book is to use the toString{} method and read the part where it tells you the
			// enchantment in words like this: "[...]stored-enchants={[enchantment name]=[enchantment level]}[...]"
			// .getEnchantments() doesn't work on enchanted books
			if (second_item.getTypeId() == 403) {
				String enchantment_name = second_item.toString().substring(second_item.toString().indexOf("stored-enchants={") + 17);
				enchantment_name = enchantment_name.substring(0, enchantment_name.indexOf("}"));
				int level = 0;
				try {
					level = Integer.parseInt(enchantment_name.split("=")[1]);
				} catch (NumberFormatException exception) {
					event.getPlayer().sendMessage(
							ChatColor.DARK_RED + "Something went wrong getting the enchantment level on that book! I beg of you...tell the admin you saw this message!");
					return;
				}
				Enchantment enchantment = Enchantment.getByName(enchantment_name.split("=")[0]);
				if (enchantment != null)
					second_item_enchantments.put(enchantment, level);
				else {
					event.getPlayer().sendMessage(ChatColor.DARK_RED + "Something went wrong! Please, oh please, tell the admin you saw this message!");
					return;
				}
			} else {
				// if it's not an enchanted book, just get the enchantments normally
				for (Enchantment ench : second_item.getEnchantments().keySet())
					second_item_enchantments.put(ench, second_item.getEnchantmentLevel(ench));
			}
			for (Enchantment enchantment2 : second_item_enchantments.keySet()) {
				int level2 = second_item_enchantments.get(enchantment2);
				// if the second item has an enchantments that the first item didn't have at all and it doesn't conflict with any other enchantments the first
				// item has, add it to the list
				if (!enchantments.containsKey(enchantment2)) {
					boolean conflicts = false;
					for (Enchantment enchantment : enchantments.keySet())
						if (enchantment.conflictsWith(enchantment2)) {
							conflicts = true;
							break;
						}
					if (!conflicts)
						enchantments.put(enchantment2, level2);
				}
				// if both items have the same kind of enchantment at the same level and they're not maxed out, add 1 to the final level of that enchantment
				else if (enchantments.containsKey(enchantment2) && level2 == enchantments.get(enchantment2) && enchantment2.getMaxLevel() > level2)
					enchantments.put(enchantment2, level2 + 1);
			}
			// combine the durabilities
			if (second_item.getTypeId() != 403) {
				short durability = (short) (first_item.getDurability() - (second_item.getType().getMaxDurability() - second_item.getDurability()));
				if (durability < 0)
					durability = 0;
				first_item.setDurability(durability);
			}
			// remove old enchantments
			for (Enchantment ench : first_item.getEnchantments().keySet())
				first_item.removeEnchantment(ench);
			// add the new enchantments
			first_item.addUnsafeEnchantments(enchantments);
			// remove the two items from the player's inventory
			event.getPlayer().getInventory().removeItem(first_item);
			event.getPlayer().getInventory().removeItem(second_item);
			// finish up
			event.getPlayer().getInventory().addItem(first_item);
			event.getPlayer().setLevel(event.getPlayer().getLevel() - 50);
			event.getPlayer().sendMessage(ChatColor.DARK_AQUA + "Here you are. Your tool is better than ever! Enjoy.");
			enchanting_players.remove(event.getPlayer().getName());
		} else if (event.getPlayer().getItemInHand().getTypeId() == 403)
			event.getPlayer().sendMessage(
					ChatColor.RED + "If you want to combine an item with an enchanted book, I'm afraid you must hit the anvil with the item first and the book second.");
		else {
			enchanting_players.put(event.getPlayer().getName(), event.getPlayer().getItemInHand());
			event.getPlayer().sendMessage(ChatColor.DARK_AQUA + "The first item has been registered for the job. The second?");
		}
	}
}
