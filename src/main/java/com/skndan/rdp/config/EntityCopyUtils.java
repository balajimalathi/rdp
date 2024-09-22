package com.skndan.rdp.config;

import java.lang.reflect.Field;

import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class EntityCopyUtils {

    public <T> void copyProperties(T existingEntity, Object dto) {
        Field[] fields = dto.getClass().getDeclaredFields();
        for (Field field : fields) {
            try {
                field.setAccessible(true);
                Object value = field.get(dto);
                if (value != null) {
                    Field entityField = existingEntity.getClass().getDeclaredField(field.getName());
                    entityField.setAccessible(true);
                    entityField.set(existingEntity, value);
                }
            } catch (Exception e) {
                // Handle exceptions as needed
            }
        }
    }
}