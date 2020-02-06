package dev.esophose.sparkstacker.utils.reflection.nms;

import dev.esophose.sparkstacker.utils.reflection.ReflectionUtils;

@SuppressWarnings("unchecked")
public class EnumMobSpawn {

    private static final Class<Enum> class_EnumMobSpawn;

    static {
        class_EnumMobSpawn = (Class<Enum>) ReflectionUtils.getNMSClass("EnumMobSpawn");
    }

    private String value;

    public EnumMobSpawn(String value) {
        this.value = value;
    }

    public Enum<?> getNMS() {
        return Enum.valueOf(class_EnumMobSpawn, this.value);
    }

}
