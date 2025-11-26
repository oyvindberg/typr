package adventureworks.userdefined;

import typo.runtime.PgText;
import typo.runtime.PgType;
import typo.runtime.PgTypes;
import typo.runtime.internal.arrayMap;

/** Type for the primary key of table `sales.creditcard` */
public record CustomCreditcardId(Integer value) {
  static public PgText<CustomCreditcardId> pgText = PgText.instance((v, sb) -> PgText.textInteger.unsafeEncode(v.value(), sb));
  static public PgType<CustomCreditcardId> pgType = PgTypes.int4.bimap(CustomCreditcardId::new, v -> v.value());
  static public PgType<CustomCreditcardId[]> pgTypeArray = PgTypes.int4Array.bimap(arr -> arrayMap.map(arr, CustomCreditcardId::new, CustomCreditcardId.class), arr -> arrayMap.map(arr, v -> v.value(), Integer.class));
}