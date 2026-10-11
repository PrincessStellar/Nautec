package com.breakinblocks.nautec.datagen;

import com.breakinblocks.nautec.content.conduits.ConduitArm;
import com.breakinblocks.nautec.content.conduits.ConduitTapBlock;
import com.breakinblocks.nautec.content.conduits.CurrentConduitBlock;
import com.breakinblocks.nautec.content.conduits.TapArm;
import com.breakinblocks.nautec.content.conduit.ConduitBeaconBlock;
import com.breakinblocks.nautec.content.blocks.OxygenDiffuserBlock;
import com.breakinblocks.nautec.content.blocks.ColonyReplicatorBlock;
import com.breakinblocks.nautec.content.bubble.BubbleAnchorBlock;
import com.breakinblocks.nautec.NTRegistries;
import com.breakinblocks.nautec.Nautec;
import com.breakinblocks.nautec.content.blocks.AdvancedBacterialAnalyzerBlock;
import com.breakinblocks.nautec.content.blocks.GraftingStationBlock;
import com.breakinblocks.nautec.content.blocks.fusion.FusionControllerBlock;
import com.breakinblocks.nautec.content.blocks.generators.CombustionDynamoBlock;
import com.breakinblocks.nautec.content.blocks.generators.ThermalVentTapBlock;
import com.breakinblocks.nautec.api.multiblocks.Multiblock;
import com.breakinblocks.nautec.content.blocks.AquaticCatalystBlock;
import com.breakinblocks.nautec.content.blocks.CrateBlock;
import com.breakinblocks.nautec.content.blocks.LaserJunctionBlock;
import com.breakinblocks.nautec.content.blocks.OilBarrelBlock;
import com.breakinblocks.nautec.registries.NTBlocks;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.PipeBlock;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.neoforged.neoforge.client.model.generators.BlockModelBuilder;
import net.neoforged.neoforge.client.model.generators.BlockStateProvider;
import net.neoforged.neoforge.client.model.generators.ConfiguredModel;
import net.neoforged.neoforge.client.model.generators.ModelFile;
import net.neoforged.neoforge.client.model.generators.MultiPartBlockStateBuilder;
import net.neoforged.neoforge.client.model.generators.VariantBlockStateBuilder;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import org.apache.commons.lang3.IntegerRange;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

public class BlockModelProvider extends BlockStateProvider {
    public static final String CUTOUT = "minecraft:cutout";
    public static final String TRANSLUCENT = "minecraft:translucent";
    private static final int[][] ARM_ROTATIONS = {{0, 0}, {180, 0}, {90, 180}, {90, 0}, {90, 90}, {90, 270}};

    private final Map<ResourceLocation, BlockModelBuilder> createdModels = new HashMap<>();

    public BlockModelProvider(PackOutput output, ExistingFileHelper existingFileHelper) {
        super(output, Nautec.MODID, existingFileHelper);
    }

    @Override
    public String getName() {
        return "NauTec Block Model Definitions";
    }

