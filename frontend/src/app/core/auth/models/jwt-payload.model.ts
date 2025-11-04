export interface JwtPayload {
  sub?: string;          // Subject / Username
  exp?: number;          // Ablaufzeit (Epoch Seconds)
  roles?: string[];      // Benutzerrollen
  [key: string]: any;    // beliebige weitere Claims
}