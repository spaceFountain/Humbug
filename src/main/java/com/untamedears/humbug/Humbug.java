package com.untamedears.humbug;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.World.Environment;
import org.bukkit.block.Biome;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EnderPearl;
import org.bukkit.entity.Enderman;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Horse;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Minecart;
import org.bukkit.entity.Player;
import org.bukkit.entity.Skeleton;
import org.bukkit.entity.Skeleton.SkeletonType;
import org.bukkit.entity.Vehicle;
import org.bukkit.entity.minecart.HopperMinecart;
import org.bukkit.entity.minecart.StorageMinecart;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockFromToEvent;
import org.bukkit.event.block.BlockPhysicsEvent;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.enchantment.EnchantItemEvent;
import org.bukkit.event.enchantment.PrepareItemEnchantEvent;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.entity.EntityCreatePortalEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.entity.ExpBottleEvent;
import org.bukkit.event.entity.ItemSpawnEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.entity.PotionSplashEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.entity.SheepDyeWoolEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.player.PlayerExpChangeEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.bukkit.event.vehicle.VehicleDestroyEvent;
import org.bukkit.event.vehicle.VehicleEnterEvent;
import org.bukkit.event.vehicle.VehicleExitEvent;
import org.bukkit.event.vehicle.VehicleMoveEvent;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.event.world.PortalCreateEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import com.trc202.CombatTag.CombatTag;
import com.trc202.CombatTagApi.CombatTagApi;
import com.untamedears.humbug.CombatTagManager;
import com.untamedears.humbug.Versioned;
import com.untamedears.humbug.annotations.BahHumbug;
import com.untamedears.humbug.annotations.BahHumbugs;
import com.untamedears.humbug.annotations.ConfigOption;
import com.untamedears.humbug.annotations.OptType;

import com.untamedears.humbug.Config;
import com.untamedears.humbug.CustomNMSItemEnderPearl;

public class Humbug extends JavaPlugin implements Listener {
  public static void severe(String message) {
    log_.severe("[Humbug] " + message);
  }

  public static void warning(String message) {
    log_.warning("[Humbug] " + message);
  }

  public static void info(String message) {
    log_.info("[Humbug] " + message);
  }

  public static void debug(String message) {
    if (config_.getDebug()) {
      log_.info("[Humbug] " + message);
    }
  }

  public static Humbug getPlugin() {
    return global_instance_;
  }

  private static final Logger log_ = Logger.getLogger("Humbug");
  private static Humbug global_instance_ = null;
  private static Config config_ = null;
  private static int max_golden_apple_stack_ = 1;

  static {
    max_golden_apple_stack_ = Material.GOLDEN_APPLE.getMaxStackSize();
    if (max_golden_apple_stack_ > 64) {
      max_golden_apple_stack_ = 64;
    }
  }

  private Random prng_ = new Random();
  private CombatTagManager combatTag_ = new CombatTagManager();
  private CombatTag combat= new CombatTag();
  private CombatTagApi combatapi= new CombatTagApi(combat);
  public Humbug() {}

  // ================================================
  // Reduce registered PlayerInteractEvent count. onPlayerInteractAll handles
  //  cancelled events.

  @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
  public void onPlayerInteract(PlayerInteractEvent event) {
    onAnvilOrEnderChestUse(event);
    if (!event.isCancelled()) {
      onCauldronInteract(event);
    }
    if (!event.isCancelled()) {
      onRecordInJukebox(event);
    }
  }

  @EventHandler(priority = EventPriority.LOWEST) // ignoreCancelled=false
  public void onPlayerInteractAll(PlayerInteractEvent event) {
    onPlayerEatGoldenApple(event);
    onPlayerPearlTeleport(event);
  }

  // ================================================
  // Stops people from dying sheep

  @BahHumbug(opt="allow_dye_sheep", def="true")
  @EventHandler
  public void onDyeWool(SheepDyeWoolEvent event) {
    if (!config_.get("allow_dye_sheep").getBool()) {
      event.setCancelled(true);
    }
  }

  // ================================================
  // Fixes Teleporting through walls and doors
  // ** and **
  // Ender Pearl Teleportation disabling

  @BahHumbugs({
    @BahHumbug(opt="ender_pearl_teleportation", def="true"),
    @BahHumbug(opt="ender_pearl_teleportation_throttled", def="true"),
    @BahHumbug(opt="fix_teleport_glitch", def="true")
  })
  @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
  public void onTeleport(PlayerTeleportEvent event) {
    TeleportCause cause = event.getCause();
    if (cause != TeleportCause.ENDER_PEARL) {
      return;
    } else if (!config_.get("ender_pearl_teleportation").getBool()) {
      event.setCancelled(true);
      return;
    }
    if (!config_.get("fix_teleport_glitch").getBool()) {
      return;
    }
    Location to = event.getTo();
    World world = to.getWorld();

    // From and To are feet positions.  Check and make sure we can teleport to a location with air
    // above the To location.
    Block toBlock = world.getBlockAt(to);
    Block aboveBlock = world.getBlockAt(to.getBlockX(), to.getBlockY()+1, to.getBlockZ());
    Block belowBlock = world.getBlockAt(to.getBlockX(), to.getBlockY()-1, to.getBlockZ());
    boolean lowerBlockBypass = false;
    double height = 0.0;
    switch( toBlock.getType() ) {
    case CHEST: // Probably never will get hit directly
    case ENDER_CHEST: // Probably never will get hit directly
      height = 0.875;
      break;
    case STEP:
      lowerBlockBypass = true;
      height = 0.5;
      break;
    case WATER_LILY:
      height = 0.016;
      break;
    case ENCHANTMENT_TABLE:
      lowerBlockBypass = true;
      height = 0.75;
      break;
    case BED:
    case BED_BLOCK:
      // This one is tricky, since even with a height offset of 2.5, it still glitches.
      //lowerBlockBypass = true;
      //height = 0.563;
      // Disabling teleporting on top of beds for now by leaving lowerBlockBypass false.
      break;
    case FLOWER_POT:
    case FLOWER_POT_ITEM:
      height = 0.375;
      break;
    case SKULL: // Probably never will get hit directly
      height = 0.5;
      break;
    default:
      break;
    }
    // Check if the below block is difficult
    // This is added because if you face downward directly on a gate, it will
    // teleport your feet INTO the gate, thus bypassing the gate until you leave that block.
    switch( belowBlock.getType() ) {
    case FENCE:
    case FENCE_GATE:
    case NETHER_FENCE:
    case COBBLE_WALL:
      height = 0.5;
      break;
    default:
      break;
    }

    boolean upperBlockBypass = false;
    if( height >= 0.5 ) {
      Block aboveHeadBlock = world.getBlockAt(aboveBlock.getX(), aboveBlock.getY()+1, aboveBlock.getZ());
      if( false == aboveHeadBlock.getType().isSolid() ) {
        height = 0.5;
      } else {
        upperBlockBypass = true; // Cancel this event.  What's happening is the user is going to get stuck due to the height.
      }
    }

    // Normalize teleport to the center of the block.  Feet ON the ground, plz.
    // Leave Yaw and Pitch alone
    to.setX(Math.floor(to.getX()) + 0.5000);
    to.setY(Math.floor(to.getY()) + height);
    to.setZ(Math.floor(to.getZ()) + 0.5000);

    if(aboveBlock.getType().isSolid() ||
       (toBlock.getType().isSolid() && !lowerBlockBypass) ||
       upperBlockBypass ) {
      // One last check because I care about Top Nether.  (someone build me a shrine up there)
      boolean bypass = false;
      if ((world.getEnvironment() == Environment.NETHER) &&
          (to.getBlockY() > 124) && (to.getBlockY() < 129)) {
        bypass = true;
      }
      if (!bypass) {
        event.setCancelled(true);
      }
    }
  }

