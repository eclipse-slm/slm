import type {NavItem} from '@/app/navigation';

/** Hook for consumers: shadow this file to add, remove or change drawer entries. */
export default function customizeNavigation(items: NavItem[]): NavItem[] {
    return items;
}
