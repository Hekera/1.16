package cofh.thermal.innovation.item;

import cofh.core.item.EnergyContainerItemAugmentable;
import cofh.core.util.ProxyUtils;
import cofh.core.util.helpers.ChatHelper;
import cofh.lib.item.IMultiModeItem;
import cofh.lib.util.AreaUtils;
import cofh.lib.util.Utils;
import cofh.lib.util.constants.Constants;
import cofh.thermal.innovation.network.packet.server.BlockLaserPacket;
import cofh.thermal.innovation.network.packet.server.EntityLaserPacket;
import cofh.thermal.lib.common.ThermalConfig;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.attributes.Attribute;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.entity.boss.dragon.EnderDragonEntity;
import net.minecraft.entity.boss.dragon.EnderDragonPartEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ProjectileHelper;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ItemStack;
import net.minecraft.item.UseAction;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.*;
import net.minecraft.util.math.*;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Set;

import static cofh.lib.util.constants.Constants.RGB_DURABILITY_FLUX;
import static cofh.lib.util.constants.NBTTags.*;
import static cofh.lib.util.helpers.AugmentableHelper.getPropertyWithDefault;
import static cofh.lib.util.helpers.AugmentableHelper.setAttributeFromAugmentAdd;
import static cofh.lib.util.references.CoreReferences.LIGHTNING_RESISTANCE;
import static cofh.thermal.lib.common.ThermalAugmentRules.createAllowValidator;

public class RFLaserItem extends EnergyContainerItemAugmentable implements IMultiModeItem {

    protected static final Set<Enchantment> VALID_ENCHANTS = new ObjectOpenHashSet<>();
    protected static final float RANGED_DAMAGE_MODIFIER = 0.1F;
    protected static final float BASE_RANGE = 16.0F;
    protected static final int BASE_EFFECT_DURATION = 10;
    protected static final int BASE_ENERGY_PER_TICK = 100;

    protected float blockProgress = 0.0F;
    protected BlockPos focusedBlock;

    static {
        VALID_ENCHANTS.add(Enchantments.POWER);
    }

    public RFLaserItem(Properties builder, int maxEnergy, int maxTransfer) {

        super(builder, maxEnergy, maxTransfer);

        ProxyUtils.registerItemModelProperty(this, new ResourceLocation("charged"), (stack, world, entity) -> getEnergyStored(stack) > 0 ? 1F : 0F);
        ProxyUtils.registerItemModelProperty(this, new ResourceLocation("active"), (stack, world, entity) -> getEnergyStored(stack) > 0 && hasActiveTag(stack) ? 1F : 0F);

        numSlots = () -> ThermalConfig.toolAugments;
        augValidator = createAllowValidator(TAG_AUGMENT_TYPE_UPGRADE, TAG_AUGMENT_TYPE_RF, TAG_AUGMENT_TYPE_REACH, TAG_AUGMENT_TYPE_ELEMENTAL);
    }