    @Override
    protected void registerStatesAndModels() {
        createdModels.clear();
        MultiblockModelHelper helper = new MultiblockModelHelper(this);

        pillarBlock(NTBlocks.DARK_PRISMARINE_PILLAR.get());
        simpleBlock(NTBlocks.CHISELED_DARK_PRISMARINE.get());
        simpleBlock(NTBlocks.PRISMARINE_SAND.get());
        simpleBlock(NTBlocks.POLISHED_PRISMARINE.get());
        simpleBlock(NTBlocks.AQUARINE_STEEL_BLOCK.get());
        simpleBlock(NTBlocks.CAST_IRON_BLOCK.get());

        simpleBlock(NTBlocks.BUDDING_PRISMARINE.get());
        prismarineBud(NTBlocks.SMALL_PRISMARINE_BUD.get());
        prismarineBud(NTBlocks.MEDIUM_PRISMARINE_BUD.get());
        prismarineBud(NTBlocks.LARGE_PRISMARINE_BUD.get());
        prismarineBud(NTBlocks.PRISMARINE_CLUSTER.get());

        simpleBlock(NTBlocks.DEEP_KELP.get(), emissiveCross(NTBlocks.DEEP_KELP.get()));
        simpleBlock(NTBlocks.DEEP_KELP_PLANT.get(), emissiveCross(NTBlocks.DEEP_KELP_PLANT.get()));
        waterPlant(NTBlocks.LUMINESCENT_ALGAE.get());
        waterPlant(NTBlocks.PRISMARINE_FROND.get());
        waterPlant(NTBlocks.VENT_TUBEWORM.get());
        waterPlant(NTBlocks.ABYSSAL_CORAL.get());
        multiface(NTBlocks.GLOW_POLYP.get(), existingModelFile(NTBlocks.GLOW_POLYP.get()));

        simpleBlock(NTBlocks.CREATIVE_POWER_SOURCE.get());
        simpleBlock(NTBlocks.CREATIVE_ENERGY_SOURCE.get(), cubeAll(name(NTBlocks.CREATIVE_ENERGY_SOURCE.get()),
                blockTexture(NTBlocks.CREATIVE_POWER_SOURCE.get())));
        simpleBlock(NTBlocks.ENERGY_CONVERTER.get(), artModel(NTBlocks.ENERGY_CONVERTER.get()));
        fusionPlant();
        facingBlock(NTBlocks.RESONANCE_NODE.get(), existingModelFile(NTBlocks.RESONANCE_NODE.get()));
        simpleBlock(NTBlocks.PRISMATIC_EMITTER.get(), existingModelFile(NTBlocks.PRISMATIC_EMITTER.get()));
        simpleBlock(NTBlocks.UPLINK_ARRAY.get(), existingModelFile(NTBlocks.UPLINK_ARRAY.get()));
        simpleBlock(NTBlocks.SATELLITE_ARRAY_TOP.get(), existingModelFile(NTBlocks.SATELLITE_ARRAY_TOP.get()));
        generators();
        aquaticCatalyst(NTBlocks.AQUATIC_CATALYST.get());

        existingFacingBlock(NTBlocks.PRISMARINE_RELAY.get(), NTBlocks.PRISMARINE_RELAY.get());

        simpleBlock(NTBlocks.SUBMARINE_DOCK.get(), artModel(NTBlocks.SUBMARINE_DOCK.get()));

        simpleBlock(NTBlocks.PRESSURE_FORGE.get(), artModel(NTBlocks.PRESSURE_FORGE.get()));

        simpleBlock(NTBlocks.GATEWAY.get(), artModel(NTBlocks.GATEWAY.get()));
        simpleBlock(NTBlocks.GATEWAY_RING.get());
        simpleBlock(NTBlocks.GATEWAY_RING_PART.get(), models().getBuilder(blockModelId(NTBlocks.GATEWAY_RING_PART.get()).toString())
                .texture("particle", blockTexture(NTBlocks.GATEWAY_RING.get())));

        simpleBlock(NTBlocks.RESONANCE_CHAMBER.get(), artModel(NTBlocks.RESONANCE_CHAMBER.get()));

        facingBlock(NTBlocks.FOCUSING_LENS.get(), artModel(NTBlocks.FOCUSING_LENS.get()));
        laserJunction(NTBlocks.LASER_JUNCTION.get());
        currentConduit(NTBlocks.CURRENT_CONDUIT.get());
        conduitTap(NTBlocks.CONDUIT_TAP.get());
        simpleBlock(NTBlocks.AQUARINE_COPPER_BLOCK.get());

        simpleBlock(NTBlocks.MIXER.get(), existingModelFile(NTBlocks.MIXER.get()));
        simpleBlock(NTBlocks.LASER_CRAFTING_MATRIX.get(), existingModelFile(NTBlocks.LASER_CRAFTING_MATRIX.get()));
        simpleBlock(NTBlocks.RESONANT_VAULT.get(), existingModelFile(NTBlocks.RESONANT_VAULT.get()));
        simpleBlock(NTBlocks.RESONANT_CISTERN.get(), existingModelFile(NTBlocks.RESONANT_CISTERN.get()));
        simpleBlock(NTBlocks.CHARGER.get(), existingModelFile(NTBlocks.CHARGER.get()));
        simpleBlock(NTBlocks.CONFINED_SPAWNER.get(), existingModelFile(NTBlocks.CONFINED_SPAWNER.get()));
        simpleBlock(NTBlocks.CRYSTAL_CRADLE.get(), existingModelFile(NTBlocks.CRYSTAL_CRADLE.get()));
        simpleBlock(NTBlocks.FISHING_STATION.get(), existingModelFile(NTBlocks.FISHING_STATION.get()));
        crateBlock(NTBlocks.CRATE.get());
        rustyCrateBlock(NTBlocks.RUSTY_CRATE.get());

        simpleBlock(NTBlocks.MUTATOR.get(), existingModelFile(NTBlocks.MUTATOR.get()));
        simpleBlock(NTBlocks.INCUBATOR.get(), existingModelFile(NTBlocks.INCUBATOR.get()));

        facingBlock(NTBlocks.BACTERIAL_FUEL_CELL.get(), artModel(NTBlocks.BACTERIAL_FUEL_CELL.get()));

        helper.drainController(NTBlocks.DRAIN.get());
        helper.drainPart(NTBlocks.DRAIN_PART.get(), IntegerRange.of(0, 8));

        helper.augmentationStationController(NTBlocks.AUGMENTATION_STATION.get());
        helper.augmentationStationPart(NTBlocks.AUGMENTATION_STATION_PART.get(), IntegerRange.of(0, 8));
        helper.augmentationStationExtension(NTBlocks.AUGMENTATION_STATION_EXTENSION.get());

        helper.bioReactorPart(NTBlocks.BIO_REACTOR_PART.get());
        helper.bioReactorController(NTBlocks.BIO_REACTOR.get());
        helper.industrialBioReactorPart(NTBlocks.INDUSTRIAL_BIO_REACTOR_PART.get());
        helper.industrialBioReactorController(NTBlocks.INDUSTRIAL_BIO_REACTOR.get());

        simpleBlock(NTBlocks.BACTERIAL_CONTAINMENT_SHIELD.get());

        simpleBlock(NTBlocks.DRAIN_WALL.get());
        simpleBlock(NTBlocks.BROWN_POLYMER_BLOCK.get());

        oilBarrel(NTBlocks.OIL_BARREL.get(), cubeBottomTop(name(NTBlocks.OIL_BARREL.get()),
                blockTexture(NTBlocks.OIL_BARREL.get(), "_side"),
                blockTexture(NTBlocks.OIL_BARREL.get(), "_bottom"),
                blockTexture(NTBlocks.OIL_BARREL.get())
        ), cubeBottomTop(name(NTBlocks.OIL_BARREL.get()) + "_open",
                blockTexture(NTBlocks.OIL_BARREL.get(), "_side"),
                blockTexture(NTBlocks.OIL_BARREL.get(), "_bottom"),
                blockTexture(NTBlocks.OIL_BARREL.get(), "_open")
        ));

        horizontalFacingBlock(NTBlocks.BACTERIAL_ANALYZER.get(), existingModelFile(NTBlocks.BACTERIAL_ANALYZER.get()));
        horizontalFacingBlock(NTBlocks.BACTERIAL_ANALYZER_TOP.get(), existingModelFile(NTBlocks.BACTERIAL_ANALYZER_TOP.get()));
    }

