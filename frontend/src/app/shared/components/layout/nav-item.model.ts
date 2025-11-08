export type NavItem = {
  path: string;              // Router-Link
  label: string;             // i18n-Key oder Plaintext
  icon?: string;             // SVG path d="" oder Icon-Key
  exact?: boolean;           // routerLinkActiveOptions.exact
  external?: boolean;        // falls externer Link
  badge?: string;            // optionales kleines Badge
  children?: NavItem[];      // Subnav (optional)

  // Zugriffssteuerung
  requiredRoles?: string[];        // z.B. ['ADMIN']
  requiredPermissions?: string[];  // z.B. ['TICKET:READ']
  hidden?: boolean;                // per Feature-Flag ausblendbar
};