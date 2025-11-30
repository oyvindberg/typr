import React, { createContext, useContext, useState } from "react";

const LanguageContext = createContext({
  language: "scala",
  setLanguage: () => {},
});

export const LANGUAGES = {
  scala: { name: "Scala", icon: "S" },
  java: { name: "Java", icon: "J" },
  kotlin: { name: "Kotlin", icon: "K" },
};

export function LanguageProvider({ children }) {
  const [language, setLanguage] = useState("scala");

  return (
    <LanguageContext.Provider value={{ language, setLanguage }}>
      {children}
    </LanguageContext.Provider>
  );
}

export function useLanguage() {
  return useContext(LanguageContext);
}

export default LanguageContext;
