/**
 * Request sent during login.
 */
export interface AuthenticationRequest {
  username: string;
  password: string;
}

/**
 * Request for user registration.
 */
export interface RegistrationRequest {
  username: string;
  email: string;
  password: string;
}

/**
 * Response returned from the backend after successful authentication.
 * 
 * - `accessToken`: the JWT used for API calls
 * - `refreshToken`: optional, for silent reauthentication
 * - `tokenType`: usually `"Bearer"`
 * - `expiresIn`: optional, seconds until expiration (client convenience)
 */
export interface AuthenticationResponse {
  accessToken: string;
  refreshToken?: string;
  tokenType?: string;
  expiresIn?: number;
}

/**
 * Decoded payload of the JWT token.
 */
export interface JwtPayload {
  sub?: string;          // Subject / username
  exp?: number;          // Expiration (epoch seconds)
  iat?: number;          // Issued at
  iss?: string;          // Issuer
  roles?: string[];      // User roles
  email?: string;        // Optional email claim
  [key: string]: any;    // Allow any additional custom claims
}
