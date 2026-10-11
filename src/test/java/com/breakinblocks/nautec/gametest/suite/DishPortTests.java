package com.breakinblocks.nautec.gametest.suite;

import com.breakinblocks.nautec.transfer.TransferCapabilities;
import com.breakinblocks.nautec.NTConfig;
import com.breakinblocks.nautec.api.bacteria.Bacteria;
import com.breakinblocks.nautec.api.bacteria.BacteriaInstance;
import com.breakinblocks.nautec.capabilities.NTCapabilities;
import com.breakinblocks.nautec.capabilities.bacteria.IBacteriaStorage;
import com.breakinblocks.nautec.content.blockentities.BacterialAnalyzerBlockEntity;
import com.breakinblocks.nautec.content.blockentities.BacterialFuelCellBlockEntity;
import com.breakinblocks.nautec.content.blockentities.ColonyReplicatorBlockEntity;
import com.breakinblocks.nautec.content.blockentities.IncubatorBlockEntity;
import com.breakinblocks.nautec.content.blockentities.MutatorBlockEntity;
import com.breakinblocks.nautec.content.blockentities.multiblock.controller.BioReactorBlockEntity;
import com.breakinblocks.nautec.registries.NTBacterias;
import com.breakinblocks.nautec.registries.NTBlocks;
import com.breakinblocks.nautec.registries.NTItems;
import com.breakinblocks.nautec.utils.ItemTemplates;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import com.breakinblocks.nautec.transfer.ResourceHandler;
import com.breakinblocks.nautec.transfer.item.ItemResource;
import com.breakinblocks.nautec.transfer.transaction.Transaction;

public final class DishPortTests {
    private static final BlockPos MACHINE = new BlockPos(4, 1, 4);

    private DishPortTests() {
    }

