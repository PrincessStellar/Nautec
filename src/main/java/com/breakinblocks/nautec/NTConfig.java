package com.breakinblocks.nautec;

import com.breakinblocks.nautec.content.conduits.TapRates;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.neoforge.common.ModConfigSpec;

import java.util.List;
import java.util.Set;

@EventBusSubscriber(modid = Nautec.MODID)
public final class NTConfig {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();
    private static final ModConfigSpec.Builder WORLDGEN_BUILDER = new ModConfigSpec.Builder();

    private static final ModConfigSpec.IntValue KELP_HEIGHT = BUILDER
            .comment("The height of kelp to be able to grow.")
            .defineInRange("kelpHeight", 40, 25, Integer.MAX_VALUE);

    private static final ModConfigSpec.IntValue MIXER_POWER_REQUIREMENT = BUILDER
            .comment("The amount of power required by the mixer each tick.")
            .defineInRange("mixerPowerRequirement", 10, 0, Integer.MAX_VALUE);
    private static final ModConfigSpec.IntValue AUGMENTATION_STATION_POWER_REQUIREMENT = BUILDER
            .comment("The amount of power required by the Augmentation Station each tick.")
            .defineInRange("augmentationPowerRequirement", 25, 0, Integer.MAX_VALUE);
    private static final ModConfigSpec.IntValue DRAIN_POWER_REQUIREMENT = BUILDER
            .comment("The amount of power required by the Deep Sea Drain each tick.")
            .defineInRange("drainPowerRequirement", 20, 0, Integer.MAX_VALUE);
    private static final ModConfigSpec.BooleanValue BEAM_OVERCLOCK = BUILDER
            .comment("Whether processing machines run faster on a laser beam stronger than they need. Speed grows with the square root of the beam, so four times the power a machine needs runs it twice as fast")
            .define("beamOverclock", true);
    private static final ModConfigSpec.DoubleValue BEAM_OVERCLOCK_MAX_SPEED = BUILDER
            .comment("The highest speed multiplier a stronger beam can give a machine. 0 means no limit")
            .defineInRange("beamOverclockMaxSpeed", 0.0, 0.0, 1_000_000.0);

    private static final ModConfigSpec.IntValue REGULAR_LASER_DISTANCE = BUILDER
            .comment("The distance of normals lasers.")
            .defineInRange("laserDistance", 16, 0, 128);
    private static final ModConfigSpec.IntValue RELAY_LASER_DISTANCE = BUILDER
            .comment("The distance a Prismarine Laser Relay sends its beam.")
            .defineInRange("relayLaserDistance", 64, 0, 128);

    private static final ModConfigSpec.IntValue ENERGY_CONVERTER_FE_PER_AP = BUILDER
            .comment("How much FE an Energy Converter spends for each AP it sends")
            .defineInRange("energyConverterFePerAp", 10, 1, 1_000_000);
    private static final ModConfigSpec.IntValue ENERGY_CONVERTER_FE_CAPACITY = BUILDER
            .comment("How much FE an Energy Converter can hold")
            .defineInRange("energyConverterFeCapacity", 4_000_000, 1_000, Integer.MAX_VALUE);
    private static final ModConfigSpec.IntValue ENERGY_CONVERTER_BASE_AP = BUILDER
            .comment("The highest transmission rate, in AP per tick, of an Energy Converter with no upgrades")
            .defineInRange("energyConverterBaseAp", 150, 1, 1_000_000);
    private static final ModConfigSpec.IntValue ENERGY_CONVERSION_UPGRADE_AP = BUILDER
            .comment("How much each Energy Conversion Upgrade raises an Energy Converter's highest rate, in AP per tick")
            .defineInRange("energyConversionUpgradeAp", 150, 0, 1_000_000);
    private static final ModConfigSpec.IntValue ADVANCED_ENERGY_CONVERSION_UPGRADE_AP = BUILDER
            .comment("How much each Advanced Energy Conversion Upgrade raises an Energy Converter's highest rate, in AP per tick")
            .defineInRange("advancedEnergyConversionUpgradeAp", 500, 0, 1_000_000);
    private static final ModConfigSpec.IntValue ULTIMATE_ENERGY_CONVERSION_UPGRADE_AP = BUILDER
            .comment("How much each Ultimate Energy Conversion Upgrade raises an Energy Converter's highest rate, in AP per tick")
            .defineInRange("ultimateEnergyConversionUpgradeAp", 1500, 0, 1_000_000);
    private static final ModConfigSpec.DoubleValue BEAM_MERGE_PURITY_DROP = BUILDER
            .comment("When beams merge into one block, how far the purity falls from the purest beam toward the average of all of them. 0 keeps the highest purity, 1 is a plain average")
            .defineInRange("beamMergePurityDrop", 0.25, 0.0, 1.0);

    private static final ModConfigSpec.IntValue MIXER_INPUT_CAPACITY = BUILDER
            .comment("The capacity of the Mixers Input Tank")
            .defineInRange("mixerInputCapacity", 32_000, 0, Integer.MAX_VALUE);
    private static final ModConfigSpec.IntValue MIXER_OUTPUT_CAPACITY = BUILDER
            .comment("The capacity of the Mixers Output Tank")
            .defineInRange("mixerOutputCapacity", 32_000, 0, Integer.MAX_VALUE);
    private static final ModConfigSpec.IntValue LASER_CRAFTING_MATRIX_TANK_CAPACITY = BUILDER
            .comment("The capacity in mB of each of the Laser Crafting Matrix's four tanks: two inputs and two outputs")
            .defineInRange("laserCraftingMatrixTankCapacity", 16_000, 1, Integer.MAX_VALUE);

    private static final ModConfigSpec.IntValue DRAIN_SALT_WATER_AMOUNT = BUILDER
            .comment("The mB of salt water a Deep Sea Drain pumps each second on a beam of exactly its power requirement. It scales with the square root of the beam, so four times the beam pumps twice as much")
            .defineInRange("drainSaltWaterAmount", 500, 0, Integer.MAX_VALUE);
    private static final ModConfigSpec.IntValue DRAIN_CAPACITY = BUILDER
            .comment("The fluid capacity of the Deep Sea Drain")
            .defineInRange("drainCapacity", 128_000, 0, Integer.MAX_VALUE);
    private static final ModConfigSpec.BooleanValue DRAIN_REQUIRES_OCEAN = BUILDER
            .comment("Whether the Deep Sea Drain only pumps in an ocean-like biome: one with a biome tag named ocean or sea, or an is_ tag with ocean or sea in its name, such as minecraft:is_ocean, c:is_deep_ocean or a mod's own ocean tag. The biome is checked at the drain and at the top of the water above it")
            .define("drainRequiresOcean", true);

    private static final ModConfigSpec.BooleanValue SPAWN_BOOK_IN_INVENTORY = BUILDER
            .comment("Determines whether to give the player a book when joining a new world")
            .define("spawnBookInInventory", true);
    public static final ModConfigSpec.BooleanValue COLLECT_SALT_WATER = BUILDER
            .comment("Determines whether the player should be able to collect salt water when picking up water in an ocean biome")
            .define("collectSaltWater", false);
    private static final ModConfigSpec.BooleanValue COLLECT_AIR_WITH_BOTTLE = BUILDER
            .comment("Determines whether the player should be able to collect pressurized air bottles by right-clicking on a bubble column")
            .define("collectAirWithBottle", true);

    private static final ModConfigSpec.IntValue GUARDIAN_AUGMENT_DAMAGE = BUILDER
            .comment("The amount of damage the guardian augments laser deals")
            .defineInRange("guardianAugmentDamage", 3, 0, Integer.MAX_VALUE);
    private static final ModConfigSpec.BooleanValue ALLOW_AUGMENT_RENDERING = BUILDER
            .comment("Set to false to disable the rendering of augments, this can improve performance")
            .define("allowAugmentRendering", true);

    private static final ModConfigSpec.IntValue FISHER_LASER_LEVEL = BUILDER
            .comment("The amount laser power required to have the fisher work")
            .defineInRange("fisherLaserLevel", 1, 0, Integer.MAX_VALUE);

    private static final ModConfigSpec.IntValue FISHER_DURATION = BUILDER
            .comment("The amount of ticks the fisher takes to make a new item")
            .defineInRange("fisherRunDuration", 40, 0, Integer.MAX_VALUE);
    private static final ModConfigSpec.IntValue FISHER_RADIUS = BUILDER
            .comment("The radius on the x and z plane the fisher checks for the water")
            .defineInRange("fisherRadius", 2, 1, Integer.MAX_VALUE);

    private static final ModConfigSpec.IntValue FISHER_DEPTH = BUILDER
            .comment("The y depth the fisher checks for water")
            .defineInRange("fisherDepth", 2, 1, Integer.MAX_VALUE);

    private static final ModConfigSpec.DoubleValue BACTERIA_GROWTH_RATE_CAP = BUILDER
            .comment("The maximum rate at which bacteria can grow")
            .defineInRange("bacteriaGrowthRateCap", 5, 0, Float.MAX_VALUE);

    private static final ModConfigSpec.DoubleValue BACTERIA_PRODUCTION_RATE_CAP = BUILDER
            .comment("The maximum rate at which bacteria can produce")
            .defineInRange("bacteriaProductionRateCap", 2, 0, Float.MAX_VALUE);

    private static final ModConfigSpec.DoubleValue BACTERIA_MUTATION_RESISTANCE_CAP = BUILDER
            .comment("The maximum rate at which bacteria can resist mutation")
            .defineInRange("bacteriaMutationResistanceCap", 1, 0, Float.MAX_VALUE);

    private static final ModConfigSpec.LongValue BACTERIA_COLONY_SIZE_CAP = BUILDER
            .comment("The maximum size a bacteria colony can grow to")
            .defineInRange("bacteriaColonySizeCap", 40_000, 0, Long.MAX_VALUE);

    private static final ModConfigSpec.IntValue BACTERIA_LIFESPAN_CAP = BUILDER
            .comment("The maximum lifespan of a bacteria colony")
            .defineInRange("bacteriaLifespanCap", 24000, 0, Integer.MAX_VALUE);

    private static final ModConfigSpec.ConfigValue<List<? extends String>> PREFERRED_TAG_NAMESPACES = BUILDER
            .comment("Mod namespaces to prefer, in order, when a bacteria's product is an item tag. Packs with a unification mod should list its namespace first",
                    "An item from an earlier namespace wins; tag members from unlisted namespaces fall back to alphabetical order")
            .defineList("preferredTagNamespaces", List.of("minecraft"), () -> "minecraft", entry -> entry instanceof String);

    private static final ModConfigSpec.IntValue BACTERIA_ANALYZER_CRAFTING_SPEED = BUILDER
            .comment("The amount of ticks it takes for the Bacterial Analyzer to analyze a Petri Dish")
            .defineInRange("bacteriaAnalyzerCraftingSpeed", 60, 0, Integer.MAX_VALUE);

    private static final ModConfigSpec.IntValue BACTERIA_ANALYZER_POWER_USAGE = BUILDER
            .comment("The amount of power used by the Bacterial Analyzer each tick")
            .defineInRange("bacteriaAnalyzerPowerUsage", 5, 0, Integer.MAX_VALUE);

    private static final ModConfigSpec.IntValue MUTATOR_CRAFTING_SPEED = BUILDER
            .comment("The amount of ticks it takes for the Mutator to attempt a mutation")
            .defineInRange("mutatorCraftingSpeed", 240, 0, Integer.MAX_VALUE);

    private static final ModConfigSpec.IntValue MUTATOR_POWER_USAGE = BUILDER
            .comment("The amount of power used by the Mutator each tick")
            .defineInRange("mutatorPowerUsage", 10, 0, Integer.MAX_VALUE);

    private static final ModConfigSpec.DoubleValue MUTATOR_FAILURE_SHRINK = BUILDER
            .comment("The fraction of a colony lost when a mutation attempt fails, before mutation resistance reduces it")
            .defineInRange("mutatorFailureShrink", 0.25, 0, 1);

    private static final ModConfigSpec.DoubleValue MUTATOR_REFINE_CHANCE = BUILDER
            .comment("The percent chance a Mutator attempt succeeds when a colony is given its own product's storage block (or the product, if it has no block).",
                    "A success keeps the strain and size and shifts the stats as a mutation does. 0 turns this off")
            .defineInRange("mutatorRefineChance", 25.0, 0, 100);

    private static final ModConfigSpec.DoubleValue MUTATOR_BOOSTER_MULTIPLIER = BUILDER
            .comment("Multiplies a Mutator attempt's success chance while an Electrolyte Algae Serum Vial is in its booster slot. A vial is used only when an attempt succeeds")
            .defineInRange("mutatorBoosterMultiplier", 3.0, 1.0, 100.0);

    private static final ModConfigSpec.DoubleValue MUTATOR_BOOSTED_FAILURE_SHRINK = BUILDER
            .comment("The share of the normal failure loss a colony still takes when a boosted Mutator attempt fails. 0 means a boosted failure costs nothing, 1 means it costs the same as an unboosted one")
            .defineInRange("mutatorBoostedFailureShrink", 0.0, 0, 1);

    private static final ModConfigSpec.IntValue INCUBATOR_CRAFTING_SPEED = BUILDER
            .comment("The amount of ticks it takes for the Incubator to complete one growth cycle")
            .defineInRange("incubatorCraftingSpeed", 100, 0, Integer.MAX_VALUE);

    private static final ModConfigSpec.IntValue INCUBATOR_POWER_USAGE = BUILDER
            .comment("The amount of power used by the Incubator each tick")
            .defineInRange("incubatorPowerUsage", 20, 0, Integer.MAX_VALUE);

    private static final ModConfigSpec.DoubleValue INCUBATOR_SIZE_GROWTH = BUILDER
            .comment("The fraction of a colony's current size an Incubator cycle adds on top of its growth roll, multiplied by Growth Rate. 0 turns this off")
            .defineInRange("incubatorSizeGrowth", 0.03, 0, 1);

    private static final ModConfigSpec.DoubleValue BIO_REACTOR_BASE_SPEED = BUILDER
            .comment("The base amount of progress a Bio Reactor colony makes each tick, before production rate and colony size scale it")
            .defineInRange("bioReactorBaseSpeed", 16.8, 0, 1000);

    private static final ModConfigSpec.IntValue BIO_REACTOR_POWER_BASE = BUILDER
            .comment("The amount of power the Bio Reactor requires before any colonies are counted")
            .defineInRange("bioReactorPowerBase", 25, 0, Integer.MAX_VALUE);

    private static final ModConfigSpec.IntValue BIO_REACTOR_POWER_PER_COLONY = BUILDER
            .comment("The extra amount of power the Bio Reactor requires for each colony it holds")
            .defineInRange("bioReactorPowerPerColony", 25, 0, Integer.MAX_VALUE);

    private static final ModConfigSpec.DoubleValue BIO_REACTOR_DECAY_PER_SECOND = BUILDER
            .comment("The fraction of a senescent, unfed colony that dies off for every 20 ticks it works in a reactor (at least 1 per 20 ticks). 0 turns decay off")
            .defineInRange("bioReactorDecayPerSecond", 0.02, 0, 1);

