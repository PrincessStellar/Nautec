package com.breakinblocks.nautec.gametest.suite;

import com.breakinblocks.nautec.capabilities.item.ItemStackHandler;
import com.breakinblocks.nautec.content.blockentities.LaserPurificationSmelterBlockEntity;
import com.breakinblocks.nautec.content.blocks.LaserPurificationSmelterBlock;
import com.breakinblocks.nautec.content.conduits.TapRates;
import com.breakinblocks.nautec.registries.NTBlocks;
import com.breakinblocks.nautec.registries.NTItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public final class LaserPurificationSmelterTests {
    private static final BlockPos SMELTER = new BlockPos(4, 2, 4);
    private static final int[] SLOTS_PER_TIER = {1, 3, 5, 7, 9};

    private LaserPurificationSmelterTests() {
    }

    private static LaserPurificationSmelterBlockEntity place(GameTestHelper helper, int tier) {
        helper.setBlock(SMELTER, NTBlocks.LASER_PURIFICATION_SMELTER.get());
        LaserPurificationSmelterBlockEntity smelter = helper.getBlockEntity(SMELTER, LaserPurificationSmelterBlockEntity.class);
        ItemStackHandler items = smelter.getItemStackHandler();
        for (int slot = 0; slot < LaserPurificationSmelterBlockEntity.SLOTS; slot++) {
            items.setStackInSlot(slot, ItemStack.EMPTY);
        }
        items.setStackInSlot(LaserPurificationSmelterBlockEntity.UPGRADE_SLOT, upgrade(tier));
        return smelter;
    }

    private static ItemStack upgrade(int tier) {
        return switch (tier) {
            case 1 -> new ItemStack(NTItems.EDDY_UPGRADE.get());
            case 2 -> new ItemStack(NTItems.SURGE_UPGRADE.get());
            case 3 -> new ItemStack(NTItems.RIPTIDE_UPGRADE.get());
            case 4 -> new ItemStack(NTItems.MAELSTROM_UPGRADE.get());
            default -> ItemStack.EMPTY;
        };
    }

    private static void run(GameTestHelper helper, LaserPurificationSmelterBlockEntity smelter, int power, float purity, int ticks) {
        BlockPos origin = helper.absolutePos(SMELTER.above());
        for (int tick = 0; tick < ticks; tick++) {
            smelter.receivePower(power, Direction.UP, origin);
            smelter.receiveNewPurity(purity, Direction.UP, origin);
            smelter.commonTick();
        }
    }

    private static int count(ItemStackHandler items, int slot, Item item) {
        ItemStack stack = items.getStackInSlot(slot);
        return stack.is(item) ? stack.getCount() : 0;
    }

    public static void register(NTTestRegistrar r) {
        r.add("smelter/upgrades_unlock_slots_and_locked_slots_refuse_items", 20, helper -> {
            for (int tier = 0; tier < SLOTS_PER_TIER.length; tier++) {
                LaserPurificationSmelterBlockEntity smelter = place(helper, tier);
                ItemStackHandler items = smelter.getItemStackHandler();
                int unlocked = SLOTS_PER_TIER[tier];
                helper.assertValueEqual(smelter.unlockedSlots(), unlocked, "unlocked slots at tier " + tier);
                for (int lane = 0; lane < LaserPurificationSmelterBlockEntity.LANES; lane++) {
                    ItemStack left = items.insertItem(LaserPurificationSmelterBlockEntity.INPUT_START + lane, new ItemStack(Items.RAW_IRON, 4), false);
                    if (lane < unlocked) {
                        helper.assertTrue(left.isEmpty(), "unlocked lane " + lane + " at tier " + tier + " takes raw iron");
                    } else {
                        helper.assertValueEqual(left.getCount(), 4, "locked lane " + lane + " at tier " + tier + " refuses raw iron");
                        helper.assertTrue(items.getStackInSlot(LaserPurificationSmelterBlockEntity.INPUT_START + lane).isEmpty(),
                                "locked lane " + lane + " stays empty");
                    }
                }
            }
            LaserPurificationSmelterBlockEntity smelter = place(helper, 0);
            ItemStackHandler items = smelter.getItemStackHandler();
            helper.assertFalse(items.isItemValid(0, new ItemStack(Items.DIRT)), "an item with no smelting recipe is refused");
            helper.assertFalse(items.isItemValid(LaserPurificationSmelterBlockEntity.OUTPUT_START, new ItemStack(Items.RAW_IRON)),
                    "nothing is put into an output slot");
            helper.assertFalse(items.isItemValid(LaserPurificationSmelterBlockEntity.UPGRADE_SLOT, new ItemStack(Items.RAW_IRON)),
                    "the upgrade slot only takes conduit upgrades");
            helper.assertTrue(items.isItemValid(LaserPurificationSmelterBlockEntity.UPGRADE_SLOT, new ItemStack(NTItems.SURGE_UPGRADE.get())),
                    "the upgrade slot takes a conduit upgrade");

            items.setStackInSlot(LaserPurificationSmelterBlockEntity.INPUT_START + 4, new ItemStack(Items.RAW_GOLD, 5));
            ItemStack taken = items.extractItem(LaserPurificationSmelterBlockEntity.INPUT_START + 4, 5, false);
            helper.assertValueEqual(taken.getCount(), 5, "items in a locked slot can still be taken out");
            helper.succeed();
        });

        r.add("smelter/smelts_raw_iron_into_the_paired_output", 20, helper -> {
            LaserPurificationSmelterBlockEntity smelter = place(helper, 1);
            ItemStackHandler items = smelter.getItemStackHandler();
            items.setStackInSlot(LaserPurificationSmelterBlockEntity.INPUT_START + 1, new ItemStack(Items.RAW_IRON, 4));
            int interval = TapRates.of(1).itemInterval();
            run(helper, smelter, 100, 1F, interval - 1);
            helper.assertValueEqual(smelter.getStatus(), LaserPurificationSmelterBlockEntity.STATUS_SMELTING, "status while smelting");
            helper.assertTrue(smelter.getBlockState().getValue(LaserPurificationSmelterBlock.ACTIVE), "the smelter looks active while smelting");
            helper.assertTrue(items.getStackInSlot(LaserPurificationSmelterBlockEntity.OUTPUT_START + 1).isEmpty(), "nothing is done before the interval ends");
            run(helper, smelter, 100, 1F, 1);
            helper.assertValueEqual(count(items, LaserPurificationSmelterBlockEntity.OUTPUT_START + 1, Items.IRON_INGOT), 4,
                    "the paired output holds the iron ingots");
            helper.assertTrue(items.getStackInSlot(LaserPurificationSmelterBlockEntity.INPUT_START + 1).isEmpty(), "the raw iron is used up");
            helper.assertTrue(items.getStackInSlot(LaserPurificationSmelterBlockEntity.OUTPUT_START).isEmpty(), "other outputs stay empty");
            run(helper, smelter, 100, 1F, 1);
            helper.assertValueEqual(smelter.getStatus(), LaserPurificationSmelterBlockEntity.STATUS_IDLE, "status once the input is gone");
            helper.assertFalse(smelter.getBlockState().getValue(LaserPurificationSmelterBlock.ACTIVE), "the smelter stops looking active");
            helper.succeed();
        });

        r.add("smelter/base_rate_ignores_a_stronger_beam", 20, helper -> {
            LaserPurificationSmelterBlockEntity smelter = place(helper, 0);
            ItemStackHandler items = smelter.getItemStackHandler();
            items.setStackInSlot(LaserPurificationSmelterBlockEntity.INPUT_START, new ItemStack(Items.RAW_IRON, 20));
            TapRates rates = TapRates.of(0);
            run(helper, smelter, 100, 1F, rates.itemInterval());
            helper.assertValueEqual(count(items, LaserPurificationSmelterBlockEntity.OUTPUT_START, Items.IRON_INGOT), rates.itemsPerOp(),
                    "one interval with no upgrade smelts the base batch");
            helper.assertValueEqual(smelter.beamSpeed(), 1F, "a much stronger beam does not speed it up");
            helper.succeed();
        });

        r.add("smelter/stalls_when_the_beam_is_below_cost", 60, helper -> {
            LaserPurificationSmelterBlockEntity smelter = place(helper, 2);
            ItemStackHandler items = smelter.getItemStackHandler();
            items.setStackInSlot(LaserPurificationSmelterBlockEntity.INPUT_START, new ItemStack(Items.RAW_IRON, 4));
            items.setStackInSlot(LaserPurificationSmelterBlockEntity.INPUT_START + 1, new ItemStack(Items.RAW_COPPER, 4));
            int cost = 2 * LaserPurificationSmelterBlockEntity.multiplierForTier(2);
            run(helper, smelter, cost - 1, 1F, 40);
            helper.assertValueEqual(smelter.getRequiredPower(), cost, "two busy slots with a Surge Upgrade");
            helper.assertValueEqual(smelter.getStatus(), LaserPurificationSmelterBlockEntity.STATUS_WAITING, "status on a weak beam");
            helper.assertValueEqual(smelter.getProgress(), 0, "no progress on a weak beam");
            helper.assertTrue(items.getStackInSlot(LaserPurificationSmelterBlockEntity.OUTPUT_START).isEmpty(), "nothing smelts on a weak beam");
            helper.assertValueEqual(smelter.beamSpeed(), 0F, "beam speed below the cost");
            run(helper, smelter, cost, 1F, TapRates.of(2).itemInterval());
            helper.assertValueEqual(count(items, LaserPurificationSmelterBlockEntity.OUTPUT_START, Items.IRON_INGOT), 4, "iron smelts once the beam meets the cost");
            helper.assertValueEqual(count(items, LaserPurificationSmelterBlockEntity.OUTPUT_START + 1, Items.COPPER_INGOT), 4, "copper smelts beside it");
            helper.succeed();
        });

        r.add("smelter/purity_divides_the_cost_but_never_raises_it", 20, helper -> {
            helper.assertValueEqual(LaserPurificationSmelterBlockEntity.cost(3, 1, 2F), 2, "1.5 AP rounds up");
            helper.assertValueEqual(LaserPurificationSmelterBlockEntity.cost(0, 16, 1F), 0, "an idle smelter costs nothing");
            LaserPurificationSmelterBlockEntity smelter = place(helper, 4);
            ItemStackHandler items = smelter.getItemStackHandler();
            items.setStackInSlot(LaserPurificationSmelterBlockEntity.INPUT_START, new ItemStack(Items.RAW_IRON, 64));
            items.setStackInSlot(LaserPurificationSmelterBlockEntity.INPUT_START + 1, new ItemStack(Items.RAW_IRON, 64));
            int full = 2 * LaserPurificationSmelterBlockEntity.multiplierForTier(4);
            run(helper, smelter, 0, 1F, 1);
            helper.assertValueEqual(smelter.getRequiredPower(), full, "cost at purity 1.0");
            run(helper, smelter, 0, 2F, 1);
            helper.assertValueEqual(smelter.getRequiredPower(), full / 2, "cost halves at purity 2.0");
            run(helper, smelter, 0, 0.5F, 1);
            helper.assertValueEqual(smelter.getRequiredPower(), full, "purity below 1.0 gives no discount and no penalty");
            run(helper, smelter, 0, 0F, 1);
            helper.assertValueEqual(smelter.getRequiredPower(), full, "no purity at all is still accepted");
            run(helper, smelter, full / 2, 2F, TapRates.of(4).itemInterval());
            helper.assertTrue(count(items, LaserPurificationSmelterBlockEntity.OUTPUT_START, Items.IRON_INGOT) > 0, "half the AP runs it at purity 2.0");
            helper.succeed();
        });

        r.add("smelter/auto_distribute_spreads_one_item_type", 20, helper -> {
            LaserPurificationSmelterBlockEntity smelter = place(helper, 1);
            ItemStackHandler items = smelter.getItemStackHandler();
            items.setStackInSlot(LaserPurificationSmelterBlockEntity.INPUT_START, new ItemStack(Items.COBBLESTONE, 10));
            smelter.setAutoDistribute(true);
            run(helper, smelter, 0, 1F, 1);
            int total = 0;
            int largest = 0;
            int smallest = Integer.MAX_VALUE;
            for (int lane = 0; lane < 3; lane++) {
                int amount = count(items, LaserPurificationSmelterBlockEntity.INPUT_START + lane, Items.COBBLESTONE);
                total += amount;
                largest = Math.max(largest, amount);
                smallest = Math.min(smallest, amount);
            }
            helper.assertValueEqual(total, 10, "no cobblestone lost or made");
            helper.assertValueEqual(largest, 4, "the largest share");
            helper.assertValueEqual(smallest, 3, "the smallest share");
            helper.assertTrue(items.getStackInSlot(LaserPurificationSmelterBlockEntity.INPUT_START + 3).isEmpty(), "locked slots get nothing");
            smelter.setAutoDistribute(false);
            items.setStackInSlot(LaserPurificationSmelterBlockEntity.INPUT_START, new ItemStack(Items.COBBLESTONE, 10));
            items.setStackInSlot(LaserPurificationSmelterBlockEntity.INPUT_START + 1, ItemStack.EMPTY);
            items.setStackInSlot(LaserPurificationSmelterBlockEntity.INPUT_START + 2, ItemStack.EMPTY);
            run(helper, smelter, 0, 1F, 30);
            helper.assertValueEqual(count(items, LaserPurificationSmelterBlockEntity.INPUT_START, Items.COBBLESTONE), 10, "switched off, items stay put");
            helper.succeed();
        });

        r.add("smelter/auto_distribute_shares_slots_between_item_types", 20, helper -> {
            LaserPurificationSmelterBlockEntity smelter = place(helper, 2);
            ItemStackHandler items = smelter.getItemStackHandler();
            items.setStackInSlot(LaserPurificationSmelterBlockEntity.INPUT_START, new ItemStack(Items.RAW_IRON, 12));
            items.setStackInSlot(LaserPurificationSmelterBlockEntity.INPUT_START + 3, new ItemStack(Items.COBBLESTONE, 4));
            items.setStackInSlot(LaserPurificationSmelterBlockEntity.INPUT_START + 6, new ItemStack(Items.RAW_GOLD, 5));
            smelter.distribute();
            int iron = 0;
            int cobble = 0;
            int ironSlots = 0;
            int cobbleSlots = 0;
            for (int lane = 0; lane < 5; lane++) {
                ItemStack stack = items.getStackInSlot(LaserPurificationSmelterBlockEntity.INPUT_START + lane);
                if (stack.is(Items.RAW_IRON)) {
                    iron += stack.getCount();
                    ironSlots++;
                    helper.assertValueEqual(stack.getCount(), 3, "each iron slot holds an even share");
                } else if (stack.is(Items.COBBLESTONE)) {
                    cobble += stack.getCount();
                    cobbleSlots++;
                } else {
                    helper.fail("lane " + lane + " should hold iron or cobblestone, holds " + stack);
                }
            }
            helper.assertValueEqual(iron, 12, "no raw iron lost or made");
            helper.assertValueEqual(cobble, 4, "no cobblestone lost or made");
            helper.assertValueEqual(ironSlots, 4, "raw iron gets the larger share of slots");
            helper.assertValueEqual(cobbleSlots, 1, "cobblestone still gets a slot");
            helper.assertValueEqual(count(items, LaserPurificationSmelterBlockEntity.INPUT_START + 6, Items.RAW_GOLD), 5,
                    "items in a locked slot stay where they are");
            helper.succeed();
        });
    }
}
