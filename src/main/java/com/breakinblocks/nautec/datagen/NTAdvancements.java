package com.breakinblocks.nautec.datagen;

import com.breakinblocks.nautec.Nautec;
import com.breakinblocks.nautec.registries.NTBlocks;
import com.breakinblocks.nautec.registries.NTCriteriaTriggers;
import com.breakinblocks.nautec.registries.NTEntities;
import com.breakinblocks.nautec.registries.NTItems;
import com.breakinblocks.nautec.tags.NTTags;
import com.breakinblocks.nautec.worldgen.NTBiomeKeys;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.AdvancementRequirements;
import net.minecraft.advancements.AdvancementRewards;
import net.minecraft.advancements.AdvancementType;
import net.minecraft.advancements.Criterion;
import net.minecraft.advancements.critereon.DamageSourcePredicate;
import net.minecraft.advancements.critereon.EntityPredicate;
import net.minecraft.advancements.critereon.FishingRodHookedTrigger;
import net.minecraft.advancements.critereon.InventoryChangeTrigger;
import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.advancements.critereon.KilledTrigger;
import net.minecraft.advancements.critereon.LocationPredicate;
import net.minecraft.advancements.critereon.PlayerTrigger;
import net.minecraft.advancements.critereon.StartRidingTrigger;
import net.minecraft.advancements.critereon.TagPredicate;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.advancements.AdvancementSubProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.biome.Biome;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

public class NTAdvancements implements AdvancementSubProvider {
    private static final ResourceLocation BACKGROUND = Nautec.rl("textures/block/polished_prismarine.png");

    private Consumer<AdvancementHolder> output;

