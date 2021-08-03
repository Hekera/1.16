package cofh.thermal.innovation.item;

import cofh.core.item.EnergyContainerItemAugmentable;
import cofh.core.util.ProxyUtils;
import cofh.lib.util.Utils;
import cofh.thermal.lib.common.ThermalConfig;
import cofh.thermal.innovation.network.packet.server.EntityLaserPacket;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.attributes.Attribute;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ProjectileHelper;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ItemStack;
import net.minecraft.item.UseAction;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EntityPredicates;
import net.minecraft.util.Hand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.EntityRayTraceResult;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;

import java.util.List;
import java.util.Set;

import static cofh.lib.util.constants.NBTTags.*;
import static cofh.lib.util.helpers.AugmentableHelper.getPropertyWithDefault;
import static cofh.lib.util.helpers.AugmentableHelper.setAttributeFromAugmentAdd;
import static cofh.thermal.lib.common.ThermalAugmentRules.createAllowValidator;

public class RFLaserItem extends EnergyContainerItemAugmentable {

    protected static final Set<Enchantment> VALID_ENCHANTS = new ObjectOpenHashSet<>();
    protected static final float BASE_ATTACK = 0.2F;
    protected static final float BASE_RANGE = 16.0F;
    protected static final int BASE_ENERGY_PER_TICK = 100;

    static {
        VALID_ENCHANTS.add(Enchantments.POWER);
    }

    public RFLaserItem(Properties builder, int maxEnergy, int maxTransfer) {
        super(builder, maxEnergy, maxTransfer);

        ProxyUtils.registerItemModelProperty(this, new ResourceLocation("charged"), (stack, world, entity) -> getEnergyStored(stack) > 0 ? 1F : 0F);
        ProxyUtils.registerItemModelProperty(this, new ResourceLocation("active"), (stack, world, entity) -> getEnergyStored(stack) > 0 && hasActiveTag(stack) ? 1F : 0F);

        numSlots = () -> ThermalConfig.toolAugments;
        augValidator = createAllowValidator(TAG_AUGMENT_TYPE_UPGRADE, TAG_AUGMENT_TYPE_RF, TAG_AUGMENT_TYPE_REACH);
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World world, PlayerEntity player, Hand hand) {
        ItemStack stack = player.getHeldItem(hand);
        if (getEnergyStored(stack) >= getEnergyPerTick(stack)) {
            setActive(stack, true);
            player.setActiveHand(hand);
            return ActionResult.resultConsume(stack);
        }
        return ActionResult.resultPass(stack);
    }

    @Override
    public void onUsingTick(ItemStack stack, LivingEntity user, int count) {
        if (user instanceof PlayerEntity && user.world.isRemote()) {
            PlayerEntity player = (PlayerEntity) user;

            if (!player.isCreative() && getEnergyStored(stack) < getEnergyPerTick(stack)) {
                player.resetActiveHand();
            }
            else {
                if (!player.isCreative()) {
                    extractEnergy(stack, getEnergyPerTick(stack), false);
                }

                double range = this.getRange(stack);
                Vector3d pos = player.getEyePosition(1.0F);
                Vector3d view = player.getLook(1.0F).scale(range);
                AxisAlignedBB searchVolume = player.getBoundingBox().expand(view).grow(1.0D, 1.0D, 1.0D);

                EntityRayTraceResult result = ProjectileHelper.rayTraceEntities(player, pos, pos.add(view), searchVolume,
                        entity -> entity != null && EntityPredicates.CAN_AI_TARGET.test(entity), range * range);
                if (result != null) {
                    EntityLaserPacket.sendToServer(result.getEntity().getEntityId(), (float) result.getHitVec().subtract(player.getEyePosition(1.0F)).length());
                }
            }
        }
    }

    @Override
    public void onPlayerStoppedUsing(ItemStack stack, World world, LivingEntity player, int useDuration) {
        stack.getOrCreateTag().remove(TAG_ACTIVE);
        super.onPlayerStoppedUsing(stack, world, player, useDuration);
    }

    @Override
    public UseAction getUseAction(ItemStack stack) {
        return UseAction.BOW;
    }

    @Override
    public int getUseDuration(ItemStack stack) {
        return 72000;
    }

    @Override
    public boolean canApplyAtEnchantingTable(ItemStack stack, Enchantment enchantment) {
        return super.canApplyAtEnchantingTable(stack, enchantment) || VALID_ENCHANTS.contains(enchantment);
    }

    @Override
    public Multimap<Attribute, AttributeModifier> getAttributeModifiers(EquipmentSlotType slot, ItemStack stack) {

        Multimap<Attribute, AttributeModifier> multimap = HashMultimap.create();
        if (slot == EquipmentSlotType.MAINHAND) {
            multimap.put(Attributes.ATTACK_DAMAGE, new AttributeModifier(ATTACK_DAMAGE_MODIFIER, "Tool modifier", getAttackDamage(stack), AttributeModifier.Operation.ADDITION));
            multimap.put(Attributes.ATTACK_SPEED, new AttributeModifier(ATTACK_SPEED_MODIFIER, "Tool modifier", getAttackSpeed(stack), AttributeModifier.Operation.ADDITION));
        }
        return multimap;
    }

    // region HELPERS
    @Override
    protected void setAttributesFromAugment(ItemStack container, CompoundNBT augmentData) {
        CompoundNBT subTag = container.getChildTag(TAG_PROPERTIES);
        if (subTag == null) {
            return;
        }
        setAttributeFromAugmentAdd(subTag, augmentData, TAG_AUGMENT_REACH);

        super.setAttributesFromAugment(container, augmentData);
    }

    protected float getAttackDamage(ItemStack stack) {
        return 2.0F + getBaseMod(stack);
    }

    protected float getAttackSpeed(ItemStack stack) {
        return -1.8F + getBaseMod(stack) / 10;
    }

    public float getRangedAttackDamage(ItemStack stack, float distance) {
        float baseDamage = (BASE_ATTACK + getBaseMod(stack) * 0.1F) * (1 + Utils.getItemEnchantmentLevel(Enchantments.POWER, stack) * 0.1F);
        float halfRange = this.getRange(stack) / 2.0F;
        return Math.max(0.0F, distance < halfRange ? baseDamage : (this.getRange(stack) - distance) * baseDamage / halfRange);
    }

    protected int getEnergyPerTick(ItemStack stack) {
        return Math.round(BASE_ENERGY_PER_TICK * (1 + Utils.getItemEnchantmentLevel(Enchantments.POWER, stack) * 0.1F));
    }

    public float getRange(ItemStack stack) {
        return BASE_RANGE * (1 + getPropertyWithDefault(stack, TAG_AUGMENT_REACH, 0.0F));
    }

    // endregion


    // region IAugmentableItem
    @Override
    public void updateAugmentState(ItemStack container, List<ItemStack> augments) {
        super.updateAugmentState(container, augments);
    }
    // endregion
}