    public static void register(NTTestRegistrar r) {
        r.add("dish_port/incubator_loads_and_unloads_at_cap", 60, helper -> {
            helper.setBlock(MACHINE, NTBlocks.INCUBATOR.get());
            IncubatorBlockEntity incubator = helper.getBlockEntity(MACHINE, IncubatorBlockEntity.class);
            BacteriaInstance colony = colony(helper, NTBacterias.LITHOPHILES, 100);
            incubator.getItemStackHandler().setStackInSlot(IncubatorBlockEntity.DISH_IN, dish(colony));

            helper.runAfterDelay(12, () -> {
                helper.assertValueEqual(100L, incubator.getBacteriaStorage().getBacteria(0).getSize(), "colony loaded from the dish");
                ItemStack emptied = incubator.getItemStackHandler().getStackInSlot(IncubatorBlockEntity.DISH_IN);
                helper.assertTrue(emptied.is(NTItems.PETRI_DISH.get()) && contents(emptied).isEmpty(), "The emptied dish should stay in the port");
                helper.assertTrue(incubator.getItemStackHandler().getStackInSlot(IncubatorBlockEntity.DISH_EMPTY_OUT).isEmpty(),
                        "Nothing should go to the old empty dish slot");
            });
            helper.runAfterDelay(24, () -> {
                helper.assertFalse(incubator.getBacteriaStorage().getBacteria(0).isEmpty(), "A growing colony should stay put");
                helper.assertTrue(incubator.getItemStackHandler().getStackInSlot(IncubatorBlockEntity.DISH_OUT).isEmpty(), "Nothing should come out yet");

                BacteriaInstance grown = incubator.getBacteriaStorage().getBacteria(0).copyWithSize(NTConfig.bacteriaColonySizeCap);
                incubator.getBacteriaStorage().setBacteria(0, grown);
            });
            helper.runAfterDelay(36, () -> {
                helper.assertTrue(incubator.getBacteriaStorage().getBacteria(0).isEmpty(), "A colony at the cap should be taken out");
                BacteriaInstance out = contents(incubator.getItemStackHandler().getStackInSlot(IncubatorBlockEntity.DISH_OUT));
                helper.assertValueEqual(NTConfig.bacteriaColonySizeCap, out.getSize(), "size of the unloaded colony");
                helper.assertTrue(incubator.getItemStackHandler().getStackInSlot(IncubatorBlockEntity.DISH_IN).isEmpty(),
                        "The port should be free for the next colony");
                helper.succeed();
            });
        });

        r.add("dish_port/mutator_unloads_only_the_result", 40, helper -> {
            helper.setBlock(MACHINE, NTBlocks.MUTATOR.get());
            MutatorBlockEntity mutator = helper.getBlockEntity(MACHINE, MutatorBlockEntity.class);
            mutator.getBacteriaStorage().setBacteria(0, colony(helper, NTBacterias.LITHOPHILES, 50));
            mutator.getItemStackHandler().setStackInSlot(MutatorBlockEntity.DISH_IN, new ItemStack(NTItems.PETRI_DISH.get()));

            helper.runAfterDelay(12, () -> {
                helper.assertFalse(mutator.getBacteriaStorage().getBacteria(0).isEmpty(), "The input colony should never be pulled out");
                helper.assertTrue(mutator.getItemStackHandler().getStackInSlot(MutatorBlockEntity.DISH_OUT).isEmpty(), "No result yet");
                mutator.getBacteriaStorage().setBacteria(1, colony(helper, NTBacterias.CALCIOPHILES, 30));
            });
            helper.runAfterDelay(24, () -> {
                helper.assertTrue(mutator.getBacteriaStorage().getBacteria(1).isEmpty(), "The result should be taken out");
                BacteriaInstance out = contents(mutator.getItemStackHandler().getStackInSlot(MutatorBlockEntity.DISH_OUT));
                helper.assertValueEqual(NTBacterias.CALCIOPHILES, out.getBacteria(), "unloaded strain");
                helper.succeed();
            });
        });

        r.add("dish_port/mutator_reuses_the_emptied_dish", 40, helper -> {
            helper.setBlock(MACHINE, NTBlocks.MUTATOR.get());
            MutatorBlockEntity mutator = helper.getBlockEntity(MACHINE, MutatorBlockEntity.class);
            mutator.getItemStackHandler().setStackInSlot(MutatorBlockEntity.DISH_IN, dish(colony(helper, NTBacterias.LITHOPHILES, 50)));

            helper.runAfterDelay(12, () -> {
                helper.assertValueEqual(NTBacterias.LITHOPHILES, mutator.getBacteriaStorage().getBacteria(0).getBacteria(), "colony loaded");
                ItemStack held = mutator.getItemStackHandler().getStackInSlot(MutatorBlockEntity.DISH_IN);
                helper.assertTrue(held.is(NTItems.PETRI_DISH.get()) && contents(held).isEmpty(), "The emptied dish should wait in the port");
                helper.assertTrue(mutator.getItemStackHandler().getStackInSlot(MutatorBlockEntity.DISH_EMPTY_OUT).isEmpty(),
                        "Nothing should go to the old empty dish slot");
                mutator.getBacteriaStorage().setBacteria(0, BacteriaInstance.EMPTY);
                mutator.getBacteriaStorage().setBacteria(1, colony(helper, NTBacterias.CALCIOPHILES, 30));
            });
            helper.runAfterDelay(24, () -> {
                BacteriaInstance out = contents(mutator.getItemStackHandler().getStackInSlot(MutatorBlockEntity.DISH_OUT));
                helper.assertValueEqual(NTBacterias.CALCIOPHILES, out.getBacteria(), "the held dish carried the mutation out");
                helper.assertTrue(mutator.getItemStackHandler().getStackInSlot(MutatorBlockEntity.DISH_IN).isEmpty(),
                        "The port should be free for the next colony");
                helper.succeed();
            });
        });

        r.add("dish_port/mutator_reclaims_a_dish_from_the_old_slot", 30, helper -> {
            helper.setBlock(MACHINE, NTBlocks.MUTATOR.get());
            MutatorBlockEntity mutator = helper.getBlockEntity(MACHINE, MutatorBlockEntity.class);
            mutator.getItemStackHandler().setStackInSlot(MutatorBlockEntity.DISH_EMPTY_OUT, new ItemStack(NTItems.PETRI_DISH.get()));
            mutator.getBacteriaStorage().setBacteria(1, colony(helper, NTBacterias.CALCIOPHILES, 30));

            helper.runAfterDelay(12, () -> {
                helper.assertTrue(mutator.getItemStackHandler().getStackInSlot(MutatorBlockEntity.DISH_EMPTY_OUT).isEmpty(), "old slot emptied");
                BacteriaInstance out = contents(mutator.getItemStackHandler().getStackInSlot(MutatorBlockEntity.DISH_OUT));
                helper.assertValueEqual(NTBacterias.CALCIOPHILES, out.getBacteria(), "the reclaimed dish took the result out");
                helper.succeed();
            });
        });

        r.add("dish_port/mutator_waits_when_input_is_taken", 30, helper -> {
            helper.setBlock(MACHINE, NTBlocks.MUTATOR.get());
            MutatorBlockEntity mutator = helper.getBlockEntity(MACHINE, MutatorBlockEntity.class);
            mutator.getBacteriaStorage().setBacteria(0, colony(helper, NTBacterias.LITHOPHILES, 50));
            mutator.getItemStackHandler().setStackInSlot(MutatorBlockEntity.DISH_IN, dish(colony(helper, NTBacterias.HALOTROPHS, 40)));

            helper.runAfterDelay(12, () -> {
                helper.assertValueEqual(NTBacterias.LITHOPHILES, mutator.getBacteriaStorage().getBacteria(0).getBacteria(), "input strain untouched");
                helper.assertFalse(contents(mutator.getItemStackHandler().getStackInSlot(MutatorBlockEntity.DISH_IN)).isEmpty(),
                        "A dish that cannot load should wait in the port");
                helper.succeed();
            });
        });

        r.add("dish_port/ports_only_take_dishes_they_can_use", 40, helper -> {
            helper.setBlock(MACHINE, NTBlocks.COLONY_REPLICATOR.get());
            ColonyReplicatorBlockEntity replicator = helper.getBlockEntity(MACHINE, ColonyReplicatorBlockEntity.class);
            ResourceHandler<ItemResource> side = items(helper);
            ItemStack fodder = dish(colony(helper, NTBacterias.LITHOPHILES, 500));
            ItemStack wrongStrain = dish(colony(helper, NTBacterias.CALCIOPHILES, 500));
            ItemStack empty = new ItemStack(NTItems.PETRI_DISH.get());

            helper.assertValueEqual(0, insert(side, ColonyReplicatorBlockEntity.DISH_IN, fodder), "fodder with no template");
            helper.assertValueEqual(0, insert(side, ColonyReplicatorBlockEntity.DISH_IN, empty), "an empty dish with no copy waiting");
            replicator.getBacteriaStorage().setBacteria(ColonyReplicatorBlockEntity.TEMPLATE, colony(helper, NTBacterias.LITHOPHILES, 500));
            helper.assertValueEqual(0, insert(side, ColonyReplicatorBlockEntity.DISH_IN, wrongStrain), "fodder of the wrong strain");
            helper.assertValueEqual(1, insert(side, ColonyReplicatorBlockEntity.DISH_IN, fodder), "fodder of the template's strain");

            helper.setBlock(MACHINE.east(2), NTBlocks.MUTATOR.get());
            MutatorBlockEntity mutator = helper.getBlockEntity(MACHINE.east(2), MutatorBlockEntity.class);
            helper.assertFalse(mutator.getItemStackHandler().isItemValid(MutatorBlockEntity.DISH_IN, fodder), "a mutator with no catalyst");

            helper.setBlock(MACHINE.west(2), NTBlocks.INCUBATOR.get());
            IncubatorBlockEntity incubator = helper.getBlockEntity(MACHINE.west(2), IncubatorBlockEntity.class);
            helper.assertFalse(incubator.getItemStackHandler().isItemValid(IncubatorBlockEntity.DISH_IN, empty), "an empty dish before any colony is grown");
            helper.assertTrue(incubator.getItemStackHandler().isItemValid(IncubatorBlockEntity.DISH_IN, fodder), "a colony that can be incubated");
            ItemStack grown = dish(colony(helper, NTBacterias.LITHOPHILES, NTConfig.bacteriaColonySizeCap));
            helper.assertFalse(incubator.getItemStackHandler().isItemValid(IncubatorBlockEntity.DISH_IN, grown), "a fresh colony already at the cap");
            BacteriaInstance aged = colony(helper, NTBacterias.LITHOPHILES, NTConfig.bacteriaColonySizeCap);
            aged.setAge(100);
            helper.assertTrue(incubator.getItemStackHandler().isItemValid(IncubatorBlockEntity.DISH_IN, dish(aged)), "an aged colony at the cap");
            incubator.getBacteriaStorage().setBacteria(0, colony(helper, NTBacterias.LITHOPHILES, NTConfig.bacteriaColonySizeCap));
            helper.assertTrue(incubator.getItemStackHandler().isItemValid(IncubatorBlockEntity.DISH_IN, empty), "an empty dish once the colony is at the cap");
            helper.assertFalse(incubator.getItemStackHandler().isItemValid(IncubatorBlockEntity.DISH_IN, wrongStrain), "a second strain while one is growing");
            helper.succeed();
        });

        r.add("dish_port/fuel_cell_loads_from_its_port", 40, helper -> {
            helper.setBlock(MACHINE, NTBlocks.BACTERIAL_FUEL_CELL.get());
            BacterialFuelCellBlockEntity cell = helper.getBlockEntity(MACHINE, BacterialFuelCellBlockEntity.class);
            ResourceHandler<ItemResource> side = items(helper);
            helper.assertTrue(side != null, "The fuel cell should expose items to automation");
            helper.assertValueEqual(0, insert(side, BacterialFuelCellBlockEntity.DISH_IN, new ItemStack(NTItems.PETRI_DISH.get())), "an empty dish");
            helper.assertValueEqual(1, insert(side, BacterialFuelCellBlockEntity.DISH_IN, dish(colony(helper, NTBacterias.LITHOPHILES, 300))), "a colony dish");

            helper.runAfterDelay(12, () -> {
                helper.assertValueEqual(NTBacterias.LITHOPHILES, cell.getBacteriaStorage().getBacteria(0).getBacteria(), "loaded strain");
                ItemStack emptied = cell.getItemStackHandler().getStackInSlot(BacterialFuelCellBlockEntity.DISH_EMPTY_OUT);
                helper.assertTrue(emptied.is(NTItems.PETRI_DISH.get()) && contents(emptied).isEmpty(), "The emptied dish should come out");
                cell.getItemStackHandler().setStackInSlot(BacterialFuelCellBlockEntity.DISH_EMPTY_OUT, ItemStack.EMPTY);
                helper.assertValueEqual(0, insert(side, BacterialFuelCellBlockEntity.DISH_IN, dish(colony(helper, NTBacterias.CALCIOPHILES, 300))),
                        "a different colony while one is loaded");
                helper.succeed();
            });
        });

        r.add("dish_port/templates_match_dishes_by_strain", 20, helper -> {
            ItemStack emptyTemplate = new ItemStack(NTItems.PETRI_DISH.get());
            ItemStack strainTemplate = dish(colony(helper, NTBacterias.LITHOPHILES, 100));
            ItemResource empty = ItemResource.of(new ItemStack(NTItems.PETRI_DISH.get()));
            ItemResource sameStrain = ItemResource.of(dish(colony(helper, NTBacterias.LITHOPHILES, 7_000)));
            ItemResource otherStrain = ItemResource.of(dish(colony(helper, NTBacterias.CALCIOPHILES, 100)));

            helper.assertTrue(ItemTemplates.matches(emptyTemplate, empty), "an empty dish matches an empty template");
            helper.assertFalse(ItemTemplates.matches(emptyTemplate, sameStrain), "a colony does not match an empty template");
            helper.assertTrue(ItemTemplates.matches(strainTemplate, sameStrain), "the same strain at another size matches");
            helper.assertFalse(ItemTemplates.matches(strainTemplate, otherStrain), "another strain does not match");
            helper.assertFalse(ItemTemplates.matches(strainTemplate, empty), "an empty dish does not match a strain template");
            helper.assertTrue(ItemTemplates.matches(new ItemStack(Items.IRON_ORE), ItemResource.of(new ItemStack(Items.IRON_ORE, 5))), "plain items match");
            ItemStack named = new ItemStack(Items.IRON_ORE);
            named.set(DataComponents.CUSTOM_NAME, Component.literal("x"));
            helper.assertFalse(ItemTemplates.matches(new ItemStack(Items.IRON_ORE), ItemResource.of(named)), "components must match");
            helper.succeed();
        });

        r.add("dish_port/basic_analyzer_refuses_empty_dishes", 20, helper -> {
            helper.setBlock(MACHINE, NTBlocks.BACTERIAL_ANALYZER.get());
            BacterialAnalyzerBlockEntity analyzer = helper.getBlockEntity(MACHINE, BacterialAnalyzerBlockEntity.class);
            helper.assertFalse(analyzer.getItemStackHandler().isItemValid(0, new ItemStack(NTItems.PETRI_DISH.get())), "an empty dish");
            helper.assertTrue(analyzer.getItemStackHandler().isItemValid(0, dish(colony(helper, NTBacterias.LITHOPHILES, 100))), "an unanalyzed colony");
            helper.succeed();
        });

        r.add("dish_port/reactor_automation_round_trip", 60, helper -> {
            helper.setBlock(MACHINE, NTBlocks.BIO_REACTOR.get());
            BioReactorBlockEntity reactor = helper.getBlockEntity(MACHINE, BioReactorBlockEntity.class);
            reactor.getBacteriaStorage().setBacteria(0, colony(helper, NTBacterias.LITHOPHILES, 80));
            reactor.setVitality(0, 500);
            ResourceHandler<ItemResource> side = helper.getLevel().getCapability(TransferCapabilities.Item.BLOCK, helper.absolutePos(MACHINE), Direction.NORTH);
            helper.assertTrue(side != null, "The reactor should expose items to automation");

            ItemStack loaded = dish(colony(helper, NTBacterias.CALCIOPHILES, 60));
            try (Transaction tx = Transaction.openRoot()) {
                helper.assertValueEqual(1, side.insert(reactor.dishInSlot(), ItemResource.of(loaded), 1, tx), "dish pushed into the port");
                tx.commit();
            }

            helper.runAfterDelay(12, () -> {
                helper.assertValueEqual(NTBacterias.CALCIOPHILES, reactor.getBacteriaStorage().getBacteria(1).getBacteria(),
                        "The colony should load into the first empty slot");
                ItemStack empty = reactor.getItemStackHandler().getStackInSlot(reactor.dishEmptyOutSlot());
                helper.assertTrue(empty.is(NTItems.PETRI_DISH.get()) && contents(empty).isEmpty(), "The emptied dish should be in the empty dish output");
                helper.assertTrue(reactor.getItemStackHandler().getStackInSlot(reactor.dishOutSlot()).isEmpty(), "Nothing in the colony output yet");
                try (Transaction tx = Transaction.openRoot()) {
                    helper.assertValueEqual(1, side.extract(reactor.dishEmptyOutSlot(), ItemResource.of(empty), 1, tx), "empty dish pulled out");
                    tx.commit();
                }
                try (Transaction tx = Transaction.openRoot()) {
                    side.insert(reactor.dishInSlot(), ItemResource.of(new ItemStack(NTItems.PETRI_DISH.get())), 1, tx);
                    tx.commit();
                }
            });
            helper.runAfterDelay(24, () -> {
                helper.assertFalse(reactor.getBacteriaStorage().getBacteria(0).isEmpty(), "The well fed colony should stay");
                helper.assertTrue(reactor.getBacteriaStorage().getBacteria(1).isEmpty(), "The unfed colony should be the one taken out");
                BacteriaInstance out = contents(reactor.getItemStackHandler().getStackInSlot(reactor.dishOutSlot()));
                helper.assertValueEqual(NTBacterias.CALCIOPHILES, out.getBacteria(), "unloaded strain");
                helper.succeed();
            });
        });

        r.add("dish_port/full_reactor_swaps_out_the_oldest_senescent", 40, helper -> {
            helper.setBlock(MACHINE, NTBlocks.BIO_REACTOR.get());
            BioReactorBlockEntity reactor = helper.getBlockEntity(MACHINE, BioReactorBlockEntity.class);
            int hungry = reactor.getColonySlots() - 1;
            int oldest = 1;
            for (int slot = 0; slot < reactor.getColonySlots(); slot++) {
                reactor.getBacteriaStorage().setBacteria(slot, colony(helper, NTBacterias.LITHOPHILES, 80 + slot));
                reactor.setVitality(slot, slot == hungry ? 10 : 500);
            }
            ResourceHandler<ItemResource> side = items(helper);
            helper.assertValueEqual(0, insert(side, reactor.dishInSlot(), dish(colony(helper, NTBacterias.CALCIOPHILES, 60))),
                    "a full reactor with no senescent colony keeps the dish out");

            for (int slot = 0; slot <= oldest; slot++) {
                BacteriaInstance aged = colony(helper, NTBacterias.LITHOPHILES, 80 + slot);
                aged.setAge(aged.getStats().lifespan() + 100L * (slot + 1));
                reactor.getBacteriaStorage().setBacteria(slot, aged);
            }
            helper.assertValueEqual(1, insert(side, reactor.dishInSlot(), dish(colony(helper, NTBacterias.CALCIOPHILES, 60))),
                    "a full reactor takes a colony dish to swap for a senescent colony");

            helper.runAfterDelay(12, () -> {
                helper.assertValueEqual(NTBacterias.CALCIOPHILES, reactor.getBacteriaStorage().getBacteria(oldest).getBacteria(),
                        "The new colony should take the oldest senescent colony's slot");
                helper.assertValueEqual(NTBacterias.LITHOPHILES, reactor.getBacteriaStorage().getBacteria(hungry).getBacteria(),
                        "The colony with the least nutrient buffer stays when it is not senescent");
                BacteriaInstance out = contents(reactor.getItemStackHandler().getStackInSlot(reactor.dishOutSlot()));
                helper.assertValueEqual(NTBacterias.LITHOPHILES, out.getBacteria(), "the old colony leaves in the same dish");
                helper.assertValueEqual(80L + oldest, out.getSize(), "size of the swapped out colony");
                helper.assertTrue(reactor.getItemStackHandler().getStackInSlot(reactor.dishEmptyOutSlot()).isEmpty(), "no empty dish comes out");
                helper.assertValueEqual(0, insert(side, reactor.dishInSlot(), dish(colony(helper, NTBacterias.HALOTROPHS, 60))),
                        "no second swap while the colony output is full");
                helper.succeed();
            });
        });
    }

