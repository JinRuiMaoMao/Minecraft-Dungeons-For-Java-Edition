package mcd_java.factories;

import net.fabricmc.fabric.api.object.builder.v1.trade.TradeOfferHelper;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.entity.npc.VillagerTrades;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.trading.MerchantOffer;
import org.jetbrains.annotations.NotNull;

public record BasicTradeFactory(MerchantOffer trade) implements VillagerTrades.ItemListing {

    public static BasicTradeFactory createTrade(
            Item buySlot1, int buyAmount1,
            Item sellSlot, int sellAmount,
            int maxUses, int merchantExperience, float priceMultiplier) {
        return new BasicTradeFactory(new MerchantOffer(
                // Buying
                new ItemStack(buySlot1, buyAmount1),
                // Selling
                new ItemStack(sellSlot, sellAmount),
                // Other Info
                maxUses, merchantExperience, priceMultiplier));
    }

    public static BasicTradeFactory createTrade(
            Item buySlot1, int buyAmount1, Item buySlot2, int buyAmount2,
            Item sellSlot, int sellAmount,
            int maxUses, int merchantExperience, float priceMultiplier) {
        return new BasicTradeFactory(new MerchantOffer(
                // Buying
                new ItemStack(buySlot1, buyAmount1),
                new ItemStack(buySlot2, buyAmount2),
                // Selling
                new ItemStack(sellSlot, sellAmount),
                // Other Info
                maxUses, merchantExperience, priceMultiplier));
    }
    public static void registerVillagerTrade(VillagerProfession villagerProfession, int level,
                                             Item buySlot1, int buyAmount1,
                                             Item sellSlot, int sellAmount,
                                             int maxUses, int merchantExperience, float priceMultiplier) {
        TradeOfferHelper.registerVillagerOffers(villagerProfession, level,
                factories -> factories.add(
                        BasicTradeFactory.createTrade(
                                buySlot1, buyAmount1,
                                sellSlot, sellAmount,
                                maxUses, merchantExperience, priceMultiplier)
                )
        );
    }
    public static void registerVillagerTrade(VillagerProfession villagerProfession, int level,
                                             Item buySlot1, int buyAmount1, Item buySlot2, int buyAmount2,
                                             Item sellSlot, int sellAmount,
                                             int maxUses, int merchantExperience, float priceMultiplier) {
        TradeOfferHelper.registerVillagerOffers(villagerProfession, level,
                factories -> factories.add(
                        BasicTradeFactory.createTrade(
                                buySlot1, buyAmount1,
                                buySlot2, buyAmount2,
                                sellSlot, sellAmount,
                                maxUses, merchantExperience, priceMultiplier)
                )
        );
    }
    public static void registerWanderingTrade(Item buySlot1, int buyAmount1, Item sellSlot, int sellAmount,
                                              int maxUses, int merchantExperience, float priceMultiplier) {
        TradeOfferHelper.registerWanderingTraderOffers(1,
                factories -> factories.add(
                        BasicTradeFactory.createTrade(
                                buySlot1, buyAmount1,
                                sellSlot, sellAmount,
                                maxUses, merchantExperience, priceMultiplier)
                )
        );
    }
    @Override
    public @NotNull MerchantOffer getOffer(Entity entity, net.minecraft.util.RandomSource random) {
        return new MerchantOffer(this.trade.createTag());
    }
}