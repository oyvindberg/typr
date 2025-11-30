import { usePluginData } from '@docusaurus/useGlobalData';

/**
 * Hook to access generated code from the frontpage schema.
 * Returns an object with code snippets organized by file path.
 *
 * Usage:
 *   const code = useGeneratedCode();
 *   const userRow = code['user/UserRow'];
 *   // userRow.scala, userRow.java
 */
export function useGeneratedCode() {
  try {
    return usePluginData('generated-code-plugin') || {};
  } catch (e) {
    // During SSR or if plugin is not loaded yet
    return {};
  }
}

/**
 * Get a specific code file by key.
 * Returns { scala, java } object with the code for each language.
 */
export function useCodeFile(key) {
  const code = useGeneratedCode();
  return code[key] || { scala: null, java: null };
}