    private void generators() {
        simpleBlock(NTBlocks.TIDAL_ROTOR.get(), cubeBottomTop("tidal_rotor", blockTexture(NTBlocks.TIDAL_ROTOR.get(), "_side"),
                blockTexture(NTBlocks.TIDAL_ROTOR.get(), "_bottom"), blockTexture(NTBlocks.TIDAL_ROTOR.get(), "_top")));
        Block tap = NTBlocks.THERMAL_VENT_TAP.get();
        booleanDispatch(tap, ThermalVentTapBlock.LIT, existingModelFile("thermal_vent_tap_lit"), existingModelFile(tap));

        Block dynamo = NTBlocks.COMBUSTION_DYNAMO.get();
        booleanHorizontalDispatch(dynamo, CombustionDynamoBlock.LIT, existingModelFile("combustion_dynamo_lit"), existingModelFile(dynamo));
    }

    private void fusionPlant() {
        ResourceLocation casing = blockTexture(NTBlocks.FUSION_CASING.get());
        simpleBlock(NTBlocks.FUSION_CASING.get(), cubeAll("fusion_casing", casing));
        simpleBlock(NTBlocks.AQUAMARINE_STRUCTURAL_GLASS.get(), cubeAll("aquamarine_structural_glass",
                blockTexture(NTBlocks.AQUAMARINE_STRUCTURAL_GLASS.get())).renderType(TRANSLUCENT));
        Block coil = NTBlocks.CONTAINMENT_COIL.get();
        simpleBlock(coil, emissiveCube("containment_coil", faces(blockTexture(coil, "_end"), blockTexture(coil, "_end"),
                blockTexture(coil, "_side"), blockTexture(coil, "_side")), blockTexture(coil, "_side"),
                glow(blockTexture(coil, "_end_emissive"), blockTexture(coil, "_end_emissive"), blockTexture(coil, "_side_emissive"))));
        Block port = NTBlocks.FUSION_PORT.get();
        simpleBlock(port, emissiveCube("fusion_port", faces(blockTexture(port), blockTexture(port), blockTexture(port), blockTexture(port)),
                blockTexture(port), glow(blockTexture(port, "_emissive"), blockTexture(port, "_emissive"), blockTexture(port, "_emissive"))));
        Block collector = NTBlocks.FUSION_COLLECTOR.get();
        simpleBlock(collector, emissiveCube("fusion_collector", faces(blockTexture(collector, "_lens"), blockTexture(collector, "_lens"),
                blockTexture(collector, "_side"), blockTexture(collector, "_side")), blockTexture(collector, "_side"),
                glow(blockTexture(collector, "_lens_emissive"), blockTexture(collector, "_lens_emissive"), null)));
        Block injector = NTBlocks.LASER_INJECTOR.get();
        facingBlock(injector, emissiveCube("laser_injector", faces(blockTexture(injector, "_back"), blockTexture(injector, "_front"),
                blockTexture(injector, "_side"), blockTexture(injector, "_side")), blockTexture(injector, "_side"),
                glow(null, blockTexture(injector, "_front_emissive"), null)));

        Block controller = NTBlocks.FUSION_CONTROLLER.get();
        ModelFile idle = createdModels.computeIfAbsent(Nautec.rl("block/fusion_controller"), key -> models().orientable(key.toString(),
                casing, blockTexture(controller, "_front"), casing));
        ModelFile active = emissiveCube("fusion_controller_active", faces(casing, casing, blockTexture(controller, "_front_active"), casing),
                blockTexture(controller, "_front_active"), front(blockTexture(controller, "_front_active_emissive")));
        booleanHorizontalDispatch(controller, FusionControllerBlock.ACTIVE, active, idle);

        graftingStation(NTBlocks.GRAFTING_STATION.get());
        advancedAnalyzer(NTBlocks.ADVANCED_BACTERIAL_ANALYZER.get());
        bubbleAnchor(NTBlocks.BUBBLE_ANCHOR.get());
        replicator(NTBlocks.COLONY_REPLICATOR.get());
        DoorBlock hatch = NTBlocks.PRESSURE_HATCH.get();
        doorBlockWithRenderType(hatch, blockTexture(hatch, "_bottom"), blockTexture(hatch, "_top"), TRANSLUCENT);
        oxygenDiffuser(NTBlocks.OXYGEN_DIFFUSER.get());
        conduitBeacon(NTBlocks.CONDUIT_BEACON.get());
        dishStorage(NTBlocks.AQUARINE_DISH_STORAGE.get());
        dishStorage(NTBlocks.DEEP_STEEL_DISH_STORAGE.get());
        dishStorage(NTBlocks.ATLANTIC_GOLD_DISH_STORAGE.get());
        simpleBlock(NTBlocks.BIOME_TANK.get(), existingModelFile(NTBlocks.BIOME_TANK.get()));
        simpleBlock(NTBlocks.HYDROTHERMAL_VENT.get(), existingModelFile(NTBlocks.HYDROTHERMAL_VENT.get()));
        simpleBlock(NTBlocks.DISTRIBUTOR.get(), cubeBottomTop("nautechnical_distributor", blockTexture(NTBlocks.DISTRIBUTOR.get(), "_side"),
                blockTexture(NTBlocks.DISTRIBUTOR.get(), "_bottom"), blockTexture(NTBlocks.DISTRIBUTOR.get(), "_top")));
    }

