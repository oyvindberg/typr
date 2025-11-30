import React from "react";
import CodeBlock from "@theme/CodeBlock";
import { useLanguage } from "../LanguageContext";
import { useGeneratedCode } from "../../hooks/useGeneratedCode";

/**
 * Component that displays actual generated code from Typo.
 * Uses the current language selection to show Scala or Java code.
 *
 * Props:
 * - fileKey: The key of the file to display (e.g., 'user/UserRow')
 * - extract: Optional function to extract a portion of the code
 * - fallback: Optional fallback code if generated code is not available
 */
export default function GeneratedCode({ fileKey, extract, fallback }) {
  const { language } = useLanguage();
  const generatedCode = useGeneratedCode();

  const codeFile = generatedCode[fileKey];

  let code;
  if (codeFile) {
    // For Kotlin, we don't have actual generated code yet, so fall back to Java-style
    const langCode = language === "kotlin" ? codeFile.java : codeFile[language];
    code = langCode || codeFile.scala;

    if (code && extract) {
      code = extract(code);
    }
  }

  if (!code && fallback) {
    code = typeof fallback === 'object' ? fallback[language] || fallback.scala : fallback;
  }

  if (!code) {
    return null;
  }

  return (
    <CodeBlock language={language === "kotlin" ? "java" : language}>
      {code}
    </CodeBlock>
  );
}

/**
 * Extract helper: gets only the class/trait/interface definition (first block)
 */
export function extractDefinition(code) {
  const lines = code.split('\n');
  const result = [];
  let braceCount = 0;
  let started = false;

  for (const line of lines) {
    // Look for class/trait/interface/record definition
    if (!started && (
      line.includes('case class ') ||
      line.includes('trait ') ||
      line.includes('class ') ||
      line.includes('interface ') ||
      line.includes('record ') ||
      line.includes('enum ')
    )) {
      started = true;
    }

    if (started) {
      result.push(line);
      braceCount += (line.match(/{/g) || []).length;
      braceCount -= (line.match(/}/g) || []).length;

      // For case classes without body, just capture until the closing paren
      if (line.includes('case class') && !line.includes('{')) {
        if (line.includes(')') && !line.includes('{')) {
          break;
        }
      }

      // End when we close the main block
      if (braceCount === 0 && result.length > 1) {
        break;
      }
    }
  }

  return result.join('\n');
}

/**
 * Extract helper: gets a method by name from a trait/interface
 */
export function extractMethods(code, methodNames) {
  const lines = code.split('\n');
  const result = [];

  for (let i = 0; i < lines.length; i++) {
    const line = lines[i];
    const matchesMethod = methodNames.some(name =>
      line.includes(`def ${name}`) ||
      line.includes(`${name}(`) ||
      line.includes(` ${name} `)
    );

    if (matchesMethod) {
      // Include the doc comment above if present
      let j = i - 1;
      while (j >= 0 && (lines[j].trim().startsWith('*') || lines[j].trim().startsWith('/'))) {
        j--;
      }
      for (let k = j + 1; k <= i; k++) {
        result.push(lines[k]);
      }

      // If the method spans multiple lines, include them
      let braceCount = 0;
      let k = i;
      do {
        if (k > i) result.push(lines[k]);
        braceCount += (lines[k].match(/\(/g) || []).length;
        braceCount -= (lines[k].match(/\)/g) || []).length;
        k++;
      } while (braceCount > 0 && k < lines.length);

      result.push('');
    }
  }

  return result.join('\n').trim();
}
