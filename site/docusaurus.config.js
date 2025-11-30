// @ts-check
// Note: type annotations allow type checking and IDEs autocompletion

/** @type {import('@docusaurus/types').Config} */
const config = {
  title: "Typr",
  tagline: "Typed postgres boilerplate generation. Hopes to avoid typos ",
  url: "https://typr.dev",
  baseUrl: "/",
  onBrokenLinks: "throw",
  onBrokenMarkdownLinks: "warn",
  favicon: "img/favicon.ico",
  
  // Modern browsers prefer SVG favicons
  headTags: [
    {
      tagName: 'link',
      attributes: {
        rel: 'icon',
        type: 'image/svg+xml',
        href: '/img/favicon.svg',
      },
    },
  ],

  // GitHub pages deployment config.
  // If you aren't using GitHub pages, you don't need these.
  organizationName: "oyvindberg", // Usually your GitHub org/user name.
  projectName: "typr", // Usually your repo name.
  trailingSlash: true,

  // Even if you don't use internalization, you can use this field to set useful
  // metadata like html lang. For example, if your site is Chinese, you may want
  // to replace "en" with "zh-Hans".
  i18n: {
    defaultLocale: "en",
    locales: ["en"],
  },

  plugins: [
    require.resolve('./plugins/generated-code-plugin'),
  ],

  presets: [
    [
      "classic",
      /** @type {import('@docusaurus/preset-classic').Options} */
      ({
        docs: {
          sidebarPath: require.resolve("./sidebars.js"),
        },
        theme: {
          customCss: require.resolve("./src/css/custom.css"),
        },
      }),
    ],
  ],
  clientModules: [
    require.resolve('./tracker.js'),
  ],
  themeConfig:
    /** @type {import('@docusaurus/preset-classic').ThemeConfig} */
    ({
      navbar: {
        title: "Typr",
        logo: {
          alt: "Typr",
          src: "img/logo.svg",
        },
        items: [
          {
            type: "doc",
            docId: "readme",
            position: "left",
            label: "Documentation",
          },
          {to: 'blog', label: 'Blog', position: 'left'}, // or position: 'right'
          {
            href: "https://github.com/oyvindberg/typr",
            label: "GitHub",
            position: "right",
          },
        ],
      },
      footer: {
        style: "dark",
        links: [
          {
            title: "Links",
            items: [
              {
                label: "Github",
                to: "https://github.com/oyvindberg/typr",
              },
            ],
          },
        ],
        copyright: `Copyright © ${new Date().getFullYear()} Øyvind Raddum Berg`,
      },
      prism: {
        theme: require("prism-react-renderer").themes.github,
        darkTheme: require("prism-react-renderer").themes.dracula,
        additionalLanguages: ["java", "scala", "yaml", "sql"],
      },
    }),
};

module.exports = config;