    private void replicator(Block block) {
        ModelFile idle = orientableWithBottom(Nautec.rl("block/colony_replicator"), block);
        ModelFile active = emissiveCube("colony_replicator_active", faces(blockTexture(block, "_bottom"), blockTexture(block, "_top"),
                blockTexture(block, "_front_active"), blockTexture(block, "_side")), blockTexture(block, "_front_active"),
                front(blockTexture(block, "_front_active_emissive")));
        booleanHorizontalDispatch(block, ColonyReplicatorBlock.ACTIVE, active, idle);
    }

    private void conduitBeacon(Block block) {
        booleanDispatch(block, ConduitBeaconBlock.ACTIVE, existingModelFile("conduit_beacon_active"), existingModelFile(block));
    }

    private void dishStorage(Block block) {
        horizontalFacingBlock(block, orientableWithBottom(blockModelId(block), block));
    }

    private BlockModelBuilder orientableWithBottom(ResourceLocation id, Block block) {
        return createdModels.computeIfAbsent(id, key -> models().orientableWithBottom(key.toString(),
                blockTexture(block, "_side"),
                blockTexture(block, "_front"),
                blockTexture(block, "_bottom"),
                blockTexture(block, "_top")));
    }

    private void oxygenDiffuser(Block block) {
        ModelFile idle = cubeBottomTop("oxygen_diffuser", blockTexture(block, "_side"), blockTexture(block, "_bottom"), blockTexture(block, "_top"));
        ModelFile active = emissiveCube("oxygen_diffuser_active", faces(blockTexture(block, "_bottom"), blockTexture(block, "_top_active"),
                blockTexture(block, "_side_active"), blockTexture(block, "_side_active")), blockTexture(block, "_side_active"),
                glow(null, blockTexture(block, "_top_active_emissive"), blockTexture(block, "_side_active_emissive")));
        booleanDispatch(block, OxygenDiffuserBlock.ACTIVE, active, idle);
    }

    private void bubbleAnchor(Block block) {
        ModelFile idle = cubeBottomTop("bubble_anchor", blockTexture(block, "_side"), blockTexture(block, "_bottom"), blockTexture(block, "_top"));
        ModelFile active = emissiveCube("bubble_anchor_active", faces(blockTexture(block, "_bottom"), blockTexture(block, "_top_active"),
                blockTexture(block, "_side_active"), blockTexture(block, "_side_active")), blockTexture(block, "_side_active"),
                glow(null, blockTexture(block, "_top_active_emissive"), blockTexture(block, "_side_active_emissive")));
        booleanDispatch(block, BubbleAnchorBlock.ACTIVE, active, idle);
    }

