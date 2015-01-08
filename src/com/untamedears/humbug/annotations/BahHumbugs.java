package com.untamedears.humbug.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.untamedears.humbug.annotations.BahHumbug;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface BahHumbugs {
  public BahHumbug[] value();
}
