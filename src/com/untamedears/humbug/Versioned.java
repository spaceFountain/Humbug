package com.untamedears.humbug;

import org.bukkit.craftbukkit.v1_7_R4.entity.CraftHorse;
import net.minecraft.server.v1_7_R4.EntityHorse;
import net.minecraft.server.v1_7_R4.GenericAttributes;
import org.bukkit.entity.Entity;

public class Versioned {
  // Static class
  private Versioned() {}

  public static double getHorseSpeed(Entity entity) {
    if (!(entity instanceof CraftHorse)) {
      return -1.0000001;
    }
    EntityHorse mcHorseAlot = ((CraftHorse)entity).getHandle();
    // GenericAttributes.d == generic.movementSpeed
    return mcHorseAlot.getAttributeInstance(GenericAttributes.d).b();
  }

  public static void setHorseSpeed(Entity entity, double speedModifier) {
    if (!(entity instanceof CraftHorse)) {
      return;
    }
    EntityHorse mcHorseAlot = ((CraftHorse)entity).getHandle();
    mcHorseAlot.getAttributeInstance(GenericAttributes.d).setValue(speedModifier);
  }
}
