package com.vietnl.sharedlibrary.core.valueobject;

import com.eps.shared.core.utils.EnumUtils;
import lombok.experimental.ExtensionMethod;

@ExtensionMethod(EnumUtils.class)
public interface IEnum {
  byte getValue();
}