    private static final ModConfigSpec.DoubleValue INDUSTRIAL_BIO_REACTOR_BASE_SPEED = BUILDER
            .comment("The base amount of progress an Industrial Bio Reactor colony makes each tick, before production rate, colony size and strain multiplier scale it")
            .defineInRange("industrialBioReactorBaseSpeed", 16.8, 0, 1000);

    private static final ModConfigSpec.IntValue INDUSTRIAL_BIO_REACTOR_POWER_BASE = BUILDER
            .comment("The amount of power the Industrial Bio Reactor requires before any colonies are counted")
            .defineInRange("industrialBioReactorPowerBase", 100, 0, Integer.MAX_VALUE);

    private static final ModConfigSpec.IntValue INDUSTRIAL_BIO_REACTOR_POWER_PER_COLONY = BUILDER
            .comment("The extra amount of power the Industrial Bio Reactor requires for each colony it holds")
            .defineInRange("industrialBioReactorPowerPerColony", 50, 0, Integer.MAX_VALUE);

    private static final ModConfigSpec.DoubleValue REACTOR_SPEED_UPGRADE_BONUS = BUILDER
            .comment("The production speed each Reactor Speed Upgrade adds, as a fraction of the base speed (0.5 means +50% per upgrade)")
            .defineInRange("reactorSpeedUpgradeBonus", 0.5, 0, 100);

    private static final ModConfigSpec.DoubleValue REACTOR_SPEED_UPGRADE_POWER = BUILDER
            .comment("The factor each Reactor Speed Upgrade multiplies the reactor power draw by")
            .defineInRange("reactorSpeedUpgradePowerMultiplier", 1.6, 1, 100);

    private static final ModConfigSpec.IntValue REACTOR_YIELD_UPGRADE_BONUS = BUILDER
            .comment("The extra items each Reactor Yield Upgrade adds to every completed production cycle")
            .defineInRange("reactorYieldUpgradeBonus", 1, 0, 64);

    private static final ModConfigSpec.DoubleValue REACTOR_YIELD_UPGRADE_POWER = BUILDER
            .comment("The factor each Reactor Yield Upgrade multiplies the reactor power draw by")
            .defineInRange("reactorYieldUpgradePowerMultiplier", 1.8, 1, 100);

    private static final ModConfigSpec.DoubleValue REACTOR_EFFICIENCY_UPGRADE_FACTOR = BUILDER
            .comment("The factor each Reactor Efficiency Upgrade multiplies the nutrient vitality a colony spends per tick by (0.75 means 25% less per upgrade)")
            .defineInRange("reactorEfficiencyUpgradeFactor", 0.75, 0, 1);

    private static final ModConfigSpec.DoubleValue REACTOR_EFFICIENCY_UPGRADE_FLOOR = BUILDER
            .comment("The lowest fraction of vitality per tick that Reactor Efficiency Upgrades can bring a colony down to")
            .defineInRange("reactorEfficiencyUpgradeFloor", 0.25, 0, 1);

    private static final ModConfigSpec.DoubleValue REACTOR_EFFICIENCY_UPGRADE_POWER = BUILDER
            .comment("The factor each Reactor Efficiency Upgrade multiplies the reactor power draw by")
            .defineInRange("reactorEfficiencyUpgradePowerMultiplier", 1.2, 1, 100);

    private static final ModConfigSpec.DoubleValue REACTOR_FUSION_UPGRADE_POWER = BUILDER
            .comment("The factor each Reactor Fusion Upgrade multiplies the reactor power draw by. It counts as one Speed, one Yield and one Efficiency Upgrade, but only this multiplier applies")
            .defineInRange("reactorFusionUpgradePowerMultiplier", 2.5, 1, 1000);

    private static final ModConfigSpec.LongValue CRYSTAL_GROWTH_POWER = BUILDER
            .comment("The total AP a Crystal Cradle must take in to grow a Prismarine Crystal Seed into a full Cultivated Prismarine Crystal")
            .defineInRange("crystalGrowthPower", 10_000_000L, 1L, Long.MAX_VALUE);

    private static final ModConfigSpec.DoubleValue CRYSTAL_GROWTH_PURITY = BUILDER
            .comment("The lowest beam purity a Crystal Cradle grows its seed with. Weaker beams add nothing")
            .defineInRange("crystalGrowthPurity", 2.0, 0, 10);

    private static final ModConfigSpec.IntValue CONFINED_SPAWNER_POWER_PER_TICK = BUILDER
            .comment("The AP a Confined Spawner spends from its buffer each tick while it runs")
            .defineInRange("confinedSpawnerPowerPerTick", 50, 0, 30000);

    private static final ModConfigSpec.IntValue CONFINED_SPAWNER_POWER_BUFFER = BUILDER
            .comment("The most AP a Confined Spawner can hold in its buffer")
            .defineInRange("confinedSpawnerPowerBuffer", 500, 1, 30000);

    private static final ModConfigSpec.IntValue CONFINED_SPAWNER_XP_CAPACITY = BUILDER
            .comment("The liquid experience a Confined Spawner's tank holds, in mb. Experience made while it is full is lost")
            .defineInRange("confinedSpawnerXpCapacity", 16_000, 0, Integer.MAX_VALUE);

    private static final ModConfigSpec.IntValue CONFINED_SPAWNER_XP_RATIO = BUILDER
            .comment("How many mb of liquid experience a Confined Spawner makes for each experience point. 20 matches most mods")
            .defineInRange("confinedSpawnerXpRatio", 20, 0, 1000);

    private static final ModConfigSpec.ConfigValue<List<? extends String>> CONFINED_SPAWNER_XP_FLUIDS = BUILDER
            .comment("The liquid experience a Confined Spawner makes, as fluid ids in order of preference. The first one that exists is used,",
                    "so another mod's liquid experience can come first, for example \"create_enchantment_industry:experience\".",
                    "Experience Algae (nautec:experience_algae) is used when none of them exist")
            .defineList("confinedSpawnerXpFluids", List.of("nautec:experience_algae"), () -> "nautec:experience_algae", entry -> entry instanceof String);

    private static final ModConfigSpec.IntValue CHARM_TRANSFER_RATE = BUILDER
            .comment("The most FE per tick a Resonance Charm draws from its network to charge the wearer's items")
            .defineInRange("charmTransferRate", 2_000, 1, Integer.MAX_VALUE / 20);
    private static final ModConfigSpec.IntValue EMITTER_RANGE = BUILDER
            .comment("How far in blocks a Prismatic Emitter can reach the machines linked to it")
            .defineInRange("emitterRange", 64, 1, 128);
    private static final ModConfigSpec.IntValue EMITTER_THROUGHPUT = BUILDER
            .comment("The most FE per tick a Prismatic Emitter sends out, shared between its linked machines")
            .defineInRange("emitterThroughput", 10_000, 1, Integer.MAX_VALUE / 2);
    private static final ModConfigSpec.IntValue EMITTER_BUFFER = BUILDER
            .comment("The FE a Prismatic Emitter holds")
            .defineInRange("emitterBuffer", 100_000, 1, Integer.MAX_VALUE);
    private static final ModConfigSpec.DoubleValue SATELLITE_LOSS = BUILDER
            .comment("The fraction of AP and FE lost relaying through a Prism Satellite from Uplink Arrays to output Resonance Nodes and charms")
            .defineInRange("satelliteLoss", 0.10, 0.0, 0.99);

    private static final ModConfigSpec.IntValue REPLICATOR_POWER_USAGE = BUILDER
            .comment("The beam power in AP a Colony Replicator needs to run")
            .defineInRange("replicatorPowerUsage", 80, 0, Integer.MAX_VALUE);

    private static final ModConfigSpec.DoubleValue REPLICATOR_PURITY = BUILDER
            .comment("The lowest beam purity a Colony Replicator runs with")
            .defineInRange("replicatorPurity", 2.0, 0, 10);

    private static final ModConfigSpec.IntValue REPLICATOR_DURATION = BUILDER
            .comment("How many ticks a Colony Replicator takes to make one colony")
            .defineInRange("replicatorDuration", 600, 1, Integer.MAX_VALUE);

    private static final ModConfigSpec.LongValue REPLICATOR_BIOMASS_COST = BUILDER
            .comment("The total colony size of fodder a Colony Replicator uses up for each colony it makes")
            .defineInRange("replicatorBiomassCost", 20_000L, 1L, Long.MAX_VALUE);

    private static final ModConfigSpec.LongValue REPLICATOR_BIOMASS_CAP = BUILDER
            .comment("The most fodder biomass a Colony Replicator stores")
            .defineInRange("replicatorBiomassCap", 200_000L, 1L, Long.MAX_VALUE);

    private static final ModConfigSpec.DoubleValue REPLICATOR_ERROR_CHANCE = BUILDER
            .comment("The chance each stat is copied with an error when the colony has no Mutation Resistance.",
                    "It falls to nothing at the Mutation Resistance cap, so a capped colony always copies perfectly")
            .defineInRange("replicatorErrorChance", 0.5, 0.0, 1.0);

    private static final ModConfigSpec.DoubleValue REPLICATOR_SPLICE_CHANCE = BUILDER
            .comment("The chance a spliced colony takes the better parent's value for each stat")
            .defineInRange("replicatorSpliceChance", 0.7, 0.0, 1.0);

    private static final ModConfigSpec.DoubleValue REPLICATOR_BIOMASS_PER_ITEM = BUILDER
            .comment("The biomass a Colony Replicator gets from one of the item its template's strain makes, divided by the strain's Strain Yield.",
                    "The item's storage block gives nine times as much")
            .defineInRange("replicatorBiomassPerItem", 100.0, 0.0, 1_000_000.0);

    private static final ModConfigSpec.DoubleValue REPLICATOR_COPY_SIZE = BUILDER
            .comment("The size a Colony Replicator copy starts at, as a fraction of replicatorBiomassCost. 0 starts copies at a fresh graft's size")
            .defineInRange("replicatorCopySize", 0.5, 0.0, 1.0);

    private static final ModConfigSpec.IntValue OXYGEN_DIFFUSER_POWER = BUILDER
            .comment("The beam power in AP an Oxygen Diffuser needs. Any purity works")
            .defineInRange("oxygenDiffuserPower", 10, 0, Integer.MAX_VALUE);

    private static final ModConfigSpec.IntValue OXYGEN_DIFFUSER_RADIUS = BUILDER
            .comment("How far an Oxygen Diffuser's water breathing reaches, in blocks")
            .defineInRange("oxygenDiffuserRadius", 12, 1, 64);

    private static final ModConfigSpec.IntValue BUBBLE_ANCHOR_RADIUS = BUILDER
            .comment("How far a Bubble Anchor's field reaches from its centre on fuel. 7 gives a 15x15x15 cube")
            .defineInRange("bubbleAnchorRadius", 7, 1, 32);

    private static final ModConfigSpec.IntValue BUBBLE_ANCHOR_MAX_RADIUS = BUILDER
            .comment("The largest radius a Bubble Anchor reaches on a high purity laser beam")
            .defineInRange("bubbleAnchorMaxRadius", 12, 1, 32);

    private static final ModConfigSpec.IntValue BUBBLE_ANCHOR_LASER_POWER = BUILDER
            .comment("The beam power in AP that runs a Bubble Anchor without fuel")
            .defineInRange("bubbleAnchorLaserPower", 20, 1, Integer.MAX_VALUE);

    private static final ModConfigSpec.DoubleValue BUBBLE_ANCHOR_FUEL_MULTIPLIER = BUILDER
            .comment("Scales how long each fuel item keeps a Bubble Anchor running. 1.0 gives 2 minutes per Dried Kelp Block")
            .defineInRange("bubbleAnchorFuelMultiplier", 1.0, 0.01, 100.0);

    private static final ModConfigSpec.IntValue BUBBLE_ANCHOR_BLOCKS_PER_TICK = BUILDER
            .comment("How many blocks a Bubble Anchor checks each tick while clearing or refilling")
            .defineInRange("bubbleAnchorBlocksPerTick", 1024, 16, 65536);

    private static final ModConfigSpec.IntValue BUBBLE_CAPSULE_RADIUS = BUILDER
            .comment("How far a Bubble Capsule's pocket reaches from the user's head. 2 gives a 5x5x5 cube")
            .defineInRange("bubbleCapsuleRadius", 2, 1, 8);

    private static final ModConfigSpec.IntValue BUBBLE_CAPSULE_SECONDS = BUILDER
            .comment("How long a Bubble Capsule's pocket holds before the water returns, in seconds")
            .defineInRange("bubbleCapsuleSeconds", 30, 5, 600);

    private static final ModConfigSpec.IntValue DISTRIBUTOR_RANGE = BUILDER
            .comment("How far in blocks a Nautechnical Distributor can link to a block")
            .defineInRange("distributorRange", 256, 1, 4096);

    private static final ModConfigSpec.IntValue DISTRIBUTOR_MAX_LINKS = BUILDER
            .comment("The most blocks one Nautechnical Distributor can link to")
            .defineInRange("distributorMaxLinks", 64, 1, 1024);

    private static final ModConfigSpec.IntValue DISTRIBUTOR_INTERVAL = BUILDER
            .comment("How many ticks a Nautechnical Distributor waits between moves. Each move has no amount limit")
            .defineInRange("distributorInterval", 4, 1, 200);

    private static final String[] CONDUIT_TIERS = {"Base", "Eddy", "Surge", "Riptide", "Maelstrom"};
    private static final ModConfigSpec.IntValue[] CONDUIT_ITEM_INTERVALS = conduitValues("conduitItemInterval",
            "Ticks between item moves on a Conduit Tap face with %s upgrade", new int[]{20, 10, 5, 2, 1}, 1, 1200);
    private static final ModConfigSpec.IntValue[] CONDUIT_ITEMS_PER_OP = conduitValues("conduitItemsPerOp",
            "Items moved per move on a Conduit Tap face with %s upgrade", new int[]{8, 16, 32, 64, 64}, 1, 4096);
    private static final ModConfigSpec.IntValue[] CONDUIT_FLUID_RATES = conduitValues("conduitFluidRate",
            "mB per tick a Conduit Tap face moves with %s upgrade", new int[]{1_000, 5_000, 30_000, 200_000, 1_000_000}, 1, Integer.MAX_VALUE);
    private static final ModConfigSpec.IntValue[] CONDUIT_ENERGY_RATES = conduitValues("conduitEnergyRate",
            "FE per tick a Conduit Tap face moves with %s upgrade. 0 means no limit", new int[]{5_000, 25_000, 150_000, 1_000_000, 5_000_000}, 0, Integer.MAX_VALUE);
    private static final ModConfigSpec.IntValue CONDUIT_MAX_NETWORK_SIZE = BUILDER
            .comment("The most Current Conduit and Conduit Tap blocks one network can hold. Blocks past this are not reached")
            .defineInRange("conduitMaxNetworkSize", 4096, 2, 65536);
    private static final ModConfigSpec.IntValue RESONANT_CISTERN_TIER_CAPACITY = BUILDER
            .comment("mB a Resonant Cistern channel holds, and the mB each Resonant Expansion adds")
            .defineInRange("resonantCisternTierCapacity", 16_000, 1_000, 100_000_000);
    private static final ModConfigSpec.IntValue RESONANT_TRANSFER_INTERVAL = BUILDER
            .comment("Ticks between automatic pushes and pulls on a Resonant Vault or Cistern face")
            .defineInRange("resonantTransferInterval", 10, 1, 1200);
    private static final ModConfigSpec.IntValue RESONANT_ITEMS_PER_TRANSFER = BUILDER
            .comment("Items a Resonant Vault face pushes or pulls each transfer")
            .defineInRange("resonantItemsPerTransfer", 16, 1, 4096);
    private static final ModConfigSpec.IntValue RESONANT_FLUID_PER_TRANSFER = BUILDER
            .comment("mB a Resonant Cistern face pushes or pulls each transfer")
            .defineInRange("resonantFluidPerTransfer", 1_000, 1, Integer.MAX_VALUE);

