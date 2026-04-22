package ${PACKAGE}.domain.models.enums;

import com.eps.shared.models.enums.IEnum;

public enum ${ENTITY}Status implements IEnum {
  INACTIVE((byte) 0),
  ACTIVE((byte) 1),
  TEMPORARILY((byte) 2);

  ${ENTITY}Status(byte value) {
    this.value = value;
  }

  private final byte value;

  @Override
  public byte getValue() {
    return value;
  }
}