    private void advancedAnalyzer(Block block) {
        ModelFile idle = orientableWithBottom(Nautec.rl("block/advanced_bacterial_analyzer"), block);
        ModelFile active = emissiveCube("advanced_bacterial_analyzer_active", faces(blockTexture(block, "_bottom"), blockTexture(block, "_top"),
                blockTexture(block, "_front_active"), blockTexture(block, "_side")), blockTexture(block, "_front_active"),
                front(blockTexture(block, "_front_active_emissive")));
        booleanHorizontalDispatch(block, AdvancedBacterialAnalyzerBlock.ACTIVE, active, idle);
    }

    private void graftingStation(Block block) {
        ModelFile idle = orientableWithBottom(Nautec.rl("block/grafting_station"), block);
        Map<Direction, ResourceLocation> graftingGlow = front(blockTexture(block, "_front_active_emissive"));
        graftingGlow.put(Direction.UP, blockTexture(block, "_top_active_emissive"));
        ModelFile active = emissiveCube("grafting_station_active", faces(blockTexture(block, "_bottom"), blockTexture(block, "_top_active"),
                blockTexture(block, "_front_active"), blockTexture(block, "_side")), blockTexture(block, "_front_active"), graftingGlow);
        booleanHorizontalDispatch(block, GraftingStationBlock.ACTIVE, active, idle);
    }

    private void pillarBlock(Block block) {
        ModelFile model = createdModels.computeIfAbsent(blockModelId(block), id ->
                models().cubeColumn(id.toString(), blockTexture(block, "_side"), blockTexture(block, "_end")));
        VariantBlockStateBuilder builder = getVariantBuilder(block);
        builder.partialState().with(BlockStateProperties.AXIS, Direction.Axis.Y).setModels(rotated(model, 0, 0));
        builder.partialState().with(BlockStateProperties.AXIS, Direction.Axis.Z).setModels(rotated(model, 90, 0));
        builder.partialState().with(BlockStateProperties.AXIS, Direction.Axis.X).setModels(rotated(model, 90, 90));
    }

    private void prismarineBud(Block block) {
        facingBlock(block, emissiveCross(block));
    }

    private void waterPlant(Block block) {
        simpleBlock(block, emissiveCross(block));
    }

    private void multiface(Block block, ModelFile model) {
        MultiPartBlockStateBuilder builder = getMultipartBuilder(block);
        Direction[] order = {Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST, Direction.UP, Direction.DOWN};
        int[][] rotations = {{0, 0}, {0, 90}, {0, 180}, {0, 270}, {270, 0}, {90, 0}};
        for (int i = 0; i < order.length; i++) {
            boolean uvLock = order[i] != Direction.NORTH;
            BooleanProperty face = PipeBlock.PROPERTY_BY_DIRECTION.get(order[i]);
            builder.part().modelFile(model).rotationX(rotations[i][0]).rotationY(rotations[i][1]).uvLock(uvLock).addModel()
                    .condition(face, true)
                    .end();
            builder.part().modelFile(model).rotationX(rotations[i][0]).rotationY(rotations[i][1]).uvLock(uvLock).addModel()
                    .condition(PipeBlock.PROPERTY_BY_DIRECTION.get(Direction.DOWN), false)
                    .condition(PipeBlock.PROPERTY_BY_DIRECTION.get(Direction.EAST), false)
                    .condition(PipeBlock.PROPERTY_BY_DIRECTION.get(Direction.NORTH), false)
                    .condition(PipeBlock.PROPERTY_BY_DIRECTION.get(Direction.SOUTH), false)
                    .condition(PipeBlock.PROPERTY_BY_DIRECTION.get(Direction.UP), false)
                    .condition(PipeBlock.PROPERTY_BY_DIRECTION.get(Direction.WEST), false)
                    .end();
        }
    }

    private BlockModelBuilder emissiveCross(Block block) {
        return createdModels.computeIfAbsent(blockModelId(block), key -> models()
                .withExistingParent(key.toString(), Nautec.rl("block/template_emissive_cross"))
                .texture("cross", blockTexture(block))
                .texture("cross_emissive", blockTexture(block, "_emissive"))
                .renderType(CUTOUT));
    }

    private static Map<Direction, ResourceLocation> faces(ResourceLocation down, ResourceLocation up, ResourceLocation north, ResourceLocation side) {
        Map<Direction, ResourceLocation> faces = new EnumMap<>(Direction.class);
        faces.put(Direction.DOWN, down);
        faces.put(Direction.UP, up);
        faces.put(Direction.NORTH, north);
        faces.put(Direction.SOUTH, side);
        faces.put(Direction.WEST, side);
        faces.put(Direction.EAST, side);
        return faces;
    }

    private static Map<Direction, ResourceLocation> glow(ResourceLocation down, ResourceLocation up, ResourceLocation sides) {
        Map<Direction, ResourceLocation> glow = new EnumMap<>(Direction.class);
        if (down != null) {
            glow.put(Direction.DOWN, down);
        }
        if (up != null) {
            glow.put(Direction.UP, up);
        }
        if (sides != null) {
            for (Direction direction : Direction.Plane.HORIZONTAL) {
                glow.put(direction, sides);
            }
        }
        return glow;
    }