    private static final ModConfigSpec.IntValue SATELLITE_AP_BUFFER = BUILDER
            .comment("The AP each Uplink Array stores")
            .defineInRange("satelliteApBuffer", 5_000_000, 1, Integer.MAX_VALUE);

    private static final ModConfigSpec.IntValue SATELLITE_FE_BUFFER = BUILDER
            .comment("The FE each Uplink Array stores")
            .defineInRange("satelliteFeBuffer", 5_000_000, 1, Integer.MAX_VALUE);

    private static final ModConfigSpec.IntValue SATELLITE_TRANSFER_LIMIT = BUILDER
            .comment("The most AP, and separately the most FE, an output Resonance Node takes from its network each tick.",
                    "Each node can be set lower in its screen")
            .defineInRange("satelliteTransferLimit", 100_000, 0, Integer.MAX_VALUE);
    private static final ModConfigSpec.DoubleValue SATELLITE_CROSS_DIMENSION_PURITY_LOSS = BUILDER
            .comment("The fraction of purity laser power loses when a network's satellite carries it to a dimension that has no Uplink Array of its own")
            .defineInRange("satelliteCrossDimensionPurityLoss", 0.05, 0.0, 1.0);
    private static final ModConfigSpec.IntValue RESONANCE_NODE_AP_BUFFER = BUILDER
            .comment("The laser power a Resonance Node holds")
            .defineInRange("resonanceNodeApBuffer", 200_000, 1, Integer.MAX_VALUE);
    private static final ModConfigSpec.IntValue RESONANCE_NODE_FE_BUFFER = BUILDER
            .comment("The FE a Resonance Node holds")
            .defineInRange("resonanceNodeFeBuffer", 1_000_000, 1, Integer.MAX_VALUE);
    private static final ModConfigSpec.BooleanValue RESONANCE_CHUNK_LOADING = BUILDER
            .comment("Whether Resonance Nodes and Uplink Arrays may keep their own chunk loaded when a player turns it on. When false, no block keeps a chunk loaded whatever its setting")
            .define("resonanceChunkLoading", true);
    private static final ModConfigSpec.IntValue EMITTER_MAX_LINKS = BUILDER
            .comment("The most machines one Prismatic Emitter can be linked to")
            .defineInRange("emitterMaxLinks", 16, 1, 256);
    private static final ModConfigSpec.IntValue RESONANCE_MAX_NETWORKS = BUILDER
            .comment("The most Resonance Networks one player can own")
            .defineInRange("resonanceMaxNetworksPerPlayer", 16, 1, 1024);

    private static final ModConfigSpec.IntValue TIDAL_ROTOR_MIN_OUTPUT = BUILDER
            .comment("The FE per tick a Tidal Rotor makes in the shallowest, most enclosed ocean water it still runs in")
            .defineInRange("tidalRotorMinOutput", 40, 0, 1_000_000);

    private static final ModConfigSpec.IntValue TIDAL_ROTOR_MAX_OUTPUT = BUILDER
            .comment("The FE per tick a Tidal Rotor makes in open water at least 24 blocks deep")
            .defineInRange("tidalRotorMaxOutput", 80, 0, 1_000_000);

    private static final ModConfigSpec.IntValue VENT_TAP_MIN_OUTPUT = BUILDER
            .comment("The FE per tick a Thermal Vent Tap makes over a single heat source")
            .defineInRange("ventTapMinOutput", 350, 0, 10_000_000);

    private static final ModConfigSpec.IntValue VENT_TAP_MAX_OUTPUT = BUILDER
            .comment("The FE per tick a Thermal Vent Tap makes with all nine blocks below it hot")
            .defineInRange("ventTapMaxOutput", 1_250, 0, 10_000_000);

    private static final ModConfigSpec.IntValue VENT_TAP_SALT_WATER_PER_TICK = BUILDER
            .comment("The mB of Salt Water a Thermal Vent Tap boils each tick while it runs")
            .defineInRange("ventTapSaltWaterPerTick", 10, 1, 100_000);

    private static final ModConfigSpec.IntValue VENT_TAP_SALT_WATER_PER_SALT = BUILDER
            .comment("The mB of Salt Water a Thermal Vent Tap boils for each Salt it leaves behind")
            .defineInRange("ventTapSaltWaterPerSalt", 1_000, 1, Integer.MAX_VALUE);

    private static final ModConfigSpec.ConfigValue<String> VENT_TAP_SALT_ITEM = BUILDER
            .comment("The item a Thermal Vent Tap leaves behind for each ventTapSaltWaterPerSalt mB it boils. Leave it empty, or set it to minecraft:air, for none. An id that is not a registered item falls back to nautec:salt")
            .define("ventTapSaltItem", "nautec:salt", entry -> entry instanceof String);

    private static final ModConfigSpec.IntValue COMBUSTION_DYNAMO_OUTPUT = BUILDER
            .comment("The FE per tick a Combustion Dynamo makes while it burns oil and water, before any additive")
            .defineInRange("combustionDynamoOutput", 320, 0, 10_000_000);

    private static final ModConfigSpec.IntValue COMBUSTION_DYNAMO_TICKS_PER_OIL = BUILDER
            .comment("How many ticks a Combustion Dynamo runs on each mB of oil, before any additive")
            .defineInRange("combustionDynamoTicksPerOil", 10, 1, 100_000);

    private static final ModConfigSpec.IntValue COMBUSTION_DYNAMO_WATER_PER_TICK = BUILDER
            .comment("The mB of water a Combustion Dynamo uses each tick it runs, before any additive")
            .defineInRange("combustionDynamoWaterPerTick", 1, 0, 100_000);

    private static final ModConfigSpec.IntValue COMBUSTION_DYNAMO_TANK_CAPACITY = BUILDER
            .comment("The mB each of a Combustion Dynamo's oil and water tanks holds")
            .defineInRange("combustionDynamoTankCapacity", 8_000, 1_000, 1_000_000);

    private static final ModConfigSpec.LongValue FUSION_IGNITION_ENERGY = BUILDER
            .comment("The AP a cold Fusion Plant must take in through its Laser Injectors before it ignites")
            .defineInRange("fusionIgnitionEnergy", 5_000_000L, 1L, Long.MAX_VALUE);

    private static final ModConfigSpec.IntValue FUSION_FE_PER_AP = BUILDER
            .comment("The FE a running Fusion Plant makes for each AP of injected beam at purity 3.0. Lower purity scales it down")
            .defineInRange("fusionFePerAp", 50, 1, 100_000);

    private static final ModConfigSpec.IntValue FUSION_MAX_OUTPUT = BUILDER
            .comment("The most FE per tick a Fusion Plant can make, however large its chamber and however many Containment Coils it has")
            .defineInRange("fusionMaxOutput", 300_000, 1, Integer.MAX_VALUE / 2);

    private static final ModConfigSpec.IntValue FUSION_COIL_CONTAINMENT = BUILDER
            .comment("The FE per tick each Containment Coil in the shell adds to a Fusion Plant's output ceiling")
            .defineInRange("fusionCoilContainment", 4_000, 0, Integer.MAX_VALUE / 2);

    private static final ModConfigSpec.IntValue FUSION_SATELLITE_CONTAINMENT = BUILDER
            .comment("The FE per tick each satellite crystal in the corners of a 7x7 or 9x9 Fusion Plant chamber adds to both its output ceiling and its maximum output")
            .defineInRange("fusionSatelliteContainment", 100_000, 0, Integer.MAX_VALUE / 8);

    private static final ModConfigSpec.IntValue FUSION_SATELLITE_FE_PER_AP = BUILDER
            .comment("The FE each satellite crystal adds to what a Fusion Plant makes for each AP of injected beam")
            .defineInRange("fusionSatelliteFePerAp", 10, 0, 100_000);

    private static final ModConfigSpec.DoubleValue FUSION_MIN_PURITY = BUILDER
            .comment("The lowest purity a beam entering a Laser Injector must have to count toward a Fusion Plant")
            .defineInRange("fusionMinPurity", 2.0, 0, 10);

    private static final ModConfigSpec.IntValue FUSION_FE_PER_MB = BUILDER
            .comment("The FE a Fusion Plant makes from each mB of Salt Water it burns")
            .defineInRange("fusionFePerMb", 2_000, 1, Integer.MAX_VALUE);

    private static final ModConfigSpec.IntValue FUSION_ENERGY_BUFFER = BUILDER
            .comment("The FE a Fusion Plant controller holds before it throttles down")
            .defineInRange("fusionEnergyBuffer", 4_000_000, 1, Integer.MAX_VALUE);

    private static final ModConfigSpec.IntValue FUEL_CELL_POWER_BASE = BUILDER
            .comment("The base amount of power a Bacterial Fuel Cell emits each tick, before production rate scales it")
            .defineInRange("fuelCellPowerBase", 24, 0, Integer.MAX_VALUE);

    private static final ModConfigSpec.DoubleValue FUEL_CELL_BURN_RATE = BUILDER
            .comment("The base amount of colony consumed by a Bacterial Fuel Cell each tick, before production rate scales it")
            .defineInRange("fuelCellBurnRate", 0.5, 0, 1000);

    private static final ModConfigSpec.DoubleValue FUEL_CELL_MAX_PURITY = BUILDER
            .comment("The purity a Bacterial Fuel Cell emits at when its colony is at the mutation resistance cap")
            .defineInRange("fuelCellMaxPurity", 2.5, 0, 10);

    private static final ModConfigSpec.DoubleValue LENS_PURITY_BONUS = BUILDER
            .comment("The purity a Focusing Lens adds to a beam passing straight through it")
            .defineInRange("lensPurityBonus", 0.5, 0, 10);

    private static final ModConfigSpec.DoubleValue RESONANCE_BASE_CEILING = BUILDER
            .comment("The charge a Resonance Chamber can hold at zero purity. Purity raises this ceiling")
            .defineInRange("resonanceBaseCeiling", 2000.0, 1, Double.MAX_VALUE);

    private static final ModConfigSpec.IntValue RESONANCE_POWER_USAGE = BUILDER
            .comment("The amount of power a Resonance Chamber requires before it starts building charge")
            .defineInRange("resonancePowerUsage", 20, 0, Integer.MAX_VALUE);

    private static final ModConfigSpec.DoubleValue RESONANCE_CRITICAL_LOW = BUILDER
            .comment("The fraction of the stability ceiling at which a Resonance Chamber becomes critical and can craft")
            .defineInRange("resonanceCriticalLow", 0.9, 0, 1);

    private static final ModConfigSpec.DoubleValue RESONANCE_CRITICAL_HIGH = BUILDER
            .comment("The fraction of the stability ceiling above which a Resonance Chamber vents instead of crafting")
            .defineInRange("resonanceCriticalHigh", 1.1, 1, 10);

    private static final ModConfigSpec.IntValue RESONANCE_VENT_COOLDOWN = BUILDER
            .comment("The amount of ticks a Resonance Chamber stays cracked and inert after venting")
            .defineInRange("resonanceVentCooldown", 200, 0, Integer.MAX_VALUE);

    private static final ModConfigSpec.DoubleValue RESONANCE_VENT_RADIUS = BUILDER
            .comment("The radius in blocks a Resonance Chamber vent reaches")
            .defineInRange("resonanceVentRadius", 4.0, 0, 32);

    private static final ModConfigSpec.DoubleValue RESONANCE_VENT_DAMAGE = BUILDER
            .comment("The damage a Resonance Chamber vent deals to everything in range")
            .defineInRange("resonanceVentDamage", 8.0, 0, 1000);

    private static final ModConfigSpec.IntValue PRESSURE_FORGE_DEPTH = BUILDER
            .comment("The Y level at or below which an Abyssal Pressure Forge will run. Recipes may demand deeper still")
            .defineInRange("pressureForgeDepth", 0, -64, 320);

    private static final ModConfigSpec.IntValue PRESSURE_FORGE_WATER_COLUMN = BUILDER
            .comment("How many blocks of water must sit directly above an Abyssal Pressure Forge for it to run")
            .defineInRange("pressureForgeWaterColumn", 8, 0, 320);

    private static final ModConfigSpec.IntValue PRESSURE_FORGE_POWER_USAGE = BUILDER
            .comment("The amount of power an Abyssal Pressure Forge requires each tick")
            .defineInRange("pressureForgePowerUsage", 40, 0, Integer.MAX_VALUE);

    private static final ModConfigSpec.IntValue PRESSURE_FORGE_ACID_USAGE = BUILDER
            .comment("The amount of Etching Acid in mb an Abyssal Pressure Forge consumes per craft")
            .defineInRange("pressureForgeAcidUsage", 250, 0, Integer.MAX_VALUE);

    private static final ModConfigSpec.IntValue PRESSURE_FORGE_CAPACITY = BUILDER
            .comment("The Etching Acid capacity of an Abyssal Pressure Forge")
            .defineInRange("pressureForgeCapacity", 4_000, 1, Integer.MAX_VALUE);

    private static final ModConfigSpec.ConfigValue<List<? extends Integer>> SMELTER_SLOTS = BUILDER
            .comment("How many input slots a Laser Purification Smelter unlocks with no upgrade, then with an Eddy, Surge, Riptide and Maelstrom Upgrade.",
                    "Five whole numbers from 1 to 9")
            .defineList("smelterSlots", List.of(1, 3, 5, 7, 9), () -> 1, entry -> entry instanceof Number number && number.intValue() >= 1 && number.intValue() <= 9);

    private static final ModConfigSpec.ConfigValue<List<? extends Integer>> SMELTER_AP_MULTIPLIERS = BUILDER
            .comment("The AP a Laser Purification Smelter needs per tick for each busy slot, with no upgrade, then with an Eddy, Surge, Riptide and Maelstrom Upgrade.",
                    "The total is divided by the beam's purity when the purity is above 1.0. Five whole numbers, 0 or more")
            .defineList("smelterApMultipliers", List.of(1, 2, 4, 8, 16), () -> 1, entry -> entry instanceof Number number && number.intValue() >= 0);

    private static final ModConfigSpec.IntValue ADVANCED_ANALYZER_POWER_USAGE = BUILDER
            .comment("The beam power in AP an Advanced Bacterial Analyzer needs to run")
            .defineInRange("advancedAnalyzerPowerUsage", 40, 0, Integer.MAX_VALUE);

