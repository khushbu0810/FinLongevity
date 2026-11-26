export interface RegisterRequest {
    fullName: string;
    email: string;
    password: string;
}
export interface LoginRequest {
    email: string;
    password: string;
}
export interface UserResponse {
    id: number;
    fullName: string;
    email: string;
    role: string;
    createdAt: string;
}
export interface AuthResponse {
    accessToken: string;
    tokenType: string;
    expiresIn: number;
    user: UserResponse;
}