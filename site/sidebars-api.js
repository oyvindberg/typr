// @ts-check

/** @type {import('@docusaurus/plugin-content-docs').SidebarsConfig} */
const sidebars = {
    apiSidebar: [
        {
            type: "category",
            label: "Typo API",
            collapsible: true,
            collapsed: false,
            items: [
                {type: "doc", id: "index"},
                {type: "doc", id: "type-safe-ids"},
                {type: "doc", id: "response-types"},
                {type: "doc", id: "server-frameworks"},
                {type: "doc", id: "client-generation"},
                {type: "doc", id: "usage"},
            ],
        },
    ]
};

module.exports = sidebars;