    private static final ModConfigSpec.DoubleValue ADVANCED_ANALYZER_PURITY = BUILDER
            .comment("The lowest beam purity an Advanced Bacterial Analyzer runs with")
            .defineInRange("advancedAnalyzerPurity", 2.1, 0, 10);

    private static final ModConfigSpec.IntValue ADVANCED_ANALYZER_CRAFTING_SPEED = BUILDER
            .comment("How many ticks an Advanced Bacterial Analyzer takes per dish. All nine dishes run at the same time")
            .defineInRange("advancedAnalyzerCraftingSpeed", 60, 1, Integer.MAX_VALUE);

    private static final ModConfigSpec.IntValue GRAFTING_STATION_POWER_USAGE = BUILDER
            .comment("The beam power in AP a Grafting Station needs to run")
            .defineInRange("graftingStationPowerUsage", 100, 0, Integer.MAX_VALUE);

    private static final ModConfigSpec.DoubleValue GRAFTING_STATION_PURITY = BUILDER
            .comment("The lowest beam purity a Grafting Station runs with")
            .defineInRange("graftingStationPurity", 2.8, 0, 10);

    private static final ModConfigSpec.IntValue GRAFTING_STATION_DURATION = BUILDER
            .comment("How many ticks a Grafting Station takes to grow one colony")
            .defineInRange("graftingStationDuration", 1_200, 1, Integer.MAX_VALUE);

    private static final ModConfigSpec.IntValue GRAFTING_STATION_SALT_WATER_USAGE = BUILDER
            .comment("The Salt Water in mb a Grafting Station uses for each colony")
            .defineInRange("graftingStationSaltWaterUsage", 4_000, 0, Integer.MAX_VALUE);

    private static final ModConfigSpec.IntValue GRAFTING_STATION_CAPACITY = BUILDER
            .comment("The Salt Water capacity of a Grafting Station")
            .defineInRange("graftingStationCapacity", 16_000, 1, Integer.MAX_VALUE);

    private static final ModConfigSpec.DoubleValue ADVANCED_GRAFTING_ANCHOR_SPEED = BUILDER
            .comment("How many times faster a Grafting Station grafts with an Advanced Grafting Anchor fitted (3.0 means +200%)")
            .defineInRange("advancedGraftingAnchorSpeed", 3.0, 1, 100);

    private static final ModConfigSpec.DoubleValue ADVANCED_GRAFTING_ANCHOR_POWER = BUILDER
            .comment("The factor an Advanced Grafting Anchor multiplies the Grafting Station's beam power requirement by")
            .defineInRange("advancedGraftingAnchorPowerMultiplier", 5.0, 1, 100);

    private static final ModConfigSpec.DoubleValue WAVE_JET_THRUST = BUILDER
            .comment("Speed the Wave Jet adds each tick while held under water, until it reaches waveJetMaxSpeed")
            .defineInRange("waveJetThrust", 0.1, 0.001, 1.0);

    private static final ModConfigSpec.DoubleValue WAVE_JET_MAX_SPEED = BUILDER
            .comment("Fastest the Wave Jet pushes you along, in blocks per tick, before water drag takes about a fifth of it back")
            .defineInRange("waveJetMaxSpeed", 0.4, 0.05, 2.0);

    private static final ModConfigSpec.IntValue WAVE_JET_POWER_USAGE = BUILDER
            .comment("The amount of power the Wave Jet uses each tick while running")
            .defineInRange("waveJetPowerUsage", 2, 0, Integer.MAX_VALUE);

    private static final ModConfigSpec.IntValue WAVE_JET_LIGHT_RANGE = BUILDER
            .comment("How far ahead the Wave Jet spotlight throws its pool of light, in blocks")
            .defineInRange("waveJetLightRange", 12, 1, 32);

    private static final ModConfigSpec.IntValue WAVE_JET_LIGHT_LEVEL = BUILDER
            .comment("Light level the Wave Jet spotlight casts where it lands, 0 to disable world lighting and leave only the visible cone")
            .defineInRange("waveJetLightLevel", 14, 0, 15);

    private static final ModConfigSpec.IntValue WAVE_JET_LIGHT_POWER_USAGE = BUILDER
            .comment("The amount of power the Wave Jet spotlight uses each tick while lit")
            .defineInRange("waveJetLightPowerUsage", 1, 0, Integer.MAX_VALUE);

    private static final ModConfigSpec.IntValue RIFLE_POWER_CAPACITY = BUILDER
            .comment("How much power an Atlantean Rifle holds")
            .defineInRange("riflePowerCapacity", 1_000_000, 1, Integer.MAX_VALUE);

    private static final ModConfigSpec.IntValue RIFLE_MAX_INPUT = BUILDER
            .comment("How much power an Atlantean Rifle accepts per tick while charging")
            .defineInRange("rifleMaxInput", 20_000, 1, Integer.MAX_VALUE);

    private static final ModConfigSpec.IntValue RIFLE_CHARGE_TICKS = BUILDER
            .comment("Ticks the Atlantean Rifle spins up for before the beam appears")
            .defineInRange("rifleChargeTicks", 10, 0, 200);

    private static final ModConfigSpec.IntValue RIFLE_RAMP_TICKS = BUILDER
            .comment("Ticks of continuous fire it takes the Atlantean Rifle to reach full damage and full power draw")
            .defineInRange("rifleRampTicks", 200, 1, Integer.MAX_VALUE);

    private static final ModConfigSpec.DoubleValue RIFLE_BASE_DAMAGE = BUILDER
            .comment("Damage the Atlantean Rifle beam deals every two ticks when it first fires")
            .defineInRange("rifleBaseDamage", 4.0, 0.0, 1024.0);

    private static final ModConfigSpec.DoubleValue RIFLE_MAX_DAMAGE = BUILDER
            .comment("Damage the Atlantean Rifle beam deals every two ticks once fully ramped")
            .defineInRange("rifleMaxDamage", 20.0, 0.0, 1024.0);

    private static final ModConfigSpec.IntValue RIFLE_BASE_DRAIN = BUILDER
            .comment("Power the Atlantean Rifle draws every two ticks when it first fires")
            .defineInRange("rifleBaseDrain", 125, 0, Integer.MAX_VALUE);

    private static final ModConfigSpec.IntValue RIFLE_MAX_DRAIN = BUILDER
            .comment("Power the Atlantean Rifle draws every two ticks once fully ramped")
            .defineInRange("rifleMaxDrain", 625, 0, Integer.MAX_VALUE);

    private static final ModConfigSpec.DoubleValue RIFLE_RANGE = BUILDER
            .comment("How far the Atlantean Rifle beam reaches, in blocks")
            .defineInRange("rifleRange", 128.0, 1.0, 256.0);

    private static final ModConfigSpec.DoubleValue TRIDENT_SHOCKWAVE_RADIUS = BUILDER
            .comment("Radius in blocks of the tidal shockwave Neptune's Trident releases where it lands")
            .defineInRange("tridentShockwaveRadius", 5.0, 0.0, 32.0);

    private static final ModConfigSpec.DoubleValue TRIDENT_SHOCKWAVE_DAMAGE = BUILDER
            .comment("Damage the tidal shockwave deals at its centre. It falls to half at the edge")
            .defineInRange("tridentShockwaveDamage", 14.0, 0.0, 1024.0);

    private static final ModConfigSpec.DoubleValue TRIDENT_SHOCKWAVE_DAMAGE_PER_SHARPNESS = BUILDER
            .comment("Extra tidal shockwave damage per level of Sharpness on the trident")
            .defineInRange("tridentShockwaveDamagePerSharpness", 3.0, 0.0, 1024.0);

    private static final ModConfigSpec.DoubleValue TRIDENT_SHOCKWAVE_KNOCKBACK = BUILDER
            .comment("Knockback strength of the tidal shockwave at its centre. It falls to half at the edge")
            .defineInRange("tridentShockwaveKnockback", 1.2, 0.0, 10.0);

    private static final ModConfigSpec.DoubleValue TRIDENT_SHOCKWAVE_KNOCKBACK_PER_LEVEL = BUILDER
            .comment("Extra tidal shockwave knockback strength per level of Knockback on the trident")
            .defineInRange("tridentShockwaveKnockbackPerLevel", 0.8, 0.0, 10.0);

    private static final ModConfigSpec.IntValue TRIDENT_SHOCKWAVE_COOLDOWN = BUILDER
            .comment("Ticks after a tidal shockwave before Neptune's Trident can release another. It can still be thrown in the meantime")
            .defineInRange("tridentShockwaveCooldown", 60, 0, 72_000);

    private static final ModConfigSpec.IntValue DOCK_POWER_USAGE = BUILDER
            .comment("The amount of power a Sea Scout Dock requires before it will service a hull")
            .defineInRange("dockPowerUsage", 20, 0, Integer.MAX_VALUE);

    private static final ModConfigSpec.IntValue DOCK_CHARGE_RATE = BUILDER
            .comment("The amount of power a Sea Scout Dock pushes into a docked hull each tick")
            .defineInRange("dockChargeRate", 40, 0, Integer.MAX_VALUE);

    private static final ModConfigSpec.IntValue GATEWAY_COOLDOWN = BUILDER
            .comment("The amount of ticks something is barred from using another gateway after arriving, which is what stops it bouncing straight back")
            .defineInRange("gatewayCooldown", 100, 0, Integer.MAX_VALUE);

    private static final ModConfigSpec.IntValue ABYSSAL_EYES_DEPTH = BUILDER
            .comment("The Y level at or below which the Abyssal Eyes augment grants night vision")
            .defineInRange("abyssalEyesDepth", 45, -64, 320);

    private static final ModConfigSpec.DoubleValue PHOTOPHORE_SKIN_RADIUS = BUILDER
            .comment("The radius in which the Photophore Skin augment reveals nearby creatures")
            .defineInRange("photophoreSkinRadius", 12.0, 1.0, 64.0);

    private static final ModConfigSpec.IntValue SUBMARINE_POWER_CAPACITY = BUILDER
            .comment("The power capacity of the submarine")
            .defineInRange("submarinePowerCapacity", 1_000_000, 1, Integer.MAX_VALUE);
    private static final ModConfigSpec.IntValue SUBMARINE_IDLE_POWER_USAGE = BUILDER
            .comment("The amount of power the submarine uses each tick while occupied")
            .defineInRange("submarineIdlePowerUsage", 1, 0, Integer.MAX_VALUE);
    private static final ModConfigSpec.IntValue SUBMARINE_MOVE_POWER_USAGE = BUILDER
            .comment("The extra amount of power the submarine uses each tick while under way")
            .defineInRange("submarineMovePowerUsage", 6, 0, Integer.MAX_VALUE);
    private static final ModConfigSpec.IntValue SUBMARINE_OXYGEN_POWER_USAGE = BUILDER
            .comment("The extra amount of power the submarine uses each tick while keeping its occupants breathing")
            .defineInRange("submarineOxygenPowerUsage", 2, 0, Integer.MAX_VALUE);
    private static final ModConfigSpec.DoubleValue SUBMARINE_SPEED = BUILDER
            .comment("Thrust applied to the submarine each tick while the throttle is open")
            .defineInRange("submarineSpeed", 0.056, 0.001, 1.0);
    private static final ModConfigSpec.DoubleValue SUBMARINE_MAX_SPEED = BUILDER
            .comment("Maximum speed of the submarine in blocks per tick. Values above 1.5 risk tripping server movement checks")
            .defineInRange("submarineMaxSpeed", 0.81, 0.05, 1.5);
    private static final ModConfigSpec.DoubleValue SUBMARINE_FLIGHT_SPEED_MULTIPLIER = BUILDER
            .comment("How much faster a submarine with a Flight Module moves through the air than through water, as a multiplier. The flying top speed is submarineMaxSpeed times this, capped at 1.5")
            .defineInRange("submarineFlightSpeedMultiplier", 1.5, 1.0, 3.0);
    private static final ModConfigSpec.IntValue SUBMARINE_FLIGHT_POWER_USAGE = BUILDER
            .comment("The extra amount of power a submarine with a Flight Module uses each tick while it is out of the water and holding itself up")
            .defineInRange("submarineFlightPowerUsage", 4, 0, Integer.MAX_VALUE);
    private static final ModConfigSpec.DoubleValue SUBMARINE_CAMERA_DISTANCE = BUILDER
            .comment("Third person camera distance while riding the submarine, in blocks. Vanilla default is 4")
            .defineInRange("submarineCameraDistance", 10.0, 4.0, 32.0);
    private static final ModConfigSpec.DoubleValue SUBMARINE_MAX_HEALTH = BUILDER
            .comment("Hull integrity of the submarine, in half hearts")
            .defineInRange("submarineMaxHealth", 80.0, 1.0, 1024.0);
    private static final ModConfigSpec.DoubleValue SUBMARINE_ARMOR = BUILDER
            .comment("Armor points of the submarine hull. Diamond armor is 20")
            .defineInRange("submarineArmor", 20.0, 0.0, 30.0);
    private static final ModConfigSpec.DoubleValue SUBMARINE_ARMOR_TOUGHNESS = BUILDER
            .comment("Armor toughness of the submarine hull. Diamond armor is 8")
            .defineInRange("submarineArmorToughness", 8.0, 0.0, 20.0);
    private static final ModConfigSpec.DoubleValue SUBMARINE_KNOCKBACK_RESISTANCE = BUILDER
            .comment("How strongly the submarine resists being knocked around when it is hit. 1 ignores knockback entirely")
            .defineInRange("submarineKnockbackResistance", 0.0, 0.0, 1.0);
    private static final ModConfigSpec.IntValue SUBMARINE_AUTOREPAIR_INTERVAL = BUILDER
            .comment("Ticks between hull self repair pulses. Set to 0 to disable self repair")
            .defineInRange("submarineAutorepairIntervalTicks", 200, 0, Integer.MAX_VALUE);
    private static final ModConfigSpec.DoubleValue SUBMARINE_AUTOREPAIR_PERCENT = BUILDER
            .comment("Fraction of the hull restored by each self repair pulse")
            .defineInRange("submarineAutorepairPercent", 0.01, 0.0, 1.0);
    private static final ModConfigSpec.ConfigValue<String> SUBMARINE_REPAIR_ITEM = BUILDER
            .comment("The item that repairs a submarine in an anvil")
            .define("submarineRepairItem", "nautec:deep_steel_plating");
    private static final ModConfigSpec.DoubleValue SUBMARINE_REPAIR_PERCENT = BUILDER
            .comment("Fraction of the hull restored by each repair item used in an anvil")
            .defineInRange("submarineRepairPercent", 0.20, 0.01, 1.0);