    @Override
    protected void tooltipDelegate(ItemStack stack, @Nullable World worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn) {

        tooltip.add(new TranslationTextComponent("info.cofh." + getElement(stack) + "_element"));
        if (getNumModes(stack) > 1) {
            addIncrementModeChangeTooltip(stack, worldIn, tooltip, flagIn);
        }
        super.tooltipDelegate(stack, worldIn, tooltip, flagIn);
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
                EntityRayTraceResult entityResult = ProjectileHelper.rayTraceEntities(player, pos, pos.add(view), searchVolume,
                        entity -> entity != null && EntityPredicates.CAN_AI_TARGET.test(entity), range * range);
                if (entityResult != null) {
                    EntityLaserPacket.sendToServer(entityResult.getEntity(), (float) entityResult.getHitVec().subtract(player.getEyePosition(1.0F)).length());
                    return;
                }

                BlockRayTraceResult blockResult = user.world.rayTraceBlocks(new RayTraceContext(pos, pos.add(view), RayTraceContext.BlockMode.COLLIDER, RayTraceContext.FluidMode.ANY, null));
                if (blockResult.getType() != RayTraceResult.Type.MISS) {
                    BlockLaserPacket.sendToServer(blockResult.getPos(), blockResult.getFace(), (float) blockResult.getHitVec().subtract(player.getEyePosition(1.0F)).length());
                    return;
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
        setAttributeFromAugmentAdd(subTag, augmentData, TAG_AUGMENT_ELEMENTAL);

        super.setAttributesFromAugment(container, augmentData);
    }

    protected float getAttackDamage(ItemStack stack) {

        return 2.0F + getBaseMod(stack);
    }

    protected float getAttackSpeed(ItemStack stack) {

        return -1.8F + getBaseMod(stack) / 10;
    }

    public float getEffectiveness(ItemStack stack, float distance) {

        float range = getRange(stack);
        float halfRange = range * 0.5F;
        return Math.max(0.0F, distance < halfRange ? 1.0F : (range - distance) / halfRange) * (2 + getBaseMod(stack));
    }

    public float getRangedAttackDamage(ItemStack stack, float distance) {

        return Math.max(0.0F, getEffectiveness(stack, distance) * RANGED_DAMAGE_MODIFIER);
    }

    protected int getEnergyPerTick(ItemStack stack) {

        return Math.round(BASE_ENERGY_PER_TICK * (1 + Utils.getItemEnchantmentLevel(Enchantments.POWER, stack) * 0.1F));
    }

    public float getRange(ItemStack stack) {

        return BASE_RANGE * (1 + getPropertyWithDefault(stack, TAG_AUGMENT_REACH, 0.0F));
    }

    public int getLaserColor(ItemStack stack) {

        switch (getElement(stack).name) {
            case "fire":
                return 0xDCA22B;
            case "ice":
                return 0x1DB6E4;
            case "earth":
                return 0x3B2E28;
            case "lightning":
                return 0xF5F258;
            default:
                return RGB_DURABILITY_FLUX;
        }
    }

    public AreaUtils.Element getElement(ItemStack stack) {

        int elements = (int) getPropertyWithDefault(stack, TAG_AUGMENT_ELEMENTAL, 0.0F);
        int mode = getMode(stack);
        if (mode <= 0) {
            return new AreaUtils.Element("none", null, null);
        }
        for (int i = 0; i < AreaUtils.ELEMENTS.length; i++) {
            if ((elements >> i & 1) > 0) {
                if (mode <= 1) {
                    return AreaUtils.ELEMENTS[i];
                }
                else {
                    mode--;
                }
            }
        }
        return new AreaUtils.Element("none", null, null);
    }

    public void attackRanged(ItemStack stack, PlayerEntity player, Entity target, float distance, float damageMultiplier, boolean attackNearby) {

        AreaUtils.Element element = getElement(stack);

        float damage = getRangedAttackDamage(stack, distance) * damageMultiplier;

        if (target instanceof EnderDragonPartEntity) {
            target = ((EnderDragonPartEntity) target).getParent();
        }
        if (target instanceof EnderDragonEntity) {
            damage = damage * 0.5F;
        }

        if (damage <= 0.001F) {
            return;
        }

        target.hurtResistantTime = 0;
        DamageSource damageSource = new EntityDamageSource("laser", player);
        if (target instanceof LivingEntity) {
            LivingEntity living = (LivingEntity) target;
            living.getAttribute(Attributes.KNOCKBACK_RESISTANCE).applyNonPersistentModifier(new AttributeModifier(Constants.UUID_LASER_KNOCKBACK_RESISTANCE, "Laser Damage", 1.0D, AttributeModifier.Operation.ADDITION));

            if (element.isElement() && player.world.getGameTime() % ((int) (50 / getEffectiveness(stack, distance))) == 0) {
                element.effectApplier.applyEffect(living, BASE_EFFECT_DURATION, 0);
            }
        }

        if (!(element.name.equals("lightning") && target instanceof LivingEntity && ((LivingEntity) target).isPotionActive(LIGHTNING_RESISTANCE))) {
            target.attackEntityFrom(damageSource, damage);
        }

        if (attackNearby && element.name.equals("lightning")) {
            Vector3d targetCenter = target.getBoundingBox().getCenter();
            Vector3d range = new Vector3d(4, 4, 4);
            List<Entity> nearbyEntities = player.world.getEntitiesInAABBexcluding(target, new AxisAlignedBB(targetCenter.subtract(range),
                    targetCenter.add(range)), (Entity e) -> e != null && EntityPredicates.CAN_AI_TARGET.test(e));
            for (Entity entity : nearbyEntities) {
                float distanceBetween = target.getDistance(target);
                if (distanceBetween <= 4) {
                    attackRanged(stack, player, entity, distance + distanceBetween, 0.25F, false);
                }
            }
        }

        if (target instanceof LivingEntity) {
            ((LivingEntity) target).getAttribute(Attributes.KNOCKBACK_RESISTANCE).removeModifier(Constants.UUID_LASER_KNOCKBACK_RESISTANCE);
        }
        target.hurtResistantTime = 10;
    }

    public void attackRanged(ItemStack stack, PlayerEntity player, Entity target, float distance) {
        attackRanged(stack, player, target, distance, 1.0F, true);
    }

    public float getBlockProgressTick(ItemStack stack, float distance) {
        switch (getElement(stack).name) {
            case "fire":
                return getEffectiveness(stack, distance) * 0.05F;
            case "ice":
                return getEffectiveness(stack, distance) * 0.09F;
            case "earth":
                return getEffectiveness(stack, distance) * 0.03F;
            case "lightning":
                return getEffectiveness(stack, distance) * 0.08F;
        }
        return 0.0F;
    }

    public void useRanged(ItemStack stack, PlayerEntity player, BlockPos pos, Direction face, float distance) {

        World world = player.world;
        if (!world.isBlockPresent(pos)) {
            return;
        }

        if (focusedBlock == null || !focusedBlock.equals(pos)) {
            blockProgress = 0.0F;
            focusedBlock = pos;
        }

        if (blockProgress < 1.0F) {
            blockProgress += getBlockProgressTick(stack, distance);
        }
        else {
            AreaUtils.Element element = getElement(stack);
            blockProgress = 0.0F;

            if (element.isElement() && element.name != "lightning") { //TODO: get creative with lightning
                if (!element.blockTransformer.transformBlock(player, world, pos, face) && element.name != "earth") {
                    element.blockTransformer.transformBlock(player, world, pos.offset(face), face);
                }
            }
        }
    }

    // endregion

    // region IAugmentableItem
    @Override
    public void updateAugmentState(ItemStack container, List<ItemStack> augments) {

        super.updateAugmentState(container, augments);
    }
    // endregion

    // region IMultiModeItem
    @Override
    public int getNumModes(ItemStack stack) {

        int elements = (int) getPropertyWithDefault(stack, TAG_AUGMENT_ELEMENTAL, 0.0F);
        return 1 + (elements & 1) + (elements >> 1 & 1) + (elements >> 2 & 1) + (elements >> 3 & 1);
    }

    @Override
    public void onModeChange(PlayerEntity player, ItemStack stack) {

        blockProgress = 0.0F;
        if (getNumModes(stack) <= 1) {
            return;
        }
        player.world.playSound(null, player.getPosition(), SoundEvents.BLOCK_LEVER_CLICK, SoundCategory.PLAYERS, 0.4F, 1.0F - 0.1F * getMode(stack));

        ChatHelper.sendIndexedChatMessageToPlayer(player, new TranslationTextComponent("info.cofh." + getElement(stack).name +"_element"));
    }
    // endregion
}