    private static Map<Direction, ResourceLocation> front(ResourceLocation north) {
        Map<Direction, ResourceLocation> glow = new EnumMap<>(Direction.class);
        glow.put(Direction.NORTH, north);
        return glow;
    }

    private BlockModelBuilder emissiveCube(String name, Map<Direction, ResourceLocation> faces, ResourceLocation particle, Map<Direction, ResourceLocation> glow) {
        ResourceLocation id = Nautec.rl("block/" + name);
        return createdModels.computeIfAbsent(id, key -> {
            StringBuilder letters = new StringBuilder();
            for (Direction direction : Direction.values()) {
                if (glow.get(direction) != null) {
                    letters.append(direction.getName().charAt(0));
                }
            }
            BlockModelBuilder builder = models().withExistingParent(key.toString(), Nautec.rl("block/template_emissive_cube_" + letters))
                    .texture("particle", particle);
            for (Direction direction : Direction.values()) {
                builder.texture(direction.getName(), faces.get(direction));
                ResourceLocation emissive = glow.get(direction);
                if (emissive != null) {
                    builder.texture(direction.getName() + "_emissive", emissive);
                }
            }
            return builder.renderType(CUTOUT);
        });
    }

    private void booleanDispatch(Block block, BooleanProperty property, ModelFile whenTrue, ModelFile whenFalse) {
        VariantBlockStateBuilder builder = getVariantBuilder(block);
        builder.partialState().with(property, true).setModels(new ConfiguredModel(whenTrue));
        builder.partialState().with(property, false).setModels(new ConfiguredModel(whenFalse));
    }

    private void booleanHorizontalDispatch(Block block, BooleanProperty property, ModelFile whenTrue, ModelFile whenFalse) {
        VariantBlockStateBuilder builder = getVariantBuilder(block);
        for (Direction direction : BlockStateProperties.HORIZONTAL_FACING.getPossibleValues()) {
            int yRot = horizontalRotation(direction);
            builder.partialState().with(BlockStateProperties.HORIZONTAL_FACING, direction).with(property, true)
                    .setModels(rotated(whenTrue, 0, yRot));
            builder.partialState().with(BlockStateProperties.HORIZONTAL_FACING, direction).with(property, false)
                    .setModels(rotated(whenFalse, 0, yRot));
        }
    }

    public void horizontalFacingBlock(Block block, ModelFile model) {
        VariantBlockStateBuilder builder = getVariantBuilder(block);
        for (Direction direction : BlockStateProperties.HORIZONTAL_FACING.getPossibleValues()) {
            builder.partialState().with(BlockStateProperties.HORIZONTAL_FACING, direction)
                    .setModels(rotated(model, 0, horizontalRotation(direction)));
        }
    }

    public static int horizontalRotation(Direction direction) {
        return switch (direction) {
            case EAST -> 90;
            case SOUTH -> 180;
            case WEST -> 270;
            default -> 0;
        };
    }

    private void aquaticCatalyst(AquaticCatalystBlock block) {
        MultiPartBlockStateBuilder builder = getMultipartBuilder(block);
        for (Direction dir : Direction.values()) {
            int xRot = dir == Direction.DOWN ? 180 : dir.getAxis().isHorizontal() ? 90 : 0;
            int yRot = dir.getAxis().isVertical() ? 0 : (((int) dir.toYRot()) + 180) % 360;
            for (int stage : AquaticCatalystBlock.STAGE.getPossibleValues()) {
                for (boolean active : AquaticCatalystBlock.ACTIVE.getPossibleValues()) {
                    builder.part().modelFile(createActiveACModel(block, stage, active)).rotationX(xRot).rotationY(yRot).addModel()
                            .condition(BlockStateProperties.FACING, dir)
                            .condition(AquaticCatalystBlock.STAGE, stage)
                            .condition(AquaticCatalystBlock.ACTIVE, active)
                            .end();
                }
            }
            for (boolean linked : AquaticCatalystBlock.LINKED.getPossibleValues()) {
                builder.part().modelFile(existingModelFile(linked ? "aquatic_catalyst_lamp_linked" : "aquatic_catalyst_lamp_unlinked"))
                        .rotationX(xRot).rotationY(yRot).addModel()
                        .condition(BlockStateProperties.FACING, dir)
                        .condition(AquaticCatalystBlock.LINKED, linked)
                        .end();
            }
        }
    }

