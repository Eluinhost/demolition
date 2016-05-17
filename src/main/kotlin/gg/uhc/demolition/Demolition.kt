package gg.uhc.demolition

import org.bukkit.Material
import org.bukkit.World
import org.bukkit.block.Biome
import org.bukkit.block.Block
import org.bukkit.configuration.InvalidConfigurationException
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.*
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockPlaceEvent
import org.bukkit.event.entity.CreatureSpawnEvent
import org.bukkit.event.entity.EntityChangeBlockEvent
import org.bukkit.event.world.WorldInitEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.material.MaterialData
import org.bukkit.plugin.java.JavaPlugin
import org.bukkit.scheduler.BukkitRunnable
import org.bukkit.util.Vector
import java.util.*

internal data class Configuration(
    val endermenHaveTntChance: Int,
    val tntPlayerPlaceAutoPrimeChance: Int,
    val tntEndermanPlaceAutoPrimeChance: Int,
    val creepersArePoweredChance: Int,
    val skeletonsHaveFireBowsChance: Int,
    val skeletonsHaveFireBowsDropChance: Float,
    val veinRules: VeinRules
)

private val random = Random()

internal fun rngesusNoticeMe(chance: Int, onPass: () -> Unit = {}, onFail: () -> Unit = {}) = if (random.nextInt(100) < chance) onPass() else onFail()

class Demolition : JavaPlugin(), Listener {
    private lateinit var settings: Configuration

    override fun onEnable() {
        config.options().copyDefaults(true)
        saveConfig()

        val section = config.getConfigurationSection("vein rules") ?: throw InvalidConfigurationException("Missing vein rules section")

        settings = Configuration(
            endermenHaveTntChance = config.getInt("enderman have tnt chance"),
            tntPlayerPlaceAutoPrimeChance = config.getInt("tnt auto prime on player place chance"),
            tntEndermanPlaceAutoPrimeChance = config.getInt("tnt auto prime on enderman place chance"),
            creepersArePoweredChance = config.getInt("creepers are powered chance"),
            skeletonsHaveFireBowsChance = config.getInt("skeletons have fire bows chance"),
            skeletonsHaveFireBowsDropChance = config.getInt("skeletons have fire bows drop chance").toFloat() / 100F,
            veinRules = VeinRules(
                material = Material.TNT,
                data = null,
                size = section.getInt("size"),
                rounds = section.getInt("rounds"),
                minHeight = section.getInt("min height"),
                maxHeight = section.getInt("max height"),
                probability = section.getDouble("probability").toFloat(),
                biomes = section.getStringList("biomes").map { Biome.valueOf(it.toUpperCase()) }
            )
        )

        server.pluginManager.registerEvents(this, this)
    }

    @EventHandler(ignoreCancelled = true)
    fun on(event: WorldInitEvent) = addPopulatorToWorld(event.world)

    @EventHandler(ignoreCancelled = true)
    fun on(event: CreatureSpawnEvent) = onMobSpawn(event.entity)

    @EventHandler(ignoreCancelled = true)
    fun on(event: BlockPlaceEvent) = onBlockPlace(event)

    @EventHandler(ignoreCancelled = true)
    fun on(event: EntityChangeBlockEvent) = onMobBlockPlace(event)

    fun addPopulatorToWorld(world: World) {
        if (world.populators.any { it is VeinPopulator }) return

        if (world.environment == World.Environment.NORMAL) {
            world.populators.add(VeinPopulator(settings.veinRules))
        }
    }

    fun modifyEnderman(enderman: Enderman?) = enderman?.let { it.carriedMaterial = MaterialData(Material.TNT) }

    fun modifyCreeper(creeper: Creeper?) = creeper?.let { it.isPowered = true }

    fun modifySkeleton(skeleton: Skeleton?) = skeleton?.let {
        val bow = ItemStack(Material.BOW, 1)
        bow.addEnchantment(Enchantment.ARROW_FIRE, 1)
        it.equipment.itemInHand = bow
        it.equipment.itemInHandDropChance = settings.skeletonsHaveFireBowsDropChance
    }

    private fun onMobSpawn(entity: Entity) = when (entity) {
        is Enderman -> rngesusNoticeMe(settings.endermenHaveTntChance, { modifyEnderman(entity) })
        is Creeper -> rngesusNoticeMe(settings.creepersArePoweredChance, { modifyCreeper(entity) })
        is Skeleton -> rngesusNoticeMe(settings.skeletonsHaveFireBowsChance, { modifySkeleton(entity) })
        else -> {}
    }

    private fun onBlockPlace(event: BlockPlaceEvent) {
        if (event.block.type != Material.TNT) return

        rngesusNoticeMe(settings.tntPlayerPlaceAutoPrimeChance, { TntSpawner(event.block).run() })
    }

    private fun onMobBlockPlace(event: EntityChangeBlockEvent) {
        if (event.to != Material.TNT || event.entityType != EntityType.ENDERMAN) return

        // run on next tick to run after event completion
        rngesusNoticeMe(settings.tntEndermanPlaceAutoPrimeChance, { TntSpawner(event.block).runTask(this) })
    }

    class TntSpawner(val block: Block) : BukkitRunnable() {
        private val blockOffset = Vector(0.5, 0.5, 0.5)
        private val initVelocity = Vector(0.0, 0.2, 0.0)

        override fun run() {
            block.type = Material.AIR
            val primed = block.world.spawn(block.location.add(blockOffset), TNTPrimed::class.java)
            primed.velocity = initVelocity
        }
    }
}