    private static final ModConfigSpec.DoubleValue SUBMARINE_SOLAR_PERCENT = BUILDER
            .comment("Percent of the submarine's power capacity the Solar Module collects every 5 seconds in open sunlit water")
            .defineInRange("submarineSolarPercentPer5s", 0.2, 0.0, 100.0);
    private static final ModConfigSpec.IntValue SUBMARINE_BOOST_POWER = BUILDER
            .comment("Power drawn by one activation of the Booster Module")
            .defineInRange("submarineBoostPowerCost", 20_000, 0, Integer.MAX_VALUE);
    private static final ModConfigSpec.IntValue SUBMARINE_BOOST_DURATION = BUILDER
            .comment("Ticks the Booster Module stays lit")
            .defineInRange("submarineBoostDurationTicks", 200, 1, Integer.MAX_VALUE);
    private static final ModConfigSpec.IntValue SUBMARINE_BOOST_COOLDOWN = BUILDER
            .comment("Ticks after the boost expires before it can be used again")
            .defineInRange("submarineBoostCooldownTicks", 60, 0, Integer.MAX_VALUE);
    private static final ModConfigSpec.DoubleValue SUBMARINE_BOOST_SPEED = BUILDER
            .comment("Extra speed the Booster Module gives, as a fraction of the base speed")
            .defineInRange("submarineBoostSpeedBonus", 0.45, 0.0, 4.0);
    private static final ModConfigSpec.IntValue SUBMARINE_STEALTH_POWER = BUILDER
            .comment("Power drawn by one activation of the Stealth Module")
            .defineInRange("submarineStealthPowerCost", 50_000, 0, Integer.MAX_VALUE);
    private static final ModConfigSpec.IntValue SUBMARINE_STEALTH_DURATION = BUILDER
            .comment("Ticks the Stealth Module hides the submarine from mobs")
            .defineInRange("submarineStealthDurationTicks", 2_400, 1, Integer.MAX_VALUE);
    private static final ModConfigSpec.IntValue SUBMARINE_STEALTH_COOLDOWN = BUILDER
            .comment("Ticks after stealth expires before it can be used again")
            .defineInRange("submarineStealthCooldownTicks", 200, 0, Integer.MAX_VALUE);
    private static final ModConfigSpec.DoubleValue SUBMARINE_STEALTH_SLOW = BUILDER
            .comment("Speed the Stealth Module gives up while it is running, as a fraction of the base speed")
            .defineInRange("submarineStealthSpeedPenalty", 0.15, 0.0, 0.9);
    private static final ModConfigSpec.IntValue SUBMARINE_SONAR_POWER = BUILDER
            .comment("Power drawn by one Sonar Module ping")
            .defineInRange("submarineSonarPowerCost", 30_000, 0, Integer.MAX_VALUE);
    private static final ModConfigSpec.IntValue SUBMARINE_SONAR_COOLDOWN = BUILDER
            .comment("Ticks between Sonar Module pings")
            .defineInRange("submarineSonarCooldownTicks", 900, 0, Integer.MAX_VALUE);
    private static final ModConfigSpec.DoubleValue SUBMARINE_SONAR_RADIUS = BUILDER
            .comment("Radius in blocks the Sonar Module marks hostile creatures in")
            .defineInRange("submarineSonarRadius", 32.0, 1.0, 128.0);
    private static final ModConfigSpec.IntValue SUBMARINE_SHIELD_POWER = BUILDER
            .comment("Power drawn by one Shield Module discharge")
            .defineInRange("submarineShieldPowerCost", 25_000, 0, Integer.MAX_VALUE);
    private static final ModConfigSpec.IntValue SUBMARINE_SHIELD_COOLDOWN = BUILDER
            .comment("Ticks between Shield Module discharges")
            .defineInRange("submarineShieldCooldownTicks", 100, 0, Integer.MAX_VALUE);
    private static final ModConfigSpec.IntValue SUBMARINE_SHIELD_STUN = BUILDER
            .comment("Ticks a Shield Module discharge stuns the creatures it hits")
            .defineInRange("submarineShieldStunTicks", 60, 0, Integer.MAX_VALUE);
    private static final ModConfigSpec.DoubleValue SUBMARINE_SHIELD_DAMAGE = BUILDER
            .comment("Damage a Shield Module discharge deals")
            .defineInRange("submarineShieldDamage", 10.0, 0.0, 1024.0);
    private static final ModConfigSpec.DoubleValue SUBMARINE_SHIELD_RADIUS = BUILDER
            .comment("Radius in blocks of a Shield Module discharge")
            .defineInRange("submarineShieldRadius", 5.0, 1.0, 32.0);
    private static final ModConfigSpec.IntValue SUBMARINE_SHIELD_POWER_PER_HEART = BUILDER
            .comment("Power an installed Shield Module burns to absorb one heart of incoming damage")
            .defineInRange("submarineShieldPowerPerHeart", 10_000, 1, Integer.MAX_VALUE);
    private static final ModConfigSpec.IntValue SUBMARINE_LASER_CHARGE_TICKS = BUILDER
            .comment("Ticks the Impulse Laser charges after the fire key is pressed before its beams appear")
            .defineInRange("submarineLaserChargeTicks", 10, 0, 200);
    private static final ModConfigSpec.IntValue SUBMARINE_LASER_RAMP_TICKS = BUILDER
            .comment("Ticks of continuous fire the Impulse Laser takes to reach full width, damage and power draw")
            .defineInRange("submarineLaserRampTicks", 100, 1, 2400);
    private static final ModConfigSpec.IntValue SUBMARINE_LASER_MIN_DRAIN = BUILDER
            .comment("Power the Impulse Laser draws each tick when it starts firing")
            .defineInRange("submarineLaserMinDrain", 400, 0, Integer.MAX_VALUE);
    private static final ModConfigSpec.IntValue SUBMARINE_LASER_MAX_DRAIN = BUILDER
            .comment("Power the Impulse Laser draws each tick once fully ramped")
            .defineInRange("submarineLaserMaxDrain", 1_400, 0, Integer.MAX_VALUE);
    private static final ModConfigSpec.DoubleValue SUBMARINE_LASER_RANGE = BUILDER
            .comment("Range in blocks of the Impulse Laser beams")
            .defineInRange("submarineLaserRange", 64.0, 1.0, 128.0);
    private static final ModConfigSpec.DoubleValue SUBMARINE_LASER_MIN_DAMAGE = BUILDER
            .comment("Damage each Impulse Laser beam deals per hit when it starts firing. A beam hits every 5 ticks")
            .defineInRange("submarineLaserMinDamage", 2.0, 0.0, 1024.0);
    private static final ModConfigSpec.DoubleValue SUBMARINE_LASER_MAX_DAMAGE = BUILDER
            .comment("Damage each Impulse Laser beam deals per hit once fully ramped")
            .defineInRange("submarineLaserMaxDamage", 6.0, 0.0, 1024.0);
    private static final ModConfigSpec.DoubleValue SUBMARINE_LASER_MAX_HEALTH_PERCENT = BUILDER
            .comment("Extra Impulse Laser damage per hit once fully ramped, as a fraction of the target's maximum health. It starts at a third of this")
            .defineInRange("submarineLaserMaxHealthPercent", 0.0125, 0.0, 1.0);
    private static final ModConfigSpec.IntValue SUBMARINE_TELEPORT_POWER = BUILDER
            .comment("Power drawn by one Teleport Module jump")
            .defineInRange("submarineTeleportPowerCost", 200_000, 0, Integer.MAX_VALUE);
    private static final ModConfigSpec.DoubleValue SUBMARINE_TELEPORT_MIN_POWER = BUILDER
            .comment("Fraction of the power capacity that must be stored before a jump may be started")
            .defineInRange("submarineTeleportMinPowerPercent", 0.20, 0.0, 1.0);
    private static final ModConfigSpec.IntValue SUBMARINE_TELEPORT_COOLDOWN = BUILDER
            .comment("Ticks between Teleport Module jumps")
            .defineInRange("submarineTeleportCooldownTicks", 600, 0, Integer.MAX_VALUE);
    private static final ModConfigSpec.DoubleValue SUBMARINE_ARMOR_MODULE_TOUGHNESS = BUILDER
            .comment("Armor toughness of the hull while an Armor Module is installed. Netherite armor is 12")
            .defineInRange("submarineArmorModuleToughness", 12.0, 0.0, 20.0);

    private static final ModConfigSpec.BooleanValue LUCKY_ZONES_ENABLED = BUILDER
            .comment("Whether lucky fishing zones appear on the water around players")
            .define("luckyZonesEnabled", true);
    private static final ModConfigSpec.IntValue LUCKY_ZONE_INTERVAL = BUILDER
            .comment("Seconds between attempts to place a lucky fishing zone near a player")
            .defineInRange("luckyZoneIntervalSeconds", 45, 1, 3600);
    private static final ModConfigSpec.IntValue LUCKY_ZONE_SPAWN_DISTANCE = BUILDER
            .comment("How far from the player a lucky fishing zone may appear")
            .defineInRange("luckyZoneSpawnDistance", 32, 4, 128);
    private static final ModConfigSpec.IntValue LUCKY_ZONE_MIN_SEPARATION = BUILDER
            .comment("Minimum distance between two lucky fishing zones")
            .defineInRange("luckyZoneMinSeparation", 48, 4, 256);
    private static final ModConfigSpec.IntValue LUCKY_ZONE_PER_CHUNK = BUILDER
            .comment("Maximum lucky fishing zones in a single chunk")
            .defineInRange("luckyZonesPerChunk", 1, 1, 16);
    private static final ModConfigSpec.IntValue LUCKY_ZONE_LIFETIME = BUILDER
            .comment("Seconds a lucky fishing zone lasts before fading away")
            .defineInRange("luckyZoneLifetimeSeconds", 300, 10, 36000);
    private static final ModConfigSpec.IntValue LUCKY_ZONE_MIN_RADIUS = BUILDER
            .comment("Smallest lucky fishing zone radius. Every block in the radius must be open water")
            .defineInRange("luckyZoneMinRadius", 2, 1, 16);
    private static final ModConfigSpec.IntValue LUCKY_ZONE_MAX_RADIUS = BUILDER
            .comment("Largest lucky fishing zone radius. Every block in the radius must be open water")
            .defineInRange("luckyZoneMaxRadius", 6, 1, 16);
    private static final ModConfigSpec.BooleanValue LUCKY_ZONE_CONSUMED = BUILDER
            .comment("Whether a lucky fishing zone disappears after one successful catch")
            .define("luckyZoneConsumedOnCatch", true);
    private static final ModConfigSpec.IntValue LUCKY_ZONE_BITE_SPEED = BUILDER
            .comment("How many times faster fish bite while the bobber floats in a lucky fishing zone")
            .defineInRange("luckyZoneBiteSpeed", 2, 1, 10);
    private static final ModConfigSpec.BooleanValue STARCATCHER_FISH_ON_NAUTEC_ROD = BUILDER
            .comment("With Starcatcher installed, the NauTec Fishing Rod catches Starcatcher fish instead of vanilla fishing loot")
            .define("starcatcherFishOnNautecRod", true);

    private static final ModConfigSpec.IntValue EYE_OF_THE_SEA_SEARCH_RADIUS = BUILDER
            .comment("How many chunks around the player the Eye of the Sea searches for its target structure. The Eye of Ender uses 100")
            .defineInRange("eyeOfTheSeaSearchRadius", 100, 1, 256);
    private static final ModConfigSpec.IntValue EYE_OF_THE_SEA_COOLDOWN = BUILDER
            .comment("Ticks before the Eye of the Sea can be thrown again")
            .defineInRange("eyeOfTheSeaCooldownTicks", 40, 0, 72000);
    private static final ModConfigSpec.IntValue EYE_OF_THE_SEA_LUCKY_BOOST = BUILDER
            .comment("Seconds after throwing an Eye of the Sea during which lucky fishing zones form more often near the thrower")
            .defineInRange("eyeOfTheSeaLuckyBoostSeconds", 300, 0, 36000);
    private static final ModConfigSpec.IntValue EYE_OF_THE_SEA_LUCKY_MULTIPLIER = BUILDER
            .comment("How many times more often lucky fishing zones form near a player during the Eye of the Sea boost")
            .defineInRange("eyeOfTheSeaLuckyZoneMultiplier", 2, 1, 10);

    private static final ModConfigSpec.BooleanValue DIVING_SUIT_ACCEPTS_OXYGEN_FLUID = BUILDER
            .comment("Determines whether the Diving Chestplate can be filled with any fluid in the c:oxygen tag, such as Stellaris oxygen. 1 mB fills 1 second of air")
            .define("divingSuitAcceptsOxygenFluid", true);
    private static final ModConfigSpec.BooleanValue BACTERIA_PRODUCT_NUTRIENTS = BUILDER
            .comment("Determines whether every strain with an incubation recipe also eats the item it makes and that item's storage block, in the Incubator and in Bio Reactors")
            .define("bacteriaProductNutrients", true);
    private static final ModConfigSpec.DoubleValue BACTERIA_PRODUCT_CONSUME_MULTIPLIER = BUILDER
            .comment("Multiplies a strain's incubation consume chance when it is fed the item it makes. A storage block is used up a ninth as often as that")
            .defineInRange("bacteriaProductConsumeMultiplier", 1.5, 0.01, 100.0);
    private static final ModConfigSpec.BooleanValue AIR_BOTTLES_FILL_OXYGEN_HELMETS = BUILDER
            .comment("Determines whether drinking an Air Bottle also adds oxygen to a worn helmet from another mod that holds c:oxygen fluid, such as a Stellaris space suit helmet")
            .define("airBottlesFillOxygenHelmets", true);
    private static final ModConfigSpec.BooleanValue DIVING_SUIT_PROTECTS_IN_SPACE = BUILDER
            .comment("Determines whether a full Diving Suit with air in its tank protects against Stellaris oxygen deprivation, using up its air instead")
            .define("divingSuitProtectsInSpace", true);

    private static final ModConfigSpec.BooleanValue ENABLE_BIOME_INJECTION = WORLDGEN_BUILDER
            .comment("Determines whether Nautec's ocean biomes are added to the world's biome layout. Turning this off leaves vanilla oceans untouched",
                    "This only applies when Lithostitched is absent. With Lithostitched installed, placement comes from the biome injectors in data/nautec/lithostitched/biome_injector, which a datapack can override or empty out")
            .define("enableBiomeInjection", true);

    private static final ModConfigSpec.ConfigValue<List<? extends String>> INJECTABLE_WORLD_PRESETS = WORLDGEN_BUILDER
            .comment("The multi-noise presets Nautec's ocean biomes are added to. Packs using a custom overworld preset should list it here",
                    "Ignored when Lithostitched is installed, since the biome injectors target the overworld dimension directly and work with Terralith and Tectonic")
            .defineList("injectableWorldPresets", List.of("minecraft:overworld"), () -> "minecraft:overworld", entry -> entry instanceof String);

    private static final ModConfigSpec.BooleanValue SKYBLOCK_LUCKY_ZONES_ANY_BIOME = WORLDGEN_BUILDER
            .comment("Options for skyblock and void-world packs, where NauTec's oceans, structures and ocean mobs do not exist. Every option is off by default and each one can be turned on alone",
                    "The recipe options take effect on the next world load or /reload. Datapacks can check any of them with the nautec:skyblock_option recipe condition or loot condition",
                    "The Deep Sea Drain and Tidal Rotor ocean check is a separate per-world setting, drainRequiresOcean in nautec-common.toml")
            .push("skyblock")
            .comment("Determines whether lucky fishing zones can appear on any open water instead of only in ocean and river biomes. Zones still need a ring of open water around them, so a player-built pool works and a bigger pool gets more zones")
            .define("luckyZonesAnyBiome", false);

