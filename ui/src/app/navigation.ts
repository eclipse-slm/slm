export interface NavContext {
    roles: string[];
    isDeveloper: boolean;
}

export interface NavItem {
    id?: string;
    icon?: string;
    titleKey?: string;
    to?: string;
    group?: string;
    /** Defaults to true. */
    visible?: boolean | ((ctx: NavContext) => boolean);
    children?: NavItem[];
    /** Rendering hints consumed by the drawer components. */
    divider?: boolean;
    text?: boolean;
    subGroup?: boolean;
    goToChildOnClick?: number;
}

export type ResolvedNavItem = Omit<NavItem, 'visible' | 'titleKey' | 'children'> & {
    title?: string;
    visible: boolean;
    children?: ResolvedNavItem[];
};

const isAdmin = (ctx: NavContext) => ctx.roles.includes('slm-admin');

export const defaultNavigation: NavItem[] = [
    {
        id: 'main-menu-button-dashboard',
        icon: 'mdi-view-dashboard',
        titleKey: 'drawer.section.dashboard.title',
        to: '/dashboard',
        visible: true,
    },
    {
        id: 'main-menu-button-jobs',
        icon: 'mdi-account-hard-hat',
        titleKey: 'drawer.section.jobs.title',
        to: '/jobs',
        visible: true,
    },
    {
        id: 'main-menu-divider-resources',
        titleKey: 'drawer.section.resources.title',
        divider: true,
        visible: true,
    },
    {
        id: 'main-menu-button-resources',
        icon: 'mdi-desktop-classic',
        titleKey: 'drawer.section.resources.devices.title',
        text: true,
        group: '/resources',
        subGroup: true,
        visible: true,
        goToChildOnClick: 0,
        children: [
            {
                id: 'main-menu-button-admin-components',
                titleKey: 'drawer.section.resources.devices.instances.title',
                icon: 'mdi-menu-open',
                to: 'instances',
                visible: true,
            },
            {
                id: 'main-menu-button-admin-service-categories',
                titleKey: 'drawer.section.resources.devices.types.title',
                icon: 'mdi-format-list-group',
                to: 'types',
                visible: true,
            },
        ],
    },
    {
        id: 'main-menu-button-clusters',
        icon: 'mdi-server',
        titleKey: 'drawer.section.resources.clusters.title',
        to: '/clusters',
        visible: false,
    },
    {
        id: 'main-menu-button-discovery',
        icon: 'mdi-tab-search',
        titleKey: 'drawer.section.resources.discovery.title',
        group: '/discovery',
        subGroup: true,
        text: true,
        to: '/discovery/inbox',
        visible: true,
        goToChildOnClick: 0,
        children: [
            {
                id: 'main-menu-button-discovery-inbox',
                titleKey: 'drawer.section.resources.discovery.inbox.title',
                icon: 'mdi-tray-full',
                to: 'inbox',
                visible: true,
            },
            {
                id: 'main-menu-button-discovery-jobs',
                titleKey: 'drawer.section.resources.discovery.jobs.title',
                icon: 'mdi-file-tree',
                to: 'jobs',
                visible: true,
            },
            {
                id: 'main-menu-button-discovery-drivers',
                titleKey: 'drawer.section.resources.discovery.drivers.title',
                icon: 'mdi-magnify-scan',
                to: 'drivers',
                visible: true,
            },
        ],
    },
    {
        id: 'main-menu-divider-services',
        titleKey: 'drawer.section.services.title',
        divider: true,
        visible: true,
    },
    {
        id: 'main-menu-button-service-offering',
        titleKey: 'drawer.section.services.serviceOfferings.title',
        icon: 'mdi-offer',
        to: '/services/offerings',
        visible: true,
    },
    {
        id: 'main-menu-button-service-instances',
        titleKey: 'drawer.section.services.services.title',
        icon: 'mdi-apps',
        to: '/services/instances',
        visible: true,
    },
    {
        id: 'main-menu-button-service-vendors',
        titleKey: 'drawer.section.services.serviceVendor.title',
        icon: 'mdi-toolbox',
        to: '/services/vendors',
        visible: ctx => ctx.isDeveloper,
    },
    {
        id: 'main-menu-divider-admin',
        divider: true,
        visible: true,
    },
    {
        id: 'main-menu-button-admin',
        titleKey: 'drawer.section.admin.title',
        icon: 'mdi-shield-account-variant-outline',
        group: '/admin',
        subGroup: true,
        text: true,
        visible: isAdmin,
        children: [
            {
                id: 'main-menu-button-admin-components',
                titleKey: 'drawer.section.admin.components.title',
                icon: 'mdi-view-module',
                to: 'components',
                visible: isAdmin,
            },
            {
                id: 'main-menu-button-admin-service-categories',
                titleKey: 'drawer.section.admin.service-categories.title',
                icon: 'mdi-shape-outline',
                to: 'service-categories',
                visible: isAdmin,
            },
            {
                id: 'main-menu-button-admin-service-vendors',
                titleKey: 'drawer.section.admin.service-vendors.title',
                icon: 'mdi-treasure-chest',
                to: 'service-vendors',
                visible: isAdmin,
            },
            {
                id: 'main-menu-button-admin-users',
                // Original used the literal string 'User Management' (no i18n key); kept as-is.
                titleKey: 'User Management',
                icon: 'mdi-account-cog',
                to: 'users',
                visible: isAdmin,
            },
        ],
    },
];

export function resolveNavItems(
    items: NavItem[],
    ctx: NavContext,
    t: (key: string) => string,
): ResolvedNavItem[] {
    return items.map(({titleKey, visible, children, ...rest}) => ({
        ...rest,
        title: titleKey === undefined ? undefined : t(titleKey),
        visible: typeof visible === 'function' ? visible(ctx) : (visible ?? true),
        children: children ? resolveNavItems(children, ctx, t) : undefined,
    }));
}
