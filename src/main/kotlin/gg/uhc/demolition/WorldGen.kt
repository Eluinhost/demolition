package gg.uhc.demolition

import org.bukkit.Chunk
import org.bukkit.Material
import org.bukkit.World
import org.bukkit.block.Biome
import org.bukkit.block.Block
import org.bukkit.generator.BlockPopulator
import java.util.*

data class VeinRules(
    val material: Material,
    val data: Byte?,
    val size: Int,
    val rounds: Int,
    val minHeight: Int,
    val maxHeight: Int,
    val probability: Float,
    val biomes: List<Biome>
)

open class VeinPopulator(val rules: VeinRules) : BlockPopulator() {
    override fun populate(world: World, random: Random, chunk: Chunk) {
        for (i in 0..rules.rounds) {
            if (rules.probability < random.nextDouble()) continue

            val x = (chunk.x shl 4) + random.nextInt(16)
            val y = rules.minHeight + random.nextInt(rules.maxHeight - rules.minHeight)
            val z = (chunk.z shl 4) + random.nextInt(16)

            if (rules.biomes.size == 0 || rules.biomes.contains(world.getBiome(x, z))) {
                generate(world, random, x, y, z)
            }
        }
    }

    protected open fun generate(world: World, rand: Random, x: Int, y: Int, z: Int) {
        val rpi = rand.nextDouble() * Math.PI

        val x1 = x + 8.0 + Math.sin(rpi) * rules.size / 8.0
        val x2 = x + 8.0 - Math.sin(rpi) * rules.size / 8.0
        val z1 = z + 8.0 + Math.cos(rpi) * rules.size / 8.0
        val z2 = z + 8.0 - Math.cos(rpi) * rules.size / 8.0

        val y1 = y + rand.nextInt(3) + 2
        val y2 = y + rand.nextInt(3) + 2

        for (i in 0..rules.size) {
            val xPos = x1 + (x2 - x1) * i / rules.size
            val yPos = y1 + (y2 - y1) * i / rules.size
            val zPos = z1 + (z2 - z1) * i / rules.size

            val fuzz = rand.nextDouble() * rules.size / 16.0
            val fuzzXZ = (Math.sin(i * Math.PI / rules.size) + 1.0) * fuzz + 1.0
            val fuzzY = (Math.sin(i * Math.PI / rules.size) + 1.0) * fuzz + 1.0

            val xStart = Math.floor(xPos - fuzzXZ / 2.0).toInt()
            val yStart = Math.floor(yPos - fuzzY / 2.0).toInt()
            val zStart = Math.floor(zPos - fuzzXZ / 2.0).toInt()

            val xEnd = Math.floor(xPos + fuzzXZ / 2.0).toInt()
            val yEnd = Math.floor(yPos + fuzzY / 2.0).toInt()
            val zEnd = Math.floor(zPos + fuzzXZ / 2.0).toInt()

            for (ix in xStart..xEnd) {
                val xThreshSq = Math.pow((ix + 0.5 - xPos) / (fuzzXZ / 2.0), 2.0)
                if (xThreshSq >= 1.0) continue

                for (iy in yStart..yEnd) {
                    val yThreshSq = Math.pow((iy + 0.5 - yPos) / (fuzzY / 2.0), 2.0)
                    if (xThreshSq + yThreshSq >= 1.0) continue

                    for (iz in zStart..zEnd) {
                        val zThreshSq = Math.pow((iz + 0.5 - zPos) / (fuzzXZ / 2.0), 2.0)
                        if (xThreshSq + yThreshSq + zThreshSq >= 1.0) continue

                        val block = getBlockAt(world, ix, iy, iz) ?: continue

                        if (block.type != Material.STONE) continue

                        // Convert block
                        block.type = rules.material
                        if (rules.data != null) {
                            block.data = rules.data
                        }
                    }
                }
            }
        }
    }
}

fun getBlockAt(world: World, x: Int, y: Int, z: Int) : Block? {
    val cx = x shr 4
    val cz = z shr 4

    if (!world.isChunkLoaded(cx, cz) && !world.loadChunk(cx, cz, false)) return null

    return world.getChunkAt(cx, cz)?.getBlock(x and 15, y, z and 15)
}