    private static final ModConfigSpec.BooleanValue SKYBLOCK_FISHING_LOOT = WORLDGEN_BUILDER
            .comment("Determines whether lucky fishing zone catches count as ocean catches in any biome, and adds Prismarine Sand, Prismarine Shards and the Heart of the Sea to the zone loot. Catches from NauTec's own biomes are unchanged")
            .define("skyblockFishingLoot", false);

    private static final ModConfigSpec.BooleanValue SKYBLOCK_GRAFTING_ANY_BIOME = WORLDGEN_BUILDER
            .comment("Determines whether the Grafting Tool ignores the biome listed in the nautec:bacteria_obtaining data map. The block and the chance still apply")
            .define("graftingAnyBiome", false);

    private static final ModConfigSpec.BooleanValue SKYBLOCK_PRISMARINE_SAND_MIXING = WORLDGEN_BUILDER
            .comment("Enables a Mixer recipe that makes Prismarine Sand from sand, a Prismarine Crystal Shard and Salt Water")
            .define("prismarineSandMixing", false);

    private static final ModConfigSpec.BooleanValue SKYBLOCK_BUDDING_PRISMARINE_MIXING = WORLDGEN_BUILDER
            .comment("Enables a Mixer recipe that makes Budding Prismarine from Prismarine Bricks, Prismarine Crystals, Prismarine Crystal Shards and Salt Water")
            .define("buddingPrismarineMixing", false);

    private static final ModConfigSpec.BooleanValue SKYBLOCK_FUEL_CELL_RESONANCE = WORLDGEN_BUILDER
            .comment("Enables Laser Crafting Matrix recipes that make Resonant Shards and Prismarine Crystal Seeds at purity 2.5, which a Bacterial Fuel Cell can reach without a Prismarine Crystal. The colony needs full mutation resistance to get there")
            .define("fuelCellResonance", false);

    private static final ModConfigSpec.BooleanValue SKYBLOCK_ELDRITCH_HEART_MIXING = WORLDGEN_BUILDER
            .comment("Enables a Mixer recipe that makes an Eldritch Heart from a Heart of the Sea, Resonant Shards, Prismarine Crystal Shards and EAS")
            .define("eldritchHeartMixing", false);

    static {
        WORLDGEN_BUILDER.pop();
    }

    private static final ModConfigSpec.DoubleValue CATALYST_BUDDING_OUTPUT_BONUS = BUILDER
            .comment("The extra AP output an Aquatic Catalyst gets for each Budding Prismarine block touching it, as a fraction of its normal output")
            .defineInRange("catalystBuddingOutputBonus", 0.25, 0.0, 10.0);

    private static final ModConfigSpec.DoubleValue CATALYST_BUDDING_PURITY_BONUS = BUILDER
            .comment("The purity an Aquatic Catalyst's beam gains for each Budding Prismarine block touching it")
            .defineInRange("catalystBuddingPurityBonus", 0.1, 0.0, 10.0);

    private static final ModConfigSpec.DoubleValue CATALYST_BUDDING_MAX_PURITY = BUILDER
            .comment("The highest purity Budding Prismarine can raise an Aquatic Catalyst's beam to")
            .defineInRange("catalystBuddingMaxPurity", 2.0, 0.0, 10.0);

    private static final ModConfigSpec.IntValue CONDUIT_BEACON_POWER_USAGE = BUILDER
            .comment("The AP a running Conduit Beacon uses each tick, taken from its internal buffer")
            .defineInRange("conduitBeaconPowerUsage", 50, 0, Integer.MAX_VALUE);

    private static final ModConfigSpec.IntValue CONDUIT_BEACON_BUFFER = BUILDER
            .comment("The AP a Conduit Beacon stores. Incoming laser power fills it, and the beacon keeps running from it after the beam stops")
            .defineInRange("conduitBeaconBuffer", 5000, 1, Integer.MAX_VALUE);

    private static final ModConfigSpec.IntValue CONDUIT_BEACON_SPAWN_RADIUS = BUILDER
            .comment("The horizontal radius in blocks around a running Conduit Beacon where hostile, passive and ambient mobs do not spawn naturally")
            .defineInRange("conduitBeaconSpawnRadius", 128, 0, 1024);

    private static final ModConfigSpec.IntValue BIOME_TANK_TICKS = BUILDER
            .comment("The ticks a Biome Tank takes to grow one of its plant")
            .defineInRange("biomeTankTicks", 600, 1, Integer.MAX_VALUE);

    static final ModConfigSpec SPEC = BUILDER.build();
    static final ModConfigSpec WORLDGEN_SPEC = WORLDGEN_BUILDER.build();

    public static int biomeTankTicks = 600;

    public static boolean skyblockLuckyZonesAnyBiome;
    public static boolean skyblockFishingLoot;
    public static boolean skyblockGraftingAnyBiome;
    public static boolean skyblockPrismarineSandMixing;
    public static boolean skyblockBuddingPrismarineMixing;
    public static boolean skyblockFuelCellResonance;
    public static boolean skyblockEldritchHeartMixing;

    public static int conduitBeaconPowerUsage = 50;
    public static int conduitBeaconBuffer = 5000;
    public static int conduitBeaconSpawnRadius = 128;

    public static double catalystBuddingOutputBonus = 0.25;
    public static double catalystBuddingPurityBonus = 0.1;
    public static double catalystBuddingMaxPurity = 2.0;

    public static int kelpHeight;
    public static boolean spawnBookInInventory;
    public static boolean collectSaltWater;
    public static boolean collectAirWithBottle;

    public static int mixerPower;
    public static int drainPower;
    public static boolean beamOverclock = true;
    public static double beamOverclockMaxSpeed = 0.0;
    public static int augmentationStationPower;
    public static int laserDistance;
    public static int relayLaserDistance;
    public static int energyConverterFePerAp = 10;
    public static int energyConverterFeCapacity = 4_000_000;
    public static int energyConverterBaseAp = 150;
    public static int energyConversionUpgradeAp = 150;
    public static int advancedEnergyConversionUpgradeAp = 500;
    public static int ultimateEnergyConversionUpgradeAp = 1500;
    public static double beamMergePurityDrop = 0.25;

    public static int mixerInputCapacity;
    public static int mixerOutputCapacity;
    public static int laserCraftingMatrixTankCapacity = 16_000;

    public static int drainSaltWaterAmount;
    public static int drainCapacity;
    public static boolean drainRequiresOcean;

    public static int guardianAugmentDamage;
    public static boolean allowAugmentRendering;

    public static int fisherLaserLevel;
    public static int fisherRunDuration;
    public static int fisherRadius;
    public static int fisherDepth;

    public static float bacteriaGrowthRateCap;
    public static float bacteriaProductionRateCap;
    public static float bacteriaMutationResistanceCap;
    public static long bacteriaColonySizeCap;
    public static int bacteriaLifespanCap;
    public static List<String> preferredTagNamespaces;

    public static int bacteriaAnalyzerCraftingSpeed;
    public static int bacteriaAnalyzerPowerUsage;

    public static int mutatorCraftingSpeed;
    public static int mutatorPowerUsage;
    public static double mutatorFailureShrink = 0.25;
    public static double mutatorRefineChance = 25.0;
    public static double mutatorBoosterMultiplier = 3.0;
    public static double mutatorBoostedFailureShrink = 0.0;

    public static int incubatorCraftingSpeed = 100;
    public static double incubatorSizeGrowth = 0.03;
    public static int incubatorPowerUsage = 20;

    public static double bioReactorBaseSpeed = 16.8;
    public static int bioReactorPowerBase = 25;
    public static int bioReactorPowerPerColony = 25;
    public static double bioReactorDecayPerSecond = 0.02;
    public static double industrialBioReactorBaseSpeed = 16.8;
    public static int industrialBioReactorPowerBase = 100;
    public static int industrialBioReactorPowerPerColony = 50;
    public static double reactorSpeedUpgradeBonus = 0.5;
    public static double reactorSpeedUpgradePowerMultiplier = 1.6;
    public static int reactorYieldUpgradeBonus = 1;
    public static double reactorYieldUpgradePowerMultiplier = 1.8;
    public static double reactorEfficiencyUpgradeFactor = 0.75;
    public static double reactorEfficiencyUpgradeFloor = 0.25;
    public static double reactorEfficiencyUpgradePowerMultiplier = 1.2;
    public static double reactorFusionUpgradePowerMultiplier = 2.5;
    public static long crystalGrowthPower = 10_000_000L;
    public static double crystalGrowthPurity = 2.0;
    public static int confinedSpawnerPowerPerTick = 50;
    public static int confinedSpawnerPowerBuffer = 500;
    public static int confinedSpawnerXpCapacity = 16_000;
    public static int confinedSpawnerXpRatio = 20;
    public static List<String> confinedSpawnerXpFluids = List.of("nautec:experience_algae");
    public static int resonanceMaxNetworksPerPlayer = 16;
    public static int charmTransferRate = 2_000;
    public static int emitterRange = 64;
    public static int emitterThroughput = 10_000;
    public static int emitterBuffer = 100_000;
    public static int emitterMaxLinks = 16;
    public static double satelliteLoss = 0.10;
    public static int replicatorPowerUsage = 80;
    public static double replicatorPurity = 2.0;
    public static int replicatorDuration = 600;
    public static long replicatorBiomassCost = 20_000L;
    public static long replicatorBiomassCap = 200_000L;
    public static double replicatorErrorChance = 0.5;
    public static double replicatorSpliceChance = 0.7;
    public static double replicatorBiomassPerItem = 100.0;
    public static double replicatorCopySize = 0.5;
    public static int oxygenDiffuserPower = 10;
    public static int oxygenDiffuserRadius = 12;
    public static int bubbleAnchorRadius = 7;
    public static int bubbleAnchorMaxRadius = 12;
    public static int bubbleAnchorLaserPower = 20;
    public static double bubbleAnchorFuelMultiplier = 1.0;
    public static int bubbleAnchorBlocksPerTick = 1024;
    public static int bubbleCapsuleRadius = 2;
    public static int bubbleCapsuleSeconds = 30;
    public static int distributorRange = 256;
    public static int distributorMaxLinks = 64;
    public static int distributorInterval = 4;
    public static int[] conduitItemIntervals = {20, 10, 5, 2, 1};
    public static int[] conduitItemsPerOp = {8, 16, 32, 64, 64};
    public static int[] conduitFluidRates = {250, 1_000, 4_000, 16_000, 64_000};
    public static int[] conduitEnergyRates = {2_000, 8_000, 32_000, 128_000, 0};
    public static int conduitMaxNetworkSize = 4096;
    public static int resonantCisternTierCapacity = 16_000;
    public static int resonantTransferInterval = 10;
    public static int resonantItemsPerTransfer = 16;
    public static int resonantFluidPerTransfer = 1_000;
    public static int satelliteApBuffer = 5_000_000;
    public static int satelliteFeBuffer = 5_000_000;
    public static int satelliteTransferLimit = 100_000;
    public static double satelliteCrossDimensionPurityLoss = 0.05;
    public static int resonanceNodeApBuffer = 200_000;
    public static int resonanceNodeFeBuffer = 1_000_000;
    public static boolean resonanceChunkLoading = true;
    public static int tidalRotorMinOutput = 40;
    public static int tidalRotorMaxOutput = 80;
    public static int ventTapMinOutput = 350;
    public static int ventTapMaxOutput = 1_250;
    public static int ventTapSaltWaterPerTick = 10;
    public static int ventTapSaltWaterPerSalt = 1_000;
    public static String ventTapSaltItem = "nautec:salt";
    public static int combustionDynamoOutput = 320;
    public static int combustionDynamoTicksPerOil = 10;
    public static int combustionDynamoWaterPerTick = 1;
    public static int combustionDynamoTankCapacity = 8_000;
    public static long fusionIgnitionEnergy = 5_000_000L;
    public static int fusionFePerAp = 50;
    public static int fusionMaxOutput = 300_000;
    public static int fusionCoilContainment = 4_000;
    public static int fusionSatelliteContainment = 100_000;
    public static int fusionSatelliteFePerAp = 10;
    public static double fusionMinPurity = 2.0;
    public static int fusionFePerMb = 2_000;
    public static int fusionEnergyBuffer = 4_000_000;
    public static int fuelCellPowerBase = 24;
    public static double fuelCellBurnRate = 0.5;
    public static double fuelCellMaxPurity = 2.5;
    public static double lensPurityBonus = 0.5;
    public static double resonanceBaseCeiling = 2000.0;
    public static int resonancePowerUsage = 20;
    public static double resonanceCriticalLow = 0.9;
    public static double resonanceCriticalHigh = 1.1;
    public static int resonanceVentCooldown = 200;
    public static double resonanceVentRadius = 4.0;
    public static double resonanceVentDamage = 8.0;
    public static int gatewayCooldown = 100;
    public static double waveJetThrust = 0.1;
    public static double waveJetMaxSpeed = 0.4;
    public static int waveJetPowerUsage = 2;
    public static int waveJetLightRange = 12;
    public static int waveJetLightLevel = 14;
    public static int waveJetLightPowerUsage = 1;
    public static int riflePowerCapacity = 1_000_000;
    public static int rifleMaxInput = 20_000;
    public static int rifleChargeTicks = 10;
    public static int rifleRampTicks = 200;
    public static double rifleBaseDamage = 4.0;
    public static double rifleMaxDamage = 20.0;
    public static int rifleBaseDrain = 125;
    public static int rifleMaxDrain = 625;
    public static double rifleRange = 128.0;
    public static double tridentShockwaveRadius = 5.0;
    public static double tridentShockwaveDamage = 14.0;
    public static double tridentShockwaveDamagePerSharpness = 3.0;
    public static double tridentShockwaveKnockback = 1.2;
    public static double tridentShockwaveKnockbackPerLevel = 0.8;
    public static int tridentShockwaveCooldown = 60;
    public static int dockPowerUsage = 20;
    public static int dockChargeRate = 40;
    public static int pressureForgeDepth = 0;
    public static int pressureForgeWaterColumn = 8;
    public static int pressureForgePowerUsage = 40;
    public static int pressureForgeAcidUsage = 250;
    public static int pressureForgeCapacity = 4_000;
    public static int[] smelterSlots = {1, 3, 5, 7, 9};
    public static int[] smelterApMultipliers = {1, 2, 4, 8, 16};

    public static int advancedAnalyzerPowerUsage = 40;
    public static double advancedAnalyzerPurity = 2.1;
    public static int advancedAnalyzerCraftingSpeed = 60;
    public static int graftingStationPowerUsage = 100;
    public static double graftingStationPurity = 2.8;
    public static int graftingStationDuration = 1_200;
    public static int graftingStationSaltWaterUsage = 4_000;
    public static int graftingStationCapacity = 16_000;
    public static double advancedGraftingAnchorSpeed = 3.0;
    public static double advancedGraftingAnchorPowerMultiplier = 5.0;