    @Override
    public void generate(HolderLookup.Provider registries, Consumer<AdvancementHolder> output) {
        this.output = output;
        HolderGetter<Biome> biomes = registries.lookupOrThrow(Registries.BIOME);

        AdvancementHolder root = Advancement.Builder.advancement()
                .display(NTBlocks.AQUATIC_CATALYST, title("root"), description("root"), BACKGROUND, AdvancementType.TASK, false, false, false)
                .requirements(AdvancementRequirements.Strategy.OR)
                .addCriterion("catalyst", InventoryChangeTrigger.TriggerInstance.hasItems(NTBlocks.AQUATIC_CATALYST))
                .addCriterion("guide", InventoryChangeTrigger.TriggerInstance.hasItems(NTItems.NAUTEC_GUIDE))
                .addCriterion("diving_helmet", InventoryChangeTrigger.TriggerInstance.hasItems(NTItems.DIVING_HELMET))
                .addCriterion("relay", InventoryChangeTrigger.TriggerInstance.hasItems(NTBlocks.PRISMARINE_RELAY))
                .save(output, Nautec.MODID + ":root");

        AdvancementHolder firstBeam = task(root, "first_beam", NTBlocks.PRISMARINE_RELAY, AdvancementType.TASK, 0,
                Map.of("relay", has(NTBlocks.PRISMARINE_RELAY)));
        task(firstBeam, "prism_monocle", NTItems.PRISM_MONOCLE, AdvancementType.TASK, 0,
                Map.of("monocle", has(NTItems.PRISM_MONOCLE)));
        task(firstBeam, "augmentation_station", NTBlocks.AUGMENTATION_STATION, AdvancementType.TASK, 0,
                Map.of("station", has(NTBlocks.AUGMENTATION_STATION)));
        AdvancementHolder crystal = task(firstBeam, "prismarine_crystal", NTBlocks.PRISMARINE_CRYSTAL, AdvancementType.TASK, 0,
                Map.of("found", player(NTCriteriaTriggers.CRYSTAL_FOUND.get(), null)));
        AdvancementHolder resonance = task(crystal, "resonant_shard", NTItems.RESONANT_SHARD, AdvancementType.TASK, 0,
                Map.of("shard", has(NTItems.RESONANT_SHARD)));
        AdvancementHolder pressing = task(resonance, "pressing_the_deep", NTItems.FLAWLESS_PRISMARINE_CRYSTAL, AdvancementType.GOAL, 50,
                Map.of("crystal", has(NTItems.FLAWLESS_PRISMARINE_CRYSTAL), "plating", has(NTItems.DEEP_STEEL_PLATING)));
        AdvancementHolder rifle = task(pressing, "atlantean_rifle", NTItems.ATLANTEAN_RIFLE, AdvancementType.TASK, 0,
                Map.of("rifle", has(NTItems.ATLANTEAN_RIFLE)));
        task(rifle, "particles_accelerated", NTItems.ATLANTEAN_RIFLE, AdvancementType.CHALLENGE, 100,
                Map.of("killed", KilledTrigger.TriggerInstance.playerKilledEntity(Optional.empty(),
                        DamageSourcePredicate.Builder.damageType().tag(TagPredicate.is(NTTags.DamageTypes.ATLANTEAN_RIFLE)))));
        task(pressing, "neptunes_trident", NTItems.NEPTUNES_TRIDENT, AdvancementType.TASK, 0,
                Map.of("trident", has(NTItems.NEPTUNES_TRIDENT)));

        AdvancementHolder suited = task(root, "diving_suit", NTItems.DIVING_HELMET, AdvancementType.TASK, 0,
                Map.of("suit", InventoryChangeTrigger.TriggerInstance.hasItems(NTItems.DIVING_HELMET, NTItems.DIVING_CHESTPLATE,
                        NTItems.DIVING_LEGGINGS, NTItems.DIVING_BOOTS)));
        AdvancementHolder trench = task(suited, "abyssal_trench", NTBlocks.ABYSSAL_CORAL, AdvancementType.TASK, 0,
                Map.of("trench", inBiome(biomes, NTBiomeKeys.ABYSSAL_TRENCH)));
        task(trench, "deep_survey", NTBlocks.PRISMARINE_FROND, AdvancementType.CHALLENGE, 100,
                Map.of("abyssal_trench", inBiome(biomes, NTBiomeKeys.ABYSSAL_TRENCH),
                        "bioluminescent_grove", inBiome(biomes, NTBiomeKeys.BIOLUMINESCENT_GROVE),
                        "hydrothermal_vents", inBiome(biomes, NTBiomeKeys.HYDROTHERMAL_VENTS),
                        "prismarine_reef", inBiome(biomes, NTBiomeKeys.PRISMARINE_REEF)));
        task(trench, "abyssal_maw", NTItems.ABYSSAL_ORGAN, AdvancementType.GOAL, 50,
                Map.of("maw", KilledTrigger.TriggerInstance.playerKilledEntity(EntityPredicate.Builder.entity().of(NTEntities.ABYSSAL_MAW.get()))));
        task(root, "prismatic_angler", NTItems.NAUTEC_FISHING_ROD, AdvancementType.TASK, 0,
                Map.of("hooked", FishingRodHookedTrigger.TriggerInstance.fishedItem(
                        Optional.of(ItemPredicate.Builder.item().of(NTItems.NAUTEC_FISHING_ROD).build()), Optional.empty(), Optional.empty())));

        EntityPredicate.Builder inSubmarine = EntityPredicate.Builder.entity()
                .vehicle(EntityPredicate.Builder.entity().of(NTEntities.SUBMARINE.get()));
        AdvancementHolder seaScout = task(suited, "sea_scout", NTItems.SUBMARINE, AdvancementType.TASK, 0,
                Map.of("riding", StartRidingTrigger.TriggerInstance.playerStartsRiding(inSubmarine)));
        task(seaScout, "impulse_laser", NTItems.IMPULSE_LASER_MODULE, AdvancementType.TASK, 0,
                Map.of("fired", player(NTCriteriaTriggers.SUBMARINE_LASER.get(), null)));
        task(seaScout, "submarine_flight", NTItems.FLIGHT_MODULE, AdvancementType.GOAL, 50,
                Map.of("flying", player(NTCriteriaTriggers.SUBMARINE_FLIGHT.get(), null)));

        AdvancementHolder gatewayFound = task(root, "gateway_found", NTBlocks.GATEWAY, AdvancementType.TASK, 0,
                Map.of("found", player(NTCriteriaTriggers.GATEWAY_FOUND.get(), null)));
        AdvancementHolder gatewayTravel = task(gatewayFound, "gateway_travel", NTBlocks.GATEWAY_RING, AdvancementType.TASK, 0,
                Map.of("travelled", player(NTCriteriaTriggers.GATEWAY_TRAVEL.get(), null)));
        task(gatewayTravel, "sea_lane", NTItems.SUBMARINE, AdvancementType.CHALLENGE, 100,
                Map.of("piloted", player(NTCriteriaTriggers.GATEWAY_TRAVEL.get(), EntityPredicate.Builder.entity()
                        .vehicle(EntityPredicate.Builder.entity().of(NTEntities.SUBMARINE.get())))));
        task(gatewayTravel, "ring_maker", NTBlocks.GATEWAY, AdvancementType.GOAL, 50,
                Map.of("gateway", has(NTBlocks.GATEWAY)));

        AdvancementHolder tidal = task(root, "tidal_rotor", NTBlocks.TIDAL_ROTOR, AdvancementType.TASK, 0,
                Map.of("rotor", has(NTBlocks.TIDAL_ROTOR)));
        AdvancementHolder vent = task(tidal, "thermal_vent_tap", NTBlocks.THERMAL_VENT_TAP, AdvancementType.TASK, 0,
                Map.of("tap", has(NTBlocks.THERMAL_VENT_TAP)));
        task(vent, "fusion_ignition", NTBlocks.FUSION_CONTROLLER, AdvancementType.GOAL, 50,
                Map.of("running", player(NTCriteriaTriggers.FUSION_RUNNING.get(), null)));
        AdvancementHolder network = task(tidal, "resonance_network", NTBlocks.RESONANCE_NODE, AdvancementType.TASK, 0,
                Map.of("created", player(NTCriteriaTriggers.RESONANCE_NETWORK.get(), null)));
        task(network, "prismatic_emitter", NTBlocks.PRISMATIC_EMITTER, AdvancementType.TASK, 0,
                Map.of("linked", player(NTCriteriaTriggers.EMITTER_LINKED.get(), null)));
        task(network, "resonance_charm", NTItems.RESONANCE_CHARM, AdvancementType.TASK, 0,
                Map.of("charged", player(NTCriteriaTriggers.CHARM_CHARGED.get(), null)));
        AdvancementHolder launched = task(network, "satellite_launch", NTItems.PRISM_SATELLITE, AdvancementType.GOAL, 50,
                Map.of("launched", player(NTCriteriaTriggers.SATELLITE_LAUNCHED.get(), null)));
        task(launched, "orbital_relay", NTBlocks.RESONANCE_NODE, AdvancementType.CHALLENGE, 100,
                Map.of("relay", player(NTCriteriaTriggers.SATELLITE_RELAY.get(), null)));

        AdvancementHolder culture = task(root, "first_culture", NTItems.GRAFTING_TOOL, AdvancementType.TASK, 0,
                Map.of("grafted", player(NTCriteriaTriggers.BACTERIA_GRAFTED.get(), null)));
        AdvancementHolder mutation = task(culture, "bacteria_mutation", NTBlocks.MUTATOR, AdvancementType.GOAL, 50,
                Map.of("mutated", player(NTCriteriaTriggers.BACTERIA_MUTATED.get(), null)));
        task(mutation, "industrial_bio_reactor", NTBlocks.INDUSTRIAL_BIO_REACTOR, AdvancementType.TASK, 0,
                Map.of("reactor", has(NTBlocks.INDUSTRIAL_BIO_REACTOR)));
    }