    private void currentConduit(Block block) {
        MultiPartBlockStateBuilder builder = getMultipartBuilder(block);
        builder.part().modelFile(existingModelFile("current_conduit_core")).addModel().end();
        ModelFile arm = existingModelFile("current_conduit_arm");
        ModelFile cap = existingModelFile("current_conduit_cap");
        ModelFile collar = existingModelFile("current_conduit_collar");
        for (Direction direction : Direction.values()) {
            int[] rotation = ARM_ROTATIONS[direction.ordinal()];
            builder.part().modelFile(arm).rotationX(rotation[0]).rotationY(rotation[1]).addModel()
                    .condition(CurrentConduitBlock.ARMS[direction.ordinal()], ConduitArm.CONNECTED)
                    .end();
            builder.part().modelFile(cap).rotationX(rotation[0]).rotationY(rotation[1]).addModel()
                    .condition(CurrentConduitBlock.ARMS[direction.ordinal()], ConduitArm.NONE, ConduitArm.BLOCKED)
                    .end();
            if (direction.getAxisDirection() == Direction.AxisDirection.POSITIVE) {
                builder.part().modelFile(collar).rotationX(rotation[0]).rotationY(rotation[1]).addModel()
                        .condition(CurrentConduitBlock.ARMS[direction.ordinal()], ConduitArm.CONNECTED)
                        .end();
            }
        }
    }

    private void conduitTap(Block block) {
        MultiPartBlockStateBuilder builder = getMultipartBuilder(block);
        builder.part().modelFile(existingModelFile("conduit_tap_core")).addModel().end();
        ModelFile arm = existingModelFile("current_conduit_arm");
        ModelFile collar = existingModelFile("current_conduit_collar");
        for (Direction direction : Direction.values()) {
            int[] rotation = ARM_ROTATIONS[direction.ordinal()];
            builder.part().modelFile(arm).rotationX(rotation[0]).rotationY(rotation[1]).addModel()
                    .condition(ConduitTapBlock.ARMS[direction.ordinal()], TapArm.CONDUIT)
                    .end();
            builder.part().modelFile(existingModelFile("conduit_tap_flange_" + direction.getSerializedName()))
                    .rotationX(rotation[0]).rotationY(rotation[1]).addModel()
                    .condition(ConduitTapBlock.ARMS[direction.ordinal()], TapArm.MACHINE)
                    .end();
            if (direction.getAxisDirection() == Direction.AxisDirection.POSITIVE) {
                builder.part().modelFile(collar).rotationX(rotation[0]).rotationY(rotation[1]).addModel()
                        .condition(ConduitTapBlock.ARMS[direction.ordinal()], TapArm.CONDUIT)
                        .end();
            }
        }
    }

    private void laserJunction(Block block) {
        MultiPartBlockStateBuilder builder = getMultipartBuilder(block);
        laserJunctionConnection(builder, block, Direction.DOWN, 0, 0);
        laserJunctionConnection(builder, block, Direction.UP, 180, 0);
        laserJunctionConnection(builder, block, Direction.NORTH, 90, 180);
        laserJunctionConnection(builder, block, Direction.EAST, 90, 270);
        laserJunctionConnection(builder, block, Direction.SOUTH, 90, 0);
        laserJunctionConnection(builder, block, Direction.WEST, 90, 90);
        builder.part().modelFile(existingModelFile(block, "_base")).addModel().end();
    }

    private void laserJunctionConnection(MultiPartBlockStateBuilder builder, Block block, Direction direction, int x, int y) {
        builder.part().modelFile(existingModelFile(block, "_connection_in")).rotationX(x).rotationY(y).addModel()
                .condition(LaserJunctionBlock.CONNECTION[direction.ordinal()], LaserJunctionBlock.ConnectionType.INPUT)
                .end();
        builder.part().modelFile(existingModelFile(block, "_connection_out")).rotationX(x).rotationY(y).addModel()
                .condition(LaserJunctionBlock.CONNECTION[direction.ordinal()], LaserJunctionBlock.ConnectionType.OUTPUT)
                .end();
    }

    public void existingFacingBlock(Block block, Block modelOf) {
        facingBlock(block, existingModelFile(modelOf));
    }

    public static int[] facingRotation(Direction direction) {
        return switch (direction) {
            case DOWN -> new int[]{180, 0};
            case NORTH -> new int[]{90, 0};
            case SOUTH -> new int[]{90, 180};
            case EAST -> new int[]{90, 90};
            case WEST -> new int[]{90, 270};
            default -> new int[]{0, 0};
        };
    }

    public void facingBlock(Block block, ModelFile model) {
        VariantBlockStateBuilder builder = getVariantBuilder(block);
        for (Direction direction : Direction.values()) {
            int[] rotation = facingRotation(direction);
            builder.partialState().with(BlockStateProperties.FACING, direction)
                    .setModels(rotated(model, rotation[0], rotation[1]));
        }
    }

    public void oilBarrel(Block block, ModelFile model, ModelFile openModel) {
        VariantBlockStateBuilder builder = getVariantBuilder(block);
        for (boolean open : new boolean[]{false, true}) {
            ModelFile variant = open ? openModel : model;
            for (Direction direction : Direction.values()) {
                int[] rotation = facingRotation(direction);
                builder.partialState().with(BlockStateProperties.FACING, direction).with(OilBarrelBlock.OPEN, open)
                        .setModels(rotated(variant, rotation[0], rotation[1]));
            }
        }
    }

