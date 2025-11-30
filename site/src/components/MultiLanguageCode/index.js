import React from "react";
import CodeBlock from "@theme/CodeBlock";
import { useLanguage } from "../LanguageContext";

export default function MultiLanguageCode({ code, language: overrideLanguage }) {
  const { language } = useLanguage();
  const selectedLang = overrideLanguage || language;

  const codeContent = code[selectedLang] || code.scala || "";
  const codeLanguage = selectedLang === "kotlin" ? "kotlin" : selectedLang;

  if (!codeContent) {
    return null;
  }

  return (
    <CodeBlock language={codeLanguage}>
      {codeContent}
    </CodeBlock>
  );
}
