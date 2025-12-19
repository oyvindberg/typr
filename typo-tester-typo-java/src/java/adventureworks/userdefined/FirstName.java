package adventureworks.userdefined;

import typo.runtime.PgText;
import typo.runtime.PgType;
import typo.runtime.PgTypes;
import typo.runtime.internal.arrayMap;

public record FirstName(String value) {
  public static PgText<FirstName> pgText =
      PgText.instance((v, sb) -> PgText.textString.unsafeEncode(v.value(), sb));
  public static PgType<FirstName> pgType = PgTypes.text.bimap(FirstName::new, v -> v.value());
  public static PgType<FirstName[]> pgTypeArray =
      PgTypes.textArray.bimap(
          arr -> arrayMap.map(arr, FirstName::new, FirstName.class),
          arr -> arrayMap.map(arr, v -> v.value(), String.class));
}
