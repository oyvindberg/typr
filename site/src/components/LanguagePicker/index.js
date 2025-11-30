import React from "react";
import { useLanguage, LANGUAGES } from "../LanguageContext";
import styles from "./styles.module.css";

export default function LanguagePicker() {
  const { language, setLanguage } = useLanguage();

  return (
    <div className={styles.languagePicker}>
      <span className={styles.label}>Language:</span>
      <div className={styles.buttons}>
        {Object.entries(LANGUAGES).map(([key, { name }]) => (
          <button
            key={key}
            className={`${styles.button} ${language === key ? styles.active : ""}`}
            onClick={() => setLanguage(key)}
          >
            {name}
          </button>
        ))}
      </div>
    </div>
  );
}
