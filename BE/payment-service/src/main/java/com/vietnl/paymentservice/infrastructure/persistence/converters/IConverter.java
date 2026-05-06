package com.vietnl.paymentservice.infrastructure.persistence.converters;

import com.vietnl.paymentservice.domain.models.enums.IEnum;
import jakarta.persistence.AttributeConverter;

import java.lang.reflect.ParameterizedType;

public abstract class IConverter<E extends Enum<E> & IEnum> implements AttributeConverter<E, Byte> {
    private final Class<E> enumClass;

    @SuppressWarnings("unchecked")
    protected IConverter() {
        this.enumClass = (Class<E>) ((ParameterizedType) getClass().getGenericSuperclass()).getActualTypeArguments()[0];
    }

    @Override
    public Byte convertToDatabaseColumn(E attribute) {
        if (attribute == null) return null;
        return attribute.getValue();
    }

    @Override
    public E convertToEntityAttribute(Byte dbData) {
        if (dbData == null) return null;
        for (E e : enumClass.getEnumConstants()) {
            if (e.getValue() == dbData) return e;
        }
        return null;
    }
}