    private AdvancementHolder task(AdvancementHolder parent, String name, ItemLike icon, AdvancementType type, int experience,
                                   Map<String, Criterion<?>> criteria) {
        Advancement.Builder builder = Advancement.Builder.advancement()
                .parent(parent)
                .display(icon, title(name), description(name), null, type, true, type != AdvancementType.TASK, false);
        criteria.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .forEach(entry -> builder.addCriterion(entry.getKey(), entry.getValue()));
        if (experience > 0) {
            builder.rewards(AdvancementRewards.Builder.experience(experience));
        }
        return builder.save(output, Nautec.MODID + ":" + name);
    }

    private static Criterion<InventoryChangeTrigger.TriggerInstance> has(ItemLike item) {
        return InventoryChangeTrigger.TriggerInstance.hasItems(item);
    }

    private static Criterion<PlayerTrigger.TriggerInstance> player(PlayerTrigger trigger, @Nullable EntityPredicate.Builder player) {
        return trigger.createCriterion(new PlayerTrigger.TriggerInstance(
                player == null ? Optional.empty() : Optional.of(EntityPredicate.wrap(player))));
    }

    private static Criterion<PlayerTrigger.TriggerInstance> inBiome(HolderGetter<Biome> biomes, ResourceKey<Biome> biome) {
        return PlayerTrigger.TriggerInstance.located(LocationPredicate.Builder.inBiome(biomes.getOrThrow(biome)));
    }

    private static Component title(String name) {
        return Component.translatable("advancements." + Nautec.MODID + "." + name + ".title");
    }

    private static Component description(String name) {
        return Component.translatable("advancements." + Nautec.MODID + "." + name + ".description");
    }
}
