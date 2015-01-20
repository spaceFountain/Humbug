package com.untamedears.humbug;


import net.minecraft.server.v1_8_R1.EntityHuman;
import net.minecraft.server.v1_8_R1.ItemEnderPearl;
import net.minecraft.server.v1_8_R1.ItemStack;
import net.minecraft.server.v1_8_R1.World;

public class CustomNMSItemEnderPearl extends ItemEnderPearl {
  private Config cfg_;

  public CustomNMSItemEnderPearl(Config cfg) {
    super();
    cfg_ = cfg;
  }

  public ItemStack a(
      ItemStack itemstack,
      World world,
      EntityHuman entityhuman) {
    if (entityhuman.abilities.canInstantlyBuild) {
      return itemstack;
    } else if (entityhuman.vehicle != null) {
      return itemstack;
    } else {
      --itemstack.count;
      world.makeSound(
          entityhuman,
          "random.bow",
          0.5F,
          0.4F / (g.nextFloat() * 0.4F + 0.8F));
      if (!world.isStatic) {
        double gravity = cfg_.get("ender_pearl_gravity").getDouble();
        world.addEntity(new CustomNMSEntityEnderPearl(world, entityhuman, gravity));
      }
      return itemstack;
    }
  }
}
