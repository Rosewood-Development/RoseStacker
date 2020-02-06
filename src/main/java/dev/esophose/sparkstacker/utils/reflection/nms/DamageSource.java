package dev.esophose.sparkstacker.utils.reflection.nms;

import dev.esophose.sparkstacker.utils.reflection.ReflectionUtils;

@SuppressWarnings("unchecked")
public class DamageSource {

    private static Class<Enum> class_DamageSource;

    static {
        class_DamageSource = (Class<Enum>) ReflectionUtils.getNMSClass("DamageSource");
    }

    private String value;

    public DamageSource(String value) {
        this.value = value;
    }

    public Object getNMS() {
        try {
            return class_DamageSource.getDeclaredField(this.value).get(null);
        } catch (ReflectiveOperationException e) {
            e.printStackTrace();
            return null;
        }
    }

}