    public static boolean luckyZonesEnabled;
    public static int luckyZoneIntervalSeconds;
    public static int luckyZoneSpawnDistance;
    public static int luckyZoneMinSeparation;
    public static int luckyZonesPerChunk;
    public static int luckyZoneLifetimeSeconds;
    public static int luckyZoneMinRadius;
    public static int luckyZoneMaxRadius;
    public static boolean luckyZoneConsumedOnCatch;
    public static int luckyZoneBiteSpeed;
    public static boolean starcatcherFishOnNautecRod = true;
    public static int eyeOfTheSeaSearchRadius = 100;
    public static int eyeOfTheSeaCooldownTicks = 40;
    public static int eyeOfTheSeaLuckyBoostSeconds = 300;
    public static int eyeOfTheSeaLuckyZoneMultiplier = 2;
    public static boolean divingSuitAcceptsOxygenFluid = true;
    public static boolean bacteriaProductNutrients = true;
    public static double bacteriaProductConsumeMultiplier = 1.5;
    public static boolean airBottlesFillOxygenHelmets = true;
    public static boolean divingSuitProtectsInSpace = true;

    public static int abyssalEyesDepth;
    public static double photophoreSkinRadius;

    public static int submarinePowerCapacity = 1_000_000;
    public static int submarineIdlePowerUsage = 1;
    public static int submarineMovePowerUsage = 6;
    public static int submarineOxygenPowerUsage = 2;
    public static double submarineSpeed = 0.056;
    public static double submarineMaxSpeed = 0.81;
    public static double submarineFlightSpeedMultiplier = 1.5;
    public static int submarineFlightPowerUsage = 4;
    public static double submarineCameraDistance = 10.0;
    public static double submarineMaxHealth = 80.0;
    public static double submarineArmor = 20.0;
    public static double submarineArmorToughness = 8.0;
    public static double submarineKnockbackResistance = 0.0;
    public static int submarineAutorepairIntervalTicks = 200;
    public static double submarineAutorepairPercent = 0.01;
    public static String submarineRepairItem = "minecraft:diamond";
    public static double submarineRepairPercent = 0.20;

    public static double submarineSolarPercentPer5s = 0.2;
    public static int submarineBoostPowerCost = 20_000;
    public static int submarineBoostDurationTicks = 200;
    public static int submarineBoostCooldownTicks = 60;
    public static double submarineBoostSpeedBonus = 0.45;
    public static int submarineStealthPowerCost = 50_000;
    public static int submarineStealthDurationTicks = 2_400;
    public static int submarineStealthCooldownTicks = 200;
    public static double submarineStealthSpeedPenalty = 0.15;
    public static int submarineSonarPowerCost = 30_000;
    public static int submarineSonarCooldownTicks = 900;
    public static double submarineSonarRadius = 32.0;
    public static int submarineShieldPowerCost = 25_000;
    public static int submarineShieldCooldownTicks = 100;
    public static int submarineShieldStunTicks = 60;
    public static double submarineShieldDamage = 10.0;
    public static double submarineShieldRadius = 5.0;
    public static int submarineShieldPowerPerHeart = 10_000;
    public static int submarineLaserChargeTicks = 10;
    public static int submarineLaserRampTicks = 100;
    public static int submarineLaserMinDrain = 400;
    public static int submarineLaserMaxDrain = 1_400;
    public static double submarineLaserRange = 64.0;
    public static double submarineLaserMinDamage = 2.0;
    public static double submarineLaserMaxDamage = 6.0;
    public static double submarineLaserMaxHealthPercent = 0.0125;
    public static int submarineTeleportPowerCost = 200_000;
    public static double submarineTeleportMinPowerPercent = 0.20;
    public static int submarineTeleportCooldownTicks = 600;
    public static double submarineArmorModuleToughness = 12.0;

    public static boolean biomeInjectionEnabled() {
        return !WORLDGEN_SPEC.isLoaded() || ENABLE_BIOME_INJECTION.getAsBoolean();
    }

    public static Set<String> injectableWorldPresets() {
        if (!WORLDGEN_SPEC.isLoaded()) {
            return Set.of("minecraft:overworld");
        }
        return Set.copyOf(INJECTABLE_WORLD_PRESETS.get());
    }

    static {
        loadValues();
        loadWorldgenValues();
    }

    private static ModConfigSpec.IntValue[] conduitValues(String key, String comment, int[] defaults, int min, int max) {
        ModConfigSpec.IntValue[] values = new ModConfigSpec.IntValue[CONDUIT_TIERS.length];
        for (int i = 0; i < values.length; i++) {
            String tier = i == 0 ? "no" : "the " + CONDUIT_TIERS[i];
            values[i] = BUILDER.comment(String.format(comment, tier)).defineInRange(key + CONDUIT_TIERS[i], defaults[i], min, max);
        }
        return values;
    }

    private static int[] tierList(List<?> configured, int[] defaults) {
        if (configured.size() != defaults.length) {
            return defaults;
        }
        int[] result = new int[defaults.length];
        for (int i = 0; i < result.length; i++) {
            if (!(configured.get(i) instanceof Number number)) {
                return defaults;
            }
            result[i] = number.intValue();
        }
        return result;
    }

    private static int[] values(ModConfigSpec.IntValue[] values) {
        int[] result = new int[values.length];
        for (int i = 0; i < values.length; i++) {
            result[i] = value(values[i]);
        }
        return result;
    }

    private static <T> T value(ModConfigSpec.ConfigValue<T> value) {
        return SPEC.isLoaded() ? value.get() : value.getDefault();
    }

    private static <T> T worldgenValue(ModConfigSpec.ConfigValue<T> value) {
        return WORLDGEN_SPEC.isLoaded() ? value.get() : value.getDefault();
    }

    @SubscribeEvent
    static void onLoad(final ModConfigEvent event) {
        if (event.getConfig().getSpec() == SPEC) loadValues();
        if (event.getConfig().getSpec() == WORLDGEN_SPEC) loadWorldgenValues();
    }

    private static void loadWorldgenValues() {
        skyblockLuckyZonesAnyBiome = worldgenValue(SKYBLOCK_LUCKY_ZONES_ANY_BIOME);
        skyblockFishingLoot = worldgenValue(SKYBLOCK_FISHING_LOOT);
        skyblockGraftingAnyBiome = worldgenValue(SKYBLOCK_GRAFTING_ANY_BIOME);
        skyblockPrismarineSandMixing = worldgenValue(SKYBLOCK_PRISMARINE_SAND_MIXING);
        skyblockBuddingPrismarineMixing = worldgenValue(SKYBLOCK_BUDDING_PRISMARINE_MIXING);
        skyblockFuelCellResonance = worldgenValue(SKYBLOCK_FUEL_CELL_RESONANCE);
        skyblockEldritchHeartMixing = worldgenValue(SKYBLOCK_ELDRITCH_HEART_MIXING);
    }