    static BacteriaInstance colony(NTGameTestHelper helper, ResourceKey<Bacteria> key, long size) {
        return BacteriaInstance.roll(key, helper.getLevel().registryAccess()).copyWithSize(size);
    }

    static ItemStack dish(BacteriaInstance colony) {
        ItemStack stack = new ItemStack(NTItems.PETRI_DISH.get());
        IBacteriaStorage storage = stack.getCapability(NTCapabilities.BacteriaStorage.ITEM);
        storage.setBacteria(0, colony);
        return stack;
    }

    private static int insert(ResourceHandler<ItemResource> handler, int slot, ItemStack stack) {
        try (Transaction tx = Transaction.openRoot()) {
            int inserted = handler.insert(slot, ItemResource.of(stack), stack.getCount(), tx);
            tx.commit();
            return inserted;
        }
    }

    private static ResourceHandler<ItemResource> items(NTGameTestHelper helper) {
        return helper.getLevel().getCapability(TransferCapabilities.Item.BLOCK, helper.absolutePos(MACHINE), Direction.NORTH);
    }

    private static BacteriaInstance contents(ItemStack stack) {
        IBacteriaStorage storage = stack.getCapability(NTCapabilities.BacteriaStorage.ITEM);
        return storage == null ? BacteriaInstance.EMPTY : storage.getBacteria(0);
    }
}
