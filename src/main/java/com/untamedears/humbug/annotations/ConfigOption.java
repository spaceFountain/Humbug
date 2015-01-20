package com.untamedears.humbug.annotations;

import com.untamedears.humbug.Config;

public class ConfigOption {
  private final String name_;
  private final OptType type_;
  private final Object default_;
  private Object value_;

  public ConfigOption(BahHumbug bug) {
    name_ = bug.opt();
    type_ = bug.type();
    Object failure = new Object();
    default_ = convert(bug.def(), failure);
    if (default_ == failure) {
      throw new Error(String.format(
          "Unable to parse default value for %s: %s",
          name_, bug.def()));
    }
    value_ = default_;
    load();
  }

  public void load() {
    if (!Config.getStorage().isSet(name_)) {
      return;
    }
    switch(type_) {
      case Bool:
        set(Config.getStorage().getBoolean(name_, (Boolean)value_));
        break;
      case Int:
        set(Config.getStorage().getInt(name_, (Integer)value_));
        break;
      case Double:
        set(Config.getStorage().getDouble(name_, (Double)value_));
        break;
      case String:
        set(Config.getStorage().getString(name_, (String)value_));
        break;
      default:
        throw new Error("Unknown OptType");
    }
  }

  public Object convert(String value, Object defaultValue) {
    switch(type_) {
      case Bool:
        return value.equals("1") || value.equalsIgnoreCase("true");
      case Int:
        if (value.isEmpty()) {
          return -1;
        }
        try {
          return Integer.parseInt(value);
        } catch(Exception e) {
          return defaultValue;
        }
      case Double:
        if (value.isEmpty()) {
          return -1.00000001;
        }
        try {
          return Double.parseDouble(value);
        } catch(Exception e) {
          return defaultValue;
        }
      case String:
        if (value == null) {
          return (String)defaultValue;
        }
        return value;
      default:
        throw new Error("Unknown OptType");
    }
  }

  public void set(Object value) {
    if (value instanceof String) {
      setString((String)value);
      return;
    }
    switch(type_) {
      case Bool:
        if (!(value instanceof Boolean)) {
          throw new Error(String.format(
              "Value set is not a Boolean for %s: %s",
              name_, value.toString()));
        }
        value_ = value;
        break;
      case Int:
        if (!(value instanceof Integer)) {
          throw new Error(String.format(
              "Value set is not a Integer for %s: %s",
              name_, value.toString()));
        }
        value_ = value;
        break;
      case Double:
        if (!(value instanceof Double)) {
          throw new Error(String.format(
              "Value set is not a Double for %s: %s",
              name_, value.toString()));
        }
        value_ = value;
        break;
      case String:
      default:
        throw new Error("Unknown OptType");
    }
    Config.getStorage().set(name_, value_);
  }

  public void setString(String value) {
    value_ = convert(value, value_);
    Config.getStorage().set(name_, value_);
  }

  public String getName() {
    return name_;
  }

  public OptType getType() {
    return type_;
  }

  public Boolean getBool() {
    if (type_ != OptType.Bool) {
      throw new Error(String.format(
          "Config option %s not of type Boolean", name_));
    }
    return (Boolean)value_;
  }

  public Integer getInt() {
    if (type_ != OptType.Int) {
      throw new Error(String.format(
          "Config option %s not of type Integer", name_));
    }
    return (Integer)value_;
  }

  public Double getDouble() {
    if (type_ != OptType.Double) {
      throw new Error(String.format(
          "Config option %s not of type Double", name_));
    }
    return (Double)value_;
  }

  public String getString() {
    return value_.toString();
  }
}