    private void crateBlock(CrateBlock crateBlock) {
        booleanDispatch(crateBlock, BlockStateProperties.OPEN, existingModelFile(crateBlock, "_open"), existingModelFile(crateBlock));
    }

    private void rustyCrateBlock(CrateBlock crateBlock) {
        booleanDispatch(crateBlock, BlockStateProperties.OPEN, rustedCrateModel(crateBlock, true), rustedCrateModel(crateBlock, false));
    }

    private ModelFile rustedCrateModel(CrateBlock block, boolean open) {
        ResourceLocation id = Nautec.rl("block/" + name(block) + (open ? "_open" : ""));
        return createdModels.computeIfAbsent(id, key -> models()
                .withExistingParent(key.toString(), blockModelId(NTBlocks.CRATE.get(), open ? "_open" : ""))
                .texture("2", Nautec.rl("block/crate/rusty_top_inner"))
                .texture("4", Nautec.rl("block/crate/rusty"))
                .texture("5", Nautec.rl("block/crate/rusty_top"))
                .texture("particle", Nautec.rl("block/crate/rusty")));
    }

    public ResourceLocation multiblockTexture(Multiblock multiblock, String name) {
        return Nautec.rl("block/multiblock/" + NTRegistries.MULTIBLOCK.getKey(multiblock).getPath() + "/" + name);
    }

    private ModelFile createActiveACModel(AquaticCatalystBlock block, int stage, boolean active) {
        String suffix = active ? "_active" : "";
        if (active) {
            Map<Direction, ResourceLocation> faces = faces(blockTexture(block, "_bottom_active"), blockTexture(block, "_top_" + stage),
                    blockTexture(block, "_side_active"), blockTexture(block, "_side_active"));
            Map<Direction, ResourceLocation> glow = glow(blockTexture(block, "_bottom_active_emissive"), null, blockTexture(block, "_side_active_emissive"));
            return emissiveCube(name(block) + suffix + (stage != 0 ? ("_" + stage) : ""), faces, blockTexture(block, "_side"), glow);
        }
        return cube(name(block) + suffix + (stage != 0 ? ("_" + stage) : ""),
                blockTexture(block, "_bottom" + suffix),
                blockTexture(block, "_top_" + stage),
                blockTexture(block, "_side" + suffix),
                blockTexture(block, "_side" + suffix),
                blockTexture(block, "_side" + suffix),
                blockTexture(block, "_side" + suffix),
                blockTexture(block, "_side"));
    }

    public BlockModelBuilder cube(String name, ResourceLocation down, ResourceLocation up, ResourceLocation north, ResourceLocation south,
                                  ResourceLocation east, ResourceLocation west, ResourceLocation particle) {
        ResourceLocation id = Nautec.rl("block/" + name);
        return createdModels.computeIfAbsent(id, key -> models().cube(key.toString(), down, up, north, south, east, west)
                .texture("particle", particle));
    }

    private ModelFile artModel(Block block) {
        return models().withExistingParent(blockModelId(block).toString(), Nautec.rl("block/art/" + name(block)));
    }

    public BlockModelBuilder cubeTop(Block block, ResourceLocation side, ResourceLocation top) {
        return createdModels.computeIfAbsent(blockModelId(block), key -> models().cubeTop(key.toString(), side, top));
    }

    public BlockModelBuilder cubeAll(String name, ResourceLocation all) {
        ResourceLocation id = Nautec.rl("block/" + name);
        return createdModels.computeIfAbsent(id, key -> models().cubeAll(key.toString(), all));
    }

    public BlockModelBuilder cubeBottomTop(String name, ResourceLocation side, ResourceLocation bottom, ResourceLocation top) {
        ResourceLocation id = Nautec.rl("block/" + name);
        return createdModels.computeIfAbsent(id, key -> models().cubeBottomTop(key.toString(), side, bottom, top));
    }

    public static ConfiguredModel rotated(ModelFile model, int xRot, int yRot) {
        return new ConfiguredModel(model, xRot, yRot, false);
    }

    public ResourceLocation blockTexture(Block block, String suffix) {
        return ModelPaths.blockModel(key(block), suffix);
    }

    public ResourceLocation blockModelId(Block block) {
        return ModelPaths.blockModel(key(block));
    }

    public ResourceLocation blockModelId(Block block, String suffix) {
        return ModelPaths.blockModel(key(block), suffix);
    }

    public ModelFile existingModelFile(Block block) {
        return models().getExistingFile(blockModelId(block));
    }

    public ModelFile existingModelFile(Block block, String suffix) {
        return models().getExistingFile(blockModelId(block, suffix));
    }

    public ModelFile existingModelFile(String name) {
        return models().getExistingFile(Nautec.rl("block/" + name));
    }

    public ResourceLocation key(Block block) {
        return BuiltInRegistries.BLOCK.getKey(block);
    }

    public String name(Block block) {
        return key(block).getPath();
    }
}