  // ================================================
  // Villager Trading

  @BahHumbug(opt="villager_trades")
  @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
  public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
    if (config_.get("villager_trades").getBool()) {
      return;
    }
    Entity npc = event.getRightClicked();
    if (npc == null) {
        return;
    }
    if (npc.getType() == EntityType.VILLAGER) {
      event.setCancelled(true);
    }
  }

  // ================================================
  // Anvil and Ender Chest usage

  // EventHandler registered in onPlayerInteract
  @BahHumbugs({
    @BahHumbug(opt="anvil"),
    @BahHumbug(opt="ender_chest")
  })
  public void onAnvilOrEnderChestUse(PlayerInteractEvent event) {
    if (config_.get("anvil").getBool() && config_.get("ender_chest").getBool()) {
      return;
    }
    Action action = event.getAction();
    Material material = event.getClickedBlock().getType();
    boolean anvil = !config_.get("anvil").getBool() &&
                    action == Action.RIGHT_CLICK_BLOCK &&
                    material.equals(Material.ANVIL);
    boolean ender_chest = !config_.get("ender_chest").getBool() &&
                          action == Action.RIGHT_CLICK_BLOCK &&
                          material.equals(Material.ENDER_CHEST);
    if (anvil || ender_chest) {
      event.setCancelled(true);
    }
  }

  @BahHumbug(opt="ender_chests_placeable", def="true")
  @EventHandler(ignoreCancelled=true)
  public void onEnderChestPlace(BlockPlaceEvent e) {
    Material material = e.getBlock().getType();
    if (!config_.get("ender_chests_placeable").getBool() && material == Material.ENDER_CHEST) {
      e.setCancelled(true);
    }
  }

  public void EmptyEnderChest(HumanEntity human) {
    if (config_.get("ender_backpacks").getBool()) {
      dropInventory(human.getLocation(), human.getEnderChest());
    }
  }

  public void dropInventory(Location loc, Inventory inv) {
    final World world = loc.getWorld();
    final int end = inv.getSize();
    for (int i = 0; i < end; ++i) {
      try {
        final ItemStack item = inv.getItem(i);
        if (item != null) {
          world.dropItemNaturally(loc, item);
          inv.clear(i);
        }
      } catch (Exception ex) {}
    }
  }

  // ================================================
  // Unlimited Cauldron water

  // EventHandler registered in onPlayerInteract
  @BahHumbug(opt="unlimitedcauldron")
  public void onCauldronInteract(PlayerInteractEvent e) {
    if (!config_.get("unlimitedcauldron").getBool()) {
      return;
    }

    // block water going down on cauldrons
    if(e.getClickedBlock().getType() == Material.CAULDRON && e.getMaterial() == Material.GLASS_BOTTLE && e.getAction() == Action.RIGHT_CLICK_BLOCK)
    {
      Block block = e.getClickedBlock();
      if(block.getData() > 0)
      {
        block.setData((byte)(block.getData()+1));
      }
    }
  }

  // ================================================
  // Quartz from Gravel

  @BahHumbug(opt="quartz_gravel_percentage", type=OptType.Int)
  @EventHandler(ignoreCancelled=true, priority = EventPriority.HIGHEST)
  public void onGravelBreak(BlockBreakEvent e) {
    if(e.getBlock().getType() != Material.GRAVEL
        || config_.get("quartz_gravel_percentage").getInt() <= 0) {
      return;
    }

    if(prng_.nextInt(100) < config_.get("quartz_gravel_percentage").getInt())
    {
      e.setCancelled(true);
      e.getBlock().setType(Material.AIR);
      e.getBlock().getWorld().dropItemNaturally(e.getBlock().getLocation(), new ItemStack(Material.QUARTZ, 1));
    }
  }

  // ================================================
  // Portals

  @BahHumbug(opt="portalcreate", def="true")
  @EventHandler(ignoreCancelled=true)
  public void onPortalCreate(PortalCreateEvent e) {
    if (!config_.get("portalcreate").getBool()) {
      e.setCancelled(true);
    }
  }

  @EventHandler(ignoreCancelled=true)
  public void onEntityPortalCreate(EntityCreatePortalEvent e) {
    if (!config_.get("portalcreate").getBool()) {
      e.setCancelled(true);
    }
  }

  // ================================================
  // EnderDragon

  @BahHumbug(opt="enderdragon", def="true")
  @EventHandler(ignoreCancelled=true)
  public void onDragonSpawn(CreatureSpawnEvent e) {
    if (e.getEntityType() == EntityType.ENDER_DRAGON
        && !config_.get("enderdragon").getBool()) {
      e.setCancelled(true);
    }
  }

  // ================================================
  // Join/Quit/Kick messages

  @BahHumbug(opt="joinquitkick", def="true")
  @EventHandler(priority=EventPriority.HIGHEST)
  public void onJoin(PlayerJoinEvent e) {
    if (!config_.get("joinquitkick").getBool()) {
      e.setJoinMessage(null);
    }
  }

  @EventHandler(priority=EventPriority.HIGHEST)
  public void onQuit(PlayerQuitEvent e) {
    EmptyEnderChest(e.getPlayer());
    if (!config_.get("joinquitkick").getBool()) {
      e.setQuitMessage(null);
    }
  }

  @EventHandler(priority=EventPriority.HIGHEST)
  public void onKick(PlayerKickEvent e) {
    EmptyEnderChest(e.getPlayer());
    if (!config_.get("joinquitkick").getBool()) {
      e.setLeaveMessage(null);
    }
  }

  // ================================================
  // Death Messages
  @BahHumbugs({
    @BahHumbug(opt="deathannounce", def="true"),
    @BahHumbug(opt="deathlog"),
    @BahHumbug(opt="deathpersonal"),
    @BahHumbug(opt="deathred"),
    @BahHumbug(opt="ender_backpacks")
  })
  @EventHandler(priority=EventPriority.HIGHEST)
  public void onDeath(PlayerDeathEvent e) {
    final boolean logMsg = config_.get("deathlog").getBool();
    final boolean sendPersonal = config_.get("deathpersonal").getBool();
    final Player player = e.getEntity();
    EmptyEnderChest(player);
    if (logMsg || sendPersonal) {
      Location location = player.getLocation();
      String msg = String.format(
          "%s ([%s] %d, %d, %d)", e.getDeathMessage(), location.getWorld().getName(),
          location.getBlockX(), location.getBlockY(), location.getBlockZ());
      if (logMsg) {
        info(msg);
      }
      if (sendPersonal) {
        e.getEntity().sendMessage(ChatColor.RED + msg);
      }
    }
    if (!config_.get("deathannounce").getBool()) {
      e.setDeathMessage(null);
    } else if (config_.get("deathred").getBool()) {
      e.setDeathMessage(ChatColor.RED + e.getDeathMessage());
    }
  }

  // ================================================
  // Endermen Griefing

  @BahHumbug(opt="endergrief", def="true")
  @EventHandler(ignoreCancelled=true)
  public void onEndermanGrief(EntityChangeBlockEvent e)
  {
    if (!config_.get("endergrief").getBool() && e.getEntity() instanceof Enderman) {
      e.setCancelled(true);
    }
  }

  // ================================================
  // Wither Insta-breaking and Explosions

  @BahHumbug(opt="wither_insta_break")
  @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
  public void onEntityChangeBlock(EntityChangeBlockEvent event) {
    if (config_.get("wither_insta_break").getBool()) {
      return;
    }
    Entity npc = event.getEntity();
    if (npc == null) {
        return;
    }
    EntityType npc_type = npc.getType();
    if (npc_type.equals(EntityType.WITHER)) {
      event.setCancelled(true);
    }
  }

  @BahHumbug(opt="wither_explosions")
  @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
  public void onEntityExplode(EntityExplodeEvent event) {
    if (config_.get("wither_explosions").getBool()) {
      return;
    }
    Entity npc = event.getEntity();
    if (npc == null) {
        return;
    }
    EntityType npc_type = npc.getType();
    if ((npc_type.equals(EntityType.WITHER) ||
         npc_type.equals(EntityType.WITHER_SKULL))) {
      event.blockList().clear();
    }
  }

  @BahHumbug(opt="wither", def="true")
  @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
  public void onWitherSpawn(CreatureSpawnEvent event) {
    if (config_.get("wither").getBool()) {
      return;
    }
    if (!event.getEntityType().equals(EntityType.WITHER)) {
      return;
    }
    event.setCancelled(true);
  }

  // ================================================
  // Prevent specified items from dropping off mobs

  public void removeItemDrops(EntityDeathEvent event) {
    if (!config_.doRemoveItemDrops()) {
      return;
    }
    if (event.getEntity() instanceof Player) {
      return;
    }
    Set<Integer> remove_ids = config_.getRemoveItemDrops();
    List<ItemStack> drops = event.getDrops();
    ItemStack item;
    int i = drops.size() - 1;
    while (i >= 0) {
      item = drops.get(i);
      if (remove_ids.contains(item.getTypeId())) {
        drops.remove(i);
      }
      --i;
    }
  }

  // ================================================
  // Spawn more Wither Skeletons and Ghasts

  @BahHumbugs ({
    @BahHumbug(opt="extra_ghast_spawn_rate", type=OptType.Int),
    @BahHumbug(opt="extra_wither_skele_spawn_rate", type=OptType.Int),
    @BahHumbug(opt="portal_extra_ghast_spawn_rate", type=OptType.Int),
    @BahHumbug(opt="portal_extra_wither_skele_spawn_rate", type=OptType.Int),
    @BahHumbug(opt="portal_pig_spawn_multiplier", type=OptType.Int)
  })
  @EventHandler(ignoreCancelled=true)
  public void spawnMoreHellMonsters(CreatureSpawnEvent e) {
    final Location loc = e.getLocation();
    final World world = loc.getWorld();
    boolean portalSpawn = false;
    final int blockType = world.getBlockTypeIdAt(loc);
    if (blockType == 90 || blockType == 49) {
      // >= because we are preventing instead of spawning
      if(prng_.nextInt(1000000) >= config_.get("portal_pig_spawn_multiplier").getInt()) {
        e.setCancelled(true);
        return;
      }
      portalSpawn = true;
    }
    if (config_.get("extra_wither_skele_spawn_rate").getInt() <= 0
        && config_.get("extra_ghast_spawn_rate").getInt() <= 0) {
      return;
    }
    if (e.getEntityType() == EntityType.PIG_ZOMBIE) {
      int adjustedwither;
      int adjustedghast;
      if (portalSpawn) {
        adjustedwither = config_.get("portal_extra_wither_skele_spawn_rate").getInt();
        adjustedghast = config_.get("portal_extra_ghast_spawn_rate").getInt();
      } else {
        adjustedwither = config_.get("extra_wither_skele_spawn_rate").getInt();
        adjustedghast = config_.get("extra_ghast_spawn_rate").getInt();
      }
      if(prng_.nextInt(1000000) < adjustedwither) {
        e.setCancelled(true);
        world.spawnEntity(loc, EntityType.SKELETON);
      } else if(prng_.nextInt(1000000) < adjustedghast) {
        e.setCancelled(true);
        int x = loc.getBlockX();
        int z = loc.getBlockZ();
        List<Integer> heights = new ArrayList<Integer>(16);
        int lastBlockHeight = 2;
        int emptyCount = 0;
        int maxHeight = world.getMaxHeight();
        for (int y = 2; y < maxHeight; ++y) {
          Block block = world.getBlockAt(x, y, z);
          if (block.isEmpty()) {
            ++emptyCount;
            if (emptyCount == 11) {
              heights.add(lastBlockHeight + 2);
            }
          } else {
            lastBlockHeight = y;
            emptyCount = 0;
          }
        }
        if (heights.size() <= 0) {
          return;
        }
        loc.setY(heights.get(prng_.nextInt(heights.size())));
        world.spawnEntity(loc, EntityType.GHAST);
      }
    } else if (e.getEntityType() == EntityType.SKELETON
        && e.getSpawnReason() == CreatureSpawnEvent.SpawnReason.CUSTOM) {
      Entity entity = e.getEntity();
      if (entity instanceof Skeleton) {
        Skeleton skele = (Skeleton)entity;
        skele.setSkeletonType(SkeletonType.WITHER);
        EntityEquipment entity_equip = skele.getEquipment();
        entity_equip.setItemInHand(new ItemStack(Material.STONE_SWORD));
        entity_equip.setItemInHandDropChance(0.0F);
      }
    }
  }

  // ================================================
  // Wither Skull drop rate

  public static final int skull_id_ = Material.SKULL_ITEM.getId();
  public static final byte wither_skull_data_ = 1;

  @BahHumbug(opt="wither_skull_drop_rate", type=OptType.Int)
  public void adjustWitherSkulls(EntityDeathEvent event) {
    Entity entity = event.getEntity();
    if (!(entity instanceof Skeleton)) {
      return;
    }
    int rate = config_.get("wither_skull_drop_rate").getInt();
    if (rate < 0 || rate > 1000000) {
      return;
    }
    Skeleton skele = (Skeleton)entity;
    if (skele.getSkeletonType() != SkeletonType.WITHER) {
      return;
    }
    List<ItemStack> drops = event.getDrops();
    ItemStack item;
    int i = drops.size() - 1;
    while (i >= 0) {
      item = drops.get(i);
      if (item.getTypeId() == skull_id_
          && item.getData().getData() == wither_skull_data_) {
        drops.remove(i);
      }
      --i;
    }
    if (rate - prng_.nextInt(1000000) <= 0) {
      return;
    }
    item = new ItemStack(Material.SKULL_ITEM);
    item.setAmount(1);
    item.setDurability((short)wither_skull_data_);
    drops.add(item);
  }

  // ================================================
  // Generic mob drop rate adjustment

  public void adjustMobItemDrops(EntityDeathEvent event){
    Entity mob = event.getEntity();
    if (mob instanceof Player){
      return;
    }
    if (mob.getWorld() == Bukkit.getWorld("world_the_end")) {
      return;
    }
    // Try specific multiplier, if that doesn't exist use generic
    EntityType mob_type = mob.getType();
    int multiplier = config_.getLootMultiplier(mob_type.toString());
    if (multiplier == 1) {
      multiplier = config_.getLootMultiplier("generic");
    }
    for (ItemStack item : event.getDrops()) {
      int amount = item.getAmount() * multiplier;   
      item.setAmount(amount);
    }  
  }

  @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
  public void onEntityDeathEvent(EntityDeathEvent event) {
    removeItemDrops(event);
    adjustWitherSkulls(event);
    adjustMobItemDrops(event);
  }

  // ================================================
  // Enchanted Golden Apple

  public boolean isEnchantedGoldenApple(ItemStack item) {
    // Golden Apples are GOLDEN_APPLE with 0 durability
    // Enchanted Golden Apples are GOLDEN_APPLE with 1 durability
    if (item == null) {
      return false;
    }
    if (item.getDurability() != 1) {
      return false;
    }
    Material material = item.getType();
    return material.equals(Material.GOLDEN_APPLE);
  }

  public void replaceEnchantedGoldenApple(
      String player_name, ItemStack item, int inventory_max_stack_size) {
    if (!isEnchantedGoldenApple(item)) {
      return;
    }
    int stack_size = max_golden_apple_stack_;
    if (inventory_max_stack_size < max_golden_apple_stack_) {
      stack_size = inventory_max_stack_size;
    }
    info(String.format(
          "Replaced %d Enchanted with %d Normal Golden Apples for %s",
          item.getAmount(), stack_size, player_name));
    item.setDurability((short)0);
    item.setAmount(stack_size);
  }

  @BahHumbug(opt="ench_gold_app_craftable")
  public void removeRecipies() {
    if (config_.get("ench_gold_app_craftable").getBool()) {
      return;
    }
    Iterator<Recipe> it = getServer().recipeIterator();
    while (it.hasNext()) {
      Recipe recipe = it.next();
      ItemStack resulting_item = recipe.getResult();
      if ( // !ench_gold_app_craftable_ &&
          isEnchantedGoldenApple(resulting_item)) {
        it.remove();
        info("Enchanted Golden Apple Recipe disabled");
      }
    }
  }

  // EventHandler registered in onPlayerInteractAll
  @BahHumbug(opt="ench_gold_app_edible")
  public void onPlayerEatGoldenApple(PlayerInteractEvent event) {
    // The event when eating is cancelled before even LOWEST fires when the
    //  player clicks on AIR.
    if (config_.get("ench_gold_app_edible").getBool()) {
      return;
    }
    Player player = event.getPlayer();
    Inventory inventory = player.getInventory();
    ItemStack item = event.getItem();
    replaceEnchantedGoldenApple(
        player.getName(), item, inventory.getMaxStackSize());
  }

  // ================================================
  // Enchanted Book

  public boolean isNormalBook(ItemStack item) {
    if (item == null) {
      return false;
    }
    Material material = item.getType();
    return material.equals(Material.BOOK);
  }

  @BahHumbug(opt="ench_book_craftable")
  @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled=true)
  public void onPrepareItemEnchantEvent(PrepareItemEnchantEvent event) {
    if (config_.get("ench_book_craftable").getBool()) {
        return;
    }
    ItemStack item = event.getItem();
    if (isNormalBook(item)) {
      event.setCancelled(true);
    }
  }

  @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled=true)
  public void onEnchantItemEvent(EnchantItemEvent event) {
    if (config_.get("ench_book_craftable").getBool()) {
        return;
    }
    ItemStack item = event.getItem();
    if (isNormalBook(item)) {
      event.setCancelled(true);
      Player player = event.getEnchanter();
      warning(
          "Prevented book enchant. This should not trigger. Watch player " +
          player.getName());
    }
  }

  // ================================================
  // Stop Cobble generation from lava+water

  private static final BlockFace[] faces_ = new BlockFace[] {
      BlockFace.NORTH,
      BlockFace.SOUTH,
      BlockFace.EAST,
      BlockFace.WEST,
      BlockFace.UP,
      BlockFace.DOWN
    };


  private BlockFace WaterAdjacentLava(Block lava_block) {
    for (BlockFace face : faces_) {
      Block block = lava_block.getRelative(face);
      Material material = block.getType();
      if (material.equals(Material.WATER) ||
          material.equals(Material.STATIONARY_WATER)) {
        return face;
      }
    }
    return BlockFace.SELF;
  }

  public void ConvertLava(Block block) {
    int data = (int)block.getData();
    if (data == 0) {
      return;
    }
    Material material = block.getType();
    if (!material.equals(Material.LAVA) &&
        !material.equals(Material.STATIONARY_LAVA)) {
      return;
    }
    if (isLavaSourceNear(block, 3)) {
      return;
    }
    BlockFace face = WaterAdjacentLava(block);
    if (face == BlockFace.SELF) {
      return;
    }
    block.setType(Material.AIR);
  }

  public boolean isLavaSourceNear(Block block, int ttl) {
    int data = (int)block.getData();
    if (data == 0) {
      Material material = block.getType();
      if (material.equals(Material.LAVA) ||
          material.equals(Material.STATIONARY_LAVA)) {
        return true;
      }
    }
    if (ttl <= 0) {
      return false;
    }
    for (BlockFace face : faces_) {
      Block child = block.getRelative(face);
      if (isLavaSourceNear(child, ttl - 1)) {
        return true;
      }
    }
    return false;
  }

  public void LavaAreaCheck(Block block, int ttl) {
    ConvertLava(block);
    if (ttl <= 0) {
      return;
    }
    for (BlockFace face : faces_) {
      Block child = block.getRelative(face);
      LavaAreaCheck(child, ttl - 1);
    }
  }

  @BahHumbugs ({
    @BahHumbug(opt="cobble_from_lava"),
    @BahHumbug(opt="cobble_from_lava_scan_radius", type=OptType.Int, def="0")
  })
  @EventHandler(priority = EventPriority.LOWEST)
  public void onBlockPhysicsEvent(BlockPhysicsEvent event) {
    if (config_.get("cobble_from_lava").getBool()) {
      return;
    }
    Block block = event.getBlock();
    LavaAreaCheck(block, config_.get("cobble_from_lava_scan_radius").getInt());
  }

  // ================================================
  // Counteract 1.4.6 protection enchant nerf

  @BahHumbug(opt="scale_protection_enchant", def="true")
  @EventHandler(priority = EventPriority.LOWEST) // ignoreCancelled=false
  public void onEntityDamageByEntityEvent(EntityDamageByEntityEvent event) {
    if (!config_.get("scale_protection_enchant").getBool()) {
        return;
    }
    double damage = event.getDamage();
    if (damage <= 0.0000001D) {
      return;
    }
    DamageCause cause = event.getCause();
    if (!cause.equals(DamageCause.ENTITY_ATTACK) &&
            !cause.equals(DamageCause.PROJECTILE)) {
        return;
    }
    Entity entity = event.getEntity();
    if (!(entity instanceof Player)) {
      return;
    }
    Player defender = (Player)entity;
    PlayerInventory inventory = defender.getInventory();
    int enchant_level = 0;
    for (ItemStack armor : inventory.getArmorContents()) {
      enchant_level += armor.getEnchantmentLevel(Enchantment.PROTECTION_ENVIRONMENTAL);
    }
    int damage_adjustment = 0;
    if (enchant_level >= 3 && enchant_level <= 6) {
      // 0 to 2
      damage_adjustment = prng_.nextInt(3);
    } else if (enchant_level >= 7 && enchant_level <= 10) {
      // 0 to 3
      damage_adjustment = prng_.nextInt(4);
    } else if (enchant_level >= 11 && enchant_level <= 14) {
      // 1 to 4
      damage_adjustment = prng_.nextInt(4) + 1;
    } else if (enchant_level >= 15) {
      // 2 to 4
      damage_adjustment = prng_.nextInt(3) + 2;
    }
    damage = Math.max(damage - (double)damage_adjustment, 0.0D);
    event.setDamage(damage);
  }

  @BahHumbug(opt="player_max_health", type=OptType.Int, def="20")
  @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled=true)
  public void onPlayerJoinEvent(PlayerJoinEvent event) {
    Player player = event.getPlayer();
    player.setMaxHealth((double)config_.get("player_max_health").getInt());
  }

  // ================================================
  // Prevent entity dup bug
  // From https://github.com/intangir/EventBlocker

  @BahHumbug(opt="fix_rail_dup_bug", def="true")
  @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled=true)
  public void onPistonPushRail(BlockPistonExtendEvent e) {
    if (!config_.get("fix_rail_dup_bug").getBool()) {
      return;
    }
    for (Block b : e.getBlocks()) {
      Material t = b.getType();
      if (t == Material.RAILS ||
          t == Material.POWERED_RAIL ||
          t == Material.DETECTOR_RAIL) {
        e.setCancelled(true);
        return;
      }
    }
  }

  @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled=true)
  public void onRailPlace(BlockPlaceEvent e) {
    if (!config_.get("fix_rail_dup_bug").getBool()) {
      return;
    }
    Block b = e.getBlock();
    Material t = b.getType();
    if (t == Material.RAILS ||
        t == Material.POWERED_RAIL ||
        t == Material.DETECTOR_RAIL) {
      for (BlockFace face : faces_) {
        t = b.getRelative(face).getType();
        if (t == Material.PISTON_STICKY_BASE ||
            t == Material.PISTON_EXTENSION ||
            t == Material.PISTON_MOVING_PIECE ||
            t == Material.PISTON_BASE) {
          e.setCancelled(true);
          return;
        }
      }
    }
  }

  //================================================
  // Give introduction book to n00bs

  private Set<String> playersWithN00bBooks_ = new TreeSet<String>();
  
    @EventHandler(priority=EventPriority.HIGHEST)
    public void onPlayerDeathBookDrop(PlayerDeathEvent e) {
      final String playerName = e.getEntity().getName();
      List<ItemStack> dropList = e.getDrops();
      for (int i = 0; i < dropList.size(); ++i) {
        final ItemStack item = dropList.get(i);
        if (item.getType().equals(Material.WRITTEN_BOOK)) {
          final BookMeta bookMeta = (BookMeta)item.getItemMeta();
          if (bookMeta.getTitle().equals(config_.getTitle())) {
            playersWithN00bBooks_.add(playerName);
            dropList.remove(i);
            return;
          }
        }
      }
      playersWithN00bBooks_.remove(playerName);
    }
   
  @EventHandler
  public void onGiveBookOnRespawn(PlayerRespawnEvent event) {
    final Player player = event.getPlayer();
    final String playerName = player.getName();
	  if (!playersWithN00bBooks_.contains(playerName)) {
      return;
    }
	  playersWithN00bBooks_.remove(playerName);
    giveN00bBook(player);
  }

  @EventHandler
  public void onGiveBookOnJoin(PlayerJoinEvent event) {
    final Player player = event.getPlayer();
    final String playerName = player.getName();
	if (player.hasPlayedBefore() && !playersWithN00bBooks_.contains(playerName)) {
	         return;
	       }
		   playersWithN00bBooks_.remove(playerName);
	       giveN00bBook(player);
	     } 

  public void giveN00bBook(Player player) {
    Inventory inv = player.getInventory();
    inv.addItem(createN00bBook());
  }

  public ItemStack createN00bBook() {
    ItemStack book = new ItemStack(Material.WRITTEN_BOOK);
    BookMeta sbook = (BookMeta)book.getItemMeta();
    sbook.setTitle(config_.getTitle());
    sbook.setAuthor(config_.getAuthor());
    sbook.setPages(config_.getPages());
    book.setItemMeta(sbook);
    return book;
  }

  // ================================================
  // Playing records in jukeboxen? Gone

  // EventHandler registered in onPlayerInteract
  @BahHumbug(opt="disallow_record_playing", def="true")
  public void onRecordInJukebox(PlayerInteractEvent event) {
    if (!config_.get("disallow_record_playing").getBool()) {
      return;
    }
    Block cb = event.getClickedBlock();
    if (cb == null || cb.getType() != Material.JUKEBOX) {
      return;
    }
    ItemStack his = event.getItem();
    if(his != null && his.getType().isRecord()) {
      event.setCancelled(true);
    }
  }

  //=================================================
  // Water in the nether? Nope.

  @BahHumbugs ({
    @BahHumbug(opt="allow_water_in_nether"),
    @BahHumbug(opt="indestructible_end_portals", def="true")
  })
  @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
  public void onPlayerBucketEmptyEvent(PlayerBucketEmptyEvent e) {
    if(!config_.get("allow_water_in_nether").getBool()) {
      if( ( e.getBlockClicked().getBiome() == Biome.HELL )
          && ( e.getBucket() == Material.WATER_BUCKET ) ) {
        e.setCancelled(true);
        e.getItemStack().setType(Material.BUCKET);
        e.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.WATER_BREATHING, 5, 1));
      }
    }
    if (config_.get("indestructible_end_portals").getBool()) {
      Block baseBlock = e.getBlockClicked();
      BlockFace face = e.getBlockFace();
      Block block = baseBlock.getRelative(face);
      if (block.getType() == Material.ENDER_PORTAL) {
          e.setCancelled(true);
      }
    }
  }

  @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
  public void onBlockFromToEvent(BlockFromToEvent e) {
    if(!config_.get("allow_water_in_nether").getBool()) {
      if( e.getToBlock().getBiome() == Biome.HELL ) {
        if( ( e.getBlock().getType() == Material.WATER )
            || ( e.getBlock().getType() == Material.STATIONARY_WATER ) ) {
          e.setCancelled(true);
        }
      }
    }
    if (config_.get("indestructible_end_portals").getBool()) {
      if (e.getToBlock().getType() == Material.ENDER_PORTAL) {
          e.setCancelled(true);
      }
    }
  }

  //=================================================
  // Nerfs Strength Potions to Pre-1.6 Levels

  @BahHumbug(opt="nerf_strength", def="true")
  @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
  public void onPlayerDamage(EntityDamageByEntityEvent event) {
    if (!config_.get("nerf_strength").getBool()) {
      return;
    }
    if (!(event.getDamager() instanceof Player)) {
      return;
    }
    Player player = (Player)event.getDamager();
    if (player.hasPotionEffect(PotionEffectType.INCREASE_DAMAGE)) {
      for (PotionEffect effect : player.getActivePotionEffects()) {
        if (effect.getType().equals(PotionEffectType.INCREASE_DAMAGE)) {
          double damagePercentage = (effect.getAmplifier()) * 1.3D + 1.0D;
          double newDamage;
          if (event.getDamage() / damagePercentage <= 1.0D) {
            newDamage = (double)((effect.getAmplifier()) * 3 + 1);
          } else {
            newDamage = event.getDamage() / damagePercentage + (double)((effect.getAmplifier()) * 3);
          }
          event.setDamage(newDamage);
          break;
        }
      }
    }
  }

  //=================================================
  // Buffs health splash to pre-1.6 levels

  @BahHumbug(opt="buff_health_pots", def="true")
  @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
  public void onPotionSplash(PotionSplashEvent event) {
    if (!config_.get("buff_health_pots").getBool()) {
      return;
    }
    for (PotionEffect effect : event.getEntity().getEffects()) {
      if (!(effect.getType().getName().equalsIgnoreCase("heal"))) { // Splash potion of poison
        return;
      }
    }
    for (LivingEntity entity : event.getAffectedEntities()) {
      if (entity instanceof Player) {
        final double newHealth = Math.min(entity.getHealth() + 4.0D, entity.getMaxHealth());
        entity.setHealth(newHealth);
      }
    }
  }

  //=================================================
  // Bow shots cause slow debuff

  @BahHumbugs ({
    @BahHumbug(opt="projectile_slow_chance", type=OptType.Int, def="30"),
    @BahHumbug(opt="projectile_slow_ticks", type=OptType.Int, def="100")
  })
  @EventHandler
  public void onEDBE(EntityDamageByEntityEvent event) {
    int rate = config_.get("projectile_slow_chance").getInt();
    if (rate <= 0 || rate > 100) {
      return;
    }
    if (!(event.getEntity() instanceof Player)) {
      return;
    }
    boolean damager_is_player_arrow = false;
    int chance_scaling = 0;
    Entity damager_entity = event.getDamager();
    if (damager_entity != null) {
      // public LivingEntity CraftArrow.getShooter()
      // Playing this game to not have to take a hard dependency on
      //  craftbukkit internals.
      try {
        Class<?> damager_class = damager_entity.getClass();
        if (damager_class.getName().endsWith(".CraftArrow")) {
          Method getShooter = damager_class.getMethod("getShooter");
          Object result = getShooter.invoke(damager_entity);
          if (result instanceof Player) {
            damager_is_player_arrow = true;
            String player_name = ((Player)result).getName();
            if (bow_level_.containsKey(player_name)) {
              chance_scaling = bow_level_.get(player_name);
            }
          }
        }
      } catch(Exception ex) {}
    }
    if (!damager_is_player_arrow) {
      return;
    }
    rate += chance_scaling * 5;
    int percent = prng_.nextInt(100);
    if (percent < rate){
      int ticks = config_.get("projectile_slow_ticks").getInt();
      Player player = (Player)event.getEntity();
      player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, ticks, 1, false));
    }
  }

  // Used to track bow enchantment levels per player
  private Map<String, Integer> bow_level_ = new TreeMap<String, Integer>();

  @EventHandler
  public void onEntityShootBow(EntityShootBowEvent event) {
    if (!(event.getEntity() instanceof Player)) {
         return;
    }
    int ench_level = 0;
    ItemStack bow = event.getBow();
    Map<Enchantment, Integer> enchants = bow.getEnchantments();
    for (Enchantment ench : enchants.keySet()) {
      int tmp_ench_level = 0;
      if (ench.equals(Enchantment.KNOCKBACK) || ench.equals(Enchantment.ARROW_KNOCKBACK)) {
        tmp_ench_level = enchants.get(ench) * 2;
      } else if (ench.equals(Enchantment.ARROW_DAMAGE)) {
        tmp_ench_level = enchants.get(ench);
      }
      if (tmp_ench_level > ench_level) {
        ench_level = tmp_ench_level;
      }
    }
    bow_level_.put(
        ((Player)event.getEntity()).getName(),
        ench_level);
  }

  // ================================================
  // Ender pearl cooldown timer

  private class PearlTeleportInfo {
    public long last_teleport;
  }

  private Map<String, PearlTeleportInfo> pearl_teleport_info_
      = new TreeMap<String, PearlTeleportInfo>();

  // EventHandler registered in onPlayerInteractAll
  public void onPlayerPearlTeleport(PlayerInteractEvent event) {
    if (!config_.get("ender_pearl_teleportation_throttled").getBool()) {
      return;
    }
    if (event.getItem() == null || !event.getItem().getType().equals(Material.ENDER_PEARL) || event.getClickedBlock() == null) {
      return;
    }
    Action action = event.getAction();
    if (action != Action.RIGHT_CLICK_AIR && action != Action.RIGHT_CLICK_BLOCK) {
      return;
    }
    long current_time = System.currentTimeMillis();
    final Player player = event.getPlayer();
    final String player_name = player.getName();
    combatTag_.tagPlayer(player);
    PearlTeleportInfo teleport_info = pearl_teleport_info_.get(player_name);
    if (teleport_info == null) {
      teleport_info = new PearlTeleportInfo();
      teleport_info.last_teleport = current_time;
    } else {
      final long time_diff = current_time - teleport_info.last_teleport;
      final long block_window = 10000;
      if (block_window > time_diff) {
        event.setCancelled(true);
          event.getPlayer().sendMessage(String.format(
              "Pearl Teleport Cooldown: %ds",
              (block_window - time_diff + 500) / 1000));
          event.getPlayer().sendMessage("You have been tagged for "+combatapi.getRemainingTagTime(player));
      } else {
        teleport_info.last_teleport = current_time;
      }
    }
    pearl_teleport_info_.put(player_name, teleport_info);
  }

  // ================================================
  // BottleO refugees

  // Changes the yield from an XP bottle
  @BahHumbugs ({
    @BahHumbug(opt="disable_experience", def="true"),
    @BahHumbug(opt="xp_per_bottle", type=OptType.Int, def="10")
  })
  @EventHandler(priority=EventPriority.HIGHEST)
  public void onExpBottleEvent(ExpBottleEvent event) {
    final int bottle_xp = config_.get("xp_per_bottle").getInt();
    if (config_.get("disable_experience").getBool()) {
      ((Player) event.getEntity().getShooter()).giveExp(bottle_xp);
      event.setExperience(0);
    } else {
      event.setExperience(bottle_xp);
    }
  }

  // Diables all XP gain except when manually changed via code.
  @EventHandler
  public void onPlayerExpChangeEvent(PlayerExpChangeEvent event) {
    if (config_.get("disable_experience").getBool()) {
      event.setAmount(0);
    }
  }

  // ================================================
  // Find the end portals

  public static final int ender_portal_id_ = Material.ENDER_PORTAL.getId();
  public static final int ender_portal_frame_id_ = Material.ENDER_PORTAL_FRAME.getId();
  private Set<Long> end_portal_scanned_chunks_ = new TreeSet<Long>();

  @BahHumbug(opt="find_end_portals", type=OptType.String)
  @EventHandler
  public void onFindEndPortals(ChunkLoadEvent event) {
    String scanWorld = config_.get("find_end_portals").getString();
    if (scanWorld.isEmpty()) {
      return;
    }
    World world = event.getWorld();
    if (!world.getName().equalsIgnoreCase(scanWorld)) {
      return;
    }
    Chunk chunk = event.getChunk();
    long chunk_id = (long)chunk.getX() << 32L + (long)chunk.getZ();
    if (end_portal_scanned_chunks_.contains(chunk_id)) {
      return;
    }
    end_portal_scanned_chunks_.add(chunk_id);
    int chunk_x = chunk.getX() * 16;
    int chunk_end_x = chunk_x + 16;
    int chunk_z = chunk.getZ() * 16;
    int chunk_end_z = chunk_z + 16;
    int max_height = 0;
    for (int x = chunk_x; x < chunk_end_x; x += 3) {
      for (int z = chunk_z; z < chunk_end_z; ++z) {
        int height = world.getMaxHeight();
        if (height > max_height) {
          max_height = height;
        }
      }
    }
    for (int y = 1; y <= max_height; ++y) {
      int z_adj = 0;
      for (int x = chunk_x; x < chunk_end_x; ++x) {
        for (int z = chunk_z + z_adj; z < chunk_end_z; z += 3) {
          int block_type = world.getBlockTypeIdAt(x, y, z);
          if (block_type == ender_portal_id_ || block_type == ender_portal_frame_id_) {
            info(String.format("End portal found at %d,%d", x, z));
            return;
          }
        }
        // This funkiness results in only searching 48 of the 256 blocks on
        //  each y-level. 81.25% fewer blocks checked.
        ++z_adj;
        if (z_adj >= 3) {
          z_adj = 0;
          x += 2;
        }
      }
    }
  }

  // ================================================
  // Prevent inventory access while in a vehicle, unless it's the Player's

  @BahHumbugs ({
    @BahHumbug(opt="prevent_opening_container_carts", def="true"),
    @BahHumbug(opt="prevent_vehicle_inventory_open", def="true")
  })
  @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
  public void onPreventVehicleInvOpen(InventoryOpenEvent event) {
    // Cheap break-able conditional statement
    while (config_.get("prevent_vehicle_inventory_open").getBool()) {
      HumanEntity human = event.getPlayer();
      if (!(human instanceof Player)) {
        break;
      }
      if (!human.isInsideVehicle()) {
        break;
      }
      InventoryHolder holder = event.getInventory().getHolder();
      if (holder == human) {
        break;
      }
      event.setCancelled(true);
      break;
    }
    if (config_.get("prevent_opening_container_carts").getBool() && !event.isCancelled()) {
      InventoryHolder holder = event.getInventory().getHolder();
      if (holder instanceof StorageMinecart || holder instanceof HopperMinecart) {
        event.setCancelled(true);
      }
    }
  }

  // ================================================
  // Adjust horse and minecart speeds
  @BahHumbugs ({
  @BahHumbug(opt="minecart_speed", type=OptType.Double, def="1"),
  @BahHumbug(opt="horse_speed", type=OptType.Double, def="0.170000")
  })
  @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
  public void onVehicleEnter(VehicleEnterEvent event) {
    // 0.17 is just a tad slower than minecarts
    Vehicle vehicle = event.getVehicle();
    if (vehicle instanceof Horse) {
        Versioned.setHorseSpeed((Entity)vehicle, config_.get("horse_speed").getDouble());
    }
    if (vehicle instanceof Minecart){
    	// cannot be greater than 2.  minecarts will fly off of track.
    	((Minecart) vehicle).setMaxSpeed(config_.get("minecart_speed").getDouble());
    	
    }  
  }
  // ================================================
  // Storage minecarts dont slow down.
  @BahHumbug(opt="storage_carts_slowable", type=OptType.Bool, def="false")
  @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
  public void onStorageCartMoveEvent(VehicleMoveEvent event){
	  Vehicle veh = event.getVehicle();
	  if (!(veh instanceof StorageMinecart)) return;
	  ((StorageMinecart)veh).setSlowWhenEmpty(config_.get("storage_carts_slowable").getBool());
  }
  // ============================
  // Admins can view player inventories

  @SuppressWarnings("deprecation")
  public void onInvseeCommand(Player admin, String playerName) {
    final Player player = Bukkit.getPlayerExact(playerName);
    if (player == null) {
      admin.sendMessage("Player not found");
      return;
    }
    final Inventory pl_inv = player.getInventory();
    final Inventory inv = Bukkit.createInventory(
        admin, 36, "Player inventory: " + playerName);
    for (int slot = 0; slot < 36; slot++) {
      final ItemStack it = pl_inv.getItem(slot);
      inv.setItem(slot, it);
    }
    admin.openInventory(inv);
    admin.updateInventory();
  }

  // ================================================
  // Fix minecarts

  public boolean checkForTeleportSpace(Location loc) {
    final Block block = loc.getBlock();
    final Material mat = block.getType();
    if (mat.isSolid()) {
      return false;
    }
    final Block above = block.getRelative(BlockFace.UP);
    if (above.getType().isSolid()) {
      return false;
    }
    return true;
  }

  public boolean tryToTeleport(Player player, Location location, String reason) {
    Location loc = location.clone();
    loc.setX(Math.floor(loc.getX()) + 0.500000D);
    loc.setY(Math.floor(loc.getY()) + 0.02D);
    loc.setZ(Math.floor(loc.getZ()) + 0.500000D);
    final Location baseLoc = loc.clone();
    final World world = baseLoc.getWorld();
    // Check if teleportation here is viable
    boolean performTeleport = checkForTeleportSpace(loc);
    if (!performTeleport) {
      loc.setY(loc.getY() + 1.000000D);
      performTeleport = checkForTeleportSpace(loc);
    }
    if (performTeleport) {
      player.setVelocity(new Vector());
      player.teleport(loc);
      Humbug.info(String.format(
          "Player '%s' %s: Teleported to %s",
          player.getName(), reason, loc.toString()));
      return true;
    }
    loc = baseLoc.clone();
    // Create a sliding window of block types and track how many of those
    //  are solid. Keep fetching the block below the current block to move down.
    int air_count = 0;
    LinkedList<Material> air_window = new LinkedList<Material>();
    loc.setY((float)world.getMaxHeight() - 2);
    Block block = world.getBlockAt(loc);
    for (int i = 0; i < 4; ++i) {
      Material block_mat = block.getType();
      if (!block_mat.isSolid()) {
        ++air_count;
      }
      air_window.addLast(block_mat);
      block = block.getRelative(BlockFace.DOWN);
    }
    // Now that the window is prepared, scan down the Y-axis.
    while (block.getY() >= 1) {
      Material block_mat = block.getType();
      if (block_mat.isSolid()) {
        if (air_count == 4) {
          player.setVelocity(new Vector());
          loc = block.getLocation();
          loc.setX(Math.floor(loc.getX()) + 0.500000D);
          loc.setY(loc.getY() + 1.02D);
          loc.setZ(Math.floor(loc.getZ()) + 0.500000D);
          player.teleport(loc);
          Humbug.info(String.format(
              "Player '%s' %s: Teleported to %s",
              player.getName(), reason, loc.toString()));
          return true;
        }
      } else { // !block_mat.isSolid()
        ++air_count;
      }
      air_window.addLast(block_mat);
      if (!air_window.removeFirst().isSolid()) {
        --air_count;
      }
      block = block.getRelative(BlockFace.DOWN);
    }
    return false;
  }

  @BahHumbug(opt="fix_vehicle_logout_bug", def="true")
  @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled=true)
  public void onDisallowVehicleLogout(PlayerQuitEvent event) {
    if (!config_.get("fix_vehicle_logout_bug").getBool()) {
      return;
    }
    Player player = event.getPlayer();
    Entity vehicle = player.getVehicle();
    if (vehicle == null || !(vehicle instanceof Minecart)) {
      return;
    }
    Location vehicleLoc = vehicle.getLocation();
    // Vehicle data has been cached, now safe to kick the player out
    player.leaveVehicle();
    if (!tryToTeleport(player, vehicleLoc, "logged out")) {
      player.setHealth(0.000000D);
      Humbug.info(String.format(
          "Player '%s' logged out in vehicle: Killed", player.getName()));
    }
  }

  @BahHumbug(opt="fix_minecart_reenter_bug", def="true")
  @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
  public void onFixMinecartReenterBug(VehicleExitEvent event) {
    if (!config_.get("fix_minecart_reenter_bug").getBool()) {
      return;
    }
    final Vehicle vehicle = event.getVehicle();
    if (vehicle == null || !(vehicle instanceof Minecart)) {
      return;
    }
    final Entity passengerEntity = event.getExited();
    if (passengerEntity == null || !(passengerEntity instanceof Player)) {
      return;
    }
    // Must delay the teleport 2 ticks or else the player's mis-managed
    //  movement still occurs. With 1 tick it could still occur.
    final Player player = (Player)passengerEntity;
    final Location vehicleLoc = vehicle.getLocation();
    Bukkit.getScheduler().runTaskLater(this, new Runnable() {
      @Override
      public void run() {
        if (!tryToTeleport(player, vehicleLoc, "exiting vehicle")) {
          player.setHealth(0.000000D);
          Humbug.info(String.format(
              "Player '%s' exiting vehicle: Killed", player.getName()));
        }
      }
    }, 2L);
  }

  @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
  public void onFixMinecartReenterBug(VehicleDestroyEvent event) {
    if (!config_.get("fix_minecart_reenter_bug").getBool()) {
      return;
    }
    final Vehicle vehicle = event.getVehicle();
    if (vehicle == null || !(vehicle instanceof Minecart)) {
      return;
    }
    final Entity passengerEntity = vehicle.getPassenger();
    if (passengerEntity == null || !(passengerEntity instanceof Player)) {
      return;
    }
    // Must delay the teleport 2 ticks or else the player's mis-managed
    //  movement still occurs. With 1 tick it could still occur.
    final Player player = (Player)passengerEntity;
    final Location vehicleLoc = vehicle.getLocation();
    Bukkit.getScheduler().runTaskLater(this, new Runnable() {
      @Override
      public void run() {
        if (!tryToTeleport(player, vehicleLoc, "in destroyed vehicle")) {
          player.setHealth(0.000000D);
          Humbug.info(String.format(
              "Player '%s' in destroyed vehicle: Killed", player.getName()));
        }
      }
    }, 2L);
  }

  // ================================================
  // Adjust ender pearl gravity

  @BahHumbug(opt="ender_pearl_gravity", type=OptType.Double, def="0.060000")
  public void hookEnderPearls() {
    net.minecraft.server.v1_6_R3.Item.byId[256 + 112] = null;
    net.minecraft.server.v1_6_R3.Item.ENDER_PEARL = (new CustomNMSItemEnderPearl(112, config_)).b("enderPearl");
  }

  // ================================================
  // General

  public void onEnable() {
    registerEvents();
    registerCommands();
    loadConfiguration();
    removeRecipies();
    hookEnderPearls();
    global_instance_ = this;
    info("Enabled");
    
  }

  public boolean isInitiaized() {
    return global_instance_ != null;
  }

  public boolean toBool(String value) {
    if (value.equals("1") || value.equalsIgnoreCase("true")) {
      return true;
    }
    return false;
  }

  public int toInt(String value, int default_value) {
    try {
      return Integer.parseInt(value);
    } catch(Exception e) {
      return default_value;
    }
  }
  
  public double toDouble(String value, double default_value) {
    try {
      return Double.parseDouble(value);
    } catch(Exception e) {
      return default_value;
    }
  }

  public int toMaterialId(String value, int default_value) {
    try {
      return Integer.parseInt(value);
    } catch(Exception e) {
      Material mat = Material.matchMaterial(value);
      if (mat != null) {
        return mat.getId();
      }
    }
    return default_value;
  }

  public boolean onCommand(
      CommandSender sender,
      Command command,
      String label,
      String[] args) {
    if (sender instanceof Player && command.getName().equals("invsee")) {
      if (args.length < 1) {
        sender.sendMessage("Provide a name");
        return false;
      }
      onInvseeCommand((Player)sender, args[0]);
      return true;
    }
    if (sender instanceof Player
        && command.getName().equals("introbook")) {
      Player sendBookTo = (Player)sender;
      if (args.length >= 1) {
        Player possible = Bukkit.getPlayerExact(args[0]);
        if (possible != null) {
          sendBookTo = possible;
        }
      }
      giveN00bBook(sendBookTo);
      return true;
    }
    if (!(sender instanceof ConsoleCommandSender) ||
        !command.getName().equals("humbug") ||
        args.length < 1) {
      return false;
    }
    String option = args[0];
    String value = null;
    String subvalue = null;
    boolean set = false;
    boolean subvalue_set = false;
    String msg = "";
    if (args.length > 1) {
      value = args[1];
      set = true;
    }
    if (args.length > 2) {
      subvalue = args[2];
      subvalue_set = true;
    }
    ConfigOption opt = config_.get(option);
    if (opt != null) {
      if (set) {
        opt.set(value);
      }
      msg = String.format("%s = %s", option, opt.getString());
    } else if (option.equals("debug")) {
      if (set) {
        config_.setDebug(toBool(value));
      }
      msg = String.format("debug = %s", config_.getDebug());
    } else if (option.equals("loot_multiplier")) {
      String entity_type = "generic";
      if (set && subvalue_set) {
        entity_type = value;
        value = subvalue;
      }
      if (set) {
        config_.setLootMultiplier(
                entity_type, toInt(value, config_.getLootMultiplier(entity_type)));
      }
      msg = String.format(
          "loot_multiplier(%s) = %d", entity_type, config_.getLootMultiplier(entity_type));
    } else if (option.equals("remove_mob_drops")) {
      if (set && subvalue_set) {
        if (value.equals("add")) {
          config_.addRemoveItemDrop(toMaterialId(subvalue, -1));
        } else if (value.equals("del")) {
          config_.removeRemoveItemDrop(toMaterialId(subvalue, -1));
        }
      }
      msg = String.format("remove_mob_drops = %s", config_.toDisplayRemoveItemDrops());
    } else if (option.equals("save")) {
      config_.save();
      msg = "Configuration saved";
    } else if (option.equals("reload")) {
      config_.reload();
      msg = "Configuration loaded";
    } else {
      msg = String.format("Unknown option %s", option);
    }
    sender.sendMessage(msg);
    return true;
  }

  public void registerCommands() {
    ConsoleCommandSender console = getServer().getConsoleSender();
    console.addAttachment(this, "humbug.console", true);
  }

  private void registerEvents() {
    getServer().getPluginManager().registerEvents(this, this);
  }

  private void loadConfiguration() {
    config_ = Config.initialize(this);
  }
}