    private static void loadValues() {
        kelpHeight = value(KELP_HEIGHT);
        spawnBookInInventory = value(SPAWN_BOOK_IN_INVENTORY);
        collectSaltWater = value(COLLECT_SALT_WATER);
        collectAirWithBottle = value(COLLECT_AIR_WITH_BOTTLE);

        mixerPower = value(MIXER_POWER_REQUIREMENT);
        drainPower = value(DRAIN_POWER_REQUIREMENT);
        beamOverclock = value(BEAM_OVERCLOCK);
        beamOverclockMaxSpeed = value(BEAM_OVERCLOCK_MAX_SPEED);
        augmentationStationPower = value(AUGMENTATION_STATION_POWER_REQUIREMENT);

        laserDistance = value(REGULAR_LASER_DISTANCE);
        relayLaserDistance = value(RELAY_LASER_DISTANCE);
        energyConverterFePerAp = value(ENERGY_CONVERTER_FE_PER_AP);
        energyConverterFeCapacity = value(ENERGY_CONVERTER_FE_CAPACITY);
        energyConverterBaseAp = value(ENERGY_CONVERTER_BASE_AP);
        energyConversionUpgradeAp = value(ENERGY_CONVERSION_UPGRADE_AP);
        advancedEnergyConversionUpgradeAp = value(ADVANCED_ENERGY_CONVERSION_UPGRADE_AP);
        ultimateEnergyConversionUpgradeAp = value(ULTIMATE_ENERGY_CONVERSION_UPGRADE_AP);
        beamMergePurityDrop = value(BEAM_MERGE_PURITY_DROP);

        mixerInputCapacity = value(MIXER_INPUT_CAPACITY);
        mixerOutputCapacity = value(MIXER_OUTPUT_CAPACITY);
        laserCraftingMatrixTankCapacity = value(LASER_CRAFTING_MATRIX_TANK_CAPACITY);

        drainSaltWaterAmount = value(DRAIN_SALT_WATER_AMOUNT);
        drainCapacity = value(DRAIN_CAPACITY);
        drainRequiresOcean = value(DRAIN_REQUIRES_OCEAN);

        guardianAugmentDamage = value(GUARDIAN_AUGMENT_DAMAGE);
        allowAugmentRendering = value(ALLOW_AUGMENT_RENDERING);

        fisherLaserLevel = value(FISHER_LASER_LEVEL);
        fisherRunDuration = value(FISHER_DURATION);
        fisherDepth = value(FISHER_DEPTH);
        fisherRadius = value(FISHER_RADIUS);

        bacteriaGrowthRateCap = value(BACTERIA_GROWTH_RATE_CAP).floatValue();
        bacteriaProductionRateCap = value(BACTERIA_PRODUCTION_RATE_CAP).floatValue();
        bacteriaMutationResistanceCap = value(BACTERIA_MUTATION_RESISTANCE_CAP).floatValue();
        bacteriaColonySizeCap = value(BACTERIA_COLONY_SIZE_CAP);
        bacteriaLifespanCap = value(BACTERIA_LIFESPAN_CAP);
        preferredTagNamespaces = List.copyOf(value(PREFERRED_TAG_NAMESPACES));

        bacteriaAnalyzerCraftingSpeed = value(BACTERIA_ANALYZER_CRAFTING_SPEED);
        bacteriaAnalyzerPowerUsage = value(BACTERIA_ANALYZER_POWER_USAGE);

        mutatorCraftingSpeed = value(MUTATOR_CRAFTING_SPEED);
        mutatorPowerUsage = value(MUTATOR_POWER_USAGE);
        mutatorFailureShrink = value(MUTATOR_FAILURE_SHRINK);
        mutatorRefineChance = value(MUTATOR_REFINE_CHANCE);
        mutatorBoosterMultiplier = value(MUTATOR_BOOSTER_MULTIPLIER);
        mutatorBoostedFailureShrink = value(MUTATOR_BOOSTED_FAILURE_SHRINK);

        incubatorCraftingSpeed = value(INCUBATOR_CRAFTING_SPEED);
        incubatorSizeGrowth = value(INCUBATOR_SIZE_GROWTH);
        incubatorPowerUsage = value(INCUBATOR_POWER_USAGE);

        bioReactorBaseSpeed = value(BIO_REACTOR_BASE_SPEED);
        bioReactorPowerBase = value(BIO_REACTOR_POWER_BASE);
        bioReactorPowerPerColony = value(BIO_REACTOR_POWER_PER_COLONY);
        bioReactorDecayPerSecond = value(BIO_REACTOR_DECAY_PER_SECOND);
        industrialBioReactorBaseSpeed = value(INDUSTRIAL_BIO_REACTOR_BASE_SPEED);
        industrialBioReactorPowerBase = value(INDUSTRIAL_BIO_REACTOR_POWER_BASE);
        industrialBioReactorPowerPerColony = value(INDUSTRIAL_BIO_REACTOR_POWER_PER_COLONY);
        reactorSpeedUpgradeBonus = value(REACTOR_SPEED_UPGRADE_BONUS);
        reactorSpeedUpgradePowerMultiplier = value(REACTOR_SPEED_UPGRADE_POWER);
        reactorYieldUpgradeBonus = value(REACTOR_YIELD_UPGRADE_BONUS);
        reactorYieldUpgradePowerMultiplier = value(REACTOR_YIELD_UPGRADE_POWER);
        reactorEfficiencyUpgradeFactor = value(REACTOR_EFFICIENCY_UPGRADE_FACTOR);
        reactorEfficiencyUpgradeFloor = value(REACTOR_EFFICIENCY_UPGRADE_FLOOR);
        reactorEfficiencyUpgradePowerMultiplier = value(REACTOR_EFFICIENCY_UPGRADE_POWER);
        reactorFusionUpgradePowerMultiplier = value(REACTOR_FUSION_UPGRADE_POWER);
        crystalGrowthPower = value(CRYSTAL_GROWTH_POWER);
        crystalGrowthPurity = value(CRYSTAL_GROWTH_PURITY);
        confinedSpawnerPowerPerTick = value(CONFINED_SPAWNER_POWER_PER_TICK);
        confinedSpawnerPowerBuffer = value(CONFINED_SPAWNER_POWER_BUFFER);
        confinedSpawnerXpCapacity = value(CONFINED_SPAWNER_XP_CAPACITY);
        confinedSpawnerXpRatio = value(CONFINED_SPAWNER_XP_RATIO);
        confinedSpawnerXpFluids = List.copyOf(value(CONFINED_SPAWNER_XP_FLUIDS));
        resonanceMaxNetworksPerPlayer = value(RESONANCE_MAX_NETWORKS);
        charmTransferRate = value(CHARM_TRANSFER_RATE);
        emitterRange = value(EMITTER_RANGE);
        emitterThroughput = value(EMITTER_THROUGHPUT);
        emitterBuffer = value(EMITTER_BUFFER);
        emitterMaxLinks = value(EMITTER_MAX_LINKS);
        satelliteLoss = value(SATELLITE_LOSS);
        replicatorPowerUsage = value(REPLICATOR_POWER_USAGE);
        replicatorPurity = value(REPLICATOR_PURITY);
        replicatorDuration = value(REPLICATOR_DURATION);
        replicatorBiomassCost = value(REPLICATOR_BIOMASS_COST);
        replicatorBiomassCap = value(REPLICATOR_BIOMASS_CAP);
        replicatorErrorChance = value(REPLICATOR_ERROR_CHANCE);
        replicatorSpliceChance = value(REPLICATOR_SPLICE_CHANCE);
        replicatorBiomassPerItem = value(REPLICATOR_BIOMASS_PER_ITEM);
        replicatorCopySize = value(REPLICATOR_COPY_SIZE);
        oxygenDiffuserPower = value(OXYGEN_DIFFUSER_POWER);
        oxygenDiffuserRadius = value(OXYGEN_DIFFUSER_RADIUS);
        bubbleAnchorRadius = value(BUBBLE_ANCHOR_RADIUS);
        bubbleAnchorMaxRadius = value(BUBBLE_ANCHOR_MAX_RADIUS);
        bubbleAnchorLaserPower = value(BUBBLE_ANCHOR_LASER_POWER);
        bubbleAnchorFuelMultiplier = value(BUBBLE_ANCHOR_FUEL_MULTIPLIER);
        bubbleAnchorBlocksPerTick = value(BUBBLE_ANCHOR_BLOCKS_PER_TICK);
        bubbleCapsuleRadius = value(BUBBLE_CAPSULE_RADIUS);
        bubbleCapsuleSeconds = value(BUBBLE_CAPSULE_SECONDS);
        distributorRange = value(DISTRIBUTOR_RANGE);
        distributorMaxLinks = value(DISTRIBUTOR_MAX_LINKS);
        distributorInterval = value(DISTRIBUTOR_INTERVAL);
        conduitItemIntervals = values(CONDUIT_ITEM_INTERVALS);
        conduitItemsPerOp = values(CONDUIT_ITEMS_PER_OP);
        conduitFluidRates = values(CONDUIT_FLUID_RATES);
        conduitEnergyRates = values(CONDUIT_ENERGY_RATES);
        conduitMaxNetworkSize = value(CONDUIT_MAX_NETWORK_SIZE);
        resonantCisternTierCapacity = value(RESONANT_CISTERN_TIER_CAPACITY);
        resonantTransferInterval = value(RESONANT_TRANSFER_INTERVAL);
        resonantItemsPerTransfer = value(RESONANT_ITEMS_PER_TRANSFER);
        resonantFluidPerTransfer = value(RESONANT_FLUID_PER_TRANSFER);
        TapRates.reload();
        satelliteApBuffer = value(SATELLITE_AP_BUFFER);
        satelliteFeBuffer = value(SATELLITE_FE_BUFFER);
        satelliteTransferLimit = value(SATELLITE_TRANSFER_LIMIT);
        satelliteCrossDimensionPurityLoss = value(SATELLITE_CROSS_DIMENSION_PURITY_LOSS);
        resonanceNodeApBuffer = value(RESONANCE_NODE_AP_BUFFER);
        resonanceNodeFeBuffer = value(RESONANCE_NODE_FE_BUFFER);
        resonanceChunkLoading = value(RESONANCE_CHUNK_LOADING);
        tidalRotorMinOutput = value(TIDAL_ROTOR_MIN_OUTPUT);
        tidalRotorMaxOutput = value(TIDAL_ROTOR_MAX_OUTPUT);
        ventTapMinOutput = value(VENT_TAP_MIN_OUTPUT);
        ventTapMaxOutput = value(VENT_TAP_MAX_OUTPUT);
        ventTapSaltWaterPerTick = value(VENT_TAP_SALT_WATER_PER_TICK);
        ventTapSaltWaterPerSalt = value(VENT_TAP_SALT_WATER_PER_SALT);
        ventTapSaltItem = value(VENT_TAP_SALT_ITEM);
        combustionDynamoOutput = value(COMBUSTION_DYNAMO_OUTPUT);
        combustionDynamoTicksPerOil = value(COMBUSTION_DYNAMO_TICKS_PER_OIL);
        combustionDynamoWaterPerTick = value(COMBUSTION_DYNAMO_WATER_PER_TICK);
        combustionDynamoTankCapacity = value(COMBUSTION_DYNAMO_TANK_CAPACITY);
        fusionIgnitionEnergy = value(FUSION_IGNITION_ENERGY);
        fusionFePerAp = value(FUSION_FE_PER_AP);
        fusionMaxOutput = value(FUSION_MAX_OUTPUT);
        fusionCoilContainment = value(FUSION_COIL_CONTAINMENT);
        fusionSatelliteContainment = value(FUSION_SATELLITE_CONTAINMENT);
        fusionSatelliteFePerAp = value(FUSION_SATELLITE_FE_PER_AP);
        fusionMinPurity = value(FUSION_MIN_PURITY);
        fusionFePerMb = value(FUSION_FE_PER_MB);
        fusionEnergyBuffer = value(FUSION_ENERGY_BUFFER);
        fuelCellPowerBase = value(FUEL_CELL_POWER_BASE);
        fuelCellBurnRate = value(FUEL_CELL_BURN_RATE);
        fuelCellMaxPurity = value(FUEL_CELL_MAX_PURITY);
        lensPurityBonus = value(LENS_PURITY_BONUS);
        resonanceBaseCeiling = value(RESONANCE_BASE_CEILING);
        resonancePowerUsage = value(RESONANCE_POWER_USAGE);
        resonanceCriticalLow = value(RESONANCE_CRITICAL_LOW);
        resonanceCriticalHigh = value(RESONANCE_CRITICAL_HIGH);
        resonanceVentCooldown = value(RESONANCE_VENT_COOLDOWN);
        resonanceVentRadius = value(RESONANCE_VENT_RADIUS);
        resonanceVentDamage = value(RESONANCE_VENT_DAMAGE);
        gatewayCooldown = value(GATEWAY_COOLDOWN);
        waveJetThrust = value(WAVE_JET_THRUST);
        waveJetMaxSpeed = value(WAVE_JET_MAX_SPEED);
        waveJetPowerUsage = value(WAVE_JET_POWER_USAGE);
        waveJetLightRange = value(WAVE_JET_LIGHT_RANGE);
        waveJetLightLevel = value(WAVE_JET_LIGHT_LEVEL);
        waveJetLightPowerUsage = value(WAVE_JET_LIGHT_POWER_USAGE);
        riflePowerCapacity = value(RIFLE_POWER_CAPACITY);
        rifleMaxInput = value(RIFLE_MAX_INPUT);
        rifleChargeTicks = value(RIFLE_CHARGE_TICKS);
        rifleRampTicks = value(RIFLE_RAMP_TICKS);
        rifleBaseDamage = value(RIFLE_BASE_DAMAGE);
        rifleMaxDamage = value(RIFLE_MAX_DAMAGE);
        rifleBaseDrain = value(RIFLE_BASE_DRAIN);
        rifleMaxDrain = value(RIFLE_MAX_DRAIN);
        rifleRange = value(RIFLE_RANGE);
        tridentShockwaveRadius = value(TRIDENT_SHOCKWAVE_RADIUS);
        tridentShockwaveDamage = value(TRIDENT_SHOCKWAVE_DAMAGE);
        tridentShockwaveDamagePerSharpness = value(TRIDENT_SHOCKWAVE_DAMAGE_PER_SHARPNESS);
        tridentShockwaveKnockback = value(TRIDENT_SHOCKWAVE_KNOCKBACK);
        tridentShockwaveKnockbackPerLevel = value(TRIDENT_SHOCKWAVE_KNOCKBACK_PER_LEVEL);
        tridentShockwaveCooldown = value(TRIDENT_SHOCKWAVE_COOLDOWN);
        dockPowerUsage = value(DOCK_POWER_USAGE);
        dockChargeRate = value(DOCK_CHARGE_RATE);
        pressureForgeDepth = value(PRESSURE_FORGE_DEPTH);
        pressureForgeWaterColumn = value(PRESSURE_FORGE_WATER_COLUMN);
        pressureForgePowerUsage = value(PRESSURE_FORGE_POWER_USAGE);
        pressureForgeAcidUsage = value(PRESSURE_FORGE_ACID_USAGE);
        pressureForgeCapacity = value(PRESSURE_FORGE_CAPACITY);
        smelterSlots = tierList(value(SMELTER_SLOTS), new int[]{1, 3, 5, 7, 9});
        smelterApMultipliers = tierList(value(SMELTER_AP_MULTIPLIERS), new int[]{1, 2, 4, 8, 16});

        advancedAnalyzerPowerUsage = value(ADVANCED_ANALYZER_POWER_USAGE);
        advancedAnalyzerPurity = value(ADVANCED_ANALYZER_PURITY);
        advancedAnalyzerCraftingSpeed = value(ADVANCED_ANALYZER_CRAFTING_SPEED);
        graftingStationPowerUsage = value(GRAFTING_STATION_POWER_USAGE);
        graftingStationPurity = value(GRAFTING_STATION_PURITY);
        graftingStationDuration = value(GRAFTING_STATION_DURATION);
        graftingStationSaltWaterUsage = value(GRAFTING_STATION_SALT_WATER_USAGE);
        graftingStationCapacity = value(GRAFTING_STATION_CAPACITY);
        advancedGraftingAnchorSpeed = value(ADVANCED_GRAFTING_ANCHOR_SPEED);
        advancedGraftingAnchorPowerMultiplier = value(ADVANCED_GRAFTING_ANCHOR_POWER);

        luckyZonesEnabled = value(LUCKY_ZONES_ENABLED);
        luckyZoneIntervalSeconds = value(LUCKY_ZONE_INTERVAL);
        luckyZoneSpawnDistance = value(LUCKY_ZONE_SPAWN_DISTANCE);
        luckyZoneMinSeparation = value(LUCKY_ZONE_MIN_SEPARATION);
        luckyZonesPerChunk = value(LUCKY_ZONE_PER_CHUNK);
        luckyZoneLifetimeSeconds = value(LUCKY_ZONE_LIFETIME);
        luckyZoneMinRadius = value(LUCKY_ZONE_MIN_RADIUS);
        luckyZoneMaxRadius = Math.max(value(LUCKY_ZONE_MIN_RADIUS), value(LUCKY_ZONE_MAX_RADIUS));
        luckyZoneConsumedOnCatch = value(LUCKY_ZONE_CONSUMED);
        luckyZoneBiteSpeed = value(LUCKY_ZONE_BITE_SPEED);
        starcatcherFishOnNautecRod = value(STARCATCHER_FISH_ON_NAUTEC_ROD);
        eyeOfTheSeaSearchRadius = value(EYE_OF_THE_SEA_SEARCH_RADIUS);
        eyeOfTheSeaCooldownTicks = value(EYE_OF_THE_SEA_COOLDOWN);
        eyeOfTheSeaLuckyBoostSeconds = value(EYE_OF_THE_SEA_LUCKY_BOOST);
        eyeOfTheSeaLuckyZoneMultiplier = value(EYE_OF_THE_SEA_LUCKY_MULTIPLIER);
        divingSuitAcceptsOxygenFluid = value(DIVING_SUIT_ACCEPTS_OXYGEN_FLUID);
        bacteriaProductNutrients = value(BACTERIA_PRODUCT_NUTRIENTS);
        bacteriaProductConsumeMultiplier = value(BACTERIA_PRODUCT_CONSUME_MULTIPLIER);
        airBottlesFillOxygenHelmets = value(AIR_BOTTLES_FILL_OXYGEN_HELMETS);
        divingSuitProtectsInSpace = value(DIVING_SUIT_PROTECTS_IN_SPACE);

        abyssalEyesDepth = value(ABYSSAL_EYES_DEPTH);
        photophoreSkinRadius = value(PHOTOPHORE_SKIN_RADIUS);

        submarinePowerCapacity = value(SUBMARINE_POWER_CAPACITY);
        submarineIdlePowerUsage = value(SUBMARINE_IDLE_POWER_USAGE);
        submarineMovePowerUsage = value(SUBMARINE_MOVE_POWER_USAGE);
        submarineOxygenPowerUsage = value(SUBMARINE_OXYGEN_POWER_USAGE);
        submarineSpeed = value(SUBMARINE_SPEED);
        submarineMaxSpeed = value(SUBMARINE_MAX_SPEED);
        submarineFlightSpeedMultiplier = value(SUBMARINE_FLIGHT_SPEED_MULTIPLIER);
        submarineFlightPowerUsage = value(SUBMARINE_FLIGHT_POWER_USAGE);
        submarineCameraDistance = value(SUBMARINE_CAMERA_DISTANCE);
        submarineMaxHealth = value(SUBMARINE_MAX_HEALTH);
        submarineArmor = value(SUBMARINE_ARMOR);
        submarineArmorToughness = value(SUBMARINE_ARMOR_TOUGHNESS);
        submarineKnockbackResistance = value(SUBMARINE_KNOCKBACK_RESISTANCE);
        submarineAutorepairIntervalTicks = value(SUBMARINE_AUTOREPAIR_INTERVAL);
        submarineAutorepairPercent = value(SUBMARINE_AUTOREPAIR_PERCENT);
        submarineRepairItem = value(SUBMARINE_REPAIR_ITEM);
        submarineRepairPercent = value(SUBMARINE_REPAIR_PERCENT);

        submarineSolarPercentPer5s = value(SUBMARINE_SOLAR_PERCENT);
        submarineBoostPowerCost = value(SUBMARINE_BOOST_POWER);
        submarineBoostDurationTicks = value(SUBMARINE_BOOST_DURATION);
        submarineBoostCooldownTicks = value(SUBMARINE_BOOST_COOLDOWN);
        submarineBoostSpeedBonus = value(SUBMARINE_BOOST_SPEED);
        submarineStealthPowerCost = value(SUBMARINE_STEALTH_POWER);
        submarineStealthDurationTicks = value(SUBMARINE_STEALTH_DURATION);
        submarineStealthCooldownTicks = value(SUBMARINE_STEALTH_COOLDOWN);
        submarineStealthSpeedPenalty = value(SUBMARINE_STEALTH_SLOW);
        submarineSonarPowerCost = value(SUBMARINE_SONAR_POWER);
        submarineSonarCooldownTicks = value(SUBMARINE_SONAR_COOLDOWN);
        submarineSonarRadius = value(SUBMARINE_SONAR_RADIUS);
        submarineShieldPowerCost = value(SUBMARINE_SHIELD_POWER);
        submarineShieldCooldownTicks = value(SUBMARINE_SHIELD_COOLDOWN);
        submarineShieldStunTicks = value(SUBMARINE_SHIELD_STUN);
        submarineShieldDamage = value(SUBMARINE_SHIELD_DAMAGE);
        submarineShieldRadius = value(SUBMARINE_SHIELD_RADIUS);
        submarineShieldPowerPerHeart = value(SUBMARINE_SHIELD_POWER_PER_HEART);
        submarineLaserChargeTicks = value(SUBMARINE_LASER_CHARGE_TICKS);
        submarineLaserRampTicks = value(SUBMARINE_LASER_RAMP_TICKS);
        submarineLaserMinDrain = value(SUBMARINE_LASER_MIN_DRAIN);
        submarineLaserMaxDrain = value(SUBMARINE_LASER_MAX_DRAIN);
        submarineLaserRange = value(SUBMARINE_LASER_RANGE);
        submarineLaserMinDamage = value(SUBMARINE_LASER_MIN_DAMAGE);
        submarineLaserMaxDamage = value(SUBMARINE_LASER_MAX_DAMAGE);
        submarineLaserMaxHealthPercent = value(SUBMARINE_LASER_MAX_HEALTH_PERCENT);
        submarineTeleportPowerCost = value(SUBMARINE_TELEPORT_POWER);
        submarineTeleportMinPowerPercent = value(SUBMARINE_TELEPORT_MIN_POWER);
        submarineTeleportCooldownTicks = value(SUBMARINE_TELEPORT_COOLDOWN);
        submarineArmorModuleToughness = value(SUBMARINE_ARMOR_MODULE_TOUGHNESS);
        catalystBuddingOutputBonus = value(CATALYST_BUDDING_OUTPUT_BONUS);
        catalystBuddingPurityBonus = value(CATALYST_BUDDING_PURITY_BONUS);
        catalystBuddingMaxPurity = value(CATALYST_BUDDING_MAX_PURITY);
        conduitBeaconPowerUsage = value(CONDUIT_BEACON_POWER_USAGE);
        conduitBeaconBuffer = value(CONDUIT_BEACON_BUFFER);
        conduitBeaconSpawnRadius = value(CONDUIT_BEACON_SPAWN_RADIUS);
        biomeTankTicks = value(BIOME_TANK_TICKS);
    }
}
