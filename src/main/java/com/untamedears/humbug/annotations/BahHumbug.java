package com.untamedears.humbug.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.untamedears.humbug.annotations.OptType;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface BahHumbug {
  public String opt();
  public String def() default "";
  public OptType type() default OptType.Bool;
}
