import React from "react";
import clsx from "clsx";
import Link from "@docusaurus/Link";
import useDocusaurusContext from "@docusaurus/useDocusaurusContext";
import Layout from "@theme/Layout";
import CodeBlock from "@theme/CodeBlock";
import HomepageFeatures from "@site/src/components/HomepageFeatures";
import FeatureShowcase from "@site/src/components/FeatureShowcase";
import WhyTypo from "@site/src/components/WhyTypo";
import TypoLogo from "@site/src/components/TypoLogo";
import { LanguageProvider } from "@site/src/components/LanguageContext";
import LanguagePicker from "@site/src/components/LanguagePicker";

import styles from "./index.module.css";

function HomepageHeader() {
    const {siteConfig} = useDocusaurusContext();
    return (
        <header className={clsx("hero", styles.heroBanner)}>
            <div className={styles.animatedBackground}>
                <div className={styles.floatingShape}></div>
                <div className={styles.floatingShape}></div>
                <div className={styles.floatingShape}></div>
                <div className={styles.gridOverlay}></div>
            </div>
            <div className="container">
                <div className={styles.heroGrid}>
                    <div className={styles.heroLeft}>
                        <div className={styles.logoSection}>
                            <TypoLogo size={80} animated={true} />
                            <h1 className={styles.brandName}>Typr</h1>
                        </div>
                        
                        <h2 className={styles.heroHeadline}>
                            The <span className={styles.highlight}>JVM + PostgreSQL</span> toolkit that
                            <br />
                            brings your database into your <span className={styles.highlight}>type system</span>.
                        </h2>

                        <p className={styles.heroSubtext}>
                            Typr generates type-safe database code for <strong>Scala</strong>, <strong>Java</strong>, and <strong>Kotlin</strong>.
                            Unprecedented type safety, a fantastic testing story, and a DSL so intuitive it feels like cheating.
                            Your database schema becomes your type system. Your domain model stays in sync automatically.
                        </p>

                        <div className={styles.valueProps}>
                            <div className={styles.valueProp}>
                                <span className={styles.valueText}><strong>Fantastic testing support</strong> - Both in-memory stubs and database test helpers</span>
                            </div>
                            <div className={styles.valueProp}>
                                <span className={styles.valueText}><strong>Composite keys done right</strong> - First-class support with type-safe helpers</span>
                            </div>
                            <div className={styles.valueProp}>
                                <span className={styles.valueText}><strong>"It just works"</strong> - Complex queries work correctly the first time</span>
                            </div>
                        </div>
                        
                        <div className={styles.buttonsContainer}>
                            <Link className="button button--primary button--lg" to="/docs/setup">
                                Start Building in 2 Minutes →
                            </Link>
                            <Link className="button button--outline button--lg" to="/docs">
                                See the Magic
                            </Link>
                        </div>
                        
                    </div>
                </div>
            </div>
        </header>
    );
}

export default function Home() {
    const {siteConfig} = useDocusaurusContext();
    return (
        <LanguageProvider>
            <Layout
                title="Typr"
                description="Type-safe PostgreSQL code generation for Scala, Java, and Kotlin"
            >
                <div className="mainContainer"></div>

                <HomepageHeader/>
                <main>
                    <div className={styles.languagePickerSection}>
                        <div className="container">
                            <LanguagePicker />
                        </div>
                    </div>
                    <HomepageFeatures/>
                    <FeatureShowcase/>
                    <WhyTypo/>
                </main>
            </Layout>
        </LanguageProvider>
    );
}
